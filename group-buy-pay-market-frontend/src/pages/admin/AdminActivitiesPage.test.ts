import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AdminActivitiesPage from './AdminActivitiesPage.vue'

vi.mock('../../lib/admin', () => ({
  queryAdminActivities: vi.fn().mockResolvedValue({
    code: '0000',
    info: 'success',
    data: { activityList: [{ activityId: 100123, activityName: '拼团读书节', goodsId: '9890001', discountId: '1', groupType: 1, takeLimitCount: 1, target: 3, validTime: 15, status: 1, startTime: '2025-01-01T00:00', endTime: '2026-12-31T23:59' }] }
  }),
  createAdminActivity: vi.fn().mockResolvedValue({ code: '0002', info: '商品已绑定其他活动' }),
  updateAdminActivity: vi.fn(),
  updateAdminActivityStatus: vi.fn()
}))

describe('AdminActivitiesPage', () => {
  it('renders activity rows', async () => {
    const wrapper = mount(AdminActivitiesPage, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('拼团读书节')
    expect(wrapper.text()).toContain('新增活动')
    expect(wrapper.find('input[placeholder="活动ID"]').exists()).toBe(false)
    expect(wrapper.find('input[placeholder="开始时间"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="结束时间"]').exists()).toBe(true)
  })

  it('shows backend error when creating duplicate-bound goods activity', async () => {
    const wrapper = mount(AdminActivitiesPage, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    await wrapper.find('input[placeholder="请输入活动名称"]').setValue('重复活动')
    await wrapper.find('input[placeholder="请输入商品ID"]').setValue('9890001')
    await wrapper.find('input[placeholder="请输入折扣ID"]').setValue('1')
    await wrapper.find('input[placeholder="开始时间"]').setValue('2026-05-26T10:00')
    await wrapper.find('input[placeholder="结束时间"]').setValue('2026-05-26T11:00')
    await wrapper.find('form').trigger('submit.prevent')

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('商品已绑定其他活动')
  })
})
