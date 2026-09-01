import http from './http'

export function getLandlordApplications(status) {
  return http.get('/api/admin/landlord-applications', { params: status ? { status } : {} })
}

export function approveLandlordApplication(id, note) {
  return http.put(`/api/admin/landlord-applications/${id}/approve`, { note })
}

export function rejectLandlordApplication(id, note) {
  return http.put(`/api/admin/landlord-applications/${id}/reject`, { note })
}

export function getMyLandlordApplication() {
  return http.get('/api/landlord/application')
}

// 租客主动提交房东入驻申请（被拒绝后可重新提交）
export function submitLandlordApplication(data) {
    return http.post('/api/landlord/application', data)
}

export function getLandlordProfile() {
    return http.get('/api/landlord/profile')
}
