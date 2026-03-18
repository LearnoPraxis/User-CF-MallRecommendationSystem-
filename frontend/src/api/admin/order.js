import request from '@/utils/request'

// 分页查询订单
export function getOrderList(params) {
  return request.get('/admin/order/list', { params })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request.get(`/admin/order/detail/${id}`)
}

// 发货
export function deliverOrder(id) {
  return request.post(`/admin/order/deliver/${id}`)
}

// 删除订单
export function deleteOrder(id) {
  return request.delete(`/admin/order/${id}`)
}

// 修改订单备注
export function updateOrderRemark(id, remark) {
  return request.put(`/admin/order/remark/${id}`, { remark })
}

// 销售趋势统计
export function getSalesStats(startTime, endTime) {
  return request.get('/admin/order/stats/sales', { params: { startTime, endTime } })
}

// 商品销量排行
export function getProductRank(startTime, endTime, limit = 10) {
  return request.get('/admin/order/stats/product-rank', { params: { startTime, endTime, limit } })
}

// 分类销售占比
export function getCategoryRatio(startTime, endTime) {
  return request.get('/admin/order/stats/category-ratio', { params: { startTime, endTime } })
}
