import FingerprintJS from '@fingerprintjs/fingerprintjs'
import { http } from './http'

export type SimpleResponse<T> = {
  code: string
  info: string
  data: T
}

export type FingerprintLoginResponse = {
  userId: string
  displayName: string
}

export async function getFingerprint(): Promise<string> {
  const fp = await FingerprintJS.load()
  const result = await fp.get()
  return result.visitorId
}

export async function fetchWeixinQrCodeTicket(sceneStr: string) {
  const { data } = await http.get<SimpleResponse<string>>('/api/v1/login/weixin_qrcode_ticket_scene', {
    params: { sceneStr }
  })
  return data
}

export async function checkLogin(ticket: string, sceneStr: string) {
  const { data } = await http.get<SimpleResponse<string>>('/api/v1/login/check_login_scene', {
    params: { ticket, sceneStr }
  })
  return data
}

export async function fingerprintLogin(visitorId: string) {
  const { data } = await http.post<SimpleResponse<FingerprintLoginResponse>>('/api/v1/login/fingerprint', { visitorId })
  return data
}
