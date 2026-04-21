import request from '../request'

export const cartApi = {
  list() {
    return request.get('/carts')
  },
  add(data) {
    return request.post('/carts', data)
  },
  update(id, data) {
    return request.put(`/carts/${id}`, data)
  },
  remove(id) {
    return request.delete(`/carts/${id}`)
  },
}
