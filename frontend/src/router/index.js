import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  // 前台路由
  {
    path: '/',
    component: () => import('@/layout/FrontLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/front/Home.vue') },
      { path: 'product', name: 'ProductList', component: () => import('@/views/front/product/List.vue') },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('@/views/front/product/Detail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('@/views/front/Cart.vue'), meta: { requireAuth: true } },
      { path: 'order', name: 'Order', component: () => import('@/views/front/order/List.vue'), meta: { requireAuth: true } },
      { path: 'order/:id', name: 'OrderDetail', component: () => import('@/views/front/order/Detail.vue'), meta: { requireAuth: true } },
      { path: 'favorite', name: 'Favorite', component: () => import('@/views/front/Favorite.vue'), meta: { requireAuth: true } },
      { path: 'history', name: 'History', component: () => import('@/views/front/History.vue'), meta: { requireAuth: true } },
      { path: 'forum', name: 'Forum', component: () => import('@/views/front/forum/List.vue') },
      { path: 'forum/:id', name: 'ForumDetail', component: () => import('@/views/front/forum/Detail.vue') },
      { path: 'forum/create', name: 'ForumCreate', component: () => import('@/views/front/forum/Create.vue'), meta: { requireAuth: true } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/front/Profile.vue'), meta: { requireAuth: true } },
      { path: 'my-products', name: 'MyProducts', component: () => import('@/views/front/MyProducts.vue'), meta: { requireAuth: true } },
      { path: 'my-posts', name: 'MyPosts', component: () => import('@/views/front/MyPosts.vue'), meta: { requireAuth: true } },
    ]
  },
  // 登录注册
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') },
  // 后台管理路由
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'user', name: 'AdminUser', component: () => import('@/views/admin/User.vue') },
      { path: 'product', name: 'AdminProduct', component: () => import('@/views/admin/Product.vue') },
      { path: 'category', name: 'AdminCategory', component: () => import('@/views/admin/Category.vue') },
      { path: 'order', name: 'AdminOrder', component: () => import('@/views/admin/Order.vue') },
      { path: 'comment', name: 'AdminComment', component: () => import('@/views/admin/Comment.vue') },
      { path: 'cart', name: 'AdminCart', component: () => import('@/views/admin/Cart.vue') },
      { path: 'forum', name: 'AdminForum', component: () => import('@/views/admin/Forum.vue') },
      { path: 'report', name: 'AdminReport', component: () => import('@/views/admin/Report.vue') },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requireAuth && !userStore.isLogin) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.meta.requireAdmin && !userStore.isAdmin) {
    next('/')
  } else {
    next()
  }
})

export default router
