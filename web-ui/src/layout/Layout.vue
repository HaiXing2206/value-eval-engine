<template>
  <el-container style="height: 100vh;">
    <el-aside width="220px">
      <div style="padding: 14px; font-weight: 700;">Value Eval Engine</div>
      <el-menu :default-active="route.path" router>
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          {{ item.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display:flex;align-items:center;justify-content:space-between;">
        <div>管理台</div>
        <div style="display:flex;align-items:center;gap:12px;">
          <span style="opacity:.7;">{{ username }}</span>
          <el-tag size="small">{{ roleLabel }}</el-tag>
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
import { canAccessPath, ROLE_LABEL_MAP } from '../auth/roles'

const route = useRoute()
const router = useRouter()

const allMenus = [
  { path: '/dashboard', title: '首页看板' },
  { path: '/indicators', title: '指标库' },
  { path: '/rules', title: '规则配置' },
  { path: '/weights', title: '权重版本' },
  { path: '/calibers', title: '口径版本' },
  { path: '/tasks', title: '评估任务' },
  { path: '/history', title: '任务历史' }
]

const role = computed(() => localStorage.getItem('vee_role') || 'evaluator')
const username = computed(() => localStorage.getItem('vee_user') || '未登录')
const roleLabel = computed(() => ROLE_LABEL_MAP[role.value] || '未知角色')
const menuItems = computed(() => allMenus.filter((item) => canAccessPath(role.value, item.path)))

const onLogout = () => {
  localStorage.removeItem('vee_token')
  localStorage.removeItem('vee_user')
  localStorage.removeItem('vee_role')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>
