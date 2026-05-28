import { http } from './http'
import type { ApiResponse, GoodsMarketRequest, GoodsMarketResponse, SkuItem, SkuListResponse, TeamSummary } from '../types/api'

export async function queryGroupBuyMarketConfig(payload: GoodsMarketRequest) {
  const { data } = await http.post<ApiResponse<GoodsMarketResponse>>(
    '/api/v1/gbm/index/query_group_buy_market_config',
    payload
  )
  return data
}

export function toTeamSummary<T extends { targetCount: number; lockCount: number; validEndTime?: string; validTimeCountdown?: string }>(team: T, now = Date.now()) {
  const countdown = formatCountdown(team.validEndTime, now, team.validTimeCountdown)

  return {
    ...team,
    remainingCount: Math.max(team.targetCount - team.lockCount, 0),
    validTimeCountdown: countdown.text,
    isExpired: countdown.isExpired
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

export function toTeamSummaries(teams: GoodsMarketResponse['teamList'], now = Date.now()): TeamSummary[] {
  return teams.map((team) => toTeamSummary(team, now))
}

function formatCountdown(validEndTime: string | undefined, now: number, fallback = '') {
  if (!validEndTime) {
    return { text: fallback, isExpired: false }
  }

  const diff = new Date(validEndTime).getTime() - now
  if (!Number.isFinite(diff) || diff <= 0) {
    return { text: '已结束', isExpired: true }
  }

  const totalSeconds = Math.floor(diff / 1000)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  const pad = (value: number) => String(value).padStart(2, '0')

  return { text: `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`, isExpired: false }
}

export async function querySkuList() {
  const { data } = await http.get<ApiResponse<SkuListResponse>>(
    '/api/v1/gbm/index/query_sku_list'
  )
  return data
}

export function resolveGoodsName(goodsId: string, skuList: SkuItem[]) {
  return skuList.find((item) => item.goodsId === goodsId)?.goodsName ?? `商品 ${goodsId}`
}

export function goodsImageUrl(_goodsId: string): string {
  return ''
}
