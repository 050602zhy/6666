import axios from 'axios'

const request = axios.create({
  baseURL: '/ai',
  timeout: 30000
})

request.interceptors.request.use(config => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    config.headers['X-User-Id'] = user.id
  }
  return config
})

request.interceptors.response.use(response => response.data)

/** 创建知识库 */
export function createKnowledgeBase(name, description, type, creatorId) {
  return request.post('/knowledge/create', null, {
    params: { name, description, type, creatorId }
  })
}

/** 获取知识库列表 */
export function getKnowledgeBaseList(creatorId) {
  return request.get('/knowledge/list', { params: { creatorId } })
}

/** 上传文档 */
export function uploadDocument(kbId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/knowledge/${kbId}/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 获取文档列表 */
export function getDocumentList(kbId) {
  return request.get(`/knowledge/${kbId}/documents`)
}

/** 删除知识库 */
export function deleteKnowledgeBase(kbId) {
  return request.delete(`/knowledge/${kbId}`)
}

/** 删除文档 */
export function deleteDocument(docId) {
  return request.delete(`/knowledge/doc/${docId}`)
}

/** RAG问答 */
export function ragChat(kbId, question) {
  return request.post(`/knowledge/${kbId}/chat`, null, {
    params: { question }
  })
}