import request from '@/utils/request'
// 弹幕相关接口

export const danmakuApi = {
  // 获取视频弹幕
  getVideoDanmakus(bookId, chapterId) {
    return request({
      url: `/danmakus/fetch/list`,
      method: 'get',
      params: {
        bookId: bookId,
        chapterId: chapterId,
      },
    })
  },
  // 获取所有弹幕（管理员使用）
  getAllDanmakus() {
    return request({
      url: `/danmakus/fetch/all`,
      method: 'get',
    })
  },

  // 添加视频弹幕
  addVideoDanmaku(data) {
    return request({
      url: '/danmakus/add',
      method: 'post',
      data,
    })
  },
  // 删除视频弹幕
  deleteVideoDanmaku(danmakuIds) {
    return request({
      url: `/danmakus/delete`,
      method: 'delete',
      params: {
        id: danmakuIds,
      },
    })
  },
}
