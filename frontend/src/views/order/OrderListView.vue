<template>
  <section class="order-list">
    <header class="page-header">
      <div>
        <p class="eyebrow">交易中心</p>
        <h1>我的订单</h1>
      </div>
      <el-radio-group v-model="role" @change="loadOrders">
        <el-radio-button value="buyer">我买的</el-radio-button>
        <el-radio-button value="seller">我卖的</el-radio-button>
      </el-radio-group>
    </header>

    <el-skeleton v-if="loading" :rows="4" animated />
    <el-empty v-else-if="orders.length === 0" description="暂无订单" />

    <div v-else class="order-cards">
      <article v-for="order in orders" :key="order.id" class="order-card" @click="$router.push(`/orders/${order.id}`)">
        <div class="product-thumb">
          <img v-if="order.productImageUrl" :src="resolveMediaUrl(order.productImageUrl)" :alt="order.productTitle" />
          <el-icon v-else><Picture /></el-icon>
        </div>
        <div class="order-info">
          <div class="title-line">
            <h2>{{ order.productTitle || `商品 #${order.productId}` }}</h2>
            <el-tag :type="statusType(order.status)">{{ statusLabel(order.status) }}</el-tag>
          </div>
          <div class="meta-line">
            <span>订单号 {{ order.orderNo }}</span>
            <span>{{ tradeModeLabel(order.tradeMode) }}</span>
            <span>{{ formatDate(order.createdAt) }}</span>
          </div>
          <div class="price-line">
            <strong>¥{{ money(order.amount) }}</strong>
            <small>{{ role === 'buyer' ? `卖家 #${order.sellerId}` : `买家 #${order.buyerId}` }}</small>
          </div>
        </div>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ArrowRight, Picture } from '@element-plus/icons-vue'
import { listMyOrders, type Order } from '../../api/order'
import { resolveMediaUrl } from '../../utils/media'

const role = ref<'buyer' | 'seller'>('buyer')
const orders = ref<Order[]>([])
const loading = ref(false)

async function loadOrders() {
  loading.value = true
  try {
    const res = await listMyOrders(role.value)
    if (res.code === 0) orders.value = res.data
  } finally {
    loading.value = false
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
    DISPUTED: 'danger',
    CLOSED: 'info',
  }
  return map[status] ?? ''
}

function tradeModeLabel(mode: string) {
  return mode === 'LOCKER' ? '柜机中转' : '面交'
}

function formatDate(dateStr: string) {
  return dateStr?.replace('T', ' ').substring(0, 16) ?? ''
}

function money(value?: number) {
  return Number(value ?? 0).toFixed(2)
}

onMounted(loadOrders)
</script>

<style scoped>
.order-list {
  max-width: 920px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}
.page-header h1 {
  margin: 4px 0 0;
  font-size: 28px;
}
.order-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.order-card {
  display: grid;
  grid-template-columns: 112px 1fr auto;
  gap: 14px;
  align-items: center;
  padding: 12px;
  border: 1px solid var(--bfu-border);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  box-shadow: 0 8px 22px rgba(7, 59, 42, 0.04);
}
.order-card:hover {
  border-color: var(--bfu-green-500);
}
.product-thumb {
  width: 112px;
  aspect-ratio: 1;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 8px;
  background: var(--bfu-leaf-50);
  color: var(--bfu-green-300);
  font-size: 30px;
}
.product-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.order-info {
  min-width: 0;
}
.title-line {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: flex-start;
}
.title-line h2 {
  margin: 0;
  font-size: 18px;
  line-height: 1.35;
}
.meta-line {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 8px 0;
  color: var(--bfu-muted);
  font-size: 13px;
}
.price-line {
  display: flex;
  align-items: center;
  gap: 12px;
}
.price-line strong {
  color: var(--bfu-price);
  font-size: 22px;
}
.price-line small {
  color: var(--bfu-muted);
}
.arrow {
  color: var(--bfu-muted);
}
@media (max-width: 700px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }
  .order-card {
    grid-template-columns: 86px 1fr;
  }
  .product-thumb {
    width: 86px;
  }
  .arrow {
    display: none;
  }
}
</style>
