<template>
  <div class="admin-login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="login-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
        </div>
        <h1>后台登录</h1>
        <p class="login-subtitle">拼团支付市场管理系统</p>
      </div>
      <form class="login-form" @submit.prevent="handleSubmit">
        <div class="field">
          <label>管理员账号</label>
          <input v-model="form.username" placeholder="请输入管理员账号" />
        </div>
        <div class="field">
          <label>管理员密码</label>
          <input v-model="form.password" type="password" placeholder="请输入管理员密码" />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" class="submit-btn">登 录</button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin } from '../../lib/admin'
import { setAdminToken } from '../../lib/admin-auth'

const router = useRouter()
const form = reactive({ username: '', password: '' })
const error = ref('')

async function handleSubmit() {
  const result = await adminLogin(form)
  if (result.code === '0000') {
    setAdminToken(result.data.adminToken)
    await router.replace('/admin/goods')
    return
  }
  error.value = result.info || '登录失败'
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);
}

.login-card {
  width: 420px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.4);
  overflow: hidden;
}

.login-header {
  text-align: center;
  padding: 40px 40px 0;
}

.login-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: #fff;
  margin-bottom: 20px;
}

.login-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.login-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: #94a3b8;
}

.login-form {
  padding: 32px 40px 40px;
  display: flex;
  flex-direction: column;
  gap: 20px;
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
  height: 44px;
  padding: 0 14px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
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
  box-shadow: 0 0 0 3px rgba(129, 140, 248, 0.15);
}

.error {
  color: #ef4444;
  margin: 0;
  font-size: 13px;
  padding: 8px 12px;
  background: #fef2f2;
  border-radius: 8px;
  border: 1px solid #fecaca;
}

.submit-btn {
  height: 48px;
  margin-top: 4px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #818cf8);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  transition: all 0.2s ease;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.45);
}

.submit-btn:active {
  transform: translateY(0);
}
</style>
