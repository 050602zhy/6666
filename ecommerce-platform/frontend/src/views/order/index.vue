<template>
  <div>
    <h1>订单管理</h1>
    <el-table :data="orderList" border>
      <el-table-column prop="id" label="订单号" width="180" />
      <el-table-column prop="userId" label="用户ID" />
      <el-table-column prop="totalAmount" label="订单金额" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作" width="200">
        <template #default>
          <el-button type="primary" size="small">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const orderList = ref([])
</script>
