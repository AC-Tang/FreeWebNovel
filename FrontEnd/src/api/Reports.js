import request from '@/utils/request'

// 举报相关接口
export const reportsApi = {
  // 获取所有举报
  getAllReportsByStatus(status) {
    return request({
      url: `/reports/fetch/all`,
      method: 'get',
      params: {
        status: status,
      },
    })
  },

  // 删除举报
  deleteReport(id) {
    return request({
      url: `/reports/delete`,
      method: 'delete',
      params: {
        id: id,
      },
    })
  },

  // 创建举报
  createReport(data) {
    return request({
      url: `/reports/create`,
      method: 'post',
      data: data,
    })
  },
  // 获取举报数量，状态：1-待处理，2-处理中，3-已处理，4-已驳回
  getReportCount(status) {
    return request({
      url: `/reports/count`,
      method: 'get',
      params: {
        status: status,
      },
    })
  },

  // 更新举报
  updateReport(data) {
    return request({
      url: `/reports/update`,
      method: 'put',
      data: data,
    })
  },

  // 获取所有举报
  getAllReports() {
    return request({
      url: `/reports/all`,
      method: 'get',
    })
  },
}
