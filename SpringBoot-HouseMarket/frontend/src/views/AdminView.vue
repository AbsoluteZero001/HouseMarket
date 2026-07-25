<template>
  <div class="admin-page">
    <AppHeader :username="user?.username" :role="user?.role" @logout="handleLogout" @profile="showProfile = true">
      <template #nav>
        <a href="#" :class="{ active: activeTab === 'dashboard' }" @click.prevent="activeTab = 'dashboard'">首页</a>
        <a href="#" :class="{ active: activeTab === 'appointments' }" @click.prevent="activeTab = 'appointments'">预约管理</a>
        <a href="#" :class="{ active: activeTab === 'houses' }" @click.prevent="activeTab = 'houses'">房源管理</a>
        <a href="#" :class="{ active: activeTab === 'users' }" @click.prevent="activeTab = 'users'">用户管理</a>
      </template>
    </AppHeader>

    <div class="container">
      <!-- Dashboard -->
      <div v-if="activeTab === 'dashboard'" class="dashboard">
        <div class="stat-cards">
          <div class="stat-card"><h3>{{ users.length }}</h3><p>用户总数</p></div>
          <div class="stat-card"><h3>{{ allHouses.length }}</h3><p>房源总数</p></div>
          <div class="stat-card"><h3>{{ allAppointments.length }}</h3><p>预约总数</p></div>
        </div>
      </div>

      <!-- Appointment Management -->
      <div v-if="activeTab === 'appointments'">
        <table class="table">
          <thead><tr><th>订单ID</th><th>房源</th><th>租客</th><th>房东</th><th>预约时间</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="allAppointments.length === 0"><td colspan="7" style="text-align:center;padding:30px;color:#999;">暂无预约</td></tr>
            <tr v-for="apt in allAppointments" :key="apt.id">
              <td>#{{ apt.id }}</td>
              <td>{{ apt.house?.title || '-' }}</td>
              <td>{{ apt.tenant?.realName || apt.tenant?.username || '-' }}</td>
              <td>{{ apt.landlord?.realName || apt.landlord?.username || '-' }}</td>
              <td>{{ apt.time }}</td>
              <td><StatusBadge :status="apt.status" /></td>
              <td><button class="btn btn-danger btn-sm" @click="handleDeleteApt(apt.id)">删除</button></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- House Management -->
      <div v-if="activeTab === 'houses'">
        <div class="search-bar">
          <input v-model="houseSearch" placeholder="搜索房源标题或地址" />
          <button class="btn" @click="searchHouses">搜索</button>
          <button class="btn btn-secondary" @click="houseSearch = ''; loadAllHouses()">重置</button>
        </div>
        <table class="table" style="margin-top:20px">
          <thead><tr><th>ID</th><th>标题</th><th>户型</th><th>面积</th><th>价格</th><th>地址</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="filteredHouses.length === 0"><td colspan="7" style="text-align:center;padding:30px;color:#999;">暂无房源</td></tr>
            <tr v-for="h in filteredHouses" :key="h.id">
              <td>{{ h.id }}</td>
              <td>{{ h.title }}</td>
              <td>{{ h.type }}</td>
              <td>{{ h.area }}㎡</td>
              <td>{{ h.price }}</td>
              <td>{{ h.address }}</td>
              <td><button class="btn btn-danger btn-sm" @click="handleDeleteHouse(h.id)">删除</button></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- User Management -->
      <div v-if="activeTab === 'users'">
        <div class="search-bar">
          <input v-model="userSearch" placeholder="搜索用户名" />
          <select v-model="userRoleFilter">
            <option value="">全部角色</option>
            <option value="TENANT">租客</option>
            <option value="LANDLORD">房东</option>
            <option value="ADMIN">管理员</option>
          </select>
          <button class="btn" @click="searchUsers">搜索</button>
          <button class="btn btn-secondary" @click="userSearch = ''; userRoleFilter = ''">重置</button>
        </div>
        <table class="table" style="margin-top:20px">
          <thead><tr><th>ID</th><th>用户名</th><th>角色</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="filteredUsers.length === 0"><td colspan="5" style="text-align:center;padding:30px;color:#999;">暂无用户</td></tr>
            <tr v-for="u in filteredUsers" :key="u.id">
              <td>{{ u.id }}</td>
              <td>{{ u.username }}</td>
              <td><StatusBadge :status="u.role === 'ADMIN' ? 'approved' : u.role === 'LANDLORD' ? 'pending' : 'completed'" /></td>
              <td><StatusBadge :status="u.status || 'normal'" /></td>
              <td><button class="btn btn-danger btn-sm" @click="handleDeleteUser(u.id)">删除</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <AppAlert :visible="!!alertMsg" :message="alertMsg" :type="alertType" @close="alertMsg = ''" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getUsers, deleteUser } from '../api/users'
import { getHouses, deleteHouse } from '../api/houses'
import { getAppointments, deleteAppointment } from '../api/appointments'
import AppHeader from '../components/AppHeader.vue'
import StatusBadge from '../components/StatusBadge.vue'
import AppAlert from '../components/AppAlert.vue'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || 'null')
if (!user || user.role !== 'ADMIN') { router.push('/login') }

const activeTab = ref('dashboard')
const users = ref([])
const allHouses = ref([])
const allAppointments = ref([])
const houseSearch = ref('')
const userSearch = ref('')
const userRoleFilter = ref('')
const alertMsg = ref('')
const alertType = ref('success')

const filteredHouses = computed(() => {
  if (!houseSearch.value) return allHouses.value
  const kw = houseSearch.value.toLowerCase()
  return allHouses.value.filter(h => h.title?.toLowerCase().includes(kw) || h.address?.toLowerCase().includes(kw))
})

const filteredUsers = computed(() => {
  let list = users.value
  if (userSearch.value) {
    const kw = userSearch.value.toLowerCase()
    list = list.filter(u => u.username?.toLowerCase().includes(kw))
  }
  if (userRoleFilter.value) list = list.filter(u => u.role === userRoleFilter.value)
  return list
})

function showAlert(msg, type = 'success') { alertMsg.value = msg; alertType.value = type; setTimeout(() => alertMsg.value = '', 3000) }

async function loadAllUsers() { try { const res = await getUsers(); users.value = res.data || [] } catch (e) { /* ignore */ } }
async function loadAllHouses() {
  try {
    const res = await getHouses({ page: 1, pageSize: 1000 })
    if (res.data.success) allHouses.value = res.data.data.houses || []
  } catch (e) { /* ignore */ }
}
async function loadAllAppointments() {
  try { const res = await getAppointments(); if (res.data.success) allAppointments.value = res.data.data.appointments || [] } catch (e) { /* ignore */ }
}

async function handleDeleteUser(id) { if (confirm('确认删除此用户？')) { await deleteUser(id); showAlert('用户已删除'); await loadAllUsers() } }
async function handleDeleteHouse(id) { if (confirm('确认删除此房源？')) { await deleteHouse(id); showAlert('房源已删除'); await loadAllHouses() } }
async function handleDeleteApt(id) { if (confirm('确认删除此记录？')) { await deleteAppointment(id); showAlert('记录已删除'); await loadAllAppointments() } }

function searchHouses() { /* filteredHouses computed, no-op */ }
function searchUsers() { /* filteredUsers computed, no-op */ }

function handleLogout() { localStorage.clear(); router.push('/login') }

watch(activeTab, (tab) => {
  if (tab === 'users') loadAllUsers()
  if (tab === 'houses') loadAllHouses()
  if (tab === 'appointments') loadAllAppointments()
})

onMounted(async () => {
  await Promise.all([loadAllUsers(), loadAllHouses(), loadAllAppointments()])
})
</script>

<style scoped>
.admin-page { min-height: 100vh; }
.container { max-width: 1200px; margin: 0 auto; padding: 20px; }
.stat-cards { display: flex; gap: 20px; justify-content: center; margin: 40px 0; flex-wrap: wrap; }
.stat-card { background: rgba(255,255,255,0.95); border-radius: 12px; padding: 30px 40px; box-shadow: 0 2px 15px rgba(0,0,0,0.1); }
.stat-card h3 { font-size: 2rem; background: linear-gradient(135deg, #667eea, #764ba2); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.stat-card p { color: #999; margin-top: 5px; }
.search-bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.search-bar input, .search-bar select { padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; }
.table { width: 100%; border-collapse: collapse; background: rgba(255,255,255,0.95); border-radius: 8px; overflow: hidden; box-shadow: 0 2px 15px rgba(0,0,0,0.1); }
.table th { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; padding: 12px 15px; text-align: left; font-size: 12px; text-transform: uppercase; }
.table td { padding: 10px 15px; border-bottom: 1px solid #e0e0e0; }
.btn-sm { padding: 5px 10px; font-size: 11px; }
</style>
