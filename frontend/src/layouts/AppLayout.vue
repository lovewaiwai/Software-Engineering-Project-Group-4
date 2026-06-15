<template>
  <el-container class="app-layout">
    <el-aside class="app-sidebar" width="220px">
      <div class="brand">
        <span class="brand-mark">林</span>
        <span>
          <strong>SwapCampus</strong>
          <small>北林闲置</small>
        </span>
      </div>
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
        <el-menu-item v-if="auth.isLoggedIn && !auth.isAdmin" index="/products/history">
          <el-icon><Clock /></el-icon>
          <span>浏览历史</span>
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
        <el-button v-if="!auth.isAdmin" type="primary" class="publish-btn" @click="$router.push('/products/new')">发布闲置</el-button>
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
import { ChatDotRound, Clock, Goods, House, Tickets, User } from '@element-plus/icons-vue'
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
  border-right: 1px solid var(--bfu-border);
  background: linear-gradient(180deg, #ffffff 0%, var(--bfu-mint-50) 100%);
}
.brand {
  height: 72px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--bfu-green-800);
}
.brand-mark {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: var(--bfu-green-700);
  color: #fff;
  font-weight: 800;
}
.brand strong,
.brand small {
  display: block;
}
.brand strong {
  font-size: 17px;
  font-weight: 700;
}
.brand small {
  margin-top: 2px;
  color: var(--bfu-muted);
  font-size: 12px;
  font-weight: 600;
}
.nav-menu {
  border-right: none;
  background: transparent;
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
  border-bottom: 1px solid var(--bfu-border);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
}
.app-main {
  background: linear-gradient(180deg, var(--bfu-mint-50) 0%, #f7faf7 100%);
  padding: 20px 24px;
}
.publish-btn {
  font-weight: 700;
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
  background: var(--bfu-green-100);
}
</style>
