import { http } from './http'
import type { ApiResponse } from '../types/api'

export async function createPayOrder(payload: {
  userId: string
  productId: string
  activityId?: number
  teamId?: string | null
  marketType: number
}) {
  const { data } = await http.post<ApiResponse<string>>('/api/v1/alipay/create_pay_order', payload)
  return data
}

export function injectPayFormHtml(formHtml: string) {
  document.querySelectorAll('form').forEach((form) => form.remove())
  document.body.insertAdjacentHTML('beforeend', formHtml)
}
