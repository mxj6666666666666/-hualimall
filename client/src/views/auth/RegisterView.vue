<template>
  <section class="auth-wrap">
    <div class="card auth-card">
      <h1>开启专属账号</h1>
      <p class="sub">轻盈注册，进入高端时尚精选空间</p>
      <form class="form" @submit.prevent="handleRegister">
        <input v-model.trim="form.username" placeholder="用户名" />
        <input v-model.trim="form.nickname" placeholder="昵称" />
        <input v-model.trim="form.password" type="password" placeholder="密码" />
        <select v-model="form.role">
          <option value="BUYER">注册买家账号</option>
          <option value="MERCHANT">注册商家账号</option>
        </select>
        <template v-if="form.role === 'BUYER'">
          <input v-model.trim="form.realName" placeholder="真实姓名" />
          <input v-model.trim="form.phone" placeholder="手机号" />
        </template>
        <template v-else>
          <input v-model.trim="form.shopName" placeholder="店铺名" />
          <input v-model.trim="form.businessLicenseNo" placeholder="营业执照号" />
          <input v-model.trim="form.contactName" placeholder="联系人" />
          <input v-model.trim="form.contactPhone" placeholder="联系电话" />
        </template>
        <button class="btn btn-primary" type="submit" :disabled="loading">
          {{ loading ? '提交中...' : '注册' }}
        </button>
      </form>
      <p class="hint">
        已有账号？
        <RouterLink to="/login">去登录</RouterLink>
      </p>
      <p v-if="message" class="success">{{ message }}</p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { userApi } from '../../api/modules/user'

const loading = ref(false)
const error = ref('')
const message = ref('')
const form = reactive({
  username: '',
  nickname: '',
  password: '',
  role: 'BUYER',
  realName: '',
  phone: '',
  shopName: '',
  businessLicenseNo: '',
  contactName: '',
  contactPhone: '',
})

async function handleRegister() {
  if (!form.username || !form.password) {
    error.value = '用户名和密码不能为空'
    return
  }
  if (form.role === 'BUYER' && (!form.realName || !form.phone)) {
    error.value = '买家注册需填写真实姓名和手机号'
    return
  }
  if (
    form.role === 'MERCHANT' &&
    (!form.shopName || !form.businessLicenseNo || !form.contactName || !form.contactPhone)
  ) {
    error.value = '商家注册需填写店铺名、营业执照号、联系人和联系电话'
    return
  }
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    const payload =
      form.role === 'BUYER'
        ? {
            username: form.username,
            nickname: form.nickname,
            password: form.password,
            role: form.role,
            realName: form.realName,
            phone: form.phone,
          }
        : {
            username: form.username,
            nickname: form.nickname,
            password: form.password,
            role: form.role,
            shopName: form.shopName,
            businessLicenseNo: form.businessLicenseNo,
            contactName: form.contactName,
            contactPhone: form.contactPhone,
          }
    await userApi.register(payload)
    message.value = '注册成功，请前往登录'
    form.username = ''
    form.nickname = ''
    form.password = ''
    form.realName = ''
    form.phone = ''
    form.shopName = ''
    form.businessLicenseNo = ''
    form.contactName = ''
    form.contactPhone = ''
  } catch (e) {
    error.value = e?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>
