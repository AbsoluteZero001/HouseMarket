<template>
  <div class="image-upload">
    <input type="file" ref="inputRef" :accept="accept" @change="handleFile" hidden />
    <div class="upload-area" @click="$refs.inputRef.click()" :class="{ 'has-image': previewUrl }">
      <img v-if="previewUrl" :src="previewUrl" class="preview-img" />
      <div v-else class="upload-placeholder">
        <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
        <p>{{ placeholder }}</p>
        <span class="upload-hint">支持 JPG、PNG、GIF、WebP，最大 2MB</span>
      </div>
    </div>
    <p v-if="uploadError" class="upload-error">{{ uploadError }}</p>
    <button v-if="previewUrl" class="btn btn-outline btn-sm remove-btn" @click="clearFile">移除图片</button>
  </div>
</template>

<script setup>
import {ref} from 'vue'

const props = defineProps({ accept: { type: String, default: 'image/*' }, placeholder: { type: String, default: '点击上传房源图片' } })
const emit = defineEmits(['select'])
const previewUrl = ref(null)
const selectedFile = ref(null)
const uploadError = ref('')

function handleFile(e) {
  const file = e.target.files[0]
  if (!file) return
  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    uploadError.value = '仅支持 JPG、PNG、GIF、WEBP 格式图片';
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    uploadError.value = '图片大小不能超过 2MB';
    return
  }
  uploadError.value = ''
  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
  emit('select', file)
}
function clearFile() {
  previewUrl.value = null
  selectedFile.value = null
  uploadError.value = ''
  emit('select', null)
}
defineExpose({ selectedFile, clearFile })
</script>

<style scoped>
.image-upload { text-align: center; }
.upload-area {
  border: 2px dashed var(--border);
  border-radius: var(--radius-sm);
  padding: 30px 20px;
  cursor: pointer;
  transition: all var(--transition);
  min-height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}
.upload-area:hover {
  border-color: var(--primary);
  background: var(--primary-light);
}
.upload-area.has-image {
  padding: 0;
  border-style: solid;
  background: transparent;
}
.preview-img {
  max-width: 100%;
  max-height: 260px;
  border-radius: var(--radius-sm);
}
.upload-placeholder {
  color: var(--text-muted);
}
.upload-placeholder svg {
  margin: 0 auto 10px;
  color: var(--text-muted);
}
.upload-placeholder p {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
}
.upload-hint {
  font-size: 12px;
  color: var(--text-muted);
}
.remove-btn {
  margin-top: 10px;
}

.upload-error {
  margin-top: 10px;
  font-size: 12px;
  color: var(--danger);
}
</style>
