<template>
  <el-card>
    <template #header>
      <div style="display:flex;gap:10px;align-items:center;justify-content:space-between;">
        <div style="font-weight:700;">指标库管理</div>
        <el-button type="primary" @click="openCreate">新增指标</el-button>
      </div>
    </template>

    <el-table :data="rows" border style="width:100%">
      <el-table-column prop="code" label="编码" width="160" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="metricType" label="类型" width="110" />
      <el-table-column prop="unit" label="单位" width="110" />
      <el-table-column prop="enabled" label="启用" width="80">
        <template #default="{row}">{{ row.enabled === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" :disabled="row.enabled!==1" @click="disable(row)">停用</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dlg" :title="form.id ? '编辑指标' : '新增指标'" width="520px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="编码">
        <el-input v-model="form.code" :disabled="!!form.id" placeholder="如 QLT_ACC_001" />
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.metricType" style="width:100%">
          <el-option label="QUANT（定量）" value="QUANT" />
          <el-option label="QUAL（定性）" value="QUAL" />
        </el-select>
      </el-form-item>
      <el-form-item label="单位">
        <el-input v-model="form.unit" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dlg=false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { indicatorApi } from '../api/indicator'

const rows = ref([])
const dlg = ref(false)
const form = reactive({ id:null, code:'', name:'', metricType:'QUANT', unit:'', remark:'', enabled:1 })

async function load() {
  const r = await indicatorApi.list()
  rows.value = r.data || []
}

function openCreate() {
  Object.assign(form, { id:null, code:'', name:'', metricType:'QUANT', unit:'', remark:'', enabled:1 })
  dlg.value = true
}

function openEdit(row) {
  Object.assign(form, { ...row })
  dlg.value = true
}

async function save() {
  try {
    if (!form.code || !form.name) throw new Error('编码/名称必填')
    if (form.id) await indicatorApi.update(form.id, form)
    else await indicatorApi.create(form)
    ElMessage.success('保存成功')
    dlg.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function disable(row) {
  try {
    await indicatorApi.disable(row.id)
    ElMessage.success('已停用')
    await load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(load)
</script>
