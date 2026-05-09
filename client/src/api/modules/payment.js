import request from '../request'
import axios from 'axios'
import { useAuthStore } from '../../store/modules/auth'

export const paymentApi = {
  async create(data) {
    if (String(data?.channel || '').toUpperCase() === 'ALIPAY') {
      const authStore = useAuthStore()
      const response = await axios.post('/payments', data, {
        baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
        timeout: 10000,
        responseType: 'text',
        headers: authStore.token ? { Authorization: `Bearer ${authStore.token}` } : undefined,
      })
      const contentType = String(response.headers?.['content-type'] || '').toLowerCase()
      if (contentType.includes('text/html') || String(response.data || '').trim().startsWith('<')) {
        return { html: response.data }
      }
      const payload = typeof response.data === 'string' ? JSON.parse(response.data) : response.data
      if (payload?.code === 200) {
        return payload.data
      }
      throw new Error(payload?.message || '请求失败')
    }
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
