<template>
  <section class="container page">
    <h1>商家端 · 销售总览</h1>
    <p class="sub">查看您名下商品类目的销售额与订单量</p>
    <p v-if="error" class="error">{{ error }}</p>

    <div class="card table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>类目ID</th>
            <th>销售额</th>
            <th>订单数</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in categoryStats" :key="item.categoryId">
            <td>{{ item.categoryId ?? '-' }}</td>
            <td>¥ {{ formatPrice(item.salesAmount) }}</td>
            <td>{{ item.orderCount }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="card table-wrap">
      <h2>关联订单</h2>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>订单号</th>
            <th>金额</th>
            <th>状态</th>
            <th>更新时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in orders" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.orderNo }}</td>
            <td>¥ {{ formatPrice(item.totalAmount) }}</td>
            <td>{{ formatOrderStatus(item.status) }}</td>
            <td>{{ item.updateTime || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { merchantApi } from '../../api/modules/merchant'
import { formatOrderStatus, formatPrice } from '../../utils/format'

const categoryStats = ref([])
const orders = ref([])
const error = ref('')

async function fetchOverview() {
  error.value = ''
  try {
    categoryStats.value = await merchantApi.categoryStats()
    const data = await merchantApi.listOrders({ page: 1, pageSize: 50 })
    orders.value = data?.rows || []
  } catch (e) {
    error.value = e.message
  }
}

onMounted(fetchOverview)
</script>
