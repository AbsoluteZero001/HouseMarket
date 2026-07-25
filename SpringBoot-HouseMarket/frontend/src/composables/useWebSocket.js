import { ref, onUnmounted } from 'vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

export function useWebSocket() {
  const connected = ref(false)
  const notification = ref(null)
  let stompClient = null
  let reconnectTimer = null

  function connect() {
    const token = localStorage.getItem('token')
    if (!token) return

    const socket = new SockJS('/ws')
    stompClient = new Client({
      webSocketFactory: () => socket,
      connectHeaders: { Authorization: `Bearer ${token}` },
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      reconnectDelay: 3000,
      onConnect: () => {
        connected.value = true
        stompClient.subscribe('/user/queue/appointment', msg => {
          try {
            notification.value = JSON.parse(msg.body)
          } catch (e) {
            notification.value = msg.body
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
    clearTimeout(reconnectTimer)
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
    }
    connected.value = false
  }

  onUnmounted(() => disconnect())

  return { connected, notification, connect, disconnect }
}
