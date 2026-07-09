<template>
  <div>
    <h1>商品管理</h1>
    <el-table :data="productList" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="price" label="价格" />
      <el-table-column prop="stock" label="库存" />
      <el-table-column label="操作" width="200">
        <template #default>
          <el-button type="primary" size="small">编辑</el-button>
          <el-button type="danger" size="small">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProductList } from '@/api/product'

const productList = ref([])

onMounted(async () => {
  const res = await getProductList()
  productList.value = res.data || []
})
</script>
