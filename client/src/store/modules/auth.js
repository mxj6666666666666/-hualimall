import { defineStore } from 'pinia'
const USER_KEY = 'hm_user'

function loadUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: loadUser(),
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.user?.id),
    role: (state) => state.user?.role || '',
    nickname: (state) => state.user?.nickname || state.user?.username || '用户',
  },
  actions: {
    setUser(user) {
      this.user = user
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    setAuth(payload) {
      if (payload.user) {
        this.setUser(payload.user)
      }
    },
    clearAuth() {
      this.user = null
      localStorage.removeItem(USER_KEY)
    },
  },
})
