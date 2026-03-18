<template>
  <div class="product-detail-page">
    <div class="page-container">
      <n-card v-if="product">
        <div class="product-main">
          <!-- 商品图片 -->
          <div class="product-gallery">
            <img :src="currentImage" class="main-image" />
            <div class="thumb-list">
              <img v-for="(img, index) in imageList" :key="index" :src="img" 
                :class="['thumb-item', { active: currentImage === img }]" @click="currentImage = img" />
            </div>
          </div>
          <!-- 商品信息 -->
          <div class="product-info">
            <h1 class="product-name">{{ product.name }}</h1>
            <p class="product-desc">{{ product.description }}</p>
            <div class="price-box">
              <span class="current-price">¥{{ product.price }}</span>
              <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
            </div>
            <div class="info-row"><span class="label">销量</span><span>{{ product.sales }}</span></div>
            <div class="info-row"><span class="label">库存</span><span>{{ product.stock }}</span></div>
            <div class="info-row"><span class="label">浏览</span><span>{{ product.viewCount }}</span></div>
            <div class="action-box">
              <n-input-number v-model:value="quantity" :min="1" :max="product.stock" />
              <n-button type="primary" size="large" @click="handleAddCart" :disabled="product.stock <= 0">
                {{ product.stock <= 0 ? '已售罄' : '加入购物车' }}
              </n-button>
              <n-button size="large" :type="isFavorite ? 'error' : 'default'" @click="handleFavorite">
                <template #icon><n-icon :color="isFavorite ? '#e53935' : ''"><Heart v-if="isFavorite" /><HeartOutline v-else /></n-icon></template>
                {{ isFavorite ? '已收藏' : '收藏' }}
              </n-button>
            </div>
          </div>
        </div>
        <!-- 商品详情 -->
        <n-divider>商品详情</n-divider>
        <div class="product-detail" v-html="product.detail"></div>
        <!-- 商品留言 -->
        <n-divider>商品留言</n-divider>
        <div class="comment-section">
          <div v-if="userStore.isLogin" class="comment-form">
            <div class="comment-form-row">
              <span class="form-label">评分：</span>
              <n-rate v-model:value="commentForm.rating" :count="5" />
              <span class="rating-text">{{ ratingText }}</span>
            </div>
            <div class="comment-form-row">
              <n-input v-model:value="commentForm.content" type="textarea" placeholder="写下您的评价..." :rows="3" />
            </div>
            <div class="comment-form-row">
              <n-button type="primary" @click="handleAddComment">发表评价</n-button>
            </div>
          </div>
          <div v-else class="login-tip">
            <n-button type="primary" text @click="$router.push('/login')">登录</n-button>
            <span>后发表评价</span>
          </div>
          <n-list bordered>
            <n-list-item v-for="item in commentList" :key="item.id">
              <n-thing>
                <template #avatar><n-avatar :src="item.userAvatar" /></template>
                <template #header>{{ item.userName }}</template>
                <template #header-extra><n-rate :value="item.rating" readonly size="small" /></template>
                <template #description>{{ formatTime(item.createTime) }}</template>
                {{ item.content }}
                <div v-if="item.replyContent" class="reply-box">
                  <n-tag type="info" size="small">商家回复</n-tag> {{ item.replyContent }}
                </div>
              </n-thing>
            </n-list-item>
          </n-list>
          <n-empty v-if="!commentList.length" description="暂无评价" />
          <div v-if="commentTotal > 5" class="pagination">
            <n-pagination v-model:page="commentPage" :page-size="5" :item-count="commentTotal" @update:page="fetchComments" />
          </div>
        </div>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { HeartOutline, Heart } from '@vicons/ionicons5'
import { useUserStore } from '@/store/user'
import { getProductDetail } from '@/api/product'
import { addToCart } from '@/api/cart'
import { addFavorite, removeFavorite } from '@/api/favorite'
import { getCommentList, addComment } from '@/api/comment'
import { formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const product = ref(null)
const isFavorite = ref(false)
const currentImage = ref('')
const quantity = ref(1)
const commentList = ref([])
const commentPage = ref(1)
const commentTotal = ref(0)
const commentForm = ref({ rating: 5, content: '' })

// 评分文字
const ratingText = computed(() => {
  const texts = ['', '很差', '较差', '一般', '满意', '非常满意']
  return texts[commentForm.value.rating] || ''
})

const imageList = computed(() => {
  if (!product.value) return []
  const images = product.value.images ? product.value.images.split(',') : []
  return [product.value.mainImage, ...images].filter(Boolean)
})

async function fetchProduct() {
  const id = route.params.id
  const data = await getProductDetail(id)
  product.value = data.product
  isFavorite.value = data.isFavorite
  currentImage.value = product.value.mainImage
}

async function fetchComments() {
  const id = route.params.id
  const res = await getCommentList(id, { page: commentPage.value, size: 5 })
  commentList.value = res.records || []
  commentTotal.value = res.total || 0
}

async function handleAddCart() {
  if (!userStore.isLogin) return message.warning('请先登录')
  await addToCart({ productId: product.value.id, quantity: quantity.value })
  message.success('已加入购物车')
  // 触发购物车数量更新
  window.dispatchEvent(new CustomEvent('cart-updated'))
}

async function handleFavorite() {
  if (!userStore.isLogin) return message.warning('请先登录')
  if (isFavorite.value) {
    await removeFavorite(product.value.id)
    isFavorite.value = false
    message.success('已取消收藏')
  } else {
    await addFavorite(product.value.id)
    isFavorite.value = true
    message.success('收藏成功')
  }
}

async function handleAddComment() {
  if (!commentForm.value.content) return message.warning('请输入评价内容')
  await addComment({ productId: product.value.id, ...commentForm.value })
  message.success('评价成功')
  commentForm.value.content = ''
  fetchComments()
}

onMounted(() => { fetchProduct(); fetchComments() })
</script>

<style scoped>
.product-detail-page { padding: 20px; }
.page-container { max-width: 1200px; margin: 0 auto; }
.product-main { display: flex; gap: 40px; }
.product-gallery { width: 400px; flex-shrink: 0; }
.main-image { width: 100%; height: 400px; object-fit: cover; border-radius: 8px; }
.thumb-list { display: flex; gap: 8px; margin-top: 12px; }
.thumb-item { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; cursor: pointer; border: 2px solid transparent; }
.thumb-item.active { border-color: #18a058; }
.product-info { flex: 1; }
.product-name { font-size: 24px; margin-bottom: 12px; }
.product-desc { color: #666; margin-bottom: 20px; }
.price-box { background: #fff7e6; padding: 16px; border-radius: 8px; margin-bottom: 20px; }
.current-price { font-size: 28px; color: #e53935; font-weight: bold; }
.original-price { font-size: 16px; color: #999; text-decoration: line-through; margin-left: 12px; }
.info-row { padding: 8px 0; border-bottom: 1px dashed #eee; }
.info-row .label { color: #999; margin-right: 20px; }
.action-box { display: flex; gap: 16px; margin-top: 30px; align-items: center; }
.product-detail { padding: 20px 0; }
.comment-section { padding: 20px 0; }
.comment-form { background: #f9f9f9; padding: 16px; border-radius: 8px; margin-bottom: 20px; }
.comment-form-row { margin-bottom: 12px; display: flex; align-items: center; gap: 12px; }
.comment-form-row:last-child { margin-bottom: 0; }
.form-label { color: #666; flex-shrink: 0; }
.rating-text { color: #f0a020; font-size: 14px; }
.login-tip { background: #f9f9f9; padding: 16px; border-radius: 8px; margin-bottom: 20px; text-align: center; }
.reply-box { margin-top: 8px; padding: 8px; background: #f5f5f5; border-radius: 4px; }
.pagination { margin-top: 16px; display: flex; justify-content: center; }
</style>
