import request from '@/utils/request'

const userApi = {
  Login: '/login',
  Logout: '/logout',
  // get my info
  UserInfo: '/user/info'
}

/**
 * login func
 * parameter: {
 *     username: '',
 *     password: '',
 * }
 * @param parameter
 * @returns {*}
 */
export function login(parameter) {
  return request({
    url: userApi.Login,
    method: 'post',
    data: parameter
  })
}

export function getInfo() {
  return request({
    url: userApi.UserInfo,
    method: 'get'
  })
}

export function logout() {
  return request({
    url: userApi.Logout,
    method: 'post'
  })
}
