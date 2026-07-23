<template>
  <div class="activity-page">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <el-button class="back-btn" @click="$router.push('/dashboard')">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <span class="page-title">活动设置</span>
    </div>

    <!-- 主体内容 -->
    <div class="content-area">
      <!-- 创建活动表单 -->
      <div class="form-card">
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span class="card-title">创建活动</span>
          <el-button type="primary" size="small" plain @click="openAiCopy">
            <el-icon><MagicStick /></el-icon> AI辅助
          </el-button>
        </div>
        <el-form :model="activityForm" label-width="100px" label-position="top">
          <el-form-item label="活动名称">
            <el-input
              v-model="activityForm.name"
              placeholder="请输入活动名称"
              maxlength="25"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="活动描述">
            <el-input
              v-model="activityForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入活动描述"
              maxlength="75"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="优惠商品">
            <div v-for="(item, index) in activityForm.products" :key="index" class="product-discount-row">
              <el-select
                v-model="item.productId"
                placeholder="选择商品"
                filterable
                class="product-select"
              >
                <el-option
                  v-for="prod in productList"
                  :key="prod.id"
                  :label="prod.name"
                  :value="prod.id"
                />
              </el-select>

              <el-input-number
                v-model="item.discount"
                :min="0.1"
                :max="0.9"
                :step="0.1"
                :precision="1"
                controls-position="right"
                class="discount-input"
              />

              <div class="discount-price">
                折后价：¥{{ calculateDiscountPrice(item) }}
              </div>

              <el-button type="danger" circle size="small" @click="removeProductRow(index)">
                <el-icon><Minus /></el-icon>
              </el-button>
            </div>

            <el-button type="primary" text @click="addProductRow">
              <el-icon><Plus /></el-icon>
              新增一行
            </el-button>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSave">保存活动</el-button>
          </el-form-item>
        </el-form>
      </div>

    <!-- AI 辅助弹窗 -->
    <el-dialog v-model="aiCopyVisible" title="AI 辅助生成活动方案" width="560px" align-center>
      <div v-if="aiCopyLoading" class="ai-loading">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p>AI 正在生成活动方案...</p>
      </div>
      <div v-else>
        <el-form label-position="top">
          <el-form-item label="描述您的活动需求">
            <el-input
              v-model="aiCopyInput"
              type="textarea"
              :rows="4"
              placeholder="例如：夏季清仓活动，面向大学生群体，降价T恤和短裤，希望利润保持20%以上..."
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="generateAiCopy" :loading="aiCopyLoading">生成方案</el-button>
          </el-form-item>
        </el-form>

        <!-- 结构化展示区 -->
        <div v-if="aiCopyResult" class="ai-result">
          <!-- 活动名称 -->
          <div class="result-section">
            <div class="result-section-header">
              <span class="section-title">活动名称</span>
              <el-tag size="small" type="success" effect="plain" v-if="aiCopyResult.name">已生成</el-tag>
            </div>
            <div class="result-content name-content" v-if="aiCopyResult.name">
              {{ aiCopyResult.name }}
            </div>
            <div class="result-empty" v-else>
              未识别到活动名称
            </div>
          </div>

          <!-- 活动描述 -->
          <div class="result-section">
            <div class="result-section-header">
              <span class="section-title">活动描述</span>
              <el-tag size="small" type="success" effect="plain" v-if="aiCopyResult.description">已生成</el-tag>
            </div>
            <div class="result-content desc-content" v-if="aiCopyResult.description">
              {{ aiCopyResult.description }}
            </div>
            <div class="result-empty" v-else>
              未识别到活动描述
            </div>
          </div>

          <!-- 改进建议 -->
          <div class="result-section">
            <div class="result-section-header">
              <span class="section-title">改进建议</span>
              <el-tag size="small" type="warning" effect="plain" v-if="aiCopyResult.suggestions && aiCopyResult.suggestions.length">
                {{ aiCopyResult.suggestions.length }} 条
              </el-tag>
            </div>
            <div v-if="aiCopyResult.suggestions && aiCopyResult.suggestions.length" class="suggestions-list">
              <div v-for="(sug, idx) in aiCopyResult.suggestions" :key="idx" class="suggestion-item">
                <span class="suggestion-index">{{ idx + 1 }}</span>
                <span class="suggestion-text">{{ sug }}</span>
              </div>
            </div>
            <div class="result-empty" v-else>
              暂无建议
            </div>
          </div>

          <!-- 操作按钮区 -->
          <div class="ai-result-actions">
            <el-button type="primary" @click="applyToForm" :disabled="!canApplyToForm">
              <el-icon><Check /></el-icon>
              应用到表单
            </el-button>
            <el-button @click="toggleRawContent">
              {{ showRawContent ? '隐藏原始内容' : '查看原始内容' }}
            </el-button>
          </div>

          <!-- 原始内容（可折叠） -->
          <div v-if="showRawContent && aiCopyResult.rawContent" class="raw-content">
            <div class="raw-title">原始 AI 回复</div>
            <pre>{{ aiCopyResult.rawContent }}</pre>
          </div>
        </div>
      </div>
    </el-dialog>

      <!-- 活动列表 -->
      <div class="list-card">
        <div class="card-header">
          <span class="card-title">活动列表</span>
        </div>
        <el-table :data="activityList" border stripe style="width: 100%">
          <el-table-column prop="name" label="活动名称" min-width="150" />
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === '已发布' ? 'success' : 'info'" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === '未发布'"
                type="success"
                size="small"
                text
                @click="handlePublish(row)"
              >
                发布
              </el-button>
              <el-button
                v-if="row.status === '已发布'"
                type="warning"
                size="small"
                text
                @click="handleUnpublish(row)"
              >
                撤销
              </el-button>
              <el-button type="danger" size="small" text @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ArrowLeft, Plus, Minus, MagicStick, Loading, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createActivity, getActivityList, publishActivity, unpublishActivity, deleteActivity } from '@/api/activity'
import { getMyProductList } from '@/api/product'
import { aiGenerateCopy } from '@/api/ai'

const loading = ref(false)
const productList = ref([])
const activityList = ref([])

const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
const userId = currentUser.id

// AI 辅助
const aiCopyVisible = ref(false)
const aiCopyInput = ref('')
const aiCopyResult = ref(null)
const aiCopyLoading = ref(false)
const showRawContent = ref(false)

/** 是否可以应用到表单（名称和描述至少有一个已生成） */
const canApplyToForm = computed(() => {
  return !!(aiCopyResult.value && (aiCopyResult.value.name || aiCopyResult.value.description))
})

/** 打开 AI 辅助弹窗 */
function openAiCopy() {
  aiCopyVisible.value = true
  aiCopyInput.value = ''
  aiCopyResult.value = null
  showRawContent.value = false
}

/** 生成 AI 活动方案 */
async function generateAiCopy() {
  const desc = aiCopyInput.value.trim()
  if (!desc) {
    ElMessage.warning('请先描述您的活动需求')
    return
  }
  aiCopyLoading.value = true
  aiCopyResult.value = null
  try {
    const res = await aiGenerateCopy(desc)
    if (res.code === 200 && res.data) {
      aiCopyResult.value = res.data
    } else {
      // 兼容旧版返回，构造一个默认对象
      aiCopyResult.value = {
        name: '',
        description: '',
        suggestions: [],
        rawContent: res.message || '生成失败',
        simulated: false
      }
    }
  } catch (e) {
    aiCopyResult.value = {
      name: '',
      description: '',
      suggestions: [],
      rawContent: '网络异常，请检查连接后重试。',
      simulated: false
    }
  } finally {
    aiCopyLoading.value = false
  }
}

/** 将 AI 生成结果应用到活动表单 */
function applyToForm() {
  if (!aiCopyResult.value) return
  let applied = false
  if (aiCopyResult.value.name) {
    activityForm.name = aiCopyResult.value.name
    applied = true
  }
  if (aiCopyResult.value.description) {
    activityForm.description = aiCopyResult.value.description
    applied = true
  }
  if (applied) {
    ElMessage.success('已应用到活动表单')
    aiCopyVisible.value = false
  } else {
    ElMessage.warning('没有可应用的内容')
  }
}

/** 切换显示原始内容 */
function toggleRawContent() {
  showRawContent.value = !showRawContent.value
}

const activityForm = reactive({
  name: '',
  description: '',
  products: [{ productId: '', discount: 0.9 }]
})

/** 计算折后价（返回数值） */
function calculateDiscountPrice(item) {
  const prod = productList.value.find(p => p.id === item.productId)
  if (!prod || !item.discount) return 0
  return parseFloat((prod.price * item.discount).toFixed(2))
}

/** 新增一行折扣商品 */
function addProductRow() {
  activityForm.products.push({ productId: '', discount: 0.9 })
}

/** 删除一行折扣商品 */
function removeProductRow(index) {
  if (activityForm.products.length <= 1) {
    ElMessage.warning('至少保留一行')
    return
  }
  activityForm.products.splice(index, 1)
}

/** 加载商品列表（只加载当前卖家的商品） */
async function loadProducts() {
  try {
    const res = await getMyProductList()
    if (res.code === 200) {
      productList.value = res.data || []
    }
  } catch (e) {
    console.error('加载商品列表失败', e)
  }
}

/** 加载活动列表 */
async function loadActivities() {
  loading.value = true
  try {
    const res = await getActivityList()
    if (res.code === 200) {
      activityList.value = (res.data || []).map(item => ({
        ...item,
        status: item.status === 0 ? '未发布' : item.status === 1 ? '已发布' : item.status
      }))
    }
  } catch (e) {
    console.error('加载活动列表失败', e)
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

/** 保存活动 */
async function handleSave() {
  if (!activityForm.name.trim()) {
    ElMessage.warning('请输入活动名称')
    return
  }
  if (!activityForm.description.trim()) {
    ElMessage.warning('请输入活动描述')
    return
  }
  // 过滤掉未选择商品的行
  const validProducts = activityForm.products.filter(p => p.productId)
  if (validProducts.length === 0) {
    ElMessage.warning('请至少选择一个优惠商品')
    return
  }

  try {
    const res = await createActivity({
      name: activityForm.name,
      description: activityForm.description,
      creatorId: userId,
      activityProducts: validProducts.map(p => {
        const prod = productList.value.find(pr => pr.id === p.productId)
        return {
          productId: p.productId,
          discount: p.discount,
          discountPrice: calculateDiscountPrice(p),
          originalPrice: prod ? prod.price : 0
        }
      })
    })
    if (res.code === 200) {
      ElMessage.success('活动保存成功')
      activityForm.name = ''
      activityForm.description = ''
      activityForm.products = [{ productId: '', discount: 0.9 }]
      loadActivities()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存活动失败')
  }
}

/** 发布活动 */
async function handlePublish(row) {
  try {
    await ElMessageBox.confirm('确认发布该活动吗？', '提示', {
      confirmButtonText: '确认发布',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await publishActivity(row.id)
    if (res.code === 200) {
      ElMessage.success('活动发布成功')
      loadActivities()
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

/** 撤销发布 */
async function handleUnpublish(row) {
  try {
    await ElMessageBox.confirm('确认撤销该活动吗？撤销后商品将恢复原价。', '提示', {
      confirmButtonText: '确认撤销',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await unpublishActivity(row.id)
    if (res.code === 200) {
      ElMessage.success('活动已撤销')
      loadActivities()
    } else {
      ElMessage.error(res.message || '撤销失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('撤销失败')
    }
  }
}

/** 删除活动 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该活动吗？', '提示', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteActivity(row.id)
    if (res.code === 200) {
      ElMessage.success('活动删除成功')
      loadActivities()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadProducts()
  loadActivities()
})
</script>

<style scoped>
.activity-page {
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
.back-btn {
  flex-shrink: 0;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* === 内容区 === */
.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.content-area::-webkit-scrollbar {
  width: 8px;
}
.content-area::-webkit-scrollbar-track {
  background: rgba(240, 240, 240, 0.5);
  border-radius: 4px;
}
.content-area::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

/* 卡片通用 */
.form-card,
.list-card {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  padding: 20px;
}

.card-header {
  margin-bottom: 16px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 折扣商品行 */
.product-discount-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.product-select {
  width: 200px;
}
.discount-input {
  width: 130px;
}
.discount-price {
  font-size: 14px;
  color: #f56c6c;
  font-weight: 600;
  min-width: 100px;
}

/* Element Plus 覆盖 */
:deep(.el-table) {
  background: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.5);
  --el-table-border-color: rgba(0, 0, 0, 0.06);
  --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.4);
}
:deep(.el-table th.el-table__cell) {
  background: rgba(255, 255, 255, 0.5) !important;
}
:deep(.el-table td.el-table__cell) {
  background: transparent !important;
}
:deep(.el-table__row:hover > td.el-table__cell) {
  background: rgba(255, 255, 255, 0.4) !important;
}
:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: rgba(255, 255, 255, 0.25) !important;
}
:deep(.el-table__body-wrapper) {
  background: transparent;
}
:deep(.el-table__inner-wrapper::before) {
  background-color: rgba(0, 0, 0, 0.06);
}
:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
}
:deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.7);
}

/* AI 辅助弹窗样式 */
.ai-loading {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}
.ai-result {
  margin-top: 16px;
}

/* 结构化结果区块 */
.result-section {
  margin-bottom: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px 16px;
}
.result-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}
.result-content {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
}
.name-content {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}
.desc-content {
  color: #606266;
}
.result-empty {
  font-size: 13px;
  color: #c0c4cc;
  font-style: italic;
}

/* 建议列表 */
.suggestions-list {
  margin-top: 4px;
}
.suggestion-item {
  display: flex;
  gap: 10px;
  padding: 6px 0;
  border-bottom: 1px dashed #e4e7ed;
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
}
.suggestion-item:last-child {
  border-bottom: none;
}
.suggestion-index {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  background: #e6a23c;
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
}
.suggestion-text {
  flex: 1;
}

/* 操作按钮区 */
.ai-result-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

/* 原始内容 */
.raw-content {
  margin-top: 16px;
}
.raw-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.raw-content pre {
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 13px;
  line-height: 1.8;
  color: #909399;
  background: #fafafa;
  padding: 12px;
  border-radius: 6px;
  margin: 0;
  border: 1px solid #ebeef5;
}
</style>
