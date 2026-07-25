<template>
  <canvas ref="canvasRef" width="120" height="42" @click="generate" class="captcha-canvas" title="点击刷新验证码"></canvas>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const canvasRef = ref(null)
const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789'

function generate() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, 120, 42)

  // Background
  ctx.fillStyle = '#fafafa'
  ctx.fillRect(0, 0, 120, 42)

  let code = ''
  for (let i = 0; i < 4; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  sessionStorage.setItem('captcha', code.toLowerCase())

  // Noise lines
  for (let i = 0; i < 3; i++) {
    ctx.strokeStyle = `rgba(${Math.random()*200},${Math.random()*200},${Math.random()*200},0.3)`
    ctx.beginPath()
    ctx.moveTo(Math.random() * 120, Math.random() * 42)
    ctx.lineTo(Math.random() * 120, Math.random() * 42)
    ctx.stroke()
  }

  // Characters
  ctx.font = 'bold 22px "Segoe UI", Arial, sans-serif'
  for (let i = 0; i < 4; i++) {
    ctx.save()
    ctx.translate(18 + i * 24, 28)
    ctx.rotate((Math.random() - 0.5) * 0.4)
    ctx.fillStyle = `hsl(${Math.random() * 60 + 200}, 60%, ${Math.random() * 20 + 30}%)`
    ctx.fillText(code.charAt(i), 0, 0)
    ctx.restore()
  }

  // Noise dots
  for (let i = 0; i < 20; i++) {
    ctx.fillStyle = `rgba(${Math.random()*180},${Math.random()*180},${Math.random()*180},0.2)`
    ctx.fillRect(Math.random() * 120, Math.random() * 42, 2, 2)
  }
}

onMounted(generate)
defineExpose({ generate })
</script>

<style scoped>
.captcha-canvas {
  cursor: pointer;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
  transition: border-color var(--transition);
  flex-shrink: 0;
}
.captcha-canvas:hover {
  border-color: var(--primary);
}
</style>
