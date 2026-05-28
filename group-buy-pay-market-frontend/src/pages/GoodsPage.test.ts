import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { describe, expect, it, vi } from 'vitest'
import GoodsPage from './GoodsPage.vue'

const mocks = vi.hoisted(() => ({
  createPayOrder: vi.fn().mockResolvedValue({ code: '0000', data: '<form></form>' })
}))

vi.mock('../lib/cookie', () => ({
  getCookie: vi.fn(() => 'user-token')
}))

vi.mock('../lib/market', () => ({
  queryGroupBuyMarketConfig: vi.fn().mockResolvedValue({
    code: '0000',
    info: '成功',
    data: {
      activityId: null,
      goods: {
        goodsId: '9890005',
        originalPrice: 19.9,
        deductionPrice: 0,
        payPrice: 19.9
      },
      teamList: [],
      teamStatistic: {
        allTeamCount: 0,
        allTeamCompleteCount: 0,
        allTeamUserCount: 0
      }
    }
  }),
  querySkuList: vi.fn().mockResolvedValue({ code: '0000', data: { skuList: [{ goodsId: '9890005', goodsName: '新商品', originalPrice: 19.9 }] } }),
  resolveGoodsName: vi.fn(() => '新商品'),
  toTeamSummaries: vi.fn(() => [])
}))

vi.mock('../lib/pay', () => ({
  createPayOrder: mocks.createPayOrder,
  injectPayFormHtml: vi.fn()
}))

describe('GoodsPage', () => {
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
          GoodsNameCover: { template: '<div />' },
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

    await wrapper.get('.plain-buy').trigger('click')

    expect(mocks.createPayOrder).toHaveBeenCalledWith({
      userId: 'user-token',
      productId: '9890005',
      activityId: undefined,
      teamId: null,
      marketType: 0
    })
  })
})
