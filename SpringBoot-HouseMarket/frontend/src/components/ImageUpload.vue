<template>
  <div class="image-upload">
    <input type="file" ref="inputRef" :accept="accept" @change="handleFile" hidden />
    <div class="upload-area" @click="$refs.inputRef.click()" :class="{ 'has-image': previewUrl }">
      <img v-if="previewUrl" :src="previewUrl" class="preview-img" />
      <div v-else class="upload-placeholder">
        <i class="fas fa-cloud-upload-alt fa-2x"></i>
        <p>{{ placeholder }}</p>
      </div>
    </div>
    <button v-if="previewUrl" class="btn btn-secondary btn-sm" @click="clearFile">移除图片</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({ accept: { type: String, default: 'image/*' }, placeholder: { type: String, default: '点击上传图片' } })
const emit = defineEmits(['select'])
const previewUrl = ref(null)
const selectedFile = ref(null)

function handleFile(e) {
  const file = e.target.files[0]
  if (!file) return
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) { alert('请上传JPG/PNG/GIF/WEBP格式的图片'); return }
  if (file.size > 2 * 1024 * 1024) { alert('图片大小不能超过2MB'); return }
  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
  emit('select', file)
}

function clearFile() {
  previewUrl.value = null
  selectedFile.value = null
  emit('select', null)
}

defineExpose({ selectedFile, clearFile })
</script>

<style scoped>
.image-upload { text-align: center; }
.upload-area { border: 2px dashed #ddd; border-radius: 8px; padding: 20px; cursor: pointer; transition: border-color 0.3s; min-height: 150px; display: flex; align-items: center; justify-content: center; }
.upload-area:hover { border-color: #667eea; }
.preview-img { max-width: 100%; max-height: 200px; border-radius: 4px; }
.upload-placeholder { color: #999; }
.upload-placeholder i { font-size: 32px; margin-bottom: 8px; }
.btn-sm { padding: 4px 12px; font-size: 12px; margin-top: 8px; }
</style>
