<template>
  <div class="pagination" v-if="totalPages > 1">
    <button class="page-btn" :disabled="current === 1" @click="$emit('change', current - 1)">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
    </button>
    <button
      v-for="p in pages"
      :key="p"
      class="page-btn"
      :class="{ active: p === current }"
      @click="$emit('change', p)"
    >{{ p }}</button>
    <button class="page-btn" :disabled="current === totalPages" @click="$emit('change', current + 1)">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ current: Number, total: Number, pageSize: Number })
defineEmits(['change'])

const totalPages = computed(() => Math.ceil(props.total / props.pageSize) || 1)
const pages = computed(() => {
  const r = []
  const start = Math.max(1, props.current - 2)
  const end = Math.min(totalPages.value, props.current + 2)
  for (let i = start; i <= end; i++) r.push(i)
  return r
})
</script>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin: 28px 0;
}
.page-btn {
  min-width: 36px;
  height: 36px;
  padding: 0 8px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--bg-white);
  color: var(--text);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition);
  display: flex;
  align-items: center;
  justify-content: center;
}
.page-btn:hover:not(:disabled):not(.active) {
  border-color: var(--primary);
  color: var(--primary);
}
.page-btn.active {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}
.page-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
</style>
