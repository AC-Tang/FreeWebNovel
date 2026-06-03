import request from '@/utils/request'

// 反馈相关接口
export const feedbacksApi = {
  // 添加反馈
  addFeedback(data) {
    return request({
      url: `/feedbacks/add`,
      method: 'post',
      data,
    })
  },
  // 删除反馈
  deleteFeedback(id) {
    return request({
      url: `/feedbacks/delete`,
      method: 'delete',
      params: {
        feedbackId: id,
      },
    })
  },
  // 获取反馈数量，状态：1-待处理，2-处理中，3-已处理，4-已关闭
  getFeedbackCount(status) {
    return request({
      url: `/feedbacks/count`,
      method: 'get',
      params: {
        status: status,
      },
    })
  },
  // 更新反馈
  updateFeedback(data) {
    return request({
      url: `/feedbacks/update`,
      method: 'put',
      data,
    })
  },
  // 获取所有反馈
  getAllFeedbacks() {
    return request({
      url: `/feedbacks/all`,
      method: 'get',
    })
  },
}
