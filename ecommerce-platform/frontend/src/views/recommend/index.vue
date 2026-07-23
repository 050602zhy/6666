<template>
  <div class="recommend-page">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <el-button class="back-btn" @click="$router.push('/dashboard')">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <span class="page-title">智能推荐</span>
    </div>

    <!-- 主体内容 -->
    <div class="content-area">
      <!-- 搜索区域 -->
      <el-card shadow="hover" class="search-card">
        <div class="search-row">
          <el-form :inline="true" class="search-form">
            <el-form-item label="用户ID">
              <el-input
                v-model="userId"
                placeholder="请输入用户ID"
                clearable
                style="width: 240px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRecommend" :loading="loading">
                <el-icon><Search /></el-icon>
                获取推荐
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>

      <!-- 推荐结果 -->
      <div v-if="recommendList.length > 0" class="result-section">
        <div class="result-title">
          为您推荐以下商品
          <span class="result-count">共 {{ recommendList.length }} 件</span>
        </div>
        <div class="recommend-grid">
          <el-card
            v-for="item in recommendList"
            :key="item.id"
            shadow="hover"
            class="recommend-card"
          >
            <!-- 商品图片 -->
            <div class="product-image-wrap">
              <img v-if="item.image" :src="item.image" :alt="item.name" class="product-image" />
              <div v-else class="product-image-placeholder">
                <el-icon :size="48"><Picture /></el-icon>
              </div>
              <!-- 匹配度标签 -->
              <el-tag :type="getMatchType(item.matchScore)" size="small" class="match-tag">
                {{ item.matchScore }}% 匹配
              </el-tag>
            </div>

            <!-- 商品信息 -->
            <div class="product-info">
              <div class="product-name" :title="item.name">{{ item.name }}</div>

              <!-- 评分 -->
              <div class="product-rating">
                <el-rate :model-value="item.rating" disabled :max="5" show-score text-color="#ff9900" />
              </div>

              <!-- 价格 -->
              <div class="product-price">
                <span v-if="item.discountPrice" class="discount-price">
                  ¥{{ item.discountPrice }}
                </span>
                <span :class="{ 'original-price': item.discountPrice }">
                  ¥{{ item.price }}
                </span>
              </div>

              <!-- 推荐理由 -->
              <div class="reason-row">
                <el-icon><Star /></el-icon>
                <span class="reason-text">{{ item.reason }}</span>
              </div>

              <!-- 匹配度进度条 -->
              <el-progress
                :percentage="item.matchScore"
                :color="getMatchColor(item.matchScore)"
                :stroke-width="6"
                :show-text="false"
              />
            </div>
          </el-card>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && !searched" class="empty-state">
        <el-empty description="请输入用户ID获取个性化推荐" />
      </div>

      <!-- 无结果状态 -->
      <div v-else-if="!loading && searched && recommendList.length === 0" class="empty-state">
        <el-empty description="暂无推荐商品" />
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <div class="loading-text">正在为您智能推荐...</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ArrowLeft, Search, Picture, Star, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getRecommend } from '@/api/recommend'

const userId = ref('')
const recommendList = ref([])
const loading = ref(false)
const searched = ref(false)

function getMatchType(score) {
  if (score >= 80) return 'success'
  if (score >= 60) return 'primary'
  if (score >= 40) return 'warning'
  return 'info'
}

function getMatchColor(score) {
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#409eff'
  if (score >= 40) return '#e6a23c'
  return '#909399'
}

async function handleRecommend() {
  if (!userId.value.trim()) {
    ElMessage.warning('请输入用户ID')
    return
  }

  loading.value = true
  searched.value = true
  recommendList.value = []

  try {
    const res = await getRecommend(userId.value)
    if (res.code === 200 && res.data) {
      recommendList.value = res.data
    } else {
      ElMessage.error(res.message || '获取推荐失败')
    }
  } catch (e) {
    console.error('获取推荐失败:', e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.recommend-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: transparent;
  border-radius: 8px;
  overflow: hidden;
}

.top-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #e4e7ed;
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

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.search-card {
  border-radius: 8px;
  margin-bottom: 20px;
}

/* Element Plus 组件半透明覆盖 */
:deep(.el-card) {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 10px;
}

.search-form {
  display: flex;
  align-items: center;
}

/* 推荐结果 */
.result-section {
  margin-top: 8px;
}

.result-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-count {
  font-size: 13px;
  font-weight: normal;
  color: #909399;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.recommend-card {
  border-radius: 8px;
  transition: box-shadow 0.2s, transform 0.2s;
  overflow: hidden;
  padding: 0;
}

.recommend-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

:deep(.recommend-card .el-card__body) {
  padding: 0;
}

/* 商品图片 */
.product-image-wrap {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: #f5f7fa;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  background: #f5f7fa;
}

.match-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  font-weight: 500;
}

/* 商品信息 */
.product-info {
  padding: 14px 16px 16px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-rating {
  margin-bottom: 8px;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.discount-price {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}

.original-price {
  font-size: 13px;
  color: #909399;
  text-decoration: line-through;
}

.product-price span:not(.discount-price):not(.original-price) {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.reason-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  color: #e6a23c;
  font-size: 13px;
}

.reason-text {
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 空状态 */
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

/* 加载中 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  gap: 12px;
  color: #909399;
}

.loading-state .el-icon {
  animation: rotating 1.5s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 14px;
}
</style>
