<template>
  <el-card>
    <template #header>
      <div style="display:flex;align-items:center;justify-content:space-between;">
        <div style="font-weight:700;">任务历史</div>
        <div style="display:flex;gap:10px;align-items:center;">
          <el-input v-model="q.assetId" placeholder="assetId" style="width:200px" />
          <el-input v-model="q.caliberVersionCode" placeholder="caliberVersionCode" style="width:220px" />
          <el-input-number v-model="q.limit" :min="1" :max="200" />
          <el-button type="primary" @click="load">查询</el-button>
        </div>
      </div>
    </template>

    <el-table :data="rows" border>
      <el-table-column prop="id" label="taskId" width="90" />
      <el-table-column prop="taskName" label="任务名" />
      <el-table-column prop="assetId" label="assetId" width="140" />
      <el-table-column prop="caliberVersionCode" label="口径" width="180" />
      <el-table-column prop="status" label="状态" width="110" />
      <el-table-column prop="createTime" label="创建时间" width="190" />
      <el-table-column label="操作" width="260">
        <template #default="{row}">
          <el-button size="small" :disabled="row.status!=='DONE'" @click="open(row.id)">详情</el-button>
          <el-button size="small" @click="exportType(row.id,'summary')">summary</el-button>
          <el-button size="small" type="success" @click="exportType(row.id,'detail')">detail</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dlg" title="任务详情" width="920px">
      <div v-if="full">
        <el-alert type="success" show-icon
          :title="`taskId=${full.task.id} | total=${full.result.totalScore} | level=${full.result.level}`" />
        <div style="margin-top:10px;display:grid;grid-template-columns:repeat(3,1fr);gap:6px;font-size:13px;opacity:.85;">
          <div>任务名：{{ full.task.taskName }}</div>
          <div>资产ID：{{ full.task.assetId }}</div>
          <div>任务状态：{{ full.task.status }}</div>
          <div>口径：{{ full.task.caliberVersionCode }}</div>
          <div>权重版本：{{ full.task.weightVersionCode }}</div>
          <div>任务创建时间：{{ full.task.createTime }}</div>
          <div>结果创建时间：{{ full.result.createTime }}</div>
        </div>
        <div style="margin-top:10px;display:flex;gap:10px;">
          <el-button @click="exportType(full.task.id,'summary')">导出 summary.csv</el-button>
          <el-button type="success" @click="exportType(full.task.id,'detail')">导出 detail.csv</el-button>
          <el-button @click="exportType(full.task.id,'snapshot')">导出 snapshot.csv</el-button>
        </div>

        <el-tabs style="margin-top:10px;">
          <el-tab-pane label="明细">
            <el-table :data="detailRows" border>
              <el-table-column prop="code" label="code" width="160" />
              <el-table-column prop="raw" label="raw" />
              <el-table-column prop="processed" label="processed" width="120" />
              <el-table-column prop="outlier" label="outlier" width="90" />
              <el-table-column prop="missing" label="missing" width="90" />
              <el-table-column prop="score" label="score" width="110" />
              <el-table-column prop="weight" label="weight" width="110" />
              <el-table-column prop="contribution" label="contribution" width="140" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="输入（input_json）">
            <el-input v-model="inputText" type="textarea" :rows="12" readonly />
          </el-tab-pane>

          <el-tab-pane label="快照（snapshot_json）">
            <el-input v-model="snapshotText" type="textarea" :rows="12" readonly />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { taskApi } from '../api/task'

const q = reactive({ assetId:'', caliberVersionCode:'', limit: 100 })
const rows = ref([])

const dlg = ref(false)
const full = ref(null)
const detailRows = ref([])
const inputText = ref('')
const snapshotText = ref('')

async function load() {
  try {
    const r = await taskApi.list(q)
    rows.value = r.data || []
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function open(taskId) {
  try {
    const r = await taskApi.full(taskId)
    full.value = r.data
    detailRows.value = safeJson(full.value.result.detailJson, [])
    inputText.value = prettyJson(full.value.task.inputJson)
    snapshotText.value = prettyJson(full.value.result.snapshotJson)
    dlg.value = true
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function exportType(taskId, type) {
  window.open(`/api/v1/task/${taskId}/export-file?type=${type}&format=csv`, '_blank')
}

function safeJson(s, defVal) {
  try {
    return JSON.parse(s || '[]')
  } catch {
    return defVal
  }
}

function prettyJson(s) {
  try {
    return JSON.stringify(JSON.parse(s || '{}'), null, 2)
  } catch {
    return s || ''
  }
}

onMounted(load)
</script>
