import request from '../request'

export const paymentApi = {
  // 【修改】createPayment 统一走 JSON 业务返回，不再解析后端 HTML
  create(data) {
    return request.post('/payments', data)
  },
  detail(id) {
    return request.get(`/payments/${id}`)
  },
  byOrder(orderId) {
    return request.get(`/payments/order/${orderId}`)
  },
  close(id) {
    return request.put(`/payments/${id}/close`)
  },
}
