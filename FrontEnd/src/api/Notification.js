import request from '@/utils/request'

// 用户通知相关接口

export const notificationApi = {
  // 获取用户通知
  getUserNotifications(userId) {
    return request({
      url: `/notifications/fetch/${userId}`,
      method: 'get',
    })
  },

  // 获取用户指定类型的通知,1:系统通知，2：互动通知，3：请求通知
  getUserNotificationsByType(userId, type) {
    return request({
      url: `/notifications/fetch`,
      method: 'get',
      params: {
        userId: userId,
        type: type,
      },
    })
  },

  // 获取未读通知数量
  getUnreadNotificationCount(userId) {
    return request({
      url: `/notifications/fetch/unread/count/${userId}`,
      method: 'get',
    })
  },

  // 标记通知为已读
  markNotificationAsRead(notificationId) {
    return request({
      url: `/notifications/markAsRead`,
      method: 'put',
      params: {
        notificationId: notificationId,
      },
    })
  },
  // 批量标记通知为已读
  batchMarkNotificationsAsRead(notificationIds) {
    return request({
      url: `/notifications/batchMarkAsRead`,
      method: 'put',
      params: {
        notificationIds: notificationIds,
      },
    })
  },
  // 标记所有通知为已读
  batchMarkNotificationsAsReadByType(userId, type) {
    return request({
      url: `/notifications/batchMarkAsRead`,
      method: 'put',
      params: {
        userId: userId,
        type: type,
      },
    })
  },

  // 删除通知
  deleteNotification(notificationId) {
    return request({
      url: `/notifications/delete`,
      method: 'delete',
      params: {
        notificationId: notificationId,
      },
    })
  },

  // 删除所有通知
  deleteAllNotificationsByType(userId, type) {
    return request({
      url: `/notifications/deleteAll`,
      method: 'delete',
      params: {
        userId: userId,
        type: type,
      },
    })
  },
  // 批量删除通知
  batchDeleteNotifications(notificationIds) {
    return request({
      url: `/notifications/batchDelete`,
      method: 'delete',
      params: {
        notificationIds: notificationIds,
      },
    })
  },

  // 添加通知
  addNotification(data) {
    return request({
      url: '/notifications/add',
      method: 'post',
      data,
    })
  },
}
