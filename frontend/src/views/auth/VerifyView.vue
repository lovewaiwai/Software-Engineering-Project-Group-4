<template>
  <main class="auth-page">
    <section class="auth-panel">
      <p class="eyebrow">SwapCampus</p>
      <h1>学生认证</h1>

      <div v-if="!auth.isLoggedIn" class="login-prompt">
        <el-alert title="请先登录后再进行学生认证" type="info" :closable="false" show-icon />
        <el-button type="primary" class="full-width" @click="goLogin">去登录</el-button>
      </div>

      <el-form v-else label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="学号">
          <el-input v-model="studentNo" placeholder="例如 20260001" maxlength="12" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="college" placeholder="请输入学院" />
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="grade" placeholder="请选择年级" style="width: 100%" filterable>
            <el-option
              v-for="year in gradeOptions"
              :key="year"
              :label="String(year)"
              :value="String(year)"
            />
          </el-select>
        </el-form-item>
        <el-button type="primary" class="full-width" native-type="submit" :loading="loading">提交认证</el-button>
      </el-form>

      <el-button class="full-width secondary-action" native-type="button" @click="$router.push('/')">返回首页</el-button>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchCurrentUser, verifyStudent } from '../../api/user'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const studentNo = ref('')
const realName = ref('')
const college = ref('')
const grade = ref('')
const loading = ref(false)

const currentYear = new Date().getFullYear()
const gradeOptions = computed(() => {
  const years: number[] = []
  for (let y = currentYear; y >= 2000; y--) {
    years.push(y)
  }
  return years
})

onMounted(async () => {
  if (!auth.isLoggedIn) return
  try {
    const response = await fetchCurrentUser()
    if (response.code === 0 && response.data.profile) {
      studentNo.value = response.data.profile.studentNo ?? ''
      realName.value = response.data.profile.realName ?? ''
      college.value = response.data.profile.college ?? ''
      grade.value = response.data.profile.grade ?? ''
    }
  } catch {
    // ignore load failure
  }
})

function goLogin() {
  router.push({ path: '/login', query: { redirect: '/verify' } })
}

async function handleSubmit() {
  if (!studentNo.value.trim()) { ElMessage.warning('请输入学号'); return }
  if (!realName.value.trim()) { ElMessage.warning('请输入真实姓名'); return }
  if (!college.value.trim()) { ElMessage.warning('请输入学院'); return }
  if (!grade.value) { ElMessage.warning('请选择年级'); return }

  loading.value = true
  try {
    const response = await verifyStudent({
      studentNo: studentNo.value.trim(),
      realName: realName.value.trim(),
      college: college.value.trim(),
      grade: grade.value,
    })
    if (response.code !== 0) {
      throw new Error(response.message || '认证失败')
    }
    auth.updateProfile(response.data.profile)
    ElMessage.success('学生认证成功')
    await router.push('/')
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
.full-width {
  width: 100%;
  margin-top: 8px;
}
.secondary-action {
  margin-left: 0;
}
.login-prompt {
  margin-bottom: 16px;
}
</style>