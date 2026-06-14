<template>
  <span v-if="count > 0" class="unread-badge" :class="{ dot: dotOnly }" :title="`${count} 条未读`">
    {{ dotOnly ? '' : formatUnreadCount(count) }}
  </span>
</template>

<script setup lang="ts">
import { formatUnreadCount } from '../../utils/unread'

defineProps<{
  count: number
  dotOnly?: boolean
}>()
</script>

<style scoped>
.unread-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #fa5151;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
  box-shadow: 0 0 0 2px #fff;
}
.unread-badge.dot {
  min-width: 10px;
  width: 10px;
  height: 10px;
  padding: 0;
}
</style>
