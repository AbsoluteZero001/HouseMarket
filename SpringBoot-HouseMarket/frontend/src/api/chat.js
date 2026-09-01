import http from './http'

// 聊天 REST API：历史消息 / 会话列表 / 未读数 / 已读标记（实时收发走 WebSocket）

export function getConversations() {
    return http.get('/api/chat/conversations')
}

export function getMessages(partnerId, {houseId, page = 1, pageSize = 50} = {}) {
    const params = {partnerId, page, pageSize}
    if (houseId) params.houseId = houseId
    return http.get('/api/chat/messages', {params})
}

export function getUnreadCount() {
    return http.get('/api/chat/unread-count')
}

export function markRead(partnerId) {
    return http.put(`/api/chat/read/${partnerId}`)
}
