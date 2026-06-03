import request from '@/utils/request'

// 阅读记录相关接口
export const readingRecordsApi = {
  // 获取用户阅读记录
  getUserReadingRecords(userId) {
    return request({
      url: `/readingRecords/fetch`,
      method: 'get',
      params: {
        userId: userId,
      },
    })
  },

  // 添加阅读记录
  addReadingRecord(data) {
    return request({
      url: '/readingRecords/add',
      method: 'post',
      data,
    })
  },

  // 删除阅读记录
  deleteReadingRecord(recordId) {
    return request({
      url: `/readingRecords/delete`,
      method: 'delete',
      params: {
        id: recordId,
      },
    })
  },
  // 删除所有阅读记录
  deleteAllReadingRecords(userId) {
    return request({
      url: `/readingRecords/delete/all`,
      method: 'delete',
      params: {
        userId: userId,
      },
    })
  },
}
