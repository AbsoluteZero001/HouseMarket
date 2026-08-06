<template>
  <Teleport to="body">
    <transition name="flow-fade">
      <div class="flow-mask" v-if="visible" @click.self="$emit('close')">
        <div class="flow-dialog">
          <div class="flow-hero">
            <div class="flow-hero-glow"></div>
            <button class="flow-close" @click="$emit('close')">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
            <p class="flow-kicker">APPROVAL FLOW ENGINE</p>
            <h2>预约审批流程</h2>
            <p class="flow-house">{{ appointment?.house?.title || `预约 #${appointment?.id || ''}` }}</p>
            <div class="flow-hero-meta">
              <StatusBadge :status="appointment?.status"/>
              <span>{{ appointment?.time || '' }}</span>
              <span>{{ appointment?.location || '' }}</span>
            </div>
          </div>

          <div class="flow-body">
            <div class="stage-rail">
              <div class="stage" v-for="(stage, idx) in stages" :key="stage.key"
                   :class="{ done: stageDone(stage.key) }">
                <div class="stage-node">
                  <span v-if="stageDone(stage.key)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
                         stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                  </span>
                  <span v-else>{{ idx + 1 }}</span>
                </div>
                <div class="stage-copy">
                  <strong>{{ stage.label }}</strong>
                  <small>{{ stage.sub }}</small>
                </div>
                <div class="stage-line" v-if="idx < stages.length - 1"></div>
              </div>
            </div>

            <div class="timeline">
              <div class="timeline-head">
                <h3>全流程轨迹</h3>
                <span>{{ flows.length }} 条记录 · 可追溯</span>
              </div>
              <div class="timeline-list">
                <div class="timeline-empty" v-if="flows.length === 0">暂无流程记录</div>
                <div class="timeline-item" v-for="f in flows" :key="f.id">
                  <div class="timeline-dot" :class="'dot-' + (f.action || '').toLowerCase()"></div>
                  <div class="timeline-content">
                    <div class="timeline-title">
                      <strong>{{ actionLabel(f.action) }}</strong>
                      <span>{{ f.createTime }}</span>
                    </div>
                    <p>{{ f.remark || '流程节点' }}</p>
                    <div class="timeline-tags">
                      <span v-if="f.operatorRole">{{ roleLabel(f.operatorRole) }}</span>
                      <span>{{ stateLabel(f.toStatus) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import {computed} from 'vue'
import StatusBadge from './StatusBadge.vue'

const props = defineProps({
  visible: Boolean,
  appointment: Object,
  flows: {type: Array, default: () => []}
})
defineEmits(['close'])

const stages = [
  {key: 'PUBLISH', label: '发布', sub: '房源上线'},
  {key: 'BOOK', label: '预约', sub: '租客申请'},
  {key: 'APPROVE', label: '审批', sub: '房东处理'},
  {key: 'NOTIFY', label: '通知', sub: '结果送达'}
]

function stageDone(key) {
  return props.flows.some(f => f.action === key)
}

const actionMap = {
  PUBLISH: '房源发布',
  BOOK: '租客预约',
  APPROVE: '房东审批',
  REJECT: '审批拒绝',
  CANCEL: '预约取消',
  COMPLETE: '看房完成',
  NOTIFY: '实时通知'
}

function actionLabel(action) {
  return actionMap[action] || action || '流程节点'
}

const stateMap = {
  published: '已发布',
  pending: '待处理',
  approved: '已批准',
  rejected: '已拒绝',
  canceled: '已取消',
  completed: '已完成'
}

function stateLabel(state) {
  return stateMap[state] || state || '初始'
}

function roleLabel(role) {
  return role === 'LANDLORD' ? '房东' : role === 'TENANT' ? '租客' : role === 'ADMIN' ? '管理员' : role
}
</script>

<style scoped>
.flow-mask {
  position: fixed;
  inset: 0;
  z-index: 1200;
  background: rgba(7, 20, 40, 0.55);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.flow-dialog {
  width: 100%;
  max-width: 720px;
  max-height: 86vh;
  background: #fff;
  border-radius: 24px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 40px 100px rgba(7, 20, 40, 0.4);
  animation: flow-in 0.42s var(--ease-spring);
}

.flow-hero {
  position: relative;
  padding: 30px 32px 26px;
  background: linear-gradient(120deg, #0b1f3f 0%, #123b75 48%, #5b21b6 100%);
  color: #fff;
  overflow: hidden;
}

.flow-hero-glow {
  position: absolute;
  inset: -40%;
  background: conic-gradient(from 210deg at 50% 50%, rgba(103, 232, 249, 0.16), transparent 30%, rgba(236, 72, 153, 0.14) 55%, transparent 80%);
  animation: aurora-drift 14s ease-in-out infinite alternate;
}

.flow-close {
  position: absolute;
  top: 18px;
  right: 18px;
  width: 36px;
  height: 36px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
}

.flow-close:hover {
  background: rgba(255, 255, 255, 0.24);
  transform: rotate(90deg);
}

.flow-kicker {
  position: relative;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 3px;
  color: #67e8f9;
  margin-bottom: 8px;
}

.flow-hero h2 {
  position: relative;
  font-size: 26px;
  font-weight: 800;
  margin-bottom: 6px;
  letter-spacing: 0;
}

.flow-house {
  position: relative;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.72);
}

.flow-hero-meta {
  position: relative;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 18px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.82);
}

.flow-body {
  padding: 26px 32px 30px;
  overflow-y: auto;
}

.stage-rail {
  display: flex;
  align-items: flex-start;
  margin-bottom: 28px;
}

.stage {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  position: relative;
}

.stage-node {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  border-radius: 12px;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 800;
  border: 1px solid #e2e8f0;
  transition: all var(--transition);
}

.stage.done .stage-node {
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 8px 18px rgba(22, 119, 255, 0.26);
}

.stage-copy strong {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: var(--text);
}

.stage-copy small {
  font-size: 12px;
  color: #9ca3af;
}

.stage-line {
  position: absolute;
  top: 17px;
  left: 44px;
  right: 10px;
  height: 2px;
  background: #e2e8f0;
  border-radius: 2px;
}

.stage.done .stage-line {
  background: linear-gradient(90deg, #1677ff, #06b6d4);
}

.timeline {
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 16px;
  padding: 20px;
}

.timeline-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.timeline-head h3 {
  font-size: 16px;
  font-weight: 800;
  color: var(--text);
}

.timeline-head span {
  font-size: 12px;
  color: #9ca3af;
}

.timeline-list {
  position: relative;
}

.timeline-list::before {
  content: "";
  position: absolute;
  left: 7px;
  top: 6px;
  bottom: 6px;
  width: 2px;
  background: linear-gradient(180deg, #1677ff, #06b6d4, #8b5cf6, #ec4899);
  border-radius: 2px;
}

.timeline-empty {
  text-align: center;
  color: #9ca3af;
  padding: 28px 0;
}

.timeline-item {
  position: relative;
  display: flex;
  gap: 18px;
  padding-bottom: 20px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: relative;
  z-index: 1;
  width: 16px;
  height: 16px;
  margin-top: 4px;
  border-radius: 50%;
  background: #1677ff;
  box-shadow: 0 0 0 4px rgba(22, 119, 255, 0.14);
  flex-shrink: 0;
}

.dot-approve {
  background: #059669;
  box-shadow: 0 0 0 4px rgba(5, 150, 105, 0.14);
}

.dot-reject {
  background: #e11d48;
  box-shadow: 0 0 0 4px rgba(225, 29, 72, 0.14);
}

.dot-notify {
  background: #8b5cf6;
  box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.14);
}

.dot-book,
.dot-publish {
  background: #06b6d4;
  box-shadow: 0 0 0 4px rgba(6, 182, 212, 0.14);
}

.dot-cancel,
.dot-complete {
  background: #f59e0b;
  box-shadow: 0 0 0 4px rgba(245, 158, 11, 0.14);
}

.timeline-content {
  flex: 1;
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 12px 14px;
}

.timeline-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 4px;
}

.timeline-title strong {
  font-size: 14px;
  font-weight: 700;
  color: var(--text);
}

.timeline-title span {
  font-size: 12px;
  color: #9ca3af;
}

.timeline-content p {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.55;
}

.timeline-tags {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.timeline-tags span {
  font-size: 11px;
  font-weight: 600;
  color: #4b5563;
  background: #f1f5f9;
  padding: 3px 9px;
  border-radius: 999px;
}

@keyframes flow-in {
  from {
    opacity: 0;
    transform: translateY(28px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
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

.flow-fade-enter-active,
.flow-fade-leave-active {
  transition: opacity 0.25s ease;
}

.flow-fade-enter-from,
.flow-fade-leave-to {
  opacity: 0;
}

@media (max-width: 640px) {
  .flow-mask {
    padding: 12px;
  }

  .flow-dialog {
    max-height: 92vh;
    border-radius: 18px;
  }

  .flow-hero {
    padding: 24px 20px 20px;
  }

  .flow-body {
    padding: 20px;
  }

  .stage-rail {
    flex-direction: column;
    gap: 14px;
  }

  .stage-line {
    display: none;
  }
}
</style>
