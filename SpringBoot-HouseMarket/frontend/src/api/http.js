import axios from 'axios'
import router from '../router'

const http = axios.create({
  baseURL: '',
  timeout: 30000
})

http.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

http.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      console.error('[HTTP] Error', error.response.status, error.config?.method?.toUpperCase(), error.config?.url)
      console.error('[HTTP] Response data:', error.response.data)
      if (error.response.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        router.push('/login')
      }
    } else if (error.request) {
      console.error('[HTTP] Network error - no response received for', error.config?.method?.toUpperCase(), error.config?.url)
    }
    return Promise.reject(error)
  }
)

export default http
