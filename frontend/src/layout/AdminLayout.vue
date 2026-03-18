<template>
  <n-layout has-sider class="admin-layout">
    <!-- 侧边栏 -->
    <n-layout-sider bordered collapse-mode="width" :collapsed-width="64" :width="220"
      :collapsed="collapsed" show-trigger @collapse="collapsed = true" @expand="collapsed = false">
      <div class="logo">
        <n-icon size="24" color="#18a058"><StorefrontOutline /></n-icon>
        <span v-show="!collapsed">后台管理</span>
      </div>
      <n-menu :collapsed="collapsed" :collapsed-width="64" :collapsed-icon-size="22"
        :value="activeMenu" :options="menuOptions" @update:value="handleMenuClick" />
    </n-layout-sider>
    <!-- 右侧内容 -->
    <n-layout>
      <n-layout-header bordered class="admin-header">
        <div class="header-left">
          <n-breadcrumb>
            <n-breadcrumb-item>后台管理</n-breadcrumb-item>
            <n-breadcrumb-item>{{ currentTitle }}</n-breadcrumb-item>
          </n-breadcrumb>
        </div>
        <div class="header-right">
          <n-dropdown :options="userMenuOptions" @select="handleUserMenu">
            <n-button quaternary>
              <n-avatar round size="small" :src="userStore.userInfo?.avatar" />
              <span style="margin-left: 8px">{{ userStore.userInfo?.nickname }}</span>
            </n-button>
          </n-dropdown>
        </div>
      </n-layout-header>
      <n-layout-content class="admin-content">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { h, ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage, NIcon } from 'naive-ui'
import { useUserStore } from '@/store/user'
import {
  StorefrontOutline, HomeOutline, PeopleOutline, CubeOutline,
  ListOutline, CartOutline, ChatbubblesOutline, NewspaperOutline,
  StatsChartOutline, GridOutline
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const userStore = useUserStore()
const collapsed = ref(false)

// 渲染图标
function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

// 菜单配置
const menuOptions = [
  { label: '控制台', key: 'dashboard', icon: renderIcon(HomeOutline) },
  { label: '用户管理', key: 'user', icon: renderIcon(PeopleOutline) },
  { label: '商品管理', key: 'product', icon: renderIcon(CubeOutline) },
  { label: '分类管理', key: 'category', icon: renderIcon(GridOutline) },
  { label: '订单管理', key: 'order', icon: renderIcon(ListOutline) },
  { label: '购物车管理', key: 'cart', icon: renderIcon(CartOutline) },
  { label: '留言管理', key: 'comment', icon: renderIcon(ChatbubblesOutline) },
  { label: '论坛管理', key: 'forum', icon: renderIcon(NewspaperOutline) },
  { label: '报表统计', key: 'report', icon: renderIcon(StatsChartOutline) }
]

// 标题映射
const titleMap = {
  dashboard: '控制台', user: '用户管理', product: '商品管理', category: '分类管理',
  order: '订单管理', cart: '购物车管理', comment: '留言管理', forum: '论坛管理', report: '报表统计'
}

// 当前激活菜单
const activeMenu = computed(() => route.path.split('/').pop())
const currentTitle = computed(() => titleMap[activeMenu.value] || '')

// 用户菜单
const userMenuOptions = [
  { label: '返回前台', key: 'front' },
  { type: 'divider', key: 'd1' },
  { label: '退出登录', key: 'logout' }
]

function handleMenuClick(key) {
  router.push(`/admin/${key}`)
}

function handleUserMenu(key) {
  if (key === 'logout') {
    userStore.logout()
    message.success('已退出登录')
    router.push('/login')
  } else if (key === 'front') {
    router.push('/')
  }
}

onMounted(() => {
  window.$message = message
})
</script>

<style scoped>
.admin-layout { height: 100vh; }
.logo { height: 60px; display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 16px; font-weight: bold; border-bottom: 1px solid #efeff5; }
.admin-header { height: 60px; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; background: #fff; }
.admin-content { padding: 20px; background: #f5f7f9; height: calc(100vh - 60px); overflow: auto; }
</style>
