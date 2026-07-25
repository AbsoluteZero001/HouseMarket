<template>
  <div class="modal-overlay" v-if="visible" @click.self="$emit('close')">
    <div class="modal-content" :style="{ maxWidth: width }">
      <span class="close" @click="$emit('close')">&times;</span>
      <h3 v-if="title" class="modal-title">{{ title }}</h3>
      <div class="modal-body"><slot /></div>
    </div>
  </div>
</template>

<script setup>
defineProps({ visible: Boolean, title: String, width: { type: String, default: '500px' } })
defineEmits(['close'])
</script>

<style scoped>
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5); display: flex; align-items: center;
  justify-content: center; z-index: 1000; animation: fadeIn 0.3s;
}
.modal-content {
  background: #fff; border-radius: 12px; padding: 30px; width: 90%;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2); position: relative;
  max-height: 80vh; overflow-y: auto;
}
.close {
  position: absolute; top: 15px; right: 20px; font-size: 24px;
  cursor: pointer; color: #999; transition: color 0.3s;
}
.close:hover { color: #333; }
.modal-title { margin-bottom: 20px; font-size: 18px; color: #333; }
.modal-body { margin-top: 10px; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
</style>
