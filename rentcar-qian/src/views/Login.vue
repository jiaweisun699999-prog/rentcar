<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="logo">
        <h2>欢迎登录悟空租车</h2>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0">
        <el-form-item prop="phone">
          <el-input 
            v-model="loginForm.phone" 
            placeholder="请输入手机号" 
            size="large"
            :prefix-icon="User">
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            size="large"
            :prefix-icon="Lock"
            show-password>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" size="large" @click="handleLogin" :loading="loading">
            登录
          </el-button>
        </el-form-item>
        <div class="extra-actions">
          <a href="#">忘记密码？</a>
          <a href="#">立即注册</a>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { User, Lock } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store';
import { ElMessage } from 'element-plus';
import request from '../utils/request';

const router = useRouter();
const userStore = useUserStore();

const loginFormRef = ref(null);
const loading = ref(false);

const loginForm = reactive({
  phone: '',
  password: ''
});

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
};

const handleLogin = () => {
  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true;
      request.post('/user/login', loginForm).then(res => {
        userStore.setToken(res.token);
        userStore.setUserInfo(res.userInfo);
        ElMessage.success('登录成功');
        router.push('/');
        loading.value = false;
      }).catch((err) => {
        loading.value = false;
        console.error(err);
      });
    }
  });
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
}
.logo {
  text-align: center;
  margin-bottom: 30px;
}
.logo h2 {
  color: #333;
}
.login-btn {
  width: 100%;
}
.extra-actions {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
}
.extra-actions a {
  color: #409EFF;
  text-decoration: none;
}
</style>
