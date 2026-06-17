<template>
  <section class="history-page">
    <div class="page-head">
      <div>
        <h1>浏览历史</h1>
        <p>按最近浏览时间回看你打开过的商品。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadHistory">刷新</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-empty v-else-if="records.length === 0" description="暂无浏览记录" />

    <div v-else class="history-list">
      <article v-for="record in records" :key="`${record.product.id}-${record.viewedAt}`" class="history-row">
        <button class="thumb" type="button" @click="openProduct(record.product.id)">
          <img v-if="coverOf(record.product)" :src="coverOf(record.product)" :alt="record.product.title" />
          <el-icon v-else><Picture /></el-icon>
        </button>

        <div class="info">
          <div class="title-line">
            <h2>{{ record.product.title }}</h2>
            <el-tag size="small" :type="statusType(record.product.status)">{{ statusText(record.product.status) }}</el-tag>
          </div>

          <div class="time-line">
            <span><el-icon><Clock /></el-icon>{{ formatDateTime(record.viewedAt) }}</span>
            <span>{{ record.product.categoryName || '未分类' }}</span>
            <span>{{ record.product.campus || '校内' }}</span>
          </div>

          <p>{{ record.product.description || '卖家暂未填写描述' }}</p>

          <div class="badge-row">
            <el-tag v-if="record.product.sellerCreditLevel" size="small" type="success">
              信用{{ record.product.sellerCreditLevel }}
            </el-tag>
            <el-tag v-for="tag in record.product.tagNames || []" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
          </div>

          <div class="meta">
            <strong>¥{{ money(record.product.price) }}</strong>
            <span><el-icon><View /></el-icon>{{ record.product.viewCount ?? 0 }}</span>
            <span><el-icon><Star /></el-icon>{{ record.product.favoriteCount ?? 0 }}</span>
            <span>{{ tradeModeText(record.product.tradeModes) }}</span>
          </div>
        </div>

        <div class="actions">
          <el-button type="primary" @click="openProduct(record.product.id)">查看详情</el-button>
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
import { Clock, Picture, Refresh, Star, View } from '@element-plus/icons-vue'
import { listBrowseHistory, type BrowseHistoryItem, type ProductItem } from '../../api/product'
import { getApiErrorMessage } from '../../utils/apiError'
import { resolveMediaUrl } from '../../utils/media'

const router = useRouter()
const loading = ref(false)
const records = ref<BrowseHistoryItem[]>([])
const page = reactive({ page: 1, pageSize: 10, total: 0 })

onMounted(loadHistory)

async function loadHistory() {
  loading.value = true
  try {
    const response = await listBrowseHistory({ page: page.page, pageSize: page.pageSize })
    if (response.code !== 0) throw new Error(response.message)
    records.value = response.data.items
    page.total = response.data.total
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载浏览历史失败'))
  } finally {
    loading.value = false
  }
}

function changePage(value: number) {
  page.page = value
  loadHistory()
}

function openProduct(id: number) {
  router.push(`/products/${id}`)
}

function coverOf(product: ProductItem) {
  return resolveMediaUrl(product.imageUrls?.[0])
}

function money(value?: number) {
  return Number(value ?? 0).toFixed(2)
}

function formatDateTime(value?: string) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
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
  if (!values?.length) return '线下面交'
  return values.map((value) => (value === 'LOCKER' ? '柜机中转' : '线下面交')).join(' / ')
}
</script>

<style scoped>
.history-page {
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
.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.history-row {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
}
.thumb {
  width: 132px;
  aspect-ratio: 4 / 3;
  border: 0;
  border-radius: 8px;
  background: #f1f5f9;
  color: #94a3b8;
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
.time-line,
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
.time-line,
.meta span {
  color: #64748b;
  font-size: 13px;
}
.time-line span,
.meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
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
  color: #ef4444;
  font-size: 18px;
}
.actions {
  display: flex;
  justify-content: flex-end;
}
@media (max-width: 760px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }
  .history-row {
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
