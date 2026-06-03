<template>
  <div class="admin-user-management">
    <el-card shadow="hover" class="page-header">
      <template #header>
        <div class="card-header">
          <h1>用户管理</h1>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item><el-icon><House /></el-icon>首页</el-breadcrumb-item>
            <el-breadcrumb-item>用户管理</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </template>
    </el-card>

    <!-- 搜索和筛选 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input
            v-model="searchForm.email"
            placeholder="请输入邮箱"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.role"
            placeholder="请选择角色"
            clearable
            style="width: 150px"
          >
            <el-option label="普通用户" :value="'ROLE_USER'" />
            <el-option label="管理员" :value="'ROLE_ADMIN'" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchUsers">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshRight /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card shadow="hover" class="users-card">
      <div class="table-wrapper">
        <el-table
          v-loading="loading"
          :data="filteredUsers"
          @selection-change="handleSelectionChange"
          @row-click="viewUserDetails"
          style="width: 100%"
          highlight-current-row
        >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="avatar" label="头像" width="100">
          <template #default="scope">
            <el-avatar
              :src="scope.row.avatar?`data:image/jpeg;base64,${scope.row.avatar}`:'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
              size="large"
              :icon="User"
            />
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="150">
          <template #default="scope">
            <div class="user-name">{{ scope.row.username }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="roleName" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.roleName === 'ROLE_ADMIN' ? 'danger' : 'success'">
              {{ scope.row.roleName === 'ROLE_ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="是否禁用" width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            >
              <template #active>
                <el-tag type="success">启用</el-tag>
              </template>
              <template #inactive>
                <el-tag type="danger">禁用</el-tag>
              </template>
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.lastLoginTime) || '从未登录' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button
                type="success"
                size="small"
                @click.stop="resetPassword(scope.row.id)"
              >
                <el-icon><Key /></el-icon> 重置密码
              </el-button>
              <el-button
                type="warning"
                size="small"
                @click.stop="editUserRole(scope.row)"
              >
                <el-icon><SetUp /></el-icon> 编辑角色
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click.stop="deleteUser(scope.row.id)"
              >
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 编辑角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="编辑角色"
      width="40%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleRules"
        label-width="100px"
        class="role-form"
      >
        <el-form-item label="用户名">
          <el-input v-model="roleForm.username" disabled />
        </el-form-item>
        <el-form-item label="当前角色">
          <el-input v-model="roleForm.currentRole" disabled />
        </el-form-item>
        <el-form-item label="新角色" prop="newRole">
          <el-select v-model="roleForm.newRole" placeholder="请选择新角色">
            <el-option label="普通用户" :value="'ROLE_USER'" />
            <el-option label="管理员" :value="'ROLE_ADMIN'" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRoleForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="userDetailDialogVisible"
      title="用户详情"
      width="50%"
      :close-on-click-modal="false"
    >
      <div class="user-detail" v-if="currentUser">
        <el-descriptions border column="2" :title="'用户基本信息'" class="user-info">
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ currentUser.roleName === 'ROLE_ADMIN' ? '管理员' : '普通用户' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ currentUser.status === 1 ? '启用' : '禁用' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentUser.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="最后登录">{{ formatDate(currentUser.lastLoginTime) || '从未登录' }}</el-descriptions-item>
        </el-descriptions>

        <h3 class="detail-section">阅读偏好</h3>
        <el-descriptions border column="3" class="reading-preferences">
          <el-descriptions-item label="字体大小">{{ currentUser.readingPreferences?.fontSize || '默认' }}</el-descriptions-item>
          <el-descriptions-item label="主题">{{ currentUser.readingPreferences?.theme || '默认' }}</el-descriptions-item>
          <el-descriptions-item label="阅读模式">{{ currentUser.readingPreferences?.mode || '默认' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="userDetailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  House,
  Search,
  RefreshRight,
  User,
  Key,
  SetUp,
  Delete,
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { userApi } from '@/api/Users'



// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  username: '',
  email: '',
  status: '',
  role: '',
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0,
})

// 选择的用户
const selectedUsers = ref([])

// 用户列表数据
const users = ref([])

// 当前用户（详情）
const currentUser = ref(null)

// 角色表单
const roleForm = reactive({
  userId: '',
  username: '',
  currentRole: '',
  newRole: '',
})

// 角色表单规则
const roleRules = reactive({
  newRole: [{ required: true, message: '请选择新角色', trigger: 'change' }],
})

// 角色表单引用
const roleFormRef = ref()

// 对话框
const roleDialogVisible = ref(false)
const userDetailDialogVisible = ref(false)

// 计算属性：筛选后的用户
const filteredUsers = computed(() => {
  return users.value.filter(user => {
    // 根据搜索条件过滤
    const matchesUsername = searchForm.username ? user.username.toLowerCase().includes(searchForm.username.toLowerCase()) : true
    const matchesEmail = searchForm.email ? user.email.toLowerCase().includes(searchForm.email.toLowerCase()) : true
    const matchesStatus = searchForm.status != null && searchForm.status !== '' ? user.status === Number(searchForm.status) : true
    const matchesRole = searchForm.role ? user.roleName === searchForm.role : true

    return matchesUsername && matchesEmail && matchesStatus && matchesRole
  })
})

// 计算总数
const total = computed(() => filteredUsers.value.length)

// 搜索用户
const searchUsers = () => {
  loading.value = true
  // 实际项目中，这里会调用带搜索条件的API
  // 暂时在前端过滤数据
  setTimeout(() => {
    loading.value = false
  }, 500)
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    username: '',
    email: '',
    status: '',
    role: '',
  })
  pagination.currentPage = 1
}

// 选择用户
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
}

// 分页页码变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
}

// 处理状态变化
const handleStatusChange = async (row) => {
  try {
    loading.value = true
    await userApi.changeStatus(row.id, Number(row.status))
    fetchUsers() // 刷新用户列表
  } catch (error) {
    console.error('更新用户状态失败:', error)
  } finally {
    loading.value = false
  }
}

// 查看用户详情
const viewUserDetails = (row) => {
  if (row) {
    currentUser.value = row
    userDetailDialogVisible.value = true
  }
}

// 重置密码
const resetPassword = async (id) => {
  try {
    await ElMessageBox.confirm('确定要重置该用户的密码吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    loading.value = true
    await userApi.resetPassword(id)
    fetchUsers() // 刷新用户列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
    }
  } finally {
    loading.value = false
  }
}

// 编辑用户角色
const editUserRole = (row) => {
  roleForm.userId = row.id
  roleForm.username = row.username
  roleForm.currentRole = row.roleName === 'ROLE_ADMIN' ? '管理员' : '普通用户'
  roleForm.newRole = row.roleName === 'ROLE_ADMIN' ? 'ROLE_ADMIN' : 'ROLE_USER'
  roleDialogVisible.value = true
}

// 提交角色表单
const submitRoleForm = async () => {
  if (!roleFormRef.value) return
  roleFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        await userApi.changeRole(roleForm.newRole, roleForm.userId)
        roleDialogVisible.value = false
        fetchUsers() // 刷新用户列表
      } catch (error) {
        console.error('更新用户角色失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 删除用户
const deleteUser = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？此操作不可恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true,
    })
    loading.value = true
    await userApi.deleteUser(id)
    fetchUsers() // 刷新用户列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
    }
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    loading.value = true
    const response = await userApi.getAllUsers()
    users.value = response.data || []
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 初始化数据
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.admin-user-management {
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;
}

.table-wrapper {
  overflow-x: auto;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.users-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.user-name {
  font-weight: 500;
}

.role-form {
  padding: 20px 0;
}

.user-detail {
  padding: 20px 0;
}

.detail-section {
  margin-top: 20px;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 600;
}

.user-info {
  margin-bottom: 20px;
}

.reading-preferences {
  margin-top: 10px;
}

.dialog-footer {
  text-align: right;
}
</style>
