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
    {path: '/house/:id', name: 'HouseDetail', component: () => import('../views/HouseDetailView.vue')}
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() { return { top: 0 } }
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) return next('/login')
    if (to.meta.roles) {
        let user = null
        try {
            user = JSON.parse(localStorage.getItem('user') || 'null')
        } catch {
            user = null
        }
        if (!user || !to.meta.roles.includes(user.role)) {
            const fallback = user?.role === 'LANDLORD' ? '/landlord' : user?.role === 'ADMIN' ? '/admin' : '/'
            return next(fallback)
        }
    }
  next()
})

export default router
