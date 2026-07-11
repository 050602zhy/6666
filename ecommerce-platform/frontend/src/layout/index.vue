<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">智能电商运营平台</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#fff"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/product">
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/recommend">
          <span>智能推荐</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="header-title">智能电商运营平台</span>
        <div class="header-right">
          <span class="user-info">{{ currentUser.nickname || currentUser.username }}</span>
          <el-button type="danger" text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main" :style="mainBgStyle">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import mainBg from '@/assets/main-bg.jpg'

const router = useRouter()
const mainBgStyle = {
  backgroundImage: `url(${mainBg})`,
  backgroundSize: 'cover',
  backgroundPosition: 'center',
  backgroundAttachment: 'fixed'
}

const userStr = localStorage.getItem('user')
const currentUser = reactive(userStr ? JSON.parse(userStr) : { username: '未知', nickname: '' })

function handleLogout() {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
}

.header {
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 10;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  color: #606266;
  font-size: 14px;
}

.main {
  padding: 20px;
  position: relative;
}

/* 给主内容区加上半透明白色遮罩，保证内容可读 */
.main::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.82);
  border-radius: 8px;
  z-index: 0;
  pointer-events: none;
}

.main :deep(> *) {
  position: relative;
  z-index: 1;
}
</style>