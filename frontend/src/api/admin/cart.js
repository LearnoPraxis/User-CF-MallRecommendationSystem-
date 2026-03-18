import request from '@/utils/request'

// 分页查询购物车
export function getCartList(params) {
  return request.get('/admin/cart/list', { params })
}

// 删除购物车项
export function deleteCartItem(id) {
  return request.delete(`/admin/cart/${id}`)
}

// 批量删除购物车项
export function batchDeleteCartItems(ids) {
  return request.delete('/admin/cart/batch', { data: ids })
}

// 清空用户购物车
export function clearUserCart(userId) {
  return request.delete(`/admin/cart/clear/${userId}`)
}
