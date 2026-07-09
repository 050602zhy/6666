<template>
  <div>
    <h1>智能推荐</h1>
    <el-card>
      <el-form :inline="true">
        <el-form-item label="用户ID">
          <el-input v-model="userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRecommend">获取推荐</el-button>
        </el-form-item>
      </el-form>
      <div v-if="recommendResult" class="result">
        <h3>推荐结果：</h3>
        <p>{{ recommendResult }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getRecommend } from '@/api/recommend'

const userId = ref('')
const recommendResult = ref('')

const handleRecommend = async () => {
  const res = await getRecommend(userId.value)
  recommendResult.value = res.data
}
</script>

<style scoped>
.result {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>
