import request from './request'

/** 获取商品列表（按ID升序） */
export function getProductList() {
  return request.get('/biz/product/list')
}

/** 根据ID获取商品详情 */
export function getProductById(id) {
  return request.get(`/biz/product/${id}`)
}

/** 更新商品信息（描述、价格） */
export function updateProduct(data) {
  return request.put('/biz/product', data)
}

/** 更新商品图片 */
export function updateProductImage(id, imageUrl) {
  return request.put('/biz/product/image', null, { params: { id, imageUrl } })
}

/** 更新商品上下架状态 */
export function updateProductOnSale(id, onSale) {
  return request.put('/biz/product/onSale', null, { params: { id, onSale } })
}