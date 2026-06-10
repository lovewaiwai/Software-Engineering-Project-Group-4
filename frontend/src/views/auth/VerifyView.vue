<template>
  <main class="auth-page">
    <section class="auth-panel">
      <p class="eyebrow">SwapCampus</p>
      <h1>学生认证</h1>

      <el-alert
        v-if="auth.isVerified"
        title="当前账号已完成学生认证"
        type="success"
        :closable="false"
        show-icon
      />

      <el-form label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="学号">
          <el-input v-model="studentNo" placeholder="例如 20260001" maxlength="30" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="realName" placeholder="请输入真实姓名" maxlength="50" />
        </el-form-item>
        <el-form-item label="教务系统密码">
          <el-input
            v-model="eduPassword"
            type="password"
            placeholder="演示账号可使用 demo123"
            maxlength="100"
            show-password
          />
        </el-form-item>
        <el-button type="primary" class="full-width" native-type="submit" :loading="loading">提交认证</el-button>
      </el-form>

      <el-button class="full-width secondary-action" native-type="button" @click="$router.push('/')">返回首页</el-button>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchCurrentUser, verifyStudent } from '../../api/user'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const studentNo = ref('')
const realName = ref('')
const eduPassword = ref('')
const loading = ref(false)

const redirectPath = computed(() => {
  const redirect = route.query.redirect
  return typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/'
})

onMounted(async () => {
  try {
    const response = await fetchCurrentUser()
    if (response.code === 0 && response.data.profile) {
      studentNo.value = response.data.profile.studentNo ?? ''
      realName.value = response.data.profile.realName ?? ''
      auth.updateProfile(response.data.profile)
    }
  } catch {
    // ignore load failure
  }
})

async function handleSubmit() {
  if (!studentNo.value.trim()) { ElMessage.warning('请输入学号'); return }
  if (!realName.value.trim()) { ElMessage.warning('请输入真实姓名'); return }
  if (!eduPassword.value) { ElMessage.warning('请输入教务系统密码'); return }

  loading.value = true
  try {
    const response = await verifyStudent({
      studentNo: studentNo.value.trim(),
      realName: realName.value.trim(),
      eduPassword: eduPassword.value,
    })
    if (response.code !== 0) {
      throw new Error(response.message || '认证失败')
    }
    auth.updateProfile(response.data.profile)
    ElMessage.success('学生认证成功')
    await router.push(redirectPath.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '认证失败')
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
.auth-panel h1 {
  margin: 0 0 18px;
}
.full-width {
  width: 100%;
  margin-top: 8px;
}
.secondary-action {
  margin-left: 0;
}
</style>
