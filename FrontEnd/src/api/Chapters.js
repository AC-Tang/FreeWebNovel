import request from '@/utils/request'
// 章节相关API
export const chaptersApi = {
  // 获取所有章节
  getAllChapters() {
    return request({
      url: '/chapters',
      method: 'get',
    })
  },

  // 根据小说ID获取章节
  getChaptersByNovelId(novelId) {
    return request({
      url: `/chapters/fetch/${novelId}`,
      method: 'get',
    })
  },

  // 根据小说ID和章节序号获取章节详情
  getChapterDetail(novelId, chapterOrder) {
    return request({
      url: `/chapters/fetch/${novelId}/${chapterOrder}`,
      method: 'get',
    })
  },
}
