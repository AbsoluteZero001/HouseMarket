<template>
  <div class="table-wrap">
    <div class="table-scroll">
      <table class="table">
        <thead>
        <tr>
          <th>订单</th>
          <th>{{ isLandlord ? '租客' : '房东' }}</th>
          <th>房源</th>
          <th>预约时间</th>
          <th>地点</th>
          <th>状态</th>
          <th>流程</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="appointments.length === 0">
          <td colspan="8" class="empty-cell">暂无预约记录</td>
        </tr>
        <tr v-for="apt in appointments" :key="apt.id">
          <td><span class="id-tag">#{{ apt.id }}</span></td>
          <td>
            <div class="person-cell">
              <span class="person-avatar">{{
                  (isLandlord ? (apt.tenant?.realName || apt.tenant?.username || '客') : (apt.landlord?.realName || apt.landlord?.username || '东')).slice(0, 1)
                }}</span>
              <span>{{
                  isLandlord ? (apt.tenant?.realName || apt.tenant?.username) : (apt.landlord?.realName || apt.landlord?.username)
                }}</span>
            </div>
          </td>
          <td>{{ apt.house?.title || '-' }}</td>
          <td>{{ apt.time }}</td>
          <td>{{ apt.location || '-' }}</td>
          <td>
            <StatusBadge :status="apt.status"/>
          </td>
          <td>
            <button class="btn-flow" @click="$emit('flow', apt)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <line x1="4" y1="6" x2="20" y2="6"/>
                <line x1="4" y1="12" x2="20" y2="12"/>
                <line x1="4" y1="18" x2="14" y2="18"/>
              </svg>
              轨迹
            </button>
          </td>
          <td>
            <div class="action-btns">
              <template v-if="isLandlord && apt.status === 'pending'">
                <button class="btn btn-sm btn-success" @click="$emit('approve', apt.id)">批准</button>
                <button class="btn btn-sm btn-danger" @click="$emit('reject', apt.id)">拒绝</button>
              </template>
              <template v-if="isLandlord && apt.status === 'approved'">
                <button class="btn btn-sm btn-success" @click="$emit('complete', apt.id)">完成</button>
              </template>
              <template v-if="!isLandlord && (apt.status === 'pending' || apt.status === 'approved')">
                <button class="btn btn-sm btn-warning" @click="$emit('cancel', apt.id)">取消</button>
              </template>
              <template v-if="apt.status === 'rejected' || apt.status === 'completed' || apt.status === 'canceled'">
                <button class="btn btn-sm btn-outline btn-danger-outline" @click="$emit('delete', apt.id)">删除</button>
              </template>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import StatusBadge from './StatusBadge.vue'

defineProps({ appointments: Array, isLandlord: Boolean })
defineEmits(['approve', 'reject', 'complete', 'cancel', 'delete', 'flow'])
</script>

<style scoped>
.table-wrap {
  background: var(--bg-white);
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.1);
  border: 1px solid #eef2f7;
}

.table-scroll {
  overflow-x: auto;
}
.table {
  width: 100%;
  border-collapse: collapse;
  min-width: 900px;
}
.table th {
  background: #f8fafc;
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
  color: #1677ff;
  background: #eff6ff;
  padding: 4px 10px;
  border-radius: 8px;
  font-weight: 700;
}

.person-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.person-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.btn-flow {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 11px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  color: #4b5563;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition);
}

.btn-flow:hover {
  border-color: #8b5cf6;
  color: #8b5cf6;
  background: #f5f3ff;
  transform: translateY(-1px);
}
.action-btns { display: flex; gap: 6px; }

.btn-warning {
  background: linear-gradient(135deg, #f59e0b, #f97316);
  color: #fff;
  box-shadow: 0 6px 16px rgba(245, 158, 11, 0.26);
}

.btn-danger-outline {
  border-color: #fda4af;
  color: #e11d48;
}

.btn-danger-outline:hover {
  border-color: #e11d48;
  color: #fff;
  background: linear-gradient(135deg, #dc2626, #f43f5e);
}
</style>
