import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { describe, expect, it, vi } from 'vitest'
import PurchaseHistoryPage from './PurchaseHistoryPage.vue'

const mocks = vi.hoisted(() => ({
  injectPayFormHtml: vi.fn(),
  queryPurchaseHistory: vi.fn().mockResolvedValue({
    code: '0000',
    data: {
      recordList: [
        { orderId: 'o1', outTradeNo: 'o1', productId: '9890001', productName: '拼团商品', status: 'PAY_WAIT', statusType: 'WAIT_PAY', purchaseType: 'GROUP_BUY', totalAmount: 99, payAmount: 79, payUrl: '<form id="pay"></form>' },
        { orderId: 'o2', outTradeNo: 'o2', productId: '9890002', productName: '待成团商品', status: 'PAY_SUCCESS', statusType: 'GROUP_WAIT', purchaseType: 'GROUP_BUY', totalAmount: 89.9, payAmount: 79.9 },
        { orderId: 'o3', outTradeNo: 'o3', productId: '9890005', productName: '普通商品', status: 'PAY_SUCCESS', statusType: 'GROUP_SUCCESS', purchaseType: 'PLAIN', totalAmount: 19.9, payAmount: 19.9 },
        { orderId: 'o4', outTradeNo: 'o4', productId: '9890006', productName: '关闭商品', status: 'CLOSE', statusType: 'CLOSED', purchaseType: 'PLAIN', totalAmount: 29.9, payAmount: 29.9 },
        { orderId: 'o5', outTradeNo: 'o5', productId: '9890007', productName: '拼团成功商品', status: 'MARKET', statusType: 'GROUP_SUCCESS', purchaseType: 'GROUP_BUY', totalAmount: 39.9, payAmount: 29.9 },
        { orderId: 'o6', outTradeNo: 'o6', productId: '9890008', productName: '普通待付款商品', status: 'PAY_WAIT', statusType: 'WAIT_PAY', purchaseType: 'PLAIN', totalAmount: 49.9, payAmount: 49.9, payUrl: '<form id="plain-pay"></form>' }
      ]
    }
  }),
  cancelOrder: vi.fn().mockResolvedValue({
    code: '0000',
    info: '退单成功',
    data: true
  }),
  refundMarketPayOrder: vi.fn().mockResolvedValue({
    code: '0000',
    info: '成功',
    data: { userId: 'user-token', orderId: 'o1', teamId: 't1', code: '0000', info: '退单成功' }
  }),
  refundPaidOrder: vi.fn().mockResolvedValue({
    code: '0000',
    info: '退款成功',
    data: true
  })
}))

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

vi.mock('../lib/cookie', () => ({
  getCookie: vi.fn(() => 'user-token')
}))

vi.mock('../lib/order', () => ({
  queryPurchaseHistory: mocks.queryPurchaseHistory,
  cancelOrder: mocks.cancelOrder,
  refundMarketPayOrder: mocks.refundMarketPayOrder,
  refundPaidOrder: mocks.refundPaidOrder
}))

vi.mock('../lib/pay', () => ({
  injectPayFormHtml: mocks.injectPayFormHtml
}))

describe('PurchaseHistoryPage', () => {
  it('renders purchase records by status and purchase type', async () => {
    vi.stubGlobal('WebSocket', MockWebSocket)

    const router = createRouter({
      history: createWebHistory(),
      routes: [{ path: '/orders', component: PurchaseHistoryPage }]
    })
    router.push('/orders')
    await router.isReady()

    const wrapper = mount(PurchaseHistoryPage, {
      global: { plugins: [router] }
    })

    await new Promise((resolve) => setTimeout(resolve, 0))
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(wrapper.text()).toContain('购物记录')
    expect(wrapper.text()).toContain('拼团商品')
    expect(wrapper.text()).toContain('拼团购买')
    expect(wrapper.text()).toContain('待付款')
    expect(wrapper.text()).toContain('普通商品')
    expect(wrapper.text()).toContain('普通购买')
    expect(wrapper.text()).toContain('拼团中')
    expect(wrapper.text()).toContain('已完成')
    expect(wrapper.text()).toContain('已关闭')

    await wrapper.get('.continue-pay-btn').trigger('click')
    expect(mocks.injectPayFormHtml).toHaveBeenCalledWith('<form id="pay"></form>')

    expect(wrapper.findAll('.refund-btn')).toHaveLength(5)
    expect(wrapper.text()).toContain('普通商品')
    await wrapper.get('.refund-btn').trigger('click')
    expect(mocks.refundMarketPayOrder).toHaveBeenCalledWith({
      userId: 'user-token',
      outTradeNo: 'o1',
      source: 's01',
      channel: 'c01'
    })
    await new Promise((resolve) => setTimeout(resolve, 0))
    expect(wrapper.text()).not.toContain('退单成功')

    await wrapper.findAll('.refund-btn')[2].trigger('click')
    expect(mocks.refundPaidOrder).toHaveBeenCalledWith({
      userId: 'user-token',
      orderId: 'o3'
    })
    expect(mocks.refundMarketPayOrder).not.toHaveBeenCalledWith({
      userId: 'user-token',
      outTradeNo: 'o3',
      source: 's01',
      channel: 'c01'
    })

    await wrapper.findAll('.refund-btn')[4].trigger('click')
    expect(mocks.cancelOrder).toHaveBeenCalledWith({
      userId: 'user-token',
      orderId: 'o6'
    })

    window.dispatchEvent(new CustomEvent('gbpm:user-notification', {
      detail: { type: 'GROUP_SUCCESS', teamId: 't1', message: '拼团已完成', outTradeNoList: ['o2'] }
    }))
    await new Promise((resolve) => setTimeout(resolve, 0))
    expect(wrapper.text()).not.toContain('拼团已完成')
    expect(mocks.queryPurchaseHistory).toHaveBeenCalledWith('user-token')

    await wrapper.get('[data-filter="CLOSED"]').trigger('click')
    expect(wrapper.text()).toContain('关闭商品')
    expect(wrapper.text()).toContain('已关闭')
    expect(wrapper.text()).not.toContain('普通商品')

    await wrapper.get('[data-filter="GROUP_WAIT"]').trigger('click')
    expect(wrapper.text()).toContain('待成团商品')
    expect(wrapper.text()).not.toContain('拼团商品')
  })
})
