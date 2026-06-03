import request from '@/utils/request'

//分类相关API
export const categoryApi = {
  // 获取分类列表
  getCategoryList() {
    return request({
      url: '/category/fetch/all',
      method: 'get',
    })
  },
  // 添加分类
  addCategory(data) {
    return request({
      url: '/category/add',
      method: 'post',
      data,
    })
  },
  // 删除分类
  deleteCategory(categoryId) {
    return request({
      url: `/category/delete`,
      method: 'delete',
      params: {
        categoryId: categoryId,
      },
    })
  },
}
