<template>
  <el-container class="layout-container">
    <!-- 左侧图标侧边栏 -->
    <el-aside width="64px" class="icon-sidebar">
      <div class="logo-icon">
        <el-icon :size="24" color="#fff"><Shop /></el-icon>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="transparent"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#fff"
        class="icon-menu"
      >
        <el-menu-item index="/dashboard" title="数据看板">
          <el-icon :size="20"><DataAnalysis /></el-icon>
        </el-menu-item>
        <el-menu-item v-if="userRole === 'seller'" index="/product" title="商品管理">
          <el-icon :size="20"><Goods /></el-icon>
        </el-menu-item>
        <el-menu-item index="/order" title="订单管理">
          <el-icon :size="20"><List /></el-icon>
        </el-menu-item>
        <el-menu-item v-if="userRole === 'seller'" index="/activity" title="活动设置">
          <el-icon :size="20"><Present /></el-icon>
        </el-menu-item>
        <el-menu-item v-if="userRole === 'buyer'" index="/recommend" title="智能推荐">
          <el-icon :size="20"><MagicStick /></el-icon>
        </el-menu-item>
        <el-menu-item index="/profile" title="个人中心">
          <el-icon :size="20"><User /></el-icon>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="right-container">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <span class="header-title">智能电商运营平台</span>
        <div class="header-center">
          <el-autocomplete
            v-model="globalSearch"
            class="search-bar"
            placeholder="全局搜索商品..."
            :prefix-icon="Search"
            clearable
            :fetch-suggestions="handleSearch"
            :trigger-on-focus="false"
            @select="handleSearchSelect"
            style="width: 200px"
          >
            <template #default="{ item }">
              <div class="search-item">
                <span class="search-item-name">{{ item.name }}</span>
                <span class="search-item-price">¥{{ item.discountPrice || item.price }}</span>
              </div>
            </template>
          </el-autocomplete>
          <el-badge :value="3" :max="99" class="notify-badge">
            <el-icon :size="20" class="notify-icon"><Bell /></el-icon>
          </el-badge>
        </div>
        <div class="header-right">
          <el-avatar :size="32" class="user-avatar">{{ avatarText }}</el-avatar>
          <span class="user-info">{{ currentUser.nickname || currentUser.username }}</span>
          <el-button type="danger" text size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main">
        <router-view />
    </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Shop, DataAnalysis, Goods, List, Present, MagicStick, User, Bell } from '@element-plus/icons-vue'
import { getOnSaleList } from '@/api/product'

const router = useRouter()
const globalSearch = ref('')
const allProducts = ref([])

const userStr = localStorage.getItem('user')
const currentUser = reactive(userStr ? JSON.parse(userStr) : { username: '未知', nickname: '' })
const userRole = currentUser.role || 'buyer'

const avatarText = computed(() => {
  const name = currentUser.nickname || currentUser.username
  return name ? name.charAt(0).toUpperCase() : 'U'
})

/** 加载上架商品（用于全局搜索） */
async function loadProducts() {
  try {
    const res = await getOnSaleList()
    if (res.code === 200 && Array.isArray(res.data)) {
      allProducts.value = res.data
    }
  } catch (e) {}
}

/** 搜索建议 */
function handleSearch(queryString, cb) {
  if (!queryString || !queryString.trim()) {
    cb([])
    return
  }
  const keyword = queryString.trim().toLowerCase()
  const results = allProducts.value
    .filter(p => p.name && p.name.toLowerCase().includes(keyword))
    .slice(0, 8)
    .map(p => ({ value: p.name, id: p.id, name: p.name, price: p.price, discountPrice: p.discountPrice }))
  cb(results)
}

/** 选中搜索结果，跳转商品详情 */
function handleSearchSelect(item) {
  router.push(`/product/${item.id}`)
  globalSearch.value = ''
}

function handleLogout() {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  router.push('/login')
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background: transparent;
}

/* === 左侧图标侧边栏 - 半透明深色 === */
.icon-sidebar {
  background: linear-gradient(180deg, rgba(26, 31, 54, 0.92) 0%, rgba(45, 53, 97, 0.92) 100%);
  backdrop-filter: blur(14px);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 0;
  overflow: hidden;
}

.logo-icon {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-menu {
  border-right: none !important;
  background: transparent !important;
  width: 64px;
}

.icon-menu .el-menu-item {
  height: 48px;
  width: 48px;
  margin: 4px auto;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 !important;
}

.icon-menu .el-menu-item.is-active {
  background: rgba(255, 255, 255, 0.15) !important;
}

.icon-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.1) !important;
}

/* === 右侧容器 === */
.right-container {
  display: flex;
  flex-direction: column;
  position: relative;
  background: linear-gradient(to top, #fddb92 0%, #d1fdff 100%);
}

/* === 顶部导航栏 === */
.header {
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(20px);
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  padding: 0 24px;
  height: 60px;
  flex-shrink: 0;
  z-index: 10;
}

.header-title {
  font-size: 17px;
  font-weight: 700;
  color: #1a1f36;
  white-space: nowrap;
}

.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.search-bar {
  width: 200px !important;
}
.search-bar :deep(.el-input) {
  width: 200px !important;
}

.search-bar :deep(.el-input__wrapper) {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(8px);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.4) inset;
  border: 1px solid rgba(255, 255, 255, 0.5);
}
.search-bar :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.6) inset;
}
.search-bar :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
  background: rgba(255, 255, 255, 0.65);
}
.search-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-item-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.search-item-price {
  margin-left: 16px;
  color: #f56c6c;
  font-weight: 600;
  flex-shrink: 0;
}

.notify-badge {
  cursor: pointer;
}

.notify-icon {
  color: #606266;
  cursor: pointer;
}

.notify-icon:hover {
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.user-info {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

/* === 主内容区 === */
.main {
  padding: 20px 24px;
  overflow-y: auto;
  position: relative;
}
</style>