<template>
  <div class="orders-container">
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
        <div class="orders-wrapper">
          <h2 class="page-title">我的租车订单</h2>
          
          <el-card class="box-card" v-loading="loading">
            <el-table :data="orderList" style="width: 100%" stripe>
              <el-table-column prop="orderId" label="订单号" width="200" />
              <el-table-column prop="brandSeries" label="预订车型" width="180" />
              <el-table-column label="租期" width="250">
                <template #default="scope">
                  {{ scope.row.startDate }} 至 {{ scope.row.endDate }}
                </template>
              </el-table-column>
              <el-table-column label="总金额" width="120">
                <template #default="scope">
                  <span class="price">¥ {{ scope.row.totalAmount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template #default="scope">
                  <el-button size="small" type="primary" plain v-if="scope.row.status === 0" @click="handlePayment(scope.row)">去支付</el-button>
                  <el-button size="small" type="info" plain v-else @click="handleDetail(scope.row)">查看详情</el-button>
                </template>
              </el-table-column>
              
              <template #empty>
                <el-empty description="您还没有任何租车订单哦"></el-empty>
              </template>
            </el-table>
          </el-card>

          <!-- 订单详情弹窗 -->
          <el-dialog v-model="detailVisible" title="订单详情" width="50%">
            <div v-loading="detailLoading" v-if="orderDetail">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
                <el-descriptions-item label="预订车型">{{ orderDetail.brandSeries }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="getStatusType(orderDetail.status)">{{ getStatusText(orderDetail.status) }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ orderDetail.createTime }}</el-descriptions-item>
                <el-descriptions-item label="取车时间">{{ orderDetail.startTime }}</el-descriptions-item>
                <el-descriptions-item label="还车时间">{{ orderDetail.endTime }}</el-descriptions-item>
                <el-descriptions-item label="车辆押金/费">¥ {{ orderDetail.basicInsuranceFee }}</el-descriptions-item>
                <el-descriptions-item label="租车费">¥ {{ orderDetail.rentFee }}</el-descriptions-item>
                <el-descriptions-item label="手续费">¥ {{ orderDetail.handlingFee }}</el-descriptions-item>
                <el-descriptions-item label="总金额"><span class="price">¥ {{ orderDetail.totalAmount }}</span></el-descriptions-item>
                <el-descriptions-item label="取车地点">{{ orderDetail.pickupLocation }}</el-descriptions-item>
                <el-descriptions-item label="还车地点">{{ orderDetail.dropoffLocation }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="detailVisible = false">关 闭</el-button>
              </span>
            </template>
          </el-dialog>
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

const activeIndex = ref('/orders');
const orderList = ref([]);
const loading = ref(false);

const detailVisible = ref(false);
const detailLoading = ref(false);
const orderDetail = ref(null);

const isLoggedIn = computed(() => !!userStore.token);

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
  } else if (command === 'profile') {
    // router.push('/profile');
  }
};

const fetchOrders = async () => {
  if (!isLoggedIn.value) {
    // 未登录直接跳去登录
    router.push('/login');
    return;
  }
  
  loading.value = true;
  try {
    const res = await request.get('/order/list', { params: { page: 1, pageSize: 20 } });
    orderList.value = res.records || [];
  } catch (error) {
    console.error('Failed to load orders', error);
  } finally {
    loading.value = false;
  }
};

const handlePayment = async (order) => {
  try {
    // 模拟调用网关
    await request.post('/pay/mock', {
      orderNo: order.orderId,
      payType: 1,
      amount: order.totalAmount
    });
    ElMessage.success('支付成功，门店正在为您准备车辆');
    fetchOrders(); // 刷新列表
  } catch (error) {
    console.error(error);
  }
};

const handleDetail = async (order) => {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const res = await request.get('/order/detail', { params: { orderNo: order.orderId } });
    orderDetail.value = res;
  } catch (error) {
    console.error('Failed to load order detail', error);
    ElMessage.error('获取订单详情失败');
    detailVisible.value = false;
  } finally {
    detailLoading.value = false;
  }
};

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'primary', 3: 'info', 4: 'success', 5: 'danger' };
  return map[status] || 'info';
};

const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '待取车', 2: '租赁中', 3: '待结算', 4: '已完成', 5: '已取消' };
  return map[status] || '未知状态';
};

onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
.orders-container {
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
.orders-wrapper {
  width: 80%;
  max-width: 1200px;
}
.page-title {
  margin-bottom: 20px;
  color: #333;
}
.box-card {
  border-radius: 8px;
}
.price {
  color: #f56c6c;
  font-weight: bold;
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
