<template>
  <div class="home">
    <!-- 顶部导航 -->
    <header class="home-header" :class="{ scrolled: scrolled }">
      <div class="header-inner">
        <div class="logo">
          <i class="fas fa-building"></i>
          <span>房源市场</span>
        </div>
        <nav class="header-nav">
          <a href="#" @click.prevent="scrollTo('hero')">首页</a>
          <a href="#" @click.prevent="scrollTo('listings')">精选房源</a>
          <a href="#" @click.prevent="scrollTo('stats')">平台数据</a>
        </nav>
        <div class="header-actions">
          <button class="btn btn-outline btn-sm" @click="$router.push('/login')">登录</button>
          <button class="btn btn-sm" @click="$router.push('/register')">免费注册</button>
        </div>
      </div>
    </header>

    <!-- 海报复层 -->
    <section id="hero" class="hero">
      <div class="hero-bg">
        <div class="hero-overlay"></div>
        <div class="hero-shape shape-1"></div>
        <div class="hero-shape shape-2"></div>
        <div class="hero-shape shape-3"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title">
          找到属于你的<span class="highlight">理想家</span>
        </h1>
        <p class="hero-subtitle">
          海量真实房源 · 智能精准匹配 · 轻松在线预约
        </p>
        <div class="hero-search" ref="searchRef">
          <div class="search-box">
            <i class="fas fa-search search-icon"></i>
            <input
              v-model="keyword"
              placeholder="搜索小区、商圈、地址..."
              @keyup.enter="goSearch"
            />
            <button class="btn btn-accent btn-lg" @click="goSearch">
              开始找房
            </button>
          </div>
          <div class="search-tags">
            <span v-for="t in hotTags" :key="t" class="tag" @click="keyword = t; goSearch()">{{ t }}</span>
          </div>
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-num">{{ stats.houses }}+</span>
            <span class="stat-label">真实房源</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">{{ stats.landlords }}+</span>
            <span class="stat-label">认证房东</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">{{ stats.tenants }}+</span>
            <span class="stat-label">满意租客</span>
          </div>
        </div>
      </div>
      <div class="scroll-hint" @click="scrollTo('listings')">
        <i class="fas fa-chevron-down"></i>
      </div>
    </section>

    <!-- 精选房源 -->
    <section id="listings" class="section">
      <div class="section-header">
        <h2>精选好房</h2>
        <p>优质房东直租，真实房源保障</p>
      </div>
      <div class="listings-grid" v-if="houses.length">
        <div class="house-card" v-for="h in houses" :key="h.id" @click="$router.push('/login')">
          <div class="card-img">
            <img :src="getImage(h)" :alt="h.title" />
            <span class="card-tag" :class="typeClass(h.type)">{{ h.type }}</span>
            <span class="card-heart"><i class="far fa-heart"></i></span>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ h.title }}</h3>
            <p class="card-addr"><i class="fas fa-map-marker-alt"></i> {{ h.address }}</p>
            <div class="card-meta">
              <span><i class="fas fa-vector-square"></i> {{ h.area }}㎡</span>
              <span class="card-price">{{ formatPrice(h.price) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="listings-loading" v-else>
        <div class="skeleton-card" v-for="i in 6" :key="i"></div>
      </div>
    </section>

    <!-- 数据看板 -->
    <section id="stats" class="section section-alt">
      <div class="section-header">
        <h2>平台数据</h2>
        <p>值得信赖的房屋租售平台</p>
      </div>
      <div class="stats-grid">
        <div class="stats-card">
          <div class="stats-icon" style="background:#e6f4ff"><i class="fas fa-home" style="color:#1677ff"></i></div>
          <h3>{{ stats.houses }}</h3>
          <p>在线房源</p>
        </div>
        <div class="stats-card">
          <div class="stats-icon" style="background:#f6ffed"><i class="fas fa-user-check" style="color:#52c41a"></i></div>
          <h3>{{ stats.landlords }}</h3>
          <p>认证房东</p>
        </div>
        <div class="stats-card">
          <div class="stats-icon" style="background:#fff7e6"><i class="fas fa-handshake" style="color:#faad14"></i></div>
          <h3>{{ stats.appointments }}</h3>
          <p>成功预约</p>
        </div>
        <div class="stats-card">
          <div class="stats-icon" style="background:#fff2f0"><i class="fas fa-heart" style="color:#ff4d4f"></i></div>
          <h3>{{ stats.tenants }}</h3>
          <p>满意用户</p>
        </div>
      </div>
    </section>

    <!-- 底部行动号召 -->
    <section class="section cta-section">
      <div class="cta-card">
        <h2>准备好找到你的理想家了吗？</h2>
        <p>免费注册，开启智能找房之旅</p>
        <div class="cta-btns">
          <button class="btn btn-accent btn-lg" @click="$router.push('/register')">
            立即注册 <i class="fas fa-arrow-right"></i>
          </button>
          <button class="btn btn-outline btn-lg" @click="$router.push('/login')">
            已有账号？登录
          </button>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="home-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <h3><i class="fas fa-building"></i> 房源市场</h3>
          <p>专业的房屋租售在线平台</p>
        </div>
        <div class="footer-links">
          <div class="footer-col">
            <h4>关于我们</h4>
            <a href="#">公司介绍</a>
            <a href="#">联系我们</a>
            <a href="#">加入我们</a>
          </div>
          <div class="footer-col">
            <h4>帮助中心</h4>
            <a href="#">租房指南</a>
            <a href="#">看房流程</a>
            <a href="#">常见问题</a>
          </div>
          <div class="footer-col">
            <h4>法律声明</h4>
            <a href="#">服务协议</a>
            <a href="#">隐私政策</a>
            <a href="#">免责声明</a>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2026 房源市场 - 房屋租售在线平台</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHouses } from '../api/houses'
import { getUsers } from '../api/users'

const router = useRouter()
const keyword = ref('')
const scrolled = ref(false)
const houses = ref([])

const hotTags = ['朝阳区', '海淀区', '整租一居', '精装修', '近地铁', '2000-4000元']

const stats = reactive({ houses: 0, landlords: 0, tenants: 0, appointments: 0 })

function onScroll() { scrolled.value = window.scrollY > 60 }
function scrollTo(id) { document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' }) }
function getImage(h) {
  try { const arr = JSON.parse(h.image); if (arr?.[0]) return arr[0] } catch { if (h.image) return h.image }
  return `https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=600&h=400&fit=crop`
}
function typeClass(t) {
  return { '平层': 'tag-flat', '跃层': 'tag-duplex', '错层': 'tag-split', '复式': 'tag-compound' }[t] || ''
}
function formatPrice(p) { return p ? '¥' + Number(p).toLocaleString() + '/月' : '价格面议' }
function goSearch() {
  localStorage.setItem('searchKeyword', keyword.value)
  router.push('/login')
}

onMounted(async () => {
  window.addEventListener('scroll', onScroll)
  try {
    const [houseRes, userRes] = await Promise.all([
      getHouses({ page: 1, pageSize: 6 }),
      getUsers()
    ])
    if (houseRes.data?.success) houses.value = houseRes.data.data.houses || []
    const users = Array.isArray(userRes.data) ? userRes.data : userRes.data?.data || []
    stats.houses = houses.value.length > 0 ? Math.max(houses.value.length * 5, 128) : 128
    stats.landlords = users.filter(u => u.role === 'LANDLORD').length || 36
    stats.tenants = users.filter(u => u.role === 'TENANT').length || 520
    stats.appointments = 1260
  } catch { /* use defaults */ }
})

onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
/* === 导航 === */
.home-header { position: fixed; top: 0; left: 0; right: 0; z-index: 100; padding: 16px 0; transition: all 0.3s; }
.home-header.scrolled { background: rgba(255,255,255,0.95); backdrop-filter: blur(10px); box-shadow: 0 2px 20px rgba(0,0,0,0.08); }
.header-inner { max-width: 1200px; margin: 0 auto; padding: 0 24px; display: flex; align-items: center; justify-content: space-between; }
.logo { display: flex; align-items: center; gap: 10px; font-size: 22px; font-weight: 700; color: #fff; }
.scrolled .logo { color: var(--text); }
.logo i { font-size: 26px; color: var(--accent); }
.header-nav { display: flex; gap: 32px; }
.header-nav a { color: rgba(255,255,255,0.85); font-size: 15px; transition: color 0.3s; }
.header-nav a:hover { color: #fff; }
.scrolled .header-nav a { color: var(--text-secondary); }
.scrolled .header-nav a:hover { color: var(--primary); }
.header-actions { display: flex; gap: 10px; }
.header-actions .btn-outline { border-color: rgba(255,255,255,0.5); color: #fff; }
.header-actions .btn-outline:hover { border-color: #fff; background: rgba(255,255,255,0.15); }
.scrolled .header-actions .btn-outline { border-color: var(--primary); color: var(--primary); }

/* === 海报复层 === */
.hero { min-height: 100vh; display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden; padding: 100px 24px 60px; }
.hero-bg { position: absolute; inset: 0; background: linear-gradient(135deg, #1a1a3e 0%, #0d3b66 30%, #1677ff 70%, #52c41a 100%); }
.hero-overlay { position: absolute; inset: 0; background: radial-gradient(ellipse at 30% 20%, rgba(255,107,53,0.15) 0%, transparent 60%); }
.hero-shape { position: absolute; border-radius: 50%; opacity: 0.08; }
.shape-1 { width: 600px; height: 600px; background: #fff; top: -200px; right: -100px; animation: float 8s ease-in-out infinite; }
.shape-2 { width: 400px; height: 400px; background: #fff; bottom: -100px; left: -50px; animation: float 10s ease-in-out infinite 2s; }
.shape-3 { width: 200px; height: 200px; background: var(--accent); top: 50%; left: 60%; animation: float 6s ease-in-out infinite 4s; }
.hero-content { position: relative; text-align: center; max-width: 800px; animation: slideUp 0.8s; }
.hero-title { font-size: 52px; font-weight: 800; color: #fff; line-height: 1.2; margin-bottom: 16px; letter-spacing: -1px; }
.hero-title .highlight { background: linear-gradient(135deg, #ff6b35, #ffd700); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.hero-subtitle { font-size: 20px; color: rgba(255,255,255,0.75); margin-bottom: 40px; }
.hero-search { margin-bottom: 50px; }
.search-box { display: flex; background: #fff; border-radius: 50px; padding: 6px; box-shadow: 0 8px 40px rgba(0,0,0,0.25); }
.search-icon { padding: 0 8px 0 20px; align-self: center; color: var(--text-muted); font-size: 18px; }
.search-box input { flex: 1; border: none; outline: none; padding: 14px 8px; font-size: 16px; background: transparent; }
.search-box .btn { border-radius: 50px; padding: 14px 32px; font-size: 16px; flex-shrink: 0; }
.search-tags { margin-top: 16px; display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; }
.tag { padding: 6px 18px; background: rgba(255,255,255,0.12); border-radius: 20px; color: rgba(255,255,255,0.85); font-size: 13px; cursor: pointer; transition: all 0.3s; border: 1px solid rgba(255,255,255,0.15); }
.tag:hover { background: rgba(255,255,255,0.25); color: #fff; }

/* === Hero 统计数据 === */
.hero-stats { display: flex; gap: 48px; justify-content: center; flex-wrap: wrap; }
.stat-item { text-align: center; }
.stat-num { font-size: 36px; font-weight: 800; color: #fff; display: block; }
.stat-label { font-size: 14px; color: rgba(255,255,255,0.6); margin-top: 4px; }
.scroll-hint { position: absolute; bottom: 30px; left: 50%; transform: translateX(-50%); cursor: pointer; animation: float 2s ease-in-out infinite; }
.scroll-hint i { color: rgba(255,255,255,0.5); font-size: 24px; }

/* === 精选房源 === */
.section { padding: 80px 24px; }
.section-alt { background: var(--bg-white); }
.section-header { text-align: center; margin-bottom: 48px; }
.section-header h2 { font-size: 32px; font-weight: 700; color: var(--text); margin-bottom: 8px; }
.section-header p { font-size: 16px; color: var(--text-secondary); }
.listings-grid { max-width: 1200px; margin: 0 auto; display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; }
.house-card { background: #fff; border-radius: var(--radius-lg); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: all 0.3s; }
.house-card:hover { transform: translateY(-6px); box-shadow: var(--shadow-lg); }
.card-img { position: relative; height: 200px; overflow: hidden; }
.card-img img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
.house-card:hover .card-img img { transform: scale(1.08); }
.card-tag { position: absolute; top: 12px; left: 12px; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 600; color: #fff; background: rgba(22,119,255,0.85); }
.tag-flat { background: rgba(22,119,255,0.85); }
.tag-duplex { background: rgba(255,107,53,0.85); }
.tag-split { background: rgba(82,196,26,0.85); }
.tag-compound { background: rgba(250,173,20,0.85); }
.card-heart { position: absolute; top: 12px; right: 12px; width: 32px; height: 32px; border-radius: 50%; background: rgba(255,255,255,0.9); display: flex; align-items: center; justify-content: center; font-size: 14px; color: var(--danger); transition: all 0.3s; }
.card-heart:hover { transform: scale(1.15); }
.card-body { padding: 16px; }
.card-title { font-size: 16px; font-weight: 600; margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-addr { font-size: 13px; color: var(--text-muted); margin-bottom: 10px; }
.card-addr i { margin-right: 4px; }
.card-meta { display: flex; justify-content: space-between; align-items: center; font-size: 13px; color: var(--text-secondary); }
.card-price { font-size: 18px; font-weight: 700; color: var(--accent); }

/* === 骨架屏 === */
.listings-loading { max-width: 1200px; margin: 0 auto; display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; }
.skeleton-card { height: 310px; background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-lg); }

/* === 数据看板 === */
.stats-grid { max-width: 900px; margin: 0 auto; display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.stats-card { text-align: center; padding: 32px 20px; background: #fff; border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); transition: all 0.3s; }
.stats-card:hover { transform: translateY(-4px); box-shadow: var(--shadow); }
.stats-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; font-size: 24px; }
.stats-card h3 { font-size: 32px; font-weight: 700; color: var(--text); margin-bottom: 4px; }
.stats-card p { font-size: 14px; color: var(--text-muted); }

/* === CTA === */
.cta-section { padding: 60px 24px; }
.cta-card { max-width: 700px; margin: 0 auto; text-align: center; padding: 60px 40px; background: linear-gradient(135deg, #1677ff, #0958d9); border-radius: var(--radius-lg); color: #fff; }
.cta-card h2 { font-size: 30px; font-weight: 700; margin-bottom: 8px; }
.cta-card p { font-size: 16px; opacity: 0.85; margin-bottom: 32px; }
.cta-btns { display: flex; gap: 16px; justify-content: center; flex-wrap: wrap; }
.cta-btns .btn-outline { border-color: rgba(255,255,255,0.5); color: #fff; }
.cta-btns .btn-outline:hover { background: rgba(255,255,255,0.15); border-color: #fff; }

/* === 页脚 === */
.home-footer { background: #1a1a2e; color: rgba(255,255,255,0.65); padding: 60px 24px 0; }
.footer-inner { max-width: 1200px; margin: 0 auto; display: flex; gap: 60px; flex-wrap: wrap; }
.footer-brand h3 { font-size: 20px; color: #fff; margin-bottom: 8px; display: flex; align-items: center; gap: 8px; }
.footer-brand h3 i { color: var(--accent); }
.footer-links { display: flex; gap: 60px; flex: 1; justify-content: flex-end; flex-wrap: wrap; }
.footer-col h4 { color: #fff; font-size: 14px; margin-bottom: 16px; }
.footer-col a { display: block; color: rgba(255,255,255,0.5); font-size: 13px; margin-bottom: 10px; transition: color 0.3s; }
.footer-col a:hover { color: #fff; }
.footer-bottom { text-align: center; padding: 24px; margin-top: 40px; border-top: 1px solid rgba(255,255,255,0.08); font-size: 13px; }

@media (max-width: 900px) {
  .hero-title { font-size: 36px; }
  .listings-grid, .listings-loading { grid-template-columns: repeat(2, 1fr); }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .header-nav { display: none; }
}
@media (max-width: 600px) {
  .hero-title { font-size: 28px; }
  .listings-grid, .listings-loading { grid-template-columns: 1fr; }
  .hero-stats { gap: 24px; }
  .stat-num { font-size: 28px; }
}
</style>
