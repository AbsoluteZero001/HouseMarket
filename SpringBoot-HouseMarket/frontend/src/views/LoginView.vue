<template>
  <AuthLayout
      gradient="linear-gradient(135deg, #1677ff 0%, #0958d9 30%, #003eb3 70%, #002766 100%)"
      title="欢迎回来"
      subtitle="登录您的房源市场账号"
      switch-text="还没有账号？"
      switch-link="立即注册"
      switch-to="/register"
  >
    <form @submit.prevent="handleLogin" class="auth-form" novalidate>
      <div class="form-group" :class="{ 'has-error': fieldErrors.username }">
        <label>用户名</label>
        <div class="input-wrap">
          <span class="input-icon">👤</span>
          <input
              v-model="form.username"
              placeholder="请输入用户名"
              autocomplete="username"
              @input="clearField('username')"
          />
        </div>
        <p v-if="fieldErrors.username" class="field-error">{{ fieldErrors.username }}</p>
      </div>

      <div class="form-group" :class="{ 'has-error': fieldErrors.password }">
        <label>密码</label>
        <div class="input-wrap">
          <span class="input-icon">🔒</span>
          <input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              @input="clearField('password')"
          />
        </div>
        <p v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</p>
      </div>

      <div class="form-group">
        <label>选择角色</label>
        <RoleSlider v-model="form.role"/>
      </div>

      <button type="submit" class="btn btn-primary btn-block btn-lg" :disabled="loading">
        <span class="btn-spinner" v-if="loading"></span>
        <span>{{ loading ? '正在登录...' : '登 录' }}</span>
      </button>

      <transition name="fade">
        <p v-if="formError" class="form-error">{{ formError }}</p>
      </transition>
    </form>
  </AuthLayout>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useAuthStore} from '../stores/auth'
import RoleSlider from '../components/RoleSlider.vue'
import AuthLayout from '../components/AuthLayout.vue'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const formError = ref('')
const fieldErrors = reactive({username: '', password: ''})

const form = reactive({ username: '', password: '', role: 'TENANT' })

function clearField(field) {
  fieldErrors[field] = ''
  formError.value = ''
}

function validate() {
  formError.value = ''
  fieldErrors.username = form.username.trim() ? '' : '请输入用户名'
  fieldErrors.password = form.password ? '' : '请输入密码'
  return !fieldErrors.username && !fieldErrors.password
}

async function handleLogin() {
  if (loading.value) return
  if (!validate()) return

  loading.value = true
  formError.value = ''
  try {
    const res = await authStore.login({
      username: form.username.trim(),
      password: form.password,
      role: form.role
    })
    if (res.code !== 200) {
      formError.value = res.msg || '登录失败，请检查账号信息'
    } else {
      const role = (res.data?.role || '').toLowerCase()
      const redirect = router.currentRoute.value.query?.redirect
      if (redirect) await router.push(String(redirect))
      else if (role === 'tenant') await router.push('/tenant')
      else if (role === 'landlord') await router.push('/landlord')
      else if (role === 'admin') await router.push('/admin')
      else formError.value = '账号角色异常，请联系管理员'
    }
  } catch (e) {
    formError.value = e.response?.data?.msg || e.message || '网络异常，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-form .form-group {
  margin-bottom: 18px;
}

.auth-form .form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
}

.input-wrap {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid #e2e8f0;
  border-radius: 11px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.05);
  transition: all var(--transition);
}

.input-wrap:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 4px rgba(22, 119, 255, 0.12), 0 8px 20px rgba(22, 119, 255, 0.1);
  transform: translateY(-1px);
}

.has-error .input-wrap {
  border-color: var(--danger);
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.1);
}

.input-icon {
  padding: 0 12px;
  font-size: 14px;
  color: var(--primary);
  opacity: 0.6;
  flex-shrink: 0;
}

.input-wrap input {
  flex: 1;
  border: none;
  padding: 12px 12px 12px 0;
  font-size: 14px;
  color: var(--text);
  background: transparent;
  outline: none;
  width: 100%;
  font-family: inherit;
}

.field-error {
  margin-top: 6px;
  font-size: 12px;
  color: var(--danger);
}

.btn-primary {
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  padding: 13px;
  border-radius: 12px;
  letter-spacing: 4px;
  box-shadow: 0 12px 26px rgba(22, 119, 255, 0.28);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(22, 119, 255, 0.34);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.form-error {
  margin-top: 14px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: #fff2f0;
  border: 1px solid #ffa39e;
  color: #cf1322;
  font-size: 13px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
