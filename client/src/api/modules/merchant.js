import request from '../request'

export const merchantApi = {
  listProducts(params) {
    return request.get('/merchant/products', { params })
  },
  addProduct(data) {
    return request.post('/merchant/products', data)
  },
  updateProduct(id, data) {
    return request.put(`/merchant/products/${id}`, data)
  },
  deleteProduct(id) {
    return request.delete(`/merchant/products/${id}`)
  },
  categoryStats() {
    return request.get('/merchant/orders/category-stats')
  },
  listOrders(params) {
    return request.get('/orders', { params })
  },
}
