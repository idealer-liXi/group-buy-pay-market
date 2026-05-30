export type AdminLoginRequest = {
  username: string
  password: string
}

export type AdminGoodsImageItem = {
  imageId: number
  imageUrl: string
  sortOrder: number
}

export type AdminGoodsItem = {
  goodsId: string
  goodsName: string
  originalPrice: number
  status: number
  coverImageUrl?: string | null
  imageList?: AdminGoodsImageItem[]
}

export type AdminGoodsCreatePayload = {
  goodsName: string
  originalPrice: number
}

export type AdminGoodsCreateResponse = {
  goodsId: string
}

export type AdminGoodsImageResponse = AdminGoodsImageItem

export type AdminDiscountItem = {
  discountId: string
  discountName: string
  discountDesc: string
  discountType: number
  marketPlan: string
  marketExpr: string
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

export type AdminUserItem = {
  userId: string
  displayName: string
  loginType: string
  status: number
  firstLoginTime?: string | null
  lastLoginTime?: string | null
}

export type AdminTagItem = {
  tagId: string
  tagName: string
  tagDesc: string
  statistics: number
}

export type AdminTagMemberItem = {
  userId: string
  displayName: string
  loginType: string
  status: number
}
