<template>
  <span class="status-badge" :class="'status-' + status">{{ label }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ status: String })
const map = { pending: '待处理', approved: '已批准', rejected: '已拒绝', completed: '已完成', canceled: '已取消', cancelled: '已取消', paid: '已支付' }
const label = computed(() => map[props.status] || props.status)
</script>

<style scoped>
.status-badge { display:inline-block; padding:4px 12px; border-radius:20px; font-size:12px; font-weight:700; text-transform:uppercase; }
.status-pending { background:#ffc107; color:#212529; }
.status-approved { background:#28a745; color:#fff; }
.status-rejected { background:#dc3545; color:#fff; }
.status-completed { background:#17a2b8; color:#fff; }
.status-canceled, .status-cancelled { background:#6c757d; color:#fff; }
.status-paid { background:#28a745; color:#fff; }
.status-normal { background:#28a745; color:#fff; }
.status-disabled { background:#dc3545; color:#fff; }
</style>
