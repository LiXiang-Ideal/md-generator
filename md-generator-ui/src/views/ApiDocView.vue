<template>
  <div class="api-page">
    <div class="page-header">
      <h2>{{ lang === 'zh' ? 'API文档' : 'API Document' }}</h2>
      <p>{{ lang === 'zh' ? '扫描Controller源码，生成API Markdown文档' : 'Scan Controller source to generate API Markdown document' }}</p>
    </div>

    <div class="api-layout">
      <div class="controller-panel">
        <h3 class="section-title">Controllers</h3>
        <div class="controller-list">
          <div v-for="(ctrl,cIdx) in controllerList" :key="cIdx" class="ctrl-card" :class="{'ctrl-card--active':activeCtrl===cIdx}" @click="activeCtrl=cIdx;activeApi=null">
            <div class="ctrl-name">{{ ctrl.name }}</div>
            <div class="ctrl-tag">{{ ctrl.tag }}</div>
            <div class="ctrl-badge">{{ ctrl.apiCount || ctrl.apis.length }} APIs</div>
          </div>
        </div>

        <div class="scan-section">
          <div class="dir-row">
            <input v-model="sourceDir" class="form-input" placeholder="Source dir path..." />
            <label class="btn btn--sm dir-btn">
              {{ lang==='zh'?'选择文件夹':'Choose Dir' }}
              <input type="file" webkitdirectory directory multiple @change="onDirPicked" style="display:none" />
            </label>
          </div>
          <div class="dir-actions">
            <button class="btn btn--primary btn--sm" @click="scanSource">{{ lang==='zh'?'扫描':'Scan' }}</button>
            <button class="btn btn--sm" @click="loadDemo">{{ lang==='zh'?'演示':'Demo' }}</button>
          </div>
          <div v-if="scanError" class="error-msg">{{ scanError }}</div>
        </div>
      </div>

      <div v-if="activeCtrl!==null" class="api-list-panel">
        <h3 class="section-title">APIs — {{ controllerList[activeCtrl].name }}</h3>
        <div class="api-list">
          <div v-for="(api,aIdx) in controllerList[activeCtrl].apis" :key="aIdx" class="api-card"
               :class="{'api-card--active':activeApi===aIdx,'api-card--deprecated':api.deprecated}" @click="activeApi=aIdx">
            <div class="api-method-row">
              <span :class="'method-badge method-'+api.httpMethod.toLowerCase()">{{ api.httpMethod }}</span>
              <span class="api-path">{{ api.path }}</span>
            </div>
            <div class="api-summary">{{ api.summary }}</div>
          </div>
        </div>
      </div>

      <div v-if="activeApi!==null&&activeCtrl!==null" class="detail-panel">
        <div class="detail-header"><h3>API Detail</h3></div>
        <div class="detail-body">
          <template v-if="selectedApi">
            <div class="detail-method">
              <span :class="'method-badge method-badge--lg method-'+selectedApi.httpMethod.toLowerCase()">{{ selectedApi.httpMethod }}</span>
              <span class="detail-path">{{ selectedApi.path }}</span>
            </div>
            <div class="detail-section">
              <h4>Summary</h4>
              <p>{{ selectedApi.summary || 'N/A' }}</p>
              <div class="detail-meta">
                <span>Controller: <code>{{ selectedApi.controllerClass }}</code></span>
                <span>Method: <code>{{ selectedApi.methodName }}()</code></span>
              </div>
            </div>
            <div v-if="selectedApi.parameters&&selectedApi.parameters.length>0" class="detail-section">
              <h4>Parameters</h4>
              <table class="detail-table">
                <thead><tr><th>Name</th><th>Type</th><th>Location</th><th>Required</th><th>Description</th></tr></thead>
                <tbody><tr v-for="p in selectedApi.parameters" :key="p.name">
                  <td><code>{{ p.name }}</code></td>
                  <td>{{ p.dataType }}</td>
                  <td><span :class="'param-type param-'+p.paramType">{{ p.paramType }}</span></td>
                  <td>{{ p.required?'YES':'-' }}</td>
                  <td>{{ p.description || '-' }}</td>
                </tr></tbody>
              </table>
            </div>
            <div class="detail-section">
              <h4>Return Type</h4>
              <code class="return-type">{{ selectedApi.returnType || 'void' }}</code>
            </div>
          </template>
        </div>
      </div>

      <div v-else class="empty-panel"><div class="empty-icon">API</div><p>Select a controller and an API</p></div>
    </div>

    <div class="bottom-bar">
      <div class="bottom-info">{{ totalApis }} APIs</div>
      <div class="bottom-actions">
        <button class="btn" @click="showRawMd=!showRawMd">{{ showRawMd?'Hide':'Show'}} Markdown</button>
        <button class="btn btn--primary" @click="downloadMd">DOWN API Doc.md</button>
      </div>
    </div>

    <div v-if="showRawMd" class="md-modal">
      <div class="md-modal-content">
        <div class="md-modal-header">
          <h3>Generated API Markdown</h3>
          <button class="btn" @click="showRawMd=false">X</button>
        </div>
        <pre class="md-raw-preview">{{ genMd }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { downloadMarkdown } from '../utils/markdown.js'
import { generateApiDoc } from '../utils/api.js'
import { useLang } from '../composables/useLang.js'

const { lang } = useLang()

const controllerList = ref([])
const activeCtrl = ref(null)
const activeApi = ref(null)
const showRawMd = ref(false)
const sourceDir = ref('')
const scanError = ref('')
const backendMd = ref('')

function onDirPicked(e) {
  const files = e.target.files
  if (files && files.length > 0) {
    // Get the directory path from webkitRelativePath
    const path = files[0].webkitRelativePath
    const dir = path.substring(0, path.indexOf('/'))
    sourceDir.value = 'C:\\your\\path\\' + dir // placeholder; browsers don't expose real path
    // Instead, use the full relative path info for the user to know
    sourceDir.value = files[0].webkitRelativePath.split('/')[0] // just the root folder name
  }
}

const selectedApi = computed(() => {
  if (activeCtrl.value===null||activeApi.value===null) return null
  const ctrl = controllerList.value[activeCtrl.value]
  return ctrl && ctrl.apis ? ctrl.apis[activeApi.value] : null
})

const totalApis = computed(() => controllerList.value.reduce((s,c)=>s+(c.apiCount||c.apis.length),0))

const genMd = computed(() => {
  const t = lang.value
  const title = t === 'zh' ? 'API文档' : 'API Document'
  const controllersLabel = t === 'zh' ? '控制器' : 'Controllers'
  const apisLabel = t === 'zh' ? '接口' : 'APIs'
  const generatedLabel = t === 'zh' ? '生成时间' : 'Generated'
  const indexLabel = t === 'zh' ? '目录' : 'Index'
  const controllerLabel = t === 'zh' ? '控制器' : 'Controller'
  const methodLabel = t === 'zh' ? '方法' : 'Method'
  const paramsLabel = t === 'zh' ? '参数' : 'Parameters'
  const nameLabel = t === 'zh' ? '名称' : 'Name'
  const typeLabel = t === 'zh' ? '类型' : 'Type'
  const locationLabel = t === 'zh' ? '位置' : 'Location'
  const requiredLabel = t === 'zh' ? '必填' : 'Required'
  const descLabel = t === 'zh' ? '说明' : 'Description'
  const returnLabel = t === 'zh' ? '返回值' : 'Return'
  const generatedBy = t === 'zh' ? '本文档由 MD Generator 自动生成' : 'Generated by MD Generator'

  let md = '# ' + title + '\n\n'
  md += '**' + controllersLabel + '**: ' + controllerList.value.length + ' | **' + apisLabel + '**: ' + totalApis.value + ' | **' + generatedLabel + '**: ' + new Date().toISOString().slice(0,19).replace('T',' ') + '\n\n---\n\n## ' + indexLabel + '\n\n'
  controllerList.value.forEach(ctrl => {
    md += '- ['+ctrl.name+'](#'+ctrl.name.toLowerCase().replace(/[^a-z0-9]/g,'-')+')\n'
    ;(ctrl.apis||[]).forEach(a => md += '  - ['+a.httpMethod+' '+a.path+'](#'+a.httpMethod.toLowerCase()+'-'+(a.path||'').replace(/[/{}]/g,'_')+')\n')
  })
  md += '\n---\n\n'
  let idx=1
  controllerList.value.forEach(ctrl => {
    md += '## '+ctrl.name+(ctrl.tag?' ('+ctrl.tag+')':'')+'\n\n'
    ;(ctrl.apis||[]).forEach(api => {
      md += '### '+(idx++)+'. '+api.httpMethod+' '+api.path+' ('+api.summary+')\n\n'
      md += '**' + controllerLabel + '**: `'+api.controllerClass+'` | **' + methodLabel + '**: `'+api.methodName+'()`\n\n'
      if (api.parameters&&api.parameters.length) {
        md += '**' + paramsLabel + '**:\n\n| ' + nameLabel + ' | ' + typeLabel + ' | ' + locationLabel + ' | ' + requiredLabel + ' | ' + descLabel + ' |\n| --- | --- | --- | --- | --- |\n'
        api.parameters.forEach(p=>md+='| '+p.name+' | '+p.dataType+' | '+p.paramType+' | '+(p.required?(t==='zh'?'是':'Yes'):'-')+' | '+(p.description||'-')+' |\n')
        md += '\n'
      }
      md += '**' + returnLabel + '**: `'+api.returnType+'`\n\n---\n\n'
    })
  })
  md += '\n*' + generatedBy + '*\n'
  return md
})

async function scanSource() {
  if (!sourceDir.value) { scanError.value='Please enter source directory'; return }
  scanError.value = ''
  try {
    const result = await generateApiDoc({ sourceDir: sourceDir.value, title: 'API Document' })
    if (result.controllers) controllerList.value = result.controllers
    if (result.markdown) backendMd.value = result.markdown
    activeCtrl.value = 0; activeApi.value = null
  } catch (e) {
    scanError.value = e.message
  }
}

function loadDemo() { controllerList.value = demoData(); activeCtrl.value=0; activeApi.value=null }
function downloadMd() { downloadMarkdown(backendMd.value || genMd.value, 'api_doc.md') }

function demoData() {
  return [
    { name:'UserController',tag:'User',apiCount:5,apis:[
      {path:'/api/user/list',httpMethod:'GET',summary:'Get user list',controllerClass:'UserController',methodName:'list',returnType:'Result<PageVO<UserVO>>',parameters:[{name:'page',paramType:'query',dataType:'Integer',required:false,description:'Page'},{name:'size',paramType:'query',dataType:'Integer',required:false,description:'Size'}]},
      {path:'/api/user/{id}',httpMethod:'GET',summary:'Get user by ID',controllerClass:'UserController',methodName:'getById',returnType:'Result<UserVO>',parameters:[{name:'id',paramType:'path',dataType:'Long',required:true,description:'User ID'}]},
      {path:'/api/user',httpMethod:'POST',summary:'Create user',controllerClass:'UserController',methodName:'create',returnType:'Result<Long>',parameters:[{name:'body',paramType:'body',dataType:'UserCreateDTO',required:true,description:'User info'}]},
      {path:'/api/user/{id}',httpMethod:'PUT',summary:'Update user',controllerClass:'UserController',methodName:'update',returnType:'Result<Void>',parameters:[{name:'id',paramType:'path',dataType:'Long',required:true,description:'User ID'},{name:'body',paramType:'body',dataType:'UserUpdateDTO',required:true}]},
      {path:'/api/user/{id}',httpMethod:'DELETE',summary:'Delete user',controllerClass:'UserController',methodName:'delete',returnType:'Result<Void>',parameters:[{name:'id',paramType:'path',dataType:'Long',required:true,description:'User ID'}]}
    ]},
    { name:'ProductController',tag:'Product',apiCount:3,apis:[
      {path:'/api/product/list',httpMethod:'GET',summary:'Get product list',controllerClass:'ProductController',methodName:'list',returnType:'Result<PageVO<ProductVO>>',parameters:[{name:'page',paramType:'query',dataType:'Integer',required:false},{name:'categoryId',paramType:'query',dataType:'Long',required:false,description:'Category'}]},
      {path:'/api/product/{id}',httpMethod:'GET',summary:'Get product detail',controllerClass:'ProductController',methodName:'getById',returnType:'Result<ProductVO>',parameters:[{name:'id',paramType:'path',dataType:'Long',required:true}]},
      {path:'/api/product',httpMethod:'POST',summary:'Create product',controllerClass:'ProductController',methodName:'create',returnType:'Result<Long>',parameters:[{name:'body',paramType:'body',dataType:'ProductCreateDTO',required:true}]}
    ]}
  ]
}
</script>

<style scoped>
.page-header { margin-bottom: 16px; }
.page-header h2 { font-size: 22px; margin-bottom: 6px; }
.page-header p { color: var(--text-secondary); font-size: 14px; }
.api-layout { display: flex; gap: 16px; height: calc(100vh - 240px); }
.controller-panel { width: 220px; min-width: 200px; display: flex; flex-direction: column; gap: 8px; }
.api-list-panel { width: 240px; min-width: 200px; overflow-y: auto; }
.detail-panel { flex: 1; overflow-y: auto; background: var(--bg-secondary); border: 1px solid var(--border); border-radius: var(--radius); }
.empty-panel { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; color: var(--text-secondary); background: var(--bg-secondary); border: 1px solid var(--border); border-radius: var(--radius); }
.empty-icon { font-size: 40px; margin-bottom: 12px; }
.section-title { font-size: 14px; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid var(--border); }
.controller-list { display: flex; flex-direction: column; gap: 6px; flex: 1; overflow-y: auto; }
.ctrl-card { padding: 10px 12px; background: var(--bg-secondary); border: 1px solid var(--border); border-radius: 6px; cursor: pointer; transition: all .15s; }
.ctrl-card:hover { border-color: var(--text-secondary); }
.ctrl-card--active { border-color: var(--accent); background: rgba(88,166,255,.06); }
.ctrl-name { font-size: 14px; font-weight: 600; }
.ctrl-tag { font-size: 12px; color: var(--text-secondary); }
.ctrl-badge { font-size: 11px; background: rgba(88,166,255,.1); color: var(--accent); padding: 1px 8px; border-radius: 8px; display: inline-block; margin-top: 4px; }
.scan-section { margin-top: 10px; display: flex; flex-direction: column; gap: 6px; }
.dir-row { display: flex; gap: 6px; }
.dir-row .form-input { flex: 1; }
.dir-actions { display: flex; gap: 6px; }
.dir-btn { cursor: pointer; display: inline-block; }
.form-input { width: 100%; padding: 8px 12px; background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text); border-radius: 5px; font-size: 13px; outline: none; }
.form-input:focus { border-color: var(--accent); }
.error-msg { padding: 6px 10px; background: rgba(248,81,73,.1); border: 1px solid rgba(248,81,73,.3); border-radius: 4px; color: var(--red); font-size: 11px; }
.btn { padding: 8px 18px; background: var(--bg-tertiary); border: 1px solid var(--border); color: var(--text); border-radius: 5px; cursor: pointer; font-size: 13px; transition: all .15s; }
.btn:hover { border-color: var(--text-secondary); }
.btn--primary { background: #238636; border-color: #2ea043; color: #fff; }
.btn--primary:hover { background: #2ea043; }
.btn--full { width: 100%; }
.api-list { display: flex; flex-direction: column; gap: 6px; }
.api-card { padding: 10px; background: var(--bg-secondary); border: 1px solid var(--border); border-radius: 6px; cursor: pointer; transition: all .15s; }
.api-card:hover { border-color: var(--text-secondary); }
.api-card--active { border-color: var(--accent); background: rgba(88,166,255,.06); }
.api-card--deprecated { opacity: .6; }
.api-method-row { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.method-badge { font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 3px; min-width: 42px; text-align: center; }
.method-badge--lg { font-size: 12px; padding: 4px 10px; min-width: 54px; }
.method-get { background: rgba(63,185,80,.15); color: #3fb950; }
.method-post { background: rgba(88,166,255,.15); color: #58a6ff; }
.method-put { background: rgba(210,153,29,.15); color: #d2991d; }
.method-delete { background: rgba(248,81,73,.15); color: #f85149; }
.api-path { font-size: 12px; font-family: 'SF Mono',Consolas,monospace; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.api-summary { font-size: 12px; color: var(--text-secondary); }
.detail-header { padding: 12px 16px; background: var(--bg-tertiary); border-bottom: 1px solid var(--border); }
.detail-header h3 { font-size: 14px; }
.detail-body { padding: 16px 20px; }
.detail-method { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; }
.detail-path { font-size: 16px; font-family: 'SF Mono',Consolas,monospace; font-weight: 600; }
.detail-section { margin-bottom: 20px; }
.detail-section h4 { font-size: 13px; color: var(--text-secondary); margin-bottom: 8px; padding-bottom: 6px; border-bottom: 1px solid var(--border); }
.detail-meta { display: flex; gap: 20px; margin-top: 8px; font-size: 12px; color: var(--text-secondary); }
.detail-meta code { background: var(--bg-tertiary); padding: 2px 6px; border-radius: 3px; font-size: 12px; }
.detail-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.detail-table th,.detail-table td { border: 1px solid var(--border); padding: 6px 10px; text-align: left; }
.detail-table th { background: var(--bg-tertiary); font-weight: 600; font-size: 12px; }
.param-type { font-size: 10px; padding: 1px 5px; border-radius: 3px; text-transform: uppercase; font-weight: 600; }
.param-path { background: rgba(88,166,255,.15); color: var(--accent); }
.param-query { background: rgba(63,185,80,.15); color: var(--green); }
.param-body { background: rgba(210,153,29,.15); color: var(--orange); }
.param-header { background: rgba(188,140,255,.15); color: #bc8cff; }
.return-type { font-family: 'SF Mono',Consolas,monospace; font-size: 13px; color: var(--green); }
.bottom-bar { display: flex; align-items: center; justify-content: space-between; padding: 10px 0; margin-top: 12px; border-top: 1px solid var(--border); }
.bottom-info { font-size: 13px; color: var(--text-secondary); }
.bottom-actions { display: flex; gap: 10px; }
.md-modal { position: fixed; inset: 0; background: rgba(0,0,0,.7); display: flex; align-items: center; justify-content: center; z-index: 100; }
.md-modal-content { width: 800px; max-height: 80vh; background: var(--bg-secondary); border: 1px solid var(--border); border-radius: var(--radius); overflow: hidden; display: flex; flex-direction: column; }
.md-modal-header { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; background: var(--bg-tertiary); border-bottom: 1px solid var(--border); }
.md-raw-preview { padding: 20px; overflow-y: auto; font-family: 'SF Mono',Consolas,monospace; font-size: 13px; line-height: 1.6; white-space: pre-wrap; word-wrap: break-word; max-height: 60vh; }
</style>