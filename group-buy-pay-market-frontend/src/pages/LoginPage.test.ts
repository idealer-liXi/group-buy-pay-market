import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { describe, expect, it, vi } from 'vitest'
import LoginPage from './LoginPage.vue'

vi.mock('../lib/auth', () => ({
  fetchWeixinQrCodeTicket: vi.fn().mockResolvedValue({ code: '0001', info: 'fail', data: '' }),
  checkLogin: vi.fn(),
  getFingerprint: vi.fn().mockResolvedValue('test-visitor-id')
}))

describe('LoginPage', () => {
  it('renders login mode selection', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/goods/9890001', component: { template: '<div />' } }]
    })
    router.push('/')
    await router.isReady()

    const wrapper = mount(LoginPage, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.text()).toContain('微信扫码登录')
    expect(wrapper.text()).toContain('无痕登录')
  })
})
