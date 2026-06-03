import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import router from '@/router'

// Cookie 处理工具函数
const cookieUtil = {
  // 设置 Cookie
  set(name, value, options = {}) {
    let cookieStr = `${encodeURIComponent(name)}=${encodeURIComponent(value)}`

    // 设置过期时间
    if (options.expires) {
      const date = new Date()
      date.setTime(date.getTime() + options.expires * 24 * 60 * 60 * 1000)
      cookieStr += `; expires=${date.toUTCString()}`
    }

    // 设置路径
    if (options.path) {
      cookieStr += `; path=${options.path}`
    }

    // 设置域
    if (options.domain) {
      cookieStr += `; domain=${options.domain}`
    }

    // 设置 secure
    if (options.secure) {
      cookieStr += '; secure'
    }

    // 设置 SameSite
    if (options.sameSite) {
      cookieStr += `; samesite=${options.sameSite}`
    }

    document.cookie = cookieStr
  },

  // 获取 Cookie
  get(name) {
    const cookieStr = document.cookie
    const cookies = cookieStr.split('; ')

    for (const cookie of cookies) {
      const [cookieName, cookieValue] = cookie.split('=')
      if (decodeURIComponent(cookieName) === name) {
        return decodeURIComponent(cookieValue)
      }
    }

    return null
  },

  // 删除 Cookie
  remove(name, options = {}) {
    // 设置过期时间为过去时间即可删除
    this.set(name, '', {
      ...options,
      expires: -1,
    })
  },
}

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api', // API 基础路径
  timeout: 15000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
  },
})

// Token 刷新相关变量
let isRefreshing = false // 是否正在刷新token
let refreshSubscribers = [] // 存储需要刷新token的请求

// 刷新token的函数
const refreshToken = async () => {
  try {
    const refreshToken = cookieUtil.get('refreshToken')
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }

    // 调用刷新token的API，使用正确的路径
    const response = await axios.post(
      `${import.meta.env.VITE_API_BASE_URL || '/api'}/refresh_token`,
      { refreshToken },
      {
        headers: {
          'Content-Type': 'application/json;charset=utf-8',
          Authorization: `Bearer ${refreshToken}`, // 在请求头中也添加refreshToken
        },
        timeout: 15000,
      },
    )

    // 解析响应，获取新的token和refreshToken
    const { accessToken: newToken, refreshToken: newRefreshToken } = response.token
    // 更新Cookie中的token和refreshToken
    cookieUtil.set('token', newToken, { expires: 7, path: '/', sameSite: 'Lax' })
    if (newRefreshToken) {
      cookieUtil.set('refreshToken', newRefreshToken, { expires: 14, path: '/', sameSite: 'Lax' })
    }

    return newToken
  } catch (error) {
    // 刷新token失败，清除所有token并跳转到登录页
    cookieUtil.remove('token', { path: '/' })
    cookieUtil.remove('refreshToken', { path: '/' })
    router.push('/login')
    throw error
  }
}

// 处理刷新token后的请求
const onRefreshed = (newToken) => {
  refreshSubscribers.forEach((cb) => cb(newToken))
  refreshSubscribers = []
}

// 添加请求到订阅者列表
const addRefreshSubscriber = (cb) => {
  refreshSubscribers.push(cb)
}

// 请求拦截器
let loadingInstance = null
service.interceptors.request.use(
  (config) => {
    // 显示加载动画
    if (config.showLoading !== false) {
      loadingInstance = ElLoading.service({
        lock: true,
        text: '加载中...',
        background: 'rgba(0, 0, 0, 0.7)',
      })
    }

    // 添加 token 到请求头（不需要认证的接口除外）
    const token = cookieUtil.get('token')
    // 使用方括号语法访问noAuth属性，避免类型检查错误
    if (token && !config['noAuth']) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    // 关闭加载动画
    if (loadingInstance) {
      loadingInstance.close()
    }
    return Promise.reject(error)
  },
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    // 关闭加载动画
    if (loadingInstance) {
      loadingInstance.close()
    }

    // 处理blob类型响应，直接返回完整response对象
    if (response.config.responseType === 'blob') {
      return response
    }

    const res = response.data

    // 根据后端约定的状态码进行处理
    if (res.code !== 200) {
      ElMessage({
        message: res.message || '请求失败',
        type: 'error',
        duration: 5 * 1000,
      })

      // 处理特定错误码
      if (res.code === 401) {
        const originalRequest = response.config

        // 如果不是刷新token的请求
        if (!originalRequest._retry) {
          // 标记为已重试
          originalRequest._retry = true

          // 如果正在刷新token，将请求添加到订阅者列表
          if (isRefreshing) {
            return new Promise((resolve) => {
              addRefreshSubscriber((newToken) => {
                originalRequest.headers.Authorization = `Bearer ${newToken}`
                resolve(service(originalRequest))
              })
            })
          }

          // 标记正在刷新token
          isRefreshing = true

          // 调用刷新token函数
          return refreshToken()
            .then((newToken) => {
              // 刷新成功，更新所有等待的请求
              onRefreshed(newToken)

              // 更新当前请求的token
              originalRequest.headers.Authorization = `Bearer ${newToken}`

              // 重置刷新状态
              isRefreshing = false

              // 重新发送请求
              return service(originalRequest)
            })
            .catch(() => {
              // 刷新失败，重置状态
              isRefreshing = false
              return Promise.reject(new Error(res.message || '请求失败'))
            })
        }
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    } else {
      return res
    }
  },
  (error) => {
    // 关闭加载动画
    if (loadingInstance) {
      loadingInstance.close()
    }

    let message = '请求失败'

    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401: {
          message = '未授权，请登录'

          const originalRequest = error.config

          // 如果不是刷新token的请求且没有重试过
          if (!originalRequest._retry && !originalRequest.url.includes('/refresh_token')) {
            // 标记为已重试
            originalRequest._retry = true

            // 如果正在刷新token，将请求添加到订阅者列表
            if (isRefreshing) {
              return new Promise((resolve) => {
                addRefreshSubscriber((newToken) => {
                  originalRequest.headers.Authorization = `Bearer ${newToken}`
                  resolve(service(originalRequest))
                })
              })
            }

            // 标记正在刷新token
            isRefreshing = true

            // 调用刷新token函数
            return refreshToken()
              .then((newToken) => {
                // 刷新成功，更新所有等待的请求
                onRefreshed(newToken)

                // 更新当前请求的token
                originalRequest.headers.Authorization = `Bearer ${newToken}`

                // 重置刷新状态
                isRefreshing = false

                // 重新发送请求
                return service(originalRequest)
              })
              .catch(() => {
                // 刷新失败，重置状态并跳转到登录页
                isRefreshing = false
                cookieUtil.remove('token', { path: '/' })
                cookieUtil.remove('refreshToken', { path: '/' })
                router.push('/login')
                return Promise.reject(error)
              })
          }
          break
        }
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = `请求地址出错: ${error.response.config.url}`
          break
        case 408:
          message = '请求超时'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 501:
          message = '服务未实现'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        case 505:
          message = 'HTTP版本不受支持'
          break
        default:
          message = `连接出错: ${error.response.status}`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    } else if (error.message.includes('Network Error')) {
      message = '网络连接异常，请检查网络'
    }

    // 只有当不是刷新token的请求时才显示错误消息
    if (!error.config?.url?.includes('/refresh_token')) {
      ElMessage({
        message,
        type: 'error',
        duration: 5 * 1000,
      })
    }

    return Promise.reject(error)
  },
)

export default service
export { cookieUtil }
