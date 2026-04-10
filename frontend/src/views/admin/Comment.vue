<template>
  <div class="admin-comment-page">
    <n-card>
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <n-input v-model:value="keyword" placeholder="搜索内容/商品/用户" clearable style="width: 200px" />
        <n-select v-model:value="rating" placeholder="评分" clearable style="width: 120px" :options="ratingOptions" />
        <n-select v-model:value="hasReply" placeholder="回复状态" clearable style="width: 120px" :options="replyOptions" />
        <n-select v-model:value="status" placeholder="显示状态" clearable style="width: 120px" :options="statusOptions" />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
        <n-button @click="handleReset">重置</n-button>
        <n-button type="error" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</n-button>
      </div>
      <!-- 数据表格 -->
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :scroll-x="1500"
        :pagination="false"
        :row-key="row => row.id"
        @update:checked-row-keys="handleCheck"
      />
      <!-- 分页 -->
      <div class="pagination">
        <n-pagination v-model:page="page" :page-size="size" :item-count="total" show-quick-jumper @update:page="fetchData" />
      </div>
    </n-card>

    <!-- 留言详情弹窗 -->
    <n-modal v-model:show="showDetailModal" preset="card" title="留言详情" style="width: 600px">
      <template v-if="currentComment">
        <n-descriptions :column="2" label-placement="left" bordered size="small">
          <n-descriptions-item label="留言ID">{{ currentComment.id }}</n-descriptions-item>
          <n-descriptions-item label="评分">
            <n-rate :value="currentComment.rating" readonly size="small" />
          </n-descriptions-item>
          <n-descriptions-item label="用户">
            <n-space align="center">
              <n-avatar v-if="currentComment.userAvatar" :src="currentComment.userAvatar" size="small" round />
              <span>{{ currentComment.userName }}</span>
            </n-space>
          </n-descriptions-item>
          <n-descriptions-item label="用户ID">{{ currentComment.userId }}</n-descriptions-item>
          <n-descriptions-item label="商品" :span="2">
            <n-space align="center">
              <n-image v-if="currentComment.productImage" :src="currentComment.productImage" width="40" height="40" object-fit="cover" preview-disabled />
              <span>{{ currentComment.productName }}</span>
            </n-space>
          </n-descriptions-item>
          <n-descriptions-item label="留言内容" :span="2">{{ currentComment.content }}</n-descriptions-item>
          <n-descriptions-item label="留言时间">{{ formatTime(currentComment.createTime) }}</n-descriptions-item>
          <n-descriptions-item label="显示状态">
            <n-tag :type="currentComment.status === 1 ? 'success' : 'error'" size="small">
              {{ currentComment.status === 1 ? '显示' : '隐藏' }}
            </n-tag>
          </n-descriptions-item>
        </n-descriptions>
        <n-divider v-if="currentComment.replyContent">管理员回复</n-divider>
        <n-descriptions v-if="currentComment.replyContent" :column="2" label-placement="left" bordered size="small">
          <n-descriptions-item label="回复内容" :span="2">{{ currentComment.replyContent }}</n-descriptions-item>
          <n-descriptions-item label="回复人">{{ currentComment.replyUserName || '-' }}</n-descriptions-item>
          <n-descriptions-item label="回复时间">{{ currentComment.replyTime ? formatTime(currentComment.replyTime) : '-' }}</n-descriptions-item>
        </n-descriptions>
      </template>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDetailModal = false">关闭</n-button>
          <n-button type="primary" @click="handleReplyFromDetail">{{ currentComment?.replyContent ? '修改回复' : '回复' }}</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 回复弹窗 -->
    <n-modal v-model:show="showReplyModal" preset="card" title="回复留言" style="width: 550px" :mask-closable="false">
      <template v-if="replyForm.comment">
        <div class="reply-comment-info">
          <n-space align="center">
            <n-avatar v-if="replyForm.comment.userAvatar" :src="replyForm.comment.userAvatar" size="small" round />
            <span>{{ replyForm.comment.userName }}</span>
            <n-rate :value="replyForm.comment.rating" readonly size="small" />
          </n-space>
          <div class="comment-content">{{ replyForm.comment.content }}</div>
          <div class="comment-time">{{ formatTime(replyForm.comment.createTime) }}</div>
        </div>
        <n-divider />
        <n-form-item label="回复内容">
          <n-input v-model:value="replyForm.replyContent" type="textarea" :rows="4" placeholder="请输入回复内容" />
        </n-form-item>
      </template>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showReplyModal = false">取消</n-button>
          <n-button type="primary" :loading="replyLoading" @click="handleSubmitReply">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { h, ref, onMounted } from 'vue'
import { NTag, NButton, NSpace, NRate, NAvatar, NImage, useMessage, useDialog } from 'naive-ui'
import { getCommentList, replyComment, updateCommentStatus, deleteComment, batchDeleteComments } from '@/api/admin/comment'
import { formatTime } from '@/utils/format'

const message = useMessage()
const dialog = useDialog()

// 列表相关
const tableData = ref([])
const loading = ref(false)
const keyword = ref('')
const rating = ref(null)
const hasReply = ref(null)
const status = ref(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const selectedIds = ref([])

// 筛选选项
const ratingOptions = [
  { label: '1星', value: 1 },
  { label: '2星', value: 2 },
  { label: '3星', value: 3 },
  { label: '4星', value: 4 },
  { label: '5星', value: 5 }
]
const replyOptions = [
  { label: '已回复', value: 1 },
  { label: '未回复', value: 0 }
]
const statusOptions = [
  { label: '显示', value: 1 },
  { label: '隐藏', value: 0 }
]

// 详情弹窗
const showDetailModal = ref(false)
const currentComment = ref(null)

// 回复弹窗
const showReplyModal = ref(false)
const replyLoading = ref(false)
const replyForm = ref({ comment: null, replyContent: '' })

// 表格列定义
const columns = [
  { type: 'selection', width: 50 },
  { title: 'ID', key: 'id', width: 70, fixed: 'left' },
  { title: '商品', key: 'product', width: 200, render: row => h(NSpace, { align: 'center' }, () => [
    row.productImage ? h(NImage, { src: row.productImage, width: 40, height: 40, objectFit: 'cover', previewDisabled: true }) : null,
    h('span', { style: 'overflow: hidden; text-overflow: ellipsis; white-space: nowrap;' }, row.productName || '-')
  ])},
  { title: '用户', key: 'user', width: 120, render: row => h(NSpace, { align: 'center' }, () => [
    row.userAvatar ? h(NAvatar, { src: row.userAvatar, size: 'small', round: true }) : null,
    h('span', null, row.userName || '-')
  ])},
  { title: '评分', key: 'rating', width: 130, render: row => h(NRate, { value: row.rating, readonly: true, size: 'small' }) },
  { title: '留言内容', key: 'content', width: 200, ellipsis: { tooltip: true } },
  { title: '回复内容', key: 'replyContent', width: 150, ellipsis: { tooltip: true }, render: row => row.replyContent || h('span', { style: 'color: #999;' }, '未回复') },
  { title: '回复人', key: 'replyUserName', width: 100, render: row => row.replyUserName || '-' },
  { title: '状态', key: 'status', width: 80, render: row => h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, () => row.status === 1 ? '显示' : '隐藏') },
  { title: '留言时间', key: 'createTime', width: 170, render: row => formatTime(row.createTime) },
  { title: '操作', key: 'action', width: 200, fixed: 'right', render: row => h(NSpace, { size: 'small' }, () => [
    h(NButton, { text: true, type: 'info', size: 'small', onClick: () => handleDetail(row) }, () => '详情'),
    h(NButton, { text: true, type: 'primary', size: 'small', onClick: () => handleReply(row) }, () => row.replyContent ? '改回复' : '回复'),
    h(NButton, { text: true, type: row.status === 1 ? 'warning' : 'success', size: 'small', onClick: () => handleToggleStatus(row) }, () => row.status === 1 ? '隐藏' : '显示'),
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
  keyword.value = ''
  rating.value = null
  hasReply.value = null
  status.value = null
  handleSearch()
}

// 获取列表数据
async function fetchData() {
  loading.value = true
  try {
    const res = await getCommentList({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      rating: rating.value,
      hasReply: hasReply.value,
      status: status.value
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

// 查看详情
function handleDetail(row) {
  currentComment.value = row
  showDetailModal.value = true
}

// 从详情弹窗打开回复
function handleReplyFromDetail() {
  showDetailModal.value = false
  handleReply(currentComment.value)
}

// 回复留言
function handleReply(row) {
  replyForm.value = {
    comment: row,
    replyContent: row.replyContent || ''
  }
  showReplyModal.value = true
}

// 提交回复
async function handleSubmitReply() {
  if (!replyForm.value.replyContent.trim()) {
    message.warning('请输入回复内容')
    return
  }
  replyLoading.value = true
  try {
    await replyComment(replyForm.value.comment.id, replyForm.value.replyContent)
    message.success('回复成功')
    showReplyModal.value = false
    fetchData()
  } finally {
    replyLoading.value = false
  }
}

// 切换状态
function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '显示' : '隐藏'
  dialog.warning({
    title: '提示',
    content: `确定${action}该留言吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await updateCommentStatus(row.id, newStatus)
      message.success('操作成功')
      fetchData()
    }
  })
}

// 删除留言
function handleDelete(row) {
  dialog.warning({
    title: '删除确认',
    content: '确定删除该留言吗？此操作不可恢复！',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteComment(row.id)
      message.success('删除成功')
      fetchData()
    }
  })
}

// 批量删除
function handleBatchDelete() {
  if (!selectedIds.value.length) {
    message.warning('请选择要删除的留言')
    return
  }
  dialog.warning({
    title: '批量删除确认',
    content: `确定删除选中的 ${selectedIds.value.length} 条留言吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await batchDeleteComments(selectedIds.value)
      message.success('删除成功')
      selectedIds.value = []
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
.reply-comment-info {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
}
.comment-content {
  margin-top: 8px;
  color: #333;
}
.comment-time {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}
</style>
