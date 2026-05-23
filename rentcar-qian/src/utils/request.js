import axios from 'axios';
import { ElMessage } from 'element-plus';

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 统一的接口前缀，后端需配有对应前缀或前端配置代理
  timeout: 10000 // 请求超时时间
});

// request 拦截器
service.interceptors.request.use(
  config => {
    // 可以在这里统一加上 Token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token;
    }
    return config;
  },
  error => {
    console.error('Request Error:', error);
    return Promise.reject(error);
  }
);

// response 拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;
    
    // 统一处理标准的响应格式
    // { code: 200, msg: "success", data: {} }
    if (res.code === 200) {
      return res.data;
    } else {
      ElMessage({
        message: res.msg || 'Error',
        type: 'error',
        duration: 3000
      });
      // 可以根据特定的 code（如 401 token 失效）做特殊处理
      if (res.code === 401) {
        localStorage.removeItem('token');
        // 跳转登录页等...
      }
      return Promise.reject(new Error(res.msg || 'Error'));
    }
  },
  error => {
    console.error('Response Error:', error);
    ElMessage({
      message: error.message,
      type: 'error',
      duration: 3000
    });
    return Promise.reject(error);
  }
);

export default service;
