import axios from 'axios'

const aiRequest = axios.create({
  baseURL: '/ai',
  timeout: 60000 // Tool Calling 可能需要更长时间
})

// 请求拦截器：自动附加用户ID头
aiRequest.interceptors.request.use(config => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    if (user.id) {
      config.headers['X-User-Id'] = user.id
    }
  }
  return config
}, error => Promise.reject(error))

// 响应拦截器：统一错误处理
aiRequest.interceptors.response.use(
  response => response.data,
  error => {
    console.error('AI API 错误', error)
    return Promise.reject(error)
  }
)

/** 智能客服：询问商品相关问题（直接传入商品实时数据，避免跨服务依赖） */
export function aiChat(productName, productDesc, question, productId, productContext) {
  const userStr = localStorage.getItem('user')
  let userId = undefined
  let userBalance = undefined
  let userVipLevel = undefined
  if (userStr) {
    const user = JSON.parse(userStr)
    userId = user.id
    userBalance = user.balance
    userVipLevel = user.vipLevel
  }
  return aiRequest.post('/chat', null, {
    params: { productName, productDesc, question, productId, userId, userBalance, userVipLevel, productContext }
  })
}

/** 评论情感分析 */
export function aiAnalyzeComments(productName, comments) {
  return aiRequest.post('/analyzeComments', comments, {
    params: { productName }
  })
}

/** 生成营销文案 */
export function aiGenerateCopy(description) {
  return aiRequest.post('/generateCopy', null, {
    params: { description }
  })
}

/** 商品广场AI咨询：回答关于所有商品的问题（推荐、比较、查询等） */
export function aiPlazaChat(question, productsJson) {
  const userStr = localStorage.getItem('user')
  let userId = undefined
  let userBalance = undefined
  let userVipLevel = undefined
  if (userStr) {
    const user = JSON.parse(userStr)
    userId = user.id
    userBalance = user.balance
    userVipLevel = user.vipLevel
  }
  return aiRequest.post('/plazaChat', {
    question, productsJson, userId, userBalance, userVipLevel
  })
}