import request from '@/utils/request'

// 分页查询帖子
export function getPostList(params) {
  return request.get('/forum/list', { params })
}

// 获取当前用户的帖子列表
export function getMyPosts(params) {
  return request.get('/forum/my', { params })
}

// 获取帖子详情
export function getPostDetail(id) {
  return request.get(`/forum/detail/${id}`)
}

// 发布帖子
export function createPost(data) {
  return request.post('/forum/create', data)
}

// 更新帖子
export function updatePost(data) {
  return request.put('/forum/update', data)
}

// 删除帖子
export function deletePost(id) {
  return request.delete(`/forum/${id}`)
}

// 添加评论
export function addForumComment(data) {
  return request.post('/forum/comment', data)
}

// 删除评论
export function deleteForumComment(id) {
  return request.delete(`/forum/comment/${id}`)
}
