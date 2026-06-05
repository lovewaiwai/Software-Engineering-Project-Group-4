<template>
  <section class="admin-users">
    <header class="page-head">
      <div>
        <h2>用户管理</h2>
        <p>查看用户状态，执行封禁、解封、禁言与解除禁言。</p>
      </div>
      <div class="search-row">
        <el-input v-model="keyword" placeholder="搜索用户名或 ID" clearable @keyup.enter="reload" />
        <el-button type="primary" @click="reload">搜索</el-button>
      </div>
    </header>

    <el-table v-loading="loading" :data="users" class="admin-table" stripe empty-text="暂无用户">
      <el-table-column prop="id" label="ID" width="72" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column label="昵称" min-width="120">
        <template #default="{ row }">{{ row.realName || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag size="small" :type="userStatusTagType(row.status, row.muted)">
            {{ row.muted && row.status !== 'BANNED' ? '禁言中' : userStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creditScore" label="信用分" width="90" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openManage(row)">管理</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="drawerVisible" title="用户处置" size="460px" destroy-on-close>
      <UserModerationPanel :user="activeUser" @updated="onUserUpdated" />
    </el-drawer>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { AdminUserSummary } from '../../api/types'
import { listAdminUsers } from '../../api/admin'
import UserModerationPanel from '../../components/admin/UserModerationPanel.vue'
import { userStatusLabel, userStatusTagType } from '../../utils/adminLabels'

const users = ref<AdminUserSummary[]>([])
const keyword = ref('')
const loading = ref(false)
const drawerVisible = ref(false)
const activeUser = ref<AdminUserSummary | null>(null)

async function reload() {
  loading.value = true
  try {
    const response = await listAdminUsers(keyword.value.trim() || undefined)
    if (response.code === 0) users.value = response.data
  } finally {
    loading.value = false
  }
}

function openManage(user: AdminUserSummary) {
  activeUser.value = user
  drawerVisible.value = true
}

function onUserUpdated(user: AdminUserSummary) {
  activeUser.value = user
  const index = users.value.findIndex((item) => item.id === user.id)
  if (index >= 0) users.value[index] = user
}

onMounted(reload)
</script>

<style scoped>
.admin-users {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  padding: 20px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
}
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}
.page-head h2 {
  margin: 0 0 6px;
  font-size: 20px;
}
.page-head p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}
.search-row {
  display: flex;
  gap: 8px;
  min-width: 280px;
}
.admin-table :deep(.el-table__header th) {
  background: #f8fafc;
}
@media (max-width: 900px) {
  .page-head {
    flex-direction: column;
  }
  .search-row {
    width: 100%;
  }
}
</style>
