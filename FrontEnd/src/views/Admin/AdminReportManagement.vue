<template>
  <div class="admin-report-management">
    <el-card shadow="hover" class="page-header">
      <template #header>
        <div class="card-header">
          <h1>举报管理</h1>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item><el-icon><House /></el-icon>首页</el-breadcrumb-item>
            <el-breadcrumb-item>系统管理</el-breadcrumb-item>
            <el-breadcrumb-item>举报管理</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </template>
    </el-card>

    <!-- 搜索和筛选 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="举报类型">
          <el-select
            v-model="searchForm.targetType"
            placeholder="请选择举报类型"
            clearable
            style="width: 150px"
          >
            <el-option label="书籍" :value="1" />
            <el-option label="评论" :value="2" />
            <el-option label="用户" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="举报原因类型">
          <el-select
            v-model="searchForm.reasonType"
            placeholder="请选择举报原因类型"
            clearable
            style="width: 200px"
          >
            <el-option label="内容违规" :value="1" />
            <el-option label="垃圾信息" :value="2" />
            <el-option label="版权侵犯" :value="3" />
            <el-option label="恶意攻击" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择处理状态"
            clearable
            style="width: 150px"
          >
            <el-option label="待处理" :value="1" />
            <el-option label="处理中" :value="2" />
            <el-option label="已处理" :value="3" />
            <el-option label="已驳回" :value="4" />
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
          <el-button type="primary" @click="searchReports">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshRight /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>



    <!-- 举报列表 -->
    <el-card shadow="hover" class="reports-card">
      <el-table
        v-loading="loading"
        :data="paginatedReports"
        style="width: 100%"
      >
        <el-table-column prop="targetType" label="举报类型" width="120">
          <template #default="scope">
            <el-tag :type="getReportTypeColor(scope.row.targetType)">
              {{ getReportTypeText(scope.row.targetType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reasonType" label="举报原因" width="150">
          <template #default="scope">
            <el-tag :type="getReasonColor(scope.row.reasonType)">
              {{ getReasonText(scope.row.reasonType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reasonDetail" label="举报说明" min-width="200">
          <template #default="scope">
            <div class="report-content">{{ scope.row.reasonDetail }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="reporterName" label="举报人" width="120" />
        <el-table-column prop="targetName" label="被举报对象" width="120" />
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
            {{ scope.row.adminName || '未处理' }}
          </template>
        </el-table-column>
        <el-table-column prop="processedAt" label="处理时间" width="180" v-if="showProcessColumns">
          <template #default="scope">
            {{ formatDate(scope.row.processedAt) || '未处理' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="viewReportDetails(scope.row.id)"
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

    <!-- 举报详情对话框 -->
    <el-dialog
      v-model="reportDetailDialogVisible"
      title="举报详情"
      width="60%"
      :close-on-click-modal="false"
    >
      <div class="report-detail" v-if="currentReport">
        <el-descriptions border column="2" :title="'举报基本信息'" class="report-info">
          <el-descriptions-item label="举报ID">{{ currentReport.id }}</el-descriptions-item>
          <el-descriptions-item label="举报类型">{{ getReportTypeText(currentReport.targetType) }}</el-descriptions-item>
          <el-descriptions-item label="举报原因类型">{{ getReasonText(currentReport.reasonType) }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">{{ getStatusText(currentReport.status) }}</el-descriptions-item>
          <el-descriptions-item label="举报人ID">{{ currentReport.reporterId }}</el-descriptions-item>
          <el-descriptions-item label="举报人">{{ currentReport.reporterName }}</el-descriptions-item>
          <el-descriptions-item label="被举报ID">{{ currentReport.targetId }}</el-descriptions-item>
          <el-descriptions-item label="被举报对象">{{ currentReport.targetName }}</el-descriptions-item>
          <el-descriptions-item label="是否匿名">{{ currentReport.isAnonymous === 1 ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDate(currentReport.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ formatDate(currentReport.processedAt) || '未处理' }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ currentReport.adminName || '未处理' }}</el-descriptions-item>
          <el-descriptions-item label="处理备注">{{ currentReport.adminNote || '无' }}</el-descriptions-item>
        </el-descriptions>

        <h3 class="detail-section">举报原因详细描述</h3>
        <div class="report-content">
          {{ currentReport.reasonDetail }}
        </div>

        <h3 class="detail-section">证据截图</h3>
        <div class="evidence-images" v-if="currentReport.evidenceImages">
          <el-image
            v-for="(image, index) in JSON.parse(currentReport.evidenceImages)"
            :key="index"
            :src="image"
            fit="cover"
            style="width: 200px; height: 200px; margin-right: 10px; margin-bottom: 10px"
            :preview-src-list="JSON.parse(currentReport.evidenceImages)"
          />
        </div>
        <div v-else class="no-evidence">
          无证据截图
        </div>

        <!-- 处理举报表单 -->
        <template v-if="currentReport.status === 1">
          <h3 class="detail-section">处理举报</h3>
          <el-form
            ref="reportProcessFormRef"
            :model="reportProcessForm"
            :rules="reportProcessRules"
            label-width="100px"
            class="process-form"
          >
            <el-form-item label="处理方式" prop="status">
              <el-radio-group v-model="reportProcessForm.status">
                <el-radio label="3">已处理</el-radio>
                <el-radio label="4">已驳回</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="处理备注" prop="adminNote">
              <el-input
                v-model="reportProcessForm.adminNote"
                type="textarea"
                placeholder="请输入处理备注"
                rows="4"
              />
            </el-form-item>
          </el-form>
        </template>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reportDetailDialogVisible = false">关闭</el-button>
          <el-button
            type="primary"
            @click="submitReportProcess"
            v-if="currentReport && currentReport.status === 1"
          >
            提交处理
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
import { ElMessage } from 'element-plus'
import { reportsApi } from '@/api/Reports'

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  targetType: null,
  reasonType: null,
  status: null,
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

// 举报列表数据
const reports = ref([])

// 当前举报（详情）
const currentReport = ref(null)

// 举报处理表单
const reportProcessForm = reactive({
  status: 3,
  adminNote: '',
})

// 举报处理表单规则
const reportProcessRules = reactive({
  status: [{ required: true, message: '请选择处理方式', trigger: 'change' }],
  adminNote: [{ required: true, message: '请输入处理备注', trigger: 'blur' }],
})

// 举报处理表单引用
const reportProcessFormRef = ref()

// 对话框
const reportDetailDialogVisible = ref(false)

// 计算属性：筛选后的举报
const filteredReports = computed(() => {
  return reports.value.filter(report => {
    const matchesTargetType = searchForm.targetType != null ? report.targetType === searchForm.targetType : true
    const matchesReasonType = searchForm.reasonType != null ? report.reasonType === searchForm.reasonType : true
    const matchesStatus = searchForm.status != null ? report.status === searchForm.status : true
    
    // 日期范围筛选
    let matchesDateRange = true
    if (dateRange.value && dateRange.value.length === 2) {
      const reportDate = new Date(report.createdAt)
      const startDate = new Date(dateRange.value[0])
      const endDate = new Date(dateRange.value[1])
      // 确保时间部分也被正确处理，将结束日期设置为当天的23:59:59
      endDate.setHours(23, 59, 59, 999)
      matchesDateRange = reportDate >= startDate && reportDate <= endDate
    }
    
    return matchesTargetType && matchesReasonType && matchesStatus && matchesDateRange
  })
})

// 计算总数
const total = computed(() => filteredReports.value.length)

// 计算属性：分页后的举报
const paginatedReports = computed(() => {
  const startIndex = (pagination.currentPage - 1) * pagination.pageSize
  const endIndex = startIndex + pagination.pageSize
  return filteredReports.value.slice(startIndex, endIndex)
})

// 计算属性：是否显示处理列
const showProcessColumns = computed(() => {
  return filteredReports.value.some(report => report.status === 2 || report.status === 3 || report.status === 4)
})

// 获取举报列表
const fetchReports = async () => {
  try {
    loading.value = true
    const response = await reportsApi.getAllReports()
    reports.value = response.data || []
  } catch (error) {
    console.error('获取举报列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索举报
const searchReports = () => {
  loading.value = true
  fetchReports()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    targetType: null,
    reasonType: null,
    status: null,
    createTime: '',
  })
  dateRange.value = []
  pagination.currentPage = 1
  fetchReports()
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

// 获取举报类型颜色
const getReportTypeColor = (type) => {
  const colorMap = {
    1: 'primary',
    2: 'success',
    3: 'warning',
  }
  return colorMap[type] || 'info'
}

// 获取举报类型文本
const getReportTypeText = (type) => {
  const textMap = {
    1: '书籍',
    2: '评论',
    3: '用户',
  }
  return textMap[type] || type
}

// 获取举报原因颜色
const getReasonColor = (reason) => {
  const colorMap = {
    1: 'danger',
    2: 'warning',
    3: 'danger',
    4: 'danger',
    5: 'info',
  }
  return colorMap[reason] || 'info'
}

// 获取举报原因文本
const getReasonText = (reason) => {
  const textMap = {
    1: '内容违规',
    2: '垃圾信息',
    3: '版权侵犯',
    4: '恶意攻击',
    5: '其他',
  }
  return textMap[reason] || reason
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    1: 'warning',
    2: 'primary',
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
    4: '已驳回',
  }
  return textMap[status] || status
}

// 查看举报详情
const viewReportDetails = (id) => {
  // 实际项目中，这里会调用API获取举报详情
  const report = reports.value.find((r) => r.id === id)
  if (report) {
    currentReport.value = report
    // 重置处理表单
    Object.assign(reportProcessForm, {
      status: 3,
      adminNote: '',
    })
    reportDetailDialogVisible.value = true
  }
}

// 提交举报处理
const submitReportProcess = async () => {
  if (!reportProcessFormRef.value) return

  try {
    await reportProcessFormRef.value.validate()

    // 实际项目中，这里会调用API提交举报处理
    console.log('提交举报处理:', {
      id: currentReport.value.id,
      ...reportProcessForm
    })

    // 模拟成功处理
    ElMessage.success('举报处理成功')
    reportDetailDialogVisible.value = false
    fetchReports()
  } catch (error) {
    console.error('提交举报处理失败:', error)
    ElMessage.error('提交举报处理失败，请稍后重试')
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
  fetchReports()
})
</script>

<style scoped>
.admin-report-management {
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

.reports-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.report-content {
  font-weight: 500;
  white-space: pre-wrap;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-top: 10px;
}

.target-content {
  font-weight: 500;
  white-space: pre-wrap;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-top: 10px;
  border-left: 4px solid #e6a23c;
}

.report-detail {
  padding: 20px 0;
}

.report-info {
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
