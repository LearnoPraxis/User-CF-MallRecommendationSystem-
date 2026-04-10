<template>
  <div class="admin-user-page">
    <n-card>
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <n-input v-model:value="keyword" placeholder="搜索用户名/昵称/手机号" clearable style="width: 250px" />
        <n-select v-model:value="role" placeholder="角色" clearable style="width: 120px" :options="roleOptions" />
        <n-select v-model:value="status" placeholder="状态" clearable style="width: 120px" :options="statusOptions" />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
        <n-button @click="handleReset">重置</n-button>
        <n-button type="success" @click="handleAdd">添加用户</n-button>
      </div>
      <!-- 数据表格 -->
      <n-data-table :columns="columns" :data="tableData" :loading="loading" :scroll-x="1200" :pagination="false" />
      <!-- 分页 -->
      <div class="pagination">
        <n-pagination v-model:page="page" :page-size="size" :item-count="total" show-quick-jumper @update:page="fetchData" />
      </div>
    </n-card>

    <!-- 添加/编辑用户弹窗 -->
    <n-modal v-model:show="showModal" preset="dialog" :title="isEdit ? '编辑用户' : '添加用户'" style="width: 500px">
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="80px">
        <n-form-item label="头像">
          <AvatarUpload v-model="formData.avatar" :size="80" />
        </n-form-item>
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </n-form-item>
        <n-form-item v-if="!isEdit" label="密码" path="password">
          <n-input v-model:value="formData.password" type="password" placeholder="请输入密码" show-password-on="click" />
        </n-form-item>
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="formData.nickname" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="formData.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="formData.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="性别">
          <n-radio-group v-model:value="formData.gender">
            <n-radio :value="0">未知</n-radio>
            <n-radio :value="1">男</n-radio>
            <n-radio :value="2">女</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="角色">
          <n-select v-model:value="formData.role" :options="roleOptions" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="formData.status" :checked-value="1" :unchecked-value="0">
            <template #checked>正常</template>
            <template #unchecked>禁用</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <template #action>
        <n-button @click="showModal = false">取消</n-button>
        <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</n-button>
      </template>
    </n-modal>

    <!-- 重置密码弹窗 -->
    <n-modal v-model:show="showResetModal" preset="dialog" title="重置密码" style="width: 400px">
      <n-form ref="resetFormRef" :model="resetFormData" :rules="resetFormRules" label-placement="left" label-width="80px">
        <n-form-item label="新密码" path="newPassword">
          <n-input v-model:value="resetFormData.newPassword" type="password" placeholder="请输入新密码" show-password-on="click" />
        </n-form-item>
        <n-form-item label="确认密码" path="confirmPassword">
          <n-input v-model:value="resetFormData.confirmPassword" type="password" placeholder="请再次输入新密码" show-password-on="click" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-button @click="showResetModal = false">取消</n-button>
        <n-button type="primary" :loading="resetLoading" @click="handleResetPassword">确定</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { NTag, NButton, NSpace, NAvatar, useMessage, useDialog } from 'naive-ui'
import { getUserList, addUser, updateUser, updateUserStatus, resetPassword, deleteUser } from '@/api/admin/user'
import { formatTime } from '@/utils/format'
import AvatarUpload from '@/components/AvatarUpload.vue'

const message = useMessage()
const dialog = useDialog()

// 列表相关
const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const role = ref(null)
const status = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const roleOptions = [{ label: '普通用户', value: 0 }, { label: '管理员', value: 1 }]
const statusOptions = [{ label: '正常', value: 1 }, { label: '禁用', value: 0 }]
const genderMap = { 0: '未知', 1: '男', 2: '女' }

// 表格列定义
const columns = [
  { title: 'ID', key: 'id', width: 70, fixed: 'left' },
  { title: '头像', key: 'avatar', width: 70, render: row => h(NAvatar, { src: row.avatar, size: 'small', round: true }) },
  { title: '用户名', key: 'username', width: 120 },
  { title: '昵称', key: 'nickname', width: 120 },
  { title: '手机号', key: 'phone', width: 130 },
  { title: '邮箱', key: 'email', width: 180 },
  { title: '性别', key: 'gender', width: 70, render: row => genderMap[row.gender] || '未知' },
  { title: '角色', key: 'role', width: 100, render: row => h(NTag, { type: row.role === 1 ? 'warning' : 'default' }, () => row.role === 1 ? '管理员' : '普通用户') },
  { title: '状态', key: 'status', width: 80, render: row => h(NTag, { type: row.status === 1 ? 'success' : 'error' }, () => row.status === 1 ? '正常' : '禁用') },
  { title: '注册时间', key: 'createTime', width: 170, render: row => formatTime(row.createTime) },
  { title: '操作', key: 'action', width: 200, fixed: 'right', render: row => h(NSpace, null, () => [
    h(NButton, { text: true, type: 'info', onClick: () => handleEdit(row) }, () => '编辑'),
    h(NButton, { text: true, type: 'warning', onClick: () => openResetModal(row) }, () => '重置密码'),
    h(NButton, { text: true, type: row.status === 1 ? 'error' : 'success', disabled: row.role === 1, onClick: () => handleToggleStatus(row) }, () => row.status === 1 ? '禁用' : '启用'),
    h(NButton, { text: true, type: 'error', disabled: row.role === 1, onClick: () => handleDelete(row) }, () => '删除')
  ])}
]

// 添加/编辑弹窗相关
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)
const formData = ref({
  id: null,
  username: '',
  password: '',
  nickname: '',
  avatar: '',
  phone: '',
  email: '',
  gender: 0,
  role: 0,
  status: 1
})

const formRules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' },
  nickname: { required: true, message: '请输入昵称', trigger: 'blur' }
}

// 重置密码弹窗相关
const showResetModal = ref(false)
const resetFormRef = ref(null)
const resetLoading = ref(false)
const currentResetUserId = ref(null)
const resetFormData = ref({
  newPassword: '',
  confirmPassword: ''
})

const resetFormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: (rule, value) => value === resetFormData.value.newPassword, message: '两次输入的密码不一致', trigger: 'blur' }
  ]
}

// 搜索
function handleSearch() {
  page.value = 1
  fetchData()
}

// 重置筛选
function handleReset() {
  keyword.value = ''
  role.value = null
  status.value = null
  handleSearch()
}

// 获取列表数据
async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList({ page: page.value, size: size.value, keyword: keyword.value, role: role.value, status: status.value })
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

// 添加用户
function handleAdd() {
  isEdit.value = false
  formData.value = { id: null, username: '', password: '', nickname: '', avatar: '', phone: '', email: '', gender: 0, role: 0, status: 1 }
  showModal.value = true
}

// 编辑用户
function handleEdit(row) {
  isEdit.value = true
  formData.value = { ...row }
  showModal.value = true
}

// 提交表单
async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitLoading.value = true
    if (isEdit.value) {
      await updateUser(formData.value)
      message.success('更新成功')
    } else {
      await addUser(formData.value)
      message.success('添加成功')
    }
    showModal.value = false
    fetchData()
  } catch (e) {
    // 验证失败
  } finally {
    submitLoading.value = false
  }
}

// 切换状态
async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  dialog.warning({
    title: '提示',
    content: `确定${action}用户 ${row.username} 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await updateUserStatus(row.id, newStatus)
      message.success('操作成功')
      fetchData()
    }
  })
}

// 打开重置密码弹窗
function openResetModal(row) {
  currentResetUserId.value = row.id
  resetFormData.value = { newPassword: '', confirmPassword: '' }
  showResetModal.value = true
}

// 重置密码
async function handleResetPassword() {
  try {
    await resetFormRef.value?.validate()
    resetLoading.value = true
    await resetPassword(currentResetUserId.value, resetFormData.value.newPassword)
    message.success('密码重置成功')
    showResetModal.value = false
  } catch (e) {
    // 验证失败
  } finally {
    resetLoading.value = false
  }
}

// 删除用户
function handleDelete(row) {
  dialog.warning({
    title: '提示',
    content: `确定删除用户 ${row.username} 吗？此操作不可恢复！`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteUser(row.id)
      message.success('删除成功')
      fetchData()
    }
  })
}

onMounted(fetchData)
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
