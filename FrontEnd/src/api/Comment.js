import request from '@/utils/request'

// 评论相关API
export const commentsApi = {
  // 获取评论详情
  getCommentDetail(id) {
    return request({
      url: `/comments/fetch/detail`,
      method: 'get',
      params: {
        id: id,
      },
    })
  },
  // 获取用户所有评论
  getUserComments(userId) {
    return request({
      url: `/comments/fetch/user/${userId}`,
      method: 'get',
    })
  },

  // 获取用户指定类型的评论,1:章评，2：段评，3：书评
  getUserCommentsByType(userId, type) {
    return request({
      url: `/comments/type`,
      method: 'get',
      params: {
        userId: userId,
        type: type,
      },
    })
  },
  // 获取书籍的最热评论
  getHottestCommentsByBookId(novelId) {
    return request({
      url: `/comments/fetch/hot/novel/${novelId}`,
      method: 'get',
    })
  },

  // 获取书评
  getCommentsByBookId(bookId) {
    return request({
      url: `/comments/fetch/novel/${bookId}`,
      method: 'get',
    })
  },
  // 获取章节评论
  getCommentsByChapterId(bookId, chapterId) {
    return request({
      url: `/comments/fetch/chapter`,
      method: 'get',
      params: {
        novelId: bookId,
        chapterId: chapterId,
      },
    })
  },
  // 获取段落评论
  getCommentsByParagraphId(bookId, chapterId, paragraphs) {
    return request({
      url: `/comments/fetch/paragraph`,
      method: 'get',
      params: {
        novelId: bookId,
        chapterId: chapterId,
        paragraphs: paragraphs, //章节段落数
      },
    })
  },

  // 发布评论
  publishComment(commentData) {
    return request({
      url: '/comments/publish',
      method: 'post',
      data: commentData,
    })
  },

  // 删除评论
  deleteComment(id) {
    return request({
      url: `/comments/delete`,
      method: 'delete',
      params: {
        id: id,
      },
    })
  },
  // 获取评论数量
  getCommentCount() {
    return request({
      url: `/comments/count`,
      method: 'get',
    })
  },
}
