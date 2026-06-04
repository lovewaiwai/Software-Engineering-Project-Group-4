<template>
  <el-container class="app-layout">
    <el-aside class="app-sidebar" width="220px">
      <div class="brand">SwapCampus</div>
      <el-menu router :default-active="$route.path" class="nav-menu">
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <span>Home</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <span>Products</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Tickets /></el-icon>
          <span>Orders</span>
        </el-menu-item>
        <el-menu-item index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>Chat</span>
        </el-menu-item>
        <el-menu-item index="/verify">
          <el-icon><User /></el-icon>
          <span>学生认证</span>
        </el-menu-item>
        <el-menu-item index="/points">
          <el-icon><Coin /></el-icon>
          <span>Points</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <el-button type="primary" @click="$router.push('/products/new')">发布</el-button>
        <el-button v-if="!auth.isLoggedIn" @click="$router.push('/login')">登录</el-button>
        <el-dropdown v-else trigger="click" @command="handleCommand">
          <span class="user-entry">
            <el-icon><User /></el-icon>
            <span>{{ auth.displayName }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="auth.userId" :command="`/profile/${auth.userId}`">个人主页</el-dropdown-item>
              <el-dropdown-item command="/verify">学生认证</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Coin, Goods, House, Tickets, User } from '@element-plus/icons-vue'
import { fetchCurrentUser } from '../api/user'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

onMounted(async () => {
  if (!auth.isLoggedIn) return
  try {
    const response = await fetchCurrentUser()
    if (response.code === 0) {
      auth.updateProfile(response.data.profile)
    }
  } catch {
    // ignore refresh failure
  }
})

function handleCommand(command: string) {
  if (command === 'logout') {
    auth.clearSession()
    router.push('/login')
    return
  }
  router.push(command)
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
}
.app-sidebar {
  border-right: 1px solid #e2e8f0;
  background: #fff;
}
.brand {
  padding: 20px 16px;
  font-size: 18px;
  font-weight: 700;
}
.nav-menu {
  border-right: none;
}
.app-header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  border-bottom: 1px solid #e2e8f0;
  background: #fff;
}
.app-main {
  background: #f8fafc;
  padding: 20px 24px;
}
.user-entry {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #334155;
  font-weight: 600;
  padding: 6px 10px;
  border-radius: 8px;
}
.user-entry:hover {
  background: #f1f5f9;
}
</style>
