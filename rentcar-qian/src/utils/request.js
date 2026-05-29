import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';

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
      const message = res.msg || res.message || '请求处理失败，请稍后重试';
      ElMessage({
        message,
        type: 'error',
        duration: 3000
      });
      // 可以根据特定的 code（如 401 token 失效）做特殊处理
      if (res.code === 401) {
        localStorage.removeItem('token');
        if (router.currentRoute.value.path !== '/login') {
          router.push('/login');
        }
      }
      return Promise.reject(new Error(message));
    }
  },
  error => {
    const status = error.response?.status;
    const data = error.response?.data;
    if (status === 401 || data?.code === 401) {
      const message = data?.message || data?.msg || '请先登录或登录已过期';
      localStorage.removeItem('token');
      ElMessage({
        message,
        type: 'warning',
        duration: 3000
      });
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login');
      }
      return Promise.reject(new Error(message));
    }
    console.error('Response Error:', error);
    const message = data?.message || data?.msg || error.message || '请求处理失败，请稍后重试';
    ElMessage({
      message,
      type: 'error',
      duration: 3000
    });
    return Promise.reject(error);
  }
);

export default service;
