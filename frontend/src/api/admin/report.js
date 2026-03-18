import request from '@/utils/request'

// 获取订单状态统计
export function getOrderStatusStats(startTime, endTime) {
  return request.get('/admin/order/stats/status', { params: { startTime, endTime } })
}

// 获取用户统计
export function getUserStats(startTime, endTime) {
  return request.get('/admin/order/stats/user', { params: { startTime, endTime } })
}
