import request from '@/utils/request'

// 数据相关接口
export const dataApi = {
  // 获取数据列表
  getDataList(params) {
    return request({
      url: '/data/list',
      method: 'get',
      params
    })
  },
  
  // 获取数据详情
  getDataDetail(id) {
    return request({
      url: `/data/${id}`,
      method: 'get'
    })
  },
  
  // 创建数据
  createData(data) {
    return request({
      url: '/data',
      method: 'post',
      data
    })
  },
  
  // 更新数据
  updateData(id, data) {
    return request({
      url: `/data/${id}`,
      method: 'put',
      data
    })
  },
  
  // 删除数据
  deleteData(id) {
    return request({
      url: `/data/${id}`,
      method: 'delete'
    })
  }
}