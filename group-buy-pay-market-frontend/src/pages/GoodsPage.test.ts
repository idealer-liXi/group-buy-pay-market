import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import GoodsPage from './GoodsPage.vue'

const mocks = vi.hoisted(() => ({
  createPayOrder: vi.fn().mockResolvedValue({ code: '0000', data: '<form></form>' }),
  queryGroupBuyMarketConfig: vi.fn(),
  querySkuList: vi.fn().mockResolvedValue({ code: '0000', data: { skuList: [{ goodsId: '9890005', goodsName: '新商品', originalPrice: 19.9 }] } }),
  toTeamSummaries: vi.fn(() => [])
}))

const plainGoodsResponse = {
  code: '0000',
  info: '成功',
  data: {
    activityId: null,
    goods: {
      goodsId: '9890005',
      goodsName: '新商品',
      originalPrice: 19.9,
      deductionPrice: 0,
      payPrice: 19.9,
      coverImageUrl: 'https://cdn.example.com/cover.png',
      imageUrls: ['https://cdn.example.com/cover.png', 'https://cdn.example.com/detail.png']
    },
    teamList: [],
    teamStatistic: {
      allTeamCount: 0,
      allTeamCompleteCount: 0,
      allTeamUserCount: 0
    },
    isVisible: true,
    isEnable: true
  }
}

vi.mock('../lib/cookie', () => ({
  getCookie: vi.fn(() => 'user-token')
}))

vi.mock('../lib/market', () => ({
  queryGroupBuyMarketConfig: mocks.queryGroupBuyMarketConfig,
  querySkuList: mocks.querySkuList,
  resolveGoodsName: vi.fn(() => '新商品'),
  toTeamSummaries: mocks.toTeamSummaries
}))

vi.mock('../lib/pay', () => ({
  createPayOrder: mocks.createPayOrder,
  injectPayFormHtml: vi.fn()
}))

describe('GoodsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.createPayOrder.mockResolvedValue({ code: '0000', data: '<form></form>' })
    mocks.queryGroupBuyMarketConfig.mockResolvedValue(plainGoodsResponse)
    mocks.querySkuList.mockResolvedValue({ code: '0000', data: { skuList: [{ goodsId: '9890005', goodsName: '新商品', originalPrice: 19.9 }] } })
    vi.spyOn(window, 'alert').mockImplementation(() => undefined)
  })

  it('renders plain goods detail and pays original price when goods has no market config', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/goods/:goodsId', component: GoodsPage }]
    })
    router.push('/goods/9890005')
    await router.isReady()

    const wrapper = mount(GoodsPage, {
      global: {
        plugins: [router],
        stubs: {
          GroupTeamList: { template: '<div />' },
          PayConfirmDialog: { template: '<div />' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('新商品')
    expect(wrapper.text()).toContain('当前商品暂未配置拼团活动')
    expect(wrapper.text()).toContain('￥19.90')
    expect(wrapper.text()).toContain('原价购买')
    expect(wrapper.text()).not.toContain('开团购买')
    expect(wrapper.find('img[alt="新商品"]').attributes('src')).toBe('https://cdn.example.com/cover.png')
    expect(wrapper.findAll('.gallery-thumb')).toHaveLength(2)

    await wrapper.get('.plain-buy').trigger('click')

    expect(mocks.createPayOrder).toHaveBeenCalledWith({
      userId: 'user-token',
      productId: '9890005',
      activityId: undefined,
      teamId: null,
      marketType: 0
    })
  })

  it('switches goods images through carousel controls and thumbnails', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/goods/:goodsId', component: GoodsPage }]
    })
    router.push('/goods/9890005')
    await router.isReady()

    const wrapper = mount(GoodsPage, {
      global: {
        plugins: [router],
        stubs: {
          GroupTeamList: { template: '<div />' },
          PayConfirmDialog: { template: '<div />' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.get('.goods-name-cover__image').attributes('src')).toBe('https://cdn.example.com/cover.png')

    await wrapper.get('.carousel-next').trigger('click')
    expect(wrapper.get('.goods-name-cover__image').attributes('src')).toBe('https://cdn.example.com/detail.png')

    await wrapper.get('.carousel-prev').trigger('click')
    expect(wrapper.get('.goods-name-cover__image').attributes('src')).toBe('https://cdn.example.com/cover.png')

    await wrapper.findAll('.gallery-thumb-btn')[1].trigger('click')
    expect(wrapper.get('.goods-name-cover__image').attributes('src')).toBe('https://cdn.example.com/detail.png')
  })

  it('hides group activity when current user cannot see it', async () => {
    mocks.queryGroupBuyMarketConfig.mockResolvedValue({
      ...plainGoodsResponse,
      data: {
        ...plainGoodsResponse.data,
        activityId: 9890001,
        isVisible: false,
        isEnable: false
      }
    })
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/goods/:goodsId', component: GoodsPage }]
    })
    router.push('/goods/9890005')
    await router.isReady()

    const wrapper = mount(GoodsPage, {
      global: {
        plugins: [router],
        stubs: {
          GroupTeamList: { template: '<div />' },
          PayConfirmDialog: { template: '<div />' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('普通商品')
    expect(wrapper.text()).not.toContain('拼团优惠')
    expect(wrapper.text()).not.toContain('开团购买')
  })

  it('alerts and does not create group order when current user cannot participate', async () => {
    mocks.queryGroupBuyMarketConfig.mockResolvedValue({
      ...plainGoodsResponse,
      data: {
        ...plainGoodsResponse.data,
        activityId: 9890001,
        isVisible: true,
        isEnable: false
      }
    })
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/goods/:goodsId', component: GoodsPage }]
    })
    router.push('/goods/9890005')
    await router.isReady()

    const wrapper = mount(GoodsPage, {
      global: {
        plugins: [router],
        stubs: {
          GroupTeamList: { template: '<div />' },
          PayConfirmDialog: { template: '<div />' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))
    await new Promise((resolve) => setTimeout(resolve, 0))
    await wrapper.get('.group-buy').trigger('click')

    expect(window.alert).toHaveBeenCalledWith('当前拼团活动仅限指定人群参与')
    expect(mocks.createPayOrder).not.toHaveBeenCalled()
  })
})
