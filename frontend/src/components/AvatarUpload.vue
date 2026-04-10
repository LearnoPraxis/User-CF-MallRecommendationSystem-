<template>
  <div class="avatar-upload">
    <n-upload
      :action="uploadUrl"
      :headers="headers"
      :show-file-list="false"
      accept="image/*"
      @finish="handleFinish"
      @error="handleError"
    >
      <div class="avatar-wrapper">
        <n-avatar v-if="modelValue" :src="modelValue" :size="size" round />
        <div v-else class="avatar-placeholder" :style="{ width: size + 'px', height: size + 'px' }">
          <n-icon size="24"><CameraOutline /></n-icon>
        </div>
        <div class="avatar-mask" :style="{ width: size + 'px', height: size + 'px' }">
          <n-icon size="20"><CameraOutline /></n-icon>
          <span>更换头像</span>
        </div>
      </div>
    </n-upload>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { CameraOutline } from '@vicons/ionicons5'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/store/user'

const props = defineProps({
  modelValue: { type: String, default: '' },
  size: { type: Number, default: 80 }
})

const emit = defineEmits(['update:modelValue'])
const message = useMessage()
const userStore = useUserStore()

// 上传地址
const uploadUrl = '/api/file/upload'

// 请求头带token
const headers = computed(() => ({
  Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
}))

// 上传成功
function handleFinish({ file, event }) {
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

// 上传失败
function handleError() {
  message.error('上传失败')
}
</script>

<style scoped>
.avatar-upload {
  display: inline-block;
}
.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}
.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 50%;
  color: #999;
}
.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}
</style>
