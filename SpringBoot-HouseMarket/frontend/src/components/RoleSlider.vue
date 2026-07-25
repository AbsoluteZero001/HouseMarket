<template>
  <div class="role-slider">
    <button
      v-for="opt in options"
      :key="opt.value"
      type="button"
      :class="{ active: modelValue === opt.value }"
      @click="$emit('update:modelValue', opt.value)"
    >
      <span class="opt-icon">{{ opt.icon }}</span>
      <span class="opt-label">{{ opt.label }}</span>
    </button>
  </div>
</template>

<script setup>
defineProps({ modelValue: String })
defineEmits(['update:modelValue'])

const options = [
  { value: 'TENANT', label: '我是租客', icon: '&#128101;' },
  { value: 'LANDLORD', label: '我是房东', icon: '&#127968;' }
]
</script>

<style scoped>
.role-slider {
  display: flex;
  background: #f0f2f5;
  border-radius: var(--radius-sm);
  padding: 4px;
  gap: 4px;
}
.role-slider button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition);
}
.role-slider button.active {
  background: #fff;
  color: var(--primary);
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  font-weight: 600;
}
.opt-icon { font-size: 16px; }
</style>
