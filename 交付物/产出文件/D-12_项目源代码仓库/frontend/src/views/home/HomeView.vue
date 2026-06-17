<template>
  <section class="home-page">
    <header class="market-hero">
      <div class="hero-copy">
        <p class="eyebrow">北京林业大学校园闲置</p>
        <h1>让用过的好东西，在北林继续发光</h1>
        <p>找教材、数码、生活小物和代步装备，优先看同校区、可面交、可信用的校园闲置。</p>
      </div>
      <div class="hero-search">
        <el-input
          v-model="searchKeyword"
          size="large"
          clearable
          placeholder="搜教材、耳机、自行车、考研资料..."
          :prefix-icon="Search"
          @keyup.enter="submitSearch"
        />
        <el-button type="primary" size="large" :icon="Search" @click="submitSearch">搜索</el-button>
        <el-button size="large" :icon="Plus" @click="$router.push('/products/new')">发布闲置</el-button>
      </div>
    </header>

    <nav class="category-strip" aria-label="商品分类">
      <button type="button" class="category-pill all" @click="$router.push('/products')">全部闲置</button>
      <button v-for="category in flatCategories" :key="category.id" type="button" class="category-pill" @click="openCategory(category.id)">
        {{ category.name }}
      </button>
    </nav>

    <section class="feed-section">
      <div class="section-head">
        <div>
          <h2>北林同学正在出</h2>
          <p>按你的浏览和最新上架综合推荐，先聊再买更安心。</p>
        </div>
        <el-button text :icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
      </div>

      <el-skeleton v-if="loading" :rows="6" animated />
      <el-empty v-else-if="recommendations.length === 0" description="暂无推荐商品" />

      <div v-else class="waterfall-grid">
        <article v-for="product in recommendations" :key="product.id" class="product-tile" @click="openProduct(product.id)">
          <div class="tile-cover">
            <img v-if="coverOf(product)" :src="coverOf(product)" :alt="product.title" />
            <el-icon v-else><Picture /></el-icon>
          </div>
          <div class="tile-body">
            <h3>{{ product.title }}</h3>
            <p>{{ product.description || '卖家暂未填写描述' }}</p>
            <div class="price-line">
              <strong>¥{{ money(product.price) }}</strong>
              <span>{{ product.campus || '校内' }}</span>
            </div>
            <div class="meta-line">
              <span>{{ product.categoryName || '闲置' }}</span>
              <span v-if="product.sellerCreditLevel">信用{{ product.sellerCreditLevel }}</span>
              <span>{{ product.recommendReason || '最新上架' }}</span>
            </div>
          </div>
        </article>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { listCategories, listRecommendations, recordProductView, type CategoryItem, type ProductItem } from '../../api/product'
import { getApiErrorMessage } from '../../utils/apiError'
import { resolveMediaUrl } from '../../utils/media'

const router = useRouter()
const loading = ref(false)
const searchKeyword = ref('')
const recommendations = ref<ProductItem[]>([])
const categories = ref<CategoryItem[]>([])
const flatCategories = computed(() => flattenCategories(categories.value).slice(0, 12))

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const [categoryResponse, recommendResponse] = await Promise.all([listCategories(), listRecommendations(16)])
    if (categoryResponse.code === 0) categories.value = categoryResponse.data
    if (recommendResponse.code === 0) recommendations.value = recommendResponse.data
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载首页数据失败'))
  } finally {
    loading.value = false
  }
}

function submitSearch() {
  router.push({ path: '/products', query: searchKeyword.value.trim() ? { keyword: searchKeyword.value.trim() } : {} })
}

async function openProduct(id: number) {
  try {
    await recordProductView(id)
  } catch {
    // 浏览记录失败不影响进入详情页。
  }
  router.push(`/products/${id}`)
}

function openCategory(categoryId: number) {
  router.push({ path: '/products', query: { categoryId } })
}

function flattenCategories(items: CategoryItem[]): CategoryItem[] {
  const seen = new Set<string>()
  return items
    .flatMap((item) => [item, ...flattenCategories(item.children ?? [])])
    .filter((item) => {
      if (seen.has(item.name)) return false
      seen.add(item.name)
      return true
    })
}

function coverOf(product: ProductItem) {
  return resolveMediaUrl(product.imageUrls?.[0])
}

function money(value?: number) {
  return Number(value ?? 0).toFixed(2)
}
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.market-hero {
  min-height: 260px;
  display: grid;
  align-items: end;
  gap: 24px;
  padding: 32px;
  border: 1px solid var(--bfu-border);
  border-radius: 8px;
  background:
    linear-gradient(120deg, rgba(7, 59, 42, 0.88), rgba(15, 107, 71, 0.76)),
    url("https://images.unsplash.com/photo-1473773508845-188df298d2d1?auto=format&fit=crop&w=1600&q=80") center/cover;
  color: #fff;
  box-shadow: var(--bfu-shadow);
}
.hero-copy {
  max-width: 720px;
}
.hero-copy .eyebrow {
  color: #c8f2d4;
}
.hero-copy h1 {
  margin: 8px 0 10px;
  font-size: clamp(30px, 5vw, 52px);
  line-height: 1.05;
  letter-spacing: 0;
}
.hero-copy p {
  margin: 0;
  color: rgba(255, 255, 255, 0.86);
  line-height: 1.7;
}
.hero-search {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) auto auto;
  gap: 10px;
  max-width: 860px;
}
.hero-search :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.38) inset, 0 8px 22px rgba(7, 59, 42, 0.18);
}
.category-strip {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 2px;
}
.category-pill {
  border: 1px solid var(--bfu-border);
  background: #fff;
  border-radius: 8px;
  padding: 9px 14px;
  color: var(--bfu-green-800);
  cursor: pointer;
  white-space: nowrap;
  font-weight: 700;
}
.category-pill.all,
.category-pill:hover {
  border-color: var(--bfu-green-600);
  background: var(--bfu-green-100);
}
.feed-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}
.section-head h2 {
  margin: 0;
  font-size: 24px;
}
.section-head p {
  margin: 4px 0 0;
  color: var(--bfu-muted);
}
.waterfall-grid {
  columns: 4 220px;
  column-gap: 14px;
}
.product-tile {
  display: inline-block;
  width: 100%;
  margin: 0 0 14px;
  overflow: hidden;
  border: 1px solid var(--bfu-border);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(7, 59, 42, 0.05);
  break-inside: avoid;
}
.product-tile:hover {
  border-color: var(--bfu-green-500);
  transform: translateY(-2px);
}
.tile-cover {
  min-height: 170px;
  display: grid;
  place-items: center;
  background: var(--bfu-leaf-50);
  color: var(--bfu-green-300);
  font-size: 34px;
}
.tile-cover img {
  width: 100%;
  height: auto;
  min-height: 170px;
  max-height: 310px;
  object-fit: cover;
  display: block;
}
.tile-body {
  padding: 12px;
}
.tile-body h3 {
  margin: 0;
  font-size: 16px;
  line-height: 1.35;
}
.tile-body p {
  margin: 7px 0 10px;
  color: var(--bfu-muted);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.price-line,
.meta-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.price-line strong {
  color: var(--bfu-price);
  font-size: 20px;
}
.price-line span,
.meta-line span {
  color: var(--bfu-muted);
  font-size: 12px;
}
.meta-line {
  margin-top: 6px;
  justify-content: flex-start;
  flex-wrap: wrap;
}
@media (max-width: 760px) {
  .market-hero {
    padding: 22px;
  }
  .hero-search {
    grid-template-columns: 1fr;
  }
  .section-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
