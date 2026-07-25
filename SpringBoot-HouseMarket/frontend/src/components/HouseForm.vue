<template>
  <div class="house-form">
    <div class="form-group">
      <label>房源标题 <span class="required">*</span></label>
      <input v-model="form.title" required placeholder="请输入房源标题" />
    </div>
    <div class="form-row">
      <div class="form-group">
        <label>户型 <span class="required">*</span></label>
        <select v-model="form.type" required>
          <option value="">请选择户型</option>
          <option value="平层">平层</option>
          <option value="跃层">跃层</option>
          <option value="错层">错层</option>
          <option value="复式">复式</option>
        </select>
      </div>
      <div class="form-group">
        <label>面积 (㎡) <span class="required">*</span></label>
        <input v-model.number="form.area" type="number" min="1" required placeholder="请输入面积" />
      </div>
    </div>
    <div class="form-row">
      <div class="form-group">
        <label>价格 (元/月) <span class="required">*</span></label>
        <input v-model.number="form.price" type="number" min="1" required placeholder="请输入价格" />
      </div>
      <div class="form-group">
        <label>地址 <span class="required">*</span></label>
        <input v-model="form.address" required placeholder="请输入地址" />
      </div>
    </div>
    <div class="form-group">
      <label>房源图片</label>
      <ImageUpload ref="imageUploadRef" @select="onImageSelect" />
    </div>
    <div class="form-group">
      <label>房源描述</label>
      <textarea v-model="form.description" rows="4" placeholder="请输入房源描述"></textarea>
    </div>
    <div class="form-actions">
      <button class="btn" @click="$emit('submit', { ...form, imageFile: selectedFile })">
        {{ submitLabel }}
      </button>
      <button class="btn btn-secondary" @click="$emit('cancel')">取消</button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import ImageUpload from './ImageUpload.vue'

const props = defineProps({ initial: { type: Object, default: () => ({}) }, submitLabel: { type: String, default: '提交' } })
defineEmits(['submit', 'cancel'])

const form = reactive({
  title: '', type: '', area: '', price: '', address: '', description: '', image: ''
})

const selectedFile = ref(null)
const imageUploadRef = ref(null)

watch(() => props.initial, (val) => {
  if (val && val.id) {
    Object.assign(form, {
      title: val.title || '', type: val.type || '', area: val.area || '',
      price: val.price || '', address: val.address || '',
      description: val.description || '', image: val.image || ''
    })
  }
}, { immediate: true })

function onImageSelect(file) { selectedFile.value = file }

defineExpose({ form, selectedFile })
</script>

<style scoped>
.house-form { }
.form-row { display: flex; gap: 16px; }
.form-row .form-group { flex: 1; }
.form-actions { display: flex; gap: 10px; margin-top: 20px; }
.required { color: #dc3545; }
</style>
