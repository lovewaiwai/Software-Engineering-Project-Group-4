<template>
  <section class="admin-reports">
    <header class="page-head">
      <div>
        <h2>举报审核</h2>
        <p>待处理 {{ dashboard?.pendingReports ?? 0 }} · 今日新增 {{ dashboard?.todayReports ?? 0 }}</p>
      </div>
      <el-button @click="reload">刷新列表</el-button>
    </header>

    <el-table v-loading="loading" :data="reports" class="admin-table" stripe empty-text="暂无待处理举报">
      <el-table-column prop="id" label="编号" width="72" />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">
          <el-tag size="small" :type="row.targetType === 'USER' ? 'warning' : 'danger'" effect="plain">
            {{ reportTargetLabel(row.targetType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="举报原因" min-width="120" show-overflow-tooltip />
      <el-table-column label="被举报人" width="120">
        <template #default="{ row }">#{{ row.reportedUserId ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ reportStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="160">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)">审核</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="detailVisible" title="举报审核详情" size="580px" destroy-on-close>
      <div v-if="detail" class="detail-body">
        <section class="detail-block">
          <h3>举报信息</h3>
          <dl class="info-grid">
            <dt>编号</dt><dd>#{{ detail.report.id }}</dd>
            <dt>类型</dt><dd>{{ reportTargetLabel(detail.report.targetType) }}</dd>
            <dt>原因</dt><dd>{{ detail.report.reason }}</dd>
            <dt>状态</dt><dd>{{ reportStatusLabel(detail.report.status) }}</dd>
            <dt>提交时间</dt><dd>{{ formatDateTime(detail.report.createdAt) }}</dd>
          </dl>
          <p v-if="detail.report.description" class="desc-box">
            <strong>补充说明</strong>{{ detail.report.description }}
          </p>
          <div v-if="detail.report.evidenceUrl" class="evidence-box">
            <strong>证据图片</strong>
            <img :src="resolveMediaUrl(detail.report.evidenceUrl)" alt="证据" />
          </div>
        </section>

        <section v-if="detail.reportedUser" class="detail-block">
          <h3>被举报用户 · 账号处置</h3>
          <p class="block-tip">审核完成后，可在此对用户执行封禁、解封、禁言等操作。</p>
          <UserModerationPanel :user="detail.reportedUser" @updated="onUserUpdated" />
        </section>

        <section v-if="detail.contextMessages.length" class="detail-block">
          <h3>会话上下文</h3>
          <div class="context-box">
            <div v-for="message in detail.contextMessages" :key="message.id" class="context-item">
              <div class="context-head">
                <strong>#{{ message.seqNo }}</strong>
                <span>{{ formatDateTime(message.createdAt) }}</span>
              </div>
              <template v-if="message.messageType === 'IMAGE' && message.imageUrl">
                <img class="context-img" :src="resolveMediaUrl(message.imageUrl)" alt="消息图片" />
              </template>
              <p v-else>{{ message.content }}</p>
            </div>
          </div>
        </section>

        <section v-if="isPending" class="detail-block actions-block">
          <h3>结案操作</h3>
          <p class="block-tip">确认违规程度后选择处理方式，系统将自动标记举报为已处理。</p>
          <div class="actions">
            <el-button type="warning" @click="handle('WARN')">警告并结案</el-button>
            <el-button type="danger" @click="handle('MUTE')">禁言并结案</el-button>
            <el-button type="danger" plain @click="handle('BAN')">封禁并结案</el-button>
            <el-button
              v-if="detail.report.targetType === 'PRODUCT'"
              type="danger"
              plain
              @click="handle('REMOVE_PRODUCT')"
            >
              下架商品并结案
            </el-button>
            <el-button @click="handle('REJECT')">驳回举报</el-button>
          </div>
        </section>
        <el-alert v-else type="success" show-icon :closable="false" title="该举报已处理，仍可对用户执行封禁/解封操作。" />
      </div>
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { AdminDashboard, AdminReportDetail, AdminUserSummary, ReportItem } from '../../api/types'
import { fetchDashboard, fetchReportDetail, handleReport, listPendingReports } from '../../api/admin'
import UserModerationPanel from '../../components/admin/UserModerationPanel.vue'
import {
  formatDateTime,
  reportStatusLabel,
  reportTargetLabel,
} from '../../utils/adminLabels'
import { resolveMediaUrl } from '../../utils/media'

const route = useRoute()
const reports = ref<ReportItem[]>([])
const dashboard = ref<AdminDashboard | null>(null)
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref<AdminReportDetail | null>(null)
const activeReportId = ref<number | null>(null)

const isPending = computed(() => {
  const status = detail.value?.report.status
  return status === 'PENDING' || status === 'PROCESSING'
})

async function reload() {
  loading.value = true
  try {
    const [dashboardRes, reportsRes] = await Promise.all([fetchDashboard(), listPendingReports()])
    if (dashboardRes.code === 0) dashboard.value = dashboardRes.data
    if (reportsRes.code === 0) reports.value = reportsRes.data
  } finally {
    loading.value = false
  }
}

async function openDetail(reportId: number) {
  activeReportId.value = reportId
  const response = await fetchReportDetail(reportId)
  if (response.code !== 0) {
    ElMessage.error(response.message)
    return
  }
  detail.value = response.data
  detailVisible.value = true
}

function onUserUpdated(user: AdminUserSummary) {
  if (detail.value) detail.value.reportedUser = user
}

async function handle(actionType: string) {
  if (!activeReportId.value) return
  let note: string | undefined
  if (actionType === 'REJECT') {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回举报')
    note = value
  } else if (actionType === 'BAN') {
    await ElMessageBox.confirm('将封禁被举报用户并结案，是否继续？', '封禁并结案', { type: 'warning' })
  } else if (actionType === 'REMOVE_PRODUCT') {
    await ElMessageBox.confirm('将下架被举报商品并扣除卖家信用分，是否继续？', '下架商品并结案', { type: 'warning' })
  }
  const response = await handleReport(activeReportId.value, {
    actionType,
    note,
    muteHours: actionType === 'MUTE' ? 24 : undefined,
  })
  if (response.code !== 0) {
    ElMessage.error(response.message)
    return
  }
  ElMessage.success('举报已处理')
  if (activeReportId.value) {
    const refreshed = await fetchReportDetail(activeReportId.value)
    if (refreshed.code === 0) detail.value = refreshed.data
  }
  await reload()
}

watch(
  () => route.query.reportId,
  async (value) => {
    const reportId = Number(value)
    if (reportId) await openDetail(reportId)
  },
  { immediate: true },
)

onMounted(reload)
</script>

<style scoped>
.admin-reports {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  padding: 20px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
}
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-head h2 {
  margin: 0 0 4px;
  font-size: 20px;
}
.page-head p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}
.admin-table :deep(.el-table__header th) {
  background: #f8fafc;
}
.detail-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.detail-block h3 {
  margin: 0 0 10px;
  font-size: 15px;
  color: #0f172a;
}
.block-tip {
  margin: 0 0 10px;
  color: #64748b;
  font-size: 13px;
}
.info-grid {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 8px 12px;
  margin: 0;
  font-size: 13px;
}
.info-grid dt {
  color: #64748b;
}
.info-grid dd {
  margin: 0;
  color: #0f172a;
}
.desc-box {
  margin: 12px 0 0;
  padding: 12px;
  background: #f8fafc;
  border-radius: 10px;
  line-height: 1.6;
  color: #475569;
}
.desc-box strong {
  display: block;
  margin-bottom: 6px;
  color: #0f172a;
}
.evidence-box img {
  max-width: 100%;
  max-height: 200px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}
.context-box {
  max-height: 280px;
  overflow: auto;
  background: #f8fafc;
  border-radius: 12px;
  padding: 12px;
}
.context-item {
  padding: 8px 0;
  border-bottom: 1px solid #e2e8f0;
}
.context-item:last-child {
  border-bottom: none;
}
.context-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 12px;
  color: #64748b;
}
.context-item p {
  margin: 0;
  color: #334155;
}
.context-img {
  max-width: 160px;
  max-height: 100px;
  border-radius: 8px;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
