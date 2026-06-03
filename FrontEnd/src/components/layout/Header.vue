<template>
  <div class="header-wrapper" :class="{ 'scrolled': isScrolled }">
    <header class="header">
      <div class="header-container">
      <div class="header-left">
        <router-link to="/" class="logo">
          <img src="/favicon.ico" alt="FreeFiction" class="logo-icon">
          <span class="logo-text">FreeFiction</span>
        </router-link>
        <nav class="main-nav">
          <el-menu mode="horizontal" :default-active="activeIndex" class="nav-menu" :ellipsis="false" @select="handleMenuSelect" :key="menuKey">
            <el-menu-item index="1" @click="navigateTo('/')">首页</el-menu-item>
            <el-menu-item index="2" class="nav-menu-item disabled-item">
              分类
              <div class="category-preview">
                <div class="category-list">
                  <div
                    v-for="category in categories"
                    :key="category.id"
                    class="category-item"
                    @click.stop="goToCategory(category.id)"
                  >
                    <span>{{ category.name }}</span>
                  </div>
                </div>
              </div>
            </el-menu-item>
            <el-menu-item index="3" @click="navigateTo('/ranking')">排行</el-menu-item>
            <el-menu-item index="4" @click="navigateTo('/library')">书库</el-menu-item>
            <el-menu-item index="5">完本</el-menu-item>
          </el-menu>
        </nav>
      </div>

      <div class="header-center">
          <div class="search-box">
            <el-input
              v-model="searchQuery"
              placeholder="搜索小说、作者"
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #suffix>
                <el-icon class="search-icon" @click="handleSearch">
                  <Search />
                </el-icon>
              </template>
            </el-input>
          </div>
        </div>

      <div class="header-right">
        <div class="user-actions">
          <!-- 消息通知 -->
          <div class="nav-item nav-item-actions" v-if="authStore.isAuthenticated">
            <a href="#" @click.prevent="navigateToUserMessages" class="nav-link" style="display: flex; align-items: center; justify-content: center; flex-direction: column !important;">
              <el-badge :value="unreadNotificationCount" :max="99" class="nav-badge" v-if="unreadNotificationCount > 0">
                <el-icon class="nav-icon"><Bell /></el-icon>
              </el-badge>
              <el-icon v-else class="nav-icon"><Bell /></el-icon>
              <span class="nav-text" style="margin-top: 4px;">消息</span>
            </a>
          </div>

          <!-- 阅读历史 -->
          <div class="nav-item nav-item-actions">
            <a href="#" @click.prevent="navigateToReadingHistory" class="nav-link" style="display: flex; align-items: center; justify-content: center; flex-direction: column !important;">
              <el-icon class="nav-icon"><Clock /></el-icon>
              <span class="nav-text" style="margin-top: 4px;">历史</span>
            </a>
            <div class="nav-preview" v-if="!authStore.isAuthenticated">
              <div class="preview-content">
                <p>登录即可查看阅读历史</p>
                <el-button type="primary" size="small" @click="navigateTo('/login')">立即登录</el-button>
              </div>
            </div>
          </div>

          <!-- 我的书架 -->
          <div class="nav-item nav-item-actions">
            <a href="#" @click.prevent="navigateToBookshelf" class="nav-link" style="display: flex; align-items: center; justify-content: center; flex-direction: column !important;">
              <el-icon class="nav-icon"><Collection /></el-icon>
              <span class="nav-text" style="margin-top: 4px;">书架</span>
            </a>
            <div class="nav-preview" v-if="!authStore.isAuthenticated">
              <div class="preview-content">
                <p>登录即可查看我的书架</p>
                <el-button type="primary" size="small" @click="navigateTo('/login')">立即登录</el-button>
              </div>
            </div>
          </div>

          <!-- 占位元素 -->
          <div class="nav-spacer"></div>

          <!-- 用户登录状态 -->
          <template v-if="authStore.isAuthenticated">
            <!-- 登录后显示用户头像和名称 -->
            <div class="nav-item nav-item-user" @click="toggleUserMenu" v-if="authStore.user">
              <div class="user-info" style="display: flex; align-items: center; justify-content: center; flex-direction: row !important;">
                <el-avatar :src="`data:image/jpeg;base64,${authStore.user.avatar}`" size="small" class="user-avatar" />
                <span class="nav-text user-name" style="margin-left: 8px; margin-top: 0;">{{ authStore.user.username }}</span>
                <el-icon class="nav-icon user-arrow" style="margin-left: 4px; margin-top: 0;"><ArrowDown /></el-icon>
              </div>
              <!-- 用户下拉菜单 -->
              <div class="nav-preview user-menu-preview" v-if="showUserMenu">
                <div class="preview-content user-menu-content">
                  <div class="user-menu-item" @click="navigateTo('/user')">
                    <el-icon class="menu-item-icon"><User /></el-icon>
                    <span>个人中心</span>
                  </div>
                  <div class="user-menu-item" @click="navigateTo('/bookshelf')">
                    <el-icon class="menu-item-icon"><Collection /></el-icon>
                    <span>我的书架</span>
                  </div>
                  <div class="user-menu-item" @click="navigateTo('/reading-history')">
                    <el-icon class="menu-item-icon"><Clock /></el-icon>
                    <span>阅读历史</span>
                  </div>
                  <div class="user-menu-item" @click="navigateTo('/user/settings')">
                    <el-icon class="menu-item-icon"><Setting /></el-icon>
                    <span>设置</span>
                  </div>
                  <!-- 管理员入口 -->
                  <div v-if="authStore.isAdmin" class="user-menu-item" @click="navigateTo('/admin')">
                    <el-icon class="menu-item-icon"><Setting /></el-icon>
                    <span>管理中心</span>
                  </div>
                  <div class="user-menu-divider"></div>
                  <div class="user-menu-item user-menu-item-logout" @click="handleLogout">
                    <el-icon class="menu-item-icon"><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <template v-else>
            <!-- 未登录显示登录按钮 -->
            <div class="nav-item nav-item-login">
              <a href="/login" @click.prevent="navigateTo('/login')" class="nav-link nav-link-login">
                <span class="nav-text nav-text-login">登录</span>
              </a>
            </div>
          </template>
        </div>
      </div>
    </div>
    </header>
  </div>
</template>

<script setup name="MainHeader">
import { ref, onMounted, onUnmounted, watch, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useHeaderStore } from '../../stores/header'
import { userApi } from '../../api/Users'
import { categoryApi } from '../../api/Category'
import { notificationApi } from '../../api/Notification'
import { Search, Bell, Clock, Collection, ArrowDown, User, Setting, SwitchButton } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const headerStore = useHeaderStore()
const activeIndex = ref('1')
const searchQuery = ref('')
const menuKey = ref(0) // 用于强制更新菜单组件
const showUserMenu = ref(false) // 控制用户下拉菜单显示
// 未读通知数量
const unreadNotificationCount = ref(0)

// 滚动位置状态
const scrollY = ref(0)
const isScrolled = computed(() => scrollY.value > 50) // 当滚动超过50px时认为已滚动

// 轮播图背景色状态
const needsWhiteText = computed(() => headerStore.needsWhiteText) // 是否需要白色文字

// 小说分类数据
const categories = ref([])

// 导航方法
const navigateTo = (path) => {
  router.push(path);
};

// 获取未读通知数量
const fetchUnreadNotificationCount = async () => {
  if (authStore.isAuthenticated && authStore.user) {
    try {
      const response = await notificationApi.getUnreadNotificationCount(authStore.user.id)
      if (response.code === 200 && response.data !== undefined) {
        unreadNotificationCount.value = response.data
      } else {
        unreadNotificationCount.value = 0
      }
    } catch (error) {
      console.error('获取未读通知数量失败:', error)
      unreadNotificationCount.value = 0
    }
  } else {
    unreadNotificationCount.value = 0
  }
}

// 处理菜单选择事件
const handleMenuSelect = (index) => {
  if (index === '5') {
    // 处理"完本"按钮点击
    // 设置activeIndex为4，确保高亮"书库"按钮
    activeIndex.value = '4';

    // 导航到书库页面并设置状态参数
    if (route.path.startsWith('/library')) {
      // 如果已经在书库页面，只更新查询参数
      router.replace({ path: '/library', query: { status: '已完结' } });
    } else {
      // 如果不在书库页面，导航到书库页面
      router.push({ path: '/library', query: { status: '已完结' } });
    }

    // 使用nextTick确保在Element Plus更新DOM后再强制设置我们的值
    nextTick(() => {
      activeIndex.value = '4';
      // 强制更新菜单组件
      menuKey.value += 1;
    });
    return;
  }

  activeIndex.value = index;
  switch (index) {
    case '1':
      navigateTo('/');
      break;
    case '3':
      navigateTo('/ranking');
      break;
    case '4':
      navigateTo('/library');
      break;
  }
};

// 更新当前激活的菜单项
const updateActiveIndex = () => {
  const path = route.path;
  if (path === '/') {
    activeIndex.value = '1';
  } else if (path.startsWith('/category')) {
    activeIndex.value = '2';
  } else if (path.startsWith('/ranking')) {
    activeIndex.value = '3';
  } else if (path.startsWith('/library')) {
    // 在书库页面时总是高亮"书库"按钮，不考虑查询参数
    activeIndex.value = '4';
  } else {
    activeIndex.value = '1';
  }
};

// 监听路由变化
watch(() => route.path, () => {
  updateActiveIndex()
}, { immediate: true })

// 监听路由查询参数变化
watch(() => route.query, () => {
  updateActiveIndex()
}, { immediate: true })

// 监听登录状态变化，更新未读通知数量
watch(() => authStore.isAuthenticated, (newVal) => {
  if (newVal) {
    // 用户登录后，获取未读通知数量
    fetchUnreadNotificationCount()
  } else {
    // 用户登出后，清空未读通知数量
    unreadNotificationCount.value = 0
  }
}, { immediate: true })

// 处理滚动事件
const handleScroll = () => {
  scrollY.value = window.scrollY
}

// 获取分类数据
const fetchCategories = async () => {
  try {
    const response = await categoryApi.getCategoryList()
    if (response.code === 200) {
      categories.value = response.data || []
    }
  } catch (error) {
    console.error('获取分类数据失败:', error)
    // 如果API调用失败，使用默认分类数据
    categories.value = [
      { id: '1', name: '玄幻', icon: 'MagicStick', count: 12580 },
      { id: '2', name: '仙侠', icon: 'Star', count: 9876 },
      { id: '3', name: '都市', icon: 'OfficeBuilding', count: 8765 },
      { id: '4', name: '历史', icon: 'Clock', count: 6543 },
      { id: '5', name: '科幻', icon: 'Star', count: 4321 },
      { id: '6', name: '言情', icon: 'Star', count: 7654 },
      { id: '7', name: '网游', icon: 'Trophy', count: 3210 },
      { id: '8', name: '悬疑', icon: 'Warning', count: 2109 }
    ]
  }
}

// 组件挂载时初始化认证状态
onMounted(async () => {
  await authStore.initAuth()
  updateActiveIndex()
  fetchCategories()
  fetchUnreadNotificationCount()
  // 添加滚动事件监听
  window.addEventListener('scroll', handleScroll)
})

// 组件卸载时清理事件监听
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 处理搜索
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      path: '/search',
      query: { q: searchQuery.value }
    })
  }
}

// 跳转到分类页面
const goToCategory = (categoryId) => {
  router.push(`/category/${categoryId}`)
}

// 切换用户菜单显示
const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

// 导航到阅读历史
const navigateToReadingHistory = () => {
  if (authStore.isAuthenticated) {
    router.push('/reading-history')
  } else {
    router.push('/login')
  }
}

// 导航到书架
const navigateToBookshelf = () => {
  if (authStore.isAuthenticated) {
    router.push('/bookshelf')
  } else {
    router.push('/login')
  }
}

// 导航到用户消息
const navigateToUserMessages = () => {
  if (authStore.isAuthenticated) {
    router.push('/user?tab=messages')
  } else {
    router.push('/login')
  }
}

// 处理退出登录
const handleLogout = async () => {
  try {
    // 显示确认提示
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    // 调试输出：退出登录前的token
    console.log('退出登录，token:', authStore.token)

    // 调用退出登录API，将token放入黑名单
    await userApi.logout()

    // 清除本地状态
    authStore.logout()
    showUserMenu.value = false

    // 显示退出成功提示
    ElMessage.success('退出登录成功')

    // 跳转到首页
    router.push('/')
  } catch (error) {
    // 如果用户取消操作，不做任何处理
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
      ElMessage.error('退出登录失败，请稍后重试')
    }
  }
}

// 更新导航栏文字颜色
const updateTextColor = (needsWhite) => {
  headerStore.updateTextColor(needsWhite)
}

// 暴露方法给父组件
defineExpose({
  updateTextColor
})
</script>

<style scoped>
.header {
  background-color: transparent;
  position: relative;
}

.header-wrapper {
  background-color: rgba(255, 255, 255, 0);
  box-shadow: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transition: all 0.3s ease;
}

.header-wrapper.scrolled {
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

/* 透明状态下的文字和图标颜色 */
.header-wrapper:not(.scrolled) .logo-text {
  color: v-bind('needsWhiteText ? "#fff" : "#333"');
}

.header-wrapper:not(.scrolled) .nav-menu {
  background-color: transparent !important;
}

.header-wrapper:not(.scrolled) .nav-menu :deep(.el-menu-item) {
  color: v-bind('needsWhiteText ? "#fff" : "#333"');
  background-color: transparent !important;
}

.header-wrapper:not(.scrolled) .nav-menu :deep(.el-menu-item:hover) {
  background-color: transparent !important;
}

.header-wrapper:not(.scrolled) .nav-menu :deep(.el-menu-item.is-active) {
  color: #409eff;
  background-color: transparent !important;
}

.header-wrapper:not(.scrolled) .nav-icon {
  color: v-bind('needsWhiteText ? "#fff" : "#333"');
}

.header-wrapper:not(.scrolled) .nav-text {
  color: v-bind('needsWhiteText ? "#fff" : "#333"');
}

.header-wrapper:not(.scrolled) .nav-item.nav-item-login .nav-link.nav-link-login .nav-text.nav-text-login {
  color: v-bind('needsWhiteText ? "#fff" : "#333"') !important;
}

.header-wrapper:not(.scrolled) .search-icon {
  color: v-bind('needsWhiteText ? "#fff" : "#333"');
}

.header-wrapper:not(.scrolled) .search-input :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px v-bind('needsWhiteText ? "rgba(255, 255, 255, 0.3)" : "rgba(0, 0, 0, 0.2)"') inset;
  background-color: v-bind('needsWhiteText ? "rgba(255, 255, 255, 0.2)" : "rgba(255, 255, 255, 0.8)"');
}

.header-wrapper:not(.scrolled) .search-input :deep(.el-input__inner) {
  color: v-bind('needsWhiteText ? "#fff" : "#333"');
}

.header-wrapper:not(.scrolled) .search-input :deep(.el-input__inner::placeholder) {
  color: v-bind('needsWhiteText ? "rgba(255, 255, 255, 0.7)" : "rgba(51, 51, 51, 0.6)"');
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.header-center {
  display: flex;
  justify-content: center;
  flex: 1;
  padding: 0 30px;
}

.header-right {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex: 1;
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 30px;
  text-decoration: none;
}

.logo-icon {
  height: 36px;
  margin-right: 10px;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.nav-menu {
  border-bottom: none;
}

.nav-menu :deep(.el-menu-item) {
  border-bottom: none;
  transition: all 0.3s;
  font-weight: normal;
}

.nav-menu :deep(.el-menu-item:hover) {
  background-color: transparent;
}

.nav-menu :deep(.el-menu-item.is-active) {
  border-bottom: none;
  background-color: transparent;
  font-weight: bold;
  color: #409eff;
}

.user-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: nowrap;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24px;
  cursor: pointer;
  transition: all 0.3s;
}

.nav-item:last-child {
  margin-right: 0;
}

/* 登录按钮特殊样式 */
.nav-item.nav-item-login {
  margin-right: 0;
}

/* 消息、历史、书架导航项特殊样式 */
.nav-item.nav-item-actions {
  margin-right: 24px;
  margin-left: 10px;
}

/* 分类预览框样式 */
.nav-menu-item {
  position: relative;
  cursor: default !important;
}

.nav-menu-item:hover {
  background-color: transparent !important;
}

/* 禁用分类菜单项的点击事件 */
.nav-menu :deep(.el-menu-item.disabled-item) {
  cursor: default !important;
}

/* 允许分类预览框和分类项接收鼠标事件 */
.nav-menu :deep(.el-menu-item.disabled-item) .category-preview,
.nav-menu :deep(.el-menu-item.disabled-item) .category-item {
  pointer-events: auto !important;
}

/* 禁用分类菜单项本身的点击事件，但保留其他鼠标事件 */
.nav-menu :deep(.el-menu-item.disabled-item) > span:first-child {
  pointer-events: none !important;
  display: inline-block;
  width: 100%;
}

/* 确保分类菜单项在所有状态下都保持原来的字体颜色 */
.nav-menu :deep(.el-menu-item.disabled-item) {
  color: inherit !important;
}

.nav-menu :deep(.el-menu-item.disabled-item:hover) {
  color: inherit !important;
}

.nav-menu :deep(.el-menu-item.disabled-item:focus) {
  color: inherit !important;
}

.nav-menu :deep(.el-menu-item.disabled-item:active) {
  color: inherit !important;
}

/* 确保在透明状态下也保持原来的字体颜色 */
.header-wrapper:not(.scrolled) .nav-menu :deep(.el-menu-item.disabled-item) {
  color: v-bind('needsWhiteText ? "#fff" : "#333"') !important;
}

.header-wrapper:not(.scrolled) .nav-menu :deep(.el-menu-item.disabled-item:hover) {
  color: v-bind('needsWhiteText ? "#fff" : "#333"') !important;
}

.header-wrapper:not(.scrolled) .nav-menu :deep(.el-menu-item.disabled-item:focus) {
  color: v-bind('needsWhiteText ? "#fff" : "#333"') !important;
}

.header-wrapper:not(.scrolled) .nav-menu :deep(.el-menu-item.disabled-item:active) {
  color: v-bind('needsWhiteText ? "#fff" : "#333"') !important;
}

.nav-menu :deep(.el-menu-item.nav-menu-item) {
  border-bottom: none !important;
  transition: all 0.3s;
  font-weight: normal !important;
}

.nav-menu :deep(.el-menu-item.nav-menu-item:hover) {
  background-color: transparent !important;
}

.nav-menu :deep(.el-menu-item.nav-menu-item.is-active) {
  border-bottom: none !important;
  background-color: transparent !important;
  font-weight: normal !important;
  color: #303133 !important;
  border-bottom: none !important;
}

.nav-menu :deep(.el-menu-item.nav-menu-item.is-active::after) {
  display: none !important;
}

.category-preview {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
  padding: 16px;
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;
  margin-top: 8px;
  width: 240px;
}

.nav-menu-item:hover .category-preview {
  opacity: 1;
  visibility: visible;
}

.category-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  justify-content: center;
}

.category-item {
  padding: 4px 8px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-item:hover {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
}

.category-item span {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.category-item:hover span {
  color: #409eff;
}

/* 占位元素样式 */
.nav-spacer {
  width: 20px;
}

.nav-item.nav-item-login .nav-link.nav-link-login {
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: #409eff;
}

.nav-item.nav-item-login .nav-link.nav-link-login .nav-text.nav-text-login {
  font-size: 18px !important;
  font-weight: 900 !important;
  color: #333 !important;
  letter-spacing: 0.5px !important;
}

.nav-link {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column !important;
  text-decoration: none;
  color: inherit;
}

.nav-icon {
  width: 24px;
  height: 24px;
  color: #606266;
  transition: color 0.3s;
}

.nav-item:hover .nav-icon {
  color: #409eff;
}

.nav-text {
  font-size: 12px;
  color: #606266;
  transition: color 0.3s;
}

.nav-item:hover .nav-text {
  color: #409eff;
}

.nav-badge {
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-preview {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
  padding: 16px 20px;
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;
  white-space: nowrap;
  margin-top: 12px;
  min-width: 180px;
}

.nav-preview::before {
  content: '';
  position: absolute;
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
  border-width: 0 6px 6px 6px;
  border-style: solid;
  border-color: transparent transparent #fff transparent;
}

.nav-item:hover .nav-preview {
  opacity: 1;
  visibility: visible;
}

.preview-content {
  text-align: center;
}

.preview-content p {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
}

.search-box {
  width: 300px;
}

.search-input {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  padding-left: 15px;
  padding-right: 15px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.search-icon {
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
}

.search-icon:hover {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-container {
    max-width: 960px;
  }
}

@media (max-width: 992px) {
  .header-container {
    max-width: 720px;
  }

  .search-box {
    width: 250px;
  }

  .user-actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    flex-wrap: nowrap;
  }

  .nav-item {
    margin-right: 20px;
  }
}

@media (max-width: 768px) {
  .header-container {
    max-width: 540px;
    padding: 0 15px;
    height: 50px;
  }

  .header-left {
    flex: 2;
  }

  .header-center {
    flex: 1;
  }

  .header-right {
    flex: 2;
  }

  .logo {
    margin-right: 15px;
  }

  .logo-icon {
    height: 30px;
    margin-right: 8px;
  }

  .logo-text {
    display: none;
  }

  .search-box {
    width: 100%;
  }

  .search-input :deep(.el-input__wrapper) {
    padding-left: 15px;
  }

  .main-nav {
    display: none;
  }

  .user-actions .el-button {
    padding: 8px 12px;
    font-size: 13px;
  }

  .user-actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    flex-wrap: nowrap;
  }

  .nav-item {
    margin-right: 16px;
  }

  .nav-link {
    flex-direction: column !important;
  }

  .nav-item .el-icon {
    font-size: 16px;
  }

  .nav-text {
    font-size: 11px;
  }
}

@media (max-width: 576px) {
  .header-container {
    max-width: 100%;
    padding: 0 12px;
  }

  .header-center {
    display: none;
  }

  .header-left {
    flex: 1;
  }

  .header-right {
    flex: 1;
  }

  .user-actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    flex-wrap: nowrap;
  }

  .nav-item {
    margin-right: 12px;
  }

  .nav-link {
    flex-direction: column !important;
  }

  .nav-item .el-icon {
    font-size: 14px;
  }

  .nav-text {
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .header-container {
    height: 50px;
  }

  .logo {
    margin-right: 10px;
  }

  .nav-item {
    margin-right: 10px;
  }

  .nav-link {
    flex-direction: column !important;
  }

  .nav-text {
    font-size: 9px;
  }
}

/* 用户菜单样式 */
.nav-item-user {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 0;
  cursor: pointer;
  transition: all 0.3s;
}

.user-info {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  padding: 4px 8px;
  border-radius: 20px;
}

.user-info:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

.user-avatar {
  margin-right: 8px;
}

.user-name {
  font-size: 14px;
  font-weight: normal;
  margin-right: 4px;
  margin-top: 0;
}

.user-arrow {
  font-size: 12px;
  transition: transform 0.3s;
}

.nav-item-user:hover .user-arrow {
  transform: rotate(180deg);
}

.user-menu-preview {
  position: absolute;
  top: 100%;
  right: 0;
  left: auto;
  transform: translateX(0);
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;
  margin-top: 8px;
  min-width: 180px;
}

.nav-item-user:hover .user-menu-preview,
.user-menu-preview:hover {
  opacity: 1;
  visibility: visible;
}

.user-menu-content {
  text-align: left;
  padding: 0;
}

.user-menu-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #606266;
}

.user-menu-item:hover {
  background-color: #ecf5ff;
  color: #409eff;
}

.menu-item-icon {
  margin-right: 8px;
  font-size: 16px;
}

.user-menu-divider {
  height: 1px;
  background-color: #ebeef5;
  margin: 4px 0;
}

.user-menu-item-logout {
  color: #f56c6c;
}

.user-menu-item-logout:hover {
  background-color: #fef0f0;
  color: #f56c6c;
}
</style>
