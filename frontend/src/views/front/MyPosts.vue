<template>
  <div class="my-posts-page">
    <div class="page-container">
      <n-card title="我的帖子" size="small">
        <!-- 筛选栏 -->
        <div class="filter-bar">
          <div class="filter-left">
            <n-select
              v-model:value="filterType"
              :options="typeOptions"
              placeholder="帖子类型"
              clearable
              style="width: 140px"
              @update:value="handleFilter"
            />
            <n-input
              v-model:value="keyword"
              placeholder="搜索标题或内容"
              clearable
              style="width: 200px"
              @keyup.enter="handleFilter"
            >
              <template #prefix><n-icon><SearchOutline /></n-icon></template>
            </n-input>
            <n-button type="primary" @click="handleFilter">搜索</n-button>
          </div>
          <n-button type="primary" @click="handleCreate">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            发布新帖
          </n-button>
        </div>

        <!-- 帖子列表 -->
        <div class="post-list">
          <div v-for="post in postList" :key="post.id" class="post-item">
            <div class="post-header">
              <n-tag :type="postTypeMap[post.type]?.type" size="small">{{ postTypeMap[post.type]?.text }}</n-tag>
              <n-tag v-if="post.status === 0" type="warning" size="small">已禁用</n-tag>
              <span class="post-time">{{ formatTime(post.createTime) }}</span>
            </div>
            <h3 class="post-title" @click="goDetail(post.id)">{{ post.title }}</h3>
            <p class="post-content">{{ post.content }}</p>
            <!-- 帖子图片预览 -->
            <div v-if="post.images" class="post-images">
              <n-image
                v-for="(img, idx) in getImageList(post.images).slice(0, 3)"
                :key="idx"
                :src="img"
                width="60"
                height="60"
                object-fit="cover"
                preview-disabled
                style="border-radius: 4px; margin-right: 6px;"
              />
              <span v-if="getImageList(post.images).length > 3" class="more-images">
                +{{ getImageList(post.images).length - 3 }}
              </span>
            </div>
            <div class="post-footer">
              <div class="post-stats">
                <span class="stat-item">
                  <n-icon><EyeOutline /></n-icon>
                  {{ post.viewCount || 0 }}
                </span>
                <span class="stat-item">
                  <n-icon><ChatbubbleOutline /></n-icon>
                  {{ post.commentCount || 0 }}
                </span>
              </div>
              <div class="post-actions">
                <n-button text type="primary" size="small" @click="handleEdit(post)">
                  <template #icon><n-icon><CreateOutline /></n-icon></template>
                  编辑
                </n-button>
                <n-button text type="error" size="small" @click="handleDelete(post)">
                  <template #icon><n-icon><TrashOutline /></n-icon></template>
                  删除
                </n-button>
              </div>
            </div>
          </div>
        </div>

        <n-empty v-if="!loading && !postList.length" description="暂无帖子，快去发布第一个帖子吧~" />

        <div class="pagination">
          <n-pagination
            v-model:page="page"
            :page-size="size"
            :item-count="total"
            @update:page="fetchData"
          />
        </div>
      </n-card>
    </div>

    <!-- 编辑帖子弹窗 -->
    <n-modal v-model:show="showEditModal" preset="card" title="编辑帖子" style="width: 600px">
      <n-form ref="formRef" :model="editForm" :rules="formRules" label-placement="left" label-width="80">
        <n-form-item label="帖子类型" path="type">
          <n-select v-model:value="editForm.type" :options="postTypeOptions" placeholder="请选择类型" />
        </n-form-item>
        <n-form-item label="标题" path="title">
          <n-input v-model:value="editForm.title" placeholder="请输入标题" maxlength="100" show-count />
        </n-form-item>
        <n-form-item label="内容" path="content">
          <n-input
            v-model:value="editForm.content"
            type="textarea"
            placeholder="请输入内容"
            :rows="6"
            maxlength="2000"
            show-count
          />
        </n-form-item>
        <n-form-item label="图片">
          <MultiImageUpload v-model="editForm.images" :max="9" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px">
          <n-button @click="showEditModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmitEdit">保存</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import { SearchOutline, AddOutline, EyeOutline, ChatbubbleOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'
import { getMyPosts, updatePost, deletePost } from '@/api/forum'
import { formatTime, postTypeMap } from '@/utils/format'
import MultiImageUpload from '@/components/MultiImageUpload.vue'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const postList = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const filterType = ref(null)
const keyword = ref('')

// 编辑相关
const showEditModal = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editForm = ref({
  id: null,
  type: null,
  title: '',
  content: '',
  images: ''
})

const typeOptions = [
  { label: '全部类型', value: null },
  { label: '普通讨论', value: 0 },
  { label: '志愿者招募', value: 1 },
  { label: '产品分享', value: 2 }
]

const postTypeOptions = [
  { label: '普通讨论', value: 0 },
  { label: '志愿者招募', value: 1 },
  { label: '产品分享', value: 2 }
]

const formRules = {
  type: { required: true, type: 'number', message: '请选择帖子类型', trigger: 'change' },
  title: { required: true, message: '请输入标题', trigger: 'blur' },
  content: { required: true, message: '请输入内容', trigger: 'blur' }
}

// 获取图片列表
function getImageList(images) {
  if (!images) return []
  return images.split(',').filter(img => img)
}

function goDetail(id) {
  router.push(`/forum/${id}`)
}

function handleCreate() {
  router.push('/forum/create')
}

function handleFilter() {
  page.value = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyPosts({
      page: page.value,
      size: size.value,
      type: filterType.value,
      keyword: keyword.value
    })
    postList.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleEdit(post) {
  editForm.value = {
    id: post.id,
    type: post.type,
    title: post.title,
    content: post.content,
    images: post.images || ''
  }
  showEditModal.value = true
}

async function handleSubmitEdit() {
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    await updatePost(editForm.value)
    message.success('更新成功')
    showEditModal.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

function handleDelete(post) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除帖子"${post.title}"吗？删除后无法恢复。`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deletePost(post.id)
        message.success('删除成功')
        fetchData()
      } catch (e) {
        message.error('删除失败')
      }
    }
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.my-posts-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-container {
  max-width: 900px;
  margin: 0 auto;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background: #fff;
  transition: all 0.2s;
}

.post-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.post-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.post-time {
  margin-left: auto;
  font-size: 12px;
  color: #999;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  margin: 8px 0;
  color: #333;
  cursor: pointer;
  transition: color 0.2s;
}

.post-title:hover {
  color: #18a058;
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
  width: 60px;
  height: 60px;
  background: #f5f5f5;
  border-radius: 4px;
  color: #666;
  font-size: 12px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
}

.post-actions {
  display: flex;
  gap: 8px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
