import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/builder'
  },
  {
    path: '/builder',
    name: 'Builder',
    component: () => import('../views/BuilderView.vue'),
    meta: { title: '可视化构建', icon: '📝' }
  },
  {
    path: '/database',
    name: 'Database',
    component: () => import('../views/DatabaseView.vue'),
    meta: { title: '数据库文档', icon: '🗄️' }
  },
  {
    path: '/api',
    name: 'ApiDoc',
    component: () => import('../views/ApiDocView.vue'),
    meta: { title: 'API文档', icon: '🔌' }
  },
  {
    path: '/convert',
    name: 'Convert',
    component: () => import('../views/ConvertView.vue'),
    meta: { title: '格式转换', icon: '🔄' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router