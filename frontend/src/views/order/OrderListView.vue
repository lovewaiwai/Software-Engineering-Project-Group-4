<template>
  <div class="order-list">
    <div class="page-header">
      <h2>我的订单</h2>
      <el-radio-group v-model="role" @change="loadOrders">
        <el-radio-button value="buyer">我买的</el-radio-button>
        <el-radio-button value="seller">我卖的</el-radio-button>
      </el-radio-group>
    </div>

    <el-skeleton v-if="loading" :rows="4" animated />

    <el-empty v-else-if="orders.length === 0" description="暂无订单" />

    <div v-else class="order-cards">
      <el-card
          v-for="order in orders"
          :key="order.id"
          class="order-card"
          shadow="hover"
          @click="$router.push(`/orders/${order.id}`)"
      >
        <div class="order-card-inner">
          <div class="order-info">
            <div class="order-no">订单号：{{ order.orderNo }}</div>
            <div class="order-meta">
              <span>金额：¥{{ order.amount }}</span>
              <span>交易方式：{{ tradeModeLabel(order.tradeMode) }}</span>
              <span>下单时间：{{ formatDate(order.createdAt) }}</span>
            </div>
          </div>
          <div class="order-right">
            <el-tag :type="statusType(order.status)">{{ statusLabel(order.status) }}</el-tag>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { listMyOrders, type Order } from '../../api/order'

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

onMounted(loadOrders)
</script>

<style scoped>
.order-list {
  max-width: 800px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}
.order-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.order-card {
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.order-card-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.order-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.order-no {
  font-weight: 600;
  color: #334155;
}
.order-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
}
.order-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.arrow {
  color: #94a3b8;
}
</style>