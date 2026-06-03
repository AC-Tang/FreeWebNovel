import { ElMessage } from 'element-plus'

class ErrorHandler {
  constructor() {
    this.errorListeners = []
  }

  // 添加错误监听器
  addErrorListener(listener) {
    this.errorListeners.push(listener)
  }

  // 移除错误监听器
  removeErrorListener(listener) {
    const index = this.errorListeners.indexOf(listener)
    if (index > -1) {
      this.errorListeners.splice(index, 1)
    }
  }

  // 通知所有错误监听器
  notifyErrorListeners(error) {
    this.errorListeners.forEach(listener => {
      try {
        listener(error)
      } catch (e) {
        console.error('Error in error listener:', e)
      }
    })
  }

  // 处理API错误
  handleApiError(error, customMessage = '') {
    let message = customMessage || '操作失败，请稍后再试'
    
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      const data = error.response.data
      
      switch (status) {
        case 400:
          message = data.message || '请求参数错误'
          break
        case 401:
          message = '登录已过期，请重新登录'
          this.handleAuthError()
          break
        case 403:
          message = '没有权限执行此操作'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误，请稍后再试'
          break
        default:
          message = data.message || `请求失败 (${status})`
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      message = '网络连接失败，请检查网络设置'
    } else {
      // 请求配置出错
      message = error.message || '请求配置错误'
    }
    
    // 显示错误消息
    ElMessage.error(message)
    
    // 通知错误监听器
    this.notifyErrorListeners({
      type: 'api',
      message,
      error
    })
    
    return message
  }

  // 处理认证错误
  handleAuthError() {
    // 清除本地存储的用户信息
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    
    // 跳转到登录页
    window.location.href = '/login'
  }

  // 处理网络错误
  handleNetworkError(error) {
    const message = '网络连接失败，请检查网络设置'
    ElMessage.error(message)
    
    this.notifyErrorListeners({
      type: 'network',
      message,
      error
    })
    
    return message
  }

  // 处理JavaScript错误
  handleJsError(error, errorInfo = {}) {
    console.error('JavaScript Error:', error, errorInfo)
    
    // 在生产环境中，可以将错误发送到错误收集服务
    if (import.meta.env.PROD) {
      // 这里可以集成错误收集服务，如Sentry
      // Sentry.captureException(error)
    }
    
    // 显示友好的错误消息
    const message = '应用运行出错，请刷新页面重试'
    ElMessage.error(message)
    
    this.notifyErrorListeners({
      type: 'javascript',
      message,
      error,
      errorInfo
    })
    
    return message
  }

  // 处理未捕获的Promise拒绝
  handleUnhandledRejection(event) {
    console.error('Unhandled Promise Rejection:', event.reason)
    
    const message = '操作失败，请稍后再试'
    ElMessage.error(message)
    
    this.notifyErrorListeners({
      type: 'promise',
      message,
      error: event.reason
    })
    
    // 阻止默认的控制台错误输出
    event.preventDefault()
  }

  // 显示自定义错误消息
  showError(message, type = 'error') {
    ElMessage({
      message,
      type,
      duration: 3000
    })
    
    this.notifyErrorListeners({
      type: 'custom',
      message,
      errorType: type
    })
  }

  // 显示成功消息
  showSuccess(message) {
    ElMessage.success(message)
  }

  // 显示警告消息
  showWarning(message) {
    ElMessage.warning(message)
  }

  // 显示信息消息
  showInfo(message) {
    ElMessage.info(message)
  }
}

// 创建全局错误处理器实例
const errorHandler = new ErrorHandler()

// 在全局范围内捕获未处理的错误
if (typeof window !== 'undefined') {
  window.addEventListener('error', (event) => {
    errorHandler.handleJsError(event.error, {
      filename: event.filename,
      lineno: event.lineno,
      colno: event.colno
    })
  })

  window.addEventListener('unhandledrejection', (event) => {
    errorHandler.handleUnhandledRejection(event)
  })
}

export default errorHandler