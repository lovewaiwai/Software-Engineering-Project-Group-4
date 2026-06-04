<template>
  <section class="product-detail">
    <el-card>
      <h1>商品详情 #{{ id }}</h1>
      <p class="desc">演示商品页：点击“联系卖家”将检查登录状态，并创建或打开与该卖家的聊天会话。</p>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="商品标题">校园二手 MacBook 保护壳</el-descriptions-item>
        <el-descriptions-item label="价格">¥ 39.00</el-descriptions-item>
        <el-descriptions-item label="卖家 ID">{{ sellerId }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-button type="primary" @click="contactSeller">联系卖家</el-button>
        <el-button @click="$router.push('/products')">返回列表</el-button>
      </div>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { openOrCreateSession } from '../../stores/chat'

const props = defineProps<{ id: string }>()
const router = useRouter()
const auth = useAuthStore()

const sellerId = computed(() => {
  const parsed = Number(props.id)
  return Number.isFinite(parsed) && parsed > 1 ? parsed - 1 : 2
})

async function contactSeller() {
  if (!auth.isLoggedIn) {
    try {
      await ElMessageBox.confirm('需要登录后才能聊天，是否前往登录？', '提示', {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'warning',
      })
      await router.push({ path: '/login', query: { redirect: `/products/${props.id}` } })
    } catch {
      // cancelled
    }
    return
  }
  if (auth.isAdmin) {
    ElMessage.warning('管理员账号不参与用户聊天，请使用普通用户账号联系卖家')
    return
  }

  try {
    const session = await openOrCreateSession(sellerId.value, Number(props.id))
    await router.push({ path: '/chat', query: { sessionId: String(session.id) } })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '无法发起聊天')
  }
}
</script>

<style scoped>
.product-detail {
  max-width: 760px;
}
.desc {
  color: #64748b;
  margin-bottom: 16px;
}
.actions {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}
</style>
