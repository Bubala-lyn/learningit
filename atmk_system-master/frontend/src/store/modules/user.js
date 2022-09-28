import storage from 'store'
import { login, getInfo, logout } from '@/api/login'
import { ACCESS_TOKEN } from '@/store/mutation-types'

const user = {
  state: {
    token: '',
    name: '',
    logined: false // 是否登录
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_LOGIN_STATUS: (state, status) => {
      state.logined = status
    }
  },

  actions: {
    // 登录
    Login({ commit }, userInfo) {
      return new Promise((resolve, reject) => {
        login(userInfo)
          .then(response => {
            const token = Date.now()
            storage.set(ACCESS_TOKEN, token, 7 * 24 * 60 * 60 * 1000)
            commit('SET_TOKEN', token)
            resolve()
          })
          .catch(error => {
            reject(error)
          })
      })
    },

    // 获取用户信息
    GetInfo({ commit }) {
      return new Promise((resolve, reject) => {
        getInfo()
          .then(response => {
            const result = response.data
            commit('SET_LOGIN_STATUS', true)
            commit('SET_NAME', result.username)
            resolve(response)
          })
          .catch(error => {
            reject(error)
          })
      })
    },

    // 登出
    Logout({ commit, state }) {
      return new Promise(resolve => {
        logout(state.token)
          .then(() => {
            commit('SET_TOKEN', '')
            commit('SET_ROLES', [])
            commit('SET_LOGIN_STATUS', false)
            storage.remove(ACCESS_TOKEN)
            resolve()
          })
          .catch(err => {
            console.log('logout fail:', err)
          })
          .finally(() => {})
      })
    }
  }
}

export default user
