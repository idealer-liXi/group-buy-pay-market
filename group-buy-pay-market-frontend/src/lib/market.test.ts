import { describe, expect, it } from 'vitest'
import { toTeamSummary } from './market'

describe('market mapping', () => {
  it('calculates remaining slots from target and lock count', () => {
    expect(toTeamSummary({ targetCount: 3, lockCount: 1 }).remainingCount).toBe(2)
  })
})
