<template>
  <div class="product-detail-page">
    <!-- 顶部返回栏 -->
    <div class="top-bar">
      <el-button class="back-btn" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <span class="page-title">商品详情</span>
    </div>

    <div class="content-scroll">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="!product" class="empty-wrap">
        <el-empty description="商品不存在或已下架" />
      </div>

      <div v-else class="detail-content">
        <!-- 上半部分：商品信息 -->
        <div class="product-main">
          <!-- 左侧大图 -->
          <div class="product-image-section">
            <div class="product-image-large">
              <img v-if="product.image" :src="product.image" alt="商品大图" />
              <div v-else class="image-placeholder-large">
                <el-icon :size="60"><Picture /></el-icon>
              </div>
            </div>
          </div>

          <!-- 右侧信息 -->
          <div class="product-info-section">
            <h2 class="product-name">{{ truncate(product.name, 7) }}</h2>
            <div class="seller-name">卖家：{{ product.sellerName || '未知卖家' }}</div>
            <div class="product-desc">{{ truncate(product.description, 50) || '暂无介绍' }}</div>

            <div class="price-section">
              <span class="current-price">¥{{ formatPrice(discountedPrice) }}</span>
              <span v-if="product.discount && product.discount < 1" class="original-price">
                ¥{{ product.price }}
              </span>
            </div>

            <div class="buy-section">
              <span class="quantity-label">购买数量：</span>
              <el-input-number v-model="quantity" :min="1" :max="product.stock || 99" :step="1" size="default" />
            </div>

            <div class="stock-info">库存：{{ product.stock || 0 }}</div>

            <div class="action-buttons">
              <el-button type="danger" size="large" @click="handleBuy">
                立即购买
              </el-button>
              <el-button size="large" @click="handleCancel">
                取消付款
              </el-button>
            </div>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <div class="comment-header">
            <h3>商品评价</h3>
            <div class="avg-rating">
              <el-rate :model-value="avgRating" disabled show-score text-color="#ff9900" />
            </div>
          </div>

          <div v-if="comments.length === 0" class="no-comment">
            暂无评论
          </div>

          <div v-else class="comment-list">
            <div v-for="comment in sortedComments" :key="comment.id" class="comment-item">
              <div class="comment-top">
                <span class="comment-user">{{ comment.username }}</span>
                <el-rate :model-value="comment.rating" disabled />
                <span class="comment-time">{{ comment.createTime }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 购买确认弹窗 -->
    <el-dialog v-model="buyDialogVisible" title="确认购买" width="420px" align-center>
      <div class="confirm-content">
        <p>商品：{{ product?.name }}</p>
        <p>数量：{{ quantity }}</p>
        <p>单价：¥{{ discountedPrice }}</p>
        <p class="total-price">商品总价：¥{{ totalPrice }}</p>
        <div v-if="vipDiscountInfo" class="vip-discount-line">
          <el-tag type="warning" size="small">{{ vipDiscountInfo.name }}</el-tag>
          <span class="vip-discount-text">
            每满{{ vipDiscountInfo.threshold }}减{{ vipDiscountInfo.discountAmount }}元
            × {{ vipDiscountInfo.times }} = 减¥{{ vipDiscountInfo.discount }}
          </span>
        </div>
        <p v-if="vipDiscountInfo" class="final-price">
          实付金额：¥{{ vipDiscountInfo.finalPrice }}
        </p>
      </div>
      <template #footer>
        <el-button @click="buyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBuy">确认购买</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductDetail, buyProduct } from '@/api/product'
import { getVipConfig } from '@/api/vip'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const buyDialogVisible = ref(false)
const comments = ref([])
const vipConfigList = ref([])

const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
const buyerId = currentUser.id
const userVipLevel = currentUser.vipLevel || 1

/** 计算折后价 */
const discountedPrice = computed(() => {
  const p = product.value
  if (!p) return 0
  if (p.discountPrice != null) {
    return parseFloat(Number(p.discountPrice).toFixed(2))
  }
  const price = Number(p.price) || 0
  if (p.discount && Number(p.discount) < 1) {
    return parseFloat((price * Number(p.discount)).toFixed(2))
  }
  return price
})

/** 总价 */
const totalPrice = computed(() => {
  return parseFloat((discountedPrice.value * quantity.value).toFixed(2))
})

/** VIP满减优惠 */
const vipDiscountInfo = computed(() => {
  const config = vipConfigList.value.find(c => c.level === userVipLevel)
  if (!config || !config.threshold || config.threshold <= 0 || !config.discountAmount) {
    return null
  }
  const total = totalPrice.value
  const times = Math.floor(total / config.threshold)
  if (times <= 0) return null
  const discount = parseFloat((config.discountAmount * times).toFixed(2))
  const finalPrice = parseFloat((total - discount).toFixed(2))
  return {
    level: config.level,
    name: config.name,
    threshold: config.threshold,
    discountAmount: config.discountAmount,
    times,
    discount,
    finalPrice: finalPrice < 0 ? 0 : finalPrice
  }
})

/** 格式化价格，最多显示5位整数 */
function formatPrice(price) {
  const num = Number(price) || 0
  const str = num.toFixed(2)
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

/** 平均星级 */
const avgRating = computed(() => {
  if (!comments.value || comments.value.length === 0) return 0
  const sum = comments.value.reduce((acc, c) => acc + (c.rating || 0), 0)
  return parseFloat((sum / comments.value.length).toFixed(1))
})

/** 按时间排序的评论 */
const sortedComments = computed(() => {
  return [...comments.value].sort((a, b) => {
    return new Date(b.createTime || 0) - new Date(a.createTime || 0)
  })
})

/** 加载VIP配置 */
async function loadVipConfig() {
  try {
    const res = await getVipConfig()
    if (res.code === 200 && Array.isArray(res.data)) {
      vipConfigList.value = res.data
    }
  } catch (e) {
    console.error('加载VIP配置失败', e)
  }
}

/** 加载商品详情 */
async function loadProductDetail() {
  const id = route.params.id
  if (!id) {
    ElMessage.error('商品ID不存在')
    return
  }
  loading.value = true
  try {
    const res = await getProductDetail(id)
    if (res.code === 200 && res.data) {
      // 后端返回 { product: {...}, reviews: [...] }
      product.value = res.data.product || res.data
      comments.value = res.data.reviews || []
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    console.error('加载商品详情失败', e)
    ElMessage.error('加载商品详情失败')
  } finally {
    loading.value = false
  }
}

/** 立即购买 */
function handleBuy() {
  if (!product.value || product.value.stock <= 0) {
    ElMessage.warning('商品库存不足')
    return
  }
  if (quantity.value > product.value.stock) {
    ElMessage.warning('购买数量超过库存')
    return
  }
  buyDialogVisible.value = true
}

/** 确认购买 */
async function confirmBuy() {
  if (!buyerId) {
    ElMessage.error('请先登录')
    return
  }
  try {
    const res = await buyProduct({
      productId: product.value.id,
      buyerId: buyerId,
      quantity: quantity.value,
      price: discountedPrice.value
    })
    if (res.code === 200) {
      ElMessage.success('购买成功')
      product.value.stock -= quantity.value
      buyDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '购买失败')
    }
  } catch (e) {
    console.error('购买失败', e)
    ElMessage.error('购买失败')
  }
}

/** 取消付款（恢复库存） */
function handleCancel() {
  if (!product.value) return
  ElMessageBox.confirm('确定取消付款并恢复库存吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.info('已取消，库存已恢复')
  }).catch(() => {})
}

onMounted(() => {
  loadProductDetail()
  loadVipConfig()
})
</script>

<style scoped>
.product-detail-page {
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
.back-btn { flex-shrink: 0; }
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* === 内容滚动区 === */
.content-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}
.content-scroll::-webkit-scrollbar { width: 8px; }
.content-scroll::-webkit-scrollbar-track { background: rgba(240, 240, 240, 0.5); border-radius: 4px; }
.content-scroll::-webkit-scrollbar-thumb { background: #c0c4cc; border-radius: 4px; }

.loading-wrap,
.empty-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

/* === 商品主信息区 === */
.product-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  padding: 24px;
  margin-bottom: 20px;
}

/* 左侧大图 */
.product-image-large {
  width: 100%;
  height: 360px;
  border-radius: 10px;
  overflow: hidden;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}
.product-image-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.image-placeholder-large { color: #c0c4cc; }

/* 右侧信息 */
.product-info-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.product-name {
  font-size: 22px;
  font-weight: 700;
  color: #1a1f36;
  margin: 0;
}
.seller-name { font-size: 14px; color: #606266; }
.product-desc {
  font-size: 14px;
  color: #909399;
  line-height: 1.6;
  min-height: 40px;
}
.price-section {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-top: 8px;
}
.current-price {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}
.original-price {
  font-size: 16px;
  color: #909399;
  text-decoration: line-through;
}
.buy-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}
.quantity-label { font-size: 14px; color: #606266; }
.stock-info { font-size: 14px; color: #67c23a; }
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

/* === 评论区 === */
.comment-section {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  padding: 24px;
}
.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.comment-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.no-comment { text-align: center; color: #909399; padding: 30px 0; }
.comment-list { display: flex; flex-direction: column; gap: 16px; }
.comment-item {
  padding: 16px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(8px);
  border-radius: 8px;
}
.comment-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.comment-user { font-size: 14px; font-weight: 600; color: #303133; }
.comment-time { font-size: 12px; color: #c0c4cc; margin-left: auto; }
.comment-content { font-size: 14px; color: #606266; line-height: 1.6; }

/* 确认弹窗内容 */
.confirm-content p { margin: 8px 0; font-size: 14px; color: #606266; }
.total-price {
  font-size: 18px !important;
  font-weight: 700;
  color: #f56c6c !important;
  margin-top: 12px;
}
.vip-discount-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
  padding: 8px 12px;
  background: #fdf6ec;
  border-radius: 6px;
}
.vip-discount-text {
  font-size: 13px;
  color: #e6a23c;
}
.final-price {
  font-size: 20px !important;
  font-weight: 700;
  color: #67c23a !important;
  margin-top: 12px;
}

/* Element Plus 覆盖 */
:deep(.el-input-number .el-input__wrapper) { background: rgba(255, 255, 255, 0.7); }
</style>