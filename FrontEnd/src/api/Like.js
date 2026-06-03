import request from '@/utils/request'
// 点赞相关API
export const likesApi = {
  // 点赞
  like(data) {
    return request({
      url: `/like/add`,
      method: 'post',
      data,
    })
  },

  // 取消点赞
  cancelLike(userId, targetId) {
    return request({
      url: `/like/remove`,
      method: 'delete',
      params: {
        userId: userId,
        targetId: targetId,
      },
    })
  },
  // 检查用户是否点赞了指定的评论或书籍,1-评论, 2-弹幕打卡
  existLike(id, userId, type) {
    return request({
      url: `/like/exist`,
      method: 'get',
      params: {
        id: id,
        userId: userId,
        typeId: type,
      },
    })
  },
  // 获取用户点赞数
  getLikeCount(userId) {
    return request({
      url: `/like/count/user`,
      method: 'get',
      params: {
        userId: userId,
      },
    })
  },
}
