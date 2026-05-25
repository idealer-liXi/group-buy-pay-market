<template>
  <div class="goods-page">
    <div class="goods-container" v-if="marketData">
      <section class="hero">
        <div class="gallery">
          <div class="gallery-image">商品图</div>
        </div>
        <div class="product-info">
          <h1 class="product-title">手写MyBatis：渐进式源码实践（全彩）</h1>
          <p class="promo-copy">
            直降 ¥{{ marketData.goods.deductionPrice.toFixed(0) }}，{{ marketData.teamStatistic.allTeamUserCount }}人再抢，参与马上抢到
          </p>
          <div class="price-group">
            <span class="pay-price">￥{{ marketData.goods.payPrice.toFixed(2) }}</span>
            <span class="original-price">￥{{ marketData.goods.originalPrice.toFixed(2) }}</span>
          </div>
        </div>
      </section>

      <section class="teams-section">
        <h2>正在拼团</h2>
        <GroupTeamList :teams="teamSummaries" @join="handleJoinTeam" />
      </section>

      <footer class="action-bar">
        <button class="action-btn buy-alone" @click="handleSingleBuy">
          单独购买(￥{{ marketData.goods.originalPrice.toFixed(2) }})
        </button>
        <button class="action-btn group-buy" @click="handleStartTeam">
          开团购买(￥{{ marketData.goods.payPrice.toFixed(2) }})
        </button>
      </footer>

      <PayConfirmDialog
        :open="payDialog.open"
        :amount="payDialog.amount"
        @confirm="submitPayForm"
        @cancel="closePayDialog"
      />
    </div>

    <div v-else class="loading-state">正在加载商品信息...</div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import GroupTeamList from '../components/GroupTeamList.vue'
import PayConfirmDialog from '../components/PayConfirmDialog.vue'
import { getCookie } from '../lib/cookie'
import { queryGroupBuyMarketConfig, toTeamSummaries } from '../lib/market'
import { createPayOrder, injectPayFormHtml } from '../lib/pay'
import type { GoodsMarketResponse } from '../types/api'

const route = useRoute()
const router = useRouter()
const marketData = ref<GoodsMarketResponse | null>(null)
const payDialog = ref({ open: false, amount: 0, html: '' })

const teamSummaries = computed(() => {
  if (!marketData.value) {
    return []
  }
  return toTeamSummaries(marketData.value.teamList)
})

onMounted(async () => {
  const loginToken = getCookie('loginToken')
  if (!loginToken) {
    await router.replace('/login')
    return
  }

  const response = await queryGroupBuyMarketConfig({
    userId: loginToken,
    source: 's01',
    channel: 'c01',
    goodsId: route.params.goodsId as string
  })

  if (response.code === '0000') {
    marketData.value = response.data
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
  const activityId = marketType === 1 ? marketData.value.activityId : undefined
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
  padding: 40px 0 120px;
}

.goods-container {
  width: 1120px;
  margin: 0 auto;
}

.hero {
  display: grid;
  grid-template-columns: 460px 1fr;
  gap: 32px;
  margin-bottom: 32px;
}

.gallery-image,
.product-info,
.teams-section {
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 8px 30px rgba(15, 23, 42, 0.05);
}

.gallery-image {
  height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.product-info,
.teams-section {
  padding: 28px;
}

.promo-copy {
  color: #dc2626;
}

.price-group {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.pay-price {
  font-size: 40px;
  font-weight: 700;
  color: #dc2626;
}

.original-price {
  color: #9ca3af;
  text-decoration: line-through;
}

.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(8px);
  border-top: 1px solid #e5e7eb;
}

.action-btn {
  min-width: 260px;
  padding: 14px 24px;
  border: none;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.buy-alone {
  background: #111827;
}

.group-buy {
  background: #2563eb;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  color: #6b7280;
}
</style>
