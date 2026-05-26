<template>
  <div class="home-container">
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
          <el-menu mode="horizontal" :default-active="activeIndex" router class="custom-menu">
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
        <!-- Hero Section -->
        <div class="hero-section">
          <div class="hero-bg-overlay"></div>
          <div class="hero-content">
            <h1 class="hero-title animate-fade-in">极尚自驾 · 随心而行</h1>
            <p class="hero-subtitle animate-fade-in-delayed">海量高端车型，信用免押金，专业管家级尊享服务</p>
            
            <!-- Search Box -->
            <el-card class="search-card-glass animate-slide-up">
              <el-form :inline="true" class="search-form" :model="searchParams">
                <el-form-item label="取还车门店" class="custom-form-item">
                  <el-select v-model="searchParams.storeId" placeholder="请选择服务门店" style="width: 280px">
                    <el-option v-for="store in stores" :key="store.id" :label="formatStoreLabel(store)" :value="store.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="租车时间段" class="custom-form-item">
                  <el-date-picker
                    v-model="searchParams.timeRange"
                    type="datetimerange"
                    range-separator="至"
                    start-placeholder="取车时间"
                    end-placeholder="还车时间"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 380px"
                  />
                </el-form-item>
                <el-form-item class="search-btn-item">
                  <el-button type="warning" class="search-btn" size="large" @click="goSelectCar">
                    立即开启旅程
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </div>
        </div>

        <!-- Car List Section -->
        <div class="car-recommend">
          <div class="section-header">
            <div class="title-area">
              <span class="sub-title">RECOMMENDED VEHICLES</span>
              <h3 class="main-title">甄选热门车型</h3>
            </div>
            <div class="filter-area">
              <el-radio-group v-model="selectedCity" @change="handleCityChange" class="custom-radio-group">
                <el-radio-button label="">全国</el-radio-button>
                <el-radio-button v-for="city in cities" :key="city" :label="city">{{ city }}</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          
          <el-row :gutter="30" v-if="recommendCars.length > 0" class="car-grid">
            <el-col :xs="24" :sm="12" :md="12" :lg="8" v-for="car in recommendCars" :key="car.id">
              <el-card shadow="hover" class="car-card-premium" :body-style="{ padding: '0px' }" @click="openCarDetail(car)">
                <div class="car-image-container">
                  <el-image :src="car.mainImage" fit="cover" class="car-premium-img" v-if="car.mainImage">
                    <template #placeholder>
                      <div class="image-loader">加载中...</div>
                    </template>
                  </el-image>
                  <div class="car-img-placeholder" v-else>
                    <el-icon :size="40"><Van /></el-icon>
                    <span>暂无精美外观图</span>
                  </div>
                  <div class="car-tag">{{ car.carType || '豪华型' }}</div>
                </div>
                <div class="car-info-premium">
                  <h4 class="car-brand-name">{{ car.brandSeries }}</h4>
                  <div class="car-specs">
                    <span><el-icon><User /></el-icon> {{ car.seatsDoors || '5座' }}</span>
                    <span class="spec-divider">|</span>
                    <span><el-icon><Location /></el-icon> {{ car.locationCity || '上海' }}</span>
                  </div>
                  <div class="car-footer-meta">
                    <div class="price-box">
                      <span class="currency">¥</span>
                      <span class="price-val">{{ car.dailyPrice || 199 }}</span>
                      <span class="unit">/日</span>
                    </div>
                    <el-button type="text" class="book-now-btn">
                      立即预订 <el-icon><ArrowRight /></el-icon>
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty description="暂无推荐车型" v-else class="custom-empty"></el-empty>

          <!-- 分页器 -->
          <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              background
              layout="total, prev, pager, next"
              :total="total"
              @current-change="loadRecommendCars"
              class="premium-pagination"
            />
          </div>
        </div>

        <!-- Service Features -->
        <div class="features-section">
          <el-row :gutter="40">
            <el-col :span="8">
              <div class="feature-item">
                <div class="feature-icon"><el-icon><Finished /></el-icon></div>
                <h4>信用免押金</h4>
                <p>芝麻信用分达标尊享双免押金特权，无忧出行</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <div class="feature-icon"><el-icon><Van /></el-icon></div>
                <h4>极速送车上门</h4>
                <p>专业司管极速送车至指定位置，省时省力又省心</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <div class="feature-icon"><el-icon><Service /></el-icon></div>
                <h4>24小时尊享客服</h4>
                <p>专属车管家全程保驾护航，遇到问题极速响应</p>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-main>

      <!-- 车型详情弹窗 (首页) -->
      <el-dialog v-model="detailDialogVisible" title="臻选车型规格" width="550px" append-to-body class="premium-dialog">
        <div v-if="detailCar" class="car-detail-popup">
          <el-image :src="detailCar.mainImage" fit="cover" class="detail-image" />
          <div class="detail-info-section">
            <h3 class="detail-brand">{{ detailCar.brandSeries }}</h3>
            <div class="detail-desc-list">
              <div class="detail-desc-item">
                <span class="label">车辆类型</span>
                <span class="val">{{ detailCar.carType }}</span>
              </div>
              <div class="detail-desc-item">
                <span class="label">配置规格</span>
                <span class="val">{{ detailCar.seatsDoors }}</span>
              </div>
              <div class="detail-desc-item">
                <span class="label">当前区域</span>
                <span class="val">
                  <el-tag type="success" effect="light" class="custom-tag">{{ detailCar.locationCity || '上海市' }}</el-tag>
                </span>
              </div>
              <div class="detail-desc-item">
                <span class="label">车辆牌照</span>
                <span class="val">
                  <el-tag type="info" effect="light" class="custom-tag">{{ detailCar.licensePlate || '沪A·88888' }}</el-tag>
                </span>
              </div>
              <div class="detail-desc-item price-row">
                <span class="label">尊享日租金</span>
                <span class="val price-highlight">¥ {{ detailCar.dailyPrice }} <small>/天起</small></span>
              </div>
            </div>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer-actions">
            <el-button @click="detailDialogVisible = false" class="cancel-btn">返回</el-button>
            <el-button type="warning" @click="bookFromHomeDetail(detailCar)" class="confirm-btn">立即去选车预订</el-button>
          </div>
        </template>
      </el-dialog>

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
import { ArrowDown, Van, User, Location, ArrowRight, Finished, Service } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import request from '../utils/request';

const router = useRouter();
const userStore = useUserStore();

const activeIndex = ref('/');
const recommendCars = ref([]);
const stores = ref([]);
const searchParams = ref({
  storeId: '',
  timeRange: []
});

const selectedCity = ref('');
const cities = computed(() => {
  const list = stores.value.map(s => s.cityName).filter(Boolean);
  return [...new Set(list)];
});

const handleCityChange = () => {
  pagination.value.page = 1;
  loadRecommendCars();
  
  if (selectedCity.value) {
    const storeInCity = stores.value.find(s => s.cityName === selectedCity.value);
    if (storeInCity) {
      searchParams.value.storeId = storeInCity.id;
    }
  } else if (stores.value.length > 0) {
    searchParams.value.storeId = stores.value[0].id;
  }
};

const pagination = ref({
  page: 1,
  pageSize: 8
});
const total = ref(0);

const detailDialogVisible = ref(false);
const detailCar = ref(null);

const openCarDetail = (car) => {
  detailCar.value = car;
  detailDialogVisible.value = true;
};

const bookFromHomeDetail = (car) => {
  detailDialogVisible.value = false;
  
  let targetStoreId = car.storeId;
  
  if (!targetStoreId) {
    targetStoreId = searchParams.value.storeId;
    if (car.locationCity) {
      const storeInCarCity = stores.value.find(s => s.cityName === car.locationCity);
      if (storeInCarCity) {
        targetStoreId = storeInCarCity.id;
      }
    }
  }

  if (!targetStoreId || !searchParams.value.timeRange || searchParams.value.timeRange.length !== 2) {
    ElMessage.warning('请选择完整的取还车时间');
    return;
  }
  
  router.push({
    path: '/search',
    query: {
      storeId: targetStoreId,
      startTime: searchParams.value.timeRange[0],
      endTime: searchParams.value.timeRange[1],
      autoBookModelId: car.id
    }
  });
};

const isLoggedIn = computed(() => userStore.isLoggedIn);

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
    ElMessage.success('已安全退出登录');
  } else if (command === 'profile') {
    router.push('/profile');
  }
};

const loadRecommendCars = async () => {
  try {
    const res = await request.get('/car/model/list', {
      params: {
        page: pagination.value.page,
        pageSize: pagination.value.pageSize,
        cityName: selectedCity.value || undefined
      }
    });
    recommendCars.value = res.records || [];
    total.value = res.total || 0;
  } catch (error) {
    console.error('Failed to load recommend cars', error);
  }
};

const loadStores = async () => {
  try {
    const res = await request.get('/store/list', { params: { page: 1, pageSize: 100 } });
    stores.value = res.records || [];
    if (stores.value.length > 0 && !searchParams.value.storeId) {
      searchParams.value.storeId = stores.value[0].id;
    }
  } catch (error) {
    console.error('Failed to load stores', error);
  }
};

const formatStoreLabel = (store) => {
  return `${store.cityName || ''} · ${store.merchantName || ''} (${store.address || ''})`;
};

const formatDate = (date) => {
  const pad = (n) => n < 10 ? '0' + n : n;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:00`;
};

const initDefaultTimeRange = () => {
  const start = new Date();
  start.setHours(10, 0, 0, 0);
  const end = new Date();
  end.setDate(end.getDate() + 2);
  end.setHours(10, 0, 0, 0);
  searchParams.value.timeRange = [formatDate(start), formatDate(end)];
};

const goSelectCar = () => {
  if (!searchParams.value.storeId || !searchParams.value.timeRange || searchParams.value.timeRange.length !== 2) {
    ElMessage.warning('请选择门店和完整的取还车时间');
    return;
  }
  router.push({
    path: '/search',
    query: {
      storeId: searchParams.value.storeId,
      startTime: searchParams.value.timeRange[0],
      endTime: searchParams.value.timeRange[1]
    }
  });
};

onMounted(() => {
  initDefaultTimeRange();
  loadRecommendCars();
  loadStores();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap');

.home-container {
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

.custom-menu :deep(.el-menu-item:hover) {
  color: #ff9f1c !important;
  background-color: transparent !important;
}

.gradient-btn {
  background: linear-gradient(135deg, #ff9f1c 0%, #f67200 100%);
  border: none;
  font-weight: 600;
  border-radius: 10px;
  padding: 10px 24px;
  box-shadow: 0 6px 20px rgba(246, 114, 0, 0.25);
  transition: all 0.3s ease;
}

.gradient-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(246, 114, 0, 0.35);
  opacity: 0.95;
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

.el-dropdown-link:hover {
  background: rgba(0, 0, 0, 0.05);
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

.custom-dropdown-menu :deep(.el-dropdown-menu__item:not(.is-disabled):focus) {
  background-color: rgba(255, 159, 28, 0.08);
  color: #f67200;
}

.logout-item {
  color: #f56c6c !important;
}

/* Hero Section */
.hero-section {
  position: relative;
  min-height: 480px;
  background-image: url('https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&q=80&w=1920');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px 100px 20px;
}

.hero-bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.85) 0%, rgba(30, 41, 59, 0.6) 100%);
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
  width: 100%;
  max-width: 1200px;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 2px;
  margin-bottom: 12px;
  text-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 1.25rem;
  font-weight: 300;
  color: #e2e8f0;
  margin-bottom: 60px;
  letter-spacing: 1px;
}

/* Search Card Glassmorphism */
.search-card-glass {
  background: rgba(255, 255, 255, 0.85) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4) !important;
  border-radius: 20px !important;
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.15) !important;
  padding: 20px 30px;
  width: 90%;
  max-width: 1000px;
  position: absolute;
  bottom: -45px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
}

.search-form {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: nowrap;
  gap: 15px;
}

.custom-form-item {
  margin-bottom: 0 !important;
  margin-right: 0 !important;
  text-align: left;
}

.custom-form-item :deep(.el-form-item__label) {
  color: #475569;
  font-weight: 600;
  font-size: 0.85rem;
  margin-bottom: 4px;
  display: block;
}

.custom-form-item :deep(.el-input__wrapper) {
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.05) inset;
  padding: 8px 12px;
  height: 48px;
}

.search-btn-item {
  margin-bottom: 0 !important;
  margin-right: 0 !important;
  align-self: flex-end;
}

.search-btn {
  background: linear-gradient(135deg, #ff9f1c 0%, #f67200 100%) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 700 !important;
  font-size: 1rem !important;
  border-radius: 12px !important;
  padding: 0 35px !important;
  height: 48px !important;
  box-shadow: 0 6px 20px rgba(246, 114, 0, 0.3) !important;
  transition: all 0.3s ease !important;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(246, 114, 0, 0.45) !important;
}

/* Recommend Cars */
.car-recommend {
  padding: 80px 6%;
  max-width: 1400px;
  margin: 0 auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 45px;
  border-left: 5px solid #ff9f1c;
  padding-left: 20px;
  flex-wrap: wrap;
  gap: 20px;
}

.title-area {
  text-align: left;
  flex-shrink: 0;
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

.main-title {
  font-size: 2.2rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  white-space: nowrap;
}

.filter-area {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  min-width: 300px;
}

.custom-radio-group {
  display: flex;
  flex-wrap: wrap;
  row-gap: 12px;
  column-gap: 8px;
  justify-content: flex-start;
}

:deep(.custom-radio-group .el-radio-button) {
  margin-bottom: 0;
}

:deep(.custom-radio-group .el-radio-button__inner) {
  border-radius: 12px !important;
  border: 1px solid rgba(15, 23, 42, 0.08) !important;
  background-color: #ffffff !important;
  color: #475569 !important;
  font-weight: 600 !important;
  padding: 10px 18px !important;
  transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1) !important;
  box-shadow: none !important;
}

:deep(.custom-radio-group .el-radio-button__inner:hover) {
  color: #ff9f1c !important;
  border-color: rgba(255, 159, 28, 0.4) !important;
  background-color: rgba(255, 159, 28, 0.02) !important;
}

:deep(.custom-radio-group .el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #ff9f1c 0%, #f67200 100%) !important;
  border-color: #f67200 !important;
  color: #ffffff !important;
  box-shadow: 0 6px 18px rgba(246, 114, 0, 0.25) !important;
}

:deep(.custom-radio-group .el-radio-button:first-child .el-radio-button__inner) {
  border-left: 1px solid rgba(15, 23, 42, 0.08) !important;
}

/* Premium Car Card */
.car-card-premium {
  border-radius: 20px !important;
  border: 1px solid rgba(0, 0, 0, 0.04) !important;
  background-color: #ffffff;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
  margin-bottom: 30px;
  cursor: pointer;
}

.car-card-premium:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.08);
  border-color: rgba(255, 159, 28, 0.2) !important;
}

.car-image-container {
  position: relative;
  width: 100%;
  height: 180px;
  background-color: #f1f5f9;
  overflow: hidden;
}

.car-premium-img {
  width: 100%;
  height: 100%;
  transition: transform 0.6s ease;
}

.car-card-premium:hover .car-premium-img {
  transform: scale(1.05);
}

.car-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  gap: 10px;
}

.car-tag {
  position: absolute;
  top: 15px;
  right: 15px;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(5px);
  color: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
}

.car-info-premium {
  padding: 24px;
  text-align: left;
}

.car-brand-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 10px 0;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.car-specs {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 0.85rem;
  margin-bottom: 20px;
}

.spec-divider {
  color: #e2e8f0;
}

.car-footer-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f1f5f9;
  padding-top: 18px;
}

.price-box {
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: 0.9rem;
  font-weight: 700;
  color: #f67200;
  margin-right: 2px;
}

.price-val {
  font-size: 1.6rem;
  font-weight: 800;
  color: #f67200;
}

.unit {
  font-size: 0.75rem;
  color: #94a3b8;
  margin-left: 2px;
}

.book-now-btn {
  color: #ff9f1c !important;
  font-weight: 700;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 4px;
}

.book-now-btn:hover {
  color: #f67200 !important;
}

/* Features */
.features-section {
  background-color: #0f172a;
  color: #fff;
  padding: 80px 10%;
  margin-top: 60px;
}

.feature-item {
  text-align: center;
  padding: 0 20px;
}

.feature-icon {
  background: rgba(255, 159, 28, 0.1);
  color: #ff9f1c;
  width: 70px;
  height: 70px;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  margin: 0 auto 25px auto;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
}

.feature-item h4 {
  font-size: 1.3rem;
  font-weight: 700;
  margin-bottom: 12px;
}

.feature-item p {
  color: #94a3b8;
  font-size: 0.95rem;
  line-height: 1.6;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.premium-pagination :deep(.el-pager li) {
  border-radius: 8px;
  margin: 0 3px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  background: #fff;
  color: #64748b;
}

.premium-pagination :deep(.el-pager li.is-active) {
  background: #ff9f1c !important;
  color: #fff !important;
  border-color: #ff9f1c !important;
}

/* Premium Dialog */
.premium-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.15);
}

.premium-dialog :deep(.el-dialog__header) {
  padding: 24px 30px;
  border-bottom: 1px solid #f1f5f9;
  margin-right: 0;
}

.premium-dialog :deep(.el-dialog__title) {
  font-weight: 700;
  color: #0f172a;
}

.car-detail-popup {
  display: flex;
  flex-direction: column;
}

.detail-image {
  width: 100%;
  height: 260px;
  border-radius: 12px;
  object-fit: cover;
  margin-bottom: 25px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
}

.detail-brand {
  font-size: 1.6rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 20px 0;
}

.detail-desc-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-desc-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px dashed #e2e8f0;
}

.detail-desc-item .label {
  color: #64748b;
  font-weight: 500;
}

.detail-desc-item .val {
  color: #0f172a;
  font-weight: 700;
}

.custom-tag {
  border-radius: 8px;
  font-weight: 600;
}

.price-row {
  border-bottom: none;
  padding-top: 20px;
  align-items: center;
}

.price-highlight {
  font-size: 2rem;
  font-weight: 800;
  color: #f67200;
}

.price-highlight small {
  font-size: 0.9rem;
  font-weight: 500;
  color: #94a3b8;
}

.dialog-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 10px 20px;
}

.cancel-btn {
  border-radius: 10px;
  padding: 12px 24px;
}

.confirm-btn {
  background: linear-gradient(135deg, #ff9f1c 0%, #f67200 100%) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 10px;
  font-weight: 700;
  padding: 12px 28px;
  box-shadow: 0 4px 15px rgba(246, 114, 0, 0.3);
}

/* Footer */
.footer {
  background-color: #0b0f19;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  padding: 40px 20px;
  height: auto !important;
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
  animation: fadeIn 1s ease-out forwards;
}

.animate-fade-in-delayed {
  opacity: 0;
  animation: fadeIn 1s ease-out 0.3s forwards;
}

.animate-slide-up {
  opacity: 0;
  animation: slideUp 0.8s cubic-bezier(0.165, 0.84, 0.44, 1) 0.5s forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideUp {
  from { opacity: 0; transform: translate(-50%, 40px); }
  to { opacity: 1; transform: translate(-50%, 0); }
}
</style>
