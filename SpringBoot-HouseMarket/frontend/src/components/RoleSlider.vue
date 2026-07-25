<template>
  <div class="role-slider">
    <div class="role-options">
      <span :class="{ active: modelValue === 'TENANT' }" @click="$emit('update:modelValue', 'TENANT')">租客</span>
      <span :class="{ active: modelValue === 'LANDLORD' }" @click="$emit('update:modelValue', 'LANDLORD')">房东</span>
    </div>
  </div>
</template>

<script setup>
defineProps({ modelValue: String })
defineEmits(['update:modelValue'])
</script>

<style scoped>
.role-slider { background: #f0f0f0; border-radius: 25px; padding: 4px; display: inline-flex; }
.role-options { display: flex; }
.role-options span { padding: 8px 28px; border-radius: 22px; cursor: pointer; font-weight: 600; color: #666; transition: all 0.3s; }
.role-options span.active { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4); }
</style>
