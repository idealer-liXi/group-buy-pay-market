<template>
  <AdminLayout>
    <section class="admin-page">
      <div class="page-header">
        <h1>商品管理</h1>
      </div>
      <div class="card form-card">
        <h2 class="card-title">{{ editing ? '编辑商品' : '新增商品' }}</h2>
        <form class="admin-form" @submit.prevent="submitForm">
          <div class="form-grid">
            <div v-if="editing" class="field">
              <label>商品ID</label>
              <input v-model="form.goodsId" disabled placeholder="请输入商品ID" />
            </div>
            <div class="field">
              <label>商品名称</label>
              <input v-model="form.goodsName" placeholder="请输入商品名称" />
            </div>
            <div class="field">
              <label>原价</label>
              <input v-model.number="form.originalPrice" type="number" min="0.01" step="0.01" placeholder="请输入原价" />
            </div>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">{{ editing ? '保存修改' : '新增商品' }}</button>
            <button v-if="editing" type="button" class="btn btn-ghost" @click="cancelEdit">取消</button>
          </div>
        </form>
        <div v-if="editing && selectedGoods" class="image-manager">
          <h3>商品图片</h3>
          <input type="file" accept="image/jpeg,image/png,image/webp" :disabled="imageUploading" @change="handleImageUpload" />
          <div class="image-list">
            <div v-for="image in selectedGoods.imageList ?? []" :key="image.imageId" class="image-item">
              <img :src="image.imageUrl" :alt="selectedGoods.goodsName" />
              <button type="button" class="btn-link link-danger" :data-test="`delete-image-${image.imageId}`" @click="deleteImage(image)">删除</button>
            </div>
            <div v-if="(selectedGoods.imageList ?? []).length === 0" class="empty-state">暂无图片</div>
          </div>
        </div>
      </div>
      <div class="card">
        <div v-if="loading" class="empty-state">加载中...</div>
        <div v-else-if="error" class="empty-state error-state">{{ error }}</div>
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>封面</th>
              <th>商品ID</th>
              <th>商品名</th>
              <th>原价</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in goodsList" :key="item.goodsId">
              <td>
                <img v-if="item.coverImageUrl" class="cover-thumb" :src="item.coverImageUrl" :alt="item.goodsName" />
                <span v-else class="cover-empty">无图</span>
              </td>
              <td><span class="id-badge">{{ item.goodsId }}</span></td>
              <td>{{ item.goodsName }}</td>
              <td class="price">¥{{ item.originalPrice }}</td>
              <td>
                <span :class="['status-tag', item.status === 0 ? 'status-active' : 'status-disabled']">
                  {{ item.status === 0 ? '可售' : '停用' }}
                </span>
              </td>
              <td class="actions">
                <button class="btn-link" :data-test="`edit-goods-${item.goodsId}`" @click="editGoods(item)">编辑</button>
                <button :class="['btn-link', item.status === 0 ? 'link-danger' : 'link-success']" @click="toggleStatus(item)">
                  {{ item.status === 0 ? '停用' : '启用' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AdminLayout from '../../components/admin/AdminLayout.vue'
import { createAdminGoods, deleteAdminGoodsImage, queryAdminGoods, updateAdminGoods, updateAdminGoodsStatus, uploadAdminGoodsImage } from '../../lib/admin'
import type { AdminGoodsImageItem, AdminGoodsItem } from '../../types/admin'

const goodsList = ref<AdminGoodsItem[]>([])
const loading = ref(true)
const error = ref('')
const editing = ref(false)
const form = ref({ goodsId: '', goodsName: '', originalPrice: 0 })
const selectedGoods = ref<AdminGoodsItem | null>(null)
const imageUploading = ref(false)

async function loadGoods() {
  loading.value = true
  error.value = ''

  try {
    const result = await queryAdminGoods()
    if (result.code === '0000') {
      goodsList.value = result.data.goodsList
    } else {
      error.value = result.info || '获取商品失败'
    }
  } finally {
    loading.value = false
  }
}

function editGoods(item: AdminGoodsItem) {
  editing.value = true
  selectedGoods.value = item
  form.value = { goodsId: item.goodsId, goodsName: item.goodsName, originalPrice: item.originalPrice }
}

function cancelEdit() {
  editing.value = false
  selectedGoods.value = null
  form.value = { goodsId: '', goodsName: '', originalPrice: 0 }
}

async function submitForm() {
  if (editing.value) {
    await updateAdminGoods(form.value.goodsId, { goodsName: form.value.goodsName, originalPrice: form.value.originalPrice })
    editing.value = false
    selectedGoods.value = null
    form.value = { goodsId: '', goodsName: '', originalPrice: 0 }
    await loadGoods()
  } else {
    const result = await createAdminGoods({ goodsName: form.value.goodsName, originalPrice: form.value.originalPrice })
    await loadGoods()
    if (result.code === '0000') {
      const created = goodsList.value.find((item) => item.goodsId === result.data.goodsId)
      if (created) {
        editGoods(created)
        return
      }
    }
    editing.value = false
    selectedGoods.value = null
    form.value = { goodsId: '', goodsName: '', originalPrice: 0 }
  }
}

async function toggleStatus(item: AdminGoodsItem) {
  await updateAdminGoodsStatus(item.goodsId, item.status === 0 ? 1 : 0)
  await loadGoods()
}

async function handleImageUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || !selectedGoods.value) {
    return
  }
  imageUploading.value = true
  const goodsId = selectedGoods.value.goodsId
  try {
    await uploadAdminGoodsImage(goodsId, file)
    await loadGoods()
    selectedGoods.value = goodsList.value.find((item) => item.goodsId === goodsId) ?? selectedGoods.value
  } finally {
    imageUploading.value = false
    input.value = ''
  }
}

async function deleteImage(image: AdminGoodsImageItem) {
  if (!selectedGoods.value) {
    return
  }
  const goodsId = selectedGoods.value.goodsId
  await deleteAdminGoodsImage(goodsId, image.imageId)
  await loadGoods()
  selectedGoods.value = goodsList.value.find((item) => item.goodsId === goodsId) ?? selectedGoods.value
}

onMounted(loadGoods)
</script>

<style scoped>
.admin-page {
  padding: 28px 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
}

.card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
}

.card-title {
  margin: 0 0 20px;
  font-size: 16px;
  font-weight: 600;
  color: #334155;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.field input {
  height: 40px;
  padding: 0 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #1e293b;
  background: #f8fafc;
  transition: all 0.2s ease;
  outline: none;
}

.field input::placeholder {
  color: #cbd5e1;
}

.field input:focus {
  border-color: #818cf8;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(129, 140, 248, 0.12);
}

.field input:disabled {
  background: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.image-manager {
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid #e2e8f0;
}

.image-manager h3 {
  margin: 0 0 12px;
  font-size: 14px;
  color: #334155;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}

.image-item {
  width: 120px;
  display: grid;
  gap: 8px;
}

.image-item img {
  width: 120px;
  height: 90px;
  object-fit: cover;
  border-radius: 10px;
}

.btn {
  padding: 9px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: #fff;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.btn-primary:hover {
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
  transform: translateY(-1px);
}

.btn-ghost {
  background: #f1f5f9;
  color: #64748b;
}

.btn-ghost:hover {
  background: #e2e8f0;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 12px 16px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.data-table th:first-child {
  border-radius: 8px 0 0 0;
}

.data-table th:last-child {
  border-radius: 0 8px 0 0;
}

.data-table td {
  padding: 14px 16px;
  font-size: 14px;
  color: #334155;
  border-bottom: 1px solid #f1f5f9;
}

.cover-thumb {
  width: 52px;
  height: 52px;
  object-fit: cover;
  border-radius: 8px;
  background: #f1f5f9;
}

.cover-empty {
  color: #94a3b8;
  font-size: 12px;
}

.data-table tbody tr:hover {
  background: #f8fafc;
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.id-badge {
  display: inline-block;
  padding: 2px 10px;
  background: #f1f5f9;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  font-family: monospace;
}

.price {
  font-weight: 600;
  color: #1e293b;
}

.status-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-active {
  background: #ecfdf5;
  color: #059669;
}

.status-disabled {
  background: #fef2f2;
  color: #dc2626;
}

.actions {
  display: flex;
  gap: 12px;
}

.btn-link {
  background: none;
  border: none;
  padding: 0;
  font-size: 13px;
  font-weight: 600;
  color: #6366f1;
  cursor: pointer;
  transition: color 0.2s;
}

.btn-link:hover {
  color: #4f46e5;
}

.link-danger {
  color: #ef4444;
}

.link-danger:hover {
  color: #dc2626;
}

.link-success {
  color: #059669;
}

.link-success:hover {
  color: #047857;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #94a3b8;
  font-size: 14px;
}

.error-state {
  color: #ef4444;
}
</style>
