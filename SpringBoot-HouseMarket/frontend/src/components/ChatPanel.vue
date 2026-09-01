<template>
  <div v-if="visible" class="chat-overlay" @click.self="emit('close')">
    <section class="chat-panel">
      <!-- 会话视图 -->
      <template v-if="activePartnerId">
        <header class="chat-head">
          <button class="chat-back" @click="backToConversations" title="返回会话列表">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <line x1="19" y1="12" x2="5" y2="12"/>
              <polyline points="12 19 5 12 12 5"/>
            </svg>
          </button>
          <div>
            <strong>{{ activePartnerName }}</strong>
            <span>{{ connected ? '在线' : '连接中...' }}</span>
          </div>
          <button class="chat-close" @click="emit('close')">关闭</button>
        </header>
        <div class="chat-messages" ref="messageBox">
          <div
              v-for="(message, index) in messages"
              :key="message.id || ('tmp-' + index)"
              class="chat-message"
              :class="{ mine: Number(message.fromUserId) === Number(currentUserId) }"
          >
            <div class="message-meta">
              <strong>{{ displayName(message) }}</strong>
              <span>{{ formatTime(message.timestamp) }}</span>
            </div>
            <p>{{ message.content }}</p>
            <small v-if="message.houseTitle" class="msg-house">关于房源：{{ message.houseTitle }}</small>
          </div>
          <div v-if="!messages.length" class="chat-empty">还没有消息，发送第一条开始沟通</div>
          <div v-if="loadingHistory" class="chat-empty">正在加载历史消息...</div>
        </div>

        <div class="chat-input">
          <textarea
              v-model="content"
              rows="2"
              placeholder="输入消息..."
              @keydown.enter.exact.prevent="send"
          ></textarea>
          <button class="btn" :disabled="!content.trim() || !connected" @click="send">发送</button>
        </div>
      </template>

      <!-- 会话列表视图 -->
      <template v-else>
        <header class="chat-head">
          <div>
            <strong>我的消息</strong>
            <span>{{ connected ? '在线' : '连接中...' }}</span>
          </div>
          <button class="chat-close" @click="emit('close')">关闭</button>
        </header>
        <div class="conversation-list">
          <div v-if="loadingConversations" class="chat-empty">正在加载会话...</div>
          <div v-else-if="!conversations.length" class="chat-empty">
            暂无会话。<br/>从房源详情页点击「在线聊」即可联系房东
          </div>
          <button
              v-for="conv in conversations"
              :key="conv.partnerId"
              class="conversation-item"
              @click="openConversation(conv)"
          >
            <span class="conv-avatar">{{ (conv.partnerName || '?').slice(0, 1) }}</span>
            <span class="conv-body">
              <span class="conv-top">
                <strong>{{ conv.partnerName }}</strong>
                <small>{{ formatTime(conv.lastTime) }}</small>
              </span>
              <span class="conv-preview">{{ conv.lastFromMe ? '我：' : '' }}{{ conv.lastMessage }}</span>
              <small v-if="conv.houseTitle" class="conv-house">🏠 {{ conv.houseTitle }}</small>
            </span>
            <span v-if="conv.unreadCount" class="conv-badge">{{ conv.unreadCount }}</span>
          </button>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, ref, watch} from 'vue'
import SockJS from 'sockjs-client'
import {Client} from '@stomp/stompjs'
import http from '../api/http'
import {getConversations, getMessages, getUnreadCount, markRead} from '../api/chat'

const props = defineProps({
  currentUserId: Number,
  currentUserName: String,
  houseId: {type: Number, default: null},
  partnerId: {type: Number, default: 0},
  partnerName: {type: String, default: ''},
  visible: Boolean
})
const emit = defineEmits(['close', 'unread'])

const conversations = ref([])
const messages = ref([])
const content = ref('')
const connected = ref(false)
const activePartnerId = ref(0)
const activePartnerName = ref('')
const unread = ref(0)
const loadingHistory = ref(false)
const loadingConversations = ref(false)
const messageBox = ref(null)

let stompClient = null

const totalUnread = computed(() =>
    conversations.value.reduce((sum, conv) => sum + (Number(conv.unreadCount) || 0), 0)
)

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
  return date.toLocaleString('zh-CN', {month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'})
}

function normalizeMessage(raw) {
  return {
    id: raw.id,
    fromUserId: raw.fromUserId ?? raw.senderId,
    toUserId: raw.toUserId ?? raw.receiverId,
    content: raw.content,
    timestamp: raw.timestamp ?? raw.createTime,
    fromName: raw.fromName ?? raw.senderName,
    houseId: raw.houseId,
    houseTitle: raw.houseTitle
  }
}

function connect() {
  const token = localStorage.getItem('token')
  if (!token || !props.currentUserId) return
  if (stompClient) return

  const socket = new SockJS('/ws')
  stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {Authorization: `Bearer ${token}`},
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    reconnectDelay: 3000,
    onConnect: () => {
      connected.value = true
      // 订阅经 STOMP 用户隔离的私有队列 /user/queue/chat（服务端按登录身份投递）
      stompClient.subscribe('/user/queue/chat', msg => {
        try {
          const data = normalizeMessage(JSON.parse(msg.body))
          handleIncoming(data)
        } catch {
          /* 忽略无法解析的帧 */
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

function disconnect() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
  connected.value = false
}

function handleIncoming(data) {
  // 去重：服务端会同时回显给收发双方
  const duplicate = messages.value.some(item =>
      item.id && data.id && Number(item.id) === Number(data.id)
  )
  const optimisticIndex = messages.value.findIndex(item =>
      item.optimistic && Number(item.toUserId) === Number(data.fromUserId) && item.content === data.content
  )
  if (optimisticIndex >= 0) messages.value.splice(optimisticIndex, 1)
  if (!duplicate) messages.value.push(data)

  if (Number(data.fromUserId) !== Number(props.currentUserId)) {
    if (activePartnerId.value && Number(data.fromUserId) === Number(activePartnerId.value)) {
      // 正在与对方聊天：立即标记已读
      markRead(activePartnerId.value).catch(() => {
      })
    } else {
      refreshConversations()
    }
  }
  scrollToBottom()
}

async function refreshConversations() {
  if (!props.currentUserId) return
  loadingConversations.value = true
  try {
    const res = await getConversations()
    conversations.value = res.data?.data?.conversations || []
    emit('unread', totalUnread.value)
  } catch {
    /* 静默 */
  } finally {
    loadingConversations.value = false
  }
}

async function refreshUnread() {
  try {
    const res = await getUnreadCount()
    emit('unread', Number(res.data?.data?.unread) || 0)
  } catch {
    /* 静默 */
  }
}

async function openConversation(conv) {
  activePartnerId.value = Number(conv.partnerId)
  activePartnerName.value = conv.partnerName || '对方'
  await loadHistory()
  markRead(activePartnerId.value).catch(() => {
  })
  const convItem = conversations.value.find(c => Number(c.partnerId) === Number(activePartnerId.value))
  if (convItem) convItem.unreadCount = 0
  emit('unread', totalUnread.value)
}

async function loadHistory() {
  loadingHistory.value = true
  try {
    const res = await getMessages(activePartnerId.value, {pageSize: 100})
    messages.value = (res.data?.data?.messages || []).map(normalizeMessage)
    scrollToBottom()
  } catch {
    messages.value = []
  } finally {
    loadingHistory.value = false
  }
}

function backToConversations() {
  activePartnerId.value = 0
  activePartnerName.value = ''
  messages.value = []
  refreshConversations()
}

function send() {
  const text = content.value.trim()
  if (!text || !activePartnerId.value || !stompClient || !connected.value) return
  // 乐观插入，服务端回显后去重替换（服务端落库为准）
  messages.value.push({
    optimistic: true,
    fromUserId: props.currentUserId,
    toUserId: activePartnerId.value,
    content: text,
    timestamp: new Date().toISOString(),
    houseId: props.houseId || null
  })
  stompClient.publish({
    destination: '/app/chat.send',
    body: JSON.stringify({
      toUserId: activePartnerId.value,
      houseId: props.houseId || null,
      content: text
    })
  })
  content.value = ''
  scrollToBottom()
}

function scrollToBottom() {
  setTimeout(() => {
    if (messageBox.value) messageBox.value.scrollTop = messageBox.value.scrollHeight
  }, 30)
}

watch(() => props.visible, async (visible) => {
  if (visible) {
    connect()
    unread.value = 0
    emit('unread', 0)
    if (props.partnerId) {
      await openConversation({partnerId: props.partnerId, partnerName: props.partnerName, unreadCount: 0})
    } else {
      await refreshConversations()
    }
  } else {
    // 关闭面板时回到会话列表态，下次打开默认展示列表
    activePartnerId.value = 0
    messages.value = []
    refreshUnread()
  }
})

onBeforeUnmount(disconnect)
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
  width: 400px;
  max-width: 94vw;
  height: min(620px, 88vh);
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
  gap: 10px;
  padding: 16px 18px;
  border-bottom: 1px solid var(--border);
  background: linear-gradient(135deg, #0f172a, #1e3a8a);
  color: #fff;
}

.chat-head strong {
  display: block;
  font-size: 16px;
}

.chat-head > div {
  flex: 1;
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

.chat-back {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  border-radius: 50%;
  cursor: pointer;
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

.msg-house {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  opacity: 0.7;
}

.chat-empty {
  color: #94a3b8;
  text-align: center;
  margin: auto;
  font-size: 13px;
  line-height: 1.8;
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

/* 会话列表 */
.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  text-align: left;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.conversation-item:hover {
  border-color: #1677ff;
  transform: translateX(2px);
}

.conv-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  flex-shrink: 0;
}

.conv-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 8px;
}

.conv-top strong {
  font-size: 14px;
  color: var(--text);
}

.conv-top small {
  color: #9ca3af;
  font-size: 11px;
}

.conv-preview {
  font-size: 13px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-house {
  color: #059669;
  font-size: 11px;
}

.conv-badge {
  position: absolute;
  right: 10px;
  bottom: 10px;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
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
