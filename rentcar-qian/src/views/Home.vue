<template>
  <div class="home-container">
    <el-container>
      <!-- Header -->
      <el-header class="header">
        <div class="logo">
          <h2>悟空租车</h2>
        </div>
        <div class="nav">
          <el-menu mode="horizontal" :default-active="activeIndex" router>
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
        <!-- Hero Section -->
        <div class="hero">
          <h1>开启您的自驾之旅</h1>
          <p>海量车型，信用免押，优质服务</p>
          
          <!-- Search Box -->
          <el-card class="search-card">
            <el-form :inline="true" class="search-form">
              <el-form-item label="取车城市">
                <el-select placeholder="请选择城市" style="width: 150px">
                  <el-option label="北京" value="beijing"></el-option>
                  <el-option label="上海" value="shanghai"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="取还车时间">
                <el-date-picker
                  type="daterange"
                  range-separator="至"
                  start-placeholder="取车日期"
                  end-placeholder="还车日期"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large">立即去选车</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>

        <!-- Car List (Placeholder) -->
        <div class="car-recommend">
          <h3>热门车型推荐</h3>
          <el-row :gutter="20">
            <el-col :span="6" v-for="i in 4" :key="i">
              <el-card shadow="hover" class="car-card">
                <div class="car-img-placeholder"></div>
                <div class="car-info">
                  <h4>大众 迈腾</h4>
                  <p class="car-desc">经济型 | 5座 | 自动挡</p>
                  <div class="car-price">
                    <span class="price">¥ 158</span> / 日起
                  </div>
                </div>
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
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store';
import { ArrowDown } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();

const activeIndex = ref('/');

const isLoggedIn = computed(() => !!userStore.token);

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
  } else if (command === 'profile') {
    // router.push('/profile');
  }
};
</script>

<style scoped>
.home-container {
  min-height: 100vh;
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
  padding: 0;
}
.hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: center;
  padding: 80px 20px 120px 20px;
  position: relative;
}
.hero h1 {
  font-size: 3rem;
  margin-bottom: 10px;
}
.hero p {
  font-size: 1.2rem;
  margin-bottom: 40px;
}
.search-card {
  position: absolute;
  bottom: -40px;
  left: 50%;
  transform: translateX(-50%);
  width: 80%;
  border-radius: 8px;
}
.search-form {
  display: flex;
  justify-content: center;
  align-items: center;
}
.car-recommend {
  padding: 80px 10%;
}
.car-recommend h3 {
  font-size: 1.8rem;
  margin-bottom: 30px;
  color: #333;
}
.car-card {
  border-radius: 8px;
  text-align: left;
}
.car-img-placeholder {
  width: 100%;
  height: 150px;
  background-color: #e4e7ed;
  border-radius: 4px;
}
.car-info h4 {
  margin: 15px 0 5px 0;
  font-size: 1.2rem;
}
.car-desc {
  color: #909399;
  font-size: 0.9rem;
  margin-bottom: 15px;
}
.car-price .price {
  color: #f56c6c;
  font-size: 1.5rem;
  font-weight: bold;
}
.footer {
  text-align: center;
  color: #909399;
  padding: 20px;
  background-color: #fff;
  margin-top: 20px;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
  display: flex;
  align-items: center;
}
</style>
