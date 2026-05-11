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
          <button class="btn btn-danger" :disabled="cancelling" @click="cancelOrder">
            {{ cancelling ? '取消中...' : '取消订单' }}
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
import { useToast } from '../../composables/useToast'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const payment = ref(null)
const loading = ref(false)
const creating = ref(false)
const refreshing = ref(false)
const cancelling = ref(false)
const error = ref('')
const channel = ref('ALIPAY')
const PAYING_ORDER_ID_KEY = 'hm_paying_order_id'
const { showToast } = useToast()

let timer = null
let redirectedAfterPaid = false
let refreshingInFlight = false

function stopPolling() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function shouldPollPaymentStatus(status) {
  return status === 0 || status === 1 || status === 'PENDING' || status === 'PROCESSING'
}

function isOrderPaid(status) {
  return Number(status) === 1 || Number(status) === 3
}

function redirectToPaidOrder() {
  if (redirectedAfterPaid) {
    return
  }
  redirectedAfterPaid = true
  stopPolling()
  localStorage.removeItem(PAYING_ORDER_ID_KEY)
  router.replace(`/orders/${route.params.id}?paid=1`)
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
    if (isOrderPaid(order.value?.status)) {
      redirectToPaidOrder()
      return
    }
    try {
      await fetchPaymentByOrder()
    } catch (e) {
      payment.value = null
      const message = String(e?.message || '')
      if (
        !message.includes('404') &&
        !message.includes('不存在') &&
        !message.includes('暂无支付记录') &&
        !message.toLowerCase().includes('not found')
      ) {
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

async function refreshPayment({ silent = false } = {}) {
  if (refreshingInFlight) return
  refreshingInFlight = true
  if (!silent) {
    refreshing.value = true
    error.value = ''
  }
  try {
    if (payment.value?.id) {
      payment.value = await paymentApi.detail(payment.value.id)
    } else {
      await fetchPaymentByOrder()
    }
    await fetchOrder()
    if (isOrderPaid(order.value?.status)) {
      redirectToPaidOrder()
      return
    }
    if (!shouldPollPaymentStatus(payment.value?.status)) {
      stopPolling()
    }
  } catch (e) {
    if (!silent) {
      error.value = e.message
    }
  } finally {
    refreshingInFlight = false
    if (!silent) {
      refreshing.value = false
    }
  }
}

function startPolling() {
  stopPolling()
  timer = setInterval(() => {
    refreshPayment({ silent: true })
  }, 3000)
}

// 【新增】将后端返回的支付入口地址转换为可直接 window.open 的完整地址
function resolvePayUrl(rawUrl) {
  if (!rawUrl) return ''
  if (/^[a-z][a-z\d+\-.]*:\/\//i.test(rawUrl)) return rawUrl
  const base = import.meta.env.VITE_API_BASE_URL || '/api'
  if (rawUrl.startsWith(base)) return rawUrl
  if (rawUrl.startsWith('/')) return `${base}${rawUrl}`
  return `${base}/${rawUrl}`
}

// 【新增】创建支付单后先确认数据库中可查询到支付单，避免事务提交时序导致 500
async function ensurePaymentReady() {
  let lastError = null
  for (let i = 0; i < 3; i += 1) {
    try {
      await fetchPaymentByOrder()
      if (payment.value?.paymentNo || payment.value?.id) {
        return
      }
    } catch (e) {
      lastError = e
    }
    await new Promise((resolve) => setTimeout(resolve, 120))
  }
  throw lastError || new Error('支付单尚未创建完成，请稍后重试')
}

async function createPayment() {
  const cashierWindow = window.open('', '_blank')
  if (!cashierWindow) {
    error.value = '浏览器拦截了收银台弹窗，请允许弹窗后重试'
    return
  }
  let cashierOpened = false
  localStorage.setItem(PAYING_ORDER_ID_KEY, String(route.params.id))
  creating.value = true
  error.value = ''
  try {
    const result = await paymentApi.create({
      orderId: Number(route.params.id),
      channel: channel.value,
    })
    // 【修改】先接收创建结果，再确认支付单可查询（避免事务时序导致首次跳转失败）
    payment.value = result || null
    await ensurePaymentReady()
    const payUrl = resolvePayUrl(payment.value?.payUrl || payment.value?.codeUrl || result?.payUrl || result?.codeUrl)
    // 【修改】payUrl 必须显式校验，杜绝 /pay/alipay/null
    if (!payUrl) {
      throw new Error('支付链接生成失败')
    }
    // 【修改】延迟保护，避免极端情况下事务提交延迟影响跳转
    await new Promise((resolve) => setTimeout(resolve, 200))
    if (payUrl) {
      cashierWindow.location.href = payUrl
      cashierOpened = true
    }
    startPolling()
  } catch (e) {
    if (!cashierOpened && cashierWindow && !cashierWindow.closed) {
      cashierWindow.close()
    }
    error.value = e.message
  } finally {
    creating.value = false
  }
}

async function cancelOrder() {
  cancelling.value = true
  error.value = ''
  try {
    await orderApi.cancel(route.params.id)
    stopPolling()
    localStorage.removeItem(PAYING_ORDER_ID_KEY)
    showToast('已取消订单', { duration: 1000, placement: 'top' })
    router.replace(`/orders/${route.params.id}`)
  } catch (e) {
    error.value = e.message
  } finally {
    cancelling.value = false
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
