<template>
  <div class="knowledge-page">
    <div class="top-bar">
      <h1 class="page-title">知识库管理</h1>
      <el-button type="primary" @click="showCreate = true">+ 创建知识库</el-button>
    </div>

    <!-- 知识库列表 -->
    <div class="kb-list">
      <el-empty v-if="knowledgeBases.length === 0" description="暂无知识库，点击上方按钮创建" />
      <div v-else class="kb-grid">
        <div
          v-for="kb in knowledgeBases"
          :key="kb.id"
          class="kb-card"
          @click="selectKb(kb)"
          :class="{ active: selectedKb?.id === kb.id }"
        >
          <div class="kb-header">
            <el-icon :size="28" color="#409EFF"><Collection /></el-icon>
            <div class="kb-name">{{ kb.name }}</div>
          </div>
          <div class="kb-desc">{{ kb.description || '暂无描述' }}</div>
          <div class="kb-meta">
            <el-tag size="small" :type="kb.type === 1 ? 'success' : 'info'">
              {{ kb.type === 1 ? '商品知识库' : '平台知识库' }}
            </el-tag>
            <span class="kb-stats">{{ kb.docCount || 0 }} 文档 / {{ kb.chunkCount || 0 }} 块</span>
          </div>
          <el-button
            class="kb-delete"
            type="danger"
            text
            size="small"
            @click.stop="handleDeleteKb(kb.id)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <!-- 选中知识库的文档管理和问答 -->
    <div v-if="selectedKb" class="kb-detail">
      <div class="detail-header">
        <h2>{{ selectedKb.name }}</h2>
        <el-upload
          action=""
          :show-file-list="false"
          :before-upload="beforeUpload"
          :http-request="customUpload"
          accept=".pdf,.txt"
        >
          <el-button type="primary" :loading="uploading">+ 上传文档</el-button>
        </el-upload>
      </div>

      <!-- 文档列表 -->
      <div class="doc-section">
        <h3>文档列表</h3>
        <el-empty v-if="documents.length === 0" description="暂无文档" />
        <el-table v-else :data="documents" size="small" class="doc-table">
          <el-table-column prop="name" label="文档名称" />
          <el-table-column prop="fileType" label="类型" width="80" />
          <el-table-column prop="chunkCount" label="块数" width="80" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'danger'" size="small">
                {{ row.status === 1 ? '已完成' : row.status === 0 ? '处理中' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="danger" text size="small" @click="handleDeleteDoc(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- RAG 问答测试 -->
      <div class="rag-section">
        <h3>知识库问答测试</h3>
        <div class="rag-chat">
          <div class="rag-history" ref="ragHistoryRef">
            <div v-for="(msg, idx) in ragMessages" :key="idx" :class="['rag-msg', msg.role]">
              <div class="rag-msg-content">{{ msg.content }}</div>
            </div>
            <div v-if="ragLoading" class="rag-msg ai">
              <div class="rag-msg-content">
                <el-icon class="is-loading"><Loading /></el-icon> 思考中...
              </div>
            </div>
          </div>
          <div class="rag-input">
            <el-input
              v-model="ragInput"
              placeholder="输入问题测试知识库问答效果..."
              @keyup.enter="sendRagChat"
            />
            <el-button type="primary" :loading="ragLoading" @click="sendRagChat">发送</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建知识库弹窗 -->
    <el-dialog v-model="showCreate" title="创建知识库" width="400px" align-center>
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="createForm.name" placeholder="如：无线耳机FAQ" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="2" placeholder="知识库用途描述" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-radio-group v-model="createForm.type">
            <el-radio :label="1">商品知识库</el-radio>
            <el-radio :label="2">平台知识库</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Collection, Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createKnowledgeBase,
  getKnowledgeBaseList,
  uploadDocument,
  getDocumentList,
  deleteKnowledgeBase,
  deleteDocument,
  ragChat
} from '@/api/knowledge'

const userStr = localStorage.getItem('user')
const currentUser = userStr ? JSON.parse(userStr) : { id: 1 }

const knowledgeBases = ref([])
const selectedKb = ref(null)
const documents = ref([])
const uploading = ref(false)
const showCreate = ref(false)
const createForm = reactive({ name: '', description: '', type: 1 })

// RAG 问答
const ragInput = ref('')
const ragMessages = ref([])
const ragLoading = ref(false)
const ragHistoryRef = ref(null)

async function loadKnowledgeBases() {
  try {
    const res = await getKnowledgeBaseList(currentUser.id)
    if (res.code === 200) {
      knowledgeBases.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

function selectKb(kb) {
  selectedKb.value = kb
  loadDocuments(kb.id)
  // 清空问答记录
  ragMessages.value = []
}

async function loadDocuments(kbId) {
  try {
    const res = await getDocumentList(kbId)
    if (res.code === 200) {
      documents.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

async function handleCreate() {
  if (!createForm.name) {
    ElMessage.warning('请输入知识库名称')
    return
  }
  try {
    const res = await createKnowledgeBase(
      createForm.name,
      createForm.description,
      createForm.type,
      currentUser.id
    )
    if (res.code === 200) {
      ElMessage.success('创建成功')
      showCreate.value = false
      createForm.name = ''
      createForm.description = ''
      createForm.type = 1
      loadKnowledgeBases()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (e) {
    ElMessage.error('创建失败: ' + (e.response?.data?.message || e.message || '网络异常'))
  }
}

function beforeUpload(file) {
  const isValid = file.name.endsWith('.pdf') || file.name.endsWith('.txt')
  if (!isValid) {
    ElMessage.error('仅支持 PDF 和 TXT 格式')
    return false
  }
  return true
}

async function customUpload({ file }) {
  if (!selectedKb.value) return
  uploading.value = true
  try {
    const res = await uploadDocument(selectedKb.value.id, file)
    if (res.code === 200) {
      ElMessage.success('上传成功')
      loadDocuments(selectedKb.value.id)
      loadKnowledgeBases()
    } else {
      ElMessage.error(res.message || res.msg || '上传失败')
    }
  } catch (e) {
    console.error('上传错误:', e)
    ElMessage.error('上传失败: ' + (e.response?.data?.message || e.message || '网络异常'))
  } finally {
    uploading.value = false
  }
}

async function handleDeleteKb(kbId) {
  try {
    await ElMessageBox.confirm('确定删除该知识库及其所有文档吗？', '提示', { type: 'warning' })
    const res = await deleteKnowledgeBase(kbId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      if (selectedKb.value?.id === kbId) {
        selectedKb.value = null
        documents.value = []
      }
      loadKnowledgeBases()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

async function handleDeleteDoc(docId) {
  try {
    await ElMessageBox.confirm('确定删除该文档吗？', '提示', { type: 'warning' })
    const res = await deleteDocument(docId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadDocuments(selectedKb.value.id)
      loadKnowledgeBases()
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

async function sendRagChat() {
  const question = ragInput.value.trim()
  if (!question || !selectedKb.value) return
  ragMessages.value.push({ role: 'user', content: question })
  ragInput.value = ''
  ragLoading.value = true
  await nextTick()
  if (ragHistoryRef.value) {
    ragHistoryRef.value.scrollTop = ragHistoryRef.value.scrollHeight
  }
  try {
    const res = await ragChat(selectedKb.value.id, question)
    if (res.code === 200) {
      ragMessages.value.push({ role: 'ai', content: res.data?.content || '无回复' })
    } else {
      ragMessages.value.push({ role: 'ai', content: '服务异常，请稍后再试' })
    }
  } catch (e) {
    ragMessages.value.push({ role: 'ai', content: '网络异常，请检查连接' })
  } finally {
    ragLoading.value = false
    await nextTick()
    if (ragHistoryRef.value) {
      ragHistoryRef.value.scrollTop = ragHistoryRef.value.scrollHeight
    }
  }
}

onMounted(() => {
  loadKnowledgeBases()
})
</script>

<style scoped>
.knowledge-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(10px);
  padding: 16px 20px;
  border-radius: 12px;
}
.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1f36;
  margin: 0;
}

/* 知识库卡片网格 */
.kb-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}
.kb-card {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
  position: relative;
}
.kb-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.75);
}
.kb-card.active {
  border-color: #409EFF;
  background: rgba(255, 255, 255, 0.8);
}
.kb-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.kb-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.kb-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
  min-height: 20px;
}
.kb-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.kb-stats {
  font-size: 12px;
  color: #909399;
}
.kb-delete {
  position: absolute;
  top: 8px;
  right: 8px;
}

/* 详情区域 */
.kb-detail {
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.detail-header h2 {
  margin: 0;
  font-size: 17px;
  color: #303133;
}

.doc-section {
  margin-bottom: 20px;
}
.doc-section h3 {
  font-size: 15px;
  color: #606266;
  margin-bottom: 10px;
}
.doc-table {
  background: rgba(255, 255, 255, 0.4);
  border-radius: 8px;
}

/* RAG 问答 */
.rag-section h3 {
  font-size: 15px;
  color: #606266;
  margin-bottom: 10px;
}
.rag-chat {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  padding: 12px;
}
.rag-history {
  height: 200px;
  overflow-y: auto;
  padding: 10px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  margin-bottom: 10px;
}
.rag-msg {
  margin-bottom: 10px;
  display: flex;
}
.rag-msg.user { justify-content: flex-end; }
.rag-msg.ai { justify-content: flex-start; }
.rag-msg-content {
  max-width: 80%;
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
}
.rag-msg.user .rag-msg-content {
  background: rgba(64, 158, 255, 0.85);
  color: #fff;
}
.rag-msg.ai .rag-msg-content {
  background: rgba(255, 255, 255, 0.7);
  color: #303133;
  border: 1px solid rgba(0, 0, 0, 0.05);
}
.rag-input {
  display: flex;
  gap: 8px;
}
.rag-input .el-input {
  flex: 1;
}
</style>
