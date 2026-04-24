<template>
  <section class="container page">
    <p v-if="error" class="error">{{ error }}</p>
    <article class="card" v-if="detail">
      <h1>订单详情</h1>
      <p class="sub">Order Couture Record</p>
      <p class="sub">订单号：{{ detail.orderNo }}</p>
      <p class="sub">金额：¥ {{ formatPrice(detail.totalAmount) }}</p>
      <p class="sub">状态：{{ formatOrderStatus(detail.status) }}</p>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>商品</th>
              <th>单价</th>
              <th>数量</th>
              <th>小计</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in detail.items || []" :key="item.id">
              <td>{{ item.productName }}</td>
              <td>¥ {{ formatPrice(item.productPrice) }}</td>
              <td>{{ item.quantity }}</td>
              <td>¥ {{ formatPrice(item.totalPrice) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { orderApi } from '../../api/modules/order'
import { formatOrderStatus, formatPrice } from '../../utils/format'

const route = useRoute()
const detail = ref(null)
const error = ref('')

onMounted(async () => {
  try {
    detail.value = await orderApi.detail(route.params.id)
  } catch (e) {
    error.value = e.message
  }
})
</script>
