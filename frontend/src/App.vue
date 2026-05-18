<template>
  <div id="app">
    <template v-if="isLoggedIn">
      <el-container class="layout-container">
        <el-header class="layout-header">
          <div class="header-content">
            <span class="title">用户管理系统</span>
            <div class="user-info">
              <span>{{ currentUser?.realName }} ({{ roleText }})</span>
              <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
            </div>
          </div>
        </el-header>
        <el-container>
          <el-aside width="200px" class="layout-aside">
            <el-menu :default-active="activeMenu" router>
              <el-menu-item index="/">
                <el-icon><HomeFilled /></el-icon>
                <span>首页</span>
              </el-menu-item>
              <el-menu-item index="/users" v-if="isAdmin">
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </el-menu-item>
            </el-menu>
          </el-aside>
          <el-main class="layout-main">
            <router-view />
          </el-main>
        </el-container>
      </el-container>
    </template>
    <template v-else>
      <router-view />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { HomeFilled, User } from '@element-plus/icons-vue'
import { useUserStore } from './stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

onMounted(async () => {
  await userStore.initUser()
})

const isLoggedIn = computed(() => userStore.isLoggedIn)
const currentUser = computed(() => userStore.currentUser)
const isAdmin = computed(() => userStore.isAdmin)

const roleText = computed(() => {
  switch (userStore.role) {
    case 'SUPER_ADMIN': return '超级管理员'
    case 'ADMIN': return '管理员'
    default: return '普通用户'
  }
})

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

.layout-container {
  height: 100vh;
}

.layout-header {
  background: #409EFF;
  color: white;
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 20px;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.layout-aside {
  background: #f5f5f5;
}

.layout-main {
  background: #fff;
}
</style>
