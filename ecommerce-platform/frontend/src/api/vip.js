import request from './request'

/** 获取VIP配置 */
export function getVipConfig() {
  return request.get('/biz/vip/config')
}
