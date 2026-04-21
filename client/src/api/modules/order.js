import request from '../request'

export const orderApi = {
  create(data) {
    return request.post('/orders', data)
  },
  list(params) {
    return request.get('/orders', { params })
  },
  detail(id) {
    return request.get(`/orders/${id}`)
  },
  cancel(id) {
    return request.put(`/orders/${id}/cancel`)
  },
}
