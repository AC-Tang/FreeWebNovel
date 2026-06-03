import request from '@/utils/request'

// 爬取相关API
export const crawlApi = {
  // 批量爬取小说（根据ID区间）
  batchFetchNovels(startId, endId) {
    return request({
      url: '/novels/crawl/batch',
      method: 'post',
      params: {
        startId: startId,
        endId: endId,
      },
      timeout: 360000,
    })
  },

  // 单个小说爬取
  fetchNovel(novelId) {
    return request({
      url: `/novels/crawl/${novelId}`,
      method: 'post',
      timeout: 60000,
    })
  },

  // 更新小说章节
  updateNovelChapters(novelId) {
    return request({
      url: `/novels/update/${novelId}/chapters`,
      method: 'put',
      timeout: 60000,
    })
  },

  updateNovelsChapters(startId, endId) {
    return request({
      url: `/novels/update/batch`,
      method: 'put',
      params: {
        startId: startId,
        endId: endId,
      },
      timeout: 360000,
    })
  },

  // 刷新小说信息和章节
  refreshNovel(novelId) {
    return request({
      url: `/novels/refresh/${novelId}`,
      method: 'put',
      timeout: 60000,
    })
  },

  // 批量刷新小说信息和章节
  refreshNovels(startId, endId) {
    return request({
      url: `/novels/refresh/batch`,
      method: 'put',
      params: {
        startId: startId,
        endId: endId,
      },
      timeout: 360000,
    })
  },

  // 更新小说基本信息
  updateNovelInfo(novelId) {
    return request({
      url: `/novels/update/info/${novelId}`,
      method: 'put',
      timeout: 60000,
    })
  },

  // 根据小说名称搜索小说
  searchNovels(keyword) {
    return request({
      url: '/novels/crawl/search',
      method: 'get',
      params: { keyword: keyword },
      timeout: 60000,
    })
  },
}
