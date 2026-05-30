import { http } from './http'
import type { ApiResponse, CreatePayOrderResponse } from '../types/api'

export async function createPayOrder(payload: {
  userId: string
  productId: string
  activityId?: number
  teamId?: string | null
  marketType: number
}) {
  const { data } = await http.post<ApiResponse<CreatePayOrderResponse>>('/api/v1/alipay/create_pay_order', payload)
  return data
}

export function injectPayFormHtml(formHtml: string) {
  document.querySelectorAll('form').forEach((form) => form.remove())
  document.body.insertAdjacentHTML('beforeend', formHtml)
  const form = document.body.querySelector('form') as HTMLFormElement | null
  form?.setAttribute('target', '_blank')
  return form
}
