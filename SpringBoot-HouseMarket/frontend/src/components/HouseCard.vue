<template>
  <div class="house-card">
    <div class="house-image-wrap">
      <img :src="imageUrl" :alt="house.title" class="house-image" />
      <div class="house-tag">{{ house.type || '未分类' }}</div>
    </div>
    <div class="house-body">
      <h3 class="house-title">{{ house.title }}</h3>
      <div class="house-price-line">
        <span class="house-price">{{ formatPrice(house.price) }}</span>
        <span class="house-status" :class="{ offline: house.status === 'OFFLINE' }">{{
            house.status === 'OFFLINE' ? '已下架' : '在租'
          }}</span>
      </div>
      <div class="house-meta">
        <span>{{ house.district || '未知区域' }}</span>
        <span class="meta-sep">|</span>
        <span>{{ house.bedrooms || 1 }}室{{ house.bathrooms || 1 }}卫</span>
        <span class="meta-sep">|</span>
        <span>{{ house.area }}㎡</span>
      </div>
      <div class="house-submeta">
        <span>{{ house.orientation || '南北' }}</span>
        <span>{{ house.floor || '楼层待定' }}</span>
        <span>{{ house.decoration || '精装' }}</span>
      </div>
      <div class="house-tags" v-if="tags.length">
        <span v-for="t in tags" :key="t">{{ t }}</span>
      </div>
      <div class="house-address">{{ house.address || '未填写' }}</div>
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
  if (props.house?.image) {
    try { const arr = JSON.parse(props.house.image); if (Array.isArray(arr) && arr.length) return arr[0] } catch (e) { return props.house.image }
  }
  return `/uploads/img.png`
})
const tags = computed(() => {
  try {
    const parsed = JSON.parse(props.house?.tags || '[]')
    return Array.isArray(parsed) ? parsed.slice(0, 3) : []
  } catch {
    return []
  }
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

.house-price-line {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}
.house-price {
  font-size: 20px;
  font-weight: 700;
  color: var(--accent);
}

.house-status {
  font-size: 11px;
  color: var(--success);
  background: #f6ffed;
  padding: 2px 8px;
  border-radius: 10px;
}

.house-status.offline {
  color: var(--text-muted);
  background: #f5f5f5;
}
.house-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.meta-sep { color: var(--border); }

.house-submeta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.house-submeta span {
  background: #fafafa;
  border: 1px solid var(--border);
  border-radius: 4px;
  padding: 2px 8px;
}

.house-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.house-tags span {
  font-size: 12px;
  color: var(--primary);
  background: var(--primary-light);
  padding: 2px 8px;
  border-radius: 4px;
}

.house-address {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.house-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding-top: 12px;
  border-top: 1px solid var(--border);
}
</style>
