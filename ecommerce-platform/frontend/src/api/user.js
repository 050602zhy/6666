import request from './request'

/** 用户登录 */
export function login(data) {
  return request.post('/biz/user/login', data)
}

/** 用户注册 */
export function register(data) {
  return request.post('/biz/user/register', data)
}

/** 获取用户信息 */
export function getUserInfo(id) {
  return request.get(`/biz/user/info/${id}`)
}

/** 更新用户信息 */
export function updateUser(data) {
  return request.put('/biz/user', data)
}

/** 更新余额 */
export function updateBalance(data) {
  return request.put('/biz/user/balance', data)
}

/** 更新VIP等级 */
export function updateVip(data) {
  return request.put('/biz/user/vip', data)
}

/** 更新自动接单设置 */
export function updateAutoAccept(data) {
  return request.put('/biz/user/autoAccept', data)
}
