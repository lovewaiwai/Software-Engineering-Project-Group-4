<template>
  <ModulePlaceholder
    module="order/payment/delivery"
    :title="`Order #${id}`"
    description="Reserved order detail for state timeline, Mock payment, meetup, locker delivery, and review."
  />
</template>

<script setup lang="ts">
import ModulePlaceholder from '../../components/ModulePlaceholder.vue'

defineProps<{ id: string }>()
</script>
