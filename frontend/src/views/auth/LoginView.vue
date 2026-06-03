<template>
  <main class="auth-page">
    <section class="auth-panel">
      <p class="eyebrow">SwapCampus</p>
      <h1>Login</h1>
      <el-form label-position="top">
        <el-form-item label="Username / phone / email">
          <el-input placeholder="student@example.edu" />
        </el-form-item>
        <el-form-item label="Password">
          <el-input type="password" show-password placeholder="Password" />
        </el-form-item>
        <el-button type="primary" class="full-width">Login</el-button>
        <el-button class="full-width secondary-action" @click="$router.push('/verify')">Student verification</el-button>
      </el-form>
    </section>
  </main>
</template>
