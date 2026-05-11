<template>
  <Transition name="toast-float">
    <div v-if="toast.visible" class="app-toast-wrap" :class="toast.placement === 'bottom' ? 'bottom' : 'top'">
      <div class="app-toast" role="status" aria-live="polite">
        {{ toast.message }}
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { useToast } from '../../composables/useToast'

const { toast } = useToast()
</script>

<style scoped>
.app-toast-wrap {
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  pointer-events: none;
  z-index: 30;
}

.app-toast-wrap.top {
  top: calc(env(safe-area-inset-top, 0px) + 92px);
}

.app-toast-wrap.bottom {
  bottom: calc(env(safe-area-inset-bottom, 0px) + 20px);
}

.app-toast {
  max-width: min(88vw, 460px);
  padding: 10px 16px;
  border-radius: 999px;
  background: rgba(29, 29, 31, 0.9);
  color: #fff;
  font-size: 13px;
  line-height: 1.4;
  text-align: center;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.18);
}

.toast-float-enter-active,
.toast-float-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.toast-float-enter-from,
.toast-float-leave-to {
  opacity: 0;
  transform: translate(-50%, -8px);
}
</style>
