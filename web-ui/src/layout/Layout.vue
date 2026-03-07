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
        <div style="display:flex;align-items:center;gap:12px;">
          <span style="opacity:.7;">{{ username }}</span>
          <el-button link type="primary" @click="onLogout">退出登录</el-button>
          <span style="opacity:.7;">后端：/api → 8080（Vite Proxy）</span>
        </div>
      </el-header>
      <el-main style="background:#f6f7f9;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const username = computed(() => localStorage.getItem('vee_user') || '未登录')

const onLogout = () => {
  localStorage.removeItem('vee_token')
  localStorage.removeItem('vee_user')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>
