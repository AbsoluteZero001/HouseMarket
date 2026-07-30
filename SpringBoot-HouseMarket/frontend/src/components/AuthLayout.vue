<template>
  <div class="auth-page">
    <div class="brand-panel" :style="{ background: gradient }">
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
        <h1 class="brand-title" v-html="brandTitle"></h1>
        <p class="brand-desc">{{ brandDesc }}</p>
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

    <div class="form-panel">
      <div class="form-wrapper">
        <div class="form-header">
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
  gradient: {type: String, default: 'linear-gradient(135deg, #1677ff 0%, #0958d9 30%, #003eb3 70%, #002766 100%)'},
  title: {type: String, required: true},
  subtitle: {type: String, required: true},
  brandTitle: {type: String, default: '找到属于你的<span class="highlight">理想家</span>'},
  brandDesc: {type: String, default: '海量真实房源，VR全景看房，一键预约，轻松安家'},
  switchText: {type: String, required: true},
  switchLink: {type: String, required: true},
  switchTo: {type: String, required: true}
})

const features = [
  {icon: '🏠', title: '海量房源', sub: '覆盖全城真实在售/租房源'},
  {icon: '🔍', title: '精准搜索', sub: '多维筛选快速锁定心仪房源'},
  {icon: '📅', title: '一键预约', sub: '在线预约看房省时省心'}
]
</script>

<style scoped>
.auth-page {
  display: flex;
  min-height: 100vh;
}

.brand-panel {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
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
  width: 500px;
  height: 500px;
  top: -150px;
  right: -100px;
}

.shape-b {
  width: 300px;
  height: 300px;
  bottom: -80px;
  left: -60px;
}

.shape-c {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 30%;
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

.brand-logo:hover {
  opacity: 1;
  color: #fff;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 2px;
}

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
  background: rgba(255, 255, 255, 0.12);
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
  .auth-page {
    flex-direction: column;
  }

  .brand-panel {
    min-height: 280px;
  }

  .brand-content {
    padding: 40px 24px;
  }

  .brand-title {
    font-size: 26px;
  }

  .brand-features {
    display: none;
  }

  .form-panel {
    padding: 24px;
  }
}
</style>
