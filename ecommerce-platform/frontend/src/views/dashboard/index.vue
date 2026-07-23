<template>
  <div class="dashboard-page">
    <!-- 顶部栏：标题 + 搜索框 -->
    <div class="top-bar">
      <h1 class="page-title">商品广场</h1>
      <el-input
        v-model="searchKeyword"
        class="search-input"
        placeholder="搜索商品名称..."
        clearable
        :prefix-icon="Search"
        @input="handleSearch"
      />
    </div>

    <!-- 商品网格 -->
    <div class="product-scroll" ref="scrollRef">
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="filteredProducts.length === 0" class="empty-wrap">
        <el-empty description="暂无商品" />
      </div>
      <div v-else class="product-grid">
        <div
          v-for="item in filteredProducts"
          :key="item.id"
          class="product-card"
          @click="goToDetail(item.id)"
        >
          <!-- 折扣标签 -->
          <div v-if="item.discount && item.discount < 1" class="discount-tag">
            {{ formatDiscount(item.discount) }}
          </div>

          <!-- 商品图片 -->
          <div class="product-image">
            <img v-if="item.image" :src="item.image" alt="商品图片" />
            <div v-else class="image-placeholder">
              <el-icon :size="40"><Picture /></el-icon>
            </div>
          </div>

          <!-- 商品信息 -->
          <div class="product-info">
            <div class="product-name">{{ truncate(item.name, 7) }}</div>
            <div class="product-price">
              <span v-if="item.discount && item.discount < 1" class="original-price">
                ¥{{ item.price }}
              </span>
              ¥{{ formatPrice(getDiscountedPrice(item)) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 滚动提示 -->
      <div v-if="showScrollHint" class="scroll-hint">↓ 向下滚动查看更多 ↓</div>
    </div>

    <!-- 悬浮 AI 咨询按钮 -->
    <div class="ai-float-btn" @click="openAiPlazaChat">
      <el-icon :size="24"><ChatLineRound /></el-icon>
      <span class="ai-float-text">AI导购</span>
    </div>

    <!-- AI 广场咨询弹窗 -->
    <el-dialog v-model="aiPlazaVisible" title="AI 广场导购助手" width="520px" align-center class="ai-plaza-dialog" :teleported="false">
      <div class="ai-chat-box">
        <div class="ai-chat-history" ref="plazaChatHistoryRef">
          <div v-for="(msg, idx) in plazaChatMessages" :key="idx" :class="['ai-msg', msg.role]">
            <div class="ai-msg-content">{{ msg.content }}</div>
          </div>
          <div v-if="plazaChatLoading" class="ai-msg ai">
            <div class="ai-msg-content">
              <el-icon class="is-loading"><Loading /></el-icon> 思考中...
            </div>
          </div>
        </div>
        <div class="ai-chat-input">
          <el-input
            v-model="plazaChatInput"
            placeholder="问我任何关于商品的问题，比如：有什么推荐的耳机？"
            @keyup.enter="sendPlazaChat"
          />
          <el-button type="primary" :loading="plazaChatLoading" @click="sendPlazaChat">发送</el-button>
        </div>
        <!-- 快捷问题 -->
        <div class="quick-questions">
          <span class="quick-title">快捷提问：</span>
          <el-tag
            v-for="(q, idx) in quickQuestions"
            :key="idx"
            class="quick-tag"
            @click="askQuickQuestion(q)"
          >
            {{ q }}
          </el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Picture, ChatLineRound, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getOnSaleList } from '@/api/product'
import { aiPlazaChat } from '@/api/ai'

const router = useRouter()
const loading = ref(false)
const searchKeyword = ref('')
const allProducts = ref([])
const scrollRef = ref(null)
const showScrollHint = ref(false)

// AI 广场咨询
const aiPlazaVisible = ref(false)
const plazaChatInput = ref('')
const plazaChatMessages = ref([])
const plazaChatLoading = ref(false)
const plazaChatHistoryRef = ref(null)

// 快捷问题列表
const quickQuestions = [
  '推荐几款好的商品',
  '有什么优惠活动',
  '最便宜的商品是什么',
  '你能帮我做什么'
]

/** 获取折后价 */
function getDiscountedPrice(item) {
  if (item.discountPrice != null) {
    return Number(item.discountPrice).toFixed(2)
  }
  if (item.discount && Number(item.discount) < 1) {
    return (Number(item.price) * Number(item.discount)).toFixed(2)
  }
  return Number(item.price).toFixed(2)
}

/** 格式化折扣显示 */
function formatDiscount(discount) {
  const map = {
    0.1: '一折',
    0.2: '二折',
    0.3: '三折',
    0.4: '四折',
    0.5: '五折',
    0.6: '六折',
    0.7: '七折',
    0.8: '八折',
    0.9: '九折'
  }
  return map[discount] || `${(discount * 10).toFixed(0)}折`
}

/** 格式化价格，最多显示5位数字 */
function formatPrice(price) {
  const str = String(price)
  // 如果整数部分超过5位，截断显示
  const parts = str.split('.')
  if (parts[0].length > 5) {
    return parts[0].slice(0, 5) + '...'
  }
  return str
}

/** 截断文本 */
function truncate(text, maxLength) {
  if (!text) return ''
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}

/** 连续子串匹配搜索 */
const filteredProducts = computed(() => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return allProducts.value
  return allProducts.value.filter(item => {
    const name = item.name || ''
    return name.includes(keyword)
  })
})

/** 加载上架商品列表 */
async function loadProducts() {
  loading.value = true
  try {
    const res = await getOnSaleList()
    if (res.code === 200) {
      allProducts.value = res.data || []
      await nextTick()
      checkScrollHint()
    }
  } catch (e) {
    console.error('加载商品列表失败', e)
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  nextTick(() => checkScrollHint())
}

function checkScrollHint() {
  if (!scrollRef.value) return
  const { scrollHeight, clientHeight } = scrollRef.value
  showScrollHint.value = scrollHeight > clientHeight
}

function goToDetail(id) {
  router.push(`/product/${id}`)
}

/** 打开 AI 广场咨询弹窗 */
function openAiPlazaChat() {
  aiPlazaVisible.value = true
  if (plazaChatMessages.value.length === 0) {
    plazaChatMessages.value.push({
      role: 'ai',
      content: '亲，您好！我是商品广场的AI导购助手 🤖\n\n我可以帮您：\n1. 推荐合适的商品\n2. 比较不同商品\n3. 查询商品信息\n4. 介绍优惠活动\n\n有什么想了解的尽管问我吧~'
    })
  }
}

/** 发送广场咨询消息 */
async function sendPlazaChat() {
  const question = plazaChatInput.value.trim()
  if (!question) return
  plazaChatMessages.value.push({ role: 'user', content: question })
  plazaChatInput.value = ''
  plazaChatLoading.value = true
  await nextTick()
  if (plazaChatHistoryRef.value) {
    plazaChatHistoryRef.value.scrollTop = plazaChatHistoryRef.value.scrollHeight
  }
  try {
    // 只传AI需要的字段，去掉image等大字段避免URL参数过长
    const essentialProducts = allProducts.value.map(p => ({
      id: p.id,
      name: p.name,
      description: p.description,
      price: p.price,
      discount: p.discount,
      discountPrice: p.discountPrice,
      stock: p.stock,
      rating: p.rating,
      sellerName: p.sellerName
    }))
    const productsJson = JSON.stringify(essentialProducts)
    const res = await aiPlazaChat(question, productsJson)
    if (res.code === 200) {
      plazaChatMessages.value.push({ role: 'ai', content: res.data?.content || '抱歉，我没听懂您的问题。' })
    } else {
      plazaChatMessages.value.push({ role: 'ai', content: '抱歉，服务暂时不可用，请稍后再试。' })
    }
  } catch (e) {
    plazaChatMessages.value.push({ role: 'ai', content: '网络异常，请检查连接后重试。' })
  } finally {
    plazaChatLoading.value = false
    await nextTick()
    if (plazaChatHistoryRef.value) {
      plazaChatHistoryRef.value.scrollTop = plazaChatHistoryRef.value.scrollHeight
    }
  }
}

/** 快捷提问 */
function askQuickQuestion(question) {
  plazaChatInput.value = question
  sendPlazaChat()
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.dashboard-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: transparent;
  border-radius: 8px;
  overflow: hidden;
}

/* === 顶部栏 === */
.top-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.4);
  flex-shrink: 0;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1f36;
  margin: 0;
  white-space: nowrap;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

/* === 商品滚动区域 === */
.product-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  position: relative;
}

.product-scroll::-webkit-scrollbar {
  width: 8px;
}
.product-scroll::-webkit-scrollbar-track {
  background: rgba(240, 240, 240, 0.5);
  border-radius: 4px;
}
.product-scroll::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

.loading-wrap,
.empty-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

/* === 商品网格：3行3列 === */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

/* === 商品卡片 === */
.product-card {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.4);
  transition: box-shadow 0.2s, transform 0.2s;
  cursor: pointer;
  position: relative;
}
.product-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 折扣标签 */
.discount-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #67c23a;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  z-index: 2;
}

/* 商品图片 */
.product-image {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #f5f7fa;
}
.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}

/* 商品信息 */
.product-info {
  padding: 12px 14px 16px;
}
.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #000;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
  margin-top: 8px;
}
.original-price {
  font-size: 13px;
  color: #909399;
  text-decoration: line-through;
  margin-right: 8px;
  font-weight: 400;
}

/* 滚动提示 */
.scroll-hint {
  text-align: center;
  padding: 16px;
  color: #909399;
  font-size: 13px;
  animation: bounce 1.5s infinite;
}
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(4px); }
}

/* Element Plus 覆盖 */
:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.4) inset;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409EFF inset;
}

/* === 悬浮 AI 咨询按钮 === */
.ai-float-btn {
  position: fixed;
  right: 32px;
  bottom: 40px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  z-index: 100;
}
.ai-float-btn:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 10px 28px rgba(102, 126, 234, 0.5);
}
.ai-float-text {
  font-size: 11px;
  margin-top: 2px;
  font-weight: 600;
}

/* === AI 广场咨询弹窗 — 整体风格统一 === */
:deep(.ai-plaza-dialog) {
  background: transparent !important;
  box-shadow: none !important;
  border-radius: 16px;
  overflow: hidden;
}
:deep(.ai-plaza-dialog .el-dialog__header) {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.4);
  padding: 16px 20px;
  margin: 0;
}
:deep(.ai-plaza-dialog .el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
:deep(.ai-plaza-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #909399;
}
:deep(.ai-plaza-dialog .el-dialog__body) {
  padding: 16px 20px 20px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.ai-chat-box {
  display: flex;
  flex-direction: column;
  height: 440px;
}

/* 聊天历史区：毛玻璃卡片 */
.ai-chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  margin-bottom: 12px;
}
.ai-chat-history::-webkit-scrollbar { width: 6px; }
.ai-chat-history::-webkit-scrollbar-track { background: transparent; }
.ai-chat-history::-webkit-scrollbar-thumb { background: rgba(192, 196, 204, 0.6); border-radius: 3px; }

/* 消息气泡 */
.ai-msg {
  margin-bottom: 12px;
  display: flex;
}
.ai-msg.user { justify-content: flex-end; }
.ai-msg.ai   { justify-content: flex-start; }

.ai-msg-content {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  white-space: pre-wrap;
}

/* 用户消息：半透明主题蓝 + 毛玻璃 */
.ai-msg.user .ai-msg-content {
  background: rgba(64, 158, 255, 0.85);
  backdrop-filter: blur(4px);
  color: #fff;
  border-bottom-right-radius: 4px;
}

/* AI 消息：半透明白色 + 毛玻璃 */
.ai-msg.ai .ai-msg-content {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(4px);
  color: #303133;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-bottom-left-radius: 4px;
}

/* 输入区 */
.ai-chat-input {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}
.ai-chat-input .el-input {
  flex: 1;
}
.ai-chat-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.7) !important;
  backdrop-filter: blur(8px);
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.5) inset;
}

/* 快捷问题 */
.quick-questions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.quick-title {
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}
.quick-tag {
  cursor: pointer;
  transition: all 0.2s;
  background: rgba(64, 158, 255, 0.1);
  border: 1px solid rgba(64, 158, 255, 0.3);
  color: #409EFF;
}
.quick-tag:hover {
  background: rgba(64, 158, 255, 0.2);
  transform: translateY(-1px);
}
</style>
