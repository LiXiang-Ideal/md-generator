<template>
  <div class="layout">
    <header class="header">
      <div class="header-left">
        <span class="logo">📋</span>
        <h1 class="title">MD Generator</h1>
        <span class="subtitle">{{ lang === 'zh' ? 'Markdown文档生成工具' : 'Markdown Doc Generator' }}</span>
      </div>
      <div class="header-right">
        <div class="header-actions">
          <select v-model="lang" @change="setLang(lang)" class="header-select">
            <option value="zh">中文</option>
            <option value="en">EN</option>
          </select>
          <select v-model="theme" @change="setTheme(theme)" class="header-select">
            <option value="system">跟随系统</option>
            <option value="dark">暗色</option>
            <option value="light">亮色</option>
          </select>
          <a href="https://github.com/md-generator/md-generator" target="_blank" rel="noopener noreferrer" class="gh-link">GitHub</a>
        </div>
      </div>
    </header>
    <div class="body-wrap">
      <aside class="sidebar">
        <nav class="nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            active-class="nav-item--active"
          >
            <span class="nav-icon">{{ item.metaIcon }}</span>
            <span class="nav-label">{{ item.metaTitle }}</span>
          </router-link>
        </nav>
        <div class="sidebar-footer">
          <span class="version">v1.0.0</span>
        </div>
      </aside>
      <main class="main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useLang, t, useTheme } from './composables/useLang.js'

const router = useRouter()
const { lang, setLang } = useLang()
const { theme, setTheme } = useTheme()

const navMetaMap = {
  '/builder': { icon: '📝', key: 'builder' },
  '/database': { icon: '🗄️', key: 'database' },
  '/api': { icon: '🔌', key: 'api' },
  '/convert': { icon: '🔄', key: 'convert' },
}

const navItems = computed(() =>
  router.options.routes
    .filter(r => r.meta)
    .map(r => ({
      ...r,
      metaTitle: t[lang.value][navMetaMap[r.path]?.key] || r.meta?.title || r.name,
      metaIcon: navMetaMap[r.path]?.icon || r.meta?.icon,
    }))
)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  /* 默认暗色主题值（会被 JavaScript 动态覆盖） */
  --bg: #0d1117;
  --bg-secondary: #161b22;
  --bg-tertiary: #21262d;
  --border: #30363d;
  --text: #c9d1d9;
  --text-secondary: #8b949e;
  --accent: #58a6ff;
  --accent-hover: #79c0ff;
  --green: #3fb950;
  --red: #f85149;
  --orange: #d2991d;
  --radius: 8px;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
  background: var(--bg);
  color: var(--text);
  min-height: 100vh;
}

.layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 52px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  font-size: 24px;
}

.title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  padding-left: 12px;
  border-left: 1px solid var(--border);
}

.gh-link {

  color: var(--text-secondary);
  text-decoration: none;
  font-size: 13px;
  padding: 6px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  transition: all 0.15s;
}

.gh-link:hover {
  color: var(--accent);
  border-color: var(--accent);
}

.header-select {
  padding: 4px 10px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border);
  color: var(--text-secondary);
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
  outline: none;
}

.header-select:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.header-select:focus {
  border-color: var(--accent);
}

.header-select option {
  background: var(--bg-tertiary);
  color: var(--text);
}

/* 全局表单元素样式 */
input:not([type='checkbox']):not([type='radio']),
textarea,
select {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  color: var(--text);
}

input:not([type='checkbox']):not([type='radio']):focus,
textarea:focus,
select:focus {
  border-color: var(--accent);
  outline: none;
}

/* 密码框特殊处理 */
input[type='password'] {
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  color: var(--text);
  caret-color: var(--text);
}

/* 密码框在 Webkit 浏览器中的密码显示/隐藏图标 */
input[type='password']::-webkit-credentials-auto-fill-button,
input[type='password']::-webkit-caps-lock-indicator,
input[type='password']::-webkit-reveal-password-button {
  filter: invert(0);
}

/* 复选框和单选框 */
input[type='checkbox'],
input[type='radio'] {
  accent-color: var(--accent);
  cursor: pointer;
  background-color: var(--bg-tertiary);
  border: 1px solid var(--border);
}

/* 选项卡样式 */
option {
  background: var(--bg-tertiary);
  color: var(--text);
}

/* 占位符颜色 */
::placeholder {
  color: var(--text-secondary);
  opacity: 0.7;
}

/* 自动填充背景色 */
input:-webkit-autofill,
input:-webkit-autofill:hover,
input:-webkit-autofill:focus,
textarea:-webkit-autofill,
textarea:-webkit-autofill:hover,
textarea:-webkit-autofill:focus {
  -webkit-text-fill-color: var(--text);
  -webkit-box-shadow: 0 0 0px 1000px var(--bg-secondary) inset;
  transition: background-color 5000s ease-in-out 0s;
}

/* 浏览器默认样式的重置 */
button {
  font-family: inherit;
}

.body-wrap {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 200px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex-shrink: 0;
  overflow-y: auto;
}

.nav {
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 6px;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.15s;
}

.nav-item:hover {
  background: var(--bg-tertiary);
  color: var(--text);
}

.nav-item--active {
  background: rgba(88, 166, 255, 0.12);
  color: var(--accent);
}

.nav-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border);
}

.version {
  font-size: 12px;
  color: var(--text-secondary);
}

.main {
  flex: 1;
  overflow-y: auto;
  padding: 28px 32px;
}
</style>