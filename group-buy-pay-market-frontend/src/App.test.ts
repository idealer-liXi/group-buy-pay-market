import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App.vue'

const socketInstances: Array<{ close: ReturnType<typeof vi.fn>; emit: (data: unknown) => void }> = []

class MockWebSocket {
  onmessage: ((event: MessageEvent) => void) | null = null
  close = vi.fn()

  constructor(public url: string) {
    socketInstances.push({
      close: this.close,
      emit: (data: unknown) => this.onmessage?.({ data: JSON.stringify(data) } as MessageEvent)
    })
  }
}

vi.mock('./lib/cookie', () => ({
  getCookie: vi.fn(() => 'u1'),
  setCookie: vi.fn()
}))

vi.mock('./lib/user', () => ({
  queryUserTags: vi.fn().mockResolvedValue({
    code: '0000',
    info: '成功',
    data: { tagList: [{ tagId: 'T001', tagName: '新人' }, { tagId: 'T002', tagName: 'VIP' }] }
  })
}))

describe('App', () => {
  beforeEach(() => {
    socketInstances.length = 0
    localStorage.clear()
    vi.stubGlobal('WebSocket', MockWebSocket)
  })

  it('shows current user tags in mall header', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/goods', component: { template: '<div />' } }]
    })
    router.push('/goods')
    await router.isReady()

    const wrapper = mount(App, {
      global: { plugins: [router] }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('我的标签：新人、VIP')
  })

  it('receives websocket notifications globally and persists them per user', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/goods', component: { template: '<div />' } }]
    })
    router.push('/goods')
    await router.isReady()

    const wrapper = mount(App, {
      global: { plugins: [router] }
    })
    await new Promise((resolve) => setTimeout(resolve, 0))

    socketInstances[0].emit({ type: 'PAY_SUCCESS', orderId: 'o1', message: '支付成功' })
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('支付成功')
    expect(wrapper.get('[data-testid="notification-badge"]').text()).toBe('1')
    expect(localStorage.getItem('gbpm_notifications_u1')).toContain('PAY_SUCCESS')

    await wrapper.get('[data-testid="notification-button"]').trigger('click')
    expect(wrapper.text()).toContain('消息中心')
    expect(wrapper.text()).toContain('订单 o1')

    await wrapper.get('[data-testid="mark-all-read"]').trigger('click')
    expect(wrapper.find('[data-testid="notification-badge"]').exists()).toBe(false)
  })
})
