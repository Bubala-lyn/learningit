import axios from 'axios'
import notification from 'ant-design-vue/es/notification'
import { VueAxios } from './axios'
import md5 from 'md5'

const { CancelToken } = axios

// 创建 axios 实例
const request = axios.create({
  // API 请求的默认前缀
  baseURL: process.env.VUE_APP_API_BASE_URL,
  timeout: 6000 // 请求超时时间
})

// 用于从cookie中匹配 csrftoken值
const regex = /.*csrftoken=([^;.]*).*$/

// 请求集合，防止重复点击
// 如何排除掉实际需要的重复请求
const requestUrls = []
let requestFlag = ''
const removeRequestUrl = () => {
  // 移除队列中的该请求
  requestUrls.splice(requestUrls.indexOf(requestFlag), 1)
}

// 异常拦截处理器
const errorHandler = error => {
  if (error.response) {
    const data = error.response.data
    // 从 localstorage 获取 token
    if (error.response.status === 403) {
      notification.error({
        message: 'Forbidden',
        description: data.message
      })
    }
    if (error.response.status === 401 && !(data.result && data.result.isLogin)) {
      notification.error({
        message: 'Unauthorized',
        description: 'Authorization verification failed'
      })
    }
  }
  removeRequestUrl()
  return Promise.reject(error)
}

// request interceptor
request.interceptors.request.use(config => {
  // 重复点击start=======
  const bodyHash = md5(JSON.stringify(config.data) || '').slice(0, 10)
  requestFlag = config.url + config.method + bodyHash
  if (requestUrls.indexOf(requestFlag) > -1) {
    config.cancelToken = new CancelToken(cancel => {
      cancel('duplicate request')
    })
  } else {
    requestUrls.push(requestFlag)
    config.headers['X-CSRFToken'] = document.cookie.match(regex) ? document.cookie.match(regex)[1] : null
  }
  // 重复点击end=======
  return config
}, errorHandler)

// response interceptor
request.interceptors.response.use(response => {
  removeRequestUrl()
  if (response.data.status === 0) {
    return Promise.reject(response)
  } else {
    return response.data
  }
}, errorHandler)

const installer = {
  vm: {},
  install(Vue) {
    Vue.use(VueAxios, request)
  }
}

export default request

export { installer as VueAxios, request as axios }
