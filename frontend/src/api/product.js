import request from '@/utils/request'

// 分页查询商品
export function getProductList(params) {
  return request.get('/product/list', { params })
}

// 获取商品详情
export function getProductDetail(id) {
  return request.get(`/product/detail/${id}`)
}

// 获取热门商品
export function getHotProducts(limit = 10) {
  return request.get('/product/hot', { params: { limit } })
}

// 获取最新商品
export function getNewProducts(limit = 10) {
  return request.get('/product/new', { params: { limit } })
}

// 用户上架商品
export function publishProduct(data) {
  return request.post('/product/publish', data)
}

// 用户更新商品
export function updateProduct(data) {
  return request.put('/product/update', data)
}
