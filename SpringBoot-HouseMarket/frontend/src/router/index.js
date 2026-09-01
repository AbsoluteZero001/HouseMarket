import {createRouter, createWebHistory} from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/HomeView.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/RegisterView.vue') },
    {
        path: '/tenant',
        name: 'Tenant',
        component: () => import('../views/TenantView.vue'),
        meta: {requiresAuth: true, roles: ['TENANT']}
    },
    {
        path: '/landlord',
        name: 'Landlord',
        component: () => import('../views/LandlordView.vue'),
        meta: {requiresAuth: true, roles: ['LANDLORD']}
    },
    {
        path: '/admin',
        name: 'Admin',
        component: () => import('../views/AdminView.vue'),
        meta: {requiresAuth: true, roles: ['ADMIN']}
    },
    {path: '/house/:id', name: 'HouseDetail', component: () => import('../views/HouseDetailView.vue')},
    {path: '/403', name: 'Forbidden', component: () => import('../views/Forbidden.vue')},
    {path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/NotFound.vue')}
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() { return { top: 0 } }
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
    let user = null
    try {
        user = JSON.parse(localStorage.getItem('user') || 'null')
    } catch {
        user = null
    }
    if (to.meta.requiresAuth && !token) {
        return next({path: '/login', query: {redirect: to.fullPath}})
    }
    if (to.meta.roles) {
        if (!user || !to.meta.roles.includes(user.role)) {
            // 角色不匹配：跳到 403 页，让用户明确知道被拒绝的原因
            if (user) return next('/403')
            return next('/login')
        }
    }
  next()
})

export default router
