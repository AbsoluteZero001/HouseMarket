<template>
  <div class="home-page">
    <header class="topbar">
      <div class="topbar-inner">
        <div class="brand">
          <span class="brand-mark">房</span>
          <span class="brand-name">房源市场</span>
        </div>
        <nav class="top-nav">
          <a href="#" @click.prevent="scrollTo('listings')">找房</a>
          <a href="#" @click.prevent="setType('平层')">整租</a>
          <a href="#" @click.prevent="setType('跃层')">LOFT</a>
          <a href="#" @click.prevent="setType('复式')">复式</a>
        </nav>
        <div class="top-actions">
          <RouterLink to="/login" class="link-login">登录</RouterLink>
          <RouterLink to="/register" class="btn-register">免费注册</RouterLink>
        </div>
      </div>
    </header>

    <section class="hero">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1>在北京，找到你的理想家</h1>
        <p class="hero-sub">整租、合租、LOFT，真实房源实时更新</p>
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
          <button class="search-btn" @click="search">找房</button>
        </div>
        <div class="quick-tags">
          <span v-for="tag in quickTags" :key="tag" @click="quickSearch(tag)">{{ tag }}</span>
        </div>
      </div>
    </section>

    <section class="stats-band">
      <div class="stats-inner">
        <div class="stat-item">
          <strong>{{ stats.houses }}</strong>
          <span>在线房源</span>
        </div>
        <div class="stat-item">
          <strong>{{ stats.landlords }}</strong>
          <span>认证房东</span>
        </div>
        <div class="stat-item">
          <strong>{{ stats.tenants }}</strong>
          <span>注册租客</span>
        </div>
        <div class="stat-item">
          <strong>{{ stats.appointments }}</strong>
          <span>预约单量</span>
        </div>
      </div>
    </section>

    <main class="page-main" id="listings">
      <div class="section-head">
        <div>
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
        <div v-for="i in 8" :key="i" class="skeleton-card"></div>
      </div>

      <div v-else-if="houses.length" class="listing-grid">
        <article class="home-card" v-for="h in houses" :key="h.id" @click="goHouse(h.id)">
          <div class="home-card-img">
            <img :src="getImage(h)" :alt="h.title" loading="lazy"/>
            <span class="card-type">{{ h.type }}</span>
            <span class="card-district">{{ h.district }}</span>
          </div>
          <div class="home-card-body">
            <h3>{{ h.title }}</h3>
            <div class="card-price">{{ formatPrice(h.price) }}</div>
            <div class="card-specs">
              <span>{{ h.bedrooms || 1 }}室{{ h.bathrooms || 1 }}卫</span>
              <span>{{ h.area }}㎡</span>
              <span>{{ h.orientation }}</span>
              <span>{{ h.floor || '楼层待定' }}</span>
            </div>
            <div class="card-tags" v-if="parseTags(h).length">
              <span v-for="t in parseTags(h).slice(0, 3)" :key="t">{{ t }}</span>
            </div>
            <div class="card-address">{{ h.address }}</div>
          </div>
        </article>
      </div>

      <div v-else class="empty-state">
        <p>没有找到符合条件的房源</p>
        <button @click="resetFilters">重置筛选</button>
      </div>
    </main>

    <footer class="home-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <span class="brand-mark">房</span>
          <span>房源市场</span>
        </div>
        <p>北京租房信息聚合平台</p>
      </div>
      <div class="footer-bottom">© 2026 房源市场</div>
    </footer>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {getPublicHouses, getPublicStats} from '../api/houses'
import {formatPrice} from '../composables/useFormat'

const router = useRouter()
const loading = ref(true)
const houses = ref([])
const total = ref(0)
const stats = reactive({houses: 0, landlords: 0, tenants: 0, appointments: 0})

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

function getImage(h) {
  try {
    const arr = JSON.parse(h.image || '[]')
    if (Array.isArray(arr) && arr.length) return arr[0]
  } catch { /* ignore */
  }
  return '/uploads/img.png'
}

function parseTags(h) {
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
      page: 1,
      pageSize: 8,
      keyword: filters.keyword || undefined,
      district: filters.district || undefined,
      type: filters.type || undefined,
      minPrice,
      maxPrice
    })
    houses.value = res.data?.data?.houses || []
    total.value = res.data?.data?.total || 0
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
  fetchHouses()
}

function resetFilters() {
  filters.keyword = ''
  filters.district = ''
  filters.type = ''
  filters.priceRange = ''
  fetchHouses()
}

onMounted(() => {
  fetchHouses()
  loadStats()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #e8e8e8;
}

.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
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
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: #1677ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
}

.brand-name {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
}

.top-nav {
  display: flex;
  gap: 24px;
}

.top-nav a {
  color: #555;
  font-size: 14px;
  text-decoration: none;
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
  text-decoration: none;
}

.btn-register {
  background: #1677ff;
  color: #fff;
  padding: 8px 18px;
  border-radius: 6px;
  font-size: 14px;
  text-decoration: none;
}

.btn-register:hover {
  background: #0958d9;
}

.hero {
  position: relative;
  min-height: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 72px 24px 64px;
  background: linear-gradient(90deg, rgba(10, 24, 45, 0.72) 0%, rgba(10, 24, 45, 0.42) 100%),
  url('/uploads/img_4.png') center / cover no-repeat;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(10, 24, 45, 0.2) 0%, rgba(10, 24, 45, 0.55) 100%);
}

.hero-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1100px;
  color: #fff;
}

.hero h1 {
  font-size: 44px;
  font-weight: 800;
  margin-bottom: 10px;
  letter-spacing: 0;
}

.hero-sub {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.82);
  margin-bottom: 28px;
}

.search-panel {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1.6fr auto;
  gap: 12px;
  align-items: end;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.22);
}

.search-field label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
}

.search-field select,
.search-keyword input {
  width: 100%;
  height: 42px;
  padding: 0 12px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  background: #fafafa;
  color: #1a1a2e;
  font-size: 14px;
  outline: none;
}

.search-field select:focus,
.search-keyword input:focus {
  border-color: #1677ff;
  background: #fff;
}

.search-keyword input {
  background: #fff;
}

.search-btn {
  height: 42px;
  padding: 0 30px;
  border: none;
  border-radius: 6px;
  background: #ff6b35;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}

.search-btn:hover {
  background: #e55a2b;
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.quick-tags span {
  padding: 6px 14px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.14);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}

.quick-tags span:hover {
  background: rgba(255, 255, 255, 0.28);
}

.stats-band {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.stats-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
}

.stat-item strong {
  display: block;
  font-size: 30px;
  font-weight: 800;
  color: #1677ff;
}

.stat-item span {
  font-size: 13px;
  color: #888;
}

.page-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px 64px;
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.section-head h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
}

.section-head p {
  color: #999;
  font-size: 13px;
  margin-top: 4px;
}

.type-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.type-chips button {
  padding: 7px 16px;
  border: 1px solid #e8e8e8;
  border-radius: 20px;
  background: #fff;
  color: #555;
  font-size: 13px;
  cursor: pointer;
}

.type-chips button.active {
  border-color: #1677ff;
  background: #e6f4ff;
  color: #1677ff;
  font-weight: 600;
}

.listing-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.home-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.home-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
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
  transition: transform 0.4s;
}

.home-card:hover .home-card-img img {
  transform: scale(1.06);
}

.card-type,
.card-district {
  position: absolute;
  top: 10px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
}

.card-type {
  left: 10px;
  background: rgba(255, 107, 53, 0.9);
}

.card-district {
  right: 10px;
  background: rgba(0, 0, 0, 0.48);
}

.home-card-body {
  padding: 14px;
}

.home-card-body h3 {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 6px;
}

.card-price {
  color: #ff6b35;
  font-size: 19px;
  font-weight: 700;
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
  color: #666;
  background: #f5f7fa;
  border: 1px solid #eef0f3;
  border-radius: 4px;
  padding: 3px 8px;
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
  background: #e6f4ff;
  border-radius: 4px;
  padding: 2px 8px;
}

.card-address {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.skeleton-card {
  aspect-ratio: 4 / 5;
  border-radius: 10px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e4e4e4 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.empty-state {
  padding: 80px 20px;
  text-align: center;
  color: #999;
  background: #fff;
  border-radius: 10px;
}

.empty-state button {
  margin-top: 16px;
  padding: 8px 20px;
  border: 1px solid #1677ff;
  border-radius: 6px;
  background: #fff;
  color: #1677ff;
  cursor: pointer;
}

.home-footer {
  background: #1a1a2e;
  color: rgba(255, 255, 255, 0.7);
  padding: 32px 24px 20px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
}

.footer-inner p {
  font-size: 13px;
}

.footer-bottom {
  max-width: 1200px;
  margin: 16px auto 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
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
    min-height: 480px;
    padding: 56px 16px 48px;
  }

  .hero h1 {
    font-size: 32px;
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
}
</style>
