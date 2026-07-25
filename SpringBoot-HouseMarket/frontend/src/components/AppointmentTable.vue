<template>
  <div class="table-wrap">
    <table class="table">
      <thead>
        <tr>
          <th>订单ID</th>
          <th>{{ isLandlord ? '租客' : '房东' }}</th>
          <th>房源</th>
          <th>预约时间</th>
          <th>地点</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="appointments.length === 0">
          <td colspan="7" class="empty-cell">暂无预约记录</td>
        </tr>
        <tr v-for="apt in appointments" :key="apt.id">
          <td><span class="id-tag">#{{ apt.id }}</span></td>
          <td>{{ isLandlord ? (apt.tenant?.realName || apt.tenant?.username) : (apt.landlord?.realName || apt.landlord?.username) }}</td>
          <td>{{ apt.house?.title || '-' }}</td>
          <td>{{ apt.time }}</td>
          <td>{{ apt.location || '-' }}</td>
          <td><StatusBadge :status="apt.status" /></td>
          <td>
            <div class="action-btns">
              <template v-if="isLandlord && apt.status === 'pending'">
                <button class="btn btn-sm btn-success" @click="$emit('approve', apt.id)">批准</button>
                <button class="btn btn-sm btn-danger" @click="$emit('reject', apt.id)">拒绝</button>
              </template>
              <template v-if="!isLandlord && (apt.status === 'pending' || apt.status === 'approved')">
                <button class="btn btn-sm" style="background:#faad14;color:#fff" @click="$emit('cancel', apt.id)">取消</button>
              </template>
              <template v-if="apt.status === 'rejected' || apt.status === 'completed' || apt.status === 'canceled'">
                <button class="btn btn-sm btn-outline" style="color:var(--danger);border-color:var(--danger)" @click="$emit('delete', apt.id)">删除</button>
              </template>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import StatusBadge from './StatusBadge.vue'
defineProps({ appointments: Array, isLandlord: Boolean })
defineEmits(['approve', 'reject', 'cancel', 'delete'])
</script>

<style scoped>
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
.action-btns { display: flex; gap: 6px; }
</style>
