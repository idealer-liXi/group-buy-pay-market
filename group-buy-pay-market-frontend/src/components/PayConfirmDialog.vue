<template>
  <div v-if="open" class="payment-overlay">
    <div class="payment-modal" role="dialog" aria-modal="true">
      <div class="modal-icon">{{ icon }}</div>
      <h3>{{ title }}</h3>
      <p>{{ message || `商品金额：￥${amount.toFixed(2)}` }}</p>
      <div class="modal-buttons">
        <button class="confirm-btn" @click="$emit('confirm')">{{ confirmText }}</button>
        <button v-if="showCancel" class="cancel-btn" @click="$emit('cancel')">{{ cancelText }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  open: boolean
  amount: number
  title?: string
  message?: string
  confirmText?: string
  cancelText?: string
  showCancel?: boolean
  icon?: string
}>(), {
  title: '支付确认',
  message: '',
  confirmText: '确认支付',
  cancelText: '取消支付',
  showCancel: true,
  icon: '¥'
})

defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()
</script>

<style scoped>
.payment-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.52);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
}

.payment-modal {
  box-sizing: border-box;
  width: min(390px, calc(100vw - 32px));
  padding: 30px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.25);
  text-align: center;
}

.modal-icon {
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
  border-radius: 18px;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 800;
}

.payment-modal h3 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
}

.payment-modal p {
  margin: 10px 0 0;
  color: #475569;
  font-size: 15px;
  line-height: 1.6;
}

.modal-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.confirm-btn,
.cancel-btn {
  flex: 1;
  padding: 12px 16px;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
}

.confirm-btn {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: #fff;
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.28);
}

.cancel-btn {
  background: #f1f5f9;
  color: #334155;
}
</style>
