export function formatPrice(value) {
  const amount = Number(value || 0)
  return amount.toFixed(2)
}

const ORDER_STATUS_TEXT = {
  0: '待支付',
  1: '已支付',
  2: '已取消',
  3: '已完成',
}

export function formatOrderStatus(status) {
  return ORDER_STATUS_TEXT[status] || '未知状态'
}

const PAYMENT_STATUS_TEXT = {
  0: '待支付',
  1: '支付中',
  2: '支付成功',
  3: '已关闭',
  4: '支付失败',
  PENDING: '待支付',
  PROCESSING: '支付中',
  SUCCESS: '支付成功',
  CLOSED: '已关闭',
  FAILED: '支付失败',
}

export function formatPaymentStatus(status) {
  return PAYMENT_STATUS_TEXT[status] || '未知状态'
}
