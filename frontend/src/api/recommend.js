import request from '@/utils/request'

// 猜你喜欢（基于协同过滤算法）
export function getPersonalizedRecommend(limit = 10) {
  return request.get('/recommend/personalized', { params: { limit } })
}

// 热门推荐
export function getHotRecommend(limit = 10) {
  return request.get('/recommend/hot', { params: { limit } })
}

// 根据浏览记录推荐
export function getHistoryRecommend(limit = 10) {
  return request.get('/recommend/history', { params: { limit } })
}
