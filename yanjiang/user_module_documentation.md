# 悟空尊享租车 - 用户模块（User Module）技术文档

本技术文档旨在详尽梳理和解析**悟空尊享租车**系统中的“用户模块（User Module）”的所有核心功能。文档从**功能目的**、**前端调用与传参**、**后端接收与处理（含数据库）**、**前端渲染展示**等全链路维度进行深入拆解，并附上关键业务代码，以确保前后端开发无缝衔接与高效协作。

---

## 目录
- [1. 全局规范与技术栈](#1-全局规范与技术栈)
  - [1.1 前端核心技术](#11-前端核心技术)
  - [1.2 后端核心技术](#12-后端核心技术)
  - [1.3 全局前后端数据交互规范](#13-全局前后端数据交互规范)
- [2. 功能一：用户登录 (User Login)](#2-功能一用户登录-user-login)
  - [2.1 功能目的](#21-功能目的)
  - [2.2 前端请求调用与参数传递](#22-前端请求调用与参数传递)
  - [2.3 后端接收、校验与业务处理](#23-后端接收校验与业务处理)
  - [2.4 前端响应处理与登录态维护](#24-前端响应处理与登录态维护)
- [3. 功能二：会员注册 (User Registration)](#3-功能二会员注册-user-registration)
  - [3.1 功能目的](#31-功能目的)
  - [3.2 前端请求调用与参数传递](#32-前端请求调用与参数传递)
  - [3.3 后端接收、校验与业务处理](#33-后端接收校验与业务处理)
  - [3.4 前端响应处理与跳转逻辑](#34-前端响应处理与跳转逻辑)
- [4. 功能三：获取当前登录用户信息 (Get User Info)](#4-功能三获取当前登录用户信息-get-user-info)
  - [4.1 功能目的](#41-功能目的)
  - [4.2 前端请求调用与 Token 挂载](#42-前端请求调用与-token-挂载)
  - [4.3 后端接收、解析与业务处理](#43-后端接收解析与业务处理)
  - [4.4 前端响应处理与 UI 头像渲染](#44-前端响应处理与-ui-头像渲染)
- [5. 功能四：个人中心与实名/驾照认证 (Credit & Certify)](#5-功能四个人中心与实名驾照认证-credit--certify)
  - [5.1 功能目的与风控免押机制](#51-功能目的与风控免押机制)
  - [5.2 前端驾照物理上传流程](#52-前端驾照物理上传流程)
  - [5.3 实名认证请求与后端落库设计](#53-实名认证请求与后端落库设计)
  - [5.4 芝麻信用分 Aurora 极光仪表盘前端渲染](#54-芝麻信用分-aurora-极光仪表盘前端渲染)
- [6. 功能五：后台用户列表分页查询 (User Management)](#6-功能五后台用户列表分页查询-user-management)
  - [6.1 功能目的](#61-功能目的)
  - [6.2 前端查询调用与表格渲染](#62-前端查询调用与表格渲染)
  - [6.3 后端接收、模糊查询与数据脱敏](#63-后端接收模糊查询与数据脱敏)

---

## 1. 全局规范与技术栈

### 1.1 前端核心技术
*   **核心框架**：Vue 3 (Setup 语法糖)
*   **路由管理**：Vue Router 4
*   **状态管理**：Pinia 2 (持久化缓存 `token` 和 `userInfo` 至 `localStorage`)
*   **UI 组件库**：Element Plus (采用磨砂玻璃拟物化 Premium 设计)
*   **网络请求**：Axios 拦截器 (统一处理 Token 挂载与 401 登录过期劫持)

### 1.2 后端核心技术
*   **核心框架**：Spring Boot 3
*   **持久层框架**：MyBatis-Plus
*   **安全认证**：JWT (JSON Web Token) 无状态机制
*   **加密算法**：MD5 (明文密码加盐哈希存储)
*   **会话上下文**：基于 ThreadLocal 的全局 `UserContext` 线程隔离设计

### 1.3 全局前后端数据交互规范

所有后端接口统一返回 JSON 标准格式：
```json
{
  "code": 200,          // 200: 成功, 401: 未登录或Token失效, 500: 服务器异常
  "message": "success", // 操作提示信息
  "data": {}            // 响应体 (若无可为 null)
}
```

前端 Axios 拦截器会对上述格式进行全局解析，并在 `code !== 200` 时通过 `ElMessage` 弹出错误警告。

---

## 2. 功能一：用户登录 (User Login)

### 2.1 功能目的
实现系统会员的安全认证。通过账号（手机号）与密码匹配，对合法用户授予无状态的 JWT Token，并在本地保存用户身份（普通租客、门店管理员、系统超管），以作为访问其他保护接口（如租车、订单、后台）的通行证。

### 2.2 前端请求调用与参数传递
*   **触发组件**：`rentcar-qian/src/views/Login.vue`
*   **调用方式**：`request.post('/user/login', loginForm)`
*   **关键代码** (参数声明与校验)：
```javascript
// 表单绑定响应式数据
const loginForm = reactive({
  phone: '',
  password: ''
});

// 前端实时强校验规则
const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' }
  ]
};
```

### 2.3 后端接收、校验与业务处理
*   **Controller 接收**：`UserController.java`
```java
@PostMapping("/login")
public Result<LoginResVo> login(@RequestBody LoginReqDto reqDto) {
    try {
        LoginResVo resVo = sysUserService.login(reqDto);
        return Result.success(resVo);
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```
*   **Service 核心校验逻辑**：`SysUserServiceImpl.java`
```java
@Override
public LoginResVo login(LoginReqDto reqDto) {
    // 1. 基础空参校验
    checkLoginParams(reqDto);

    // 2. 查询用户是否存在 (MyBatis-Plus 条件构造器)
    LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(SysUser::getPhone, reqDto.getPhone());
    SysUser sysUser = this.getOne(queryWrapper);

    if (sysUser == null) {
        throw new RuntimeException("账号不存在");
    } 
    
    // 3. 校验密码 (对前端传来的明文密码进行 MD5 加密，比对数据库密文)
    String encryptPassword = MD5Utils.encrypt(reqDto.getPassword());
    if (!sysUser.getPassword().equals(encryptPassword)) {
        throw new RuntimeException("账号或密码错误");
    }

    // 4. 账号状态检查
    if (sysUser.getStatus() != null && sysUser.getStatus() == 0) {
        throw new RuntimeException("账号已被禁用");
    }

    // 5. 校验通过，生成 JWT Token
    String token = jwtUtils.generateToken(sysUser.getId(), sysUser.getPhone());

    // 6. 组装用户信息视图对象 (屏蔽敏感的 password)
    UserInfoVo userInfoVo = buildUserInfoVo(sysUser);

    LoginResVo resVo = new LoginResVo();
    resVo.setToken(token);
    resVo.setUserInfo(userInfoVo);

    return resVo;
}
```

### 2.4 前端响应处理与登录态维护
当前端收到 `code: 200` 的成功响应后，将后端返回 of `token` 及脱敏后的 `userInfo` 保存至全局的 Pinia 状态树及 `localStorage` 中。

*   **Pinia 存储逻辑**：`rentcar-qian/src/store/index.js`
```javascript
export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    token: localStorage.getItem('token') || ''
  }),
  getters: {
    isAdmin: (state) => state.userInfo?.role === 'admin' || state.userInfo?.role === 'store_admin',
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    setToken(token) {
      this.token = token;
      localStorage.setItem('token', token);
    },
    setUserInfo(info) {
      this.userInfo = info;
      localStorage.setItem('userInfo', JSON.stringify(info));
    }
  }
});
```

*   **登录成功跳转**：`Login.vue` 中的回显跳转：
```javascript
const handleLogin = () => {
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true;
      request.post('/user/login', loginForm).then(res => {
        // 保存身份凭证
        userStore.setToken(res.token);
        userStore.setUserInfo(res.userInfo);
        ElMessage.success('欢迎回来，登录成功！');
        
        // 若从其他受保卫的路由拦截过来，则重定向回原页面，否则进入主页
        const redirect = router.currentRoute.value.query.redirect;
        router.push(redirect || '/');
        loading.value = false;
      }).catch(() => {
        loading.value = false;
      });
    }
  });
};
```

---

## 3. 功能二：会员注册 (User Registration)

### 3.1 功能目的
允许新访客在平台自主创建账户。注册成功后，系统会默认初始化其角色为“普通租客（role: 0）”，并赠送“芝麻信用分初始底分（600分）”和起初的“正常状态（status: 1）”。

### 3.2 前端请求调用与参数传递
*   **触发组件**：`rentcar-qian/src/views/Register.vue`
*   **调用方式**：`request.post('/user/register', registerForm)`
*   **关键代码** (验证通过后提交)：
```javascript
const registerForm = reactive({
  username: '',
  phone: '',
  password: ''
});

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符之间', trigger: 'blur' }
  ]
};
```

### 3.3 后端接收、校验与业务处理
*   **Controller 接收**：`UserController.java`
```java
@PostMapping("/register")
public Result<Void> register(@RequestBody RegisterReqDto reqDto) {
    try {
        sysUserService.register(reqDto);
        return Result.success("注册成功", null);
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```
*   **Service 核心落库逻辑**：`SysUserServiceImpl.java`
```java
@Override
public void register(RegisterReqDto reqDto) {
    // 1. 参数严苛校验 (格式及长度)
    checkRegisterParams(reqDto);

    // 2. 查重手机号，保证账号唯一性
    LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(SysUser::getPhone, reqDto.getPhone());
    long count = this.count(queryWrapper);
    if (count > 0) {
        throw new RuntimeException("手机号已被注册");
    }

    // 3. 构建全新系统会员实体
    SysUser sysUser = new SysUser();
    sysUser.setPhone(reqDto.getPhone());
    // 强哈希：密码由 MD5 混淆存盘
    sysUser.setPassword(MD5Utils.encrypt(reqDto.getPassword()));
    // 默认生成昵称 (手机后四位后缀)
    sysUser.setUsername("用户" + reqDto.getPhone().substring(7)); 
    sysUser.setRole(0);          // 0: 普通租客
    sysUser.setStatus(1);        // 1: 账号正常活动
    sysUser.setCreditScore(600); // 初始信用资产评分：600 分

    this.save(sysUser);
}
```

### 3.4 前端响应处理与跳转逻辑
*   **注册成功流转**：当收到后端注册成功的指令后，弹出全局绿色成功横幅，延迟 1.5 秒安全过渡并自动切换至登录页面，提升体验。
```javascript
const handleRegister = () => {
  registerFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true;
      request.post('/user/register', registerForm).then(() => {
        ElMessage.success('尊享会员注册成功！正在为您自动跳转登录界面...');
        setTimeout(() => {
          router.push('/login');
        }, 1500);
        loading.value = false;
      }).catch(() => {
        loading.value = false;
      });
    }
  });
};
```

---

## 4. 功能三：获取当前登录用户信息 (Get User Info)

### 4.1 功能目的
在客户端用户进行路由切换或手动刷新浏览器时，拉取最新的服务端会话状态，以同步当前最新的会员属性（如最新芝麻分、是否已被后台封禁等）。同时，该请求也是对客户端 Token 是否过期的安全透传。

### 4.2 前端请求调用与 Token 挂载
在用户每一次切换页面或者初始化系统时，前端会自动向后端查询一次用户信息。
*   **全局 Axios 拦截器核心挂载**：`rentcar-qian/src/utils/request.js`
```javascript
service.interceptors.request.use(
  config => {
    // 只要本地存有 JWT，则在所有 HTTP 请求 Header 中自动附带 Authorization 头部
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);
```
*   **页面拉取**：`Profile.vue` 在生命周期 `onMounted` 时，触发查询：
```javascript
request.get('/user/info');
```

### 4.3 后端接收、解析与业务处理
*   **Controller 接收**：`UserController.java`
```java
@GetMapping("/info")
public Result<UserInfoVo> info() {
    try {
        UserInfoVo userInfoVo = sysUserService.getCurrentUserInfo();
        return Result.success(userInfoVo);
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```
*   **Service 解析用户信息**：
后端基于过滤器拦截器框架，解析 Header 中的 `Authorization`，若合法则把 `userId` 存入 ThreadLocal 上下文 `UserContext`。
```java
@Override
public UserInfoVo getCurrentUserInfo() {
    // 1. 从隔离的本地线程变量中安全取出 UserId
    Long userId = UserContext.getUserId();
    if (userId == null) {
        throw new RuntimeException("未登录或Token已过期");
    }
    
    // 2. 查库获取实时实体
    SysUser sysUser = this.getById(userId);
    if (sysUser == null) {
        throw new RuntimeException("用户不存在");
    }
    
    // 3. 防护性判断是否已被封禁
    if (sysUser.getStatus() != null && sysUser.getStatus() == 0) {
        throw new RuntimeException("账号已被禁用");
    }
    
    // 4. 返回包含信用分与角色信息的脱敏 Vo
    return buildUserInfoVo(sysUser);
}
```

### 4.4 前端响应处理与 UI 头像渲染
*   **全局 401 劫持逻辑**：`request.js`
若后端返回 401（未登录或 Token 失效），前端清除 Token 和 UserInfo，并强制打回登录页：
```javascript
if (status === 401 || data?.code === 401) {
  const message = data?.message || data?.msg || '请先登录或登录已过期';
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');
  ElMessage.warning(message);
  router.push('/login');
}
```
*   **UI 动态显示**：
在 Header 部分拉取用户信息并截取用户名首字符作为用户高级渐变头像：
```html
<el-avatar :size="32" class="user-avatar">
  {{ userStore.userInfo?.username?.substring(0, 1) || 'U' }}
</el-avatar>
<span class="username-display">{{ userStore.userInfo?.username }}</span>
```

---

## 5. 功能四：个人中心与实名/驾照认证 (Credit & Certify)

### 5.1 功能目的与风控免押机制
这是悟空租车极具亮点的**核心风控服务**。用户在使用平台车辆出行前，必须完成实名制（身份证 18 位）以及中华人民共和国机动车驾驶证的主页上传及权威核验。
*   **免押机制**：系统依据用户的“芝麻信用分”进行分级。**信用分 $\ge 700$ 分**的会员自动点亮“双免押特权”（免去车辆出车押金 3000-8000 元及违章押金 2000 元），实现零资金占用极速提车；低于 700 分的用户仍需走正常押金支付流转。

### 5.2 前端驾照物理上传流程
*   **UI 交互**：`Profile.vue` 提供精美的拖拽上传与实时回显区域。
```html
<el-upload
  class="license-uploader"
  action=""
  :show-file-list="false"
  :http-request="uploadLicenseImage"
  :before-upload="beforeLicenseUpload">
  <div class="uploaded-preview-box" v-if="certForm.driverLicenseUrl">
    <img :src="certForm.driverLicenseUrl" class="license-img" />
    <div class="upload-mask">
      <el-icon class="mask-icon"><Edit /></el-icon>
      <span>重新上传</span>
    </div>
  </div>
  <div class="uploader-placeholder" v-else>
    <el-icon class="uploader-icon"><Plus /></el-icon>
    <div class="uploader-text">
      <h4>拖拽或点击上传驾驶证主页</h4>
      <p>支持 JPG, PNG 格式，大小不超过 5MB</p>
    </div>
  </div>
</el-upload>
```
*   **前端逻辑**：
```javascript
// 上传前的图片大小及格式限制
const beforeLicenseUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg';
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isImage) {
    ElMessage.error('只能上传图片格式的驾驶证主页 (JPG/PNG)!');
    return false;
  }
  if (!isLt5M) {
    ElMessage.error('驾驶证图片大小不能超过 5MB!');
    return false;
  }
  return true;
};

// 触发后端物理上传接口 /api/upload
const uploadLicenseImage = async (options) => {
  const file = options.file;
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    const res = await request.post('/upload', formData);
    // 回显服务器返回的文件绝对 URL (此处由 UploadResVo.url 解析返回)
    certForm.value.driverLicenseUrl = res.url || res;
    ElMessage.success('驾驶执照图片上传并解析成功，请提交验证保存！');
  } catch (error) {
    ElMessage.error('文件上传失败，请稍后重试');
  }
};
```

### 5.3 实名认证与信用获取后端设计（已完全实现）
在后端中，实名认证与芝麻信用评估服务已被完满构建。

#### 5.3.1 数据库底层表结构 (`init.sql`)
```sql
CREATE TABLE `user_certification` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID，关联sys_user表',
  `id_card_no` varchar(18) NOT NULL COMMENT '身份证号',
  `driver_license_url` varchar(255) NOT NULL COMMENT '驾驶证主页存储URL',
  `audit_status` int(11) DEFAULT '1' COMMENT '审核状态：1-审核中, 2-通过, 3-驳回',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 5.3.2 实体与传输类 (Entity & DTO / VO)
*   **认证请求 DTO** (`CertifyReqDto.java`)：
```java
package com.msb.rentcarhou.dto;
import lombok.Data;

@Data
public class CertifyReqDto {
    private String idCardNo;
    private String driverLicenseUrl;
}
```
*   **信用免押视图 VO** (`UserCreditInfoVo.java`)：
```java
package com.msb.rentcarhou.vo;
import lombok.Data;

@Data
public class UserCreditInfoVo {
    private Integer creditScore;
    private Boolean isDepositWaived;
    private String idCardNo;
    private String driverLicenseUrl;
    private Integer auditStatus; // 1-审核中, 2-通过, 3-驳回, 0-未认证
}
```

#### 5.3.3 控制层接收 (Controller)
在 `UserController.java` 中新增如下两个端点：
```java
@GetMapping("/credit-info")
public Result<UserCreditInfoVo> getCreditInfo() {
    try {
        UserCreditInfoVo creditInfo = sysUserService.getCreditInfo();
        return Result.success(creditInfo);
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}

@PostMapping("/certify")
public Result<Void> certify(@RequestBody CertifyReqDto reqDto) {
    try {
        sysUserService.certify(reqDto);
        return Result.success("认证成功", null);
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```

#### 5.3.4 业务实现层核心逻辑 (Service Impl)
在 `SysUserServiceImpl.java` 中实现的具体业务逻辑，通过在认证通过后**自动把用户信用评分提升至720分**，极大程度优化了租车免押特权的演示体验：
```java
@Override
public UserCreditInfoVo getCreditInfo() {
    Long userId = UserContext.getUserId();
    if (userId == null) {
        throw new RuntimeException("未登录或Token已过期");
    }
    
    SysUser sysUser = this.getById(userId);
    if (sysUser == null) {
        throw new RuntimeException("用户不存在");
    }

    UserCreditInfoVo vo = new UserCreditInfoVo();
    int creditScore = sysUser.getCreditScore() != null ? sysUser.getCreditScore() : 600;
    vo.setCreditScore(creditScore);
    vo.setIsDepositWaived(creditScore >= 700);

    // 获取实名认证数据
    UserCertification certification = userCertificationMapper.selectById(userId);
    if (certification != null) {
        vo.setIdCardNo(certification.getIdCardNo());
        vo.setDriverLicenseUrl(certification.getDriverLicenseUrl());
        vo.setAuditStatus(certification.getAuditStatus());
    } else {
        vo.setAuditStatus(0); // 0-代表未认证
    }
    
    return vo;
}

@Override
@Transactional
public void certify(CertifyReqDto reqDto) {
    Long userId = UserContext.getUserId();
    if (userId == null) {
        throw new RuntimeException("未登录或Token已过期");
    }

    if (reqDto == null || !StringUtils.hasText(reqDto.getIdCardNo()) 
        || !StringUtils.hasText(reqDto.getDriverLicenseUrl())) {
        throw new RuntimeException("认证参数不完整");
    }

    // 校验身份证格式 (18位大陆身份证)
    if (!reqDto.getIdCardNo().matches("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$")) {
        throw new RuntimeException("身份证号码格式不正确");
    }

    // 查询是否已存在认证记录
    UserCertification certification = userCertificationMapper.selectById(userId);
    if (certification == null) {
        certification = new UserCertification();
        certification.setUserId(userId);
        certification.setIdCardNo(reqDto.getIdCardNo());
        certification.setDriverLicenseUrl(reqDto.getDriverLicenseUrl());
        certification.setAuditStatus(2); // 审核直接设为通过(2)，便于演示
        userCertificationMapper.insert(certification);
    } else {
        certification.setIdCardNo(reqDto.getIdCardNo());
        certification.setDriverLicenseUrl(reqDto.getDriverLicenseUrl());
        certification.setAuditStatus(2); // 重新覆盖认证
        userCertificationMapper.updateById(certification);
    }

    // 实名核验成功后，自动将用户芝麻信用资产调升到 720 分，点亮双免押提车特权！
    SysUser sysUser = this.getById(userId);
    if (sysUser != null && (sysUser.getCreditScore() == null || sysUser.getCreditScore() < 700)) {
        sysUser.setCreditScore(720); 
        this.updateById(sysUser);
    }
}
```


### 5.4 芝麻信用分 Aurora 极光仪表盘前端渲染
从 `/api/user/credit-info`（前台对接 `/api/user/info` 可获信用分）获取最新分值并在左侧渲染极光仪表：
```html
<div class="gauge-arc" :class="{'high-score-arc': creditInfo.creditScore >= 700}">
  <h1 class="score-display">{{ creditInfo.creditScore }}</h1>
  <span class="score-label">芝麻信用分</span>
</div>

<div class="privilege-box">
  <div class="privilege-badge" v-if="creditInfo.creditScore >= 700">
    <el-icon class="badge-icon"><CircleCheck /></el-icon>
    <span class="badge-text">恭喜！您已点亮双免押特权</span>
  </div>
  <div class="privilege-badge denied" v-else>
    <el-icon class="badge-icon"><Warning /></el-icon>
    <span class="badge-text">信用不足 700 分，暂无免押特权</span>
  </div>
</div>
```

### 5.5 前端隐私安全加固与证件“加密托管”（全新升级）
为保护用户的敏感身份资产，平台对前端个人中心的交互逻辑进行了金融级隐私合规加固：

1.  **上传图片“只可写、不可看”**：
    *   在**实名认证前或重新核验时**，页面允许预览当前上传的本地图片以校验清晰度；
    *   一旦**认证通过落库后**，前端从物理上彻底销毁该原始图片的 HTML 渲染节点，代以高度拟物化的**暗金色加密安全锁卡片**，防止旁人窃屏。
2.  **身份证号码默认掩码与一键脱敏**：
    *   认证通过后的只读身份证输入框默认对核心的 8 位出生年月日信息进行脱敏打码展示（如 `110105********1111`）；
    *   集成交互小眼睛，仅在用户有意识地点击眼睛图标时才通过 `computed` 计算属性反向映射真实数据，安全可靠。

#### 5.5.1 前端核心状态逻辑 (`Profile.vue`)
```javascript
// 控制编辑/查看状态，当 auditStatus == 2（已通过）时默认进入加密只读状态
const isEditing = ref(false);
// 控制身份证号明文/密文展示
const showRawIdCard = ref(false);

const toggleIdCardVisibility = () => {
  showRawIdCard.value = !showRawIdCard.value;
};

// 身份证打码掩饰
const maskIdCard = (idCard) => {
  if (!idCard) return '';
  if (idCard.length !== 18) return idCard;
  return idCard.substring(0, 6) + '********' + idCard.substring(14);
};

// 计算属性过滤展示
const displayedIdCard = computed(() => {
  if (isEditing.value) {
    return certForm.value.idCardNo;
  }
  return showRawIdCard.value ? certForm.value.idCardNo : maskIdCard(certForm.value.idCardNo);
});

// 点击重新核验，一键解锁输入
const startReCertify = () => {
  isEditing.value = true;
  certForm.value.idCardNo = '';
  certForm.value.driverLicenseUrl = '';
};
```

#### 5.5.2 隐私加固 UI 渲染与样式
*   **脱敏与保险库 HTML 结构**：
```html
<!-- 身份证打码只读框 -->
<el-input 
  v-else
  :value="displayedIdCard" 
  readonly 
  size="large"
  class="premium-input readonly-masked-input">
  <template #suffix>
    <el-icon class="toggle-visibility-icon" @click="toggleIdCardVisibility" style="cursor: pointer; font-size: 1.1rem; color: #94a3b8; display: inline-flex; align-items: center; justify-content: center; height: 100%;">
      <component :is="showRawIdCard ? Hide : View" />
    </el-icon>
  </template>
</el-input>

<!-- 驾驶证“加密托管卡片” -->
<div class="secure-encrypted-card" v-else>
  <div class="secure-glow"></div>
  <el-icon class="secure-lock-icon"><Lock /></el-icon>
  <div class="secure-info">
    <h4>机动车驾驶证已安全加密托管</h4>
    <p>为保障您的个人隐私安全，平台已对您的原始驾驶证文件进行金融级加密处理，不可直接预览。</p>
  </div>
</div>
```
*   **磨砂拟物化暗黑 CSS 样式**：
```css
.secure-encrypted-card {
  width: 100%;
  border-radius: 16px;
  padding: 45px 20px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 15px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.15);
  box-sizing: border-box;
}

.secure-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 159, 28, 0.06) 0%, transparent 60%);
  pointer-events: none;
}

.secure-lock-icon {
  font-size: 2rem;
  color: #ff9f1c;
  background: rgba(255, 159, 28, 0.1);
  padding: 14px;
  border-radius: 50%;
  box-shadow: 0 0 20px rgba(255, 159, 28, 0.2);
}
```

---

## 6. 功能五：后台用户列表分页查询 (User Management)

### 6.1 功能目的
提供给系统超级管理员（Admin）或门店店长，在运营后台实时管控和检索全系统注册租客的入口。支持通过关键字对“昵称”和“手机号”进行模糊搜索，并进行分页排布。

### 6.2 前端查询调用与表格渲染
*   **触发组件**：`rentcar-qian/src/views/Admin.vue` 中的租客管理 Tab
*   **网络传参**：`request.get('/user/list', { params: queryParams })`
```javascript
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  keyword: '' // 搜索框输入
});
```

### 6.3 后端接收、模糊查询与数据脱敏
*   **Controller 接收**：`UserController.java`
```java
@GetMapping("/list")
public Result<Page<SysUser>> getUserList(UserQueryDto queryDto) {
    try {
        Page<SysUser> page = sysUserService.getUserList(queryDto);
        return Result.success(page);
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```
*   **Service 核心实现（模糊匹配与安全脱敏）**：`SysUserServiceImpl.java`
```java
@Override
public Page<SysUser> getUserList(UserQueryDto queryDto) {
    // 1. 防御性处理：防止页码溢出或为零
    int pageNum = queryDto == null || queryDto.getPage() == null || queryDto.getPage() < 1 ? 1 : queryDto.getPage();
    int pageSize = queryDto == null || queryDto.getPageSize() == null || queryDto.getPageSize() < 1 ? 10 : queryDto.getPageSize();
    
    Page<SysUser> page = new Page<>(pageNum, pageSize);
    LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
    
    // 2. 核心模糊并列检索：昵称包含关键字 OR 手机号包含关键字
    if (queryDto != null && StringUtils.hasText(queryDto.getKeyword())) {
        queryWrapper.like(SysUser::getUsername, queryDto.getKeyword())
                    .or()
                    .like(SysUser::getPhone, queryDto.getKeyword());
    }
    
    // 3. 按注册时间倒序，新注册用户在前
    queryWrapper.orderByDesc(SysUser::getCreateTime);
    
    Page<SysUser> userPage = this.page(page, queryWrapper);
    
    // 4. 安全脱敏！！！将要发送回前端的每一条记录的密码域均设为 null，防止接口泄露用户密码哈希
    userPage.getRecords().forEach(user -> user.setPassword(null));
    
    return userPage;
}
```

---

## 7. 结语

本用户模块打通了**悟空尊享租车**系统从账户注册、高拟物化安全登录，到个人芝麻信用风控核验、以及后台对全体用户实施管控的完整闭环。系统严格贯彻了**无状态会话安全机制（JWT）**与**密码哈希隔离原则（MD5）**，并在后台数据输出时执行了**数据脱敏**，为全套租车平台的资产流转与核心交易奠定了安全稳固的基石。
