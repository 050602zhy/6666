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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getOnSaleList } from '@/api/product'

const router = useRouter()
const loading = ref(false)
const searchKeyword = ref('')
const allProducts = ref([])
const scrollRef = ref(null)
const showScrollHint = ref(false)

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
</style>
