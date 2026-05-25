export type ApiResponse<T> = {
  code: string
  info: string
  data: T
}

export type GoodsMarketRequest = {
  userId: string
  source: string
  channel: string
  goodsId: string
}

export type GoodsMarketResponse = {
  activityId: number
  goods: {
    goodsId: string
    originalPrice: number
    deductionPrice: number
    payPrice: number
  }
  teamList: TeamItem[]
  teamStatistic: {
    allTeamCount: number
    allTeamCompleteCount: number
    allTeamUserCount: number
  }
}

export type TeamItem = {
  userId: string
  teamId: string
  activityId: number
  targetCount: number
  lockCount: number
  completeCount: number
  outTradeNo: string
  validStartTime: string
  validEndTime: string
  validTimeCountdown: string
}

export type TeamSummary = TeamItem & {
  remainingCount: number
}
