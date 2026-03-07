<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="login-title">价值评估引擎 登录</div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="onSubmit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" clearable />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" clearable />
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%;" placeholder="请选择角色">
            <el-option v-for="item in ROLE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" style="width: 100%;" @click="onSubmit">登录</el-button>
        </el-form-item>
      </el-form>

      <div class="tip">
        演示账号：admin / 123456（四种角色均可登录）
        <br />
        没有账号？
        <el-link type="primary" @click="goRegister">去注册</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { ROLE_OPTIONS, getDefaultPathByRole } from '../auth/roles'
import { verifyUser } from '../auth/users'

const router = useRouter()
const formRef = ref()

const form = reactive({
  username: '',
  password: '',
  role: 'evaluator'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const onSubmit = async () => {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) {
    return
  }

  if (verifyUser({ username: form.username, password: form.password, role: form.role })) {
    localStorage.setItem('vee_token', 'demo-token')
    localStorage.setItem('vee_user', form.username)
    localStorage.setItem('vee_role', form.role)
    ElMessage.success('登录成功')
    router.push(getDefaultPathByRole(form.role))
    return
  }

  ElMessage.error('用户名、密码或角色不正确')
}

const goRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-card {
  width: 420px;
  max-width: calc(100vw - 32px);
}

.login-title {
  text-align: center;
  font-size: 18px;
  font-weight: 700;
}

.tip {
  margin-top: 4px;
  text-align: center;
  color: #909399;
  font-size: 13px;
  line-height: 1.8;
}
</style>
