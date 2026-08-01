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
    <form @submit.prevent="handleRegister" class="auth-form">
      <div class="form-group">
        <label>用户名</label>
        <div class="input-wrap">
          <span class="input-icon">👤</span>
          <input v-model="form.username" placeholder="3-20位字符" minlength="3" maxlength="20" autocomplete="username"
                 required/>
        </div>
      </div>

      <div class="form-group">
        <label>密码</label>
        <div class="input-wrap">
          <span class="input-icon">🔒</span>
          <input v-model="form.password" type="password" placeholder="6-20位字符" minlength="6" maxlength="20"
                 autocomplete="new-password" required/>
        </div>
      </div>

      <div class="form-group">
        <label>确认密码</label>
        <div class="input-wrap">
          <span class="input-icon">🔒</span>
          <input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" minlength="6"
                 maxlength="20" autocomplete="new-password" required/>
        </div>
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

      <div class="form-group">
        <label>验证码</label>
        <div class="captcha-row">
          <div class="input-wrap" style="flex:1">
            <span class="input-icon">🖊</span>
            <input v-model="form.captcha" maxlength="4" placeholder="验证码" required/>
          </div>
          <div class="captcha-img" @click="fetchCaptcha" title="点击刷新验证码">
            <img v-if="captchaImage" :src="'data:image/png;base64,' + captchaImage" alt="验证码"/>
            <span v-else>加载中</span>
          </div>
        </div>
      </div>

      <button type="submit" class="btn btn-primary btn-block btn-lg">
        <span>注 册</span>
      </button>
    </form>
  </AuthLayout>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {getCaptcha, register} from '../api/auth'
import AuthLayout from '../components/AuthLayout.vue'

const router = useRouter()
const loading = ref(false)
const captchaId = ref('')
const captchaImage = ref('')

const form = reactive({username: '', password: '', confirmPassword: '', role: 'TENANT', captcha: ''})

async function fetchCaptcha() {
  try {
    const res = await getCaptcha()
    captchaId.value = res.data?.data?.captchaId || ''
    captchaImage.value = res.data?.data?.imageBase64 || ''
  } catch {
    captchaImage.value = ''
  }
}

const roleOptions = [
  {value: 'TENANT', label: '我是租客', icon: '👥'},
  {value: 'LANDLORD', label: '我是房东', icon: '🏠'}
]

async function handleRegister() {
  if (loading.value) return
  if (form.password !== form.confirmPassword) {
    alert('两次密码不一致');
    return
  }
  if (!form.captcha) {
    alert('请输入验证码')
    form.captcha = ''
    return
  }
  loading.value = true
  try {
    const res = await register({
      username: form.username,
      password: form.password,
      role: form.role.toUpperCase(),
      captchaId: captchaId.value,
      captchaCode: form.captcha
    })
    if (res.data?.success) {
      alert('注册成功，请登录')
      router.push('/login')
    } else {
      alert(res.data?.message || '注册失败')
    }
  } catch (e) {
    alert('注册失败: ' + (e.response?.data?.message || e.message))
  } finally {
    loading.value = false
  }
  await fetchCaptcha()
  form.captcha = ''
}

onMounted(fetchCaptcha)
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

.captcha-img {
  width: 120px;
  height: 42px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.captcha-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
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
  border: 2px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--bg-white);
  cursor: pointer;
  transition: all var(--transition);
}

.role-options button:hover {
  border-color: var(--primary);
}

.role-options button.active {
  border-color: var(--primary);
  background: rgba(22, 119, 255, 0.05);
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
