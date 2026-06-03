import request from '@/utils/request'
import { cookieUtil } from '@/utils/request'

// 用户相关接口
export const userApi = {
  // 用户登录
  login(data) {
    return request({
      url: '/user/login',
      method: 'post',
      data,
      showLoading: false,
      noAuth: true, // 标记不需要认证
    })
  },

  // 用户注册
  register(data) {
    return request({
      url: '/user/register',
      method: 'post',
      data,
      showLoading: false,
      noAuth: true, // 标记不需要认证
    })
  },

  // 获取图形验证码
  getCaptcha() {
    return request({
      url: '/captcha',
      method: 'get',
      responseType: 'blob', // 响应类型为二进制数据
      showLoading: false,
      noAuth: true, // 标记不需要认证
    })
  },

  // 验证图形验证码
  verifyCaptcha(data) {
    return request({
      url: '/verify',
      method: 'post',
      data,
      showLoading: false,
      noAuth: true, // 标记不需要认证
    })
  },

  // 获取邮箱验证码
  getEmailCaptcha(data) {
    return request({
      url: '/send-email',
      method: 'post',
      data,
      showLoading: false,
      noAuth: true, // 标记不需要认证
    })
  },

  // 验证邮箱验证码
  verifyEmailCaptcha(data) {
    return request({
      url: '/verify-email',
      method: 'post',
      data,
      showLoading: false,
      noAuth: true, // 标记不需要认证
    })
  },

  // 获取用户信息
  getUserInfo() {
    return request({
      url: '/user/info',
      method: 'get',
    })
  },

  // 更新用户信息
  updateUserInfo(data) {
    return request({
      url: '/user/update/info',
      method: 'put',
      data,
    })
  },

  // 修改密码
  changePassword(data) {
    return request({
      url: '/user/update/password',
      method: 'put',
      data,
    })
  },

  // 更改用户头像 - 使用multipart/form-data格式上传
  changeAvatar(file, userId) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('userId', userId)

    return request({
      url: '/user/update/avatar',
      method: 'put',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  // 更改用户邮箱
  changeEmail(data) {
    return request({
      url: '/user/update/email',
      method: 'put',
      data,
    })
  },
  // 更改用户角色
  changeRole(roleName, userId) {
    return request({
      url: '/user/update/role',
      method: 'put',
      data: {
        role: roleName,
        userId: userId,
      },
    })
  },
  // 更改用户状态
  changeStatus(userId, status) {
    return request({
      url: '/user/update/status',
      method: 'put',
      data: {
        userId: userId,
        status: status,
      },
    })
  },

  // 重置用户密码
  resetPassword(userId) {
    return request({
      url: '/user/reset/password',
      method: 'put',
      data: {
        userId: userId,
      },
    })
  },

  // 退出登录
  logout() {
    const token = cookieUtil.get('token')

    return request({
      url: '/user/logout',
      method: 'post',
      // 确保token被传递给后端
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
  },
  // 获取用户数量
  getUserCount() {
    return request({
      url: `/user/count`,
      method: 'get',
    })
  },
  // 获取所有用户信息
  getAllUsers() {
    return request({
      url: `/user/all`,
      method: 'get',
    })
  },
  deleteUser(userId) {
    return request({
      url: `/user/delete`,
      method: 'delete',
      params: {
        userId: userId,
      },
    })
  },
}
