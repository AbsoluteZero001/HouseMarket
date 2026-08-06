import http from './http'

export function getNotifications(limit = 50) {
  return http.get('/api/notifications', { params: { limit } })
}
