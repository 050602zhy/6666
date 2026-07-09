import request from './request'

export function createOrder(data) {
  return request.post('/biz/order/create', data)
}

export function getOrderById(id) {
  return request.get(`/biz/order/${id}`)
}
