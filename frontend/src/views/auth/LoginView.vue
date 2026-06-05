<template>
  <main class="auth-page">
    <section class="auth-panel">
      <p class="eyebrow">SwapCampus</p>
      <h1>登录</h1>
      <p class="login-hint">管理员请使用专用账号登录，将自动进入管理后台。</p>
      <el-form label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" class="full-width" native-type="submit" :loading="loading">登录</el-button>
        <el-button class="full-width secondary-action" native-type="button" @click="router.push('/register')">注册账号</el-button>
      </el-form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { getApiErrorMessage } from '../../utils/apiError'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

function resolveRedirectPath() {
  if (auth.isAdmin) return '/admin'
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  if (redirect && !redirect.startsWith('/admin')) return redirect
  return '/'
}

const username = ref('')
const password = ref('')
const loading = ref(false)

onMounted(() => {
  if (route.query.banned === '1') {
    auth.clearSession()
    ElMessage.error('账号已被封禁，如有疑问请联系平台审核员')
  }
})

async function handleLogin() {
  loading.value = true
  try {
    await auth.login(username.value.trim(), password.value)
    ElMessage.success('登录成功')
    await router.replace(resolveRedirectPath())
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '登录失败'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #eef6ff, #f8fbff);
}
.auth-panel {
  width: min(420px, 92vw);
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.08);
}
.eyebrow {
  color: #64748b;
  margin: 0 0 8px;
}
.login-hint {
  margin: 0 0 16px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}
.full-width {
  width: 100%;
  margin-top: 8px;
}
.secondary-action {
  margin-left: 0;
}
</style>