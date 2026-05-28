import { describe, expect, it } from 'vitest'
import goodsNameCoverSource from '../components/GoodsNameCover.vue?raw'
import goodsListPageSource from './GoodsListPage.vue?raw'
import goodsPageSource from './GoodsPage.vue?raw'

function getStyleBlock(source: string) {
  const match = source.match(/<style scoped>([\s\S]*?)<\/style>/)
  return match?.[1] ?? ''
}

describe('goods placeholder layout styles', () => {
  it('keeps list placeholder text centered within the full image area', () => {
    const style = getStyleBlock(goodsNameCoverSource)

    expect(goodsListPageSource).toContain('<GoodsNameCover :title="item.goodsName" :image-url="item.coverImageUrl" size="card" />')
    expect(style).toMatch(/\.goods-name-cover\s*\{[\s\S]*display:\s*flex;/)
    expect(style).toMatch(/\.goods-name-cover__title\s*\{[\s\S]*text-align:\s*center;/)
    expect(style).toMatch(/\.goods-name-cover__title\s*\{[\s\S]*overflow-wrap:\s*anywhere;/)
  })

  it('fills the detail gallery card and centers wrapped placeholder text', () => {
    const style = getStyleBlock(goodsNameCoverSource)

    expect(goodsPageSource).toContain(':image-url="currentGalleryImageUrl"')
    expect(style).toMatch(/\.goods-name-cover--hero\s*\{[\s\S]*min-height:\s*320px;/)
    expect(style).not.toMatch(/\.goods-name-cover--hero\s*\{[\s\S]*?(?:^|\n)\s*height:\s*280px;/m)
  })

  it('stacks the detail hero into a single column on narrow screens', () => {
    const style = getStyleBlock(goodsPageSource)

    expect(style).toMatch(/@media\s*\(max-width:\s*768px\)\s*\{[\s\S]*\.hero\s*\{[\s\S]*grid-template-columns:\s*1fr;/)
  })

  it('renders detail placeholder and title from resolved currentGoodsName', () => {
    expect(goodsPageSource).toContain(':image-url="currentGalleryImageUrl"')
    expect(goodsPageSource).toContain('<h1 class="product-title">{{ currentGoodsName }}</h1>')
    expect(goodsPageSource).toContain("const currentGoodsName = computed(() => marketData.value?.goods.goodsName || resolveGoodsName(currentGoodsId.value, skuList.value))")
  })
})
