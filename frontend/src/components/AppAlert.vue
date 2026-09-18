<template>
  <Teleport to="body">
    <transition name="alert-slide">
      <div class="alert" :class="'alert-' + type" v-if="visible" role="status">
        <span class="alert-icon">
          <svg v-if="type === 'success'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
               stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline
              points="20 6 9 17 4 12"/></svg>
          <svg v-else-if="type === 'error'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
               stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line
              x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <svg v-else-if="type === 'warning'" width="16" height="16" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path
              d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line
              x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
               stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16"
                                                                                                    x2="12" y2="12"/><line
              x1="12" y1="8" x2="12.01" y2="8"/></svg>
        </span>
        <span class="alert-message">{{ message }}</span>
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
  right: 24px;
  bottom: 24px;
  padding: 12px 20px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 2000;
  max-width: 360px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 22px 50px rgba(15, 23, 42, 0.18);
  animation: alertIn 0.25s ease;
}
.alert-success { background: #f6ffed; color: #389e0d; border: 1px solid #b7eb8f; }
.alert-error { background: #fff2f0; color: #cf1322; border: 1px solid #ffa39e; }
.alert-warning { background: #fffbe6; color: #d48806; border: 1px solid #ffe58f; }

.alert-info {
  background: #e6f4ff;
  color: #0958d9;
  border: 1px solid #91caff;
}

.alert-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.alert-message {
  flex: 1;
  line-height: 1.5;
}
.alert-close {
  background: none; border: none;
  cursor: pointer; color: inherit;
  padding: 2px; opacity: 0.6; display: flex;
}
.alert-close:hover { opacity: 1; }

.alert-slide-enter-active { transition: all 0.3s ease; }
.alert-slide-leave-active { transition: all 0.2s ease; }

.alert-slide-enter-from {
  transform: translateY(20px);
  opacity: 0;
}

.alert-slide-leave-to {
  transform: translateY(20px);
  opacity: 0;
}

@keyframes alertIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
