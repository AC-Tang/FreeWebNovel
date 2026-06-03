import request from '@/utils/request'

//书籍请求相关接口
export const bookRequestApi = {
  // 添加书籍请求
  addBookRequest(data) {
    return request({
      url: '/bookRequests/add',
      method: 'post',
      data,
    })
  },
  // 删除书籍请求
  removeBookRequest(requestId) {
    return request({
      url: `/bookRequests/remove/${requestId}`,
      method: 'delete',
    })
  },
  // 更新书籍请求
  updateBookRequest(data) {
    return request({
      url: '/bookRequests/update',
      method: 'put',
      data,
    })
  },
  // 获取书籍请求数量，状态: 1-待处理, 2-已添加, 3-已拒绝
  getBookRequestCount(status) {
    return request({
      url: '/bookRequests/count',
      method: 'get',
      params: {
        status,
      },
    })
  },
  // 获取所有书籍请求
  getAllBookRequests() {
    return request({
      url: '/bookRequests/all',
      method: 'get',
    })
  },
}
