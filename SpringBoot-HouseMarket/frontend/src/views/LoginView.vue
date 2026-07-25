<template>
  <div class="auth-page">
    <!-- Brand Panel -->
    <div class="brand-panel">
      <div class="brand-bg">
        <div class="brand-shape shape-a"></div>
        <div class="brand-shape shape-b"></div>
        <div class="brand-shape shape-c"></div>
      </div>
      <div class="brand-content">
        <RouterLink to="/" class="brand-logo">
          <span class="logo-icon">🏠</span>
          <span class="logo-text">房源市场</span>
        </RouterLink>
        <h1 class="brand-title">找到属于你的<span class="highlight">理想家</span></h1>
        <p class="brand-desc">海量真实房源，VR全景看房，一键预约，轻松安家</p>
        <div class="brand-features">
          <div class="feature-item" v-for="f in features" :key="f.icon">
            <span class="feature-icon">{{ f.icon }}</span>
            <div>
              <div class="feature-title">{{ f.title }}</div>
              <div class="feature-sub">{{ f.sub }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Form Panel -->
    <div class="form-panel">
      <div class="form-wrapper">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录您的房源市场账号</p>
        </div>

        <form @submit.prevent="handleLogin" class="auth-form">
          <div class="form-group">
            <label>用户名</label>
            <div class="input-wrap">
              <span class="input-icon">👤</span>
              <input v-model="form.username" placeholder="请输入用户名" autocomplete="username" required />
            </div>
          </div>

          <div class="form-group">
            <label>密码</label>
            <div class="input-wrap">
              <span class="input-icon">🔒</span>
              <input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="current-password" required />
            </div>
          </div>

          <div class="form-group">
            <label>选择角色</label>
            <RoleSlider v-model="form.role" />
          </div>

          <div class="form-group">
            <label>验证码</label>
            <div class="captcha-row">
              <div class="input-wrap" style="flex:1">
                <span class="input-icon">🖊</span>
                <input v-model="form.captcha" maxlength="4" placeholder="验证码" required />
              </div>
              <CaptchaCanvas ref="captchaRef" />
            </div>
          </div>

          <button type="submit" class="btn btn-primary btn-block btn-lg">
            <span>登 录</span>
          </button>
        </form>

        <p class="switch-text">
          还没有账号？<RouterLink to="/register">立即注册</RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import RoleSlider from '../components/RoleSlider.vue'
import CaptchaCanvas from '../components/CaptchaCanvas.vue'

const router = useRouter()
const authStore = useAuthStore()
const captchaRef = ref(null)

const form = reactive({ username: '', password: '', role: 'TENANT', captcha: '' })

const features = [
  { icon: '🏠', title: '海量房源', sub: '覆盖全城真实在售/租房源' },
  { icon: '🔍', title: '精准搜索', sub: '多维筛选快速锁定心仪房源' },
  { icon: '📅', title: '一键预约', sub: '在线预约看房省时省心' }
]

async function handleLogin() {
  const captcha = sessionStorage.getItem('captcha')
  if (form.captcha.toLowerCase() !== captcha) {
    alert('验证码错误')
    captchaRef.value?.generate()
    form.captcha = ''
    return
  }
  try {
    const res = await authStore.login({ username: form.username, password: form.password, role: form.role })
    if (res.code === 200) {
      const role = res.data.role?.toLowerCase()
      if (role === 'tenant') router.push('/tenant')
      else if (role === 'landlord') router.push('/landlord')
      else if (role === 'admin') router.push('/admin')
    } else {
      alert(res.msg || '登录失败')
    }
  } catch (e) {
    alert('登录失败: ' + (e.response?.data?.message || e.message))
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  min-height: 100vh;
}

/* ===== Brand Panel ===== */
.brand-panel {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 30%, #003eb3 70%, #002766 100%);
  overflow: hidden;
}
.brand-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}
.brand-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  background: #fff;
}
.shape-a {
  width: 500px; height: 500px;
  top: -150px; right: -100px;
}
.shape-b {
  width: 300px; height: 300px;
  bottom: -80px; left: -60px;
}
.shape-c {
  width: 200px; height: 200px;
  top: 50%; left: 30%;
  transform: translate(-50%, -50%);
}
.brand-content {
  position: relative;
  z-index: 1;
  max-width: 440px;
  padding: 60px 40px;
  color: #fff;
}
.brand-logo {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #fff;
  margin-bottom: 48px;
  opacity: 0.9;
  transition: opacity 0.2s;
}
.brand-logo:hover { opacity: 1; color: #fff; }
.logo-icon { font-size: 28px; }
.logo-text { font-size: 22px; font-weight: 700; letter-spacing: 2px; }
.brand-title {
  font-size: 36px;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 16px;
}
.brand-title .highlight {
  color: #ffd666;
}
.brand-desc {
  font-size: 15px;
  opacity: 0.75;
  line-height: 1.6;
  margin-bottom: 48px;
}
.brand-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
}
.feature-icon {
  font-size: 24px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.12);
  border-radius: 12px;
  flex-shrink: 0;
}
.feature-title {
  font-size: 14px;
  font-weight: 600;
}
.feature-sub {
  font-size: 12px;
  opacity: 0.65;
  margin-top: 2px;
}

/* ===== Form Panel ===== */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  padding: 40px;
}
.form-wrapper {
  width: 100%;
  max-width: 420px;
}
.form-header {
  margin-bottom: 32px;
}
.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 6px;
}
.form-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

/* Form controls */
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
  box-shadow: 0 0 0 3px rgba(22,119,255,0.1);
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

.switch-text {
  text-align: center;
  margin-top: 24px;
  color: var(--text-muted);
  font-size: 14px;
}
.switch-text a {
  color: var(--primary);
  font-weight: 600;
}

@media (max-width: 768px) {
  .auth-page { flex-direction: column; }
  .brand-panel { min-height: 280px; }
  .brand-content { padding: 40px 24px; }
  .brand-title { font-size: 26px; }
  .brand-features { display: none; }
  .form-panel { padding: 24px; }
}
</style>
