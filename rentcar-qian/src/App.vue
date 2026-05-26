<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
  'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  background-color: #f5f7fa;
}

#app {
  width: 100%;
  height: 100%;
}

/* ==========================================
   悟空租车 · 极享金橙全局分页器样式统合与优化
   ========================================== */

/* 容器布局 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  width: 100%;
}

/* 分页器主容器 */
.el-pagination {
  font-family: 'Outfit', 'PingFang SC', sans-serif !important;
  --el-pagination-hover-color: #ff9f1c !important;
  --el-color-primary: #ff9f1c !important;
}

/* 页码项通用设计 */
.el-pagination .el-pager li {
  border-radius: 8px !important;
  margin: 0 4px !important;
  border: 1px solid rgba(15, 23, 42, 0.08) !important;
  background-color: #ffffff !important;
  color: #475569 !important;
  font-weight: 600 !important;
  font-size: 0.9rem !important;
  height: 36px !important;
  line-height: 36px !important;
  min-width: 36px !important;
  transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1) !important;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02) !important;
}

/* 未激活项的悬浮变化 */
.el-pagination .el-pager li:not(.is-active):hover {
  color: #ff9f1c !important;
  border-color: rgba(255, 159, 28, 0.4) !important;
  background-color: rgba(255, 159, 28, 0.02) !important;
  transform: translateY(-1px) !important;
}

/* 激活项底色（豪华渐变秋橙） */
.el-pagination .el-pager li.is-active {
  background: linear-gradient(135deg, #ff9f1c 0%, #f67200 100%) !important;
  color: #ffffff !important;
  border-color: #f67200 !important;
  box-shadow: 0 6px 15px rgba(246, 114, 0, 0.22) !important;
  transform: translateY(-1px) !important;
}

/* 前后翻页箭头按钮 */
.el-pagination .btn-prev,
.el-pagination .btn-next {
  border-radius: 8px !important;
  border: 1px solid rgba(15, 23, 42, 0.08) !important;
  background-color: #ffffff !important;
  color: #475569 !important;
  height: 36px !important;
  min-width: 36px !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02) !important;
}

.el-pagination .btn-prev:hover,
.el-pagination .btn-next:hover {
  color: #ff9f1c !important;
  border-color: rgba(255, 159, 28, 0.4) !important;
  background-color: rgba(255, 159, 28, 0.02) !important;
}

.el-pagination .btn-prev:disabled,
.el-pagination .btn-next:disabled {
  background-color: #f8fafc !important;
  color: #cbd5e1 !important;
  border-color: rgba(15, 23, 42, 0.04) !important;
}

/* 共计与条数下拉栏字体 */
.el-pagination__total {
  font-weight: 600 !important;
  color: #475569 !important;
  font-size: 0.88rem !important;
}

.el-pagination__sizes {
  margin-right: 12px !important;
}

/* 下拉大小选项框的输入内嵌阴影 */
.el-pagination .el-select .el-input__wrapper {
  border-radius: 8px !important;
  background-color: #ffffff !important;
  border: 1px solid rgba(15, 23, 42, 0.08) !important;
  box-shadow: none !important;
  height: 36px !important;
  transition: all 0.3s ease !important;
}

.el-pagination .el-select .el-input__wrapper.is-focus {
  border-color: #ff9f1c !important;
  box-shadow: 0 0 0 1px rgba(255, 159, 28, 0.1) !important;
}

.el-pagination .el-select .el-input__wrapper:hover {
  border-color: rgba(255, 159, 28, 0.4) !important;
}

/* 下拉菜单项选中态（豪华金） */
.el-select-dropdown__item.is-selected {
  color: #f67200 !important;
  font-weight: bold !important;
}

.el-select-dropdown__item.hover, 
.el-select-dropdown__item:hover {
  background-color: rgba(255, 159, 28, 0.08) !important;
  color: #f67200 !important;
}

/* 跳转栏（Jumper） */
.el-pagination__jump {
  font-weight: 600 !important;
  color: #475569 !important;
  font-size: 0.88rem !important;
}

.el-pagination__editor.el-input .el-input__wrapper {
  border-radius: 8px !important;
  border: 1px solid rgba(15, 23, 42, 0.08) !important;
  box-shadow: none !important;
  height: 36px !important;
}

.el-pagination__editor.el-input .el-input__wrapper.is-focus {
  border-color: #ff9f1c !important;
  box-shadow: 0 0 0 1px rgba(255, 159, 28, 0.1) !important;
}

.el-pagination__editor.el-input .el-input__wrapper:hover {
  border-color: rgba(255, 159, 28, 0.4) !important;
}
</style>
