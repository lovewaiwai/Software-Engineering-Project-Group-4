<template>
  <el-container class="app-layout">
    <el-aside class="app-sidebar" width="220px">
      <div class="brand">SwapCampus</div>
      <el-menu router :default-active="$route.path" class="nav-menu">
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <span>商品</span>
        </el-menu-item>
        <el-menu-item v-if="auth.isLoggedIn && !auth.isAdmin" index="/products/mine">
          <el-icon><Goods /></el-icon>
          <span>我的发布</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Tickets /></el-icon>
          <span>订单</span>
        </el-menu-item>
        <el-menu-item v-if="!auth.isAdmin" index="/chat" class="chat-menu-item">
          <el-icon><ChatDotRound /></el-icon>
          <span>聊天</span>
          <UnreadBadge class="menu-edge-badge" :count="chatNotify.totalUnread" />
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="app-header">
        <el-button v-if="!auth.isAdmin" type="primary" @click="$router.push('/products/new')">发布</el-button>
        <el-button v-if="!auth.isLoggedIn" @click="$router.push('/login')">登录</el-button>
        <el-dropdown v-else trigger="click" @command="handleCommand">
          <span class="user-entry">
            <el-icon><User /></el-icon>
            <span>{{ auth.displayName }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="auth.userId && !auth.isAdmin" :command="`/profile/${auth.userId}`">个人主页</el-dropdown-item>
              <el-dropdown-item v-if="!auth.isAdmin" command="/points">积分中心</el-dropdown-item>
              <el-dropdown-item v-if="auth.isAdmin" command="/admin">审核后台</el-dropdown-item>
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
import { onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, Goods, House, Tickets, User } from '@element-plus/icons-vue'
import UnreadBadge from '../components/chat/UnreadBadge.vue'
import { fetchCurrentUser } from '../api/user'
import { useAuthStore } from '../stores/auth'
import { useChatNotifyStore } from '../stores/chatNotify'

const router = useRouter()
const auth = useAuthStore()
const chatNotify = useChatNotifyStore()

function syncChatNotify() {
  if (auth.isLoggedIn && !auth.isAdmin) {
    chatNotify.start()
    return
  }
  chatNotify.stop()
}

onMounted(async () => {
  syncChatNotify()
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

onUnmounted(() => {
  chatNotify.stop()
})

watch(
  () => [auth.isLoggedIn, auth.isAdmin] as const,
  () => syncChatNotify(),
)

function handleCommand(command: string) {
  if (command === 'logout') {
    chatNotify.stop()
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
.nav-menu :deep(.chat-menu-item) {
  position: relative;
  overflow: visible;
}
.menu-edge-badge {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
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
