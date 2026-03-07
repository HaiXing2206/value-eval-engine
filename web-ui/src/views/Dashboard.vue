<template>
  <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:12px;">
    <el-card>
      <div style="font-weight:700;">任务总数（最近{{ q.limit }}个）</div>
      <div style="font-size:28px;margin-top:6px;">{{ stats.count }}</div>
    </el-card>
    <el-card>
      <div style="font-weight:700;">平均分</div>
      <div style="font-size:28px;margin-top:6px;">{{ stats.avg.toFixed(2) }}</div>
    </el-card>
    <el-card>
      <div style="font-weight:700;">A / B / C / D</div>
      <div style="margin-top:10px;opacity:.85;">
        A {{ stats.level.A }} · B {{ stats.level.B }} · C {{ stats.level.C }} · D {{ stats.level.D }}
      </div>
    </el-card>
    <el-card>
      <div style="font-weight:700;">最近口径</div>
      <div style="margin-top:10px;opacity:.85;">{{ stats.latestCaliber || '-' }}</div>
    </el-card>
  </div>

  <el-card style="margin-top:12px;">
    <template #header>
      <div style="display:flex;align-items:center;justify-content:space-between;">
        <div style="font-weight:700;">最近任务</div>
        <div style="display:flex;gap:10px;align-items:center;">
          <el-input v-model="q.assetId" placeholder="assetId筛选" style="width:200px" />
          <el-input v-model="q.caliberVersionCode" placeholder="caliber筛选" style="width:220px" />
          <el-button @click="load">查询</el-button>
          <el-button type="primary" @click="$router.push('/history')">去任务历史</el-button>
        </div>
      </div>
    </template>

    <el-table :data="rows" border>
      <el-table-column prop="id" label="taskId" width="90" />
      <el-table-column prop="taskName" label="任务名" />
      <el-table-column prop="assetId" label="assetId" width="140" />
      <el-table-column prop="caliberVersionCode" label="口径" width="180" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="260">
        <template #default="{row}">
          <el-button size="small" :disabled="row.status!=='DONE'" @click="open(row.id)">详情</el-button>
          <el-button size="small" type="success" @click="exportDetail(row.id)">导出detail</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dlg" title="任务详情" width="900px">
      <div v-if="full">
        <el-alert type="success" show-icon
          :title="`taskId=${full.task.id} | ${full.result.totalScore} | level=${full.result.level}`" />
        <div style="margin-top:10px;display:grid;grid-template-columns:repeat(3,1fr);gap:6px;font-size:13px;opacity:.85;">
          <div>任务名：{{ full.task.taskName }}</div>
          <div>资产ID：{{ full.task.assetId }}</div>
          <div>任务状态：{{ full.task.status }}</div>
          <div>口径：{{ full.task.caliberVersionCode }}</div>
          <div>权重版本：{{ full.task.weightVersionCode }}</div>
          <div>任务创建时间：{{ full.task.createTime }}</div>
          <div>结果创建时间：{{ full.result.createTime }}</div>
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
          <el-tab-pane label="快照">
            <el-input v-model="snapshotText" type="textarea" :rows="14" readonly />
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

const q = reactive({ assetId: '', caliberVersionCode: '', limit: 30 })
const rows = ref([])

const stats = reactive({
  count: 0,
  avg: 0,
  level: { A: 0, B: 0, C: 0, D: 0 },
  latestCaliber: ''
})

const dlg = ref(false)
const full = ref(null)
const detailRows = ref([])
const snapshotText = ref('')

async function load() {
  try {
    const r = await taskApi.list(q)
    rows.value = r.data || []
    await calcStats(rows.value)
  } catch (e) {
    ElMessage.error(e.message)
  }
}

async function calcStats(list) {
  stats.count = list.length
  stats.level = { A: 0, B: 0, C: 0, D: 0 }
  stats.avg = 0
  stats.latestCaliber = list[0]?.caliberVersionCode || ''

  if (list.length === 0) return

  const ids = list.slice(0, 10).map(x => x.id)
  let total = 0
  let n = 0
  for (const id of ids) {
    try {
      const rr = await taskApi.getResult(id)
      const res = rr.data
      total += Number(res.totalScore || 0)
      n += 1
      const lv = res.level || 'D'
      stats.level[lv] = (stats.level[lv] || 0) + 1
    } catch {}
  }
  stats.avg = n ? total / n : 0
}

async function open(taskId) {
  try {
    const r = await taskApi.full(taskId)
    full.value = r.data
    detailRows.value = safeJson(full.value.result.detailJson, [])
    snapshotText.value = prettyJson(full.value.result.snapshotJson)
    dlg.value = true
  } catch (e) {
    ElMessage.error(e.message)
  }
}

function exportDetail(taskId) {
  window.open(`/api/v1/task/${taskId}/export-file?type=detail&format=csv`, '_blank')
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
