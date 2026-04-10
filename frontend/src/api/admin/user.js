import request from '@/utils/request'

// 分页查询用户
export function getUserList(params) {
  return request.get('/admin/user/list', { params })
}

// 获取用户详情
export function getUserById(id) {
  return request.get(`/admin/user/${id}`)
}

// 添加用户
export function addUser(data) {
  return request.post('/admin/user', data)
}

// 更新用户
export function updateUser(data) {
  return request.put('/admin/user', data)
}

// 更新用户状态
export function updateUserStatus(id, status) {
  return request.put(`/admin/user/status/${id}`, null, { params: { status } })
}

// 重置用户密码
export function resetPassword(id, newPassword) {
  return request.put(`/admin/user/reset-password/${id}`, { newPassword })
}

// 删除用户
export function deleteUser(id) {
  return request.delete(`/admin/user/${id}`)
}
