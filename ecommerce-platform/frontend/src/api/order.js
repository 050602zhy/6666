import request from './request'

export function createOrder(data) {
  return request.post('/biz/order/create', data)
}

export function getOrderById(id) {
  return request.get(`/biz/order/${id}`)
}

/** 获取订单列表 */
export function getOrderList(params) {
  return request.get('/biz/order/list', { params })
}

/** 卖家同意出货 */
export function shipOrder(orderId) {
  return request.put('/biz/order/ship', { orderId })
}

/** 完成订单 */
export function completeOrder(orderId) {
  return request.put('/biz/order/complete', { orderId })
}

/** 申请退货 */
export function applyRefund(data) {
  return request.post('/biz/order/refund', data)
}

/** 处理退货 */
export function handleRefund(data) {
  return request.put('/biz/order/refund/handle', data)
}

/** 订单评分 */
export function rateOrder(data) {
  return request.put('/biz/order/rate', data)
}
