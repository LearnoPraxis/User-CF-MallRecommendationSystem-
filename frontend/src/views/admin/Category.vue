<template>
  <div class="admin-category-page">
    <n-card>
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <n-input v-model:value="keyword" placeholder="搜索分类名称" clearable style="width: 200px" />
        <n-select v-model:value="statusFilter" placeholder="状态" clearable style="width: 120px" :options="statusOptions" />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
        <n-button @click="handleReset">重置</n-button>
        <n-button type="success" @click="handleAdd(null)">添加一级分类</n-button>
      </div>
      <!-- 树形表格 -->
      <n-data-table
        :columns="columns"
        :data="treeData"
        :loading="loading"
        :scroll-x="1000"
        :pagination="false"
        :row-key="row => row.id"
        default-expand-all
      />
    </n-card>

    <!-- 添加/编辑分类弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 500px" :mask-closable="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80px">
        <n-form-item label="分类名称" path="name">
          <n-input v-model:value="form.name" placeholder="请输入分类名称" />
        </n-form-item>
        <n-form-item label="父级分类">
          <n-select
            v-model:value="form.parentId"
            placeholder="请选择父级分类"
            :options="parentOptions"
            :disabled="!!editId && hasChildren"
            clearable
          />
        </n-form-item>
        <n-form-item label="分类图标">
          <ImageUpload v-model="form.icon" :width="80" :height="80" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="form.sort" :min="0" placeholder="数字越小越靠前" style="width: 100%" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="form.status" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { h, ref, computed, onMounted } from 'vue'
import { NButton, NSpace, NImage, NTag, useMessage, useDialog } from 'naive-ui'
import { getAdminCategoryList, addCategory, updateCategory, deleteCategory, updateCategoryStatus } from '@/api/admin/product'
import { formatTime } from '@/utils/format'
import ImageUpload from '@/components/ImageUpload.vue'

const message = useMessage()
const dialog = useDialog()

// 列表相关
const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref(null)
const statusOptions = [{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]

// 构建树形数据
const treeData = computed(() => {
  let data = tableData.value
  // 筛选
  if (keyword.value || statusFilter.value !== null) {
    data = data.filter(item => {
      const matchKeyword = !keyword.value || item.name.includes(keyword.value)
      const matchStatus = statusFilter.value === null || item.status === statusFilter.value
      return matchKeyword && matchStatus
    })
    return data // 筛选时不构建树形，直接返回平铺数据
  }
  // 构建树形结构
  const map = {}
  const roots = []
  // 先按sort排序
  const sorted = [...data].sort((a, b) => (a.sort || 0) - (b.sort || 0))
  sorted.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  sorted.forEach(item => {
    if (item.parentId === 0 || !map[item.parentId]) {
      roots.push(map[item.id])
    } else {
      map[item.parentId].children.push(map[item.id])
    }
  })
  // 移除空的children
  const removeEmptyChildren = (nodes) => {
    nodes.forEach(node => {
      if (node.children && node.children.length === 0) {
        delete node.children
      } else if (node.children) {
        removeEmptyChildren(node.children)
      }
    })
  }
  removeEmptyChildren(roots)
  return roots
})

// 父级分类选项（只能选一级分类）
const parentOptions = computed(() => [
  { label: '无（一级分类）', value: 0 },
  ...tableData.value.filter(c => c.parentId === 0).map(c => ({ label: c.name, value: c.id }))
])

// 表格列定义
const columns = [
  { title: '分类名称', key: 'name', width: 200 },
  { title: '图标', key: 'icon', width: 80, render: row => row.icon ? h(NImage, { src: row.icon, width: 40, height: 40, objectFit: 'cover', previewDisabled: true }) : '-' },
  { title: '排序', key: 'sort', width: 80 },
  { title: '状态', key: 'status', width: 80, render: row => h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, () => row.status === 1 ? '启用' : '禁用') },
  { title: '创建时间', key: 'createTime', width: 170, render: row => formatTime(row.createTime) },
  { title: '操作', key: 'action', width: 220, fixed: 'right', render: row => h(NSpace, { size: 'small' }, () => [
    row.parentId === 0 ? h(NButton, { text: true, type: 'success', size: 'small', onClick: () => handleAdd(row.id) }, () => '添加子分类') : null,
    h(NButton, { text: true, type: 'primary', size: 'small', onClick: () => handleEdit(row) }, () => '编辑'),
    h(NButton, { text: true, type: row.status === 1 ? 'warning' : 'success', size: 'small', onClick: () => handleToggleStatus(row) }, () => row.status === 1 ? '禁用' : '启用'),
    h(NButton, { text: true, type: 'error', size: 'small', onClick: () => handleDelete(row) }, () => '删除')
  ])}
]

// 添加/编辑弹窗相关
const showModal = ref(false)
const editId = ref(null)
const hasChildren = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const form = ref({
  name: '',
  parentId: 0,
  icon: '',
  sort: 0,
  status: 1
})

const modalTitle = computed(() => {
  if (editId.value) return '编辑分类'
  if (form.value.parentId === 0) return '添加一级分类'
  return '添加子分类'
})

const rules = {
  name: { required: true, message: '请输入分类名称', trigger: 'blur' }
}

// 搜索
function handleSearch() {
  // 前端筛选
}

// 重置筛选
function handleReset() {
  keyword.value = ''
  statusFilter.value = null
}

// 获取列表数据
async function fetchData() {
  loading.value = true
  try {
    tableData.value = await getAdminCategoryList() || []
  } finally {
    loading.value = false
  }
}

// 添加分类
function handleAdd(parentId) {
  editId.value = null
  hasChildren.value = false
  form.value = {
    name: '',
    parentId: parentId || 0,
    icon: '',
    sort: 0,
    status: 1
  }
  showModal.value = true
}

// 编辑分类
function handleEdit(row) {
  editId.value = row.id
  // 检查是否有子分类
  hasChildren.value = tableData.value.some(c => c.parentId === row.id)
  form.value = {
    name: row.name,
    parentId: row.parentId,
    icon: row.icon || '',
    sort: row.sort,
    status: row.status
  }
  showModal.value = true
}

// 提交表单
async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitLoading.value = true
    if (editId.value) {
      await updateCategory({ id: editId.value, ...form.value })
      message.success('更新成功')
    } else {
      await addCategory(form.value)
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
  const action = newStatus === 1 ? '启用' : '禁用'
  dialog.warning({
    title: '提示',
    content: `确定${action}分类 "${row.name}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await updateCategoryStatus(row.id, newStatus)
      message.success('操作成功')
      fetchData()
    }
  })
}

// 删除分类
function handleDelete(row) {
  // 检查是否有子分类
  const hasChildrenData = tableData.value.some(c => c.parentId === row.id)
  if (hasChildrenData) {
    message.warning('该分类下有子分类，无法删除')
    return
  }
  dialog.warning({
    title: '提示',
    content: `确定删除分类 "${row.name}" 吗？此操作不可恢复！`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteCategory(row.id)
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
</style>
