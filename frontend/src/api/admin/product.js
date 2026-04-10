import request from '@/utils/request'

// 分页查询商品
export function getProductList(params) {
  return request.get('/admin/product/list', { params })
}

// 添加商品
export function addProduct(data) {
  return request.post('/admin/product/add', data)
}

// 更新商品
export function updateProduct(data) {
  return request.put('/admin/product/update', data)
}

// 删除商品
export function deleteProduct(id) {
  return request.delete(`/admin/product/${id}`)
}

// 更新商品状态
export function updateProductStatus(id, status) {
  return request.put(`/admin/product/status/${id}`, null, { params: { status } })
}

// 批量删除商品
export function batchDeleteProduct(ids) {
  return request.delete('/admin/product/batch', { data: ids })
}

// 批量上架商品
export function batchOnShelf(ids) {
  return request.put('/admin/product/batch/on', ids)
}

// 批量下架商品
export function batchOffShelf(ids) {
  return request.put('/admin/product/batch/off', ids)
}

// 获取所有分类（含禁用）
export function getAdminCategoryList() {
  return request.get('/admin/product/category/list')
}

// 添加分类
export function addCategory(data) {
  return request.post('/admin/product/category/add', data)
}

// 更新分类
export function updateCategory(data) {
  return request.put('/admin/product/category/update', data)
}

// 删除分类
export function deleteCategory(id) {
  return request.delete(`/admin/product/category/${id}`)
}

// 更新分类状态
export function updateCategoryStatus(id, status) {
  return request.put(`/admin/product/category/status/${id}`, null, { params: { status } })
}
