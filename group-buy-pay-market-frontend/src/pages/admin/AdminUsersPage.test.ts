import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AdminUsersPage from './AdminUsersPage.vue'

vi.mock('../../lib/admin', () => ({
  queryAdminUsers: vi.fn().mockResolvedValue({
    code: '0000',
    info: 'success',
    data: { userList: [{ userId: 'u1', displayName: '指纹用户-u1', loginType: 'FINGERPRINT', status: 0, firstLoginTime: '2026-05-28T10:00:00', lastLoginTime: '2026-05-28T10:10:00' }] }
  }),
  queryAdminUserTags: vi.fn().mockResolvedValue({ code: '0000', info: 'success', data: { tagList: [{ tagId: 'T001', tagName: '新人', tagDesc: '新人标签', statistics: 1 }] } })
}))

describe('AdminUsersPage', () => {
  it('renders fingerprint users', async () => {
    const wrapper = mount(AdminUsersPage, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' }
        }
      }
    })
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('用户管理')
    expect(wrapper.text()).toContain('u1')
    expect(wrapper.text()).toContain('指纹用户-u1')
  })
})
