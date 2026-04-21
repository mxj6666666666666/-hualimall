import axios from 'axios'
import router from '../router'
import { useAuthStore } from '../store/modules/auth'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload?.code === 200) {
      return payload.data
    }
    return Promise.reject(new Error(payload?.message || '请求失败'))
  },
  (error) => {
    const authStore = useAuthStore()
    if (error.response?.status === 401) {
      authStore.clearAuth()
      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    }
    const serverMessage = error.response?.data?.message
    const message = serverMessage || error.message || '网络异常，请稍后重试'
    return Promise.reject(new Error(message))
  },
)

export default request
