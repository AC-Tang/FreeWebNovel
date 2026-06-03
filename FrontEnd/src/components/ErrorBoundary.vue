<template>
  <div class="error-boundary">
    <slot v-if="!hasError" />
    <div v-else class="error-fallback">
      <div class="error-icon">
        <el-icon size="48" color="#f56c6c">
          <CircleCloseFilled />
        </el-icon>
      </div>
      <div class="error-message">{{ errorMessage }}</div>
      <div class="error-description" v-if="errorDescription">
        {{ errorDescription }}
      </div>
      <div class="error-actions">
        <el-button type="primary" @click="handleReload">
          <el-icon><Refresh /></el-icon>
          重新加载
        </el-button>
        <el-button @click="handleGoHome">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'
import { CircleCloseFilled, Refresh, House } from '@element-plus/icons-vue'
import errorHandler from '../utils/errorHandler'

const router = useRouter()
const hasError = ref(false)
const errorMessage = ref('页面出错了')
const errorDescription = ref('')

// 捕获子组件中的错误
onErrorCaptured((error, instance, info) => {
  console.error('ErrorBoundary捕获到错误:', error, info)
  
  hasError.value = true
  errorMessage.value = error.message || '页面出错了'
  errorDescription.value = info || '未知错误'
  
  // 使用全局错误处理器处理错误
  errorHandler.handleJsError(error, { info })
  
  // 返回false以阻止错误继续向上传播
  return false
})

// 重新加载页面
const handleReload = () => {
  hasError.value = false
  window.location.reload()
}

// 返回首页
const handleGoHome = () => {
  hasError.value = false
  router.push('/')
}

// 重置错误状态
const resetError = () => {
  hasError.value = false
  errorMessage.value = '页面出错了'
  errorDescription.value = ''
}

// 暴露重置方法给父组件
defineExpose({
  resetError
})
</script>

<style scoped>
.error-boundary {
  width: 100%;
  height: 100%;
}

.error-fallback {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: 20px;
  text-align: center;
}

.error-icon {
  margin-bottom: 20px;
}

.error-message {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.error-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 30px;
  max-width: 500px;
  line-height: 1.5;
}

.error-actions {
  display: flex;
  gap: 15px;
}

@media (max-width: 480px) {
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .error-actions .el-button {
    width: 200px;
  }
}
</style>