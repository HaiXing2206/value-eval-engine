<template>
  <el-container style="height: 100vh;">
    <el-aside width="220px">
      <div style="padding: 14px; font-weight: 700;">Value Eval Engine</div>
      <el-menu :default-active="route.path" router>
        <el-menu-item index="/dashboard">首页看板</el-menu-item>
        <el-menu-item index="/indicators">指标库</el-menu-item>
        <el-menu-item index="/rules">规则配置</el-menu-item>
        <el-menu-item index="/weights">权重版本</el-menu-item>
        <el-menu-item index="/calibers">口径版本</el-menu-item>
        <el-menu-item index="/tasks">评估任务</el-menu-item>
        <el-menu-item index="/history">任务历史</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display:flex;align-items:center;justify-content:space-between;">
        <div>管理台</div>
        <div style="opacity:.7;">后端：/api → 8080（Vite Proxy）</div>
      </el-header>
      <el-main style="background:#f6f7f9;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>
