<template>
  <section class="product-detail-page">
    <el-button text :icon="ArrowLeft" @click="$router.push('/products')">返回商品列表</el-button>

    <el-skeleton v-if="loading" :rows="8" animated />

    <template v-else-if="product">
      <div class="detail-grid">
        <div class="gallery">
          <div class="main-image">
            <img v-if="activeImage" :src="resolveMediaUrl(activeImage)" :alt="product.title" />
            <el-icon v-else><Picture /></el-icon>
          </div>
          <div v-if="product.imageUrls?.length" class="thumb-list">
            <button
              v-for="url in product.imageUrls"
              :key="url"
              type="button"
              :class="{ active: url === activeImage }"
              @click="activeImage = url"
            >
              <img :src="resolveMediaUrl(url)" :alt="product.title" />
            </button>
          </div>
        </div>

        <div class="summary">
          <div class="title-line">
            <h1>{{ product.title }}</h1>
            <el-tag :type="statusType(product.status)">{{ statusText(product.status) }}</el-tag>
          </div>
          <div class="price">¥{{ money(product.price) }}</div>
          <div v-if="product.originalPrice" class="original">原价 ¥{{ money(product.originalPrice) }}</div>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="分类">{{ product.categoryName || product.categoryId }}</el-descriptions-item>
            <el-descriptions-item label="成色">{{ conditionLabel(product.conditionLevel) }}</el-descriptions-item>
            <el-descriptions-item label="校区">{{ product.campus || '校内' }}</el-descriptions-item>
            <el-descriptions-item label="交易方式">{{ tradeModeText(product.tradeModes) }}</el-descriptions-item>
            <el-descriptions-item label="浏览/收藏">
              {{ product.viewCount ?? 0 }} / {{ product.favoriteCount ?? 0 }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="actions">
            <el-button
              type="danger"
              :icon="ShoppingCart"
              :loading="orderLoading"
              :disabled="!canBuy"
              @click="openBuyDialog"
            >
              购买
            </el-button>
            <el-button type="primary" :icon="ChatDotRound" @click="contactSeller">联系卖家</el-button>
            <el-button :icon="Star" :loading="favoriteLoading" @click="toggleFavorite">
              {{ product.favorited ? '取消收藏' : '收藏' }}
            </el-button>
          </div>
          <p v-if="!canBuy" class="hint">只有已上架商品可以购买，不能购买自己发布的商品。</p>
        </div>
      </div>

      <div class="description">
        <h2>商品描述</h2>
        <p>{{ product.description || '卖家暂未填写描述。' }}</p>
      </div>
    </template>

    <el-empty v-else description="商品不存在或已下架" />

    <el-dialog v-model="buyDialogVisible" title="确认购买" width="420px">
      <el-form label-position="top">
        <el-form-item label="交易方式">
          <el-radio-group v-model="selectedTradeMode">
            <el-radio-button v-if="supportsTradeMode('MEETUP')" value="MEETUP">线下面交</el-radio-button>
            <el-radio-button v-if="supportsTradeMode('LOCKER')" value="LOCKER">柜机中转</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <p class="order-note">提交后将创建订单，后续支付、交付和评价在订单详情页完成。</p>
      </el-form>
      <template #footer>
        <el-button @click="buyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="orderLoading" @click="submitOrder">创建订单</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ChatDotRound, Picture, ShoppingCart, Star } from '@element-plus/icons-vue'
import { favoriteProduct, getProduct, recordProductView, unfavoriteProduct, type ProductItem } from '../../api/product'
import { createOrder } from '../../api/order'
import { useAuthStore } from '../../stores/auth'
import { openOrCreateSession } from '../../stores/chat'
import { getApiErrorMessage } from '../../utils/apiError'
import { resolveMediaUrl } from '../../utils/media'

const props = defineProps<{ id: string }>()
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const favoriteLoading = ref(false)
const orderLoading = ref(false)
const buyDialogVisible = ref(false)
const selectedTradeMode = ref('MEETUP')
const product = ref<ProductItem | null>(null)
const activeImage = ref('')

const canBuy = computed(() => {
  if (!product.value) return false
  return product.value.status === 'ACTIVE' && product.value.sellerId !== auth.userId
})

onMounted(loadProduct)

async function loadProduct() {
  const id = Number(props.id)
  if (!Number.isFinite(id)) return
  loading.value = true
  try {
    const response = await getProduct(id)
    if (response.code !== 0) throw new Error(response.message)
    product.value = response.data
    activeImage.value = response.data.imageUrls?.[0] ?? ''
    selectedTradeMode.value = response.data.tradeModes?.includes('MEETUP') ? 'MEETUP' : response.data.tradeModes?.[0] ?? 'MEETUP'
    try {
      await recordProductView(id)
    } catch {
      // 记录浏览失败不影响详情展示。
    }
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载商品详情失败'))
  } finally {
    loading.value = false
  }
}

function openBuyDialog() {
  if (!product.value) return
  if (!auth.isLoggedIn) {
    goLogin()
    return
  }
  if (auth.isAdmin) {
    ElMessage.warning('管理员账号不参与购买，请使用普通用户账号')
    return
  }
  if (!ensureVerified()) return
  buyDialogVisible.value = true
}

async function submitOrder() {
  if (!product.value) return
  orderLoading.value = true
  try {
    const response = await createOrder(product.value.id, selectedTradeMode.value)
    if (response.code !== 0) throw new Error(response.message)
    ElMessage.success('订单已创建')
    buyDialogVisible.value = false
    await router.push(`/orders/${response.data.id}`)
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '创建订单失败'))
  } finally {
    orderLoading.value = false
  }
}

async function toggleFavorite() {
  if (!product.value) return
  if (!auth.isLoggedIn) {
    await goLogin()
    return
  }
  favoriteLoading.value = true
  try {
    if (product.value.favorited) {
      await unfavoriteProduct(product.value.id)
      product.value.favorited = false
      product.value.favoriteCount = Math.max(0, (product.value.favoriteCount ?? 0) - 1)
    } else {
      await favoriteProduct(product.value.id)
      product.value.favorited = true
      product.value.favoriteCount = (product.value.favoriteCount ?? 0) + 1
    }
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '收藏操作失败'))
  } finally {
    favoriteLoading.value = false
  }
}

async function contactSeller() {
  if (!product.value) return
  if (!auth.isLoggedIn) {
    await goLogin()
    return
  }
  if (auth.isAdmin) {
    ElMessage.warning('管理员账号不参与用户聊天，请使用普通用户账号联系卖家')
    return
  }
  if (!ensureVerified()) return
  try {
    const session = await openOrCreateSession(product.value.sellerId, product.value.id)
    await router.push({ path: '/chat', query: { sessionId: String(session.id) } })
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '无法发起聊天'))
  }
}

function ensureVerified() {
  if (auth.isVerified) return true
  ElMessage.warning('请先完成学生认证')
  router.push({ path: '/verify', query: { redirect: `/products/${props.id}` } })
  return false
}

async function goLogin() {
  try {
    await ElMessageBox.confirm('需要登录后才能继续操作，是否前往登录？', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await router.push({ path: '/login', query: { redirect: `/products/${props.id}` } })
  } catch {
    // cancelled
  }
}

function supportsTradeMode(mode: string) {
  return product.value?.tradeModes?.includes(mode)
}

function money(value?: number) {
  return Number(value ?? 0).toFixed(2)
}

function conditionLabel(value?: string) {
  const map: Record<string, string> = { NEW: '全新', LIKE_NEW: '九成新', GOOD: '良好', NORMAL: '普通' }
  return value ? map[value] ?? value : '未知'
}

function tradeModeText(values?: string[]) {
  if (!values?.length) return '面交'
  return values.map((value) => (value === 'LOCKER' ? '柜机中转' : '线下面交')).join(' / ')
}

function statusText(value?: string) {
  const map: Record<string, string> = {
    ACTIVE: '已上架',
    PENDING_REVIEW: '待审核',
    REVIEW_REJECTED: '审核拒绝',
    LOCKED: '交易中',
    SOLD: '已售出',
    OFFLINE: '已下架',
  }
  return value ? map[value] ?? value : '未知'
}

function statusType(value?: string) {
  if (value === 'ACTIVE') return 'success'
  if (value === 'PENDING_REVIEW') return 'warning'
  if (value === 'SOLD' || value === 'LOCKED') return 'info'
  return 'danger'
}
</script>

<style scoped>
.product-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.detail-grid {
  display: grid;
  grid-template-columns: minmax(320px, 1.1fr) minmax(320px, 0.9fr);
  gap: 18px;
}
.gallery,
.summary,
.description {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
}
.main-image {
  aspect-ratio: 4 / 3;
  background: #f1f5f9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 44px;
  overflow: hidden;
}
.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(74px, 1fr));
  gap: 8px;
  margin-top: 10px;
}
.thumb-list button {
  border: 2px solid transparent;
  background: #f8fafc;
  border-radius: 6px;
  padding: 0;
  aspect-ratio: 1;
  overflow: hidden;
  cursor: pointer;
}
.thumb-list button.active {
  border-color: #409eff;
}
.thumb-list img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.title-line {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.title-line h1 {
  margin: 0;
  font-size: 26px;
  line-height: 1.3;
}
.price {
  margin-top: 16px;
  color: #ef4444;
  font-size: 30px;
  font-weight: 700;
}
.original {
  color: #94a3b8;
  text-decoration: line-through;
  margin-bottom: 16px;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}
.hint,
.order-note {
  color: #64748b;
  line-height: 1.6;
}
.hint {
  margin: 10px 0 0;
}
.description h2 {
  margin: 0 0 10px;
  font-size: 20px;
}
.description p {
  margin: 0;
  color: #475569;
  line-height: 1.8;
  white-space: pre-wrap;
}
@media (max-width: 860px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
