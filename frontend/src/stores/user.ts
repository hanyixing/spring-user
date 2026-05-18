import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi, type User } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  const isLoggedIn = computed(() => !!currentUser.value)
  
  const role = computed(() => currentUser.value?.role || 'USER')
  
  const roleLevel = computed(() => {
    switch (role.value) {
      case 'SUPER_ADMIN': return 3
      case 'ADMIN': return 2
      default: return 1
    }
  })

  const isAdmin = computed(() => roleLevel.value >= 2)
  const isSuperAdmin = computed(() => role.value === 'SUPER_ADMIN')

  async function login(username: string, password: string) {
    const user = await userApi.login(username, password)
    currentUser.value = user
    localStorage.setItem('user', JSON.stringify(user))
    return user
  }

  function logout() {
    currentUser.value = null
    token.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }

  async function initUser() {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      try {
        const user = JSON.parse(savedUser)
        const validUser = await userApi.getById(user.id)
        if (validUser.status === 1) {
          logout()
          throw new Error('用户已被禁用')
        }
        currentUser.value = validUser
        localStorage.setItem('user', JSON.stringify(validUser))
      } catch (error) {
        logout()
      }
    }
  }

  function canManage(targetRole: string): boolean {
    const targetLevel = targetRole === 'SUPER_ADMIN' ? 3 : targetRole === 'ADMIN' ? 2 : 1
    return roleLevel.value > targetLevel
  }

  return {
    currentUser,
    token,
    isLoggedIn,
    role,
    roleLevel,
    isAdmin,
    isSuperAdmin,
    login,
    logout,
    initUser,
    canManage
  }
})
