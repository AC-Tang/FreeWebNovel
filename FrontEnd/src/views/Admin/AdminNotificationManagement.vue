<template>
  <div class="admin-notification-management">
    <el-card shadow="hover" class="page-header">
      <template #header>
        <div class="card-header">
          <h1>发送通知</h1>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item><el-icon><House /></el-icon>首页</el-breadcrumb-item>
            <el-breadcrumb-item>系统管理</el-breadcrumb-item>
            <el-breadcrumb-item>发送通知</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </template>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button type="primary" @click="showCreateNotificationDialog">
        <el-icon><Plus /></el-icon> 新建通知
      </el-button>
    </div>

    <!-- 发送通知对话框 -->
    <el-dialog
      v-model="notificationDialogVisible"
      title="发送通知"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="notificationFormRef"
        :model="notificationForm"
        :rules="notificationRules"
        label-width="120px"
        class="notification-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="通知标题" prop="title">
              <el-input
                v-model="notificationForm.title"
                placeholder="请输入通知标题"
              />
            </el-form-item>
            <el-form-item label="通知类型" prop="type">
              <el-select
                v-model="notificationForm.type"
                placeholder="请选择通知类型"
              >
                <el-option label="评论回复" :value="1" />
                <el-option label="收到点赞" :value="2" />
                <el-option label="书籍更新" :value="3" />
                <el-option label="请求处理" :value="4" />
                <el-option label="系统通知" :value="5" />
                <el-option label="个人通知" :value="6" />
              </el-select>
            </el-form-item>
            <el-form-item label="接收对象" prop="targetType">
              <el-radio-group v-model="notificationForm.targetType">
                <el-radio label="all">全体用户</el-radio>
                <el-radio label="user">指定用户</el-radio>
                <el-radio label="role">用户类型</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="notificationForm.targetType === 'user'" label="用户ID">
              <el-input
                v-model="notificationForm.userIds"
                placeholder="请输入用户ID，多个ID用逗号分隔"
              />
            </el-form-item>
            <el-form-item v-if="notificationForm.targetType === 'role'" label="用户类型">
              <el-select
                v-model="notificationForm.userIds"
                placeholder="请选择用户类型"
              >
                <el-option label="普通用户" :value="'ROLE_USER'" />
                <el-option label="管理员" :value="'ROLE_ADMIN'" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="通知内容" prop="content">
          <el-input
            v-model="notificationForm.content"
            type="textarea"
            placeholder="请输入通知内容"
            rows="8"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="notificationDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitNotificationForm">发送</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import {
  House,
  Plus,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { notificationApi } from '@/api/Notification'

// 加载状态
const loading = ref(false)

// 通知表单
const notificationForm = reactive({
  title: '',
  content: '',
  type: 5, // 默认系统通知
  targetType: 'all',
  userIds: '',
})

// 通知表单规则
const notificationRules = reactive({
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
  targetType: [{ required: true, message: '请选择接收对象', trigger: 'change' }],
})

// 通知表单引用
const notificationFormRef = ref()

// 对话框
const notificationDialogVisible = ref(false)

// 显示创建通知对话框
const showCreateNotificationDialog = () => {
  resetNotificationForm()
  notificationDialogVisible.value = true
}

// 重置通知表单
const resetNotificationForm = () => {
  Object.assign(notificationForm, {
    title: '',
    content: '',
    type: 5, // 默认系统通知
    targetType: 'all',
    userIds: '',
  })
  if (notificationFormRef.value) {
    notificationFormRef.value.resetFields()
  }
}

// 提交通知表单（发送通知）
const submitNotificationForm = () => {
  if (!notificationFormRef.value) return
  
  notificationFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 准备提交数据
        const submitData = {
          ...notificationForm,
          // 根据接收对象类型设置userIds
          userIds: notificationForm.targetType === 'all' ? 'ALL' : notificationForm.userIds
        }
        
        // 使用指定的API发送通知
        await notificationApi.addNotification(submitData)
        ElMessage.success('通知发送成功')
        notificationDialogVisible.value = false
      } catch (error) {
        console.error('发送通知失败:', error)
        ElMessage.error('发送通知失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.admin-notification-management {
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

.action-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.notification-form {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
