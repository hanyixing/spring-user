<template>
  <div class="user-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>
      
      <el-table :data="userList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)">{{ getRoleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">
              {{ row.status === 0 ? '正常' : '已注销' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" size="small" @click="handleEditPassword(row)" v-if="canManage(row.role)">改密</el-button>
            <el-button type="info" size="small" @click="handleEditRole(row)" v-if="canManage(row.role)">改角色</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)" v-if="canManage(row.role)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="!!editId" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role" v-if="!editId">
          <el-select v-model="formData.role" placeholder="请选择角色">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" v-if="userStore.isSuperAdmin" />
            <el-option label="超级管理员" value="SUPER_ADMIN" v-if="userStore.isSuperAdmin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" ref="passwordFormRef" label-width="80px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="修改角色" width="400px">
      <el-form :model="roleForm" ref="roleFormRef" label-width="80px">
        <el-form-item label="新角色" prop="newRole">
          <el-select v-model="roleForm.newRole" placeholder="请选择新角色">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" v-if="userStore.isSuperAdmin" />
            <el-option label="超级管理员" value="SUPER_ADMIN" v-if="userStore.isSuperAdmin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { userApi, type User, type UserDTO } from '../api/user'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const loading = ref(false)
const userList = ref<User[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref<FormInstance>()
const editId = ref<number | null>(null)

const passwordDialogVisible = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({ newPassword: '' })
const passwordEditId = ref<number | null>(null)

const roleDialogVisible = ref(false)
const roleFormRef = ref<FormInstance>()
const roleForm = reactive({ newRole: '' })
const roleEditId = ref<number | null>(null)

const formData = reactive<UserDTO>({
  username: '',
  password: '',
  realName: '',
  idCard: '',
  phone: '',
  email: '',
  role: 'USER'
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const getRoleText = (role: string) => {
  switch (role) {
    case 'SUPER_ADMIN': return '超级管理员'
    case 'ADMIN': return '管理员'
    default: return '普通用户'
  }
}

const getRoleTagType = (role: string) => {
  switch (role) {
    case 'SUPER_ADMIN': return 'danger'
    case 'ADMIN': return 'warning'
    default: return 'info'
  }
}

const canManage = (targetRole: string) => {
  return userStore.canManage(targetRole)
}

const fetchUsers = async () => {
  loading.value = true
  try {
    userList.value = await userApi.getAll()
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  editId.value = null
  Object.assign(formData, {
    username: '',
    password: '',
    realName: '',
    idCard: '',
    phone: '',
    email: '',
    role: 'USER'
  })
  dialogVisible.value = true
}

const handleEdit = (row: User) => {
  dialogTitle.value = '编辑用户'
  editId.value = row.id
  Object.assign(formData, {
    username: row.username,
    password: '',
    realName: row.realName,
    idCard: row.idCard,
    phone: row.phone,
    email: row.email,
    role: row.role || 'USER'
  })
  dialogVisible.value = true
}

const handleEditPassword = (row: User) => {
  passwordEditId.value = row.id
  passwordForm.newPassword = ''
  passwordDialogVisible.value = true
}

const handleEditRole = (row: User) => {
  roleEditId.value = row.id
  roleForm.newRole = row.role || 'USER'
  roleDialogVisible.value = true
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userApi.delete(id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (editId.value) {
          await userApi.update(editId.value, formData)
          ElMessage.success('更新成功')
        } else {
          await userApi.create(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchUsers()
      } catch (error) {
        ElMessage.error(editId.value ? '更新失败' : '创建失败')
      }
    }
  })
}

const handlePasswordSubmit = async () => {
  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  try {
    await userApi.updatePassword(passwordEditId.value!, passwordForm.newPassword)
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } catch (error) {
    ElMessage.error('密码修改失败')
  }
}

const handleRoleSubmit = async () => {
  if (!roleForm.newRole) {
    ElMessage.warning('请选择新角色')
    return
  }
  try {
    await userApi.updateRole(roleEditId.value!, roleForm.newRole)
    ElMessage.success('角色修改成功')
    roleDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error('角色修改失败')
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
