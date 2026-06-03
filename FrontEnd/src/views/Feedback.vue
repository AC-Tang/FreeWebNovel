<template>
  <div class="feedback-page">
    <div class="container">
      <h1 class="page-title">意见反馈</h1>
      <p class="page-description">感谢您使用我们的服务！请将您的宝贵意见和建议告诉我们，我们会认真对待每一条反馈。</p>

      <div class="feedback-form-container">
        <el-form
          ref="feedbackFormRef"
          :model="feedbackForm"
          :rules="feedbackRules"
          label-position="top"
          class="feedback-form"
        >
          <!-- 反馈类型 -->
          <el-form-item label="反馈类型" prop="type">
            <el-select v-model="feedbackForm.type" placeholder="请选择反馈类型" class="feedback-select">
              <el-option label="功能建议" value="suggestion" />
              <el-option label="内容错误" value="content_error" />
              <el-option label="系统bug" value="bug" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>

          <!-- 反馈标题 -->
          <el-form-item label="反馈标题" prop="title">
            <el-input
              v-model="feedbackForm.title"
              placeholder="请输入反馈标题（5-50字）"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>

          <!-- 反馈内容 -->
          <el-form-item label="反馈内容" prop="content">
            <el-input
              v-model="feedbackForm.content"
              type="textarea"
              placeholder="请详细描述您的问题或建议（10-500字）"
              :rows="6"
              maxlength="500"
              show-word-limit
              resize="vertical"
            />
          </el-form-item>

          <!-- 联系方式（可选） -->
          <el-form-item label="联系方式（可选）">
            <el-input
              v-model="feedbackForm.contact"
              placeholder="请输入邮箱或手机号，方便我们联系您"
              maxlength="50"
            />
          </el-form-item>

          <!-- 验证码 -->
          <el-form-item label="验证码" prop="captcha">
            <div class="captcha-container">
              <el-input
                v-model="captchaValue"
                placeholder="请输入验证码"
                maxlength="4"
                style="width: 160px; margin-right: 10px;"
                @input="captchaValue = captchaValue.replace(/[^A-Za-z0-9]/g, '')"
              />
              <div class="captcha-image-container">
                <img
                  :src="captchaImage"
                  alt="验证码"
                  class="captcha-image"
                  @click="refreshCaptcha"
                >
                <span class="captcha-refresh" @click="refreshCaptcha">刷新</span>
              </div>
            </div>
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item>
            <div class="form-actions">
              <el-button @click="resetForm">重置</el-button>
              <el-button type="primary" @click="submitForm" :loading="submitting">
                提交反馈
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 反馈须知 -->
      <div class="feedback-notice">
        <h3 class="notice-title">反馈须知</h3>
        <ul class="notice-list">
          <li>请尽量详细描述您的问题或建议，以便我们更好地理解和解决。</li>
          <li>我们会在1-3个工作日内处理您的反馈，如需回复会通过您留下的联系方式与您取得联系。</li>
          <li>请勿提交违法、违规或与本平台无关的内容。</li>
          <li>感谢您对我们的支持与理解！</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { feedbacksApi } from '../api/Feedbacks'
import { userApi } from '../api/Users'

const authStore = useAuthStore()

// 表单引用
const feedbackFormRef = ref()

// 提交状态
const submitting = ref(false)

// 验证码相关
const captchaImage = ref('') // 验证码图片URL
const captchaKey = ref('') // 验证码key
const captchaValue = ref('') // 用户输入的验证码

// 反馈表单数据
const feedbackForm = reactive({
  type: '',
  title: '',
  content: '',
  contact: ''
})

// 表单验证规则
const feedbackRules = {
  type: [
    { required: true, message: '请选择反馈类型', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入反馈标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度在 5 到 50 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
    { min: 10, max: 500, message: '内容长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 4, message: '验证码长度为4个字符', trigger: 'blur' }
  ]
}

// 获取验证码
const getCaptcha = async () => {
  try {
    const response = await userApi.getCaptcha()
    const blob = new Blob([response.data], { type: 'image/jpeg' })
    captchaImage.value = URL.createObjectURL(blob)

    // 从响应头中获取验证码key
    captchaKey.value = response.headers['captcha-key'] || response.headers['x-captcha-id'] || Date.now().toString()
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请稍后重试')
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  getCaptcha()
}

// 重置表单
const resetForm = () => {
  if (feedbackFormRef.value) {
    feedbackFormRef.value.resetFields()
  }
  captchaValue.value = ''
  getCaptcha()
}

// 组件挂载时获取验证码
onMounted(() => {
  getCaptcha()
})

// 映射反馈类型到数字
const mapFeedbackType = (type) => {
  const typeMap = {
    'suggestion': 1, // 功能建议
    'content_error': 2, // 内容错误
    'bug': 2, // 系统bug -> 问题反馈
    'other': 4 // 其他
  }
  return typeMap[type] || 4
}

// 提交表单
const submitForm = async () => {
  if (!feedbackFormRef.value) return

  try {
    await feedbackFormRef.value.validate()
    submitting.value = true

    // 1. 验证验证码
    try {
      await userApi.verifyCaptcha({
        captcha: captchaValue.value.toLowerCase(), // 验证码不区分大小写
        captchaId: captchaKey.value
      })
    } catch{
      submitting.value = false
      ElMessage.error('验证码错误，请重新输入')
      getCaptcha() // 刷新验证码
      return
    }

    // 2. 准备提交数据
    const submitData = {
      feedbackType: mapFeedbackType(feedbackForm.type),
      title: feedbackForm.title,
      content: feedbackForm.content
    }

    // 如果用户已登录，添加userId
    if (authStore.isAuthenticated && authStore.user?.id) {
      submitData.userId = authStore.user.id
    }

    // 如果有联系方式，添加contact字段
    if (feedbackForm.contact.trim()) {
      submitData.contactInfo = feedbackForm.contact.trim()
    }

    // 3. 调用API提交反馈
    const response = await feedbacksApi.addFeedback(submitData)

    submitting.value = false

    if (response && response.code === 200) {
      ElMessage.success('反馈提交成功！感谢您的宝贵意见。')
      resetForm()
      // 可以选择返回上一页或跳转到其他页面
      // router.push('/')
    } else {
      ElMessage.error(response?.message || '反馈提交失败，请稍后重试')
    }
  } catch (error) {
    submitting.value = false
    console.error('反馈提交失败:', error)
    ElMessage.error('反馈提交失败，请稍后重试')
  }
}
</script>

<style scoped>
.feedback-page {
  min-height: 100vh;
  padding: 60px 20px;
  background-color: #f5f7fa;
}

.container {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  text-align: center;
}

.page-description {
  font-size: 16px;
  color: #606266;
  margin-bottom: 40px;
  text-align: center;
  line-height: 1.6;
}

.feedback-form-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-bottom: 30px;
}

.feedback-form {
  width: 100%;
}

.feedback-select {
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 20px;
}

.form-actions .el-button {
  padding: 10px 30px;
  font-size: 16px;
}

/* 反馈须知 */
.feedback-notice {
  background-color: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 8px;
  padding: 24px;
}

/* 验证码样式 */
.captcha-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-image-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.captcha-image {
  width: 120px;
  height: 40px;
  border-radius: 4px;
  cursor: pointer;
  object-fit: cover;
  border: 1px solid #dcdfe6;
}

.captcha-refresh {
  font-size: 14px;
  color: #409eff;
  cursor: pointer;
  user-select: none;
}

.captcha-refresh:hover {
  text-decoration: underline;
}

.notice-title {
  font-size: 18px;
  font-weight: 500;
  color: #67c23a;
  margin-bottom: 16px;
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-list li {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  position: relative;
  padding-left: 20px;
  line-height: 1.6;
}

.notice-list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #67c23a;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .feedback-page {
    padding: 40px 15px;
  }

  .page-title {
    font-size: 24px;
  }

  .page-description {
    font-size: 14px;
  }

  .feedback-form-container {
    padding: 20px;
  }

  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .feedback-notice {
    padding: 16px;
  }
}
</style>
