import http from './http'

export function login(data) {
  return http.post('/api/v1/auth/login', data)
}

export function register(data) {
  return http.post('/api/v1/auth/register', data)
}

export function getCaptcha() {
  return http.get('/api/v1/auth/captcha', { responseType: 'blob' })
}
