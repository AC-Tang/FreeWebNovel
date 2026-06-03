<template>
  <div class="admin-dashboard">
    <el-card shadow="hover" class="dashboard-header">
      <template #header>
        <div class="card-header">
          <h1>管理员控制台</h1>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>管理员控制台</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </template>
    </el-card>

    <!-- 系统概览 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-number">{{ stats.bookCount }}</div>
              <div class="stat-label">书籍总数</div>
            </div>
            <div class="stat-icon book-icon">
              <el-icon><Document /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-number">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
            <div class="stat-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-number">{{ stats.commentCount }}</div>
              <div class="stat-label">评论总数</div>
            </div>
            <div class="stat-icon comment-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-number">{{ stats.feedbackCount }}</div>
              <div class="stat-label">待处理反馈</div>
            </div>
            <div class="stat-icon feedback-icon">
              <el-icon><Message /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="20" class="main-content">
      <!-- 左侧：待处理任务 -->
      <el-col :span="12">
        <!-- 待处理任务 -->
        <el-card shadow="hover" class="tasks-card">
          <template #header>
            <div class="card-title">
              <h3>待处理任务</h3>
            </div>
          </template>
          <el-list>
            <el-list-item
              v-for="(task, index) in pendingTasks"
              :key="index"
              class="task-item"
            >
              <div class="task-content">
                <div class="task-title">{{ task.title }}</div>
                <div class="task-count">{{ task.count }} 条</div>
              </div>
              <el-button type="primary" size="small" @click="navigateToTask(task.url)">
                处理
              </el-button>
            </el-list-item>
          </el-list>
        </el-card>
      </el-col>

      <!-- 右侧：系统状态 -->
      <el-col :span="12">
        <!-- 系统状态 -->
        <el-card shadow="hover" class="system-status-card">
          <template #header>
            <div class="card-title">
              <h3>系统状态</h3>
            </div>
          </template>
          <el-descriptions border :column="2">
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="服务器状态">
              <el-tag type="success">正常</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="数据库状态">
              <el-tag type="success">正常</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="AI模型状态">
              <el-tag type="success">运行中</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最后备份">2025-12-03 00:00:00</el-descriptions-item>
            <el-descriptions-item label="在线用户">
              <el-tag type="info">{{ onlineUsers }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Document,
  User,
  ChatDotRound,
  Message,
} from '@element-plus/icons-vue'
import { commentsApi } from '@/api/Comment'
import { feedbacksApi } from '@/api/Feedbacks'
import { userApi } from '@/api/Users'
import { booksApi } from '@/api/Books'
import { reportsApi } from '@/api/Reports'
import { bookRequestApi } from '@/api/BookRequest'

const router = useRouter()

// 模拟数据
const stats = ref({
  bookCount: '',
  userCount: '',
  commentCount: '',
  feedbackCount: '',
})

const onlineUsers = ref(123)



const pendingTasks = ref([
  {
    title: '待处理反馈',
    count: 12,
    url: '/admin/feedback',
  },
  {
    title: '待处理举报',
    count: 8,
    url: '/admin/reports',
  },
  {
    title: '待处理愿望单',
    count: 5,
    url: '/admin/wishlist',
  },
])

// 导航到任务页面
const navigateToTask = (url) => {
  router.push(url)
}

// 获取统计数据
const fetchStats = async () => {
  try {
    // 并行获取所有统计数据
    const [
      bookCountRes,
      userCountRes,
      commentCountRes,
      feedbackCountRes,
      pendingFeedbackRes,
      pendingReportsRes,
      pendingBookRequestsRes
    ] = await Promise.all([
      booksApi.getBookCount(),
      userApi.getUserCount(),
      commentsApi.getCommentCount(),
      feedbacksApi.getFeedbackCount(1), // 待处理反馈
      feedbacksApi.getFeedbackCount(1), // 待处理反馈（用于待处理任务）
      reportsApi.getReportCount(1),   // 待处理举报
      bookRequestApi.getBookRequestCount(1) // 待处理书籍请求
    ])

    // 更新统计数据
    stats.value = {
      bookCount: bookCountRes.data || 0,
      userCount: userCountRes.data || 0,
      commentCount: commentCountRes.data || 0,
      feedbackCount: feedbackCountRes.data || 0
    }

    // 更新待处理任务
    pendingTasks.value = [
      {
        title: '待处理反馈',
        count: pendingFeedbackRes.data || 0,
        url: '/admin/feedback',
      },
      {
        title: '待处理举报',
        count: pendingReportsRes.data || 0,
        url: '/admin/reports',
      },
      {
        title: '待处理愿望单',
        count: pendingBookRequestsRes.data || 0,
        url: '/admin/wishlist',
      },
    ]
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 页面挂载时获取数据
onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.admin-dashboard {
  background-color: #f5f7fa;
}

.dashboard-header {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  color: white;
}

.book-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.user-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.comment-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.feedback-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.main-content {
  margin-bottom: 20px;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}


.tasks-card {
  margin-top: 20px;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.task-content {
  flex: 1;
}

.task-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.task-count {
  font-size: 12px;
  color: #909399;
}

.quick-entry-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.entry-button {
  height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.entry-button .el-icon {
  margin-bottom: 8px;
  font-size: 24px;
}

.system-status-card {
  margin-top: 20px;
}
</style>

