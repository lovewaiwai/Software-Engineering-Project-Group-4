<template>
  <main class="auth-page">
    <section class="auth-panel">
      <p class="eyebrow">Verification</p>
      <h1>Student Identity</h1>
      <el-form label-position="top">
        <el-form-item label="Student number">
          <el-input placeholder="2026000001" />
        </el-form-item>
        <el-form-item label="Real name">
          <el-input placeholder="Name" />
        </el-form-item>
        <el-form-item label="College">
          <el-select placeholder="Select college">
            <el-option label="Computer Science" value="computer-science" />
            <el-option label="Business" value="business" />
          </el-select>
        </el-form-item>
        <el-button type="primary" class="full-width">Submit</el-button>
      </el-form>
    </section>
  </main>
</template>
