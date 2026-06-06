<template>
  <el-container class="admin-layout">
    <el-aside class="admin-sidebar" width="240px">
      <div class="brand">
        <span class="brand-badge">审核</span>
        <div>
          <strong>SwapCampus</strong>
          <small>内容审核后台</small>
        </div>
      </div>
      <el-menu
        router
        :default-active="$route.path"
        class="admin-menu"
        background-color="#0f172a"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
      >
        <el-menu-item v-if="auth.isSystemReviewer" index="/admin">
          <el-icon><DataBoard /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item v-if="auth.isSystemReviewer" index="/admin/reports">
          <el-icon><Warning /></el-icon>
          <span>举报审核</span>
        </el-menu-item>
        <el-menu-item v-if="auth.canReviewProducts" index="/admin/products">
          <el-icon><Goods /></el-icon>
          <span>商品审核</span>
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
          <span class="header-tag">审核员专用</span>
        </div>
        <div class="header-right">
          <el-button text @click="$router.push('/')">返回用户端</el-button>
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
  if (route.path.startsWith('/admin/reports')) return '举报审核'
  if (route.path.startsWith('/admin/products')) return '商品审核'
  if (route.path.startsWith('/admin/users')) return '用户管理'
  if (route.path.startsWith('/admin/lockers')) return '柜机管理'
  return '审核工作台'
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
  background: #f1f5f9;
}
.admin-sidebar {
  background: #0f172a;
  border-right: 1px solid #1e293b;
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 22px 18px;
  color: #fff;
}
.brand-badge {
  background: #ef4444;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 6px 8px;
  border-radius: 8px;
}
.brand strong {
  display: block;
  font-size: 16px;
}
.brand small {
  color: #94a3b8;
  font-size: 12px;
}
.admin-menu {
  border-right: none;
}
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  height: 64px;
  padding: 0 24px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-left h1 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}
.header-tag {
  background: #fef2f2;
  color: #dc2626;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.admin-user {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #334155;
  font-weight: 600;
  padding: 8px 10px;
  border-radius: 8px;
}
.admin-user:hover {
  background: #f8fafc;
}
.admin-main {
  padding: 24px;
}
</style>
