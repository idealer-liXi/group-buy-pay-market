<template>
  <div class="history-page">
    <header class="history-header">
      <button class="back-btn" @click="router.push('/goods')">&larr; 返回商品列表</button>
      <h1>购物记录</h1>
      <p>查看当前账号的普通购买和拼团购买记录</p>
    </header>

    <div class="filter-bar">
      <button
        v-for="item in filters"
        :key="item.value"
        :data-filter="item.value"
        :class="['filter-btn', { active: activeFilter === item.value }]"
        @click="activeFilter = item.value"
      >
        {{ item.label }}
      </button>
    </div>

    <div v-if="notice" class="notice">{{ notice }}</div>

    <div v-if="loading" class="state">加载中...</div>
    <div v-else-if="error" class="state error">{{ error }}</div>
    <div v-else-if="filteredRecords.length === 0" class="state">暂无购物记录</div>

    <div v-else class="record-list">
      <article v-for="record in filteredRecords" :key="record.orderId" class="record-card">
        <div class="record-main">
          <div>
            <h2>{{ record.productName }}</h2>
            <p class="order-id">订单号：{{ record.orderId }}</p>
          </div>
          <span :class="['status-badge', record.statusType.toLowerCase()]">
            {{ statusLabel(record.statusType) }}
          </span>
        </div>
        <div class="record-meta">
          <span>{{ purchaseTypeLabel(record.purchaseType) }}</span>
          <span>商品ID：{{ record.productId }}</span>
          <span v-if="record.orderTime">{{ formatTime(record.orderTime) }}</span>
        </div>
        <div class="amount-row">
          <span>订单金额 ￥{{ record.totalAmount.toFixed(2) }}</span>
          <strong>实付 ￥{{ record.payAmount.toFixed(2) }}</strong>
        </div>
        <div v-if="record.statusType === 'WAIT_PAY'" class="record-actions">
          <button class="continue-pay-btn" :disabled="!record.payUrl" @click="continuePay(record)">
            继续支付
          </button>
        </div>
        <div v-if="canRefund(record) && record.statusType !== 'WAIT_PAY'" class="record-actions">
          <button class="refund-btn" :disabled="refundingOrderId === record.orderId || !record.outTradeNo" @click="refund(record)">
            {{ refundingOrderId === record.orderId ? '退单中...' : '退单' }}
          </button>
        </div>
        <div v-if="canRefund(record) && record.statusType === 'WAIT_PAY'" class="record-actions secondary-actions">
          <button class="refund-btn" :disabled="refundingOrderId === record.orderId || !record.outTradeNo" @click="refund(record)">
            {{ refundingOrderId === record.orderId ? '退单中...' : '退单' }}
          </button>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCookie } from '../lib/cookie'
import { openNotificationSocket } from '../lib/notificationSocket'
import { queryPurchaseHistory, refundMarketPayOrder } from '../lib/order'
import { injectPayFormHtml } from '../lib/pay'
import type { PurchaseRecord } from '../types/api'

type FilterType = 'ALL' | PurchaseRecord['statusType']

const router = useRouter()
const records = ref<PurchaseRecord[]>([])
const loading = ref(true)
const error = ref('')
const notice = ref('')
const activeFilter = ref<FilterType>('ALL')
const refundingOrderId = ref('')
let notificationSocket: WebSocket | null = null

const filters: Array<{ value: FilterType; label: string }> = [
  { value: 'ALL', label: '全部' },
  { value: 'WAIT_PAY', label: '待付款' },
  { value: 'GROUP_WAIT', label: '拼团中' },
  { value: 'GROUP_SUCCESS', label: '已完成' },
  { value: 'CLOSED', label: '已关闭' }
]

const filteredRecords = computed(() => {
  if (activeFilter.value === 'ALL') {
    return records.value
  }
  return records.value.filter((record) => record.statusType === activeFilter.value)
})

onMounted(async () => {
  const loginToken = getCookie('loginToken')
  if (!loginToken) {
    await router.replace('/login')
    return
  }

  try {
    notificationSocket = openNotificationSocket(loginToken, async (message) => {
      notice.value = message.message || '拼团已完成'
      await loadRecords(loginToken)
    })
  } catch {
    // WebSocket is only a real-time enhancement; history loading must still work.
  }

  try {
    await loadRecords(loginToken)
  } catch {
    error.value = '网络异常，请稍后重试'
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  notificationSocket?.close()
  notificationSocket = null
})

async function loadRecords(userId: string) {
  const result = await queryPurchaseHistory(userId)
  if (result.code === '0000') {
    records.value = result.data.recordList
  } else {
    error.value = result.info || '获取购物记录失败'
  }
}

function statusLabel(statusType: PurchaseRecord['statusType']) {
  return {
    WAIT_PAY: '待付款',
    GROUP_WAIT: '拼团中',
    GROUP_SUCCESS: '已完成',
    CLOSED: '已关闭'
  }[statusType]
}

function purchaseTypeLabel(purchaseType: PurchaseRecord['purchaseType']) {
  return purchaseType === 'GROUP_BUY' ? '拼团购买' : '普通购买'
}

function formatTime(value: string) {
  return new Date(value).toLocaleString()
}

function continuePay(record: PurchaseRecord) {
  if (!record.payUrl) {
    return
  }

  injectPayFormHtml(record.payUrl)
  const form = document.querySelector('form') as HTMLFormElement | null
  form?.submit()
}

function canRefund(record: PurchaseRecord) {
  return record.purchaseType === 'GROUP_BUY' && record.statusType !== 'CLOSED'
}

async function refund(record: PurchaseRecord) {
  const loginToken = getCookie('loginToken')
  if (!loginToken || !record.outTradeNo) {
    return
  }

  refundingOrderId.value = record.orderId
  notice.value = ''
  try {
    const result = await refundMarketPayOrder({
      userId: loginToken,
      outTradeNo: record.outTradeNo,
      source: 's01',
      channel: 'c01'
    })
    notice.value = result.data?.info || result.info || '退单已提交'
    if (result.code === '0000') {
      await loadRecords(loginToken)
    }
  } catch {
    notice.value = '退单失败，请稍后重试'
  } finally {
    refundingOrderId.value = ''
  }
}
</script>

<style scoped>
.history-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px 80px;
}

.history-header {
  margin-bottom: 18px;
}

.back-btn {
  background: none;
  border: none;
  color: #2563eb;
  cursor: pointer;
  padding: 8px 0;
  margin-bottom: 10px;
}

.history-header h1 {
  font-size: 24px;
  color: #111827;
  margin-bottom: 6px;
}

.history-header p {
  color: #6b7280;
  font-size: 14px;
}

.filter-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.filter-btn {
  border: 1px solid #d1d5db;
  background: #fff;
  border-radius: 999px;
  padding: 8px 16px;
  color: #374151;
  cursor: pointer;
}

.filter-btn.active {
  border-color: #2563eb;
  background: #eff6ff;
  color: #2563eb;
  font-weight: 600;
}

.state {
  text-align: center;
  padding: 56px 0;
  color: #6b7280;
}

.notice {
  border-radius: 10px;
  background: #ecfdf5;
  color: #047857;
  padding: 10px 12px;
  margin-bottom: 14px;
}

.state.error {
  color: #dc2626;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.record-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.08);
  padding: 18px;
}

.record-main,
.amount-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.record-main h2 {
  font-size: 17px;
  color: #111827;
  margin-bottom: 6px;
}

.order-id,
.record-meta {
  color: #6b7280;
  font-size: 13px;
}

.record-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin: 12px 0;
}

.status-badge {
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  white-space: nowrap;
}

.status-badge.wait_pay {
  background: #fff7ed;
  color: #ea580c;
}

.status-badge.group_wait {
  background: #eff6ff;
  color: #2563eb;
}

.status-badge.group_success {
  background: #ecfdf5;
  color: #059669;
}

.status-badge.closed {
  background: #f3f4f6;
  color: #4b5563;
}

.amount-row {
  color: #4b5563;
  font-size: 14px;
  align-items: baseline;
}

.amount-row strong {
  color: #dc2626;
  font-size: 18px;
}

.record-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.continue-pay-btn {
  border: none;
  border-radius: 999px;
  background: #2563eb;
  color: #fff;
  cursor: pointer;
  padding: 8px 18px;
  font-weight: 600;
}

.continue-pay-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.secondary-actions {
  margin-top: 8px;
}

.refund-btn {
  border: 1px solid #dc2626;
  border-radius: 999px;
  background: #fff;
  color: #dc2626;
  cursor: pointer;
  padding: 8px 18px;
  font-weight: 600;
}

.refund-btn:disabled {
  border-color: #9ca3af;
  color: #9ca3af;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .record-main,
  .amount-row {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
