<template>
  <AuthLayout
      gradient="linear-gradient(135deg, #ff6b35 0%, #d44a1a 30%, #b8380a 70%, #8a2000 100%)"
      title="创建账号"
      subtitle="立即注册，开始找房"
      brand-title="开启你的<span class='highlight'>找房之旅</span>"
      brand-desc="注册账号，享受智能推荐、VR看房、专属经纪人服务"
      switch-text="已有账号？"
      switch-link="立即登录"
      switch-to="/login"
  >
    <form @submit.prevent="handleRegister" class="auth-form" novalidate>
      <div class="form-group" :class="{ 'has-error': fieldErrors.username }">
        <label>用户名</label>
        <div class="input-wrap">
          <span class="input-icon">👤</span>
          <input
              v-model="form.username"
              placeholder="3-20位字符"
              maxlength="20"
              autocomplete="username"
              @input="clearField('username')"
          />
        </div>
        <p v-if="fieldErrors.username" class="field-error">{{ fieldErrors.username }}</p>
      </div>

      <div class="form-group" :class="{ 'has-error': fieldErrors.nickname }">
        <label>昵称</label>
        <div class="input-wrap">
          <span class="input-icon">✨</span>
          <input
              v-model="form.nickname"
              placeholder="留空自动生成，如：低调玩家"
              maxlength="20"
              @input="clearField('nickname')"
          />
        </div>
        <p v-if="fieldErrors.nickname" class="field-error">{{ fieldErrors.nickname }}</p>
      </div>

      <div class="form-group" :class="{ 'has-error': fieldErrors.password }">
        <label>密码</label>
        <div class="input-wrap">
          <span class="input-icon">🔒</span>
          <input
              v-model="form.password"
              type="password"
              placeholder="6-20位字符"
              maxlength="20"
              autocomplete="new-password"
              @input="clearField('password')"
          />
        </div>
        <p v-if="fieldErrors.password" class="field-error">{{ fieldErrors.password }}</p>
      </div>

      <div class="form-group" :class="{ 'has-error': fieldErrors.confirmPassword }">
        <label>确认密码</label>
        <div class="input-wrap">
          <span class="input-icon">🔒</span>
          <input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              maxlength="20"
              autocomplete="new-password"
              @input="clearField('confirmPassword')"
          />
        </div>
        <p v-if="fieldErrors.confirmPassword" class="field-error">{{ fieldErrors.confirmPassword }}</p>
      </div>

      <div class="form-group">
        <label>选择角色</label>
        <div class="role-options">
          <button
              v-for="opt in roleOptions"
              :key="opt.value"
              type="button"
              :class="{ active: form.role === opt.value }"
              @click="form.role = opt.value"
          >
            <span class="opt-icon">{{ opt.icon }}</span>
            <span class="opt-label">{{ opt.label }}</span>
          </button>
        </div>
      </div>

      <button type="submit" class="btn btn-primary btn-block btn-lg" :disabled="loading">
        <span class="btn-spinner" v-if="loading"></span>
        <span>{{ loading ? '注册中...' : '注 册' }}</span>
      </button>

      <transition name="fade">
        <p v-if="formError" class="form-error">{{ formError }}</p>
      </transition>
      <transition name="fade">
        <p v-if="successMsg" class="form-success">{{ successMsg }}</p>
      </transition>
    </form>
  </AuthLayout>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {register} from '../api/auth'
import AuthLayout from '../components/AuthLayout.vue'

const router = useRouter()
const loading = ref(false)
const formError = ref('')
const successMsg = ref('')
const fieldErrors = reactive({username: '', nickname: '', password: '', confirmPassword: ''})

const form = reactive({username: '', nickname: '', password: '', confirmPassword: '', role: 'TENANT'})

const roleOptions = [
  {value: 'TENANT', label: '我是租客', icon: '👥'},
  {value: 'LANDLORD', label: '我想成为房东（需审核）', icon: '🏠'}
]

function clearField(field) {
  fieldErrors[field] = ''
  formError.value = ''
}

function validate() {
  formError.value = ''
  fieldErrors.username = form.username.trim().length >= 3 ? '' : '用户名至少3位字符'
  fieldErrors.nickname = ''
  fieldErrors.password = form.password.length >= 6 ? '' : '密码至少6位字符'
  fieldErrors.confirmPassword = form.confirmPassword === form.password ? '' : '两次输入的密码不一致'
  return !fieldErrors.username && !fieldErrors.password && !fieldErrors.confirmPassword
}

async function handleRegister() {
  if (loading.value) return
  if (!validate()) return

  loading.value = true
  formError.value = ''
  successMsg.value = ''
  try {
    const res = await register({
      username: form.username.trim(),
      nickname: form.nickname.trim(),
      password: form.password,
      role: form.role
    })
    if (res.data?.success) {
      successMsg.value = form.role === 'LANDLORD'
          ? '房东入驻申请已提交，等待管理员审核，正在跳转到登录页...'
          : '注册成功，正在跳转到登录页...'
      setTimeout(() => router.push('/login'), 800)
    } else {
      formError.value = res.data?.message || '注册失败，请稍后重试'
    }
  } catch (e) {
    formError.value = e.response?.data?.message || e.message || '网络异常，请稍后重试'
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

.role-options {
  display: flex;
  gap: 10px;
}

.role-options button {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: var(--bg-white);
  cursor: pointer;
  transition: all var(--transition);
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
}

.role-options button:hover {
  border-color: #06b6d4;
  transform: translateY(-2px);
}

.role-options button.active {
  border-color: transparent;
  background: linear-gradient(135deg, rgba(22, 119, 255, 0.1), rgba(6, 182, 212, 0.12));
  box-shadow: 0 10px 22px rgba(22, 119, 255, 0.12);
  transform: translateY(-2px);
}

.opt-icon {
  font-size: 24px;
}

.opt-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
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

.form-error,
.form-success {
  margin-top: 14px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  font-size: 13px;
}

.form-error {
  background: #fff2f0;
  border: 1px solid #ffa39e;
  color: #cf1322;
}

.form-success {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #389e0d;
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
