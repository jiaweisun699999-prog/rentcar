<template>
  <!-- 最外层容器，包含整个页面 -->
  <div class="orders-container">
    <!-- Element Plus 的布局容器 -->
    <el-container>
      <!-- 顶部导航栏 (Header) -->
      <el-header class="header">
        <!-- 网站 Logo 区域 -->
        <div class="logo">
          <h2>悟空租车</h2>
        </div>
        <!-- 导航菜单区域 -->
        <div class="nav">
          <el-menu mode="horizontal" :default-active="activeIndex" router>
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/orders">我的订单</el-menu-item>
            <el-menu-item index="/admin">管理后台</el-menu-item>
          </el-menu>
        </div>
        <!-- 用户操作区域（登录/注册 或 下拉菜单） -->
        <div class="user-action">
          <!-- 如果未登录，显示登录/注册按钮 -->
          <el-button type="primary" v-if="!isLoggedIn" @click="$router.push('/login')">登录 / 注册</el-button>
          <!-- 如果已登录，显示用户的下拉菜单 -->
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

      <!-- 页面主要内容区域 (Main Content) -->
      <el-main class="main-content">
        <div class="orders-wrapper">
          <h2 class="page-title">我的租车订单</h2>
          
          <!-- 订单列表卡片，带有加载状态控制 v-loading -->
          <el-card class="box-card" v-loading="loading">
            <!-- 订单表格，数据源为 orderList，带有斑马纹 stripe -->
            <el-table :data="orderList" style="width: 100%" stripe>
              <!-- 订单号列 -->
              <el-table-column prop="orderId" label="订单号" width="200" />
              <!-- 预订车型列 -->
              <el-table-column prop="brandSeries" label="预订车型" width="180" />
              <!-- 租期列，使用自定义插槽格式化时间显示 -->
              <el-table-column label="租期" width="250">
                <template #default="scope">
                  {{ scope.row.startDate }} 至 {{ scope.row.endDate }}
                </template>
              </el-table-column>
              <!-- 总金额列，使用自定义插槽设置价格样式 -->
              <el-table-column label="总金额" width="120">
                <template #default="scope">
                  <span class="price">¥ {{ scope.row.totalAmount }}</span>
                </template>
              </el-table-column>
              <!-- 状态列，使用动态标签类型和文本描述 -->
              <el-table-column prop="status" label="状态" width="120">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <!-- 操作列，根据订单状态显示不同的操作按钮 -->
              <el-table-column label="操作">
                <template #default="scope">
                  <!-- 状态0：待支付，显示去支付按钮 -->
                  <el-button size="small" type="primary" plain v-if="scope.row.status === 0" @click="handlePayment(scope.row)">去支付</el-button>
                  <!-- 状态2：租赁中，显示还车按钮 -->
                  <el-button size="small" type="warning" plain v-else-if="scope.row.status === 2" @click="handleReturn(scope.row)">还车</el-button>
                  <!-- 其他状态：显示查看详情按钮 -->
                  <el-button size="small" type="info" plain v-else @click="handleDetail(scope.row)">查看详情</el-button>
                </template>
              </el-table-column>
              
              <!-- 表格数据为空时显示的占位内容 -->
              <template #empty>
                <el-empty description="您还没有任何租车订单哦"></el-empty>
              </template>
            </el-table>
          </el-card>

          <!-- 订单详情弹窗组件 -->
          <el-dialog v-model="detailVisible" title="订单详情" width="50%">
            <!-- 详情内容区域，带有加载状态，有数据时才渲染 -->
            <div v-loading="detailLoading" v-if="orderDetail">
              <!-- 使用描述列表组件展示详情数据，两列布局带边框 -->
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
            <!-- 弹窗底部操作区 -->
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="detailVisible = false">关 闭</el-button>
              </span>
            </template>
          </el-dialog>
        </div>
      </el-main>

      <!-- 页面底部 (Footer) -->
      <el-footer class="footer">
        <p>&copy; 2024 悟空租车 版权所有</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
// 导入 Vue 的核心响应式和生命周期 API
import { ref, computed, onMounted } from 'vue';
// 导入 Vue Router 用于页面路由跳转
import { useRouter } from 'vue-router';
// 导入 Pinia 状态管理库中的用户 store
import { useUserStore } from '../store';
// 导入 Element Plus 的向下箭头图标组件
import { ArrowDown } from '@element-plus/icons-vue';
// 导入 Element Plus 的消息提示组件
import { ElMessage } from 'element-plus';
// 导入封装好的 axios 请求工具
import request from '../utils/request';

// 获取路由实例
const router = useRouter();
// 获取用户状态管理实例
const userStore = useUserStore();

// 定义顶部导航栏的默认激活项
const activeIndex = ref('/orders');
// 定义订单列表数据的响应式引用
const orderList = ref([]);
// 定义列表加载状态的响应式引用
const loading = ref(false);

// 定义订单详情弹窗显示状态的响应式引用
const detailVisible = ref(false);
// 定义订单详情加载状态的响应式引用
const detailLoading = ref(false);
// 定义订单详情数据的响应式引用
const orderDetail = ref(null);

// 计算属性：根据 store 中是否存在 token 来判断用户是否已登录
const isLoggedIn = computed(() => !!userStore.token);

// 处理用户下拉菜单指令
const handleCommand = (command) => {
  if (command === 'logout') {
    // 执行退出登录操作（清除状态）
    userStore.logout();
    // 跳转到登录页面
    router.push('/login');
  } else if (command === 'profile') {
    // 个人中心逻辑（暂未实现）
    // router.push('/profile');
  }
};

// 获取订单列表数据的异步函数
const fetchOrders = async () => {
  // 如果未登录，则重定向到登录页面
  if (!isLoggedIn.value) {
    // 未登录直接跳去登录
    router.push('/login');
    return;
  }
  
  // 开启加载动画
  loading.value = true;
  try {
    // 发起 GET 请求获取订单列表，并传递分页参数
    const res = await request.get('/order/list', { params: { page: 1, pageSize: 20 } });
    // 将返回的订单记录赋值给 orderList
    orderList.value = res.records || [];
  } catch (error) {
    // 请求失败时打印错误日志
    console.error('Failed to load orders', error);
  } finally {
    // 无论成功还是失败，最后都关闭加载动画
    loading.value = false;
  }
};

// 处理去支付逻辑的异步函数
const handlePayment = async (order) => {
  try {
    // 模拟调用支付网关，发送 POST 请求
    await request.post('/pay/mock', {
      orderNo: order.orderId,
      payType: 1,
      amount: order.totalAmount
    });
    // 提示支付成功
    ElMessage.success('支付成功，门店正在为您准备车辆');
    // 刷新订单列表数据
    fetchOrders(); 
  } catch (error) {
    // 支付失败时打印错误日志
    console.error(error);
  }
};

// 处理还车逻辑的异步函数
const handleReturn = async (order) => {
  try {
    // 发送 PUT 请求更新订单状态为还车（状态码为 3）
    await request.put('/order/status/update', {
      orderNo: order.orderId,
      status: 3
    });
    // 提示还车成功
    ElMessage.success('还车成功，等待门店结算');
    // 刷新订单列表数据
    fetchOrders();
  } catch (error) {
    // 还车失败时打印错误日志
    console.error(error);
  }
};

// 处理查看订单详情逻辑的异步函数
const handleDetail = async (order) => {
  // 显示详情弹窗
  detailVisible.value = true;
  // 开启详情加载动画
  detailLoading.value = true;
  try {
    // 发送 GET 请求获取该订单的详情数据
    const res = await request.get('/order/detail', { params: { orderNo: order.orderId } });
    // 将返回的详情数据赋值给 orderDetail
    orderDetail.value = res;
  } catch (error) {
    // 请求失败时打印错误日志并提示用户
    console.error('Failed to load order detail', error);
    ElMessage.error('获取订单详情失败');
    // 获取失败则关闭弹窗
    detailVisible.value = false;
  } finally {
    // 无论成功还是失败，最后都关闭详情加载动画
    detailLoading.value = false;
  }
};

// 工具函数：根据订单状态码返回对应的 Element Plus 标签类型（颜色）
const getStatusType = (status) => {
  // 状态码对应的颜色映射表
  const map = { 0: 'warning', 1: 'success', 2: 'primary', 3: 'info', 4: 'success', 5: 'danger' };
  // 返回对应的颜色，默认返回 info（灰色）
  return map[status] || 'info';
};

// 工具函数：根据订单状态码返回对应的文本描述
const getStatusText = (status) => {
  // 状态码对应的文本描述映射表
  const map = { 0: '待支付', 1: '待取车', 2: '租赁中', 3: '待结算', 4: '已完成', 5: '已取消' };
  // 返回对应的文本，默认返回“未知状态”
  return map[status] || '未知状态';
};

// 生命周期钩子：组件挂载完成后自动执行获取订单列表的操作
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
