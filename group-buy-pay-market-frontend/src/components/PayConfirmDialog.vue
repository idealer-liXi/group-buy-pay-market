<template>
  <div v-if="open" class="payment-overlay">
    <div class="payment-modal">
      <h3>支付确认</h3>
      <p>商品金额：￥{{ amount.toFixed(2) }}</p>
      <div class="modal-buttons">
        <button class="confirm-btn" @click="$emit('confirm')">确认支付</button>
        <button class="cancel-btn" @click="$emit('cancel')">取消支付</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  open: boolean
  amount: number
}>()

defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()
</script>

<style scoped>
.payment-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
}

.payment-modal {
  width: 360px;
  padding: 28px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.2);
}

.modal-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.confirm-btn,
.cancel-btn {
  flex: 1;
  padding: 12px 16px;
  border: none;
  border-radius: 999px;
}

.confirm-btn {
  background: #2563eb;
  color: #fff;
}

.cancel-btn {
  background: #e5e7eb;
  color: #111827;
}
</style>
