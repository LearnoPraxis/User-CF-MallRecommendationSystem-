<template>
  <div class="home-page">
    <div class="home-container">
      <!-- 左侧分类 -->
      <div class="home-left">
        <n-card title="商品分类" size="small">
          <n-menu :options="categoryOptions" @update:value="handleCategoryClick" />
        </n-card>
        <n-card title="热门排行" size="small" style="margin-top: 16px">
          <template #header-extra>
            <n-tag type="error" size="tiny">TOP5</n-tag>
          </template>
          <div v-for="(item, index) in hotProducts.slice(0, 5)" :key="item.id" class="rank-item" @click="goDetail(item.id)">
            <span class="rank-num" :class="{ top: index < 3 }">{{ index + 1 }}</span>
            <span class="rank-name">{{ item.name }}</span>
            <span class="rank-sales">{{ item.sales }}件</span>
          </div>
          <n-empty v-if="!hotProducts.length" description="暂无数据" size="small" />
        </n-card>
        <!-- 推荐算法说明 -->
        <n-card title="智能推荐" size="small" style="margin-top: 16px">
          <div class="algorithm-info">
            <div class="algo-item">
              <n-icon color="#18a058"><SparklesOutline /></n-icon>
              <div class="algo-text">
                <div class="algo-title">协同过滤算法</div>
                <div class="algo-desc">基于用户行为相似度推荐</div>
              </div>
            </div>
            <div class="algo-item">
              <n-icon color="#f0a020"><FlameOutline /></n-icon>
              <div class="algo-text">
                <div class="algo-title">热度排序</div>
                <div class="algo-desc">销量+浏览量综合排名</div>
              </div>
            </div>
            <div class="algo-item">
              <n-icon color="#2080f0"><TimeOutline /></n-icon>
              <div class="algo-text">
                <div class="algo-title">浏览记录分析</div>
                <div class="algo-desc">基于历史偏好推荐</div>
              </div>
            </div>
          </div>
        </n-card>
      </div>
      <!-- 右侧内容 -->
      <div class="home-right">
        <!-- 猜你喜欢（协同过滤） -->
        <n-card size="small" class="recommend-card cf-card">
          <template #header>
            <div class="card-header">
              <n-icon size="20" color="#18a058"><SparklesOutline /></n-icon>
              <span class="card-title">猜你喜欢</span>
              <n-tag type="success" size="small">协同过滤算法</n-tag>
            </div>
          </template>
          <template #header-extra>
            <n-tooltip trigger="hover">
              <template #trigger>
                <n-icon size="16" style="cursor: help;"><HelpCircleOutline /></n-icon>
              </template>
              基于用户-商品评分矩阵，通过余弦相似度计算找到相似用户，推荐相似用户喜欢的商品
            </n-tooltip>
          </template>
          <div v-if="personalizedProducts.length" class="product-grid">
            <div v-for="item in personalizedProducts" :key="item.id" class="product-card" @click="goDetail(item.id)">
              <div class="product-img-wrap">
                <img :src="item.mainImage" class="product-image" />
                <span v-if="item.stock <= 0" class="sold-out">已售罄</span>
              </div>
              <div class="product-info">
                <div class="product-name">{{ item.name }}</div>
                <div class="product-bottom">
                  <span class="product-price">¥{{ item.price }}</span>
                  <span class="product-sales">{{ item.sales }}人付款</span>
                </div>
              </div>
            </div>
          </div>
          <n-empty v-else description="登录后查看个性化推荐" size="small">
            <template #extra>
              <n-button size="small" type="primary" @click="router.push('/login')">去登录</n-button>
            </template>
          </n-empty>
        </n-card>

        <!-- 热门推荐（全局热度） -->
        <n-card size="small" class="recommend-card hot-card" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <n-icon size="20" color="#f0a020"><FlameOutline /></n-icon>
              <span class="card-title">热门推荐</span>
              <n-tag type="warning" size="small">全局热度</n-tag>
            </div>
          </template>
          <template #header-extra>
            <n-button text type="primary" @click="router.push('/product')">查看更多</n-button>
          </template>
          <div v-if="hotProducts.length" class="product-grid">
            <div v-for="item in hotProducts" :key="item.id" class="product-card" @click="goDetail(item.id)">
              <div class="product-img-wrap">
                <img :src="item.mainImage" class="product-image" />
                <span v-if="item.stock <= 0" class="sold-out">已售罄</span>
              </div>
              <div class="product-info">
                <div class="product-name">{{ item.name }}</div>
                <div class="product-bottom">
                  <span class="product-price">¥{{ item.price }}</span>
                  <span class="product-sales">{{ item.sales }}人付款</span>
                </div>
              </div>
            </div>
          </div>
          <n-empty v-else description="暂无商品" size="small" />
        </n-card>

        <!-- 根据浏览记录推荐 -->
        <n-card v-if="historyProducts.length" size="small" class="recommend-card history-card" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <n-icon size="20" color="#2080f0"><TimeOutline /></n-icon>
              <span class="card-title">根据您最近浏览</span>
              <n-tag type="info" size="small">浏览记录分析</n-tag>
            </div>
          </template>
          <template #header-extra>
            <n-tooltip trigger="hover">
              <template #trigger>
                <n-icon size="16" style="cursor: help;"><HelpCircleOutline /></n-icon>
              </template>
              分析您的浏览历史，推荐同类别的热门商品
            </n-tooltip>
          </template>
          <div class="product-grid">
            <div v-for="item in historyProducts" :key="item.id" class="product-card" @click="goDetail(item.id)">
              <div class="product-img-wrap">
                <img :src="item.mainImage" class="product-image" />
              </div>
              <div class="product-info">
                <div class="product-name">{{ item.name }}</div>
                <div class="product-bottom">
                  <span class="product-price">¥{{ item.price }}</span>
                  <span class="product-sales">{{ item.sales }}人付款</span>
                </div>
              </div>
            </div>
          </div>
        </n-card>

        <!-- 最新上架 -->
        <n-card size="small" class="recommend-card" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <n-icon size="20" color="#d03050"><RocketOutline /></n-icon>
              <span class="card-title">最新上架</span>
              <n-tag type="error" size="small">新品</n-tag>
            </div>
          </template>
          <div v-if="newProducts.length" class="product-grid">
            <div v-for="item in newProducts" :key="item.id" class="product-card" @click="goDetail(item.id)">
              <div class="product-img-wrap">
                <img :src="item.mainImage" class="product-image" />
                <span class="new-tag">NEW</span>
              </div>
              <div class="product-info">
                <div class="product-name">{{ item.name }}</div>
                <div class="product-bottom">
                  <span class="product-price">¥{{ item.price }}</span>
                  <span class="product-sales">{{ item.sales }}人付款</span>
                </div>
              </div>
            </div>
          </div>
          <n-empty v-else description="暂无商品" size="small" />
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { SparklesOutline, FlameOutline, TimeOutline, RocketOutline, HelpCircleOutline } from '@vicons/ionicons5'
import { getCategoryTree } from '@/api/category'
import { getNewProducts } from '@/api/product'
import { getPersonalizedRecommend, getHotRecommend, getHistoryRecommend } from '@/api/recommend'

const router = useRouter()
const categoryOptions = ref([])
const personalizedProducts = ref([])
const hotProducts = ref([])
const historyProducts = ref([])
const newProducts = ref([])

function goDetail(id) {
  router.push(`/product/${id}`)
}

function handleCategoryClick(key) {
  router.push({ path: '/product', query: { categoryId: key } })
}

async function fetchData() {
  try {
    // 获取分类
    const categories = await getCategoryTree()
    categoryOptions.value = categories.map(c => ({
      label: c.name, key: c.id,
      children: c.children?.map(cc => ({ label: cc.name, key: cc.id }))
    }))
    // 获取推荐数据
    const [personalized, hot, history, newList] = await Promise.all([
      getPersonalizedRecommend(8).catch(() => []),
      getHotRecommend(8).catch(() => []),
      getHistoryRecommend(8).catch(() => []),
      getNewProducts(8).catch(() => [])
    ])
    personalizedProducts.value = personalized || []
    hotProducts.value = hot || []
    historyProducts.value = history || []
    newProducts.value = newList || []
  } catch (e) {
    console.error('获取数据失败', e)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.home-page { padding: 20px; background: #f5f7fa; min-height: calc(100vh - 60px); }
.home-container { max-width: 1400px; margin: 0 auto; display: flex; gap: 20px; }
.home-left { width: 260px; flex-shrink: 0; }
.home-right { flex: 1; }

/* 排行榜 */
.rank-item { display: flex; align-items: center; gap: 8px; padding: 10px 0; cursor: pointer; border-bottom: 1px dashed #eee; }
.rank-item:last-child { border-bottom: none; }
.rank-item:hover .rank-name { color: #18a058; }
.rank-num { width: 20px; height: 20px; border-radius: 4px; background: #e8e8e8; color: #666; font-size: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.rank-num.top { background: linear-gradient(135deg, #ff6b6b, #ee5a24); color: #fff; }
.rank-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; }
.rank-sales { font-size: 12px; color: #999; }

/* 算法说明 */
.algorithm-info { display: flex; flex-direction: column; gap: 12px; }
.algo-item { display: flex; align-items: flex-start; gap: 10px; }
.algo-text { flex: 1; }
.algo-title { font-size: 13px; font-weight: 500; color: #333; }
.algo-desc { font-size: 12px; color: #999; margin-top: 2px; }

/* 推荐卡片 */
.recommend-card { border-radius: 8px; }
.card-header { display: flex; align-items: center; gap: 8px; }
.card-title { font-size: 16px; font-weight: 600; color: #333; }

/* 商品网格 */
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.product-card { background: #fff; border-radius: 8px; overflow: hidden; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; border: 1px solid #eee; }
.product-card:hover { transform: translateY(-4px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }

.product-img-wrap { position: relative; }
.product-image { width: 100%; height: 160px; object-fit: cover; display: block; }
.sold-out { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.new-tag { position: absolute; top: 8px; left: 8px; background: linear-gradient(135deg, #f0a020, #ff6b6b); color: #fff; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 500; }

.product-info { padding: 12px; }
.product-name { font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #333; }
.product-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.product-price { color: #e53935; font-size: 16px; font-weight: bold; }
.product-sales { color: #999; font-size: 12px; }

@media (max-width: 1200px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 900px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
  .home-container { flex-direction: column; }
  .home-left { width: 100%; }
}
</style>
