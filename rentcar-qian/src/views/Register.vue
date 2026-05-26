<template>
  <div class="register-container">
    <div class="background-decor animate-bg"></div>
    <div class="background-decor-2 animate-bg-2"></div>
    
    <el-card class="register-card-glass animate-fade-in">
      <div class="logo-area">
        <div class="logo-icon">
          <el-icon><Van /></el-icon>
        </div>
        <h2>悟空尊享会员注册</h2>
        <p class="tagline">加入我们 · 开启品质自驾新时代</p>
      </div>

      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="0" class="custom-form">
        <el-form-item prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="请输入个性用户名" 
            size="large"
            :prefix-icon="User"
            class="premium-input">
          </el-input>
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input 
            v-model="registerForm.phone" 
            placeholder="请输入您的手机号" 
            size="large"
            :prefix-icon="Iphone"
            class="premium-input">
          </el-input>
        </el-form-item>
        
        <el-form-item prop="password" class="password-item">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="请设定您的安全密码" 
            size="large"
            :prefix-icon="Lock"
            show-password
            class="premium-input"
            @keyup.enter="handleRegister">
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="warning" class="register-btn-gradient" size="large" @click="handleRegister" :loading="loading">
            同意协议并完成注册
          </el-button>
        </el-form-item>
        
        <div class="extra-actions">
          <span class="terms">注册即代表同意<a href="#">《用户隐私与服务协议》</a></span>
          <a href="javascript:void(0)" @click="$router.push('/login')" class="go-login">
            已有账号？<span>去登录</span>
          </a>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { User, Lock, Iphone, Van } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import request from '../utils/request';

const router = useRouter();
const registerFormRef = ref(null);
const loading = ref(false);

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
      }).catch((err) => {
        loading.value = false;
        console.error(err);
      });
    }
  });
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap');

.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  position: relative;
  background-color: #0b0f19;
  overflow: hidden;
  font-family: 'Outfit', 'PingFang SC', sans-serif;
}

/* Background floating decors */
.background-decor {
  position: absolute;
  top: -10%;
  right: -10%;
  width: 40%;
  height: 60%;
  background: radial-gradient(circle, rgba(255, 159, 28, 0.15) 0%, rgba(246, 114, 0, 0) 70%);
  border-radius: 50%;
  z-index: 1;
}

.background-decor-2 {
  position: absolute;
  bottom: -15%;
  left: -10%;
  width: 45%;
  height: 65%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.15) 0%, rgba(99, 102, 241, 0) 70%);
  border-radius: 50%;
  z-index: 1;
}

/* Glassmorphism Card */
.register-card-glass {
  width: 450px;
  background: rgba(15, 23, 42, 0.75) !important;
  backdrop-filter: blur(25px);
  -webkit-backdrop-filter: blur(25px);
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: 24px !important;
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.4) !important;
  padding: 35px 25px;
  z-index: 10;
  color: #fff;
  transition: all 0.3s;
}

.register-card-glass:hover {
  border-color: rgba(255, 159, 28, 0.25) !important;
  box-shadow: 0 30px 60px rgba(255, 159, 28, 0.05) !important;
}

.logo-area {
  text-align: center;
  margin-bottom: 35px;
}

.logo-icon {
  background: linear-gradient(135deg, #ff9f1c 0%, #f67200 100%);
  color: #fff;
  width: 60px;
  height: 60px;
  border-radius: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  margin-bottom: 15px;
  box-shadow: 0 8px 25px rgba(246, 114, 0, 0.3);
}

.logo-area h2 {
  font-size: 1.6rem;
  font-weight: 800;
  background: linear-gradient(120deg, #ffffff 40%, #ff9f1c 90%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 6px 0;
  letter-spacing: 1px;
}

.tagline {
  color: #94a3b8;
  font-size: 0.85rem;
  margin: 0;
  letter-spacing: 2px;
  text-transform: uppercase;
}

/* Form Styling */
.custom-form {
  margin-top: 10px;
}

.premium-input :deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.05) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset !important;
  border-radius: 14px;
  height: 52px;
  padding: 0 18px;
  transition: all 0.3s;
}

.premium-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #ff9f1c inset !important;
  background-color: rgba(255, 255, 255, 0.08) !important;
}

.premium-input :deep(.el-input__inner) {
  color: #fff !important;
  font-weight: 500;
}

.premium-input :deep(.el-input__inner::placeholder) {
  color: #64748b;
}

.premium-input :deep(.el-input__icon) {
  color: #94a3b8;
  font-size: 1.1rem;
}

.password-item {
  margin-bottom: 28px;
}

/* Gradient Button */
.register-btn-gradient {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #ff9f1c 0%, #f67200 100%) !important;
  border: none !important;
  font-weight: 700;
  font-size: 1.05rem;
  border-radius: 14px;
  box-shadow: 0 8px 25px rgba(246, 114, 0, 0.3) !important;
  transition: all 0.3s ease;
  color: #fff !important;
}

.register-btn-gradient:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(246, 114, 0, 0.45) !important;
}

/* Extra Actions */
.extra-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.82rem;
  margin-top: 18px;
  padding: 0 5px;
}

.terms {
  color: #64748b;
}

.terms a {
  color: #94a3b8;
  text-decoration: none;
  transition: color 0.3s;
}

.terms a:hover {
  color: #ff9f1c;
}

.go-login {
  color: #94a3b8;
  text-decoration: none;
  white-space: nowrap;
}

.go-login span {
  color: #ff9f1c;
  font-weight: 700;
  transition: color 0.3s;
}

.go-login:hover span {
  color: #f67200;
}

/* Animating gradient decors */
@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-15px); }
  100% { transform: translateY(0px); }
}

.animate-bg {
  animation: float 8s ease-in-out infinite;
}

.animate-bg-2 {
  animation: float 10s ease-in-out infinite;
}

.animate-fade-in {
  animation: fadeIn 0.8s cubic-bezier(0.165, 0.84, 0.44, 1) forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
