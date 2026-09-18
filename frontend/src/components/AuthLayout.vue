<template>
  <div class="auth-page">
    <div class="brand-panel" :style="{ '--brand-gradient': gradient }">
      <img :src="'/backgrounds/auth-hero.png'" class="brand-photo" alt=""/>
      <div class="brand-aurora"></div>
      <div class="brand-image-stack">
        <div class="brand-image-card card-a">
          <img :src="'/uploads/img_1.png'" alt="精品房源"/>
        </div>
        <div class="brand-image-card card-b">
          <img :src="'/uploads/img_3.png'" alt="复式空间"/>
        </div>
        <div class="brand-image-card card-c">
          <img :src="'/uploads/img_5.png'" alt="城市夜景"/>
        </div>
        <div class="brand-float-chip chip-price">¥ 6,500 / 月</div>
        <div class="brand-float-chip chip-flow">预约审批 · 全程可溯</div>
      </div>
      <div class="brand-content">
        <RouterLink to="/" class="brand-logo">
          <span class="logo-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round"><path
                d="M3 9l1.5-5.5A1.5 1.5 0 0 1 5.97 2.5h12.06a1.5 1.5 0 0 1 1.47 1L21 9"/><path
                d="M3 9a3 3 0 0 0 6 0 3 3 0 0 0 6 0 3 3 0 0 0 6 0"/><path d="M4 9v11a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V9"/><path
                d="M9 21v-6h6v6"/></svg>
          </span>
          <span class="logo-text">房源市场</span>
        </RouterLink>
        <h1 class="brand-title" v-html="brandTitle"></h1>
        <p class="brand-desc">{{ brandDesc }}</p>
        <div class="brand-features">
          <div class="feature-item" v-for="f in features" :key="f.icon">
            <span class="feature-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round"><path v-if="f.icon === 'home'"
                                                                        d="M3 9l1.5-5.5A1.5 1.5 0 0 1 5.97 2.5h12.06a1.5 1.5 0 0 1 1.47 1L21 9"/><path
                  v-if="f.icon === 'home'" d="M3 9a3 3 0 0 0 6 0 3 3 0 0 0 6 0 3 3 0 0 0 6 0"/><path
                  v-if="f.icon === 'home'" d="M4 9v11a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V9"/><circle
                  v-if="f.icon === 'search'" cx="11" cy="11" r="8"/><line v-if="f.icon === 'search'" x1="21" y1="21"
                                                                          x2="16.65" y2="16.65"/><path
                  v-if="f.icon === 'calendar'" d="M8 2v4"/><path v-if="f.icon === 'calendar'" d="M16 2v4"/><rect
                  v-if="f.icon === 'calendar'" x="3" y="4" width="18" height="18" rx="2"/><path
                  v-if="f.icon === 'calendar'" d="M3 10h18"/></svg>
            </span>
            <div>
              <div class="feature-title">{{ f.title }}</div>
              <div class="feature-sub">{{ f.sub }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="form-panel">
      <div class="form-panel-glow"></div>
      <div class="form-wrapper">
        <div class="form-header">
          <p class="form-kicker">HOUSEMARKET</p>
          <h2>{{ title }}</h2>
          <p>{{ subtitle }}</p>
        </div>
        <slot/>
        <p class="switch-text">
          {{ switchText }}
          <RouterLink :to="switchTo">{{ switchLink }}</RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  gradient: {type: String, default: 'linear-gradient(135deg, #1677ff 0%, #06b6d4 40%, #8b5cf6 78%, #ec4899 100%)'},
  title: {type: String, required: true},
  subtitle: {type: String, required: true},
  brandTitle: {type: String, default: '找到属于你的<span class="highlight">理想家</span>'},
  brandDesc: {type: String, default: '海量真实房源，VR全景看房，一键预约，轻松安家'},
  switchText: {type: String, required: true},
  switchLink: {type: String, required: true},
  switchTo: {type: String, required: true}
})

const features = [
  {icon: 'home', title: '海量房源', sub: '覆盖全城真实在售/租房源'},
  {icon: 'search', title: '精准搜索', sub: '多维筛选快速锁定心仪房源'},
  {icon: 'calendar', title: '一键预约', sub: '在线预约看房，审批通知闭环'}
]
</script>

<style scoped>
.auth-page {
  display: flex;
  min-height: 100vh;
  background: #f4f7fc;
}

.brand-panel {
  flex: 1.08;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: #fff;
  background: linear-gradient(140deg, rgba(7, 28, 52, 0.25), rgba(7, 28, 52, 0.42)),
  var(--brand-gradient);
  min-width: 0;
}

.brand-aurora {
  position: absolute;
  inset: -30%;
  z-index: 1;
  background: linear-gradient(115deg, transparent 0%, rgba(255, 255, 255, 0.16) 28%, transparent 44%, rgba(255, 255, 255, 0.1) 62%, transparent 82%),
  conic-gradient(from 210deg at 50% 50%, rgba(255, 255, 255, 0.16), transparent 22%, rgba(103, 232, 249, 0.2) 42%, transparent 62%, rgba(255, 255, 255, 0.16));
  animation: aurora-drift 16s ease-in-out infinite alternate;
}

.brand-photo {
  position: absolute;
  inset: 0;
  z-index: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.92;
}

.brand-image-stack {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
}

.brand-image-card {
  position: absolute;
  border-radius: 22px;
  overflow: hidden;
  box-shadow: 0 30px 70px rgba(0, 0, 0, 0.34);
  border: 1px solid rgba(255, 255, 255, 0.26);
  transform: rotate(-3deg);
  animation: float 7s ease-in-out infinite;
}

.brand-image-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.card-a {
  width: 210px;
  height: 150px;
  top: 12%;
  left: 6%;
}

.card-b {
  width: 180px;
  height: 240px;
  right: 8%;
  top: 18%;
  transform: rotate(4deg);
  animation-delay: -2s;
}

.card-c {
  width: 230px;
  height: 160px;
  left: 10%;
  bottom: 10%;
  transform: rotate(2deg);
  animation-delay: -4s;
}

.brand-float-chip {
  position: absolute;
  padding: 10px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.34);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.2);
  animation: float 5.5s ease-in-out infinite;
}

.chip-price {
  right: 5%;
  bottom: 22%;
}

.chip-flow {
  left: 50%;
  top: 8%;
  transform: translateX(-50%);
  animation-delay: -2.4s;
}

.brand-content {
  position: relative;
  z-index: 2;
  max-width: 500px;
  padding: 64px 48px;
  text-shadow: 0 3px 18px rgba(0, 0, 0, 0.2);
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: #fff;
  margin-bottom: 54px;
  opacity: 0.94;
  transition: all 0.25s ease;
}

.brand-logo:hover {
  opacity: 1;
  color: #fff;
  transform: translateY(-1px);
}

.logo-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.32);
  border-radius: 13px;
  backdrop-filter: blur(10px);
}

.logo-text {
  font-size: 23px;
  font-weight: 800;
  letter-spacing: 2px;
}

.brand-title {
  font-size: clamp(34px, 4.4vw, 50px);
  font-weight: 800;
  line-height: 1.22;
  margin-bottom: 16px;
  letter-spacing: 0;
}

.brand-title .highlight {
  background: linear-gradient(90deg, #fde68a, #f9a8d4, #a5f3fc);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

.brand-desc {
  font-size: 15px;
  opacity: 0.82;
  line-height: 1.7;
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
  width: 46px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.26);
  border-radius: 14px;
  backdrop-filter: blur(10px);
  flex-shrink: 0;
}

.feature-title {
  font-size: 14px;
  font-weight: 700;
}

.feature-sub {
  font-size: 12px;
  opacity: 0.68;
  margin-top: 2px;
}

.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f7fc;
  padding: 40px;
  position: relative;
  overflow: hidden;
}

.form-panel-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(700px 360px at 88% 4%, rgba(236, 72, 153, 0.1), transparent 60%),
  radial-gradient(720px 420px at 4% 96%, rgba(139, 92, 246, 0.1), transparent 60%);
  pointer-events: none;
}

.form-wrapper {
  position: relative;
  width: 100%;
  max-width: 430px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 22px;
  padding: 36px 34px 30px;
  backdrop-filter: blur(20px) saturate(160%);
  -webkit-backdrop-filter: blur(20px) saturate(160%);
  box-shadow: 0 28px 70px rgba(15, 23, 42, 0.12);
}

.form-kicker {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 3px;
  background: linear-gradient(90deg, #1677ff, #06b6d4, #8b5cf6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  margin-bottom: 8px;
}

.form-header {
  margin-bottom: 28px;
}

.form-header h2 {
  font-size: 30px;
  font-weight: 800;
  color: var(--text);
  margin-bottom: 6px;
  letter-spacing: 0;
}

.form-header p:not(.form-kicker) {
  color: var(--text-secondary);
  font-size: 14px;
}

.switch-text {
  text-align: center;
  margin-top: 24px;
  color: var(--text-muted);
  font-size: 14px;
}

.switch-text a {
  color: var(--primary);
  font-weight: 700;
}

@keyframes aurora-drift {
  0% {
    transform: translate3d(-4%, -3%, 0) rotate(0deg) scale(1);
  }
  50% {
    transform: translate3d(5%, 4%, 0) rotate(10deg) scale(1.12);
  }
  100% {
    transform: translate3d(-4%, -3%, 0) rotate(0deg) scale(1);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(var(--r, -3deg));
  }
  50% {
    transform: translateY(-12px) rotate(var(--r, -3deg));
  }
}

@media (max-width: 900px) {
  .auth-page {
    flex-direction: column;
  }

  .brand-panel {
    min-height: 300px;
    flex: none;
  }

  .brand-content {
    padding: 42px 26px;
    max-width: none;
  }

  .brand-logo {
    margin-bottom: 30px;
  }

  .brand-title {
    font-size: 32px;
  }

  .brand-features {
    display: none;
  }

  .brand-image-card {
    opacity: 0.6;
  }

  .card-a {
    width: 150px;
    height: 110px;
    left: 4%;
    top: 8%;
  }

  .card-b {
    width: 130px;
    height: 170px;
    right: 4%;
    top: 12%;
  }

  .card-c {
    display: none;
  }

  .chip-price,
  .chip-flow {
    display: none;
  }

  .form-panel {
    padding: 24px 18px 34px;
  }

  .form-wrapper {
    padding: 28px 22px 24px;
  }
}
</style>
