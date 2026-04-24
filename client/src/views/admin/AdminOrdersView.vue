<template>
  <section class="container page">
    <h1>管理端 · 订单履约管理</h1>
    <p class="sub">统一监控订单状态，保持高品质交付节奏</p>
    <p v-if="error" class="error">{{ error }}</p>
    <div class="card table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>订单号</th>
            <th>金额</th>
            <th>状态</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in orders" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.orderNo }}</td>
            <td>¥ {{ formatPrice(item.totalAmount) }}</td>
            <td>{{ formatOrderStatus(item.status) }}</td>
            <td>{{ item.updateTime || '-' }}</td>
            <td class="row-actions">
              <select v-model.number="statusMap[item.id]">
                <option :value="0">待支付</option>
                <option :value="1">已支付</option>
                <option :value="2">已取消</option>
                <option :value="3">已完成</option>
              </select>
              <button class="btn btn-primary" @click="updateStatus(item.id)">更新</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminApi } from '../../api/modules/admin'
import { formatOrderStatus, formatPrice } from '../../utils/format'

const orders = ref([])
const error = ref('')
const statusMap = ref({})

async function fetchOrders() {
  error.value = ''
  try {
    const data = await adminApi.listOrders({ page: 1, pageSize: 50 })
    orders.value = data?.rows || []
    const nextMap = {}
    orders.value.forEach((item) => {
      nextMap[item.id] = Number(item.status)
    })
    statusMap.value = nextMap
  } catch (e) {
    error.value = e.message
  }
}

async function updateStatus(id) {
  error.value = ''
  try {
    await adminApi.updateOrderStatus(id, Number(statusMap.value[id]))
    await fetchOrders()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(fetchOrders)
</script>
