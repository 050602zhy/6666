import request from './request'

export function getProductList() {
  return request.get('/biz/product/list')
}

export function getProductById(id) {
  return request.get(`/biz/product/${id}`)
}
