<template>
  <div class="login-container">
    <div class="login-form-wrapper">
      <div class="login-title">欢迎登录</div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        class="login-form"
      >
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名或邮箱"
            prefix-icon="User"
            clearable
            size="large"
            class="password-input-with-visible-btn"
            @focus="handleInputFocus('username')"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            :show-password="true"
            clearable
            size="large"
            class="password-input-with-visible-btn"
            @focus="handleInputFocus('password')"
          />
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-wrapper">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              prefix-icon="Picture"
              clearable
              size="large"
              class="captcha-input password-input-with-visible-btn"
              @focus="handleInputFocus('captcha')"
            />
            <div class="captcha-image-wrapper">
              <img
                :src="captchaUrl"
                alt="验证码"
                class="captcha-image"
                @click="refreshCaptcha"
                title="点击刷新验证码"
              />
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="login-form-footer">
            <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
            <div class="register-link-wrapper">
              <el-link type="primary" :underline="false" @click="handleRegister">立即注册</el-link>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            size="large"
            class="login-btn"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/Users'
import { ElMessage } from 'element-plus'
import { cookieUtil } from '@/utils/request'

const router = useRouter()
const authStore = useAuthStore()
const loginFormRef = ref(null)
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
  email: '',
  captcha: '',
  rememberMe: false
})

// 登录失败状态标记，为每个输入框单独管理
const inputClearStates = reactive({
  username: false,
  password: false,
  captcha: false
})

// 监听用户名输入变化，自动设置邮箱字段
watch(() => loginForm.username, (newVal) => {
  const emailRegex = /^[1-9]\d{4,10}@qq\.com$/;
  if (emailRegex.test(newVal)) {
    loginForm.email = newVal;
  } else {
    loginForm.email = '';
  }
})

// 验证码相关变量
const captchaUrl = ref('')
const captchaId = ref('')

// 设置所有输入框的清除状态为true
const setAllInputsClearable = () => {
  inputClearStates.username = true;
  inputClearStates.password = true;
  inputClearStates.captcha = true;
}

// 输入框获取焦点时的处理函数
const handleInputFocus = (field) => {
  if (inputClearStates[field]) {
    loginForm[field] = '';
    // 清除对应字段后重置该字段的清除状态
    inputClearStates[field] = false;
  }
}

// 表单验证规则
const loginRules = reactive({
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        // 支持用户名（3-20位）或QQ邮箱格式（前缀纯数字）
        const usernameRegex = /^[a-zA-Z0-9_-]{3,20}$/;
        const emailRegex = /^[1-9]\d{4,10}@qq\.com$/;
        if (usernameRegex.test(value) || emailRegex.test(value)) {
          callback();
        } else {
          callback(new Error('请输入有效的用户名或QQ邮箱'));
        }
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 6, message: '验证码长度不正确', trigger: 'blur' }
  ]
})

// 获取验证码
const getCaptcha = async () => {
  try {
    // 使用封装的API获取验证码
    const response = await userApi.getCaptcha()

    // 获取验证码ID（从axios响应头中获取）
    captchaId.value = response.headers.get('X-Captcha-Id')

    // 将响应数据转换为Blob并创建URL
    const blob = new Blob([response.data], { type: 'image/jpeg' })
    captchaUrl.value = URL.createObjectURL(blob)
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请稍后重试')
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  getCaptcha()
}

// 处理登录
const handleLogin = async () => {
  try {
    // 表单验证
    await loginFormRef.value.validate()

    // 显示加载状态
    loading.value = true

    // 先验证验证码
    try {
      await userApi.verifyCaptcha({
        captcha: loginForm.captcha,
        captchaId: captchaId.value
      })
    } catch (error) {
      console.error('验证码验证失败:', error)
      // 设置所有输入框可清除
      setAllInputsClearable()
      // 刷新验证码
      refreshCaptcha()
      return
    }

    console.log('登录数据:', loginForm)
    
    // 准备登录数据，确保username和email必有一个为空
    const loginData = {
      password: loginForm.password,
      rememberMe: loginForm.rememberMe
    }
    
    // 根据输入内容判断是用户名登录还是邮箱登录
    const emailRegex = /^[1-9]\d{4,10}@qq\.com$/;
    if (emailRegex.test(loginForm.username)) {
      loginData.email = loginForm.username;
      loginData.username = '';
    } else {
      loginData.username = loginForm.username;
      loginData.email = '';
    }
    
    // 使用authStore的login方法处理登录，传入处理后的登录数据
    const result = await authStore.login(loginData)

    if (result.success) {
      // 存储token到Cookie，设置7天过期
      cookieUtil.set('token', authStore.token, { expires: 7, path: '/', sameSite: 'Lax' })

      // 记住密码逻辑
      if (loginForm.rememberMe) {
        cookieUtil.set('rememberedUsername', loginForm.username, { expires: 30, path: '/', sameSite: 'Lax' })
      } else {
        cookieUtil.remove('rememberedUsername', { path: '/' })
      }

      // 调试输出：登录成功后的token
      console.log('登录成功，token:', authStore.token)

      // 显示成功消息
      ElMessage.success('登录成功')

      // 跳转到原页面或首页
      const redirect = router.currentRoute.value.query.redirect
      router.push(redirect ? redirect.toString() : '/')
    } else {
      // 显示登录失败消息
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    // 登录失败处理
    console.error('登录失败:', error)
    // 错误信息已在响应拦截器中处理，不再重复提示
    // 设置所有输入框可清除
    setAllInputsClearable()
  } finally {
    // 隐藏加载状态
    loading.value = false
  }
}

// 处理注册跳转
const handleRegister = () => {
  router.push('/register')
}

// 页面加载时，检查是否有记住的用户名并获取验证码
onMounted(() => {
  const rememberedUsername = cookieUtil.get('rememberedUsername')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.rememberMe = true
  }

  // 获取验证码
  getCaptcha()
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-form-wrapper {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.login-title {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.login-form {
  width: 100%;
}

.login-form .el-form-item {
  margin-bottom: 24px;
}

.login-form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  width: 100%;
}

.register-link-wrapper {
  display: flex;
  justify-content: flex-end;
}

.login-btn {
  height: 48px;
  font-size: 16px;
  width: 100%;
  max-width: 200px;
  margin: 0 auto;
  display: block;
}

/* 固定验证码输入框宽度，防止出现清除按钮时宽度变化 */
.captcha-input {
  flex: 1;
  min-width: 0;
}

.captcha-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
  width: 100%;
}

/* 验证码图片样式 */
.captcha-image-wrapper {
  display: flex;
  align-items: center;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

.captcha-image:hover {
  border-color: #409eff;
  transform: scale(1.02);
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
