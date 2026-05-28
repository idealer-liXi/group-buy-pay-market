import { http } from './http'
import { getAdminToken } from './admin-auth'
import type { ApiResponse } from '../types/api'
import type { AdminActivityItem, AdminDiscountItem, AdminGoodsCreatePayload, AdminGoodsCreateResponse, AdminGoodsImageResponse, AdminGoodsItem, AdminLoginRequest, AdminTagItem, AdminTagMemberItem, AdminUserItem } from '../types/admin'

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

export function createAdminGoods(payload: AdminGoodsCreatePayload) {
  return http.post<ApiResponse<AdminGoodsCreateResponse>>('/api/v1/admin/goods', payload, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function uploadAdminGoodsImage(goodsId: string, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<ApiResponse<AdminGoodsImageResponse>>(`/api/v1/admin/goods/${goodsId}/images`, formData, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function deleteAdminGoodsImage(goodsId: string, imageId: number) {
  return http.delete<ApiResponse<void>>(`/api/v1/admin/goods/${goodsId}/images/${imageId}`, { headers: adminHeaders() })
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

export function queryAdminUsers(keyword = '') {
  return http.get<ApiResponse<{ userList: AdminUserItem[] }>>('/api/v1/admin/users', { headers: adminHeaders(), params: { keyword } })
    .then((res) => res.data)
}

export function queryAdminUserTags(userId: string) {
  return http.get<ApiResponse<{ tagList: AdminTagItem[] }>>(`/api/v1/admin/users/${userId}/tags`, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function queryAdminTags() {
  return http.get<ApiResponse<{ tagList: AdminTagItem[] }>>('/api/v1/admin/tags', { headers: adminHeaders() })
    .then((res) => res.data)
}

export function createAdminTag(payload: Pick<AdminTagItem, 'tagId' | 'tagName' | 'tagDesc'>) {
  return http.post<ApiResponse<void>>('/api/v1/admin/tags', payload, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function updateAdminTag(tagId: string, payload: Pick<AdminTagItem, 'tagName' | 'tagDesc'>) {
  return http.put<ApiResponse<void>>(`/api/v1/admin/tags/${tagId}`, payload, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function queryAdminTagMembers(tagId: string) {
  return http.get<ApiResponse<{ memberList: AdminTagMemberItem[] }>>(`/api/v1/admin/tags/${tagId}/members`, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function addAdminTagMember(tagId: string, userId: string) {
  return http.post<ApiResponse<void>>(`/api/v1/admin/tags/${tagId}/members`, { userId }, { headers: adminHeaders() })
    .then((res) => res.data)
}

export function removeAdminTagMember(tagId: string, userId: string) {
  return http.delete<ApiResponse<void>>(`/api/v1/admin/tags/${tagId}/members/${userId}`, { headers: adminHeaders() })
    .then((res) => res.data)
}
