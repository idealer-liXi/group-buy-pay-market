<template>
  <AdminLayout>
    <section class="admin-page">
      <div class="page-header">
        <h1>用户管理</h1>
      </div>
      <div class="card form-card">
        <div class="field">
          <label>搜索用户</label>
          <input v-model="keyword" placeholder="输入用户ID或昵称" @keyup.enter="loadUsers" />
        </div>
        <button class="btn btn-primary" @click="loadUsers">搜索</button>
      </div>
      <div class="card">
        <div v-if="loading" class="empty-state">加载中...</div>
        <table v-else class="data-table">
          <thead>
            <tr>
              <th>用户ID</th>
              <th>昵称</th>
              <th>登录方式</th>
              <th>状态</th>
              <th>最近登录</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in userList" :key="item.userId">
              <td><span class="id-badge">{{ item.userId }}</span></td>
              <td>{{ item.displayName }}</td>
              <td>{{ item.loginType }}</td>
              <td>{{ item.status === 0 ? '正常' : '停用' }}</td>
              <td>{{ item.lastLoginTime }}</td>
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
import { queryAdminUsers } from '../../lib/admin'
import type { AdminUserItem } from '../../types/admin'

const keyword = ref('')
const loading = ref(true)
const userList = ref<AdminUserItem[]>([])

async function loadUsers() {
  loading.value = true
  const result = await queryAdminUsers(keyword.value)
  if (result.code === '0000') {
    userList.value = result.data.userList
  }
  loading.value = false
}

onMounted(loadUsers)
</script>

<style scoped>
.admin-page { padding: 28px 32px; display: flex; flex-direction: column; gap: 24px; }
.page-header h1 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }
.card { background: #fff; border-radius: 14px; padding: 24px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04); }
.form-card { display: flex; align-items: flex-end; gap: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 13px; font-weight: 600; color: #475569; }
.field input { height: 40px; padding: 0 12px; border: 1.5px solid #e2e8f0; border-radius: 8px; font-size: 14px; color: #1e293b; background: #f8fafc; outline: none; }
.btn { border: none; border-radius: 8px; padding: 10px 18px; font-weight: 600; cursor: pointer; }
.btn-primary { color: #fff; background: #6366f1; }
.empty-state { padding: 24px; text-align: center; color: #64748b; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 14px; border-bottom: 1px solid #e2e8f0; text-align: left; font-size: 14px; color: #334155; }
.data-table th { color: #64748b; font-weight: 700; background: #f8fafc; }
.id-badge { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; font-size: 12px; color: #475569; }
</style>
