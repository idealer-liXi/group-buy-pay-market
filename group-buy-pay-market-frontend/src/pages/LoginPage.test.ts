import { flushPromises, mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { describe, expect, it, vi } from 'vitest'
import LoginPage from './LoginPage.vue'
import { fingerprintLogin } from '../lib/auth'

vi.mock('../lib/auth', () => ({
  fetchWeixinQrCodeTicket: vi.fn().mockResolvedValue({ code: '0001', info: 'fail', data: '' }),
  checkLogin: vi.fn(),
  fingerprintLogin: vi.fn().mockResolvedValue({ code: '0000', info: 'success', data: { userId: 'test-visitor-id', displayName: '指纹用户-tor-id' } }),
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

  it('registers fingerprint user before navigating', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        { path: '/', component: LoginPage },
        { path: '/goods', component: { template: '<div />' } }
      ]
    })
    router.push('/')
    await router.isReady()

    const wrapper = mount(LoginPage, {
      global: {
        plugins: [router]
      }
    })

    await wrapper.find('.mode-btn.fingerprint').trigger('click')
    await wrapper.find('.fingerprint-btn').trigger('click')
    await flushPromises()

    expect(fingerprintLogin).toHaveBeenCalledWith('test-visitor-id')
    expect(router.currentRoute.value.path).toBe('/goods')
  })
})
