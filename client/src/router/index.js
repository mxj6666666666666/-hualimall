import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/modules/auth'

const routes = [
  { path: '/', redirect: '/products' },
  { path: '/login', component: () => import('../views/auth/LoginView.vue'), meta: { guestOnly: true } },
  { path: '/register', component: () => import('../views/auth/RegisterView.vue'), meta: { guestOnly: true } },
  { path: '/products', component: () => import('../views/product/ProductListView.vue') },
  { path: '/products/:id', component: () => import('../views/product/ProductDetailView.vue') },
  { path: '/cart', component: () => import('../views/cart/CartView.vue'), meta: { requiresAuth: true } },
  { path: '/orders', component: () => import('../views/order/OrderListView.vue'), meta: { requiresAuth: true } },
  { path: '/orders/:id', component: () => import('../views/order/OrderDetailView.vue'), meta: { requiresAuth: true } },
  { path: '/admin/products', component: () => import('../views/admin/AdminProductsView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/orders', component: () => import('../views/admin/AdminOrdersView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/merchant/products', component: () => import('../views/merchant/MerchantProductsView.vue'), meta: { requiresAuth: true, requiresMerchant: true } },
  { path: '/merchant/overview', component: () => import('../views/merchant/MerchantOverviewView.vue'), meta: { requiresAuth: true, requiresMerchant: true } },
  { path: '/forbidden', component: () => import('../views/error/ForbiddenView.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.meta.guestOnly && authStore.isLoggedIn) {
    return '/products'
  }
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  }
  if (to.meta.requiresAdmin && authStore.role !== 'ADMIN') {
    return '/forbidden'
  }
  if (to.meta.requiresMerchant && authStore.role !== 'MERCHANT') {
    return '/forbidden'
  }
  return true
})

export default router
