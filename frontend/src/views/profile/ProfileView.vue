<template>
  <section class="profile-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">User</p>
        <h1>个人主页</h1>
      </div>
      <div class="header-actions">
        <el-button v-if="isOwnProfile" :icon="Coin" @click="router.push('/points')">积分中心</el-button>
        <el-button :icon="Refresh" :loading="loading" @click="loadProfile">刷新</el-button>
      </div>
    </div>

    <el-skeleton v-if="loading && !user" :rows="8" animated />

    <template v-else-if="user">
      <section class="summary-band">
        <div class="avatar-wrap">
          <el-avatar :src="user.profile?.avatarUrl" :size="72">
            {{ displayInitial }}
          </el-avatar>
          <div>
            <h2>{{ displayName }}</h2>
            <div class="meta-line">
              <el-tag :type="user.status === 'BANNED' ? 'danger' : 'success'" effect="light">
                {{ user.status || 'ACTIVE' }}
              </el-tag>
              <el-tag :type="user.profile?.verifiedAt ? 'success' : 'info'" effect="light">
                {{ user.profile?.verifiedAt ? '已认证' : '未认证' }}
              </el-tag>
              <span>ID {{ user.id }}</span>
            </div>
          </div>
        </div>
        <div class="score-grid">
          <div class="score-item">
            <span>信用分</span>
            <strong>{{ user.creditScore ?? 100 }}</strong>
          </div>
          <div class="score-item">
            <span>积分</span>
            <strong>{{ user.pointBalance ?? 0 }}</strong>
          </div>
        </div>
      </section>

      <section class="content-grid">
        <el-card class="profile-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>资料</span>
              <el-button v-if="isOwnProfile" type="primary" :icon="Edit" @click="editing = !editing">
                {{ editing ? '收起' : '编辑' }}
              </el-button>
            </div>
          </template>

          <el-descriptions v-if="!editing" :column="1" border>
            <el-descriptions-item label="真实姓名">{{ user.profile?.realName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学院">{{ user.profile?.college || '-' }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ user.profile?.grade || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学号">{{ user.profile?.studentNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系方式">{{ user.profile?.contactMasked || '-' }}</el-descriptions-item>
            <el-descriptions-item label="简介">{{ user.profile?.bio || '-' }}</el-descriptions-item>
          </el-descriptions>

          <el-form v-else label-position="top" class="profile-form" @submit.prevent="saveProfile">
            <el-form-item label="真实姓名">
              <el-input v-model="form.realName" maxlength="50" />
            </el-form-item>
            <el-form-item label="学院">
              <el-input v-model="form.college" maxlength="80" />
            </el-form-item>
            <el-form-item label="年级">
              <el-input v-model="form.grade" maxlength="20" />
            </el-form-item>
            <el-form-item label="头像链接">
              <el-input v-model="form.avatarUrl" maxlength="500" />
            </el-form-item>
            <el-form-item label="联系方式">
              <el-input v-model="form.contactMasked" maxlength="100" />
            </el-form-item>
            <el-form-item label="简介">
              <el-input v-model="form.bio" type="textarea" maxlength="500" :rows="4" show-word-limit />
            </el-form-item>
            <div class="form-actions">
              <el-button @click="resetForm">取消</el-button>
              <el-button type="primary" native-type="submit" :loading="saving">保存</el-button>
            </div>
          </el-form>
        </el-card>

        <el-card class="profile-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>账号</span>
              <el-button v-if="isOwnProfile && !user.profile?.verifiedAt" @click="router.push('/verify')">
                学生认证
              </el-button>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ user.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ user.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ user.role }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(user.createdAt) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </section>

      <el-card v-if="isOwnProfile" class="records-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>信用记录</span>
            <el-button :icon="Refresh" :loading="creditLoading" @click="loadCreditRecords">刷新</el-button>
          </div>
        </template>
        <el-table :data="creditRecords" v-loading="creditLoading" empty-text="暂无信用记录">
          <el-table-column prop="reason" label="原因" min-width="180" />
          <el-table-column label="变动" width="100">
            <template #default="{ row }">
              <el-tag :type="row.delta >= 0 ? 'success' : 'danger'">
                {{ row.delta >= 0 ? '+' : '' }}{{ row.delta }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="scoreAfter" label="变动后" width="100" />
          <el-table-column label="时间" min-width="170">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="creditPage"
          class="pagination"
          background
          layout="prev, pager, next"
          :page-size="creditPageSize"
          :total="creditTotal"
          @current-change="loadCreditRecords"
        />
      </el-card>
    </template>

    <el-empty v-else description="用户不存在" />
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Coin, Edit, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  fetchCreditRecords,
  fetchUserById,
  updateCurrentUserProfile,
  type UserProfileUpdatePayload,
} from '../../api/user'
import type { CreditRecord, UserInfo } from '../../api/types'
import { useAuthStore } from '../../stores/auth'

const props = defineProps<{ id: string }>()

const router = useRouter()
const auth = useAuthStore()

const user = ref<UserInfo | null>(null)
const loading = ref(false)
const saving = ref(false)
const editing = ref(false)
const creditLoading = ref(false)
const creditRecords = ref<CreditRecord[]>([])
const creditPage = ref(1)
const creditPageSize = 10
const creditTotal = ref(0)

const form = reactive<UserProfileUpdatePayload>({
  realName: '',
  college: '',
  grade: '',
  avatarUrl: '',
  bio: '',
  contactMasked: '',
})

const isOwnProfile = computed(() => auth.userId === Number(props.id))
const displayName = computed(() => user.value?.profile?.realName || user.value?.username || '用户')
const displayInitial = computed(() => displayName.value.slice(0, 1).toUpperCase())

watch(
  () => props.id,
  () => {
    editing.value = false
    creditPage.value = 1
    loadProfile()
  },
  { immediate: true },
)

async function loadProfile() {
  loading.value = true
  try {
    const response = await fetchUserById(props.id)
    if (response.code !== 0) {
      throw new Error(response.message || '用户加载失败')
    }
    user.value = response.data
    resetForm()
    if (isOwnProfile.value) {
      await loadCreditRecords()
    }
  } catch (error) {
    user.value = null
    ElMessage.error(error instanceof Error ? error.message : '用户加载失败')
  } finally {
    loading.value = false
  }
}

async function loadCreditRecords() {
  if (!isOwnProfile.value) return
  creditLoading.value = true
  try {
    const response = await fetchCreditRecords(creditPage.value, creditPageSize)
    if (response.code !== 0) {
      throw new Error(response.message || '信用记录加载失败')
    }
    creditRecords.value = response.data.items
    creditTotal.value = response.data.total
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '信用记录加载失败')
  } finally {
    creditLoading.value = false
  }
}

function resetForm() {
  const profile = user.value?.profile
  form.realName = profile?.realName ?? ''
  form.college = profile?.college ?? ''
  form.grade = profile?.grade ?? ''
  form.avatarUrl = profile?.avatarUrl ?? ''
  form.bio = profile?.bio ?? ''
  form.contactMasked = profile?.contactMasked ?? ''
}

async function saveProfile() {
  saving.value = true
  try {
    const response = await updateCurrentUserProfile({
      realName: normalize(form.realName),
      college: normalize(form.college),
      grade: normalize(form.grade),
      avatarUrl: normalize(form.avatarUrl),
      bio: normalize(form.bio),
      contactMasked: normalize(form.contactMasked),
    })
    if (response.code !== 0) {
      throw new Error(response.message || '资料保存失败')
    }
    user.value = response.data
    auth.updateProfile(response.data.profile)
    editing.value = false
    ElMessage.success('资料已保存')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '资料保存失败')
  } finally {
    saving.value = false
  }
}

function normalize(value?: string) {
  const trimmed = value?.trim()
  return trimmed || undefined
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString() : '-'
}
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.page-header,
.card-header,
.header-actions,
.avatar-wrap,
.meta-line,
.form-actions {
  display: flex;
  align-items: center;
}
.page-header,
.card-header {
  justify-content: space-between;
  gap: 12px;
}
.eyebrow {
  margin: 0 0 4px;
  color: #64748b;
  font-size: 12px;
  text-transform: uppercase;
}
h1,
h2 {
  margin: 0;
  color: #111827;
}
.header-actions,
.meta-line,
.form-actions {
  gap: 10px;
  flex-wrap: wrap;
}
.summary-band {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 22px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}
.avatar-wrap {
  gap: 16px;
}
.score-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(112px, 1fr));
  gap: 12px;
  min-width: 250px;
}
.score-item {
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f9fafb;
}
.score-item span {
  display: block;
  color: #64748b;
  font-size: 13px;
}
.score-item strong {
  display: block;
  margin-top: 6px;
  font-size: 26px;
  color: #111827;
}
.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.8fr);
  gap: 18px;
}
.profile-card,
.records-card {
  border-radius: 8px;
}
.profile-form {
  max-width: 680px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
@media (max-width: 860px) {
  .summary-band,
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }
  .content-grid {
    grid-template-columns: 1fr;
  }
  .score-grid {
    width: 100%;
  }
}
</style>
