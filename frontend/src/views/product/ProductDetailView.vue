<template>
  <ModulePlaceholder
    module="product"
    :title="`Product #${id}`"
    description="Reserved detail page for images, seller credit, favorite, report, chat, and order entry."
  />
</template>

<script setup lang="ts">
import ModulePlaceholder from '../../components/ModulePlaceholder.vue'

defineProps<{ id: string }>()
</script>
