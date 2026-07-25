<template>
  <Teleport to="body">
    <div class="modal-overlay" v-if="visible" @click.self="$emit('close')">
      <div class="modal-content" :style="{ maxWidth: width }">
        <div class="modal-header" v-if="title">
          <h3>{{ title }}</h3>
          <button class="modal-close" @click="$emit('close')">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="modal-body"><slot /></div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({ visible: Boolean, title: String, width: { type: String, default: '500px' } })
defineEmits(['close'])
</script>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
  padding: 20px;
}
.modal-content {
  background: #fff;
  border-radius: var(--radius);
  width: 90%;
  box-shadow: var(--shadow-lg);
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  animation: scaleIn 0.2s ease;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
}
.modal-header h3 {
  font-size: 17px;
  font-weight: 600;
  color: var(--text);
}
.modal-close {
  width: 32px; height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition);
}
.modal-close:hover {
  background: #f0f0f0;
  color: var(--text);
}
.modal-body {
  padding: 24px;
  overflow-y: auto;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes scaleIn { from { opacity: 0; transform: scale(0.96); } to { opacity: 1; transform: scale(1); } }
</style>
