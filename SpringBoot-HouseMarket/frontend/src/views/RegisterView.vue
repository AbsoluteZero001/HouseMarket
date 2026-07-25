<template>
  <div class="register-page">
    <div class="particles-container">
      <div class="particle" v-for="i in 5" :key="i"></div>
    </div>
    <div class="decoration-1"></div>
    <div class="decoration-2"></div>
    <div class="form-container">
      <h2>注册</h2>
      <p class="subtitle">创建您的账号</p>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" required minlength="3" maxlength="20" placeholder="3-20位字符" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" required minlength="6" maxlength="20" placeholder="6-20位字符" />
        </div>
        <div class="form-group">
          <label>确认密码</label>
          <input v-model="form.confirmPassword" type="password" required minlength="6" maxlength="20" placeholder="再次输入密码" />
        </div>
        <div class="form-group">
          <label>选择角色</label>
          <select v-model="form.role" required>
            <option value="">请选择</option>
            <option value="tenant">租客</option>
            <option value="landlord">房东</option>
          </select>
        </div>
        <div class="form-group">
          <label>验证码</label>
          <div class="captcha-row">
            <input v-model="form.captcha" maxlength="4" required placeholder="验证码" style="flex:1" />
            <CaptchaCanvas ref="captchaRef" />
          </div>
        </div>
        <button type="submit" class="btn btn-block">注册</button>
      </form>
      <p class="switch-link">已有账号？<RouterLink to="/login">立即登录</RouterLink></p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'
import CaptchaCanvas from '../components/CaptchaCanvas.vue'

const router = useRouter()
const captchaRef = ref(null)

const form = reactive({ username: '', password: '', confirmPassword: '', role: '', captcha: '' })

async function handleRegister() {
  if (form.password !== form.confirmPassword) {
    alert('两次密码不一致')
    return
  }
  const captcha = sessionStorage.getItem('captcha')
  if (form.captcha.toLowerCase() !== captcha) {
    alert('验证码错误')
    captchaRef.value?.generate()
    form.captcha = ''
    return
  }
  try {
    const roleMap = { tenant: 'TENANT', landlord: 'LANDLORD' }
    const res = await register({ username: form.username, password: form.password, role: roleMap[form.role], status: 'normal' })
    if (res.data.success) {
      alert('注册成功！即将跳转到登录页')
      setTimeout(() => router.push('/login'), 1500)
    } else {
      alert(res.data.message || '注册失败')
    }
  } catch (e) {
    alert('注册失败: ' + (e.response?.data?.message || e.message))
  }
}
</script>

<style scoped>
.register-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden; }
.form-container { background: rgba(255,255,255,0.95); border-radius: 12px; padding: 40px; width: 90%; max-width: 450px; box-shadow: 0 10px 40px rgba(0,0,0,0.15); }
.form-container h2 { text-align: center; color: #333; margin-bottom: 5px; }
.subtitle { text-align: center; color: #999; margin-bottom: 25px; }
.btn-block { width: 100%; padding: 12px; margin-top: 15px; background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; border: none; border-radius: 6px; font-size: 16px; cursor: pointer; font-weight: 700; transition: transform 0.3s; }
.btn-block:hover { transform: translateY(-2px); }
.switch-link { text-align: center; margin-top: 20px; color: #999; }
.switch-link a { color: #667eea; text-decoration: none; font-weight: 600; }
.captcha-row { display: flex; gap: 10px; align-items: center; }
.particles-container { position: fixed; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; z-index: -1; }
.particle { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.1); animation: float 15s infinite ease-in-out; }
.particle:nth-child(1) { width:10px; height:10px; top:20%; left:10%; animation-delay:0s; }
.particle:nth-child(2) { width:15px; height:15px; top:60%; left:20%; animation-delay:2s; }
.particle:nth-child(3) { width:8px; height:8px; top:40%; left:80%; animation-delay:4s; }
.particle:nth-child(4) { width:12px; height:12px; top:80%; left:60%; animation-delay:6s; }
.particle:nth-child(5) { width:6px; height:6px; top:30%; left:50%; animation-delay:8s; }
.decoration-1, .decoration-2 { position: fixed; z-index: -1; opacity: 0.1; }
.decoration-1 { top:10%; right:10%; width:100px; height:100px; border:2px solid #ff6b6b; border-radius:50%; animation: rotate 20s linear infinite; }
.decoration-2 { bottom:20%; left:15%; width:80px; height:80px; border:2px solid #4ecdc4; border-radius:50%; animation: rotate 25s linear infinite reverse; }
@keyframes float { 0%,100% { transform:translate(0,0); } 25% { transform:translate(20px,30px); } 50% { transform:translate(-20px,20px); } 75% { transform:translate(10px,-30px); } }
@keyframes rotate { 0% { transform:rotate(0deg); } 100% { transform:rotate(360deg); } }
</style>
