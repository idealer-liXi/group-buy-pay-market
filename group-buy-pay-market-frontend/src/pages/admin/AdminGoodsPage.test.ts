import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AdminGoodsPage from './AdminGoodsPage.vue'

vi.mock('../../lib/admin', () => ({
  queryAdminGoods: vi.fn().mockResolvedValue({
    code: '0000',
    info: 'success',
    data: { goodsList: [{ goodsId: '9890001', goodsName: '测试商品', originalPrice: 10, status: 0 }] }
  })
}))

describe('AdminGoodsPage', () => {
  it('renders goods table rows', async () => {
    const wrapper = mount(AdminGoodsPage, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('测试商品')
    expect(wrapper.text()).toContain('新增商品')
    expect(wrapper.find('input[placeholder="请输入商品ID"]').exists()).toBe(false)
  })
})
