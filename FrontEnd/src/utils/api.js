import axios from 'axios'
import errorHandler from './errorHandler'

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 在发送请求之前做些什么
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    // 对请求错误做些什么
    return Promise.reject(error)
  },
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 对响应数据做点什么
    const res = response.data

    // 如果返回的状态码为200，说明接口请求成功，可以正常拿到数据
    // 这里可以根据实际的后端响应结构进行调整
    if (res.code === 200 || res.success) {
      return res.data || res
    } else {
      // 否则的话抛出错误
      const error = new Error(res.message || '请求失败')
      error.code = res.code
      error.response = response
      return Promise.reject(error)
    }
  },
  (error) => {
    // 对响应错误做点什么
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权，跳转到登录页
          errorHandler.handleAuthError()
          break
        case 403:
          // 权限不足
          errorHandler.showError('没有权限执行此操作')
          break
        case 404:
          // 资源不存在
          errorHandler.showError('请求的资源不存在')
          break
        case 500:
          // 服务器错误
          errorHandler.showError('服务器内部错误，请稍后再试')
          break
        default:
          // 其他错误
          errorHandler.handleApiError(error)
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      errorHandler.handleNetworkError(error)
    } else {
      // 请求配置出错
      errorHandler.showError(error.message || '请求配置错误')
    }

    return Promise.reject(error)
  },
)

// 封装GET请求
export function get(url, params = {}) {
  return api.get(url, { params })
}

// 封装POST请求
export function post(url, data = {}) {
  return api.post(url, data)
}

// 封装PUT请求
export function put(url, data = {}) {
  return api.put(url, data)
}

// 封装DELETE请求
export function del(url) {
  return api.delete(url)
}

// 封装文件上传
export function upload(url, formData, onUploadProgress) {
  return api.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress,
  })
}

// 封装文件下载
export function download(url, params = {}) {
  return api.get(url, {
    params,
    responseType: 'blob',
  })
}

export default api
