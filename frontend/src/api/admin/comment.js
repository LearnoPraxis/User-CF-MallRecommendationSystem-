import request from '@/utils/request'

// 分页查询留言
export function getCommentList(params) {
  return request.get('/admin/comment/list', { params })
}

// 获取留言详情
export function getCommentById(id) {
  return request.get(`/admin/comment/${id}`)
}

// 回复留言
export function replyComment(id, replyContent) {
  return request.post(`/admin/comment/reply/${id}`, { replyContent })
}

// 更新留言状态
export function updateCommentStatus(id, status) {
  return request.put(`/admin/comment/status/${id}`, null, { params: { status } })
}

// 删除留言
export function deleteComment(id) {
  return request.delete(`/admin/comment/${id}`)
}

// 批量删除留言
export function batchDeleteComments(ids) {
  return request.delete('/admin/comment/batch', { data: ids })
}
