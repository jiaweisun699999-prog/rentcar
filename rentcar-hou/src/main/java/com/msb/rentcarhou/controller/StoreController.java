package com.msb.rentcarhou.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.msb.rentcarhou.common.result.Result;
import com.msb.rentcarhou.dto.StoreQueryDto;
import com.msb.rentcarhou.dto.StoreSaveDto;
import com.msb.rentcarhou.service.StoreInfoService;
import com.msb.rentcarhou.vo.StoreInfoVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
@CrossOrigin
public class StoreController {

    /**
     * 门店业务层对象，由 Spring 通过构造方法自动注入。
     */
    private final StoreInfoService storeInfoService;

    /**
     * 分页查询门店列表。
     *
     * @param queryDto 查询条件，包含页码、每页条数和城市名称
     * @return 分页后的门店展示数据
     */
    @GetMapping("/list")
    public Result<Page<StoreInfoVo>> list(StoreQueryDto queryDto) {
        try {
            Page<StoreInfoVo> page = storeInfoService.getStoreList(queryDto);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 新增门店。
     *
     * @param saveDto 新增门店参数，包含商户名称、城市、地址和是否支持送车上门
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody StoreSaveDto saveDto) {
        try {
            storeInfoService.addStore(saveDto);
            return Result.success("新增门店成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改门店。
     *
     * @param saveDto 修改门店参数，必须包含门店 ID
     * @return 修改结果
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody StoreSaveDto saveDto) {
        try {
            storeInfoService.updateStore(saveDto);
            return Result.success("修改门店成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据门店 ID 删除门店。
     *
     * @param id 门店 ID，来自请求路径
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            storeInfoService.deleteStore(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
