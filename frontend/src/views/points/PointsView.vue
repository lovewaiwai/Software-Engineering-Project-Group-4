<template>
  <section class="points-page">
    <div class="page-header">
      <div class="header-left">
        <div>
          <p class="eyebrow">积分</p>
          <h1>积分中心</h1>
        </div>
        <div class="header-points">
          <span class="metric-label">可用积分</span>
          <strong>{{ currentUser?.pointBalance ?? 0 }}</strong>
        </div>
      </div>
      <el-button v-if="auth.userId" :icon="User" @click="router.push(`/profile/${auth.userId}`)">个人主页</el-button>
    </div>

    <section class="task-grid">
      <el-card v-for="task in tasks" :key="task.code" shadow="never" class="task-card">
        <div class="task-main">
          <div>
            <h2>{{ task.name }}</h2>
            <p>{{ task.taskType }}</p>
          </div>
          <el-tag :type="task.claimed ? 'info' : task.claimable ? 'success' : 'warning'">
            {{ claimLabel(task) }}
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

    <!-- 商品曝光加速：选择商品弹窗 -->
    <el-dialog v-model="boostDialogVisible" title="选择要加速曝光的商品" width="520px" :close-on-click-modal="false">
      <div v-loading="boostLoading">
        <el-empty v-if="!boostLoading && boostProducts.length === 0" description="您当前暂未发布商品" />
        <div v-else class="boost-product-list">
          <div
            v-for="product in boostProducts"
            :key="product.id"
            :class="['boost-product-item', { selected: selectedProductId === product.id }]"
            @click="selectedProductId = product.id"
          >
            <div class="boost-product-thumb">
              <img v-if="product.imageUrls?.[0]" :src="resolveMediaUrl(product.imageUrls[0])" :alt="product.title" />
              <el-icon v-else :size="28"><Picture /></el-icon>
            </div>
            <div class="boost-product-info">
              <strong>{{ product.title || '未命名商品' }}</strong>
              <span class="boost-product-price">&yen;{{ money(product.price) }}</span>
            </div>
            <el-icon v-if="selectedProductId === product.id" color="#409eff" :size="22"><Check /></el-icon>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="boostDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="!selectedProductId"
          :loading="actionLoading === 'PRODUCT_BOOST'"
          @click="confirmProductBoost"
        >
          确认兑换
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Check, Picture, Refresh, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  checkIn,
  claimPointTask,
  fetchPointItems,
  fetchPointRecords,
  fetchPointTasks,
  redeemPointItem,
} from '../../api/points'
import { listMyProducts, type ProductItem } from '../../api/product'
import { fetchCurrentUser } from '../../api/user'
import type { PointItem, PointRecord, PointTask, UserInfo } from '../../api/types'
import { useAuthStore } from '../../stores/auth'
import { getApiErrorMessage } from '../../utils/apiError'
import { resolveMediaUrl } from '../../utils/media'

const auth = useAuthStore()
const router = useRouter()

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

// 商品曝光加速弹窗
const boostDialogVisible = ref(false)
const boostLoading = ref(false)
const boostProducts = ref<ProductItem[]>([])
const selectedProductId = ref<number | null>(null)

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

function claimLabel(task: PointTask) {
  if (!task.claimed) {
    return task.claimable ? '可领取' : '待完成'
  }
  return task.code === 'DAILY_CHECK_IN' ? '今日已领取' : '已领取'
}

async function handleCheckIn() {
  await runAction('DAILY_CHECK_IN', async () => {
    const response = await checkIn()
    if (response.code !== 0) throw new Error(response.message || '签到失败')
    ElMessage.success(`签到成功，获得${response.data.delta} 积分`)
  })
}

async function handleClaim(code: string) {
  if (code === 'DAILY_CHECK_IN') {
    await handleCheckIn()
    return
  }
  await runAction(code, async () => {
    const response = await claimPointTask(code)
    if (response.code !== 0) throw new Error(response.message || '领取失败')
    ElMessage.success(`已获得${response.data.delta} 积分`)
  })
}

async function handleRedeem(itemCode: string) {
  // 商品曝光加速：弹出商品选择窗
  if (itemCode === 'PRODUCT_BOOST') {
    await openBoostDialog()
    return
  }

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
    ElMessage.success(`兑换成功，剩余${response.data.balanceAfter} 积分`)
  })
}

async function openBoostDialog() {
  boostDialogVisible.value = true
  boostLoading.value = true
  selectedProductId.value = null
  try {
    const response = await listMyProducts({ status: 'ACTIVE', pageSize: 100 })
    if (response.code !== 0) throw new Error(response.message || '加载商品失败')
    boostProducts.value = response.data.items
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载商品失败'))
    boostDialogVisible.value = false
  } finally {
    boostLoading.value = false
  }
}

async function confirmProductBoost() {
  if (!selectedProductId.value) return
  await runAction('PRODUCT_BOOST', async () => {
    const response = await redeemPointItem('PRODUCT_BOOST', selectedProductId.value ?? undefined)
    if (response.code !== 0) throw new Error(response.message || '兑换失败')
    ElMessage.success(`商品曝光加速成功，剩余${response.data.balanceAfter} 积分`)
    boostDialogVisible.value = false
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

function money(value?: number) {
  return Number(value ?? 0).toFixed(2)
}
</script>

<style scoped>
.points-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.page-header,
.header-left,
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
.header-left {
  gap: 28px;
}
.header-points {
  padding-left: 28px;
  border-left: 1px solid #e5e7eb;
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
.metric-label {
  display: block;
  color: #64748b;
  font-size: 13px;
}
.header-points strong {
  display: block;
  margin-top: 4px;
  color: #111827;
  font-size: 26px;
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

/* 商品加速弹窗 */
.boost-product-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.boost-product-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}
.boost-product-item:hover {
  border-color: #409eff;
  background: #f0f7ff;
}
.boost-product-item.selected {
  border-color: #409eff;
  background: #ecf5ff;
}
.boost-product-thumb {
  width: 64px;
  height: 48px;
  border-radius: 6px;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}
.boost-product-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.boost-product-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.boost-product-info strong {
  color: #111827;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.boost-product-price {
  color: #ef4444;
  font-weight: 600;
}

@media (max-width: 980px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .header-points {
    padding-left: 0;
    border-left: none;
  }
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>