<template>
  <div class="profile-container">
    <el-container>
      <!-- Header -->
      <el-header class="header">
        <div class="logo-wrapper" @click="$router.push('/')">
          <div class="logo-icon">
            <el-icon><Van /></el-icon>
          </div>
          <span class="logo-text">悟空租车</span>
        </div>
        <div class="nav">
          <el-menu mode="horizontal" default-active="/profile" router class="custom-menu">
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/orders" v-if="isLoggedIn">我的订单</el-menu-item>
            <el-menu-item index="/admin" v-if="userStore.isAdmin">管理后台</el-menu-item>
          </el-menu>
        </div>
        <div class="user-action">
          <el-button type="primary" class="gradient-btn" v-if="!isLoggedIn" @click="$router.push('/login')">
            登录 / 注册
          </el-button>
          <el-dropdown v-else @command="handleCommand" trigger="click">
            <span class="el-dropdown-link">
              <el-avatar :size="32" class="user-avatar">{{ userStore.userInfo?.username?.substring(0,1) || 'U' }}</el-avatar>
              <span class="username-display">{{ userStore.userInfo?.username }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown-menu">
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided class="logout-item">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Main Content -->
      <el-main class="main-content">
        <div class="profile-wrapper animate-fade-in">
          <div class="section-title-box">
            <span class="sub-title">PERSONAL VIP MEMBER CENTER</span>
            <h2 class="page-title">尊享会员中心</h2>
          </div>

          <el-row :gutter="30" class="profile-grid">
            <!-- 左侧信用信息：极光仪表盘卡片 -->
            <el-col :xs="24" :md="8">
              <el-card class="premium-credit-card" v-loading="loadingCredit">
                <div class="credit-card-glow"></div>
                <div class="credit-header">
                  <span class="credit-title-eng">CREDIT WAIVER</span>
                  <h3>芝麻信用免押</h3>
                </div>
                <div class="credit-dashboard" v-if="creditInfo">
                  <div class="gauge-box">
                    <div class="gauge-arc" :class="{'high-score-arc': creditInfo.creditScore >= 700}">
                      <h1 class="score-display">{{ creditInfo.creditScore }}</h1>
                      <span class="score-label">芝麻信用分</span>
                    </div>
                  </div>
                  <div class="privilege-box">
                    <div class="privilege-badge" v-if="creditInfo.isDepositWaived">
                      <el-icon class="badge-icon"><CircleCheck /></el-icon>
                      <span class="badge-text">恭喜！您已点亮双免押特权</span>
                    </div>
                    <div class="privilege-badge denied" v-else>
                      <el-icon class="badge-icon"><Warning /></el-icon>
                      <span class="badge-text">信用不足 700 分，暂无免押特权</span>
                    </div>
                    <p class="credit-desc">
                      芝麻分满 700 分及以上自动享有免预授权车损押金与违章押金服务，极速取车，自驾无忧。
                    </p>
                  </div>
                </div>
                <div v-else class="empty-state-box">
                  <el-empty description="暂无相关信用评估数据" />
                </div>
              </el-card>
            </el-col>

            <!-- 右侧实名认证与驾照物理上传 -->
            <el-col :xs="24" :md="16">
              <el-card class="premium-form-card">
                <div class="form-card-header">
                  <h3>实名制与驾驶执照认证</h3>
                  <p>根据《中华人民共和国网络安全法》及道路安全管理规定，租车出行前须完成实名及有效驾照核验。</p>
                </div>
                
                <el-form :model="certForm" ref="certFormRef" label-position="top" class="premium-custom-form">
                  <el-row :gutter="20">
                    <el-col :span="24">
                      <el-form-item label="真实身份证件号 (18位)">
                        <!-- 编辑模式下显示输入框 -->
                        <el-input 
                          v-if="isEditing"
                          v-model="certForm.idCardNo" 
                          placeholder="请输入您的 18 位身份证号码" 
                          size="large"
                          class="premium-input">
                        </el-input>
                        <!-- 认证成功且处于非编辑模式下显示打码输入框 -->
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
                      </el-form-item>
                    </el-col>
                  </el-row>

                  <el-form-item label="中华人民共和国机动车驾驶证 (主页图像)">
                    <!-- 编辑模式下显示上传区域，支持上传时预览 -->
                    <div class="uploader-wrapper" v-if="isEditing">
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
                    </div>
                    
                    <!-- 认证成功且处于非编辑状态下，隐藏照片展示，代以高度拟物化的安全保险库卡片 -->
                    <div class="secure-encrypted-card" v-else>
                      <div class="secure-glow"></div>
                      <el-icon class="secure-lock-icon"><Lock /></el-icon>
                      <div class="secure-info">
                        <h4>机动车驾驶证已安全加密托管</h4>
                        <p>为保障您的个人隐私安全，平台已对您的原始驾驶证文件进行金融级加密处理，不可直接预览。</p>
                      </div>
                    </div>
                  </el-form-item>

                  <el-form-item class="form-action-row">
                    <!-- 编辑状态：显示提交按钮 -->
                    <el-button 
                      v-if="isEditing"
                      type="warning" 
                      @click="submitCertify" 
                      :loading="submitLoading" 
                      class="submit-btn-gradient">
                      提交权威机构实人验证
                    </el-button>
                    <!-- 认证成功且非编辑状态：显示勋章及重新认证按钮 -->
                    <div class="certified-banner-row" v-else>
                      <div class="certified-badge">
                        <el-icon class="badge-icon"><CircleCheck /></el-icon>
                        <span class="badge-text">已完成尊享实名及驾驶执照核验，享有完整租车资格</span>
                      </div>
                      <el-button @click="startReCertify" class="re-verify-btn">
                        重新核验登记
                      </el-button>
                    </div>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-main>

      <!-- Footer -->
      <el-footer class="footer">
        <div class="footer-content">
          <p>© 2026 悟空尊享出行 版权所有 · 极致品质 自驾首选</p>
          <div class="footer-links">
            <a href="#">服务条款</a>
            <span class="divider">|</span>
            <a href="#">隐私协议</a>
            <span class="divider">|</span>
            <a href="#">常见问题</a>
          </div>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store';
import { ArrowDown, Van, CircleCheck, Warning, Plus, Edit, View, Hide, Lock } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import request from '../utils/request';

const router = useRouter();
const userStore = useUserStore();

const isLoggedIn = computed(() => userStore.isLoggedIn);
const loadingCredit = ref(false);
const submitLoading = ref(false);
const creditInfo = ref(null);
const isEditing = ref(false);
const showRawIdCard = ref(false);

const certForm = ref({
  idCardNo: '',
  driverLicenseUrl: ''
});

const toggleIdCardVisibility = () => {
  showRawIdCard.value = !showRawIdCard.value;
};

const maskIdCard = (idCard) => {
  if (!idCard) return '';
  if (idCard.length !== 18) return idCard;
  return idCard.substring(0, 6) + '********' + idCard.substring(14);
};

const displayedIdCard = computed(() => {
  if (isEditing.value) {
    return certForm.value.idCardNo;
  }
  return showRawIdCard.value ? certForm.value.idCardNo : maskIdCard(certForm.value.idCardNo);
});

const startReCertify = () => {
  isEditing.value = true;
  certForm.value.idCardNo = '';
  certForm.value.driverLicenseUrl = '';
};

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
    ElMessage.success('已安全退出登录');
  } else if (command === 'profile') {
    router.push('/profile');
  }
};

const fetchCreditInfo = async () => {
  loadingCredit.value = true;
  try {
    const data = await request.get('/user/credit-info');
    creditInfo.value = data;
    // 回显已经认证的信息
    if (data && data.idCardNo) {
      certForm.value.idCardNo = data.idCardNo;
    }
    if (data && data.driverLicenseUrl) {
      certForm.value.driverLicenseUrl = data.driverLicenseUrl;
    }
    // 如果已经认证通过，则默认展示安全锁状态(非编辑状态)，否则允许编辑
    if (data && data.auditStatus === 2) {
      isEditing.value = false;
    } else {
      isEditing.value = true;
    }
  } catch (error) {
    console.error(error);
  } finally {
    loadingCredit.value = false;
  }
};

// 物理驾照图片上传
const uploadLicenseImage = async (options) => {
  const file = options.file;
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    const res = await request.post('/upload', formData);
    // 修复图片无法显示的Bug：后台返回的是包含 url 的对象 {"url": "..."}，我们需要提取 res.url 字符串
    certForm.value.driverLicenseUrl = res.url || res;
    ElMessage.success('驾驶执照图片上传并解析成功，请提交验证保存！');
  } catch (error) {
    console.error('Upload failed', error);
    ElMessage.error('文件上传失败，请稍后重试');
  }
};

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

const submitCertify = async () => {
  if (!certForm.value.idCardNo) {
    ElMessage.warning('请输入您的18位身份证号');
    return;
  }
  const idCardPattern = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/;
  if (!idCardPattern.test(certForm.value.idCardNo)) {
    ElMessage.warning('请输入合法格式的身份证号码');
    return;
  }
  if (!certForm.value.driverLicenseUrl) {
    ElMessage.warning('请上传您的机动车驾驶证图片');
    return;
  }

  submitLoading.value = true;
  try {
    await request.post('/user/certify', certForm.value);
    ElMessage.success('实名认证与驾驶执照权威核验已全部通过！恭喜您享有完整租车资格');
    fetchCreditInfo(); // 刷新数据
  } catch (error) {
    console.error(error);
  } finally {
    submitLoading.value = false;
  }
};

onMounted(() => {
  if (!isLoggedIn.value) {
    router.push('/login');
  } else {
    fetchCreditInfo();
  }
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap');

.profile-container {
  min-height: 100vh;
  background-color: #f7f9fc;
  font-family: 'Outfit', 'PingFang SC', sans-serif;
}

/* Header */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  padding: 0 6%;
  height: 70px !important;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.02);
}

.logo-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 10px;
}

.logo-icon {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  color: #ff9f1c;
  padding: 8px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(255, 159, 28, 0.2);
}

.logo-text {
  font-size: 1.4rem;
  font-weight: 700;
  background: linear-gradient(120deg, #0f172a 30%, #ff9f1c 90%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 0.5px;
}

.nav {
  flex: 1;
  margin-left: 60px;
}

.custom-menu {
  background: transparent !important;
  border-bottom: none !important;
}

.custom-menu :deep(.el-menu-item) {
  font-size: 0.95rem;
  font-weight: 500;
  color: #475569 !important;
  border-bottom: none !important;
  padding: 0 20px;
  transition: all 0.3s ease;
}

.custom-menu :deep(.el-menu-item.is-active) {
  color: #ff9f1c !important;
  font-weight: 700;
}

.gradient-btn {
  background: linear-gradient(135deg, #ff9f1c 0%, #f67200 100%);
  border: none;
  font-weight: 600;
  border-radius: 10px;
  padding: 10px 24px;
  box-shadow: 0 6px 20px rgba(246, 114, 0, 0.25);
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 12px;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.02);
  transition: all 0.3s;
}

.user-avatar {
  background: linear-gradient(135deg, #ff9f1c, #f67200);
  font-weight: bold;
}

.username-display {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1e293b;
}

/* Main Content */
.main-content {
  padding: 60px 0;
  display: flex;
  justify-content: center;
}

.profile-wrapper {
  width: 88%;
  max-width: 1200px;
}

.section-title-box {
  text-align: left;
  border-left: 5px solid #ff9f1c;
  padding-left: 20px;
  margin-bottom: 35px;
}

.sub-title {
  font-size: 0.75rem;
  font-weight: 700;
  color: #ff9f1c;
  letter-spacing: 3px;
  text-transform: uppercase;
  margin-bottom: 5px;
  display: block;
}

.page-title {
  font-size: 2.2rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
}

/* Credit Panel - Aurora Theme */
.premium-credit-card {
  border-radius: 24px !important;
  background-color: #0f172a !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.15) !important;
  padding: 24px 15px;
  color: #fff;
  position: relative;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.credit-card-glow {
  position: absolute;
  top: -20%;
  left: -20%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, rgba(255, 159, 28, 0.2) 0%, rgba(255, 159, 28, 0) 70%);
  border-radius: 50%;
  pointer-events: none;
}

.credit-header {
  text-align: left;
  margin-bottom: 30px;
  z-index: 2;
}

.credit-title-eng {
  font-size: 0.7rem;
  font-weight: 700;
  color: #ff9f1c;
  letter-spacing: 2px;
}

.credit-header h3 {
  font-size: 1.5rem;
  font-weight: 800;
  margin: 4px 0 0 0;
  color: #ffffff;
}

.credit-dashboard {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  z-index: 2;
}

.gauge-box {
  width: 190px;
  height: 190px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 25px;
}

.gauge-arc {
  width: 170px;
  height: 170px;
  border-radius: 50%;
  border: 4px solid rgba(255, 255, 255, 0.05);
  border-top-color: #ff4d4f;
  border-right-color: #ff9f1c;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 30px rgba(255, 159, 28, 0.1) inset;
  transition: all 0.5s;
}

.high-score-arc {
  border-top-color: #22c55e !important;
  border-right-color: #10b981 !important;
  box-shadow: 0 0 30px rgba(34, 197, 94, 0.1) inset !important;
}

.score-display {
  font-size: 3.5rem;
  font-weight: 900;
  margin: 0;
  line-height: 1.1;
  background: linear-gradient(120deg, #ffffff 40%, #ff9f1c 90%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.score-label {
  font-size: 0.75rem;
  color: #94a3b8;
  font-weight: 600;
  letter-spacing: 1px;
}

.privilege-box {
  text-align: center;
  width: 100%;
}

.privilege-badge {
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.2);
  color: #4ade80;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 18px;
  border-radius: 20px;
  font-weight: 700;
  font-size: 0.88rem;
  margin-bottom: 20px;
}

.privilege-badge.denied {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: #fca5a5;
}

.badge-icon {
  font-size: 1.1rem;
}

.credit-desc {
  font-size: 0.82rem;
  color: #94a3b8;
  line-height: 1.6;
  margin: 0;
}

.empty-state-box {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

/* Right Premium Form Panel */
.premium-form-card {
  border-radius: 24px !important;
  border: 1px solid rgba(0, 0, 0, 0.04) !important;
  box-shadow: 0 10px 40px rgba(15, 23, 42, 0.03) !important;
  padding: 30px;
  background-color: #ffffff;
  text-align: left;
}

.form-card-header {
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 20px;
  margin-bottom: 30px;
}

.form-card-header h3 {
  font-size: 1.4rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 6px 0;
}

.form-card-header p {
  font-size: 0.85rem;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.premium-custom-form {
  margin-top: 10px;
}

.premium-custom-form :deep(.el-form-item__label) {
  color: #334155;
  font-weight: 700;
  font-size: 0.9rem;
  margin-bottom: 8px;
}

.premium-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  background-color: #f8fafc;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.05) inset;
  padding: 10px 15px;
  height: 50px;
  transition: all 0.3s;
}

.premium-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #ff9f1c inset !important;
  background-color: #ffffff !important;
}

/* Driver License Uploader */
.uploader-wrapper {
  width: 100%;
}

.license-uploader {
  width: 100%;
}

.license-uploader :deep(.el-upload) {
  width: 100%;
  border: 2px dashed #cbd5e1;
  border-radius: 16px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  background-color: #f8fafc;
}

.license-uploader :deep(.el-upload:hover) {
  border-color: #ff9f1c;
  background-color: rgba(255, 159, 28, 0.02);
}

.uploader-placeholder {
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.uploader-icon {
  font-size: 2rem;
  color: #94a3b8;
  background-color: #fff;
  padding: 14px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: all 0.3s;
}

.license-uploader :deep(.el-upload:hover) .uploader-icon {
  color: #ff9f1c;
  transform: translateY(-2px);
}

.uploader-text h4 {
  font-size: 1.05rem;
  font-weight: 700;
  color: #334155;
  margin: 0 0 4px 0;
}

.uploader-text p {
  font-size: 0.8rem;
  color: #94a3b8;
  margin: 0;
}

.uploaded-preview-box {
  width: 100%;
  height: 240px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.license-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(4px);
  opacity: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  gap: 10px;
  transition: opacity 0.3s;
}

.uploaded-preview-box:hover .upload-mask {
  opacity: 1;
}

.mask-icon {
  font-size: 1.5rem;
}

.form-action-row {
  margin-top: 40px !important;
  margin-bottom: 0 !important;
}

.submit-btn-gradient {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #ff9f1c 0%, #f67200 100%) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 700;
  font-size: 1.05rem;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(246, 114, 0, 0.25) !important;
  transition: all 0.3s;
}

.submit-btn-gradient:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 25px rgba(246, 114, 0, 0.35) !important;
}

/* Footer */
.footer {
  background-color: #0b0f19;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  padding: 40px 20px;
  height: auto !important;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.footer p {
  color: #64748b;
  margin: 0;
  font-size: 0.9rem;
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 15px;
}

.footer-links a {
  color: #94a3b8;
  text-decoration: none;
  font-size: 0.85rem;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #ff9f1c;
}

.divider {
  color: rgba(255, 255, 255, 0.1);
}

/* Animations */
.animate-fade-in {
  animation: fadeIn 0.8s cubic-bezier(0.165, 0.84, 0.44, 1) forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(15px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 隐私安全防护样式 */
.readonly-masked-input :deep(.el-input__wrapper) {
  background-color: rgba(0, 0, 0, 0.02) !important;
  box-shadow: 0 0 0 1px #e2e8f0 inset !important;
}

.readonly-masked-input :deep(.el-input__inner) {
  letter-spacing: 2px;
  font-family: 'Consolas', 'Courier New', monospace;
  font-weight: 600;
  color: #334155 !important;
}

.toggle-visibility-icon {
  transition: all 0.3s;
}

.toggle-visibility-icon:hover {
  color: #ff9f1c !important;
  transform: scale(1.15);
}

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
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.secure-info h4 {
  font-size: 1.05rem;
  font-weight: 700;
  color: #fff;
  margin: 0 0 6px 0;
  letter-spacing: 0.5px;
}

.secure-info p {
  font-size: 0.8rem;
  color: #94a3b8;
  margin: 0;
  max-width: 420px;
  line-height: 1.5;
}

.certified-banner-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 15px;
  width: 100%;
  flex-wrap: wrap;
}

.certified-badge {
  background: rgba(34, 197, 94, 0.05);
  border: 1px solid rgba(34, 197, 94, 0.15);
  color: #16a34a;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 700;
  flex: 1;
  text-align: left;
}

.certified-badge .badge-icon {
  font-size: 1.1rem;
  flex-shrink: 0;
}

.re-verify-btn {
  border-radius: 12px !important;
  height: 40px;
  font-weight: 600;
  border-color: #cbd5e1 !important;
  transition: all 0.3s;
  background-color: #fff !important;
  color: #64748b !important;
}

.re-verify-btn:hover {
  color: #ff9f1c !important;
  border-color: #ff9f1c !important;
  background-color: rgba(255, 159, 28, 0.02) !important;
}
</style>
