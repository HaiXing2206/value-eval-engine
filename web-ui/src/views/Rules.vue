<template>
  <el-card>
    <template #header>
      <div style="display:flex;gap:12px;align-items:center;justify-content:space-between;">
        <div style="font-weight:700;">规则配置</div>
        <div style="display:flex;gap:10px;align-items:center;">
          <span style="opacity:.7;">选择指标：</span>
          <el-select v-model="sel" style="width:280px" filterable>
            <el-option v-for="i in indicators" :key="i.code" :label="`${i.code} - ${i.name} (${i.metricType})`" :value="i.code" />
          </el-select>
        </div>
      </div>
    </template>

    <el-tabs v-model="tab">
      <el-tab-pane label="定性映射（QUAL）" name="qual">
        <el-alert type="info" show-icon title="适用于 metricType=QUAL，例如 高/中/低 → 分值(0~100)" />
        <el-table :data="qualRows" border style="margin-top:10px;">
          <el-table-column label="枚举值" prop="enumKey">
            <template #default="{row}">
              <el-input v-model="row.enumKey" placeholder="如 高" />
            </template>
          </el-table-column>
          <el-table-column label="分值" prop="score" width="160">
            <template #default="{row}">
              <el-input-number v-model="row.score" :min="0" :max="100" :step="1" style="width:100%" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ $index }">
              <el-button size="small" type="danger" @click="qualRows.splice($index,1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top:10px;display:flex;gap:10px;">
          <el-button @click="qualRows.push({enumKey:'',score:80})">新增一行</el-button>
          <el-button type="primary" :disabled="!sel" @click="saveQual">保存定性映射</el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="定量区间（QUANT）" name="quant">
        <el-alert type="info" show-icon title="适用于 metricType=QUANT，区间[min,max) → 分值(0~100)。建议用0~1区间。" />
        <el-table :data="quantRows" border style="margin-top:10px;">
          <el-table-column label="minVal">
            <template #default="{row}">
              <el-input-number v-model="row.minVal" :step="0.01" style="width:100%" />
            </template>
          </el-table-column>
          <el-table-column label="maxVal">
            <template #default="{row}">
              <el-input-number v-model="row.maxVal" :step="0.01" style="width:100%" />
            </template>
          </el-table-column>
          <el-table-column label="score" width="160">
            <template #default="{row}">
              <el-input-number v-model="row.score" :min="0" :max="100" :step="1" style="width:100%" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ $index }">
              <el-button size="small" type="danger" @click="quantRows.splice($index,1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top:10px;display:flex;gap:10px;">
          <el-button @click="quantRows.push({minVal:0,maxVal:1,score:80})">新增一行</el-button>
          <el-button type="primary" :disabled="!sel" @click="saveQuant">保存定量区间</el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="异常值策略（QUANT）" name="prep">
        <el-alert type="info" show-icon title="CLAMP=截断到[min,max]；FAIL=发现异常直接失败；MARK=标记异常但继续计算" />
        <div style="margin-top:12px;max-width:560px;">
          <el-form :model="prep" label-width="120px">
            <el-form-item label="outlierPolicy">
              <el-select v-model="prep.outlierPolicy" style="width:100%">
                <el-option label="CLAMP" value="CLAMP" />
                <el-option label="FAIL" value="FAIL" />
                <el-option label="MARK" value="MARK" />
              </el-select>
            </el-form-item>
            <el-form-item label="minVal">
              <el-input-number v-model="prep.minVal" :step="0.01" style="width:100%" />
            </el-form-item>
            <el-form-item label="maxVal">
              <el-input-number v-model="prep.maxVal" :step="0.01" style="width:100%" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :disabled="!sel" @click="savePreprocess">保存异常值策略</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { indicatorApi } from '../api/indicator'
import { ruleApi } from '../api/rule'

const indicators = ref([])
const sel = ref('')
const tab = ref('qual')

const qualRows = ref([{ enumKey: '高', score: 100 }, { enumKey: '中', score: 80 }, { enumKey: '低', score: 60 }])
const quantRows = ref([
  { minVal: 0, maxVal: 0.6, score: 40 },
  { minVal: 0.6, maxVal: 0.8, score: 70 },
  { minVal: 0.8, maxVal: 0.95, score: 90 },
  { minVal: 0.95, maxVal: 1.00000001, score: 100 }
])

const prep = ref({ outlierPolicy: 'CLAMP', minVal: 0, maxVal: 1.00000001 })

async function loadIndicators() {
  const r = await indicatorApi.list()
  indicators.value = (r.data || []).filter(x => x.enabled === 1)
}

async function saveQual() {
  try {
    await ruleApi.upsertQual(sel.value, qualRows.value)
    ElMessage.success('已保存定性映射')
  } catch (e) { ElMessage.error(e.message) }
}

async function saveQuant() {
  try {
    await ruleApi.upsertQuant(sel.value, quantRows.value)
    ElMessage.success('已保存定量区间')
  } catch (e) { ElMessage.error(e.message) }
}

async function savePreprocess() {
  try {
    await ruleApi.upsertPreprocess(sel.value, prep.value)
    ElMessage.success('已保存异常值策略')
  } catch (e) { ElMessage.error(e.message) }
}

onMounted(loadIndicators)
</script>
