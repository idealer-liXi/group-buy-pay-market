<template>
  <AdminLayout>
    <section class="admin-page">
      <div class="page-header">
        <h1>标签管理</h1>
      </div>
      <div class="card form-card">
        <h2 class="card-title">新增标签</h2>
        <div class="form-grid">
          <div class="field">
            <label>标签ID</label>
            <input v-model="form.tagId" placeholder="例如 T001" />
          </div>
          <div class="field">
            <label>标签名称</label>
            <input v-model="form.tagName" placeholder="例如 新人" />
          </div>
          <div class="field">
            <label>标签描述</label>
            <input v-model="form.tagDesc" placeholder="标签描述" />
          </div>
        </div>
        <button class="btn btn-primary" @click="submitTag">新增标签</button>
      </div>
      <div class="card">
        <table class="data-table">
          <thead>
            <tr>
              <th>标签ID</th>
              <th>名称</th>
              <th>描述</th>
              <th>人数</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in tagList" :key="item.tagId">
              <td><span class="id-badge">{{ item.tagId }}</span></td>
              <td>{{ item.tagName }}</td>
              <td>{{ item.tagDesc }}</td>
              <td>{{ item.statistics }}</td>
              <td><button class="btn-link" @click="selectTag(item)">成员</button></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="selectedTag" class="card">
        <h2 class="card-title">{{ selectedTag.tagName }} 成员</h2>
        <div class="member-actions">
          <div class="field">
            <label>添加用户</label>
            <select v-model="selectedUserId">
              <option value="">请选择用户</option>
              <option v-for="user in userList" :key="user.userId" :value="user.userId">{{ user.displayName }} - {{ user.userId }}</option>
            </select>
          </div>
          <button class="btn btn-primary" @click="addMember">添加成员</button>
        </div>
        <table class="data-table">
          <tbody>
            <tr v-for="member in memberList" :key="member.userId">
              <td>{{ member.userId }}</td>
              <td>{{ member.displayName }}</td>
              <td><button class="btn-link link-danger" @click="removeMember(member.userId)">移除</button></td>
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
import { addAdminTagMember, createAdminTag, queryAdminTagMembers, queryAdminTags, queryAdminUsers, removeAdminTagMember } from '../../lib/admin'
import type { AdminTagItem, AdminTagMemberItem, AdminUserItem } from '../../types/admin'

const tagList = ref<AdminTagItem[]>([])
const userList = ref<AdminUserItem[]>([])
const memberList = ref<AdminTagMemberItem[]>([])
const selectedTag = ref<AdminTagItem | null>(null)
const selectedUserId = ref('')
const form = ref({ tagId: '', tagName: '', tagDesc: '' })

async function loadTags() {
  const result = await queryAdminTags()
  if (result.code === '0000') tagList.value = result.data.tagList
}

async function loadUsers() {
  const result = await queryAdminUsers('')
  if (result.code === '0000') userList.value = result.data.userList
}

async function submitTag() {
  const result = await createAdminTag(form.value)
  if (result.code === '0000') {
    form.value = { tagId: '', tagName: '', tagDesc: '' }
    await loadTags()
  }
}

async function selectTag(tag: AdminTagItem) {
  selectedTag.value = tag
  const result = await queryAdminTagMembers(tag.tagId)
  if (result.code === '0000') memberList.value = result.data.memberList
}

async function addMember() {
  if (!selectedTag.value || !selectedUserId.value) return
  await addAdminTagMember(selectedTag.value.tagId, selectedUserId.value)
  await selectTag(selectedTag.value)
  await loadTags()
}

async function removeMember(userId: string) {
  if (!selectedTag.value) return
  await removeAdminTagMember(selectedTag.value.tagId, userId)
  await selectTag(selectedTag.value)
  await loadTags()
}

onMounted(async () => {
  await Promise.all([loadTags(), loadUsers()])
})
</script>

<style scoped>
.admin-page { padding: 28px 32px; display: flex; flex-direction: column; gap: 24px; }
.page-header h1 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }
.card { background: #fff; border-radius: 14px; padding: 24px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04); }
.card-title { margin: 0 0 20px; font-size: 16px; font-weight: 600; color: #334155; }
.form-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; margin-bottom: 16px; }
.member-actions { display: flex; align-items: flex-end; gap: 16px; margin-bottom: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 13px; font-weight: 600; color: #475569; }
.field input, .field select { height: 40px; padding: 0 12px; border: 1.5px solid #e2e8f0; border-radius: 8px; font-size: 14px; color: #1e293b; background: #f8fafc; outline: none; }
.btn { border: none; border-radius: 8px; padding: 10px 18px; font-weight: 600; cursor: pointer; }
.btn-primary { color: #fff; background: #6366f1; }
.btn-link { border: none; background: transparent; color: #4f46e5; cursor: pointer; font-weight: 600; }
.link-danger { color: #dc2626; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 14px; border-bottom: 1px solid #e2e8f0; text-align: left; font-size: 14px; color: #334155; }
.data-table th { color: #64748b; font-weight: 700; background: #f8fafc; }
.id-badge { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size: 12px; color: #475569; }
</style>
