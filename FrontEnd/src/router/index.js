import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Ranking from '../views/Ranking.vue'
import Library from '../views/Library.vue'
import UserCenter from '../views/UserCenter.vue'
import BookDetail from '../views/BookDetail.vue'
import CommentDetail from '../views/CommentDetail.vue'
import Read from '../views/Read.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Category from '../views/Category.vue'
import { useAuthStore } from '../stores/auth'
import errorHandler from '../utils/errorHandler'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  // 全局滚动行为配置
  /* eslint-disable no-unused-vars */
  scrollBehavior(to, from, savedPosition) {
    // 每次路由跳转时滚动到页面顶部
    return { top: 0 }
  },
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home,
    },
    {
      path: '/ranking',
      name: 'ranking',
      component: Ranking,
    },
    {
      path: '/ranking/:type',
      name: 'rankingType',
      component: Ranking,
    },
    {
      path: '/library',
      name: 'library',
      component: Library,
    },
    {
      path: '/user',
      name: 'user',
      component: UserCenter,
    },
    {
      path: '/book/:id',
      name: 'book',
      component: BookDetail,
    },
    {
      path: '/comment/:id',
      name: 'comment',
      component: CommentDetail,
    },
    {
      path: '/read/:bookId/:chapterId?',
      name: 'read',
      component: Read,
    },
    {
      path: '/login',
      name: 'login',
      component: Login,
    },
    {
      path: '/register',
      name: 'register',
      component: Register,
    },
    {
      path: '/category',
      name: 'category',
      component: Category,
    },
    {
      path: '/category/:categoryId',
      name: 'categoryDetail',
      component: Category,
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('../views/Search.vue'),
    },
    {
      path: '/feedback',
      name: 'feedback',
      component: () => import('../views/Feedback.vue'),
    },
    {
      path: '/bookshelf',
      name: 'bookshelf',
      component: () => import('../views/Bookshelf.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/reading-history',
      name: 'readingHistory',
      component: () => import('../views/ReadingHistory.vue'),
      meta: { requiresAuth: true },
    },
    // 管理员路由
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/Admin/AdminDashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/books',
      name: 'adminBooks',
      component: () => import('../views/Admin/AdminBookManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/books/crawl',
      name: 'adminBookCrawl',
      component: () => import('../views/Admin/AdminBookCrawl.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/users',
      name: 'adminUsers',
      component: () => import('../views/Admin/AdminUserManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/ai',
      name: 'adminAI',
      component: () => import('../views/Admin/AdminAIManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/wishlist',
      name: 'adminWishlist',
      component: () => import('../views/Admin/AdminWishlistManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/notifications',
      name: 'adminNotifications',
      component: () => import('../views/Admin/AdminNotificationManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/feedback',
      name: 'adminFeedback',
      component: () => import('../views/Admin/AdminFeedbackManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/admin/reports',
      name: 'adminReports',
      component: () => import('../views/Admin/AdminReportManagement.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFound.vue'),
    },
  ],
})

// 全局前置守卫
router.beforeEach((to, _, next) => {
  const authStore = useAuthStore()

  // 检查页面是否需要认证
  if (to.matched.some((record) => record.meta.requiresAuth)) {
    // 检查用户是否已登录
    if (!authStore.isAuthenticated) {
      errorHandler.showError('请先登录')
      next({
        path: '/login',
        // query: { redirect: to.fullPath },
      })
    } else {
      // 检查是否需要管理员权限
      if (to.matched.some((record) => record.meta.requiresAdmin)) {
        // 检查用户是否是管理员
        // 实际项目中，这里应该检查authStore中的用户角色
        // 假设authStore中有isAdmin属性
        if (!authStore.isAdmin) {
          errorHandler.showError('无管理员权限')
          next({
            path: '/',
          })
        } else {
          next()
        }
      } else {
        next()
      }
    }
  } else {
    next()
  }
})

// 全局后置钩子
// router.afterEach((to) => {
//   // 可以在这里添加页面访问统计等逻辑
//   console.log(`导航到: ${to.path}`)
// })

export default router
