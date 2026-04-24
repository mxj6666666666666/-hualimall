<template>
  <section class="auth-wrap login-scene">
    <Transition :name="introFast ? 'intro-fade-fast' : 'intro-fade'">
      <div
        v-if="showIntro"
        class="intro-screen"
        :class="{ 'intro-screen-fast': introFast }"
        tabindex="0"
        @pointerdown="finishIntro(true)"
        @keydown="finishIntro(true)"
      >
        <div class="intro-glow"></div>
        <div class="intro-lines"></div>
        <div class="intro-copy">
          <p class="intro-eyebrow">HualiMall</p>
          <h2>Elevated Commerce</h2>
          <p>极简、克制、流畅的高端购物体验</p>
        </div>
      </div>
    </Transition>

    <div class="card auth-card" :class="{ 'auth-card-ready': !showIntro }">
      <h1>欢迎回到 Huali Maison</h1>
      <p class="sub">以优雅方式继续您的高端购物旅程</p>
      <form class="form" @submit.prevent="handleLogin">
        <input v-model.trim="form.username" placeholder="用户名" />
        <input v-model.trim="form.password" type="password" placeholder="密码" />
        <button class="btn btn-primary" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <p class="hint">
        还没有账号？
        <RouterLink to="/register">去注册</RouterLink>
      </p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </section>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { userApi } from '../../api/modules/user'
import { useAuthStore } from '../../store/modules/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const error = ref('')
const showIntro = ref(true)
const introFast = ref(false)
const form = reactive({ username: '', password: '' })
let introTimer = null

onMounted(() => {
  window.addEventListener('keydown', handleIntroInteraction)
  window.addEventListener('pointerdown', handleIntroInteraction)
  introTimer = setTimeout(() => {
    finishIntro()
  }, 3000)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleIntroInteraction)
  window.removeEventListener('pointerdown', handleIntroInteraction)
  if (introTimer) {
    clearTimeout(introTimer)
    introTimer = null
  }
})

function handleIntroInteraction() {
  finishIntro(true)
}

function finishIntro(fast = false) {
  if (!showIntro.value) return
  if (introTimer) {
    clearTimeout(introTimer)
    introTimer = null
  }
  introFast.value = fast
  showIntro.value = false
}

async function handleLogin() {
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const result = await userApi.login(form)
    authStore.setToken(result.token)
    const profile = await userApi.profile()
    authStore.setUser(profile)
    const redirectPath = route.query.redirect || '/products'
    router.push(String(redirectPath))
  } catch (e) {
    error.value = e?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-scene {
  position: relative;
  overflow: hidden;
}

.intro-screen {
  position: absolute;
  inset: 0;
  z-index: 99;
  display: grid;
  place-items: center;
  background: linear-gradient(180deg, #ffffff 0%, #f5f6fa 55%, #eef1f7 100%);
}

.intro-screen-fast .intro-glow,
.intro-screen-fast .intro-lines,
.intro-screen-fast .intro-copy {
  animation-duration: 0.45s;
}

.intro-glow {
  position: absolute;
  width: min(70vw, 920px);
  height: min(70vw, 920px);
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 113, 227, 0.16) 0%, rgba(0, 113, 227, 0) 62%);
  filter: blur(4px);
  animation: glow-drift 3s ease-in-out forwards;
}

.intro-lines {
  position: absolute;
  inset: 0;
  background-image: repeating-linear-gradient(
    90deg,
    rgba(255, 255, 255, 0) 0,
    rgba(255, 255, 255, 0) 82px,
    rgba(214, 222, 236, 0.44) 82px,
    rgba(214, 222, 236, 0.44) 83px
  );
  animation: lines-pan 3s linear forwards;
}

.intro-copy {
  position: relative;
  text-align: center;
  color: #1d1d1f;
  animation: copy-rise 3s cubic-bezier(0.22, 0.61, 0.36, 1) forwards;
}

.intro-eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.3em;
  text-transform: uppercase;
  color: #6e6e73;
}

.intro-copy h2 {
  margin: 14px 0 10px;
  font-size: clamp(36px, 5vw, 66px);
  letter-spacing: -0.02em;
  font-weight: 600;
}

.intro-copy p {
  margin: 0;
  color: #515154;
  letter-spacing: 0.03em;
}

.auth-card {
  opacity: 0;
  transform: translateY(26px);
}

.auth-card-ready {
  animation: card-enter 0.95s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

.intro-fade-leave-active {
  transition: opacity 0.8s ease;
}

.intro-fade-leave-to {
  opacity: 0;
}

.intro-fade-fast-leave-active {
  transition: opacity 0.24s ease;
}

.intro-fade-fast-leave-to {
  opacity: 0;
}

@keyframes glow-drift {
  0% {
    transform: translateY(18px) scale(0.9);
    opacity: 0.6;
  }
  100% {
    transform: translateY(-12px) scale(1.05);
    opacity: 1;
  }
}

@keyframes lines-pan {
  0% {
    transform: translateX(24px);
    opacity: 0.2;
  }
  100% {
    transform: translateX(-16px);
    opacity: 0.58;
  }
}

@keyframes copy-rise {
  0% {
    opacity: 0;
    transform: translateY(36px);
  }
  30% {
    opacity: 1;
  }
  80% {
    opacity: 1;
    transform: translateY(0);
  }
  100% {
    opacity: 0;
    transform: translateY(-8px);
  }
}

@keyframes card-enter {
  0% {
    opacity: 0;
    transform: translateY(26px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
