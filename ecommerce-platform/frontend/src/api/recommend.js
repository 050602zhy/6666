import request from './request'

export function getRecommend(userId) {
  return request.get('/ai/recommend', { params: { userId } })
}
