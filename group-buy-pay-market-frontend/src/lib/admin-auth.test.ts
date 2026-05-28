import { describe, expect, it } from 'vitest'
import { clearAdminToken, getAdminToken, setAdminToken } from './admin-auth'

describe('admin auth helpers', () => {
  it('stores and clears admin token', () => {
    setAdminToken('admin-token')
    expect(getAdminToken()).toBe('admin-token')
    clearAdminToken()
    expect(getAdminToken()).toBe('')
  })
})
