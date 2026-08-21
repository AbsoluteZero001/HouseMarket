<template>
  <div class="house-card">
    <div class="house-image-wrap">
      <img :src="imageUrl" :alt="house.title" class="house-image" />
      <div class="house-tag">{{ house.type || '未分类' }}</div>
      <div class="house-status-chip" :class="{ offline: house.status === 'OFFLINE' }">
        {{ house.status === 'OFFLINE' ? '已下架' : '在租' }}
      </div>
      <div class="house-shine"></div>
    </div>
    <div class="house-body">
      <h3 class="house-title">{{ house.title }}</h3>
      <div class="house-price-line">
        <span class="house-price">{{ formatPrice(house.price) }}</span>
      </div>
      <div class="house-meta">
        <span>{{ house.layout || house.type || '未分类' }}</span>
        <span class="meta-sep">|</span>
        <span>{{ house.area }}㎡</span>
        <span class="meta-sep">|</span>
        <span>{{ house.district || '未知区域' }}</span>
      </div>
      <div class="house-submeta">
        <span>{{ house.orientation || '南北' }}</span>
        <span>{{ house.floor || '楼层待定' }}</span>
        <span>{{ house.decoration || '精装' }}</span>
        <span v-if="house.subwayDistance">{{ house.subwayDistance }}</span>
      </div>
      <div class="house-tags" v-if="tags.length">
        <span v-for="t in tags" :key="t">{{ t }}</span>
      </div>
      <div class="house-address">
        {{ house.community ? house.community + ' · ' : '' }}{{ house.address || '未填写' }}
      </div>
      <div class="house-actions">
        <slot name="actions" :house="house" />
      </div>
      <div class="house-hover-line"></div>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {formatPrice} from '../composables/useFormat'

const props = defineProps({ house: Object })

const imageUrl = computed(() => {
  if (props.house?.coverImage) return props.house.coverImage
  const first = props.house?.images?.[0]
  return typeof first === 'string' ? first : first?.imageUrl || ''
})
const tags = computed(() => {
  if (Array.isArray(props.house?.tags)) return props.house.tags.slice(0, 3)
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
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 26px rgba(15, 23, 42, 0.08);
  transition: transform 0.45s var(--ease-spring), box-shadow 0.45s var(--ease-spring);
  position: relative;
}
.house-card:hover {
  transform: translateY(-7px);
  box-shadow: 0 26px 54px rgba(15, 23, 42, 0.15);
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
  transition: transform 0.7s var(--ease-spring), filter 0.5s ease;
}

.house-card:hover .house-image {
  transform: scale(1.08);
  filter: saturate(1.12);
}
.house-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.92), rgba(244, 63, 94, 0.92));
  backdrop-filter: blur(8px);
  color: #fff;
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  box-shadow: 0 8px 18px rgba(244, 63, 94, 0.3);
}

.house-status-chip {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 11px;
  color: #059669;
  background: rgba(236, 253, 245, 0.9);
  padding: 5px 12px;
  border-radius: 999px;
  font-weight: 700;
  backdrop-filter: blur(8px);
}

.house-status-chip.offline {
  color: #6b7280;
  background: rgba(243, 244, 246, 0.9);
}

.house-shine {
  position: absolute;
  inset: 0;
  background: linear-gradient(115deg, transparent 34%, rgba(255, 255, 255, 0.26) 48%, transparent 62%);
  transform: translateX(-130%);
  transition: transform 0.9s ease;
  pointer-events: none;
}

.house-card:hover .house-shine {
  transform: translateX(130%);
}
.house-body {
  padding: 18px;
}
.house-title {
  font-size: 16px;
  font-weight: 700;
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
  font-size: 21px;
  font-weight: 800;
  background: linear-gradient(135deg, #ff6b35, #f43f5e);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
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
  background: #f8fafc;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 4px 9px;
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
  background: #eff6ff;
  padding: 4px 10px;
  border-radius: 8px;
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
  position: relative;
}

.house-hover-line {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  background: linear-gradient(90deg, #1677ff, #06b6d4, #8b5cf6, #ec4899);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.55s var(--ease-spring);
}

.house-card:hover .house-hover-line {
  transform: scaleX(1);
}
</style>
