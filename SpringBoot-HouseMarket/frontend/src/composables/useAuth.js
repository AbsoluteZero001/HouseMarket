import {ref} from 'vue'
import {useRouter} from 'vue-router'

const roleMap = {ADMIN: '管理员', LANDLORD: '房东', TENANT: '租客'}

export function useAuth() {
    const router = useRouter()
    const user = ref(null)
    const token = ref('')

    function loadUser() {
        token.value = localStorage.getItem('token') || ''
        try {
            user.value = JSON.parse(localStorage.getItem('user') || 'null')
        } catch {
            user.value = null
        }
        if (!user.value || !token.value) {
            router.push('/login')
            return null
        }
        return user.value
    }

    function handleLogout(cleanup) {
        if (cleanup) cleanup()
        localStorage.clear()
        router.push('/login')
    }

    return {user, token, loadUser, handleLogout}
}

export function roleLabel(role) {
    return roleMap[role] || role
}
