<template>
  <div class="notification-center">
    <div class="nc-head">
      <div>
        <p class="nc-kicker">NOTIFICATION CENTER</p>
        <h3>通知中心</h3>
        <p class="nc-sub">预约、审核、实名认证等业务通知的完整记录</p>
      </div>
      <div class="nc-actions">
        <button class="btn btn-sm btn-outline" @click="markAll" :disabled="!unreadCount">全部已读</button>
        <button class="btn btn-sm btn-outline" @click="load" :disabled="loading">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 12a9 9 0 1 1-2.64-6.36"/>
            <polyline points="21 3 21 9 15 9"/>
          </svg>
          刷新
        </button>
      </div>
    </div>

    <div class="nc-unread-bar" v-if="unreadCount">
      <span>{{ unreadCount }} 条未读通知</span>
    </div>

    <div class="nc-list" v-if="items.length">
      <button
          class="nc-item"
          v-for="n in items"
          :key="n.id"
          :class="['nc-' + tone(n.type), { unread: !n.readStatus }]"
          @click="handleClick(n)"
      >
        <span class="nc-icon">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
        </span>
        <div class="nc-body">
          <div class="nc-title">
            <strong>{{ n.title || eventLabel(n.type) }}</strong>
            <span>{{ formatTime(n.createTime) }}</span>
          </div>
          <p>{{ n.content || '系统通知' }}</p>
        </div>
        <span v-if="!n.readStatus" class="nc-dot" title="未读"></span>
        <span v-else class="nc-status">已读</span>
      </button>
    </div>

    <div class="nc-empty" v-else-if="!loading">
      暂无通知，新的预约和审批结果会实时出现在这里
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {getNotifications, markAllNotificationsRead, markNotificationRead} from '../api/notifications'

const emit = defineEmits(['open-related'])

const items = ref([])
const unreadCount = ref(0)
const loading = ref(false)

const eventMap = {
  APPOINTMENT_CREATED: '新预约申请',
  APPOINTMENT_APPROVED: '预约已批准',
  APPOINTMENT_REJECTED: '预约已拒绝',
  APPOINTMENT_CANCELED: '预约已取消',
  APPOINTMENT_COMPLETED: '看房已完成',
  APPOINTMENT_EXPIRED: '预约已过期',
  LANDLORD_APPROVED: '房东入驻审核通过',
  LANDLORD_REJECTED: '房东入驻审核未通过',
  IDENTITY_APPROVED: '实名认证已通过',
  IDENTITY_REJECTED: '实名认证未通过',
  HOUSE_APPROVED: '房源审核通过',
  HOUSE_REJECTED: '房源审核未通过'
}

function eventLabel(type) {
  return eventMap[type] || type || '系统通知'
}

function tone(type) {
  if (['APPOINTMENT_APPROVED', 'APPOINTMENT_COMPLETED', 'LANDLORD_APPROVED', 'IDENTITY_APPROVED', 'HOUSE_APPROVED'].includes(type)) return 'success'
  if (['APPOINTMENT_REJECTED', 'LANDLORD_REJECTED', 'IDENTITY_REJECTED', 'HOUSE_REJECTED'].includes(type)) return 'danger'
  if (['APPOINTMENT_CANCELED', 'APPOINTMENT_EXPIRED'].includes(type)) return 'warn'
  return 'info'
}

function formatTime(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 16)
  return date.toLocaleString('zh-CN', {month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'})
}

async function load() {
  loading.value = true
  try {
    const res = await getNotifications(100)
    items.value = res.data?.data?.notifications || []
    unreadCount.value = Number(res.data?.data?.unread) || 0
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
}

async function handleClick(n) {
  if (!n.readStatus) {
    try {
      await markNotificationRead(n.id)
      n.readStatus = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch {
      /* 静默 */
    }
  }
  // 点击通知跳转对应业务（由父视图决定具体 Tab）
  emit('open-related', n)
}

async function markAll() {
  try {
    await markAllNotificationsRead()
    items.value = items.value.map(n => ({...n, readStatus: 1}))
    unreadCount.value = 0
  } catch {
    /* 静默 */
  }
}

onMounted(load)
</script>

<style scoped>
.notification-center {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 16px 44px rgba(15, 23, 42, 0.1);
}

.nc-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.nc-actions {
  display: flex;
  gap: 8px;
}

.nc-kicker {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #1677ff, #06b6d4, #8b5cf6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  margin-bottom: 6px;
}

.nc-head h3 {
  font-size: 22px;
  font-weight: 800;
  color: var(--text);
}

.nc-sub {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

.nc-unread-bar {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  color: #c2410c;
  border-radius: 10px;
  padding: 8px 14px;
  font-size: 13px;
  margin-bottom: 12px;
}

.nc-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.nc-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 14px 16px;
  transition: all var(--transition);
  cursor: pointer;
  width: 100%;
  text-align: left;
}

.nc-item:hover {
  transform: translateX(3px);
  border-color: #c7d2fe;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.nc-item.unread {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.nc-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  flex-shrink: 0;
}

.nc-success .nc-icon { background: linear-gradient(135deg, #16a34a, #22c55e); }
.nc-danger .nc-icon { background: linear-gradient(135deg, #dc2626, #f43f5e); }
.nc-warn .nc-icon { background: linear-gradient(135deg, #f59e0b, #f97316); }

.nc-body {
  flex: 1;
  min-width: 0;
}

.nc-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 4px;
}

.nc-title strong {
  font-size: 14px;
  font-weight: 700;
  color: var(--text);
}

.nc-title span {
  font-size: 12px;
  color: #9ca3af;
}

.nc-body p {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.55;
}

.nc-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ef4444;
  flex-shrink: 0;
  margin-top: 6px;
}

.nc-status {
  font-size: 11px;
  font-weight: 700;
  color: #94a3b8;
  flex-shrink: 0;
}

.nc-empty {
  text-align: center;
  color: #9ca3af;
  padding: 60px 20px;
}

@media (max-width: 640px) {
  .nc-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }

  .nc-status {
    display: none;
  }
}
</style>
