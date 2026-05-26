<template>
  <div class="convert-page">
    <div class="page-header">
      <h2>{{ lang === 'zh' ? '格式转换' : 'Format Converter' }}</h2>
      <p>{{ lang === 'zh' ? 'JSON/CSV转Markdown表格' : 'Convert JSON, CSV to Markdown tables - powered by server' }}</p>
    </div>

    <div class="convert-layout">
      <div class="convert-tabs">
        <button :class="['tab',{'tab--active':activeTab==='json'}]" @click="activeTab='json'">JSON</button>
        <button :class="['tab',{'tab--active':activeTab==='csv'}]" @click="activeTab='csv'">CSV</button>
        <button :class="['tab',{'tab--active':activeTab==='table'}]" @click="activeTab='table'">Table</button>
      </div>

      <div class="convert-body">
        <div class="input-panel">
          <div class="panel-header">
            <h3>Input</h3>
            <div class="panel-actions" v-if="activeTab==='json'">
              <button class="btn btn--sm" @click="loadJsonDemo">Demo</button>
              <select v-model="jsonFormat" class="format-select"><option value="table">Table</option><option value="code">Code</option></select>
            </div>
            <div class="panel-actions" v-if="activeTab==='csv'">
              <button class="btn btn--sm" @click="loadCsvDemo">Demo</button>
              <select v-model="csvDelimiter" class="format-select"><option value=",">Comma</option><option value=";">Semicolon</option><option value="\t">Tab</option></select>
            </div>
            <div class="panel-actions" v-if="activeTab==='table'">
              <button class="btn btn--sm" @click="addRow">+Row</button>
              <button class="btn btn--sm" @click="addCol">+Col</button>
            </div>
          </div>
          <textarea v-if="activeTab!=='table'" v-model="inputText" class="code-textarea" :placeholder="inputPlaceholder" rows="12"></textarea>
          <div v-if="activeTab==='table'" class="table-editor">
            <table><thead><tr><th v-for="(h,hi) in tblHeaders" :key="'h'+hi"><input v-model="tblHeaders[hi]" class="cell-input" placeholder="Col"/><span class="col-x" @click="tblHeaders.splice(hi,1)" v-if="tblHeaders.length>1">X</span></th></tr></thead>
            <tbody><tr v-for="(r,ri) in tblRows" :key="'r'+ri"><td v-for="(c,ci) in r" :key="'c'+ci"><input v-model="tblRows[ri][ci]" class="cell-input" placeholder="Val"/></td><td class="row-x-cell"><span class="row-x" @click="tblRows.splice(ri,1)" v-if="tblRows.length>1">X</span></td></tr></tbody></table>
          </div>
          <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
          <button class="btn btn--primary btn--full" @click="doConvert" :disabled="loading">{{ loading?'Converting...':'Convert' }}</button>
        </div>
        <div class="output-panel">
          <div class="panel-header">
            <h3>Output</h3>
            <div class="panel-actions">
              <button v-if="convertOutput" class="btn btn--sm" @click="showRaw=!showRaw">{{ showRaw?'Preview':'Raw'}}</button>
              <button v-if="convertOutput" class="btn btn--primary btn--sm" @click="downloadResult">DOWN .md</button>
            </div>
          </div>
          <div class="output-content">
            <div v-if="!convertOutput" class="output-empty"><p>Result will appear here</p></div>
            <div v-else-if="showRaw" class="md-raw"><pre>{{ convertOutput }}</pre></div>
            <div v-else class="md-rendered" v-html="resultHtml"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { renderMarkdown, downloadMarkdown } from '../utils/markdown.js'
import { convertJson, convertCsv, createTable } from '../utils/api.js'
import { useLang } from '../composables/useLang.js'

const { lang } = useLang()

const activeTab = ref('json')
const showRaw = ref(false)
const inputText = ref('')
const convertOutput = ref('')
const errorMsg = ref('')
const loading = ref(false)
const jsonFormat = ref('table')
const csvDelimiter = ref(',')
const tblHeaders = reactive(['Col A','Col B','Col C'])
const tblRows = reactive([['','',''],['','','']])

const resultHtml = computed(() => renderMarkdown(convertOutput.value))

const inputPlaceholder = computed(() => activeTab.value==='json'?'Paste JSON...':'Paste CSV (first row = header)...')

async function doConvert() {
  errorMsg.value = ''; loading.value = true
  try {
    let result
    if (activeTab.value === 'json') {
      if (!inputText.value.trim()) { errorMsg.value='Enter JSON'; loading.value=false; return }
      result = await convertJson(inputText.value, jsonFormat.value)
    } else if (activeTab.value === 'csv') {
      if (!inputText.value.trim()) { errorMsg.value='Enter CSV'; loading.value=false; return }
      result = await convertCsv(inputText.value, csvDelimiter.value)
    } else {
      result = await createTable(tblHeaders.filter(h=>h.trim()), tblRows)
    }
    convertOutput.value = result.markdown || ''
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    loading.value = false
  }
}

function loadJsonDemo() {
  inputText.value = JSON.stringify([
    {"id":1,"name":"Alice","age":25,"city":"Beijing","role":"Engineer"},
    {"id":2,"name":"Bob","age":30,"city":"Shanghai","role":"Manager"},
    {"id":3,"name":"Charlie","age":28,"city":"Shenzhen","role":"Designer"}
  ], null, 2)
}
function loadCsvDemo() { inputText.value = 'name,age,city,salary\nAlice,25,Beijing,15000\nBob,30,Shanghai,22000\nCharlie,28,Shenzhen,18000' }

function addRow() { tblRows.push(tblHeaders.map(()=>'')) }
function addCol() { tblHeaders.push('New'); tblRows.forEach(r=>r.push('')) }

function downloadResult() {
  const ts = new Date().toISOString().slice(0,16).replace('T','_').replace(':','')
  downloadMarkdown(convertOutput.value, 'converted_'+activeTab.value+'_'+ts+'.md')
}
</script>

<style scoped>
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; margin-bottom: 6px; }
.page-header p { color: var(--text-secondary); font-size: 14px; }
.convert-layout { display: flex; flex-direction: column; gap: 16px; }
.convert-tabs { display: flex; gap: 4px; background: var(--bg-secondary); padding: 4px; border-radius: var(--radius); border: 1px solid var(--border); }
.tab { flex: 1; padding: 8px 16px; background: transparent; border: none; color: var(--text-secondary); font-size: 13px; cursor: pointer; border-radius: 5px; transition: all .15s; }
.tab:hover { color: var(--text); }
.tab--active { background: var(--accent); color: #fff; }
.convert-body { display: flex; gap: 20px; height: calc(100vh - 230px); }
.input-panel,.output-panel { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.panel-header h3 { font-size: 14px; }
.panel-actions { display: flex; gap: 8px; align-items: center; }
.btn { padding: 8px 18px; background: var(--bg-tertiary); border: 1px solid var(--border); color: var(--text); border-radius: 5px; cursor: pointer; font-size: 13px; transition: all .15s; }
.btn:hover { border-color: var(--text-secondary); }
.btn:disabled { opacity: .5; cursor: default; }
.btn--primary { background: #238636; border-color: #2ea043; color: #fff; }
.btn--primary:hover { background: #2ea043; }
.btn--sm { padding: 5px 12px; font-size: 11px; }
.btn--full { width: 100%; margin-top: 10px; }
.format-select { padding: 4px 8px; background: var(--bg-tertiary); border: 1px solid var(--border); color: var(--text); border-radius: 4px; font-size: 12px; }
.code-textarea { width: 100%; flex: 1; padding: 14px; background: var(--bg-secondary); border: 1px solid var(--border); border-radius: var(--radius); color: var(--text); font-family: 'SF Mono',Consolas,monospace; font-size: 13px; line-height: 1.6; outline: none; resize: none; min-height: 200px; }
.code-textarea:focus { border-color: var(--accent); }
.error-msg { margin-top: 8px; padding: 8px 12px; background: rgba(248,81,73,.1); border: 1px solid rgba(248,81,73,.3); border-radius: 4px; color: var(--red); font-size: 12px; }
.output-panel { background: var(--bg-secondary); border: 1px solid var(--border); border-radius: var(--radius); padding: 12px 16px; overflow: hidden; }
.output-content { flex: 1; overflow-y: auto; }
.output-empty { display: flex; align-items: center; justify-content: center; height: 100%; color: var(--text-secondary); }
.md-rendered { line-height: 1.7; font-size: 15px; padding: 8px 0; }
.md-rendered :deep(table) { border-collapse: collapse; width: 100%; margin: 10px 0; font-size: 13px; }
.md-rendered :deep(th),.md-rendered :deep(td) { border: 1px solid var(--border); padding: 6px 10px; text-align: left; }
.md-rendered :deep(th) { background: var(--bg-tertiary); font-weight: 600; }
.md-rendered :deep(pre) { background: var(--bg); padding: 14px; border-radius: 6px; overflow-x: auto; }
.md-rendered :deep(code) { background: var(--bg-tertiary); padding: 2px 6px; border-radius: 3px; font-size: 13px; }
.md-raw pre { font-family: 'SF Mono',Consolas,monospace; font-size: 13px; line-height: 1.6; white-space: pre-wrap; word-wrap: break-word; }
.table-editor { overflow-x: auto; flex: 1; background: var(--bg-secondary); border: 1px solid var(--border); border-radius: var(--radius); padding: 12px; }
.table-editor table { border-collapse: collapse; width: max-content; }
.table-editor th,.table-editor td { position: relative; padding: 2px; border: 1px solid var(--border); }
.cell-input { width: 100px; padding: 6px 8px; background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text); font-size: 13px; outline: none; border-radius: 2px; }
.cell-input:focus { border-color: var(--accent); }
th .cell-input { font-weight: 600; color: var(--accent); }
.col-x { position: absolute; top: -6px; right: -6px; width: 14px; height: 14px; background: var(--red); color: #fff; border-radius: 50%; font-size: 8px; cursor: pointer; display: none; align-items: center; justify-content: center; }
th:hover .col-x { display: flex; }
.row-x-cell { border: none!important; padding: 2px!important; }
.row-x { width: 18px; height: 18px; background: transparent; border: 1px solid transparent; color: var(--text-secondary); border-radius: 50%; font-size: 10px; cursor: pointer; display: none; align-items: center; justify-content: center; }
tr:hover .row-x { display: flex; }
.row-x:hover { color: var(--red); border-color: var(--red); }
</style>