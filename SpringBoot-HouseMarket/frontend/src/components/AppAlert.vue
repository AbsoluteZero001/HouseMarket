<template>
  <div class="alert" :class="'alert-' + type" v-if="visible">
    <span>{{ message }}</span>
    <button class="alert-close" @click="$emit('close')">&times;</button>
  </div>
</template>

<script setup>
defineProps({ visible: Boolean, message: String, type: { type: String, default: 'success' } })
defineEmits(['close'])
</script>

<style scoped>
.alert {
  position: fixed; top: 20px; left: 50%; transform: translateX(-50%);
  padding: 12px 24px; border-radius: 8px; display: flex; align-items: center;
  gap: 12px; z-index: 2000; animation: slideInDown 0.3s; min-width: 300px;
  justify-content: space-between; font-weight: 500;
}
.alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
.alert-error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
.alert-warning { background: #fff3cd; color: #856404; border: 1px solid #ffeaa7; }
.alert-close { background: none; border: none; font-size: 20px; cursor: pointer; color: inherit; }
@keyframes slideInDown { from { transform: translate(-50%, -30px); opacity: 0; } to { transform: translate(-50%, 0); opacity: 1; } }
</style>
