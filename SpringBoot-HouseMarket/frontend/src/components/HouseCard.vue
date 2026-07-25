<template>
  <div class="house-card">
    <div class="house-image-container">
      <img :src="imageUrl" :alt="house.title" class="house-image" />
    </div>
    <div class="house-info">
      <h3 class="house-title">{{ house.title }}</h3>
      <div class="house-price">{{ formatPrice(house.price) }}</div>
      <div class="house-params">
        <div class="house-param-row">
          <span class="house-param"><i class="fas fa-home"></i> {{ house.type || '未分类' }}</span>
          <span class="house-param"><i class="fas fa-vector-square"></i> {{ house.area }} ㎡</span>
        </div>
        <div class="house-param"><i class="fas fa-map-marker-alt"></i> {{ house.address || '未填写' }}</div>
      </div>
      <p class="house-desc">{{ truncatedDesc }}</p>
      <div class="house-actions">
        <slot name="actions" :house="house" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ house: Object })

const imageUrl = computed(() => {
  if (props.house.image) {
    try { const arr = JSON.parse(props.house.image); if (Array.isArray(arr) && arr.length) return arr[0] } catch (e) { return props.house.image }
  }
  return `https://picsum.photos/seed/house${props.house.id}/800/600`
})

const truncatedDesc = computed(() => {
  const d = props.house.description || ''
  return d.length > 100 ? d.slice(0, 100) + '...' : d
})

function formatPrice(p) { return p ? '¥' + Number(p).toLocaleString() + '/月' : '价格面议' }
</script>

<style scoped>
.house-card { display: flex; background: rgba(255,255,255,0.95); border-radius: 12px; overflow: hidden; box-shadow: 0 2px 15px rgba(0,0,0,0.1); transition: all 0.3s; margin-bottom: 16px; }
.house-card:hover { transform: translateY(-2px); box-shadow: 0 8px 25px rgba(0,0,0,0.15); }
.house-image-container { width: 55%; min-width: 250px; overflow: hidden; }
.house-image { width: 100%; height: 100%; object-fit: cover; min-height: 220px; transition: transform 0.3s; }
.house-card:hover .house-image { transform: scale(1.05); }
.house-info { width: 45%; padding: 20px; display: flex; flex-direction: column; }
.house-title { font-size: 18px; color: #333; margin-bottom: 8px; }
.house-price { font-size: 1.5rem; font-weight: 700; background: linear-gradient(135deg, #ff6b6b, #ff8e53); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 10px; }
.house-params { margin-bottom: 10px; color: #666; font-size: 14px; display: flex; flex-direction: column; gap: 4px; }
.house-param-row { display: flex; gap: 16px; }
.house-param i { margin-right: 4px; color: #667eea; }
.house-desc { color: #999; font-size: 13px; flex: 1; margin-bottom: 12px; }
.house-actions { display: flex; gap: 8px; flex-wrap: wrap; }
</style>
