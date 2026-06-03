<template>
  <template v-if="isAdminPage">
    <AdminLayout />
  </template>
  <template v-else>
    <div class="app-layout">
      <Header v-if="!isAuthPage && !isReadPage" />
      <main class="main-content" :class="{ 'auth-main-content': isAuthPage, 'read-main-content': isReadPage }">
        <PageTransition>
          <router-view />
        </PageTransition>
      </main>
      <Footer v-if="!isReadPage" />
      <!-- 悬浮侧边栏 -->
      <FloatingSidebar />
    </div>
  </template>
</template>

<script setup name="MainLayout">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from './Header.vue'
import Footer from './Footer.vue'
import PageTransition from '../PageTransition.vue'
import AdminLayout from './AdminLayout.vue'
import FloatingSidebar from '../FloatingSidebar.vue'

const route = useRoute()

// 判断是否是登录/注册页面
const isAuthPage = computed(() => {
  return route.path === '/login' || route.path === '/register'
})

// 判断是否是阅读页面（只匹配/read路径，不匹配/reading-history等）
const isReadPage = computed(() => {
  return route.path === '/read' || route.path.startsWith('/read/')
})

// 判断是否是管理员页面
const isAdminPage = computed(() => {
  return route.path.startsWith('/admin')
})
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  padding: 0;
}

.auth-main-content {
  padding: 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .main-content {
    padding-bottom: 60px; /* 为移动端底部导航预留空间 */
  }
}
</style>
