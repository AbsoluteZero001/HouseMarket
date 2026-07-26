<template>
  <div class="tenant-page">
    <AppHeader :username="user?.username" :role="user?.role" @logout="handleLogout" @profile="showProfile = true">
      <template #nav>
        <a href="#" :class="{ active: activeTab === 'search' }" @click.prevent="activeTab = 'search'">找房源</a>
        <a href="#" :class="{ active: activeTab === 'appointments' }" @click.prevent="activeTab = 'appointments'">预约记录</a>
        <a href="#" :class="{ active: activeTab === 'favorites' }" @click.prevent="activeTab = 'favorites'">我的收藏</a>
      </template>
    </AppHeader>

    <div class="container">
      <!-- Search Section -->
      <div v-if="activeTab === 'search'" class="tab-content">
        <HouseFilter @search="handleSearch" @reset="handleReset" />

        <div v-if="houseStore.loading" class="loading-grid">
          <div class="skeleton-card" v-for="i in 6" :key="i">
            <div class="skeleton-img"></div>
            <div class="skeleton-body">
              <div class="skeleton-line w-70"></div>
              <div class="skeleton-line w-40"></div>
              <div class="skeleton-line w-60"></div>
            </div>
          </div>
        </div>

        <div v-else-if="houseStore.houses.length === 0" class="empty-state">
          <span class="empty-icon">🔍</span>
          <p>暂无符合条件的房源</p>
          <button class="btn btn-outline" @click="handleReset">清除筛选</button>
        </div>

        <div v-else class="house-grid">
          <HouseCard v-for="h in houseStore.houses" :key="h.id" :house="h">
            <template #actions="{ house }">
              <button class="btn btn-sm" @click="viewDetail(house.id)">查看详情</button>
              <button class="btn btn-sm btn-accent" @click="bookHouse(house.id)">预约看房</button>
              <button
                class="btn-icon"
                :class="{ favorited: favStore.isFavorited(house.id) }"
                @click="toggleFav(house.id)"
                :title="favStore.isFavorited(house.id) ? '取消收藏' : '收藏'"
              >
                <svg width="16" height="16" viewBox="0 0 24 24" :fill="favStore.isFavorited(house.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
              </button>
            </template>
          </HouseCard>
        </div>
        <AppPagination v-if="houseStore.total > pageSize" :current="page" :total="houseStore.total" :pageSize="pageSize" @change="onPageChange" />
      </div>

      <!-- Appointments Section -->
      <div v-if="activeTab === 'appointments'" class="tab-content">
        <AppointmentTable
          :appointments="aptStore.appointments"
          :isLandlord="false"
          @cancel="handleCancelAppointment"
          @delete="handleDeleteAppointment"
        />
      </div>

      <!-- Favorites Section -->
      <div v-if="activeTab === 'favorites'" class="tab-content">
        <div v-if="favStore.favorites.length === 0" class="empty-state">
          <span class="empty-icon">💗</span>
          <p>还没有收藏房源</p>
          <button class="btn btn-outline" @click="activeTab = 'search'">去发现好房</button>
        </div>
        <div v-else class="house-grid">
          <HouseCard v-for="f in favStore.favorites" :key="f.id" :house="f.house || f">
            <template #actions="{ house }">
              <button class="btn btn-sm" @click="viewDetail(house.id)">查看详情</button>
              <button class="btn btn-sm btn-accent" @click="bookHouse(house.id)">预约看房</button>
              <button class="btn btn-sm btn-danger" @click="removeFav(house.id)">取消收藏</button>
            </template>
          </HouseCard>
        </div>
      </div>
    </div>

    <!-- Profile Modal -->
    <AppModal :visible="showProfile" title="个人信息" @close="showProfile = false">
      <div class="form-group">
        <label>用户名</label>
        <input :value="user?.username" disabled />
      </div>
      <div class="form-group">
        <label>角色</label>
        <input :value="roleLabel" disabled />
      </div>
      <div class="form-group">
        <label>新密码</label>
        <input v-model="passwordForm.newPassword" type="password" placeholder="留空则不修改" />
      </div>
      <div class="form-group">
        <label>确认新密码</label>
        <input v-model="passwordForm.confirmNewPassword" type="password" placeholder="再次输入新密码" />
      </div>
      <button class="btn btn-block" @click="handleChangePassword">保存修改</button>
    </AppModal>

    <AppAlert :visible="!!alertMsg" :message="alertMsg" :type="alertType" @close="alertMsg = ''" />
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {useHouseStore} from '../stores/houses'
import {useAppointmentStore} from '../stores/appointments'
import {useFavoriteStore} from '../stores/favorites'
import {changePassword} from '../api/users'
import {useWebSocket} from '../composables/useWebSocket'
import AppHeader from '../components/AppHeader.vue'
import HouseFilter from '../components/HouseFilter.vue'
import HouseCard from '../components/HouseCard.vue'
import AppointmentTable from '../components/AppointmentTable.vue'
import AppPagination from '../components/AppPagination.vue'
import AppModal from '../components/AppModal.vue'
import AppAlert from '../components/AppAlert.vue'

const router = useRouter()
const houseStore = useHouseStore()
const aptStore = useAppointmentStore()
const favStore = useFavoriteStore()

let user = null
try {
  user = JSON.parse(localStorage.getItem('user') || 'null')
} catch {
  user = null
}
if (!user) { router.push('/login') }

const activeTab = ref('search')
const page = ref(1)
const pageSize = ref(10)
const searchParams = ref({})
const showProfile = ref(false)
const alertMsg = ref('')
const alertType = ref('success')
const passwordForm = reactive({ newPassword: '', confirmNewPassword: '' })

const roleLabel = computed(() => ({ ADMIN: '管理员', LANDLORD: '房东', TENANT: '租客' })[user?.role] || user?.role)
const { notification, connect, disconnect } = useWebSocket()

function showAlert(msg, type = 'success') { alertMsg.value = msg; alertType.value = type; setTimeout(() => alertMsg.value = '', 3000) }

async function loadHouses() {
  await houseStore.fetchHouses({ ...searchParams.value, page: page.value, pageSize: pageSize.value })
  await favStore.fetchFavorites()
}

async function handleSearch(params) { searchParams.value = params; page.value = 1; await loadHouses() }
async function handleReset() { searchParams.value = {}; page.value = 1; await loadHouses() }
async function onPageChange(p) { page.value = p; await loadHouses() }

function viewDetail(id) { router.push(`/house/${id}`) }
function bookHouse(id) { router.push(`/house/${id}?action=book`) }

async function toggleFav(houseId) {
  if (favStore.isFavorited(houseId)) await removeFav(houseId)
  else { await favStore.add(houseId, user.id); showAlert('已收藏'); await loadHouses() }
}
async function removeFav(houseId) { await favStore.remove(houseId); showAlert('已取消收藏'); await loadHouses() }

async function handleCancelAppointment(id) {
  if (confirm('确认取消此预约？')) { await aptStore.cancel(id); await aptStore.fetchAppointments(); showAlert('预约已取消') }
}
async function handleDeleteAppointment(id) {
  if (confirm('确认删除此记录？')) { await aptStore.remove(id); await aptStore.fetchAppointments(); showAlert('记录已删除') }
}

async function handleChangePassword() {
  if (!passwordForm.newPassword) { alert('请输入新密码'); return }
  if (passwordForm.newPassword !== passwordForm.confirmNewPassword) { alert('两次密码不一致'); return }
  await changePassword(user.id, '', passwordForm.newPassword)
  showAlert('密码修改成功')
  showProfile.value = false
  passwordForm.newPassword = ''; passwordForm.confirmNewPassword = ''
}

function handleLogout() { disconnect(); localStorage.clear(); router.push('/login') }

watch(notification, (n) => {
  if (n) { showAlert(`预约状态更新: ${n.message || ''}`); aptStore.fetchAppointments() }
})
watch(activeTab, (tab) => {
  if (tab === 'appointments') aptStore.fetchAppointments()
  if (tab === 'favorites') favStore.fetchFavorites()
})

onMounted(async () => {
  await loadHouses()
  connect()
})
</script>

<style scoped>
.tenant-page { min-height: 100vh; background: var(--bg); }
.container { max-width: 1200px; margin: 0 auto; padding: 24px; }
.tab-content { animation: fadeIn 0.25s ease; }

/* House grid */
.house-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

/* Action buttons */
.btn-icon {
  width: 34px; height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: #fff;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition);
}
.btn-icon:hover { border-color: #ff4d4f; color: #ff4d4f; }
.btn-icon.favorited {
  background: #fff2f0;
  border-color: #ff4d4f;
  color: #ff4d4f;
}

/* Loading skeleton */
.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
  margin-top: 20px;
}
.skeleton-card {
  background: var(--bg-white);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.skeleton-img {
  height: 200px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
.skeleton-body { padding: 16px; }
.skeleton-line {
  height: 14px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  margin-bottom: 10px;
}
.skeleton-line:last-child { margin-bottom: 0; }
.w-70 { width: 70%; }
.w-60 { width: 60%; }
.w-40 { width: 40%; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--text-muted);
}
.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}
.empty-state p {
  font-size: 15px;
  margin-bottom: 20px;
}

/* Modal button */
.btn-block {
  width: 100%;
  padding: 12px;
  margin-top: 16px;
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

@media (max-width: 768px) {
  .container { padding: 16px; }
  .house-grid { grid-template-columns: 1fr; }
}
</style>
