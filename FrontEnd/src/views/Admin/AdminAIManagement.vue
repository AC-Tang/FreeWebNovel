<template>
  <div class="admin-ai-management">
    <el-card shadow="hover" class="page-header">
      <template #header>
        <div class="card-header">
          <h1>AI模型管理</h1>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item><el-icon><House /></el-icon>首页</el-breadcrumb-item>
            <el-breadcrumb-item>AI管理</el-breadcrumb-item>
            <el-breadcrumb-item>AI模型管理</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </template>
    </el-card>

    <!-- 标签页 -->
    <el-card shadow="hover" class="main-card">
      <el-tabs v-model="activeTab" class="ai-tabs">
        <!-- 模型配置 -->
        <el-tab-pane label="模型配置" name="config">
          <div class="model-config">
            <h3>推荐算法参数</h3>
            <el-form :model="algorithmParams" label-width="180px" class="config-form">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="协同过滤权重">
                    <el-slider
                      v-model="algorithmParams.collaborativeWeight"
                      :min="0"
                      :max="1"
                      :step="0.1"
                      show-input
                    />
                    <span class="form-tip">协同过滤算法在推荐中的权重</span>
                  </el-form-item>
                  <el-form-item label="内容推荐权重">
                    <el-slider
                      v-model="algorithmParams.contentWeight"
                      :min="0"
                      :max="1"
                      :step="0.1"
                      show-input
                    />
                    <span class="form-tip">基于内容的推荐算法在推荐中的权重</span>
                  </el-form-item>
                  <el-form-item label="热门推荐权重">
                    <el-slider
                      v-model="algorithmParams.hotWeight"
                      :min="0"
                      :max="1"
                      :step="0.1"
                      show-input
                    />
                    <span class="form-tip">热门推荐在推荐中的权重</span>
                  </el-form-item>
                  <el-form-item label="最新推荐权重">
                    <el-slider
                      v-model="algorithmParams.newWeight"
                      :min="0"
                      :max="1"
                      :step="0.1"
                      show-input
                    />
                    <span class="form-tip">最新推荐在推荐中的权重</span>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="相似度阈值">
                    <el-slider
                      v-model="algorithmParams.similarityThreshold"
                      :min="0"
                      :max="1"
                      :step="0.05"
                      show-input
                    />
                    <span class="form-tip">物品相似度计算的阈值</span>
                  </el-form-item>
                  <el-form-item label="推荐数量">
                    <el-input-number
                      v-model="algorithmParams.recommendCount"
                      :min="5"
                      :max="50"
                      :step="5"
                    />
                    <span class="form-tip">每次推荐的书籍数量</span>
                  </el-form-item>
                  <el-form-item label="历史记录长度">
                    <el-input-number
                      v-model="algorithmParams.historyLength"
                      :min="10"
                      :max="100"
                      :step="10"
                    />
                    <span class="form-tip">用于推荐的历史记录长度</span>
                  </el-form-item>
                  <el-form-item label="更新频率(小时)">
                    <el-input-number
                      v-model="algorithmParams.updateFrequency"
                      :min="1"
                      :max="24"
                      :step="1"
                    />
                    <span class="form-tip">模型自动更新的频率</span>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item>
                <el-button type="primary" @click="saveAlgorithmParams">保存参数</el-button>
                <el-button @click="resetAlgorithmParams">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 模型管理 -->
        <el-tab-pane label="模型管理" name="model">
          <div class="model-management">
            <h3>AI模型管理</h3>
            <el-card shadow="hover" class="model-info-card">
              <h4>当前模型信息</h4>
              <el-descriptions border column="3" class="model-info">
                <el-descriptions-item label="模型名称">{{ currentModel.name }}</el-descriptions-item>
                <el-descriptions-item label="版本">{{ currentModel.version }}</el-descriptions-item>
                <el-descriptions-item label="部署时间">{{ formatDate(currentModel.deployTime) }}</el-descriptions-item>
                <el-descriptions-item label="状态">{{ currentModel.status === 'running' ? '运行中' : '已停用' }}</el-descriptions-item>
                <el-descriptions-item label="准确率">{{ currentModel.accuracy }}%</el-descriptions-item>
                <el-descriptions-item label="召回率">{{ currentModel.recall }}%</el-descriptions-item>
              </el-descriptions>
              <div class="model-actions">
                <el-button
                  type="primary"
                  @click="toggleModelStatus"
                >
                  <el-icon><SwitchButton /></el-icon>
                  {{ currentModel.status === 'running' ? '停用模型' : '启用模型' }}
                </el-button>
                <el-button
                  type="success"
                  @click="testModel"
                >
                  <el-icon><Document /></el-icon> 测试模型
                </el-button>
              </div>
            </el-card>

            <h3>模型上传</h3>
            <el-card shadow="hover" class="model-upload-card">
              <el-upload
                class="model-upload"
                action="#"
                :auto-upload="false"
                :on-change="handleModelUpload"
                :file-list="modelFileList"
                accept=".h5,.pt,.pb"
              >
                <el-button type="primary" icon="Upload">选择模型文件</el-button>
                <template #tip>
                  <div class="upload-tip">
                    支持上传 .h5, .pt, .pb 格式的模型文件，文件大小不超过 100MB
                  </div>
                </template>
              </el-upload>
              <el-form :model="modelUploadForm" label-width="120px" class="upload-form">
                <el-form-item label="模型名称" prop="name">
                  <el-input v-model="modelUploadForm.name" placeholder="请输入模型名称" />
                </el-form-item>
                <el-form-item label="版本" prop="version">
                  <el-input v-model="modelUploadForm.version" placeholder="请输入版本号" />
                </el-form-item>
                <el-form-item label="描述" prop="description">
                  <el-input
                    v-model="modelUploadForm.description"
                    type="textarea"
                    placeholder="请输入模型描述"
                    rows="3"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="uploadModel" :disabled="modelFileList.length === 0">上传模型</el-button>
                  <el-button @click="cancelUpload">取消</el-button>
                </el-form-item>
              </el-form>
            </el-card>

            <h3>模型历史</h3>
            <el-table
              :data="modelHistory"
              style="width: 100%"
              class="model-history-table"
            >
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="模型名称" min-width="150" />
              <el-table-column prop="version" label="版本" width="100" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
                    {{ scope.row.status === 'active' ? '当前使用' : '已归档' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="accuracy" label="准确率" width="100">
                <template #default="scope">
                  {{ scope.row.accuracy }}%
                </template>
              </el-table-column>
              <el-table-column prop="recall" label="召回率" width="100">
                <template #default="scope">
                  {{ scope.row.recall }}%
                </template>
              </el-table-column>
              <el-table-column prop="deployTime" label="部署时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.deployTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="uploader" label="上传人" width="120" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="scope">
                  <el-button
                    type="primary"
                    size="small"
                    @click="rollbackModel(scope.row.id)"
                    :disabled="scope.row.status === 'active'"
                  >
                    <el-icon><RefreshRight /></el-icon> 回滚
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 推荐规则 -->
        <el-tab-pane label="推荐规则" name="rules">
          <div class="recommend-rules">
            <h3>推荐规则管理</h3>
            <el-card shadow="hover" class="rules-card">
              <h4>推荐规则列表</h4>
              <el-table
                :data="recommendRules"
                style="width: 100%"
                class="rules-table"
              >
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="规则名称" min-width="150" />
                <el-table-column prop="type" label="规则类型" width="120">
                  <template #default="scope">
                    <el-tag :type="getRuleTypeColor(scope.row.type)">
                      {{ getRuleTypeText(scope.row.type) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="priority" label="优先级" width="100">
                  <template #default="scope">
                    <el-tag :type="getPriorityColor(scope.row.priority)">
                      {{ getPriorityText(scope.row.priority) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-switch
                      v-model="scope.row.status"
                      active-value="1"
                      inactive-value="0"
                      @change="handleRuleStatusChange(scope.row)"
                    >
                      <template #active>启用</template>
                      <template #inactive>禁用</template>
                    </el-switch>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="描述" min-width="200" />
                <el-table-column prop="createTime" label="创建时间" width="180">
                  <template #default="scope">
                    {{ formatDate(scope.row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="scope">
                    <el-button
                      type="primary"
                      size="small"
                      @click="editRule(scope.row)"
                    >
                      <el-icon><EditPen /></el-icon> 编辑
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      @click="deleteRule(scope.row.id)"
                    >
                      <el-icon><Delete /></el-icon> 删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="rules-actions">
                <el-button type="primary" @click="addRule">
                  <el-icon><Plus /></el-icon> 添加规则
                </el-button>
                <el-button
                  type="warning"
                  @click="batchUpdatePriority"
                  :disabled="selectedRules.length === 0"
                >
                  <el-icon><Promotion /></el-icon> 批量调整优先级
                </el-button>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- 推荐效果 -->
        <el-tab-pane label="推荐效果" name="effect">
          <div class="recommend-effect">
            <h3>推荐效果分析</h3>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="card-title">
                      <h4>推荐准确率趋势</h4>
                    </div>
                  </template>
                  <div class="chart-placeholder">
                    <el-empty description="准确率趋势图" />
                  </div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="card-title">
                      <h4>推荐召回率趋势</h4>
                    </div>
                  </template>
                  <div class="chart-placeholder">
                    <el-empty description="召回率趋势图" />
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px">
              <el-col :span="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="card-title">
                      <h4>用户点击率分布</h4>
                    </div>
                  </template>
                  <div class="chart-placeholder">
                    <el-empty description="点击率分布图" />
                  </div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover" class="chart-card">
                  <template #header>
                    <div class="card-title">
                      <h4>推荐类型分布</h4>
                    </div>
                  </template>
                  <div class="chart-placeholder">
                    <el-empty description="推荐类型分布图" />
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 测试模型对话框 -->
    <el-dialog
      v-model="testModelDialogVisible"
      title="测试模型效果"
      width="60%"
      :close-on-click-modal="false"
    >
      <div class="model-test">
        <h3>测试结果</h3>
        <el-descriptions border column="2" class="test-results">
          <el-descriptions-item label="测试时间">{{ formatDate(testResults.testTime) }}</el-descriptions-item>
          <el-descriptions-item label="测试数据集">{{ testResults.dataset }}</el-descriptions-item>
          <el-descriptions-item label="准确率">{{ testResults.accuracy }}%</el-descriptions-item>
          <el-descriptions-item label="召回率">{{ testResults.recall }}%</el-descriptions-item>
          <el-descriptions-item label="F1分数">{{ testResults.f1Score }}%</el-descriptions-item>
          <el-descriptions-item label="覆盖率">{{ testResults.coverage }}%</el-descriptions-item>
        </el-descriptions>

        <h3>测试日志</h3>
        <el-input
          type="textarea"
          :rows="10"
          v-model="testResults.logs"
          readonly
          class="test-logs"
        />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="testModelDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  House,
  SwitchButton,
  Document,
  RefreshRight,
  EditPen,
  Delete,
  Plus,
  Promotion,
} from '@element-plus/icons-vue'

// 活动标签页
const activeTab = ref('config')

// 算法参数
const algorithmParams = reactive({
  collaborativeWeight: 0.4,
  contentWeight: 0.3,
  hotWeight: 0.2,
  newWeight: 0.1,
  similarityThreshold: 0.5,
  recommendCount: 20,
  historyLength: 50,
  updateFrequency: 24,
})

// 当前模型
const currentModel = ref({
  id: 1,
  name: 'AI推荐模型',
  version: '1.0.0',
  deployTime: new Date(),
  status: 'running',
  accuracy: 85.5,
  recall: 78.2,
})

// 模型文件列表
const modelFileList = ref([])

// 模型上传表单
const modelUploadForm = reactive({
  name: '',
  version: '',
  description: '',
})

// 模型历史
const modelHistory = ref([
  {
    id: 1,
    name: 'AI推荐模型',
    version: '1.0.0',
    status: 'active',
    accuracy: 85.5,
    recall: 78.2,
    deployTime: new Date(),
    uploader: 'admin',
  },
  {
    id: 2,
    name: 'AI推荐模型',
    version: '0.9.0',
    status: 'archived',
    accuracy: 82.3,
    recall: 75.1,
    deployTime: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
    uploader: 'admin',
  },
])

// 推荐规则
const recommendRules = ref([
  {
    id: 1,
    name: '热门推荐规则',
    type: 'hot',
    priority: 'high',
    status: '1',
    description: '基于书籍热度的推荐规则',
    createTime: new Date(),
  },
  {
    id: 2,
    name: '相似推荐规则',
    type: 'similar',
    priority: 'medium',
    status: '1',
    description: '基于书籍相似度的推荐规则',
    createTime: new Date(),
  },
  {
    id: 3,
    name: '最新推荐规则',
    type: 'new',
    priority: 'low',
    status: '1',
    description: '基于最新上架的推荐规则',
    createTime: new Date(),
  },
])

// 选中的规则
const selectedRules = ref([])

// 测试模型对话框
const testModelDialogVisible = ref(false)
const testResults = ref({
  testTime: new Date(),
  dataset: '测试集A',
  accuracy: 86.2,
  recall: 79.5,
  f1Score: 82.7,
  coverage: 92.3,
  logs: '测试开始...\n加载模型...\n测试进行中...\n测试完成！',
})

// 保存算法参数
const saveAlgorithmParams = () => {
  // 实际项目中，这里会调用API保存算法参数
  console.log('保存算法参数:', algorithmParams)
}

// 重置算法参数
const resetAlgorithmParams = () => {
  Object.assign(algorithmParams, {
    collaborativeWeight: 0.4,
    contentWeight: 0.3,
    hotWeight: 0.2,
    newWeight: 0.1,
    similarityThreshold: 0.5,
    recommendCount: 20,
    historyLength: 50,
    updateFrequency: 24,
  })
}

// 切换模型状态
const toggleModelStatus = () => {
  // 实际项目中，这里会调用API切换模型状态
  currentModel.value.status = currentModel.value.status === 'running' ? 'stopped' : 'running'
}

// 测试模型
const testModel = () => {
  // 实际项目中，这里会调用API测试模型
  testModelDialogVisible.value = true
}

// 处理模型上传
const handleModelUpload = (file, fileList) => {
  modelFileList.value = fileList
}

// 上传模型
const uploadModel = () => {
  // 实际项目中，这里会调用API上传模型
  console.log('上传模型:', modelUploadForm, modelFileList.value)
}

// 取消上传
const cancelUpload = () => {
  modelFileList.value = []
  Object.assign(modelUploadForm, {
    name: '',
    version: '',
    description: '',
  })
}

// 回滚模型
const rollbackModel = (id) => {
  // 实际项目中，这里会调用API回滚模型
  console.log('回滚模型:', id)
}

// 处理规则状态变化
const handleRuleStatusChange = (row) => {
  // 实际项目中，这里会调用API更新规则状态
  console.log('更新规则状态:', row.id, row.status)
}

// 编辑规则
const editRule = (row) => {
  // 实际项目中，这里会跳转到编辑规则页面
  console.log('编辑规则:', row.id)
}

// 删除规则
const deleteRule = (id) => {
  // 实际项目中，这里会调用API删除规则
  console.log('删除规则:', id)
}

// 添加规则
const addRule = () => {
  // 实际项目中，这里会跳转到添加规则页面
  console.log('添加规则')
}

// 批量调整优先级
const batchUpdatePriority = () => {
  // 实际项目中，这里会调用API批量调整规则优先级
  console.log('批量调整优先级:', selectedRules.value)
}

// 获取规则类型颜色
const getRuleTypeColor = (type) => {
  const colorMap = {
    hot: 'warning',
    similar: 'primary',
    new: 'success',
  }
  return colorMap[type] || 'info'
}

// 获取规则类型文本
const getRuleTypeText = (type) => {
  const textMap = {
    hot: '热门推荐',
    similar: '相似推荐',
    new: '最新推荐',
  }
  return textMap[type] || type
}

// 获取优先级颜色
const getPriorityColor = (priority) => {
  const colorMap = {
    high: 'danger',
    medium: 'warning',
    low: 'success',
  }
  return colorMap[priority] || 'info'
}

// 获取优先级文本
const getPriorityText = (priority) => {
  const textMap = {
    high: '高',
    medium: '中',
    low: '低',
  }
  return textMap[priority] || priority
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
  // 实际项目中，这里会调用API获取各种数据
  console.log('AdminAIManagement mounted')
})
</script>

<style scoped>
.admin-ai-management {
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

.main-card {
  margin-bottom: 20px;
}

.ai-tabs {
  padding: 20px 0;
}

.ai-tabs .el-tabs__header {
  margin-bottom: 20px;
}

/* 模型配置 */
.model-config h3,
.model-management h3,
.recommend-rules h3,
.recommend-effect h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
}

.config-form {
  max-width: 800px;
}

.form-tip {
  display: block;
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
}

/* 模型管理 */
.model-info-card {
  margin-bottom: 20px;
  padding: 20px;
}

.model-info-card h4,
.model-upload-card h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
}

.model-info {
  margin-bottom: 20px;
}

.model-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
  margin-top: 20px;
}

.model-upload-card {
  margin-bottom: 20px;
  padding: 20px;
}

.model-upload {
  margin-bottom: 20px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
}

.upload-form {
  max-width: 600px;
}

.model-history-table {
  margin-top: 20px;
}

/* 推荐规则 */
.rules-card {
  padding: 20px;
}

.rules-card h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
}

.rules-table {
  margin-bottom: 20px;
}

.rules-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
}

/* 推荐效果 */
.chart-card {
  height: 300px;
}

.chart-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.chart-placeholder {
  height: 250px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 模型测试 */
.model-test h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
}

.test-results {
  margin-bottom: 20px;
}

.test-logs {
  margin-top: 10px;
  background-color: #f5f7fa;
  border: 1px solid #e6e6e6;
}

.dialog-footer {
  text-align: right;
}
</style>
