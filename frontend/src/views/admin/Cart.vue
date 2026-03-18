<template>
  <div class="admin-cart-page">
    <n-card>
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <n-input v-model:value="userName" placeholder="用户名" clearable style="width: 150px" />
        <n-input v-model:value="productName" placeholder="商品名称" clearable style="width: 180px" />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
        <n-button @click="handleReset">重置</n-button>
        <n-button type="error" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</n-button>
      </div>
      <!-- 数据表格 -->
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1300"
        :pagination="false"
        :row-key="row => row.id"
        @update:checked-row-keys="handleCheck"
      />
      <!-- 分页 -->
      <div class="pagination">
        <n-pagination v-model:page="page" :page-size="size" :item-count="total" show-quick-jumper @update:page="fetchData" />
      </div>
    </n-card>

    <!-- 商品详情弹窗 -->
    <n-modal v-model:show="showProductModal" preset="card" title="商品信息" style="width: 500px">
      <template v-if="currentProduct">
        <div class="product-detail">
          <n-image v-if="currentProduct.mainImage" :src="currentProduct.mainImage" width="200" height="200" object-fit="cover" />
          <n-descriptions :column="1" label-placement="left" bordered size="small" style="margin-top: 16px;">
            <n-descriptions-item label="商品ID">{{ currentProduct.id }}</n-descriptions-item>
            <n-descriptions-item label="商品名称">{{ currentProduct.name }}</n-descriptions-item>
            <n-descriptions-item label="商品价格">
              <span style="color: #f5222d; font-weight: bold;">¥{{ formatPrice(currentProduct.price) }}</span>
              <span v-if="currentProduct.originalPrice" style="text-decoration: line-through; color: #999; margin-left: 8px;">
                ¥{{ formatPrice(currentProduct.originalPrice) }}
              </span>
            </n-descriptions-item>
            <n-descriptions-item label="库存数量">
              <span :style="currentProduct.stock <= 10 ? 'color: #f5222d;' : ''">{{ currentProduct.stock }}</span>
            </n-descriptions-item>
            <n-descriptions-item label="商品状态">
              <n-tag :type="currentProduct.status === 1 ? 'success' : 'default'" size="small">
                {{ currentProduct.status === 1 ? '上架' : '下架' }}
              </n-tag>
            </n-descriptions-item>
          </n-descriptions>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { NTag, NButton, NSpace, NImage, NCheckbox, useMessage, useDialog } from 'naive-ui'
import { getCartList, deleteCartItem, batchDeleteCartItems, clearUserCart } from '@/api/admin/cart'
import { formatTime, formatPrice } from '@/utils/format'

const message = useMessage()
const dialog = useDialog()

// 列表相关
const tableData = ref([])
const loading = ref(false)
const userName = ref('')
const productName = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedIds = ref([])

// 商品详情弹窗
const showProductModal = ref(false)
const currentProduct = ref(null)

// 表格列定义
const columns = [
  { type: 'selection', width: 50 },
  { title: 'ID', key: 'id', width: 70 },
  { title: '用户', key: 'userName', width: 100, render: row => row.userName || '-' },
  { title: '用户ID', key: 'userId', width: 80 },
  { title: '商品图片', key: 'productImage', width: 90, render: row => {
    const img = row.product?.mainImage
    return img ? h(NImage, { src: img, width: 50, height: 50, objectFit: 'cover', previewDisabled: true }) : '-'
  }},
  { title: '商品名称', key: 'productName', width: 180, ellipsis: { tooltip: true }, render: row => {
    return h('a', {
      style: 'color: #18a058; cursor: pointer;',
      onClick: () => handleViewProduct(row)
    }, row.product?.name || '-')
  }},
  { title: '单价', key: 'price', width: 100, render: row => {
    const price = row.product?.price
    return price ? h('span', { style: 'color: #f5222d;' }, '¥' + formatPrice(price)) : '-'
  }},
  { title: '数量', key: 'quantity', width: 80 },
  { title: '小计', key: 'subtotal', width: 100, render: row => {
    const price = row.product?.price
    if (price) {
      const subtotal = (parseFloat(price) * row.quantity).toFixed(2)
      return h('span', { style: 'color: #f5222d; font-weight: bold;' }, '¥' + subtotal)
    }
    return '-'
  }},
  { title: '选中', key: 'selected', width: 70, render: row => h(NTag, { type: row.selected === 1 ? 'success' : 'default', size: 'small' }, () => row.selected === 1 ? '是' : '否') },
  { title: '商品状态', key: 'productStatus', width: 90, render: row => {
    const status = row.product?.status
    if (status === undefined) return '-'
    return h(NTag, { type: status === 1 ? 'success' : 'error', size: 'small' }, () => status === 1 ? '上架' : '下架')
  }},
  { title: '添加时间', key: 'createTime', width: 170, render: row => formatTime(row.createTime) },
  { title: '操作', key: 'action', width: 150, fixed: 'right', render: row => h(NSpace, { size: 'small' }, () => [
    h(NButton, { text: true, type: 'warning', size: 'small', onClick: () => handleClearUserCart(row) }, () => '清空该用户'),
    h(NButton, { text: true, type: 'error', size: 'small', onClick: () => handleDelete(row) }, () => '删除')
  ])}
]

// 搜索
function handleSearch() {
  page.value = 1
  fetchData()
}

// 重置筛选
function handleReset() {
  userName.value = ''
  productName.value = ''
  handleSearch()
}

// 获取列表数据
async function fetchData() {
  loading.value = true
  try {
    const res = await getCartList({
      page: page.value,
      size: size.value,
      userName: userName.value || undefined,
      productName: productName.value || undefined
    })
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

// 选中行
function handleCheck(keys) {
  selectedIds.value = keys
}

// 查看商品详情
function handleViewProduct(row) {
  if (row.product) {
    currentProduct.value = row.product
    showProductModal.value = true
  }
}

// 删除单个
function handleDelete(row) {
  dialog.warning({
    title: '删除确认',
    content: '确定删除该购物车项吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteCartItem(row.id)
      message.success('删除成功')
      fetchData()
    }
  })
}

// 批量删除
function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message.warning('请选择要删除的项')
    return
  }
  dialog.warning({
    title: '批量删除确认',
    content: `确定删除选中的 ${selectedIds.value.length} 项吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await batchDeleteCartItems(selectedIds.value)
      message.success('删除成功')
      selectedIds.value = []
      fetchData()
    }
  })
}

// 清空用户购物车
function handleClearUserCart(row) {
  dialog.warning({
    title: '清空确认',
    content: `确定清空用户 "${row.userName || row.userId}" 的所有购物车项吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await clearUserCart(row.userId)
      message.success('清空成功')
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
.product-detail {
  text-align: center;
}
</style>
