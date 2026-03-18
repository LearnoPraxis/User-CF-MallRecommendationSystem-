<template>
  <div class="profile-page">
    <div class="page-container">
      <n-grid :cols="3" :x-gap="16">
        <!-- 左侧用户信息卡片 -->
        <n-gi>
          <n-card size="small">
            <div class="user-card">
              <n-avatar :size="80" :src="userInfo.avatar" round>
                <template #fallback>
                  <n-icon size="40"><PersonOutline /></n-icon>
                </template>
              </n-avatar>
              <div class="user-name">{{ userInfo.nickname || userInfo.username }}</div>
              <div class="user-id">ID: {{ userInfo.id }}</div>
              <n-divider />
              <div class="user-stats">
                <div class="stat-item" @click="router.push('/order')">
                  <div class="stat-value">{{ stats.orderCount }}</div>
                  <div class="stat-label">订单</div>
                </div>
                <div class="stat-item" @click="router.push('/favorite')">
                  <div class="stat-value">{{ stats.favoriteCount }}</div>
                  <div class="stat-label">收藏</div>
                </div>
                <div class="stat-item" @click="router.push('/history')">
                  <div class="stat-value">{{ stats.historyCount }}</div>
                  <div class="stat-label">足迹</div>
                </div>
              </div>
            </div>
          </n-card>
        </n-gi>
        <!-- 右侧表单 -->
        <n-gi :span="2">
          <n-card title="个人中心">
            <n-tabs type="line">
              <n-tab-pane name="info" tab="基本信息">
                <n-form ref="infoFormRef" :model="infoForm" label-placement="left" label-width="80" style="max-width: 500px">
                  <n-form-item label="头像">
                    <n-upload :custom-request="handleUpload" :show-file-list="false" accept="image/*">
                      <div class="avatar-upload">
                        <n-avatar :size="80" :src="infoForm.avatar" round style="cursor: pointer">
                          <template #fallback>
                            <n-icon size="40"><PersonOutline /></n-icon>
                          </template>
                        </n-avatar>
                        <div class="upload-tip">点击更换头像</div>
                      </div>
                    </n-upload>
                  </n-form-item>
                  <n-form-item label="用户名">
                    <n-input :value="userInfo.username" disabled />
                  </n-form-item>
                  <n-form-item label="昵称">
                    <n-input v-model:value="infoForm.nickname" placeholder="请输入昵称" maxlength="20" show-count />
                  </n-form-item>
                  <n-form-item label="性别">
                    <n-radio-group v-model:value="infoForm.gender">
                      <n-radio :value="0">保密</n-radio>
                      <n-radio :value="1">男</n-radio>
                      <n-radio :value="2">女</n-radio>
                    </n-radio-group>
                  </n-form-item>
                  <n-form-item label="邮箱">
                    <n-input v-model:value="infoForm.email" placeholder="请输入邮箱" />
                  </n-form-item>
                  <n-form-item label="手机号">
                    <n-input v-model:value="infoForm.phone" placeholder="请输入手机号" maxlength="11" />
                  </n-form-item>
                  <n-form-item label="注册时间">
                    <n-input :value="formatTime(userInfo.createTime)" disabled />
                  </n-form-item>
                  <n-form-item>
                    <n-button type="primary" :loading="saveLoading" @click="handleUpdateInfo">保存修改</n-button>
                  </n-form-item>
                </n-form>
              </n-tab-pane>
              <n-tab-pane name="password" tab="修改密码">
                <n-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-placement="left" label-width="80" style="max-width: 500px">
                  <n-form-item label="原密码" path="oldPassword">
                    <n-input v-model:value="pwdForm.oldPassword" type="password" placeholder="请输入原密码" show-password-on="click" />
                  </n-form-item>
                  <n-form-item label="新密码" path="newPassword">
                    <n-input v-model:value="pwdForm.newPassword" type="password" placeholder="请输入新密码（6-20位）" show-password-on="click" />
                  </n-form-item>
                  <n-form-item label="确认密码" path="confirmPassword">
                    <n-input v-model:value="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password-on="click" />
                  </n-form-item>
                  <n-form-item>
                    <n-button type="primary" :loading="pwdLoading" @click="handleUpdatePassword">修改密码</n-button>
                  </n-form-item>
                </n-form>
              </n-tab-pane>
            </n-tabs>
          </n-card>
        </n-gi>
      </n-grid>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { PersonOutline } from '@vicons/ionicons5'
import { useUserStore } from '@/store/user'
import { getUserInfo, updateUserInfo, updatePassword } from '@/api/user'
import { uploadFile } from '@/api/upload'
import { getOrderList } from '@/api/order'
import { getFavoriteList } from '@/api/favorite'
import { getHistoryList } from '@/api/history'
import { formatTime } from '@/utils/format'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const userInfo = ref({})
const stats = ref({ orderCount: 0, favoriteCount: 0, historyCount: 0 })

const infoFormRef = ref(null)
const infoForm = ref({ nickname: '', avatar: '', gender: 0, email: '', phone: '' })
const saveLoading = ref(false)

const pwdFormRef = ref(null)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdLoading = ref(false)
const pwdRules = {
  oldPassword: { required: true, message: '请输入原密码', trigger: 'blur' },
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value) => value === pwdForm.value.newPassword,
      message: '两次输入的密码不一致',
      trigger: 'blur'
    }
  ]
}

async function fetchUserInfo() {
  try {
    const user = await getUserInfo()
    userInfo.value = user
    infoForm.value = {
      nickname: user.nickname || '',
      avatar: user.avatar || '',
      gender: user.gender || 0,
      email: user.email || '',
      phone: user.phone || ''
    }
  } catch (e) {}
}

async function fetchStats() {
  try {
    const [orders, favorites, history] = await Promise.all([
      getOrderList({ page: 1, size: 1 }),
      getFavoriteList({ page: 1, size: 1 }),
      getHistoryList({ page: 1, size: 1 })
    ])
    stats.value = {
      orderCount: orders.total || 0,
      favoriteCount: favorites.total || 0,
      historyCount: history.total || 0
    }
  } catch (e) {}
}

async function handleUpload({ file }) {
  try {
    const url = await uploadFile(file.file)
    infoForm.value.avatar = url
    message.success('上传成功')
  } catch (e) {}
}

async function handleUpdateInfo() {
  saveLoading.value = true
  try {
    await updateUserInfo(infoForm.value)
    userStore.setUserInfo({ ...userStore.userInfo, ...infoForm.value })
    userInfo.value = { ...userInfo.value, ...infoForm.value }
    message.success('保存成功')
  } finally {
    saveLoading.value = false
  }
}

async function handleUpdatePassword() {
  try {
    await pwdFormRef.value?.validate()
    pwdLoading.value = true
    await updatePassword(pwdForm.value)
    message.success('密码修改成功')
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e) {
    // 验证失败
  } finally {
    pwdLoading.value = false
  }
}

onMounted(() => {
  if (!userStore.isLogin) {
    message.warning('请先登录')
    router.push('/login?redirect=/profile')
    return
  }
  fetchUserInfo()
  fetchStats()
})
</script>

<style scoped>
.profile-page { padding: 20px; }
.page-container { max-width: 1000px; margin: 0 auto; }

.user-card { text-align: center; padding: 20px 0; }
.user-name { font-size: 18px; font-weight: bold; margin-top: 12px; }
.user-id { font-size: 13px; color: #999; margin-top: 4px; }

.user-stats { display: flex; justify-content: space-around; }
.stat-item { cursor: pointer; padding: 8px; border-radius: 8px; transition: background 0.2s; }
.stat-item:hover { background: #f5f5f5; }
.stat-value { font-size: 20px; font-weight: bold; color: #18a058; }
.stat-label { font-size: 13px; color: #666; margin-top: 4px; }

.avatar-upload { text-align: center; }
.upload-tip { font-size: 12px; color: #999; margin-top: 8px; }
</style>
