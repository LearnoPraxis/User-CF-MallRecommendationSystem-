import request from '@/utils/request'

// 分页查询商品留言
export function getCommentList(productId, params) {
  return request.get(`/comment/list/${productId}`, { params })
}

// 添加留言
export function addComment(data) {
  return request.post('/comment/add', data)
}
