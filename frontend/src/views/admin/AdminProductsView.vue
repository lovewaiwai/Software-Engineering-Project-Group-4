<template>
  <section class="product-review-page">
    <div class="page-head">
      <div>
        <h1>商品审核</h1>
        <p>查看待审核商品，确认信息合规后上架，或填写原因退回给卖家修改。</p>
      </div>
      <el-button type="primary" :icon="Refresh" :loading="loading" @click="loadProducts">刷新列表</el-button>
    </div>

    <section class="bulk-panel">
      <div class="bulk-head">
        <div>
          <h2>关键词批量通过</h2>
          <p>适合教材、文具、数码配件等低风险闲置品；命中标题、描述、分类或标签后自动审核通过。</p>
        </div>
        <el-button type="success" :loading="bulkLoading" :disabled="!selectedKeywords.length" @click="handleBulkApprove">
          一键通过命中商品
        </el-button>
      </div>
      <div class="keyword-row">
        <el-check-tag
          v-for="keyword in keywordPresets"
          :key="keyword"
          :checked="selectedKeywords.includes(keyword)"
          @change="toggleKeyword(keyword)"
        >
          {{ keyword }}
        </el-check-tag>
      </div>
      <div class="custom-keyword">
        <el-input
          v-model="customKeyword"
          placeholder="补充关键词，例如：画板、球拍"
          maxlength="30"
          clearable
          @keyup.enter="addCustomKeyword"
        />
        <el-button @click="addCustomKeyword">添加</el-button>
      </div>
    </section>

    <div class="review-panel">
      <el-table
        v-loading="loading"
        :data="products"
        row-key="id"
        class="review-table"
        empty-text="暂无待审核商品"
      >
        <el-table-column label="商品" min-width="320">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image
                class="thumb"
                :src="firstImage(row)"
                fit="cover"
                :preview-src-list="row.imageUrls || []"
                preview-teleported
              >
                <template #error>
                  <div class="thumb-empty">无图</div>
                </template>
              </el-image>
              <div class="product-main">
                <strong>{{ row.title }}</strong>
                <span>{{ row.categoryName || '未分类' }} · {{ conditionLabel(row.conditionLevel) }}</span>
                <div v-if="row.tagNames?.length" class="tag-line">
                  <el-tag v-for="tagName in row.tagNames" :key="tagName" size="small" effect="plain">
                    {{ tagName }}
                  </el-tag>
                </div>
                <div v-else class="tag-line empty">暂无标签</div>
                <p>{{ row.description || '暂无描述' }}</p>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="价格" width="110">
          <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>

        <el-table-column label="交易方式" width="150">
          <template #default="{ row }">
            <el-tag v-for="mode in row.tradeModes" :key="mode" size="small" class="tag" effect="plain">
              {{ tradeModeLabel(mode) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>

        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag type="warning" effect="plain">{{ productStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" :loading="actingId === row.id" @click="handleApprove(row.id)">
              通过
            </el-button>
            <el-button type="danger" size="small" :disabled="actingId === row.id" @click="handleReject(row.id)">
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { approveProduct, bulkApproveProducts, listPendingProducts, rejectProduct } from '../../api/admin'
import type { ProductItem } from '../../api/product'
import { formatDateTime } from '../../utils/adminLabels'
import { getApiErrorMessage } from '../../utils/apiError'

const products = ref<ProductItem[]>([])
const loading = ref(false)
const actingId = ref<number | null>(null)
const keywordPresets = ref([
  '教材',
  '课本',
  '习题集',
  '复习资料',
  '笔记',
  '计算器',
  '台灯',
  '耳机',
  '键盘',
  '鼠标',
  '保护壳',
  '自行车',
  '篮球',
  '文具',
])
const selectedKeywords = ref<string[]>(['教材', '课本', '复习资料', '计算器', '台灯', '耳机', '键盘', '鼠标'])
const customKeyword = ref('')
const bulkLoading = ref(false)

onMounted(loadProducts)

async function loadProducts() {
  loading.value = true
  try {
    const response = await listPendingProducts()
    if (response.code !== 0) {
      throw new Error(response.message || '加载待审核商品失败')
    }
    products.value = response.data
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载待审核商品失败'))
  } finally {
    loading.value = false
  }
}

async function handleApprove(productId: number) {
  actingId.value = productId
  try {
    const response = await approveProduct(productId)
    if (response.code !== 0) {
      throw new Error(response.message || '审核通过失败')
    }
    products.value = products.value.filter((item) => item.id !== productId)
    ElMessage.success('商品已审核通过')
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '审核通过失败'))
  } finally {
    actingId.value = null
  }
}

async function handleBulkApprove() {
  bulkLoading.value = true
  try {
    const response = await bulkApproveProducts(selectedKeywords.value)
    if (response.code !== 0) {
      throw new Error(response.message || '批量审核失败')
    }
    const approvedIds = new Set(response.data.map((item) => item.id))
    products.value = products.value.filter((item) => !approvedIds.has(item.id))
    ElMessage.success(response.data.length ? `已自动通过 ${response.data.length} 个商品` : '没有商品命中当前关键词')
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '批量审核失败'))
  } finally {
    bulkLoading.value = false
  }
}

function toggleKeyword(keyword: string) {
  selectedKeywords.value = selectedKeywords.value.includes(keyword)
    ? selectedKeywords.value.filter((item) => item !== keyword)
    : [...selectedKeywords.value, keyword]
}

function addCustomKeyword() {
  const keyword = customKeyword.value.trim()
  if (!keyword) return
  if (!keywordPresets.value.includes(keyword)) keywordPresets.value.push(keyword)
  if (!selectedKeywords.value.includes(keyword)) selectedKeywords.value.push(keyword)
  customKeyword.value = ''
}

async function handleReject(productId: number) {
  try {
    await ElMessageBox.confirm('确认拒绝该商品审核？卖家可修改后重新提交。', '拒绝商品', { type: 'warning' })
  } catch {
    return
  }
  actingId.value = productId
  try {
    const response = await rejectProduct(productId)
    if (response.code !== 0) {
      throw new Error(response.message || '拒绝商品失败')
    }
    products.value = products.value.filter((item) => item.id !== productId)
    ElMessage.success('商品已拒绝')
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '拒绝商品失败'))
  } finally {
    actingId.value = null
  }
}

function firstImage(product: ProductItem) {
  return product.imageUrls?.[0] || ''
}

function productStatusLabel(status: string) {
  if (status === 'PENDING_REVIEW') return '待审核'
  if (status === 'ACTIVE') return '已上架'
  if (status === 'REVIEW_REJECTED') return '已拒绝'
  if (status === 'DRAFT') return '草稿'
  if (status === 'LOCKED') return '交易锁定'
  if (status === 'SOLD') return '已售出'
  if (status === 'OFFLINE') return '已下架'
  return status
}

function conditionLabel(condition: string) {
  if (condition === 'NEW') return '全新'
  if (condition === 'LIKE_NEW') return '几乎全新'
  if (condition === 'GOOD') return '轻微使用'
  if (condition === 'FAIR') return '明显使用'
  return condition
}

function tradeModeLabel(mode: string) {
  if (mode === 'MEETUP') return '面交'
  if (mode === 'LOCKER') return '柜机'
  if (mode === 'DELIVERY') return '配送'
  return mode
}
</script>

<style scoped>
.product-review-page {
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
.review-panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px;
}
.bulk-panel {
  background: #fff;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  padding: 14px;
}
.bulk-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 12px;
}
.bulk-head h2 {
  margin: 0;
  font-size: 16px;
  color: #0f172a;
}
.bulk-head p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}
.keyword-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.custom-keyword {
  display: flex;
  max-width: 420px;
  gap: 8px;
  margin-top: 12px;
}
.review-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
}
.product-cell {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.thumb {
  width: 72px;
  height: 72px;
  flex: 0 0 72px;
  border-radius: 6px;
  background: #f1f5f9;
}
.thumb-empty {
  width: 72px;
  height: 72px;
  display: grid;
  place-items: center;
  color: #94a3b8;
  font-size: 12px;
}
.product-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.product-main strong {
  color: #0f172a;
  font-size: 14px;
}
.product-main span {
  color: #64748b;
  font-size: 12px;
}
.tag-line {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.tag-line.empty {
  color: #94a3b8;
  font-size: 12px;
}
.product-main p {
  margin: 0;
  color: #475569;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.tag {
  margin: 2px 4px 2px 0;
}
@media (max-width: 760px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
