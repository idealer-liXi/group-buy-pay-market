import { describe, expect, it } from 'vitest'
import router from './router'

describe('frontend bootstrap', () => {
  it('defines login and goods routes', () => {
    const paths = router.getRoutes().map((route) => route.path)
    expect(paths).toContain('/login')
    expect(paths).toContain('/goods/:goodsId')
  })
})
