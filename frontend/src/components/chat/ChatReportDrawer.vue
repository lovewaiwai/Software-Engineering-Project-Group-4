<template>
  <el-drawer
    :model-value="visible"
    title="举报"
    direction="rtl"
    size="420px"
    :close-on-click-modal="false"
    class="chat-report-drawer"
    @close="emit('close')"
  >
    <div class="report-body">
      <section class="report-target">
        <p class="section-label">举报对象</p>
        <div class="target-card">
          <div class="avatar">{{ peerInitial(displayName) }}</div>
          <div class="target-info">
            <strong>{{ displayName }}</strong>
            <span>{{ mode === 'message' ? '聊天消息' : '聊天用户' }}</span>
          </div>
        </div>
        <div v-if="mode === 'message' && message" class="message-preview">
          <template v-if="message.messageType === 'IMAGE' && message.imageUrl">
            <img :src="resolveMediaUrl(message.imageUrl)" alt="被举报图片" />
            <span>[图片消息]</span>
          </template>
          <p v-else>{{ message.content }}</p>
        </div>
      </section>

      <div v-if="loadingExisting" class="report-status loading">正在查询举报状态…</div>
      <div v-else-if="isPendingReview" class="report-status pending">
        <strong>您已举报过该内容</strong>
        <span>当前状态：{{ reportStatusLabel(existingReport!.status) }}，请等待审核员处理。</span>
      </div>
      <div v-else-if="canResubmit" class="report-status resubmit">
        <span>您此前已举报过（{{ reportStatusLabel(existingReport!.status) }}），再次提交将重新进入审核队列。</span>
      </div>

      <section class="report-reasons">
        <p class="section-label">请选择举报原因 <em>*</em></p>
        <div class="reason-list">
          <button
            v-for="item in CHAT_REPORT_REASONS"
            :key="item.value"
            type="button"
            class="reason-item"
            :class="{ active: reason === item.value }"
            @click="reason = item.value"
          >
            <span class="reason-radio" />
            <span class="reason-text">
              <strong>{{ item.label }}</strong>
              <small>{{ item.desc }}</small>
            </span>
          </button>
        </div>
      </section>

      <section class="report-desc">
        <p class="section-label">补充说明（选填）</p>
        <el-input
          v-model="description"
          type="textarea"
          :rows="4"
          maxlength="200"
          show-word-limit
          placeholder="请描述具体情况，便于平台核实（最多200字）"
        />
      </section>

      <p class="report-notice">
        平台会在 24 小时内核实处理。请勿恶意举报，虚假举报可能影响您的账号信用。
      </p>
    </div>

    <template #footer>
      <el-button @click="emit('close')">取消</el-button>
      <el-button
        type="danger"
        :loading="submitting"
        :disabled="!reason || isPendingReview || loadingExisting"
        @click="handleSubmit"
      >
        {{ isPendingReview ? '等待审核中' : '提交举报' }}
      </el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { ChatMessage, ReportItem } from '../../api/types'
import { listMyReports } from '../../api/report'
import { getApiErrorMessage } from '../../utils/apiError'
import { reportStatusLabel } from '../../utils/adminLabels'
import { CHAT_REPORT_REASONS, formatReportReason } from '../../constants/chatReportReasons'
import { submitChatReport } from '../../stores/chat'
import { peerInitial, resolveMediaUrl } from '../../utils/media'

const props = defineProps<{
  visible: boolean
  mode: 'message' | 'user'
  message?: ChatMessage | null
  peerId: number
  peerName: string
  sessionId: number
}>()

const emit = defineEmits<{
  close: []
  submitted: []
}>()

const reason = ref('')
const description = ref('')
const submitting = ref(false)
const loadingExisting = ref(false)
const existingReport = ref<ReportItem | null>(null)

const displayName = computed(() => props.peerName || `用户 ${props.peerId}`)

const isPendingReview = computed(
  () =>
    existingReport.value?.status === 'PENDING' || existingReport.value?.status === 'PROCESSING',
)

const canResubmit = computed(
  () =>
    existingReport.value?.status === 'RESOLVED' || existingReport.value?.status === 'REJECTED',
)

watch(
  () => props.visible,
  (open) => {
    if (open) {
      reason.value = ''
      description.value = ''
      existingReport.value = null
      void loadExistingReport()
    }
  },
)

async function loadExistingReport() {
  loadingExisting.value = true
  try {
    const response = await listMyReports()
    if (response.code !== 0) return
    const targetType = props.mode === 'message' ? 'CHAT_MESSAGE' : 'USER'
    const targetId =
      props.mode === 'message' && props.message ? props.message.id : props.peerId
    existingReport.value =
      response.data.find((item) => item.targetType === targetType && item.targetId === targetId) ??
      null
  } catch {
    existingReport.value = null
  } finally {
    loadingExisting.value = false
  }
}

async function handleSubmit() {
  if (!reason.value) {
    ElMessage.warning('请选择举报原因')
    return
  }
  submitting.value = true
  try {
    const reasonLabel = formatReportReason(reason.value)
    const detail = description.value.trim()
    if (props.mode === 'message' && props.message) {
      await submitChatReport({
        targetType: 'CHAT_MESSAGE',
        targetId: props.message.id,
        sessionId: props.sessionId,
        reportedUserId: props.message.senderId,
        reason: reasonLabel,
        description: detail || undefined,
        evidenceUrl: props.message.imageUrl,
      })
    } else {
      await submitChatReport({
        targetType: 'USER',
        targetId: props.peerId,
        sessionId: props.sessionId,
        reportedUserId: props.peerId,
        reason: reasonLabel,
        description: detail || undefined,
      })
    }
    ElMessage.success('举报已提交，我们会尽快处理')
    emit('submitted')
    emit('close')
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '举报提交失败'))
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.report-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.section-label {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}
.section-label em {
  color: #ef4444;
  font-style: normal;
}
.target-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 12px;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  color: #1d4ed8;
  display: grid;
  place-items: center;
  font-weight: 700;
  flex-shrink: 0;
}
.target-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.target-info strong {
  font-size: 15px;
  color: #0f172a;
}
.target-info span {
  font-size: 12px;
  color: #94a3b8;
}
.message-preview {
  margin-top: 10px;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  color: #475569;
  word-break: break-word;
}
.message-preview p {
  margin: 0;
}
.message-preview img {
  max-width: 120px;
  max-height: 80px;
  border-radius: 6px;
  object-fit: cover;
  display: block;
  margin-bottom: 6px;
}
.reason-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.reason-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  width: 100%;
  text-align: left;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  transition: 0.15s ease;
}
.reason-item:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}
.reason-item.active {
  border-color: #f87171;
  background: #fef2f2;
}
.reason-radio {
  width: 16px;
  height: 16px;
  border: 2px solid #cbd5e1;
  border-radius: 50%;
  margin-top: 2px;
  flex-shrink: 0;
  position: relative;
}
.reason-item.active .reason-radio {
  border-color: #ef4444;
}
.reason-item.active .reason-radio::after {
  content: '';
  position: absolute;
  inset: 3px;
  background: #ef4444;
  border-radius: 50%;
}
.reason-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.reason-text strong {
  font-size: 14px;
  color: #0f172a;
}
.reason-text small {
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}
.report-notice {
  margin: 0;
  padding: 10px 12px;
  background: #fffbeb;
  border-radius: 10px;
  font-size: 12px;
  color: #92400e;
  line-height: 1.5;
}
.report-status {
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.report-status.loading {
  background: #f8fafc;
  color: #64748b;
}
.report-status.pending {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #991b1b;
}
.report-status.pending strong {
  font-size: 14px;
}
.report-status.resubmit {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  color: #1e40af;
}
</style>
