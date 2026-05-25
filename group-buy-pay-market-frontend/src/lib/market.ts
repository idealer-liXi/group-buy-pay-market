import { http } from './http'
import type { ApiResponse, GoodsMarketRequest, GoodsMarketResponse, TeamSummary } from '../types/api'

export async function queryGroupBuyMarketConfig(payload: GoodsMarketRequest) {
  const { data } = await http.post<ApiResponse<GoodsMarketResponse>>(
    '/api/v1/gbm/index/query_group_buy_market_config',
    payload
  )
  return data
}

export function toTeamSummary<T extends { targetCount: number; lockCount: number }>(team: T) {
  return {
    ...team,
    remainingCount: Math.max(team.targetCount - team.lockCount, 0)
  }
}

export function obfuscateUserId(userId: string) {
  if (userId.length <= 4) {
    return userId
  }

  const start = userId.slice(0, 2)
  const end = userId.slice(-2)
  const middle = '*'.repeat(userId.length - 4)
  return `${start}${middle}${end}`
}

export function toTeamSummaries(teams: GoodsMarketResponse['teamList']): TeamSummary[] {
  return teams.map((team) => toTeamSummary(team))
}
