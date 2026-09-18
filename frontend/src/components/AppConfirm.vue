<template>
  <Teleport to="body">
    <transition name="confirm-fade">
      <div v-if="visible" class="confirm-mask" @click.self="$emit('cancel')">
        <div class="confirm-box" role="dialog" aria-modal="true">
          <h3>{{ title }}</h3>
          <p>{{ message }}</p>
          <div class="confirm-actions">
            <button class="btn btn-outline" @click="$emit('cancel')">取消</button>
            <button class="btn btn-danger" @click="$emit('confirm')">确认</button>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
defineProps({
  visible: Boolean,
  title: {type: String, default: '请确认'},
  message: {type: String, default: ''}
})
defineEmits(['confirm', 'cancel'])
</script>

<style scoped>
.confirm-mask {
  position: fixed;
  inset: 0;
  z-index: 3000;
  background: rgba(7, 20, 40, 0.5);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.confirm-box {
  width: 100%;
  max-width: 380px;
  background: #fff;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 32px 80px rgba(7, 20, 40, 0.34);
}

.confirm-box h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 10px;
}

.confirm-box p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 22px;
}

.confirm-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.confirm-fade-enter-active,
.confirm-fade-leave-active {
  transition: opacity 0.2s ease;
}

.confirm-fade-enter-from,
.confirm-fade-leave-to {
  opacity: 0;
}
</style>
