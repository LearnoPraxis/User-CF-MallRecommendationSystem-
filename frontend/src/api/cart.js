import request from '@/utils/request'

// 获取购物车列表
export function getCartList() {
  return request.get('/cart/list')
}

// 添加商品到购物车
export function addToCart(data) {
  return request.post('/cart/add', data)
}

// 更新购物车数量
export function updateCartQuantity(id, quantity) {
  return request.put(`/cart/quantity/${id}`, null, { params: { quantity } })
}

// 更新选中状态
export function updateCartSelected(id, selected) {
  return request.put(`/cart/selected/${id}`, null, { params: { selected } })
}

// 全选/取消全选
export function selectAllCart(selected) {
  return request.put('/cart/selectAll', null, { params: { selected } })
}

// 删除购物车项
export function deleteCartItem(id) {
  return request.delete(`/cart/${id}`)
}

// 清空购物车
export function clearCart() {
  return request.delete('/cart/clear')
}
