import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import GoodsPage from './GoodsPage.vue'

const mocks = vi.hoisted(() => ({
  createPayOrder: vi.fn().mockResolvedValue({ code: '0000', data: { payUrl: '<form></form>', reusedPayOrder: false } }),
  injectPayFormHtml: vi.fn(),
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
  injectPayFormHtml: mocks.injectPayFormHtml
}))

describe('GoodsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.createPayOrder.mockResolvedValue({ code: '0000', data: { payUrl: '<form></form>', reusedPayOrder: false } })
    mocks.queryGroupBuyMarketConfig.mockResolvedValue(plainGoodsResponse)
    mocks.querySkuList.mockResolvedValue({ code: '0000', data: { skuList: [{ goodsId: '9890005', goodsName: '新商品', originalPrice: 19.9 }] } })
    vi.spyOn(window, 'alert').mockImplementation(() => undefined)
  })

  it('opens pay confirmation first and locks order only after confirm', async () => {
    mocks.createPayOrder.mockResolvedValue({ code: '0000', data: { orderId: 'new-order', payUrl: '<form></form>', reusedPayOrder: false } })
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/goods/:goodsId', component: GoodsPage },
        { path: '/mock-pay/:orderId', component: { template: '<div />' } }
      ]
    })
    router.push('/goods/9890005')
    await router.isReady()

    const wrapper = mount(GoodsPage, {
      global: {
        plugins: [router],
        stubs: {
          GroupTeamList: { template: '<div />' }
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
    expect(wrapper.find('.meta-row .activity-kicker').exists()).toBe(false)
    expect(wrapper.find('.title-row .activity-summary').exists()).toBe(false)
    expect(wrapper.findAll('.activity-pill')).toHaveLength(0)
    expect(wrapper.find('img[alt="新商品"]').attributes('src')).toBe('https://cdn.example.com/cover.png')
    expect(wrapper.findAll('.gallery-thumb')).toHaveLength(2)

    await wrapper.get('.plain-buy').trigger('click')
    expect(mocks.createPayOrder).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('支付确认')

    await wrapper.get('.confirm-btn').trigger('click')

    expect(mocks.createPayOrder).toHaveBeenCalledWith({
      userId: 'user-token',
      productId: '9890005',
      activityId: undefined,
      teamId: null,
      marketType: 0
    })
    await new Promise((resolve) => setTimeout(resolve, 0))
    expect(router.currentRoute.value.fullPath).toBe('/mock-pay/new-order')
    expect(mocks.injectPayFormHtml).not.toHaveBeenCalled()
  })

  it('prompts user to pay existing unpaid order before submitting reused pay form', async () => {
    mocks.createPayOrder.mockResolvedValue({
      code: '0000',
      data: { orderId: 'old-order', payUrl: '<form id="old-pay"></form>', reusedPayOrder: true }
    })
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/goods/:goodsId', component: GoodsPage },
        { path: '/mock-pay/:orderId', component: { template: '<div />' } }
      ]
    })
    router.push('/goods/9890005')
    await router.isReady()

    const wrapper = mount(GoodsPage, {
      global: {
        plugins: [router],
        stubs: {
          GroupTeamList: { template: '<div />' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))
    await new Promise((resolve) => setTimeout(resolve, 0))
    await wrapper.get('.plain-buy').trigger('click')
    await wrapper.get('.confirm-btn').trigger('click')

    expect(wrapper.text()).toContain('已有未支付订单')
    expect(wrapper.text()).toContain('请先完成这笔订单的支付')
    expect(mocks.injectPayFormHtml).not.toHaveBeenCalled()

    await wrapper.get('.confirm-btn').trigger('click')

    await new Promise((resolve) => setTimeout(resolve, 0))
    expect(router.currentRoute.value.fullPath).toBe('/mock-pay/old-order')
    expect(mocks.injectPayFormHtml).not.toHaveBeenCalled()
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

  it('shows an in-page notice and does not create group order when current user cannot participate', async () => {
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
          GroupTeamList: { template: '<div />' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))
    await new Promise((resolve) => setTimeout(resolve, 0))
    await wrapper.get('.group-buy').trigger('click')

    expect(window.alert).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('当前拼团活动仅限指定人群参与')
    expect(mocks.createPayOrder).not.toHaveBeenCalled()
  })

  it('renders activity information for group-buy goods', async () => {
    mocks.queryGroupBuyMarketConfig.mockResolvedValue({
      ...plainGoodsResponse,
      data: {
        ...plainGoodsResponse.data,
        activityId: 9890001,
        activity: {
          activityId: 9890001,
          activityName: '新人拼团',
          groupType: 1,
          target: 3,
          validTime: 15,
          tagId: 'T001',
          tagName: '新人',
          tagScope: '2'
        },
        goods: {
          ...plainGoodsResponse.data.goods,
          originalPrice: 99,
          deductionPrice: 10,
          payPrice: 89
        },
        teamStatistic: {
          allTeamCount: 2,
          allTeamCompleteCount: 1,
          allTeamUserCount: 5
        }
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

    expect(wrapper.text()).toContain('当前活动：新人拼团')
    expect(wrapper.text()).toContain('达成目标拼团')
    expect(wrapper.text()).toContain('3人成团')
    expect(wrapper.text()).toContain('15分钟有效')
    expect(wrapper.text()).toContain('仅 新人 可参与')
    expect(wrapper.text()).toContain('2团进行中')
    expect(wrapper.find('.activity-grid').exists()).toBe(false)
    expect(wrapper.find('.activity-card').exists()).toBe(false)
    expect(wrapper.find('.meta-row .activity-kicker').exists()).toBe(true)
    expect(wrapper.find('.title-row .activity-summary').exists()).toBe(true)
    expect(wrapper.findAll('.activity-pill').length).toBeGreaterThanOrEqual(4)
  })
})
