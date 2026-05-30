import { describe, expect, it } from 'vitest'
import goodsNameCoverSource from '../components/GoodsNameCover.vue?raw'
import goodsListPageSource from './GoodsListPage.vue?raw'
import goodsPageSource from './GoodsPage.vue?raw'

function getStyleBlock(source: string) {
  const match = source.match(/<style scoped>([\s\S]*?)<\/style>/)
  return match?.[1] ?? ''
}

function getRuleBlock(style: string, selector: string) {
  const escapedSelector = selector.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const match = style.match(new RegExp(`${escapedSelector}\\s*\\{([^}]*)\\}`))
  return match?.[1] ?? ''
}

describe('goods placeholder layout styles', () => {
  it('keeps list placeholder text centered within the full image area', () => {
    const style = getStyleBlock(goodsNameCoverSource)

    expect(goodsListPageSource).toContain('<GoodsNameCover :title="item.goodsName" :image-url="item.coverImageUrl" size="card" />')
    expect(style).toMatch(/\.goods-name-cover\s*\{[\s\S]*display:\s*flex;/)
    expect(getRuleBlock(style, '.goods-name-cover--card')).toMatch(/(?:^|\n)\s*height:\s*200px;/)
    expect(style).toMatch(/\.goods-name-cover__title\s*\{[\s\S]*text-align:\s*center;/)
    expect(style).toMatch(/\.goods-name-cover__title\s*\{[\s\S]*overflow-wrap:\s*anywhere;/)
  })

  it('keeps product images inside fixed-size cover areas', () => {
    const style = getStyleBlock(goodsNameCoverSource)
    const pageStyle = getStyleBlock(goodsPageSource)

    expect(goodsPageSource).toContain(':image-url="currentGalleryImageUrl"')
    expect(getRuleBlock(style, '.goods-name-cover--hero')).toMatch(/(?:^|\n)\s*height:\s*320px;/)
    expect(getRuleBlock(style, '.goods-name-cover__image')).toMatch(/object-fit:\s*cover;/)
    expect(getRuleBlock(pageStyle, '.carousel-main')).toMatch(/(?:^|\n)\s*height:\s*320px;/)
  })

  it('uses a full hero placeholder instead of list-card whitespace on detail pages', () => {
    const style = getStyleBlock(goodsNameCoverSource)
    const heroTitleStyle = getRuleBlock(style, '.goods-name-cover--hero .goods-name-cover__title')

    expect(heroTitleStyle).toMatch(/height:\s*100%;/)
    expect(heroTitleStyle).toMatch(/display:\s*flex;/)
    expect(heroTitleStyle).toMatch(/align-items:\s*center;/)
    expect(heroTitleStyle).toMatch(/justify-content:\s*center;/)
    expect(heroTitleStyle).not.toMatch(/max-width:\s*12em;/)
  })

  it('does not stretch the gallery to match the taller product info card', () => {
    const style = getStyleBlock(goodsPageSource)

    expect(getRuleBlock(style, '.hero')).toMatch(/align-items:\s*start;/)
  })

  it('keeps the detail price card compact in a single row on desktop', () => {
    const style = getStyleBlock(goodsPageSource)

    expect(getRuleBlock(style, '.product-info')).toMatch(/height:\s*320px;/)
    expect(getRuleBlock(style, '.product-info')).toMatch(/box-sizing:\s*border-box;/)
    expect(getRuleBlock(style, '.product-info')).toMatch(/justify-content:\s*space-between;/)
    expect(getRuleBlock(style, '.meta-row')).toMatch(/display:\s*flex;/)
    expect(getRuleBlock(style, '.meta-row')).toMatch(/justify-content:\s*space-between;/)
    expect(getRuleBlock(style, '.meta-row')).toMatch(/align-items:\s*center;/)
    expect(getRuleBlock(style, '.title-row')).toMatch(/align-items:\s*baseline;/)
    expect(getRuleBlock(style, '.price-card')).toMatch(/display:\s*flex;/)
    expect(getRuleBlock(style, '.price-card')).toMatch(/align-items:\s*center;/)
    expect(getRuleBlock(style, '.price-card')).toMatch(/min-height:\s*72px;/)
    expect(getRuleBlock(style, '.price-row')).toMatch(/display:\s*grid;/)
    expect(getRuleBlock(style, '.price-row')).toMatch(/grid-template-columns:\s*auto auto;/)
    expect(getRuleBlock(style, '.price-row')).toMatch(/align-items:\s*center;/)
    expect(getRuleBlock(style, '.pay-price')).toMatch(/line-height:\s*1;/)
    expect(getRuleBlock(style, '.original-price')).toMatch(/line-height:\s*1;/)
    expect(getRuleBlock(style, '.price-row.secondary')).not.toMatch(/margin-top:/)
  })

  it('aligns group price label and amount on goods list cards', () => {
    const style = getStyleBlock(goodsListPageSource)

    expect(getRuleBlock(style, '.goods-group-price')).toMatch(/display:\s*flex;/)
    expect(getRuleBlock(style, '.goods-group-price')).toMatch(/align-items:\s*center;/)
    expect(getRuleBlock(style, '.goods-group-price')).toMatch(/gap:\s*6px;/)
    expect(getRuleBlock(style, '.goods-group-price')).toMatch(/line-height:\s*1;/)
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
