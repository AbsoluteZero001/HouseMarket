<template>
  <canvas ref="canvasRef" width="120" height="40" @click="generate" style="cursor:pointer; border-radius:4px;"></canvas>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const canvasRef = ref(null)
const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789'

function generate() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, 120, 40)
  ctx.fillStyle = '#f0f0f0'
  ctx.fillRect(0, 0, 120, 40)

  let code = ''
  for (let i = 0; i < 4; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  sessionStorage.setItem('captcha', code.toLowerCase())

  for (let i = 0; i < 3; i++) {
    ctx.strokeStyle = `rgba(${Math.random()*255},${Math.random()*255},${Math.random()*255},0.5)`
    ctx.beginPath()
    ctx.moveTo(Math.random() * 120, Math.random() * 40)
    ctx.lineTo(Math.random() * 120, Math.random() * 40)
    ctx.stroke()
  }

  ctx.font = 'bold 22px Arial'
  ctx.fillStyle = '#333'
  for (let i = 0; i < 4; i++) {
    ctx.save()
    ctx.translate(20 + i * 22, 25)
    ctx.rotate((Math.random() - 0.5) * 0.4)
    ctx.fillText(code.charAt(i), 0, 0)
    ctx.restore()
  }

  for (let i = 0; i < 20; i++) {
    ctx.fillStyle = `rgba(${Math.random()*255},${Math.random()*255},${Math.random()*255},0.3)`
    ctx.fillRect(Math.random() * 120, Math.random() * 40, 2, 2)
  }
}

onMounted(generate)
defineExpose({ generate })
</script>
