import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AdminDiscountsPage from './AdminDiscountsPage.vue'

vi.mock('../../lib/admin', () => ({
  queryAdminDiscounts: vi.fn().mockResolvedValue({
    code: '0000',
    info: 'success',
    data: { discountList: [{ discountId: '1', discountName: '直减10元', discountDesc: '拼团直减10元', discountType: 1, marketPlan: 'ZJ', marketExpr: '10', status: 0 }] }
  }),
  queryAdminTags: vi.fn().mockResolvedValue({
    code: '0000',
    info: 'success',
    data: { tagList: [{ tagId: 'T001', tagName: '新人', tagDesc: '新人标签', statistics: 1 }] }
  })
}))

describe('AdminDiscountsPage', () => {
  it('renders discount rows', async () => {
    const wrapper = mount(AdminDiscountsPage, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('直减10元')
    expect(wrapper.text()).toContain('新增折扣')
    expect(wrapper.find('input[placeholder="请输入折扣ID"]').exists()).toBe(false)
    expect(wrapper.find('thead').text()).toContain('折扣ID')
    expect(wrapper.find('tbody').text()).toContain('1')
    expect(wrapper.text()).toContain('ZJ')
    expect(wrapper.text()).toContain('MJ')
    expect(wrapper.text()).toContain('N')
    expect(wrapper.text()).toContain('ZK')
    expect(wrapper.text()).toContain('新人 - T001')
  })
})
