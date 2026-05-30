import { http } from './http'
import type {
  ApiResponse,
  CancelOrderRequest,
  PurchaseHistoryResponse,
  RefundPaidOrderRequest,
  RefundMarketPayOrderRequest,
  RefundMarketPayOrderResponse
} from '../types/api'

export async function queryPurchaseHistory(userId: string) {
  const { data } = await http.get<ApiResponse<PurchaseHistoryResponse>>(
    '/api/v1/gbm/order/query_purchase_history',
    { params: { userId } }
  )
  return data
}

export async function refundMarketPayOrder(payload: RefundMarketPayOrderRequest) {
  const { data } = await http.post<ApiResponse<RefundMarketPayOrderResponse>>(
    '/api/v1/gbm/trade/refund_market_pay_order',
    payload
  )
  return data
}

export async function cancelOrder(payload: CancelOrderRequest) {
  const { data } = await http.post<ApiResponse<boolean>>(
    '/api/v1/gbm/order/cancel_order',
    payload
  )
  return data
}

export async function refundPaidOrder(payload: RefundPaidOrderRequest) {
  const { data } = await http.post<ApiResponse<boolean>>(
    '/api/v1/gbm/order/refund_paid_order',
    payload
  )
  return data
}
