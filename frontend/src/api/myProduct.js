import request from '@/utils/request'

// 获取我的商品列表
export function getMyProducts(params) {
  return request.get('/product/my', { params })
}

// 发布商品
export function publishProduct(data) {
  return request.post('/product/publish', data)
}

// 更新我的商品
export function updateMyProduct(data) {
  return request.put('/product/my/update', data)
}

// 删除我的商品
export function deleteMyProduct(id) {
  return request.delete(`/product/my/${id}`)
}

// 上架/下架我的商品
export function updateMyProductStatus(id, status) {
  return request.put(`/product/my/${id}/status`, null, { params: { status } })
}
