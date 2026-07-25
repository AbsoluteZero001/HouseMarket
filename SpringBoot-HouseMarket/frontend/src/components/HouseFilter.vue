<template>
  <div class="filter-card">
    <div class="filter-header">
      <h3>房源搜索</h3>
      <span class="filter-hint">多维度筛选，精准找房</span>
    </div>
    <div class="filter-body">
      <div class="filter-row">
        <div class="form-group">
          <label>关键词</label>
          <input v-model="local.keyword" placeholder="搜索房源标题..." @keyup.enter="search" />
        </div>
        <div class="form-group">
          <label>户型</label>
          <select v-model="local.type">
            <option value="">全部户型</option>
            <option value="平层">平层</option>
            <option value="跃层">跃层</option>
            <option value="错层">错层</option>
            <option value="复式">复式</option>
          </select>
        </div>
        <div class="form-group">
          <label>面积范围</label>
          <div class="range-inputs">
            <input v-model.number="local.minArea" type="number" min="0" placeholder="最小㎡" />
            <span class="range-sep">&ndash;</span>
            <input v-model.number="local.maxArea" type="number" min="0" placeholder="最大㎡" />
          </div>
        </div>
      </div>
      <div class="filter-row">
        <div class="form-group">
          <label>价格范围</label>
          <div class="range-inputs">
            <input v-model.number="local.minPrice" type="number" min="0" placeholder="最低¥" />
            <span class="range-sep">&ndash;</span>
            <input v-model.number="local.maxPrice" type="number" min="0" placeholder="最高¥" />
          </div>
        </div>
        <div class="form-group">
          <label>地址</label>
          <input v-model="local.address" placeholder="搜索地址..." @keyup.enter="search" />
        </div>
        <div class="form-group filter-actions-group">
          <label>&nbsp;</label>
          <div class="filter-actions">
            <button class="btn" @click="search">搜索</button>
            <button class="btn btn-outline" @click="reset">重置</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const emit = defineEmits(['search', 'reset'])

function defaults() {
  return { keyword: '', type: '', minArea: '', maxArea: '', minPrice: '', maxPrice: '', address: '' }
}
const local = reactive(defaults())

function search() { emit('search', { ...local }) }
function reset() { Object.assign(local, defaults()); emit('reset') }
</script>

<style scoped>
.filter-card {
  background: var(--bg-white);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}
.filter-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.filter-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
}
.filter-hint {
  font-size: 12px;
  color: var(--text-muted);
}
.filter-body {
  padding: 20px;
}
.filter-row {
  display: flex;
  gap: 16px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.filter-row:last-child { margin-bottom: 0; }
.filter-row .form-group {
  flex: 1;
  min-width: 180px;
}
.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 5px;
}
.form-group input,
.form-group select {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text);
  background: #fafafa;
  outline: none;
  transition: all var(--transition);
}
.form-group input:focus,
.form-group select:focus {
  border-color: var(--primary);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(22,119,255,0.08);
}

.range-inputs {
  display: flex;
  align-items: center;
  gap: 6px;
}
.range-inputs input {
  flex: 1;
  min-width: 0;
}
.range-sep {
  color: var(--text-muted);
  flex-shrink: 0;
}

.filter-actions-group {
  display: flex;
  flex-direction: column;
}
.filter-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  height: 100%;
}

@media (max-width: 768px) {
  .filter-row { flex-direction: column; gap: 12px; }
  .filter-row .form-group { min-width: auto; }
}
</style>
