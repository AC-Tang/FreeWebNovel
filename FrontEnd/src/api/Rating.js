import request from '@/utils/request'
// 评分相关接口

export const ratingsApi = {
  // 获取用户对书籍的评分
  getBookRatingByUserId(userId, bookId) {
    return request({
      url: `/ratings/fetch`,
      method: 'get',
      params: {
        userId: userId,
        bookId: bookId,
      },
    })
  },

  // 添加或修改评分
  addOrUpdateBookRating(data) {
    return request({
      url: `/ratings/addOrUpdate`,
      method: 'put',
      data,
    })
  },
}
