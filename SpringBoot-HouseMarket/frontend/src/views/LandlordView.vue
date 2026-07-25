<template>
  <div class="landlord-page">
    <AppHeader :username="user?.username" :role="user?.role" @logout="handleLogout" @profile="showProfile = true">
      <template #nav>
        <a href="#" :class="{ active: activeTab === 'dashboard' }" @click.prevent="activeTab = 'dashboard'">工作台</a>
        <a href="#" :class="{ active: activeTab === 'houses' }" @click.prevent="activeTab = 'houses'">房源管理</a>
        <a href="#" :class="{ active: activeTab === 'appointments' }" @click.prevent="activeTab = 'appointments'">
          预约记录
          <span class="badge" v-if="pendingCount">{{ pendingCount }}</span>
        </a>
      </template>
    </AppHeader>

    <div class="container">
      <!-- Dashboard -->
      <div v-if="activeTab === 'dashboard'" class="tab-content">
        <div class="dashboard">
          <div class="stat-cards">
            <div class="stat-card">
              <div class="stat-icon stat-icon-houses">🏠</div>
              <div class="stat-body">
                <h3>{{ houseStore.houses.length }}</h3>
                <p>我的房源</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon stat-icon-pending">🔔</div>
              <div class="stat-body">
                <h3>{{ pendingCount }}</h3>
                <p>待处理预约</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon stat-icon-total">📋</div>
              <div class="stat-body">
                <h3>{{ aptStore.appointments.length }}</h3>
                <p>总预约数</p>
              </div>
            </div>
          </div>
          <div class="quick-actions">
            <button class="btn btn-lg" @click="openAddHouse">+ 发布新房源</button>
            <button class="btn btn-lg btn-outline" @click="activeTab = 'appointments'">查看预约</button>
          </div>
        </div>
      </div>

      <!-- House Management -->
      <div v-if="activeTab === 'houses'" class="tab-content">
        <div class="section-header">
          <h3>房源管理</h3>
          <button class="btn" @click="openAddHouse">+ 发布新房源</button>
        </div>
        <div v-if="houseStore.houses.length === 0" class="empty-state">
          <span class="empty-icon">🏠</span>
          <p>还没有发布房源</p>
          <button class="btn" @click="openAddHouse">发布您的第一套房源</button>
        </div>
        <div v-else class="house-grid">
          <HouseCard v-for="h in houseStore.houses" :key="h.id" :house="h">
            <template #actions="{ house }">
              <button class="btn btn-sm btn-outline" @click="openEditHouse(house.id)">编辑</button>
              <button class="btn btn-sm btn-danger" @click="handleDeleteHouse(house.id)">删除</button>
            </template>
          </HouseCard>
        </div>
      </div>

      <!-- Appointment Management -->
      <div v-if="activeTab === 'appointments'" class="tab-content">
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
  try { await aptStore.fetchAppointments('pending'); pendingCount.value = aptStore.appointments.length } catch (e) { /* ignore */ }
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
  await loadAppointments(); await loadPendingCount()
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
  await loadHouses(); await loadAppointments(); await loadPendingCount()
  connect()
  pollTimer = setInterval(loadPendingCount, 30000)
})
onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })
</script>

<style scoped>
.landlord-page { min-height: 100vh; background: var(--bg); }
.container { max-width: 1200px; margin: 0 auto; padding: 24px; }
.tab-content { animation: fadeIn 0.25s ease; }

/* Dashboard */
.dashboard { padding: 20px 0; }
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}
.stat-card {
  background: var(--bg-white);
  border-radius: var(--radius);
  padding: 24px;
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all var(--transition);
}
.stat-card:hover { box-shadow: var(--shadow); transform: translateY(-2px); }
.stat-icon {
  width: 52px; height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}
.stat-icon-houses { background: var(--primary-light); }
.stat-icon-pending { background: #fff7e6; }
.stat-icon-total { background: #f6ffed; }
.stat-body h3 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text);
  line-height: 1.2;
}
.stat-body p {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 2px;
}
.quick-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 8px;
}

/* Section header */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text);
}

/* House grid */
.house-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

/* Badge */
.badge {
  background: var(--danger);
  color: #fff;
  border-radius: 10px;
  padding: 1px 7px;
  font-size: 11px;
  font-weight: 600;
  min-width: 18px;
  text-align: center;
  line-height: 18px;
  display: inline-block;
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
  .stat-cards { grid-template-columns: 1fr; }
  .quick-actions { flex-direction: column; align-items: stretch; }
}
</style>
