<template>
  <div class="search-container">
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
          <el-menu mode="horizontal" default-active="/search" router class="custom-menu">
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

      <el-main class="main-content">
        <!-- Result Header / Search Filter Glassmorphism -->
        <div class="result-header animate-fade-in">
          <div class="filter-controls">
            <div class="title-area">
              <span class="sub-title">SELECT SERVICE STORE</span>
              <h3 class="main-title">可选车门店与时间</h3>
            </div>
            <div class="inputs-row">
              <el-form :inline="true" class="search-form">
                <el-form-item class="custom-form-item">
                  <el-select v-model="searchParams.storeId" @change="fetchAvailableCars" placeholder="请选择服务门店" style="width: 280px;">
                    <el-option v-for="store in stores" :key="store.id" :label="formatStoreLabel(store)" :value="store.id" />
                  </el-select>
                </el-form-item>
                <el-form-item class="custom-form-item">
                  <el-date-picker
                    v-model="searchParams.timeRange"
                    type="datetimerange"
                    range-separator="至"
                    start-placeholder="取车时间"
                    end-placeholder="还车时间"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    @change="fetchAvailableCars"
                    style="width: 380px;"
                  />
                </el-form-item>
              </el-form>
            </div>
          </div>
          <el-button type="info" plain class="back-home-btn" @click="router.push('/')">返回首页</el-button>
        </div>

        <!-- 车辆列表区 -->
        <div class="car-list animate-slide-up" v-loading="loading">
          <el-row :gutter="30" v-if="carList.length > 0">
            <el-col :xs="24" :sm="12" :md="12" :lg="8" v-for="car in carList" :key="car.id">
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
                    <el-button type="warning" class="book-btn-gradient" size="small" @click.stop="openOrderPreview(car)">
                      立即预订
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty description="该时间段暂无可租车型，请重新调整筛选条件" v-else class="custom-empty"></el-empty>
          
          <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              background
              layout="total, prev, pager, next"
              :total="total"
              @current-change="fetchAvailableCars"
              class="premium-pagination"
            />
          </div>
        </div>
      </el-main>

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

    <!-- 车型详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="臻选车型规格" width="550px" class="premium-dialog">
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
          <el-button type="warning" @click="bookFromDetail(detailCar)" class="confirm-btn">立即预订</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 订单预结算弹窗 -->
    <el-dialog v-model="previewDialogVisible" title="核对尊享预订明细" width="550px" class="premium-dialog">
      <div v-loading="previewLoading" class="order-detail-popup">
        <div class="popup-header-status">
          <span class="label">租车时间段</span>
          <span class="date-range-display" v-if="searchParams.timeRange.length === 2">
            {{ searchParams.timeRange[0]?.split(' ')[0] }} 至 {{ searchParams.timeRange[1]?.split(' ')[0] }}
          </span>
        </div>
        
        <div class="bill-details" v-if="previewData">
          <div class="bill-item">
            <span class="bill-label">预订尊享车型</span>
            <span class="bill-val font-bold">{{ selectedCar?.brandSeries }}</span>
          </div>
          <div class="bill-item">
            <span class="bill-label">核算总租期</span>
            <span class="bill-val">{{ previewData.rentDays }} 天</span>
          </div>
          <div class="bill-item">
            <span class="bill-label">车辆基本租金</span>
            <span class="bill-val">¥ {{ previewData.rentFee }}</span>
          </div>
          <div class="bill-item">
            <span class="bill-label">基础安全保障费</span>
            <span class="bill-val">¥ {{ previewData.basicInsuranceFee }}</span>
          </div>
          <div class="bill-item">
            <span class="bill-label">尊享整备手续费</span>
            <span class="bill-val">¥ {{ previewData.handlingFee }}</span>
          </div>
          <div class="bill-item">
            <span class="bill-label">尊享押金冻结</span>
            <span class="bill-val">
              <span v-if="previewData.depositAmount > 0">¥ {{ previewData.depositAmount }}</span>
              <el-tag size="small" type="success" effect="dark" v-else class="custom-tag">芝麻信用 · 免双押</el-tag>
            </span>
          </div>
          
          <div class="bill-divider">预估结算</div>
          
          <div class="bill-item total-row">
            <span class="bill-label">实付总金额</span>
            <span class="bill-val total-price">¥ {{ previewData.totalAmount }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer-actions">
          <el-button @click="previewDialogVisible = false" class="cancel-btn">考虑一下</el-button>
          <el-button type="warning" @click="submitOrder" :disabled="!previewData" class="confirm-btn">确认提交订单</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store';
import { ArrowDown, Van, User, Location } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import request from '../utils/request';

const router = useRouter();
const route = router.currentRoute;
const userStore = useUserStore();
const isLoggedIn = computed(() => userStore.isLoggedIn);

const loading = ref(false);
const stores = ref([]);
const carList = ref([]);
const total = ref(0);
const pagination = ref({
  page: 1,
  pageSize: 8
});

const searchParams = ref({
  storeId: '',
  timeRange: []
});

const formatStoreLabel = (store) => {
  return `${store.cityName || ''} · ${store.merchantName || ''} (${store.address || ''})`;
};

const loadStores = async () => {
  try {
    const res = await request.get('/store/list', { params: { page: 1, pageSize: 100 } });
    stores.value = res.records || [];
  } catch (error) {
    console.error(error);
  }
};

const fetchAvailableCars = async () => {
  if (!searchParams.value.storeId || !searchParams.value.timeRange || searchParams.value.timeRange.length !== 2) {
    ElMessage.warning('请选择门店和完整的取还车时间');
    return;
  }
  loading.value = true;
  try {
    const res = await request.get('/car/model/list', {
      params: {
        storeId: searchParams.value.storeId,
        startTime: searchParams.value.timeRange[0],
        endTime: searchParams.value.timeRange[1],
        page: pagination.value.page,
        pageSize: pagination.value.pageSize
      }
    });
    carList.value = res.records || [];
    total.value = res.total || 0;

    const autoBookId = route.value.query.autoBookModelId;
    if (autoBookId && carList.value.length > 0) {
      const targetCar = carList.value.find(car => String(car.id) === String(autoBookId));
      if (targetCar) {
        setTimeout(() => {
          openOrderPreview(targetCar);
        }, 100);
      }
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const detailDialogVisible = ref(false);
const detailCar = ref(null);

const openCarDetail = (car) => {
  detailCar.value = car;
  detailDialogVisible.value = true;
};

const bookFromDetail = (car) => {
  detailDialogVisible.value = false;
  openOrderPreview(car);
};

const previewDialogVisible = ref(false);
const previewLoading = ref(false);
const previewData = ref(null);
const selectedCar = ref(null);

const openOrderPreview = async (car) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后预订');
    router.push('/login');
    return;
  }
  selectedCar.value = car;
  previewDialogVisible.value = true;
  previewLoading.value = true;
  previewData.value = null;

  try {
    const res = await request.post('/order/preview', {
      carModelId: car.id,
      pickupStoreId: searchParams.value.storeId,
      dropoffStoreId: searchParams.value.storeId,
      startTime: searchParams.value.timeRange[0],
      endTime: searchParams.value.timeRange[1]
    });
    previewData.value = res;
  } catch (error) {
    previewDialogVisible.value = false;
  } finally {
    previewLoading.value = false;
  }
};

const submitOrder = async () => {
  try {
    await request.post('/order/create', {
      carModelId: selectedCar.value.id,
      pickupStoreId: searchParams.value.storeId,
      dropoffStoreId: searchParams.value.storeId,
      startTime: searchParams.value.timeRange[0],
      endTime: searchParams.value.timeRange[1]
    });
    ElMessage.success('订单创建成功！');
    previewDialogVisible.value = false;
    router.push('/orders');
  } catch (error) {
    console.error(error);
  }
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

onMounted(() => {
  const qStoreId = route.value.query.storeId;
  searchParams.value.storeId = qStoreId ? Number(qStoreId) : '';
  searchParams.value.timeRange = [route.value.query.startTime || '', route.value.query.endTime || ''];
  loadStores();
  fetchAvailableCars();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap');

.search-container {
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
  padding: 40px 6%;
  max-width: 1400px;
  margin: 0 auto;
}

/* Result Header / Search Box */
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 24px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.03);
  padding: 24px 35px;
  margin-bottom: 40px;
}

.filter-controls {
  text-align: left;
}

.sub-title {
  font-size: 0.72rem;
  font-weight: 700;
  color: #ff9f1c;
  letter-spacing: 3px;
  text-transform: uppercase;
  margin-bottom: 4px;
  display: block;
}

.main-title {
  font-size: 1.45rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 15px 0;
}

.inputs-row {
  display: flex;
  align-items: center;
}

.search-form {
  display: flex;
  gap: 15px;
  align-items: center;
}

.custom-form-item {
  margin-bottom: 0 !important;
  margin-right: 0 !important;
}

.custom-form-item :deep(.el-input__wrapper) {
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.05) inset;
  padding: 8px 12px;
  height: 48px;
}

.back-home-btn {
  border-radius: 12px;
  font-weight: 600;
  padding: 12px 24px;
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

.book-btn-gradient {
  background: linear-gradient(135deg, #ff9f1c 0%, #f67200 100%) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 700 !important;
  border-radius: 10px !important;
  padding: 8px 18px !important;
  box-shadow: 0 4px 12px rgba(246, 114, 0, 0.2) !important;
  transition: all 0.3s;
}

.book-btn-gradient:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 15px rgba(246, 114, 0, 0.3) !important;
}

.custom-empty {
  padding: 80px 0;
  background-color: #fff;
  border-radius: 24px;
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
  background-color: #0f172a;
  margin-right: 0;
  padding: 20px 24px;
}

.premium-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 700;
}

.premium-dialog :deep(.el-dialog__close) {
  color: #fff;
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

/* Order Detail Popup in preview */
.order-detail-popup {
  display: flex;
  flex-direction: column;
}

.popup-header-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: rgba(255, 159, 28, 0.05);
  border-left: 4px solid #ff9f1c;
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 25px;
  font-weight: 700;
}

.popup-header-status .label {
  color: #475569;
}

.date-range-display {
  color: #ff9f1c;
  font-family: monospace;
}

.bill-details {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.bill-item {
  display: flex;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px dashed #f1f5f9;
}

.bill-label {
  color: #64748b;
  font-weight: 500;
}

.bill-val {
  color: #0f172a;
  font-weight: 700;
  text-align: right;
}

.bill-val.font-bold {
  font-size: 1.1rem;
  color: #ff9f1c;
}

.bill-divider {
  font-size: 0.85rem;
  font-weight: 700;
  color: #94a3b8;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin: 15px 0 5px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.bill-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background-color: #cbd5e1;
}

.total-row {
  border-bottom: none;
  padding-top: 15px;
  align-items: center;
}

.total-price {
  color: #f67200;
  font-size: 1.8rem;
  font-weight: 800;
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
  animation: fadeIn 0.8s cubic-bezier(0.165, 0.84, 0.44, 1) forwards;
}

.animate-slide-up {
  opacity: 0;
  animation: slideUp 0.8s cubic-bezier(0.165, 0.84, 0.44, 1) 0.2s forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
