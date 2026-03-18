import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(sessionStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(sessionStorage.getItem('userInfo') || 'null'))

  // 计算属性
  const isLogin = computed(() => !!token.value)
  const isLoggedIn = computed(() => !!token.value) // 别名
  const isAdmin = computed(() => userInfo.value?.role === 1)

  // 设置Token
  function setToken(newToken) {
    token.value = newToken
    sessionStorage.setItem('token', newToken)
  }

  // 设置用户信息
  function setUserInfo(info) {
    userInfo.value = info
    sessionStorage.setItem('userInfo', JSON.stringify(info))
  }

  // 登录
  function login(data) {
    setToken(data.token)
    setUserInfo(data.user)
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = null
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLogin,
    isLoggedIn,
    isAdmin,
    setToken,
    setUserInfo,
    login,
    logout
  }
})
