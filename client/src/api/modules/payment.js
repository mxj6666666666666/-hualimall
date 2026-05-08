import request from '../request'

export const paymentApi = {
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
