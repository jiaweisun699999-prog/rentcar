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
        <div class="result-header">
          <div>
            <h3>可选车型</h3>
            <p>{{ selectedStoreLabel }} ｜ {{ searchParams.timeRange[0] }} 至 {{ searchParams.timeRange[1] }}</p>
          </div>
          <el-button type="primary" plain @click="router.push('/')">返回修改条件</el-button>
        </div>

        <!-- 车辆列表区 -->
        <div class="car-list" v-loading="loading">
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
          <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :page-sizes="[4, 8, 12, 16]"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="fetchAvailableCars"
            />
          </div>
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
const route = router.currentRoute;
const userStore = useUserStore();
const isLoggedIn = computed(() => !!userStore.token);

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

const selectedStoreLabel = computed(() => {
  const store = stores.value.find(item => String(item.id) === String(searchParams.value.storeId));
  if (!store) {
    return '已选门店';
  }
  return `${store.merchantName || ''}${store.address ? ' - ' + store.address : ''}`;
});

// 加载门店下拉列表
const loadStores = async () => {
  try {
    const res = await request.get('/store/list', { params: { page: 1, pageSize: 100 } });
    stores.value = res.records || [];
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
        page: pagination.value.page,
        pageSize: pagination.value.pageSize
      }
    });
    carList.value = res.records || [];
    total.value = res.total || 0;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const handleSizeChange = (size) => {
  pagination.value.pageSize = size;
  pagination.value.page = 1;
  fetchAvailableCars();
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
  searchParams.value.storeId = route.value.query.storeId || '';
  searchParams.value.timeRange = [route.value.query.startTime || '', route.value.query.endTime || ''];
  loadStores();
  fetchAvailableCars();
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
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  padding: 20px;
  margin-bottom: 20px;
}
.result-header h3 {
  margin: 0 0 8px 0;
  color: #303133;
}
.result-header p {
  margin: 0;
  color: #606266;
}
.car-card {
  border-radius: 8px;
  margin-bottom: 20px;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin: 20px 0 10px;
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
