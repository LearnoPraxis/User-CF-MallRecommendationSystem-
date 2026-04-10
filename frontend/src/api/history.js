import request from '@/utils/request'

// 分页查询浏览记录
export function getHistoryList(params) {
  return request.get('/history/list', { params })
}

// 删除浏览记录
export function deleteHistory(id) {
  return request.delete(`/history/${id}`)
}

// 清空浏览记录
export function clearHistory() {
  return request.delete('/history/clear')
}
