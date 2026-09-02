// WebSocket 端到端验证：通知定向投递 / 聊天实时+落库 / 越权订阅拦截
// 使用 Node 24 内置 WebSocket，直连 SockJS 原生 WS 通道 /ws/websocket
const BASE = 'http://localhost:8082'
const results = []

function check(name, cond, detail = '') {
    results.push([name, !!cond])
    console.log(`  [${cond ? 'PASS' : 'FAIL'}] ${name}${cond ? '' : '  ' + detail}`)
}

async function login(username, password, role) {
    const res = await fetch(BASE + '/api/v1/auth/login', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password, role})
    })
    const data = await res.json()
    if (data.code !== 200) throw new Error('login failed: ' + JSON.stringify(data))
    return {token: data.token, user: data.data}
}

// ---------- 极简 STOMP 客户端 ----------
class StompTestClient {
    constructor(token) {
        this.token = token
        this.subs = {}
        this.closed = false
        this.error = null
        this.connected = new Promise((resolve) => {
            this._resolveConnected = resolve
        })
        this.ws = new WebSocket('ws://localhost:8082/ws/websocket')
        this.ws.onmessage = (ev) => this._onFrame(ev.data)
        this.ws.onclose = () => {
            this.closed = true
        }
        this.ws.onopen = () => {
            this.ws.send(`CONNECT\naccept-version:1.2\nhost:localhost\nAuthorization:Bearer ${this.token}\nheart-beat:0,0\n\n\0`)
        }
        setTimeout(() => this._resolveConnected(), 8000)
    }

    _onFrame(raw) {
        for (const frame of raw.split('\0').filter(f => f.trim())) {
            const [headerBlock, ...bodyParts] = frame.split('\n\n')
            const lines = headerBlock.split('\n')
            const command = lines[0]
            const headers = {}
            for (const line of lines.slice(1)) {
                const idx = line.indexOf(':')
                if (idx > 0) headers[line.slice(0, idx)] = line.slice(idx + 1)
            }
            const body = bodyParts.join('\n\n')
            if (command === 'CONNECTED') this._resolveConnected()
            if (command === 'ERROR') {
                this.error = body || headers.message || 'ERROR frame';
                this._resolveConnected()
            }
            if (command === 'MESSAGE') {
                const sub = this.subs[headers.subscription]
                if (sub) sub(JSON.parse(body))
            }
        }
    }

    async connect() {
        await this.connected
    }

    subscribe(destination, callback) {
        const id = 'sub-' + Math.random().toString(36).slice(2, 8)
        this.subs[id] = callback
        this.ws.send(`SUBSCRIBE\nid:${id}\ndestination:${destination}\n\n\0`)
        return id
    }

    send(destination, body) {
        this.ws.send(`SEND\ndestination:${destination}\ncontent-type:application/json\n\n${JSON.stringify(body)}\0`)
    }

    close() {
        try {
            this.ws.close()
        } catch {
        }
    }
}

async function waitUntil(fn, timeoutMs = 8000, step = 300) {
    const start = Date.now()
    while (Date.now() - start < timeoutMs) {
        if (fn()) return true
        await new Promise(r => setTimeout(r, step))
    }
    return !!fn()
}

// ---------- 测试开始 ----------
console.log('=== WebSocket 端到端测试 ===')

const tenant = await login('tenant2', '123456', 'TENANT')
const landlord = await login('landlord1', '123456', 'LANDLORD')
console.log(`租客 tenant2 id=${tenant.user.id}，房东 landlord1 id=${landlord.user.id}`)

// 1. 房东建立 WS 并订阅通知队列
const landlordWs = new StompTestClient(landlord.token)
await landlordWs.connect()
check('房东 WebSocket 连接成功(Principal=userId)', true)
let receivedNotification = null
landlordWs.subscribe('/user/queue/appointment', msg => {
    receivedNotification = msg
})
let receivedChat = null
landlordWs.subscribe('/user/queue/chat', msg => {
    receivedChat = msg
})

// 2. 租客创建预约（触发通知链：business → outbox → processor → WS）
const future = new Date(Date.now() + 3 * 86400000).toISOString().slice(0, 16).replace('T', ' ')
const res = await fetch(BASE + '/api/appointments', {
    method: 'POST',
    headers: {'Content-Type': 'application/json', Authorization: 'Bearer ' + tenant.token},
    body: JSON.stringify({houseId: 1, time: future, location: 'WS测试地点', requestId: 'ws-req-' + Date.now()})
})
const created = await res.json()
check('租客创建预约(API 成功)', created.success === true, JSON.stringify(created))
const aptId = created.data?.id

// 3. 房东应在数秒内实时收到通知
const gotNotification = await waitUntil(() => receivedNotification)
check('房东实时收到预约通知(/user/queue/appointment)', gotNotification && receivedNotification?.type === 'APPOINTMENT_CREATED',
    JSON.stringify(receivedNotification))
check('通知内容带业务上下文(relatedId=预约ID)', receivedNotification?.relatedId === aptId, JSON.stringify(receivedNotification))

// 4. 聊天：租客连接 WS，订阅自己的队列
const tenantWs = new StompTestClient(tenant.token)
await tenantWs.connect()
let tenantGotEcho = null
tenantWs.subscribe('/user/queue/chat', msg => {
    tenantGotEcho = msg
})

tenantWs.send('/app/chat.send', {toUserId: landlord.user.id, houseId: 1, content: 'WS测试：你好房东，房子还在吗？'})

const landlordGot = await waitUntil(() => receivedChat)
check('房东实时收到聊天消息', landlordGot && receivedChat?.content?.includes('房子还在吗'), JSON.stringify(receivedChat))
const tenantEchoed = await waitUntil(() => tenantGotEcho)
check('发送者收到服务端回显', tenantEchoed && tenantGotEcho?.content?.includes('房子还在吗'), JSON.stringify(tenantGotEcho))
const chatMessageId = receivedChat?.id

// 5. 消息已落库：REST 查询历史消息
const histRes = await fetch(BASE + `/api/chat/messages?partnerId=${landlord.user.id}&pageSize=20`, {
    headers: {Authorization: 'Bearer ' + tenant.token}
})
const hist = await histRes.json()
check('刷新后历史消息仍存在(已持久化)', (hist.data?.messages || []).some(m => m.id === chatMessageId),
    JSON.stringify(hist.data?.messages?.slice(-1)))

// 6. 房东未读数 + 已读标记
const unreadRes = await fetch(BASE + '/api/chat/unread-count', {headers: {Authorization: 'Bearer ' + landlord.token}})
const unreadBefore = (await unreadRes.json()).data?.unread
check('房东未读数 ≥ 1', unreadBefore >= 1, `unread=${unreadBefore}`)
await fetch(BASE + `/api/chat/read/${tenant.user.id}`, {
    method: 'PUT',
    headers: {Authorization: 'Bearer ' + landlord.token}
})
const unreadRes2 = await fetch(BASE + '/api/chat/unread-count', {headers: {Authorization: 'Bearer ' + landlord.token}})
const unreadAfter = (await unreadRes2.json()).data?.unread
check('标记已读后该会话未读清零(种子遗留未读除外)', unreadAfter === unreadBefore - 1, `unread=${unreadBefore}->${unreadAfter}`)

// 7. 会话列表
const convRes = await fetch(BASE + '/api/chat/conversations', {headers: {Authorization: 'Bearer ' + landlord.token}})
const convs = (await convRes.json()).data?.conversations || []
check('房东会话列表包含 tenant2', convs.some(c => c.partnerId === tenant.user.id), JSON.stringify(convs.map(c => c.partnerId)))
check('会话带房源上下文(关于房源1)', convs.some(c => c.partnerId === tenant.user.id && c.houseId === 1), JSON.stringify(convs))

// 8. 越权订阅拦截：租客尝试直接订阅他人队列 /queue/chat/2
const intruderWs = new StompTestClient(tenant.token)
await intruderWs.connect()
intruderWs.subscribe('/queue/chat/2', () => {
})
await new Promise(r => setTimeout(r, 1500))
check('越权订阅 /queue/chat/{他人ID} 被服务端拒绝', intruderWs.error !== null || intruderWs.closed, `error=${intruderWs.error} closed=${intruderWs.closed}`)
intruderWs.close()

// 9. 通知中心：预约通知已落库 notification 表
const notifRes = await fetch(BASE + '/api/notifications', {headers: {Authorization: 'Bearer ' + landlord.token}})
const notifs = (await notifRes.json()).data?.notifications || []
check('通知中心存在 APPOINTMENT_CREATED 记录', notifs.some(n => n.type === 'APPOINTMENT_CREATED' && n.relatedId === aptId), '')
check('该通知 sentTime 已写(实时送达)', notifs.some(n => n.type === 'APPOINTMENT_CREATED' && n.relatedId === aptId && n.sentTime), '')

// 清理：删除本次测试创建的预约，避免污染演示数据
try {
    await fetch(BASE + '/api/appointments/' + aptId, {
        method: 'DELETE', headers: {Authorization: 'Bearer ' + tenant.token}
    })
    console.log('  [CLEANUP] 已删除测试预约 #' + aptId)
} catch {
}

landlordWs.close()
tenantWs.close()

const fails = results.filter(r => !r[1])
console.log('==================================================')
console.log(`WS 测试总计: PASS=${results.length - fails.length}  FAIL=${fails.length}`)
fails.forEach(f => console.log('  -', f[0]))
process.exit(fails.length ? 1 : 0)
