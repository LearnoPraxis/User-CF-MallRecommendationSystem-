import request from '@/utils/request'

// 分页查询收藏列表
export function getFavoriteList(params) {
  return request.get('/favorite/list', { params })
}

// 添加收藏
export function addFavorite(productId) {
  return request.post(`/favorite/${productId}`)
}

// 取消收藏
export function removeFavorite(productId) {
  return request.delete(`/favorite/${productId}`)
}

// 检查是否已收藏
export function checkFavorite(productId) {
  return request.get(`/favorite/check/${productId}`)
}
