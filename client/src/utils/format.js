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
