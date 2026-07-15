<template>
  <div class="order-page">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <el-button class="back-btn" @click="$router.push('/dashboard')">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <span class="page-title">订单管理</span>
      <el-input
        v-model="searchKeyword"
        class="search-input"
        placeholder="搜索商品名称或订单ID..."
        clearable
        :prefix-icon="Search"
      />
    </div>

    <!-- 订单列表 -->
    <div class="content-area">
      <div class="table-card">
        <el-table
          :data="filteredOrders"
          border
          stripe
          style="width: 100%"
          height="calc(100vh - 220px)"
        >
          <el-table-column prop="orderNo" label="订单编号" width="120" />

          <el-table-column :label="userRole === 'buyer' ? '购买商品' : '买家所需商品'" min-width="150">
            <template #default="{ row }">
              {{ row.productName }}
            </template>
          </el-table-column>

          <el-table-column :label="userRole === 'buyer' ? '购买商家' : '购买用户'" min-width="120">
            <template #default="{ row }">
              {{ userRole === 'buyer' ? (row.sellerName || '商家' + row.sellerId) : (row.buyerName || '买家' + row.buyerId) }}
            </template>
          </el-table-column>

          <el-table-column label="购买日期" width="170">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>

          <el-table-column label="状态" width="180" align="center">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              <el-tag v-if="row.refundStatus === 1" type="warning" size="small" style="margin-left: 4px">退款中</el-tag>
              <el-tag v-else-if="row.refundStatus === 2" type="success" size="small" style="margin-left: 4px">已退款</el-tag>
              <el-tag v-else-if="row.refundStatus === 3" type="danger" size="small" style="margin-left: 4px">退款被拒</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="操作" min-width="200" align="center">
            <template #default="{ row }">
              <template v-if="userRole === 'buyer'">
                <el-button
                  type="warning"
                  size="small"
                  plain
                  :disabled="row.refundStatus === 1 || row.refundStatus === 2 || row.status === 3 || row.isRated === 1"
                  @click="openRefundDialog(row)"
                >
                  {{ row.refundStatus === 2 ? '已退款' : row.refundStatus === 1 ? '退款中' : '申请退货' }}
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  plain
                  :disabled="row.isRated === 1 || row.refundStatus === 1 || row.refundStatus === 2"
                  @click="openRateDialog(row)"
                >
                  {{ row.isRated === 1 ? '已评分' : '评分' }}
                </el-button>
              </template>
              <template v-if="userRole === 'seller'">
                <template v-if="row.refundStatus === 1">
                  <el-button type="success" size="small" plain @click="handleRefundAction(row, 1)">同意退款</el-button>
                  <el-button type="danger" size="small" plain @click="handleRefundAction(row, 2)">拒绝退款</el-button>
                </template>
                <template v-else-if="row.status === 1">
                  <el-button type="success" size="small" plain @click="handleShip(row)">同意出货</el-button>
                </template>
                <span v-else style="color: #909399; font-size: 13px">--</span>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <div class="scroll-tip">提示：一页最多显示3.5个订单，请滚动查看</div>
      </div>
    </div>

    <!-- 退货弹窗 -->
    <el-dialog v-model="refundDialogVisible" title="申请退货" width="400px" align-center>
      <el-form :model="refundForm" label-width="80px">
        <el-form-item label="订单编号">
          <span>{{ refundForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="refundForm.reason" type="textarea" :rows="3" placeholder="请输入退款原因..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 评分弹窗 -->
    <el-dialog v-model="rateDialogVisible" title="订单评分" width="400px" align-center>
      <el-form :model="rateForm" label-width="80px">
        <el-form-item label="订单编号">
          <span>{{ rateForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="星级评分">
          <el-rate v-model="rateForm.rating" show-score />
        </el-form-item>
        <el-form-item label="评论内容">
          <el-input v-model="rateForm.content" type="textarea" :rows="3" placeholder="请输入评论内容..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRate">提交评分</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, shipOrder, applyRefund, rateOrder, handleRefund } from '@/api/order'

const loading = ref(false)
const searchKeyword = ref('')
const orderList = ref([])

const refundDialogVisible = ref(false)
const refundForm = ref({ orderId: null, orderNo: '', reason: '' })

const rateDialogVisible = ref(false)
const rateForm = ref({ orderId: null, orderNo: '', rating: 5, content: '' })

const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
const userId = currentUser.id
const userRole = currentUser.role || 'buyer'

function statusType(status) {
  const map = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}
function statusText(status) {
  const map = { 0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

function formatDateTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  if (isNaN(date.getTime())) return time
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const filteredOrders = computed(() => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return orderList.value
  return orderList.value.filter(item => {
    const name = item.productName || ''
    const no = String(item.orderNo || '')
    return name.includes(keyword) || no.includes(keyword)
  })
})

async function loadOrders() {
  if (!userId) return
  loading.value = true
  try {
    const res = await getOrderList({ userId, role: userRole })
    if (res.code === 200 && Array.isArray(res.data)) {
      orderList.value = res.data
    } else {
      orderList.value = []
    }
  } catch (e) {
    console.error('加载订单列表失败', e)
    orderList.value = []
  } finally {
    loading.value = false
  }
}

function openRefundDialog(row) {
  refundForm.value = { orderId: row.id, orderNo: row.orderNo, reason: '' }
  refundDialogVisible.value = true
}

async function submitRefund() {
  if (!refundForm.value.reason.trim()) {
    ElMessage.warning('请输入退款原因')
    return
  }
  try {
    const res = await applyRefund({
      orderId: refundForm.value.orderId,
      reason: refundForm.value.reason
    })
    if (res.code === 200) {
      ElMessage.success('退货申请已提交')
      refundDialogVisible.value = false
      loadOrders()
    }
  } catch (e) {}
}

function openRateDialog(row) {
  if (row.isRated === 1) {
    ElMessage.warning('该订单已评分')
    return
  }
  rateForm.value = { orderId: row.id, orderNo: row.orderNo, rating: 5, content: '' }
  rateDialogVisible.value = true
}

async function submitRate() {
  if (!rateForm.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  try {
    const res = await rateOrder({
      orderId: rateForm.value.orderId,
      rating: rateForm.value.rating,
      content: rateForm.value.content
    })
    if (res.code === 200) {
      ElMessage.success('评分成功')
      rateDialogVisible.value = false
      loadOrders()
    }
  } catch (e) {}
}

async function handleShip(row) {
  try {
    await ElMessageBox.confirm('确认同意出货吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await shipOrder(row.id)
    if (res.code === 200) {
      ElMessage.success('已同意出货')
      loadOrders()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

/** 卖家处理退款：status=1 同意，status=2 拒绝 */
async function handleRefundAction(row, status) {
  const action = status === 1 ? '同意退款' : '拒绝退款'
  try {
    await ElMessageBox.confirm(`确认${action}吗？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await handleRefund({
      refundId: row.refundId,
      status: status,
      reply: status === 2 ? '卖家拒绝了退款申请' : ''
    })
    if (res.code === 200) {
      ElMessage.success(action + '成功')
      loadOrders()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-page {
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
  border-bottom: 1px solid rgba(255, 255, 255, 0.4);
  flex-shrink: 0;
}
.back-btn { flex-shrink: 0; }
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}
.search-input {
  flex: 1;
  max-width: 320px;
  margin-left: auto;
}
.content-area {
  flex: 1;
  padding: 20px;
  overflow: hidden;
}
.table-card {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.scroll-tip {
  text-align: center;
  padding: 10px;
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
}
:deep(.el-table) {
  background: transparent;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.5);
  --el-table-border-color: rgba(0, 0, 0, 0.06);
  --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.4);
}
:deep(.el-table th.el-table__cell) { background: rgba(255, 255, 255, 0.5) !important; }
:deep(.el-table td.el-table__cell) { background: transparent !important; }
:deep(.el-table__row:hover > td.el-table__cell) { background: rgba(255, 255, 255, 0.4) !important; }
:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: rgba(255, 255, 255, 0.25) !important;
}
:deep(.el-table__body-wrapper) { background: transparent; }
:deep(.el-table__inner-wrapper::before) { background-color: rgba(0, 0, 0, 0.06); }
:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
}
</style>
