<template>
  <div class="landlord-page">
    <AppHeader :username="user?.username" :role="user?.role" @logout="handleLogout" @profile="showProfile = true">
      <template #nav>
        <a href="#" :class="{ active: activeTab === 'dashboard' }" @click.prevent="activeTab = 'dashboard'">工作台</a>
        <a href="#" :class="{ active: activeTab === 'houses' }" @click.prevent="activeTab = 'houses'">房源管理</a>
        <a href="#" :class="{ active: activeTab === 'appointments' }" @click.prevent="activeTab = 'appointments'">
          预约审批
          <span class="badge" v-if="pendingCount">{{ pendingCount }}</span>
        </a>
        <a href="#" :class="{ active: activeTab === 'notifications' }" @click.prevent="activeTab = 'notifications'">通知中心</a>
      </template>
    </AppHeader>

    <div class="page-shell">
      <!-- Dashboard -->
      <div v-if="activeTab === 'dashboard'" class="tab-content">
        <section class="workbench-hero" v-reveal>
          <div class="hero-glow"></div>
          <div class="hero-copy">
            <p class="kicker">LANDLORD WORKBENCH</p>
            <h1>房东审批工作台</h1>
            <p>发布 → 预约 → 审批 → 通知，全程状态一致、可追踪</p>
          </div>
          <div class="review-banner" v-if="applicationStatus && applicationStatus !== 'approved'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            <span>房东入驻审核{{ applicationStatus === 'pending' ? '中，审核通过后才能发布房源' : '未通过，请联系管理员' }}</span>
          </div>
          <div class="hero-actions">
            <button class="btn btn-lg" :disabled="!canPublish" @click="openAddHouse">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
                   stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              发布新房源
            </button>
            <button class="btn btn-lg btn-outline" @click="activeTab = 'appointments'">查看预约审批</button>
          </div>
          <div class="hero-stats">
            <div class="hero-stat">
              <strong>{{ houseStore.houses.length }}</strong>
              <span>我的房源</span>
            </div>
            <div class="hero-stat">
              <strong>{{ pendingCount }}</strong>
              <span>待审批</span>
            </div>
            <div class="hero-stat">
              <strong>{{ aptStore.appointments.length }}</strong>
              <span>总预约</span>
            </div>
            <div class="hero-stat">
              <strong>{{ notifications.length }}</strong>
              <span>通知</span>
            </div>
          </div>
        </section>

        <section class="pipeline-section" v-reveal>
          <div class="pipeline-head">
            <div>
              <p class="kicker">APPROVAL FLOW ENGINE</p>
              <h3>预约流程闭环</h3>
            </div>
            <span class="engine-status"><i></i> 引擎运行中</span>
          </div>
          <div class="pipeline">
            <div class="pipeline-stage" v-for="(stage, idx) in pipelineStages" :key="stage.key"
                 :class="{ hot: stage.key === 'APPROVE' }">
              <div class="pipeline-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     stroke-linecap="round" stroke-linejoin="round">
                  <path v-if="stage.key === 'PUBLISH'"
                        d="M3 9l1.5-5.5A1.5 1.5 0 0 1 5.97 2.5h12.06a1.5 1.5 0 0 1 1.47 1L21 9"/>
                  <path v-if="stage.key === 'PUBLISH'" d="M3 9a3 3 0 0 0 6 0 3 3 0 0 0 6 0 3 3 0 0 0 6 0"/>
                  <path v-if="stage.key === 'PUBLISH'" d="M4 9v11a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V9"/>
                  <path v-if="stage.key === 'BOOK'" d="M8 2v4"/>
                  <path v-if="stage.key === 'BOOK'" d="M16 2v4"/>
                  <rect v-if="stage.key === 'BOOK'" x="3" y="4" width="18" height="18" rx="2"/>
                  <path v-if="stage.key === 'BOOK'" d="M3 10h18"/>
                  <path v-if="stage.key === 'APPROVE'" d="M20 6 9 17l-5-5"/>
                  <path v-if="stage.key === 'NOTIFY'" d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                  <path v-if="stage.key === 'NOTIFY'" d="M13.73 21a2 2 0 0 1-3.46 0"/>
                </svg>
              </div>
              <div class="pipeline-copy">
                <span>{{ stage.label }}</span>
                <strong>{{ stage.count }}</strong>
                <small>{{ stage.sub }}</small>
              </div>
              <div class="pipeline-connector" v-if="idx < pipelineStages.length - 1"></div>
            </div>
          </div>
        </section>

        <section class="dashboard-grid">
          <div class="panel notification-card" v-reveal>
            <div class="panel-head">
              <h3>通知中心</h3>
              <span class="live-dot"><i></i> 实时</span>
            </div>
            <div class="notif-list">
              <div v-for="n in notifications" :key="n.id" class="notif-item" :class="'notif-' + n.type">
                <span class="notif-dot"></span>
                <div>
                  <p>{{ n.title }}</p>
                  <small>{{ n.time }}</small>
                </div>
              </div>
              <div v-if="notifications.length === 0" class="panel-empty">暂无通知，有新的预约会实时提醒</div>
            </div>
          </div>

          <div class="panel quick-panel" v-reveal>
            <div class="panel-head">
              <h3>快捷入口</h3>
            </div>
            <div class="quick-grid">
              <button class="quick-action" @click="openAddHouse">
                <span class="qa-icon qa-blue">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                       stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5"
                                                                                                                  y1="12"
                                                                                                                  x2="19"
                                                                                                                  y2="12"/></svg>
                </span>
                <strong>发布房源</strong>
                <small>新体验即刻上线</small>
              </button>
              <button class="quick-action" @click="activeTab = 'appointments'">
                <span class="qa-icon qa-pink">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                       stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                </span>
                <strong>审批预约</strong>
                <small>{{ pendingCount }} 条待处理</small>
              </button>
              <button class="quick-action" @click="activeTab = 'houses'">
                <span class="qa-icon qa-violet">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                       stroke-linecap="round" stroke-linejoin="round"><path
                      d="M3 9l1.5-5.5A1.5 1.5 0 0 1 5.97 2.5h12.06a1.5 1.5 0 0 1 1.47 1L21 9"/><path
                      d="M3 9a3 3 0 0 0 6 0 3 3 0 0 0 6 0 3 3 0 0 0 6 0"/><path
                      d="M4 9v11a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V9"/></svg>
                </span>
                <strong>管理房源</strong>
                <small>{{ houseStore.houses.length }} 套在管</small>
              </button>
            </div>
          </div>
        </section>
      </div>

      <!-- House Management -->
      <div v-if="activeTab === 'houses'" class="tab-content">
        <div class="section-header" v-reveal>
          <div>
            <p class="kicker">MY PROPERTIES</p>
            <h3>房源管理</h3>
          </div>
          <button class="btn" :disabled="!canPublish" @click="openAddHouse">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
                 stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            发布新房源
          </button>
        </div>
        <div v-if="houseStore.houses.length === 0" class="empty-state" v-reveal>
          <span class="empty-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4"
                 stroke-linecap="round" stroke-linejoin="round"><path
                d="M3 9l1.5-5.5A1.5 1.5 0 0 1 5.97 2.5h12.06a1.5 1.5 0 0 1 1.47 1L21 9"/><path
                d="M3 9a3 3 0 0 0 6 0 3 3 0 0 0 6 0 3 3 0 0 0 6 0"/><path d="M4 9v11a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V9"/></svg>
          </span>
          <p>还没有发布房源</p>
          <button class="btn" :disabled="!canPublish" @click="openAddHouse">发布您的第一套房源</button>
        </div>
        <div v-else class="house-grid">
          <HouseCard v-for="(h, i) in houseStore.houses" :key="h.id" :house="h" v-reveal="{ delay: (i % 3) * 70 }">
            <template #actions="{ house }">
              <button class="btn btn-sm btn-outline" @click="openEditHouse(house.id)">编辑</button>
              <button class="btn btn-sm btn-danger" @click="handleDeleteHouse(house.id)">删除</button>
              <button class="btn btn-sm" :class="house.status === 'NORMAL' ? 'btn-offline' : 'btn-online'"
                      @click="handleToggleStatus(house)">
                {{ house.status === 'NORMAL' ? '下架' : '上架' }}
              </button>
            </template>
          </HouseCard>
        </div>
      </div>

      <!-- Appointment Approval -->
      <div v-if="activeTab === 'appointments'" class="tab-content">
        <div class="section-header" v-reveal>
          <div>
            <p class="kicker">APPROVAL QUEUE</p>
            <h3>预约审批</h3>
            <p class="section-sub">状态机流转全程留痕，点击“轨迹”查看完整时间线</p>
          </div>
          <div class="approval-summary">
            <span><i class="sum-dot pending"></i>{{ pendingCount }} 待审批</span>
            <span><i class="sum-dot approved"></i>{{ approvedCount }} 已批准</span>
          </div>
        </div>
        <div class="appointment-shell" v-reveal>
          <AppointmentTable
              :appointments="aptStore.appointments"
              :isLandlord="true"
              @approve="handleApprove"
              @reject="handleReject"
              @complete="handleComplete"
              @delete="handleDeleteApt"
              @flow="openFlow"
          />
        </div>
      </div>

      <!-- Notification Center -->
      <div v-if="activeTab === 'notifications'" class="tab-content">
        <NotificationCenter />
      </div>
    </div>

    <!-- Add/Edit House Modal -->
    <AppModal :visible="showAddModal || showEditModal" :title="showEditModal ? '编辑房源' : '发布新房源'" width="720px"
              @close="closeHouseModal">
      <HouseForm ref="houseFormRef" :initial="editingHouse" :submitLabel="showEditModal ? '保存修改' : '发布房源'" @submit="handleHouseSubmit" @cancel="closeHouseModal" />
    </AppModal>

    <!-- Profile Modal -->
    <AppModal :visible="showProfile" title="个人信息" @close="showProfile = false">
      <div class="form-group"><label>用户名</label><input :value="user?.username" disabled /></div>
      <div class="form-group"><label>角色</label><input :value="roleLabelComputed" disabled/></div>
      <div class="form-group"><label>旧密码</label><input v-model="passwordForm.oldPassword" type="password" /></div>
      <div class="form-group"><label>新密码</label><input v-model="passwordForm.newPassword" type="password" placeholder="留空则不修改" /></div>
      <div class="form-group"><label>确认新密码</label><input v-model="passwordForm.confirmNewPassword" type="password" /></div>
      <button class="btn btn-block" @click="handleChangePassword">保存修改</button>
    </AppModal>

    <AppointmentFlow
        :visible="showFlow"
        :appointment="flowAppointment"
        :flows="flowRecords"
        @close="showFlow = false"
    />

    <AppAlert :visible="!!alertMsg" :message="alertMsg" :type="alertType" @close="alertMsg = ''" />
    <AppConfirm
        :visible="confirmState.visible"
        :title="confirmState.title"
        :message="confirmState.message"
        @confirm="confirmAction"
        @cancel="confirmState.visible = false"
    />
  </div>
</template>

<script setup>
import {computed, onMounted, onUnmounted, reactive, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {useHouseStore} from '../stores/houses'
import {useAppointmentStore} from '../stores/appointments'
import {changePassword} from '../api/users'
import {useWebSocket} from '../composables/useWebSocket'
import {roleLabel, useAuth} from '../composables/useAuth'
import {useAlert} from '../composables/useAlert'
import {getMyLandlordApplication} from '../api/landlordApplications'
import AppHeader from '../components/AppHeader.vue'
import HouseCard from '../components/HouseCard.vue'
import HouseForm from '../components/HouseForm.vue'
import AppointmentTable from '../components/AppointmentTable.vue'
import AppointmentFlow from '../components/AppointmentFlow.vue'
import NotificationCenter from '../components/NotificationCenter.vue'
import AppModal from '../components/AppModal.vue'
import AppAlert from '../components/AppAlert.vue'
import AppConfirm from '../components/AppConfirm.vue'

const router = useRouter()
const houseStore = useHouseStore()
const aptStore = useAppointmentStore()

const {loadUser, handleLogout: doLogout} = useAuth()
let user = loadUser()
if (!user) user = {}

const activeTab = ref('dashboard')
const showAddModal = ref(false)
const showEditModal = ref(false)
const showProfile = ref(false)
const editingHouse = ref({})
const houseFormRef = ref(null)
const {alertMsg, alertType, showAlert} = useAlert()
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmNewPassword: '' })
const pendingCount = ref(0)
const confirmState = reactive({visible: false, title: '', message: '', handler: null})
const notifications = ref([])
const showFlow = ref(false)
const flowAppointment = ref(null)
const flowRecords = ref([])
const applicationStatus = ref(null)

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

function pushNotification(title, type = 'info') {
  const time = new Date().toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
  notifications.value.unshift({id: `${Date.now()}-${Math.random().toString(36).slice(2, 6)}`, title, time, type})
}

let pollTimer = null

const roleLabelComputed = computed(() => roleLabel(user.role))
const approvedCount = computed(() => aptStore.appointments.filter(a => a.status === 'approved').length)
const canPublish = computed(() => applicationStatus.value === 'approved')
const pipelineStages = computed(() => [
  {key: 'PUBLISH', label: '发布', count: houseStore.houses.length, sub: '在线房源'},
  {key: 'BOOK', label: '预约', count: aptStore.appointments.length, sub: '累计预约'},
  {key: 'APPROVE', label: '审批', count: pendingCount, sub: '待处理'},
  {key: 'NOTIFY', label: '通知', count: notifications.length, sub: '实时消息'}
])

const { notification, connect, disconnect } = useWebSocket()

async function loadHouses() { await houseStore.fetchLandlordHouses(user.id) }
async function loadAppointments() { await aptStore.fetchAppointments() }
async function loadPendingCount() {
  try { await aptStore.fetchAppointments('pending'); pendingCount.value = aptStore.appointments.length } catch (e) { /* ignore */ }
}

async function loadApplicationStatus() {
  try {
    const res = await getMyLandlordApplication()
    applicationStatus.value = res.data?.data?.application?.status || null
  } catch (e) {
    applicationStatus.value = null
  }
}

function openAddHouse() { editingHouse.value = {}; showAddModal.value = true }
async function openEditHouse(id) {
  const res = await houseStore.fetchHouseById(id)
  if (res.success) { editingHouse.value = res.data.house; showEditModal.value = true }
}
function closeHouseModal() { showAddModal.value = false; showEditModal.value = false }

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

async function handleHouseSubmit(data) {
  let imageUrl = data.image || ''
  if (data.imageFile) {
    try {
      const uploadRes = await houseStore.uploadHouseImage(data.imageFile)
      imageUrl = typeof uploadRes === 'string' ? uploadRes : (uploadRes?.url || uploadRes?.data?.url || '')
    } catch (e) { showAlert('图片上传失败', 'error'); return }
  }
  const tags = (data.tags || '').split(',').map(s => s.trim()).filter(Boolean)
  const payload = {
    title: data.title,
    type: data.type,
    district: data.district,
    bedrooms: Number(data.bedrooms) || 1,
    bathrooms: Number(data.bathrooms) || 1,
    area: data.area,
    price: data.price,
    orientation: data.orientation,
    floor: data.floor,
    decoration: data.decoration,
    leaseTerm: data.leaseTerm,
    tags: JSON.stringify(tags),
    address: data.address,
    description: data.description,
    image: imageUrl
  }
  if (showEditModal.value) {
    await houseStore.editHouse(editingHouse.value.id, payload)
    showAlert('房源已更新')
  } else {
    await houseStore.addHouse(payload)
    showAlert('房源发布成功')
    pushNotification('房源已发布，等待租客预约', 'success')
  }
  closeHouseModal()
  await loadHouses()
}

async function handleDeleteHouse(id) {
  askConfirm('删除房源', '删除后房源将不再展示，确认继续吗？', async () => {
    await houseStore.removeHouse(id)
    showAlert('房源已删除')
    await loadHouses()
  })
}

async function handleToggleStatus(house) {
  const status = house.status === 'NORMAL' ? 'OFFLINE' : 'NORMAL'
  await houseStore.editHouse(house.id, {status})
  showAlert(status === 'NORMAL' ? '房源已上架' : '房源已下架')
  await loadHouses()
}

async function handleApprove(id) {
  await aptStore.approve(id)
  showAlert('预约已批准')
  pushNotification('已批准预约 #' + id + '，通知已送达租客', 'success')
  await loadAppointments();
  await loadPendingCount()
}

async function handleReject(id) {
  await aptStore.reject(id)
  showAlert('预约已拒绝')
  pushNotification('已拒绝预约 #' + id + '，通知已送达租客', 'warning')
  await loadAppointments();
  await loadPendingCount()
}

async function handleComplete(id) {
  await aptStore.complete(id)
  showAlert('预约已完成')
  pushNotification('预约 #' + id + ' 看房完成，闭环结束', 'success')
  await loadAppointments();
  await loadPendingCount()
}
async function handleDeleteApt(id) {
  askConfirm('删除记录', '删除后无法恢复，确认继续吗？', async () => {
    await aptStore.remove(id)
    showAlert('记录已删除')
    await loadAppointments()
    await loadPendingCount()
  })
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
  await changePassword(user.id, passwordForm.oldPassword, passwordForm.newPassword)
  showAlert('密码修改成功')
  showProfile.value = false
  passwordForm.newPassword = ''; passwordForm.confirmNewPassword = ''; passwordForm.oldPassword = ''
}

function handleLogout() {
  doLogout(() => {
    disconnect();
    if (pollTimer) clearInterval(pollTimer)
  })
}

watch(notification, (n) => {
  if (n) {
    showAlert(`新预约通知: ${n.message || ''}`)
    pushNotification(`预约 #${n.appointmentId || ''} ${n.message || ''}`, n.status === 'approved' ? 'success' : n.status === 'rejected' ? 'warning' : 'info')
    loadAppointments();
    loadPendingCount()
  }
})

onMounted(async () => {
  await loadHouses(); await loadAppointments(); await loadPendingCount()
  await loadApplicationStatus()
  if (pendingCount.value > 0) pushNotification(`有 ${pendingCount.value} 条预约待审批`, 'warning')
  else pushNotification('审批工作台已就绪', 'success')
  connect()
  pollTimer = setInterval(loadPendingCount, 30000)
})
onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })
</script>

<style scoped>
.landlord-page {
  min-height: 100vh;
  background: radial-gradient(900px 460px at 88% -4%, rgba(139, 92, 246, 0.1), transparent 58%),
  radial-gradient(900px 460px at 2% 4%, rgba(6, 182, 212, 0.1), transparent 55%),
  #f4f7fc;
}

.page-shell {
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 24px 64px;
}

.tab-content {
  animation: fadeIn 0.3s ease;
}

.kicker {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 3px;
  color: #06b6d4;
  margin-bottom: 6px;
}

/* Hero */
.workbench-hero {
  position: relative;
  border-radius: 24px;
  padding: 40px 42px 30px;
  color: #fff;
  overflow: hidden;
  background: linear-gradient(120deg, rgba(11, 31, 63, 0.92), rgba(91, 33, 182, 0.82)),
  url('/backgrounds/landlord-hero.png') center / cover no-repeat;
  box-shadow: 0 28px 70px rgba(11, 31, 63, 0.28);
}

.hero-glow {
  position: absolute;
  inset: -40%;
  background: conic-gradient(from 210deg at 50% 50%, rgba(103, 232, 249, 0.16), transparent 28%, rgba(236, 72, 153, 0.14) 52%, transparent 78%);
  animation: aurora-drift 16s ease-in-out infinite alternate;
}

.hero-copy,
.hero-actions,
.hero-stats {
  position: relative;
  z-index: 1;
}

.hero-copy .kicker {
  color: #67e8f9;
}

.hero-copy h1 {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: 0;
  margin-bottom: 8px;
}

.hero-copy p:last-child {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.72);
}

.review-banner {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 18px;
  padding: 9px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.32);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
}

.hero-actions .btn-outline {
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.34);
  box-shadow: none;
}

.hero-actions .btn-outline:hover {
  background: rgba(255, 255, 255, 0.24);
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-top: 34px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.16);
}

.hero-stat strong {
  display: block;
  font-size: 30px;
  font-weight: 800;
}

.hero-stat span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.68);
}

/* Pipeline */
.pipeline-section {
  margin-top: 26px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  padding: 26px 28px;
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 44px rgba(15, 23, 42, 0.08);
}

.pipeline-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.pipeline-head h3 {
  font-size: 20px;
  font-weight: 800;
  color: var(--text);
}

.engine-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 700;
  color: #059669;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  padding: 6px 14px;
  border-radius: 999px;
}

.engine-status i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  animation: pulse-dot 1.8s ease-in-out infinite;
}

.pipeline {
  display: flex;
  align-items: stretch;
}

.pipeline-stage {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 16px;
  padding: 16px;
  transition: all var(--transition);
}

.pipeline-stage:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.1);
}

.pipeline-stage.hot {
  border-color: rgba(244, 63, 94, 0.4);
  background: linear-gradient(135deg, #fff1f2, #fdf2f8);
}

.pipeline-icon {
  width: 44px;
  height: 44px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  flex-shrink: 0;
  box-shadow: 0 8px 18px rgba(22, 119, 255, 0.24);
}

.pipeline-stage.hot .pipeline-icon {
  background: linear-gradient(135deg, #f43f5e, #ff6b35);
  box-shadow: 0 8px 18px rgba(244, 63, 94, 0.26);
}

.pipeline-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.pipeline-copy span {
  font-size: 13px;
  color: #6b7280;
}

.pipeline-copy strong {
  font-size: 26px;
  font-weight: 800;
  color: var(--text);
  line-height: 1.2;
}

.pipeline-copy small {
  font-size: 11px;
  color: #9ca3af;
}

.pipeline-connector {
  position: absolute;
  top: 50%;
  right: -14px;
  width: 28px;
  height: 2px;
  background: linear-gradient(90deg, #1677ff, #06b6d4, #8b5cf6);
  transform: translateY(-50%);
  z-index: 2;
}

/* Dashboard grid */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 24px;
  margin-top: 26px;
}

.panel {
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  padding: 24px;
  backdrop-filter: blur(14px);
  box-shadow: 0 16px 44px rgba(15, 23, 42, 0.08);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.panel-head h3 {
  font-size: 17px;
  font-weight: 800;
  color: var(--text);
}

.live-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #059669;
  font-weight: 700;
}

.live-dot i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  animation: pulse-dot 1.8s ease-in-out infinite;
}

.notif-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 320px;
  overflow-y: auto;
}

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 13px;
  padding: 12px 14px;
}

.notif-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 6px;
  background: #1677ff;
  flex-shrink: 0;
}

.notif-success .notif-dot {
  background: #10b981;
}

.notif-warning .notif-dot {
  background: #f59e0b;
}

.notif-item p {
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
}

.notif-item small {
  font-size: 11px;
  color: #9ca3af;
}

.panel-empty {
  text-align: center;
  color: #9ca3af;
  padding: 36px 0;
  font-size: 13px;
}

.quick-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.quick-action {
  display: flex;
  align-items: center;
  gap: 14px;
  text-align: left;
  padding: 14px 16px;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  background: #f8fafc;
  cursor: pointer;
  transition: all var(--transition);
}

.quick-action:hover {
  transform: translateX(4px);
  border-color: #06b6d4;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.1);
}

.qa-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.qa-blue {
  background: linear-gradient(135deg, #1677ff, #06b6d4);
}

.qa-pink {
  background: linear-gradient(135deg, #f43f5e, #ff6b35);
}

.qa-violet {
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
}

.quick-action strong {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: var(--text);
}

.quick-action small {
  font-size: 12px;
  color: #9ca3af;
}

/* Section header */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
  margin-bottom: 24px;
}

.section-header h3 {
  font-size: 24px;
  font-weight: 800;
  color: var(--text);
}

.section-sub {
  color: #6b7280;
  font-size: 13px;
  margin-top: 4px;
}

.approval-summary {
  display: flex;
  gap: 14px;
}

.approval-summary span {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  font-weight: 700;
  color: #4b5563;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid #eef2f7;
  padding: 7px 14px;
  border-radius: 999px;
}

.sum-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.sum-dot.pending {
  background: #f59e0b;
}

.sum-dot.approved {
  background: #10b981;
}

.appointment-shell {
  animation: slideUp 0.5s var(--ease-spring) both;
}

/* House grid */
.house-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 22px;
}

/* Badge */
.badge {
  background: linear-gradient(135deg, #f43f5e, #ff6b35);
  color: #fff;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  min-width: 18px;
  text-align: center;
  line-height: 18px;
  display: inline-block;
  margin-left: 3px;
}

.btn-offline {
  background: linear-gradient(135deg, #f59e0b, #f97316);
  color: #fff;
  box-shadow: 0 6px 16px rgba(245, 158, 11, 0.24);
}

.btn-online {
  background: linear-gradient(135deg, #16a34a, #22c55e);
  color: #fff;
  box-shadow: 0 6px 16px rgba(34, 197, 94, 0.24);
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--text-muted);
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.08);
}

.empty-icon {
  display: block;
  margin-bottom: 16px;
  color: #94a3b8;
}

.empty-state p {
  font-size: 15px;
  margin-bottom: 20px;
}

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

@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.55;
    transform: scale(0.82);
  }
}

@media (max-width: 900px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .pipeline {
    flex-direction: column;
    gap: 12px;
  }

  .pipeline-connector {
    display: none;
  }
}

@media (max-width: 768px) {
  .page-shell {
    padding: 16px;
  }
  .house-grid { grid-template-columns: 1fr; }

  .workbench-hero {
    padding: 28px 22px 24px;
  }

  .hero-copy h1 {
    font-size: 27px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .hero-actions .btn {
    width: 100%;
  }

  .hero-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .approval-summary {
    flex-wrap: wrap;
  }
}
</style>
