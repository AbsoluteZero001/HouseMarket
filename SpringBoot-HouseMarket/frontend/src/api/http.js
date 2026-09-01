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

let redirecting401 = false

http.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
        const {status, data} = error.response
        if (status === 401) {
            // 统一处理：清理本地登录态并跳转登录页（带提示），避免页面静默变匿名
        localStorage.removeItem('token')
        localStorage.removeItem('user')
            window.dispatchEvent(new CustomEvent('auth:expired', {detail: {message: data?.message || '登录状态已失效，请重新登录'}}))
            if (!redirecting401) {
                redirecting401 = true
                const current = router.currentRoute.value
                router.push({path: '/login', query: current.path !== '/login' ? {redirect: current.fullPath} : {}})
                setTimeout(() => {
                    redirecting401 = false
                }, 1000)
            }
        } else if (status === 403) {
            window.dispatchEvent(new CustomEvent('app:error', {detail: {message: data?.message || '没有权限执行该操作'}}))
        }
    }
    return Promise.reject(error)
  }
)

export default http
