<template>
  <div class="admin-wishlist-management">
    <el-card shadow="hover" class="page-header">
      <template #header>
        <div class="card-header">
          <h1>愿望单管理</h1>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item><el-icon><House /></el-icon>首页</el-breadcrumb-item>
            <el-breadcrumb-item>用户管理</el-breadcrumb-item>
            <el-breadcrumb-item>愿望单管理</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </template>
    </el-card>

    <!-- 搜索和筛选 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="书籍名称">
          <el-input
            v-model="searchForm.bookName"
            placeholder="请输入书籍名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
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
            <el-option label="待处理" :value="1" />
            <el-option label="处理中" :value="2" />
            <el-option label="已添加" :value="3" />
            <el-option label="已拒绝" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchWishlists">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshRight /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 愿望单列表 -->
    <el-card shadow="hover" class="wishlists-card">
      <el-table
        v-loading="loading"
        :data="filteredWishlists"
        style="width: 100%"
      >
        <el-table-column prop="bookTitle" label="书籍名称" min-width="180">
          <template #default="scope">
            <div class="book-name">{{ scope.row.bookTitle }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="userName" label="用户名" width="150" />
        <el-table-column prop="description" label="补充" min-width="200" />
        <el-table-column prop="createdAt" label="提交时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="processedAt" label="处理时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.processedAt) || '未处理' }}
          </template>
        </el-table-column>
        <el-table-column prop="adminNote" label="处理结果" min-width="200" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="viewWishlistDetails(scope.row.id)"
            >
              <el-icon><View /></el-icon> 详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 愿望单详情对话框 -->
    <el-dialog
      v-model="wishlistDetailDialogVisible"
      title="愿望单详情"
      width="50%"
      :close-on-click-modal="false"
    >
      <div class="wishlist-detail" v-if="currentWishlist">
        <el-descriptions border column="2" :title="'基本信息'" class="wishlist-info">
          <el-descriptions-item label="愿望单ID">{{ currentWishlist.id }}</el-descriptions-item>
          <el-descriptions-item label="书籍名称">{{ currentWishlist.bookTitle }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentWishlist.author || '未提供' }}</el-descriptions-item>
          <el-descriptions-item label="出版社">{{ currentWishlist.publisher || '未提供' }}</el-descriptions-item>
          <el-descriptions-item label="ISBN">{{ currentWishlist.isbn || '未提供' }}</el-descriptions-item>
          <el-descriptions-item label="提交用户">{{ currentWishlist.userName }}</el-descriptions-item>
          <el-descriptions-item label="来源链接">{{ currentWishlist.requestSource || '未提供' }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDate(currentWishlist.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(currentWishlist.status) }}</el-descriptions-item>
        </el-descriptions>

        <h3 class="detail-section">补充信息</h3>
        <div class="supplement-info">
          <p>{{ currentWishlist.description || '无补充信息' }}</p>
        </div>

        <h3 class="detail-section" v-if="currentWishlist.status !== 1">处理记录</h3>
        <el-descriptions border column="2" class="process-record" v-if="currentWishlist.status !== 1">
          <el-descriptions-item label="处理时间">{{ formatDate(currentWishlist.processedAt) }}</el-descriptions-item>
          <el-descriptions-item label="处理结果">{{ getStatusText(currentWishlist.status) }}</el-descriptions-item>
          <el-descriptions-item label="处理说明">{{ currentWishlist.adminNote || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="wishlistDetailDialogVisible = false">关闭</el-button>
          <el-button
            type="success"
            @click="acceptWishlist(currentWishlist)"
            :disabled="currentWishlist?.status !== 1"
          >
            <el-icon><Check /></el-icon> 收录
          </el-button>
          <el-button
            type="danger"
            @click="rejectWishlist(currentWishlist)"
            :disabled="currentWishlist?.status !== 1"
          >
            <el-icon><Close /></el-icon> 拒绝
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 处理愿望单对话框 -->
    <el-dialog
      v-model="processWishlistDialogVisible"
      :title="isAcceptWishlist ? '收录书籍' : '拒绝请求'"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="processFormRef"
        :model="processForm"
        :rules="processRules"
        label-width="120px"
        class="process-form"
      >
        <el-form-item label="书籍名称" prop="bookName">
          <el-input v-model="processForm.bookName" readonly />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="processForm.author" readonly />
        </el-form-item>
        <el-form-item v-if="isAcceptWishlist" label="入库书籍ID">
          <el-input
            v-model="processForm.bookId"
            placeholder="请输入入库后的书籍ID"
          />
        </el-form-item>
        <el-form-item v-if="isAcceptWishlist" label="入库链接">
          <el-input
            v-model="processForm.bookUrl"
            placeholder="请输入书籍详情页链接"
          />
        </el-form-item>
        <el-form-item label="处理说明" prop="processResult">
          <el-input
            v-model="processForm.processResult"
            type="textarea"
            placeholder="请输入处理说明"
            rows="4"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="processWishlistDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProcessForm">确定</el-button>
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
  View,
} from '@element-plus/icons-vue'
import { bookRequestApi } from '@/api/BookRequest'

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  bookName: '',
  username: '',
  status: 1,
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0,
})

// 愿望单列表数据
const wishlists = ref([])

// 当前愿望单（详情）
const currentWishlist = ref(null)

// 处理表单
const processForm = reactive({
  wishlistId: '',
  bookName: '',
  author: '',
  bookId: '',
  bookUrl: '',
  processResult: '',
})

// 处理表单规则
const processRules = reactive({
  processResult: [{ required: true, message: '请输入处理说明', trigger: 'blur' }],
  bookId: [{ message: '请输入入库书籍ID', trigger: 'blur' }],
})

// 处理表单引用
const processFormRef = ref()

// 对话框
const wishlistDetailDialogVisible = ref(false)
const processWishlistDialogVisible = ref(false)
const isAcceptWishlist = ref(true)

// 计算属性：筛选后的愿望单
const filteredWishlists = computed(() => {
  return wishlists.value.filter(wishlist => {
    const matchesBookName = searchForm.bookName ? wishlist.bookTitle.toLowerCase().includes(searchForm.bookName.toLowerCase()) : true
    const matchesUsername = searchForm.username ? wishlist.userName.toLowerCase().includes(searchForm.username.toLowerCase()) : true
    const matchesStatus = searchForm.status != null && searchForm.status !== '' ? wishlist.status === searchForm.status : true
    return matchesBookName && matchesUsername && matchesStatus
  })
})

// 计算总数
const total = computed(() => filteredWishlists.value.length)

// 搜索愿望单
const searchWishlists = () => {
  loading.value = true
  fetchWishlists()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    bookName: '',
    username: '',
    status: 'pending',
  })
  pagination.currentPage = 1
  fetchWishlists()
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

// 获取愿望单列表
const fetchWishlists = async () => {
  try {
    loading.value = true
    const response = await bookRequestApi.getAllBookRequests()
    wishlists.value = response.data || []
  } catch (error) {
    console.error('获取愿望单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    1: 'warning',
    2: 'info',
    3: 'success',
    4: 'danger',
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    1: '待处理',
    2: '处理中',
    3: '已添加',
    4: '已拒绝',
  }
  return textMap[status] || status
}

// 查看愿望单详情
const viewWishlistDetails = (id) => {
  // 实际项目中，这里会调用API获取愿望单详情
  const wishlist = wishlists.value.find((w) => w.id === id)
  if (wishlist) {
    currentWishlist.value = wishlist
    wishlistDetailDialogVisible.value = true
  }
}

// 接受愿望单
const acceptWishlist = (row) => {
  if (!row) return
  isAcceptWishlist.value = true
  processForm.wishlistId = row.id
  processForm.bookName = row.bookTitle
  processForm.author = row.author
  processForm.bookId = ''
  processForm.bookUrl = ''
  processForm.processResult = ''
  processWishlistDialogVisible.value = true
}

// 拒绝愿望单
const rejectWishlist = (row) => {
  if (!row) return
  isAcceptWishlist.value = false
  processForm.wishlistId = row.id
  processForm.bookName = row.bookTitle
  processForm.author = row.author
  processForm.bookId = ''
  processForm.bookUrl = ''
  processForm.processResult = ''
  processWishlistDialogVisible.value = true
}

// 提交处理表单
const submitProcessForm = async () => {
  if (!processFormRef.value) return
  await processFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        const data = {
          id: processForm.wishlistId,
          bookId: isAcceptWishlist.value ? processForm.bookId : null,
          status: isAcceptWishlist.value ? 3 : 4,
          adminNote: processForm.processResult,
          adminId: 1, // 这里应该从登录信息中获取，暂时硬编码
        }
        await bookRequestApi.updateBookRequest(data)
        processWishlistDialogVisible.value = false
        wishlistDetailDialogVisible.value = false
        fetchWishlists() // 刷新愿望单列表
      } catch (error) {
        console.error('处理愿望单失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
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

// 初始化数据
onMounted(() => {
  fetchWishlists()
})
</script>

<style scoped>
.admin-wishlist-management {
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;
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

.wishlists-card {
  margin-bottom: 20px;
}

.el-table .el-table__cell {
  padding: 12px 0;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.book-name {
  font-weight: 500;
}

/* 详情对话框 */
.wishlist-detail {
  padding: 20px 0;
}

.detail-section {
  margin-top: 20px;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 600;
}

.supplement-info {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.process-record {
  margin-top: 10px;
}

/* 处理表单 */
.process-form {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
