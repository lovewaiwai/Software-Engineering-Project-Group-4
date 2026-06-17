<template>
  <el-container class="admin-layout">
    <el-aside class="admin-sidebar" width="220px">
      <div class="brand">SwapCampus</div>
      <el-menu router :default-active="$route.path" class="admin-menu">
        <el-menu-item v-if="auth.isSystemReviewer" index="/admin">
          <el-icon><DataBoard /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item v-if="auth.canReviewProducts" index="/admin/products">
          <el-icon><Goods /></el-icon>
          <span>商品审核</span>
        </el-menu-item>
        <el-menu-item v-if="auth.isSystemReviewer" index="/admin/reports">
          <el-icon><Warning /></el-icon>
          <span>举报审核</span>
        </el-menu-item>
        <el-menu-item v-if="auth.isSystemReviewer" index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item v-if="auth.isSystemReviewer" index="/admin/lockers">
          <el-icon><Box /></el-icon>
          <span>柜机管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <h1>{{ pageTitle }}</h1>
          <el-tag type="info" effect="plain">{{ roleLabel }}</el-tag>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="admin-user">
              <el-icon><UserFilled /></el-icon>
              {{ auth.displayName }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Box, DataBoard, Goods, User, UserFilled, Warning } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const pageTitle = computed(() => {
  if (route.path.startsWith('/admin/products')) return '商品审核'
  if (route.path.startsWith('/admin/reports')) return '举报审核'
  if (route.path.startsWith('/admin/users')) return '用户管理'
  if (route.path.startsWith('/admin/lockers')) return '柜机管理'
  return '审核工作台'
})

const roleLabel = computed(() => {
  if (auth.role === 'PRODUCT_REVIEWER') return '商品审核员'
  return '系统管理员'
})

function handleCommand(command: string) {
  if (command === 'logout') {
    auth.clearSession()
    router.push('/login')
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}
.admin-sidebar {
  border-right: 1px solid #e2e8f0;
  background: #fff;
}
.brand {
  padding: 20px 16px;
  font-size: 18px;
  font-weight: 700;
  color: #0f766e;
}
.admin-menu {
  border-right: none;
}
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-bottom: 1px solid #e2e8f0;
  background: #fff;
}
.header-left,
.header-right,
.admin-user {
  display: flex;
  align-items: center;
}
.header-left {
  gap: 10px;
}
.header-left h1 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}
.header-right {
  gap: 8px;
}
.admin-user {
  gap: 6px;
  cursor: pointer;
  color: #334155;
  font-weight: 600;
  padding: 6px 10px;
  border-radius: 8px;
}
.admin-user:hover {
  background: #f1f5f9;
}
.admin-main {
  background: #f8fafc;
  padding: 20px 24px;
}
</style>
