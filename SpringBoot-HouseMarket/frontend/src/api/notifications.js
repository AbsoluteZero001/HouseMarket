import http from './http'

// 通知中心 API：数据源为后端 notification 表（未读/已读由用户维护）

export function getNotifications(limit = 50) {
  return http.get('/api/notifications', { params: { limit } })
}

export function getNotificationUnreadCount() {
    return http.get('/api/notifications/unread-count')
}

export function markNotificationRead(id) {
    return http.put(`/api/notifications/${id}/read`)
}

export function markAllNotificationsRead() {
    return http.put('/api/notifications/read-all')
}
