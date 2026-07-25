<template>
  <div class="image-gallery">
    <img :src="mainImage" class="main-image" />
    <div class="thumbnails" v-if="images.length > 1">
      <img v-for="(img, i) in images" :key="i" :src="img" :class="{ active: img === mainImage }" @click="mainImage = img" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({ images: { type: Array, default: () => [] } })
const mainImage = ref(props.images[0] || '')
</script>

<style scoped>
.main-image { width: 100%; height: 400px; object-fit: cover; border-radius: 12px; }
.thumbnails { display: flex; gap: 10px; margin-top: 10px; overflow-x: auto; padding: 5px 0; }
.thumbnails img { width: 80px; height: 60px; object-fit: cover; border-radius: 6px; cursor: pointer; border: 2px solid transparent; transition: border-color 0.3s; }
.thumbnails img.active { border-color: #667eea; }
.thumbnails img:hover { border-color: #b8a9d4; }
</style>
