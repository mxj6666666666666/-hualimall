import { reactive, readonly } from 'vue'

const toastState = reactive({
  visible: false,
  message: '',
  duration: 1000,
  placement: 'top',
})

let hideTimer = null

function normalizeDuration(duration) {
  const value = Number(duration)
  if (!Number.isFinite(value)) return 1000
  return Math.min(1500, Math.max(500, value))
}

function showToast(message, options = {}) {
  if (!message) return
  if (hideTimer) {
    clearTimeout(hideTimer)
    hideTimer = null
  }
  toastState.message = String(message)
  toastState.duration = normalizeDuration(options.duration)
  toastState.placement = options.placement === 'bottom' ? 'bottom' : 'top'
  toastState.visible = true
  hideTimer = setTimeout(() => {
    toastState.visible = false
  }, toastState.duration)
}

function hideToast() {
  if (hideTimer) {
    clearTimeout(hideTimer)
    hideTimer = null
  }
  toastState.visible = false
}

export function useToast() {
  return {
    toast: readonly(toastState),
    showToast,
    hideToast,
  }
}
