<template>
  <section class="admin-dashboard">
    <div class="welcome-banner">
      <div>
        <h2>平台运营看板</h2>
        <p>汇总用户、商品、交易与举报审核数据，便于掌握平台整体运行状况。</p>
      </div>
      <div class="banner-actions">
        <el-button size="large" @click="reload">刷新数据</el-button>
        <el-button type="primary" size="large" @click="$router.push('/admin/reports')">进入举报审核</el-button>
      </div>
    </div>

    <section class="metric-section">
      <header class="section-head">
        <h3>用户概况</h3>
        <span>普通用户账号统计</span>
      </header>
      <div class="stat-grid cols-4">
        <article v-for="item in userStats" :key="item.label" class="stat-card" :class="item.tone">
          <div class="stat-icon">{{ item.icon }}</div>
          <div class="stat-body">
            <p>{{ item.label }}</p>
            <strong>{{ item.value }}</strong>
            <small v-if="item.hint">{{ item.hint }}</small>
          </div>
        </article>
      </div>
    </section>

    <section class="metric-section">
      <header class="section-head">
        <h3>商品与交易</h3>
        <span>商品上架与订单成交情况</span>
      </header>
      <div class="stat-grid cols-4">
        <article v-for="item in tradeStats" :key="item.label" class="stat-card" :class="item.tone">
          <div class="stat-icon">{{ item.icon }}</div>
          <div class="stat-body">
            <p>{{ item.label }}</p>
            <strong>{{ item.value }}</strong>
            <small v-if="item.hint">{{ item.hint }}</small>
          </div>
        </article>
      </div>
    </section>

    <section class="metric-section">
      <header class="section-head">
        <h3>审核待办</h3>
        <span>聊天举报与今日活跃</span>
      </header>
      <div class="stat-grid cols-3">
        <article v-for="item in moderationStats" :key="item.label" class="stat-card" :class="item.tone">
          <div class="stat-icon">{{ item.icon }}</div>
          <div class="stat-body">
            <p>{{ item.label }}</p>
            <strong>{{ item.value }}</strong>
            <small v-if="item.hint">{{ item.hint }}</small>
          </div>
        </article>
      </div>
    </section>

    <section class="panel">
      <header class="panel-head">
        <div>
          <h3>待审核举报</h3>
          <span class="panel-sub">优先处理待处理队列</span>
        </div>
        <el-button @click="reload">刷新</el-button>
      </header>
      <el-table v-loading="loading" :data="reports" class="admin-table" empty-text="暂无待处理举报">
        <el-table-column prop="id" label="编号" width="72" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ reportTargetLabel(row.targetType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="举报原因" min-width="120" show-overflow-tooltip />
        <el-table-column label="被举报人" width="130">
          <template #default="{ row }">#{{ row.reportedUserId ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goReview(row.id)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { AdminDashboard, ReportItem } from '../../api/types'
import { fetchDashboard, listPendingReports } from '../../api/admin'
import { formatCount, formatCurrency, formatDateTime, reportTargetLabel } from '../../utils/adminLabels'

const router = useRouter()
const dashboard = ref<AdminDashboard | null>(null)
const reports = ref<ReportItem[]>([])
const loading = ref(false)

const userStats = computed(() => [
  { label: '用户总数', value: formatCount(dashboard.value?.totalUsers), icon: '👥', tone: 'blue', hint: '含全部普通用户' },
  { label: '正常用户', value: formatCount(dashboard.value?.activeUsers), icon: '✓', tone: 'green', hint: '状态为 ACTIVE' },
  { label: '今日活跃', value: formatCount(dashboard.value?.todayActiveUsers), icon: '⚡', tone: 'purple', hint: '登录/聊天/下单' },
  { label: '今日新增', value: formatCount(dashboard.value?.todayNewUsers), icon: '+', tone: 'neutral', hint: '新注册用户' },
])

const tradeStats = computed(() => [
  { label: '商品总数', value: formatCount(dashboard.value?.totalProducts), icon: '📦', tone: 'blue', hint: `在售 ${formatCount(dashboard.value?.activeProducts)}` },
  { label: '今日上新', value: formatCount(dashboard.value?.todayNewProducts), icon: '🆕', tone: 'neutral', hint: '今日发布商品' },
  { label: '累计成交额', value: formatCurrency(dashboard.value?.totalGmv), icon: '¥', tone: 'gold', hint: `完成订单 ${formatCount(dashboard.value?.completedOrders)} 笔` },
  { label: '今日成交额', value: formatCurrency(dashboard.value?.todayGmv), icon: '💰', tone: 'gold', hint: `今日新订单 ${formatCount(dashboard.value?.todayNewOrders)} 笔` },
])

const moderationStats = computed(() => [
  { label: '待处理举报', value: formatCount(dashboard.value?.pendingReports), icon: '!', tone: 'urgent', hint: '需尽快审核' },
  { label: '今日新增举报', value: formatCount(dashboard.value?.todayReports), icon: '⚑', tone: 'neutral', hint: '今日提交' },
  { label: '今日聊天活跃', value: formatCount(dashboard.value?.activeChatUsers), icon: '💬', tone: 'blue', hint: `封禁用户 ${formatCount(dashboard.value?.bannedUsers)}` },
])

function goReview(reportId: number) {
  router.push({ path: '/admin/reports', query: { reportId: String(reportId) } })
}

async function reload() {
  loading.value = true
  try {
    const [dashboardRes, reportsRes] = await Promise.all([fetchDashboard(), listPendingReports()])
    if (dashboardRes.code === 0) dashboard.value = dashboardRes.data
    if (reportsRes.code === 0) reports.value = reportsRes.data.slice(0, 10)
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.welcome-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 22px 24px;
  border-radius: 16px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  color: #fff;
}
.welcome-banner h2 {
  margin: 0 0 6px;
  font-size: 22px;
}
.welcome-banner p {
  margin: 0;
  color: #cbd5e1;
  font-size: 14px;
  max-width: 560px;
}
.banner-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}
.metric-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.section-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.section-head h3 {
  margin: 0;
  font-size: 16px;
  color: #0f172a;
}
.section-head span {
  color: #94a3b8;
  font-size: 12px;
}
.stat-grid {
  display: grid;
  gap: 14px;
}
.stat-grid.cols-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}
.stat-grid.cols-3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}
.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
  min-height: 108px;
}
.stat-card.urgent {
  border-color: #fecaca;
  background: linear-gradient(180deg, #fff 0%, #fff7f7 100%);
}
.stat-card.blue .stat-icon {
  background: #eff6ff;
  color: #2563eb;
}
.stat-card.green .stat-icon {
  background: #ecfdf5;
  color: #059669;
}
.stat-card.purple .stat-icon {
  background: #f5f3ff;
  color: #7c3aed;
}
.stat-card.gold .stat-icon {
  background: #fffbeb;
  color: #d97706;
}
.stat-card.neutral .stat-icon {
  background: #f8fafc;
  color: #64748b;
}
.stat-card.urgent .stat-icon {
  background: #fee2e2;
  color: #dc2626;
}
.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 18px;
  flex-shrink: 0;
}
.stat-body p {
  margin: 0 0 6px;
  color: #64748b;
  font-size: 13px;
}
.stat-body strong {
  display: block;
  font-size: 26px;
  color: #0f172a;
  line-height: 1.1;
  word-break: break-all;
}
.stat-body small {
  display: block;
  margin-top: 6px;
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.4;
}
.panel {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  padding: 18px 20px 8px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.panel-head h3 {
  margin: 0;
  font-size: 17px;
}
.panel-sub {
  color: #94a3b8;
  font-size: 12px;
}
.admin-table :deep(.el-table__header th) {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
}
@media (max-width: 1100px) {
  .stat-grid.cols-4,
  .stat-grid.cols-3 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 700px) {
  .stat-grid.cols-4,
  .stat-grid.cols-3 {
    grid-template-columns: 1fr;
  }
  .welcome-banner {
    flex-direction: column;
    align-items: flex-start;
  }
  .banner-actions {
    width: 100%;
    flex-direction: column;
  }
}
</style>
