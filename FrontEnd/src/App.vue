<template>
  <ErrorBoundary>
    <Layout />
  </ErrorBoundary>
</template>

<script setup>
import Layout from './components/layout/Layout.vue'
import ErrorBoundary from './components/ErrorBoundary.vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  background-color: #f5f5f5;
  color: #333;
  overflow-x: hidden;
}

a {
  text-decoration: none;
  color: inherit;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 响应式容器 */
@media (max-width: 1200px) {
  .container {
    max-width: 960px;
  }
}

@media (max-width: 992px) {
  .container {
    max-width: 720px;
  }
}

@media (max-width: 768px) {
  .container {
    max-width: 540px;
    padding: 0 15px;
  }
}

@media (max-width: 576px) {
  .container {
    max-width: 100%;
    padding: 0 12px;
  }
}

/* Element Plus 样式覆盖 */
.el-menu--horizontal {
  border-bottom: none !important;
}

.el-menu--horizontal .el-menu-item {
  border-bottom: none !important;
}

.el-menu--horizontal .el-menu-item.is-active {
  border-bottom: 2px solid #409eff !important;
}

/* 响应式菜单调整 */
@media (max-width: 768px) {
  .el-menu--horizontal .el-menu-item {
    padding: 0 10px;
    font-size: 14px;
  }
}

@media (max-width: 576px) {
  .el-menu--horizontal .el-menu-item {
    padding: 0 8px;
    font-size: 13px;
  }
}
</style>
