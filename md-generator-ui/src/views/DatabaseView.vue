<template>
  <div class="db-page">
    <div class="page-header">
      <h2>{{ lang === 'zh' ? '数据库文档' : 'Database Document' }}</h2>
      <p>{{ lang === 'zh' ? '连接数据库，选择表，生成文档' : 'Connect, select tables, generate doc' }}</p>
    </div>

    <div class="db-layout">
      <div class="config-panel">
        <h3 class="section-title">{{ lang==='zh'?'连接信息':'Connection' }}</h3>
        <div class="form-grid">
          <div class="form-group">
            <label>{{ lang==='zh'?'类型':'Type' }}</label>
            <select v-model="config.dbType" class="form-input"><option value="mysql">MySQL</option></select>
          </div>
          <div class="form-group">
            <label>{{ lang==='zh'?'主机':'Host' }}</label>
            <input v-model="config.host" class="form-input" placeholder="localhost" />
          </div>
          <div class="form-group">
            <label>{{ lang==='zh'?'端口':'Port' }}</label>
            <input v-model="config.port" class="form-input" placeholder="3306" />
          </div>
          <div class="form-group">
            <label>{{ lang==='zh'?'数据库名':'Database' }}</label>
            <input v-model="config.dbName" class="form-input" placeholder="mydb" />
          </div>
          <div class="form-group">
            <label>{{ lang==='zh'?'用户名':'Username' }}</label>
            <input v-model="config.username" class="form-input" placeholder="root" />
          </div>
          <div class="form-group">
            <label>{{ lang==='zh'?'密码':'Password' }}</label>
            <input v-model="config.password" type="password" class="form-input" />
          </div>
        </div>

        <div class="form-actions">
          <button class="btn btn--primary" @click="doConnect" :disabled="loading">
            {{ loading ? (lang==='zh'?'连接中...':'Connecting...') : (lang==='zh'?'连接数据库':'Connect') }}
          </button>
          <button class="btn" @click="loadDemo" :disabled="loading">{{ lang==='zh'?'加载演示':'Load Demo' }}</button>
          <button v-if="connected" class="btn" @click="doDisconnect">{{ lang==='zh'?'断开':'Disconnect' }}</button>
        </div>
        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

        <!-- 表列表勾选 -->
        <div v-if="tableList.length > 0" class="table-select-section">
          <h4>{{ lang==='zh'?'选择要生成的表':'Select tables' }}
            <button class="btn btn--sm" @click="toggleAll">{{ selAll ? (lang==='zh'?'取消全选':'Deselect All') : (lang==='zh'?'全选':'Select All') }}</button>
          </h4>
          <div class="table-checks">
            <label v-for="t in tableList" :key="t.tableName" class="table-check-row">
              <input type="checkbox" v-model="t.checked" />
              <span class="tname">{{ t.tableName }}</span>
              <span class="tcomment" v-if="t.tableComment">{{ t.tableComment }}</span>
            </label>
          </div>
          <button class="btn btn--primary btn--full" @click="doGenerate">
            {{ lang==='zh'?'生成文档':'Generate' }}
          </button>
        </div>
      </div>

      <div class="preview-panel">
        <div class="preview-header">
          <h3>Preview</h3>
          <div class="preview-actions">
            <button v-if="mdContent" class="btn btn--primary" @click="downloadDoc">DOWN .md</button>
            <button v-if="mdContent" class="btn" @click="showRaw=!showRaw">{{ showRaw?'EYE':'RAW' }}</button>
          </div>
        </div>
        <div class="preview-content">
          <div v-if="!mdContent" class="preview-empty"><div class="empty-icon">DB</div><p>{{ lang==='zh'?'点击连接或加载演示':'Connect or load demo' }}</p></div>
          <div v-else-if="showRaw" class="md-raw"><pre>{{ mdContent }}</pre></div>
          <div v-else class="md-rendered" v-html="renderedHtml"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { renderMarkdown, downloadMarkdown } from '../utils/markdown.js'
import { connectDatabase, generateDbDoc, disconnectDatabase } from '../utils/api.js'
import { useLang } from '../composables/useLang.js'

const { lang, setLang } = useLang()
const showRaw = ref(false); const mdContent = ref(''); const loading = ref(false); const errorMsg = ref('')
const connected = ref(false); const tableList = ref([]); const sessionId = ref('')

const config = reactive({ dbType:'mysql', host:'localhost', port:'3306', dbName:'', username:'root', password:'' })

const renderedHtml = computed(() => renderMarkdown(mdContent.value))
const selAll = computed(() => tableList.value.length > 0 && tableList.value.every(t => t.checked))
const selCount = computed(() => tableList.value.filter(t => t.checked).length)

async function doConnect() {
  if (!config.dbName || !config.password) {
    errorMsg.value = lang.value === 'zh' ? '请输入数据库名和密码' : 'Please enter database name and password'
    return
  }
  loading.value = true; errorMsg.value = ''
  try {
    const result = await connectDatabase({ ...config })
    // 后端返回的数据结构是 { sessionId: "...", tables: [...], dbType: "...", dbName: "..." }
    if (!result.tables || result.tables.length === 0) {
      errorMsg.value = lang.value === 'zh' ? '连接成功但未找到任何表' : 'Connected but no tables found'
      connected.value = true; tableList.value = []; sessionId.value = result.sessionId || ''
      return
    }
    // 保存 sessionId 用于后续生成文档请求
    sessionId.value = result.sessionId || ''
    tableList.value = result.tables.map(t => ({ ...t, checked: true }))
    connected.value = true
  } catch (e) {
    console.error('DB connect error:', e)
    errorMsg.value = e.message || (lang.value === 'zh' ? '连接失败' : 'Connection failed')
  } finally { loading.value = false }
}

async function doDisconnect() {
  try { await disconnectDatabase({ sessionId: sessionId.value }) } catch (e) { console.error('Disconnect error:', e) }
  tableList.value = []; connected.value = false; sessionId.value = ''
}

async function doGenerate() {
  if (!sessionId.value) {
    errorMsg.value = lang.value === 'zh' ? '会话已过期，请重新连接' : 'Session expired, please reconnect'
    return
  }
  const sel = tableList.value.filter(t => t.checked).map(t => t.tableName)
  if (sel.length === 0) {
    errorMsg.value = lang.value === 'zh' ? '请至少选择一张表' : 'Select at least one table'
    return
  }
  loading.value = true; errorMsg.value = ''
  try {
    const result = await generateDbDoc({ sessionId: sessionId.value, tables: sel, language: lang.value })
    mdContent.value = result.markdown || ''
  } catch (e) {
    console.error('Generate error:', e)
    errorMsg.value = e.message || (lang.value === 'zh' ? '生成失败' : 'Generate failed')
  } finally { loading.value = false }
}

function toggleAll() {
  const v = !selAll.value; tableList.value.forEach(t => t.checked = v)
}

function loadDemo() {
  lang.value = 'zh'; mdContent.value = demoZh(); tableList.value = []
}
function downloadDoc() { downloadMarkdown(mdContent.value, 'database_doc_'+(config.dbName||'db')+'.md') }

function demoZh() {
  const ts = [ {n:'user_info',c:'用户信息表',cols:[['id','BIGINT(20)','✓','✓','','用户ID，自增'],['username','VARCHAR(50)','','✓','','用户名'],['email','VARCHAR(100)','','✓','','邮箱'],['password_hash','VARCHAR(255)','','✓','','密码哈希'],['phone','VARCHAR(20)','','','','手机号'],['status','TINYINT(1)','','✓','1','0禁用 1启用'],['created_at','DATETIME','','✓','CURRENT_TIMESTAMP','创建时间']]}, {n:'product',c:'商品表',cols:[['id','BIGINT(20)','✓','✓','','商品ID'],['name','VARCHAR(200)','','✓','','商品名称'],['category_id','BIGINT(20)','','✓','','分类ID'],['price','DECIMAL(10,2)','','✓','0.00','价格'],['stock','INT(11)','','✓','0','库存']]}, {n:'order_main',c:'订单表',cols:[['id','BIGINT(20)','✓','✓','','订单ID'],['order_no','VARCHAR(32)','','✓','','订单编号'],['user_id','BIGINT(20)','','✓','','用户ID'],['total_amount','DECIMAL(12,2)','','✓','0.00','总金额'],['pay_status','TINYINT(1)','','✓','0','0未付 1已付 2退款'],['created_at','DATETIME','','✓','CURRENT_TIMESTAMP','下单时间']]}]
  let md = '# 数据库设计文档\n\n**数据库**: demo_db | **表数量**: '+ts.length+' | **生成时间**: '+new Date().toISOString().slice(0,19).replace('T',' ')+'\n\n---\n\n## 目录\n\n'
  ts.forEach((t,i) => md += '- ['+(i+1)+'. '+t.n+' ('+t.c+')](#'+t.n.toLowerCase()+')\n')
  md += '\n---\n\n'
  ts.forEach((t,i) => {
    md += '<a id="'+t.n.toLowerCase()+'"></a>\n\n## '+(i+1)+'. '+t.n+' ('+t.c+')\n\n**存储引擎**: InnoDB | **字段数**: '+t.cols.length+'\n\n| 序号 | 字段名 | 类型 | 主键 | 非空 | 默认值 | 说明 |\n| --- | --- | --- | --- | --- | --- | --- |\n'
    t.cols.forEach((c,ci) => md += '| '+(ci+1)+' | '+c.join(' | ')+' |\n')
    md += '\n---\n\n'
  })
  md += '*本文档由 MD Generator 自动生成*\n'
  return md
}
</script>

<style scoped>
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; margin-bottom: 6px; }
.page-header p { color: var(--text-secondary); font-size: 14px; }
.db-layout { display: flex; gap: 24px; height: calc(100vh - 160px); }
.config-panel { width: 420px; min-width: 360px; overflow-y: auto; padding-right: 8px; }
.section-title { font-size: 15px; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 1px solid var(--border); }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 12px; color: var(--text-secondary); margin-bottom: 5px; font-weight: 500; }
.form-input { width: 100%; padding: 8px 12px; background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text); border-radius: 5px; font-size: 13px; outline: none; }
.form-input:focus { border-color: var(--accent); }
.form-actions { display: flex; gap: 10px; margin-top: 16px; flex-wrap: wrap; }
.btn { padding: 8px 18px; background: var(--bg-tertiary); border: 1px solid var(--border); color: var(--text); border-radius: 5px; cursor: pointer; font-size: 13px; transition: all .15s; }
.btn:hover { border-color: var(--text-secondary); }
.btn:disabled { opacity: .5; cursor: default; }
.btn--primary { background: #238636; border-color: #2ea043; color: #fff; }
.btn--primary:hover { background: #2ea043; }
.btn--sm { padding: 4px 10px; font-size: 11px; }
.btn--full { width: 100%; margin-top: 6px; }
.error-msg { margin-top: 12px; padding: 8px 12px; background: rgba(248,81,73,.1); border: 1px solid rgba(248,81,73,.3); border-radius: 4px; color: var(--red); font-size: 12px; }
.table-select-section { margin-top: 20px; padding-top: 16px; border-top: 1px solid var(--border); }
.table-select-section h4 { font-size: 13px; margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center; }
.table-checks { max-height: 300px; overflow-y: auto; margin-bottom: 10px; }
.table-check-row { display: flex; align-items: center; gap: 8px; padding: 6px 8px; cursor: pointer; border-radius: 4px; font-size: 13px; }
.table-check-row:hover { background: var(--bg-tertiary); }
.table-check-row input { accent-color: var(--accent); }
.tname { font-family: 'SF Mono',Consolas,monospace; color: var(--accent); font-weight: 600; }
.tcomment { color: var(--text-secondary); font-size: 12px; }
.preview-panel { flex: 1; display: flex; flex-direction: column; background: var(--bg-secondary); border: 1px solid var(--border); border-radius: var(--radius); overflow: hidden; min-width: 0; }
.preview-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: var(--bg-tertiary); border-bottom: 1px solid var(--border); }
.preview-header h3 { font-size: 14px; }
.preview-actions { display: flex; gap: 8px; }
.preview-content { flex: 1; overflow-y: auto; padding: 16px 24px; }
.preview-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: var(--text-secondary); }
.empty-icon { font-size: 40px; margin-bottom: 12px; }
.md-rendered { line-height: 1.7; font-size: 15px; }
.md-rendered :deep(h1) { font-size: 24px; border-bottom: 1px solid var(--border); padding-bottom: 8px; margin: 20px 0 12px; }
.md-rendered :deep(h2) { font-size: 20px; border-bottom: 1px solid var(--border); padding-bottom: 6px; margin: 18px 0 10px; }
.md-rendered :deep(table) { border-collapse: collapse; width: 100%; margin: 10px 0; font-size: 13px; }
.md-rendered :deep(th),.md-rendered :deep(td) { border: 1px solid var(--border); padding: 6px 10px; text-align: left; }
.md-rendered :deep(th) { background: var(--bg-tertiary); font-weight: 600; }
.md-rendered :deep(hr) { border: none; border-top: 1px solid var(--border); margin: 20px 0; }
.md-raw pre { font-family: 'SF Mono',Consolas,monospace; font-size: 13px; line-height: 1.6; white-space: pre-wrap; }
</style>