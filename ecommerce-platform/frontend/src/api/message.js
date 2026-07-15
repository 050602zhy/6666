import request from './request'

/** 获取消息列表 */
export function getMessageList(userId, type = 'all') {
  return request.get('/biz/message/list', { params: { userId, type } })
}

/** 标记消息已读 */
export function readMessage(id) {
  return request.put(`/biz/message/read/${id}`)
}
