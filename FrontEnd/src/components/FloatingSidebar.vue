<template>
  <div class="floating-sidebar" v-if="shouldShowSidebar">
    <!-- 意见反馈按钮 -->
    <div class="sidebar-btn feedback-btn" @click="handleFeedback">
      <img src="../../dist/意见反馈.svg" alt="意见反馈" class="btn-icon">
      <span class="btn-text">意见反馈</span>
    </div>

    <!-- 智能AI按钮 -->
    <div class="sidebar-btn ai-btn" @click="handleAI">
      <img src="../../dist/智能AI.png" alt="智能AI" class="btn-icon">
      <span class="btn-text">智能AI</span>
    </div>

    <!-- 繁简切换按钮 -->
    <div class="sidebar-btn toggle-btn" @click="handleToggleLanguage">
      <img src="../../dist/繁简切换.svg" alt="繁简切换" class="btn-icon">
      <span class="btn-text">繁简切换</span>
    </div>

    <!-- 返回顶部按钮（滚动时显示） -->
    <div
      class="sidebar-btn top-btn"
      @click="scrollToTop"
      v-show="showTopBtn"
      :class="{ 'visible': showTopBtn }"
    >
      <img src="../../dist/返回顶部.svg" alt="返回顶部" class="btn-icon">
      <span class="btn-text">返回顶部</span>
    </div>
  </div>

  <!-- 智能AI聊天组件 -->
  <AIChat :visible="showAI" @close="showAI = false" />
</template>

<script setup name="FloatingSidebar">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AIChat from './AIChat.vue'

const route = useRoute()
const router = useRouter()

// 监听滚动位置，控制返回顶部按钮显示
const showTopBtn = ref(false)
const scrollThreshold = 300 // 滚动超过300px显示返回顶部按钮

// 控制AI聊天窗口显示
const showAI = ref(false)

// 判断当前页面是否应该显示侧边栏
const shouldShowSidebar = computed(() => {
  const path = route.path
  // 显示在：首页，分类页，排行榜页，搜索页，书库页，书籍详情页
  const showOnPages = ['/', '/category', '/ranking', '/search', '/library']

  // 检查是否是书籍详情页（/book/:id）
  const isBookDetailPage = path.startsWith('/book/') && path.split('/').length === 3

  // 隐藏在：管理页，登录/注册页，阅读页
  const hideOnPages = ['/login', '/register', '/read', '/admin']
  const shouldHide = hideOnPages.some(hidePath => path === hidePath || path.startsWith(hidePath))

  return !shouldHide && (showOnPages.includes(path) || isBookDetailPage)
})

// 滚动事件处理
const handleScroll = () => {
  showTopBtn.value = window.scrollY > scrollThreshold
}

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

// 处理意见反馈
const handleFeedback = () => {
  // 跳转到反馈页面
  router.push('/feedback')
}

// 处理智能AI
const handleAI = () => {
  showAI.value = true
}

// 处理繁简切换
const handleToggleLanguage = () => {
  // 繁简切换功能占位
  ElMessage.info('繁简切换功能开发中...')
}

// 组件挂载时添加滚动监听
onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  // 初始检查滚动位置
  handleScroll()
})

// 组件卸载时移除滚动监听
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.floating-sidebar {
  position: fixed;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 16px;
  z-index: 1000; /* 确保在其他内容之上 */
}

.sidebar-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  cursor: pointer;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  opacity: 0.8;
  text-align: center;
  aspect-ratio: 1/1; /* 正方形比例，宽度由文字决定 */
  min-width: 80px;
}

.sidebar-btn:hover {
  opacity: 1;
  /* 移除突起效果，只保留透明度变化 */
}

/* 按钮图标 */
.btn-icon {
  width: 32px;
  height: 32px;
  object-fit: contain;
  flex-shrink: 0;
}

/* 按钮文字 */
.btn-text {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  transition: color 0.3s ease;
}

/* 悬停时文字变红高亮 */
.sidebar-btn:hover .btn-text {
  color: #f56c6c;
}

/* 返回顶部按钮动画 */
.top-btn {
  opacity: 0;
  transform: translateX(100px);
  transition: all 0.3s ease;
  pointer-events: none;
}

.top-btn.visible {
  opacity: 0.8;
  transform: translateX(0);
  pointer-events: auto;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .floating-sidebar {
    right: 12px;
    gap: 12px;
  }

  .sidebar-btn {
    padding: 10px;
    min-width: 70px;
  }

  .btn-icon {
    width: 28px;
    height: 28px;
  }

  .btn-text {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .floating-sidebar {
    right: 8px;
    gap: 10px;
  }

  .sidebar-btn {
    padding: 8px;
    min-width: 60px;
  }

  .btn-icon {
    width: 24px;
    height: 24px;
  }

  .btn-text {
    font-size: 12px;
  }
}
</style>
