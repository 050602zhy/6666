import request from './request'

/** 创建活动 */
export function createActivity(data) {
  return request.post('/biz/activity', data)
}

/** 获取活动列表 */
export function getActivityList() {
  return request.get('/biz/activity/list')
}

/** 发布活动 */
export function publishActivity(id) {
  return request.put(`/biz/activity/publish/${id}`)
}

/** 撤销发布活动 */
export function unpublishActivity(id) {
  return request.put(`/biz/activity/unpublish/${id}`)
}

/** 删除活动 */
export function deleteActivity(id) {
  return request.delete(`/biz/activity/${id}`)
}

/** 获取活动详情 */
export function getActivityDetail(id) {
  return request.get(`/biz/activity/${id}`)
}
