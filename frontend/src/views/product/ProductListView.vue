<template>
  <section class="product-list-page">
    <header class="market-toolbar">
      <div>
        <p class="eyebrow">校园闲置广场</p>
        <h1>找一件刚好需要的北林闲置</h1>
      </div>
      <el-button type="primary" :icon="Plus" @click="$router.push('/products/new')">发布闲置</el-button>
    </header>

    <section class="search-band">
      <el-input
        v-model="filters.keyword"
        size="large"
        clearable
        placeholder="搜商品、课程、宿舍用品..."
        :prefix-icon="Search"
        @keyup.enter="reload"
        @clear="reload"
      />
      <el-select v-model="filters.categoryId" size="large" placeholder="分类" clearable @change="reload">
        <el-option v-for="category in flatCategories" :key="category.id" :label="category.name" :value="category.id" />
      </el-select>
      <el-select v-model="filters.sort" size="large" placeholder="排序" @change="reload">
        <el-option label="最新发布" value="newest" />
        <el-option label="价格从低到高" value="price_asc" />
        <el-option label="价格从高到低" value="price_desc" />
        <el-option label="热度优先" value="hot" />
      </el-select>
      <el-button type="primary" size="large" :icon="Search" @click="reload">搜索</el-button>
    </section>

    <div class="content-grid">
      <aside class="filters">
        <h2>筛选</h2>
        <div class="range-row">
          <el-input-number v-model="filters.minPrice" :min="0" :precision="0" placeholder="最低价" controls-position="right" />
          <el-input-number v-model="filters.maxPrice" :min="0" :precision="0" placeholder="最高价" controls-position="right" />
        </div>
        <el-select v-model="filters.conditionLevel" placeholder="成色" clearable @change="reload">
          <el-option label="全新" value="NEW" />
          <el-option label="九成新" value="LIKE_NEW" />
          <el-option label="良好" value="GOOD" />
          <el-option label="普通" value="NORMAL" />
        </el-select>
        <el-input v-model="filters.campus" placeholder="校区/地点" clearable @keyup.enter="reload" @clear="reload" />
        <el-segmented v-model="filters.tradeMode" :options="tradeModeOptions" @change="reload" />
        <div class="filter-actions">
          <el-button type="primary" :icon="Search" @click="reload">筛选</el-button>
          <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
        </div>
        <div class="tag-box">
          <button v-for="tag in tags.slice(0, 16)" :key="tag.id" type="button" @click="filters.keyword = tag.name; reload()">#{{ tag.name }}</button>
        </div>
      </aside>

      <main class="results">
        <div class="result-bar">
          <span>共 {{ page.total }} 件闲置</span>
          <el-button text :icon="Refresh" :loading="loading" @click="loadProducts">刷新</el-button>
        </div>

        <el-empty v-if="!loading && products.length === 0" description="没有找到符合条件的商品" />

        <div v-else class="product-grid">
          <article v-for="product in products" :key="product.id" class="product-card" @click="openProduct(product.id)">
            <div class="thumb">
              <img v-if="coverOf(product)" :src="coverOf(product)" :alt="product.title" />
              <el-icon v-else><Picture /></el-icon>
              <span class="condition-chip">{{ conditionLabel(product.conditionLevel) }}</span>
            </div>
            <div class="card-body">
              <h2>{{ product.title }}</h2>
              <p class="desc">{{ product.description || '卖家暂未填写描述' }}</p>
              <div class="price-row">
                <strong>¥{{ money(product.price) }}</strong>
                <span>{{ product.campus || '校内' }}</span>
              </div>
              <div class="badge-row">
                <span>{{ product.categoryName || '未分类' }}</span>
                <span v-if="product.sellerCreditLevel">信用{{ product.sellerCreditLevel }}</span>
                <span v-for="tag in product.tagNames || []" :key="tag">#{{ tag }}</span>
              </div>
              <div class="stat-row">
                <span><el-icon><View /></el-icon>{{ product.viewCount ?? 0 }}</span>
                <span><el-icon><Star /></el-icon>{{ product.favoriteCount ?? 0 }}</span>
                <span>{{ tradeModeText(product.tradeModes) }}</span>
              </div>
            </div>
          </article>
        </div>

        <el-pagination
          v-if="page.total > page.pageSize"
          layout="prev, pager, next"
          :current-page="page.page"
          :page-size="page.pageSize"
          :total="page.total"
          @current-change="changePage"
        />
      </main>

      <aside class="recommend">
        <h2>可能喜欢</h2>
        <el-skeleton v-if="recommendLoading" :rows="5" animated />
        <div v-else class="recommend-list">
          <button v-for="item in recommendations" :key="item.id" type="button" @click="openProduct(item.id)">
            <img v-if="coverOf(item)" :src="coverOf(item)" :alt="item.title" />
            <span>
              <strong>{{ item.title }}</strong>
              <small>{{ item.recommendReason || '最新上架' }}</small>
            </span>
          </button>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Plus, Refresh, Search, Star, View } from '@element-plus/icons-vue'
import {
  listCategories,
  listRecommendations,
  listTags,
  recordProductView,
  searchProducts,
  type CategoryItem,
  type ProductItem,
  type TagItem,
} from '../../api/product'
import { getApiErrorMessage } from '../../utils/apiError'
import { resolveMediaUrl } from '../../utils/media'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const recommendLoading = ref(false)
const products = ref<ProductItem[]>([])
const recommendations = ref<ProductItem[]>([])
const categories = ref<CategoryItem[]>([])
const tags = ref<TagItem[]>([])

const filters = reactive({
  keyword: '',
  categoryId: undefined as number | undefined,
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  conditionLevel: '',
  campus: '',
  tradeMode: '',
  sort: 'newest',
})

const page = reactive({ page: 1, pageSize: 12, total: 0 })

const tradeModeOptions = [
  { label: '全部', value: '' },
  { label: '面交', value: 'MEETUP' },
  { label: '柜机', value: 'LOCKER' },
]

const flatCategories = computed(() => flattenCategories(categories.value))

onMounted(async () => {
  applyRouteQuery()
  await Promise.all([loadMeta(), loadProducts(), loadRecommendations()])
})

function applyRouteQuery() {
  const categoryId = Number(route.query.categoryId)
  if (Number.isFinite(categoryId) && categoryId > 0) filters.categoryId = categoryId
  if (typeof route.query.keyword === 'string') filters.keyword = route.query.keyword
}

async function loadMeta() {
  try {
    const [categoryResponse, tagResponse] = await Promise.all([listCategories(), listTags()])
    if (categoryResponse.code === 0) categories.value = categoryResponse.data
    if (tagResponse.code === 0) tags.value = tagResponse.data
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载分类标签失败'))
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const response = await searchProducts({
      ...filters,
      conditionLevel: filters.conditionLevel || undefined,
      campus: filters.campus || undefined,
      tradeMode: filters.tradeMode || undefined,
      page: page.page,
      pageSize: page.pageSize,
    })
    if (response.code !== 0) throw new Error(response.message)
    products.value = response.data.items
    page.total = response.data.total
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载商品失败'))
  } finally {
    loading.value = false
  }
}

async function loadRecommendations() {
  recommendLoading.value = true
  try {
    const response = await listRecommendations(6)
    if (response.code === 0) recommendations.value = response.data
  } catch {
    recommendations.value = []
  } finally {
    recommendLoading.value = false
  }
}

function reload() {
  page.page = 1
  loadProducts()
}

function resetFilters() {
  filters.keyword = ''
  filters.categoryId = undefined
  filters.minPrice = undefined
  filters.maxPrice = undefined
  filters.conditionLevel = ''
  filters.campus = ''
  filters.tradeMode = ''
  filters.sort = 'newest'
  reload()
}

function changePage(value: number) {
  page.page = value
  loadProducts()
}

async function openProduct(id: number) {
  try {
    await recordProductView(id)
  } catch {
    // 浏览记录失败不影响进入详情页。
  }
  router.push(`/products/${id}`)
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

function conditionLabel(value?: string) {
  const map: Record<string, string> = { NEW: '全新', LIKE_NEW: '九成新', GOOD: '良好', NORMAL: '普通' }
  return value ? map[value] ?? value : '未知成色'
}

function tradeModeText(values?: string[]) {
  if (!values?.length) return '面交'
  return values.map((value) => (value === 'LOCKER' ? '柜机' : '面交')).join(' / ')
}
</script>

<style scoped>
.product-list-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.market-toolbar,
.search-band,
.filters,
.recommend,
.results {
  border: 1px solid var(--bfu-border);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(7, 59, 42, 0.04);
}
.market-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
}
.market-toolbar h1 {
  margin: 4px 0 0;
  font-size: 28px;
  letter-spacing: 0;
}
.search-band {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 180px 180px auto;
  gap: 10px;
  padding: 14px;
}
.content-grid {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr) 260px;
  gap: 16px;
  align-items: start;
}
.filters,
.recommend {
  padding: 14px;
}
.filters {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.filters h2,
.recommend h2 {
  margin: 0 0 2px;
  font-size: 17px;
}
.range-row {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}
.range-row :deep(.el-input-number) {
  width: 100%;
}
.range-row :deep(.el-input__inner) {
  text-align: left;
}
.filter-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.tag-box {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.tag-box button {
  border: 1px solid transparent;
  background: var(--bfu-green-100);
  color: var(--bfu-green-800);
  border-radius: 8px;
  padding: 5px 8px;
  cursor: pointer;
  font-size: 12px;
}
.results {
  min-width: 0;
  padding: 14px;
}
.result-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  color: var(--bfu-muted);
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 12px;
}
.product-card {
  overflow: hidden;
  border: 1px solid var(--bfu-border);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}
.product-card:hover {
  border-color: var(--bfu-green-500);
  box-shadow: 0 8px 20px rgba(7, 59, 42, 0.08);
}
.thumb {
  position: relative;
  aspect-ratio: 1 / 0.78;
  display: grid;
  place-items: center;
  background: var(--bfu-leaf-50);
  color: var(--bfu-green-300);
  font-size: 32px;
  overflow: hidden;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.condition-chip {
  position: absolute;
  left: 8px;
  bottom: 8px;
  padding: 3px 8px;
  border-radius: 8px;
  background: rgba(7, 59, 42, 0.78);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}
.card-body {
  padding: 11px;
}
.card-body h2 {
  margin: 0;
  font-size: 16px;
  line-height: 1.35;
}
.desc {
  min-height: 40px;
  margin: 7px 0;
  color: var(--bfu-muted);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.price-row,
.stat-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.price-row strong {
  color: var(--bfu-price);
  font-size: 20px;
}
.price-row span,
.stat-row span,
.badge-row span {
  color: var(--bfu-muted);
  font-size: 12px;
}
.badge-row {
  min-height: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 8px 0;
}
.badge-row span {
  padding: 3px 7px;
  border-radius: 8px;
  background: var(--bfu-mint-50);
}
.stat-row span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.recommend-list button {
  display: grid;
  grid-template-columns: 52px 1fr;
  gap: 8px;
  align-items: center;
  text-align: left;
  border: 1px solid var(--bfu-border);
  background: #fff;
  border-radius: 8px;
  padding: 8px;
  cursor: pointer;
}
.recommend-list button:hover {
  border-color: var(--bfu-green-500);
}
.recommend-list img {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  object-fit: cover;
  background: var(--bfu-leaf-50);
}
.recommend-list strong,
.recommend-list small {
  display: block;
}
.recommend-list strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.recommend-list small {
  margin-top: 4px;
  color: var(--bfu-muted);
}
@media (max-width: 1120px) {
  .content-grid {
    grid-template-columns: 220px 1fr;
  }
  .recommend {
    display: none;
  }
}
@media (max-width: 820px) {
  .market-toolbar,
  .search-band {
    grid-template-columns: 1fr;
  }
  .market-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
