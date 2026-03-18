<template>
  <n-layout class="layout">
    <!-- 顶部导航栏 -->
    <n-layout-header bordered class="header">
      <div class="header-content">
        <!-- 左侧：Logo + 导航 -->
        <div class="header-left">
          <div class="logo" @click="router.push('/')">
            <n-icon size="28" color="#18a058"><StorefrontOutline /></n-icon>
            <span>商城推荐系统</span>
          </div>
          <div class="nav-menu">
            <n-menu mode="horizontal" :value="activeMenu" :options="menuOptions" />
          </div>
        </div>
        <!-- 右侧：搜索 + 购物车 + 用户 -->
        <div class="header-right">
          <!-- 搜索框 -->
          <div class="search-box">
            <n-input v-model:value="searchKeyword" placeholder="搜索商品" clearable @keyup.enter="handleSearch">
              <template #suffix>
                <n-icon style="cursor: pointer;" @click="handleSearch"><SearchOutline /></n-icon>
              </template>
            </n-input>
          </div>
          <template v-if="userStore.isLogin">
            <!-- 购物车 -->
            <n-badge :value="cartCount" :max="99" :show="cartCount > 0">
              <n-button quaternary circle size="large" @click="router.push('/cart')">
                <template #icon><n-icon size="22"><CartOutline /></n-icon></template>
              </n-button>
            </n-badge>
            <!-- 用户信息 -->
            <n-dropdown :options="userMenuOptions" @select="handleUserMenu">
              <div class="user-info">
                <n-avatar round size="small" :src="userStore.userInfo?.avatar">
                  <template #fallback>
                    <n-icon><PersonOutline /></n-icon>
                  </template>
                </n-avatar>
                <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
                <n-icon size="14"><ChevronDownOutline /></n-icon>
              </div>
            </n-dropdown>
          </template>
          <template v-else>
            <n-button text @click="router.push('/login')">登录</n-button>
            <n-divider vertical />
            <n-button text type="primary" @click="router.push('/register')">注册</n-button>
          </template>
        </div>
      </div>
    </n-layout-header>
    <!-- 内容区域 -->
    <n-layout-content class="content">
      <router-view />
    </n-layout-content>
  </n-layout>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { StorefrontOutline, CartOutline, SearchOutline, PersonOutline, ChevronDownOutline } from '@vicons/ionicons5'
import { useUserStore } from '@/store/user'
import { getCartList } from '@/api/cart'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const userStore = useUserStore()

// 购物车数量
const cartCount = ref(0)

// 搜索关键词
const searchKeyword = ref('')

// 搜索商品
function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/product', query: { keyword: searchKeyword.value.trim() } })
  } else {
    router.push('/product')
  }
}

// 当前激活菜单
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/') return 'home'
  if (path.startsWith('/product')) return 'product'
  if (path.startsWith('/forum')) return 'forum'
  return ''
})

// 导航菜单
const menuOptions = [
  { label: '首页', key: 'home', onClick: () => router.push('/') },
  { label: '商品', key: 'product', onClick: () => router.push('/product') },
  { label: '论坛', key: 'forum', onClick: () => router.push('/forum') }
]

// 用户下拉菜单
const userMenuOptions = computed(() => {
  const options = [
    { label: '个人中心', key: 'profile' },
    { label: '我的商品', key: 'my-products' },
    { label: '我的帖子', key: 'my-posts' },
    { label: '我的订单', key: 'order' },
    { label: '我的收藏', key: 'favorite' },
    { label: '浏览记录', key: 'history' },
    { type: 'divider', key: 'd1' },
    { label: '退出登录', key: 'logout' }
  ]
  if (userStore.isAdmin) {
    options.unshift({ label: '后台管理', key: 'admin' })
  }
  return options
})

// 处理用户菜单点击
function handleUserMenu(key) {
  if (key === 'logout') {
    userStore.logout()
    message.success('已退出登录')
    router.push('/')
  } else if (key === 'admin') {
    router.push('/admin')
  } else {
    router.push(`/${key}`)
  }
}

// 获取购物车数量
async function fetchCartCount() {
  if (userStore.isLogin) {
    try {
      const data = await getCartList()
      cartCount.value = data?.length || 0
    } catch (e) {}
  } else {
    cartCount.value = 0
  }
}

// 监听登录状态变化
watch(() => userStore.isLogin, () => {
  fetchCartCount()
})

// 监听购物车更新事件
function handleCartUpdate() {
  fetchCartCount()
}

onMounted(() => {
  window.$message = message
  fetchCartCount()
  window.addEventListener('cart-updated', handleCartUpdate)
})
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
  color: #333;
  flex-shrink: 0;
}

.logo:hover {
  color: #18a058;
}

.nav-menu :deep(.n-menu) {
  background: transparent;
}

.nav-menu :deep(.n-menu-item) {
  height: 60px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  width: 220px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f5f5;
}

.user-name {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.content {
  min-height: calc(100vh - 130px);
  background: #f5f7f9;
}

.footer {
  text-align: center;
  padding: 20px;
  color: #666;
  background: #fff;
}
</style>
