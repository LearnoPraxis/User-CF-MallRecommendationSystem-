import request from '@/utils/request'

// 获取所有分类
export function getCategoryList() {
  return request.get('/category/list')
}

// 获取分类树
export function getCategoryTree() {
  return request.get('/category/tree')
}
