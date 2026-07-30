<template>
  <div class="house-card">
    <div class="house-image-wrap">
      <img :src="imageUrl" :alt="house.title" class="house-image" />
      <div class="house-tag">{{ house.type || '未分类' }}</div>
    </div>
    <div class="house-body">
      <h3 class="house-title">{{ house.title }}</h3>
      <div class="house-price">{{ formatPrice(house.price) }}</div>
      <div class="house-meta">
        <span>{{ house.area }} ㎡</span>
        <span class="meta-sep">|</span>
        <span>{{ house.address || '未填写' }}</span>
      </div>
      <p class="house-desc">{{ truncatedDesc }}</p>
      <div class="house-actions">
        <slot name="actions" :house="house" />
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {formatPrice} from '../composables/useFormat'

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
</script>

<style scoped>
.house-card {
  background: var(--bg-white);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition);
}
.house-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}
.house-image-wrap {
  position: relative;
  overflow: hidden;
  aspect-ratio: 16 / 10;
}
.house-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}
.house-card:hover .house-image { transform: scale(1.06); }
.house-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px);
  color: #fff;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.house-body {
  padding: 16px;
}
.house-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.house-price {
  font-size: 20px;
  font-weight: 700;
  color: var(--accent);
  margin-bottom: 8px;
}
.house-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.meta-sep { color: var(--border); }
.house-desc {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.house-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding-top: 12px;
  border-top: 1px solid var(--border);
}
</style>
