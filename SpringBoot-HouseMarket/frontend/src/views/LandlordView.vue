<template>
  <div class="landlord-page">
    <AppHeader :username="user?.username" :role="user?.role" @logout="handleLogout" @profile="showProfile = true">
      <template #nav>
        <a href="#" :class="{ active: activeTab === 'dashboard' }" @click.prevent="activeTab = 'dashboard'">首页</a>
        <a href="#" :class="{ active: activeTab === 'houses' }" @click.prevent="activeTab = 'houses'">房源管理</a>
        <a href="#" :class="{ active: activeTab === 'appointments' }" @click.prevent="activeTab = 'appointments'">
          预约记录 <span class="badge" v-if="pendingCount">{{ pendingCount }}</span>
        </a>
      </template>
    </AppHeader>

    <div class="container">
      <!-- Dashboard -->
      <div v-if="activeTab === 'dashboard'" class="dashboard">
        <div class="stat-cards">
          <div class="stat-card"><h3>{{ houseStore.houses.length }}</h3><p>我的房源</p></div>
          <div class="stat-card"><h3>{{ pendingCount }}</h3><p>待处理预约</p></div>
          <div class="stat-card"><h3>{{ aptStore.appointments.length }}</h3><p>总预约</p></div>
        </div>
        <div class="quick-actions">
          <button class="btn" @click="activeTab = 'houses'; showAddModal = true">发布新房源</button>
          <button class="btn" @click="activeTab = 'appointments'">查看预约</button>
        </div>
      </div>

      <!-- House Management -->
      <div v-if="activeTab === 'houses'">
        <button class="btn" style="margin-bottom:20px" @click="openAddHouse">+ 发布新房源</button>
        <div class="house-list">
          <HouseCard v-for="h in houseStore.houses" :key="h.id" :house="h">
            <template #actions="{ house }">
              <button class="btn" @click="openEditHouse(house.id)">编辑</button>
              <button class="btn btn-danger" @click="handleDeleteHouse(house.id)">删除</button>
            </template>
          </HouseCard>
        </div>
      </div>

      <!-- Appointment Management -->
      <div v-if="activeTab === 'appointments'">
        <AppointmentTable
          :appointments="aptStore.appointments"
          :isLandlord="true"
          @approve="handleApprove"
          @reject="handleReject"
          @delete="handleDeleteApt"
        />
      </div>
    </div>

    <!-- Add/Edit House Modal -->
    <AppModal :visible="showAddModal || showEditModal" :title="showEditModal ? '编辑房源' : '发布新房源'" width="700px" @close="closeHouseModal">
      <HouseForm ref="houseFormRef" :initial="editingHouse" :submitLabel="showEditModal ? '保存修改' : '发布房源'" @submit="handleHouseSubmit" @cancel="closeHouseModal" />
    </AppModal>

    <!-- Profile Modal -->
    <AppModal :visible="showProfile" title="个人信息" @close="showProfile = false">
      <div class="form-group"><label>用户名</label><input :value="user?.username" disabled /></div>
      <div class="form-group"><label>角色</label><input :value="roleLabel" disabled /></div>
      <div class="form-group"><label>旧密码</label><input v-model="passwordForm.oldPassword" type="password" /></div>
      <div class="form-group"><label>新密码</label><input v-model="passwordForm.newPassword" type="password" placeholder="留空则不修改" /></div>
      <div class="form-group"><label>确认新密码</label><input v-model="passwordForm.confirmNewPassword" type="password" /></div>
      <button class="btn btn-block" @click="handleChangePassword">保存修改</button>
    </AppModal>

    <AppAlert :visible="!!alertMsg" :message="alertMsg" :type="alertType" @close="alertMsg = ''" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useHouseStore } from '../stores/houses'
import { useAppointmentStore } from '../stores/appointments'
import { changePassword } from '../api/users'
import { useWebSocket } from '../composables/useWebSocket'
import AppHeader from '../components/AppHeader.vue'
import HouseCard from '../components/HouseCard.vue'
import HouseForm from '../components/HouseForm.vue'
import AppointmentTable from '../components/AppointmentTable.vue'
import AppModal from '../components/AppModal.vue'
import AppAlert from '../components/AppAlert.vue'

const router = useRouter()
const houseStore = useHouseStore()
const aptStore = useAppointmentStore()

const user = JSON.parse(localStorage.getItem('user') || 'null')
if (!user) { router.push('/login') }

const activeTab = ref('dashboard')
const showAddModal = ref(false)
const showEditModal = ref(false)
const showProfile = ref(false)
const editingHouse = ref({})
const houseFormRef = ref(null)
const alertMsg = ref('')
const alertType = ref('success')
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmNewPassword: '' })
const pendingCount = ref(0)

let pollTimer = null

const roleLabel = computed(() => ({ ADMIN: '管理员', LANDLORD: '房东', TENANT: '租客' })[user?.role] || user?.role)
const { notification, connect, disconnect } = useWebSocket()

function showAlert(msg, type = 'success') { alertMsg.value = msg; alertType.value = type; setTimeout(() => alertMsg.value = '', 3000) }

async function loadHouses() { await houseStore.fetchLandlordHouses(user.id) }
async function loadAppointments() { await aptStore.fetchAppointments() }
async function loadPendingCount() {
  try { const res = await aptStore.fetchAppointments('pending'); pendingCount.value = aptStore.appointments.length } catch (e) { /* ignore */ }
}

function openAddHouse() { editingHouse.value = {}; showAddModal.value = true }
async function openEditHouse(id) {
  const res = await houseStore.fetchHouseById(id)
  if (res.success) { editingHouse.value = res.data.house; showEditModal.value = true }
}

function closeHouseModal() { showAddModal.value = false; showEditModal.value = false }

async function handleHouseSubmit(data) {
  let imageUrl = data.image || ''
  if (data.imageFile) {
    try {
      const uploadRes = await houseStore.uploadHouseImage(data.imageFile)
      imageUrl = typeof uploadRes === 'string' ? uploadRes : uploadRes.data
    } catch (e) { showAlert('图片上传失败', 'error'); return }
  }

  const payload = { title: data.title, type: data.type, area: data.area, price: data.price, address: data.address, description: data.description, image: imageUrl }

  if (showEditModal.value) {
    await houseStore.editHouse(editingHouse.value.id, payload)
    showAlert('房源已更新')
  } else {
    await houseStore.addHouse(payload)
    showAlert('房源发布成功')
  }
  closeHouseModal()
  await loadHouses()
}

async function handleDeleteHouse(id) {
  if (!confirm('确认删除此房源？')) return
  await houseStore.removeHouse(id)
  showAlert('房源已删除')
  await loadHouses()
}

async function handleApprove(id) { await aptStore.approve(id); showAlert('预约已批准'); await loadAppointments(); await loadPendingCount() }
async function handleReject(id) { await aptStore.reject(id); showAlert('预约已拒绝'); await loadAppointments(); await loadPendingCount() }
async function handleDeleteApt(id) {
  if (!confirm('确认删除此记录？')) return
  await aptStore.remove(id)
  showAlert('记录已删除')
  await loadAppointments()
  await loadPendingCount()
}

async function handleChangePassword() {
  if (!passwordForm.newPassword) { alert('请输入新密码'); return }
  if (passwordForm.newPassword !== passwordForm.confirmNewPassword) { alert('两次密码不一致'); return }
  await changePassword(user.id, passwordForm.oldPassword, passwordForm.newPassword)
  showAlert('密码修改成功')
  showProfile.value = false
  passwordForm.newPassword = ''; passwordForm.confirmNewPassword = ''; passwordForm.oldPassword = ''
}

function handleLogout() { disconnect(); if (pollTimer) clearInterval(pollTimer); localStorage.clear(); router.push('/login') }

watch(notification, (n) => {
  if (n) { showAlert(`新预约通知: ${n.message || ''}`); loadAppointments(); loadPendingCount() }
})

onMounted(async () => {
  await loadHouses()
  await loadAppointments()
  await loadPendingCount()
  connect()
  pollTimer = setInterval(loadPendingCount, 30000)
})

onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })
</script>

<style scoped>
.landlord-page { min-height: 100vh; }
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.dashboard { text-align: center; }
.stat-cards { display: flex; gap: 20px; justify-content: center; margin: 40px 0; flex-wrap: wrap; }
.stat-card { background: rgba(255,255,255,0.95); border-radius: 12px; padding: 30px 40px; box-shadow: 0 2px 15px rgba(0,0,0,0.1); }
.stat-card h3 { font-size: 2rem; background: linear-gradient(135deg, #667eea, #764ba2); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.stat-card p { color: #999; margin-top: 5px; }
.quick-actions { display: flex; gap: 16px; justify-content: center; }
.badge { background: #dc3545; color: #fff; border-radius: 10px; padding: 1px 6px; font-size: 10px; }
.house-list { margin-top: 20px; }
.btn-block { width: 100%; padding: 12px; margin-top: 15px; background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; border: none; border-radius: 6px; font-size: 16px; cursor: pointer; font-weight: 700; }
</style>
