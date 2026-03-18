<template>
  <div class="forum-detail-page">
    <div class="page-container">
      <!-- 返回按钮 -->
      <div class="back-bar">
        <n-button text @click="router.push('/forum')">
          <template #icon><n-icon><ArrowBackOutline /></n-icon></template>
          返回论坛
        </n-button>
      </div>
      
      <n-card v-if="post" class="post-card">
        <div class="post-header">
          <n-tag :type="postTypeMap[post.type]?.type" size="medium">{{ postTypeMap[post.type]?.text }}</n-tag>
          <h1 class="post-title">{{ post.title }}</h1>
          <div class="post-meta">
            <div class="author-info">
              <n-avatar :src="post.userAvatar" size="medium" round />
              <div class="author-detail">
                <span class="author-name">{{ post.userName || '匿名用户' }}</span>
                <span class="post-time">{{ formatTime(post.createTime) }}</span>
              </div>
            </div>
            <div class="post-stats">
              <span><n-icon><EyeOutline /></n-icon> {{ post.viewCount || 0 }}</span>
              <span><n-icon><ChatbubbleOutline /></n-icon> {{ comments.length }}</span>
            </div>
          </div>
        </div>
        
        <n-divider />
        
        <div class="post-content">{{ post.content }}</div>
        
        <div v-if="post.images" class="post-images">
          <n-image-group>
            <n-space>
              <n-image 
                v-for="(img, index) in post.images.split(',')" 
                :key="index" 
                :src="img" 
                width="200"
                height="200"
                object-fit="cover"
                style="border-radius: 8px;"
              />
            </n-space>
          </n-image-group>
        </div>
        
        <!-- 操作按钮 -->
        <div v-if="isAuthor" class="post-actions">
          <n-button type="error" size="small" @click="handleDeletePost">
            <template #icon><n-icon><TrashOutline /></n-icon></template>
            删除帖子
          </n-button>
        </div>
      </n-card>
      
      <!-- 评论区 -->
      <n-card class="comment-card">
        <template #header>
          <div class="comment-header">
            <span>评论 ({{ comments.length }})</span>
          </div>
        </template>
        
        <!-- 评论输入框 -->
        <div v-if="userStore.isLogin" class="comment-form">
          <n-input 
            v-model:value="commentContent" 
            type="textarea" 
            placeholder="写下你的评论..." 
            :rows="3"
            :maxlength="500"
            show-count
          />
          <div class="comment-form-actions">
            <n-button type="primary" :loading="submitting" @click="handleAddComment">
              <template #icon><n-icon><SendOutline /></n-icon></template>
              发表评论
            </n-button>
          </div>
        </div>
        <n-alert v-else type="info" style="margin-bottom: 20px">
          请 <n-button text type="primary" @click="router.push('/login')">登录</n-button> 后发表评论
        </n-alert>
        
        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-for="item in comments" :key="item.id" class="comment-item">
            <n-avatar :src="item.userAvatar" size="medium" round />
            <div class="comment-body">
              <div class="comment-info">
                <span class="comment-author">{{ item.userName || '匿名用户' }}</span>
                <span class="comment-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <p class="comment-content">{{ item.content }}</p>
              <!-- 删除评论按钮 -->
              <div v-if="canDeleteComment(item)" class="comment-actions">
                <n-button text type="error" size="tiny" @click="handleDeleteComment(item.id)">
                  删除
                </n-button>
              </div>
            </div>
          </div>
        </div>
        <n-empty v-if="!comments.length" description="暂无评论，快来抢沙发吧~" />
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import { useUserStore } from '@/store/user'
import { getPostDetail, addForumComment, deletePost, deleteForumComment } from '@/api/forum'
import { formatTime, postTypeMap } from '@/utils/format'
import { ArrowBackOutline, EyeOutline, ChatbubbleOutline, SendOutline, TrashOutline } from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()

const post = ref(null)
const comments = ref([])
const commentContent = ref('')
const submitting = ref(false)

// 是否是帖子作者
const isAuthor = computed(() => {
  return userStore.isLogin && post.value && userStore.userInfo?.id === post.value.userId
})

// 是否可以删除评论（作者或管理员）
function canDeleteComment(comment) {
  if (!userStore.isLogin) return false
  return userStore.userInfo?.id === comment.userId || userStore.userInfo?.role === 1
}

async function fetchData() {
  try {
    const data = await getPostDetail(route.params.id)
    post.value = data.post
    comments.value = data.comments || []
  } catch (e) {
    message.error('帖子不存在或已被删除')
    router.push('/forum')
  }
}

async function handleAddComment() {
  if (!commentContent.value.trim()) {
    return message.warning('请输入评论内容')
  }
  submitting.value = true
  try {
    await addForumComment({ postId: post.value.id, content: commentContent.value })
    message.success('评论成功')
    commentContent.value = ''
    fetchData()
  } finally {
    submitting.value = false
  }
}

function handleDeletePost() {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除这个帖子吗？删除后无法恢复。',
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deletePost(post.value.id)
      message.success('删除成功')
      router.push('/forum')
    }
  })
}

async function handleDeleteComment(id) {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除这条评论吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteForumComment(id)
      message.success('删除成功')
      fetchData()
    }
  })
}

onMounted(fetchData)
</script>

<style scoped>
.forum-detail-page { 
  padding: 20px; 
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}
.page-container { 
  max-width: 900px; 
  margin: 0 auto; 
}
.back-bar {
  margin-bottom: 16px;
}
.post-card {
  margin-bottom: 20px;
}
.post-header { 
  margin-bottom: 16px; 
}
.post-title { 
  font-size: 26px; 
  font-weight: 600;
  margin: 16px 0; 
  line-height: 1.4;
  color: #1a1a1a;
}
.post-meta { 
  display: flex; 
  align-items: center; 
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}
.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.author-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.author-name {
  font-weight: 500;
  color: #333;
}
.post-time {
  font-size: 13px;
  color: #999;
}
.post-stats {
  display: flex;
  gap: 16px;
  color: #666;
  font-size: 14px;
}
.post-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}
.post-content { 
  font-size: 16px; 
  line-height: 1.8; 
  white-space: pre-wrap; 
  color: #333;
}
.post-images { 
  margin-top: 24px; 
}
.post-actions {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
}

/* 评论区 */
.comment-card {
  margin-top: 20px;
}
.comment-header {
  font-size: 16px;
  font-weight: 500;
}
.comment-form { 
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}
.comment-form-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
.comment-list { 
  display: flex; 
  flex-direction: column; 
  gap: 16px; 
}
.comment-item { 
  display: flex; 
  gap: 12px; 
  padding: 16px 0; 
  border-bottom: 1px solid #f0f0f0; 
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-body { 
  flex: 1; 
}
.comment-info { 
  display: flex; 
  align-items: center;
  gap: 12px;
  margin-bottom: 8px; 
}
.comment-author { 
  font-weight: 500;
  color: #333;
}
.comment-time { 
  color: #999; 
  font-size: 13px; 
}
.comment-content { 
  color: #333;
  line-height: 1.6;
  margin: 0;
}
.comment-actions {
  margin-top: 8px;
}
</style>
