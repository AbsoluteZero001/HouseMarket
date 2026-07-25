<template>
  <div class="filter-container">
    <h3>房源搜索</h3>
    <div class="filter-row">
      <div class="form-group">
        <label>关键词</label>
        <input v-model="local.keyword" placeholder="搜索房源标题" />
      </div>
      <div class="form-group">
        <label>户型</label>
        <select v-model="local.type">
          <option value="">全部</option>
          <option value="平层">平层</option>
          <option value="跃层">跃层</option>
          <option value="错层">错层</option>
          <option value="复式">复式</option>
        </select>
      </div>
      <div class="form-group">
        <label>最小面积</label>
        <input v-model.number="local.minArea" type="number" min="0" placeholder="㎡" />
      </div>
      <div class="form-group">
        <label>最大面积</label>
        <input v-model.number="local.maxArea" type="number" min="0" placeholder="㎡" />
      </div>
    </div>
    <div class="filter-row">
      <div class="form-group">
        <label>最低价格</label>
        <input v-model.number="local.minPrice" type="number" min="0" placeholder="¥" />
      </div>
      <div class="form-group">
        <label>最高价格</label>
        <input v-model.number="local.maxPrice" type="number" min="0" placeholder="¥" />
      </div>
      <div class="form-group">
        <label>地址</label>
        <input v-model="local.address" placeholder="搜索地址" />
      </div>
    </div>
    <div class="filter-actions">
      <button class="btn" @click="search">搜索</button>
      <button class="btn btn-secondary" @click="reset">重置</button>
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
.filter-container { background: rgba(255,255,255,0.95); border-radius: 12px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 15px rgba(0,0,0,0.1); }
.filter-container h3 { margin-bottom: 15px; color: #333; }
.filter-row { display: flex; gap: 16px; margin-bottom: 12px; flex-wrap: wrap; }
.filter-row .form-group { flex: 1; min-width: 180px; }
.filter-actions { display: flex; gap: 10px; }
</style>
