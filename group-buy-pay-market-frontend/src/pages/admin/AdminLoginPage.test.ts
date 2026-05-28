import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { describe, expect, it, vi } from 'vitest'
import AdminLoginPage from './AdminLoginPage.vue'

vi.mock('../../lib/admin', () => ({
  adminLogin: vi.fn().mockResolvedValue({ code: '0000', info: 'success', data: { adminToken: 'token', displayName: '管理员' } })
}))

describe('AdminLoginPage', () => {
  it('renders account login form', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/admin/login', component: AdminLoginPage },
        { path: '/admin/goods', component: { template: '<div />' } }
      ]
    })
    router.push('/admin/login')
    await router.isReady()

    const wrapper = mount(AdminLoginPage, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.text()).toContain('后台登录')
    expect(wrapper.find('input[placeholder="请输入管理员账号"]').exists()).toBe(true)
  })
})
