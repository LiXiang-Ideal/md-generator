const API_BASE = import.meta.env.VITE_API_BASE || '/api'

/**
 * 通用请求封装
 * @param {string} url - 接口路径
 * @param {object} options - fetch参数
 */
async function request(url, options = {}) {
  const config = {
    headers: { 'Content-Type': 'application/json' },
    ...options
  }

  try {
    const response = await fetch(API_BASE + url, config)
    const data = await response.json()

    if (!response.ok || data.code !== 200) {
      throw new Error(data.message || '请求失败')
    }

    return data.data
  } catch (error) {
    throw error
  }
}

/**
 * POST请求
 */
function post(url, body) {
  return request(url, {
    method: 'POST',
    body: JSON.stringify(body)
  })
}

// ========== 数据库文档 ==========

/** 连接数据库并获取表列表 */
export function connectDatabase(params) {
  return post('/database/connect', params)
}

/** 根据勾选表生成文档 */
export function generateDbDoc(params) {
  return post('/database/generate', params)
}

/** 断开数据库连接 */
export function disconnectDatabase(params) {
  return post('/database/disconnect', params)
}

// ========== API文档 ==========

/** 扫描源码目录生成API文档 */
export function generateApiDoc(params) {
  return post('/apidoc/generate', params)
}

// ========== Markdown构建 ==========

/** 提交元素列表构建Markdown */
export function buildMarkdown(elements) {
  return post('/builder/build', elements)
}

// ========== 格式转换 ==========

/** JSON转表格/代码块 */
export function convertJson(data, format = 'table') {
  return post('/convert/json', { data, format })
}

/** CSV转表格 */
export function convertCsv(data, delimiter = ',') {
  return post('/convert/csv', { data, delimiter })
}

/** 手动创建表格 */
export function createTable(headers, rows) {
  return post('/convert/table', { headers, rows })
}

export default { API_BASE }