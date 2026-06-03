<template>
  <div class="not-found">
    <div class="not-found-container">
      <div class="error-code">404</div>
      <div class="error-message">页面未找到</div>
      <div class="error-description">
        抱歉，您访问的页面不存在或已被移除
      </div>
      <div class="error-image">
        <img src="https://picsum.photos/seed/404-error/600/400.jpg" alt="404错误" />
      </div>
      <div class="actions">
        <el-button type="primary" @click="goHome">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup name="NotFoundPage">
import { useRouter } from 'vue-router'
import { House, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}

.not-found-container {
  text-align: center;
  padding: 20px;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #409eff;
  line-height: 1.2;
  margin-bottom: 20px;
}

.error-message {
  font-size: 28px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 15px;
}

.error-description {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}

.error-image {
  width: 100%;
  max-width: 500px;
  margin: 0 auto 30px;
}

.error-image img {
  width: 100%;
  height: auto;
  border-radius: 8px;
  object-fit: cover;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

@media (max-width: 480px) {
  .error-code {
    font-size: 80px;
  }
  
  .error-message {
    font-size: 24px;
  }
  
  .actions {
    flex-direction: column;
    align-items: center;
  }
  
  .actions .el-button {
    width: 200px;
  }
}
</style>