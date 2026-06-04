<template>
  <section v-if="user" class="user-mod-panel">
    <div class="user-head">
      <div class="user-avatar">{{ initial }}</div>
      <div>
        <strong>{{ displayName }}</strong>
        <p>@{{ user.username }} · ID {{ user.id }}</p>
      </div>
      <el-tag :type="statusTagType" size="small">{{ statusText }}</el-tag>
    </div>

    <dl class="user-meta">
      <dt>信用分</dt><dd>{{ user.creditScore ?? '-' }}</dd>
      <dt v-if="user.muted">禁言至</dt>
      <dd v-if="user.muted">{{ formatDateTime(user.mutedUntil) }}</dd>
    </dl>

    <div class="action-grid">
      <el-button
        v-if="user.status !== 'BANNED'"
        type="danger"
        plain
        :loading="loading === 'ban'"
        @click="run('ban')"
      >
        封禁账号
      </el-button>
      <el-button
        v-else
        type="success"
        plain
        :loading="loading === 'unban'"
        @click="run('unban')"
      >
        解除封禁
      </el-button>

      <el-button
        v-if="!user.muted"
        type="warning"
        plain
        :loading="loading === 'mute'"
        @click="run('mute')"
      >
        禁言 24h
      </el-button>
      <el-button
        v-else
        type="success"
        plain
        :loading="loading === 'unmute'"
        @click="run('unmute')"
      >
        解除禁言
      </el-button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { AdminUserSummary } from '../../api/types'
import { banUser, muteUser, unbanUser, unmuteUser } from '../../api/admin'
import { formatDateTime, userStatusLabel, userStatusTagType as tagType } from '../../utils/adminLabels'

const props = defineProps<{
  user: AdminUserSummary | null | undefined
}>()

const emit = defineEmits<{
  updated: [user: AdminUserSummary]
}>()

const loading = ref('')

const displayName = computed(() => props.user?.realName || props.user?.username || '用户')
const initial = computed(() => displayName.value.charAt(0).toUpperCase())
const statusText = computed(() => {
  if (!props.user) return '-'
  if (props.user.muted && props.user.status !== 'BANNED') return '禁言中'
  return userStatusLabel(props.user.status)
})
const statusTagType = computed(() => tagType(props.user?.status, props.user?.muted))

async function run(action: 'ban' | 'unban' | 'mute' | 'unmute') {
  if (!props.user) return
  const userId = props.user.id
  const prompts: Record<string, { title: string; confirm: string; needNote?: boolean }> = {
    ban: { title: '封禁账号', confirm: '确认封禁该用户？封禁后将无法登录。', needNote: true },
    unban: { title: '解除封禁', confirm: '确认解除封禁并恢复账号正常使用？' },
    mute: { title: '禁言 24 小时', confirm: '确认禁言该用户 24 小时？', needNote: true },
    unmute: { title: '解除禁言', confirm: '确认立即解除禁言？' },
  }
  const cfg = prompts[action]
  try {
    if (cfg.needNote) {
      await ElMessageBox.confirm(cfg.confirm, cfg.title, { type: 'warning' })
    } else {
      await ElMessageBox.confirm(cfg.confirm, cfg.title, { type: 'info' })
    }
  } catch {
    return
  }

  loading.value = action
  try {
    let response
    if (action === 'ban') response = await banUser(userId)
    else if (action === 'unban') response = await unbanUser(userId)
    else if (action === 'mute') response = await muteUser(userId, { muteHours: 24 })
    else response = await unmuteUser(userId)

    if (response.code !== 0) throw new Error(response.message)
    ElMessage.success('操作成功')
    emit('updated', response.data)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '操作失败')
  } finally {
    loading.value = ''
  }
}
</script>

<style scoped>
.user-mod-panel {
  background: linear-gradient(180deg, #f8fafc 0%, #fff 100%);
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 16px;
}
.user-head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  color: #1d4ed8;
  display: grid;
  place-items: center;
  font-weight: 700;
  flex-shrink: 0;
}
.user-head strong {
  display: block;
  font-size: 15px;
  color: #0f172a;
}
.user-head p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748b;
}
.user-head .el-tag {
  margin-left: auto;
}
.user-meta {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 6px 10px;
  margin: 14px 0;
  font-size: 13px;
}
.user-meta dt {
  color: #64748b;
}
.user-meta dd {
  margin: 0;
  color: #334155;
}
.action-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
</style>
