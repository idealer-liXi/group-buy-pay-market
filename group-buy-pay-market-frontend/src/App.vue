<template>
  <div class="app-layout">
    <header v-if="showMallHeader" class="app-header">
      <div class="header-left">
        <span class="header-title">拼团商城</span>
      </div>
      <div class="header-right">
        <span v-if="userTagsText" class="user-tags">我的标签：{{ userTagsText }}</span>
        <span v-else-if="showMallHeader" class="user-tags user-tags-muted">暂无标签</span>
        <div class="notification-area">
          <button data-testid="notification-button" class="notification-btn" @click="toggleNotificationPanel">
            <span class="notification-btn-icon">铃</span>
            <span>消息</span>
            <span v-if="unreadCount > 0" data-testid="notification-badge" class="notification-badge">{{ unreadCount }}</span>
          </button>
          <section v-if="notificationPanelOpen" class="notification-panel">
            <div class="notification-panel-header">
              <div>
                <strong>消息中心</strong>
                <p>{{ unreadCount > 0 ? `${unreadCount} 条未读消息` : '订单动态会在这里同步' }}</p>
              </div>
              <button v-if="notifications.length" data-testid="mark-all-read" @click="markAllRead">全部已读</button>
            </div>
            <p v-if="notifications.length === 0" class="notification-empty">暂无消息</p>
            <button
              v-for="item in notifications"
              :key="item.id"
              :class="['notification-item', { unread: !item.read }]"
              @click="openNotification(item.id)"
            >
              <span class="notification-dot" aria-hidden="true"></span>
              <span class="notification-item-body">
                <span>{{ item.message }}</span>
                <small>{{ item.orderId ? `订单 ${item.orderId}` : notificationTypeText(item.type) }} · {{ formatNotificationTime(item.receivedAt) }}</small>
              </span>
            </button>
            <button v-if="notifications.length" class="clear-notifications" @click="clearNotifications">清空消息</button>
          </section>
        </div>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </header>
    <div v-if="bannerMessage" class="notification-banner">
      <span class="notification-banner-icon">✓</span>
      <span>{{ bannerMessage }}</span>
    </div>
    <RouterView />
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCookie, setCookie } from './lib/cookie'
import { openNotificationSocket } from './lib/notificationSocket'
import { queryUserTags } from './lib/user'
import type { UserNotificationMessage } from './types/api'

type StoredNotification = UserNotificationMessage & {
  id: string
  receivedAt: number
  read: boolean
}

const router = useRouter()
const route = useRoute()
const showMallHeader = computed(() => !route.path.startsWith('/admin') && !!getCookie('loginToken'))
const userTags = ref<string[]>([])
const userTagsText = computed(() => userTags.value.join('、'))
const notifications = ref<StoredNotification[]>([])
const notificationPanelOpen = ref(false)
const bannerMessage = ref('')
const unreadCount = computed(() => notifications.value.filter((item) => !item.read).length)

let notificationSocket: WebSocket | null = null
let bannerTimer: ReturnType<typeof setTimeout> | null = null

watch(showMallHeader, async (visible) => {
  const userId = getCookie('loginToken')
  if (!visible || !userId) {
    userTags.value = []
    notifications.value = []
    closeNotificationSocket()
    return
  }
  loadNotifications(userId)
  openAppNotificationSocket(userId)
  try {
    const result = await queryUserTags(userId)
    userTags.value = result.code === '0000' ? result.data.tagList.map((tag) => tag.tagName).filter(Boolean) : []
  } catch {
    userTags.value = []
  }
}, { immediate: true })

onUnmounted(() => {
  closeNotificationSocket()
  if (bannerTimer) {
    clearTimeout(bannerTimer)
  }
})

function handleLogout() {
  const userId = getCookie('loginToken')
  if (userId) {
    localStorage.removeItem(storageKey(userId))
  }
  closeNotificationSocket()
  setCookie('loginToken', '', -1)
  router.replace('/login')
}

function openAppNotificationSocket(userId: string) {
  closeNotificationSocket()
  try {
    notificationSocket = openNotificationSocket(userId, (message) => handleNotification(userId, message))
  } catch {
    notificationSocket = null
  }
}

function closeNotificationSocket() {
  notificationSocket?.close()
  notificationSocket = null
}

function handleNotification(userId: string, message: UserNotificationMessage) {
  const stored: StoredNotification = {
    ...message,
    message: message.message || notificationTypeText(message.type),
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    receivedAt: Date.now(),
    read: false
  }
  notifications.value = [stored, ...notifications.value].slice(0, 50)
  saveNotifications(userId)
  showBanner(stored.message)
  window.dispatchEvent(new CustomEvent('gbpm:user-notification', { detail: message }))
}

function loadNotifications(userId: string) {
  try {
    const raw = localStorage.getItem(storageKey(userId))
    notifications.value = raw ? JSON.parse(raw) : []
  } catch {
    notifications.value = []
  }
}

function saveNotifications(userId = getCookie('loginToken')) {
  if (!userId) {
    return
  }
  localStorage.setItem(storageKey(userId), JSON.stringify(notifications.value))
}

function storageKey(userId: string) {
  return `gbpm_notifications_${userId}`
}

function showBanner(message: string) {
  bannerMessage.value = message
  if (bannerTimer) {
    clearTimeout(bannerTimer)
  }
  bannerTimer = setTimeout(() => {
    bannerMessage.value = ''
  }, 4000)
}

function toggleNotificationPanel() {
  notificationPanelOpen.value = !notificationPanelOpen.value
}

function markAllRead() {
  notifications.value = notifications.value.map((item) => ({ ...item, read: true }))
  saveNotifications()
}

function clearNotifications() {
  notifications.value = []
  saveNotifications()
}

function openNotification(id: string) {
  notifications.value = notifications.value.map((item) => item.id === id ? { ...item, read: true } : item)
  saveNotifications()
  router.push('/orders')
}

function notificationTypeText(type: string) {
  return {
    PAY_SUCCESS: '支付成功',
    REFUND_SUCCESS: '退单成功',
    GROUP_SUCCESS: '拼团已完成',
    GROUP_FAIL: '拼团失败'
  }[type] || '订单状态更新'
}

function formatNotificationTime(value: number) {
  return new Date(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
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

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-area {
  position: relative;
}

.notification-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid #dbeafe;
  background: linear-gradient(135deg, #eff6ff 0%, #eef2ff 100%);
  color: #1d4ed8;
  border-radius: 999px;
  padding: 6px 15px 6px 8px;
  cursor: pointer;
  font-weight: 600;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.12);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.notification-btn:hover {
  border-color: #93c5fd;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.18);
  transform: translateY(-1px);
}

.notification-btn-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 999px;
  background: #2563eb;
  color: #fff;
  font-size: 12px;
}

.notification-badge {
  position: absolute;
  top: -7px;
  right: -7px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 12px;
  line-height: 18px;
}

.notification-panel {
  position: absolute;
  top: 42px;
  right: 0;
  z-index: 200;
  width: 340px;
  max-height: 420px;
  overflow: hidden auto;
  padding: 14px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 20px;
  box-shadow: 0 22px 60px rgba(15, 23, 42, 0.18);
  backdrop-filter: blur(14px);
}

.notification-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  padding: 2px 2px 10px;
  border-bottom: 1px solid #eef2f7;
}

.notification-panel-header strong {
  display: block;
  color: #0f172a;
  font-size: 15px;
}

.notification-panel-header p {
  margin-top: 3px;
  color: #64748b;
  font-size: 12px;
}

.notification-panel-header button,
.clear-notifications {
  border: none;
  background: transparent;
  color: #2563eb;
  cursor: pointer;
  font-weight: 600;
}

.notification-empty {
  margin: 10px 0 4px;
  padding: 22px 0;
  color: #94a3b8;
  font-size: 13px;
  text-align: center;
}

.notification-item {
  display: flex;
  width: 100%;
  gap: 10px;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid transparent;
  border-radius: 14px;
  background: #f8fafc;
  text-align: left;
  cursor: pointer;
  margin-bottom: 8px;
  transition: background 0.18s ease, border-color 0.18s ease, transform 0.18s ease;
}

.notification-item:hover {
  border-color: #bfdbfe;
  background: #f1f5ff;
  transform: translateY(-1px);
}

.notification-item.unread {
  border-color: #bfdbfe;
  background: linear-gradient(135deg, #eff6ff 0%, #eef2ff 100%);
}

.notification-dot {
  width: 9px;
  height: 9px;
  margin-top: 5px;
  border-radius: 999px;
  background: #cbd5e1;
  flex: 0 0 auto;
}

.notification-item.unread .notification-dot {
  background: #2563eb;
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.12);
}

.notification-item-body {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 5px;
}

.notification-item-body > span {
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.45;
}

.notification-item small {
  color: #64748b;
}

.clear-notifications {
  display: block;
  margin: 4px auto 0;
  padding: 6px 10px;
}

.notification-banner {
  position: fixed;
  top: 60px;
  left: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transform: translateX(-50%);
  z-index: 300;
  min-width: 260px;
  max-width: min(520px, calc(100vw - 32px));
  padding: 13px 18px;
  border: 1px solid rgba(187, 247, 208, 0.85);
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(240, 253, 244, 0.98) 0%, rgba(236, 253, 245, 0.98) 100%);
  color: #065f46;
  text-align: center;
  font-weight: 700;
  box-shadow: 0 18px 42px rgba(16, 185, 129, 0.2);
  backdrop-filter: blur(12px);
}

.notification-banner-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 999px;
  background: #10b981;
  color: #fff;
  flex: 0 0 auto;
}

.user-tags {
  max-width: 46vw;
  color: #2563eb;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-tags-muted {
  color: #94a3b8;
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

@media (max-width: 640px) {
  .app-header {
    padding: 0 12px;
  }

  .header-right {
    gap: 8px;
  }

  .notification-panel {
    position: fixed;
    top: 56px;
    right: 12px;
    left: 12px;
    width: auto;
  }
}
</style>
