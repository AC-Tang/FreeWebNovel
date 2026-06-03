import request from '@/utils/request'

// 排行榜相关接口
export const rankingApi = {
  // 获取所有排行榜
  getAllRankingList: () =>
    request({
      url: '/rankings/all',
      method: 'get',
    }),
}
