import { ref, watch, onMounted } from 'vue'

const lang = ref(localStorage.getItem('mdgen_lang') || 'zh')

export function useLang() {
  function setLang(l) {
    lang.value = l
    localStorage.setItem('mdgen_lang', l)
  }

  return { lang, setLang }
}

// 主题管理
const themeStorage = localStorage.getItem('mdgen_theme') || 'system'
const theme = ref(themeStorage)
const isDark = ref(false)

export function useTheme() {
  function setTheme(t) {
    theme.value = t
    localStorage.setItem('mdgen_theme', t)
    applyTheme()
  }

  function applyTheme() {
    const t = theme.value
    if (t === 'system') {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      isDark.value = prefersDark
    } else {
      isDark.value = t === 'dark'
    }

    // 设置 CSS 变量
    document.documentElement.style.setProperty('--bg', isDark.value ? '#0d1117' : '#ffffff')
    document.documentElement.style.setProperty('--bg-secondary', isDark.value ? '#161b22' : '#f6f8fa')
    document.documentElement.style.setProperty('--bg-tertiary', isDark.value ? '#21262d' : '#eaeef2')
    document.documentElement.style.setProperty('--border', isDark.value ? '#30363d' : '#d0d7de')
    document.documentElement.style.setProperty('--text', isDark.value ? '#c9d1d9' : '#24292f')
    document.documentElement.style.setProperty('--text-secondary', isDark.value ? '#8b949e' : '#57606a')
    document.documentElement.style.setProperty('--accent', '#58a6ff')
    document.documentElement.style.setProperty('--accent-hover', '#79c0ff')
    document.documentElement.style.setProperty('--green', '#3fb950')
    document.documentElement.style.setProperty('--red', '#f85149')
    document.documentElement.style.setProperty('--orange', '#d2991d')
    document.documentElement.style.setProperty('--radius', '8px')
  }

  function cycleTheme() {
    const themes = ['system', 'dark', 'light']
    const current = theme.value
    const idx = themes.indexOf(current)
    setTheme(themes[(idx + 1) % themes.length])
  }

  onMounted(() => {
    applyTheme()

    // 监听系统主题变化
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    const handler = () => {
      if (theme.value === 'system') {
        applyTheme()
      }
    }
    mediaQuery.addEventListener('change', handler)

    return () => {
      mediaQuery.removeEventListener('change', handler)
    }
  })

  return { theme, setTheme, isDark, cycleTheme }
}

export const t = {
  zh: {
    builder: '可视化构建',
    database: '数据库文档',
    api: 'API文档',
    convert: '格式转换',
    dbTitle: '数据库文档',
    dbSubtitle: '连接数据库，选择表，生成文档',
    connection: '连接信息',
    type: '类型',
    host: '主机',
    port: '端口',
    databaseName: '数据库名',
    username: '用户名',
    password: '密码',
    connectBtn: '连接数据库',
    connecting: '连接中...',
    loadDemo: '加载演示',
    disconnect: '断开',
    selectTables: '选择要生成的表',
    selectAll: '全选',
    deselectAll: '取消全选',
    generate: '生成文档',
    enterDbNamePwd: '请输入数据库名和密码',
    selectOneTable: '请至少选择一张表',
    clickConnect: '点击连接或加载演示',
    apiTitle: 'API文档',
    apiSubtitle: '扫描Controller源码，生成API Markdown文档',
    convertTitle: '格式转换',
    convertSubtitle: 'JSON/CSV转Markdown表格',
  },
  en: {
    builder: 'Visual Builder',
    database: 'Database Doc',
    api: 'API Doc',
    convert: 'Convert',
    dbTitle: 'Database Document',
    dbSubtitle: 'Connect, select tables, generate doc',
    connection: 'Connection',
    type: 'Type',
    host: 'Host',
    port: 'Port',
    databaseName: 'Database',
    username: 'Username',
    password: 'Password',
    connectBtn: 'Connect',
    connecting: 'Connecting...',
    loadDemo: 'Load Demo',
    disconnect: 'Disconnect',
    selectTables: 'Select tables',
    selectAll: 'Select All',
    deselectAll: 'Deselect All',
    generate: 'Generate',
    enterDbNamePwd: 'Enter DB name & password',
    selectOneTable: 'Select at least one table',
    clickConnect: 'Connect or load demo',
    apiTitle: 'API Document',
    apiSubtitle: 'Scan Controller source to generate API Markdown doc',
    convertTitle: 'Format Converter',
    convertSubtitle: 'Convert JSON, CSV to Markdown tables',
  }
}