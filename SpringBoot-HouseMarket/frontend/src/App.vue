<template>
  <LoadingBar />
  <RouterView v-slot="{ Component }">
    <Transition name="page" mode="out-in">
      <component :is="Component"/>
    </Transition>
  </RouterView>

  <!-- 全局提示：401/403 等由 axios 拦截器统一派发 -->
  <transition name="toast">
    <div v-if="toast" class="global-toast" :class="'toast-' + toast.type">{{ toast.message }}</div>
  </transition>
</template>

<script setup>
import {onMounted, onUnmounted, ref} from 'vue'
import LoadingBar from './components/LoadingBar.vue'

const toast = ref(null)
let timer = null

function showToast(message, type = 'error') {
  clearTimeout(timer)
  toast.value = {message, type}
  timer = setTimeout(() => {
    toast.value = null
  }, 3200)
}

function onAppError(e) {
  showToast(e.detail?.message || '操作失败', 'error')
}

function onAuthExpired(e) {
  showToast(e.detail?.message || '登录状态已失效，请重新登录', 'warning')
}

onMounted(() => {
  window.addEventListener('app:error', onAppError)
  window.addEventListener('auth:expired', onAuthExpired)
})

onUnmounted(() => {
  window.removeEventListener('app:error', onAppError)
  window.removeEventListener('auth:expired', onAuthExpired)
  clearTimeout(timer)
})
</script>

<style>
.global-toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2000;
  padding: 12px 22px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.24);
  animation: toast-in 0.25s ease;
}

.toast-error {
  background: linear-gradient(135deg, #dc2626, #f43f5e);
}

.toast-warning {
  background: linear-gradient(135deg, #d97706, #f59e0b);
}

.toast-success {
  background: linear-gradient(135deg, #16a34a, #22c55e);
}

.toast-enter-active, .toast-leave-active {
  transition: all 0.25s ease;
}

.toast-enter-from, .toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-12px);
}
</style>
