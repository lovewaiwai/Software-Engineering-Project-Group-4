<template>
  <section class="locker-page">
    <div class="page-head">
      <div>
        <h1>柜机管理</h1>
        <p>查看 Mock 柜机站点、格口占用和订单流转任务。</p>
      </div>
      <el-button type="primary" :icon="Refresh" :loading="loading" @click="reload">刷新</el-button>
    </div>

    <div class="station-grid">
      <article v-for="station in stations" :key="station.id" class="station-card">
        <div class="station-top">
          <div>
            <h2>{{ station.name }}</h2>
            <p>{{ station.location }}</p>
          </div>
          <el-tag :type="station.status === 'ACTIVE' ? 'success' : 'info'" effect="plain">
            {{ station.status === 'ACTIVE' ? '启用' : station.status }}
          </el-tag>
        </div>
        <div class="box-stats">
          <span><strong>{{ station.emptyBoxes }}</strong> 空闲</span>
          <span><strong>{{ station.reservedBoxes }}</strong> 已预约</span>
          <span><strong>{{ station.occupiedBoxes }}</strong> 已存入</span>
        </div>
      </article>
    </div>

    <section class="task-panel">
      <header>
        <h2>柜机任务</h2>
        <span>最近 100 条 Mock 任务</span>
      </header>
      <el-table v-loading="loading" :data="tasks" empty-text="暂无柜机任务" class="task-table">
        <el-table-column prop="taskNo" label="任务号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="orderId" label="订单" width="90">
          <template #default="{ row }">#{{ row.orderId }}</template>
        </el-table-column>
        <el-table-column prop="stationName" label="站点" min-width="180" show-overflow-tooltip />
        <el-table-column prop="boxNo" label="格口" width="90" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="taskStatusType(row.status)" effect="plain">{{ taskStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pickupCode" label="取件码" width="110" />
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { listLockerStations, listLockerTasks, type LockerStation, type LockerTask } from '../../api/admin'
import { formatDateTime } from '../../utils/adminLabels'
import { getApiErrorMessage } from '../../utils/apiError'

const stations = ref<LockerStation[]>([])
const tasks = ref<LockerTask[]>([])
const loading = ref(false)

async function reload() {
  loading.value = true
  try {
    const [stationRes, taskRes] = await Promise.all([listLockerStations(), listLockerTasks()])
    if (stationRes.code !== 0) throw new Error(stationRes.message || '加载柜机站点失败')
    if (taskRes.code !== 0) throw new Error(taskRes.message || '加载柜机任务失败')
    stations.value = stationRes.data
    tasks.value = taskRes.data
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '加载柜机数据失败'))
  } finally {
    loading.value = false
  }
}

function taskStatusLabel(status: string) {
  if (status === 'RESERVED') return '已预约'
  if (status === 'STORED') return '已存入'
  if (status === 'PICKED_UP') return '已取出'
  if (status === 'CANCELLED') return '已取消'
  return status
}

function taskStatusType(status: string): 'success' | 'warning' | 'info' | '' {
  if (status === 'PICKED_UP') return 'success'
  if (status === 'RESERVED') return 'warning'
  if (status === 'CANCELLED') return 'info'
  return ''
}

onMounted(reload)
</script>

<style scoped>
.locker-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}
.page-head h1 {
  margin: 0;
  font-size: 26px;
}
.page-head p {
  margin: 6px 0 0;
  color: #64748b;
}
.station-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 14px;
}
.station-card,
.task-panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
}
.station-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.station-top h2,
.task-panel h2 {
  margin: 0;
  font-size: 17px;
}
.station-top p {
  margin: 5px 0 0;
  color: #64748b;
  font-size: 13px;
}
.box-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 14px;
}
.box-stats span {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px;
  color: #64748b;
  font-size: 13px;
}
.box-stats strong {
  display: block;
  color: #0f172a;
  font-size: 22px;
}
.task-panel header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 12px;
}
.task-panel header span {
  color: #94a3b8;
  font-size: 12px;
}
.task-table :deep(.el-table__header th) {
  background: #f8fafc;
}
@media (max-width: 700px) {
  .page-head,
  .task-panel header {
    flex-direction: column;
  }
}
</style>
