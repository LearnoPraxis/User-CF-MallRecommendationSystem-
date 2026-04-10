import axios from 'axios'
import { useUserStore } from '@/store/user'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else {
      window.$message?.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        // 只有在非公开页面才跳转登录
        const publicPaths = ['/', '/product', '/forum', '/login', '/register']
        const currentPath = window.location.pathname
        const isPublicPage = publicPaths.some(p => currentPath === p || currentPath.startsWith('/product/') || currentPath.startsWith('/forum/'))
        
        if (!isPublicPage) {
          window.$message?.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          window.location.href = '/login?redirect=' + encodeURIComponent(currentPath)
        }
      } else if (status === 403) {
        window.$message?.error('没有权限访问')
      } else {
        window.$message?.error(data?.message || '请求失败')
      }
    } else {
      window.$message?.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
