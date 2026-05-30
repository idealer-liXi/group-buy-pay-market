import { describe, expect, it } from 'vitest'
import { resolveApiBaseURL } from './http'

describe('resolveApiBaseURL', () => {
  it('uses the public backend port when the frontend is opened through frp', () => {
    const location = new URL('http://110.42.207.45:15173/goods')

    expect(resolveApiBaseURL(location)).toBe('http://110.42.207.45:18080')
  })

  it('keeps the local hostname so auth cookies are sent to websocket handshakes', () => {
    expect(resolveApiBaseURL(new URL('http://localhost:5173/goods'))).toBe('http://localhost:8080')
    expect(resolveApiBaseURL(new URL('http://127.0.0.1:5173/goods'))).toBe('http://127.0.0.1:8080')
  })
})
