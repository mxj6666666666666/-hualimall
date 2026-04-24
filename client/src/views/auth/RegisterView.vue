<template>
  <section class="auth-wrap">
    <div class="card auth-card">
      <h1>开启专属账号</h1>
      <p class="sub">轻盈注册，进入高端时尚精选空间</p>
      <form class="form" @submit.prevent="handleRegister">
        <input v-model.trim="form.username" placeholder="用户名" />
        <input v-model.trim="form.nickname" placeholder="昵称" />
        <input v-model.trim="form.password" type="password" placeholder="密码" />
        <button class="btn btn-primary" type="submit" :disabled="loading">
          {{ loading ? '提交中...' : '注册' }}
        </button>
      </form>
      <p class="hint">
        已有账号？
        <RouterLink to="/login">去登录</RouterLink>
      </p>
      <p v-if="message" class="success">{{ message }}</p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { userApi } from '../../api/modules/user'

const loading = ref(false)
const error = ref('')
const message = ref('')
const form = reactive({ username: '', nickname: '', password: '' })

async function handleRegister() {
  if (!form.username || !form.password) {
    error.value = '用户名和密码不能为空'
    return
  }
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    await userApi.register(form)
    message.value = '注册成功，请前往登录'
    form.username = ''
    form.nickname = ''
    form.password = ''
  } catch (e) {
    error.value = e?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>
