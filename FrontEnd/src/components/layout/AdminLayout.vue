<template>
  <div class="admin-layout">
    <!-- 侧边导航栏 -->
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <router-link to="/admin" class="sidebar-logo">
          <el-icon class="logo-icon"><Setting /></el-icon>
          <span class="logo-text">管理中心</span>
        </router-link>
      </div>
      <nav class="admin-nav">
        <el-menu
          :default-active="activeMenu"
          class="admin-menu"
          router
          :unique-opened="true"
        >
          <el-menu-item index="/admin">
            <template #icon>
              <el-icon><House /></el-icon>
            </template>
            <span>控制台</span>
          </el-menu-item>
          <el-menu-item index="/admin/books">
            <template #icon>
              <el-icon><Document /></el-icon>
            </template>
            <span>书籍管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/books/crawl">
            <template #icon>
              <el-icon><Download /></el-icon>
            </template>
            <span>小说爬取</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <template #icon>
              <el-icon><User /></el-icon>
            </template>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/ai">
            <template #icon>
              <el-icon><Setting /></el-icon>
            </template>
            <span>AI模型管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/wishlist">
            <template #icon>
              <el-icon><Star /></el-icon>
            </template>
            <span>愿望单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/notifications">
            <template #icon>
              <el-icon><Bell /></el-icon>
            </template>
            <span>通知管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/feedback">
            <template #icon>
              <el-icon><Message /></el-icon>
            </template>
            <span>反馈管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/reports">
            <template #icon>
              <el-icon><Message /></el-icon>
            </template>
            <span>举报管理</span>
          </el-menu-item>
        </el-menu>
      </nav>
    </aside>

    <!-- 主内容区域 -->
    <main class="admin-main">
      <!-- 顶部导航 -->
      <header class="admin-topbar">
        <div class="topbar-left">
          <span class="page-title">{{ currentPageTitle }}</span>
        </div>
        <div class="topbar-right">
          <el-dropdown>
            <el-button type="text" class="user-dropdown-btn">
              <el-avatar size="small" :src="userAvatar"></el-avatar>
              <span class="user-name">{{ userName }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="navigateTo('/user')">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <div class="admin-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup name="AdminLayout">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Setting,
  House,
  Document,
  Download,
  User,
  Star,
  Bell,
  Message,
  ArrowDown,
  SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 当前活跃菜单
const activeMenu = computed(() => route.path)

// 当前页面标题
const currentPageTitle = computed(() => {
  const titleMap = {
    '/admin': '管理员控制台',
    '/admin/books': '书籍管理',
    '/admin/books/crawl': '小说爬取',
    '/admin/users': '用户管理',
    '/admin/ai': 'AI模型管理',
    '/admin/wishlist': '愿望单管理',
    '/admin/notifications': '通知管理',
    '/admin/feedback': '反馈管理',
    '/admin/reports': '举报管理'
  }
  return titleMap[route.path] || '管理员控制台'
})

// 用户信息
const userName = computed(() => authStore.user?.username || '管理员')
const userAvatar = computed(() => authStore.user?.avatar || '')

// 导航到个人中心
const navigateTo = (path) => {
  router.push(path)
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    authStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    // 用户取消操作
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
      ElMessage.error('退出登录失败，请稍后重试')
    }
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 侧边导航栏 */
.admin-sidebar {
  width: 180px;
  background-color: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px 0;
  border-bottom: 1px solid #e4e7ed;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: #303133;
  font-weight: 600;
  font-size: 18px;
}

.logo-icon {
  margin-right: 10px;
  font-size: 24px;
  color: #409eff;
}

.admin-nav {
  flex: 1;
  overflow-y: auto;
}

.admin-menu {
  border-right: none;
}

/* 主内容区域 */
.admin-main {
  margin-left: 180px;
  flex: 1;
  display: flex;
  flex-direction: column;
  width: calc(100% - 180px);
  overflow: hidden;
}

/* 顶部导航 */
.admin-topbar {
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  position: fixed;
  left: 180px;
  right: 0;
  top: 0;
  z-index: 99;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.topbar-right {
  display: flex;
  align-items: center;
}

.user-dropdown-btn {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
}

.user-name {
  margin: 0 8px;
}

/* 页面内容 */
.admin-content {
  flex: 1;
  padding: 20px;
  margin-top: 60px;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .admin-sidebar {
    width: 160px;
  }

  .admin-main {
    margin-left: 160px;
    width: calc(100% - 160px);
  }

  .admin-topbar {
    left: 160px;
  }
}

@media (max-width: 768px) {
  .admin-sidebar {
    transform: translateX(-100%);
    transition: transform 0.3s;
  }

  .admin-main {
    margin-left: 0;
    width: 100%;
  }

  .admin-topbar {
    left: 0;
  }
}

/* 导航菜单项样式 */
.el-menu-item {
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: 600;
}

.el-menu-item span {
  margin-left: 8px;
}

/* 导航图标样式 */
.el-menu-item .el-icon {
  font-size: 18px;
}
</style>
