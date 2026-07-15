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

/** 更新商品图片（POST body，支持base64长字符串） */
export function updateProductImage(id, imageUrl) {
  return request.post('/biz/product/image', { id, imageUrl })
}

/** 更新商品上下架状态 */
export function updateProductOnSale(id, onSale) {
  return request.put('/biz/product/onSale', null, { params: { id, onSale } })
}

/** 获取当前卖家的商品列表 */
export function getMyProductList() {
  return request.get('/biz/product/myList')
}

/** 发布新商品 */
export function saveProduct(data) {
  return request.post('/biz/product/save', data)
}

/** 获取上架商品列表（买家视角） */
export function getOnSaleList() {
  return request.get('/biz/product/onSaleList')
}

/** 获取商品详情（买家视角，含活动折扣信息） */
export function getProductDetail(id) {
  return request.get(`/biz/product/detail/${id}`)
}

/** 购买商品 */
export function buyProduct(data) {
  return request.post('/biz/product/buy', data)
}

/** 添加商品评论 */
export function addComment(data) {
  return request.post('/biz/product/comment', data)
}
