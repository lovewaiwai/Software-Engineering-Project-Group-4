<template>
  <section class="product-review">
    <div class="toolbar">
      <div>
        <h2>商品审核队列</h2>
        <p>只展示待审核商品，审核通过后商品会上架展示。</p>
      </div>
      <el-button type="primary" :loading="loading" @click="loadProducts">刷新</el-button>
    </div>

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
              <p>{{ row.description || '暂无描述' }}</p>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="价格" width="110">
        <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
      </el-table-column>

      <el-table-column label="交易方式" width="160">
        <template #default="{ row }">
          <el-tag v-for="mode in row.tradeModes" :key="mode" size="small" class="tag">
            {{ tradeModeLabel(mode) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="提交时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>

      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag type="warning">{{ productStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <el-button type="success" size="small" :loading="actingId === row.id" @click="handleApprove(row.id)">
            通过
          </el-button>
          <el-button type="danger" size="small" :disabled="actingId === row.id" @click="openReject(row)">
            拒绝
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="rejectDialogVisible" title="拒绝商品" width="420px">
      <el-form label-position="top">
        <el-form-item label="拒绝原因">
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="请填写拒绝原因，卖家可在商品记录中看到"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="actingId === rejectingProduct?.id" @click="handleReject">
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { approveProduct, listPendingProducts, rejectProduct } from '../../api/admin'
import type { ProductItem } from '../../api/product'
import { formatDateTime } from '../../utils/adminLabels'
import { getApiErrorMessage } from '../../utils/apiError'

const products = ref<ProductItem[]>([])
const loading = ref(false)
const actingId = ref<number | null>(null)
const rejectDialogVisible = ref(false)
const rejectingProduct = ref<ProductItem | null>(null)
const rejectReason = ref('')

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

function openReject(product: ProductItem) {
  rejectingProduct.value = product
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

async function handleReject() {
  const product = rejectingProduct.value
  const reason = rejectReason.value.trim()
  if (!product) return
  if (!reason) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  actingId.value = product.id
  try {
    const response = await rejectProduct(product.id, reason)
    if (response.code !== 0) {
      throw new Error(response.message || '拒绝商品失败')
    }
    products.value = products.value.filter((item) => item.id !== product.id)
    rejectDialogVisible.value = false
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
.product-review {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.toolbar h2 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
}

.toolbar p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.review-table {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
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
</style>
