import { http } from './http'
import type { ApiResponse, UserTagItem } from '../types/api'

export function queryUserTags(userId: string) {
  return http.get<ApiResponse<{ tagList: UserTagItem[] }>>('/api/v1/user/tags', { params: { userId } })
    .then((res) => res.data)
}
