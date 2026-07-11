<template>
  <div class="product-page">
    <!-- 顶部栏：返回按钮 + 搜索框 -->
    <div class="top-bar">
      <el-button class="back-btn" @click="$router.push('/dashboard')">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <el-input
        v-model="searchKeyword"
        class="search-input"
        placeholder="搜索商品名称或描述..."
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 商品滚动区域：最多显示3行 -->
    <div class="product-scroll" ref="scrollRef" @scroll="handleScroll">
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="filteredProducts.length === 0" class="empty-wrap">
        <el-empty description="暂无商品数据" />
      </div>
      <div v-else class="product-grid">
        <div
          v-for="item in filteredProducts"
          :key="item.id"
          class="product-card"
        >
          <!-- 商品图片 -->
          <div
            class="product-image"
            :style="{ backgroundColor: item.image ? '#fff' : '#1a1a1a' }"
            @click="handleUploadImage(item)"
          >
            <img v-if="item.image" :src="item.image" alt="商品图片" />
            <div v-else class="image-placeholder">
              <el-icon :size="32"><Plus /></el-icon>
              <span>点击上传图片</span>
            </div>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="onFileChange"
            />
          </div>

          <!-- 商品信息 -->
          <div class="product-info">
            <div class="product-name" :title="item.name">{{ item.name }}</div>
            <div class="product-desc" :title="item.description">{{ item.description || '暂无描述' }}</div>
            <div class="product-price">¥{{ item.price }}</div>
            <div
              class="product-stock"
              :class="{ 'stock-warning': item.onSale === 1 && item.stock < 20 }"
            >
              库存: {{ item.stock }}
            </div>
            <div class="product-actions">
              <!-- 编辑描述 -->
              <el-input
                v-model="item.description"
                class="desc-input"
                maxlength="50"
                show-word-limit
                placeholder="输入商品描述"
                size="small"
                @blur="handleUpdateDesc(item)"
              />
              <!-- 编辑价格 -->
              <div class="price-row">
                <el-input-number
                  v-model="item.price"
                  :min="0.01"
                  :max="9999.99"
                  :precision="2"
                  :step="1"
                  size="small"
                  controls-position="right"
                  @blur="handleUpdatePrice(item)"
                />
              </div>
              <!-- 上下架按钮 -->
              <div class="on-sale-row">
                <span class="on-sale-label">是否上架:</span>
                <el-button
                  :type="item.onSale === 1 ? 'success' : 'default'"
                  :plain="item.onSale !== 1"
                  size="small"
                  circle
                  @click="handleToggleOnSale(item, 1)"
                >
                  <el-icon><Check /></el-icon>
                </el-button>
                <el-button
                  :type="item.onSale === 0 ? 'danger' : 'default'"
                  :plain="item.onSale !== 0"
                  size="small"
                  circle
                  @click="handleToggleOnSale(item, 0)"
                >
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 底部滚动提示 -->
      <div v-if="showScrollHint" class="scroll-hint">↓ 向下滚动查看更多 ↓</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ArrowLeft, Search, Plus, Check, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProductList, updateProduct, updateProductImage, updateProductOnSale } from '@/api/product'

// 商品列表原始数据
const allProducts = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const scrollRef = ref(null)
const fileInput = ref(null)
const showScrollHint = ref(false)
// 当前上传图片的商品ID
const currentUploadId = ref(null)

/**
 * 连续子串匹配搜索
 * 规则：输入的关键词必须是商品名称或描述中的连续子串
 * 例如输入"水壶"："水杯牌壶"不匹配（"水"后没跟"壶"），"水壶牌杯"匹配
 */
const filteredProducts = computed(() => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return allProducts.value
  return allProducts.value.filter(item => {
    const name = item.name || ''
    const desc = item.description || ''
    return name.includes(keyword) || desc.includes(keyword)
  })
})

/** 加载商品列表 */
async function loadProducts() {
  loading.value = true
  try {
    const res = await getProductList()
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

/** 搜索输入 */
function handleSearch() {
  // filteredProducts 是 computed，自动响应
  nextTick(() => checkScrollHint())
}

/** 检查是否需要显示滚动提示 */
function checkScrollHint() {
  if (!scrollRef.value) return
  const { scrollHeight, clientHeight } = scrollRef.value
  showScrollHint.value = scrollHeight > clientHeight
}

/** 滚动时隐藏提示 */
function handleScroll() {
  if (!showScrollHint.value) return
  const { scrollTop } = scrollRef.value
  if (scrollTop > 30) showScrollHint.value = false
}

/** 点击图片区域触发文件选择 */
function handleUploadImage(item) {
  currentUploadId.value = item.id
  // 使用同一个 file input，触发点击
  const inputs = document.querySelectorAll('input[type="file"]')
  if (inputs.length > 0) {
    inputs[0].click()
  }
}

/** 文件选择后，转base64上传 */
function onFileChange(e) {
  const file = e.target.files[0]
  if (!file || !currentUploadId.value) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过5MB')
    return
  }
  const reader = new FileReader()
  reader.onload = async (ev) => {
    const base64 = ev.target.result
    try {
      await updateProductImage(currentUploadId.value, base64)
      ElMessage.success('图片上传成功')
      await loadProducts()
    } catch (err) {
      ElMessage.error('图片上传失败')
    }
  }
  reader.readAsDataURL(file)
  // 清空 input 以便重复选择同一文件
  e.target.value = ''
}

/** 更新商品描述 */
async function handleUpdateDesc(item) {
  if (item.description && item.description.length > 50) {
    item.description = item.description.slice(0, 50)
    ElMessage.warning('商品描述最多50字')
    return
  }
  try {
    await updateProduct({ id: item.id, description: item.description })
  } catch (e) {
    ElMessage.error('更新描述失败')
  }
}

/** 更新商品价格 */
async function handleUpdatePrice(item) {
  if (item.price <= 0 || item.price >= 10000) {
    ElMessage.warning('商品价格必须在0到10000之间（不含0和10000）')
    await loadProducts() // 重新加载恢复原值
    return
  }
  try {
    await updateProduct({ id: item.id, price: item.price })
  } catch (e) {
    ElMessage.error('更新价格失败')
  }
}

/** 切换上下架状态 */
async function handleToggleOnSale(item, onSale) {
  if (item.onSale === onSale) return
  try {
    await updateProductOnSale(item.id, onSale)
    item.onSale = onSale
    ElMessage.success(onSale === 1 ? '已上架' : '已下架')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}

/* === 顶部栏 === */
.top-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}
.back-btn {
  flex-shrink: 0;
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
/* 自定义滚动条 */
.product-scroll::-webkit-scrollbar {
  width: 8px;
}
.product-scroll::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 4px;
}
.product-scroll::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}
.product-scroll::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

.loading-wrap,
.empty-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

/* === 商品网格 === */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

/* === 商品卡片 === */
.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  transition: box-shadow 0.2s;
}
.product-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 商品图片区域 */
.product-image {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
}
.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 13px;
}

/* 商品信息区域 */
.product-info {
  padding: 12px 14px 16px;
}
.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 7em;
}
.product-desc {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
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
.product-stock {
  font-size: 14px;
  color: #67c23a;
  margin-top: 4px;
}
.product-stock.stock-warning {
  color: #f56c6c;
  font-weight: 600;
}

/* 操作区域 */
.product-actions {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.desc-input {
  width: 100%;
}
.price-row {
  width: 100%;
}
.price-row .el-input-number {
  width: 100%;
}
.on-sale-row {
  display: flex;
  align-items: center;
  gap: 6px;
}
.on-sale-label {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}

/* 滚动提示 */
.scroll-hint {
  text-align: center;
  padding: 10px;
  color: #909399;
  font-size: 13px;
  animation: bounce 1.5s infinite;
}
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(4px); }
}
</style>