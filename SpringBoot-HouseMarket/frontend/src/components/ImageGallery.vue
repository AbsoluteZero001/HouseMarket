<template>
  <div class="image-gallery">
    <div class="main-image-wrap">
      <img :src="mainImage" class="main-image" />
    </div>
    <div class="thumbnails" v-if="images.length > 1">
      <button
        v-for="(img, i) in images"
        :key="i"
        class="thumb-btn"
        :class="{ active: img === mainImage }"
        @click="mainImage = img"
      >
        <img :src="img" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({ images: { type: Array, default: () => [] } })
const mainImage = ref(props.images[0] || '')
</script>

<style scoped>
.main-image-wrap {
  border-radius: 20px;
  overflow: hidden;
  background: #f1f5f9;
  box-shadow: 0 22px 54px rgba(15, 23, 42, 0.14);
}
.main-image {
  width: 100%;
  height: 420px;
  object-fit: cover;
  display: block;
}
.thumbnails {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  overflow-x: auto;
  padding: 4px 0;
}
.thumb-btn {
  flex-shrink: 0;
  width: 76px;
  height: 56px;
  border: 2px solid transparent;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  padding: 0;
  background: #f0f0f0;
  transition: border-color var(--transition);
}
.thumb-btn.active {
  border-color: #06b6d4;
  box-shadow: 0 6px 16px rgba(6, 182, 212, 0.2);
}
.thumb-btn:hover {
  border-color: var(--primary);
  opacity: 0.85;
}
.thumb-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 768px) {
  .main-image { height: 280px; }
}
</style>
