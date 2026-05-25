<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="title">欢迎登录 - 拼团商城</h1>

      <template v-if="!loginMode">
        <p class="subtitle">请选择登录方式</p>
        <div class="mode-buttons">
          <button class="mode-btn weixin" @click="selectWeixin">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="#07c160">
              <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05a6.077 6.077 0 01-.259-1.736c0-3.678 3.292-6.66 7.352-6.66.307 0 .609.023.909.053C16.588 4.922 13.003 2.188 8.691 2.188zm5.396 13.414c-.122 0-.237-.035-.34-.098l-1.382-.81a.628.628 0 00-.522-.072 7.413 7.413 0 01-2.067.293c-3.243 0-5.877-2.236-5.877-4.993S6.533 4.929 9.776 4.929s5.878 2.236 5.878 4.993c0 1.13-.463 2.172-1.24 3.003a.429.429 0 00-.155.484l.284 1.078c.014.051.035.103.035.155 0 .119-.099.216-.216.216a.264.264 0 01-.137-.044l-.138-.082z"/>
              <path d="M23.935 14.98c0-3.258-3.07-5.9-6.856-5.9s-6.856 2.642-6.856 5.9 3.07 5.9 6.856 5.9c.714 0 1.402-.103 2.051-.293a.627.627 0 01.521.072l1.09.638a.264.264 0 00.137.044c.117 0 .216-.097.216-.216 0-.052-.02-.104-.035-.155l-.224-.85a.429.429 0 01.155-.485c.617-.662.945-1.462.945-2.655zm-9.344-1.098a.808.808 0 110-1.616.808.808 0 010 1.616zm4.976 0a.808.808 0 110-1.616.808.808 0 010 1.616z"/>
            </svg>
            微信扫码登录
          </button>
          <button class="mode-btn fingerprint" @click="selectFingerprint">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="#3b82f6">
              <path d="M17.81 4.47c-.08 0-.16-.02-.23-.06C15.66 3.42 14 3 12.01 3c-1.98 0-3.86.47-5.57 1.41-.24.13-.54.04-.68-.2-.13-.24-.04-.55.2-.68C7.82 2.52 9.86 2 12.01 2c2.13 0 3.99.47 6.03 1.52.25.13.34.43.21.67-.09.18-.26.28-.44.28zM3.5 9.72a.499.499 0 01-.41-.79c.99-1.4 2.25-2.5 3.75-3.27C9.98 4.04 14 4.03 17.15 5.65c1.5.77 2.76 1.86 3.75 3.25.16.22.11.54-.12.7-.23.16-.54.11-.7-.12a9.388 9.388 0 00-3.39-2.94c-2.87-1.47-6.54-1.47-9.4.01-1.36.7-2.5 1.7-3.4 2.96-.08.14-.23.21-.39.21zm6.25 12.07a.47.47 0 01-.35-.15c-.87-.87-1.34-1.43-2.01-2.64-.69-1.23-1.05-2.73-1.05-4.34 0-2.97 2.54-5.39 5.66-5.39s5.66 2.42 5.66 5.39c0 .28-.22.5-.5.5s-.5-.22-.5-.5c0-2.42-2.09-4.39-4.66-4.39-2.57 0-4.66 1.97-4.66 4.39 0 1.44.32 2.77.93 3.85.64 1.15 1.08 1.64 1.85 2.42.19.2.19.51 0 .71-.11.1-.24.15-.37.15zm7.17-1.85c-1.19 0-2.24-.3-3.1-.89-1.49-1.01-2.38-2.65-2.38-4.39 0-.28.22-.5.5-.5s.5.22.5.5c0 1.41.72 2.74 1.94 3.56.71.48 1.54.71 2.54.71.24 0 .64-.03 1.04-.1.27-.05.53.13.58.41.05.27-.13.53-.41.58-.57.11-1.07.12-1.21.12zM14.91 22c-.04 0-.09-.01-.13-.02-1.59-.44-2.63-1.03-3.72-2.1a7.297 7.297 0 01-2.17-5.22c0-1.62 1.38-2.94 3.08-2.94 1.7 0 3.08 1.32 3.08 2.94 0 1.07.93 1.94 2.08 1.94s2.08-.87 2.08-1.94c0-3.77-3.25-6.83-7.25-6.83-2.84 0-5.44 1.58-6.61 4.03-.39.81-.59 1.76-.59 2.8 0 .78.07 2.01.67 3.61.1.26-.03.55-.29.64-.26.1-.55-.04-.64-.29a11.73 11.73 0 01-.73-3.96c0-1.2.23-2.29.68-3.24 1.33-2.79 4.28-4.6 7.51-4.6 4.55 0 8.25 3.51 8.25 7.83 0 1.62-1.38 2.94-3.08 2.94s-3.08-1.32-3.08-2.94c0-1.07-.93-1.94-2.08-1.94s-2.08.87-2.08 1.94c0 1.71.66 3.31 1.87 4.51.95.94 1.86 1.46 3.27 1.85.27.07.42.35.35.61-.05.23-.27.37-.48.37z"/>
            </svg>
            无痕登录
          </button>
        </div>
      </template>

      <template v-else-if="loginMode === 'weixin'">
        <QrLoginCard :qr-image-url="qrImageUrl" :status-text="statusText" />
        <button class="back-btn" @click="goBack">返回选择登录方式</button>
      </template>

      <template v-else>
        <FingerprintLoginCard :loading="fingerprintLoading" @login="handleFingerprintLogin" />
        <button class="back-btn" @click="goBack">返回选择登录方式</button>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import QrLoginCard from '../components/QrLoginCard.vue'
import FingerprintLoginCard from '../components/FingerprintLoginCard.vue'
import { checkLogin, fetchWeixinQrCodeTicket, getFingerprint } from '../lib/auth'
import { setCookie } from '../lib/cookie'

const router = useRouter()
const loginMode = ref<'weixin' | 'fingerprint' | ''>('')
const qrImageUrl = ref('')
const statusText = ref('')
const fingerprintLoading = ref(false)
let timer: number | undefined

async function selectWeixin() {
  loginMode.value = 'weixin'
  statusText.value = '正在获取二维码...'
  try {
    const visitorId = await getFingerprint()
    const sceneStr = visitorId.toUpperCase()
    const result = await fetchWeixinQrCodeTicket(sceneStr)
    if (result.code !== '0000') {
      statusText.value = `二维码获取失败：${result.info}`
      return
    }
    const ticket = result.data
    qrImageUrl.value = `https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=${ticket}`
    statusText.value = '请使用微信扫描二维码登录'
    timer = window.setInterval(async () => {
      const loginResult = await checkLogin(ticket, sceneStr)
      if (loginResult.code === '0000') {
        setCookie('loginToken', loginResult.data, 30)
        statusText.value = '登录成功，正在跳转...'
        if (timer) window.clearInterval(timer)
        router.replace('/goods/9890001')
      }
    }, 3000)
  } catch {
    statusText.value = '二维码获取失败：网络异常'
  }
}

async function selectFingerprint() {
  loginMode.value = 'fingerprint'
}

async function handleFingerprintLogin() {
  fingerprintLoading.value = true
  try {
    const visitorId = await getFingerprint()
    setCookie('loginToken', visitorId, 20)
    router.replace('/goods/9890001')
  } finally {
    fingerprintLoading.value = false
  }
}

function goBack() {
  if (timer) {
    window.clearInterval(timer)
    timer = undefined
  }
  loginMode.value = ''
  qrImageUrl.value = ''
  statusText.value = ''
}

onMounted(() => {
  // wait for user choice
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #dbe6f6 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.title {
  margin-bottom: 24px;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.subtitle {
  color: #6b7280;
  margin-bottom: 24px;
}

.mode-buttons {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mode-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 24px;
  border-radius: 12px;
  font-size: 16px;
  cursor: pointer;
  border: 2px solid #e5e7eb;
  background: #fff;
  color: #333;
  transition: all 0.2s ease;
}

.mode-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.mode-btn.weixin:hover {
  border-color: #07c160;
  color: #07c160;
}

.mode-btn.fingerprint:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}

.back-btn {
  margin-top: 20px;
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  color: #333;
}
</style>
