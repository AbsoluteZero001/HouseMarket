import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import {login as loginApi} from '../api/auth'

function safeParseUser() {
    try {
        return JSON.parse(localStorage.getItem('user') || 'null')
    } catch {
        return null
    }
}

/**
 * 登录态单一数据源：
 * - Pinia store 为运行态真源；localStorage 仅作刷新恢复的持久化镜像。
 * - 所有视图/组件一律通过本 store 读写用户信息，禁止各自维护副本。
 * - updateUser 保证响应式更新 + localStorage 同步落盘。
 */
export const useAuthStore = defineStore('auth', () => {
    const token = ref(localStorage.getItem('token') || '')
    const user = ref(safeParseUser())

    const isLoggedIn = computed(() => !!token.value)
    const userRole = computed(() => user.value?.role || '')
    const userId = computed(() => user.value?.id || null)

    function persist() {
        if (token.value) localStorage.setItem('token', token.value)
        else localStorage.removeItem('token')
        if (user.value) localStorage.setItem('user', JSON.stringify(user.value))
        else localStorage.removeItem('user')
    }

    async function login(credentials) {
        const res = await loginApi(credentials)
        if (res.data.code === 200) {
            token.value = res.data.token || ''
            user.value = res.data.data || null
            persist()
        }
        return res.data
    }

    function logout() {
        token.value = ''
        user.value = null
        persist()
    }

    /**
     * 登录态下局部更新当前用户（昵称/头像/实名状态等），视图自动响应。
     */
    function updateUser(partial) {
        if (!user.value) return
        user.value = {...user.value, ...partial}
        persist()
    }

    /** 401 拦截器专用：仅清理凭据，不做路由跳转（由拦截器统一决定） */
    function clearCredentials() {
        token.value = ''
        user.value = null
        persist()
    }

    return {token, user, isLoggedIn, userRole, userId, login, logout, updateUser, clearCredentials}
})
