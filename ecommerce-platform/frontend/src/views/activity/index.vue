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
        <div class="card-header">
          <span class="card-title">创建活动</span>
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
import { ref, reactive, onMounted } from 'vue'
import { ArrowLeft, Plus, Minus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createActivity, getActivityList, publishActivity, unpublishActivity, deleteActivity } from '@/api/activity'
import { getMyProductList } from '@/api/product'

const loading = ref(false)
const productList = ref([])
const activityList = ref([])

const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
const userId = currentUser.id

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
</style>
