<template>
  <section class="favorites-page">
    <div class="page-head">
      <div>
        <h1>我的收藏</h1>
        <p>集中查看已经收藏的闲置商品，随时回到详情页继续沟通或下单。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadFavorites">刷新</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="products.length === 0" description="暂无收藏商品">
      <el-button type="primary" @click="router.push('/')">去首页逛逛</el-button>
    </el-empty>

    <div v-else class="favorite-list">
      <article v-for="product in products" :key="product.id" class="favorite-row">
        <button class="thumb" type="button" @click="openProduct(product.id)">
          <img v-if="coverOf(product)" :src="coverOf(product)" :alt="product.title" />
          <el-icon v-else><Picture /></el-icon>
        </button>

        <div class="info">
          <div class="title-line">
            <h2>{{ product.title }}</h2>
            <el-tag size="small" :type="statusType(product.status)">{{ statusText(product.status) }}</el-tag>
          </div>
          <p>{{ product.description || '卖家暂未填写描述' }}</p>
          <div class="badge-row">
            <el-tag v-if="product.categoryName" size="small" effect="plain">{{ product.categoryName }}</el-tag>
            <el-tag v-if="product.sellerCreditLevel" size="small" type="success">信用{{ product.sellerCreditLevel }}</el-tag>
            <el-tag v-for="tag in product.tagNames || []" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
          </div>
          <div class="meta">
            <strong>¥{{ money(product.price) }}</strong>
            <span><el-icon><View /></el-icon>{{ product.viewCount ?? 0 }}</span>
            <span><el-icon><Star /></el-icon>{{ product.favoriteCount ?? 0 }}</span>
            <span>{{ product.campus || '校内' }}</span>
            <span>{{ tradeModeText(product.tradeModes) }}</span>
          </div>
        </div>

        <div class="actions">
          <el-button type="primary" @click="openProduct(product.id)">查看详情</el-button>
          <el-button :loading="removingId === product.id" @click="removeFavorite(product)">取消收藏</el-button>
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
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, Refresh, Star, View } from '@element-plus/icons-vue'
import { listFavoriteProducts, unfavoriteProduct, type ProductItem } from '../../api/product'
import { getApiErrorMessage } from '../../utils/apiError'
import { resolveMediaUrl } from '../../utils/media'

const router = useRouter()
const loading = ref(false)
const removingId = ref<number>()
const products = ref<ProductItem[]>([])
const page = reactive({ page: 1, pageSize: 10, total: 0 })

onMounted(loadFavorites)

async function loadFavorites() {
  loading.value = true
  try {
    const response = await listFavoriteProducts({ page: page.page, pageSize: page.pageSize })
    if (response.code !== 0) throw new Error(response.message)
    products.value = response.data.items
    page.total = response.data.total
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载收藏失败'))
  } finally {
    loading.value = false
  }
}

function changePage(value: number) {
  page.page = value
  loadFavorites()
}

function openProduct(id: number) {
  router.push(`/products/${id}`)
}

async function removeFavorite(product: ProductItem) {
  removingId.value = product.id
  try {
    const response = await unfavoriteProduct(product.id)
    if (response.code !== 0) throw new Error(response.message)
    ElMessage.success('已取消收藏')
    await loadFavorites()
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '取消收藏失败'))
  } finally {
    removingId.value = undefined
  }
}

function coverOf(product: ProductItem) {
  return resolveMediaUrl(product.imageUrls?.[0])
}

function money(value?: number) {
  return Number(value ?? 0).toFixed(2)
}

function statusText(value?: string) {
  const map: Record<string, string> = {
    PENDING_REVIEW: '待审核',
    ACTIVE: '已上架',
    REVIEW_REJECTED: '审核未通过',
    LOCKED: '交易中',
    SOLD: '已售出',
    OFFLINE: '已下架',
    DRAFT: '草稿',
  }
  return value ? map[value] ?? value : '未知'
}

function statusType(value?: string) {
  if (value === 'ACTIVE') return 'success'
  if (value === 'LOCKED') return 'warning'
  if (value === 'SOLD' || value === 'OFFLINE') return 'info'
  return 'info'
}

function tradeModeText(values?: string[]) {
  if (!values?.length) return '线下面交'
  return values.map((value) => (value === 'LOCKER' ? '柜机中转' : '线下面交')).join(' / ')
}
</script>

<style scoped>
.favorites-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
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
.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.favorite-row {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  background: #fff;
  border: 1px solid var(--bfu-border);
  border-radius: 8px;
  padding: 12px;
}
.thumb {
  width: 132px;
  aspect-ratio: 4 / 3;
  border: 0;
  border-radius: 8px;
  background: var(--bfu-leaf-50);
  color: var(--bfu-green-300);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  overflow: hidden;
  cursor: pointer;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.title-line,
.badge-row,
.meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.title-line h2 {
  min-width: 0;
  margin: 0;
  font-size: 18px;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.info p {
  margin: 0;
  color: #64748b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.meta strong {
  color: var(--bfu-price);
  font-size: 18px;
}
.meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
  font-size: 13px;
}
.actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
@media (max-width: 760px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }
  .favorite-row {
    grid-template-columns: 1fr;
  }
  .thumb {
    width: 100%;
  }
  .actions,
  .actions .el-button {
    width: 100%;
  }
}
</style>
