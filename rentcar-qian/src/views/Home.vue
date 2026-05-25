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
            <el-form :inline="true" class="search-form" :model="searchParams">
              <el-form-item label="取还车门店">
                <el-select v-model="searchParams.storeId" placeholder="请选择门店" style="width: 250px">
                  <el-option v-for="store in stores" :key="store.id" :label="formatStoreLabel(store)" :value="store.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="用车时间">
                <el-date-picker
                  v-model="searchParams.timeRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="取车时间"
                  end-placeholder="还车时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" @click="goSelectCar">立即去选车</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>

        <!-- Car List (Placeholder) -->
        <div class="car-recommend">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px;">
            <h3 style="margin: 0; font-size: 1.8rem; color: #333;">热门车型推荐</h3>
            <el-radio-group v-model="selectedCity" @change="handleCityChange" size="large">
              <el-radio-button label="">全国</el-radio-button>
              <el-radio-button v-for="city in cities" :key="city" :label="city">{{ city }}</el-radio-button>
            </el-radio-group>
          </div>
          <el-row :gutter="20" v-if="recommendCars.length > 0">
            <el-col :span="6" v-for="car in recommendCars" :key="car.id">
              <el-card shadow="hover" class="car-card" @click="openCarDetail(car)" style="cursor: pointer; margin-bottom: 20px;">
                <el-image :src="car.mainImage" fit="cover" style="width: 100%; height: 150px; border-radius: 4px;" v-if="car.mainImage"></el-image>
                <div class="car-img-placeholder" v-else>暂无图片</div>
                <div class="car-info">
                  <h4>{{ car.brandSeries }}</h4>
                  <p class="car-desc">{{ car.carType }} | {{ car.seatsDoors }}</p>
                  <div class="car-price">
                    <span class="price">¥ {{ car.dailyPrice || 0 }}</span> / 日起
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty description="暂无推荐车型" v-else></el-empty>

          <!-- 分页器 -->
          <div class="pagination-wrapper" v-if="total > 0" style="margin-top: 20px; display: flex; justify-content: center;">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :page-sizes="[4, 8, 12, 16]"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="loadRecommendCars"
            />
          </div>
        </div>
      </el-main>

      <!-- 车型详情弹窗 (首页) -->
      <el-dialog v-model="detailDialogVisible" title="车型详情" width="500px" append-to-body>
        <div v-if="detailCar" class="car-detail-popup">
          <el-image :src="detailCar.mainImage" fit="cover" class="detail-image" />
          <div class="detail-info-section">
            <h3 class="detail-brand">{{ detailCar.brandSeries }}</h3>
            <el-descriptions :column="1" border style="margin-top: 15px;">
              <el-descriptions-item label="车辆类型">{{ detailCar.carType }}</el-descriptions-item>
              <el-descriptions-item label="配置规格">{{ detailCar.seatsDoors }}</el-descriptions-item>
              <el-descriptions-item label="车辆所在地">
                <el-tag type="success" effect="plain">{{ detailCar.locationCity || '上海市' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="车牌号">
                <el-tag type="info" effect="plain">{{ detailCar.licensePlate || '暂无车牌' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="起租价格">
                <span class="detail-price">¥ {{ detailCar.dailyPrice }}</span> / 日起
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="detailDialogVisible = false">关闭</el-button>
            <el-button type="success" @click="bookFromHomeDetail(detailCar)">立即去选车预订</el-button>
          </span>
        </template>
      </el-dialog>

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

const handleSizeChange = (size) => {
  pagination.value.pageSize = size;
  pagination.value.page = 1;
  loadRecommendCars();
};

const detailDialogVisible = ref(false);
const detailCar = ref(null);

const openCarDetail = (car) => {
  detailCar.value = car;
  detailDialogVisible.value = true;
};

const bookFromHomeDetail = (car) => {
  detailDialogVisible.value = false;
  
  // 精确匹配：直接使用后端返回的该车型实际所在的门店 ID
  let targetStoreId = car.storeId;
  
  // 降级匹配：如果后端没返回，再根据城市猜
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

const isLoggedIn = computed(() => !!userStore.token);

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
  } else if (command === 'profile') {
    // router.push('/profile');
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
  return `${store.merchantName || ''}${store.address ? ' - ' + store.address : ''}`;
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
.car-detail-popup {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.detail-image {
  width: 100%;
  height: 240px;
  border-radius: 8px;
  object-fit: cover;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.detail-info-section {
  width: 100%;
}
.detail-brand {
  margin: 0 0 15px 0;
  font-size: 1.5rem;
  color: #303133;
}
.detail-price {
  color: #f56c6c;
  font-size: 1.6rem;
  font-weight: bold;
}
</style>
