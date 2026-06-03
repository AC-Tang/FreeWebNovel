import request from '@/utils/request'

// 用户偏好相关接口
export const userPreferencesApi = {
  // 获取用户偏好
  getUserPreferences(userId) {
    return request({
      url: `/userPreferences/fetch`,
      method: 'get',
      params: {
        userId: userId,
      },
    })
  },

  // 更新用户偏好
  addOrUpdateUserPreferences(data) {
    return request({
      url: `/userPreferences/addOrUpdate`,
      method: 'put',
      data,
    })
  },
}
