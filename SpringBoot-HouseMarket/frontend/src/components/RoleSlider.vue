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
  { value: 'TENANT', label: '我是租客', icon: '👥' },
  { value: 'LANDLORD', label: '我是房东', icon: '🏠' }
]
</script>

<style scoped>
.role-slider {
  display: flex;
  background: #f1f5f9;
  border-radius: 12px;
  padding: 4px;
  gap: 4px;
  box-shadow: inset 0 1px 4px rgba(15, 23, 42, 0.06);
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
  border-radius: 9px;
}
.role-slider button.active {
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  color: #fff;
  box-shadow: 0 8px 18px rgba(22, 119, 255, 0.26);
  font-weight: 600;
  transform: translateY(-1px);
}
.opt-icon { font-size: 16px; }
</style>
