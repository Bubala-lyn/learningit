// eslint-disable-next-line
import { UserLayout, BasicLayout } from '@/layouts'

const RouteView = {
  name: 'RouteView',
  render: h => h('router-view')
}

export const asyncRouterMap = [
  {
    path: '/',
    name: 'index',
    component: BasicLayout,
    meta: { title: '首页' },
    redirect: '/dataset/question',
    children: [
      {
        path: '/dataset',
        name: 'dataset',
        redirect: '/dataset/question',
        component: RouteView,
        meta: { title: '数据采集', keepAlive: false, icon: 'database' },
        children: [
          {
            path: '/dataset/question',
            name: 'Question',
            component: () => import('@/views/dataset/Question'),
            meta: { title: '题目管理', keepAlive: false }
          },
          {
            path: '/dataset/knowledge',
            name: 'Knowledge',
            component: () => import('@/views/dataset/Knowledge'),
            meta: { title: '知识点管理', keepAlive: false }
          },
          {
            path: '/dataset/tag',
            name: 'Tag',
            component: () => import('@/views/dataset/Tag'),
            meta: { title: '打标签', keepAlive: false }
          },
          {
            path: '/dataset/check',
            name: 'Check',
            component: () => import('@/views/dataset/Check'),
            meta: { title: '数据审核', keepAlive: false }
          }
        ]
      },
      {
        path: '/dataclean',
        name: 'DataClean',
        component: () => import('@/views/dataset/DataClean'),
        meta: { title: '数据清洗', icon: 'scissor', keepAlive: false }
      },
      {
        path: '/PreTrainVec',
        name: 'PreTrainVec',
        component: () => import('@/views/dataset/PreTrainVec'),
        meta: { title: '预训练向量', icon: 'highlight', keepAlive: false }
      },
      {
        path: '/summary',
        name: 'Summary',
        component: () => import('@/views/dataset/Summary'),
        meta: { title: '数据集准备', icon: 'pie-chart', keepAlive: false }
      },
      {
        path: '/atmkmodel',
        name: 'ATMKModel',
        component: () => import('@/views/autotag/ATMKModel'),
        meta: { title: '模型训练', icon: 'deployment-unit', keepAlive: false }
      },
      {
        path: '/analysis',
        name: 'Analysis',
        component: () => import('@/views/autotag/Analysis'),
        meta: { title: '结果分析', icon: 'monitor', keepAlive: false }
      }
    ]
  },
  {
    path: '*',
    redirect: '/404',
    hidden: true
  }
]

/**
 * 基础路由
 * @type { *[] }
 */
export const constantRouterMap = [
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/login',
    hidden: true,
    children: [
      {
        path: 'login',
        name: 'login',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/Login')
      },
      {
        path: 'register',
        name: 'register',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/Register')
      },
      {
        path: 'register-result',
        name: 'registerResult',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/RegisterResult')
      },
      {
        path: 'recover',
        name: 'recover',
        component: undefined
      }
    ]
  },

  {
    path: '/404',
    component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/404')
  }
]
