import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { describe, expect, it, vi } from 'vitest'
import GoodsListPage from './GoodsListPage.vue'

vi.mock('../lib/market', () => ({
  querySkuList: vi.fn().mockResolvedValue({
    code: '0000',
    info: '成功',
    data: {
      skuList: [
        { goodsId: '9890001', goodsName: '读书卡', originalPrice: 99, payPrice: 89, deductionPrice: 10, activityId: 0, activityName: '新人拼团', tagName: '新人', tagScope: '2' },
        { goodsId: '9890002', goodsName: '咖啡卡', originalPrice: 39 }
      ]
    }
  })
}))

describe('GoodsListPage', () => {
  it('renders group price and restricted tag for activity goods', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/goods/:goodsId', component: { template: '<div />' } }]
    })

    const wrapper = mount(GoodsListPage, {
      global: { plugins: [router] }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('读书卡')
    expect(wrapper.text()).toContain('拼团价 ￥89.00')
    expect(wrapper.text()).toContain('仅 新人 人群可参与')
    expect(wrapper.text()).toContain('原价 ￥99.00')
    expect(wrapper.text()).toContain('咖啡卡')
    expect(wrapper.text()).toContain('￥39.00')
  })
})
