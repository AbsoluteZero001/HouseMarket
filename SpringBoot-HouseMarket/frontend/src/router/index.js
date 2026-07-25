import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/RegisterView.vue') },
  { path: '/tenant', name: 'Tenant', component: () => import('../views/TenantView.vue'), meta: { requiresAuth: true, role: 'TENANT' } },
  { path: '/landlord', name: 'Landlord', component: () => import('../views/LandlordView.vue'), meta: { requiresAuth: true, role: 'LANDLORD' } },
  { path: '/admin', name: 'Admin', component: () => import('../views/AdminView.vue'), meta: { requiresAuth: true, role: 'ADMIN' } },
  { path: '/house/:id', name: 'HouseDetail', component: () => import('../views/HouseDetailView.vue'), meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  let user = null
  try { user = JSON.parse(userStr) } catch (e) { /* ignore */ }

  if (to.meta.requiresAuth && !token) {
    return next('/login')
  }

  if (to.meta.role && user) {
    const userRole = user.role ? user.role.toUpperCase() : ''
    if (userRole !== to.meta.role && userRole !== 'ADMIN') {
      if (userRole === 'TENANT') return next('/tenant')
      if (userRole === 'LANDLORD') return next('/landlord')
      return next('/login')
    }
  }

  next()
})

export default router
