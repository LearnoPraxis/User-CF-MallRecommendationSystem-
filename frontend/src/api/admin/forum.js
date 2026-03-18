import request from '@/utils/request'

// 分页查询帖子
export function getPostList(params) {
  return request.get('/admin/forum/list', { params })
}

// 获取帖子详情
export function getPostById(id) {
  return request.get(`/admin/forum/${id}`)
}

// 获取帖子评论列表
export function getPostComments(id) {
  return request.get(`/admin/forum/${id}/comments`)
}

// 分页查询所有评论
export function getCommentList(params) {
  return request.get('/admin/forum/comment/list', { params })
}

// 更新帖子状态
export function updatePostStatus(id, status) {
  return request.put(`/admin/forum/status/${id}`, null, { params: { status } })
}

// 删除帖子
export function deletePost(id) {
  return request.delete(`/admin/forum/${id}`)
}

// 批量删除帖子
export function batchDeletePosts(ids) {
  return request.delete('/admin/forum/batch', { data: ids })
}

// 更新评论状态
export function updateCommentStatus(id, status) {
  return request.put(`/admin/forum/comment/status/${id}`, null, { params: { status } })
}

// 删除评论
export function deleteForumComment(id) {
  return request.delete(`/admin/forum/comment/${id}`)
}

// 批量删除评论
export function batchDeleteForumComments(ids) {
  return request.delete('/admin/forum/comment/batch', { data: ids })
}
