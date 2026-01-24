<template>
  <el-card>
    <template #header>
      <div style="display:flex;align-items:center;justify-content:space-between;">
        <div style="font-weight:700;">口径版本管理</div>
        <div style="display:flex;gap:10px;">
          <el-input v-model="newCal" placeholder="CAL_20260124_001" style="width:240px;" />
          <el-input v-model="bindWeight" placeholder="绑定权重版本，如 W_20260124_001" style="width:260px;" />
          <el-button type="primary" @click="create">创建口径</el-button>
        </div>
      </div>
    </template>

    <div style="display:flex;gap:10px;align-items:center;margin-bottom:10px;">
      <span style="opacity:.7;">口径：</span>
      <el-input v-model="sel" placeholder="输入口径号后回车加载" style="width:260px" @keyup.enter="load" />
      <el-button :disabled="!sel" @click="load">加载</el-button>
      <el-button type="success" :disabled="!loaded" @click="publish">发布（生成快照）</el-button>
      <el-button :disabled="!loaded" @click="copyDlg=true">复制新版本</el-button>
      <div style="margin-left:auto;opacity:.8;">
        状态：<b>{{ model.status || '-' }}</b>
      </div>
    </div>

    <el-form :model="model" label-width="140px" style="max-width:720px;">
      <el-form-item label="weightVersionCode">
        <el-input v-model="model.weightVersionCode" :disabled="model.status==='PUBLISHED'" />
      </el-form-item>
      <el-form-item label="missingPolicy">
        <el-select v-model="model.missingPolicy" style="width:100%" :disabled="model.status==='PUBLISHED'">
          <el-option label="ZERO（缺失=0分）" value="ZERO" />
          <el-option label="WORST（缺失=最差分）" value="WORST" />
          <el-option label="FAIL（缺失失败）" value="FAIL" />
        </el-select>
      </el-form-item>
      <el-form-item label="等级阈值（A/B/C）">
        <div style="display:flex;gap:10px;width:100%">
          <el-input-number v-model="model.levelA" :min="0" :max="100" :disabled="model.status==='PUBLISHED'" />
          <el-input-number v-model="model.levelB" :min="0" :max="100" :disabled="model.status==='PUBLISHED'" />
          <el-input-number v-model="model.levelC" :min="0" :max="100" :disabled="model.status==='PUBLISHED'" />
        </div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="model.ruleRemark" type="textarea" :disabled="model.status==='PUBLISHED'" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="!loaded || model.status==='PUBLISHED'" @click="saveDraft">保存草稿</el-button>
      </el-form-item>
    </el-form>

    <el-divider />

    <el-collapse>
      <el-collapse-item title="快照（snapshot_json）" name="1">
        <el-input v-model="snapshot" type="textarea" :rows="12" readonly />
      </el-collapse-item>
    </el-collapse>
  </el-card>

  <el-dialog v-model="copyDlg" title="复制新口径（生成草稿）" width="520px">
    <el-form label-width="120px">
      <el-form-item label="newCaliberCode">
        <el-input v-model="copyNew" placeholder="CAL_20260124_002" />
      </el-form-item>
      <el-form-item label="remark">
        <el-input v-model="copyRemark" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="copyDlg=false">取消</el-button>
      <el-button type="primary" @click="doCopy">复制</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { caliberApi } from '../api/caliber'

const newCal = ref('CAL_')
const bindWeight = ref('W_')
const sel = ref('')
const loaded = ref(false)
const snapshot = ref('')

const model = reactive({
  caliberCode: '',
  weightVersionCode: '',
  missingPolicy: 'ZERO',
  levelA: 90,
  levelB: 75,
  levelC: 60,
  status: '',
  ruleRemark: '',
  snapshotJson: ''
})

const copyDlg = ref(false)
const copyNew = ref('CAL_')
const copyRemark = ref('')

async function create() {
  try {
    await caliberApi.create(newCal.value, bindWeight.value, '默认口径')
    ElMessage.success('口径已创建（草稿）')
    sel.value = newCal.value
    await load()
  } catch (e) { ElMessage.error(e.message) }
}

async function load() {
  try {
    const r = await caliberApi.get(sel.value)
    const d = r.data
    Object.assign(model, d)
    snapshot.value = d.snapshotJson || ''
    loaded.value = true
  } catch (e) {
    loaded.value = false
    ElMessage.error(e.message)
  }
}

async function saveDraft() {
  try {
    await caliberApi.patch(sel.value, {
      weightVersionCode: model.weightVersionCode,
      ruleRemark: model.ruleRemark,
      missingPolicy: model.missingPolicy,
      levelA: model.levelA,
      levelB: model.levelB,
      levelC: model.levelC
    })
    ElMessage.success('草稿已保存')
    await load()
  } catch (e) { ElMessage.error(e.message) }
}

async function publish() {
  try {
    await caliberApi.publish(sel.value)
    ElMessage.success('已发布（已生成快照）')
    await load()
  } catch (e) { ElMessage.error(e.message) }
}

async function doCopy() {
  try {
    await caliberApi.copy(sel.value, copyNew.value, copyRemark.value)
    ElMessage.success('已复制为新草稿')
    copyDlg.value = false
    sel.value = copyNew.value
    await load()
  } catch (e) { ElMessage.error(e.message) }
}
</script>
