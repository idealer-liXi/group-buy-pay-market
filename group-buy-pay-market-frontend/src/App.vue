<template>
  <div class="app-layout">
    <header v-if="showMallHeader" class="app-header">
      <div class="header-left">
        <span class="header-title">拼团商城</span>
      </div>
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </header>
    <RouterView />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCookie, setCookie } from './lib/cookie'

const router = useRouter()
const route = useRoute()
const showMallHeader = computed(() => !route.path.startsWith('/admin') && !!getCookie('loginToken'))

function handleLogout() {
  setCookie('loginToken', '', -1)
  router.replace('/login')
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 48px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.logout-btn {
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 4px 14px;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
}

.logout-btn:hover {
  color: #ef4444;
  border-color: #ef4444;
}
</style>
