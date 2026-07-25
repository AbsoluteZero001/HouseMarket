<template>
  <div class="pagination" v-if="totalPages > 1">
    <button :disabled="current === 1" @click="$emit('change', current - 1)">&laquo;</button>
    <button v-for="p in pages" :key="p" :class="{ active: p === current }" @click="$emit('change', p)">{{ p }}</button>
    <button :disabled="current === totalPages" @click="$emit('change', current + 1)">&raquo;</button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ current: Number, total: Number, pageSize: Number })
defineEmits(['change'])

const totalPages = computed(() => Math.ceil(props.total / props.pageSize) || 1)
const pages = computed(() => {
  const r = []
  for (let i = 1; i <= totalPages.value; i++) r.push(i)
  const start = Math.max(1, props.current - 2)
  const end = Math.min(totalPages.value, props.current + 2)
  return r.filter(p => p >= start && p <= end)
})
</script>

<style scoped>
.pagination { display: flex; justify-content: center; gap: 8px; margin: 20px 0; }
.pagination button { padding: 8px 14px; border: 1px solid #ddd; border-radius: 6px; background: #fff; cursor: pointer; color: #333; transition: all 0.3s; }
.pagination button:hover:not(:disabled) { background: #667eea; color: #fff; border-color: #667eea; }
.pagination button.active { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; border-color: transparent; }
.pagination button:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
