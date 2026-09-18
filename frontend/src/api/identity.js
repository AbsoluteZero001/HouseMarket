import http from './http'

// 实名认证（人工审核制）API

export function getMyIdentityVerification() {
    return http.get('/api/identity-verification/me')
}

export function submitIdentityVerification(data) {
    return http.post('/api/identity-verification', data)
}

// 管理员实名审核
export function getAdminIdentityVerifications(status) {
    return http.get('/api/admin/identity-verifications', {params: status ? {status} : {}})
}

export function approveIdentityVerification(id, note) {
    return http.put(`/api/admin/identity-verifications/${id}/approve`, {note})
}

export function rejectIdentityVerification(id, note) {
    return http.put(`/api/admin/identity-verifications/${id}/reject`, {note})
}
