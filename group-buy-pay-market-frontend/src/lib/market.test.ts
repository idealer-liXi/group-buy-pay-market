import { describe, expect, it } from 'vitest'
import { resolveGoodsName, toTeamSummary } from './market'

describe('market mapping', () => {
  it('calculates remaining slots from target and lock count', () => {
    expect(toTeamSummary({ targetCount: 3, lockCount: 1 }).remainingCount).toBe(2)
  })

  it('resolves goods name from sku list by goodsId', () => {
    expect(
      resolveGoodsName('9890001', [
        { goodsId: '9890001', goodsName: '手写MyBatis：渐进式源码实践（全彩）', originalPrice: 89.9 }
      ])
    ).toBe('手写MyBatis：渐进式源码实践（全彩）')
  })

  it('falls back to goodsId when sku list does not contain the item', () => {
    expect(resolveGoodsName('9890002', [])).toBe('商品 9890002')
  })

  it('calculates live countdown from validEndTime', () => {
    const summary = toTeamSummary({
      targetCount: 3,
      lockCount: 1,
      validEndTime: '2026-05-27T20:57:14+08:00',
      validTimeCountdown: 'stale'
    }, new Date('2026-05-27T20:55:10+08:00').getTime())

    expect(summary.validTimeCountdown).toBe('00:02:04')
    expect(summary.isExpired).toBe(false)
  })

  it('marks teams expired when validEndTime has passed', () => {
    const summary = toTeamSummary({
      targetCount: 3,
      lockCount: 1,
      validEndTime: '2026-05-27T20:57:14+08:00',
      validTimeCountdown: 'stale'
    }, new Date('2026-05-27T20:57:15+08:00').getTime())

    expect(summary.validTimeCountdown).toBe('已结束')
    expect(summary.isExpired).toBe(true)
  })
})
