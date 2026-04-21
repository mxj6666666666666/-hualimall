import request from '../request'

export const adminApi = {
  listProducts(params) {
    return request.get('/products', { params })
  },
  addProduct(data) {
    return request.post('/admin/products', data)
  },
  addProductsBatch(products) {
    return request.post('/admin/products/batch', products)
  },
  updateProduct(id, data) {
    return request.put(`/admin/products/${id}`, data)
  },
  updateProductsBatch(products) {
    return request.put('/admin/products/batch', products)
  },
  deleteProduct(id) {
    return request.delete(`/admin/products/${id}`)
  },
  deleteProductsBatch(ids) {
    return request.delete('/admin/products/batch', { data: ids })
  },
  listOrders(params) {
    return request.get('/orders', { params })
  },
  updateOrderStatus(id, status) {
    return request.put(`/admin/orders/${id}/status`, { status })
  },
}
