<template>
  <AdminLayout>
    <section class="admin-page">
      <div class="page-header">
        <h1>折扣管理</h1>
      </div>
      <div class="card form-card">
        <h2 class="card-title">{{ editing ? '编辑折扣' : '新增折扣' }}</h2>
        <form class="admin-form" @submit.prevent="submitDiscount">
          <div class="form-grid">
            <div v-if="editing" class="field">
              <label>折扣ID</label>
              <input v-model="form.discountId" disabled placeholder="请输入折扣ID" />
            </div>
            <div class="field">
              <label>折扣名称</label>
              <input v-model="form.discountName" placeholder="请输入折扣名称" />
            </div>
            <div class="field">
              <label>折扣描述</label>
              <input v-model="form.discountDesc" placeholder="请输入折扣描述" />
            </div>
            <div class="field">
              <label>优惠方案</label>
              <select v-model="form.marketPlan">
                <option value="ZJ">ZJ</option>
                <option value="MJ">MJ</option>
                <option value="N">N</option>
                <option value="ZK">ZK</option>
              </select>
            </div>
            <div class="field">
              <label>优惠表达式</label>
              <input v-model="form.marketExpr" placeholder="请输入优惠表达式" />
            </div>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">{{ editing ? '保存折扣' : '新增折扣' }}</button>
            <button v-if="editing" type="button" class="btn btn-ghost" @click="cancelEdit">取消</button>
          </div>
        </form>
      </div>
      <div class="card">
        <div v-if="loading" class="empty-state">加载中...</div>
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>折扣ID</th>
              <th>折扣名称</th>
              <th>优惠方案</th>
              <th>优惠表达式</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in discountList" :key="item.discountId">
              <td><span class="id-badge">{{ item.discountId }}</span></td>
              <td>{{ item.discountName }}</td>
              <td><code class="code-badge">{{ item.marketPlan }}</code></td>
              <td><code class="code-badge">{{ item.marketExpr }}</code></td>
              <td>
                <span :class="['status-tag', item.status === 0 ? 'status-active' : 'status-disabled']">
                  {{ item.status === 0 ? '启用' : '停用' }}
                </span>
              </td>
              <td class="actions">
                <button class="btn-link" @click="editDiscount(item)">编辑</button>
                <button :class="['btn-link', item.status === 0 ? 'link-danger' : 'link-success']" @click="toggleDiscountStatus(item)">
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
import { createAdminDiscount, queryAdminDiscounts, updateAdminDiscount, updateAdminDiscountStatus } from '../../lib/admin'
import type { AdminDiscountItem } from '../../types/admin'

const discountList = ref<AdminDiscountItem[]>([])
const loading = ref(true)
const editing = ref(false)
const form = ref({ discountId: '', discountName: '', discountDesc: '', discountType: 1, marketPlan: 'ZJ', marketExpr: '' })

async function loadDiscounts() {
  const result = await queryAdminDiscounts()
  if (result.code === '0000') {
    discountList.value = result.data.discountList
  }
  loading.value = false
}

function editDiscount(item: AdminDiscountItem) {
  editing.value = true
  form.value = { ...item }
}

function cancelEdit() {
  editing.value = false
  form.value = { discountId: '', discountName: '', discountDesc: '', discountType: 1, marketPlan: 'ZJ', marketExpr: '' }
}

async function submitDiscount() {
  if (editing.value) {
    await updateAdminDiscount(form.value.discountId, {
      discountName: form.value.discountName,
      discountDesc: form.value.discountDesc,
      discountType: form.value.discountType,
      marketPlan: form.value.marketPlan,
      marketExpr: form.value.marketExpr
    })
  } else {
    await createAdminDiscount(form.value)
  }

  editing.value = false
  form.value = { discountId: '', discountName: '', discountDesc: '', discountType: 1, marketPlan: 'ZJ', marketExpr: '' }
  await loadDiscounts()
}

async function toggleDiscountStatus(item: AdminDiscountItem) {
  await updateAdminDiscountStatus(item.discountId, item.status === 0 ? 1 : 0)
  await loadDiscounts()
}

onMounted(() => {
  loadDiscounts()
})
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

.field select {
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

.field select:focus {
  border-color: #818cf8;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(129, 140, 248, 0.12);
}

.field select {
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

.field select:focus {
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

.data-table tbody tr:hover {
  background: #f8fafc;
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.code-badge {
  display: inline-block;
  padding: 2px 8px;
  background: #f1f5f9;
  border-radius: 6px;
  font-size: 13px;
  color: #475569;
  font-family: monospace;
}

.id-badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 6px;
  background: #eef2ff;
  color: #4f46e5;
  font-family: monospace;
  font-size: 13px;
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
</style>
