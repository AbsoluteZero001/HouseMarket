<template>
  <div class="tenant-page">
    <AppHeader :username="user?.username" :role="user?.role" @logout="handleLogout" @profile="showProfile = true">
      <template #nav>
        <a href="#" :class="{ active: activeTab === 'search' }" @click.prevent="activeTab = 'search'">找房源</a>
        <a href="#" :class="{ active: activeTab === 'appointments' }" @click.prevent="activeTab = 'appointments'">预约记录</a>
        <a href="#" :class="{ active: activeTab === 'notifications' }" @click.prevent="activeTab = 'notifications'">通知中心</a>
        <a href="#" :class="{ active: activeTab === 'favorites' }" @click.prevent="activeTab = 'favorites'">我的收藏</a>
      </template>
    </AppHeader>

    <div class="container">
      <!-- Search Section -->
      <div v-if="activeTab === 'search'" class="tab-content">
        <section class="tenant-hero" v-reveal>
          <div class="tenant-hero-copy">
            <p class="kicker">RENT SMARTER</p>
            <h1>找到你的理想家</h1>
            <p>真实房源实时更新，预约、审批、通知全程透明可溯</p>
          </div>
          <div class="tenant-hero-stats">
            <div><strong>{{ houseStore.houses.length }}</strong><span>在找房源</span></div>
            <div><strong>{{ favStore.favorites.length }}</strong><span>我的收藏</span></div>
            <div><strong>{{ aptStore.appointments.length }}</strong><span>预约记录</span></div>
          </div>
        </section>
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
          <HouseCard v-for="(h, i) in houseStore.houses" :key="h.id" :house="h" v-reveal="{ delay: (i % 3) * 70 }">
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
          @flow="openFlow"
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
          <HouseCard v-for="(f, i) in favStore.favorites" :key="f.id" :house="f.house || f"
                     v-reveal="{ delay: (i % 3) * 70 }">
            <template #actions="{ house }">
              <button class="btn btn-sm" @click="viewDetail(house.id)">查看详情</button>
              <button class="btn btn-sm btn-accent" @click="bookHouse(house.id)">预约看房</button>
              <button class="btn btn-sm btn-danger" @click="removeFav(house.id)">取消收藏</button>
            </template>
          </HouseCard>
        </div>
      </div>

      <!-- Notification Center -->
      <div v-if="activeTab === 'notifications'" class="tab-content">
        <NotificationCenter />
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
        <input :value="roleLabelComputed" disabled/>
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
    <AppConfirm
        :visible="confirmState.visible"
        :title="confirmState.title"
        :message="confirmState.message"
        @confirm="confirmAction"
        @cancel="confirmState.visible = false"
    />
    <AppointmentFlow
        :visible="showFlow"
        :appointment="flowAppointment"
        :flows="flowRecords"
        @close="showFlow = false"
    />
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
import {roleLabel, useAuth} from '../composables/useAuth'
import {useAlert} from '../composables/useAlert'
import AppHeader from '../components/AppHeader.vue'
import HouseFilter from '../components/HouseFilter.vue'
import HouseCard from '../components/HouseCard.vue'
import AppointmentTable from '../components/AppointmentTable.vue'
import AppointmentFlow from '../components/AppointmentFlow.vue'
import NotificationCenter from '../components/NotificationCenter.vue'
import AppPagination from '../components/AppPagination.vue'
import AppModal from '../components/AppModal.vue'
import AppAlert from '../components/AppAlert.vue'
import AppConfirm from '../components/AppConfirm.vue'

const router = useRouter()
const houseStore = useHouseStore()
const aptStore = useAppointmentStore()
const favStore = useFavoriteStore()

const {loadUser, handleLogout: doLogout} = useAuth()
let user = loadUser()
if (!user) user = {}

const activeTab = ref('search')
const page = ref(1)
const pageSize = ref(10)
const searchParams = ref({})
const showProfile = ref(false)
const {alertMsg, alertType, showAlert} = useAlert()
const passwordForm = reactive({ newPassword: '', confirmNewPassword: '' })
const confirmState = reactive({visible: false, title: '', message: '', handler: null})
const showFlow = ref(false)
const flowAppointment = ref(null)
const flowRecords = ref([])

function askConfirm(title, message, handler) {
  confirmState.title = title
  confirmState.message = message
  confirmState.handler = handler
  confirmState.visible = true
}

function confirmAction() {
  const handler = confirmState.handler
  confirmState.visible = false
  if (handler) handler()
}

const roleLabelComputed = computed(() => roleLabel(user.role))
const { notification, connect, disconnect } = useWebSocket()

async function loadHouses() {
  await houseStore.fetchHouses({ ...searchParams.value, status: 'NORMAL', page: page.value, pageSize: pageSize.value })
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
  askConfirm('取消预约', '确认取消这条看房预约吗？', async () => {
    await aptStore.cancel(id)
    await aptStore.fetchAppointments()
    showAlert('预约已取消')
  })
}
async function handleDeleteAppointment(id) {
  askConfirm('删除记录', '删除后无法恢复，确认继续吗？', async () => {
    await aptStore.remove(id)
    await aptStore.fetchAppointments()
    showAlert('记录已删除')
  })
}

async function openFlow(apt) {
  flowAppointment.value = apt
  flowRecords.value = []
  showFlow.value = true
  try {
    const res = await aptStore.fetchFlow(apt.id)
    flowRecords.value = res?.data?.flows || []
  } catch (e) { /* ignore */
  }
}

async function handleChangePassword() {
  if (!passwordForm.newPassword) {
    showAlert('请输入新密码', 'error');
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmNewPassword) {
    showAlert('两次密码不一致', 'error');
    return
  }
  await changePassword(user.id, '', passwordForm.newPassword)
  showAlert('密码修改成功')
  showProfile.value = false
  passwordForm.newPassword = ''; passwordForm.confirmNewPassword = ''
}

function handleLogout() {
  doLogout(() => disconnect())
}

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

.tenant-hero {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 22px;
  padding: 30px 34px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(90deg, rgba(11, 31, 63, 0.9), rgba(91, 33, 182, 0.72)),
  url('/backgrounds/tenant-hero.png') center / cover no-repeat;
  box-shadow: 0 22px 54px rgba(11, 31, 63, 0.24);
  overflow: hidden;
}

.tenant-hero::after {
  content: "";
  position: absolute;
  inset: -40%;
  background: conic-gradient(from 210deg at 50% 50%, rgba(103, 232, 249, 0.16), transparent 28%, rgba(236, 72, 153, 0.13) 52%, transparent 78%);
  animation: aurora-drift 16s ease-in-out infinite alternate;
}

.tenant-hero-copy,
.tenant-hero-stats {
  position: relative;
  z-index: 1;
}

.tenant-hero .kicker {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 3px;
  color: #67e8f9;
  margin-bottom: 6px;
}

.tenant-hero h1 {
  font-size: 30px;
  font-weight: 800;
  margin-bottom: 6px;
  letter-spacing: 0;
}

.tenant-hero-copy p:last-child {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.tenant-hero-stats {
  display: flex;
  gap: 26px;
}

.tenant-hero-stats strong {
  display: block;
  font-size: 28px;
  font-weight: 800;
}

.tenant-hero-stats span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.66);
}

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

@keyframes aurora-drift {
  0% {
    transform: translate3d(-4%, -3%, 0) rotate(0deg) scale(1);
  }
  50% {
    transform: translate3d(5%, 4%, 0) rotate(8deg) scale(1.1);
  }
  100% {
    transform: translate3d(-4%, -3%, 0) rotate(0deg) scale(1);
  }
}

@media (max-width: 768px) {
  .container { padding: 16px; }
  .house-grid { grid-template-columns: 1fr; }

  .tenant-hero {
    flex-direction: column;
    align-items: flex-start;
    padding: 24px 20px;
  }

  .tenant-hero-stats {
    width: 100%;
    justify-content: space-between;
    gap: 12px;
  }

  .tenant-hero-stats strong {
    font-size: 22px;
  }
}
</style>
