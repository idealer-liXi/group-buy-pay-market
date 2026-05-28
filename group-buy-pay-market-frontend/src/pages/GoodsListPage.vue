<template>
  <div class="goods-list-page">
    <div class="page-header">
      <h1 class="page-title">全部商品</h1>
      <button class="history-btn" @click="router.push('/orders')">购物记录</button>
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else class="goods-grid">
      <div
        v-for="item in skuList"
        :key="item.goodsId"
        class="goods-card"
        @click="goToDetail(item.goodsId)"
      >
        <GoodsNameCover :title="item.goodsName" size="card" />
        <div class="goods-info">
          <p class="goods-name">{{ item.goodsName }}</p>
          <p class="goods-price">
            <span class="price-symbol">¥</span>{{ item.originalPrice.toFixed(2) }}
          </p>
        </div>
      </div>
    </div>
    <div v-if="!loading && !error && skuList.length === 0" class="empty">暂无商品</div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import GoodsNameCover from '../components/GoodsNameCover.vue'
import { querySkuList } from '../lib/market'
import type { SkuItem } from '../types/api'

const router = useRouter()
const skuList = ref<SkuItem[]>([])
const loading = ref(true)
const error = ref('')

function goToDetail(goodsId: string) {
  router.push(`/goods/${goodsId}`)
}

onMounted(async () => {
  try {
    const result = await querySkuList()
    if (result.code === '0000') {
      skuList.value = result.data.skuList
    } else {
      error.value = result.info || '获取商品列表失败'
    }
  } catch {
    error.value = '网络异常，请稍后重试'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.goods-list-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px 80px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.history-btn {
  border: 1px solid #2563eb;
  background: #eff6ff;
  color: #2563eb;
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
  font-weight: 600;
}

.loading,
.empty,
.error {
  text-align: center;
  padding: 60px 0;
  color: #6b7280;
  font-size: 16px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.goods-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.goods-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.goods-info {
  padding: 12px;
}

.goods-name {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.goods-price {
  color: #ef4444;
  font-size: 18px;
  font-weight: 600;
}

.price-symbol {
  font-size: 12px;
}
</style>
