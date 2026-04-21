<template>
  <section class="auth-wrap">
    <div class="card auth-card">
      <h1>欢迎回来</h1>
      <p class="sub">登录后继续你的购物流程</p>
      <form class="form" @submit.prevent="handleLogin">
        <input v-model.trim="form.username" placeholder="用户名" />
        <input v-model.trim="form.password" type="password" placeholder="密码" />
        <button class="btn btn-primary" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <p class="hint">
        还没有账号？
        <RouterLink to="/register">去注册</RouterLink>
      </p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { userApi } from '../../api/modules/user'
import { useAuthStore } from '../../store/modules/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const error = ref('')
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const result = await userApi.login(form)
    authStore.setToken(result.token)
    const profile = await userApi.profile()
    authStore.setUser(profile)
    const redirectPath = route.query.redirect || '/products'
    router.push(String(redirectPath))
  } catch (e) {
    error.value = e?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>
