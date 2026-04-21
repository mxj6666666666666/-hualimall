<template>
  <header class="header">
    <div class="container header-inner">
      <RouterLink class="brand" to="/products">HualiMall</RouterLink>
      <nav class="nav">
        <RouterLink to="/products">商品</RouterLink>
        <RouterLink to="/cart">购物车</RouterLink>
        <RouterLink to="/orders">订单</RouterLink>
        <RouterLink v-if="auth.role === 'ADMIN'" to="/admin/products">管理端</RouterLink>
        <RouterLink v-if="auth.role === 'ADMIN'" to="/admin/orders">订单管理</RouterLink>
      </nav>
      <div class="actions">
        <template v-if="auth.isLoggedIn">
          <span class="user">{{ auth.nickname }}</span>
          <button class="btn btn-light" @click="logout">退出</button>
        </template>
        <template v-else>
          <RouterLink class="btn btn-light" to="/login">登录</RouterLink>
          <RouterLink class="btn btn-primary" to="/register">注册</RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../../store/modules/auth'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.clearAuth()
  router.push('/login')
}
</script>
