<template>
  <div class="error-page">
    <div class="error-card">
      <span class="error-code">403</span>
      <h1>没有访问权限</h1>
      <p>当前账号无权访问该页面，请切换对应角色的账号</p>
      <div class="error-actions">
        <button class="btn" @click="$router.push('/')">返回首页</button>
        <button class="btn btn-outline" @click="$router.push('/login')">切换账号</button>
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
  background: linear-gradient(135deg, #f59e0b, #ef4444);
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
