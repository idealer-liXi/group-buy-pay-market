<template>
  <div class="login-container">
    <h1>微信扫码登录</h1>
    <div class="qr-code">
      <img v-if="qrImageUrl" :src="qrImageUrl" alt="微信二维码" />
      <div v-else class="qr-placeholder">二维码加载中...</div>
    </div>
    <p class="instructions">{{ statusText }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  qrImageUrl: string
  statusText: string
}>()
</script>

<style scoped>
.login-container {
  width: 420px;
  padding: 40px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.qr-code {
  margin: 24px 0;
}

.qr-code img,
.qr-placeholder {
  width: 220px;
  height: 220px;
  margin: 0 auto;
}

.qr-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  color: #6b7280;
}

.instructions {
  color: #6b7280;
}
</style>
