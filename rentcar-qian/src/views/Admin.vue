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
        
        <!-- 默认欢迎页 -->
        <el-card v-if="activeIndex === '1'">
          <h3>用户管理 (分配给组员 A)</h3>
          <p>此处将展示用户列表和实名认证审核...</p>
        </el-card>

        <!-- 门店管理 -->
        <el-card v-if="activeIndex === '2'">
          <div class="toolbar">
            <h3>门店列表 (分配给组员 B)</h3>
            <el-button type="primary" @click="storeDialogVisible = true">新增门店</el-button>
          </div>
          <el-table :data="storeList" style="width: 100%" border>
            <el-table-column prop="merchantName" label="所属商户" width="180" />
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
              <template #default>
                <el-button size="small" type="primary" link>编辑</el-button>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 车型与车辆管理 (含图片上传) -->
        <el-card v-if="activeIndex === '3'">
          <div class="toolbar">
            <h3>车型库管理 (分配给组员 C)</h3>
            <el-button type="success" @click="carDialogVisible = true">上架新车型</el-button>
          </div>
          <el-table :data="carModelList" style="width: 100%" border>
            <el-table-column label="车型图片" width="150">
              <template #default="scope">
                <el-image style="width: 100px; height: 60px" :src="scope.row.mainImage" fit="cover" />
              </template>
            </el-table-column>
            <el-table-column prop="brandSeries" label="品牌车系" width="180" />
            <el-table-column prop="carType" label="类型 (SUV/轿车)" width="150" />
            <el-table-column prop="seatsDoors" label="座位数/车门" />
            <el-table-column label="操作" width="200">
              <template #default>
                <el-button size="small" type="primary" link>录入库存(SKU)</el-button>
                <el-button size="small" type="danger" link>下架</el-button>
              </template>
            </el-table-column>
          </el-table>
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
          <el-button type="primary" @click="storeDialogVisible = false">确认添加</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 上架新车型弹窗 (图片上传演示) -->
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
        <el-form-item label="车辆主图">
          <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :auto-upload="false"
          >
            <div class="upload-placeholder">
              <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
              <div class="el-upload__text">点击上传车辆靓照</div>
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="carDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="carDialogVisible = false">确认上架</el-button>
        </span>
      </template>
    </el-dialog>

  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue';
import { User, Location, Van, Tickets, Money, Plus } from '@element-plus/icons-vue';

const activeIndex = ref('3'); // 默认展示车辆管理方便你预览
const menuNames = {
  '1': '用户管理', '2': '门店管理', '3': '车型与车辆管理', '4': '订单管理', '5': '财务流水'
};
const currentMenuName = computed(() => menuNames[activeIndex.value]);

const handleSelect = (key) => {
  activeIndex.value = key;
};

// 门店状态
const storeDialogVisible = ref(false);
const storeForm = ref({ merchantName: '', cityName: '', address: '', isSupportDelivery: 0 });
const storeList = ref([
  { merchantName: '直营店', cityName: '北京', address: '朝阳区三里屯SOHO地下车库', isSupportDelivery: 1 }
]);

// 车辆状态
const carDialogVisible = ref(false);
const carForm = ref({ brandSeries: '', carType: '', seatsDoors: '' });
const carModelList = ref([
  { mainImage: 'https://img.alicdn.com/imgextra/i3/O1CN013iNpxf1aL1NfU0yRj_!!6000000003312-2-tps-800-600.png', brandSeries: '特斯拉 Model 3', carType: '豪华型', seatsDoors: '5座4门' }
]);
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
.upload-placeholder {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8c939d;
  transition: border-color 0.3s;
}
.upload-placeholder:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  margin-bottom: 10px;
}
</style>
