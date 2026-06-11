<template>
  <section class="product-list-page">
    <div class="page-head">
      <div>
        <h1>商品浏览</h1>
        <p>按分类、价格、成色和交易方式筛选校园闲置商品。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="$router.push('/products/new')">发布商品</el-button>
    </div>

    <div class="content-grid">
      <aside class="filters">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索标题或描述"
          clearable
          :prefix-icon="Search"
          @keyup.enter="reload"
          @clear="reload"
        />

        <el-select v-model="filters.categoryId" placeholder="分类" clearable @change="reload">
          <el-option v-for="category in flatCategories" :key="category.id" :label="category.name" :value="category.id" />
        </el-select>

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

        <el-input v-model="filters.campus" placeholder="校区" clearable @keyup.enter="reload" @clear="reload" />

        <el-segmented v-model="filters.tradeMode" :options="tradeModeOptions" @change="reload" />

        <el-select v-model="filters.sort" placeholder="排序" @change="reload">
          <el-option label="最新发布" value="newest" />
          <el-option label="价格从低到高" value="price_asc" />
          <el-option label="价格从高到低" value="price_desc" />
          <el-option label="热度优先" value="hot" />
        </el-select>

        <div class="filter-actions">
          <el-button type="primary" :icon="Search" @click="reload">筛选</el-button>
          <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
        </div>

        <div class="tag-box">
          <span v-for="tag in tags" :key="tag.id">#{{ tag.name }}</span>
        </div>
      </aside>

      <main class="results">
        <div class="result-bar">
          <span>共 {{ page.total }} 件商品</span>
          <el-button text :icon="Refresh" :loading="loading" @click="loadProducts">刷新</el-button>
        </div>

        <el-empty v-if="!loading && products.length === 0" description="没有找到符合条件的商品" />

        <div v-else class="product-grid">
          <article v-for="product in products" :key="product.id" class="product-card" @click="openProduct(product.id)">
            <div class="thumb">
              <img v-if="coverOf(product)" :src="coverOf(product)" :alt="product.title" />
              <el-icon v-else><Picture /></el-icon>
            </div>
            <div class="card-body">
              <div class="title-row">
                <h2>{{ product.title }}</h2>
                <el-tag size="small">{{ product.categoryName || '未分类' }}</el-tag>
              </div>
              <div class="badge-row">
                <el-tag v-if="product.sellerCreditLevel" size="small" type="success">
                  信用{{ product.sellerCreditLevel }}
                </el-tag>
                <el-tag v-for="tag in product.tagNames || []" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
              </div>
              <p class="desc">{{ product.description || '卖家暂未填写描述' }}</p>
              <div class="meta-row">
                <strong>¥{{ money(product.price) }}</strong>
                <span>{{ product.campus || '校内' }}</span>
                <span>{{ conditionLabel(product.conditionLevel) }}</span>
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
        <h2>推荐</h2>
        <el-skeleton v-if="recommendLoading" :rows="5" animated />
        <div v-else class="recommend-list">
          <button v-for="item in recommendations" :key="item.id" type="button" @click="openProduct(item.id)">
            <span>{{ item.title }}</span>
            <small>{{ item.recommendReason || '最新上架商品' }}</small>
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
  gap: 18px;
}
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.page-head h1 {
  margin: 0;
  font-size: 26px;
}
.page-head p {
  margin: 6px 0 0;
  color: #64748b;
}
.content-grid {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr) 260px;
  gap: 16px;
  align-items: start;
}
.filters,
.recommend,
.results {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
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
  color: #64748b;
  font-size: 12px;
}
.results {
  min-width: 0;
  padding: 14px;
}
.result-bar,
.meta-row,
.stat-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.result-bar {
  margin-bottom: 12px;
  color: #475569;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 12px;
}
.product-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: #fff;
}
.product-card:hover {
  border-color: #409eff;
}
.thumb {
  height: 150px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 32px;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.card-body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.title-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}
.title-row h2 {
  margin: 0;
  font-size: 16px;
  line-height: 1.35;
}
.desc {
  min-height: 40px;
  margin: 0;
  color: #64748b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.badge-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 24px;
}
.meta-row strong {
  color: #ef4444;
  font-size: 18px;
}
.meta-row span,
.stat-row span {
  color: #64748b;
  font-size: 12px;
}
.stat-row span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.recommend h2 {
  margin: 0 0 12px;
  font-size: 18px;
}
.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.recommend-list button {
  text-align: left;
  border: 1px solid #e2e8f0;
  background: #fff;
  border-radius: 8px;
  padding: 10px;
  cursor: pointer;
}
.recommend-list button:hover {
  border-color: #409eff;
}
.recommend-list span,
.recommend-list small {
  display: block;
}
.recommend-list small {
  margin-top: 4px;
  color: #64748b;
}
@media (max-width: 1100px) {
  .content-grid {
    grid-template-columns: 240px 1fr;
  }
  .recommend {
    display: none;
  }
}
@media (max-width: 760px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
