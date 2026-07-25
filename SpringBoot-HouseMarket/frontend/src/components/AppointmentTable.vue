<template>
  <table class="table">
    <thead>
      <tr>
        <th>订单ID</th>
        <th>{{ isLandlord ? '租客' : '房东' }}</th>
        <th>房源</th>
        <th>预约时间</th>
        <th>预约地点</th>
        <th>状态</th>
        <th>操作</th>
      </tr>
    </thead>
    <tbody>
      <tr v-if="appointments.length === 0">
        <td colspan="7" style="text-align:center; padding:30px; color:#999;">暂无预约记录</td>
      </tr>
      <tr v-for="apt in appointments" :key="apt.id" class="appointment-row">
        <td>#{{ apt.id }}</td>
        <td>{{ isLandlord ? apt.tenant?.realName || apt.tenant?.username : apt.landlord?.realName || apt.landlord?.username }}</td>
        <td>{{ apt.house?.title || '-' }}</td>
        <td>{{ apt.time }}</td>
        <td>{{ apt.location }}</td>
        <td><StatusBadge :status="apt.status" /></td>
        <td>
          <div class="action-btns">
            <template v-if="isLandlord && apt.status === 'pending'">
              <button class="btn btn-success btn-sm" @click="$emit('approve', apt.id)">批准</button>
              <button class="btn btn-danger btn-sm" @click="$emit('reject', apt.id)">拒绝</button>
            </template>
            <template v-if="!isLandlord && (apt.status === 'pending' || apt.status === 'approved')">
              <button class="btn btn-warning btn-sm" @click="$emit('cancel', apt.id)">取消</button>
            </template>
            <template v-if="apt.status === 'rejected' || apt.status === 'completed' || apt.status === 'canceled'">
              <button class="btn btn-danger btn-sm" @click="$emit('delete', apt.id)">删除</button>
            </template>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script setup>
import StatusBadge from './StatusBadge.vue'

defineProps({ appointments: Array, isLandlord: Boolean })
defineEmits(['approve', 'reject', 'cancel', 'delete'])
</script>

<style scoped>
.table { width: 100%; border-collapse: collapse; background: rgba(255,255,255,0.95); border-radius: 8px; overflow: hidden; box-shadow: 0 2px 15px rgba(0,0,0,0.1); }
.table th { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; padding: 15px; text-align: left; text-transform: uppercase; font-size: 12px; }
.table td { padding: 12px 15px; color: #333; border-bottom: 1px solid #e0e0e0; }
.appointment-row { transition: all 0.3s; }
.appointment-row:hover { background: rgba(102,126,234,0.05); }
.action-btns { display: flex; gap: 6px; }
.btn-sm { padding: 5px 10px; font-size: 11px; }
.btn-warning { background: #ffc107; color: #212529; border: none; border-radius: 4px; cursor: pointer; }
</style>
