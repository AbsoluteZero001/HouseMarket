import http from './http'

export function getAppointments(status) {
  return http.get('/api/appointments', { params: status ? { status } : {} })
}

export function createAppointment(data) {
  return http.post('/api/appointments', data)
}

export function approveAppointment(id) {
  return http.put(`/api/appointments/${id}/approve`)
}

export function rejectAppointment(id) {
  return http.put(`/api/appointments/${id}/reject`)
}

export function cancelAppointment(id) {
  return http.put(`/api/appointments/${id}/cancel`)
}

export function deleteAppointment(id) {
  return http.delete(`/api/appointments/${id}`)
}
