<template>
  <div class="home-page">
    <header class="topbar">
      <div class="topbar-inner">
        <div class="brand">
          <span class="brand-mark">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round"><path
                d="M3 9l1.5-5.5A1.5 1.5 0 0 1 5.97 2.5h12.06a1.5 1.5 0 0 1 1.47 1L21 9"/><path
                d="M3 9a3 3 0 0 0 6 0 3 3 0 0 0 6 0 3 3 0 0 0 6 0"/><path d="M4 9v11a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V9"/><path
                d="M9 21v-6h6v6"/></svg>
          </span>
          <span class="brand-name">房源市场</span>
        </div>
        <nav class="top-nav">
          <a href="#" @click.prevent="scrollTo('showcase')">精选好房</a>
          <a href="#" @click.prevent="setType('平层')">平层</a>
          <a href="#" @click.prevent="setType('跃层')">LOFT</a>
          <a href="#" @click.prevent="setType('复式')">复式</a>
        </nav>
        <div class="top-actions">
          <RouterLink to="/login" class="link-login">登录</RouterLink>
          <RouterLink to="/register" class="btn-register">免费注册</RouterLink>
        </div>
      </div>
      <div class="scroll-progress" :style="{ transform: `scaleX(${scrollProgress})` }"></div>
    </header>

    <section class="hero">
      <div class="hero-media" :style="heroStyle">
        <img :src="'/backgrounds/beijing-hero.png'" alt="北京城市房源景观"/>
      </div>
      <div class="hero-overlay"></div>
      <div class="hero-content" v-reveal>
        <p class="hero-eyebrow">BEIJING · 2026</p>
        <h1>在北京，找到你的<br/><span class="gradient-text">理想家</span></h1>
        <p class="hero-sub">整租、合租、LOFT，真实房源实时更新，看房预约一路丝滑</p>
        <div class="search-panel">
          <div class="search-field">
            <label>区域</label>
            <select v-model="filters.district">
              <option value="">全部区域</option>
              <option v-for="d in districts" :key="d">{{ d }}</option>
            </select>
          </div>
          <div class="search-field">
            <label>户型</label>
            <select v-model="filters.type">
              <option value="">全部户型</option>
              <option v-for="t in types" :key="t">{{ t }}</option>
            </select>
          </div>
          <div class="search-field">
            <label>预算</label>
            <select v-model="filters.priceRange">
              <option value="">不限</option>
              <option v-for="r in priceRanges" :key="r.value" :value="r.value">{{ r.label }}</option>
            </select>
          </div>
          <div class="search-keyword">
            <input v-model="filters.keyword" placeholder="小区、地址或关键词" @keyup.enter="search"/>
          </div>
          <button class="search-btn" @click="search">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            找房
          </button>
        </div>
        <div class="quick-tags">
          <span v-for="tag in quickTags" :key="tag" @click="quickSearch(tag)">{{ tag }}</span>
        </div>
      </div>
      <div class="scroll-cue" @click="scrollTo('showcase')">
        <span></span>
      </div>
    </section>

    <section class="stats-band" v-reveal>
      <div class="stats-inner">
        <div class="stat-item" v-reveal="{ delay: 0 }">
          <strong>{{ stats.houses }}</strong>
          <span>在线房源</span>
        </div>
        <div class="stat-item" v-reveal="{ delay: 80 }">
          <strong>{{ stats.landlords }}</strong>
          <span>认证房东</span>
        </div>
        <div class="stat-item" v-reveal="{ delay: 160 }">
          <strong>{{ stats.tenants }}</strong>
          <span>注册租客</span>
        </div>
        <div class="stat-item" v-reveal="{ delay: 240 }">
          <strong>{{ stats.appointments }}</strong>
          <span>预约单量</span>
        </div>
      </div>
    </section>

    <section class="showcase" id="showcase">
      <div class="showcase-head">
        <div>
          <p class="section-kicker">CURATED HOMES</p>
          <h2>精选好房，滑动遇见</h2>
          <p class="section-sub">横向浏览当前热门房源，让每一次滚动都有衔接</p>
        </div>
        <div class="showcase-nav">
          <button class="btn-icon" @click="scrollShowcase(-1)" title="上一组">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="15 18 9 12 15 6"/>
            </svg>
          </button>
          <button class="btn-icon" @click="scrollShowcase(1)" title="下一组">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </button>
        </div>
      </div>
      <div class="showcase-track" ref="showcaseTrack" v-if="houses.length">
        <article class="showcase-card" v-for="(h, i) in houses" :key="h.id" @click="goHouse(h.id)"
                 v-reveal="{ delay: i * 60 }">
          <div class="showcase-media">
            <img :src="getImage(h)" :alt="h.title" loading="lazy"/>
            <span class="showcase-index">0{{ i + 1 }}</span>
            <span class="card-type">{{ h.type }}</span>
          </div>
          <div class="showcase-body">
            <div class="showcase-price">{{ formatPrice(h.price) }}</div>
            <h3>{{ h.title }}</h3>
            <p>{{ h.community ? h.community + ' · ' : '' }}{{ h.address }}</p>
            <div class="showcase-specs">
              <span>{{ h.layout || h.type || '未分类' }}</span>
              <span>{{ h.area }}㎡</span>
              <span>{{ h.orientation }}</span>
              <span v-if="h.subwayDistance">{{ h.subwayDistance }}</span>
            </div>
          </div>
        </article>
      </div>
      <div class="showcase-empty" v-else>
        <p>精选房源加载中…</p>
      </div>
    </section>

    <main class="page-main" id="listings">
      <div class="section-head" v-reveal>
        <div>
          <p class="section-kicker">NEW LISTINGS</p>
          <h2>{{ filters.type || '最新房源' }}</h2>
          <p v-if="total">共找到 {{ total }} 套房源</p>
        </div>
        <div class="type-chips">
          <button :class="{ active: filters.type === '' }" @click="setType('')">全部</button>
          <button v-for="t in types" :key="t" :class="{ active: filters.type === t }" @click="setType(t)">{{
              t
            }}
          </button>
        </div>
      </div>

      <div v-if="loading" class="listing-grid">
        <div v-for="i in 8" :key="i" class="skeleton-card" v-reveal="{ delay: i * 50 }"></div>
      </div>

      <div v-else-if="houses.length" class="listing-grid">
        <article class="home-card" v-for="(h, i) in houses" :key="h.id" @click="goHouse(h.id)"
                 v-reveal="{ delay: (i % 4) * 70 }">
          <div class="home-card-img">
            <img :src="getImage(h)" :alt="h.title" loading="lazy"/>
            <span class="card-type">{{ h.type }}</span>
            <span class="card-district">{{ h.district }}</span>
            <div class="card-shine"></div>
          </div>
          <div class="home-card-body">
            <h3>{{ h.title }}</h3>
            <div class="card-price">{{ formatPrice(h.price) }}</div>
            <div class="card-specs">
              <span>{{ h.layout || h.type || '未分类' }}</span>
              <span>{{ h.area }}㎡</span>
              <span>{{ h.orientation }}</span>
              <span>{{ h.floor || '楼层待定' }}</span>
              <span v-if="h.subwayDistance">{{ h.subwayDistance }}</span>
            </div>
            <div class="card-tags" v-if="parseTags(h).length">
              <span v-for="t in parseTags(h).slice(0, 3)" :key="t">{{ t }}</span>
            </div>
            <div class="card-address">{{ h.community ? h.community + ' · ' : '' }}{{ h.address }}</div>
            <div class="card-cta">查看房源
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                   stroke-linecap="round" stroke-linejoin="round">
                <line x1="5" y1="12" x2="19" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="empty-state" v-reveal>
        <p>没有找到符合条件的房源</p>
        <button @click="resetFilters">重置筛选</button>
      </div>
      <AppPagination
          v-if="!loading && total > pageSize"
          :current="page"
          :total="total"
          :page-size="pageSize"
          @change="changePage"
      />
    </main>

    <section class="journey">
      <div class="journey-inner" v-reveal>
        <p class="section-kicker">FULL FLOW</p>
        <h2>从发布到看房，一个闭环</h2>
        <div class="journey-steps">
          <div class="journey-step">
            <span class="step-index">01</span>
            <h3>房东发布</h3>
            <p>真实房源一键上线</p>
          </div>
          <div class="journey-line"></div>
          <div class="journey-step">
            <span class="step-index">02</span>
            <h3>租客预约</h3>
            <p>在线选择看房时间</p>
          </div>
          <div class="journey-line"></div>
          <div class="journey-step">
            <span class="step-index">03</span>
            <h3>房东审批</h3>
            <p>流程轨迹全程可溯</p>
          </div>
          <div class="journey-line"></div>
          <div class="journey-step">
            <span class="step-index">04</span>
            <h3>实时通知</h3>
            <p>结果即时送达双方</p>
          </div>
        </div>
      </div>
    </section>

    <footer class="home-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <span class="brand-mark">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round"><path
                d="M3 9l1.5-5.5A1.5 1.5 0 0 1 5.97 2.5h12.06a1.5 1.5 0 0 1 1.47 1L21 9"/><path
                d="M3 9a3 3 0 0 0 6 0 3 3 0 0 0 6 0 3 3 0 0 0 6 0"/><path d="M4 9v11a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1V9"/><path
                d="M9 21v-6h6v6"/></svg>
          </span>
          <span>房源市场</span>
        </div>
        <p>北京租房信息聚合平台</p>
      </div>
      <div class="footer-bottom">© 2026 房源市场</div>
    </footer>
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, onUnmounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {getPublicHouses, getPublicStats} from '../api/houses'
import {formatPrice} from '../composables/useFormat'
import AppPagination from '../components/AppPagination.vue'

const router = useRouter()
const loading = ref(true)
const houses = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(8)
const stats = reactive({houses: 0, landlords: 0, tenants: 0, appointments: 0})
const scrollY = ref(0)
const showcaseTrack = ref(null)

const districts = ['朝阳区', '海淀区', '西城区', '东城区', '丰台区', '通州区', '大兴区']
const types = ['平层', '跃层', '错层', '复式']
const priceRanges = [
  {label: '2000元以下', value: '0-2000'},
  {label: '2000-4000元', value: '2000-4000'},
  {label: '4000-6000元', value: '4000-6000'},
  {label: '6000-10000元', value: '6000-10000'},
  {label: '10000元以上', value: '10000-999999'}
]
const quickTags = ['朝阳区', '海淀区', '望京', '国贸', '复式', '五道口']
const filters = reactive({keyword: '', district: '', type: '', priceRange: ''})

const heroStyle = computed(() => ({
  transform: `translate3d(0, ${scrollY.value * 0.22}px, 0) scale(1.1)`
}))

const scrollProgress = computed(() => {
  const max = document.documentElement.scrollHeight - window.innerHeight
  return max > 0 ? Math.min(scrollY.value / max, 1) : 0
})

function onScroll() {
  scrollY.value = window.scrollY
}

function getImage(h) {
  if (h.coverImage) return h.coverImage
  const first = h.images?.[0]
  return typeof first === 'string' ? first : first?.imageUrl || ''
}

function parseTags(h) {
  if (Array.isArray(h.tags)) return h.tags
  try {
    const arr = JSON.parse(h.tags || '[]')
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

function scrollTo(id) {
  document.getElementById(id)?.scrollIntoView({behavior: 'smooth'})
}

function goHouse(id) {
  router.push(`/house/${id}`)
}

async function fetchHouses() {
  loading.value = true
  const [minPrice, maxPrice] = filters.priceRange ? filters.priceRange.split('-').map(Number) : [undefined, undefined]
  try {
    const res = await getPublicHouses({
      page: page.value,
      pageSize: pageSize.value,
      keyword: filters.keyword || undefined,
      district: filters.district || undefined,
      type: filters.type || undefined,
      minPrice,
      maxPrice
    })
    houses.value = res.data?.data?.houses || []
    total.value = res.data?.data?.total || 0
    await nextTick()
    window.requestAnimationFrame(() => window.dispatchEvent(new Event('scroll')))
  } catch {
    houses.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    const res = await getPublicStats()
    Object.assign(stats, res.data?.data || {})
  } catch { /* ignore */
  }
}

function search() {
  page.value = 1
  fetchHouses()
  scrollTo('listings')
}

function quickSearch(tag) {
  if (districts.includes(tag)) {
    filters.district = tag
    filters.keyword = ''
  } else if (types.includes(tag)) {
    filters.type = tag
    filters.keyword = ''
  } else {
    filters.keyword = tag
  }
  search()
}

function setType(type) {
  filters.type = type
  page.value = 1
  fetchHouses()
}

function resetFilters() {
  filters.keyword = ''
  filters.district = ''
  filters.type = ''
  filters.priceRange = ''
  page.value = 1
  fetchHouses()
}

function changePage(nextPage) {
  page.value = nextPage
  fetchHouses()
  document.getElementById('listings')?.scrollIntoView({behavior: 'smooth'})
}

function scrollShowcase(dir) {
  if (!showcaseTrack.value) return
  showcaseTrack.value.scrollBy({left: dir * 360, behavior: 'smooth'})
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, {passive: true})
  fetchHouses()
  loadStats()
})

onUnmounted(() => window.removeEventListener('scroll', onScroll))
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f4f7fc;
  overflow-x: hidden;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(20px) saturate(170%);
  -webkit-backdrop-filter: blur(20px) saturate(170%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.6);
}

.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.32);
}

.brand-name {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.5px;
  color: var(--text);
}

.top-nav {
  display: flex;
  gap: 26px;
}

.top-nav a {
  color: #4b5563;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  position: relative;
}

.top-nav a::after {
  content: "";
  position: absolute;
  left: 0;
  right: 100%;
  bottom: -4px;
  height: 2px;
  border-radius: 2px;
  background: linear-gradient(90deg, #1677ff, #06b6d4);
  transition: right 0.28s var(--ease-spring);
}

.top-nav a:hover::after {
  right: 0;
}

.top-nav a:hover {
  color: #1677ff;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.link-login {
  color: #1677ff;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
}

.btn-register {
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  color: #fff;
  padding: 9px 20px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  box-shadow: 0 8px 20px rgba(22, 119, 255, 0.26);
  transition: all var(--transition);
}

.btn-register:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 26px rgba(22, 119, 255, 0.32);
}

.scroll-progress {
  position: absolute;
  left: 0;
  bottom: -1px;
  height: 3px;
  width: 100%;
  transform-origin: left;
  background: linear-gradient(90deg, #1677ff, #06b6d4, #8b5cf6, #ec4899);
  border-radius: 0 3px 3px 0;
}

.hero {
  position: relative;
  min-height: 680px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 96px 24px 92px;
  overflow: hidden;
  isolation: isolate;
}

.hero::before {
  content: "";
  position: absolute;
  inset: -20%;
  z-index: -2;
  background: linear-gradient(120deg, rgba(22, 119, 255, 0.55), rgba(6, 182, 212, 0.25) 36%, rgba(139, 92, 246, 0.3) 64%, rgba(236, 72, 153, 0.28)),
  #081c33;
  animation: aurora-drift 18s ease-in-out infinite alternate;
}

.hero-media {
  position: absolute;
  inset: -12% 0;
  z-index: -3;
  overflow: hidden;
}

.hero-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: saturate(1.12) contrast(1.04);
}

.hero-overlay {
  position: absolute;
  inset: 0;
  z-index: -1;
  background: linear-gradient(180deg, rgba(6, 20, 40, 0.28) 0%, rgba(6, 20, 40, 0.5) 52%, rgba(6, 20, 40, 0.78) 100%),
  linear-gradient(90deg, rgba(6, 20, 40, 0.38), transparent 70%);
}

.hero-content {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 1120px;
  color: #fff;
}

.hero-eyebrow {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 4px;
  color: rgba(255, 255, 255, 0.72);
  margin-bottom: 14px;
}

.hero h1 {
  font-size: clamp(38px, 6vw, 68px);
  font-weight: 800;
  line-height: 1.12;
  margin-bottom: 16px;
  letter-spacing: 0;
  text-shadow: 0 6px 30px rgba(0, 0, 0, 0.24);
}

.hero-sub {
  font-size: 17px;
  color: rgba(255, 255, 255, 0.84);
  margin-bottom: 34px;
  max-width: 560px;
}

.search-panel {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1.6fr auto;
  gap: 12px;
  align-items: end;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 18px;
  padding: 18px;
  box-shadow: 0 30px 70px rgba(0, 0, 0, 0.28);
  backdrop-filter: blur(16px);
}

.search-field label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 6px;
}

.search-field select,
.search-keyword input {
  width: 100%;
  height: 44px;
  padding: 0 13px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  color: #111827;
  font-size: 14px;
  outline: none;
  transition: all var(--transition);
}

.search-field select:focus,
.search-keyword input:focus {
  border-color: #1677ff;
  box-shadow: 0 0 0 4px rgba(22, 119, 255, 0.12);
}

.search-keyword input {
  background: #fff;
}

.search-btn {
  height: 44px;
  padding: 0 28px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #ff6b35, #f43f5e);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 12px 26px rgba(244, 63, 94, 0.32);
  transition: all var(--transition);
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(244, 63, 94, 0.4);
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.quick-tags span {
  padding: 7px 16px;
  border: 1px solid rgba(255, 255, 255, 0.32);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  backdrop-filter: blur(8px);
  transition: all var(--transition);
}

.quick-tags span:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.scroll-cue {
  position: absolute;
  left: 50%;
  bottom: 26px;
  width: 28px;
  height: 46px;
  border: 2px solid rgba(255, 255, 255, 0.72);
  border-radius: 20px;
  cursor: pointer;
  transform: translateX(-50%);
}

.scroll-cue span {
  position: absolute;
  top: 8px;
  left: 50%;
  width: 4px;
  height: 9px;
  border-radius: 4px;
  background: #fff;
  transform: translateX(-50%);
  animation: cue-drop 1.8s ease-in-out infinite;
}

@keyframes cue-drop {
  0% {
    transform: translate(-50%, 0);
    opacity: 1;
  }
  70% {
    transform: translate(-50%, 16px);
    opacity: 0;
  }
  100% {
    transform: translate(-50%, 0);
    opacity: 0;
  }
}

.stats-band {
  background: #fff;
  border-bottom: 1px solid #eef2f7;
  position: relative;
  z-index: 3;
}

.stats-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 24px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 8px 0;
}

.stat-item strong {
  display: block;
  font-size: 34px;
  font-weight: 800;
  background: linear-gradient(135deg, #1677ff, #06b6d4, #8b5cf6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

.stat-item span {
  font-size: 13px;
  color: #6b7280;
}

.showcase {
  max-width: 1200px;
  margin: 0 auto;
  padding: 72px 24px 48px;
}

.showcase-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 28px;
}

.section-kicker {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 3px;
  color: #06b6d4;
  margin-bottom: 6px;
}

.showcase-head h2,
.section-head h2,
.journey h2 {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 0;
  color: var(--text);
}

.section-sub {
  color: #6b7280;
  font-size: 14px;
  margin-top: 6px;
}

.showcase-nav {
  display: flex;
  gap: 10px;
}

.showcase-track {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: minmax(300px, 1fr);
  gap: 20px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  scrollbar-width: none;
  padding: 8px 4px 20px;
}

.showcase-track::-webkit-scrollbar {
  display: none;
}

.showcase-card {
  scroll-snap-align: start;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 12px 36px rgba(15, 23, 42, 0.1);
  cursor: pointer;
  transition: transform 0.4s var(--ease-spring), box-shadow 0.4s var(--ease-spring);
}

.showcase-card:hover {
  transform: translateY(-8px) rotateX(1.5deg);
  box-shadow: 0 28px 60px rgba(15, 23, 42, 0.16);
}

.showcase-media {
  position: relative;
  aspect-ratio: 16 / 11;
  overflow: hidden;
}

.showcase-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s var(--ease-spring);
}

.showcase-card:hover .showcase-media img {
  transform: scale(1.08);
}

.showcase-index {
  position: absolute;
  top: 14px;
  right: 14px;
  font-size: 12px;
  font-weight: 800;
  color: #fff;
  background: rgba(0, 0, 0, 0.4);
  padding: 4px 10px;
  border-radius: 999px;
  backdrop-filter: blur(6px);
}

.card-type {
  position: absolute;
  top: 14px;
  left: 14px;
  background: linear-gradient(135deg, #ff6b35, #f43f5e);
  color: #fff;
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  box-shadow: 0 8px 18px rgba(244, 63, 94, 0.3);
}

.showcase-body {
  padding: 18px 20px 22px;
}

.showcase-price {
  font-size: 22px;
  font-weight: 800;
  background: linear-gradient(135deg, #ff6b35, #f43f5e);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  margin-bottom: 6px;
}

.showcase-body h3 {
  font-size: 17px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.showcase-body p {
  font-size: 13px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 12px;
}

.showcase-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.showcase-specs span {
  font-size: 12px;
  color: #4b5563;
  background: #f3f4f6;
  padding: 4px 10px;
  border-radius: 8px;
}

.showcase-empty {
  padding: 60px 20px;
  text-align: center;
  color: #9ca3af;
}

.page-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 64px 24px 72px;
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 28px;
}

.section-head p {
  color: #6b7280;
  font-size: 13px;
  margin-top: 4px;
}

.type-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.type-chips button {
  padding: 8px 18px;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  color: #4b5563;
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition);
}

.type-chips button:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
}

.type-chips button.active {
  border-color: transparent;
  background: linear-gradient(135deg, #1677ff, #06b6d4);
  color: #fff;
  box-shadow: 0 10px 22px rgba(22, 119, 255, 0.26);
}

.listing-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 22px;
}

.home-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.08);
  cursor: pointer;
  transition: transform 0.45s var(--ease-spring), box-shadow 0.45s var(--ease-spring);
  position: relative;
}

.home-card:hover {
  transform: translateY(-7px);
  box-shadow: 0 24px 50px rgba(15, 23, 42, 0.15);
}

.home-card-img {
  position: relative;
  aspect-ratio: 16 / 10;
  overflow: hidden;
}

.home-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s var(--ease-spring), filter 0.5s ease;
}

.home-card:hover .home-card-img img {
  transform: scale(1.08);
  filter: saturate(1.12);
}

.card-shine {
  position: absolute;
  inset: 0;
  background: linear-gradient(115deg, transparent 30%, rgba(255, 255, 255, 0.28) 46%, transparent 62%);
  transform: translateX(-120%);
  transition: transform 0.9s ease;
  pointer-events: none;
}

.home-card:hover .card-shine {
  transform: translateX(120%);
}

.card-district {
  position: absolute;
  right: 12px;
  bottom: 12px;
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 12px;
  color: #fff;
  background: rgba(0, 0, 0, 0.46);
  backdrop-filter: blur(8px);
}

.home-card-body {
  padding: 16px;
}

.home-card-body h3 {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 6px;
}

.card-price {
  color: #f43f5e;
  font-size: 20px;
  font-weight: 800;
  margin-bottom: 8px;
}

.card-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.card-specs span {
  font-size: 12px;
  color: #4b5563;
  background: #f3f4f6;
  border: 1px solid #eef2f7;
  border-radius: 8px;
  padding: 4px 9px;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.card-tags span {
  font-size: 12px;
  color: #1677ff;
  background: #eff6ff;
  border-radius: 8px;
  padding: 3px 9px;
}

.card-address {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-cta {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #1677ff;
  opacity: 0;
  transform: translateY(6px);
  transition: all 0.4s var(--ease-spring);
}

.home-card:hover .card-cta {
  opacity: 1;
  transform: translateY(0);
}

.skeleton-card {
  aspect-ratio: 4 / 5;
  border-radius: 16px;
  background: linear-gradient(90deg, #eef2f7 25%, #e2e8f0 50%, #eef2f7 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.empty-state {
  padding: 80px 20px;
  text-align: center;
  color: #6b7280;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 18px;
  box-shadow: 0 12px 36px rgba(15, 23, 42, 0.08);
}

.empty-state button {
  margin-top: 16px;
  padding: 9px 22px;
  border: 1px solid #1677ff;
  border-radius: 999px;
  background: #fff;
  color: #1677ff;
  cursor: pointer;
  transition: all var(--transition);
}

.empty-state button:hover {
  background: #eff6ff;
}

.journey {
  background: linear-gradient(120deg, #0b1f3f 0%, #123b75 45%, #5b21b6 100%);
  color: #fff;
  padding: 80px 24px;
}

.journey-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.journey .section-kicker {
  color: #67e8f9;
}

.journey h2 {
  color: #fff;
  margin-bottom: 44px;
}

.journey-steps {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.journey-step {
  flex: 1;
  text-align: center;
}

.step-index {
  display: inline-flex;
  width: 54px;
  height: 54px;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  font-size: 15px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.24);
  color: #fff;
  margin-bottom: 16px;
  backdrop-filter: blur(10px);
}

.journey-step h3 {
  font-size: 17px;
  font-weight: 700;
  margin-bottom: 6px;
}

.journey-step p {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.journey-line {
  flex: 0 0 48px;
  height: 2px;
  margin-top: 27px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.5), rgba(103, 232, 249, 0.7));
}

.home-footer {
  background: #0b1f3f;
  color: rgba(255, 255, 255, 0.72);
  padding: 36px 24px 22px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 22px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 800;
  color: #fff;
}

.footer-brand .brand-mark {
  width: 30px;
  height: 30px;
}

.footer-inner p {
  font-size: 13px;
}

.footer-bottom {
  max-width: 1200px;
  margin: 16px auto 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.42);
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

@media (max-width: 1024px) {
  .listing-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .search-panel {
    grid-template-columns: 1fr 1fr 1fr;
  }

  .search-keyword,
  .search-btn {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .top-nav {
    display: none;
  }

  .hero {
    min-height: 580px;
    padding: 72px 16px 84px;
  }

  .hero h1 {
    font-size: 38px;
  }

  .search-panel {
    grid-template-columns: 1fr 1fr;
  }

  .search-keyword,
  .search-btn {
    grid-column: span 2;
  }

  .stats-inner {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .listing-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .journey-steps {
    flex-direction: column;
    gap: 26px;
  }

  .journey-line {
    display: none;
  }

  .journey-step {
    display: flex;
    align-items: center;
    gap: 16px;
    text-align: left;
  }

  .step-index {
    margin: 0;
    flex-shrink: 0;
  }
}

@media (max-width: 520px) {
  .listing-grid {
    grid-template-columns: 1fr;
  }

  .search-panel {
    grid-template-columns: 1fr;
  }

  .search-keyword,
  .search-btn {
    grid-column: auto;
  }

  .showcase-track {
    grid-auto-columns: 82%;
  }

  .footer-inner {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
