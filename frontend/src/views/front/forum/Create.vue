<template>
  <div class="forum-create-page">
    <div class="page-container">
      <!-- 返回按钮 -->
      <div class="back-bar">
        <n-button text @click="router.push('/forum')">
          <template #icon><n-icon><ArrowBackOutline /></n-icon></template>
          返回论坛
        </n-button>
      </div>
      
      <n-card title="发布帖子" class="create-card">
        <template #header-extra>
          <n-text depth="3">分享你的想法</n-text>
        </template>
        
        <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
          <n-form-item label="帖子类型" path="type">
            <n-radio-group v-model:value="form.type">
              <n-space>
                <n-radio :value="0">
                  <n-tag type="default" size="small">普通讨论</n-tag>
                </n-radio>
                <n-radio :value="1">
                  <n-tag type="success" size="small">志愿者招募</n-tag>
                </n-radio>
                <n-radio :value="2">
                  <n-tag type="info" size="small">产品分享</n-tag>
                </n-radio>
              </n-space>
            </n-radio-group>
          </n-form-item>
          
          <n-form-item label="帖子标题" path="title">
            <n-input 
              v-model:value="form.title" 
              placeholder="请输入一个吸引人的标题" 
              maxlength="100" 
              show-count 
            />
          </n-form-item>
          
          <n-form-item label="帖子内容" path="content">
            <n-input 
              v-model:value="form.content" 
              type="textarea" 
              placeholder="请详细描述你想分享的内容..." 
              :rows="12"
              :maxlength="5000"
              show-count
            />
          </n-form-item>
          
          <n-form-item label="添加图片（可选，最多5张）">
            <n-upload 
              :custom-request="handleUpload" 
              list-type="image-card" 
              :max="5" 
              v-model:file-list="fileList"
              accept="image/*"
            >
              <div class="upload-trigger">
                <n-icon size="24"><ImageOutline /></n-icon>
                <span>点击上传</span>
              </div>
            </n-upload>
          </n-form-item>
          
          <n-form-item>
            <n-space>
              <n-button type="primary" size="large" :loading="loading" @click="handleSubmit">
                <template #icon><n-icon><SendOutline /></n-icon></template>
                发布帖子
              </n-button>
              <n-button size="large" @click="router.back()">取消</n-button>
            </n-space>
          </n-form-item>
        </n-form>
      </n-card>
      
      <!-- 发帖提示 -->
      <n-card title="发帖须知" size="small" class="tips-card">
        <n-ul>
          <n-li>请遵守社区规范，文明发言</n-li>
          <n-li>标题要简洁明了，内容要详细具体</n-li>
          <n-li>上传的图片大小不超过5MB</n-li>
          <n-li>禁止发布广告、违法违规内容</n-li>
        </n-ul>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { createPost } from '@/api/forum'
import { uploadFile } from '@/api/upload'
import { useUserStore } from '@/store/user'
import { ArrowBackOutline, SendOutline, ImageOutline } from '@vicons/ionicons5'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const fileList = ref([])
const form = ref({ type: 0, title: '', content: '', images: '' })

const rules = {
  type: { required: true, type: 'number', message: '请选择帖子类型', trigger: 'change' },
  title: { required: true, message: '请输入帖子标题', trigger: 'blur' },
  content: { required: true, message: '请输入帖子内容', trigger: 'blur' }
}

async function handleUpload({ file, onFinish, onError }) {
  try {
    const url = await uploadFile(file.file)
    file.url = url
    onFinish()
  } catch (e) {
    message.error('图片上传失败')
    onError()
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }
  
  loading.value = true
  try {
    form.value.images = fileList.value.map(f => f.url).filter(Boolean).join(',')
    await createPost(form.value)
    message.success('发布成功')
    router.push('/forum')
  } catch (e) {
    message.error(e.message || '发布失败')
  } finally {
    loading.value = false
  }
}

// 检查登录状态
onMounted(() => {
  if (!userStore.isLogin) {
    message.warning('请先登录后再发帖')
    router.push('/login')
  }
})
</script>

<style scoped>
.forum-create-page { 
  padding: 20px; 
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}
.page-container { 
  max-width: 800px; 
  margin: 0 auto; 
}
.back-bar {
  margin-bottom: 16px;
}
.create-card {
  margin-bottom: 20px;
}
.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #666;
  font-size: 12px;
}
.tips-card {
  background: #fffbe6;
}
.tips-card :deep(.n-card-header) {
  padding-bottom: 8px;
}
.tips-card :deep(.n-ul) {
  margin: 0;
  padding-left: 20px;
}
.tips-card :deep(.n-li) {
  color: #666;
  font-size: 13px;
  line-height: 1.8;
}
</style>
