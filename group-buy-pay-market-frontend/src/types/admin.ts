export type AdminLoginRequest = {
  username: string
  password: string
}

export type AdminGoodsItem = {
  goodsId: string
  goodsName: string
  originalPrice: number
  status: number
}

export type AdminDiscountItem = {
  discountId: string
  discountName: string
  discountDesc: string
  discountType: number
  marketPlan: string
  marketExpr: string
  tagId?: string | null
  status: number
}

export type AdminActivityItem = {
  activityId: number
  activityName: string
  goodsId: string
  discountId: string
  groupType: number
  takeLimitCount: number
  target: number
  validTime: number
  status: number
  startTime: string
  endTime: string
  tagId?: string | null
  tagScope?: string | null
}
