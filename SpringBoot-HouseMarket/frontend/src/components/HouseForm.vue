<template>
  <div class="house-form">
    <section class="form-card">
      <div class="form-card-title">
        <span>基本信息</span>
        <small>标题、地址、小区和租金</small>
      </div>
      <div class="form-group">
        <label>房源标题 <span class="required">*</span></label>
        <input v-model="form.title" required placeholder="例如：高新区精装套三 近地铁"/>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>所在区域 <span class="required">*</span></label>
          <input v-model="form.district" required placeholder="如：高新区"/>
        </div>
        <div class="form-group">
          <label>小区名称</label>
          <input v-model="form.community" placeholder="如：天府名邸"/>
        </div>
      </div>
      <div class="form-group">
        <label>详细地址 <span class="required">*</span></label>
        <input v-model="form.address" required placeholder="请输入详细地址"/>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>租金 (元/月) <span class="required">*</span></label>
          <input v-model.number="form.price" type="number" min="0" required placeholder="请输入租金"/>
        </div>
        <div class="form-group">
          <label>押金 (元)</label>
          <input v-model.number="form.deposit" type="number" min="0" placeholder="请输入押金"/>
        </div>
      </div>
    </section>

    <section class="form-card">
      <div class="form-card-title">
        <span>房屋信息</span>
        <small>户型、面积、楼层和朝向</small>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>房屋类型</label>
          <select v-model="form.type">
            <option value="平层">平层</option>
            <option value="跃层">跃层</option>
            <option value="错层">错层</option>
            <option value="复式">复式</option>
          </select>
        </div>
        <div class="form-group">
          <label>户型 <span class="required">*</span></label>
          <select v-model="form.layout" required>
            <option value="">请选择户型</option>
            <option value="一室一厅">一室一厅</option>
            <option value="两室一厅">两室一厅</option>
            <option value="两室两厅">两室两厅</option>
            <option value="三室一厅">三室一厅</option>
            <option value="三室两厅">三室两厅</option>
            <option value="四室两厅">四室两厅</option>
            <option value="其他">其他</option>
          </select>
        </div>
      </div>
      <div class="form-row four">
        <div class="form-group">
          <label>卧室</label>
          <input v-model.number="form.bedrooms" type="number" min="0" placeholder="卧室数"/>
        </div>
        <div class="form-group">
          <label>客厅</label>
          <input v-model.number="form.livingRooms" type="number" min="0" placeholder="客厅数"/>
        </div>
        <div class="form-group">
          <label>厨房</label>
          <input v-model.number="form.kitchens" type="number" min="0" placeholder="厨房数"/>
        </div>
        <div class="form-group">
          <label>卫生间</label>
          <input v-model.number="form.bathrooms" type="number" min="0" placeholder="卫生间数"/>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>面积 (㎡) <span class="required">*</span></label>
          <input v-model.number="form.area" type="number" min="1" required placeholder="请输入面积"/>
        </div>
        <div class="form-group">
          <label>朝向</label>
          <select v-model="form.orientation">
            <option value="南北">南北</option>
            <option value="南">南</option>
            <option value="北">北</option>
            <option value="东南">东南</option>
            <option value="西南">西南</option>
            <option value="东西">东西</option>
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>所在楼层</label>
          <input v-model="form.floor" placeholder="如：12"/>
        </div>
        <div class="form-group">
          <label>总楼层</label>
          <input v-model.number="form.totalFloors" type="number" min="1" placeholder="如：26"/>
        </div>
      </div>
    </section>

    <section class="form-card">
      <div class="form-card-title">
        <span>配套与入住</span>
        <small>装修、电梯、地铁和入住方式</small>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>装修情况</label>
          <select v-model="form.decoration">
            <option value="精装">精装</option>
            <option value="简装">简装</option>
            <option value="豪华装修">豪华装修</option>
            <option value="毛坯">毛坯</option>
          </select>
        </div>
        <div class="form-group">
          <label>租期/付款</label>
          <input v-model="form.leaseTerm" placeholder="如：押一付三"/>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>地铁距离</label>
          <input v-model="form.subwayDistance" placeholder="如：500m 或 近地铁"/>
        </div>
        <div class="form-group">
          <label>入住方式</label>
          <select v-model="form.moveInType">
            <option value="随时入住">随时入住</option>
            <option value="提前预约">提前预约</option>
            <option value="月底入住">月底入住</option>
            <option value="已出租">已出租</option>
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>房屋状态</label>
          <select v-model="form.rentStatus">
            <option value="随时入住">随时入住</option>
            <option value="已出租">已出租</option>
            <option value="待出租">待出租</option>
            <option value="暂停出租">暂停出租</option>
          </select>
        </div>
        <div class="form-group switch-group">
          <label>是否有电梯</label>
          <label class="switch">
            <input v-model="form.hasElevator" type="checkbox"/>
            <span>{{ form.hasElevator ? '有电梯' : '无电梯' }}</span>
          </label>
        </div>
      </div>
      <div class="form-group">
        <label>标签</label>
        <input v-model="form.tags" placeholder="如：近地铁,拎包入住,带阳台"/>
      </div>
    </section>

    <section class="form-card">
      <div class="form-card-title">
        <span>房源描述</span>
        <small>补充亮点和真实居住体验</small>
      </div>
      <div class="form-group">
        <textarea v-model="form.description" rows="5" placeholder="请输入房源描述"></textarea>
      </div>
    </section>

    <div v-if="showLandlord" class="form-card">
      <div class="form-card-title">
        <span>房源归属</span>
        <small>管理员可为指定房东创建房源</small>
      </div>
      <div class="form-group">
        <label>房东ID <span class="required">*</span></label>
        <input v-model.number="form.landlordId" type="number" min="1" required placeholder="请输入房东用户ID"/>
      </div>
    </div>

    <div v-if="showImageUpload" class="form-card">
      <div class="form-card-title">
        <span>房源图片</span>
        <small>也可在保存后进入分类图片管理</small>
      </div>
      <ImageUpload ref="imageUploadRef" @select="onImageSelect"/>
    </div>

    <div class="form-actions">
      <button class="btn btn-lg" @click="submitForm">{{ submitLabel }}</button>
      <button class="btn btn-lg btn-outline" @click="$emit('cancel')">取消</button>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref, watch} from 'vue'
import ImageUpload from './ImageUpload.vue'

const props = defineProps({
  initial: {type: Object, default: () => ({})},
  submitLabel: {type: String, default: '提交'},
  showImageUpload: {type: Boolean, default: true},
  showLandlord: {type: Boolean, default: false}
})
const emit = defineEmits(['submit', 'cancel'])

const form = reactive({
  title: '',
  type: '平层',
  layout: '',
  district: '',
  community: '',
  bedrooms: 1,
  livingRooms: 1,
  kitchens: 1,
  bathrooms: 1,
  area: '',
  price: '',
  deposit: '',
  orientation: '南北',
  floor: '',
  totalFloors: '',
  decoration: '精装',
  leaseTerm: '押一付三',
  hasElevator: false,
  subwayDistance: '',
  moveInType: '随时入住',
  rentStatus: '随时入住',
  tags: '',
  address: '',
  description: '',
  landlordId: ''
})
const selectedFile = ref(null)
const imageUploadRef = ref(null)

watch(() => props.initial, (val) => {
  if (!val || !val.id) return
  Object.assign(form, {
    title: val.title || '',
    type: val.type || '平层',
    layout: val.layout || '',
    district: val.district || '',
    community: val.community || '',
    bedrooms: val.bedrooms ?? 1,
    livingRooms: val.livingRooms ?? 1,
    kitchens: val.kitchens ?? 1,
    bathrooms: val.bathrooms ?? 1,
    area: val.area || '',
    price: val.price || '',
    deposit: val.deposit || '',
    orientation: val.orientation || '南北',
    floor: val.floor || '',
    totalFloors: val.totalFloors || '',
    decoration: val.decoration || '精装',
    leaseTerm: val.leaseTerm || '押一付三',
    hasElevator: Number(val.hasElevator) === 1,
    subwayDistance: val.subwayDistance || '',
    moveInType: val.moveInType || '随时入住',
    rentStatus: val.rentStatus || '随时入住',
    tags: Array.isArray(val.tags) ? val.tags.join(',') : (val.tags || ''),
    address: val.address || '',
    description: val.description || '',
    landlordId: val.landlordId || ''
  })
}, {immediate: true})

function onImageSelect(file) {
  selectedFile.value = file
}

function submitForm() {
  emit('submit', {
    ...form,
    hasElevator: form.hasElevator ? 1 : 0,
    imageFile: selectedFile.value
  })
}

defineExpose({form, selectedFile})
</script>

<style scoped>
.house-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-card {
  border: 1px solid var(--border);
  border-radius: 14px;
  background: #fff;
  padding: 18px;
}

.form-card-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.form-card-title span {
  font-size: 16px;
  font-weight: 800;
  color: var(--text);
}

.form-card-title small {
  color: var(--text-muted);
  font-size: 12px;
}

.form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 0;
}

.form-row.four {
  flex-wrap: wrap;
}

.form-row.four .form-group {
  min-width: 120px;
}

.form-group {
  margin-bottom: 12px;
}

.form-group label {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: #fafafa;
  color: var(--text);
  font-size: 14px;
  padding: 10px 12px;
  outline: none;
}

.form-group textarea {
  resize: vertical;
  font-family: inherit;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: var(--primary);
  background: #fff;
  box-shadow: 0 0 0 4px rgba(22, 119, 255, 0.1);
}

.required {
  color: var(--danger);
}

.switch-group {
  display: flex;
  flex-direction: column;
}

.switch {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--text);
  font-size: 14px;
}

.switch input {
  width: auto;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  position: sticky;
  bottom: 0;
  background: rgba(244, 247, 252, 0.94);
  padding: 12px 0;
  backdrop-filter: blur(10px);
}

@media (max-width: 640px) {
  .form-row {
    flex-direction: column;
  }
}
</style>
