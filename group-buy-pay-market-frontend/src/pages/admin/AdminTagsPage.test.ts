import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AdminTagsPage from './AdminTagsPage.vue'

vi.mock('../../lib/admin', () => ({
  addAdminTagMember: vi.fn().mockResolvedValue({ code: '0000' }),
  createAdminTag: vi.fn().mockResolvedValue({ code: '0000' }),
  queryAdminTagMembers: vi.fn().mockResolvedValue({ code: '0000', data: { memberList: [{ userId: 'u1', displayName: '指纹用户-u1', loginType: 'FINGERPRINT', status: 0 }] } }),
  queryAdminTags: vi.fn().mockResolvedValue({ code: '0000', data: { tagList: [{ tagId: 'T001', tagName: '新人', tagDesc: '新人标签', statistics: 1 }] } }),
  queryAdminUsers: vi.fn().mockResolvedValue({ code: '0000', data: { userList: [{ userId: 'u1', displayName: '指纹用户-u1', loginType: 'FINGERPRINT', status: 0 }] } }),
  removeAdminTagMember: vi.fn().mockResolvedValue({ code: '0000' }),
  updateAdminTag: vi.fn().mockResolvedValue({ code: '0000' })
}))

describe('AdminTagsPage', () => {
  it('renders tags and members', async () => {
    const wrapper = mount(AdminTagsPage, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' }
        }
      }
    })
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('标签管理')
    expect(wrapper.text()).toContain('T001')
    expect(wrapper.text()).toContain('新人')
  })
})
