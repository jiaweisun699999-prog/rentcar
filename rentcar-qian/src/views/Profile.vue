<template>
  <div class="profile-container">
    <el-container>
      <!-- Header -->
      <el-header class="header">
        <div class="logo">
          <h2>悟空租车</h2>
        </div>
        <div class="nav">
          <el-menu mode="horizontal" default-active="/profile" router>
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/orders">我的订单</el-menu-item>
            <el-menu-item index="/admin">管理后台</el-menu-item>
          </el-menu>
        </div>
        <div class="user-action">
          <el-button type="primary" v-if="!isLoggedIn" @click="$router.push('/login')">登录 / 注册</el-button>
          <el-dropdown v-else @command="handleCommand">
            <span class="el-dropdown-link">
              我的账户<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Main Content -->
      <el-main class="main-content">
        <div class="profile-wrapper">
          <el-row :gutter="20">
            <!-- 左侧信用信息 -->
            <el-col :span="8">
              <el-card class="box-card" v-loading="loadingCredit">
                <template #header>
                  <div class="card-header">
                    <span>信用与免押</span>
                  </div>
                </template>
                <div class="credit-info" v-if="creditInfo">
                  <h1 class="score" :class="{'high-score': creditInfo.creditScore >= 700}">{{ creditInfo.creditScore }}</h1>
                  <p>当前芝麻信用分模拟</p>
                  <el-tag :type="creditInfo.isDepositWaived ? 'success' : 'warning'" size="large" style="margin-top: 15px;">
                    {{ creditInfo.isDepositWaived ? '太棒了！您享有免押金特权' : '分值不足，暂无免押特权' }}
                  </el-tag>
                </div>
                <div v-else>
                  <el-empty description="暂无信用信息"></el-empty>
                </div>
              </el-card>
            </el-col>

            <!-- 右侧实名认证 -->
            <el-col :span="16">
              <el-card class="box-card">
                <template #header>
                  <div class="card-header">
                    <span>实名与驾照认证</span>
                  </div>
                </template>
                <el-form :model="certForm" label-width="120px" style="max-width: 500px;">
                  <el-form-item label="身份证号">
                    <el-input v-model="certForm.idCardNo" placeholder="请输入18位身份证号"></el-input>
                  </el-form-item>
                  <el-form-item label="驾照图片URL">
                    <el-input v-model="certForm.driverLicenseUrl" placeholder="请输入驾照图片直链"></el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="submitCertify">提交认证</el-button>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-main>

      <!-- Footer -->
      <el-footer class="footer">
        <p>&copy; 2024 悟空租车 版权所有</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store';
import { ArrowDown } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import request from '../utils/request';

const router = useRouter();
const userStore = useUserStore();

const isLoggedIn = computed(() => !!userStore.token);
const loadingCredit = ref(false);
const creditInfo = ref(null);

const certForm = ref({
  idCardNo: '',
  driverLicenseUrl: ''
});

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
  } else if (command === 'profile') {
    router.push('/profile');
  }
};

const fetchCreditInfo = async () => {
  loadingCredit.value = true;
  try {
    const data = await request.get('/user/credit-info');
    creditInfo.value = data;
  } catch (error) {
    console.error(error);
  } finally {
    loadingCredit.value = false;
  }
};

const submitCertify = async () => {
  if (!certForm.value.idCardNo) {
    ElMessage.warning('请输入身份证号');
    return;
  }
  try {
    await request.post('/user/certify', certForm.value);
    ElMessage.success('实名认证提交成功');
  } catch (error) {
    console.error(error);
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
.profile-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 0 50px;
}
.logo h2 {
  color: #409EFF;
  margin: 0;
}
.nav {
  flex: 1;
  margin-left: 50px;
}
.el-menu {
  border-bottom: none !important;
}
.main-content {
  padding: 40px 0;
  display: flex;
  justify-content: center;
}
.profile-wrapper {
  width: 80%;
  max-width: 1200px;
}
.card-header {
  font-weight: bold;
}
.credit-info {
  text-align: center;
  padding: 20px 0;
}
.score {
  font-size: 4rem;
  margin: 0;
  color: #f56c6c;
}
.high-score {
  color: #67c23a;
}
.footer {
  text-align: center;
  color: #909399;
  padding: 20px;
  background-color: #fff;
  margin-top: auto;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
  display: flex;
  align-items: center;
}
</style>
