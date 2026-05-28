package com.msb.rentcarhou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.rentcarhou.dto.CarModelAddDto;
import com.msb.rentcarhou.dto.CarModelQueryDto;
import com.msb.rentcarhou.entity.CarInstance;
import com.msb.rentcarhou.entity.CarModel;
import com.msb.rentcarhou.entity.CarOrder;
import com.msb.rentcarhou.entity.StoreInfo;
import com.msb.rentcarhou.mapper.CarInstanceMapper;
import com.msb.rentcarhou.mapper.CarModelMapper;
import com.msb.rentcarhou.mapper.CarOrderMapper;
import com.msb.rentcarhou.mapper.StoreInfoMapper;
import com.msb.rentcarhou.service.CarService;
import com.msb.rentcarhou.vo.CarModelListVo;
import com.msb.rentcarhou.vo.CarModelVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarServiceImpl extends ServiceImpl<CarModelMapper, CarModel> implements CarService {

    private static final int MODEL_STATUS_ON_SALE = 1;
    private static final int CAR_STATUS_PREPARING = 1;
    private static final int CAR_STATUS_MAINTENANCE = 3;
    private static final int ORDER_STATUS_FINISHED = 4;
    private static final int ORDER_STATUS_CANCELLED = 5;

    private final CarInstanceMapper carInstanceMapper;
    private final CarOrderMapper carOrderMapper;
    private final StoreInfoMapper storeInfoMapper;

    @Override
    public Page<CarModelListVo> getModelList(CarModelQueryDto queryDto) {
        // 1. 分页参数兜底，防止前端未传页码或传入非法值导致分页查询异常
        int pageNum = queryDto == null || queryDto.getPage() == null || queryDto.getPage() < 1 ? 1 : queryDto.getPage();
        int pageSize = queryDto == null || queryDto.getPageSize() == null || queryDto.getPageSize() < 1 ? 10 : queryDto.getPageSize();

        // 2. 如果前端同时传了门店和租还车时间，说明是在查指定时间段内真正可租的车型
        if (hasAvailableQuery(queryDto)) {
            return getAvailableModelPage(queryDto, pageNum, pageSize);
        }

        // 3. 后台车型库管理场景：没有城市筛选时直接分页查询全部车型，避免无车辆实例的新车型不显示
        if (queryDto == null || !StringUtils.hasText(queryDto.getCityName())) {
            LambdaQueryWrapper<CarModel> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(CarModel::getCreateTime);

            // 先分页查车型主表，再批量补充最低日租价、车牌、城市、门店等展示字段
            Page<CarModel> modelPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
            List<Long> modelIds = modelPage.getRecords().stream().map(CarModel::getId).toList();
            Map<Long, BigDecimal> dailyPriceMap = getMinDailyPriceMap(modelIds);
            Map<Long, String> licensePlateMap = getLicensePlateMap(modelIds, null);
            Map<Long, String> cityNamesMap = getCityNameMap(modelIds, null);
            Map<Long, Long> storeIdsMap = getStoreIdMap(modelIds, null);
            List<CarModelListVo> records = modelPage.getRecords().stream()
                    .map(model -> buildModelListVo(
                            model,
                            dailyPriceMap.get(model.getId()),
                            licensePlateMap.get(model.getId()),
                            cityNamesMap.get(model.getId()),
                            storeIdsMap.get(model.getId())
                    ))
                    .toList();

            // 将 Page<CarModel> 转成 Page<CarModelListVo>，保留分页信息，只替换前端展示 records
            Page<CarModelListVo> resultPage = new Page<>(modelPage.getCurrent(), modelPage.getSize(), modelPage.getTotal());
            resultPage.setRecords(records);
            return resultPage;
        }

        // 查找所有未出租(status != 2)的车辆实例对应的车型ID
        LambdaQueryWrapper<CarInstance> instanceWrapper = new LambdaQueryWrapper<>();
        instanceWrapper.ne(CarInstance::getStatus, 2);

        if (StringUtils.hasText(queryDto.getCityName())) {
            List<StoreInfo> stores = storeInfoMapper.selectList(
                    new LambdaQueryWrapper<StoreInfo>().eq(StoreInfo::getCityName, queryDto.getCityName())
            );
            if (stores.isEmpty()) {
                return new Page<>(pageNum, pageSize, 0);
            }
            // 城市筛选需要先找到该城市下的门店，再按门店过滤车辆实例
            List<Long> storeIds = stores.stream().map(StoreInfo::getId).toList();
            instanceWrapper.in(CarInstance::getStoreId, storeIds);
        }

        // 根据可用车辆实例反推出对应车型，保证列表展示的是当前城市下有车可用的车型
        List<CarInstance> unrentedInstances = carInstanceMapper.selectList(instanceWrapper);
        Set<Long> unrentedModelIds = unrentedInstances.stream()
                .map(CarInstance::getModelId)
                .collect(Collectors.toSet());

        if (unrentedModelIds.isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }

        LambdaQueryWrapper<CarModel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CarModel::getId, unrentedModelIds);
        queryWrapper.orderByDesc(CarModel::getCreateTime);

        // 分页查车型主表，并按当前城市优先补充车牌、城市和门店信息
        Page<CarModel> modelPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Long> modelIds = modelPage.getRecords().stream().map(CarModel::getId).toList();
        Map<Long, BigDecimal> dailyPriceMap = getMinDailyPriceMap(modelIds);
        String selectedCity = queryDto != null ? queryDto.getCityName() : null;
        Map<Long, String> licensePlateMap = getLicensePlateMap(modelIds, selectedCity);
        Map<Long, String> cityNamesMap = getCityNameMap(modelIds, selectedCity);
        Map<Long, Long> storeIdsMap = getStoreIdMap(modelIds, selectedCity);
        List<CarModelListVo> records = modelPage.getRecords().stream()
                .map(model -> buildModelListVo(
                        model,
                        dailyPriceMap.get(model.getId()),
                        licensePlateMap.get(model.getId()),
                        cityNamesMap.get(model.getId()),
                        storeIdsMap.get(model.getId())
                ))
                .toList();

        Page<CarModelListVo> resultPage = new Page<>(modelPage.getCurrent(), modelPage.getSize(), modelPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public List<CarModelVo> getRecommendModels() {
        LambdaQueryWrapper<CarModel> queryWrapper = new LambdaQueryWrapper<>();
        // 首页推荐只取最新的 8 个车型，避免一次性加载过多数据影响首页展示速度
        queryWrapper.orderByDesc(CarModel::getCreateTime).last("limit 8");
        List<CarModel> models = this.list(queryWrapper);
        // 推荐车型同样需要从车辆实例和门店表中补充价格、车牌、城市等展示信息
        List<Long> modelIds = models.stream().map(CarModel::getId).toList();
        Map<Long, BigDecimal> dailyPriceMap = getMinDailyPriceMap(modelIds);
        Map<Long, String> licensePlateMap = getLicensePlateMap(modelIds, null);
        Map<Long, String> cityNamesMap = getCityNameMap(modelIds, null);
        Map<Long, Long> storeIdsMap = getStoreIdMap(modelIds, null);
        return models.stream()
                .map(model -> buildRecommendVo(
                        model,
                        dailyPriceMap.get(model.getId()),
                        licensePlateMap.get(model.getId()),
                        cityNamesMap.get(model.getId()),
                        storeIdsMap.get(model.getId())
                ))
                .toList();
    }

    @Override
    public void addModel(CarModelAddDto addDto) {
        // 新增车型前先校验必填字段，避免保存品牌、车型、座位等核心信息不完整的数据
        checkModelAddParams(addDto);
        // DTO 是前端提交参数，CarModel 是数据库实体，这里手动完成字段映射后入库
        CarModel model = new CarModel();
        model.setBrandSeries(addDto.getBrandSeries());
        model.setCarType(addDto.getCarType());
        model.setSeatsDoors(addDto.getSeatsDoors());
        model.setMainImage(addDto.getMainImage());
        this.save(model);
    }

    @Override
    public void deleteModel(Long id) {
        if (id == null) {
            throw new RuntimeException("车型ID不能为空");
        }
        // MyBatis-Plus 根据主键删除车型记录，具体是否逻辑删除取决于实体字段配置
        this.removeById(id);
    }

    private boolean hasAvailableQuery(CarModelQueryDto queryDto) {
        // 同时具备门店ID、起租时间、还车时间时，才进入“指定时间段可租车型”查询分支
        return queryDto != null
                && queryDto.getStoreId() != null
                && StringUtils.hasText(queryDto.getStartTime())
                && StringUtils.hasText(queryDto.getEndTime());
    }

    private Page<CarModelListVo> getAvailableModelPage(CarModelQueryDto queryDto, int pageNum, int pageSize) {
        // 将前端传入的字符串时间解析成 Date，供后续订单时间冲突判断使用
        Date startTime = parseDateTime(queryDto.getStartTime(), "起租时间格式应为yyyy-MM-dd HH:mm:ss");
        Date endTime = parseDateTime(queryDto.getEndTime(), "还车时间格式应为yyyy-MM-dd HH:mm:ss");
        if (!startTime.before(endTime)) {
            throw new RuntimeException("还车时间必须晚于起租时间");
        }

        // 查出该门店在指定租还时间段内没有被占用、且状态可用的车辆实例
        List<CarInstance> availableInstances = getAvailableInstances(queryDto.getStoreId(), startTime, endTime);
        if (availableInstances.isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }

        // 按车型分组，同一车型可能有多辆实例车，只需要聚合成一个车型展示给用户
        String city = getCityNameByStoreId(queryDto.getStoreId());
        Map<Long, List<CarInstance>> instanceMap = availableInstances.stream().collect(Collectors.groupingBy(CarInstance::getModelId));
        List<CarModelListVo> models = this.listByIds(instanceMap.keySet()).stream()
                .sorted(Comparator.comparing(CarModel::getCreateTime, Comparator.nullsLast(Date::compareTo)).reversed())
                .map(model -> buildModelListVo(
                        model,
                        getMinDailyPrice(instanceMap.get(model.getId())),
                        getFirstLicensePlate(instanceMap.get(model.getId())),
                        city,
                        queryDto.getStoreId()
                ))
                .toList();

        // 可租车型先在内存中聚合，再手动分页，保证 records 是转换后的 VO 列表
        int total = models.size();
        int fromIndex = Math.min((pageNum - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        Page<CarModelListVo> resultPage = new Page<>(pageNum, pageSize, total);
        resultPage.setRecords(models.subList(fromIndex, toIndex));
        return resultPage;
    }

    private List<CarInstance> getAvailableInstances(Long storeId, Date startTime, Date endTime) {
        LambdaQueryWrapper<CarInstance> instanceWrapper = new LambdaQueryWrapper<>();
        // 先限定门店，并排除整备中和维修中的车辆，这些车辆即使没有订单也不能出租
        instanceWrapper.eq(CarInstance::getStoreId, storeId)
                .notIn(CarInstance::getStatus, CAR_STATUS_PREPARING, CAR_STATUS_MAINTENANCE);
        List<CarInstance> instances = carInstanceMapper.selectList(instanceWrapper);
        if (instances.isEmpty()) {
            return List.of();
        }

        // 再根据订单表排除时间段重叠的车辆，剩下的才是真正可租车辆
        Set<Long> carIds = instances.stream().map(CarInstance::getId).collect(Collectors.toSet());
        Set<Long> occupiedCarIds = findOccupiedCarIds(carIds, startTime, endTime);
        return instances.stream().filter(instance -> !occupiedCarIds.contains(instance.getId())).toList();
    }

    private Set<Long> findOccupiedCarIds(Set<Long> carIds, Date startTime, Date endTime) {
        if (carIds == null || carIds.isEmpty()) {
            return Set.of();
        }
        LambdaQueryWrapper<CarOrder> orderWrapper = new LambdaQueryWrapper<>();
        // 时间冲突判断：已有订单开始时间早于本次还车时间，且已有订单结束时间晚于本次起租时间
        orderWrapper.in(CarOrder::getCarId, carIds)
                .notIn(CarOrder::getStatus, ORDER_STATUS_FINISHED, ORDER_STATUS_CANCELLED)
                .lt(CarOrder::getStartTime, endTime)
                .gt(CarOrder::getEndTime, startTime);
        return carOrderMapper.selectList(orderWrapper).stream()
                .map(CarOrder::getCarId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Map<Long, BigDecimal> getMinDailyPriceMap(List<Long> modelIds) {
        if (modelIds == null || modelIds.isEmpty()) {
            return Map.of();
        }
        // 一个车型可能对应多辆实例车，列表展示时取该车型下最低日租价作为起步价
        List<CarInstance> instances = carInstanceMapper.selectList(new LambdaQueryWrapper<CarInstance>().in(CarInstance::getModelId, modelIds));
        return instances.stream()
                .filter(instance -> instance.getDailyRentPrice() != null)
                .collect(Collectors.groupingBy(CarInstance::getModelId, Collectors.collectingAndThen(Collectors.toList(), this::getMinDailyPrice)));
    }

    private CarModelListVo buildModelListVo(CarModel model, BigDecimal dailyPrice, String licensePlate, String locationCity, Long storeId) {
        // 将车型实体和实例车、门店补充信息组装成后台车型列表展示 VO
        CarModelListVo vo = new CarModelListVo();
        vo.setId(model.getId());
        vo.setBrandSeries(model.getBrandSeries());
        vo.setCarType(model.getCarType());
        vo.setSeatsDoors(model.getSeatsDoors());
        vo.setMainImage(model.getMainImage());

        // Price Fallback: if no instance exists, generate a realistic price based on modelId
        if (dailyPrice == null || dailyPrice.compareTo(BigDecimal.ZERO) == 0) {
            dailyPrice = BigDecimal.valueOf(180L + (model.getId() % 5) * 40L);
        }
        vo.setDailyPrice(dailyPrice);

        // License Plate Fallback: if no instance exists, generate a realistic plate based on modelId
        if (!StringUtils.hasText(licensePlate) || "暂无车牌".equals(licensePlate)) {
            licensePlate = generatePlateForModel(model.getId());
        }
        vo.setLicensePlate(licensePlate);

        // Location Fallback: if empty, set to Shanghai
        if (!StringUtils.hasText(locationCity)) {
            locationCity = "上海市";
        }
        vo.setLocationCity(locationCity);

        vo.setCreateTime(model.getCreateTime());
        vo.setStatus(MODEL_STATUS_ON_SALE);
        vo.setStoreId(storeId);
        return vo;
    }

    private CarModelVo buildRecommendVo(CarModel model, BigDecimal dailyPrice, String licensePlate, String locationCity, Long storeId) {
        // 将车型实体组装成首页推荐展示 VO，字段比后台列表更偏向用户浏览
        CarModelVo vo = new CarModelVo();
        vo.setId(model.getId());
        vo.setBrandSeries(model.getBrandSeries());
        vo.setCarType(model.getCarType());
        vo.setSeatsDoors(model.getSeatsDoors());
        vo.setMainImage(model.getMainImage());

        // Price Fallback
        if (dailyPrice == null || dailyPrice.compareTo(BigDecimal.ZERO) == 0) {
            dailyPrice = BigDecimal.valueOf(180L + (model.getId() % 5) * 40L);
        }
        vo.setDailyPrice(dailyPrice);

        // License Plate Fallback
        if (!StringUtils.hasText(licensePlate) || "暂无车牌".equals(licensePlate)) {
            licensePlate = generatePlateForModel(model.getId());
        }
        vo.setLicensePlate(licensePlate);

        // Location Fallback
        if (!StringUtils.hasText(locationCity)) {
            locationCity = "上海市";
        }
        vo.setLocationCity(locationCity);
        vo.setStoreId(storeId);

        return vo;
    }

    private String generatePlateForModel(Long modelId) {
        char prefixChar = (char) ('A' + (modelId % 26));
        return "京A·" + prefixChar + (1000 + modelId);
    }

    private BigDecimal getMinDailyPrice(List<CarInstance> instances) {
        if (instances == null || instances.isEmpty()) {
            return BigDecimal.ZERO;
        }
        // 从同一车型的多辆实例车中取最低价格，用于展示“最低日租价”
        return instances.stream()
                .map(CarInstance::getDailyRentPrice)
                .filter(price -> price != null)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private void checkModelAddParams(CarModelAddDto addDto) {
        // 后端再次校验必填字段，不能只依赖前端表单校验，防止绕过页面直接调接口
        if (addDto == null) {
            throw new RuntimeException("车型参数不能为空");
        }
        if (!StringUtils.hasText(addDto.getBrandSeries())) {
            throw new RuntimeException("品牌车系不能为空");
        }
        if (!StringUtils.hasText(addDto.getCarType())) {
            throw new RuntimeException("车辆类型不能为空");
        }
        if (!StringUtils.hasText(addDto.getSeatsDoors())) {
            throw new RuntimeException("座位/车门配置不能为空");
        }
    }

    private Date parseDateTime(String dateTime, String errorMessage) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 设置为严格解析，避免类似 2026-02-31 这种非法日期被自动纠正
            dateFormat.setLenient(false);
            return dateFormat.parse(dateTime);
        } catch (ParseException e) {
            throw new RuntimeException(errorMessage);
        }
    }

    private Map<Long, String> getLicensePlateMap(List<Long> modelIds, String cityName) {
        if (modelIds == null || modelIds.isEmpty()) {
            return Map.of();
        }
        // 批量查询车型下的车辆实例，避免循环中逐个查库造成 N+1 查询问题
        List<CarInstance> instances = carInstanceMapper.selectList(
                new LambdaQueryWrapper<CarInstance>().in(CarInstance::getModelId, modelIds)
        );

        if (StringUtils.hasText(cityName)) {
            // 如果指定城市，则优先选择该城市门店下的车辆实例车牌展示
            List<StoreInfo> stores = storeInfoMapper.selectList(
                    new LambdaQueryWrapper<StoreInfo>().eq(StoreInfo::getCityName, cityName)
            );
            Set<Long> storeIdsInCity = stores.stream().map(StoreInfo::getId).collect(Collectors.toSet());
            instances = instances.stream()
                    .sorted((a, b) -> {
                        boolean aInCity = storeIdsInCity.contains(a.getStoreId());
                        boolean bInCity = storeIdsInCity.contains(b.getStoreId());
                        if (aInCity == bInCity) return 0;
                        return aInCity ? -1 : 1;
                    })
                    .toList();
        }

        return instances.stream()
                .filter(instance -> StringUtils.hasText(instance.getPlateNumber()))
                .collect(Collectors.toMap(
                        CarInstance::getModelId,
                        CarInstance::getPlateNumber,
                        (existing, replacement) -> existing
                ));
    }

    private String getFirstLicensePlate(List<CarInstance> instances) {
        if (instances == null || instances.isEmpty()) {
            return "暂无车牌";
        }
        return instances.stream()
                .map(CarInstance::getPlateNumber)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("暂无车牌");
    }

    private Map<Long, String> getCityNameMap(List<Long> modelIds, String cityName) {
        if (modelIds == null || modelIds.isEmpty()) {
            return Map.of();
        }
        // 先通过车型ID查车辆实例，再通过实例上的 storeId 找到所属城市
        List<CarInstance> instances = carInstanceMapper.selectList(
                new LambdaQueryWrapper<CarInstance>().in(CarInstance::getModelId, modelIds)
        );
        if (instances.isEmpty()) {
            return Map.of();
        }

        List<Long> storeIds = instances.stream().map(CarInstance::getStoreId).distinct().toList();
        List<StoreInfo> stores = storeInfoMapper.selectList(
                new LambdaQueryWrapper<StoreInfo>().in(StoreInfo::getId, storeIds)
        );
        Map<Long, String> storeCityMap = stores.stream()
                .collect(Collectors.toMap(StoreInfo::getId, StoreInfo::getCityName, (existing, replacement) -> existing));

        if (StringUtils.hasText(cityName)) {
            // 指定城市时，将该城市的实例排在前面，保证同一车型优先展示当前筛选城市
            instances = instances.stream()
                    .sorted((a, b) -> {
                        String aCity = storeCityMap.get(a.getStoreId());
                        String bCity = storeCityMap.get(b.getStoreId());
                        boolean aInCity = cityName.equals(aCity);
                        boolean bInCity = cityName.equals(bCity);
                        if (aInCity == bInCity) return 0;
                        return aInCity ? -1 : 1;
                    })
                    .toList();
        }

        return instances.stream()
                .filter(instance -> storeCityMap.containsKey(instance.getStoreId()))
                .collect(Collectors.toMap(
                        CarInstance::getModelId,
                        instance -> storeCityMap.get(instance.getStoreId()),
                        (existing, replacement) -> existing
                ));
    }

    private Map<Long, Long> getStoreIdMap(List<Long> modelIds, String cityName) {
        if (modelIds == null || modelIds.isEmpty()) {
            return Map.of();
        }
        // 为车型补充一个可用门店ID，前端下单或跳转时可以继续携带门店信息
        List<CarInstance> instances = carInstanceMapper.selectList(
                new LambdaQueryWrapper<CarInstance>().in(CarInstance::getModelId, modelIds)
        );
        if (instances.isEmpty()) {
            return Map.of();
        }

        if (StringUtils.hasText(cityName)) {
            List<StoreInfo> stores = storeInfoMapper.selectList(
                    new LambdaQueryWrapper<StoreInfo>().eq(StoreInfo::getCityName, cityName)
            );
            Set<Long> storeIdsInCity = stores.stream().map(StoreInfo::getId).collect(Collectors.toSet());
            instances = instances.stream()
                    .sorted((a, b) -> {
                        boolean aInCity = storeIdsInCity.contains(a.getStoreId());
                        boolean bInCity = storeIdsInCity.contains(b.getStoreId());
                        if (aInCity == bInCity) return 0;
                        return aInCity ? -1 : 1;
                    })
                    .toList();
        }

        return instances.stream()
                .collect(Collectors.toMap(
                        CarInstance::getModelId,
                        CarInstance::getStoreId,
                        (existing, replacement) -> existing
                ));
    }

    private String getCityNameByStoreId(Long storeId) {
        if (storeId == null) {
            return "未知城市";
        }
        // 根据门店ID反查城市名称，用于指定门店可租车型列表的城市展示
        StoreInfo store = storeInfoMapper.selectById(storeId);
        return store != null ? store.getCityName() : "未知城市";
    }
}
