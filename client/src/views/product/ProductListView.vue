<template>
  <section class="container page">
    <div class="hero-stage">
      <p class="hero-eyebrow">Huali Signature Selection</p>
      <h1 class="hero-title">探索高端时尚单品</h1>
      <p class="hero-sub">在奶油色留白与暖色聚光中，探索更具质感的服饰与风格。</p>
    </div>

    <div class="card">
      <h3>甄选筛选</h3>
      <p class="sub">按关键词、分类与状态快速聚焦心仪单品</p>
      <div class="toolbar">
        <input v-model.trim="query.keyword" placeholder="搜索商品名称/描述" @keyup.enter="search" />
        <input v-model.number="query.categoryId" type="number" min="1" placeholder="分类ID（可选）" />
        <select v-model="query.status">
          <option value="">全部状态</option>
          <option :value="1">上架</option>
          <option :value="0">下架</option>
        </select>
        <button class="btn btn-primary" @click="search">搜索</button>
      </div>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <div v-if="loading" class="card">加载中...</div>
    <div v-else-if="products.length === 0" class="card">暂无商品</div>
    <div v-else class="grid">
      <article v-for="item in products" :key="item.id" class="card product-card">
        <img :src="item.imageUrl || fallbackImage" :alt="item.name" />
        <h3>{{ item.name }}</h3>
        <p class="sub clamp-2">{{ item.description || '暂无描述' }}</p>
        <p class="price">¥ {{ formatPrice(item.price) }}</p>
        <div class="row-actions">
          <RouterLink class="btn btn-light" :to="`/products/${item.id}`">查看详情</RouterLink>
          <button class="btn btn-primary" @click="addToCart(item.id)">加入购物车</button>
        </div>
      </article>
    </div>
    <div class="pager">
      <button class="btn btn-light" :disabled="query.page <= 1 || loading" @click="changePage(query.page - 1)">上一页</button>
      <span>第 {{ query.page }} / {{ totalPages }} 页（共 {{ total }} 条）</span>
      <button class="btn btn-light" :disabled="query.page >= totalPages || loading" @click="changePage(query.page + 1)">下一页</button>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { productApi } from '../../api/modules/product'
import { cartApi } from '../../api/modules/cart'
import { useAuthStore } from '../../store/modules/auth'
import { formatPrice } from '../../utils/format'

const fallbackImage = 'https://via.placeholder.com/360x220?text=HualiMall'
const products = ref([])
const total = ref(0)
const loading = ref(false)
const error = ref('')
const router = useRouter()
const authStore = useAuthStore()
const query = reactive({ page: 1, pageSize: 12, keyword: '', categoryId: '', status: '' })
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / query.pageSize)))

async function fetchProducts() {
  loading.value = true
  error.value = ''
  try {
    const params = {
      page: query.page,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      status: query.status === '' ? undefined : query.status,
    }
    const data = await productApi.list(params)
    products.value = data?.rows || []
    total.value = Number(data?.total || 0)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  fetchProducts()
}

function changePage(page) {
  query.page = page
  fetchProducts()
}

async function addToCart(productId) {
  if (!authStore.isLoggedIn) {
    router.push(`/login?redirect=${encodeURIComponent('/products')}`)
    return
  }
  try {
    await cartApi.add({ productId, quantity: 1 })
    error.value = ''
  } catch (e) {
    error.value = e.message
  }
}

onMounted(fetchProducts)
</script>
