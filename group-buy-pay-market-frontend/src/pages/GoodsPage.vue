<template>
  <div class="goods-page">
    <div class="goods-container" v-if="marketData && !isPlainGoods">
      <button class="back-btn" @click="router.push('/goods')">&larr; 返回商品列表</button>

      <section class="hero">
        <div class="gallery">
          <GoodsNameCover :title="currentGoodsName" size="hero" />
        </div>
        <div class="product-info">
          <div class="product-badge">拼团优惠</div>
          <h1 class="product-title">{{ currentGoodsName }}</h1>
          <p class="promo-copy">
            🔥 直降 ¥{{ marketData.goods.deductionPrice.toFixed(0) }}，{{ marketData.teamStatistic.allTeamUserCount }}人正在抢购，参与马上到手
          </p>
          <div class="price-card">
            <div class="price-row">
              <span class="price-label">拼团价</span>
              <span class="pay-price">￥{{ marketData.goods.payPrice.toFixed(2) }}</span>
            </div>
            <div class="price-row secondary">
              <span class="price-label">原价</span>
              <span class="original-price">￥{{ marketData.goods.originalPrice.toFixed(2) }}</span>
            </div>
            <div class="save-tag">省 ￥{{ marketData.goods.deductionPrice.toFixed(2) }}</div>
          </div>
        </div>
      </section>

      <section class="teams-section">
        <div class="teams-header">
          <h2>正在拼团</h2>
          <span class="teams-count">{{ marketData.teamStatistic.allTeamCount }} 个团进行中</span>
        </div>
        <GroupTeamList :teams="teamSummaries" @join="handleJoinTeam" />
      </section>

      <footer class="action-bar">
        <button class="action-btn buy-alone" @click="handleSingleBuy">
          <span class="btn-label">单独购买</span>
          <span class="btn-price">￥{{ marketData.goods.originalPrice.toFixed(2) }}</span>
        </button>
        <button class="action-btn group-buy" @click="handleStartTeam">
          <span class="btn-label">开团购买</span>
          <span class="btn-price">￥{{ marketData.goods.payPrice.toFixed(2) }}</span>
        </button>
      </footer>

      <PayConfirmDialog
        :open="payDialog.open"
        :amount="payDialog.amount"
        @confirm="submitPayForm"
        @cancel="closePayDialog"
      />
    </div>

    <div v-else-if="marketData && isPlainGoods" class="goods-container">
      <button class="back-btn" @click="router.push('/goods')">&larr; 返回商品列表</button>

      <section class="hero fallback-hero">
        <div class="gallery">
          <GoodsNameCover :title="currentGoodsName" size="hero" />
        </div>
        <div class="product-info">
          <div class="product-badge product-badge-muted">普通商品</div>
          <h1 class="product-title">{{ currentGoodsName }}</h1>
          <p class="promo-copy promo-copy-muted">当前商品暂未配置拼团活动</p>
          <div class="price-card">
            <div class="price-row">
              <span class="price-label">售价</span>
              <span class="pay-price">￥{{ marketData.goods.originalPrice.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </section>

      <footer class="action-bar">
        <button class="action-btn plain-buy" @click="handleSingleBuy">
          <span class="btn-label">原价购买</span>
          <span class="btn-price">￥{{ marketData.goods.originalPrice.toFixed(2) }}</span>
        </button>
      </footer>

      <PayConfirmDialog
        :open="payDialog.open"
        :amount="payDialog.amount"
        @confirm="submitPayForm"
        @cancel="closePayDialog"
      />
    </div>

    <div v-else-if="pageError" class="goods-container error-shell">
      <button class="back-btn" @click="router.push('/goods')">&larr; 返回商品列表</button>
      <div class="loading-state">
        <span>{{ pageError }}</span>
      </div>
    </div>

    <div v-else class="loading-state">
      <div class="loading-spinner"></div>
      <span>正在加载商品信息...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import GoodsNameCover from '../components/GoodsNameCover.vue'
import GroupTeamList from '../components/GroupTeamList.vue'
import PayConfirmDialog from '../components/PayConfirmDialog.vue'
import { getCookie } from '../lib/cookie'
import { queryGroupBuyMarketConfig, querySkuList, resolveGoodsName, toTeamSummaries } from '../lib/market'
import { createPayOrder, injectPayFormHtml } from '../lib/pay'
import type { GoodsMarketResponse, SkuItem } from '../types/api'

const route = useRoute()
const router = useRouter()
const marketData = ref<GoodsMarketResponse | null>(null)
const skuList = ref<SkuItem[]>([])
const pageError = ref('')
const payDialog = ref({ open: false, amount: 0, html: '' })
const currentTimeMs = ref(Date.now())
let countdownTimer: ReturnType<typeof window.setInterval> | null = null

const currentGoodsId = computed(() => String(route.params.goodsId ?? ''))

const currentGoodsName = computed(() => resolveGoodsName(currentGoodsId.value, skuList.value))

const isPlainGoods = computed(() => marketData.value?.activityId == null)

const teamSummaries = computed(() => {
  if (!marketData.value) {
    return []
  }
  return toTeamSummaries(marketData.value.teamList, currentTimeMs.value)
})

onMounted(async () => {
  countdownTimer = window.setInterval(() => {
    currentTimeMs.value = Date.now()
  }, 1000)

  const loginToken = getCookie('loginToken')
  if (!loginToken) {
    await router.replace('/login')
    return
  }

  const [marketResult, skuResult] = await Promise.allSettled([
    queryGroupBuyMarketConfig({
      userId: loginToken,
      source: 's01',
      channel: 'c01',
      goodsId: currentGoodsId.value
    }),
    querySkuList()
  ])

  if (marketResult.status === 'fulfilled' && marketResult.value.code === '0000') {
    marketData.value = marketResult.value.data
  } else if (marketResult.status === 'fulfilled') {
    pageError.value = marketResult.value.info || '商品暂无拼团活动'
  } else {
    pageError.value = '商品信息加载失败，请稍后重试'
  }

  if (skuResult.status === 'fulfilled' && skuResult.value.code === '0000') {
    skuList.value = skuResult.value.data.skuList
  }
})

onUnmounted(() => {
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
  }
})

async function handleSingleBuy() {
  await requestPay(0, null)
}

async function handleStartTeam() {
  await requestPay(1, null)
}

async function handleJoinTeam(teamId: string) {
  await requestPay(1, teamId)
}

async function requestPay(marketType: number, teamId: string | null) {
  const loginToken = getCookie('loginToken')
  if (!loginToken || !marketData.value) {
    return
  }

  const goods = marketData.value.goods
  const activityId = marketType === 1 && marketData.value.activityId != null ? marketData.value.activityId : undefined
  const result = await createPayOrder({
    userId: loginToken,
    productId: goods.goodsId,
    activityId,
    teamId,
    marketType
  })

  if (result.code === '0000') {
    payDialog.value = {
      open: true,
      amount: marketType === 1 ? goods.payPrice : goods.originalPrice,
      html: result.data
    }
  }
}

function submitPayForm() {
  injectPayFormHtml(payDialog.value.html)
  const form = document.querySelector('form') as HTMLFormElement | null
  if (form) {
    form.submit()
  }
  closePayDialog()
}

function closePayDialog() {
  payDialog.value = { open: false, amount: 0, html: '' }
}
</script>

<style scoped>
.goods-page {
  min-height: 100vh;
  padding: 24px 0 120px;
  background: #f5f7fa;
}

.goods-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 16px;
}

.back-btn {
  background: none;
  border: none;
  color: #2563eb;
  font-size: 14px;
  cursor: pointer;
  padding: 8px 0;
  margin-bottom: 16px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.back-btn:hover {
  color: #1d4ed8;
}

.hero {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.gallery {
  display: flex;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.fallback-hero {
  margin-bottom: 0;
}

.product-info {
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  padding: 28px;
  display: flex;
  flex-direction: column;
}

.product-badge {
  display: inline-block;
  background: linear-gradient(135deg, #ef4444, #f97316);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
  width: fit-content;
  margin-bottom: 16px;
}

.product-badge-muted {
  background: linear-gradient(135deg, #64748b, #94a3b8);
}

.product-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  line-height: 1.4;
  margin-bottom: 12px;
}

.promo-copy {
  color: #dc2626;
  font-size: 14px;
  margin-bottom: 20px;
}

.promo-copy-muted {
  color: #475569;
}

.price-card {
  background: linear-gradient(135deg, #fef2f2, #fff1f2);
  border-radius: 12px;
  padding: 20px;
  margin-top: auto;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price-row.secondary {
  margin-top: 8px;
}

.price-label {
  font-size: 13px;
  color: #6b7280;
  min-width: 48px;
}

.pay-price {
  font-size: 36px;
  font-weight: 700;
  color: #dc2626;
}

.original-price {
  color: #9ca3af;
  text-decoration: line-through;
  font-size: 16px;
}

.save-tag {
  display: inline-block;
  margin-top: 12px;
  background: #dc2626;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 4px;
}

.teams-section {
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  padding: 24px;
}

.teams-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.teams-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.teams-count {
  font-size: 13px;
  color: #6b7280;
}

.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  border-top: 1px solid #e5e7eb;
}

.action-btn {
  min-width: 220px;
  padding: 14px 28px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.btn-label {
  font-size: 15px;
}

.btn-price {
  font-size: 13px;
  opacity: 0.9;
}

.buy-alone {
  background: #374151;
}

.plain-buy {
  background: #374151;
}

.group-buy {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  gap: 16px;
  color: #6b7280;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e5e7eb;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .goods-page {
    padding: 16px 0 104px;
  }

  .goods-container {
    padding: 0 12px;
  }

  .hero {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .product-info,
  .teams-section {
    padding: 20px;
  }

  .teams-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }

  .action-bar {
    gap: 12px;
    padding: 12px;
  }

  .action-btn {
    min-width: 0;
    flex: 1;
    padding: 14px 12px;
  }
}
</style>
