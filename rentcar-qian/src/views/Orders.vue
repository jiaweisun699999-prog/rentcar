<template>
  <div class="orders-container">
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
        <div class="orders-wrapper animate-fade-in">
          <div class="section-title-box">
            <span class="sub-title">MY RENTAL ORDERS</span>
            <h2 class="page-title">我的租车订单</h2>
          </div>
          
          
          <!--将数据渲染成表格 (HTML 模板部分)-->
          <!--这部分使用了Element Plus（Vue 常用 UI 库）的表格组件-->
          
          <el-card class="premium-card" v-loading="loading">
            <el-table :data="orderList" style="width: 100%" class="premium-table">
              <el-table-column prop="orderId" label="订单号" width="220" align="center" />
              <el-table-column prop="brandSeries" label="预订车型" min-width="180">
                <template #default="scope">
                  <span style="font-weight: 700; color: #0f172a;">{{ scope.row.brandSeries }}</span>
                </template>
              </el-table-column>
              <el-table-column label="租车时间段" width="300" align="center">
                <template #default="scope">
                  <span class="date-text">{{ scope.row.startDate }}</span> 
                  <span class="date-arrow">至</span> 
                  <span class="date-text">{{ scope.row.endDate }}</span>
                </template>
              </el-table-column>
              <el-table-column label="实付总金额" width="150" align="center">
                <template #default="scope">
                  <span class="price-highlight">¥ {{ scope.row.totalAmount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="订单状态" width="130" align="center">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)" effect="dark" class="status-tag">
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="业务操作" width="180" align="center">
                <template #default="scope">
                  <el-button size="small" type="warning" class="action-btn" v-if="scope.row.status === 0" @click="handlePayment(scope.row)">立即支付</el-button>
                  <el-button size="small" type="primary" class="action-btn" v-else-if="scope.row.status === 2" @click="handleReturn(scope.row)">我要还车</el-button>
                  <el-button size="small" type="info" plain class="action-btn-plain" @click="handleDetail(scope.row)">查看明细</el-button>
                </template>
              </el-table-column>
              
              <template #empty>
                <el-empty description="您当前还没有任何租车预订订单哦" class="custom-empty"></el-empty>
              </template>
            </el-table>
          </el-card>

          <!-- 订单详情弹窗 -->
          <el-dialog v-model="detailVisible" title="核对尊享账单明细" width="550px" append-to-body class="premium-dialog">
            <div v-loading="detailLoading" v-if="orderDetail" class="order-detail-popup">
              <div class="popup-header-status">
                <span class="label">订单当前节点</span>
                <el-tag :type="getStatusType(orderDetail.status)" effect="dark" size="large">
                  {{ getStatusText(orderDetail.status) }}
                </el-tag>
              </div>

              <div class="bill-details">
                <div class="bill-item">
                  <span class="bill-label">系统业务单号</span>
                  <span class="bill-val">{{ orderDetail.orderNo }}</span>
                </div>
                <div class="bill-item">
                  <span class="bill-label">预订尊享车型</span>
                  <span class="bill-val font-bold">{{ orderDetail.brandSeries }}</span>
                </div>
                <div class="bill-item">
                  <span class="bill-label">取车门店</span>
                  <span class="bill-val">{{ orderDetail.pickupLocation }}</span>
                </div>
                <div class="bill-item">
                  <span class="bill-label">还车门店</span>
                  <span class="bill-val">{{ orderDetail.dropoffLocation }}</span>
                </div>
                <div class="bill-item">
                  <span class="bill-label">取车时间</span>
                  <span class="bill-val date-highlight">{{ orderDetail.startTime }}</span>
                </div>
                <div class="bill-item">
                  <span class="bill-label">还车时间</span>
                  <span class="bill-val date-highlight">{{ orderDetail.endTime }}</span>
                </div>
                
                <div class="bill-divider">费用细则</div>
                
                <div class="bill-item">
                  <span class="bill-label">车辆基本租金</span>
                  <span class="bill-val">¥ {{ orderDetail.rentFee }}</span>
                </div>
                <div class="bill-item">
                  <span class="bill-label">基础安全保障费</span>
                  <span class="bill-val">¥ {{ orderDetail.basicInsuranceFee }}</span>
                </div>
                <div class="bill-item">
                  <span class="bill-label">尊享整备手续费</span>
                  <span class="bill-val">¥ {{ orderDetail.handlingFee }}</span>
                </div>
                <div class="bill-item total-row">
                  <span class="bill-label">实付总金额</span>
                  <span class="bill-val total-price">¥ {{ orderDetail.totalAmount }}</span>
                </div>
              </div>
            </div>
            <template #footer>
              <div class="dialog-footer-actions">
                <el-button type="warning" @click="detailVisible = false" class="confirm-btn">确认并返回</el-button>
              </div>
            </template>
          </el-dialog>
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
import { ArrowDown, Van } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import request from '../utils/request';

/**
 * 【全局路由与状态】
 * useRouter: 用于页面跳转
 * useUserStore: 访问 Pinia 状态管理库中的用户信息
 */
const router = useRouter();
const userStore = useUserStore();

// 顶部导航激活项
const activeIndex = ref('/orders');
// 响应式变量：存放从后端获取的订单列表数据，绑定到页面的 <el-table>
const orderList = ref([]);
// 列表加载状态，控制界面的 loading 动画
const loading = ref(false);

// 详情弹窗的可见性
const detailVisible = ref(false);
const detailLoading = ref(false);
// 响应式变量：存放单个订单的详情数据，绑定到弹窗内的各种展示字段
const orderDetail = ref(null);

// 计算属性：动态判断用户是否已登录
const isLoggedIn = computed(() => userStore.isLoggedIn);

// 处理顶部下拉菜单命令（退出登录/个人中心）
const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
    ElMessage.success('已安全退出登录');
  } else if (command === 'profile') {
    router.push('/profile');
  }
};

/**
 * 【1. 查列表 (GET 请求)】
 *
 * 闭环前端部分：页面挂载时调用 -> axios 发送 GET 请求到 '/order/list' 携带分页参数 
 * -> 拿到后端返回的 Result JSON (被拦截器解包出 data) 
 * -> 将 data.records 赋值给响应式变量 orderList 
 * -> Vue 检测到数据变化，自动重绘模板中的 <el-table>
 */
const fetchOrders = async () => {
  if (!isLoggedIn.value) {
    router.push('/login');//判断用户是否登录，如果未登录则跳转到登录页面
    return;
  }
  
  loading.value = true;
  try {
    // axios发送携带分页参数的 GET 请求，相当于访问 /api/order/list?page=1&pageSize=20
    const res = await request.get('/order/list', { params: { page: 1, pageSize: 20 } });
    // 后端传回的数据赋值给页面变量
    orderList.value = res.records || [];
  } catch (error) {
    console.error('Failed to load orders', error);
  } finally {
    loading.value = false;
  }
};

/**
 * 【2. 去支付 (POST 请求)】
 * 闭环前端部分：点击表格中的“立即支付”按钮 -> 传入当前行的 order 数据 
 * -> axios 发送 POST 请求到 '/pay/mock'，并将 JSON body 附带上 
 * -> 后端返回成功 -> 前端弹窗提示并重新拉取列表刷新页面
 */
const handlePayment = async (order) => {
  try {
    // 使用 POST 请求，携带 JSON 格式的数据体
    await request.post('/pay/mock', {
      orderNo: order.orderId,
      payType: 1,
      amount: order.totalAmount
    });
    ElMessage.success('尊享账单支付成功！门店正在为您调配准备车辆');//调用了 Element Plus UI 组件库的消息提示功能
    fetchOrders(); // 支付成功后，数据库里的状态其实已经从 0（待支付）变成了 1（已支付），
                   // 但前端页面上的数据还是旧的。调用这个方法会重新去后端拉取一次最新的订单列表数据，
                   // 页面上的那条订单状态就会自动刷新成“已支付”或“待取车”
  } catch (error) {
    console.error(error);
  }
};

/**
 * 【4. 更新状态 - 发起还车 (PUT 请求)】
 * 闭环前端部分：点击“我要还车” -> axios 触发 PUT 请求到 '/order/status/update'
 * -> 携带订单号和目标状态码 3 -> 后端 Service 更新数据库并释放车辆库存
 * -> 成功返回后，前端弹窗并刷新列表数据
 */
const handleReturn = async (order) => {
  try {
    await request.put('/order/status/update', {
      orderNo: order.orderId,
      status: 3
    });
    ElMessage.success('还车申请发起成功，正在等待门店核验结算！');
    fetchOrders(); // 刷新表格
  } catch (error) {
    console.error(error);
  }
};

/**
 * 【3. 查详情 (GET 请求)】
 * 闭环前端部分：点击“查看明细” -> 打开弹窗(detailVisible=true)并展示 loading
 * -> axios 发送 GET 请求到 '/order/detail?orderNo=xxx'
 * -> 后端返回 OrderDetailVo (已拼接好门店中文字符串、日期格式)
 * -> 赋值给 orderDetail，Vue 自动将数据填入弹窗的 HTML 模板中展示
 */
const handleDetail = async (order) => {
  detailVisible.value = true; // 打开弹窗
  detailLoading.value = true; // 开启弹窗内部的骨架屏/loading
  try {
    const res = await request.get('/order/detail', { params: { orderNo: order.orderId } });
    orderDetail.value = res; // 将详细数据交给 Vue 渲染
  } catch (error) {
    console.error('Failed to load order detail', error);
    ElMessage.error('获取订单详情失败');
    detailVisible.value = false;
  } finally {
    detailLoading.value = false;
  }
};

// 工具方法：根据后端状态码(0,1,2,3...)返回 Element UI 的主题颜色类别
const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'primary', 3: 'info', 4: 'success', 5: 'danger' };
  return map[status] || 'info';
};

// 工具方法：根据后端状态码转换为给用户看的中文状态文本
const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '已支付/待取车', 2: '正在租赁中', 3: '待还车结算', 4: '已履约完成', 5: '订单已取消' };
  return map[status] || '未知状态';
};


/**
 * onMounted：这是 Vue 3 的生命周期函数。它的意思是**“当这个页面（组件）刚刚在浏览器中挂载（显示）完毕时，自动执行里面的代码”**。
在这里，用户进入“我的订单”页面，它就会立刻、自动调用 fetchOrders() 方法，去向后端索要订单数据，从而保证用户一进来就能看到内容，而不是一个空页面。
 */
onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap');

.orders-container {
  min-height: 100vh;
  background-color: #f7f9fc;
  font-family: 'Outfit', 'PingFang SC', sans-serif;
}

.orders-container :deep(.el-container) {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
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
  transition: all 0.3s ease;
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

.custom-dropdown-menu :deep(.el-dropdown-menu__item:not(.is-disabled):focus) {
  background-color: rgba(255, 159, 28, 0.08);
  color: #f67200;
}

.logout-item {
  color: #f56c6c !important;
}

/* Main Content */
.main-content {
  padding: 60px 0;
  display: flex;
  justify-content: center;
}

.orders-wrapper {
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

.premium-card {
  border-radius: 20px !important;
  border: 1px solid rgba(0, 0, 0, 0.04) !important;
  box-shadow: 0 10px 40px rgba(15, 23, 42, 0.03) !important;
  background-color: #fff;
  padding: 24px;
}

/* Premium Table */
.premium-table :deep(th.el-table__cell) {
  background-color: #f8fafc !important;
  color: #475569;
  font-weight: 700;
  font-size: 0.9rem;
  height: 54px;
}

.premium-table :deep(td.el-table__cell) {
  padding: 18px 0;
  color: #1e293b;
}

.date-text {
  font-weight: 500;
  color: #64748b;
}

.date-arrow {
  color: #ff9f1c;
  margin: 0 8px;
  font-weight: 700;
}

.price-highlight {
  color: #f67200;
  font-weight: 800;
  font-size: 1.2rem;
}

.status-tag {
  border-radius: 8px;
  font-weight: 600;
  padding: 4px 10px;
}

.action-btn {
  background: linear-gradient(135deg, #ff9f1c 0%, #f67200 100%) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 700;
  border-radius: 8px;
  padding: 8px 16px;
  box-shadow: 0 4px 12px rgba(246, 114, 0, 0.2);
  transition: all 0.3s;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 15px rgba(246, 114, 0, 0.3);
}

.action-btn-plain {
  border-radius: 8px;
  font-weight: 600;
  margin-left: 8px;
}

.custom-empty {
  padding: 60px 0;
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
}

.popup-header-status .label {
  font-weight: 700;
  color: #475569;
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

.date-highlight {
  color: #334155;
  font-family: monospace;
  font-size: 0.95rem;
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

.dialog-footer-actions {
  display: flex;
  justify-content: flex-end;
  padding: 10px 20px;
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
</style>
