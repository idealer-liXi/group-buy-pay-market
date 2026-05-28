<template>
  <AdminLayout>
    <section class="admin-page">
      <div class="page-header">
        <h1>活动管理</h1>
      </div>
      <div class="card form-card">
        <h2 class="card-title">{{ editing ? '编辑活动' : '新增活动' }}</h2>
        <form class="admin-form" @submit.prevent="submitActivity">
          <div class="form-grid">
            <div v-if="editing" class="field">
              <label>活动ID</label>
              <input v-model.number="form.activityId" disabled placeholder="活动ID" />
            </div>
            <div class="field">
              <label>活动名称</label>
              <input v-model="form.activityName" placeholder="请输入活动名称" />
            </div>
            <div class="field">
              <label>商品ID</label>
              <input v-model="form.goodsId" placeholder="请输入商品ID" />
            </div>
            <div class="field">
              <label>折扣ID</label>
              <input v-model="form.discountId" placeholder="请输入折扣ID" />
            </div>
            <div class="field">
              <label>拼团类型</label>
              <input v-model.number="form.groupType" type="number" min="0" placeholder="拼团类型" />
            </div>
            <div class="field">
              <label>参与次数限制</label>
              <input v-model.number="form.takeLimitCount" type="number" min="1" placeholder="参与次数限制" />
            </div>
            <div class="field">
              <label>成团人数</label>
              <input v-model.number="form.target" type="number" min="2" placeholder="成团人数" />
            </div>
            <div class="field">
              <label>团有效时长(分)</label>
              <input v-model.number="form.validTime" type="number" min="1" placeholder="团有效时长" />
            </div>
            <div class="field">
              <label>开始时间</label>
              <input v-model="form.startTime" type="datetime-local" placeholder="开始时间" />
            </div>
            <div class="field">
              <label>结束时间</label>
              <input v-model="form.endTime" type="datetime-local" placeholder="结束时间" />
            </div>
            <div class="field">
              <label>标签ID</label>
              <input v-model="form.tagId" placeholder="标签ID" />
            </div>
            <div class="field">
              <label>标签范围</label>
              <input v-model="form.tagScope" placeholder="标签范围" />
            </div>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">{{ editing ? '保存活动' : '新增活动' }}</button>
            <button v-if="editing" type="button" class="btn btn-ghost" @click="cancelEdit">取消</button>
          </div>
          <p v-if="error" class="form-error">{{ error }}</p>
        </form>
      </div>
      <div class="card">
        <div v-if="loading" class="empty-state">加载中...</div>
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>活动名称</th>
              <th>商品ID</th>
              <th>折扣ID</th>
              <th>状态</th>
              <th>开始时间</th>
              <th>结束时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in activityList" :key="item.activityId">
              <td>{{ item.activityName }}</td>
              <td><span class="id-badge">{{ item.goodsId }}</span></td>
              <td><span class="id-badge">{{ item.discountId }}</span></td>
              <td>
                <span :class="['status-tag', statusClass(item.status)]">
                  {{ statusText(item.status) }}
                </span>
              </td>
              <td class="time">{{ item.startTime }}</td>
              <td class="time">{{ item.endTime }}</td>
              <td class="actions">
                <button class="btn-link" @click="editActivity(item)">编辑</button>
                <button class="btn-link link-success" @click="setStatus(item, 1)">生效</button>
                <button class="btn-link link-danger" @click="setStatus(item, 3)">废弃</button>
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
import { createAdminActivity, queryAdminActivities, updateAdminActivity, updateAdminActivityStatus } from '../../lib/admin'
import type { AdminActivityItem } from '../../types/admin'

const activityList = ref<AdminActivityItem[]>([])
const loading = ref(true)
const editing = ref(false)
const error = ref('')
const form = ref({ activityId: 0, activityName: '', goodsId: '', discountId: '', groupType: 1, takeLimitCount: 1, target: 2, validTime: 15, startTime: '', endTime: '', tagId: '', tagScope: '' })

function statusText(status: number) {
  const map: Record<number, string> = { 0: '创建', 1: '生效', 2: '过期', 3: '废弃' }
  return map[status] ?? '未知'
}

function statusClass(status: number) {
  const map: Record<number, string> = { 0: 'status-created', 1: 'status-active', 2: 'status-expired', 3: 'status-disabled' }
  return map[status] ?? 'status-created'
}

async function loadActivities() {
  const result = await queryAdminActivities()
  if (result.code === '0000') {
    activityList.value = result.data.activityList
  }
  loading.value = false
}

function editActivity(item: AdminActivityItem) {
  editing.value = true
  form.value = {
    activityId: item.activityId,
    activityName: item.activityName,
    goodsId: item.goodsId,
    discountId: item.discountId,
    groupType: item.groupType,
    takeLimitCount: item.takeLimitCount,
    target: item.target,
    validTime: item.validTime,
    startTime: item.startTime,
    endTime: item.endTime,
    tagId: item.tagId ?? '',
    tagScope: item.tagScope ?? ''
  }
}

function cancelEdit() {
  editing.value = false
  form.value = { activityId: 0, activityName: '', goodsId: '', discountId: '', groupType: 1, takeLimitCount: 1, target: 2, validTime: 15, startTime: '', endTime: '', tagId: '', tagScope: '' }
}

async function submitActivity() {
  error.value = ''

  const result = editing.value
    ? await updateAdminActivity(form.value.activityId, form.value)
    : await createAdminActivity(form.value)

  if (result.code !== '0000') {
    error.value = result.info || '保存活动失败'
    return
  }

  editing.value = false
  form.value = { activityId: 0, activityName: '', goodsId: '', discountId: '', groupType: 1, takeLimitCount: 1, target: 2, validTime: 15, startTime: '', endTime: '', tagId: '', tagScope: '' }
  await loadActivities()
}

async function setStatus(item: AdminActivityItem, status: number) {
  await updateAdminActivityStatus(item.activityId, status)
  await loadActivities()
}

onMounted(loadActivities)
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

.form-error {
  margin: 12px 0 0;
  color: #dc2626;
  font-size: 13px;
  font-weight: 600;
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

.time {
  font-size: 13px;
  color: #64748b;
  white-space: nowrap;
}

.status-tag {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-created {
  background: #f0f9ff;
  color: #0284c7;
}

.status-active {
  background: #ecfdf5;
  color: #059669;
}

.status-expired {
  background: #fefce8;
  color: #ca8a04;
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
