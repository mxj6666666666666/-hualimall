const ABSOLUTE_URL_REGEX = /^(?:[a-z]+:)?\/\//i

function getApiOrigin() {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  if (!ABSOLUTE_URL_REGEX.test(baseUrl)) {
    return ''
  }
  try {
    return new URL(baseUrl).origin
  } catch {
    return ''
  }
}

const API_ORIGIN = getApiOrigin()

export function resolveMediaUrl(url) {
  const value = String(url || '').trim()
  if (!value) return ''
  if (ABSOLUTE_URL_REGEX.test(value) || value.startsWith('data:') || value.startsWith('blob:')) {
    return value
  }
  if (value.startsWith('/')) {
    return API_ORIGIN ? `${API_ORIGIN}${value}` : value
  }
  return API_ORIGIN ? `${API_ORIGIN}/${value}` : `/${value}`
}
