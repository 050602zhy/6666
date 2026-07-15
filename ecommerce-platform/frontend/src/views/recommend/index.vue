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
              <el-button type="primary" @click="handleRecommend">
                <el-icon><Search /></el-icon>
                获取推荐
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>

      <!-- 推荐结果 -->
      <div v-if="recommendList.length > 0" class="result-section">
        <div class="result-title">为您推荐以下商品</div>
        <div class="recommend-grid">
          <el-card
            v-for="item in recommendList"
            :key="item.id"
            shadow="hover"
            class="recommend-card"
          >
            <div class="card-header">
              <span class="product-name">{{ item.productName }}</span>
              <el-tag :type="getMatchType(item.matchDegree)" size="small">
                {{ item.matchDegree }}% 匹配
              </el-tag>
            </div>
            <div class="card-body">
              <div class="reason-label">推荐理由：</div>
              <div class="reason-text">{{ item.reason }}</div>
            </div>
            <div class="card-footer">
              <el-progress
                :percentage="item.matchDegree"
                :color="getMatchColor(item.matchDegree)"
                :stroke-width="8"
                :show-text="false"
              />
            </div>
          </el-card>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="请输入用户ID获取个性化推荐" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ArrowLeft, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const userId = ref('')
const recommendList = ref([])

function getMatchType(degree) {
  if (degree >= 80) return 'success'
  if (degree >= 60) return 'primary'
  if (degree >= 40) return 'warning'
  return 'info'
}

function getMatchColor(degree) {
  if (degree >= 80) return '#67c23a'
  if (degree >= 60) return '#409eff'
  if (degree >= 40) return '#e6a23c'
  return '#909399'
}

function handleRecommend() {
  if (!userId.value.trim()) {
    ElMessage.warning('请输入用户ID')
    return
  }
  // Mock 推荐数据
  recommendList.value = [
    {
      id: 1,
      productName: '高端蓝牙降噪耳机 Pro',
      reason: '基于您近期浏览的音频设备记录，以及同类型用户的购买偏好分析，该商品与您的兴趣高度匹配。',
      matchDegree: 92
    },
    {
      id: 2,
      productName: '智能手表 Ultra 运动版',
      reason: '您的历史订单中包含运动配件类商品，结合您的消费能力评估，推荐此款智能手表。',
      matchDegree: 78
    },
    {
      id: 3,
      productName: '4K超清投影仪',
      reason: '根据您浏览的电子产品品类，以及平台上与您画像相似用户的购买行为，为您推荐此款投影仪。',
      matchDegree: 65
    }
  ]
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
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.recommend-card {
  border-radius: 8px;
  transition: box-shadow 0.2s;
}

.recommend-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-body {
  margin-bottom: 16px;
}

.reason-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.reason-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.card-footer {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* 空状态 */
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}
</style>