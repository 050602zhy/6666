<template>
  <div>
    <h1>数据看板</h1>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <div class="stat-title">今日订单</div>
          <div class="stat-value">1,234</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-title">今日销售额</div>
          <div class="stat-value">￥56,789</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-title">商品总数</div>
          <div class="stat-value">3,456</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-title">用户总数</div>
          <div class="stat-value">12,345</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
</script>

<style scoped>
.stat-title {
  color: #999;
  font-size: 14px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-top: 10px;
}
</style>
