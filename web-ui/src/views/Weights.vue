<template>
  <el-card>
    <template #header>
      <div style="display:flex;align-items:center;justify-content:space-between;">
        <div style="font-weight:700;">权重版本管理</div>
        <div style="display:flex;gap:10px;">
          <el-input v-model="newCode" placeholder="W_20260124_001" style="width:220px;" />
          <el-button type="primary" @click="createVersion">创建版本</el-button>
        </div>
      </div>
    </template>

    <div style="display:flex;gap:12px;align-items:center;margin-bottom:10px;">
      <span style="opacity:.7;">选择版本：</span>
      <el-select v-model="sel" style="width:320px" @change="loadItems">
        <el-option v-for="v in versions" :key="v.versionCode" :label="`${v.versionCode} (${v.status})`" :value="v.versionCode" />
      </el-select>
      <el-button :disabled="!sel" @click="fillFromIndicators">从指标库填充</el-button>
      <el-button type="primary" :disabled="!sel" @click="saveItems">保存权重</el-button>
      <el-button type="success" :disabled="!sel" @click="publish">发布</el-button>
      <div style="margin-left:auto;opacity:.8;">权重和：<b>{{ sum.toFixed(6) }}</b></div>
    </div>

    <el-table :data="items" border style="width:100%">
      <el-table-column prop="indicatorCode" label="指标编码" width="200" />
      <el-table-column label="weight（和=1）">
        <template #default="{row}">
          <el-input-number v-model="row.weight" :min="0" :max="1" :step="0.01" style="width:100%" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ $index }">
          <el-button size="small" type="danger" @click="items.splice($index,1)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { weightApi } from '../api/weight'
import { indicatorApi } from '../api/indicator'

const versions = ref([])
const sel = ref('')
const newCode = ref('W_')
const items = ref([])

const sum = computed(() => items.value.reduce((a, b) => a + (Number(b.weight) || 0), 0))

async function loadVersions() {
  const r = await weightApi.listVersions()
  versions.value = r.data || []
}

async function createVersion() {
  try {
    if (!newCode.value || newCode.value.length < 3) throw new Error('版本号不能为空')
    await weightApi.createVersion(newCode.value, '')
    ElMessage.success('创建成功')
    await loadVersions()
    sel.value = newCode.value
    await loadItems()
  } catch (e) { ElMessage.error(e.message) }
}

async function loadItems() {
  try {
    if (!sel.value) return
    const r = await weightApi.listItems(sel.value)
    items.value = (r.data || []).map(x => ({ indicatorCode: x.indicatorCode, weight: Number(x.weight) }))
  } catch (e) { ElMessage.error(e.message) }
}

async function fillFromIndicators() {
  try {
    const r = await indicatorApi.list()
    const inds = (r.data || []).filter(x => x.enabled === 1)
    if (inds.length === 0) throw new Error('指标库为空')
    items.value = inds.map(x => ({ indicatorCode: x.code, weight: Number((1 / inds.length).toFixed(6)) }))
  } catch (e) { ElMessage.error(e.message) }
}

async function saveItems() {
  try {
    if (!sel.value) return
    await weightApi.upsertItems(sel.value, items.value)
    ElMessage.success('保存成功（服务端会校验权重和=1）')
    await loadItems()
  } catch (e) { ElMessage.error(e.message) }
}

async function publish() {
  try {
    await weightApi.publish(sel.value)
    ElMessage.success('已发布')
    await loadVersions()
  } catch (e) { ElMessage.error(e.message) }
}

onMounted(loadVersions)
</script>
