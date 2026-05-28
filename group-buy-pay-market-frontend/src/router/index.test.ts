import { describe, expect, it, vi } from 'vitest'

vi.mock('../lib/cookie', () => ({
  getCookie: vi.fn(() => 'user-token')
}))

vi.mock('../lib/admin-auth', () => ({
  getAdminToken: vi.fn(() => '')
}))

describe('router', () => {
  it('registers purchase history route', async () => {
    const { default: router } = await import('./index')

    expect(router.resolve('/orders').matched.length).toBeGreaterThan(0)
  })
})
