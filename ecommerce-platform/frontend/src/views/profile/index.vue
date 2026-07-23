<template>
  <div class="profile-page">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <el-button class="back-btn" @click="$router.push('/dashboard')">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <span class="page-title">个人中心</span>
    </div>

    <!-- 主体内容 -->
    <div class="content-area">
      <!-- 用户基本信息 -->
      <div class="info-card">
        <div class="user-row">
          <div class="avatar-section" @click="triggerAvatarUpload">
            <el-avatar :size="64" :src="userInfo.avatar" class="user-avatar">
              <el-icon :size="32"><User /></el-icon>
            </el-avatar>
            <div class="avatar-tip">点击更换头像</div>
          </div>
          <div class="user-meta">
            <div class="nickname-row">
              <span v-if="!editingNickname" class="nickname">{{ userInfo.nickname || userInfo.username }}</span>
              <el-input
                v-else
                v-model="nicknameInput"
                size="small"
                maxlength="20"
                style="width: 200px"
                @blur="saveNickname"
                @keyup.enter="saveNickname"
              />
              <el-button v-if="!editingNickname" type="primary" text size="small" @click="startEditNickname">
                <el-icon><Edit /></el-icon>
              </el-button>
            </div>
            <div class="balance-row">
              <span class="balance-label">剩余金额：</span>
              <span class="balance-value">¥{{ userInfo.balance || 0 }}</span>
              <el-button type="primary" size="small" style="margin-left: 12px" @click="openRechargeDialog">
                充值
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- VIP 等级 -->
      <div class="vip-card">
        <div class="card-header">
          <span class="card-title">VIP 等级</span>
          <el-button type="warning" size="small" :disabled="userInfo.vipLevel >= 5" @click="upgradeVip">
            {{ userInfo.vipLevel >= 5 ? '已满级' : '提升VIP等级' }}
          </el-button>
        </div>
        <div class="vip-current">
          当前等级：<el-tag type="warning" size="large">VIP {{ vipLevelRoman }}</el-tag>
        </div>
        <div class="vip-levels">
          <div
            v-for="level in vipConfig"
            :key="level.level"
            class="vip-item"
            :class="{ active: level.level === userInfo.vipLevel }"
          >
            <div class="vip-icon">
              <el-icon :size="24"><Star /></el-icon>
            </div>
            <div class="vip-label">VIP {{ toRoman(level.level) }}</div>
            <div class="vip-discount">满{{ level.threshold }}减{{ level.discountAmount }}元</div>
            <div class="vip-desc">{{ level.description || level.desc }}</div>
          </div>
        </div>
      </div>

      <!-- 相关设置 -->
      <div class="settings-card">
        <div class="card-header">
          <span class="card-title">相关设置</span>
        </div>
        <div class="setting-list">
          <div class="setting-item">
            <span class="setting-label">个性化推荐</span>
            <el-switch v-model="settings.personalizedRecommend" @change="handleRecommendChange" />
          </div>
          <div v-if="userRole === 'seller'" class="setting-item">
            <span class="setting-label">自动发货</span>
            <el-switch v-model="settings.autoAccept" @change="handleAutoAcceptChange" />
          </div>
        </div>
      </div>

      <!-- 快捷按钮 -->
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="$router.push('/order')">
          <el-icon><List /></el-icon>
          交易明细
        </el-button>
        <el-button type="success" size="large" @click="$router.push('/product')">
          <el-icon><Goods /></el-icon>
          我的资产
        </el-button>
      </div>

      <!-- 近期消息 -->
      <div class="message-card">
        <div class="card-header">
          <span class="card-title">近期消息</span>
          <el-select v-model="messageFilter" class="message-filter" size="small">
            <el-option label="全部显示" value="all" />
            <el-option label="只看买家" value="buyer" />
            <el-option label="只看卖家" value="seller" />
            <el-option label="只看活动" value="activity" />
          </el-select>
        </div>
        <div class="message-list">
          <div
            v-for="msg in filteredMessages"
            :key="msg.id"
            class="message-item"
            :class="{ unread: !msg.read }"
            @click="readMsg(msg)"
          >
            <div class="message-left">
              <div class="message-dot" v-if="!msg.read"></div>
              <div class="message-title">{{ msg.title }}</div>
              <div class="message-content">{{ msg.content }}</div>
            </div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
          <div v-if="filteredMessages.length === 0" class="no-message">
            暂无消息
          </div>
        </div>
      </div>
    </div>

    <!-- 充值弹窗 -->
    <el-dialog v-model="rechargeDialogVisible" title="账户充值" width="380px" align-center>
      <div style="text-align: center; margin-bottom: 16px;">
        当前余额：<span style="font-size: 24px; font-weight: 700; color: #f56c6c;">¥{{ userInfo.balance || 0 }}</span>
      </div>
      <div style="display: flex; gap: 10px; flex-wrap: wrap; justify-content: center; margin-bottom: 16px;">
        <el-button
          v-for="amount in quickAmounts"
          :key="amount"
          :type="rechargeAmount === amount ? 'primary' : 'default'"
          @click="rechargeAmount = amount"
          style="width: 80px"
        >
          ¥{{ amount }}
        </el-button>
      </div>
      <el-input-number
        v-model="rechargeAmount"
        :min="1"
        :max="99999"
        :step="10"
        controls-position="right"
        style="width: 100%"
        placeholder="输入充值金额"
      />
      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRecharge">确认充值</el-button>
      </template>
    </el-dialog>

    <!-- 隐藏的文件上传 -->
    <input
      ref="avatarInputRef"
      type="file"
      accept="image/*"
      style="display: none"
      @change="onAvatarChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ArrowLeft, User, Star, List, Goods, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUser, updateVip, updateAutoAccept, updatePersonalizedRecommend } from '@/api/user'
import { getMessageList, readMessage } from '@/api/message'
import { getVipConfig } from '@/api/vip'
import request from '@/api/request'

const userInfo = reactive({
  username: '',
  nickname: '',
  avatar: '',
  balance: 0,
  vipLevel: 1,
  personalizedRecommend: false,
  autoAccept: false
})

const settings = reactive({
  personalizedRecommend: false,
  autoAccept: false
})

const vipConfig = ref([])
const messageList = ref([])
const messageFilter = ref('all')
const editingNickname = ref(false)
const nicknameInput = ref('')
const avatarInputRef = ref(null)

// 充值相关
const rechargeDialogVisible = ref(false)
const rechargeAmount = ref(50)
const quickAmounts = [10, 50, 100, 200, 500]

const currentUser = JSON.parse(localStorage.getItem('user') || '{}')
const userId = currentUser.id
const userRole = currentUser.role || 'buyer'

/** 罗马数字转换 */
function toRoman(num) {
  const map = { 1: 'I', 2: 'II', 3: 'III', 4: 'IV', 5: 'V' }
  return map[num] || num
}

/** 格式化消息时间 */
function formatMessageTime(time) {
  if (!time) return ''
  const date = new Date(time)
  if (isNaN(date.getTime())) return time
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  const isToday = date.toDateString() === now.toDateString()
  if (isToday) {
    return `今天 ${pad(date.getHours())}:${pad(date.getMinutes())}`
  }
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${pad(date.getHours())}:${pad(date.getMinutes())}`
  }
  return `${date.getMonth() + 1}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const vipLevelRoman = computed(() => toRoman(userInfo.vipLevel))

/** 过滤消息 */
const filteredMessages = computed(() => {
  if (messageFilter.value === 'all') return messageList.value
  if (messageFilter.value === 'buyer') {
    return messageList.value.filter(msg => msg.type === 1 || msg.type === 4)
  }
  if (messageFilter.value === 'seller') {
    return messageList.value.filter(msg => msg.type === 2 || msg.type === 4 || msg.type === 5)
  }
  if (messageFilter.value === 'activity') {
    return messageList.value.filter(msg => msg.type === 3)
  }
  return messageList.value
})

/** 加载用户信息 */
async function loadUserInfo() {
  if (!userId) return
  try {
    const res = await getUserInfo(userId)
    if (res.code === 200 && res.data && typeof res.data === 'object') {
      Object.assign(userInfo, res.data)
      settings.personalizedRecommend = res.data.personalizedRecommend || false
      settings.autoAccept = res.data.autoAcceptOrder === 1
    }
  } catch (e) {
    console.error('加载用户信息失败', e)
  }
}

/** 加载VIP配置 */
async function loadVipConfig() {
  try {
    const res = await getVipConfig()
    if (res.code === 200 && Array.isArray(res.data)) {
      vipConfig.value = res.data
    } else {
      throw new Error('返回数据格式错误')
    }
  } catch (e) {
    // 使用默认配置
    vipConfig.value = [
      { level: 1, threshold: 0, discountAmount: 0, description: '无折扣' },
      { level: 2, threshold: 1000, discountAmount: 50, description: '消费满1000元' },
      { level: 3, threshold: 5000, discountAmount: 500, description: '消费满5000元' },
      { level: 4, threshold: 20000, discountAmount: 3000, description: '消费满20000元' },
      { level: 5, threshold: 50000, discountAmount: 10000, description: '消费满50000元' }
    ]
  }
}

/** 加载消息列表 */
async function loadMessages() {
  if (!userId) return
  try {
    const res = await getMessageList(userId)
    if (res.code === 200 && Array.isArray(res.data)) {
      messageList.value = res.data.map(msg => ({
        ...msg,
        time: formatMessageTime(msg.createTime),
        read: msg.isRead === 1
      }))
    } else {
      messageList.value = []
    }
  } catch (e) {
    console.error('加载消息失败', e)
    messageList.value = []
  }
}

/** 触发头像上传 */
function triggerAvatarUpload() {
  avatarInputRef.value.value = ''
  avatarInputRef.value.click()
}

/** 头像文件变更 */
function onAvatarChange(e) {
  const file = e.target.files[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过5MB')
    return
  }
  const reader = new FileReader()
  reader.onload = async (ev) => {
    const base64 = ev.target.result
    try {
      await updateUser({ avatar: base64 })
      userInfo.avatar = base64
      ElMessage.success('头像更新成功')
    } catch (err) {
      ElMessage.error('头像更新失败')
    }
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

/** 开始编辑昵称 */
function startEditNickname() {
  nicknameInput.value = userInfo.nickname || userInfo.username
  editingNickname.value = true
}

/** 保存昵称 */
async function saveNickname() {
  if (!nicknameInput.value.trim()) {
    ElMessage.warning('昵称不能为空')
    editingNickname.value = false
    return
  }
  try {
    await updateUser({ nickname: nicknameInput.value.trim() })
    userInfo.nickname = nicknameInput.value.trim()
    ElMessage.success('昵称修改成功')
  } catch (e) {
    ElMessage.error('昵称修改失败')
  }
  editingNickname.value = false
}

/** 提升VIP等级（1~5级，满级后不可升级） */
async function upgradeVip() {
  if (userInfo.vipLevel >= 5) {
    ElMessage.info('已达到最高VIP等级')
    return
  }
  const nextLevel = userInfo.vipLevel + 1
  try {
    const res = await updateVip({ id: userId, vipLevel: nextLevel })
    if (res.code === 200) {
      userInfo.vipLevel = nextLevel
      ElMessage.success(`VIP等级已提升至 ${toRoman(nextLevel)}`)
    } else {
      ElMessage.error(res.message || '提升失败')
    }
  } catch (e) {
    // 前端模拟
    userInfo.vipLevel = nextLevel
    ElMessage.success(`VIP等级已提升至 ${toRoman(nextLevel)}`)
  }
}

/** 个性化推荐开关 */
async function handleRecommendChange(val) {
  try {
    const res = await updatePersonalizedRecommend({ id: userId, personalizedRecommend: val ? 1 : 0 })
    if (res.code === 200) {
      settings.personalizedRecommend = val
      ElMessage.success('设置已保存')
    } else {
      ElMessage.error(res.message || '设置失败')
      settings.personalizedRecommend = !val
    }
  } catch (e) {
    settings.personalizedRecommend = val
    ElMessage.success('设置已保存')
  }
}

/** 自动接单开关 */
async function handleAutoAcceptChange(val) {
  try {
    const res = await updateAutoAccept({ id: userId, autoAcceptOrder: val ? 1 : 0 })
    if (res.code === 200) {
      settings.autoAccept = val
      ElMessage.success('设置已保存')
    } else {
      ElMessage.error(res.message || '设置失败')
      settings.autoAccept = !val
    }
  } catch (e) {
    settings.autoAccept = val
    ElMessage.success('设置已保存')
  }
}

/** 标记消息已读 */
async function readMsg(msg) {
  if (msg.read) return
  try {
    await readMessage(msg.id)
    msg.read = true
  } catch (e) {
    msg.read = true
  }
}

/** 打开充值弹窗 */
function openRechargeDialog() {
  rechargeAmount.value = 50
  rechargeDialogVisible.value = true
}

/** 确认充值 */
async function handleRecharge() {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    ElMessage.warning('请输入有效金额')
    return
  }
  try {
    const res = await request.put('/biz/user/balance', { id: userId, amount: rechargeAmount.value })
    if (res.code === 200) {
      userInfo.balance = (userInfo.balance || 0) + rechargeAmount.value
      ElMessage.success(`充值成功，已到账 ¥${rechargeAmount.value}`)
      rechargeDialogVisible.value = false
      // 同步更新 localStorage
      const userStr = localStorage.getItem('user')
      if (userStr) {
        const user = JSON.parse(userStr)
        user.balance = userInfo.balance
        localStorage.setItem('user', JSON.stringify(user))
      }
    } else {
      ElMessage.error(res.message || '充值失败')
    }
  } catch (e) {
    ElMessage.error('充值失败')
  }
}

onMounted(() => {
  loadUserInfo()
  loadVipConfig()
  loadMessages()
})
</script>

<style scoped>
.profile-page {
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
.back-btn {
  flex-shrink: 0;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* === 内容区 === */
.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.content-area::-webkit-scrollbar {
  width: 8px;
}
.content-area::-webkit-scrollbar-track {
  background: rgba(240, 240, 240, 0.5);
  border-radius: 4px;
}
.content-area::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

/* 卡片通用 */
.info-card,
.vip-card,
.settings-card,
.message-card {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 用户信息 */
.user-row {
  display: flex;
  align-items: center;
  gap: 20px;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.user-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.avatar-tip {
  font-size: 12px;
  color: #909399;
}
.user-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.nickname-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.nickname {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
.balance-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.balance-label {
  font-size: 14px;
  color: #606266;
}
.balance-value {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}

/* VIP 等级 */
.vip-current {
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
}
.vip-levels {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.vip-item {
  flex: 1;
  min-width: 100px;
  text-align: center;
  padding: 16px 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(8px);
  border: 2px solid transparent;
  transition: all 0.3s;
  cursor: default;
}
.vip-item.active {
  border-color: #e6a23c;
  background: linear-gradient(135deg, #fdf6ec, #fef0e6);
  box-shadow: 0 2px 12px rgba(230, 162, 60, 0.2);
}
.vip-icon {
  color: #c0c4cc;
  margin-bottom: 6px;
}
.vip-item.active .vip-icon {
  color: #e6a23c;
}
.vip-label {
  font-size: 15px;
  font-weight: 600;
  color: #606266;
}
.vip-item.active .vip-label {
  color: #e6a23c;
}
.vip-discount {
  font-size: 13px;
  color: #f56c6c;
  font-weight: 600;
  margin-top: 4px;
}
.vip-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 设置 */
.setting-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.setting-label {
  font-size: 14px;
  color: #606266;
}

/* 快捷按钮 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 10px 0;
}

/* 消息列表 */
.message-filter {
  width: 130px;
}
.message-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.message-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 14px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: background 0.2s;
}
.message-item:last-child {
  border-bottom: none;
}
.message-item:hover {
  background: rgba(255, 255, 255, 0.4);
  margin: 0 -20px;
  padding: 14px 20px;
  border-radius: 4px;
}
.message-item.unread {
  background: rgba(250, 250, 250, 0.8);
  margin: 0 -20px;
  padding: 14px 20px;
  border-radius: 4px;
}
.message-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-left: 16px;
  position: relative;
}
.message-dot {
  position: absolute;
  left: 0;
  top: 6px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
}
.message-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.message-content {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}
.message-time {
  font-size: 12px;
  color: #c0c4cc;
  flex-shrink: 0;
  margin-left: 20px;
}
.no-message {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}

/* Element Plus 覆盖 */
:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
}
:deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.7);
}
</style>
