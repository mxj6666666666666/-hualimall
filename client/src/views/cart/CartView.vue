<template>
  <section class="container page">
    <h1>购物车</h1>
    <p class="sub">勾选商品后可直接创建订单</p>
    <p v-if="error" class="error">{{ error }}</p>
    <div v-if="loading" class="card">加载中...</div>
    <div v-else-if="items.length === 0" class="card">购物车为空</div>
    <div v-else class="cart-layout">
      <div class="card">
        <div v-for="item in items" :key="item.id" class="cart-row">
          <div class="cart-title">
            <input type="checkbox" :checked="item.selected === 1" @change="toggleSelected(item)" />
            <span>{{ item.productName }}</span>
          </div>
          <span class="sub">¥ {{ formatPrice(item.productPrice) }}</span>
          <div class="cart-actions">
            <input v-model.number="item.quantity" type="number" min="1" />
            <button class="btn btn-light" @click="updateItem(item)">更新</button>
            <button class="btn btn-danger" @click="removeItem(item.id)">删除</button>
          </div>
        </div>
      </div>
      <div class="card summary-card">
        <h3>结算</h3>
        <p class="sub">已选商品：{{ selectedCount }} 件</p>
        <p class="price">¥ {{ formatPrice(selectedAmount) }}</p>
        <button class="btn btn-primary full" :disabled="submitting || selectedCount === 0" @click="submitOrder">
          {{ submitting ? '提交中...' : '提交订单' }}
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { cartApi } from '../../api/modules/cart'
import { orderApi } from '../../api/modules/order'
import { formatPrice } from '../../utils/format'

const items = ref([])
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const router = useRouter()

const selectedItems = computed(() => items.value.filter((item) => item.selected === 1))
const selectedCount = computed(() => selectedItems.value.length)
const selectedAmount = computed(() =>
  selectedItems.value.reduce((sum, item) => sum + Number(item.productPrice) * Number(item.quantity), 0),
)

async function fetchCart() {
  loading.value = true
  error.value = ''
  try {
    items.value = await cartApi.list()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function updateItem(item) {
  if (!item.quantity || item.quantity < 1) {
    error.value = '数量必须大于0'
    return
  }
  error.value = ''
  try {
    await cartApi.update(item.id, { quantity: item.quantity, selected: item.selected })
    await fetchCart()
  } catch (e) {
    error.value = e.message
  }
}

async function toggleSelected(item) {
  error.value = ''
  try {
    await cartApi.update(item.id, {
      quantity: item.quantity,
      selected: item.selected === 1 ? 0 : 1,
    })
    await fetchCart()
  } catch (e) {
    error.value = e.message
  }
}

async function removeItem(id) {
  error.value = ''
  try {
    await cartApi.remove(id)
    await fetchCart()
  } catch (e) {
    error.value = e.message
  }
}

async function submitOrder() {
  submitting.value = true
  error.value = ''
  try {
    const order = await orderApi.create({})
    router.push(`/orders/${order.id}`)
  } catch (e) {
    error.value = e.message
  } finally {
    submitting.value = false
  }
}

onMounted(fetchCart)
</script>
