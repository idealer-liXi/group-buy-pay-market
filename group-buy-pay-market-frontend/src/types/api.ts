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
    goodsName?: string | null
    originalPrice: number
    deductionPrice: number
    payPrice: number
    coverImageUrl?: string | null
    imageUrls?: string[]
  }
  teamList: TeamItem[]
  teamStatistic: {
    allTeamCount: number
    allTeamCompleteCount: number
    allTeamUserCount: number
  }
  activity?: ActivitySummary | null
  isVisible?: boolean
  isEnable?: boolean
}

export type ActivitySummary = {
  activityId: number
  activityName: string
  groupType: number
  target: number
  validTime: number
  tagId?: string | null
  tagName?: string | null
  tagScope?: string | null
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
  deductionPrice?: number | null
  payPrice?: number | null
  activityId?: number | null
  activityName?: string | null
  tagId?: string | null
  tagName?: string | null
  tagScope?: string | null
  coverImageUrl?: string | null
}

export type UserTagItem = {
  tagId: string
  tagName: string
  tagDesc?: string | null
  statistics?: number
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

export type CreatePayOrderResponse = {
  orderId?: string
  payUrl: string
  reusedPayOrder?: boolean
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

export type CancelOrderRequest = {
  userId: string
  orderId: string
}

export type RefundPaidOrderRequest = {
  userId: string
  orderId: string
}

export type UserNotificationMessage = {
  type: 'GROUP_SUCCESS' | 'GROUP_FAIL' | 'PAY_SUCCESS' | 'REFUND_SUCCESS' | string
  orderId?: string
  teamId?: string
  message: string
  outTradeNoList?: string[]
}
