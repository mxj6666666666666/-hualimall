import request from '../request'

export const productApi = {
  list(params) {
    return request.get('/products', { params })
  },
  detail(id) {
    return request.get(`/products/${id}`)
  },
}
