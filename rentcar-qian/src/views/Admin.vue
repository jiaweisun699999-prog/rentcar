<template>
  <el-container class="admin-container">
    <el-aside width="200px" class="aside">
      <div class="logo">管理后台</div>
      <el-menu
        :default-active="activeIndex"
        @select="handleSelect"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
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
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentMenuName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="user-info">
          <span>管理员</span>
          <el-button type="danger" link @click="$router.push('/')">返回前台</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        
        <!-- 用户管理 -->
        <el-card v-if="activeIndex === '1'">
          <div class="toolbar">
            <h3>用户列表</h3>
          </div>
          <el-table :data="userList" style="width: 100%" border v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                  {{ scope.row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="180" />
          </el-table>
        </el-card>

        <!-- 门店管理 -->
        <el-card v-if="activeIndex === '2'">
          <div class="toolbar">
            <h3>门店列表</h3>
            <el-button type="primary" @click="storeDialogVisible = true">新增门店</el-button>
          </div>
          <el-table :data="storeList" style="width: 100%" border v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="merchantName" label="所属商户" width="150" />
            <el-table-column prop="cityName" label="城市" width="120" />
            <el-table-column prop="address" label="详细地址" />
            <el-table-column prop="isSupportDelivery" label="送车上门" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.isSupportDelivery === 1 ? 'success' : 'info'">
                  {{ scope.row.isSupportDelivery === 1 ? '支持' : '不支持' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" type="danger" link @click="deleteStore(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 车型与车辆管理 -->
        <el-card v-if="activeIndex === '3'">
          <div class="toolbar">
            <h3>车型库管理</h3>
            <el-button type="success" @click="carDialogVisible = true">上架新车型</el-button>
          </div>
          <el-table :data="carModelList" style="width: 100%" border v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="车型图片" width="150">
              <template #default="scope">
                <el-image style="width: 100px; height: 60px" :src="scope.row.mainImage" fit="cover" />
              </template>
            </el-table-column>
            <el-table-column prop="brandSeries" label="品牌车系" width="180" />
            <el-table-column prop="carType" label="类型 (SUV/轿车)" width="150" />
            <el-table-column prop="seatsDoors" label="座位数/车门" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button size="small" type="primary" link>录入库存</el-button>
                <el-button size="small" type="danger" link @click="deleteCar(scope.row.id)">下架</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 其他模块占位 -->
        <el-card v-if="activeIndex === '4' || activeIndex === '5'">
          <el-empty description="该模块尚未开发"></el-empty>
        </el-card>

      </el-main>
    </el-container>

    <!-- 新增门店弹窗 -->
    <el-dialog v-model="storeDialogVisible" title="新增门店" width="500px">
      <el-form :model="storeForm" label-width="100px">
        <el-form-item label="所属商户">
          <el-input v-model="storeForm.merchantName" placeholder="如：飞猪出行" />
        </el-form-item>
        <el-form-item label="所在城市">
          <el-select v-model="storeForm.cityName" placeholder="请选择城市">
            <el-option label="北京" value="北京" />
            <el-option label="上海" value="上海" />
            <el-option label="广州" value="广州" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="storeForm.address" placeholder="输入门店具体地址" />
        </el-form-item>
        <el-form-item label="送车上门">
          <el-switch v-model="storeForm.isSupportDelivery" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="storeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStore">确认添加</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 上架新车型弹窗 -->
    <el-dialog v-model="carDialogVisible" title="上架新车型 (SPU)" width="600px">
      <el-form :model="carForm" label-width="100px">
        <el-form-item label="品牌车系">
          <el-input v-model="carForm.brandSeries" placeholder="如：特斯拉 Model 3" />
        </el-form-item>
        <el-form-item label="车辆类型">
          <el-select v-model="carForm.carType">
            <el-option label="经济型" value="经济型" />
            <el-option label="SUV" value="SUV" />
            <el-option label="豪华型" value="豪华型" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置">
          <el-input v-model="carForm.seatsDoors" placeholder="如：5座4门" />
        </el-form-item>
        <el-form-item label="图片直链">
          <el-input v-model="carForm.mainImage" placeholder="请输入图片URL(暂代上传)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="carDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCar">确认上架</el-button>
        </span>
      </template>
    </el-dialog>

  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { User, Location, Van, Tickets, Money } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '../utils/request';

const activeIndex = ref('1');
const menuNames = {
  '1': '用户管理', '2': '门店管理', '3': '车型与车辆管理', '4': '订单管理', '5': '财务流水'
};
const currentMenuName = computed(() => menuNames[activeIndex.value]);

const loading = ref(false);

// 数据列表
const userList = ref([]);
const storeList = ref([]);
const carModelList = ref([]);

// 门店状态
const storeDialogVisible = ref(false);
const storeForm = ref({ merchantName: '', cityName: '', address: '', isSupportDelivery: 0 });

// 车辆状态
const carDialogVisible = ref(false);
const carForm = ref({ brandSeries: '', carType: '', seatsDoors: '', mainImage: '' });

// 监听菜单切换，加载不同数据
const handleSelect = (key) => {
  activeIndex.value = key;
  loadData();
};

const loadData = () => {
  if (activeIndex.value === '1') fetchUsers();
  if (activeIndex.value === '2') fetchStores();
  if (activeIndex.value === '3') fetchCars();
};

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

const fetchStores = async () => {
  loading.value = true;
  try {
    const res = await request.get('/store/list', { params: { page: 1, pageSize: 100 } });
    storeList.value = res.records || [];
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

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

// 提交门店
const submitStore = async () => {
  try {
    await request.post('/store/add', storeForm.value);
    ElMessage.success('添加成功');
    storeDialogVisible.value = false;
    storeForm.value = { merchantName: '', cityName: '', address: '', isSupportDelivery: 0 };
    fetchStores();
  } catch (e) {
    console.error(e);
  }
};

// 提交车型
const submitCar = async () => {
  try {
    await request.post('/car/model/add', carForm.value);
    ElMessage.success('上架成功');
    carDialogVisible.value = false;
    carForm.value = { brandSeries: '', carType: '', seatsDoors: '', mainImage: '' };
    fetchCars();
  } catch (e) {
    console.error(e);
  }
};

// 删除门店
const deleteStore = (id) => {
  ElMessageBox.confirm('确定要删除该门店吗?', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/store/delete/${id}`);
    ElMessage.success('删除成功');
    fetchStores();
  }).catch(() => {});
};

// 删除车型
const deleteCar = (id) => {
  ElMessageBox.confirm('确定要下架该车型吗?', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/car/model/delete/${id}`);
    ElMessage.success('下架成功');
    fetchCars();
  }).catch(() => {});
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.admin-container {
  height: 100vh;
}
.aside {
  background-color: #304156;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 1.2rem;
  font-weight: bold;
  background-color: #2b3643;
}
.el-menu-vertical {
  border-right: none;
}
.header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.toolbar h3 {
  margin: 0;
  color: #333;
}
</style>
