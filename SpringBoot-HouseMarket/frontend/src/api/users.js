import http from './http'

export function getUsers() {
  return http.get('/user')
}

export function getUserById(id) {
  return http.get(`/user/${id}`)
}

export function getCurrentUser() {
  return http.get('/user/current')
}

export function updateUser(id, data) {
  return http.put(`/user/${id}`, data)
}

export function deleteUser(id) {
  return http.delete(`/user/${id}`)
}

export function changePassword(id, oldPassword, newPassword) {
  return http.put(`/user/${id}/password`, { oldPassword, newPassword })
}
