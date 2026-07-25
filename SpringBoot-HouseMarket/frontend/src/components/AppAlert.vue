<template>
  <Teleport to="body">
    <transition name="alert-slide">
      <div class="alert" :class="'alert-' + type" v-if="visible">
        <span class="alert-icon">
          <template v-if="type === 'success'">✓</template>
          <template v-else-if="type === 'error'">✗</template>
          <template v-else>⚠</template>
        </span>
        <span>{{ message }}</span>
        <button class="alert-close" @click="$emit('close')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
defineProps({ visible: Boolean, message: String, type: { type: String, default: 'success' } })
defineEmits(['close'])
</script>

<style scoped>
.alert {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 20px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 2000;
  min-width: 280px;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 500;
  box-shadow: var(--shadow-lg);
}
.alert-success { background: #f6ffed; color: #389e0d; border: 1px solid #b7eb8f; }
.alert-error { background: #fff2f0; color: #cf1322; border: 1px solid #ffa39e; }
.alert-warning { background: #fffbe6; color: #d48806; border: 1px solid #ffe58f; }
.alert-icon { font-size: 16px; }
.alert-close {
  background: none; border: none;
  cursor: pointer; color: inherit;
  padding: 2px; opacity: 0.6; display: flex;
}
.alert-close:hover { opacity: 1; }

.alert-slide-enter-active { transition: all 0.3s ease; }
.alert-slide-leave-active { transition: all 0.2s ease; }
.alert-slide-enter-from { transform: translate(-50%, -20px); opacity: 0; }
.alert-slide-leave-to { transform: translate(-50%, -20px); opacity: 0; }
</style>
