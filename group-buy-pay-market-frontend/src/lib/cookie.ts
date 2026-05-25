export function setCookie(name: string, value: string, days: number) {
  const date = new Date()
  date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000)
  document.cookie = `${name}=${encodeURIComponent(value)}; expires=${date.toUTCString()}; path=/`
}

export function getCookie(name: string) {
  const cookieArr = document.cookie.split(';')
  for (const item of cookieArr) {
    const cookiePair = item.split('=')
    if (name === cookiePair[0]?.trim()) {
      return decodeURIComponent(cookiePair[1] ?? '')
    }
  }
  return null
}
