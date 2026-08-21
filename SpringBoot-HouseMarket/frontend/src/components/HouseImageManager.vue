<template>
  <div class="house-image-manager">
    <div class="manager-tip">
      <span>分类上传房源图片</span>
      <small>只展示实际上传的分类，首页封面单独管理</small>
    </div>

    <div class="category-grid">
      <section
          v-for="category in categories"
          :key="category.key"
          class="category-card"
          :class="{ 'cover-card': category.key === 'COVER' }"
      >
        <header class="category-head">
          <div>
            <strong>{{ category.label }}</strong>
            <span>{{ categoryImages(category.key).length }} 张</span>
          </div>
          <input
              :ref="el => setFileInput(category.key, el)"
              type="file"
              accept="image/*"
              multiple
              hidden
              @change="onFilesChange(category, $event)"
          />
          <button
              type="button"
              class="btn btn-sm"
              :disabled="uploadingCategory === category.key"
              @click="openPicker(category.key)"
          >
            {{ uploadingCategory === category.key ? '上传中...' : '上传图片' }}
          </button>
        </header>

        <p class="category-hint">{{ category.hint }}</p>

        <div v-if="categoryImages(category.key).length" class="image-list">
          <article
              v-for="(image, index) in categoryImages(category.key)"
              :key="image.id"
              class="image-item"
              :class="{ cover: Number(image.isCover) === 1 }"
          >
            <img :src="image.imageUrl" :alt="`${category.label} ${index + 1}`"/>
            <div class="image-overlay">
              <span v-if="Number(image.isCover) === 1" class="cover-badge">首页封面</span>
              <span class="sort-badge">{{ index + 1 }}</span>
            </div>
            <div class="image-actions">
              <button type="button" class="mini-btn" :disabled="Number(image.isCover) === 1"
                      @click="setCover(image.id)">
                设为首页封面
              </button>
              <button type="button" class="mini-btn" :disabled="index === 0" @click="move(category.key, index, -1)">
                上移
              </button>
              <button
                  type="button"
                  class="mini-btn"
                  :disabled="index === categoryImages(category.key).length - 1"
                  @click="move(category.key, index, 1)"
              >
                下移
              </button>
              <button type="button" class="mini-btn danger" @click="remove(image.id)">删除</button>
            </div>
          </article>
        </div>
        <div v-else class="category-empty">暂无{{ category.label }}图片</div>
      </section>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {deleteHouseImage, reorderHouseImages, setCoverImage, uploadHouseImage} from '../api/houses'

const props = defineProps({houseId: Number, images: {type: Array, default: () => []}})
const emit = defineEmits(['changed'])

const categories = [
  {key: 'COVER', label: '首页封面', hint: '将展示在租客首页房源卡片中，建议上传 1 张'},
  {key: 'LIVING_ROOM', label: '客厅', hint: '可上传多张客厅图片'},
  {key: 'BEDROOM', label: '卧室', hint: '可上传多张卧室图片'},
  {key: 'KITCHEN', label: '厨房', hint: '可选'},
  {key: 'BATHROOM', label: '卫生间', hint: '可选'},
  {key: 'BALCONY', label: '阳台', hint: '可选'},
  {key: 'DINING_ROOM', label: '餐厅', hint: '可选'},
  {key: 'STUDY', label: '书房', hint: '可选'},
  {key: 'FLOOR_PLAN', label: '户型图', hint: '可选'},
  {key: 'OTHER', label: '其他', hint: '可选'}
]

const fileInputs = {}
const uploadingCategory = ref('')

function setFileInput(key, el) {
  if (el) fileInputs[key] = el
}

function categoryImages(type) {
  return props.images
      .filter(image => image.imageType === type)
      .sort((a, b) => (Number(a.sortOrder) || 0) - (Number(b.sortOrder) || 0))
}

function openPicker(key) {
  fileInputs[key]?.click()
}

async function onFilesChange(category, event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!files.length || !props.houseId) return

  uploadingCategory.value = category.key
  try {
    for (let i = 0; i < files.length; i++) {
      const currentCount = categoryImages(category.key).length
      const isCover = category.key === 'COVER' && currentCount === 0
      await uploadHouseImage(props.houseId, files[i], category.key, currentCount + i, isCover)
    }
    emit('changed')
  } finally {
    uploadingCategory.value = ''
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

async function move(type, index, delta) {
  const ids = categoryImages(type).map(image => image.id)
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
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.manager-tip {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  color: var(--text);
}

.manager-tip span {
  font-size: 16px;
  font-weight: 800;
}

.manager-tip small {
  color: var(--text-muted);
}

.category-grid {
  display: grid;
  gap: 12px;
}

.category-card {
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px;
  background: #fff;
}

.category-card.cover-card {
  border-color: rgba(255, 107, 53, 0.45);
  background: linear-gradient(135deg, #fff7f5, #fff);
}

.category-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.category-head strong {
  font-size: 15px;
  color: var(--text);
}

.category-head span {
  margin-left: 8px;
  font-size: 12px;
  color: var(--text-muted);
}

.category-hint {
  color: var(--text-muted);
  font-size: 12px;
  margin: 4px 0 12px;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
}

.image-item {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #fff;
}

.image-item.cover {
  border-color: #ff6b35;
  box-shadow: 0 0 0 2px rgba(255, 107, 53, 0.16);
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
  background: rgba(0, 0, 0, 0.6);
  padding: 3px 9px;
  border-radius: 999px;
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
  padding: 5px 8px;
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

.category-empty {
  color: #9ca3af;
  font-size: 12px;
  padding: 10px 0 2px;
}

@media (max-width: 640px) {
  .category-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .manager-tip {
    flex-direction: column;
  }
}
</style>
