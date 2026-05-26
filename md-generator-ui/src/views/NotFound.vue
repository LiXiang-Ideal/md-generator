<template>
  <div class="not-found">
    <div class="nf-icon">404</div>
    <h2>{{ lang === 'zh' ? '页面未找到' : 'Page Not Found' }}</h2>
    <p>{{ lang === 'zh' ? '您访问的页面不存在' : 'The page you visited does not exist' }}</p>
    <router-link to="/" class="btn btn--primary">{{ lang === 'zh' ? '返回首页' : 'Go Home' }}</router-link>
  </div>
</template>

<script setup>
import { useLang } from '../composables/useLang.js'
const { lang } = useLang()
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: var(--text-secondary);
}
.nf-icon {
  font-size: 72px;
  font-weight: 700;
  color: var(--accent);
  margin-bottom: 16px;
}
h2 {
  font-size: 22px;
  color: var(--text);
  margin-bottom: 8px;
}
p {
  margin-bottom: 24px;
}
.btn {
  padding: 10px 24px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border);
  color: var(--text);
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  text-decoration: none;
  transition: all .15s;
}
.btn--primary {
  background: #238636;
  border-color: #2ea043;
  color: #fff;
}
.btn--primary:hover {
  background: #2ea043;
}
</style>
