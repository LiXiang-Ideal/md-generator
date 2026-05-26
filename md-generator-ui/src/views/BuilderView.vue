<template>
  <div class="builder-page">
    <div class="page-header">
      <h2>{{ lang === 'zh' ? 'Markdown构建器' : 'Markdown Builder' }}</h2>
      <p>{{ lang === 'zh' ? '自由组合Markdown元素，实时预览，一键下载' : 'Compose Markdown elements, live preview, one-click download' }}</p>
    </div>

    <div class="builder-layout">
      <div class="editor-panel">
        <div class="toolbar">
          <button class="tb-btn" @click="addHeading(1)" title="一级标题">H1</button>
          <button class="tb-btn" @click="addHeading(2)" title="二级标题">H2</button>
          <button class="tb-btn" @click="addHeading(3)" title="三级标题">H3</button>
          <span class="tb-sep"></span>
          <button class="tb-btn" @click="addParagraph" title="段落">p</button>
          <button class="tb-btn" @click="addCodeBlock" title="代码块">```</button>
          <button class="tb-btn" @click="addBlockQuote" title="引用">></button>
          <button class="tb-btn" @click="addTable" title="表格">TABLE</button>
          <button class="tb-btn" @click="addUnorderedList" title="无序列表">UL</button>
          <button class="tb-btn" @click="addOrderedList" title="有序列表">OL</button>
          <button class="tb-btn" @click="addHorizontalRule" title="分隔线">HR</button>
          <span class="tb-sep"></span>
          <button class="tb-btn tb-btn--danger" @click="clearAll" title="清空">X Clear</button>
        </div>

        <div class="elements-area">
          <div v-if="elements.length === 0" class="elements-empty">
            <div class="empty-icon">[ ]</div>
            <p>Click toolbar buttons above to add Markdown elements</p>
          </div>

          <div v-for="(el, idx) in elements" :key="el.id" class="element-card">
            <template v-if="el.type === 'heading'">
              <div class="el-header">
                <span class="el-type-tag">H{{ el.level }}</span>
                <div class="el-actions">
                  <button class="el-btn" @click="moveElement(idx, -1)" :disabled="idx===0">UP</button>
                  <button class="el-btn" @click="moveElement(idx, 1)" :disabled="idx===elements.length-1">DN</button>
                  <button class="el-btn el-btn--del" @click="removeElement(idx)">X</button>
                </div>
              </div>
              <input v-model="el.content" class="el-input" placeholder="Heading text..." @input="buildMd" />
            </template>
            <template v-if="el.type === 'paragraph'">
              <div class="el-header">
                <span class="el-type-tag">PARA</span>
                <div class="el-actions">
                  <button class="el-btn" @click="moveElement(idx,-1)" :disabled="idx===0">UP</button>
                  <button class="el-btn" @click="moveElement(idx,1)" :disabled="idx===elements.length-1">DN</button>
                  <button class="el-btn el-btn--del" @click="removeElement(idx)">X</button>
                </div>
              </div>
              <textarea v-model="el.content" class="el-textarea" placeholder="Paragraph text..." rows="3" @input="buildMd"></textarea>
            </template>
            <template v-if="el.type === 'codeblock'">
              <div class="el-header">
                <span class="el-type-tag">CODE</span>
                <input v-model="el.language" class="el-lang-input" placeholder="lang" />
                <div class="el-actions">
                  <button class="el-btn" @click="moveElement(idx,-1)" :disabled="idx===0">UP</button>
                  <button class="el-btn" @click="moveElement(idx,1)" :disabled="idx===elements.length-1">DN</button>
                  <button class="el-btn el-btn--del" @click="removeElement(idx)">X</button>
                </div>
              </div>
              <textarea v-model="el.content" class="el-textarea el-textarea--code" placeholder="Code..." rows="6" @input="buildMd"></textarea>
            </template>
            <template v-if="el.type === 'table'">
              <div class="el-header">
                <span class="el-type-tag">TABLE</span>
                <div class="el-actions">
                  <button class="el-btn el-btn--add" @click="addTableRow(el);buildMd()">+Row</button>
                  <button class="el-btn el-btn--add" @click="addTableCol(el);buildMd()">+Col</button>
                  <button class="el-btn el-btn--del" @click="removeTableRow(el);buildMd()" :disabled="el.rows.length<=1">-Row</button>
                  <button class="el-btn el-btn--del" @click="removeTableCol(el);buildMd()" :disabled="el.headers.length<=1">-Col</button>
                  <button class="el-btn" @click="moveElement(idx,-1)" :disabled="idx===0">UP</button>
                  <button class="el-btn" @click="moveElement(idx,1)" :disabled="idx===elements.length-1">DN</button>
                  <button class="el-btn el-btn--del" @click="removeElement(idx)">X</button>
                </div>
              </div>
              <div class="table-editor">
                <table>
                  <thead><tr><th v-for="(h,hi) in el.headers" :key="'h'+hi"><input v-model="el.headers[hi]" class="cell-input" placeholder="Col" @input="buildMd" /></th></tr></thead>
                  <tbody><tr v-for="(row,ri) in el.rows" :key="'r'+ri"><td v-for="(cell,ci) in row" :key="'c'+ci"><input v-model="el.rows[ri][ci]" class="cell-input" placeholder="Val" @input="buildMd" /></td></tr></tbody>
                </table>
              </div>
            </template>
            <template v-if="el.type === 'blockquote'">
              <div class="el-header">
                <span class="el-type-tag">QUOTE</span>
                <div class="el-actions">
                  <button class="el-btn" @click="moveElement(idx,-1)" :disabled="idx===0">UP</button>
                  <button class="el-btn" @click="moveElement(idx,1)" :disabled="idx===elements.length-1">DN</button>
                  <button class="el-btn el-btn--del" @click="removeElement(idx)">X</button>
                </div>
              </div>
              <textarea v-model="el.content" class="el-textarea" placeholder="Quote..." rows="2" @input="buildMd"></textarea>
            </template>
            <template v-if="el.type === 'unordered-list' || el.type === 'ordered-list'">
              <div class="el-header">
                <span class="el-type-tag">{{ el.type === 'ordered-list' ? 'OL' : 'UL' }}</span>
                <div class="el-actions">
                  <button class="el-btn el-btn--add" @click="el.items.push('New item');buildMd()">+Item</button>
                  <button class="el-btn" @click="moveElement(idx,-1)" :disabled="idx===0">UP</button>
                  <button class="el-btn" @click="moveElement(idx,1)" :disabled="idx===elements.length-1">DN</button>
                  <button class="el-btn el-btn--del" @click="removeElement(idx)">X</button>
                </div>
              </div>
              <div class="list-editor">
                <div v-for="(item,ii) in el.items" :key="ii" class="list-item-row">
                  <span class="list-bullet">{{ el.type === 'ordered-list' ? (ii+1)+'.' : '-' }}</span>
                  <input v-model="el.items[ii]" class="el-input" placeholder="List item..." @input="buildMd" />
                  <button v-if="el.items.length>1" class="el-btn el-btn--del el-btn--sm" @click="el.items.splice(ii,1);buildMd()">X</button>
                </div>
              </div>
            </template>
            <template v-if="el.type === 'hr'">
              <div class="el-header">
                <span class="el-type-tag">HR</span>
                <div class="el-actions">
                  <button class="el-btn" @click="moveElement(idx,-1)" :disabled="idx===0">UP</button>
                  <button class="el-btn" @click="moveElement(idx,1)" :disabled="idx===elements.length-1">DN</button>
                  <button class="el-btn el-btn--del" @click="removeElement(idx)">X</button>
                </div>
              </div>
              <div class="hr-preview">---</div>
            </template>
          </div>
        </div>
      </div>

      <div class="preview-panel">
        <div class="preview-header">
          <h3>Preview</h3>
          <div class="preview-actions">
            <button class="btn btn--primary" @click="downloadDoc" :disabled="!mdOutput">DOWN .md</button>
            <button class="btn" @click="copyMd" :disabled="!mdOutput">{{ copyLabel }}</button>
            <button class="btn" @click="showRaw=!showRaw">{{ showRaw?'EYE':'RAW' }}</button>
          </div>
        </div>
        <div class="preview-content">
          <div v-if="showRaw" class="md-raw"><pre>{{ mdOutput }}</pre></div>
          <div v-else class="md-rendered" v-html="renderedHtml"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { renderMarkdown, downloadMarkdown, copyToClipboard } from '../utils/markdown.js'
import { buildMarkdown } from '../utils/api.js'
import { useLang } from '../composables/useLang.js'

const { lang } = useLang()

let idCounter = 0
const genId = () => 'el_' + (++idCounter)
const elements = ref([])
const showRaw = ref(false)
const mdOutput = ref('')
const loading = ref(false)

let debounceTimer = null
function debounce(fn, delay = 300) {
  return (...args) => {
    clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => fn(...args), delay)
  }
}

const renderedHtml = computed(() => renderMarkdown(mdOutput.value))

function serverBuildMap(el) {
  const m = { type: el.type }
  if (el.type === 'heading') { m.level = el.level; m.content = el.content || '' }
  else if (el.type === 'paragraph') { m.content = el.content || '' }
  else if (el.type === 'codeblock') { m.content = el.content || ''; m.language = el.language || '' }
  else if (el.type === 'table') { m.headers = el.headers || []; m.rows = (el.rows || []).map(r => r.slice(0, m.headers.length)) }
  else if (el.type === 'blockquote') { m.content = el.content || '' }
  else if (el.type === 'unordered-list' || el.type === 'ordered-list') { m.items = el.items || [] }
  return m
}

const debouncedBuildMd = debounce(buildMdImmediate, 300)

async function buildMdImmediate() {
  if (elements.value.length === 0) { mdOutput.value = ''; return }
  loading.value = true
  try {
    const serverElements = elements.value.map(serverBuildMap)
    const result = await buildMarkdown(serverElements)
    mdOutput.value = result.markdown || ''
  } catch (e) {
    mdOutput.value = buildMdFrontend()
  } finally {
    loading.value = false
  }
}

function buildMd() {
  debouncedBuildMd()
}

function buildMdFrontend() {
  let md = ''
  for (const el of elements.value) {
    switch (el.type) {
      case 'heading': md += '#'.repeat(el.level||1) + ' ' + (el.content||'') + '\n\n'; break
      case 'paragraph': md += (el.content||'') + '\n\n'; break
      case 'codeblock': md += '```' + (el.language||'') + '\n' + (el.content||'') + '\n```\n\n'; break
      case 'table':
        if (el.headers && el.headers.length) {
          md += '| ' + el.headers.join(' | ') + ' |\n'
          md += '| ' + el.headers.map(()=>'---').join(' | ') + ' |\n'
          for (const row of (el.rows||[])) { const c=[]; for(let i=0;i<el.headers.length;i++)c.push(row[i]||''); md += '| '+c.join(' | ')+' |\n' }
          md += '\n'
        }
        break
      case 'blockquote': for(const l of (el.content||'').split('\n')) md += '> '+l+'\n'; md += '\n'; break
      case 'unordered-list': for(const i of (el.items||[])) md += '- '+i+'\n'; md += '\n'; break
      case 'ordered-list': (el.items||[]).forEach((i,n)=>md+=(n+1)+'. '+i+'\n'); md += '\n'; break
      case 'hr': md += '---\n\n'; break
    }
  }
  return md.trim()
}

function addHeading(l){ elements.value.push({id:genId(),type:'heading',level:l,content:'Heading'}); buildMd() }
function addParagraph(){ elements.value.push({id:genId(),type:'paragraph',content:''}); buildMd() }
function addCodeBlock(){ elements.value.push({id:genId(),type:'codeblock',content:'',language:''}); buildMd() }
function addBlockQuote(){ elements.value.push({id:genId(),type:'blockquote',content:'Quote'}); buildMd() }
function addTable(){ elements.value.push({id:genId(),type:'table',headers:['Col1','Col2','Col3'],rows:[['','',''],['','','']]}); buildMd() }
function addUnorderedList(){ elements.value.push({id:genId(),type:'unordered-list',items:['Item1','Item2']}); buildMd() }
function addOrderedList(){ elements.value.push({id:genId(),type:'ordered-list',items:['Step1','Step2']}); buildMd() }
function addHorizontalRule(){ elements.value.push({id:genId(),type:'hr'}); buildMd() }
function addTableRow(el){ el.rows.push(el.headers.map(()=>'')) }
function addTableCol(el){ el.headers.push('New'); el.rows.forEach(r=>r.push('')) }
function removeTableRow(el){ if(el.rows.length>1) el.rows.pop() }
function removeTableCol(el){ if(el.headers.length>1){ el.headers.pop(); el.rows.forEach(r=>r.pop()) } }
function removeElement(idx){ elements.value.splice(idx,1); buildMd() }
function moveElement(idx,dir){ const ni=idx+dir; if(ni<0||ni>=elements.value.length)return; const t=elements.value[idx];elements.value[idx]=elements.value[ni];elements.value[ni]=t;elements.value=[...elements.value];buildMd() }
function clearAll(){ if(confirm('Clear all elements?')){ elements.value=[]; mdOutput.value='' } }
function downloadDoc(){ const ts=new Date().toISOString().slice(0,16).replace('T','_').replace(':',''); downloadMarkdown(mdOutput.value, 'document_'+ts+'.md') }

const copyLabel = ref('COPY')
async function copyMd() {
  const ok = await copyToClipboard(mdOutput.value)
  if (ok) { copyLabel.value = 'OK!'; setTimeout(() => copyLabel.value = 'COPY', 1500) }
}

// trigger initial build immediately (empty)
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; margin-bottom: 6px; }
.page-header p { color: var(--text-secondary); font-size: 14px; }
.builder-layout { display: flex; gap: 24px; height: calc(100vh - 160px); }
.editor-panel { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.toolbar { display: flex; align-items: center; gap: 4px; padding: 8px 12px; background: var(--bg-secondary); border: 1px solid var(--border); border-radius: var(--radius); margin-bottom: 12px; flex-wrap: wrap; }
.tb-btn { padding: 6px 10px; background: var(--bg-tertiary); border: 1px solid var(--border); color: var(--text); border-radius: 4px; cursor: pointer; font-size: 12px; transition: all .15s; font-family: 'SF Mono',Consolas,monospace; }
.tb-btn:hover { background: rgba(88,166,255,.15); border-color: var(--accent); color: var(--accent); }
.tb-btn--danger:hover { background: rgba(248,81,73,.15); border-color: var(--red); color: var(--red); }
.tb-sep { width: 1px; height: 20px; background: var(--border); margin: 0 4px; }
.elements-area { flex: 1; overflow-y: auto; padding-right: 4px; }
.elements-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 20px; border: 2px dashed var(--border); border-radius: var(--radius); color: var(--text-secondary); }
.empty-icon { font-size: 40px; margin-bottom: 12px; }
.element-card { background: var(--bg-secondary); border: 1px solid var(--border); border-radius: var(--radius); margin-bottom: 10px; overflow: hidden; }
.el-header { display: flex; align-items: center; gap: 10px; padding: 8px 12px; background: var(--bg-tertiary); border-bottom: 1px solid var(--border); }
.el-type-tag { font-size: 11px; font-weight: 600; padding: 2px 8px; background: rgba(88,166,255,.15); color: var(--accent); border-radius: 3px; }
.el-lang-input { width: 80px; padding: 4px 8px; background: var(--bg); border: 1px solid var(--border); color: var(--text); border-radius: 3px; font-size: 12px; }
.el-actions { margin-left: auto; display: flex; gap: 4px; }
.el-btn { padding: 3px 8px; background: transparent; border: 1px solid var(--border); color: var(--text-secondary); border-radius: 3px; cursor: pointer; font-size: 11px; transition: all .15s; }
.el-btn:hover { color: var(--text); border-color: var(--text-secondary); }
.el-btn:disabled { opacity: .3; cursor: default; }
.el-btn--del:hover { color: var(--red); border-color: var(--red); }
.el-btn--add { color: var(--green); border-color: var(--green); }
.el-btn--add:hover { background: rgba(63,185,80,.1); }
.el-btn--sm { padding: 2px 6px; font-size: 10px; }
.el-input { width: 100%; padding: 10px 12px; background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text); font-size: 14px; outline: none; }
.el-textarea { width: 100%; padding: 10px 12px; background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text); font-size: 13px; outline: none; resize: vertical; font-family: 'SF Mono',Consolas,monospace; }
.el-textarea--code { font-family: 'SF Mono',Consolas,monospace; line-height: 1.5; background: var(--bg); }
.table-editor { padding: 8px 12px 12px; overflow-x: auto; }
.table-editor table { border-collapse: collapse; width: max-content; }
.table-editor th,.table-editor td { padding: 2px; border: 1px solid var(--border); }
.cell-input { width: 100px; padding: 6px 8px; background: var(--bg-secondary); border: 1px solid var(--border); color: var(--text); font-size: 13px; outline: none; }
th .cell-input { font-weight: 600; color: var(--accent); }
.list-editor { padding: 8px 12px 12px; }
.list-item-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.list-bullet { color: var(--accent); font-weight: 600; width: 22px; font-family: 'SF Mono',Consolas,monospace; }
.list-item-row .el-input { padding: 6px 10px; font-size: 13px; border: 1px solid var(--border); border-radius: 4px; }
.hr-preview { padding: 12px; text-align: center; color: var(--text-secondary); font-size: 18px; }
.preview-panel { width: 480px; min-width: 380px; display: flex; flex-direction: column; background: var(--bg-secondary); border: 1px solid var(--border); border-radius: var(--radius); overflow: hidden; }
.preview-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: var(--bg-tertiary); border-bottom: 1px solid var(--border); }
.preview-header h3 { font-size: 14px; }
.preview-actions { display: flex; gap: 8px; }
.btn { padding: 6px 14px; background: var(--bg-tertiary); border: 1px solid var(--border); color: var(--text); border-radius: 5px; cursor: pointer; font-size: 12px; transition: all .15s; }
.btn:hover { border-color: var(--text-secondary); }
.btn:disabled { opacity: .4; cursor: default; }
.btn--primary { background: #238636; border-color: #2ea043; color: #fff; }
.btn--primary:hover { background: #2ea043; }
.preview-content { flex: 1; overflow-y: auto; padding: 16px 24px; }
.md-rendered { line-height: 1.7; font-size: 15px; }
.md-rendered :deep(h1) { font-size: 26px; border-bottom: 1px solid var(--border); padding-bottom: 8px; margin: 20px 0 12px; }
.md-rendered :deep(h2) { font-size: 21px; border-bottom: 1px solid var(--border); padding-bottom: 6px; margin: 18px 0 10px; }
.md-rendered :deep(h3) { font-size: 17px; margin: 16px 0 8px; }
.md-rendered :deep(p) { margin: 8px 0; }
.md-rendered :deep(code) { background: var(--bg-tertiary); padding: 2px 6px; border-radius: 3px; font-size: 13px; font-family: 'SF Mono',Consolas,monospace; }
.md-rendered :deep(pre) { background: var(--bg); padding: 14px; border-radius: 6px; overflow-x: auto; margin: 10px 0; }
.md-rendered :deep(pre code) { background: none; padding: 0; }
.md-rendered :deep(table) { border-collapse: collapse; width: 100%; margin: 10px 0; }
.md-rendered :deep(th),.md-rendered :deep(td) { border: 1px solid var(--border); padding: 8px 12px; text-align: left; }
.md-rendered :deep(th) { background: var(--bg-tertiary); font-weight: 600; }
.md-rendered :deep(blockquote) { border-left: 3px solid var(--accent); padding: 4px 0 4px 14px; margin: 10px 0; color: var(--text-secondary); }
.md-rendered :deep(ul),.md-rendered :deep(ol) { padding-left: 24px; margin: 8px 0; }
.md-rendered :deep(li) { margin: 4px 0; }
.md-rendered :deep(hr) { border: none; border-top: 1px solid var(--border); margin: 20px 0; }
.md-raw pre { font-family: 'SF Mono',Consolas,monospace; font-size: 13px; line-height: 1.6; white-space: pre-wrap; word-wrap: break-word; }
</style>