import { http } from './http'
import { getAdminToken } from './admin-auth'
import type { ApiResponse } from '../types/api'
import type { AdminActivityItem, AdminDiscountItem, AdminGoodsItem, AdminLoginRequest } from '../types/admin'

function adminHeaders() {
  return { 'X-Admin-Token': getAdminToken() }
}

export function adminLogin(payload: AdminLoginRequest) {
  return http.post<ApiResponse<{ adminToken: string; displayName: string }>>('/api/v1/admin/login', payload)
    .then((res) => res.data)
}

export function queryAdminGoods() {
  return http.get<ApiResponse<{ goodsList: AdminGoodsItem[] }>>('/api/v1/admin/goods', { headers: adminHeaders() })
    .then((res) => res.data)
}

export function createAdminGoods(payload: Pick<AdminGoodsItem, 'goodsId' | 'goodsName' | 'originalPrice'>) {
  return http.post<ApiResponse<void>>('/api/v1/admin/goods', payload, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function updateAdminGoods(goodsId: string, payload: Pick<AdminGoodsItem, 'goodsName' | 'originalPrice'>) {
  return http.put<ApiResponse<void>>(`/api/v1/admin/goods/${goodsId}`, payload, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function updateAdminGoodsStatus(goodsId: string, status: number) {
  return http.put<ApiResponse<void>>(`/api/v1/admin/goods/${goodsId}/status`, { status }, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function queryAdminDiscounts() {
  return http.get<ApiResponse<{ discountList: AdminDiscountItem[] }>>('/api/v1/admin/discounts', { headers: adminHeaders() })
    .then((res) => res.data)
}

export function createAdminDiscount(payload: Omit<AdminDiscountItem, 'status'>) {
  return http.post<ApiResponse<void>>('/api/v1/admin/discounts', payload, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function updateAdminDiscount(discountId: string, payload: Omit<AdminDiscountItem, 'discountId' | 'status'>) {
  return http.put<ApiResponse<void>>(`/api/v1/admin/discounts/${discountId}`, payload, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function updateAdminDiscountStatus(discountId: string, status: number) {
  return http.put<ApiResponse<void>>(`/api/v1/admin/discounts/${discountId}/status`, { status }, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function queryAdminActivities() {
  return http.get<ApiResponse<{ activityList: AdminActivityItem[] }>>('/api/v1/admin/activities', { headers: adminHeaders() })
    .then((res) => res.data)
}

export function createAdminActivity(payload: Record<string, unknown>) {
  return http.post<ApiResponse<void>>('/api/v1/admin/activities', payload, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function updateAdminActivity(activityId: number, payload: Record<string, unknown>) {
  return http.put<ApiResponse<void>>(`/api/v1/admin/activities/${activityId}`, payload, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function updateAdminActivityStatus(activityId: number, status: number) {
  return http.put<ApiResponse<void>>(`/api/v1/admin/activities/${activityId}/status`, { status }, { headers: adminHeaders() })
    .then((res) => res.data)
}
