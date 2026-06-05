<template>
  <section class="admin-dashboard">
    <div class="welcome-banner">
      <div>
        <h2>审核工作台</h2>
        <p>处理用户举报，并对违规账号执行封禁、禁言或解封操作。</p>
      </div>
      <el-button type="primary" size="large" @click="$router.push('/admin/reports')">进入举报审核</el-button>
    </div>

    <div class="stat-grid">
      <article class="stat-card urgent">
        <div class="stat-icon">!</div>
        <div class="stat-body">
          <p>待处理举报</p>
          <strong>{{ dashboard?.pendingReports ?? 0 }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon neutral">+</div>
        <div class="stat-body">
          <p>今日新增举报</p>
          <strong>{{ dashboard?.todayReports ?? 0 }}</strong>
        </div>
      </article>
      <article class="stat-card">
        <div class="stat-icon neutral">@</div>
        <div class="stat-body">
          <p>今日活跃用户</p>
          <strong>{{ dashboard?.activeChatUsers ?? 0 }}</strong>
        </div>
      </article>
    </div>

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
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { AdminDashboard, ReportItem } from '../../api/types'
import { fetchDashboard, listPendingReports } from '../../api/admin'
import { formatDateTime, reportTargetLabel } from '../../utils/adminLabels'

const router = useRouter()
const dashboard = ref<AdminDashboard | null>(null)
const reports = ref<ReportItem[]>([])
const loading = ref(false)

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
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}
.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
}
.stat-card.urgent {
  border-color: #fecaca;
  background: linear-gradient(180deg, #fff 0%, #fff7f7 100%);
}
.stat-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-weight: 800;
  background: #fee2e2;
  color: #dc2626;
  font-size: 18px;
}
.stat-icon.neutral {
  background: #eff6ff;
  color: #2563eb;
}
.stat-body p {
  margin: 0 0 4px;
  color: #64748b;
  font-size: 13px;
}
.stat-body strong {
  font-size: 28px;
  color: #0f172a;
  line-height: 1;
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
@media (max-width: 900px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
  .welcome-banner {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
