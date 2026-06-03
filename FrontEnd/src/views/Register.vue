<template>
  <div class="register-container">
    <div class="register-form-wrapper">
      <div class="register-title">用户注册</div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        class="register-form"
      >
        <!-- 第一步：邮箱验证 -->
        <template v-if="registerStep === 1">
          <el-form-item label="QQ邮箱" prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入QQ邮箱"
              prefix-icon="Message"
              clearable
              size="large"
              class="password-input-with-visible-btn"
            />
          </el-form-item>

          <el-form-item label="验证码" prop="emailCode">
            <div class="captcha-wrapper">
              <el-input
                v-model="registerForm.emailCode"
                placeholder="请输入验证码"
                prefix-icon="Picture"
                clearable
                size="large"
                class="captcha-input password-input-with-visible-btn"
              />
              <el-button
                type="primary"
                :disabled="countdown > 0"
                @click="handleSendEmailCode"
                size="large"
              >
                {{ countdown > 0 ? `${countdown}秒后重发` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              @click="handleVerifyEmailCode"
              size="large"
              class="register-btn"
            >
              下一步
            </el-button>
          </el-form-item>
        </template>

        <!-- 第二步：设置密码 -->
        <template v-else-if="registerStep === 2">
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              :show-password="true"
              clearable
              size="large"
              class="password-input-with-visible-btn"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              prefix-icon="Lock"
              :show-password="true"
              clearable
              size="large"
              class="password-input-with-visible-btn"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              @click="handleRegister"
              size="large"
              class="register-btn"
            >
              注册
            </el-button>
          </el-form-item>
        </template>

        <!-- 注册成功 -->
        <template v-else-if="registerStep === 3">
          <div class="register-success">
            <el-icon class="success-icon"><CircleCheck /></el-icon>
            <h3>注册成功！</h3>
            <p>您的用户名是：<span class="username">{{ generatedUsername }}</span></p>
            <p>请使用用户名或邮箱登录系统</p>
            <el-button
              type="primary"
              @click="handleLoginRedirect"
              size="large"
              class="register-btn"
              style="margin-top: 20px"
            >
              立即登录
            </el-button>
          </div>
        </template>

        <div class="register-form-footer" v-if="registerStep < 3">
          <span>已有账号？</span>
          <el-link type="primary" :underline="false" @click="handleLoginRedirect">立即登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/Users'
import { ElMessage } from 'element-plus'
import { CircleCheck } from '@element-plus/icons-vue'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

// 注册步骤：1-邮箱验证，2-设置密码，3-注册成功
const registerStep = ref(1)
// 倒计时
const countdown = ref(0)
// 生成的用户名
const generatedUsername = ref('')

// 注册表单数据
const registerForm = reactive({
  email: '',
  emailCode: '',
  password: '',
  confirmPassword: ''
})

// 表单验证规则
const registerRules = reactive({
  // 邮箱验证规则
  email: [
    { required: true, message: '请输入QQ邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
    { pattern: /^[1-9]\d{4,10}@qq\.com$/, message: '请输入有效的QQ邮箱', trigger: 'blur' }
  ],
  // 邮箱验证码规则
  emailCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  // 密码规则
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  // 确认密码规则
  confirmPassword: [
      { required: true, message: '请再次输入密码', trigger: 'blur' },
      {
        validator: (_, value, callback) => {
          if (value !== registerForm.password) {
            callback(new Error('两次输入密码不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
})

// 发送邮箱验证码
const handleSendEmailCode = async () => {
  try {
    // 验证邮箱格式
    await registerFormRef.value.validateField('email')

    // 显示加载状态
    loading.value = true

    console.log(registerForm.email)
    // 调用发送邮箱验证码接口
    await userApi.getEmailCaptcha({ email: registerForm.email })

    // 发送成功
    ElMessage.success('验证码发送成功，请查收邮箱')

    // 开始倒计时
    startCountdown()
  } catch (error) {
    // 发送失败处理
    console.error('发送验证码失败:', error)
    if (error.response) {
      ElMessage.error(error.response.data.message || '发送验证码失败')
    } else {
      ElMessage.error('发送验证码失败，请稍后重试')
    }
  } finally {
    // 隐藏加载状态
    loading.value = false
  }
}

// 开始倒计时
const startCountdown = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 验证邮箱验证码
const handleVerifyEmailCode = async () => {
  try {
    // 表单验证
    await registerFormRef.value.validate()

    // 显示加载状态
    loading.value = true

    // 调用验证邮箱验证码接口
    await userApi.verifyEmailCaptcha({
      email: registerForm.email,
      code: registerForm.emailCode
    })

    // 验证成功，进入下一步
    registerStep.value = 2
  } catch (error) {
    // 验证失败处理
    console.error('验证验证码失败:', error)
    if (error.response) {
      ElMessage.error(error.response.data.message || '验证验证码失败')
    } else {
      ElMessage.error('验证验证码失败，请稍后重试')
    }
  } finally {
    // 隐藏加载状态
    loading.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  try {
    // 表单验证（只验证密码相关字段）
    await registerFormRef.value.validateField('password')
    await registerFormRef.value.validateField('confirmPassword')

    // 显示加载状态
    loading.value = true

    // 调用注册接口
    const response = await userApi.register({
      email: registerForm.email,
      password: registerForm.password
    })

    // 注册成功，保存生成的用户名
    generatedUsername.value = response.data.username

    // 进入注册成功页面
    registerStep.value = 3
  } catch (error) {
    // 注册失败处理
    console.error('注册失败:', error)
    if (error.response) {
      ElMessage.error(error.response.data.message || '注册失败')
    } else {
      ElMessage.error('注册失败，请稍后重试')
    }
  } finally {
    // 隐藏加载状态
    loading.value = false
  }
}

// 跳转到登录页面
const handleLoginRedirect = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-form-wrapper {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.register-title {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.register-form {
  width: 100%;
}

.register-form .el-form-item {
  margin-bottom: 24px;
}

.register-btn {
  height: 48px;
  font-size: 16px;
  width: 100%;
  max-width: 200px;
  margin: 0 auto;
  display: block;
}

.register-form-footer {
  text-align: center;
  margin-top: 20px;
  color: #606266;
  font-size: 14px;
}

.register-form-footer span,
.register-form-footer .el-link {
  font-size: 14px;
  vertical-align: middle;
}

/* 增强Element Plus内置密码可见性按钮的样式 */
.password-input-with-visible-btn :deep(.el-input__suffix-inner) {
  display: flex;
  align-items: center;
  gap: 8px;
}

.password-input-with-visible-btn :deep(.el-input__icon) {
  color: #606266 !important; /* 加深颜色，提高可见性 */
  font-size: 18px !important; /* 增大图标大小 */
  width: 24px !important;
  height: 24px !important;
  transition: all 0.3s !important;
}

.password-input-with-visible-btn :deep(.el-input__icon:hover) {
  color: #409eff !important; /* 悬停时变为蓝色 */
  transform: scale(1.1) !important; /* 悬停放大效果 */
  cursor: pointer !important;
}

/* 确保清除按钮也有良好的样式 */
.password-input-with-visible-btn :deep(.el-input__clear) {
  font-size: 16px !important;
  width: 20px !important;
  height: 20px !important;
}

.password-input-with-visible-btn :deep(.el-input__clear:hover) {
  color: #f56c6c !important;
  transform: scale(1.1) !important;
}
</style>
