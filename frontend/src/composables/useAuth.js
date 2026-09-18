import {useRouter} from 'vue-router'
import {useAuthStore} from '../stores/auth'

const roleMap = {ADMIN: '管理员', LANDLORD: '房东', TENANT: '租客'}

/**
 * 登录态组合式函数：统一委托给 Pinia auth store。
 * loadUser() 返回 store 中的响应式用户对象（未登录跳转 /login）。
 */
export function useAuth() {
    const router = useRouter()
    const authStore = useAuthStore()

    function loadUser() {
        if (!authStore.isLoggedIn || !authStore.user) {
            router.push('/login')
            return null
        }
        return authStore.user
    }

    function handleLogout(cleanup) {
        if (cleanup) cleanup()
        authStore.logout()
        router.push('/login')
    }

    return {user: authStore.user, token: authStore.token, loadUser, handleLogout, authStore}
}

export function roleLabel(role) {
    return roleMap[role] || role
}
