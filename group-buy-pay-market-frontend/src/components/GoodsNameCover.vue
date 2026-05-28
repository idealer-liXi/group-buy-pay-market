<template>
  <div class="goods-name-cover" :class="`goods-name-cover--${size}`">
    <img v-if="imageUrl && !imageFailed" class="goods-name-cover__image" :src="imageUrl" :alt="title" @error="imageFailed = true" />
    <span v-else class="goods-name-cover__title">{{ title }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  title: string
  size?: 'card' | 'hero'
  imageUrl?: string | null
}>(), {
  size: 'card',
  imageUrl: null
})

const imageFailed = ref(false)

watch(() => props.imageUrl, () => {
  imageFailed.value = false
})
</script>

<style scoped>
.goods-name-cover {
  box-sizing: border-box;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  overflow: hidden;
}

.goods-name-cover--card {
  min-height: 200px;
  padding: 20px;
}

.goods-name-cover--hero {
  flex: 1;
  min-height: 320px;
}

.goods-name-cover__title {
  width: 100%;
  max-width: 100%;
  color: #3730a3;
  font-weight: 700;
  text-align: center;
  line-height: 1.45;
  overflow-wrap: anywhere;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.goods-name-cover__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.goods-name-cover--card .goods-name-cover__title {
  font-size: 18px;
  line-height: 1.5;
  -webkit-line-clamp: 3;
}

.goods-name-cover--hero .goods-name-cover__title {
  max-width: 12em;
  font-size: 28px;
}

@media (max-width: 768px) {
  .goods-name-cover--hero {
    min-height: 280px;
  }

  .goods-name-cover--card .goods-name-cover__title {
    font-size: 17px;
  }

  .goods-name-cover--hero .goods-name-cover__title {
    font-size: 24px;
  }
}
</style>
