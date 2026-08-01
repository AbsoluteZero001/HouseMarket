<template>
  <div class="detail-page">
    <AppHeader :username="user?.username" :role="user?.role" @logout="handleLogout">
      <template #nav>
        <a href="#" @click.prevent="goBack">&larr; 返回</a>
      </template>
    </AppHeader>

    <div class="container" v-if="house">
      <!-- Breadcrumb -->
      <div class="breadcrumb">
        <RouterLink to="/">首页</RouterLink>
        <span class="sep">/</span>
        <span>{{ house.type }}</span>
        <span class="sep">/</span>
        <span class="current">{{ house.title }}</span>
      </div>

      <div class="detail-layout">
        <!-- Gallery -->
        <div class="detail-gallery">
          <ImageGallery :images="imageList" />
        </div>

        <!-- Info -->
        <div class="detail-info">
          <h2 class="house-title">{{ house.title }}</h2>
          <div class="house-price">{{ formatPrice(house.price) }}</div>
          <div class="house-meta">
            <span class="meta-item">
              <span class="meta-icon">🏠</span>
              {{ house.type }}
            </span>
            <span class="meta-item">
              <span class="meta-icon">📐</span>
              {{ house.area }} ㎡
            </span>
            <span class="meta-item">
              <span class="meta-icon">📍</span>
              {{ house.address }}
            </span>
          </div>

          <div class="house-description">
            <h4>房源描述</h4>
            <p>{{ house.description || '暂无描述' }}</p>
          </div>

          <div class="house-actions">
            <button class="btn btn-lg btn-accent" @click="bookHouse">预约看房</button>
            <button
              class="btn btn-lg"
              :class="isFav ? 'btn-fav-active' : 'btn-fav'"
              @click="toggleFavorite"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" :fill="isFav ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
              {{ isFav ? '已收藏' : '收藏' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Landlord Info -->
      <div class="landlord-section" v-if="house.landlordId">
        <h3>房东信息</h3>
        <div class="landlord-card">
          <div class="landlord-avatar">🏠</div>
          <div>
            <div class="landlord-name">房东 #{{ house.landlordId }}</div>
            <div class="landlord-sub">认证房东</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading state -->
    <div class="container loading-container" v-else>
      <div class="skeleton-detail">
        <div class="skeleton-gallery"></div>
        <div class="skeleton-info">
          <div class="skeleton-line w-60" style="height:28px"></div>
          <div class="skeleton-line w-30" style="height:36px"></div>
          <div class="skeleton-line w-80"></div>
          <div class="skeleton-line w-100"></div>
          <div class="skeleton-line w-50"></div>
        </div>
      </div>
    </div>

    <!-- Booking Modal -->
    <AppModal :visible="showBookModal" title="预约看房" @close="showBookModal = false">
      <div class="form-group"><label>预约日期</label><input v-model="bookForm.date" type="date" required /></div>
      <div class="form-group"><label>预约时间</label><input v-model="bookForm.time" type="time" required /></div>
      <div class="form-group"><label>见面地点</label><input v-model="bookForm.location" required placeholder="请输入见面地点" /></div>
      <div class="form-group"><label>留言</label><textarea v-model="bookForm.notes" rows="3" placeholder="给房东留言（可选）"></textarea></div>
      <p v-if="bookingError" class="booking-error">{{ bookingError }}</p>
      <button class="btn btn-block" @click="submitBooking">提交预约</button>
    </AppModal>

    <AppAlert :visible="!!alertMsg" :message="alertMsg" :type="alertType" @close="alertMsg = ''" />
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useHouseStore} from '../stores/houses'
import {useFavoriteStore} from '../stores/favorites'
import {useAppointmentStore} from '../stores/appointments'
import AppHeader from '../components/AppHeader.vue'
import ImageGallery from '../components/ImageGallery.vue'
import AppModal from '../components/AppModal.vue'
import AppAlert from '../components/AppAlert.vue'
import {useAuth} from '../composables/useAuth'
import {useAlert} from '../composables/useAlert'
import {formatPrice} from '../composables/useFormat'

const route = useRoute()

const router = useRouter()
const houseStore = useHouseStore()
const favStore = useFavoriteStore()
const aptStore = useAppointmentStore()

const {handleLogout: doLogout} = useAuth()
let user = {}
try {
  user = JSON.parse(localStorage.getItem('user') || 'null') || {}
} catch {
  user = {}
}
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const house = ref(null)
const isFav = ref(false)
const showBookModal = ref(false)
const bookingError = ref('')
const {alertMsg, alertType, showAlert} = useAlert()

const bookForm = reactive({ date: '', time: '', location: '', notes: '' })

const imageList = computed(() => {
  if (!house.value?.image) return []
  try { const arr = JSON.parse(house.value.image); return Array.isArray(arr) ? arr : [house.value.image] } catch (e) { return [house.value.image] }
})

function goBack() {
  const role = user?.role?.toLowerCase()
  if (role === 'tenant') router.push('/tenant')
  else if (role === 'landlord') router.push('/landlord')
  else router.push('/')
}

async function toggleFavorite() {
  if (!isLoggedIn.value) {
    router.push('/login');
    return
  }
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

function bookHouse() {
  if (!isLoggedIn.value) {
    router.push('/login');
    return
  }
  bookingError.value = ''
  showBookModal.value = true
}

async function submitBooking() {
  if (!bookForm.date || !bookForm.time || !bookForm.location) {
    bookingError.value = '请填写完整的预约时间和地点'
    return
  }
  bookingError.value = ''
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
    bookingError.value = e.response?.data?.message || '预约失败，请稍后重试'
  }
}

function handleLogout() {
  doLogout()
}

onMounted(async () => {
  const id = route.params.id
  if (!id) return
  const res = await houseStore.fetchHouseById(id)
  if (res.success) house.value = res.data.house

  if (isLoggedIn.value && user.id) {
    try {
      const checkRes = await favStore.check(id)
    isFav.value = checkRes.data?.favorited || false
    } catch (e) { /* ignore */
    }
  }

  if (route.query.action === 'book') {
    if (isLoggedIn.value) showBookModal.value = true
    else router.push('/login')
  }
})
</script>

<style scoped>
.detail-page { min-height: 100vh; background: var(--bg); }
.container { max-width: 1200px; margin: 0 auto; padding: 24px; }

/* Breadcrumb */
.breadcrumb { margin-bottom: 20px; font-size: 13px; color: var(--text-muted); }
.breadcrumb a { color: var(--text-secondary); }
.breadcrumb a:hover { color: var(--primary); }
.breadcrumb .sep { margin: 0 8px; }
.breadcrumb .current { color: var(--text); font-weight: 500; }

/* Detail layout */
.detail-layout { display: flex; gap: 30px; flex-wrap: wrap; }
.detail-gallery { flex: 1; min-width: 480px; }
.detail-info {
  flex: 1; min-width: 340px;
  background: var(--bg-white);
  border-radius: var(--radius);
  padding: 32px;
  box-shadow: var(--shadow-sm);
}

.house-title { font-size: 24px; font-weight: 700; color: var(--text); margin-bottom: 12px; line-height: 1.3; }
.house-price {
  font-size: 32px;
  font-weight: 700;
  color: var(--accent);
  margin-bottom: 20px;
}
.house-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  color: var(--text-secondary);
  flex-wrap: wrap;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}
.meta-icon { font-size: 16px; }

.house-description {
  padding-top: 20px;
  border-top: 1px solid var(--border);
}
.house-description h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 10px;
}
.house-description p {
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 14px;
}

.house-actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
}
.house-actions .btn-lg { padding: 12px 28px; font-size: 15px; }

.btn-fav {
  background: #fff;
  color: var(--text-secondary);
  border: 1px solid var(--border);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.btn-fav:hover { border-color: #ff4d4f; color: #ff4d4f; }
.btn-fav-active {
  background: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.btn-fav-active:hover { background: #ff4d4f; color: #fff; }

/* Landlord section */
.landlord-section {
  background: var(--bg-white);
  border-radius: var(--radius);
  padding: 24px;
  margin-top: 20px;
  box-shadow: var(--shadow-sm);
}
.landlord-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 16px;
}
.landlord-card {
  display: flex;
  align-items: center;
  gap: 14px;
}
.landlord-avatar {
  width: 48px; height: 48px;
  border-radius: 50%;
  background: var(--primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
.landlord-name { font-size: 15px; font-weight: 600; color: var(--text); }
.landlord-sub { font-size: 12px; color: var(--text-muted); margin-top: 2px; }

/* Modal */
.btn-block { width: 100%; padding: 12px; margin-top: 16px; }

.booking-error {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: #fff2f0;
  border: 1px solid #ffa39e;
  color: #cf1322;
  font-size: 13px;
}

/* Loading skeleton */
.loading-container { padding-top: 40px; }
.skeleton-detail { display: flex; gap: 30px; flex-wrap: wrap; }
.skeleton-gallery {
  flex: 1; min-width: 480px; min-height: 400px;
  border-radius: var(--radius);
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
.skeleton-info { flex: 1; min-width: 300px; }
.skeleton-line {
  height: 14px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  margin-bottom: 14px;
}
.w-30 { width: 30%; }
.w-50 { width: 50%; }
.w-60 { width: 60%; }
.w-80 { width: 80%; }
.w-100 { width: 100%; }

@media (max-width: 768px) {
  .detail-layout { flex-direction: column; }
  .detail-gallery { min-width: auto; }
  .detail-info { min-width: auto; padding: 20px; }
  .house-price { font-size: 24px; }
  .house-actions { flex-direction: column; }
  .house-actions .btn { width: 100%; justify-content: center; }
  .skeleton-gallery { min-width: auto; min-height: 280px; }
}
</style>
