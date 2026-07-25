<template>
  <div class="detail-page">
    <AppHeader :username="user?.username" :role="user?.role" @logout="handleLogout">
      <template #nav>
        <a href="#" @click.prevent="goBack">返回</a>
      </template>
    </AppHeader>

    <div class="container" v-if="house">
      <div class="detail-layout">
        <div class="detail-gallery">
          <ImageGallery :images="imageList" />
        </div>
        <div class="detail-info">
          <h2 class="house-title">{{ house.title }}</h2>
          <div class="house-price">{{ formatPrice(house.price) }}</div>
          <div class="house-meta">
            <span><i class="fas fa-home"></i> {{ house.type }}</span>
            <span><i class="fas fa-vector-square"></i> {{ house.area }} ㎡</span>
            <span><i class="fas fa-map-marker-alt"></i> {{ house.address }}</span>
          </div>
          <div class="house-description">
            <h4>房源描述</h4>
            <p>{{ house.description || '暂无描述' }}</p>
          </div>
          <div class="house-actions">
            <button class="btn" @click="bookHouse">预约看房</button>
            <button class="btn" :class="isFav ? 'favorited' : 'btn-heart'" @click="toggleFavorite">
              <i :class="isFav ? 'fas fa-heart' : 'far fa-heart'"></i> {{ isFav ? '已收藏' : '收藏' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Landlord Info -->
      <div class="landlord-section" v-if="house.landlordId">
        <h3>房东信息</h3>
        <p>房东ID: {{ house.landlordId }}</p>
      </div>
    </div>

    <!-- Booking Modal -->
    <AppModal :visible="showBookModal" title="预约看房" @close="showBookModal = false">
      <div class="form-group"><label>预约日期</label><input v-model="bookForm.date" type="date" required /></div>
      <div class="form-group"><label>预约时间</label><input v-model="bookForm.time" type="time" required /></div>
      <div class="form-group"><label>见面地点</label><input v-model="bookForm.location" required placeholder="请输入见面地点" /></div>
      <div class="form-group"><label>留言</label><textarea v-model="bookForm.notes" rows="3" placeholder="给房东留言"></textarea></div>
      <button class="btn btn-block" @click="submitBooking">提交预约</button>
    </AppModal>

    <AppAlert :visible="!!alertMsg" :message="alertMsg" :type="alertType" @close="alertMsg = ''" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useHouseStore } from '../stores/houses'
import { useFavoriteStore } from '../stores/favorites'
import { useAppointmentStore } from '../stores/appointments'
import AppHeader from '../components/AppHeader.vue'
import ImageGallery from '../components/ImageGallery.vue'
import AppModal from '../components/AppModal.vue'
import AppAlert from '../components/AppAlert.vue'

const route = useRoute()
const router = useRouter()
const houseStore = useHouseStore()
const favStore = useFavoriteStore()
const aptStore = useAppointmentStore()

const user = JSON.parse(localStorage.getItem('user') || 'null')
const house = ref(null)
const isFav = ref(false)
const showBookModal = ref(false)
const alertMsg = ref('')
const alertType = ref('success')

const bookForm = reactive({ date: '', time: '', location: '', notes: '' })

const imageList = computed(() => {
  if (!house.value?.image) return []
  try { const arr = JSON.parse(house.value.image); return Array.isArray(arr) ? arr : [house.value.image] } catch (e) { return [house.value.image] }
})

function showAlert(msg, type = 'success') { alertMsg.value = msg; alertType.value = type; setTimeout(() => alertMsg.value = '', 3000) }
function formatPrice(p) { return p ? '¥' + Number(p).toLocaleString() + '/月' : '价格面议' }

function goBack() {
  const role = user?.role?.toLowerCase()
  if (role === 'tenant') router.push('/tenant')
  else if (role === 'landlord') router.push('/landlord')
  else router.push('/')
}

async function toggleFavorite() {
  if (isFav.value) {
    await favStore.remove(house.value.id)
    isFav.value = false
    showAlert('已取消收藏')
  } else {
    await favStore.add(house.value.id, user.id)
    isFav.value = true
    showAlert('已收藏')
  }
}

function bookHouse() { showBookModal.value = true }

async function submitBooking() {
  if (!bookForm.date || !bookForm.time || !bookForm.location) { alert('请填写完整信息'); return }
  try {
    await aptStore.addAppointment({
      houseId: house.value.id,
      tenantId: user.id,
      landlordId: house.value.landlordId,
      time: `${bookForm.date} ${bookForm.time}`,
      location: bookForm.location,
      notes: bookForm.notes,
      status: 'pending'
    })
    showAlert('预约申请已提交')
    showBookModal.value = false
    Object.assign(bookForm, { date: '', time: '', location: '', notes: '' })
  } catch (e) {
    showAlert('预约失败: ' + (e.response?.data?.message || e.message), 'error')
  }
}

function handleLogout() { localStorage.clear(); router.push('/login') }

onMounted(async () => {
  const id = route.params.id
  if (!id) return
  const res = await houseStore.fetchHouseById(id)
  if (res.success) house.value = res.data.house

  try {
    const checkRes = await favStore.check(user.id, id)
    isFav.value = checkRes.data?.favorited || false
  } catch (e) { /* ignore */ }

  if (route.query.action === 'book') showBookModal.value = true
})
</script>

<style scoped>
.detail-page { min-height: 100vh; }
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.detail-layout { display: flex; gap: 30px; flex-wrap: wrap; }
.detail-gallery { flex: 1; min-width: 500px; }
.detail-info { flex: 1; min-width: 350px; background: rgba(255,255,255,0.95); border-radius: 12px; padding: 30px; box-shadow: 0 2px 15px rgba(0,0,0,0.1); }
.house-title { font-size: 24px; color: #333; margin-bottom: 10px; }
.house-price { font-size: 2rem; font-weight: 700; background: linear-gradient(135deg, #ff6b6b, #ff8e53); -webkit-background-clip: text; -webkit-text-fill-color: transparent; margin-bottom: 20px; }
.house-meta { display: flex; gap: 20px; margin-bottom: 20px; color: #666; flex-wrap: wrap; }
.house-meta i { color: #667eea; margin-right: 4px; }
.house-description { margin: 20px 0; padding-top: 20px; border-top: 1px solid #eee; }
.house-description h4 { margin-bottom: 10px; color: #333; }
.house-description p { color: #666; line-height: 1.8; }
.house-actions { display: flex; gap: 12px; margin-top: 20px; }
.favorited { background: linear-gradient(135deg, #ff6b6b, #ff8e53); color: #fff; }
.btn-heart { background: #fff; color: #ff6b6b; border: 1px solid #ff6b6b; }
.btn-block { width: 100%; padding: 12px; margin-top: 15px; background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; border: none; border-radius: 6px; font-size: 16px; cursor: pointer; font-weight: 700; }
.landlord-section { background: rgba(255,255,255,0.95); border-radius: 12px; padding: 20px; margin-top: 20px; box-shadow: 0 2px 15px rgba(0,0,0,0.1); }
</style>
