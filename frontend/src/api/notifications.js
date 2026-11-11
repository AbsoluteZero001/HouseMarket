import http from './http'

// 通知中心 API：数据源为后端 notification 表（未读/已读由用户维护）

export function getNotifications(limit = 50) {
  return http.get('/api/notifications', { params: { limit } })
}

// 分页查询通知：后端返回 { notifications, unread, total, page, size, hasMore }。
// 之前只有 limit 一个参数，最多只能拿到最新 100 条，用户看不到更早的通知。
export function getNotificationsPage(page = 1, size = 20) {
  return http.get('/api/notifications', { params: { page, size } })
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
