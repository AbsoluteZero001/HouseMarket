<template>
  <header class="app-header" :class="{ scrolled }">
    <div class="header-inner">
      <div class="header-left">
        <RouterLink to="/" class="logo-link">
          <span class="logo-icon">🏠</span>
          <span class="logo-text">房源市场</span>
        </RouterLink>
        <nav class="nav-links">
          <slot name="nav" />
        </nav>
      </div>
      <div class="header-right">
        <div class="user-chip" @click="$emit('profile')" v-if="username">
          <span class="role-dot" :class="'dot-' + role.toLowerCase()"></span>
          <span class="username-text">{{ username }}</span>
          <span class="role-tag">{{ roleLabel }}</span>
        </div>
        <button class="btn-logout" @click="$emit('logout')" v-if="username" title="退出登录">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({ username: String, role: String })
defineEmits(['logout', 'profile'])

const scrolled = ref(false)

function onScroll() { scrolled.value = window.scrollY > 10 }
onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

const roleLabel = computed(() => {
  const map = { ADMIN: '管理员', LANDLORD: '房东', TENANT: '租客' }
  return map[props.role] || props.role
})
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid transparent;
  transition: all var(--transition);
}
.app-header.scrolled {
  border-bottom-color: var(--border);
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 56px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
}
.logo-link {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: var(--text);
}
.logo-link:hover { color: var(--primary); }
.logo-icon { font-size: 22px; }
.logo-text { font-size: 18px; font-weight: 700; letter-spacing: 1px; }

/* Nav tabs */
.nav-links {
  display: flex;
  gap: 2px;
}
:deep(.nav-links a) {
  padding: 7px 16px;
  border-radius: 6px;
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  transition: all var(--transition);
}
:deep(.nav-links a:hover) {
  color: var(--primary);
  background: var(--primary-light);
}
:deep(.nav-links a.active) {
  background: var(--primary);
  color: #fff;
}

/* Right side */
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 14px;
  border-radius: 20px;
  background: #f5f5f5;
  cursor: pointer;
  transition: all var(--transition);
  font-size: 13px;
}
.user-chip:hover {
  background: var(--primary-light);
}
.role-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.dot-admin { background: #cf1322; }
.dot-landlord { background: var(--accent); }
.dot-tenant { background: var(--primary); }
.username-text {
  font-weight: 600;
  color: var(--text);
}
.role-tag {
  font-size: 11px;
  color: var(--text-muted);
}
.btn-logout {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: #fff;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}
.btn-logout:hover {
  color: var(--danger);
  border-color: var(--danger);
  background: #fff2f0;
}

@media (max-width: 768px) {
  .header-inner { padding: 0 12px; }
  .header-left { gap: 16px; }
  .logo-text { display: none; }
  :deep(.nav-links a) { padding: 6px 10px; font-size: 13px; }
  .role-tag { display: none; }
}
</style>
