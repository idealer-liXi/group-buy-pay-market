import { describe, expect, it } from 'vitest'
import { getCookie, setCookie } from './cookie'

describe('cookie helpers', () => {
  it('writes and reads login token', () => {
    setCookie('loginToken', 'abc123', 1)
    expect(getCookie('loginToken')).toBe('abc123')
  })
})
