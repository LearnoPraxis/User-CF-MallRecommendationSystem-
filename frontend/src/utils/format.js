/**
 * 格式化工具函数
 */
import dayjs from 'dayjs'

/**
 * 格式化时间
 * @param {string|Date} time - 时间
 * @param {string} format - 格式，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(time, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!time) return ''
  return dayjs(time).format(format)
}

/**
 * 格式化日期（不含时间）
 * @param {string|Date} time - 时间
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(time) {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD')
}

/**
 * 格式化价格
 * @param {number} price - 价格
 * @returns {string} 格式化后的价格字符串
 */
export function formatPrice(price) {
  if (price === null || price === undefined) return '0.00'
  return Number(price).toFixed(2)
}

/**
 * 订单状态映射
 */
export const orderStatusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '待发货', type: 'info' },
  2: { text: '待收货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'error' }
}

/**
 * 帖子类型映射
 */
export const postTypeMap = {
  0: { text: '普通讨论', type: 'default' },
  1: { text: '志愿者招募', type: 'success' },
  2: { text: '产品分享', type: 'info' }
}
