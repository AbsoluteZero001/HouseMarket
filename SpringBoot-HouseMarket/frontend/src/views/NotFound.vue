<template>
  <div class="error-page">
    <div class="error-card">
      <span class="error-code">404</span>
      <h1>页面不存在</h1>
      <p>您访问的页面可能已被移除或地址有误</p>
      <div class="error-actions">
        <button class="btn" @click="$router.push('/')">返回首页</button>
        <button class="btn btn-outline" @click="$router.back()">返回上页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(900px 460px at 88% -4%, rgba(139, 92, 246, 0.1), transparent 58%),
  radial-gradient(900px 460px at 2% 4%, rgba(6, 182, 212, 0.1), transparent 55%),
  #f4f7fc;
}

.error-card {
  text-align: center;
  background: #fff;
  border-radius: 24px;
  padding: 60px 80px;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.14);
}

.error-code {
  display: block;
  font-size: 88px;
  font-weight: 900;
  background: linear-gradient(135deg, #1677ff, #8b5cf6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.error-card h1 {
  font-size: 24px;
  margin: 8px 0;
}

.error-card p {
  color: #6b7280;
  margin-bottom: 24px;
}

.error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
