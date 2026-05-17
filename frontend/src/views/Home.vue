<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎使用用户管理系统</span>
        </div>
      </template>
      
      <div class="welcome-content">
        <el-icon class="welcome-icon"><User /></el-icon>
        <h2>用户管理系统</h2>
        <p>这是一个基于 Spring Boot + Vue 3 的用户管理系统</p>
        
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
                <h4>数据加密</h4>
                <p>敏感信息采用AES加密存储</p>
              </el-card>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="12">
              <el-card shadow="hover" class="feature-card">
                <el-icon><DocumentChecked /></el-icon>
                <h4>数据验证</h4>
                <p>身份证、手机号等格式验证</p>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover" class="feature-card">
                <el-icon><Connection /></el-icon>
                <h4>SQLite数据库</h4>
                <p>轻量级数据库，易于部署</p>
              </el-card>
            </el-col>
          </el-row>
        </div>
        
        <el-divider />
        
        <el-button type="primary" size="large" @click="goToUsers">
          <el-icon><List /></el-icon>
          进入用户管理
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { User, UserFilled, Lock, DocumentChecked, Connection, List } from '@element-plus/icons-vue'

const router = useRouter()

const goToUsers = () => {
  router.push('/users')
}
</script>

<style scoped>
.home-container {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: calc(100vh - 40px);
}

.welcome-card {
  max-width: 900px;
  width: 100%;
}

.card-header {
  font-size: 20px;
  font-weight: bold;
}

.welcome-content {
  text-align: center;
  padding: 20px;
}

.welcome-icon {
  font-size: 80px;
  color: #409EFF;
  margin-bottom: 20px;
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
