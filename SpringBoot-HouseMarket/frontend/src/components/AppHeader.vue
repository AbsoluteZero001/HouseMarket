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
          <span class="role-tag">{{ roleLabelComputed }}</span>
        </div>
        <button class="btn-logout" @click="$emit('logout')" v-if="username" title="退出登录">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import {computed, onMounted, onUnmounted, ref} from 'vue'
import {roleLabel} from '../composables/useAuth'

const props = defineProps({ username: String, role: String })
defineEmits(['logout', 'profile'])

const scrolled = ref(false)

function onScroll() { scrolled.value = window.scrollY > 10 }
onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

const roleLabelComputed = computed(() => roleLabel(props.role))
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(20px) saturate(170%);
  -webkit-backdrop-filter: blur(20px) saturate(170%);
  border-bottom: 1px solid transparent;
  transition: all var(--transition);
}
.app-header.scrolled {
  border-bottom-color: rgba(229, 231, 235, 0.8);
  box-shadow: 0 8px 30px rgba(15, 23, 42, 0.08);
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

.logo-icon {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  color: #fff;
  border-radius: 10px;
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.3);
}

.logo-text {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.5px;
}

/* Nav tabs */
.nav-links {
  display: flex;
  gap: 2px;
}
:deep(.nav-links a) {
  padding: 8px 16px;
  border-radius: 999px;
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  transition: all var(--transition);
}
:deep(.nav-links a:hover) {
  color: var(--primary);
  background: var(--primary-light);
  transform: translateY(-1px);
}
:deep(.nav-links a.active) {
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  color: #fff;
  box-shadow: 0 8px 18px rgba(22, 119, 255, 0.26);
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
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all var(--transition);
  font-size: 13px;
}
.user-chip:hover {
  border-color: #06b6d4;
  box-shadow: 0 8px 18px rgba(22, 119, 255, 0.1);
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
  border-radius: 10px;
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
