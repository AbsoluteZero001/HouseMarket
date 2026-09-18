<template>
  <div class="progress-bar" :style="{ width: progress + '%', opacity: loading ? 1 : 0 }"></div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({ loading: { type: Boolean, default: false } })
const progress = ref(0)
let timer = null

watch(() => props.loading, (val) => {
  if (val) {
    progress.value = 0
    timer = setInterval(() => {
      if (progress.value < 90) progress.value += Math.random() * 15
    }, 100)
  } else {
    clearInterval(timer)
    progress.value = 100
    setTimeout(() => { progress.value = 0 }, 300)
  }
})
</script>

<style scoped>
.progress-bar {
  position: fixed; top: 0; left: 0; height: 3px;
  background: linear-gradient(90deg, #1677ff, #06b6d4, #8b5cf6, #ec4899);
  transition: width 0.3s, opacity 0.3s; z-index: 9999;
  box-shadow: 0 1px 6px rgba(139, 92, 246, 0.4);
}
</style>
