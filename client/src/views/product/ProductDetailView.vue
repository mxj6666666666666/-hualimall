<template>
  <section class="container page">
    <p v-if="error" class="error">{{ error }}</p>
    <article class="card detail-card" v-if="detail">
      <img :src="resolveMediaUrl(detail.imageUrl) || fallbackImage" :alt="detail.name" />
      <div class="detail-panel">
        <p class="hero-eyebrow">Atelier Detail</p>
        <h1>{{ detail.name }}</h1>
        <p class="sub">{{ detail.description || '暂无描述' }}</p>
        <p class="price">¥ {{ formatPrice(detail.price) }}</p>
        <p class="sub">库存：{{ detail.stock }}</p>
        <div class="toolbar">
          <input v-model.number="quantity" type="number" min="1" placeholder="数量" />
          <button class="btn btn-primary" @click="addToCart" :disabled="loading">
            {{ loading ? '提交中...' : '加入购物车' }}
          </button>
          <RouterLink class="btn btn-light" to="/cart">查看购物车</RouterLink>
        </div>
      </div>
    </article>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { productApi } from '../../api/modules/product'
import { cartApi } from '../../api/modules/cart'
import { useAuthStore } from '../../store/modules/auth'
import { formatPrice } from '../../utils/format'
import { resolveMediaUrl } from '../../utils/media'

const route = useRoute()
const router = useRouter()
const detail = ref(null)
const loading = ref(false)
const error = ref('')
const quantity = ref(1)
const fallbackImage = 'https://via.placeholder.com/720x420?text=HualiMall'
const authStore = useAuthStore()

onMounted(async () => {
  try {
    detail.value = await productApi.detail(route.params.id)
  } catch (e) {
    error.value = e.message
  }
})

async function addToCart() {
  if (!authStore.isLoggedIn) {
    router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
    return
  }
  if (!detail.value) return
  if (!quantity.value || quantity.value < 1) {
    error.value = '数量必须大于0'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await cartApi.add({ productId: detail.value.id, quantity: quantity.value })
    router.push('/cart')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>
