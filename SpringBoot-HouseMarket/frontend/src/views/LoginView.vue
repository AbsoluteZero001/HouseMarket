<template>
  <AuthLayout
      gradient="linear-gradient(135deg, #1677ff 0%, #0958d9 30%, #003eb3 70%, #002766 100%)"
      title="欢迎回来"
      subtitle="登录您的房源市场账号"
      switch-text="还没有账号？"
      switch-link="立即注册"
      switch-to="/register"
  >
    <form @submit.prevent="handleLogin" class="auth-form">
      <div class="form-group">
        <label>用户名</label>
        <div class="input-wrap">
          <span class="input-icon">👤</span>
          <input v-model="form.username" placeholder="请输入用户名" autocomplete="username" required/>
        </div>
      </div>

      <div class="form-group">
        <label>密码</label>
        <div class="input-wrap">
          <span class="input-icon">🔒</span>
          <input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="current-password"
                 required/>
        </div>
      </div>

      <div class="form-group">
        <label>选择角色</label>
        <RoleSlider v-model="form.role"/>
      </div>

      <div class="form-group">
        <label>验证码</label>
        <div class="captcha-row">
          <div class="input-wrap" style="flex:1">
            <span class="input-icon">🖊</span>
            <input v-model="form.captcha" maxlength="4" placeholder="验证码" required/>
          </div>
          <CaptchaCanvas ref="captchaRef"/>
        </div>
      </div>

      <button type="submit" class="btn btn-primary btn-block btn-lg" :disabled="loading">
        <span>{{ loading ? '登录中...' : '登 录' }}</span>
      </button>
    </form>
  </AuthLayout>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useAuthStore} from '../stores/auth'
import RoleSlider from '../components/RoleSlider.vue'
import CaptchaCanvas from '../components/CaptchaCanvas.vue'
import AuthLayout from '../components/AuthLayout.vue'

const router = useRouter()
const authStore = useAuthStore()
const captchaRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '', role: 'TENANT', captcha: '' })

async function handleLogin() {
  if (loading.value) return
  const captcha = sessionStorage.getItem('captcha')
  if (form.captcha.toLowerCase() !== captcha) {
    alert('验证码错误')
    captchaRef.value?.generate()
    form.captcha = ''
    return
  }
  loading.value = true
  try {
    const res = await authStore.login({ username: form.username, password: form.password, role: form.role })
    if (res.code === 200) {
      const role = (res.data?.role || '').toLowerCase()
      if (role === 'tenant') await router.push('/tenant')
      else if (role === 'landlord') await router.push('/landlord')
      else if (role === 'admin') await router.push('/admin')
      else alert('未知角色: ' + (res.data?.role || '无'))
    } else {
      alert(res.msg || '登录失败')
    }
  } catch (e) {
    alert('登录失败: ' + (e.response?.data?.msg || e.message))
  } finally {
    loading.value = false
  }
  captchaRef.value?.generate()
  form.captcha = ''
}
</script>

<style scoped>
.auth-form .form-group {
  margin-bottom: 20px;
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
  background: var(--bg-white);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  transition: all var(--transition);
}

.input-wrap:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
}

.input-icon {
  padding: 0 12px;
  font-size: 14px;
  opacity: 0.4;
  flex-shrink: 0;
}

.input-wrap input {
  flex: 1;
  border: none;
  padding: 11px 12px 11px 0;
  font-size: 14px;
  color: var(--text);
  background: transparent;
  outline: none;
  width: 100%;
}

.captcha-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.btn-primary {
  background: var(--primary);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  padding: 13px;
  border-radius: var(--radius-sm);
  letter-spacing: 4px;
}

.btn-primary:hover {
  background: var(--primary-dark);
}
</style>
