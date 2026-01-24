<template>
  <el-card>
    <template #header>
      <div style="font-weight:700;">评估任务</div>
    </template>

    <el-form :model="req" label-width="140px" style="max-width:820px;">
      <el-form-item label="taskName">
        <el-input v-model="req.taskName" placeholder="示例评估任务" />
      </el-form-item>
      <el-form-item label="assetId">
        <el-input v-model="req.assetId" placeholder="ASSET_0001" />
      </el-form-item>
      <el-form-item label="caliberVersionCode">
        <el-input v-model="req.caliberVersionCode" placeholder="CAL_20260124_001" />
      </el-form-item>
    </el-form>

    <el-divider />

    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;">
      <div style="font-weight:600;">输入 values（原始值：0.92 / “高” / 0.78）</div>
      <div style="display:flex;gap:10px;">
        <el-button @click="rows.push({code:'',val:''})">新增一行</el-button>
        <el-button type="primary" @click="calculate">开始计算</el-button>
      </div>
    </div>

    <el-table :data="rows" border>
      <el-table-column label="indicatorCode" width="240">
        <template #default="{row}">
          <el-input v-model="row.code" placeholder="如 QLT_ACC_001" />
        </template>
      </el-table-column>
      <el-table-column label="rawValue">
        <template #default="{row}">
          <el-input v-model="row.val" placeholder='如 0.92 或 高' />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ $index }">
          <el-button size="small" type="danger" @click="rows.splice($index,1)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-divider />

    <div v-if="resp" style="margin-top:8px;">
      <el-alert type="success" show-icon :title="`taskId=${resp.taskId} | total=${resp.totalScore.toFixed(4)} | level=${resp.level}`" />
      <div style="margin-top:10px;display:flex;gap:10px;">
        <el-button @click="openExport('summary')">导出 summary.csv</el-button>
        <el-button @click="openExport('detail')">导出 detail.csv</el-button>
        <el-button @click="openExport('snapshot')">导出 snapshot.csv</el-button>
      </div>

      <el-table :data="resp.items" border style="margin-top:10px;">
        <el-table-column prop="code" label="code" width="180" />
        <el-table-column prop="raw" label="raw" />
        <el-table-column prop="processed" label="processed" width="120" />
        <el-table-column prop="outlier" label="outlier" width="90" />
        <el-table-column prop="missing" label="missing" width="90" />
        <el-table-column prop="score" label="score" width="110" />
        <el-table-column prop="weight" label="weight" width="110" />
        <el-table-column prop="contribution" label="contribution" width="140" />
      </el-table>
    </div>
  </el-card>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { taskApi } from '../api/task'

const req = reactive({
  taskName: '示例评估任务',
  assetId: 'ASSET_0001',
  caliberVersionCode: 'CAL_',
  values: {}
})

const rows = ref([
  { code: 'QLT_ACC_001', val: '0.92' },
  { code: 'SEC_AUTH_001', val: '高' },
  { code: 'USE_DOC_001', val: '0.78' }
])

const resp = ref(null)

function parseVal(v) {
  const s = String(v ?? '').trim()
  if (s === '') return null
  const n = Number(s)
  return Number.isFinite(n) && s.match(/^-?\d+(\.\d+)?$/) ? n : s
}

async function calculate() {
  try {
    if (!req.taskName || !req.assetId || !req.caliberVersionCode) throw new Error('taskName/assetId/caliberVersionCode 必填')
    const values = {}
    for (const r of rows.value) {
      if (!r.code) continue
      values[r.code.trim()] = parseVal(r.val)
    }
    const body = { taskName: req.taskName, assetId: req.assetId, caliberVersionCode: req.caliberVersionCode, values }
    const r = await taskApi.calculate(body)
    resp.value = r.data
    ElMessage.success('计算完成')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function openExport(type) {
  if (!resp.value?.taskId) return
  const taskId = resp.value.taskId
  window.open(`/api/v1/task/${taskId}/export-file?type=${type}&format=csv`, '_blank')
}
</script>
