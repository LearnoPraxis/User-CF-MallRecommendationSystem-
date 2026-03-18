<template>
  <div class="product-list-page">
    <div class="page-container">
      <!-- 左侧筛选 -->
      <div class="page-left">
        <n-card title="商品分类" size="small">
          <n-menu 
            :value="currentCategory" 
            :options="categoryOptions" 
            :default-expanded-keys="expandedKeys"
            @update:value="handleCategoryChange" 
          />
        </n-card>
        <!-- 热门商品 -->
        <n-card title="热门商品" size="small" style="margin-top: 16px;">
          <div v-if="hotProducts.length" class="hot-products">
            <div v-for="item in hotProducts" :key="item.id" class="hot-product-item" @click="goDetail(item.id)">
              <img :src="item.mainImage" class="hot-product-img" />
              <div class="hot-product-info">
                <div class="hot-product-name">{{ item.name }}</div>
                <div class="hot-product-price">¥{{ item.price }}</div>
              </div>
            </div>
          </div>
          <n-empty v-else description="暂无热门商品" size="small" />
        </n-card>
      </div>
      <!-- 右侧商品列表 -->
      <div class="page-right">
        <n-card size="small">
          <!-- 搜索 -->
          <div class="search-bar">
            <n-input-group>
              <n-input v-model:value="keyword" placeholder="搜索商品" clearable style="width: 300px" @keyup.enter="handleSearch" />
              <n-button type="primary" @click="handleSearch">搜索</n-button>
            </n-input-group>
          </div>
          <!-- 排序和价格筛选 -->
          <div class="filter-bar">
            <div class="sort-bar">
              <span class="filter-label">排序：</span>
              <n-button-group size="small">
                <n-button :type="sortBy === '' ? 'primary' : 'default'" @click="handleSort('')">综合</n-button>
                <n-button :type="sortBy === 'sales' ? 'primary' : 'default'" @click="handleSort('sales')">销量</n-button>
                <n-button :type="sortBy === 'price_asc' ? 'primary' : 'default'" @click="handleSort('price_asc')">价格↑</n-button>
                <n-button :type="sortBy === 'price_desc' ? 'primary' : 'default'" @click="handleSort('price_desc')">价格↓</n-button>
                <n-button :type="sortBy === 'new' ? 'primary' : 'default'" @click="handleSort('new')">最新</n-button>
              </n-button-group>
            </div>
            <div class="price-filter">
              <span class="filter-label">价格：</span>
              <n-input v-model:value="minPriceInput" placeholder="最低" size="small" style="width: 80px" />
              <span class="price-sep">-</span>
              <n-input v-model:value="maxPriceInput" placeholder="最高" size="small" style="width: 80px" />
              <n-button size="small" type="primary" @click="handlePriceFilter">确定</n-button>
              <n-button size="small" @click="handleClearPrice">清除</n-button>
            </div>
          </div>
          <!-- 商品数量 -->
          <div class="result-info">
            共找到 <b>{{ total }}</b> 件商品
          </div>
          <!-- 商品列表 -->
          <div v-if="productList.length" class="product-grid">
            <div v-for="item in productList" :key="item.id" class="product-card" @click="goDetail(item.id)">
              <div class="product-img-wrap">
                <img :src="item.mainImage" class="product-image" />
                <span v-if="item.stock <= 0" class="sold-out">已售罄</span>
                <span v-else-if="item.stock <= 10" class="low-stock">仅剩{{ item.stock }}件</span>
              </div>
              <div class="product-info">
                <div class="product-name">{{ item.name }}</div>
                <div class="product-desc">{{ item.description || '暂无描述' }}</div>
                <div class="product-bottom">
                  <div class="price-wrap">
                    <span class="product-price">¥{{ item.price }}</span>
                    <span v-if="item.originalPrice && item.originalPrice > item.price" class="original-price">¥{{ item.originalPrice }}</span>
                  </div>
                  <span class="product-sales">{{ item.sales }}人付款</span>
                </div>
              </div>
            </div>
          </div>
          <n-empty v-else description="暂无商品" style="padding: 60px 0;" />
          <!-- 分页 -->
          <div v-if="total > 0" class="pagination">
            <n-pagination v-model:page="page" :page-size="size" :item-count="total" show-quick-jumper @update:page="fetchData" />
          </div>
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCategoryTree } from '@/api/category'
import { getProductList } from '@/api/product'

const router = useRouter()
const route = useRoute()

const categoryOptions = ref([])
const expandedKeys = ref([])
const currentCategory = ref(null)
const keyword = ref('')
const minPriceInput = ref('')
const maxPriceInput = ref('')
const minPrice = ref(null)
const maxPrice = ref(null)
const sortBy = ref('')
const productList = ref([])
const page = ref(1)
const size = ref(12)
const total = ref(0)
const hotProducts = ref([])

function goDetail(id) {
  router.push(`/product/${id}`)
}

function handleCategoryChange(key) {
  currentCategory.value = key
  page.value = 1
  fetchData()
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleSort(sort) {
  sortBy.value = sort
  page.value = 1
  fetchData()
}

function handlePriceFilter() {
  minPrice.value = minPriceInput.value ? Number(minPriceInput.value) : null
  maxPrice.value = maxPriceInput.value ? Number(maxPriceInput.value) : null
  page.value = 1
  fetchData()
}

function handleClearPrice() {
  minPriceInput.value = ''
  maxPriceInput.value = ''
  minPrice.value = null
  maxPrice.value = null
  page.value = 1
  fetchData()
}

async function fetchData() {
  try {
    const params = {
      page: page.value,
      size: size.value,
      categoryId: currentCategory.value || undefined,
      keyword: keyword.value || undefined,
      minPrice: minPrice.value || undefined,
      maxPrice: maxPrice.value || undefined,
      sortBy: sortBy.value || undefined
    }
    const res = await getProductList(params)
    productList.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error('获取商品列表失败', e)
  }
}

async function fetchCategories() {
  try {
    const categories = await getCategoryTree()
    // 收集所有分类ID用于默认展开
    const keys = []
    categoryOptions.value = [
      { label: '全部分类', key: null },
      ...categories.map(c => {
        keys.push(c.id)
        return {
          label: c.name, 
          key: c.id,
          children: c.children?.map(cc => ({ label: cc.name, key: cc.id }))
        }
      })
    ]
    expandedKeys.value = keys
  } catch (e) {}
}

// 获取热门商品
async function fetchHotProducts() {
  try {
    const res = await getProductList({ page: 1, size: 5, sortBy: 'sales' })
    hotProducts.value = res.records || []
  } catch (e) {}
}

watch(() => route.query.categoryId, (val) => {
  currentCategory.value = val ? Number(val) : null
  page.value = 1
  fetchData()
}, { immediate: true })

watch(() => route.query.keyword, (val) => {
  keyword.value = val || ''
  page.value = 1
  fetchData()
}, { immediate: true })

onMounted(() => {
  fetchCategories()
  fetchHotProducts()
})
</script>

<style scoped>
.product-list-page { padding: 20px; }
.page-container { max-width: 1400px; margin: 0 auto; display: flex; gap: 20px; }
.page-left { width: 240px; flex-shrink: 0; }
.page-right { flex: 1; }

.search-bar { margin-bottom: 16px; }

.filter-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding: 12px; background: #f9f9f9; border-radius: 6px; flex-wrap: wrap; gap: 12px; }
.sort-bar { display: flex; align-items: center; gap: 8px; }
.price-filter { display: flex; align-items: center; gap: 8px; }
.filter-label { color: #666; font-size: 14px; flex-shrink: 0; }
.price-sep { color: #999; }

.result-info { font-size: 14px; color: #666; margin-bottom: 16px; }
.result-info b { color: #18a058; }

.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.product-card { background: #fff; border-radius: 8px; overflow: hidden; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; border: 1px solid #eee; }
.product-card:hover { transform: translateY(-4px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }

.product-img-wrap { position: relative; }
.product-image { width: 100%; height: 180px; object-fit: cover; display: block; }
.sold-out { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.low-stock { position: absolute; bottom: 0; left: 0; right: 0; background: rgba(208, 48, 80, 0.9); color: #fff; text-align: center; font-size: 12px; padding: 4px 0; }

.product-info { padding: 12px; }
.product-name { font-size: 14px; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #333; }
.product-desc { font-size: 12px; color: #999; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-bottom { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 10px; }
.price-wrap { display: flex; align-items: baseline; gap: 6px; }
.product-price { color: #e53935; font-size: 18px; font-weight: bold; }
.original-price { color: #999; font-size: 12px; text-decoration: line-through; }
.product-sales { color: #999; font-size: 12px; }

.pagination { margin-top: 20px; display: flex; justify-content: center; }

/* 热门商品 */
.hot-products { display: flex; flex-direction: column; gap: 12px; }
.hot-product-item { display: flex; gap: 10px; cursor: pointer; padding: 8px; border-radius: 6px; transition: background 0.2s; }
.hot-product-item:hover { background: #f5f5f5; }
.hot-product-img { width: 50px; height: 50px; object-fit: cover; border-radius: 4px; flex-shrink: 0; }
.hot-product-info { flex: 1; min-width: 0; }
.hot-product-name { font-size: 13px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hot-product-price { font-size: 14px; color: #e53935; font-weight: bold; margin-top: 4px; }
</style>
