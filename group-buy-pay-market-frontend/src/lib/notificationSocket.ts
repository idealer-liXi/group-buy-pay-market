import { http } from './http'
import type { UserNotificationMessage } from '../types/api'

export function openNotificationSocket(
  userId: string,
  onMessage: (message: UserNotificationMessage) => void
) {
  const baseURL = http.defaults.baseURL || window.location.origin
  const wsBaseURL = baseURL.replace(/^http/, 'ws')
  const socket = new WebSocket(`${wsBaseURL}/ws/notifications?userId=${encodeURIComponent(userId)}`)

  socket.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data) as UserNotificationMessage
      if (message.type === 'GROUP_SUCCESS') {
        onMessage(message)
      }
    } catch {
      // Ignore malformed notification payloads.
    }
  }

  return socket
}
