<template>
  <header class="app-header">
    <div class="header-left">
      <h1 class="logo">房源市场</h1>
      <nav class="nav-links">
        <slot name="nav" />
      </nav>
    </div>
    <div class="header-right">
      <div class="user-info" @click="$emit('profile')">
        <span class="role-badge" :class="'role-' + role.toLowerCase()">{{ roleLabel }}</span>
        <span class="username-text">{{ username }}</span>
      </div>
      <button class="btn btn-secondary btn-sm" @click="$emit('logout')">退出</button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ username: String, role: String })
defineEmits(['logout', 'profile'])

const roleLabel = computed(() => {
  const map = { ADMIN: '管理员', LANDLORD: '房东', TENANT: '租客' }
  return map[props.role] || props.role
})
</script>

<style scoped>
.app-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 24px; background: rgba(255,255,255,0.95); box-shadow: 0 2px 15px rgba(0,0,0,0.1); position: sticky; top: 0; z-index: 100; backdrop-filter: blur(10px); }
.header-left { display: flex; align-items: center; gap: 30px; }
.logo { font-size: 20px; background: linear-gradient(135deg, #667eea, #764ba2); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.nav-links { display: flex; gap: 5px; }
.nav-links a { padding: 8px 16px; border-radius: 6px; text-decoration: none; color: #666; font-weight: 500; transition: all 0.3s; }
.nav-links a:hover, .nav-links a.active { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; }
.header-right { display: flex; align-items: center; gap: 12px; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 12px; border-radius: 20px; background: #f5f5f5; transition: background 0.3s; }
.user-info:hover { background: #e8e8e8; }
.role-badge { padding: 3px 10px; border-radius: 12px; font-size: 11px; font-weight: 700; color: #fff; }
.role-admin { background: #dc3545; }
.role-landlord { background: #ff8e53; }
.role-tenant { background: #667eea; }
.btn-sm { padding: 6px 14px; font-size: 12px; }
</style>
