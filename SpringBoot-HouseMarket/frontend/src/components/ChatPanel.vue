<template>
  <div v-if="visible" class="chat-overlay" @click.self="emit('close')">
    <section class="chat-panel">
      <header class="chat-head">
        <div>
          <strong>{{ activePartnerName || partnerName || '在线聊' }}</strong>
          <span>{{ connected ? '在线' : '连接中...' }}</span>
        </div>
        <button class="chat-close" @click="emit('close')">关闭</button>
      </header>

      <div class="chat-time">服务器校准时间 {{ formatTime(calibratedNow) }}</div>

      <div class="chat-messages" ref="messageBox">
        <div
            v-for="(message, index) in messages"
            :key="index"
            class="chat-message"
            :class="{ mine: Number(message.fromUserId) === Number(currentUserId) }"
        >
          <div class="message-meta">
            <strong>{{ displayName(message) }}</strong>
            <span>{{ formatTime(message.timestamp) }}</span>
          </div>
          <p>{{ message.content }}</p>
        </div>
        <div v-if="!messages.length" class="chat-empty">还没有消息，发送第一条开始沟通</div>
      </div>

      <div class="chat-input">
        <textarea
            v-model="content"
            rows="2"
            :placeholder="activePartnerId ? '输入消息...' : '等待对方发来消息后可回复'"
            @keydown.enter.exact.prevent="send"
        ></textarea>
        <button class="btn" :disabled="!content.trim() || !activePartnerId" @click="send">发送</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, ref, watch} from 'vue'
import SockJS from 'sockjs-client'
import {Client} from '@stomp/stompjs'
import http from '../api/http'

const props = defineProps({
  currentUserId: Number,
  currentUserName: String,
  houseId: Number,
  partnerId: {type: Number, default: 0},
  partnerName: {type: String, default: ''},
  visible: Boolean
})
const emit = defineEmits(['close', 'unread'])

const messages = ref([])
const content = ref('')
const connected = ref(false)
const activePartnerId = ref(Number(props.partnerId) || 0)
const activePartnerName = ref(props.partnerName || '')
const unread = ref(0)
const serverOffset = ref(0)
const calibratedNow = ref(Date.now())
const messageBox = ref(null)

let stompClient = null
let clockTimer = null

function displayName(message) {
  if (Number(message.fromUserId) === Number(props.currentUserId)) {
    return props.currentUserName || '我'
  }
  return message.fromName || '对方'
}

function formatTime(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value).replace('T', ' ').slice(0, 16)
  return date.toLocaleString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

async function loadServerTime() {
  try {
    const res = await http.get('/api/public/server-time')
    const server = new Date(res.data?.data?.serverTime).getTime()
    serverOffset.value = server - Date.now()
  } catch {
    serverOffset.value = 0
  }
}

function connect() {
  const token = localStorage.getItem('token')
  if (!token || !props.currentUserId) return

  const socket = new SockJS('/ws')
  stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {Authorization: `Bearer ${token}`},
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    reconnectDelay: 3000,
    onConnect: () => {
      connected.value = true
      stompClient.subscribe(`/queue/chat/${props.currentUserId}`, msg => {
        try {
          const data = JSON.parse(msg.body)
          const duplicate = messages.value.some(item =>
              item.content === data.content &&
              item.fromUserId === data.fromUserId &&
              item.timestamp === data.timestamp
          )
          if (!duplicate) {
            messages.value.push(data)
            if (!activePartnerId.value) {
              if (Number(data.fromUserId) !== Number(props.currentUserId)) {
                activePartnerId.value = Number(data.fromUserId)
                activePartnerName.value = data.fromName || '对方'
              } else if (data.toUserId) {
                activePartnerId.value = Number(data.toUserId)
              }
            }
            if (!props.visible) {
              unread.value += 1
              emit('unread', unread.value)
            }
            scrollToBottom()
          }
        } catch {
          messages.value.push({content: msg.body, timestamp: new Date().toISOString()})
        }
      })
    },
    onDisconnect: () => {
      connected.value = false
    },
    onStompError: () => {
      connected.value = false
    }
  })
  stompClient.activate()
}

function send() {
  const text = content.value.trim()
  if (!text || !activePartnerId.value || !stompClient) return
  const payload = {
    houseId: props.houseId,
    fromUserId: props.currentUserId,
    fromName: props.currentUserName,
    toUserId: activePartnerId.value,
    content: text,
    timestamp: new Date(Date.now() + serverOffset.value).toISOString()
  }
  stompClient.publish({
    destination: '/app/chat.send',
    body: JSON.stringify(payload)
  })
  messages.value.push(payload)
  content.value = ''
  scrollToBottom()
}

function scrollToBottom() {
  setTimeout(() => {
    if (messageBox.value) messageBox.value.scrollTop = messageBox.value.scrollHeight
  }, 30)
}

watch(() => props.visible, (visible) => {
  if (visible) {
    unread.value = 0
    emit('unread', 0)
    scrollToBottom()
  }
})

onMounted(() => {
  loadServerTime()
  connect()
  clockTimer = setInterval(() => {
    calibratedNow.value = Date.now() + serverOffset.value
  }, 1000)
})

onBeforeUnmount(() => {
  clearInterval(clockTimer)
  stompClient?.deactivate()
})
</script>

<style scoped>
.chat-overlay {
  position: fixed;
  inset: 0;
  z-index: 900;
  background: rgba(15, 23, 42, 0.4);
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  padding: 24px;
}

.chat-panel {
  width: 380px;
  max-width: 94vw;
  height: min(600px, 88vh);
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.28);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid var(--border);
  background: linear-gradient(135deg, #0f172a, #1e3a8a);
  color: #fff;
}

.chat-head strong {
  display: block;
  font-size: 16px;
}

.chat-head span {
  font-size: 12px;
  color: #67e8f9;
}

.chat-close {
  border: 1px solid rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  border-radius: 999px;
  padding: 6px 12px;
  cursor: pointer;
}

.chat-time {
  padding: 8px 16px;
  font-size: 11px;
  color: #64748b;
  background: #f8fafc;
  border-bottom: 1px solid var(--border);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-message {
  max-width: 82%;
  align-self: flex-start;
  background: #f1f5f9;
  border-radius: 12px 12px 12px 2px;
  padding: 10px 12px;
}

.chat-message.mine {
  align-self: flex-end;
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  color: #fff;
  border-radius: 12px 12px 2px 12px;
}

.message-meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 4px;
  font-size: 11px;
  opacity: 0.75;
}

.chat-message p {
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-empty {
  color: #94a3b8;
  text-align: center;
  margin: auto;
  font-size: 13px;
}

.chat-input {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-top: 1px solid var(--border);
  background: #f8fafc;
}

.chat-input textarea {
  flex: 1;
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 10px;
  font: inherit;
  resize: none;
}

@media (max-width: 640px) {
  .chat-overlay {
    padding: 0;
  }

  .chat-panel {
    width: 100%;
    max-width: none;
    height: 92vh;
    border-radius: 0;
  }
}
</style>
