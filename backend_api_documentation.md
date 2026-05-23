# 悟空租车 - 后端接口规范文档

## 1. 全局说明
### 1.1 基础路径
所有接口的基础路径为：`/api`

### 1.2 统一响应格式 (Result)
所有接口无论成功或失败，均返回统一的 JSON 结构：
```json
{
  "code": 200,          // 状态码：200为成功，其他为失败
  "message": "success", // 提示信息
  "data": {}            // 响应数据（可能为 null、对象或数组）
}
```

### 1.3 鉴权方式
除登录、注册及部分公开查询接口外，其他接口需在请求头携带 Token：
`Authorization: Bearer <token>`

---

## 2. 接口列表

### 2.1 用户模块 (User)

#### 2.1.1 登录
- **接口路径**: `/user/login`
- **请求方式**: `POST`
- **请求参数**:
  ```json
  {
    "phone": "13800138000",
    "password": "password123"
  }
  ```
- **响应数据**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "token": "eyJhbGciOiJIUzUxMiJ9...",
      "userInfo": {
        "id": 1,
        "phone": "13800138000",
        "username": "张三",
        "role": "admin"
      }
    }
  }
  ```

#### 2.1.2 注册
- **接口路径**: `/user/register`
- **请求方式**: `POST`
- **请求参数**:
  ```json
  {
    "phone": "13800138000",
    "password": "password123",
    "smsCode": "123456" 
  }
  ```
- **响应数据**: 
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": null
  }
  ```

#### 2.1.3 获取用户列表（后台）
- **接口路径**: `/user/list`
- **请求方式**: `GET`
- **请求参数**: `page=1&pageSize=10&keyword=张三` (可选)
- **响应数据**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "total": 100,
      "records": [
        {
          "id": 1,
          "phone": "13800138000",
          "username": "张三",
          "status": 1,
          "createTime": "2024-03-15 10:00:00"
        }
      ]
    }
  }
  ```

---

### 2.2 门店模块 (Store)

#### 2.2.1 门店列表查询（后台&前台均可使用）
- **接口路径**: `/store/list`
- **请求方式**: `GET`
- **请求参数**: `page=1&pageSize=10&cityName=北京` (可选)
- **响应数据**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "total": 50,
      "records": [
        {
          "id": 1,
          "merchantName": "直营店",
          "cityName": "北京",
          "address": "朝阳区三里屯SOHO地下车库",
          "isSupportDelivery": 1,
          "createTime": "2024-03-15 10:00:00"
        }
      ]
    }
  }
  ```

#### 2.2.2 新增门店
- **接口路径**: `/store/add`
- **请求方式**: `POST`
- **请求参数**:
  ```json
  {
    "merchantName": "飞猪出行",
    "cityName": "北京",
    "address": "海淀区中关村",
    "isSupportDelivery": 1
  }
  ```
- **响应数据**:
  ```json
  {
    "code": 200,
    "message": "新增门店成功",
    "data": null
  }
  ```

#### 2.2.3 编辑门店
- **接口路径**: `/store/update`
- **请求方式**: `PUT`
- **请求参数**:
  ```json
  {
    "id": 1,
    "merchantName": "飞猪出行(中关村店)",
    "cityName": "北京",
    "address": "海淀区中关村大街1号",
    "isSupportDelivery": 0
  }
  ```
- **响应数据**:
  ```json
  {
    "code": 200,
    "message": "修改门店成功",
    "data": null
  }
  ```

#### 2.2.4 删除门店
- **接口路径**: `/store/delete/{id}`
- **请求方式**: `DELETE`
- **路径参数**: `id` - 门店ID
- **响应数据**:
  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

---

### 2.3 车辆与车型模块 (Car)

#### 2.3.1 获取推荐车型列表（前台首页）
- **接口路径**: `/car/recommend`
- **请求方式**: `GET`
- **响应数据**: 
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 101,
        "brandSeries": "大众 迈腾",
        "carType": "经济型",
        "seatsDoors": "5座4门",
        "mainImage": "https://your-oss-domain.com/path/maiteng.png",
        "dailyPrice": 158.00
      },
      {
        "id": 102,
        "brandSeries": "特斯拉 Model 3",
        "carType": "豪华型",
        "seatsDoors": "5座4门",
        "mainImage": "https://your-oss-domain.com/path/model3.png",
        "dailyPrice": 358.00
      }
    ]
  }
  ```

#### 2.3.2 车型库分页列表（后台）
- **接口路径**: `/car/model/list`
- **请求方式**: `GET`
- **请求参数**: `page=1&pageSize=10`
- **响应数据**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "total": 20,
      "records": [
        {
          "id": 101,
          "brandSeries": "大众 迈腾",
          "carType": "经济型",
          "seatsDoors": "5座4门",
          "mainImage": "https://your-oss-domain.com/path/maiteng.png",
          "status": 1,
          "createTime": "2024-03-15 10:00:00"
        }
      ]
    }
  }
  ```

#### 2.3.3 上架新车型 (SPU)
- **接口路径**: `/car/model/add`
- **请求方式**: `POST`
- **请求参数**:
  ```json
  {
    "brandSeries": "特斯拉 Model 3",
    "carType": "豪华型",
    "seatsDoors": "5座4门",
    "mainImage": "https://your-oss-domain.com/path/model3.png"
  }
  ```
- **响应数据**:
  ```json
  {
    "code": 200,
    "message": "车型上架成功",
    "data": null
  }
  ```

#### 2.3.4 下架/删除车型
- **接口路径**: `/car/model/delete/{id}`
- **请求方式**: `DELETE`
- **响应数据**:
  ```json
  {
    "code": 200,
    "message": "车型下架成功",
    "data": null
  }
  ```

---

### 2.4 文件上传模块 (File)

#### 2.4.1 通用文件/图片上传
- **接口路径**: `/upload`
- **请求方式**: `POST`
- **请求头**: `Content-Type: multipart/form-data`
- **请求参数**: `file` (File对象，表单参数)
- **响应数据**:
  ```json
  {
    "code": 200,
    "message": "上传成功",
    "data": {
      "url": "https://your-oss-domain.com/path/to/image.png"
    }
  }
  ```

---

### 2.5 订单模块 (Order) (预留)

#### 2.5.1 订单列表查询
- **接口路径**: `/order/list`
- **请求方式**: `GET`
- **请求参数**: `page=1&pageSize=10&status=1`
- **响应数据**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "total": 150,
      "records": [
        {
          "orderId": "ORD202403150001",
          "userId": 1,
          "carModelId": 101,
          "brandSeries": "大众 迈腾",
          "startDate": "2024-03-20",
          "endDate": "2024-03-22",
          "totalAmount": 316.00,
          "status": 1,
          "createTime": "2024-03-15 10:00:00"
        }
      ]
    }
  }
  ```

---

### 2.6 财务模块 (Finance) (预留)

#### 2.6.1 财务流水查询
- **接口路径**: `/finance/list`
- **请求方式**: `GET`
- **请求参数**: `page=1&pageSize=10&startDate=2024-03-01&endDate=2024-03-31`
- **响应数据**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "total": 300,
      "records": [
        {
          "transactionId": "TRX202403150001",
          "orderId": "ORD202403150001",
          "amount": 316.00,
          "type": 1, // 1: 收入, 2: 支出, 3: 退款
          "remark": "租车费用支付",
          "createTime": "2024-03-15 10:05:00"
        }
      ]
    }
  }
  ```
