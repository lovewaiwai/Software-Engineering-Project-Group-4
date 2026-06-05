<template>
  <section class="points-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">Points</p>
        <h1>积分中心</h1>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadAll">刷新</el-button>
    </div>

    <section class="balance-band">
      <div>
        <span class="metric-label">可用积分</span>
        <strong>{{ currentUser?.pointBalance ?? 0 }}</strong>
      </div>
      <div>
        <span class="metric-label">信用分</span>
        <strong>{{ currentUser?.creditScore ?? 100 }}</strong>
      </div>
      <el-button type="primary" :icon="CircleCheck" :loading="actionLoading === 'DAILY_CHECK_IN'" @click="handleCheckIn">
        今日签到
      </el-button>
    </section>

    <section class="task-grid">
      <el-card v-for="task in tasks" :key="task.code" shadow="never" class="task-card">
        <div class="task-main">
          <div>
            <h2>{{ task.name }}</h2>
            <p>{{ task.taskType }}</p>
          </div>
          <el-tag :type="task.claimed ? 'info' : task.claimable ? 'success' : 'warning'">
            {{ task.claimed ? '已领取' : task.claimable ? '可领取' : '待完成' }}
          </el-tag>
        </div>
        <div class="task-footer">
          <span>+{{ task.rewardPoints }} 积分</span>
          <el-button
            type="primary"
            :disabled="task.claimed || !task.claimable"
            :loading="actionLoading === task.code"
            @click="handleClaim(task.code)"
          >
            领取
          </el-button>
        </div>
      </el-card>
    </section>

    <section class="content-grid">
      <el-card shadow="never" class="panel-card">
        <template #header>
          <div class="card-header">
            <span>兑换</span>
          </div>
        </template>
        <el-table :data="items" v-loading="loading" empty-text="暂无兑换项">
          <el-table-column prop="itemName" label="名称" min-width="140" />
          <el-table-column prop="description" label="说明" min-width="180" />
          <el-table-column prop="costPoints" label="积分" width="90" />
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                :disabled="(currentUser?.pointBalance ?? 0) < row.costPoints"
                :loading="actionLoading === row.itemCode"
                @click="handleRedeem(row.itemCode)"
              >
                兑换
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" class="panel-card">
        <template #header>
          <div class="card-header">
            <span>积分流水</span>
            <el-button :icon="Refresh" :loading="recordsLoading" @click="loadRecords">刷新</el-button>
          </div>
        </template>
        <el-table :data="records" v-loading="recordsLoading" empty-text="暂无积分流水">
          <el-table-column prop="reason" label="原因" min-width="160" />
          <el-table-column label="变动" width="100">
            <template #default="{ row }">
              <el-tag :type="row.delta >= 0 ? 'success' : 'danger'">
                {{ row.delta >= 0 ? '+' : '' }}{{ row.delta }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="balanceAfter" label="余额" width="90" />
          <el-table-column label="时间" min-width="168">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="recordPage"
          class="pagination"
          background
          layout="prev, pager, next"
          :page-size="recordPageSize"
          :total="recordTotal"
          @current-change="loadRecords"
        />
      </el-card>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { CircleCheck, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  checkIn,
  claimPointTask,
  fetchPointItems,
  fetchPointRecords,
  fetchPointTasks,
  redeemPointItem,
} from '../../api/points'
import { fetchCurrentUser } from '../../api/user'
import type { PointItem, PointRecord, PointTask, UserInfo } from '../../api/types'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()

const currentUser = ref<UserInfo | null>(null)
const tasks = ref<PointTask[]>([])
const records = ref<PointRecord[]>([])
const items = ref<PointItem[]>([])
const loading = ref(false)
const recordsLoading = ref(false)
const actionLoading = ref('')
const recordPage = ref(1)
const recordPageSize = 10
const recordTotal = ref(0)

onMounted(loadAll)

async function loadAll() {
  loading.value = true
  try {
    const [userResponse, taskResponse, itemResponse] = await Promise.all([
      fetchCurrentUser(),
      fetchPointTasks(),
      fetchPointItems(),
    ])
    if (userResponse.code !== 0) throw new Error(userResponse.message || '用户信息加载失败')
    if (taskResponse.code !== 0) throw new Error(taskResponse.message || '积分任务加载失败')
    if (itemResponse.code !== 0) throw new Error(itemResponse.message || '兑换项加载失败')
    currentUser.value = userResponse.data
    auth.updateProfile(userResponse.data.profile)
    tasks.value = taskResponse.data
    items.value = itemResponse.data
    await loadRecords()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '积分中心加载失败')
  } finally {
    loading.value = false
  }
}

async function loadRecords() {
  recordsLoading.value = true
  try {
    const response = await fetchPointRecords(recordPage.value, recordPageSize)
    if (response.code !== 0) {
      throw new Error(response.message || '积分流水加载失败')
    }
    records.value = response.data.items
    recordTotal.value = response.data.total
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '积分流水加载失败')
  } finally {
    recordsLoading.value = false
  }
}

async function handleCheckIn() {
  await runAction('DAILY_CHECK_IN', async () => {
    const response = await checkIn()
    if (response.code !== 0) throw new Error(response.message || '签到失败')
    ElMessage.success(`签到成功，获得 ${response.data.delta} 积分`)
  })
}

async function handleClaim(code: string) {
  await runAction(code, async () => {
    const response = await claimPointTask(code)
    if (response.code !== 0) throw new Error(response.message || '领取失败')
    ElMessage.success(`已获得 ${response.data.delta} 积分`)
  })
}

async function handleRedeem(itemCode: string) {
  const item = items.value.find((entry) => entry.itemCode === itemCode)
  if (!item) return
  await ElMessageBox.confirm(`确认兑换 ${item.itemName}？`, '兑换确认', {
    type: 'warning',
    confirmButtonText: '兑换',
    cancelButtonText: '取消',
  })
  await runAction(itemCode, async () => {
    const response = await redeemPointItem(itemCode)
    if (response.code !== 0) throw new Error(response.message || '兑换失败')
    ElMessage.success(`兑换成功，剩余 ${response.data.balanceAfter} 积分`)
  })
}

async function runAction(key: string, action: () => Promise<void>) {
  actionLoading.value = key
  try {
    await action()
    await loadAll()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '操作失败')
  } finally {
    actionLoading.value = ''
  }
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString() : '-'
}
</script>

<style scoped>
.points-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.page-header,
.balance-band,
.task-main,
.task-footer,
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.page-header,
.task-main,
.task-footer,
.card-header {
  justify-content: space-between;
}
.eyebrow {
  margin: 0 0 4px;
  color: #64748b;
  font-size: 12px;
  text-transform: uppercase;
}
h1,
h2,
p {
  margin: 0;
}
h1,
h2 {
  color: #111827;
}
.balance-band {
  justify-content: space-between;
  padding: 22px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}
.metric-label {
  display: block;
  color: #64748b;
  font-size: 13px;
}
.balance-band strong {
  display: block;
  margin-top: 6px;
  color: #111827;
  font-size: 30px;
}
.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 14px;
}
.task-card,
.panel-card {
  border-radius: 8px;
}
.task-main {
  align-items: flex-start;
}
.task-main p {
  margin-top: 6px;
  color: #64748b;
  font-size: 13px;
}
.task-footer {
  margin-top: 18px;
}
.task-footer span {
  color: #15803d;
  font-weight: 700;
}
.content-grid {
  display: grid;
  grid-template-columns: minmax(320px, 0.9fr) minmax(0, 1.1fr);
  gap: 18px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
@media (max-width: 980px) {
  .balance-band,
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
