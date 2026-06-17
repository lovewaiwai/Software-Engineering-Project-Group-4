<template>
  <section class="order-detail">
    <el-button text :icon="ArrowLeft" @click="$router.push('/orders')">返回订单</el-button>

    <el-skeleton v-if="loading" :rows="6" animated class="loading-box" />

    <template v-else-if="order">
      <section class="product-summary">
        <div class="product-thumb">
          <img v-if="order.productImageUrl" :src="resolveMediaUrl(order.productImageUrl)" :alt="order.productTitle" />
          <el-icon v-else><Picture /></el-icon>
        </div>
        <div class="summary-main">
          <div class="summary-top">
            <div>
              <p class="eyebrow">订单商品</p>
              <h1>{{ order.productTitle || `商品 #${order.productId}` }}</h1>
            </div>
            <el-tag :type="statusType(order.status)">{{ statusLabel(order.status) }}</el-tag>
          </div>
          <div class="price-line">¥{{ money(order.amount) }}</div>
          <div class="summary-meta">
            <span>订单号 {{ order.orderNo }}</span>
            <span>{{ tradeModeLabel(order.tradeMode) }}</span>
            <span>{{ formatDate(order.createdAt) }}</span>
          </div>
        </div>
      </section>

      <section class="section-card">
        <h2>下一步操作</h2>
        <div class="action-buttons">
          <el-button v-if="isBuyer && order.status === 'CREATED'" type="primary" :loading="actionLoading" @click="handlePay">去支付</el-button>
          <el-button v-if="isBuyer && order.status === 'CREATED' && payment?.providerTradeNo" type="success" :loading="actionLoading" @click="handleMockCallback">模拟支付成功</el-button>
          <el-button v-if="isBuyer && order.status === 'SELLER_CONFIRMED' && order.tradeMode === 'MEETUP'" type="success" :loading="actionLoading" @click="handleComplete">确认收货</el-button>
          <el-button v-if="isBuyer && ['PAID', 'SELLER_CONFIRMED', 'DELIVERY_PENDING'].includes(order.status)" type="danger" plain :loading="actionLoading" @click="handleRefund">申请退款</el-button>
          <el-button v-if="isSeller && order.status === 'PAID'" type="success" :loading="actionLoading" @click="handleSellerConfirm">确认订单</el-button>
          <el-button v-if="isSeller && order.status === 'PAID'" type="danger" plain :loading="actionLoading" @click="handleSellerReject">拒绝订单并退款</el-button>
          <el-button v-if="isSeller && order.status === 'SELLER_CONFIRMED' && order.tradeMode === 'LOCKER' && !delivery" type="primary" :loading="actionLoading" @click="handleReserveLocker">预约柜机</el-button>
          <el-button v-if="isSeller && delivery?.status === 'RESERVED'" type="primary" :loading="actionLoading" @click="handleConfirmStored">确认存入柜机</el-button>
          <el-button v-if="isBuyer && delivery?.status === 'STORED'" type="success" :loading="actionLoading" @click="pickupDialogVisible = true">输入取件码取货</el-button>
          <el-button v-if="isBuyer && order.status === 'CREATED'" plain :loading="actionLoading" @click="handleCancel">取消订单</el-button>
          <el-button v-if="order.status === 'COMPLETED' && !hasReviewed" type="warning" @click="reviewDialogVisible = true">评价</el-button>
          <span v-if="!hasPrimaryAction" class="no-action">当前状态暂无需要你处理的操作</span>
        </div>
      </section>

      <section class="info-grid">
        <div class="section-card">
          <h2>订单信息</h2>
          <dl>
            <dt>买家</dt><dd>#{{ order.buyerId }}</dd>
            <dt>卖家</dt><dd>#{{ order.sellerId }}</dd>
            <dt>商品状态</dt><dd>{{ order.productStatus || '未知' }}</dd>
            <dt v-if="order.completedAt">完成时间</dt><dd v-if="order.completedAt">{{ formatDate(order.completedAt) }}</dd>
          </dl>
        </div>

        <div v-if="payment" class="section-card">
          <h2>支付信息</h2>
          <dl>
            <dt>支付单号</dt><dd>{{ payment.paymentNo }}</dd>
            <dt>状态</dt><dd>{{ payStatusLabel(payment.status) }}</dd>
            <dt v-if="payment.paidAt">支付时间</dt><dd v-if="payment.paidAt">{{ formatDate(payment.paidAt) }}</dd>
          </dl>
        </div>

        <div v-if="delivery" class="section-card">
          <h2>柜机信息</h2>
          <dl>
            <dt>任务编号</dt><dd>{{ delivery.taskNo }}</dd>
            <dt>柜机站点</dt><dd>{{ delivery.stationName || `站点 #${delivery.stationId}` }}</dd>
            <dt>格口</dt><dd>{{ delivery.boxNo || `#${delivery.boxId}` }}</dd>
            <dt>状态</dt><dd>{{ deliveryStatusLabel(delivery.status) }}</dd>
            <dt>取件码</dt><dd><el-tag type="danger" size="large">{{ delivery.pickupCode }}</el-tag></dd>
          </dl>
        </div>
      </section>

      <section v-if="reviews.length > 0" class="section-card">
        <h2>交易评价</h2>
        <div v-for="review in reviews" :key="review.id" class="review-item">
          <div class="review-header">
            <el-tag size="small" type="info">{{ review.reviewerId === order?.buyerId ? '买家评价' : '卖家评价' }}</el-tag>
            <el-rate :model-value="review.rating" disabled />
          </div>
          <p>{{ review.content }}</p>
          <small>{{ formatDate(review.createdAt) }}</small>
        </div>
      </section>
    </template>

    <el-dialog v-model="pickupDialogVisible" title="输入取件码" width="360px">
      <el-input v-model="pickupCodeInput" placeholder="请输入 6 位取件码" maxlength="6" />
      <template #footer>
        <el-button @click="pickupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="handlePickup">确认取货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogVisible" title="评价交易" width="400px">
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="分享你的交易体验..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="handleReview">提交评价</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import {
  cancelOrder,
  confirmComplete,
  confirmPickedUp,
  confirmStored,
  createPayment,
  createReview,
  getDeliveryByOrder,
  getOrder,
  getPaymentByOrder,
  getReviewsByOrder,
  mockPayCallback,
  refundPayment,
  reserveLocker,
  sellerConfirmOrder,
  sellerRejectOrder,
  type Delivery,
  type Order,
  type Payment,
  type Review,
} from '../../api/order'
import { resolveMediaUrl } from '../../utils/media'

const props = defineProps<{ id: string }>()
const auth = useAuthStore()

const loading = ref(false)
const actionLoading = ref(false)
const order = ref<Order | null>(null)
const payment = ref<Payment | null>(null)
const delivery = ref<Delivery | null>(null)
const reviews = ref<Review[]>([])
const pickupDialogVisible = ref(false)
const pickupCodeInput = ref('')
const reviewDialogVisible = ref(false)
const reviewForm = ref({ rating: 5, content: '' })

const isBuyer = computed(() => order.value?.buyerId === auth.userId)
const isSeller = computed(() => order.value?.sellerId === auth.userId)
const hasReviewed = computed(() => reviews.value.some((item) => item.reviewerId === auth.userId))
const hasPrimaryAction = computed(() => {
  if (!order.value) return false
  return Boolean(
    (isBuyer.value && order.value.status === 'CREATED')
    || (isBuyer.value && order.value.status === 'SELLER_CONFIRMED' && order.value.tradeMode === 'MEETUP')
    || (isBuyer.value && ['PAID', 'SELLER_CONFIRMED', 'DELIVERY_PENDING'].includes(order.value.status))
    || (isSeller.value && order.value.status === 'PAID')
    || (isSeller.value && order.value.status === 'SELLER_CONFIRMED' && order.value.tradeMode === 'LOCKER' && !delivery.value)
    || (isSeller.value && delivery.value?.status === 'RESERVED')
    || (isBuyer.value && delivery.value?.status === 'STORED')
    || (order.value.status === 'COMPLETED' && !hasReviewed.value),
  )
})

async function loadAll() {
  loading.value = true
  try {
    const orderId = Number(props.id)
    const res = await getOrder(orderId)
    if (res.code === 0) order.value = res.data

    try {
      const payRes = await getPaymentByOrder(orderId)
      if (payRes.code === 0 && payRes.data) payment.value = payRes.data
    } catch {
      payment.value = null
    }

    if (order.value?.tradeMode === 'LOCKER') {
      try {
        const delRes = await getDeliveryByOrder(orderId)
        if (delRes.code === 0) delivery.value = delRes.data
      } catch {
        delivery.value = null
      }
    }

    const revRes = await getReviewsByOrder(orderId)
    if (revRes.code === 0) reviews.value = revRes.data
  } finally {
    loading.value = false
  }
}

async function runAction(action: () => Promise<void>) {
  actionLoading.value = true
  try {
    await action()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message ?? '操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function handlePay() {
  await runAction(async () => {
    const res = await createPayment(Number(props.id))
    if (res.code === 0) {
      payment.value = res.data
      ElMessage.success('支付单创建成功，请完成支付')
    }
  })
}

async function handleMockCallback() {
  if (!payment.value?.providerTradeNo) return
  await runAction(async () => {
    const res = await mockPayCallback(payment.value!.providerTradeNo!)
    if (res.code === 0) {
      payment.value = res.data
      ElMessage.success('支付成功')
      await loadAll()
    }
  })
}

async function handleComplete() {
  await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'warning' })
  await runAction(async () => {
    const res = await confirmComplete(Number(props.id))
    if (res.code === 0) {
      order.value = res.data
      ElMessage.success('已确认收货')
    }
  })
}

async function handleCancel() {
  await ElMessageBox.confirm('确认取消订单？', '取消订单', { type: 'warning' })
  await runAction(async () => {
    const res = await cancelOrder(Number(props.id))
    if (res.code === 0) {
      order.value = res.data
      ElMessage.success('订单已取消')
    }
  })
}

async function handleRefund() {
  await ElMessageBox.confirm('确认申请退款？', '申请退款', { type: 'warning' })
  await runAction(async () => {
    const res = await refundPayment(Number(props.id))
    if (res.code === 0) {
      ElMessage.success('退款申请成功')
      await loadAll()
    }
  })
}

async function handleReserveLocker() {
  await runAction(async () => {
    const res = await reserveLocker(Number(props.id), 1)
    if (res.code === 0) {
      delivery.value = res.data
      ElMessage.success('柜机预约成功')
      await loadAll()
    }
  })
}

async function handleConfirmStored() {
  if (!delivery.value) return
  await runAction(async () => {
    const res = await confirmStored(delivery.value!.taskNo)
    if (res.code === 0) {
      delivery.value = res.data
      ElMessage.success('已确认存入柜机')
    }
  })
}

async function handlePickup() {
  if (!delivery.value) return
  await runAction(async () => {
    const res = await confirmPickedUp(delivery.value!.taskNo, pickupCodeInput.value)
    if (res.code === 0) {
      delivery.value = res.data
      pickupDialogVisible.value = false
      ElMessage.success('取货成功，订单已完成')
      await loadAll()
    }
  })
}

async function handleReview() {
  await runAction(async () => {
    const res = await createReview(Number(props.id), reviewForm.value.rating, reviewForm.value.content)
    if (res.code === 0) {
      reviews.value.push(res.data)
      reviewDialogVisible.value = false
      ElMessage.success('评价成功')
    }
  })
}

async function handleSellerConfirm() {
  await ElMessageBox.confirm('确认接受这笔订单？', '确认订单', { type: 'warning' })
  await runAction(async () => {
    const res = await sellerConfirmOrder(Number(props.id))
    if (res.code === 0) {
      order.value = res.data
      ElMessage.success('已确认订单')
    }
  })
}

async function handleSellerReject() {
  await ElMessageBox.confirm('确认拒绝订单？买家支付金额将原路退回。', '拒绝订单', { type: 'warning' })
  await runAction(async () => {
    const res = await sellerRejectOrder(Number(props.id))
    if (res.code === 0) {
      order.value = res.data
      ElMessage.success('已拒绝订单，款项将退回买家')
      await loadAll()
    }
  })
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    CREATED: '待支付',
    PAID: '已支付，待卖家确认',
    SELLER_CONFIRMED: '卖家已确认',
    SELLER_REJECTED: '卖家已拒绝',
    DELIVERY_PENDING: '配送中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUNDING: '退款中',
    REFUNDED: '已退款',
    DISPUTED: '争议中',
    CLOSED: '已关闭',
  }
  return map[status] ?? status
}

function statusType(status: string): 'success' | 'warning' | 'danger' | 'info' | '' {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info' | ''> = {
    CREATED: 'warning',
    PAID: 'warning',
    SELLER_CONFIRMED: '',
    SELLER_REJECTED: 'danger',
    DELIVERY_PENDING: '',
    COMPLETED: 'success',
    CANCELLED: 'info',
    REFUNDING: 'warning',
    REFUNDED: 'info',
    DISPUTED: 'danger',
    CLOSED: 'info',
  }
  return map[status] ?? ''
}

function payStatusLabel(status: string) {
  const map: Record<string, string> = {
    CREATED: '待支付',
    SUCCESS: '已支付',
    FAILED: '失败',
    REFUNDING: '退款中',
    REFUNDED: '已退款',
  }
  return map[status] ?? status
}

function deliveryStatusLabel(status: string) {
  const map: Record<string, string> = {
    RESERVED: '已预约',
    STORED: '已存入',
    PICKED_UP: '已取出',
    CANCELLED: '已取消',
  }
  return map[status] ?? status
}

function tradeModeLabel(mode: string) {
  return mode === 'LOCKER' ? '柜机中转' : '面交'
}

function formatDate(dateStr?: string) {
  return dateStr?.replace('T', ' ').substring(0, 16) ?? ''
}

function money(value?: number) {
  return Number(value ?? 0).toFixed(2)
}

onMounted(loadAll)
</script>

<style scoped>
.order-detail {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.loading-box {
  margin-top: 18px;
}
.product-summary,
.section-card {
  border: 1px solid var(--bfu-border);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(7, 59, 42, 0.04);
}
.product-summary {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 18px;
  padding: 16px;
}
.product-thumb {
  aspect-ratio: 1;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 8px;
  background: var(--bfu-leaf-50);
  color: var(--bfu-green-300);
  font-size: 42px;
}
.product-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.summary-main {
  min-width: 0;
}
.summary-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.summary-top h1 {
  margin: 4px 0 0;
  font-size: 28px;
  line-height: 1.25;
}
.price-line {
  margin-top: 14px;
  color: var(--bfu-price);
  font-size: 30px;
  font-weight: 800;
}
.summary-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  color: var(--bfu-muted);
}
.section-card {
  padding: 16px;
}
.section-card h2 {
  margin: 0 0 12px;
  font-size: 18px;
}
.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.no-action {
  color: var(--bfu-muted);
  align-self: center;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 14px;
}
dl {
  display: grid;
  grid-template-columns: 82px 1fr;
  gap: 8px 10px;
  margin: 0;
}
dt {
  color: var(--bfu-muted);
}
dd {
  margin: 0;
  color: var(--bfu-text);
}
.review-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--bfu-border);
}
.review-item:last-child {
  border-bottom: none;
}
.review-item p {
  margin: 6px 0;
}
.review-item small {
  color: var(--bfu-muted);
}
.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
@media (max-width: 720px) {
  .product-summary {
    grid-template-columns: 1fr;
  }
  .product-thumb {
    max-height: 280px;
  }
  .summary-top {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
