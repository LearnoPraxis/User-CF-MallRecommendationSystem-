<template>
  <div class="image-upload">
    <n-upload
      :action="uploadUrl"
      :headers="headers"
      :show-file-list="false"
      accept="image/*"
      @finish="handleFinish"
      @error="handleError"
    >
      <div class="upload-trigger">
        <n-image v-if="modelValue" :src="modelValue" :width="width" :height="height" object-fit="cover" preview-disabled />
        <div v-else class="upload-placeholder" :style="{ width: width + 'px', height: height + 'px' }">
          <n-icon size="24"><AddOutline /></n-icon>
          <span>上传图片</span>
        </div>
        <div class="upload-mask" :style="{ width: width + 'px', height: height + 'px' }">
          <n-icon size="20"><CloudUploadOutline /></n-icon>
          <span>{{ modelValue ? '更换图片' : '上传图片' }}</span>
        </div>
      </div>
    </n-upload>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { AddOutline, CloudUploadOutline } from '@vicons/ionicons5'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/store/user'

const props = defineProps({
  modelValue: { type: String, default: '' },
  width: { type: Number, default: 100 },
  height: { type: Number, default: 100 }
})

const emit = defineEmits(['update:modelValue'])
const message = useMessage()
const userStore = useUserStore()

const uploadUrl = '/api/file/upload'

const headers = computed(() => ({
  Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
}))

function handleFinish({ event }) {
  try {
    const res = JSON.parse(event.target.response)
    if (res.code === 200) {
      emit('update:modelValue', res.data)
      message.success('上传成功')
    } else {
      message.error(res.message || '上传失败')
    }
  } catch (e) {
    message.error('上传失败')
  }
}

function handleError() {
  message.error('上传失败')
}
</script>

<style scoped>
.image-upload {
  display: inline-block;
}
.upload-trigger {
  position: relative;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  color: #999;
  font-size: 12px;
  gap: 4px;
}
.upload-placeholder:hover {
  border-color: #18a058;
}
.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}
.upload-trigger:hover .upload-mask {
  opacity: 1;
}
</style>
