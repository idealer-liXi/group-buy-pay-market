<template>
  <div class="goods-page">
    <div class="goods-container" v-if="marketData && !isPlainGoods">
      <button class="back-btn" @click="router.push('/goods')">&larr; 返回商品列表</button>

      <section class="hero">
        <div class="gallery">
          <div class="carousel-main">
            <GoodsNameCover :title="currentGoodsName" :image-url="currentGalleryImageUrl" size="hero" />
            <button v-if="galleryImageUrls.length > 1" class="carousel-btn carousel-prev" type="button" aria-label="上一张商品图" @click="showPrevImage">‹</button>
            <button v-if="galleryImageUrls.length > 1" class="carousel-btn carousel-next" type="button" aria-label="下一张商品图" @click="showNextImage">›</button>
          </div>
          <div v-if="galleryImageUrls.length > 1" class="gallery-thumbs">
            <button
              v-for="(imageUrl, index) in galleryImageUrls"
              :key="imageUrl"
              :class="['gallery-thumb-btn', selectedImageIndex === index ? 'gallery-thumb-active' : '']"
              type="button"
              @click="selectImage(index)"
            >
              <img class="gallery-thumb" :src="imageUrl" :alt="currentGoodsName" />
            </button>
          </div>
        </div>
        <div class="product-info">
          <div class="meta-row">
            <div class="product-badge">拼团优惠</div>
            <span v-if="marketData.activity" class="activity-kicker">当前活动：{{ marketData.activity.activityName }}</span>
          </div>
          <div class="title-row">
            <h1 class="product-title">{{ currentGoodsName }}</h1>
            <strong v-if="marketData.activity" class="activity-summary">{{ groupTypeText(marketData.activity.groupType) }}</strong>
          </div>
          <div v-if="marketData.activity" class="activity-pills">
            <span class="activity-pill highlight">{{ marketData.activity.target }}人成团</span>
            <span class="activity-pill">{{ marketData.activity.validTime }}分钟有效</span>
            <span class="activity-pill">{{ activityLimitShortText }}</span>
            <span class="activity-pill hot">{{ marketData.teamStatistic.allTeamCount }}团进行中</span>
          </div>
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
      <PayConfirmDialog
        :open="noticeDialog.open"
        :amount="0"
        :title="noticeDialog.title"
        :message="noticeDialog.message"
        :confirm-text="noticeDialog.confirmText"
        :show-cancel="false"
        icon="!"
        @confirm="closeNoticeDialog"
        @cancel="closeNoticeDialog"
      />
    </div>

    <div v-else-if="marketData && isPlainGoods" class="goods-container">
      <button class="back-btn" @click="router.push('/goods')">&larr; 返回商品列表</button>

      <section class="hero fallback-hero">
        <div class="gallery">
          <div class="carousel-main">
            <GoodsNameCover :title="currentGoodsName" :image-url="currentGalleryImageUrl" size="hero" />
            <button v-if="galleryImageUrls.length > 1" class="carousel-btn carousel-prev" type="button" aria-label="上一张商品图" @click="showPrevImage">‹</button>
            <button v-if="galleryImageUrls.length > 1" class="carousel-btn carousel-next" type="button" aria-label="下一张商品图" @click="showNextImage">›</button>
          </div>
          <div v-if="galleryImageUrls.length > 1" class="gallery-thumbs">
            <button
              v-for="(imageUrl, index) in galleryImageUrls"
              :key="imageUrl"
              :class="['gallery-thumb-btn', selectedImageIndex === index ? 'gallery-thumb-active' : '']"
              type="button"
              @click="selectImage(index)"
            >
              <img class="gallery-thumb" :src="imageUrl" :alt="currentGoodsName" />
            </button>
          </div>
        </div>
        <div class="product-info">
          <div class="meta-row">
            <div class="product-badge product-badge-muted">普通商品</div>
          </div>
          <div class="title-row">
            <h1 class="product-title">{{ currentGoodsName }}</h1>
          </div>
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
      <PayConfirmDialog
        :open="noticeDialog.open"
        :amount="0"
        :title="noticeDialog.title"
        :message="noticeDialog.message"
        :confirm-text="noticeDialog.confirmText"
        :show-cancel="false"
        icon="!"
        @confirm="closeNoticeDialog"
        @cancel="closeNoticeDialog"
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
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
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
const payDialog = ref({ open: false, amount: 0, marketType: 0, teamId: null as string | null })
const noticeDialog = ref({ open: false, title: '', message: '', confirmText: '知道了', payFormHtml: '' })
const currentTimeMs = ref(Date.now())
const selectedImageIndex = ref(0)
let countdownTimer: ReturnType<typeof window.setInterval> | null = null

const currentGoodsId = computed(() => String(route.params.goodsId ?? ''))

const currentGoodsName = computed(() => marketData.value?.goods.goodsName || resolveGoodsName(currentGoodsId.value, skuList.value))

const galleryImageUrls = computed(() => {
  if (!marketData.value) {
    return []
  }
  return Array.from(new Set([marketData.value.goods.coverImageUrl, ...(marketData.value.goods.imageUrls ?? [])].filter(Boolean))) as string[]
})

const currentGalleryImageUrl = computed(() => galleryImageUrls.value[selectedImageIndex.value] ?? marketData.value?.goods.coverImageUrl ?? null)

const isPlainGoods = computed(() => marketData.value?.activityId == null || marketData.value?.isVisible === false)

const canJoinGroup = computed(() => marketData.value?.isEnable !== false)

const activityLimitShortText = computed(() => {
  const tagName = marketData.value?.activity?.tagName
  return tagName ? `仅 ${tagName} 可参与` : '不限人群'
})

const teamSummaries = computed(() => {
  if (!marketData.value) {
    return []
  }
  return toTeamSummaries(marketData.value.teamList, currentTimeMs.value)
})

watch(galleryImageUrls, () => {
  selectedImageIndex.value = 0
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
  if (!marketData.value) {
    return
  }

  if (marketType === 1 && !canJoinGroup.value) {
    showNotice('暂不可参与', '当前拼团活动仅限指定人群参与')
    return
  }

  const goods = marketData.value.goods
  payDialog.value = {
    open: true,
    amount: marketType === 1 ? goods.payPrice : goods.originalPrice,
    marketType,
    teamId
  }
}

async function submitPayForm() {
  const loginToken = getCookie('loginToken')
  if (!loginToken || !marketData.value) {
    return
  }

  const goods = marketData.value.goods
  const { marketType, teamId } = payDialog.value
  const activityId = marketType === 1 && marketData.value.activityId != null ? marketData.value.activityId : undefined
  const result = await createPayOrder({
    userId: loginToken,
    productId: goods.goodsId,
    activityId,
    teamId,
    marketType
  })

  if (result.code === '0000') {
    if (result.data.reusedPayOrder) {
      closePayDialog()
      showNotice('已有未支付订单', '该商品已有未支付订单，请先完成这笔订单的支付', '去支付', result.data.payUrl)
      return
    }
    injectPayFormHtml(result.data.payUrl)?.submit()
    closePayDialog()
  } else {
    closePayDialog()
    showNotice('下单失败', result.info || '下单失败，请稍后重试')
  }
}

function closePayDialog() {
  payDialog.value = { open: false, amount: 0, marketType: 0, teamId: null }
}

function showNotice(title: string, message: string, confirmText = '知道了', payFormHtml = '') {
  noticeDialog.value = { open: true, title, message, confirmText, payFormHtml }
}

function closeNoticeDialog() {
  const payFormHtml = noticeDialog.value.payFormHtml
  noticeDialog.value = { open: false, title: '', message: '', confirmText: '知道了', payFormHtml: '' }
  if (payFormHtml) {
    injectPayFormHtml(payFormHtml)?.submit()
  }
}

function selectImage(index: number) {
  selectedImageIndex.value = index
}

function groupTypeText(groupType: number) {
  return groupType === 0 ? '自动成团' : '达成目标拼团'
}

function showPrevImage() {
  if (galleryImageUrls.value.length <= 1) {
    return
  }
  selectedImageIndex.value = (selectedImageIndex.value - 1 + galleryImageUrls.value.length) % galleryImageUrls.value.length
}

function showNextImage() {
  if (galleryImageUrls.value.length <= 1) {
    return
  }
  selectedImageIndex.value = (selectedImageIndex.value + 1) % galleryImageUrls.value.length
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
  align-items: start;
  gap: 24px;
  margin-bottom: 24px;
}

.gallery {
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.carousel-main {
  position: relative;
  height: 320px;
  display: flex;
}

.carousel-main :deep(.goods-name-cover) {
  height: 100%;
}

.carousel-btn {
  position: absolute;
  top: 50%;
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.56);
  color: #fff;
  font-size: 30px;
  line-height: 1;
  cursor: pointer;
  transform: translateY(-50%);
  transition: background 0.2s ease, transform 0.2s ease;
}

.carousel-btn:hover {
  background: rgba(37, 99, 235, 0.86);
  transform: translateY(-50%) scale(1.05);
}

.carousel-prev {
  left: 12px;
}

.carousel-next {
  right: 12px;
}

.gallery-thumbs {
  display: flex;
  gap: 10px;
  padding: 12px;
  overflow-x: auto;
  background: #fff;
}

.gallery-thumb-btn {
  padding: 0;
  border: 2px solid transparent;
  border-radius: 12px;
  background: transparent;
  cursor: pointer;
  flex: 0 0 auto;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.gallery-thumb-btn:hover,
.gallery-thumb-active {
  border-color: #2563eb;
  transform: translateY(-1px);
}

.gallery-thumb {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 10px;
  background: #f1f5f9;
  flex: 0 0 auto;
}

.fallback-hero {
  margin-bottom: 0;
}

.product-info {
  box-sizing: border-box;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  padding: 22px 28px;
  height: 320px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
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
}

.product-badge-muted {
  background: linear-gradient(135deg, #64748b, #94a3b8);
}

.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.product-title {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  line-height: 1.4;
  margin: 0;
}

.title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 8px;
}

.activity-summary {
  max-width: 320px;
  color: #ea580c;
  font-size: 14px;
  white-space: nowrap;
  text-align: right;
}

.promo-copy {
  color: #dc2626;
  font-size: 14px;
  margin: 0;
}

.promo-copy-muted {
  color: #475569;
}

.price-card {
  background: linear-gradient(135deg, #fef2f2, #fff1f2);
  border-radius: 12px;
  padding: 12px 20px;
  margin-top: 0;
  border: 1px solid rgba(248, 113, 113, 0.18);
  display: flex;
  align-items: center;
  gap: 28px;
  min-height: 72px;
}

.price-row {
  display: grid;
  grid-template-columns: auto auto;
  align-items: center;
  gap: 12px;
}

.price-row.secondary {
  align-items: center;
}

.price-label {
  font-size: 13px;
  color: #6b7280;
  min-width: 48px;
}

.pay-price {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
  color: #dc2626;
}

.original-price {
  color: #9ca3af;
  text-decoration: line-through;
  font-size: 16px;
  line-height: 1;
}

.save-tag {
  display: inline-block;
  background: #dc2626;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 4px;
  white-space: nowrap;
}

.activity-kicker {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #9a3412;
  font-size: 13px;
  text-align: right;
}

.activity-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.activity-pill {
  border: 1px solid rgba(251, 146, 60, 0.24);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.82);
  color: #9a3412;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 10px;
}

.activity-pill.highlight {
  background: #fed7aa;
  border-color: #fdba74;
  color: #7c2d12;
}

.activity-pill.hot {
  background: #fee2e2;
  border-color: #fecaca;
  color: #b91c1c;
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

  .carousel-main,
  .carousel-main :deep(.goods-name-cover) {
    height: 280px;
  }

  .product-info,
  .teams-section {
    padding: 20px;
  }

  .product-info {
    height: auto;
    gap: 14px;
  }

  .title-row {
    flex-direction: column;
    gap: 8px;
  }

  .meta-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .activity-summary {
    max-width: none;
    text-align: left;
  }

  .activity-kicker {
    text-align: left;
  }

  .price-card {
    flex-wrap: wrap;
    gap: 12px 18px;
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
