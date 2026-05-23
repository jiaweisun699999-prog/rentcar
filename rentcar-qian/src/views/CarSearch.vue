<template>
  <div class="search-container">
    <el-container>
      <!-- Header -->
      <el-header class="header">
        <div class="logo">
          <h2>悟空租车</h2>
        </div>
        <div class="nav">
          <el-menu mode="horizontal" default-active="/search" router>
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

      <el-main class="main-content">
        <!-- 搜索条件区 -->
        <el-card class="filter-card">
          <el-form :inline="true" :model="searchParams">
            <el-form-item label="取还车门店">
              <el-select v-model="searchParams.storeId" placeholder="请选择门店" style="width: 250px;">
                <el-option v-for="store in stores" :key="store.id" :label="store.merchantName + ' - ' + store.address" :value="store.id"></el-option>
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
              <el-button type="primary" @click="fetchAvailableCars" :loading="loading">查找可用车辆</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 车辆列表区 -->
        <div class="car-list">
          <el-row :gutter="20" v-if="carList.length > 0">
            <el-col :span="6" v-for="car in carList" :key="car.id">
              <el-card shadow="hover" class="car-card">
                <el-image :src="car.mainImage" fit="cover" style="width: 100%; height: 160px; border-radius: 4px;" v-if="car.mainImage"></el-image>
                <div class="car-img-placeholder" v-else>暂无图片</div>
                <div class="car-info">
                  <h4>{{ car.brandSeries }}</h4>
                  <p class="car-desc">{{ car.carType }} | {{ car.seatsDoors }}</p>
                  <div class="car-price-row">
                    <div><span class="price">¥ {{ car.dailyPrice }}</span> / 日起</div>
                    <el-button type="success" size="small" @click="openOrderPreview(car)">预订</el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty description="该时间段暂无可租车型，请尝试更改条件" v-else style="margin-top: 50px;"></el-empty>
        </div>
      </el-main>

      <el-footer class="footer">
        <p>&copy; 2024 悟空租车 版权所有</p>
      </el-footer>
    </el-container>

    <!-- 订单预结算弹窗 -->
    <el-dialog v-model="previewDialogVisible" title="核对订单明细" width="500px">
      <div v-loading="previewLoading">
        <el-descriptions :column="1" border v-if="previewData">
          <el-descriptions-item label="预订车型">{{ selectedCar?.brandSeries }}</el-descriptions-item>
          <el-descriptions-item label="取还时间">
            {{ searchParams.timeRange[0] }} <br/>至<br/> {{ searchParams.timeRange[1] }}
          </el-descriptions-item>
          <el-descriptions-item label="租期">{{ previewData.rentDays }} 天</el-descriptions-item>
          <el-descriptions-item label="车辆租金">¥ {{ previewData.rentFee }}</el-descriptions-item>
          <el-descriptions-item label="基础保障费">¥ {{ previewData.basicInsuranceFee }}</el-descriptions-item>
          <el-descriptions-item label="手续费">¥ {{ previewData.handlingFee }}</el-descriptions-item>
          <el-descriptions-item label="总金额" label-class-name="highlight-label">
            <span class="highlight-price">¥ {{ previewData.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="需冻结押金">
            ¥ {{ previewData.depositAmount }} 
            <el-tag size="small" type="success" v-if="previewData.depositAmount == 0">信用免押</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="previewDialogVisible = false">考虑一下</el-button>
          <el-button type="primary" @click="submitOrder" :disabled="!previewData">确认提交订单</el-button>
        </span>
      </template>
    </el-dialog>

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

const loading = ref(false);
const stores = ref([]);
const carList = ref([]);

const searchParams = ref({
  storeId: '',
  timeRange: []
});

// 加载门店下拉列表
const loadStores = async () => {
  try {
    const res = await request.get('/store/list', { params: { page: 1, pageSize: 100 } });
    stores.value = res.records || [];
    if (stores.value.length > 0 && !searchParams.value.storeId) {
      searchParams.value.storeId = stores.value[0].id;
    }
  } catch (error) {
    console.error(error);
  }
};

// 根据条件查询可用车辆
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
        page: 1, 
        pageSize: 100 
      } 
    });
    carList.value = res.records || [];
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 预订与弹窗逻辑
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
  } else if (command === 'profile') {
    router.push('/profile');
  }
};

onMounted(() => {
  loadStores();
  // 默认填充近两天的日期
  const start = new Date();
  start.setHours(10, 0, 0, 0);
  const end = new Date();
  end.setDate(end.getDate() + 2);
  end.setHours(10, 0, 0, 0);
  
  const formatDate = (date) => {
    const pad = (n) => n < 10 ? '0' + n : n;
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:00`;
  };
  
  searchParams.value.timeRange = [formatDate(start), formatDate(end)];
});
</script>

<style scoped>
.search-container {
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
  padding: 20px 50px;
}
.filter-card {
  margin-bottom: 20px;
}
.car-card {
  border-radius: 8px;
  margin-bottom: 20px;
}
.car-img-placeholder {
  width: 100%;
  height: 160px;
  background-color: #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
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
.car-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price {
  color: #f56c6c;
  font-size: 1.5rem;
  font-weight: bold;
}
.highlight-price {
  color: #f56c6c;
  font-size: 1.2rem;
  font-weight: bold;
}
.footer {
  text-align: center;
  color: #909399;
  padding: 20px;
  background-color: #fff;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
  display: flex;
  align-items: center;
}
</style>
