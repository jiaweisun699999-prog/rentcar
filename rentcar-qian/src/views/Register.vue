<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="logo">
        <h2>注册悟空租车</h2>
      </div>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="0">
        <el-form-item prop="phone">
          <el-input 
            v-model="registerForm.phone" 
            placeholder="请输入手机号" 
            size="large"
            :prefix-icon="User">
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="请输入密码" 
            size="large"
            :prefix-icon="Lock"
            show-password>
          </el-input>
        </el-form-item>
        <el-form-item prop="smsCode">
          <div style="display: flex; width: 100%; gap: 10px;">
            <el-input 
              v-model="registerForm.smsCode" 
              placeholder="短信验证码" 
              size="large"
              :prefix-icon="Message"
              style="flex: 1;">
            </el-input>
            <el-button size="large">获取验证码</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="success" class="login-btn" size="large" @click="handleRegister" :loading="loading">
            立即注册
          </el-button>
        </el-form-item>
        <div class="extra-actions">
          <a href="javascript:void(0)" @click="$router.push('/login')">已有账号？去登录</a>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { User, Lock, Message } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import request from '../utils/request';

const router = useRouter();

const registerFormRef = ref(null);
const loading = ref(false);

const registerForm = reactive({
  phone: '',
  password: '',
  smsCode: '123456' // 模拟默认验证码
});

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  smsCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
};

const handleRegister = () => {
  registerFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true;
      request.post('/user/register', registerForm).then(() => {
        ElMessage.success('注册成功，请登录');
        router.push('/login');
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
  justify-content: center;
  font-size: 0.9rem;
}
.extra-actions a {
  color: #409EFF;
  text-decoration: none;
}
</style>
