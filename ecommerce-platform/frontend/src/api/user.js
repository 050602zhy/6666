import request from './request'

/** 用户登录 */
export function login(data) {
  return request.post('/biz/user/login', data)
}

/** 用户注册 */
export function register(data) {
  return request.post('/biz/user/register', data)
}