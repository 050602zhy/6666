<template>
  <div class="login-page" :style="bgStyle">
    <div class="login-card">
      <h2 class="title">智能电商运营平台</h2>
      <!-- 登录/注册 Tab 切换 -->
      <el-tabs v-model="activeTab" class="auth-tabs" @tab-click="clearForm">
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @keyup.enter="handleLogin">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" prefix-icon="User" placeholder="请输入用户名" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" prefix-icon="Lock" placeholder="请输入密码" type="password" show-password size="large" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="handleLogin">
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" @keyup.enter="handleRegister">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" prefix-icon="User" placeholder="请输入用户名" size="large" />
            </el-form-item>
            <el-form-item prop="nickname">
              <el-input v-model="registerForm.nickname" prefix-icon="UserFilled" placeholder="请输入昵称" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" prefix-icon="Lock" placeholder="请输入密码" type="password" show-password size="large" />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" prefix-icon="Lock" placeholder="请确认密码" type="password" show-password size="large" />
            </el-form-item>
            <el-form-item>
              <el-button type="success" size="large" :loading="loading" style="width: 100%" @click="handleRegister">
                注 册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/user'
import oceanBg from '@/assets/ocean-bg.jpg'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const bgStyle = { backgroundImage: `url(${oceanBg})` }

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', nickname: '', password: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== registerForm.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度2-20个字符', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, max: 20, message: '密码长度4-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

function clearForm() {
  loginForm.username = ''
  loginForm.password = ''
  registerForm.username = ''
  registerForm.nickname = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
}

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login(loginForm)
    if (res.code === 200) {
      localStorage.setItem('user', JSON.stringify(res.data))
      localStorage.setItem('token', 'mock-token-' + res.data.id)
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '登录失败，请检查网络')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await register({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname
    })
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      activeTab.value = 'login'
      loginForm.username = registerForm.username
      loginForm.password = ''
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '注册失败，请检查网络')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const user = localStorage.getItem('user')
  if (user) router.push('/')
})
</script>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: cover;
  background-position: center;
}

.login-card {
  width: 420px;
  padding: 36px 36px 20px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
}

.title {
  text-align: center;
  font-size: 22px;
  color: #1a5276;
  margin-bottom: 20px;
  font-weight: 700;
}

.auth-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}
.auth-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
}
</style>