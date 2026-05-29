<template>
  <el-container class="admin-container">
    <el-aside width="240px" class="aside">
      <div class="logo">
        <el-icon class="logo-icon"><Van /></el-icon>
        <span class="logo-text">尊享租车 · 后台</span>
      </div>
      <el-menu
        :default-active="activeIndex"
        @select="handleSelect"
        class="el-menu-vertical"
        background-color="#0f172a"
        text-color="#94a3b8"
        active-text-color="#ff9f1c"
      >
        <el-menu-item index="1">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="2">
          <el-icon><Location /></el-icon>
          <span>门店管理</span>
        </el-menu-item>
        <el-menu-item index="3">
          <el-icon><Van /></el-icon>
          <span>车型与车辆</span>
        </el-menu-item>
        <el-menu-item index="4">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="5">
          <el-icon><Money /></el-icon>
          <span>财务流水</span>
        </el-menu-item>
      </el-menu>
      
      <div class="aside-footer">
        <el-button type="info" plain class="exit-btn" @click="$router.push('/')">
          <el-icon><HomeFilled /></el-icon> 返回前台首页
        </el-button>
      </div>
    </el-aside>
    <el-container class="main-container">
      <el-header class="header">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>管理中心</el-breadcrumb-item>
            <el-breadcrumb-item class="active-crumb">{{ currentMenuName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="user-info">
          <el-avatar :size="32" class="admin-avatar">{{ userStore.userInfo?.username?.substring(0,1) || 'A' }}</el-avatar>
          <span class="admin-name">{{ userStore.userInfo?.username || '系统管理员' }}</span>
          <el-tag size="small" type="warning" effect="dark" class="role-tag">
            {{ userStore.userInfo?.role === 'admin' ? '系统超管' : '门店管理员' }}
          </el-tag>
        </div>
      </el-header>
      
      <el-main class="main-content">
        
        <!-- 用户管理 -->
        <el-card v-if="activeIndex === '1'" class="premium-card">
          <div class="toolbar">
            <h3 class="card-title">系统用户列表</h3>
          </div>
          <el-table :data="userList" style="width: 100%" border v-loading="loading" class="premium-table">
            <el-table-column prop="id" label="ID" width="100" align="center" />
            <el-table-column prop="phone" label="手机号" width="180" align="center" />
            <el-table-column prop="username" label="用户名" min-width="150" />
            <el-table-column prop="role" label="角色身份" width="150" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.role === 2 ? 'danger' : (scope.row.role === 1 ? 'warning' : 'info')">
                  {{ scope.row.role === 2 ? '系统超管' : (scope.row.role === 1 ? '门店管理员' : '普通会员') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="creditScore" label="芝麻信用分" width="120" align="center">
              <template #default="scope">
                <span class="credit-highlight">{{ scope.row.creditScore || 600 }}分</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="帐号状态" width="120" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                  {{ scope.row.status === 1 ? '启用正常' : '禁用封禁' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="200" align="center" />
          </el-table>
        </el-card>

        <!-- 门店管理 -->
        <el-card v-if="activeIndex === '2'" class="premium-card">
          <div class="toolbar">
            <h3 class="card-title">门店列表</h3>
            <div class="filters">
              <el-input
                v-model="storeQuery.cityName"
                placeholder="搜索城市..."
                clearable
                style="width: 200px"
                @keyup.enter="handleStoreSearch"
              />
              <el-button type="primary" @click="handleStoreSearch">查询</el-button>
              <el-button @click="resetStoreSearch">重置</el-button>
              <el-button type="warning" class="action-btn" @click="storeDialogVisible = true">新增门店</el-button>
            </div>
          </div>
          <el-table :data="storeList" style="width: 100%" border v-loading="loading" class="premium-table">
            <el-table-column prop="id" label="ID" width="100" align="center" />
            <el-table-column prop="merchantName" label="所属商户" width="200" />
            <el-table-column prop="cityName" label="运营城市" width="150" align="center" />
            <el-table-column prop="address" label="门店详细服务地址" />
            <el-table-column prop="isSupportDelivery" label="上门送取车" width="150" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.isSupportDelivery === 1 ? 'success' : 'info'" effect="light">
                  {{ scope.row.isSupportDelivery === 1 ? '支持送车' : '不支持' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="管理操作" width="200" align="center">
              <template #default="scope">
                <el-button size="small" type="primary" link @click="openEditStore(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" link @click="deleteStore(scope.row.id)">注销门店</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="storeQuery.page"
              v-model:page-size="storeQuery.pageSize"
              background
              layout="total, sizes, prev, pager, next"
              :page-sizes="[5, 10, 20]"
              :total="storeTotal"
              @size-change="handleStoreSizeChange"
              @current-change="fetchStores"
            />
          </div>
        </el-card>

        <!-- 车型与车辆管理（重大升级：SKU 与 SPU 联合控制台） -->
        <el-card v-if="activeIndex === '3'" class="premium-card">
          <el-tabs v-model="carSubActiveTab" class="custom-tabs">
            
            <!-- SKU 车辆实例 -->
            <el-tab-pane label="物理车辆管理 (SKU)" name="sku">
              <div class="toolbar sub-toolbar">
                <h4 class="sub-card-title">物理车辆 SKU</h4>
                <el-button type="warning" @click="openAddInstanceDialog">投放新车实例</el-button>
              </div>
              <el-table :data="carInstanceList" style="width: 100%" border v-loading="loading" class="premium-table">
                <el-table-column prop="id" label="车辆ID" width="100" align="center" />
                <el-table-column label="对应车型品牌" min-width="180">
                  <template #default="scope">
                    <span class="font-bold" style="font-weight:600;">{{ getModelName(scope.row.modelId) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="所属服务门店" min-width="180">
                  <template #default="scope">
                    <span>{{ getStoreName(scope.row.storeId) }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="plateNumber" label="车牌号码" width="160" align="center">
                  <template #default="scope">
                    <el-tag type="info" effect="plain" class="plate-tag">{{ scope.row.plateNumber }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="dailyRentPrice" label="专属日租金" width="150" align="center">
                  <template #default="scope">
                    <span class="price-highlight">¥ {{ scope.row.dailyRentPrice }}</span> <small>/天</small>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="运营状态" width="140" align="center">
                  <template #default="scope">
                    <el-tag :type="getInstanceStatusType(scope.row.status)" effect="dark">
                      {{ getInstanceStatusText(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="物理管理" width="150" align="center">
                  <template #default="scope">
                    <el-button size="small" type="danger" link @click="deleteCarInstance(scope.row.id)">撤销报废</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div class="pagination-wrapper">
                <el-pagination
                  v-model:current-page="instanceQuery.page"
                  v-model:page-size="instanceQuery.pageSize"
                  background
                  layout="total, prev, pager, next"
                  :total="instanceTotal"
                  @current-change="fetchCarInstances"
                />
              </div>
            </el-tab-pane>

            <!-- SPU 车型库 -->
            <el-tab-pane label="车型库管理 (SPU)" name="spu">
              <div class="toolbar sub-toolbar">
                <h4 class="sub-card-title">车型设计库 SPU</h4>
                <el-button type="warning" @click="carDialogVisible = true">上架全新车型</el-button>
              </div>
              <el-table :data="carModelList" style="width: 100%" border v-loading="loading" class="premium-table">
                <el-table-column prop="id" label="车型ID" width="100" align="center" />
                <el-table-column label="车型外观图" width="180" align="center">
                  <template #default="scope">
                    <el-image style="width: 120px; height: 75px; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.08)" :src="scope.row.mainImage" fit="cover" />
                  </template>
                </el-table-column>
                <el-table-column prop="brandSeries" label="品牌车系" min-width="180" />
                <el-table-column prop="carType" label="车辆品类" width="150" align="center" />
                <el-table-column prop="seatsDoors" label="规格配置" width="150" align="center" />
                <el-table-column label="车型管理" width="150" align="center">
                  <template #default="scope">
                    <el-button size="small" type="danger" link @click="deleteCar(scope.row.id)">下架该车型</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

          </el-tabs>
        </el-card>

        <!-- 订单管理 -->
        <el-card v-if="activeIndex === '4'" class="premium-card">
          <div class="toolbar">
            <h3 class="card-title">全局订单流转控制</h3>
          </div>
          <el-table :data="adminOrderList" style="width: 100%" border v-loading="loading" class="premium-table">
            <el-table-column prop="orderId" label="业务订单号" width="200" align="center" />
            <el-table-column prop="brandSeries" label="预订车型" width="180" />
            <el-table-column prop="startDate" label="起租时间" width="160" align="center" />
            <el-table-column prop="endDate" label="还车时间" width="160" align="center" />
            <el-table-column prop="totalAmount" label="实付总金额" width="140" align="center">
              <template #default="scope">
                <span class="price-highlight">¥{{ scope.row.totalAmount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="订单状态" width="140" align="center">
              <template #default="scope">
                <el-tag :type="getOrderStatusType(scope.row.status)" effect="dark">
                  {{ getOrderStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="交接操作流转" min-width="200" align="center">
              <template #default="scope">
                <el-button size="small" type="success" plain v-if="scope.row.status === 1" @click="updateOrderStatus(scope.row.orderId, 2)">确认客户取车</el-button>
                <el-button size="small" type="primary" plain v-if="scope.row.status === 2" @click="updateOrderStatus(scope.row.orderId, 3)">确认客户还车</el-button>
                <el-button size="small" type="warning" plain v-if="scope.row.status === 3" @click="updateOrderStatus(scope.row.orderId, 4)">完成清算存档</el-button>
                <span v-if="scope.row.status === 4" class="text-muted">订单已结束</span>
                <span v-if="scope.row.status === 5" class="text-muted">订单已取消</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 财务流水 -->
        <el-card v-if="activeIndex === '5'" class="premium-card">
          <div class="toolbar">
            <h3 class="card-title">全局财务收支流水</h3>
            <div class="filters">
              <el-date-picker
                v-model="financeDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始账期"
                end-placeholder="结束账期"
                value-format="YYYY-MM-DD"
                style="width: 260px"
              />
              <el-button type="primary" @click="handleFinanceSearch">查询</el-button>
              <el-button @click="resetFinanceSearch">重置</el-button>
            </div>
          </div>
          <el-table :data="financeList" style="width: 100%" border v-loading="loading" class="premium-table">
            <el-table-column prop="transactionId" label="交易账单号" min-width="180" align="center" />
            <el-table-column prop="orderId" label="关联业务订单" min-width="180" align="center" />
            <el-table-column prop="amount" label="交易数额" width="140" align="center">
              <template #default="scope">
                <span class="amount-text price-highlight">¥{{ scope.row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="tradeTypeName" label="款项说明" width="150" align="center" />
            <el-table-column prop="type" label="款项属性" width="120" align="center">
              <template #default="scope">
                <el-tag :type="getFinanceTypeTag(scope.row.type)" effect="light">
                  {{ getFinanceTypeText(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="交易状态" width="120" align="center">
              <template #default="scope">
                <el-tag :type="getPaymentStatusTag(scope.row.status)" effect="dark">
                  {{ getPaymentStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="交易完成时间" width="200" align="center" />
          </el-table>
          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="financeQuery.page"
              v-model:page-size="financeQuery.pageSize"
              background
              layout="total, sizes, prev, pager, next"
              :page-sizes="[5, 10, 20]"
              :total="financeTotal"
              @size-change="handleFinanceSizeChange"
              @current-change="fetchFinance"
            />
          </div>
        </el-card>

      </el-main>
    </el-container>

    <!-- 新增/编辑门店弹窗 -->
    <el-dialog v-model="storeDialogVisible" :title="storeForm.id ? '编辑门店信息' : '运营网络新增门店'" width="550px" class="premium-dialog">
      <el-form :model="storeForm" label-width="100px" label-position="left">
        <el-form-item label="商户招牌">
          <el-input v-model="storeForm.merchantName" placeholder="如：飞猪尊享自驾" />
        </el-form-item>
        <el-form-item label="落地城市">
          <el-select v-model="storeForm.cityName" placeholder="请选择服务运营城市" style="width: 100%">
            <el-option label="北京" value="北京" />
            <el-option label="上海" value="上海" />
            <el-option label="广州" value="广州" />
            <el-option label="深圳" value="深圳" />
            <el-option label="杭州" value="杭州" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="storeForm.address" placeholder="输入门店精确定位地址" />
        </el-form-item>
        <el-form-item label="上门送取车">
          <el-switch v-model="storeForm.isSupportDelivery" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="storeDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="submitStore" class="gradient-btn">{{ storeForm.id ? '确认修改' : '确认新增' }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 上架新车型弹窗 -->
    <el-dialog v-model="carDialogVisible" title="上架全新车型 (SPU)" width="650px" class="premium-dialog">
      <el-form :model="carForm" label-width="100px" label-position="left">
        <el-form-item label="品牌车系">
          <el-input v-model="carForm.brandSeries" placeholder="如：保时捷 Taycan 4S" />
        </el-form-item>
        <el-form-item label="车型定位">
          <el-select v-model="carForm.carType" placeholder="选择档次品类" style="width: 100%">
            <el-option label="经济型" value="经济型" />
            <el-option label="SUV" value="SUV" />
            <el-option label="豪华型" value="豪华型" />
            <el-option label="尊享跑车" value="尊享跑车" />
          </el-select>
        </el-form-item>
        <el-form-item label="车辆规格">
          <el-input v-model="carForm.seatsDoors" placeholder="如：4座4门 电动" />
        </el-form-item>
        <el-form-item label="车型展示图">
          <el-upload
            class="car-image-uploader"
            :show-file-list="false"
            accept="image/*"
            :http-request="uploadCarImage"
          >
            <div class="upload-preview-box">
              <img v-if="carForm.mainImage" :src="carForm.mainImage" class="car-image-preview" />
              <el-icon v-else class="uploader-icon"><Plus /></el-icon>
            </div>
          </el-upload>
          <el-input v-model="carForm.mainImage" placeholder="上传后自动填入URL，亦可在此直接填入网络图片地址" style="margin-top: 10px;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="carDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="submitCar" class="gradient-btn">确认上架车型</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增车辆实例弹窗 (SKU) -->
    <el-dialog v-model="instanceDialogVisible" title="向门店投放物理车辆 (SKU)" width="600px" class="premium-dialog">
      <el-form :model="instanceForm" label-width="100px" label-position="left">
        <el-form-item label="目标车型 SPU">
          <el-select v-model="instanceForm.modelId" placeholder="请选择车辆所对应的车型品牌" style="width: 100%">
            <el-option v-for="model in carModelList" :key="model.id" :label="model.brandSeries + ' (' + model.carType + ')'" :value="model.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="投放门店">
          <el-select v-model="instanceForm.storeId" placeholder="请选择物理停放与取车的门店" style="width: 100%">
            <el-option v-for="store in storeList" :key="store.id" :label="store.cityName + ' · ' + store.merchantName" :value="store.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="物理车牌">
          <el-input v-model="instanceForm.plateNumber" placeholder="如：沪A·D8888" />
        </el-form-item>
        <el-form-item label="日租金金额">
          <el-input-number v-model="instanceForm.dailyRentPrice" :min="1" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="instanceDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="submitCarInstance" class="gradient-btn">确认投放</el-button>
        </div>
      </template>
    </el-dialog>

  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { User, Location, Van, Tickets, Money, HomeFilled, Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../utils/request';
import { useUserStore } from '../store';

const router = useRouter();
const userStore = useUserStore();
const activeIndex = ref('3'); // 默认进车型车辆 tab 方便演示
const carSubActiveTab = ref('sku'); // 默认展示 SKU 车辆实例

const menuNames = {
  '1': '用户管理', '2': '门店管理', '3': '车型与车辆管理', '4': '订单管理', '5': '财务流水'
};
const currentMenuName = computed(() => menuNames[activeIndex.value]);
const loading = ref(false);

// 数据列表
const userList = ref([]);
const storeList = ref([]);
const carModelList = ref([]);
const carInstanceList = ref([]);
const adminOrderList = ref([]);
const financeList = ref([]);

const storeTotal = ref(0);
const storeQuery = ref({ page: 1, pageSize: 10, cityName: '' });

const instanceTotal = ref(0);
const instanceQuery = ref({ page: 1, pageSize: 10 });

const financeTotal = ref(0);
const financeDateRange = ref([]);
const financeQuery = ref({ page: 1, pageSize: 10, startDate: '', endDate: '' });

// 弹窗状态
const storeDialogVisible = ref(false);
const storeForm = ref({ id: null, merchantName: '', cityName: '', address: '', isSupportDelivery: 0 });

const carDialogVisible = ref(false);
const carForm = ref({ brandSeries: '', carType: '', seatsDoors: '', mainImage: '' });

const instanceDialogVisible = ref(false);
const instanceForm = ref({ modelId: '', storeId: '', plateNumber: '', dailyRentPrice: 300 });

// 监听菜单切换，加载不同数据
const handleSelect = (key) => {
  activeIndex.value = key;
  loadData();
};

const loadData = () => {
  if (!userStore.token) {
    ElMessage.warning('请登录后访问管理后台');
    router.push('/login');
    return;
  }
  if (activeIndex.value === '1') fetchUsers();
  if (activeIndex.value === '2') fetchStores();
  if (activeIndex.value === '3') {
    fetchCars();
    fetchCarInstances();
    fetchStores(); // 为新增实例准备门店下拉框
  }
  if (activeIndex.value === '4') fetchOrders();
  if (activeIndex.value === '5') fetchFinance();
};

// 获取用户
const fetchUsers = async () => {
  loading.value = true;
  try {
    const res = await request.get('/user/list', { params: { page: 1, pageSize: 100 } });
    userList.value = res.records || [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

// 获取门店
const fetchStores = async (page) => {
  if (page) {
    storeQuery.value.page = page;
  }
  loading.value = true;
  try {
    const res = await request.get('/store/list', { params: storeQuery.value });
    storeList.value = res.records || [];
    storeTotal.value = res.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const handleStoreSearch = () => {
  storeQuery.value.page = 1;
  fetchStores();
};

const resetStoreSearch = () => {
  storeQuery.value = { page: 1, pageSize: 10, cityName: '' };
  fetchStores();
};

const handleStoreSizeChange = (size) => {
  storeQuery.value.pageSize = size;
  storeQuery.value.page = 1;
  fetchStores();
};

// 获取 SPU 车型
const fetchCars = async () => {
  loading.value = true;
  try {
    const res = await request.get('/car/model/list', { params: { page: 1, pageSize: 100 } });
    carModelList.value = res.records || [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

// 获取 SKU 车辆物理实例
const fetchCarInstances = async (page) => {
  if (page) {
    instanceQuery.value.page = page;
  }
  loading.value = true;
  try {
    const res = await request.get('/car/instance/list', {
      params: {
        page: instanceQuery.value.page,
        pageSize: instanceQuery.value.pageSize
      }
    });
    carInstanceList.value = res.records || [];
    instanceTotal.value = res.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

// SKU 配套辅助工具方法
const getModelName = (modelId) => {
  const model = carModelList.value.find(m => m.id === modelId);
  return model ? model.brandSeries : '加载中车型...';
};

const getStoreName = (storeId) => {
  const store = storeList.value.find(s => s.id === storeId);
  return store ? `${store.cityName} · ${store.merchantName}` : '未知运营门店';
};

const getInstanceStatusText = (status) => {
  if (status === 0) return '空闲待租';
  if (status === 1) return '整备整顿';
  if (status === 2) return '客户租赁中';
  if (status === 3) return '深度维修中';
  return '离线';
};

const getInstanceStatusType = (status) => {
  if (status === 0) return 'success';
  if (status === 1) return 'warning';
  if (status === 2) return 'primary';
  return 'danger';
};

// 打开投放物理车辆弹窗
const openAddInstanceDialog = async () => {
  await fetchCars();
  await fetchStores();
  if (carModelList.value.length === 0) {
    ElMessage.warning('请先去 SPU 车型库上架至少一种车型');
    return;
  }
  if (storeList.value.length === 0) {
    ElMessage.warning('请先在门店管理中创建至少一家服务门店');
    return;
  }
  // 赋初始默认值
  instanceForm.value.modelId = carModelList.value[0].id;
  instanceForm.value.storeId = storeList.value[0].id;
  instanceForm.value.plateNumber = '沪A·';
  instanceForm.value.dailyRentPrice = 300;
  
  instanceDialogVisible.value = true;
};

// 提交车辆实例
const submitCarInstance = async () => {
  if (!instanceForm.value.plateNumber || instanceForm.value.plateNumber === '沪A·') {
    ElMessage.error('请填写合规的车牌号码');
    return;
  }
  try {
    await request.post('/car/instance/add', instanceForm.value);
    ElMessage.success('物理车辆（SKU）实例投放部署成功');
    instanceDialogVisible.value = false;
    fetchCarInstances();
  } catch (e) {
    console.error(e);
  }
};

// 删除车辆实例
const deleteCarInstance = (id) => {
  ElMessageBox.confirm('撤销投放将使此物理车辆退出运营网络，确定确定报废？', '注意', { type: 'warning' }).then(async () => {
    await request.delete(`/car/instance/delete/${id}`);
    ElMessage.success('该物理车辆实例注销成功');
    fetchCarInstances();
  }).catch(() => {});
};

// 获取订单
const fetchOrders = async () => {
  loading.value = true;
  try {
    const res = await request.get('/order/list', { params: { page: 1, pageSize: 100 } });
    adminOrderList.value = res.records || [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const updateOrderStatus = async (orderId, status) => {
  try {
    await request.put('/order/status/update', { orderNo: orderId, status });
    ElMessage.success('订单状态节点更新完成');
    fetchOrders();
  } catch (e) {
    console.error(e);
  }
};

const getOrderStatusText = (status) => {
  if (status === 1) return '待交车/待取车';
  if (status === 2) return '正在租赁中';
  if (status === 3) return '待还车结算';
  if (status === 4) return '已交车归档';
  if (status === 5) return '客户已取消';
  return '异常状态';
};

const getOrderStatusType = (status) => {
  if (status === 1) return 'warning';
  if (status === 2) return 'primary';
  if (status === 3) return 'warning';
  if (status === 4) return 'success';
  return 'info';
};

// 财务管理
const fetchFinance = async (page) => {
  // 分页组件切换页码时会把新页码传进来，这里同步到查询参数
  if (page) {
    financeQuery.value.page = page;
  }
  loading.value = true;
  try {
    // 通过 axios 封装的 request 请求财务分页接口，params 会被拼到 URL 查询参数中
    const res = await request.get('/finance/list', { params: financeQuery.value });
    // 后端返回 MyBatis-Plus 分页对象，records 给表格，total 给分页器
    financeList.value = res.records  [];
    financeTotal.value = res.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const handleFinanceSearch = () => {
  // 筛选条件变化后回到第一页，避免停留在旧页码导致查不到数据
  financeQuery.value.page = 1;
  // 日期选择器返回数组：[开始日期, 结束日期]，这里拆成后端 DTO 需要的两个字段
  financeQuery.value.startDate = financeDateRange.value?.[0] || '';
  financeQuery.value.endDate = financeDateRange.value?.[1] || '';
  fetchFinance();
};

const resetFinanceSearch = () => {
  // 清空日期选择器和查询条件，再重新加载全部财务流水
  financeDateRange.value = [];
  financeQuery.value = { page: 1, pageSize: 10, startDate: '', endDate: '' };
  fetchFinance();
};

const handleFinanceSizeChange = (size) => {
  // 每页条数变化后重置到第一页，并按新的 pageSize 重新查询
  financeQuery.value.pageSize = size;
  financeQuery.value.page = 1;
  fetchFinance();
};

const getFinanceTypeTag = (type) => {
  if (type === 1) return 'success';
  if (type === 3) return 'danger';
  return 'info';
};

const getFinanceTypeText = (type) => {
  if (type === 1) return '营业收入';
  if (type === 3) return '退款赔付';
  return '其它记账';
};

const getPaymentStatusTag = (status) => {
  if (status === 1) return 'success';
  if (status === 2) return 'danger';
  return 'warning';
};

const getPaymentStatusText = (status) => {
  if (status === 1) return '清算成功';
  if (status === 2) return '清算失败';
  return '挂账处理中';
};

// 打开编辑门店弹窗
const openEditStore = (row) => {
  storeForm.value = { id: row.id, merchantName: row.merchantName, cityName: row.cityName, address: row.address, isSupportDelivery: row.isSupportDelivery };
  storeDialogVisible.value = true;
};

// 提交门店（新增或修改）
const submitStore = async () => {
  try {
    if (storeForm.value.id) {
      await request.put('/store/update', storeForm.value);
      ElMessage.success('门店信息修改成功');
    } else {
      await request.post('/store/add', storeForm.value);
      ElMessage.success('全新城市门店部署成功');
    }
    storeDialogVisible.value = false;
    storeForm.value = { id: null, merchantName: '', cityName: '', address: '', isSupportDelivery: 0 };
    storeQuery.value.page = 1;
    fetchStores();
  } catch (e) {
    console.error(e);
  }
};

// 提交 SPU 车型
const submitCar = async () => {
  try {
    // 提交车型基础信息和上传后得到的 mainImage URL，后端保存到 car_model 表
    await request.post('/car/model/add', carForm.value);
    ElMessage.success('新车型 SPU 上架成功');
    carDialogVisible.value = false;
    carForm.value = { brandSeries: '', carType: '', seatsDoors: '', mainImage: '' };
    fetchCars();
  } catch (e) {
    console.error(e);
  }
};

const uploadCarImage = async (options) => {
  // el-upload 自定义上传入口：把用户选择的文件封装成 multipart/form-data
  const formData = new FormData();
  formData.append('file', options.file);
  try {
    // 上传接口返回图片可访问 URL，request 会自动补 /api 前缀并解包 Result.data
    const res = await request.post('/upload', formData);
    // 把后端返回的 URL 写入车型表单，同时触发页面图片预览
    carForm.value.mainImage = res.url;
    ElMessage.success('车型图片上传成功');
    options.onSuccess?.(res);
  } catch (e) {
    console.error(e);
    ElMessage.error('车型图片上传失败');
    options.onError?.(e);
  }
};

// 注销门店
const deleteStore = (id) => {
  ElMessageBox.confirm('确定要彻底注销该门店吗？与之绑定的资产可能受到影响！', '注意', { type: 'warning' }).then(async () => {
    await request.delete(`/store/delete/${id}`);
    ElMessage.success('门店注销成功');
    fetchStores();
  }).catch(() => {});
};

// 删除车型
const deleteCar = (id) => {
  ElMessageBox.confirm('下架此 SPU 车型后，客户在前台将无法浏览与检索该类型！确定？', '警告', { type: 'danger' }).then(async () => {
    await request.delete(`/car/model/delete/${id}`);
    ElMessage.success('车型下架封档成功');
    fetchCars();
  }).catch(() => {});
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap');

.admin-container {
  height: 100vh;
  font-family: 'Outfit', 'PingFang SC', sans-serif;
  background-color: #f8fafc;
}

/* Sidebar */
.aside {
  background-color: #0f172a;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 24px;
  background-color: #020617;
}

.logo-icon {
  color: #ff9f1c;
  font-size: 1.5rem;
}

.logo-text {
  color: #fff;
  font-weight: 700;
  font-size: 1.1rem;
  letter-spacing: 0.5px;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
  padding-top: 15px;
}

.el-menu-vertical :deep(.el-menu-item) {
  height: 54px;
  line-height: 54px;
  margin: 4px 12px;
  border-radius: 12px;
  font-weight: 500;
  transition: all 0.3s;
}

.el-menu-vertical :deep(.el-menu-item:hover) {
  background-color: rgba(255, 159, 28, 0.08) !important;
  color: #ff9f1c !important;
}

.el-menu-vertical :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(255, 159, 28, 0.15) 0%, rgba(246, 114, 0, 0.05) 100%) !important;
  color: #ff9f1c !important;
  font-weight: 700;
}

.aside-footer {
  padding: 20px;
  background-color: #090d16;
}

.exit-btn {
  width: 100%;
  border-radius: 10px;
  font-weight: 600;
}

/* Header */
.main-container {
  overflow-y: auto;
}

.header {
  background-color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  height: 70px !important;
  border-bottom: 1px solid #f1f5f9;
}

.active-crumb :deep(.el-breadcrumb__inner) {
  color: #0f172a !important;
  font-weight: 700;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-avatar {
  background: linear-gradient(135deg, #ff9f1c, #f67200);
  font-weight: bold;
}

.admin-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 0.95rem;
}

.role-tag {
  border-radius: 8px;
  font-weight: 600;
}

/* Main Content area */
.main-content {
  padding: 35px 40px;
  background-color: #f8fafc;
}

.premium-card {
  border-radius: 20px !important;
  border: 1px solid rgba(0, 0, 0, 0.03) !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.02) !important;
  background-color: #fff;
  padding: 24px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.card-title {
  font-size: 1.35rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  border-left: 4px solid #ff9f1c;
  padding-left: 12px;
}

.filters {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* Tables */
.premium-table :deep(th.el-table__cell) {
  background-color: #f8fafc !important;
  color: #475569;
  font-weight: 700;
  font-size: 0.9rem;
  height: 52px;
}

.premium-table :deep(td.el-table__cell) {
  padding: 14px 0;
  color: #1e293b;
}

.credit-highlight {
  color: #10b981;
  font-weight: 700;
}

.price-highlight {
  color: #f67200;
  font-weight: 800;
  font-size: 1.1rem;
}

.plate-tag {
  font-weight: 700;
  font-size: 0.9rem;
  padding: 4px 10px;
}

/* Tab details */
.custom-tabs :deep(.el-tabs__item) {
  font-size: 1rem;
  font-weight: 600;
  color: #64748b;
  height: 48px;
  line-height: 48px;
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  color: #ff9f1c;
}

.custom-tabs :deep(.el-tabs__active-bar) {
  background-color: #ff9f1c;
  height: 3px;
}

.sub-toolbar {
  margin-top: 15px;
  margin-bottom: 20px;
}

.sub-card-title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: #475569;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 25px;
}

/* Dialog design */
.premium-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
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

.premium-dialog :deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
}

.premium-dialog :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 8px 12px;
}

/* Image Upload UI */
.car-image-uploader {
  display: flex;
  justify-content: flex-start;
}

.upload-preview-box {
  border: 2px dashed #cbd5e1;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 180px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8fafc;
  transition: all 0.3s;
}

.upload-preview-box:hover {
  border-color: #ff9f1c;
  background-color: rgba(255, 159, 28, 0.02);
}

.uploader-icon {
  font-size: 2rem;
  color: #94a3b8;
}

.car-image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gradient-btn {
  background: linear-gradient(135deg, #ff9f1c 0%, #f67200 100%) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 700 !important;
  border-radius: 10px !important;
  padding: 10px 24px !important;
  box-shadow: 0 4px 15px rgba(246, 114, 0, 0.2) !important;
}

.gradient-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(246, 114, 0, 0.3) !important;
}
</style>
