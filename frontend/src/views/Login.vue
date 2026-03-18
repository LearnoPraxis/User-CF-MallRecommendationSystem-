<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <n-icon size="32" color="#18a058"><StorefrontOutline /></n-icon>
          <span>商城推荐系统</span>
        </div>
        <p class="subtitle">登录您的账号</p>
      </div>
      <n-form ref="formRef" :model="form" :rules="rules" size="large">
        <n-form-item path="username">
          <n-input v-model:value="form.username" placeholder="用户名">
            <template #prefix><n-icon :component="PersonOutline" /></template>
          </n-input>
        </n-form-item>
        <n-form-item path="password">
          <n-input v-model:value="form.password" type="password" placeholder="密码" show-password-on="click" @keyup.enter="handleLogin">
            <template #prefix><n-icon :component="LockClosedOutline" /></template>
          </n-input>
        </n-form-item>
        <n-form-item>
          <n-button type="primary" block size="large" :loading="loading" @click="handleLogin">登 录</n-button>
        </n-form-item>
      </n-form>
      <div class="login-footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/store/user'
import { login } from '@/api/auth'
import { StorefrontOutline, PersonOutline, LockClosedOutline } from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = ref({ username: '', password: '' })

const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    const data = await login(form.value)
    userStore.login(data)
    message.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    message.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  font-weight: 600;
  color: #333;
}

.subtitle {
  margin-top: 12px;
  font-size: 14px;
  color: #999;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.login-footer a {
  color: #18a058;
  text-decoration: none;
}

.login-footer a:hover {
  text-decoration: underline;
}

.page-footer {
  position: fixed;
  bottom: 20px;
  font-size: 12px;
  color: #999;
}
</style>
