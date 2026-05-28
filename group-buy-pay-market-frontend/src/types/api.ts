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
  activityId: number | null
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
  isExpired: boolean
}

export type SkuItem = {
  goodsId: string
  goodsName: string
  originalPrice: number
}

export type SkuListResponse = {
  skuList: SkuItem[]
}

export type PurchaseRecord = {
  orderId: string
  outTradeNo: string
  productId: string
  productName: string
  orderTime?: string
  totalAmount: number
  payAmount: number
  payUrl?: string
  status: string
  statusType: 'WAIT_PAY' | 'GROUP_WAIT' | 'GROUP_SUCCESS' | 'CLOSED'
  marketType: number
  purchaseType: 'PLAIN' | 'GROUP_BUY'
}

export type PurchaseHistoryResponse = {
  recordList: PurchaseRecord[]
}

export type RefundMarketPayOrderRequest = {
  userId: string
  outTradeNo: string
  source: string
  channel: string
}

export type RefundMarketPayOrderResponse = {
  userId: string
  orderId: string
  teamId: string
  code: string
  info: string
}

export type UserNotificationMessage = {
  type: 'GROUP_SUCCESS'
  teamId: string
  message: string
  outTradeNoList: string[]
}
