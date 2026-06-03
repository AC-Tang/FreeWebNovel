<template>
  <div class="admin-feedback-management">
    <el-card shadow="hover" class="page-header">
      <template #header>
        <div class="card-header">
          <h1>反馈管理</h1>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item><el-icon><House /></el-icon>首页</el-breadcrumb-item>
            <el-breadcrumb-item>系统管理</el-breadcrumb-item>
            <el-breadcrumb-item>反馈管理</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </template>
    </el-card>

    <!-- 搜索和筛选 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="反馈内容">
          <el-input
            v-model="searchForm.content"
            placeholder="请输入反馈内容"
            clearable
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item label="反馈类型">
          <el-select
            v-model="searchForm.feedbackType"
            placeholder="请选择反馈类型"
            clearable
            style="width: 150px"
          >
            <el-option label="功能建议" :value="1" />
            <el-option label="问题反馈" :value="2" />
            <el-option label="内容投诉" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="处理状态" clearable style="width: 150px">
            <el-option label="待处理" :value="1" />
            <el-option label="处理中" :value="2" />
            <el-option label="已处理" :value="3" />
            <el-option label="已关闭" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 300px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchFeedbacks">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshRight /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>



    <!-- 反馈列表 -->
    <el-card shadow="hover" class="feedbacks-card">
      <el-table
        v-loading="loading"
        :data="filteredFeedbacks"
        style="width: 100%"
      >
        <el-table-column prop="title" label="反馈标题" min-width="200">
          <template #default="scope">
            <div class="feedback-title">{{ scope.row.title }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="反馈内容" min-width="300">
          <template #default="scope">
            <div class="feedback-content">{{ scope.row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="feedbackType" label="反馈类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeColor(scope.row.feedbackType)">
              {{ getTypeText(scope.row.feedbackType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120">
          <template #default="scope">
            {{ scope.row.username || '匿名用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="contactInfo" label="联系方式" width="200">
          <template #default="scope">
            {{ scope.row.contactInfo || '无' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusColor(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminName" label="处理人" width="120" v-if="showProcessColumns">
          <template #default="scope">
            <span v-if="scope.row.status === 3">{{ scope.row.adminName || '未处理' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="processedAt" label="处理时间" width="180" v-if="showProcessColumns">
          <template #default="scope">
            <span v-if="scope.row.status === 3">{{ formatDate(scope.row.processedAt) || '未处理' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="viewFeedbackDetails(scope.row.id)"
            >
              <el-icon><View /></el-icon> 详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 反馈详情对话框 -->
    <el-dialog
      v-model="feedbackDetailDialogVisible"
      title="反馈详情"
      width="60%"
      :close-on-click-modal="false"
    >
      <div class="feedback-detail" v-if="currentFeedback">
        <el-descriptions border column="2" :title="'反馈基本信息'" class="feedback-info">
          <el-descriptions-item label="反馈ID">{{ currentFeedback.id }}</el-descriptions-item>
          <el-descriptions-item label="反馈类型">{{ getTypeText(currentFeedback.feedbackType) }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentFeedback.userId || '匿名' }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentFeedback.username || '匿名用户' }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ currentFeedback.contactInfo || '无' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDate(currentFeedback.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">{{ getStatusText(currentFeedback.status) }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ formatDate(currentFeedback.processedAt) || '未处理' }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ currentFeedback.adminName || currentFeedback.adminId || '未处理' }}</el-descriptions-item>
          <el-descriptions-item label="处理说明">{{ currentFeedback.adminReply || '无' }}</el-descriptions-item>
        </el-descriptions>

        <h3 class="detail-section">反馈标题</h3>
        <div class="feedback-title">
          {{ currentFeedback.title }}
        </div>

        <h3 class="detail-section">反馈内容</h3>
        <div class="feedback-content">
          {{ currentFeedback.content }}
        </div>

        <!-- 处理反馈表单 -->
        <template v-if="currentFeedback.status === 1">
          <h3 class="detail-section">处理反馈</h3>
          <el-form
            ref="feedbackProcessFormRef"
            :model="feedbackProcessForm"
            :rules="feedbackProcessRules"
            label-width="100px"
            class="process-form"
          >
            <el-form-item label="处理方式" prop="status">
              <el-radio-group v-model="feedbackProcessForm.status">
                <el-radio label="3">采纳</el-radio>
                <el-radio label="4">忽略</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="处理说明" prop="adminReply" v-if="feedbackProcessForm.status === '3'">
              <el-input
                v-model="feedbackProcessForm.adminReply"
                type="textarea"
                placeholder="请输入处理说明"
                rows="4"
              />
            </el-form-item>
          </el-form>
        </template>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="feedbackDetailDialogVisible = false">关闭</el-button>
          <el-button
            type="success"
            @click="handleProcessFeedback('3')"
            :disabled="currentFeedback.status !== 1"
          >
            采纳
          </el-button>
          <el-button
            type="warning"
            @click="handleProcessFeedback('4')"
            :disabled="currentFeedback.status !== 1"
          >
            忽略
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  House,
  Search,
  RefreshRight,
  View,
} from '@element-plus/icons-vue'
import { feedbacksApi } from '@/api/Feedbacks'
import { useAuthStore } from '@/stores/auth'

// 从认证存储中获取当前用户信息
const authStore = useAuthStore()

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  content: '',
  feedbackType: '',
  status: 1,
  createTime: '',
})

// 日期范围
const dateRange = ref([])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0,
})

// 反馈列表数据
const feedbacks = ref([])

// 当前反馈（详情）
const currentFeedback = ref(null)

// 反馈处理表单
const feedbackProcessForm = reactive({
  status: '3',
  adminReply: '',
})

// 反馈处理表单规则
const feedbackProcessRules = reactive({
  status: [{ required: true, message: '请选择处理方式', trigger: 'change' }],
  adminReply: [{
    required: true,
    message: '请输入处理说明',
    trigger: 'blur',
    validator: (rule, value, callback) => {
      if (feedbackProcessForm.status === '3' && !value) {
        callback(new Error('请输入处理说明'))
      } else {
        callback()
      }
    }
  }],
})

// 反馈处理表单引用
const feedbackProcessFormRef = ref()

// 对话框
const feedbackDetailDialogVisible = ref(false)

// 处理反馈操作
const handleProcessFeedback = async (status) => {
  if (!feedbackProcessFormRef.value || !currentFeedback.value) return

  // 设置处理状态
  feedbackProcessForm.status = status

  // 验证表单
  await feedbackProcessFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        const data = {
          id: currentFeedback.value.id,
          adminId: authStore.userInfo.id, // 从登录信息中获取当前管理员ID
          adminReply: status === '3' ? feedbackProcessForm.adminReply : '',
          status: Number(status),
        }
        await feedbacksApi.updateFeedback(data)
        feedbackDetailDialogVisible.value = false
        fetchFeedbacks() // 刷新反馈列表
      } catch (error) {
        console.error('处理反馈失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 计算属性：筛选后的反馈
const filteredFeedbacks = computed(() => {
  return feedbacks.value.filter(feedback => {
    const matchesContent = searchForm.content ? feedback.content.toLowerCase().includes(searchForm.content.toLowerCase()) : true
    const matchesType = searchForm.feedbackType ? feedback.feedbackType === searchForm.feedbackType : true
    const matchesStatus = searchForm.status ? feedback.status === searchForm.status : true
    
    // 日期范围筛选
    let matchesDateRange = true
    if (dateRange.value && dateRange.value.length === 2) {
      const feedbackDate = new Date(feedback.createdAt)
      const startDate = new Date(dateRange.value[0])
      const endDate = new Date(dateRange.value[1])
      // 确保时间部分也被正确处理，将结束日期设置为当天的23:59:59
      endDate.setHours(23, 59, 59, 999)
      matchesDateRange = feedbackDate >= startDate && feedbackDate <= endDate
    }
    
    return matchesContent && matchesType && matchesStatus && matchesDateRange
  })
})

// 计算总数
const total = computed(() => filteredFeedbacks.value.length)

// 计算属性：是否显示处理人一列
const showProcessColumns = computed(() => {
  return filteredFeedbacks.value.some(feedback => feedback.status === 3)
})

// 获取反馈列表
const fetchFeedbacks = async () => {
  try {
    loading.value = true
    const response = await feedbacksApi.getAllFeedbacks()
    feedbacks.value = response.data || []
  } catch (error) {
    console.error('获取反馈列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索反馈
const searchFeedbacks = () => {
  loading.value = true
  fetchFeedbacks()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    content: '',
    feedbackType: '',
    status: 1,
    createTime: '',
  })
  dateRange.value = []
  pagination.currentPage = 1
  fetchFeedbacks()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
}

// 分页页码变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
}

// 获取类型颜色
const getTypeColor = (type) => {
  const colorMap = {
    1: 'success',
    2: 'warning',
    3: 'danger',
    4: 'info',
  }
  return colorMap[type] || 'info'
}

// 获取类型文本
const getTypeText = (type) => {
  const textMap = {
    1: '功能建议',
    2: '问题反馈',
    3: '内容投诉',
    4: '其他',
  }
  return textMap[type] || type
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    1: 'warning',
    2: 'info',
    3: 'success',
    4: 'danger',
  }
  return colorMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    1: '待处理',
    2: '处理中',
    3: '已处理',
    4: '已关闭（忽略）',
  }
  return textMap[status] || status
}

// 查看反馈详情
const viewFeedbackDetails = (id) => {
  // 实际项目中，这里会调用API获取反馈详情
  const feedback = feedbacks.value.find((f) => f.id === id)
  if (feedback) {
    currentFeedback.value = feedback
    // 重置处理表单
    Object.assign(feedbackProcessForm, {
      status: '3',
      adminReply: '',
    })
    feedbackDetailDialogVisible.value = true
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

// 初始化数据
onMounted(() => {
  fetchFeedbacks()
})
</script>

<style scoped>
.admin-feedback-management {
  background-color: #f5f7fa;
}

.page-header {
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

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: end;
}

.action-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.feedbacks-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.feedback-content {
  font-weight: 500;
  white-space: pre-wrap;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-top: 10px;
}

.feedback-detail {
  padding: 20px 0;
}

.feedback-info {
  margin-bottom: 20px;
}

.detail-section {
  margin-top: 20px;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 600;
}

.process-form {
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>
