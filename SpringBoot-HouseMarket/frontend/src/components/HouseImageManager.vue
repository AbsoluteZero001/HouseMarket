<template>
  <div class="house-image-manager">
    <div class="image-list" v-if="images.length">
      <div class="image-item" v-for="(image, index) in sortedImages" :key="image.id">
        <img :src="image.imageUrl" :alt="`房源图片 ${index + 1}`"/>
        <div class="image-overlay">
          <span v-if="Number(image.isCover) === 1" class="cover-badge">封面</span>
          <span class="sort-badge">{{ index + 1 }}</span>
        </div>
        <div class="image-actions">
          <button type="button" class="mini-btn" :disabled="Number(image.isCover) === 1" @click="setCover(image.id)">
            设为封面
          </button>
          <button type="button" class="mini-btn" :disabled="index === 0" @click="move(index, -1)">上移</button>
          <button type="button" class="mini-btn" :disabled="index === sortedImages.length - 1" @click="move(index, 1)">
            下移
          </button>
          <button type="button" class="mini-btn danger" @click="remove(image.id)">删除</button>
        </div>
      </div>
    </div>

    <div class="upload-row">
      <input ref="fileInput" type="file" accept="image/*" hidden @change="onFileChange"/>
      <button type="button" class="btn btn-sm" :disabled="uploading" @click="fileInput?.click()">
        {{ uploading ? '上传中...' : '上传图片' }}
      </button>
      <span class="upload-tip">支持 JPG、PNG、GIF、WEBP，最大 5MB</span>
    </div>
  </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import {deleteHouseImage, reorderHouseImages, setCoverImage, uploadHouseImage} from '../api/houses'

const props = defineProps({houseId: Number, images: {type: Array, default: () => []}})
const emit = defineEmits(['changed'])

const fileInput = ref(null)
const uploading = ref(false)

const sortedImages = computed(() => {
  return [...props.images].sort((a, b) => (Number(a.sortOrder) || 0) - (Number(b.sortOrder) || 0))
})

async function onFileChange(e) {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file || !props.houseId) return
  uploading.value = true
  try {
    await uploadHouseImage(props.houseId, file, props.images.length, props.images.length === 0)
    emit('changed')
  } finally {
    uploading.value = false
  }
}

async function remove(imageId) {
  await deleteHouseImage(props.houseId, imageId)
  emit('changed')
}

async function setCover(imageId) {
  await setCoverImage(props.houseId, imageId)
  emit('changed')
}

async function move(index, delta) {
  const ids = sortedImages.value.map(image => image.id)
  const target = index + delta
  if (target < 0 || target >= ids.length) return
  const moved = ids.splice(index, 1)[0]
  ids.splice(target, 0, moved)
  await reorderHouseImages(props.houseId, ids)
  emit('changed')
}
</script>

<style scoped>
.house-image-manager {
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px;
  background: #f8fafc;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.image-item {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  border: 1px solid var(--border);
}

.image-item img {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
  display: block;
}

.image-overlay {
  position: absolute;
  top: 8px;
  left: 8px;
  right: 8px;
  display: flex;
  justify-content: space-between;
  pointer-events: none;
}

.cover-badge,
.sort-badge {
  font-size: 11px;
  color: #fff;
  background: rgba(0, 0, 0, 0.58);
  border-radius: 999px;
  padding: 3px 9px;
}

.cover-badge {
  background: linear-gradient(135deg, #ff6b35, #f43f5e);
}

.image-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 10px;
}

.mini-btn {
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fff;
  color: var(--text-secondary);
  font-size: 12px;
  padding: 5px 9px;
  cursor: pointer;
}

.mini-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.mini-btn.danger {
  color: #dc2626;
  border-color: #fecaca;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.upload-tip {
  color: var(--text-muted);
  font-size: 12px;
}
</style>
