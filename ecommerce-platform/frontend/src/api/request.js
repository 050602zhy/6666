import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 携带用户ID用于后端身份校验
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        if (user.id) {
          config.headers['X-User-Id'] = user.id
        }
      } catch (e) {}
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const data = response.data
    // 防御：后端可能返回非 JSON（如 HTML 错误页）
    if (!data || typeof data !== 'object') {
      console.warn('后端返回非 JSON 响应:', data)
      return { code: 500, message: '后端返回非 JSON 响应', data: null }
    }
    if (data.code !== 200) {
      const msg = typeof data.message === 'string' ? data.message : '请求失败'
      ElMessage.error(msg)
      // 401 未登录，跳转到登录页
      if (data.code === 401) {
        localStorage.removeItem('user')
        localStorage.removeItem('token')
        router.push('/login')
      }
    }
    return data
  },
  (error) => {
    console.error('请求错误:', error)
    const msg = error?.response?.data?.message || error?.message || '网络异常'
    const safeMsg = typeof msg === 'string' ? msg : String(msg)
    ElMessage.error(safeMsg)
    return Promise.reject(error)
  }
)

export default request
