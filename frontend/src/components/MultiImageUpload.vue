<template>
  <div class="multi-image-upload">
    <div class="image-list">
      <!-- 已上传的图片 -->
      <div v-for="(url, index) in imageList" :key="index" class="image-item">
        <n-image :src="url" width="80" height="80" object-fit="cover" preview-disabled />
        <div class="image-mask">
          <n-icon size="18" @click.stop="handleRemove(index)"><CloseOutline /></n-icon>
        </div>
      </div>
      <!-- 上传按钮 -->
      <n-upload
        v-if="imageList.length < max"
        :action="uploadUrl"
        :headers="headers"
        :show-file-list="false"
        accept="image/*"
        @finish="handleFinish"
        @error="handleError"
      >
        <div class="upload-btn">
          <n-icon size="24"><AddOutline /></n-icon>
          <span>上传</span>
        </div>
      </n-upload>
    </div>
    <div class="upload-tip">最多上传 {{ max }} 张图片</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { AddOutline, CloseOutline } from '@vicons/ionicons5'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/store/user'

const props = defineProps({
  modelValue: { type: String, default: '' },  // 逗号分隔的图片URL
  max: { type: Number, default: 5 }
})

const emit = defineEmits(['update:modelValue'])
const message = useMessage()
const userStore = useUserStore()

const uploadUrl = '/api/file/upload'

const headers = computed(() => ({
  Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
}))

// 图片列表
const imageList = computed(() => {
  if (!props.modelValue) return []
  return props.modelValue.split(',').filter(url => url)
})

// 上传成功
function handleFinish({ event }) {
  try {
    const res = JSON.parse(event.target.response)
    if (res.code === 200) {
      const newList = [...imageList.value, res.data]
      emit('update:modelValue', newList.join(','))
      message.success('上传成功')
    } else {
      message.error(res.message || '上传失败')
    }
  } catch (e) {
    message.error('上传失败')
  }
}

// 上传失败
function handleError() {
  message.error('上传失败')
}

// 删除图片
function handleRemove(index) {
  const newList = [...imageList.value]
  newList.splice(index, 1)
  emit('update:modelValue', newList.join(','))
}
</script>

<style scoped>
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.image-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
}
.image-mask {
  position: absolute;
  top: 0;
  right: 0;
  padding: 4px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  cursor: pointer;
  border-radius: 0 0 0 4px;
  opacity: 0;
  transition: opacity 0.3s;
}
.image-item:hover .image-mask {
  opacity: 1;
}
.upload-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  color: #999;
  font-size: 12px;
  cursor: pointer;
  gap: 4px;
}
.upload-btn:hover {
  border-color: #18a058;
}
.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}
</style>
