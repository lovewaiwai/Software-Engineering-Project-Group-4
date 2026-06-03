<template>
  <section class="module-page">
    <div class="module-page__header">
      <p class="eyebrow">{{ module }}</p>
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>
    </div>

    <el-alert
      title="Scaffold placeholder"
      type="info"
      :closable="false"
      show-icon
    />

    <el-table :data="todoRows" class="todo-table">
      <el-table-column prop="name" label="Next task" />
      <el-table-column prop="owner" label="Agent" width="160" />
      <el-table-column prop="status" label="Status" width="140" />
    </el-table>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  module: string
  title: string
  description: string
}>()

const todoRows = [
  { name: 'Connect REST API contract from D4-D5', owner: 'Module agent', status: 'TODO' },
  { name: 'Add real form/table state', owner: 'Frontend agent', status: 'TODO' },
  { name: 'Cover happy-path demo flow', owner: 'QA agent', status: 'TODO' },
]
</script>
