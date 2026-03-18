<template>
  <div class="register-page">
    <div class="register-box">
      <div class="register-header">
        <div class="logo">
          <n-icon size="32" color="#18a058"><StorefrontOutline /></n-icon>
          <span>商城推荐系统</span>
        </div>
        <p class="subtitle">创建新账号</p>
      </div>
      <n-form ref="formRef" :model="form" :rules="rules" size="large">
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item path="username">
              <n-input v-model:value="form.username" placeholder="用户名（3-20个字符）">
                <template #prefix><n-icon :component="PersonOutline" /></template>
              </n-input>
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item path="nickname">
              <n-input v-model:value="form.nickname" placeholder="昵称（选填）">
                <template #prefix><n-icon :component="HappyOutline" /></template>
              </n-input>
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item path="password">
              <n-input v-model:value="form.password" type="password" placeholder="密码（6-20个字符）" show-password-on="click">
                <template #prefix><n-icon :component="LockClosedOutline" /></template>
              </n-input>
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item path="confirmPassword">
              <n-input v-model:value="form.confirmPassword" type="password" placeholder="确认密码" show-password-on="click">
                <template #prefix><n-icon :component="LockClosedOutline" /></template>
              </n-input>
            </n-form-item>
          </n-gi>
          <n-gi :span="2">
            <n-form-item path="phone">
              <n-input v-model:value="form.phone" placeholder="手机号（选填）" maxlength="11">
                <template #prefix><n-icon :component="CallOutline" /></template>
              </n-input>
            </n-form-item>
          </n-gi>
          <n-gi :span="2">
            <n-form-item>
              <n-button type="primary" block size="large" :loading="loading" @click="handleRegister">注 册</n-button>
            </n-form-item>
          </n-gi>
        </n-grid>
      </n-form>
      <div class="register-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { register } from '@/api/auth'
import { StorefrontOutline, PersonOutline, LockClosedOutline, HappyOutline, CallOutline } from '@vicons/ionicons5'

const router = useRouter()
const message = useMessage()

const formRef = ref(null)
const loading = ref(false)
const form = ref({ username: '', password: '', confirmPassword: '', nickname: '', phone: '' })

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value) => value === form.value.password,
      message: '两次输入的密码不一致',
      trigger: 'blur'
    }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

async function handleRegister() {
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await register({
      username: form.value.username,
      password: form.value.password,
      nickname: form.value.nickname,
      phone: form.value.phone
    })
    message.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    message.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.register-box {
  width: 500px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.register-header {
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

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.register-footer a {
  color: #18a058;
  text-decoration: none;
}

.register-footer a:hover {
  text-decoration: underline;
}

.page-footer {
  position: fixed;
  bottom: 20px;
  font-size: 12px;
  color: #999;
}
</style>
