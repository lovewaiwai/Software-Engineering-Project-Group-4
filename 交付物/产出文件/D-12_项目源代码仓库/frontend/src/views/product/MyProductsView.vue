<template>
  <section class="my-products-page">
    <div class="page-head">
      <div>
        <h1>我的发布</h1>
        <p>查看自己发布的商品，包括草稿、待审核、已上架、审核未通过和已下架记录。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="$router.push('/products/new')">发布商品</el-button>
    </div>

    <div class="toolbar">
      <el-segmented v-model="statusFilter" :options="statusOptions" @change="reload" />
      <el-button :icon="Refresh" :loading="loading" @click="loadProducts">刷新</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="products.length === 0" description="暂无发布记录" />

    <div v-else class="product-list">
      <article v-for="product in products" :key="product.id" class="product-row">
        <div class="thumb">
          <img v-if="coverOf(product)" :src="coverOf(product)" :alt="product.title" />
          <el-icon v-else><Picture /></el-icon>
        </div>
        <div class="info">
          <div class="title-line">
            <h2>{{ product.title }}</h2>
            <el-tag :type="statusType(product.status)">{{ statusText(product.status) }}</el-tag>
          </div>
          <div class="badge-row">
            <el-tag v-if="product.sellerCreditLevel" size="small" type="success">信用{{ product.sellerCreditLevel }}</el-tag>
            <el-tag v-for="tag in product.tagNames || []" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
          </div>
          <p>{{ product.description || '暂无描述' }}</p>
          <div class="meta">
            <strong>¥{{ money(product.price) }}</strong>
            <span>{{ product.categoryName || '未分类' }}</span>
            <span>{{ product.campus || '校内' }}</span>
            <span>{{ tradeModeText(product.tradeModes) }}</span>
          </div>
          <div v-if="product.auditReason" class="reason">审核意见：{{ product.auditReason }}</div>
        </div>
        <div class="actions">
          <el-button @click="$router.push(`/products/${product.id}`)">查看详情</el-button>
          <el-button
            v-if="product.status === 'DRAFT' || product.status === 'REVIEW_REJECTED'"
            @click="$router.push(`/products/${product.id}/edit`)"
          >
            编辑
          </el-button>
          <el-button v-if="product.status === 'DRAFT' || product.status === 'REVIEW_REJECTED'" type="primary" @click="changeStatus(product, 'submit')">
            提交审核
          </el-button>
          <el-button v-if="product.status === 'ACTIVE'" type="warning" @click="changeStatus(product, 'offline')">下架</el-button>
          <el-button v-if="product.status === 'OFFLINE'" type="success" @click="changeStatus(product, 'relist')">重新上架</el-button>
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
import { ElMessage } from 'element-plus'
import { Picture, Plus, Refresh } from '@element-plus/icons-vue'
import { listMyProducts, offlineProduct, relistProduct, submitProductForReview, type ProductItem } from '../../api/product'
import { getApiErrorMessage } from '../../utils/apiError'
import { resolveMediaUrl } from '../../utils/media'

const loading = ref(false)
const products = ref<ProductItem[]>([])
const statusFilter = ref('')
const page = reactive({ page: 1, pageSize: 10, total: 0 })

const statusOptions = [
  { label: '全部', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '待审核', value: 'PENDING_REVIEW' },
  { label: '已上架', value: 'ACTIVE' },
  { label: '未通过', value: 'REVIEW_REJECTED' },
  { label: '已下架', value: 'OFFLINE' },
  { label: '交易中', value: 'LOCKED' },
  { label: '已售出', value: 'SOLD' },
]

onMounted(loadProducts)

async function loadProducts() {
  loading.value = true
  try {
    const response = await listMyProducts({
      page: page.page,
      pageSize: page.pageSize,
      status: statusFilter.value || undefined,
    })
    if (response.code !== 0) throw new Error(response.message)
    products.value = response.data.items
    page.total = response.data.total
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载我的发布失败'))
  } finally {
    loading.value = false
  }
}

function reload() {
  page.page = 1
  loadProducts()
}

function changePage(value: number) {
  page.page = value
  loadProducts()
}

async function changeStatus(product: ProductItem, action: 'submit' | 'offline' | 'relist') {
  try {
    const response =
      action === 'submit'
        ? await submitProductForReview(product.id)
        : action === 'offline'
          ? await offlineProduct(product.id)
          : await relistProduct(product.id)
    if (response.code !== 0) throw new Error(response.message)
    ElMessage.success(action === 'offline' ? '商品已下架' : action === 'relist' ? '商品已重新上架' : '商品已提交审核')
    await loadProducts()
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '操作失败'))
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
  if (value === 'PENDING_REVIEW') return 'warning'
  if (value === 'REVIEW_REJECTED') return 'danger'
  return 'info'
}

function tradeModeText(values?: string[]) {
  if (!values?.length) return '面交'
  return values.map((value) => (value === 'LOCKER' ? '柜机中转' : '线下面交')).join(' / ')
}
</script>

<style scoped>
.my-products-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.page-head,
.toolbar {
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
.toolbar {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
}
.product-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.product-row {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
}
.thumb {
  width: 120px;
  aspect-ratio: 4 / 3;
  border-radius: 8px;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  overflow: hidden;
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
.meta,
.badge-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.title-line h2 {
  margin: 0;
  font-size: 18px;
}
.info p {
  margin: 0;
  color: #64748b;
  line-height: 1.5;
}
.meta strong {
  color: #ef4444;
  font-size: 18px;
}
.meta span {
  color: #64748b;
  font-size: 13px;
}
.reason {
  color: #b45309;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 6px;
  padding: 8px;
  font-size: 13px;
}
.actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
@media (max-width: 760px) {
  .page-head,
  .toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
  .product-row {
    grid-template-columns: 1fr;
  }
  .thumb {
    width: 100%;
  }
  .actions {
    justify-self: stretch;
  }
  .actions .el-button {
    width: 100%;
    margin-left: 0;
  }
}
</style>
