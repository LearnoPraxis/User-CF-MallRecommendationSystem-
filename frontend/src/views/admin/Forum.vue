<template>
  <div class="admin-forum-page">
    <!-- 标签页切换 -->
    <n-tabs v-model:value="activeTab" type="line">
      <n-tab-pane name="posts" tab="帖子管理">
        <n-card>
          <!-- 筛选栏 -->
          <div class="filter-bar">
            <n-input v-model:value="postKeyword" placeholder="搜索标题/内容" clearable style="width: 200px" />
            <n-input v-model:value="postUserName" placeholder="发布者" clearable style="width: 120px" />
            <n-select v-model:value="postType" placeholder="类型" clearable style="width: 130px" :options="typeOptions" />
            <n-select v-model:value="postStatus" placeholder="状态" clearable style="width: 100px" :options="statusOptions" />
            <n-button type="primary" @click="handlePostSearch">搜索</n-button>
            <n-button @click="handlePostReset">重置</n-button>
            <n-button type="error" :disabled="!selectedPostIds.length" @click="handleBatchDeletePosts">批量删除</n-button>
          </div>
          <!-- 数据表格 -->
          <n-data-table
            :columns="postColumns"
            :data="postTableData"
            :loading="postLoading"
            :scroll-x="1400"
            :pagination="false"
            :row-key="row => row.id"
            @update:checked-row-keys="keys => selectedPostIds = keys"
          />
          <!-- 分页 -->
          <div class="pagination">
            <n-pagination v-model:page="postPage" :page-size="postSize" :item-count="postTotal" show-quick-jumper @update:page="fetchPosts" />
          </div>
        </n-card>
      </n-tab-pane>

      <n-tab-pane name="comments" tab="评论管理">
        <n-card>
          <!-- 筛选栏 -->
          <div class="filter-bar">
            <n-input v-model:value="commentKeyword" placeholder="搜索内容/用户" clearable style="width: 200px" />
            <n-select v-model:value="commentStatus" placeholder="状态" clearable style="width: 100px" :options="statusOptions" />
            <n-button type="primary" @click="handleCommentSearch">搜索</n-button>
            <n-button @click="handleCommentReset">重置</n-button>
            <n-button type="error" :disabled="!selectedCommentIds.length" @click="handleBatchDeleteComments">批量删除</n-button>
          </div>
          <!-- 数据表格 -->
          <n-data-table
            :columns="commentColumns"
            :data="commentTableData"
            :loading="commentLoading"
            :scroll-x="1200"
            :pagination="false"
            :row-key="row => row.id"
            @update:checked-row-keys="keys => selectedCommentIds = keys"
          />
          <!-- 分页 -->
          <div class="pagination">
            <n-pagination v-model:page="commentPage" :page-size="commentSize" :item-count="commentTotal" show-quick-jumper @update:page="fetchComments" />
          </div>
        </n-card>
      </n-tab-pane>
    </n-tabs>

    <!-- 帖子详情弹窗 -->
    <n-modal v-model:show="showPostDetail" preset="card" title="帖子详情" style="width: 700px">
      <template v-if="currentPost">
        <n-descriptions :column="2" label-placement="left" bordered size="small">
          <n-descriptions-item label="帖子ID">{{ currentPost.id }}</n-descriptions-item>
          <n-descriptions-item label="类型">
            <n-tag :type="postTypeMap[currentPost.type]?.type" size="small">{{ postTypeMap[currentPost.type]?.text }}</n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="发布者">
            <n-space align="center">
              <n-avatar v-if="currentPost.userAvatar" :src="currentPost.userAvatar" size="small" round />
              <span>{{ currentPost.userName }}</span>
            </n-space>
          </n-descriptions-item>
          <n-descriptions-item label="用户ID">{{ currentPost.userId }}</n-descriptions-item>
          <n-descriptions-item label="标题" :span="2">{{ currentPost.title }}</n-descriptions-item>
          <n-descriptions-item label="浏览量">{{ currentPost.viewCount }}</n-descriptions-item>
          <n-descriptions-item label="评论数">{{ currentPost.commentCount }}</n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="currentPost.status === 1 ? 'success' : 'default'" size="small">
              {{ currentPost.status === 1 ? '显示' : '隐藏' }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="发布时间">{{ formatTime(currentPost.createTime) }}</n-descriptions-item>
        </n-descriptions>
        <n-divider>帖子内容</n-divider>
        <div class="post-content">{{ currentPost.content }}</div>
        <template v-if="currentPost.images">
          <n-divider>帖子图片</n-divider>
          <n-space>
            <n-image v-for="(img, idx) in currentPost.images.split(',')" :key="idx" :src="img" width="100" height="100" object-fit="cover" />
          </n-space>
        </template>
        <n-divider>评论列表 ({{ postComments.length }})</n-divider>
        <div v-if="postComments.length" class="comment-list">
          <div v-for="comment in postComments" :key="comment.id" class="comment-item">
            <n-space align="center">
              <n-avatar v-if="comment.userAvatar" :src="comment.userAvatar" size="small" round />
              <span class="comment-user">{{ comment.userName }}</span>
              <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
            </n-space>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
        </div>
        <n-empty v-else description="暂无评论" />
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { h, ref, onMounted, watch } from 'vue'
import { NTag, NButton, NSpace, NAvatar, NImage, useMessage, useDialog } from 'naive-ui'
import { getPostList, getPostById, getPostComments, getCommentList, updatePostStatus, deletePost, batchDeletePosts, updateCommentStatus, deleteForumComment, batchDeleteForumComments } from '@/api/admin/forum'
import { formatTime, postTypeMap } from '@/utils/format'

const message = useMessage()
const dialog = useDialog()

// 标签页
const activeTab = ref('posts')

// ========== 帖子管理 ==========
const postTableData = ref([])
const postLoading = ref(false)
const postKeyword = ref('')
const postUserName = ref('')
const postType = ref(null)
const postStatus = ref(null)
const postPage = ref(1)
const postSize = ref(10)
const postTotal = ref(0)
const selectedPostIds = ref([])

// 帖子详情
const showPostDetail = ref(false)
const currentPost = ref(null)
const postComments = ref([])

const typeOptions = Object.entries(postTypeMap).map(([k, v]) => ({ label: v.text, value: Number(k) }))
const statusOptions = [{ label: '显示', value: 1 }, { label: '隐藏', value: 0 }]

// 帖子表格列
const postColumns = [
  { type: 'selection', width: 50 },
  { title: 'ID', key: 'id', width: 70, fixed: 'left' },
  { title: '标题', key: 'title', width: 200, ellipsis: { tooltip: true } },
  { title: '发布者', key: 'user', width: 120, render: row => h(NSpace, { align: 'center' }, () => [
    row.userAvatar ? h(NAvatar, { src: row.userAvatar, size: 'small', round: true }) : null,
    h('span', null, row.userName || '-')
  ])},
  { title: '类型', key: 'type', width: 110, render: row => h(NTag, { type: postTypeMap[row.type]?.type, size: 'small' }, () => postTypeMap[row.type]?.text) },
  { title: '内容预览', key: 'content', width: 200, ellipsis: { tooltip: true } },
  { title: '浏览', key: 'viewCount', width: 70 },
  { title: '评论', key: 'commentCount', width: 70 },
  { title: '状态', key: 'status', width: 80, render: row => h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, () => row.status === 1 ? '显示' : '隐藏') },
  { title: '发布时间', key: 'createTime', width: 170, render: row => formatTime(row.createTime) },
  { title: '操作', key: 'action', width: 180, fixed: 'right', render: row => h(NSpace, { size: 'small' }, () => [
    h(NButton, { text: true, type: 'info', size: 'small', onClick: () => handlePostDetail(row) }, () => '详情'),
    h(NButton, { text: true, type: row.status === 1 ? 'warning' : 'success', size: 'small', onClick: () => handleTogglePostStatus(row) }, () => row.status === 1 ? '隐藏' : '显示'),
    h(NButton, { text: true, type: 'error', size: 'small', onClick: () => handleDeletePost(row) }, () => '删除')
  ])}
]

// ========== 评论管理 ==========
const commentTableData = ref([])
const commentLoading = ref(false)
const commentKeyword = ref('')
const commentStatus = ref(null)
const commentPage = ref(1)
const commentSize = ref(10)
const commentTotal = ref(0)
const selectedCommentIds = ref([])

// 评论表格列
const commentColumns = [
  { type: 'selection', width: 50 },
  { title: 'ID', key: 'id', width: 70, fixed: 'left' },
  { title: '所属帖子', key: 'postTitle', width: 180, ellipsis: { tooltip: true }, render: row => row.postTitle || `帖子#${row.postId}` },
  { title: '评论者', key: 'user', width: 120, render: row => h(NSpace, { align: 'center' }, () => [
    row.userAvatar ? h(NAvatar, { src: row.userAvatar, size: 'small', round: true }) : null,
    h('span', null, row.userName || '-')
  ])},
  { title: '评论内容', key: 'content', width: 250, ellipsis: { tooltip: true } },
  { title: '状态', key: 'status', width: 80, render: row => h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, () => row.status === 1 ? '显示' : '隐藏') },
  { title: '评论时间', key: 'createTime', width: 170, render: row => formatTime(row.createTime) },
  { title: '操作', key: 'action', width: 150, fixed: 'right', render: row => h(NSpace, { size: 'small' }, () => [
    h(NButton, { text: true, type: row.status === 1 ? 'warning' : 'success', size: 'small', onClick: () => handleToggleCommentStatus(row) }, () => row.status === 1 ? '隐藏' : '显示'),
    h(NButton, { text: true, type: 'error', size: 'small', onClick: () => handleDeleteComment(row) }, () => '删除')
  ])}
]

// ========== 帖子方法 ==========
function handlePostSearch() {
  postPage.value = 1
  fetchPosts()
}

function handlePostReset() {
  postKeyword.value = ''
  postUserName.value = ''
  postType.value = null
  postStatus.value = null
  handlePostSearch()
}

async function fetchPosts() {
  postLoading.value = true
  try {
    const res = await getPostList({
      page: postPage.value,
      size: postSize.value,
      keyword: postKeyword.value || undefined,
      userName: postUserName.value || undefined,
      type: postType.value,
      status: postStatus.value
    })
    postTableData.value = res.records || []
    postTotal.value = res.total || 0
  } finally {
    postLoading.value = false
  }
}

async function handlePostDetail(row) {
  currentPost.value = await getPostById(row.id)
  postComments.value = await getPostComments(row.id)
  showPostDetail.value = true
}

function handleTogglePostStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '显示' : '隐藏'
  dialog.warning({
    title: '提示',
    content: `确定${action}帖子 "${row.title}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await updatePostStatus(row.id, newStatus)
      message.success('操作成功')
      fetchPosts()
    }
  })
}

function handleDeletePost(row) {
  dialog.warning({
    title: '删除确认',
    content: `确定删除帖子 "${row.title}" 吗？相关评论也会被删除！`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deletePost(row.id)
      message.success('删除成功')
      fetchPosts()
    }
  })
}

function handleBatchDeletePosts() {
  if (!selectedPostIds.value.length) return
  dialog.warning({
    title: '批量删除确认',
    content: `确定删除选中的 ${selectedPostIds.value.length} 个帖子吗？相关评论也会被删除！`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await batchDeletePosts(selectedPostIds.value)
      message.success('删除成功')
      selectedPostIds.value = []
      fetchPosts()
    }
  })
}

// ========== 评论方法 ==========
function handleCommentSearch() {
  commentPage.value = 1
  fetchComments()
}

function handleCommentReset() {
  commentKeyword.value = ''
  commentStatus.value = null
  handleCommentSearch()
}

async function fetchComments() {
  commentLoading.value = true
  try {
    const res = await getCommentList({
      page: commentPage.value,
      size: commentSize.value,
      keyword: commentKeyword.value || undefined,
      status: commentStatus.value
    })
    commentTableData.value = res.records || []
    commentTotal.value = res.total || 0
  } finally {
    commentLoading.value = false
  }
}

function handleToggleCommentStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '显示' : '隐藏'
  dialog.warning({
    title: '提示',
    content: `确定${action}该评论吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await updateCommentStatus(row.id, newStatus)
      message.success('操作成功')
      fetchComments()
    }
  })
}

function handleDeleteComment(row) {
  dialog.warning({
    title: '删除确认',
    content: '确定删除该评论吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteForumComment(row.id)
      message.success('删除成功')
      fetchComments()
    }
  })
}

function handleBatchDeleteComments() {
  if (!selectedCommentIds.value.length) return
  dialog.warning({
    title: '批量删除确认',
    content: `确定删除选中的 ${selectedCommentIds.value.length} 条评论吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await batchDeleteForumComments(selectedCommentIds.value)
      message.success('删除成功')
      selectedCommentIds.value = []
      fetchComments()
    }
  })
}

// 切换标签页时加载数据
watch(activeTab, (val) => {
  if (val === 'posts' && !postTableData.value.length) {
    fetchPosts()
  } else if (val === 'comments' && !commentTableData.value.length) {
    fetchComments()
  }
})

onMounted(() => {
  fetchPosts()
})
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
.post-content {
  padding: 12px;
  background: #f5f5f5;
  border-radius: 4px;
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
}
.comment-list {
  max-height: 300px;
  overflow-y: auto;
}
.comment-item {
  padding: 12px;
  border-bottom: 1px solid #eee;
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-user {
  font-weight: bold;
}
.comment-time {
  font-size: 12px;
  color: #999;
}
.comment-content {
  margin-top: 8px;
  color: #333;
}
</style>
