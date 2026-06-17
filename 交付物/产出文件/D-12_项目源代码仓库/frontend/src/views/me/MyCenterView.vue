<template>
  <section class="me-page">
    <div class="me-content">
      <div class="me-card">
        <div class="avatar">{{ initial }}</div>
        <div>
          <strong>{{ auth.displayName }}</strong>
          <span>{{ auth.isVerified ? '已完成实名' : '未实名' }}</span>
        </div>
      </div>

      <div class="page-head">
        <div>
          <h1>我的</h1>
          <p>管理发布、收藏、浏览历史、订单和个人主页。</p>
        </div>
        <el-button type="primary" @click="router.push('/products/new')">发布闲置</el-button>
      </div>

      <div class="entry-grid">
        <article v-for="item in entries" :key="item.title" class="entry-card" @click="router.push(item.path)">
          <el-icon><component :is="item.icon" /></el-icon>
          <div>
            <h2>{{ item.title }}</h2>
            <p>{{ item.description }}</p>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Clock, Goods, Star, Tickets, User } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const profilePath = computed(() => (auth.userId ? `/profile/${auth.userId}` : '/login'))
const initial = computed(() => auth.displayName?.slice(0, 1).toUpperCase() || '我')

const entries = computed(() => [
  {
    title: '我的发布',
    description: '查看草稿、待审核、已上架、已下架和已售出的商品。',
    path: '/products/mine',
    icon: Goods,
  },
  {
    title: '我的收藏',
    description: '找回已经收藏的商品，继续查看详情或联系卖家。',
    path: '/products/favorites',
    icon: Star,
  },
  {
    title: '浏览历史',
    description: '按最近浏览时间回看打开过的闲置商品。',
    path: '/products/history',
    icon: Clock,
  },
  {
    title: '我的订单',
    description: '查看买入和卖出的交易记录与订单状态。',
    path: '/orders',
    icon: Tickets,
  },
  {
    title: '个人主页',
    description: '查看与编辑个人资料、实名信息和信用状态。',
    path: profilePath.value,
    icon: User,
  },
])
</script>

<style scoped>
.me-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.me-card,
.entry-card {
  border: 1px solid var(--bfu-border);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 6px 18px rgba(7, 59, 42, 0.04);
}
.me-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
}
.avatar {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: var(--bfu-green-700);
  color: #fff;
  font-weight: 800;
}
.me-card strong,
.me-card span {
  display: block;
}
.me-card span {
  margin-top: 3px;
  color: var(--bfu-muted);
  font-size: 13px;
}
.me-content {
  min-width: 0;
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
  font-size: 28px;
}
.page-head p {
  margin: 6px 0 0;
  color: var(--bfu-muted);
}
.entry-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}
.entry-card {
  min-height: 128px;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  cursor: pointer;
}
.entry-card:hover {
  border-color: var(--bfu-green-500);
  transform: translateY(-2px);
}
.entry-card > .el-icon {
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  border-radius: 8px;
  background: var(--bfu-green-100);
  color: var(--bfu-green-800);
  font-size: 20px;
}
.entry-card h2 {
  margin: 0;
  font-size: 18px;
}
.entry-card p {
  margin: 8px 0 0;
  color: var(--bfu-muted);
  line-height: 1.5;
}
@media (max-width: 860px) {
  .page-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
