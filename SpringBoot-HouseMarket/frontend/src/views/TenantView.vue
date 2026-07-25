<template>
  <div class="tenant-page">
    <AppHeader :username="user?.username" :role="user?.role" @logout="handleLogout" @profile="showProfile = true">
      <template #nav>
        <a href="#" :class="{ active: activeTab === 'search' }" @click.prevent="activeTab = 'search'">首页</a>
        <a href="#" :class="{ active: activeTab === 'appointments' }" @click.prevent="activeTab = 'appointments'">预约记录</a>
        <a href="#" :class="{ active: activeTab === 'favorites' }" @click.prevent="activeTab = 'favorites'">我的收藏</a>
      </template>
    </AppHeader>

    <div class="container">
      <!-- Search Section -->
      <div v-if="activeTab === 'search'">
        <HouseFilter @search="handleSearch" @reset="handleReset" />
        <div class="house-list">
          <HouseCard v-for="h in houseStore.houses" :key="h.id" :house="h">
            <template #actions="{ house }">
              <button class="btn" @click="viewDetail(house.id)">查看详情</button>
              <button class="btn" @click="bookHouse(house.id)">预约看房</button>
              <button class="btn" :class="favStore.isFavorited(house.id) ? 'favorited' : 'btn-heart'" @click="toggleFav(house.id)">
                <i :class="favStore.isFavorited(house.id) ? 'fas fa-heart' : 'far fa-heart'"></i>
              </button>
            </template>
          </HouseCard>
        </div>
        <AppPagination :current="page" :total="houseStore.total" :pageSize="pageSize" @change="onPageChange" />
      </div>

      <!-- Appointments Section -->
      <div v-if="activeTab === 'appointments'">
        <AppointmentTable
          :appointments="aptStore.appointments"
          :isLandlord="false"
          @cancel="handleCancelAppointment"
          @delete="handleDeleteAppointment"
        />
      </div>

      <!-- Favorites Section -->
      <div v-if="activeTab === 'favorites'">
        <div class="house-list">
          <HouseCard v-for="f in favStore.favorites" :key="f.id" :house="f.house || f">
            <template #actions="{ house }">
              <button class="btn" @click="viewDetail(house.id)">查看详情</button>
              <button class="btn" @click="bookHouse(house.id)">预约看房</button>
              <button class="btn btn-danger" @click="removeFav(house.id)">取消收藏</button>
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useHouseStore } from '../stores/houses'
import { useAppointmentStore } from '../stores/appointments'
import { useFavoriteStore } from '../stores/favorites'
import { changePassword } from '../api/users'
import { useWebSocket } from '../composables/useWebSocket'
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

const user = JSON.parse(localStorage.getItem('user') || 'null')
if (!user) { router.push('/login') }

const activeTab = ref('search')
const page = ref(1)
const pageSize = ref(10)
const searchParams = ref({})
const showProfile = ref(false)
const alertMsg = ref('')
const alertType = ref('success')
const passwordForm = reactive({ newPassword: '', confirmNewPassword: '' })

const roleLabel = computed(() => {
  const map = { ADMIN: '管理员', LANDLORD: '房东', TENANT: '租客' }
  return map[user?.role] || user?.role
})

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
.tenant-page { min-height: 100vh; }
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.house-list { margin-top: 20px; }
.favorited { background: linear-gradient(135deg, #ff6b6b, #ff8e53); color: #fff; }
.btn-heart { background: #fff; color: #ff6b6b; border: 1px solid #ff6b6b; }
.btn-block { width: 100%; padding: 12px; margin-top: 15px; background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; border: none; border-radius: 6px; font-size: 16px; cursor: pointer; font-weight: 700; }
</style>
