import request from '@/utils/request'
//打卡相关接口

export const checkInsApi = {
  // 打卡
  checkIn(data) {
    return request({
      url: `/checkIns/add`,
      method: 'post',
      data: data,
    })
  },
  // 获取打卡信息
  getCheckIn(userId, bookId, chapterId) {
    return request({
      url: `/checkIns/fetch`,
      method: 'get',
      params: {
        userId: userId,
        bookId: bookId,
        chapterId: chapterId,
      },
    })
  },
  // 检查用户是否已打卡
  checkUserCheckIn(userId, bookId, chapterId) {
    return request({
      url: `/checkIns/check`,
      method: 'get',
      params: {
        userId: userId,
        bookId: bookId,
        chapterId: chapterId,
      },
    })
  },
  // 获取用户打卡记录
  getUserCheckIns(userId) {
    return request({
      url: `/checkIns/fetch/list/user`,
      method: 'get',
      params: {
        userId: userId,
      },
    })
  },
  // 获取书籍打卡记录
  getBookCheckIns(bookId) {
    return request({
      url: `/checkIns/fetch/list/book`,
      method: 'get',
      params: {
        bookId: bookId,
      },
    })
  },
  // 获取章节打卡记录
  getChapterCheckIns(chapterId) {
    return request({
      url: `/checkIns/fetch/list/chapter`,
      method: 'get',
      params: {
        chapterId: chapterId,
      },
    })
  },
}
