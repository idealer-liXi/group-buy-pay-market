import axios from 'axios'

type RuntimeLocation = Pick<Location | URL, 'protocol' | 'hostname' | 'port'>

export function resolveApiBaseURL(location: RuntimeLocation = window.location) {
  const envBaseURL = import.meta.env.VITE_API_BASE_URL
  if (envBaseURL) {
    return envBaseURL.replace(/\/$/, '')
  }

  const isLocal = location.hostname === '127.0.0.1' || location.hostname === 'localhost'
  if (isLocal) {
    return `${location.protocol}//${location.hostname}:8080`
  }

  if (!isLocal && location.port === '15173') {
    return `${location.protocol}//${location.hostname}:18080`
  }

  return 'http://127.0.0.1:8080'
}

export const http = axios.create({
  baseURL: resolveApiBaseURL(),
  timeout: 10000
})
