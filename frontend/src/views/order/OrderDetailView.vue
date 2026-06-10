<template>
  <div class="order-detail">
    <el-page-header @back="$router.push('/orders')" content="订单详情" />

    <el-skeleton v-if="loading" :rows="6" animated style="margin-top: 20px" />

    <template v-else-if="order">
      <!-- 订单基本信息 -->
      <el-card class="section-card" style="margin-top: 20px">
        <template #header>
          <span class="card-title">订单信息</span>
          <el-tag :type="statusType(order.status)" style="margin-left: 12px">
            {{ statusLabel(order.status) }}
          </el-tag>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ order.amount }}</el-descriptions-item>
          <el-descriptions-item label="交易方式">{{ tradeModeLabel(order.tradeMode) }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDate(order.createdAt) }}</el-descriptions-item>
          <el-descriptions-item v-if="order.completedAt" label="完成时间">
            {{ formatDate(order.completedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 操作按钮 -->
      <el-card class="section-card">
        <template #header><span class="card-title">操作</span></template>
        <div class="action-buttons">
          <!-- 买家：待支付 → 去支付 -->
          <el-button
              v-if="isBuyer && order.status === 'CREATED'"
              type="primary"
              :loading="actionLoading"
              @click="handlePay"
          >
            去支付
          </el-button>

          <!-- 模拟支付回调（测试用） -->
          <el-button
              v-if="isBuyer && order.status === 'CREATED' && payment?.providerTradeNo"
              type="success"
              :loading="actionLoading"
              @click="handleMockCallback"
          >
            模拟支付成功
          </el-button>

          <!-- 买家：卖家已确认 面交 → 确认收货 或 申请退款 -->
          <el-button
              v-if="isBuyer && order.status === 'SELLER_CONFIRMED' && order.tradeMode === 'MEETUP'"
              type="success"
              :loading="actionLoading"
              @click="handleComplete"
          >
            确认收货
          </el-button>
          <el-button
              v-if="isBuyer && ['PAID', 'SELLER_CONFIRMED', 'DELIVERY_PENDING'].includes(order.status)"
              type="danger"
              plain
              :loading="actionLoading"
              @click="handleRefund"
          >
            申请退款
          </el-button>
          <!-- 卖家：已支付 → 确认或拒绝 -->
          <el-button
              v-if="isSeller && order.status === 'PAID'"
              type="success"
              :loading="actionLoading"
              @click="handleSellerConfirm"
          >
            确认订单
          </el-button>
          <el-button
              v-if="isSeller && order.status === 'PAID'"
              type="danger"
              plain
              :loading="actionLoading"
              @click="handleSellerReject"
          >
            拒绝订单（退款给买家）
          </el-button>
          <!-- 卖家：已确认 柜机 → 预约柜机 -->
          <el-button
              v-if="isSeller && order.status === 'SELLER_CONFIRMED' && order.tradeMode === 'LOCKER' && !delivery"
              type="primary"
              :loading="actionLoading"
              @click="handleReserveLocker"
          >
            预约柜机
          </el-button>

          <!-- 卖家：已预约 → 确认存入 -->
          <el-button
              v-if="isSeller && delivery?.status === 'RESERVED'"
              type="primary"
              :loading="actionLoading"
              @click="handleConfirmStored"
          >
            确认存入柜机
          </el-button>

          <!-- 买家：已存入 → 取件 -->
          <el-button
              v-if="isBuyer && delivery?.status === 'STORED'"
              type="success"
              :loading="actionLoading"
              @click="pickupDialogVisible = true"
          >
            输入取件码取货
          </el-button>

<!--          &lt;!&ndash; 买家：已取件（订单已完成）→ 退款 &ndash;&gt;-->
<!--          <el-button-->
<!--              v-if="isBuyer && ['PAID', 'SELLER_CONFIRMED', 'DELIVERY_PENDING'].includes(order.status)"-->
<!--              type="danger"-->
<!--              plain-->
<!--              :loading="actionLoading"-->
<!--              @click="handleRefund"-->
<!--          >-->
<!--            申请退款-->
<!--          </el-button>-->

          <!-- 买家：待支付 → 取消订单 -->
          <el-button
              v-if="isBuyer && order.status === 'CREATED'"
              plain
              :loading="actionLoading"
              @click="handleCancel"
          >
            取消订单
          </el-button>

          <!-- 已完成 → 评价 -->
          <el-button
              v-if="order.status === 'COMPLETED' && !hasReviewed"
              type="warning"
              @click="reviewDialogVisible = true"
          >
            评价
          </el-button>
        </div>
      </el-card>

      <!-- 支付信息 -->
      <el-card v-if="payment" class="section-card">
        <template #header><span class="card-title">支付信息</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="支付单号">{{ payment.paymentNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ payStatusLabel(payment.status) }}</el-descriptions-item>
          <el-descriptions-item v-if="payment.paidAt" label="支付时间">
            {{ formatDate(payment.paidAt) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="payment.payUrl" label="支付链接">
            <el-text type="info" size="small">{{ payment.payUrl }}</el-text>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 柜机信息 -->
      <el-card v-if="delivery" class="section-card">
        <template #header><span class="card-title">柜机信息</span></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务编号">{{ delivery.taskNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ deliveryStatusLabel(delivery.status) }}</el-descriptions-item>
          <el-descriptions-item label="取件码">
            <el-tag type="danger" size="large">{{ delivery.pickupCode }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="delivery.storedAt" label="存入时间">
            {{ formatDate(delivery.storedAt) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="delivery.pickedUpAt" label="取出时间">
            {{ formatDate(delivery.pickedUpAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 评价列表 -->
      <el-card v-if="reviews.length > 0" class="section-card">
        <template #header><span class="card-title">评价</span></template>
        <div v-for="review in reviews" :key="review.id" class="review-item">
          <div class="review-header">
            <el-tag size="small" type="info">
              {{ review.reviewerId === order?.buyerId ? '买家评价' : '卖家评价' }}
            </el-tag>
            <el-rate :model-value="review.rating" disabled />
          </div>
          <p class="review-content">{{ review.content }}</p>
          <p class="review-time">{{ formatDate(review.createdAt) }}</p>
        </div>
      </el-card>
    </template>

    <!-- 取件码弹窗 -->
    <el-dialog v-model="pickupDialogVisible" title="输入取件码" width="360px">
      <el-input v-model="pickupCodeInput" placeholder="请输入6位取件码" maxlength="6" />
      <template #footer>
        <el-button @click="pickupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="handlePickup">确认取货</el-button>
      </template>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="reviewDialogVisible" title="评价交易" width="400px">
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="3"
              placeholder="分享你的交易体验..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="handleReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import {
  getOrder, cancelOrder, confirmComplete,
  sellerConfirmOrder, sellerRejectOrder,
  createPayment, mockPayCallback, refundPayment,
  getPaymentByOrder,
  reserveLocker, confirmStored, confirmPickedUp, getDeliveryByOrder,
  createReview, getReviewsByOrder,
  type Order, type Payment, type Delivery, type Review,
} from '../../api/order'


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
const hasReviewed = computed(() => reviews.value.some(r => r.reviewerId === auth.userId))

async function loadAll() {
  loading.value = true
  try {
    const orderId = Number(props.id)

    // 加载订单
    const res = await getOrder(orderId)
    if (res.code === 0) order.value = res.data

    // 加载支付信息
    try {
      const payRes = await getPaymentByOrder(orderId)
      if (payRes.code === 0 && payRes.data) payment.value = payRes.data
    } catch {}

    // 加载柜机信息
    if (order.value?.tradeMode === 'LOCKER') {
      try {
        const delRes = await getDeliveryByOrder(orderId)
        if (delRes.code === 0) delivery.value = delRes.data
      } catch {}
    }

    // 加载评价
    const revRes = await getReviewsByOrder(orderId)
    if (revRes.code === 0) reviews.value = revRes.data

  } finally {
    loading.value = false
  }
}

async function handlePay() {
  actionLoading.value = true
  try {
    const res = await createPayment(Number(props.id))
    if (res.code === 0) {
      payment.value = res.data
      ElMessage.success('支付单创建成功，请完成支付')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message ?? '操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleMockCallback() {
  if (!payment.value?.providerTradeNo) return
  actionLoading.value = true
  try {
    const res = await mockPayCallback(payment.value.providerTradeNo)
    if (res.code === 0) {
      payment.value = res.data
      ElMessage.success('支付成功')
      await loadAll()
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message ?? '操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleComplete() {
  await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'warning' })
  actionLoading.value = true
  try {
    const res = await confirmComplete(Number(props.id))
    if (res.code === 0) {
      order.value = res.data
      ElMessage.success('已确认收货')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message ?? '操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleCancel() {
  await ElMessageBox.confirm('确认取消订单？', '取消订单', { type: 'warning' })
  actionLoading.value = true
  try {
    const res = await cancelOrder(Number(props.id))
    if (res.code === 0) {
      order.value = res.data
      ElMessage.success('订单已取消')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message ?? '操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleRefund() {
  await ElMessageBox.confirm('确认申请退款？', '申请退款', { type: 'warning' })
  actionLoading.value = true
  try {
    const res = await refundPayment(Number(props.id))
    if (res.code === 0) {
      ElMessage.success('退款申请成功')
      await loadAll()
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message ?? '操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleReserveLocker() {
  actionLoading.value = true
  try {
    const res = await reserveLocker(Number(props.id), 1)
    if (res.code === 0) {
      delivery.value = res.data
      ElMessage.success('柜机预约成功')
      await loadAll()
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message ?? '操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleConfirmStored() {
  if (!delivery.value) return
  actionLoading.value = true
  try {
    const res = await confirmStored(delivery.value.taskNo)
    if (res.code === 0) {
      delivery.value = res.data
      ElMessage.success('已确认存入柜机')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message ?? '操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function handlePickup() {
  if (!delivery.value) return
  actionLoading.value = true
  try {
    const res = await confirmPickedUp(delivery.value.taskNo, pickupCodeInput.value)
    if (res.code === 0) {
      delivery.value = res.data
      pickupDialogVisible.value = false
      ElMessage.success('取货成功，订单已完成')
      await loadAll()
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message ?? '取件码错误或操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleReview() {
  actionLoading.value = true
  try {
    const res = await createReview(Number(props.id), reviewForm.value.rating, reviewForm.value.content)
    if (res.code === 0) {
      reviews.value.push(res.data)
      reviewDialogVisible.value = false
      ElMessage.success('评价成功')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message ?? '操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleSellerConfirm() {
  await ElMessageBox.confirm('确认接受这笔订单？', '确认订单', { type: 'warning' })
  actionLoading.value = true
  try {
    const res = await sellerConfirmOrder(Number(props.id))
    if (res.code === 0) {
      order.value = res.data
      ElMessage.success('已确认订单')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message ?? '操作失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleSellerReject() {
  await ElMessageBox.confirm('确认拒绝订单？买家支付金额将原路退回。', '拒绝订单', { type: 'warning' })
  actionLoading.value = true
  try {
    const res = await sellerRejectOrder(Number(props.id))
    if (res.code === 0) {
      order.value = res.data
      ElMessage.success('已拒绝订单，款项将退回买家')
      await loadAll()
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message ?? '操作失败')
  } finally {
    actionLoading.value = false
  }
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
  }
  return map[status] ?? ''
}

function payStatusLabel(status: string) {
  const map: Record<string, string> = {
    CREATED: '待支付', SUCCESS: '已支付', FAILED: '失败', REFUNDING: '退款中', REFUNDED: '已退款',
  }
  return map[status] ?? status
}

function deliveryStatusLabel(status: string) {
  const map: Record<string, string> = {
    RESERVED: '已预约', STORED: '已存入', PICKED_UP: '已取出', CANCELLED: '已取消',
  }
  return map[status] ?? status
}

function tradeModeLabel(mode: string) {
  return mode === 'LOCKER' ? '柜机中转' : '面交'
}

function formatDate(dateStr: string) {
  return dateStr?.replace('T', ' ').substring(0, 16) ?? ''
}

onMounted(loadAll)
</script>

<style scoped>
.order-detail {
  max-width: 800px;
  margin: 0 auto;
}
.section-card {
  margin-top: 16px;
}
.card-title {
  font-weight: 600;
  font-size: 15px;
}
.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.review-item {
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}
.review-item:last-child {
  border-bottom: none;
}
.review-content {
  margin: 6px 0;
  color: #334155;
}
.review-time {
  font-size: 12px;
  color: #94a3b8;
  margin: 0;
}
.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}
</style>