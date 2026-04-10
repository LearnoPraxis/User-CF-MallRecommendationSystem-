<template>
  <div class="admin-order-page">
    <n-card>
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <n-input v-model:value="orderNo" placeholder="订单号" clearable style="width: 200px" />
        <n-input v-model:value="userName" placeholder="用户名" clearable style="width: 150px" />
        <n-select v-model:value="status" placeholder="订单状态" clearable style="width: 120px" :options="statusOptions" />
        <n-date-picker v-model:value="dateRange" type="daterange" clearable style="width: 260px" />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
        <n-button @click="handleReset">重置</n-button>
      </div>
      <!-- 数据表格 -->
      <n-data-table :columns="columns" :data="tableData" :loading="loading" :scroll-x="1600" :pagination="false" />
      <!-- 分页 -->
      <div class="pagination">
        <n-pagination v-model:page="page" :page-size="size" :item-count="total" show-quick-jumper @update:page="fetchData" />
      </div>
    </n-card>

    <!-- 订单详情弹窗 -->
    <n-modal v-model:show="showDetail" preset="card" title="订单详情" style="width: 800px">
      <template v-if="currentOrder">
        <!-- 基本信息 -->
        <n-descriptions :column="2" label-placement="left" bordered size="small">
          <n-descriptions-item label="订单号">{{ currentOrder.orderNo }}</n-descriptions-item>
          <n-descriptions-item label="订单状态">
            <n-tag :type="orderStatusMap[currentOrder.status]?.type" size="small">
              {{ orderStatusMap[currentOrder.status]?.text }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="下单用户">{{ currentOrder.userName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="用户ID">{{ currentOrder.userId }}</n-descriptions-item>
          <n-descriptions-item label="订单总金额">
            <span style="color: #f5222d; font-weight: bold;">¥{{ formatPrice(currentOrder.totalAmount) }}</span>
          </n-descriptions-item>
          <n-descriptions-item label="实付金额">
            <span style="color: #f5222d; font-weight: bold;">¥{{ formatPrice(currentOrder.payAmount) }}</span>
          </n-descriptions-item>
        </n-descriptions>

        <!-- 收货信息 -->
        <n-divider>收货信息</n-divider>
        <n-descriptions :column="2" label-placement="left" bordered size="small">
          <n-descriptions-item label="收货人">{{ currentOrder.receiverName }}</n-descriptions-item>
          <n-descriptions-item label="联系电话">{{ currentOrder.receiverPhone }}</n-descriptions-item>
          <n-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress }}</n-descriptions-item>
          <n-descriptions-item label="订单备注" :span="2">{{ currentOrder.remark || '无' }}</n-descriptions-item>
        </n-descriptions>

        <!-- 时间信息 -->
        <n-divider>时间信息</n-divider>
        <n-descriptions :column="2" label-placement="left" bordered size="small">
          <n-descriptions-item label="下单时间">{{ formatTime(currentOrder.createTime) }}</n-descriptions-item>
          <n-descriptions-item label="支付时间">{{ currentOrder.payTime ? formatTime(currentOrder.payTime) : '-' }}</n-descriptions-item>
          <n-descriptions-item label="发货时间">{{ currentOrder.deliverTime ? formatTime(currentOrder.deliverTime) : '-' }}</n-descriptions-item>
          <n-descriptions-item label="收货时间">{{ currentOrder.receiveTime ? formatTime(currentOrder.receiveTime) : '-' }}</n-descriptions-item>
          <n-descriptions-item label="更新时间">{{ formatTime(currentOrder.updateTime) }}</n-descriptions-item>
        </n-descriptions>

        <!-- 商品明细 -->
        <n-divider>商品明细</n-divider>
        <n-table :bordered="true" size="small" striped>
          <thead>
            <tr>
              <th style="width: 80px;">图片</th>
              <th>商品名称</th>
              <th style="width: 100px;">单价</th>
              <th style="width: 80px;">数量</th>
              <th style="width: 100px;">小计</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in currentOrder.orderItems" :key="item.id">
              <td>
                <n-image v-if="item.productImage" :src="item.productImage" width="50" height="50" object-fit="cover" preview-disabled />
                <span v-else>-</span>
              </td>
              <td>{{ item.productName }}</td>
              <td>¥{{ formatPrice(item.price) }}</td>
              <td>{{ item.quantity }}</td>
              <td style="color: #f5222d;">¥{{ formatPrice(item.totalPrice) }}</td>
            </tr>
          </tbody>
        </n-table>
      </template>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDetail = false">关闭</n-button>
          <n-button v-if="currentOrder && currentOrder.status === 1" type="primary" @click="handleDeliver(currentOrder)">发货</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 修改备注弹窗 -->
    <n-modal v-model:show="showRemarkModal" preset="dialog" title="修改备注" style="width: 400px">
      <n-input v-model:value="remarkForm.remark" type="textarea" :rows="3" placeholder="请输入订单备注" />
      <template #action>
        <n-button @click="showRemarkModal = false">取消</n-button>
        <n-button type="primary" :loading="remarkLoading" @click="handleRemarkSubmit">确定</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { NTag, NButton, NSpace, NImage, useMessage, useDialog } from 'naive-ui'
import { getOrderList, getOrderDetail, deliverOrder, deleteOrder, updateOrderRemark } from '@/api/admin/order'
import { formatTime, formatPrice, orderStatusMap } from '@/utils/format'

const message = useMessage()
const dialog = useDialog()

// 列表相关
const tableData = ref([])
const loading = ref(false)
const orderNo = ref('')
const userName = ref('')
const status = ref(null)
const dateRange = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 详情弹窗
const showDetail = ref(false)
const currentOrder = ref(null)

// 备注弹窗
const showRemarkModal = ref(false)
const remarkLoading = ref(false)
const remarkForm = ref({ id: null, remark: '' })

const statusOptions = Object.entries(orderStatusMap).map(([k, v]) => ({ label: v.text, value: Number(k) }))

// 表格列定义
const columns = [
  { title: '订单号', key: 'orderNo', width: 180, fixed: 'left', ellipsis: { tooltip: true } },
  { title: '用户', key: 'userName', width: 100, render: row => row.userName || '-' },
  { title: '商品数', key: 'itemCount', width: 70, render: row => row.orderItems?.length || '-' },
  { title: '总金额', key: 'totalAmount', width: 100, render: row => h('span', { style: 'color: #f5222d;' }, '¥' + formatPrice(row.totalAmount)) },
  { title: '实付金额', key: 'payAmount', width: 100, render: row => h('span', { style: 'color: #f5222d; font-weight: bold;' }, '¥' + formatPrice(row.payAmount)) },
  { title: '状态', key: 'status', width: 90, render: row => h(NTag, { type: orderStatusMap[row.status]?.type, size: 'small' }, () => orderStatusMap[row.status]?.text) },
  { title: '收货人', key: 'receiverName', width: 90 },
  { title: '联系电话', key: 'receiverPhone', width: 120 },
  { title: '收货地址', key: 'receiverAddress', width: 200, ellipsis: { tooltip: true } },
  { title: '下单时间', key: 'createTime', width: 170, render: row => formatTime(row.createTime) },
  { title: '支付时间', key: 'payTime', width: 170, render: row => row.payTime ? formatTime(row.payTime) : '-' },
  { title: '操作', key: 'action', width: 200, fixed: 'right', render: row => h(NSpace, { size: 'small' }, () => [
    h(NButton, { text: true, type: 'info', size: 'small', onClick: () => handleDetail(row) }, () => '详情'),
    row.status === 1 ? h(NButton, { text: true, type: 'success', size: 'small', onClick: () => handleDeliver(row) }, () => '发货') : null,
    h(NButton, { text: true, type: 'warning', size: 'small', onClick: () => handleEditRemark(row) }, () => '备注'),
    row.status === 3 || row.status === 4 ? h(NButton, { text: true, type: 'error', size: 'small', onClick: () => handleDelete(row) }, () => '删除') : null
  ])}
]

// 搜索
function handleSearch() {
  page.value = 1
  fetchData()
}

// 重置筛选
function handleReset() {
  orderNo.value = ''
  userName.value = ''
  status.value = null
  dateRange.value = null
  handleSearch()
}

// 获取列表数据
async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      orderNo: orderNo.value || undefined,
      userName: userName.value || undefined,
      status: status.value
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = new Date(dateRange.value[0]).toISOString()
      params.endTime = new Date(dateRange.value[1]).toISOString()
    }
    const res = await getOrderList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

// 查看详情
async function handleDetail(row) {
  currentOrder.value = await getOrderDetail(row.id)
  showDetail.value = true
}

// 发货
function handleDeliver(row) {
  dialog.info({
    title: '发货确认',
    content: `确定对订单 ${row.orderNo} 进行发货吗？`,
    positiveText: '确定发货',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deliverOrder(row.id)
      message.success('发货成功')
      showDetail.value = false
      fetchData()
    }
  })
}

// 编辑备注
function handleEditRemark(row) {
  remarkForm.value = { id: row.id, remark: row.remark || '' }
  showRemarkModal.value = true
}

// 提交备注
async function handleRemarkSubmit() {
  remarkLoading.value = true
  try {
    await updateOrderRemark(remarkForm.value.id, remarkForm.value.remark)
    message.success('备注更新成功')
    showRemarkModal.value = false
    fetchData()
  } finally {
    remarkLoading.value = false
  }
}

// 删除订单
function handleDelete(row) {
  dialog.warning({
    title: '删除确认',
    content: `确定删除订单 ${row.orderNo} 吗？此操作不可恢复！`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteOrder(row.id)
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
