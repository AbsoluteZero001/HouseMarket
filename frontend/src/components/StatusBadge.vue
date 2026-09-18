<template>
  <span class="status-badge" :class="'status-' + status">{{ label }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ status: String })
const map = {
  pending: '待处理', approved: '已批准', rejected: '已拒绝',
  completed: '已完成', canceled: '已取消', cancelled: '已取消',
  paid: '已支付', normal: '正常', disabled: '已禁用'
}
const label = computed(() => map[props.status] || props.status)
</script>

<style scoped>
.status-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}
.status-pending { background: #fff7e6; color: #d46b08; }
.status-approved { background: #f6ffed; color: #389e0d; }
.status-rejected { background: #fff2f0; color: #cf1322; }
.status-completed { background: #e6f4ff; color: #0958d9; }
.status-canceled, .status-cancelled { background: #f5f5f5; color: #666; }
.status-paid { background: #f6ffed; color: #389e0d; }
.status-normal { background: #f6ffed; color: #389e0d; }
.status-disabled { background: #fff2f0; color: #cf1322; }
</style>
