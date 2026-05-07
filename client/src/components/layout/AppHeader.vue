<template>
  <header class="header">
    <div class="container header-inner">
      <RouterLink class="brand" to="/products">HualiMall</RouterLink>
      <nav class="nav">
        <RouterLink to="/products">商品</RouterLink>
        <RouterLink v-if="auth.role === 'BUYER'" to="/cart">购物车</RouterLink>
        <RouterLink v-if="auth.role === 'BUYER'" to="/orders">订单</RouterLink>
        <RouterLink v-if="auth.role === 'MERCHANT'" to="/merchant/products">我的商品</RouterLink>
        <RouterLink v-if="auth.role === 'MERCHANT'" to="/merchant/overview">销售看板</RouterLink>
        <RouterLink v-if="auth.role === 'ADMIN'" to="/admin/products">管理端</RouterLink>
        <RouterLink v-if="auth.role === 'ADMIN'" to="/admin/orders">订单管理</RouterLink>
      </nav>
      <div class="actions">
        <template v-if="auth.isLoggedIn">
          <img class="user-avatar" :src="avatarUrl" :alt="`${auth.nickname}头像`" />
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
import { computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '../../store/modules/auth'
import { resolveMediaUrl } from '../../utils/media'

const auth = useAuthStore()
const router = useRouter()
const fallbackAvatar = 'https://via.placeholder.com/72x72?text=U'
const avatarUrl = computed(() => resolveMediaUrl(auth.user?.avatarUrl) || fallbackAvatar)

function logout() {
  auth.clearAuth()
  router.push('/login')
}
</script>
