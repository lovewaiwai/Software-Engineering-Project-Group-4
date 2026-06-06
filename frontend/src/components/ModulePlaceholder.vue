<template>
  <section class="module-page">
    <div class="module-page__header">
      <p class="eyebrow">{{ module }}</p>
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>
    </div>

    <el-alert
      title="功能占位"
      type="info"
      :closable="false"
      show-icon
    />

    <el-table :data="todoRows" class="todo-table">
      <el-table-column prop="name" label="后续任务" />
      <el-table-column prop="owner" label="负责人" width="160" />
      <el-table-column prop="status" label="状态" width="140" />
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
  { name: '接入 D4-D5 接口契约', owner: '模块开发', status: '待处理' },
  { name: '补充真实表单和表格状态', owner: '前端开发', status: '待处理' },
  { name: '覆盖演示主流程', owner: '测试协作', status: '待处理' },
]
</script>
