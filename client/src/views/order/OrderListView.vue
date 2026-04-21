<template>
  <section class="container page">
    <h1>我的订单</h1>
    <p v-if="error" class="error">{{ error }}</p>
    <div v-if="loading" class="card">加载中...</div>
    <div v-else-if="orders.length === 0" class="card">暂无订单</div>
    <div v-else class="card">
      <div v-for="order in orders" :key="order.id" class="list-row">
        <div>
          <p>#{{ order.orderNo }}</p>
          <p class="sub">状态：{{ formatOrderStatus(order.status) }}</p>
        </div>
        <span>¥ {{ formatPrice(order.totalAmount) }}</span>
        <div class="row-actions">
          <RouterLink class="btn btn-light" :to="`/orders/${order.id}`">详情</RouterLink>
          <button v-if="order.status === 0" class="btn btn-danger" @click="cancelOrder(order.id)">取消</button>
        </div>
      </div>
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
import { RouterLink } from 'vue-router'
import { orderApi } from '../../api/modules/order'
import { formatOrderStatus, formatPrice } from '../../utils/format'

const orders = ref([])
const loading = ref(false)
const error = ref('')
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10 })
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / query.pageSize)))

async function fetchOrders() {
  loading.value = true
  error.value = ''
  try {
    const data = await orderApi.list(query)
    orders.value = data?.rows || []
    total.value = Number(data?.total || 0)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function changePage(page) {
  query.page = page
  fetchOrders()
}

async function cancelOrder(id) {
  error.value = ''
  try {
    await orderApi.cancel(id)
    await fetchOrders()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(fetchOrders)
</script>
