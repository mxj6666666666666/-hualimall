<template>
  <section class="container page">
    <h1>订单支付</h1>
    <p class="sub">请在支付时限内完成付款，支付成功后订单将自动更新</p>
    <p v-if="error" class="error">{{ error }}</p>

    <div v-if="loading" class="card">加载中...</div>
    <div v-else-if="!order" class="card">订单不存在或已删除</div>
    <div v-else class="payment-layout">
      <article class="card">
        <h3>订单信息</h3>
        <p class="sub">订单号：{{ order.orderNo }}</p>
        <p class="sub">状态：{{ formatOrderStatus(order.status) }}</p>
        <p class="price">¥ {{ formatPrice(order.totalAmount) }}</p>
        <div class="row-actions">
          <button class="btn btn-light" @click="goOrderDetail">查看订单详情</button>
        </div>
      </article>

      <article class="card" v-if="order.status === 0">
        <h3>选择支付方式</h3>
        <div class="channel-group">
          <label class="channel-item">
            <input v-model="channel" type="radio" value="ALIPAY" />
            支付宝
          </label>
          <label class="channel-item">
            <input v-model="channel" type="radio" value="WECHAT" />
            微信支付
          </label>
        </div>
        <div class="row-actions">
          <button class="btn btn-primary" :disabled="creating" @click="createPayment">
            {{ creating ? '创建中...' : '创建支付单' }}
          </button>
          <button class="btn btn-light" :disabled="refreshing" @click="refreshPayment">
            {{ refreshing ? '刷新中...' : '我已支付，刷新状态' }}
          </button>
          <button class="btn btn-danger" :disabled="closing || !payment?.id" @click="closePayment">
            {{ closing ? '关闭中...' : '关闭支付单' }}
          </button>
        </div>
      </article>

      <article class="card" v-if="payment">
        <h3>支付单信息</h3>
        <p class="sub">支付单号：{{ payment.paymentNo || payment.id || '-' }}</p>
        <p class="sub">支付状态：{{ formatPaymentStatus(payment.status) }}</p>
        <p v-if="payment.expireTime" class="sub">过期时间：{{ payment.expireTime }}</p>
        <div class="pay-link" v-if="payment.payUrl || payment.codeUrl">
          <a class="btn btn-primary" :href="payment.payUrl || payment.codeUrl" target="_blank" rel="noreferrer">
            打开收银台/二维码链接
          </a>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '../../api/modules/order'
import { paymentApi } from '../../api/modules/payment'
import { formatOrderStatus, formatPaymentStatus, formatPrice } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const payment = ref(null)
const loading = ref(false)
const creating = ref(false)
const refreshing = ref(false)
const closing = ref(false)
const error = ref('')
const channel = ref('ALIPAY')

let timer = null

function stopPolling() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function shouldPollPaymentStatus(status) {
  return status === 0 || status === 1 || status === 'PENDING' || status === 'PROCESSING'
}

async function fetchOrder() {
  order.value = await orderApi.detail(route.params.id)
}

async function fetchPaymentByOrder() {
  payment.value = await paymentApi.byOrder(route.params.id)
}

async function initPage() {
  loading.value = true
  error.value = ''
  try {
    await fetchOrder()
    try {
      await fetchPaymentByOrder()
    } catch (e) {
      payment.value = null
      const message = String(e?.message || '')
      if (!message.includes('404') && !message.includes('不存在') && !message.toLowerCase().includes('not found')) {
        throw e
      }
    }
    if (payment.value && shouldPollPaymentStatus(payment.value.status)) {
      startPolling()
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function refreshPayment() {
  refreshing.value = true
  error.value = ''
  try {
    if (payment.value?.id) {
      payment.value = await paymentApi.detail(payment.value.id)
    } else {
      await fetchPaymentByOrder()
    }
    await fetchOrder()
    if (!shouldPollPaymentStatus(payment.value?.status)) {
      stopPolling()
    }
  } catch (e) {
    error.value = e.message
  } finally {
    refreshing.value = false
  }
}

function startPolling() {
  stopPolling()
  timer = setInterval(() => {
    refreshPayment()
  }, 3000)
}

async function createPayment() {
  let cashierWindow = null
  if (channel.value === 'ALIPAY') {
    cashierWindow = window.open('', '_blank')
    if (!cashierWindow) {
      error.value = '浏览器拦截了收银台弹窗，请允许弹窗后重试'
      return
    }
  }
  creating.value = true
  error.value = ''
  try {
    const result = await paymentApi.create({
      orderId: Number(route.params.id),
      channel: channel.value,
    })
    if (result?.html) {
      cashierWindow.document.open()
      cashierWindow.document.write(result.html)
      cashierWindow.document.close()
      await fetchPaymentByOrder()
    } else {
      payment.value = result
      if (cashierWindow && !cashierWindow.closed) {
        cashierWindow.close()
      }
    }
    startPolling()
  } catch (e) {
    if (cashierWindow && !cashierWindow.closed) {
      cashierWindow.close()
    }
    error.value = e.message
  } finally {
    creating.value = false
  }
}

async function closePayment() {
  if (!payment.value?.id) {
    error.value = '当前没有可关闭的支付单'
    return
  }
  closing.value = true
  error.value = ''
  try {
    await paymentApi.close(payment.value.id)
    await refreshPayment()
  } catch (e) {
    error.value = e.message
  } finally {
    closing.value = false
  }
}

function goOrderDetail() {
  router.push(`/orders/${route.params.id}`)
}

onMounted(initPage)
onUnmounted(stopPolling)
</script>

<style scoped>
.payment-layout {
  display: grid;
  gap: 16px;
}

.price {
  margin: 12px 0;
  font-size: 24px;
  font-weight: 700;
  color: #1d4ed8;
}

.channel-group {
  display: flex;
  gap: 16px;
  margin-top: 12px;
}

.channel-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.row-actions {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.pay-link {
  margin-top: 12px;
}
</style>
