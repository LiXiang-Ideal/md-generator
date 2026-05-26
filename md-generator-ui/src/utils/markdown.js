import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

marked.use({
  breaks: true,
  gfm: true
})

marked.use({
  renderer: {
    code({ text, lang }) {
      const language = lang && hljs.getLanguage(lang) ? lang : 'plaintext'
      let highlighted
      try {
        highlighted = hljs.highlight(text || '', { language }).value
      } catch {
        highlighted = text || ''
      }
      return `<pre><code class="hljs language-${language}">${highlighted}</code></pre>`
    }
  }
})

export function renderMarkdown(text) {
  return marked(text || '')
}

export function buildMarkdown(content) {
  return (content || '').trim()
}

export function downloadMarkdown(content, filename = 'document.md') {
  const blob = new Blob(['\uFEFF' + content], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

export async function copyToClipboard(text) {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    const ok = document.execCommand('copy')
    document.body.removeChild(textarea)
    return ok
  }
}