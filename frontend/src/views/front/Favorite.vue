<template>
  <div class="favorite-page">
    <div class="page-container">
      <n-card title="我的收藏">
        <template #header-extra>
          <span class="total-count">共 {{ total }} 件商品</span>
        </template>
        <div v-if="favoriteList.length" class="product-grid">
          <div v-for="item in favoriteList" :key="item.id" class="product-card">
            <n-button class="remove-btn" circle size="tiny" @click.stop="handleRemove(item.productId)">
              <template #icon><n-icon><CloseOutline /></n-icon></template>
            </n-button>
            <div class="product-img-wrap" @click="goDetail(item.productId)">
              <img :src="item.product?.mainImage" class="product-image" />
              <span v-if="item.product && item.product.stock !== undefined && item.product.stock <= 0" class="sold-out">已售罄</span>
            </div>
            <div class="product-info">
              <div class="product-name" @click="goDetail(item.productId)">{{ item.product?.name }}</div>
              <div class="product-bottom">
                <span class="product-price">¥{{ item.product?.price }}</span>
                <span class="product-sales">{{ item.product?.sales || 0 }}人付款</span>
              </div>
              <div class="product-actions">
                <n-button type="primary" size="small" :disabled="!item.product || item.product.stock === undefined || item.product.stock <= 0" @click="handleAddCart(item)">
                  <template #icon><n-icon><CartOutline /></n-icon></template>
                  加入购物车
                </n-button>
              </div>
            </div>
          </div>
        </div>
        <n-empty v-else description="暂无收藏，去逛逛吧~" style="padding: 60px 0;">
          <template #extra>
            <n-button type="primary" @click="router.push('/product')">去逛逛</n-button>
          </template>
        </n-empty>
        <div v-if="total > size" class="pagination">
          <n-pagination v-model:page="page" :page-size="size" :item-count="total" @update:page="fetchData" />
        </div>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { CloseOutline, CartOutline } from '@vicons/ionicons5'
import { getFavoriteList, removeFavorite } from '@/api/favorite'
import { addToCart } from '@/api/cart'
import { useUserStore } from '@/store/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const favoriteList = ref([])
const page = ref(1)
const size = ref(12)
const total = ref(0)

function goDetail(id) { router.push(`/product/${id}`) }

async function fetchData() {
  try {
    const res = await getFavoriteList({ page: page.value, size: size.value })
    favoriteList.value = res.records || []
    total.value = res.total || 0
  } catch (e) {}
}

async function handleRemove(productId) {
  await removeFavorite(productId)
  message.success('已取消收藏')
  fetchData()
}

async function handleAddCart(item) {
  try {
    await addToCart({ productId: item.productId, quantity: 1 })
    message.success('已加入购物车')
    window.dispatchEvent(new CustomEvent('cart-updated'))
  } catch (e) {}
}

onMounted(() => {
  if (!userStore.isLogin) {
    message.warning('请先登录')
    router.push('/login?redirect=/favorite')
    return
  }
  fetchData()
})
</script>

<style scoped>
.favorite-page { padding: 20px; }
.page-container { max-width: 1200px; margin: 0 auto; }

.total-count { font-size: 14px; color: #666; }

.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }

.product-card { position: relative; background: #fff; border-radius: 8px; overflow: hidden; border: 1px solid #eee; transition: box-shadow 0.2s; }
.product-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }

.remove-btn { position: absolute; top: 8px; right: 8px; z-index: 1; background: rgba(255,255,255,0.9); }

.product-img-wrap { position: relative; cursor: pointer; }
.product-image { width: 100%; height: 180px; object-fit: cover; display: block; }
.sold-out { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; }

.product-info { padding: 12px; }
.product-name { font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; cursor: pointer; }
.product-name:hover { color: #18a058; }
.product-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.product-price { color: #e53935; font-size: 16px; font-weight: bold; }
.product-sales { color: #999; font-size: 12px; }
.product-actions { margin-top: 12px; }

.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
