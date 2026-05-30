import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AdminActivitiesPage from './AdminActivitiesPage.vue'
import { createAdminActivity } from '../../lib/admin'

vi.mock('../../lib/admin', () => ({
  queryAdminActivities: vi.fn().mockResolvedValue({
    code: '0000',
    info: 'success',
    data: { activityList: [{ activityId: 100123, activityName: '拼团读书节', goodsId: '9890001', discountId: '1', groupType: 1, takeLimitCount: 1, target: 3, validTime: 15, status: 1, startTime: '2025-01-01T00:00:00.000+08:00', endTime: '2026-12-31T23:59:59.000+08:00', tagId: 'T001', tagScope: '2' }] }
  }),
  queryAdminGoods: vi.fn().mockResolvedValue({
    code: '0000',
    info: 'success',
    data: { goodsList: [{ goodsId: '9890001', goodsName: '读书卡', originalPrice: 99, status: 0 }, { goodsId: '9890002', goodsName: '咖啡卡', originalPrice: 39, status: 0 }] }
  }),
  queryAdminDiscounts: vi.fn().mockResolvedValue({
    code: '0000',
    info: 'success',
    data: { discountList: [{ discountId: '1', discountName: '直减10元', discountDesc: '拼团直减10元', discountType: 1, marketPlan: 'ZJ', marketExpr: '10', status: 0 }] }
  }),
  queryAdminTags: vi.fn().mockResolvedValue({
    code: '0000',
    info: 'success',
    data: { tagList: [{ tagId: 'T001', tagName: '新人', tagDesc: '新人标签', statistics: 1 }] }
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
    expect(wrapper.find('select[multiple]').exists()).toBe(false)
    expect(wrapper.findAll('.goods-option-card')).toHaveLength(1)
    expect(wrapper.find('select:not([multiple])').exists()).toBe(true)
    expect(wrapper.text()).not.toContain('9890001 - 读书卡')
    expect(wrapper.text()).toContain('9890002 - 咖啡卡')
    expect(wrapper.text()).toContain('1 - 直减10元')
    expect(wrapper.text()).toContain('自动成团')
    expect(wrapper.text()).toContain('达成目标拼团')
    expect(wrapper.text()).toContain('新人 - T001')
    expect(wrapper.find('input[placeholder="开始时间"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="结束时间"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="标签ID"]').exists()).toBe(false)
    expect(wrapper.find('input[placeholder="标签范围"]').exists()).toBe(false)
  })

  it('keeps current activity goods visible when editing', async () => {
    const wrapper = mount(AdminActivitiesPage, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))
    const editButton = wrapper.findAll('button').find((button) => button.text() === '编辑')
    expect(editButton).toBeTruthy()
    await editButton!.trigger('click')

    expect(wrapper.text()).toContain('9890001 - 读书卡')
    expect(wrapper.text()).toContain('9890002 - 咖啡卡')
  })

  it('fills edit form with backend time and tag fields', async () => {
    const wrapper = mount(AdminActivitiesPage, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))
    const editButton = wrapper.findAll('button').find((button) => button.text() === '编辑')
    await editButton!.trigger('click')

    expect((wrapper.find('input[placeholder="开始时间"]').element as HTMLInputElement).value).toBe('2025-01-01T00:00')
    expect((wrapper.find('input[placeholder="结束时间"]').element as HTMLInputElement).value).toBe('2026-12-31T23:59')
    expect((wrapper.findAll('select').at(2)!.element as HTMLSelectElement).value).toBe('T001')
    expect((wrapper.findAll('select').at(3)!.element as HTMLSelectElement).value).toBe('2')
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
    await wrapper.find('input[type="checkbox"][value="9890002"]').setValue(true)
    await wrapper.find('select:not([multiple])').setValue('1')
    await wrapper.find('input[placeholder="开始时间"]').setValue('2026-05-26T10:00')
    await wrapper.find('input[placeholder="结束时间"]').setValue('2026-05-26T11:00')
    await wrapper.find('form').trigger('submit.prevent')

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('商品已绑定其他活动')
  })

  it('submits selected goods ids as a comma-separated string', async () => {
    const wrapper = mount(AdminActivitiesPage, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' }
        }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    await wrapper.find('input[placeholder="请输入活动名称"]').setValue('多商品活动')
    await wrapper.find('input[type="checkbox"][value="9890002"]').setValue(true)
    await wrapper.find('select:not([multiple])').setValue('1')
    await wrapper.find('select[aria-label="拼团类型"]').setValue('0')
    await wrapper.find('input[placeholder="开始时间"]').setValue('2026-05-26T10:00')
    await wrapper.find('input[placeholder="结束时间"]').setValue('2026-05-26T11:00')
    await wrapper.find('form').trigger('submit.prevent')

    expect(createAdminActivity).toHaveBeenCalledWith(expect.objectContaining({ goodsId: '9890002', discountId: '1', groupType: 0 }))
    expect(createAdminActivity).toHaveBeenCalledWith(expect.not.objectContaining({ activityId: 0 }))
  })
})
