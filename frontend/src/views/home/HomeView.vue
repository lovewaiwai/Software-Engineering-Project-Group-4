<template>
  <section class="home-page">
    <div class="home-head">
      <div>
        <h1>校园闲置精选</h1>
        <p>浏览最新上架和个性化推荐的二手教材、数码和生活用品。</p>
      </div>
      <div class="head-actions">
        <el-button :icon="Search" @click="$router.push('/products')">去搜索</el-button>
        <el-button type="primary" :icon="Plus" @click="$router.push('/products/new')">发布商品</el-button>
      </div>
    </div>

    <div class="category-strip">
      <button v-for="category in flatCategories" :key="category.id" type="button" @click="openCategory(category.id)">
        {{ category.name }}
      </button>
    </div>

    <div class="section-head">
      <h2>推荐商品</h2>
      <el-button text :icon="Refresh" :loading="loading" @click="loadData">刷新</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="recommendations.length === 0" description="暂无推荐商品" />

    <div v-else class="feed-grid">
      <article v-for="product in recommendations" :key="product.id" class="feed-card" @click="openProduct(product.id)">
        <div class="cover">
          <img v-if="product.imageUrls?.[0]" :src="resolveMediaUrl(product.imageUrls[0])" :alt="product.title" />
          <el-icon v-else><Picture /></el-icon>
        </div>
        <div class="feed-body">
          <div class="title-row">
            <h3>{{ product.title }}</h3>
            <strong>¥{{ money(product.price) }}</strong>
          </div>
          <p>{{ product.description || '卖家暂未填写描述' }}</p>
          <div class="meta-row">
            <el-tag size="small">{{ product.categoryName || '未分类' }}</el-tag>
            <el-tag v-if="product.sellerCreditLevel" size="small" type="success">信用{{ product.sellerCreditLevel }}</el-tag>
            <span>{{ product.recommendReason || '最新上架商品' }}</span>
          </div>
        </div>
      </article>
    </div>
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
const recommendations = ref<ProductItem[]>([])
const categories = ref<CategoryItem[]>([])
const flatCategories = computed(() => flattenCategories(categories.value))

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const [categoryResponse, recommendResponse] = await Promise.all([listCategories(), listRecommendations(12)])
    if (categoryResponse.code === 0) categories.value = categoryResponse.data
    if (recommendResponse.code === 0) recommendations.value = recommendResponse.data
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载首页数据失败'))
  } finally {
    loading.value = false
  }
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
.home-head,
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.home-head h1,
.section-head h2 {
  margin: 0;
}
.home-head h1 {
  font-size: 28px;
}
.home-head p {
  margin: 6px 0 0;
  color: #64748b;
}
.head-actions {
  display: flex;
  gap: 10px;
}
.category-strip {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
}
.category-strip button {
  border: 1px solid #dbe4ee;
  background: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  color: #334155;
  cursor: pointer;
  white-space: nowrap;
}
.category-strip button:hover {
  border-color: #409eff;
  color: #409eff;
}
.feed-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}
.feed-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}
.feed-card:hover {
  border-color: #409eff;
}
.cover {
  height: 160px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 34px;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.feed-body {
  padding: 12px;
}
.title-row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}
.title-row h3 {
  margin: 0;
  font-size: 16px;
}
.title-row strong {
  color: #ef4444;
}
.feed-body p {
  min-height: 42px;
  margin: 8px 0;
  color: #64748b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.meta-row span {
  color: #64748b;
  font-size: 12px;
}
@media (max-width: 700px) {
  .home-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
