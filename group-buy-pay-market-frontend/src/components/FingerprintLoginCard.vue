<template>
  <div class="fingerprint-login">
    <div class="fingerprint-icon">
      <svg viewBox="0 0 64 64" width="80" height="80" fill="none" stroke="#6b7280" stroke-width="2">
        <path d="M32 4C17.6 4 6 15.6 6 30s11.6 26 26 26" />
        <path d="M32 10c-11 0-20 9-20 20s9 20 20 20" />
        <path d="M32 16c-7.7 0-14 6.3-14 14s6.3 14 14 14" />
        <path d="M32 22c-4.4 0-8 3.6-8 8s3.6 8 8 8" />
        <path d="M32 28c-1.1 0-2 .9-2 2s.9 2 2 2" />
        <path d="M38 30c0-3.3-2.7-6-6-6" />
        <path d="M26 30c0 3.3 2.7 6 6 6" />
      </svg>
    </div>
    <p class="desc">使用浏览器指纹快速登录，无需扫码</p>
    <button class="fingerprint-btn" @click="$emit('login')" :disabled="loading">
      {{ loading ? '登录中...' : '无痕登录' }}
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  loading: boolean
}>()

defineEmits<{
  login: []
}>()
</script>

<style scoped>
.fingerprint-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.fingerprint-icon {
  margin-bottom: 8px;
}

.desc {
  color: #6b7280;
  font-size: 14px;
}

.fingerprint-btn {
  background-color: #3b82f6;
  color: #fff;
  border: none;
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.fingerprint-btn:hover:not(:disabled) {
  background-color: #2563eb;
}

.fingerprint-btn:disabled {
  background-color: #93c5fd;
  cursor: not-allowed;
}
</style>
