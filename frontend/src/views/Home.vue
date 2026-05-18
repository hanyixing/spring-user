<template>
  <div class="home-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="welcome-card">
          <template #header>
            <div class="card-header">
              <span>欢迎使用用户管理系统</span>
            </div>
          </template>
          
          <div class="welcome-content">
            <h2>欢迎，{{ userStore.currentUser?.realName }}</h2>
            <p>当前角色：{{ roleText }}</p>
            
            <el-divider />
            
            <div class="features">
              <h3>系统功能</h3>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-card shadow="hover" class="feature-card">
                    <el-icon><UserFilled /></el-icon>
                    <h4>用户管理</h4>
                    <p>支持用户的增删改查操作</p>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card shadow="hover" class="feature-card">
                    <el-icon><Lock /></el-icon>
                    <h4>权限控制</h4>
                    <p>基于角色的权限管理</p>
                  </el-card>
                </el-col>
              </el-row>
              <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                  <el-card shadow="hover" class="feature-card">
                    <el-icon><DocumentChecked /></el-icon>
                    <h4>登录记录</h4>
                    <p>记录用户登录时间和IP</p>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card shadow="hover" class="feature-card">
                    <el-icon><Connection /></el-icon>
                    <h4>数据加密</h4>
                    <p>敏感信息AES加密存储</p>
                  </el-card>
                </el-col>
              </el-row>
            </div>
            
            <el-divider />
            
            <el-button type="primary" size="large" @click="goToUsers" v-if="userStore.isAdmin">
              <el-icon><List /></el-icon>
              进入用户管理
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="login-log-card">
          <template #header>
            <div class="card-header">
              <span>最近登录记录</span>
            </div>
          </template>
          
          <el-table :data="loginLogs" v-loading="loading" max-height="400">
            <el-table-column prop="loginTime" label="时间" width="160" />
            <el-table-column prop="ipAddress" label="IP地址" width="120" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.loginStatus === 1 ? 'success' : 'danger'" size="small">
                  {{ row.loginStatus === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, Lock, DocumentChecked, Connection, List } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { userApi, type LoginLog } from '../api/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const loginLogs = ref<LoginLog[]>([])

const roleText = computed(() => {
  switch (userStore.role) {
    case 'SUPER_ADMIN': return '超级管理员'
    case 'ADMIN': return '管理员'
    default: return '普通用户'
  }
})

const goToUsers = () => {
  router.push('/users')
}

const fetchLoginLogs = async () => {
  if (userStore.currentUser?.id) {
    loading.value = true
    try {
      loginLogs.value = await userApi.getLoginLogs(userStore.currentUser.id)
    } catch (error) {
      console.error('获取登录记录失败')
    } finally {
      loading.value = false
    }
  }
}

onMounted(() => {
  fetchLoginLogs()
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.welcome-card, .login-log-card {
  height: 100%;
}

.card-header {
  font-size: 20px;
  font-weight: bold;
}

.welcome-content {
  text-align: center;
  padding: 20px;
}

.welcome-content h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.welcome-content p {
  color: #909399;
  margin-bottom: 20px;
}

.features h3 {
  margin-bottom: 20px;
  color: #303133;
}

.feature-card {
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-card .el-icon {
  font-size: 40px;
  color: #409EFF;
  margin-bottom: 10px;
}

.feature-card h4 {
  margin: 10px 0;
  color: #303133;
}

.feature-card p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}
</style>
