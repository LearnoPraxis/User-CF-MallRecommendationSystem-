<template>
  <div class="admin-product-page">
    <n-card>
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <n-input v-model:value="keyword" placeholder="搜索商品名称" clearable style="width: 200px" />
        <n-select v-model:value="categoryId" placeholder="分类" clearable style="width: 150px" :options="categoryOptions" />
        <n-select v-model:value="status" placeholder="状态" clearable style="width: 120px" :options="statusOptions" />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
        <n-button @click="handleReset">重置</n-button>
        <n-button type="success" @click="handleAdd">添加商品</n-button>
      </div>
      <!-- 批量操作栏 -->
      <div class="batch-bar" v-if="checkedRowKeys.length > 0">
        <n-space align="center">
          <span>已选择 <n-text type="primary">{{ checkedRowKeys.length }}</n-text> 项</span>
          <n-button type="success" size="small" @click="handleBatchOnShelf">批量上架</n-button>
          <n-button type="warning" size="small" @click="handleBatchOffShelf">批量下架</n-button>
          <n-button type="error" size="small" @click="handleBatchDelete">批量删除</n-button>
          <n-button size="small" @click="checkedRowKeys = []">取消选择</n-button>
        </n-space>
      </div>
      <!-- 数据表格 -->
      <n-data-table 
        :columns="columns" 
        :data="tableData" 
        :loading="loading" 
        :scroll-x="1600" 
        :pagination="false"
        :row-key="row => row.id"
        v-model:checked-row-keys="checkedRowKeys"
      />
      <!-- 分页 -->
      <div class="pagination">
        <n-pagination v-model:page="page" :page-size="size" :item-count="total" show-quick-jumper @update:page="fetchData" />
      </div>
    </n-card>

    <!-- 添加/编辑商品弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="editId ? '编辑商品' : '添加商品'" style="width: 800px" :mask-closable="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80px">
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="商品名称" path="name">
              <n-input v-model:value="form.name" placeholder="请输入商品名称" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="商品分类" path="categoryId">
              <n-select v-model:value="form.categoryId" placeholder="请选择分类" :options="categoryOptions" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="商品价格" path="price">
              <n-input-number v-model:value="form.price" :min="0" :precision="2" placeholder="请输入价格" style="width: 100%">
                <template #prefix>¥</template>
              </n-input-number>
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="原价">
              <n-input-number v-model:value="form.originalPrice" :min="0" :precision="2" placeholder="请输入原价" style="width: 100%">
                <template #prefix>¥</template>
              </n-input-number>
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="库存数量" path="stock">
              <n-input-number v-model:value="form.stock" :min="0" placeholder="请输入库存" style="width: 100%" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="商品状态">
              <n-switch v-model:value="form.status" :checked-value="1" :unchecked-value="0">
                <template #checked>上架</template>
                <template #unchecked>下架</template>
              </n-switch>
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-form-item label="商品主图" path="mainImage">
          <ImageUpload v-model="form.mainImage" :width="120" :height="120" />
        </n-form-item>
        <n-form-item label="商品图片">
          <MultiImageUpload v-model="form.images" :max="5" />
        </n-form-item>
        <n-form-item label="商品描述">
          <n-input v-model:value="form.description" type="textarea" :rows="2" placeholder="请输入商品简短描述" />
        </n-form-item>
        <n-form-item label="商品详情">
          <n-input v-model:value="form.detail" type="textarea" :rows="4" placeholder="请输入商品详细介绍" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 查看详情弹窗 -->
    <n-modal v-model:show="showDetailModal" preset="card" title="商品详情" style="width: 700px">
      <n-descriptions :column="2" label-placement="left" bordered>
        <n-descriptions-item label="商品ID">{{ detailData.id }}</n-descriptions-item>
        <n-descriptions-item label="商品名称">{{ detailData.name }}</n-descriptions-item>
        <n-descriptions-item label="商品分类">{{ detailData.categoryName }}</n-descriptions-item>
        <n-descriptions-item label="上架用户">{{ detailData.userName || '-' }}</n-descriptions-item>
        <n-descriptions-item label="商品价格">¥{{ formatPrice(detailData.price) }}</n-descriptions-item>
        <n-descriptions-item label="原价">¥{{ formatPrice(detailData.originalPrice) }}</n-descriptions-item>
        <n-descriptions-item label="库存数量">{{ detailData.stock }}</n-descriptions-item>
        <n-descriptions-item label="销量">{{ detailData.sales }}</n-descriptions-item>
        <n-descriptions-item label="浏览次数">{{ detailData.viewCount }}</n-descriptions-item>
        <n-descriptions-item label="商品状态">
          <n-tag :type="detailData.status === 1 ? 'success' : 'default'">{{ detailData.status === 1 ? '上架' : '下架' }}</n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="创建时间">{{ formatTime(detailData.createTime) }}</n-descriptions-item>
        <n-descriptions-item label="更新时间">{{ formatTime(detailData.updateTime) }}</n-descriptions-item>
        <n-descriptions-item label="商品主图" :span="2">
          <n-image v-if="detailData.mainImage" :src="detailData.mainImage" width="100" />
          <span v-else>-</span>
        </n-descriptions-item>
        <n-descriptions-item label="商品图片" :span="2">
          <n-space v-if="detailData.images">
            <n-image v-for="(img, idx) in detailData.images.split(',')" :key="idx" :src="img" width="80" />
          </n-space>
          <span v-else>-</span>
        </n-descriptions-item>
        <n-descriptions-item label="商品描述" :span="2">{{ detailData.description || '-' }}</n-descriptions-item>
        <n-descriptions-item label="商品详情" :span="2">
          <div style="max-height: 200px; overflow-y: auto;">{{ detailData.detail || '-' }}</div>
        </n-descriptions-item>
      </n-descriptions>
    </n-modal>
  </div>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { NTag, NButton, NSpace, NImage, NText, useMessage, useDialog } from 'naive-ui'
import { getProductList, addProduct, updateProduct, deleteProduct, updateProductStatus, batchDeleteProduct, batchOnShelf, batchOffShelf } from '@/api/admin/product'
import { getCategoryList } from '@/api/category'
import { formatTime, formatPrice } from '@/utils/format'
import ImageUpload from '@/components/ImageUpload.vue'
import MultiImageUpload from '@/components/MultiImageUpload.vue'

const message = useMessage()
const dialog = useDialog()

// 列表相关
const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const categoryId = ref(null)
const status = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const categoryOptions = ref([])
const statusOptions = [{ label: '上架', value: 1 }, { label: '下架', value: 0 }]
const checkedRowKeys = ref([])

// 表格列定义
const columns = [
  { type: 'selection', fixed: 'left' },
  { title: 'ID', key: 'id', width: 70 },
  { title: '主图', key: 'mainImage', width: 80, render: row => row.mainImage ? h(NImage, { src: row.mainImage, width: 50, height: 50, objectFit: 'cover', previewDisabled: true }) : '-' },
  { title: '商品名称', key: 'name', width: 180, ellipsis: { tooltip: true } },
  { title: '分类', key: 'categoryName', width: 100 },
  { title: '上架用户', key: 'userName', width: 100, render: row => row.userName || '-' },
  { title: '价格', key: 'price', width: 100, render: row => h('span', { style: 'color: #f5222d; font-weight: bold;' }, '¥' + formatPrice(row.price)) },
  { title: '原价', key: 'originalPrice', width: 100, render: row => row.originalPrice ? h('span', { style: 'text-decoration: line-through; color: #999;' }, '¥' + formatPrice(row.originalPrice)) : '-' },
  { title: '库存', key: 'stock', width: 80, render: row => h('span', { style: row.stock <= 10 ? 'color: #f5222d;' : '' }, row.stock) },
  { title: '销量', key: 'sales', width: 80 },
  { title: '浏览', key: 'viewCount', width: 80 },
  { title: '状态', key: 'status', width: 80, render: row => h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, () => row.status === 1 ? '上架' : '下架') },
  { title: '创建时间', key: 'createTime', width: 170, render: row => formatTime(row.createTime) },
  { title: '操作', key: 'action', width: 200, fixed: 'right', render: row => h(NSpace, { size: 'small' }, () => [
    h(NButton, { text: true, type: 'info', size: 'small', onClick: () => handleDetail(row) }, () => '详情'),
    h(NButton, { text: true, type: 'primary', size: 'small', onClick: () => handleEdit(row) }, () => '编辑'),
    h(NButton, { text: true, type: row.status === 1 ? 'warning' : 'success', size: 'small', onClick: () => handleToggleStatus(row) }, () => row.status === 1 ? '下架' : '上架'),
    h(NButton, { text: true, type: 'error', size: 'small', onClick: () => handleDelete(row) }, () => '删除')
  ])}
]

// 添加/编辑弹窗相关
const showModal = ref(false)
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)
const form = ref({
  name: '',
  categoryId: null,
  price: null,
  originalPrice: null,
  stock: 0,
  mainImage: '',
  images: '',
  description: '',
  detail: '',
  status: 1
})

const rules = {
  name: { required: true, message: '请输入商品名称', trigger: 'blur' },
  categoryId: { required: true, type: 'number', message: '请选择商品分类', trigger: 'change' },
  price: { required: true, type: 'number', message: '请输入商品价格', trigger: 'blur' },
  stock: { required: true, type: 'number', message: '请输入库存数量', trigger: 'blur' },
  mainImage: { required: true, message: '请上传商品主图', trigger: 'change' }
}

// 详情弹窗相关
const showDetailModal = ref(false)
const detailData = ref({})

// 搜索
function handleSearch() {
  page.value = 1
  fetchData()
}

// 重置筛选
function handleReset() {
  keyword.value = ''
  categoryId.value = null
  status.value = null
  handleSearch()
}

// 获取列表数据
async function fetchData() {
  loading.value = true
  try {
    const res = await getProductList({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      categoryId: categoryId.value,
      status: status.value
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

// 获取分类列表
async function fetchCategories() {
  const list = await getCategoryList()
  categoryOptions.value = list.map(c => ({ label: c.name, value: c.id }))
}

// 添加商品
function handleAdd() {
  editId.value = null
  form.value = {
    name: '',
    categoryId: null,
    price: null,
    originalPrice: null,
    stock: 0,
    mainImage: '',
    images: '',
    description: '',
    detail: '',
    status: 1
  }
  showModal.value = true
}

// 编辑商品
function handleEdit(row) {
  editId.value = row.id
  form.value = {
    name: row.name,
    categoryId: row.categoryId,
    price: row.price,
    originalPrice: row.originalPrice,
    stock: row.stock,
    mainImage: row.mainImage || '',
    images: row.images || '',
    description: row.description || '',
    detail: row.detail || '',
    status: row.status
  }
  showModal.value = true
}

// 查看详情
function handleDetail(row) {
  detailData.value = row
  showDetailModal.value = true
}

// 提交表单
async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitLoading.value = true
    if (editId.value) {
      await updateProduct({ id: editId.value, ...form.value })
      message.success('更新成功')
    } else {
      await addProduct(form.value)
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
  const action = newStatus === 1 ? '上架' : '下架'
  dialog.warning({
    title: '提示',
    content: `确定${action}商品 "${row.name}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await updateProductStatus(row.id, newStatus)
      message.success('操作成功')
      fetchData()
    }
  })
}

// 删除商品
function handleDelete(row) {
  dialog.warning({
    title: '提示',
    content: `确定删除商品 "${row.name}" 吗？此操作不可恢复！`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteProduct(row.id)
      message.success('删除成功')
      fetchData()
    }
  })
}

// 批量上架
function handleBatchOnShelf() {
  dialog.warning({
    title: '提示',
    content: `确定上架选中的 ${checkedRowKeys.value.length} 个商品吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await batchOnShelf(checkedRowKeys.value)
      message.success('批量上架成功')
      checkedRowKeys.value = []
      fetchData()
    }
  })
}

// 批量下架
function handleBatchOffShelf() {
  dialog.warning({
    title: '提示',
    content: `确定下架选中的 ${checkedRowKeys.value.length} 个商品吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await batchOffShelf(checkedRowKeys.value)
      message.success('批量下架成功')
      checkedRowKeys.value = []
      fetchData()
    }
  })
}

// 批量删除
function handleBatchDelete() {
  dialog.warning({
    title: '警告',
    content: `确定删除选中的 ${checkedRowKeys.value.length} 个商品吗？此操作不可恢复！`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      await batchDeleteProduct(checkedRowKeys.value)
      message.success('批量删除成功')
      checkedRowKeys.value = []
      fetchData()
    }
  })
}

onMounted(() => {
  fetchData()
  fetchCategories()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.batch-bar {
  margin-bottom: 16px;
  padding: 10px 16px;
  background: #f5f7fa;
  border-radius: 4px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
