# 悟空租车 Backend API 标准文档

为了保证前后端分离开发的顺利进行，请各位后端组员严格遵循以下接口标准和数据格式。

## 1. 统一接口返回格式 (Result Entity)

所有后端接口 **必须** 返回统一的 JSON 结构：

```json
{
  "code": 200,      // 业务状态码：200代表成功，401代表未登录，403无权限，500服务器异常，其他自定义
  "msg": "操作成功",  // 提示信息，前端会根据报错直接展示该字段
  "data": {         // 具体的业务数据（对象或数组），如果失败可以为 null
    // ...
  }
}
```

### 1.1 分页数据统一格式 (PageResult)

当返回列表数据涉及分页时，`data` 字段内的结构应统一为：

```json
{
  "total": 100,       // 总条数
  "records": [        // 当前页数据列表
    { "id": 1, "name": "..." },
    { "id": 2, "name": "..." }
  ]
}
```

---

## 2. 核心模块 API 划分 (分配给 5 位组员)

请各位组员在各自的 Spring Boot Controller 中实现以下接口，并在类上使用 `@RequestMapping("/api/模块名前缀")`。

### 模块 A：用户与风控中心 (`/api/user`)
*负责：注册登录、个人信息、实名认证、信用积分*

1.  **用户登录/注册**
    *   **POST** `/api/user/login`
    *   **Req**: `{ "phone": "13800000000", "password": "xxx" }` (或者验证码登录)
    *   **Res Data**: `{ "token": "jwt_string", "userInfo": { "id": 1, "phone": "...", "role": 0 } }`
2.  **获取当前登录用户信息**
    *   **GET** `/api/user/info`
    *   **Header**: `Authorization: Bearer <token>`
    *   **Res Data**: `{ "id": 1, "phone": "...", "creditScore": 750 }`
3.  **提交实名认证**
    *   **POST** `/api/user/certification`
    *   **Req**: `{ "idCardNo": "...", "driverLicenseUrl": "..." }`
4.  **审核实名认证 (管理端)**
    *   **POST** `/api/user/certification/audit`
    *   **Req**: `{ "userId": 1, "status": 2 }`

### 模块 B：商户与门店服务 (`/api/store`)
*负责：门店查询、位置、商户入驻*

1.  **获取支持的城市列表**
    *   **GET** `/api/store/cities`
    *   **Res Data**: `["北京", "上海", "广州", "成都"]`
2.  **根据城市/经纬度获取门店列表**
    *   **GET** `/api/store/list?cityName=北京&lat=xxx&lng=yyy`
    *   **Res Data**: `[{ "id": 1, "merchantName": "飞猪", "address": "...", "distance": "1.5km", "isSupportDelivery": 1 }]`
3.  **获取门店详情**
    *   **GET** `/api/store/detail/{id}`
    *   **Res Data**: 门店详细信息对象
4.  **门店CRUD (管理端)**
    *   **POST/PUT/DELETE** `/api/store`

### 模块 C：车辆资产与库存 (`/api/car`)
*负责：车型展示、库存防超卖查询、状态流转*

1.  **查询车型列表 (带综合筛选)**
    *   **GET** `/api/car/model/list?carType=SUV&page=1&size=10`
    *   **Res Data**: PageResult (含车型基础信息、主图、配置)
2.  **查询门店某时间段内的可用车辆 SKU** (重点+难点)
    *   **GET** `/api/car/instance/available?storeId=1&startTime=2024-05-01 10:00:00&endTime=2024-05-03 10:00:00&modelId=5`
    *   **说明**：必须结合 `car_order` 表，排除被占用的车辆。
    *   **Res Data**: `[{ "carId": 12, "plateNumber": "京A88888", "dailyRentPrice": 200.00 }]`
3.  **车辆库存录入与状态维护 (管理端)**
    *   **POST** `/api/car/instance`
    *   **PUT** `/api/car/instance/status` (修改车辆状态：空闲/维修中等)

### 模块 D：核心交易订单枢纽 (`/api/order`)
*负责：下单、计费、订单状态机*

1.  **费用试算 (预估价格)**
    *   **POST** `/api/order/calculate`
    *   **Req**: `{ "carId": 12, "pickupStoreId": 1, "dropoffStoreId": 1, "startTime": "...", "endTime": "..." }`
    *   **Res Data**: `{ "rentDays": 2, "rentFee": 400, "basicInsuranceFee": 100, "handlingFee": 35, "totalAmount": 535 }`
2.  **提交订单 (下单核心)** (重点+难点)
    *   **POST** `/api/order/create`
    *   **Req**: 同试算接口
    *   **说明**：需使用事务，并锁住车辆防止并发超卖。生成全局唯一 `orderNo`。
    *   **Res Data**: `{ "orderNo": "R20240522xxxx", "totalAmount": 535 }`
3.  **查询我的订单列表**
    *   **GET** `/api/order/my_list?status=1&page=1&size=10`
    *   **Res Data**: PageResult
4.  **取消订单/确认还车**
    *   **POST** `/api/order/cancel/{orderNo}`
    *   **POST** `/api/order/finish/{orderNo}` (流转状态并通知库存模块)

### 模块 E：财务与支付清算 (`/api/finance`)
*负责：支付流水、免押金扣减*

1.  **发起支付请求 (收银台)**
    *   **POST** `/api/finance/pay`
    *   **Req**: `{ "orderNo": "R20240522xxxx", "payMethod": "WECHAT" }`
    *   **Res Data**: `{ "payUrl": "weixin://wxpay/bizpayurl?..." }` (或支付宝表单HTML)
2.  **模拟支付成功 (沙箱或前端直调)**
    *   **POST** `/api/finance/mock_pay_success`
    *   **Req**: `{ "orderNo": "R20240522xxxx" }`
    *   **说明**：更新 payment_record 状态，并通知 order 模块更新订单为“已支付”。
3.  **查询押金退款进度**
    *   **GET** `/api/finance/deposit_status/{orderNo}`

---

## 3. 开发建议

1.  **时间格式**：前后端统一使用 `yyyy-MM-dd HH:mm:ss`，后端实体类推荐加上 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")`。
2.  **跨域问题**：前端目前在 Vue 层配置了 `/api` 代理，或者后端统一增加 `@CrossOrigin` 或 `CorsFilter` 跨域配置。
3.  **异常捕获**：使用 `@RestControllerAdvice` 结合 `@ExceptionHandler`，将所有系统异常转化为规范的 JSON (`code: 500, msg: "系统繁忙"`) 抛给前端。
