<template>
  <ModulePlaceholder
    module="user"
    :title="`Profile #${id}`"
    description="Reserved profile page for public items, credit score, reviews, and masked contact details."
  />
</template>

<script setup lang="ts">
import ModulePlaceholder from '../../components/ModulePlaceholder.vue'

defineProps<{ id: string }>()
</script>
