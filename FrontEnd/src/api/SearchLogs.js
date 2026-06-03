import request from '@/utils/request'

// 搜索记录相关接口
export const searchLogsApi = {
  // 获取所有搜索记录
  getAllSearchLogs() {
    return request({
      url: `/searchLogs/fetch/all`,
      method: 'get',
    })
  },
  // 获取用户搜索记录
  getUserSearchLogs(userId) {
    return request({
      url: `/searchLogs/fetch/user`,
      method: 'get',
      params: {
        userId: userId,
      },
    })
  },

  // 添加搜索记录
  addSearchLog(data) {
    return request({
      url: '/searchLogs/add',
      method: 'post',
      data,
    })
  },

  // 删除指定搜索记录
  deleteSearchLog(searchLogIds) {
    return request({
      url: `/searchLogs/delete`,
      method: 'delete',
      params: {
        searchLogIds: searchLogIds,
      },
    })
  },

  // 删除用户所有搜索记录
  deleteAllSearchLogs(userId) {
    return request({
      url: `/searchLogs/delete/user`,
      method: 'delete',
      params: {
        userId: userId,
      },
    })
  },
}
