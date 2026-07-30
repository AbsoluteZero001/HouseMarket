<template>
  <div class="admin-page">
    <AppHeader :username="user?.username" :role="user?.role" @logout="handleLogout">
      <template #nav>
        <a href="#" :class="{ active: activeTab === 'dashboard' }" @click.prevent="activeTab = 'dashboard'">工作台</a>
        <a href="#" :class="{ active: activeTab === 'appointments' }" @click.prevent="activeTab = 'appointments'">预约管理</a>
        <a href="#" :class="{ active: activeTab === 'houses' }" @click.prevent="activeTab = 'houses'">房源管理</a>
        <a href="#" :class="{ active: activeTab === 'users' }" @click.prevent="activeTab = 'users'">用户管理</a>
      </template>
    </AppHeader>

    <div class="container">
      <!-- Dashboard -->
      <div v-if="activeTab === 'dashboard'" class="tab-content">
        <div class="stat-cards">
          <div class="stat-card">
            <div class="stat-icon stat-icon-users">👥</div>
            <div class="stat-body">
              <h3>{{ users.length }}</h3>
              <p>用户总数</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon stat-icon-houses">🏠</div>
            <div class="stat-body">
              <h3>{{ allHouses.length }}</h3>
              <p>房源总数</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon stat-icon-apts">📅</div>
            <div class="stat-body">
              <h3>{{ allAppointments.length }}</h3>
              <p>预约总数</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Appointment Management -->
      <div v-if="activeTab === 'appointments'" class="tab-content">
        <div class="section-header">
          <h3>预约管理</h3>
          <span class="count-tag">共 {{ allAppointments.length }} 条</span>
        </div>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr><th>订单ID</th><th>房源</th><th>租客</th><th>房东</th><th>预约时间</th><th>状态</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-if="allAppointments.length === 0">
                <td colspan="7" class="empty-cell">暂无预约记录</td>
              </tr>
              <tr v-for="apt in allAppointments" :key="apt.id">
                <td><span class="id-tag">#{{ apt.id }}</span></td>
                <td>{{ apt.house?.title || '-' }}</td>
                <td>{{ apt.tenant?.realName || apt.tenant?.username || '-' }}</td>
                <td>{{ apt.landlord?.realName || apt.landlord?.username || '-' }}</td>
                <td>{{ apt.time }}</td>
                <td><StatusBadge :status="apt.status" /></td>
                <td>
                  <button class="btn btn-sm btn-danger" @click="handleDeleteApt(apt.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- House Management -->
      <div v-if="activeTab === 'houses'" class="tab-content">
        <div class="section-header">
          <h3>房源管理</h3>
          <span class="count-tag">共 {{ filteredHouses.length }} 条</span>
        </div>
        <div class="search-bar">
          <div class="input-wrap">
            <input v-model="houseSearch" placeholder="搜索房源标题或地址..." @keyup.enter="searchHouses" />
          </div>
          <button class="btn btn-sm" @click="searchHouses">搜索</button>
          <button class="btn btn-sm btn-outline" @click="houseSearch = ''; loadAllHouses()">重置</button>
        </div>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr><th>ID</th><th>标题</th><th>户型</th><th>面积</th><th>价格</th><th>地址</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-if="filteredHouses.length === 0">
                <td colspan="7" class="empty-cell">暂无房源</td>
              </tr>
              <tr v-for="h in filteredHouses" :key="h.id">
                <td><span class="id-tag">#{{ h.id }}</span></td>
                <td>{{ h.title }}</td>
                <td>{{ h.type }}</td>
                <td>{{ h.area }}㎡</td>
                <td>&yen;{{ h.price }}</td>
                <td>{{ h.address }}</td>
                <td>
                  <button class="btn btn-sm btn-danger" @click="handleDeleteHouse(h.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- User Management -->
      <div v-if="activeTab === 'users'" class="tab-content">
        <div class="section-header">
          <h3>用户管理</h3>
          <span class="count-tag">共 {{ filteredUsers.length }} 条</span>
        </div>
        <div class="search-bar">
          <div class="input-wrap" style="flex:1">
            <input v-model="userSearch" placeholder="搜索用户名..." @keyup.enter="searchUsers" />
          </div>
          <select v-model="userRoleFilter" class="select-filter">
            <option value="">全部角色</option>
            <option value="TENANT">租客</option>
            <option value="LANDLORD">房东</option>
            <option value="ADMIN">管理员</option>
          </select>
          <button class="btn btn-sm" @click="searchUsers">搜索</button>
          <button class="btn btn-sm btn-outline" @click="userSearch = ''; userRoleFilter = ''">重置</button>
        </div>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr><th>ID</th><th>用户名</th><th>角色</th><th>状态</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-if="filteredUsers.length === 0">
                <td colspan="5" class="empty-cell">暂无用户</td>
              </tr>
              <tr v-for="u in filteredUsers" :key="u.id">
                <td><span class="id-tag">#{{ u.id }}</span></td>
                <td>{{ u.username }}</td>
                <td>
                  <StatusBadge :status="u.role === 'ADMIN' ? 'approved' : u.role === 'LANDLORD' ? 'pending' : 'completed'" />
                </td>
                <td><StatusBadge :status="u.status || 'normal'" /></td>
                <td>
                  <button class="btn btn-sm btn-danger" @click="handleDeleteUser(u.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <AppAlert :visible="!!alertMsg" :message="alertMsg" :type="alertType" @close="alertMsg = ''" />
  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {deleteUser, getUsers} from '../api/users'
import {deleteHouse, getHouses} from '../api/houses'
import {deleteAppointment, getAppointments} from '../api/appointments'
import {useAuth} from '../composables/useAuth'
import {useAlert} from '../composables/useAlert'
import AppHeader from '../components/AppHeader.vue'
import StatusBadge from '../components/StatusBadge.vue'
import AppAlert from '../components/AppAlert.vue'

const router = useRouter()
const {loadUser, handleLogout: doLogout} = useAuth()
let user = loadUser()
if (!user || user.role !== 'ADMIN') { router.push('/login') }

const activeTab = ref('dashboard')
const users = ref([])
const allHouses = ref([])
const allAppointments = ref([])
const houseSearch = ref('')
const userSearch = ref('')
const userRoleFilter = ref('')
const {alertMsg, alertType, showAlert} = useAlert()

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

function handleLogout() {
  doLogout()
}

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
.admin-page { min-height: 100vh; background: var(--bg); }
.container { max-width: 1200px; margin: 0 auto; padding: 24px; }
.tab-content { animation: fadeIn 0.25s ease; }

/* Stat cards */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 8px;
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
.stat-icon-users { background: var(--primary-light); }
.stat-icon-houses { background: #fff7e6; }
.stat-icon-apts { background: #f6ffed; }
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

/* Section header */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text);
}
.count-tag {
  font-size: 13px;
  color: var(--text-muted);
  background: #f0f0f0;
  padding: 3px 10px;
  border-radius: 12px;
}

/* Search bar */
.search-bar {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 16px;
}
.input-wrap {
  flex: 1;
  max-width: 320px;
}
.input-wrap input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text);
  background: var(--bg-white);
  outline: none;
  transition: border-color var(--transition);
}
.input-wrap input:focus { border-color: var(--primary); }
.select-filter {
  padding: 8px 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text);
  background: var(--bg-white);
  outline: none;
  cursor: pointer;
}

/* Table */
.table-wrap {
  background: var(--bg-white);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th {
  background: #fafafa;
  color: var(--text);
  font-weight: 600;
  padding: 13px 16px;
  text-align: left;
  font-size: 13px;
  border-bottom: 2px solid var(--border);
}
.table td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
  color: var(--text);
}
.table tr:hover td { background: #fafafa; }
.table tr:last-child td { border-bottom: none; }
.empty-cell {
  text-align: center;
  padding: 40px 16px !important;
  color: var(--text-muted);
  font-size: 14px;
}
.id-tag {
  font-family: "SF Mono", "Fira Code", monospace;
  font-size: 12px;
  color: var(--text-muted);
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
}


@media (max-width: 768px) {
  .container { padding: 12px; }
  .stat-cards { grid-template-columns: 1fr; }
  .search-bar { flex-wrap: wrap; }
  .input-wrap { max-width: none; }
  .table-wrap { overflow-x: auto; }
}
</style>
