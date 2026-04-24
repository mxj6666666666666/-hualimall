<template>
  <section class="container page">
    <h1>管理端 · 商品陈列管理</h1>
    <p class="sub">以高端视觉规范维护商品信息与陈列状态</p>
    <p v-if="error" class="error">{{ error }}</p>
    <div class="card">
      <div class="toolbar">
        <input v-model.trim="query.keyword" placeholder="关键词搜索" @keyup.enter="fetchProducts" />
        <input v-model.number="query.categoryId" type="number" min="1" placeholder="分类ID（可选）" />
        <select v-model="query.status">
          <option value="">全部状态</option>
          <option :value="1">上架</option>
          <option :value="0">下架</option>
        </select>
        <button class="btn btn-primary" @click="fetchProducts">查询</button>
      </div>
    </div>
    <div class="card">
      <h3>{{ editingId ? '编辑商品' : '新增商品' }}</h3>
      <div class="form form-grid">
        <input v-model.trim="form.name" placeholder="商品名称" />
        <input v-model.number="form.price" type="number" min="0" step="0.01" placeholder="价格" />
        <input v-model.number="form.stock" type="number" min="0" placeholder="库存" />
        <input v-model.number="form.categoryId" type="number" min="1" placeholder="分类ID" />
        <select v-model.number="form.status">
          <option :value="1">上架</option>
          <option :value="0">下架</option>
        </select>
        <input v-model.trim="form.imageUrl" placeholder="图片URL" />
        <input v-model.trim="form.description" class="full" placeholder="描述" />
      </div>
      <div class="toolbar">
        <button class="btn btn-primary" @click="submitForm">{{ editingId ? '保存修改' : '新增商品' }}</button>
        <button class="btn btn-light" @click="resetForm">重置</button>
        <button class="btn btn-danger" :disabled="selectedIds.length === 0" @click="batchDelete">批量删除</button>
      </div>
    </div>
    <div class="card table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th><input type="checkbox" :checked="allChecked" @change="toggleAll" /></th>
            <th>ID</th>
            <th>名称</th>
            <th>价格</th>
            <th>库存</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in products" :key="item.id">
            <td><input v-model="selectedIds" type="checkbox" :value="item.id" /></td>
            <td>{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>¥ {{ formatPrice(item.price) }}</td>
            <td>{{ item.stock }}</td>
            <td>
              <span :class="['tag', item.status === 1 ? 'tag-success' : 'tag-muted']">
                {{ item.status === 1 ? '上架' : '下架' }}
              </span>
            </td>
            <td class="row-actions">
              <button class="btn btn-light" @click="startEdit(item)">编辑</button>
              <button class="btn btn-danger" @click="remove(item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { adminApi } from '../../api/modules/admin'
import { formatPrice } from '../../utils/format'

const products = ref([])
const selectedIds = ref([])
const editingId = ref(null)
const error = ref('')
const query = reactive({ page: 1, pageSize: 100, keyword: '', categoryId: '', status: '' })
const form = reactive({
  name: '',
  price: '',
  stock: '',
  categoryId: '',
  status: 1,
  imageUrl: '',
  description: '',
})

const allChecked = computed(() => products.value.length > 0 && selectedIds.value.length === products.value.length)

async function fetchProducts() {
  error.value = ''
  try {
    const params = {
      page: query.page,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      status: query.status === '' ? undefined : query.status,
    }
    const data = await adminApi.listProducts(params)
    products.value = data?.rows || []
    selectedIds.value = []
  } catch (e) {
    error.value = e.message
  }
}

function resetForm() {
  editingId.value = null
  form.name = ''
  form.price = ''
  form.stock = ''
  form.categoryId = ''
  form.status = 1
  form.imageUrl = ''
  form.description = ''
}

function startEdit(item) {
  editingId.value = item.id
  form.name = item.name || ''
  form.price = Number(item.price || 0)
  form.stock = Number(item.stock || 0)
  form.categoryId = item.categoryId || ''
  form.status = Number(item.status ?? 1)
  form.imageUrl = item.imageUrl || ''
  form.description = item.description || ''
}

async function submitForm() {
  if (!form.name || form.price === '' || form.stock === '') {
    error.value = '请填写商品名称、价格、库存'
    return
  }
  const payload = {
    name: form.name,
    price: Number(form.price),
    stock: Number(form.stock),
    categoryId: form.categoryId === '' ? null : Number(form.categoryId),
    status: Number(form.status),
    imageUrl: form.imageUrl || null,
    description: form.description || null,
  }
  error.value = ''
  try {
    if (editingId.value) {
      await adminApi.updateProduct(editingId.value, payload)
    } else {
      await adminApi.addProduct(payload)
    }
    resetForm()
    await fetchProducts()
  } catch (e) {
    error.value = e.message
  }
}

async function remove(id) {
  error.value = ''
  try {
    await adminApi.deleteProduct(id)
    await fetchProducts()
  } catch (e) {
    error.value = e.message
  }
}

function toggleAll(event) {
  if (event.target.checked) {
    selectedIds.value = products.value.map((item) => item.id)
    return
  }
  selectedIds.value = []
}

async function batchDelete() {
  if (selectedIds.value.length === 0) return
  error.value = ''
  try {
    await adminApi.deleteProductsBatch(selectedIds.value)
    await fetchProducts()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(fetchProducts)
</script>
