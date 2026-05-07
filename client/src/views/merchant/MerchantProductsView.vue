<template>
  <section class="container page">
    <h1>商家端 · 我的商品</h1>
    <p class="sub">仅管理您名下的商品信息</p>
    <p v-if="error" class="error">{{ error }}</p>

    <form class="card form" @submit.prevent="saveProduct">
      <input v-model.trim="form.name" placeholder="商品名称" />
      <input v-model.number="form.categoryId" type="number" placeholder="类目ID" />
      <input v-model.number="form.price" type="number" step="0.01" placeholder="价格" />
      <input v-model.number="form.stock" type="number" placeholder="库存" />
      <input v-model.trim="form.imageUrl" placeholder="图片URL" />
      <input type="file" accept="image/*" @change="handleFileChange" />
      <button class="btn btn-light" type="button" :disabled="uploading" @click="uploadImage">
        {{ uploading ? '上传中...' : '上传图片到服务器' }}
      </button>
      <textarea v-model.trim="form.description" placeholder="描述"></textarea>
      <button class="btn btn-primary" type="submit">{{ editingId ? '保存修改' : '新增商品' }}</button>
    </form>
    <div v-if="form.imageUrl" class="card">
      <p class="sub">图片预览</p>
      <img :src="resolveMediaUrl(form.imageUrl)" alt="商品图片预览" style="max-width: 240px; border-radius: 8px;" />
    </div>

    <div class="card table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>类目</th>
            <th>价格</th>
            <th>库存</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in products" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.categoryId ?? '-' }}</td>
            <td>¥ {{ item.price }}</td>
            <td>{{ item.stock }}</td>
            <td class="row-actions">
              <button class="btn btn-light" @click="editProduct(item)">编辑</button>
              <button class="btn btn-danger" @click="removeProduct(item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { merchantApi } from '../../api/modules/merchant'
import { resolveMediaUrl } from '../../utils/media'

const products = ref([])
const error = ref('')
const editingId = ref(null)
const uploading = ref(false)
const selectedFile = ref(null)
const form = reactive({
  name: '',
  categoryId: null,
  price: null,
  stock: null,
  imageUrl: '',
  description: '',
})

function handleFileChange(event) {
  selectedFile.value = event.target?.files?.[0] || null
}

async function uploadImage() {
  if (!selectedFile.value) {
    error.value = '请先选择图片文件'
    return
  }
  uploading.value = true
  error.value = ''
  try {
    const uploadedUrl = await merchantApi.uploadProductImage(selectedFile.value)
    form.imageUrl = uploadedUrl
    selectedFile.value = null
  } catch (e) {
    error.value = e.message
  } finally {
    uploading.value = false
  }
}

function resetForm() {
  form.name = ''
  form.categoryId = null
  form.price = null
  form.stock = null
  form.imageUrl = ''
  form.description = ''
  editingId.value = null
}

async function fetchProducts() {
  error.value = ''
  try {
    const data = await merchantApi.listProducts({ page: 1, pageSize: 100 })
    products.value = data?.rows || []
  } catch (e) {
    error.value = e.message
  }
}

function editProduct(item) {
  editingId.value = item.id
  form.name = item.name
  form.categoryId = item.categoryId
  form.price = item.price
  form.stock = item.stock
  form.imageUrl = item.imageUrl || ''
  form.description = item.description || ''
}

async function saveProduct() {
  if (!form.name || form.name.trim() === '') {
    error.value = '商品名称不能为空'
    return
  }
  if (form.price === null || form.price === '' || Number.isNaN(Number(form.price))) {
    error.value = '商品价格不能为空'
    return
  }
  if (Number(form.price) < 0) {
    error.value = '商品价格不能小于0'
    return
  }
  if (form.stock === null || form.stock === '' || Number.isNaN(Number(form.stock))) {
    error.value = '商品库存不能为空'
    return
  }
  if (Number(form.stock) < 0) {
    error.value = '商品库存不能小于0'
    return
  }
  error.value = ''
  try {
    const payload = {
      name: form.name,
      categoryId: form.categoryId,
      price: Number(form.price),
      stock: Number(form.stock),
      imageUrl: form.imageUrl || null,
      description: form.description || null,
    }
    if (editingId.value) {
      await merchantApi.updateProduct(editingId.value, payload)
    } else {
      await merchantApi.addProduct(payload)
    }
    resetForm()
    await fetchProducts()
  } catch (e) {
    error.value = e.message
  }
}

async function removeProduct(id) {
  error.value = ''
  try {
    await merchantApi.deleteProduct(id)
    if (editingId.value === id) {
      resetForm()
    }
    await fetchProducts()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(fetchProducts)
</script>
