<template>
  <div class="forum-list-page">
    <div class="page-container">
      <!-- 左侧筛选 -->
      <div class="page-left">
        <n-card title="帖子分类" size="small">
          <n-menu :value="currentType" :options="typeOptions" @update:value="handleTypeChange" />
        </n-card>
        <n-card title="发帖" size="small" style="margin-top: 16px">
          <n-button type="primary" block @click="handleCreatePost">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            发布新帖
          </n-button>
        </n-card>
        <!-- 热门帖子 -->
        <n-card title="热门帖子" size="small" style="margin-top: 16px">
          <div v-if="hotPosts.length" class="hot-posts">
            <div v-for="(post, index) in hotPosts" :key="post.id" class="hot-post-item" @click="goDetail(post.id)">
              <span class="hot-rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <span class="hot-title">{{ post.title }}</span>
            </div>
          </div>
          <n-empty v-else description="暂无热门帖子" size="small" />
        </n-card>
      </div>
      <!-- 右侧帖子列表 -->
      <div class="page-right">
        <n-card size="small">
          <!-- 搜索和排序栏 -->
          <div class="filter-bar">
            <div class="search-box">
              <n-input v-model:value="keyword" placeholder="搜索帖子标题或内容" clearable style="width: 280px" @keyup.enter="fetchData">
                <template #prefix><n-icon><SearchOutline /></n-icon></template>
              </n-input>
              <n-button type="primary" @click="fetchData">搜索</n-button>
            </div>
            <div class="sort-box">
              <n-button-group>
                <n-button :type="sortBy === 'new' ? 'primary' : 'default'" @click="handleSortChange('new')">
                  <template #icon><n-icon><TimeOutline /></n-icon></template>
                  最新
                </n-button>
                <n-button :type="sortBy === 'hot' ? 'primary' : 'default'" @click="handleSortChange('hot')">
                  <template #icon><n-icon><FlameOutline /></n-icon></template>
                  最热
                </n-button>
                <n-button :type="sortBy === 'comment' ? 'primary' : 'default'" @click="handleSortChange('comment')">
                  <template #icon><n-icon><ChatbubblesOutline /></n-icon></template>
                  最多评论
                </n-button>
              </n-button-group>
            </div>
          </div>
          
          <!-- 帖子列表 -->
          <div class="post-list">
            <div v-for="post in postList" :key="post.id" class="post-item" @click="goDetail(post.id)">
              <div class="post-header">
                <div class="post-user">
                  <n-avatar :src="post.userAvatar" size="small" round />
                  <span class="post-author">{{ post.userName || '匿名用户' }}</span>
                </div>
                <n-tag :type="postTypeMap[post.type]?.type" size="small">{{ postTypeMap[post.type]?.text }}</n-tag>
              </div>
              <h3 class="post-title">{{ post.title }}</h3>
              <p class="post-content">{{ post.content }}</p>
              <!-- 帖子图片预览 -->
              <div v-if="post.images" class="post-images">
                <n-image
                  v-for="(img, idx) in getImageList(post.images).slice(0, 3)"
                  :key="idx"
                  :src="img"
                  width="80"
                  height="80"
                  object-fit="cover"
                  preview-disabled
                  style="border-radius: 4px; margin-right: 8px;"
                />
                <span v-if="getImageList(post.images).length > 3" class="more-images">
                  +{{ getImageList(post.images).length - 3 }}
                </span>
              </div>
              <div class="post-footer">
                <span class="stat-item">
                  <n-icon><EyeOutline /></n-icon>
                  {{ post.viewCount || 0 }}
                </span>
                <span class="stat-item">
                  <n-icon><ChatbubbleOutline /></n-icon>
                  {{ post.commentCount || 0 }}
                </span>
                <span class="stat-item time">
                  <n-icon><TimeOutline /></n-icon>
                  {{ formatTime(post.createTime) }}
                </span>
              </div>
            </div>
          </div>
          
          <n-empty v-if="!loading && !postList.length" description="暂无帖子，快来发布第一个帖子吧~" />
          
          <div class="pagination">
            <n-pagination 
              v-model:page="page" 
              :page-size="size" 
              :item-count="total" 
              show-size-picker
              :page-sizes="[10, 20, 30]"
              @update:page="fetchData"
              @update:page-size="handlePageSizeChange"
            />
          </div>
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { EyeOutline, ChatbubbleOutline, TimeOutline, SearchOutline, AddOutline, FlameOutline, ChatbubblesOutline } from '@vicons/ionicons5'
import { getPostList } from '@/api/forum'
import { formatTime, postTypeMap } from '@/utils/format'
import { useUserStore } from '@/store/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const postList = ref([])
const hotPosts = ref([])
const currentType = ref(null)
const keyword = ref('')
const sortBy = ref('new')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

const typeOptions = [
  { label: '全部', key: null },
  { label: '普通讨论', key: 0 },
  { label: '志愿者招募', key: 1 },
  { label: '产品分享', key: 2 }
]

// 获取图片列表
function getImageList(images) {
  if (!images) return []
  return images.split(',').filter(img => img)
}

function goDetail(id) { 
  router.push(`/forum/${id}`) 
}

function handleCreatePost() {
  if (!userStore.isLogin) {
    message.warning('请先登录后再发帖')
    router.push('/login')
    return
  }
  router.push('/forum/create')
}

function handleTypeChange(key) {
  currentType.value = key
  page.value = 1
  fetchData()
}

function handleSortChange(sort) {
  sortBy.value = sort
  page.value = 1
  fetchData()
}

function handlePageSizeChange(newSize) {
  size.value = newSize
  page.value = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getPostList({ 
      page: page.value, 
      size: size.value, 
      type: currentType.value, 
      keyword: keyword.value,
      sortBy: sortBy.value
    })
    postList.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

// 获取热门帖子
async function fetchHotPosts() {
  try {
    const res = await getPostList({ page: 1, size: 5, sortBy: 'hot' })
    hotPosts.value = res.records || []
  } catch (e) {
    console.error('获取热门帖子失败', e)
  }
}

onMounted(() => {
  fetchData()
  fetchHotPosts()
})
</script>

<style scoped>
.forum-list-page { 
  padding: 20px; 
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}
.page-container { 
  max-width: 1200px; 
  margin: 0 auto; 
  display: flex; 
  gap: 20px; 
}
.page-left { 
  width: 260px; 
  flex-shrink: 0; 
}
.page-right { 
  flex: 1; 
}
.filter-bar { 
  display: flex; 
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px; 
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}
.search-box {
  display: flex;
  gap: 8px;
}
.sort-box {
  display: flex;
  align-items: center;
  gap: 8px;
}
.post-list { 
  display: flex; 
  flex-direction: column; 
  gap: 16px; 
}
.post-item { 
  padding: 20px; 
  border: 1px solid #e8e8e8; 
  border-radius: 12px; 
  cursor: pointer; 
  transition: all 0.3s;
  background: #fff;
}
.post-item:hover { 
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  border-color: #18a058;
}
.post-header { 
  display: flex; 
  align-items: center; 
  justify-content: space-between;
  margin-bottom: 12px; 
}
.post-user {
  display: flex;
  align-items: center;
  gap: 8px;
}
.post-author { 
  font-size: 14px; 
  color: #333;
  font-weight: 500;
}
.post-title { 
  font-size: 18px; 
  font-weight: 600;
  margin: 12px 0 8px; 
  color: #1a1a1a;
  line-height: 1.4;
}
.post-content { 
  font-size: 14px; 
  color: #666; 
  overflow: hidden; 
  text-overflow: ellipsis; 
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.6;
  margin-bottom: 12px;
}
.post-images {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.more-images {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: #f5f5f5;
  border-radius: 4px;
  color: #666;
  font-size: 14px;
}
.post-footer { 
  display: flex; 
  gap: 24px; 
  font-size: 13px; 
  color: #999;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
.stat-item { 
  display: flex; 
  align-items: center; 
  gap: 4px; 
}
.stat-item.time {
  margin-left: auto;
}
.pagination { 
  margin-top: 24px; 
  display: flex; 
  justify-content: center; 
}

/* 热门帖子 */
.hot-posts {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.hot-post-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 0;
}
.hot-post-item:hover .hot-title {
  color: #18a058;
}
.hot-rank {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: #e8e8e8;
  color: #666;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.hot-rank.top {
  background: #ff6b6b;
  color: #fff;
}
.hot-title {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

@media (max-width: 768px) {
  .page-container {
    flex-direction: column;
  }
  .page-left {
    width: 100%;
  }
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .search-box {
    width: 100%;
  }
  .search-box .n-input {
    flex: 1;
  }
}
</style>
