<template>
  <div class="admin-book-management">
    <el-card shadow="hover" class="page-header">
      <template #header>
        <div class="card-header">
          <h1>已导入小说管理</h1>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item><el-icon><House /></el-icon>首页</el-breadcrumb-item>
            <el-breadcrumb-item>小说管理</el-breadcrumb-item>
            <el-breadcrumb-item>已导入小说</el-breadcrumb-item>
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
        <el-form-item label="作者">
          <el-input
            v-model="searchForm.author"
            placeholder="请输入作者"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择分类"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="连载状态">
          <el-select
            v-model="searchForm.serialStatus"
            placeholder="请选择连载状态"
            clearable
            style="width: 150px"
          >
            <el-option label="连载中" :value="1" />
            <el-option label="已完结" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchBooks">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><RefreshRight /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddBookDialog">
        <el-icon><Plus /></el-icon> 添加书籍
      </el-button>
      <el-button type="success" @click="showCategoryDialog">
        <el-icon><FolderPlus /></el-icon> 分类管理
      </el-button>
      <el-button @click="handleBatchExport" :disabled="selectedBooks.length === 0">
        <el-icon><Download /></el-icon> 批量导出
      </el-button>
      <el-button type="warning" @click="handleBatchStatusChange(1)" :disabled="selectedBooks.length === 0">
        <el-icon><SwitchButton /></el-icon> 批量上架
      </el-button>
      <el-button type="danger" @click="handleBatchStatusChange(0)" :disabled="selectedBooks.length === 0">
        <el-icon><SwitchButton /></el-icon> 批量下架
      </el-button>
      <el-button type="danger" @click="handleBatchDelete" :disabled="selectedBooks.length === 0">
        <el-icon><Delete /></el-icon> 批量删除
      </el-button>
    </div>

    <!-- 书籍列表 -->
    <el-card shadow="hover" class="books-card">
      <div class="table-wrapper">
        <el-table
          v-loading="loading"
          :data="paginatedBooks"
          @selection-change="handleSelectionChange"
          style="width: 100%"
        >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="cover" label="封面" width="100">
          <template #default="scope">
            <el-image
              :src="getImageUrl(scope.row.cover)"
              :fit="'cover'"
              style="width: 60px; height: 80px; border-radius: 4px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" min-width="180">
          <template #default="scope">
            <div class="book-title">{{ scope.row.title }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="wordCount" label="字数" width="100">
          <template #default="scope">
            {{ formatWordCount(scope.row.wordCount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            >
              <template #active>
                <el-tag type="success">上架</el-tag>
              </template>
              <template #inactive>
                <el-tag type="danger">下架</el-tag>
              </template>
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="连载状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'warning' : 'success'">
              {{ scope.row.status === 1 ? '连载中' : scope.row.status === 2 ? '已完结' : '' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="点击量" width="100" />
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="showEditBookDialog(scope.row)"
            >
              <el-icon><EditPen /></el-icon> 编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(scope.row.id)"
            >
              <el-icon><Delete /></el-icon> 删除
            </el-button>
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

    <!-- 添加/编辑书籍对话框 -->
    <el-dialog
      v-model="bookDialogVisible"
      :title="isEditBook ? '编辑书籍' : '添加书籍'"
      width="70%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="bookFormRef"
        :model="bookForm"
        :rules="bookRules"
        label-width="120px"
        class="book-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="书名" prop="title">
              <el-input v-model="bookForm.title" placeholder="请输入书名" />
            </el-form-item>
            <el-form-item label="作者" prop="author">
              <el-input v-model="bookForm.author" placeholder="请输入作者" />
            </el-form-item>
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="bookForm.categoryId" placeholder="请选择分类">
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="字数" prop="wordCount">
              <el-input-number
                v-model="bookForm.wordCount"
                :min="0"
                placeholder="请输入字数"
              />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-switch
                v-model="bookForm.status"
                :active-value="1"
                :inactive-value="0"
              >
                <template #active>上架</template>
                <template #inactive>下架</template>
              </el-switch>
            </el-form-item>
            <el-form-item label="连载状态" prop="serialStatus">
              <el-select v-model="bookForm.serialStatus" placeholder="请选择连载状态">
                <el-option label="连载中" :value="1" />
                <el-option label="已完结" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面">
              <el-upload
                class="cover-upload"
                action="#"
                :auto-upload="false"
                :on-change="handleCoverChange"
                :file-list="coverFileList"
                :before-upload="beforeCoverUpload"
                :limit="1"
                :accept="'.jpg,.jpeg,.png,.gif'"
              >
                <el-image
                  v-if="bookForm.cover"
                  :src="isEditBook ? getImageUrl(bookForm.cover) : bookForm.cover"
                  :fit="'cover'"
                  style="width: 150px; height: 200px; border-radius: 4px"
                />
                <el-empty
                  v-else
                  description="点击上传封面"
                  style="width: 150px; height: 200px; display: flex; align-items: center; justify-content: center; border: 1px dashed #d9d9d9; border-radius: 4px; cursor: pointer"
                />
              </el-upload>
            </el-form-item>
            <el-form-item label="简介" prop="description">
              <el-input
                v-model="bookForm.description"
                type="textarea"
                rows="4"
                placeholder="请输入书籍简介"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="bookDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBookForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分类管理对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      title="分类管理"
      width="60%"
      :close-on-click-modal="false"
    >
      <div class="category-management">
        <div class="category-list">
          <h3>分类列表</h3>
          <el-tree
            :data="categories"
            :expand-on-click-node="false"
            :default-expand-all="true"
            node-key="id"
            class="category-tree"
          >
            <template #default="{ node, data }">
              <div class="category-node">
                <span>{{ node.label }}</span>
                <div class="category-actions">
                  <el-button
                    type="primary"
                    size="small"
                    @click="showEditCategoryDialog(data)"
                  >
                    <el-icon><EditPen /></el-icon>
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="handleDeleteCategory(data.id)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </template>
          </el-tree>
        </div>
        <div class="category-form-container">
          <h3>{{ isEditCategory ? '编辑分类' : '添加分类' }}</h3>
          <el-form
            ref="categoryFormRef"
            :model="categoryForm"
            :rules="categoryRules"
            label-width="80px"
            class="category-form"
          >
            <el-form-item label="分类名称" prop="name">
              <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
            </el-form-item>
            <el-form-item label="父分类">
              <el-select v-model="categoryForm.parentId" placeholder="请选择父分类">
                <el-option label="顶级分类" :value="0" />
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number
                v-model="categoryForm.sort"
                :min="0"
                placeholder="请输入排序"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitCategoryForm">
                {{ isEditCategory ? '更新' : '添加' }}
              </el-button>
              <el-button @click="resetCategoryForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  House,
  Search,
  RefreshRight,
  Plus,
  Plus as FolderPlus,
  Download,
  SwitchButton,
  Delete,
  EditPen,
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { getImageUrl } from '@/utils/imageUtils'
import { booksApi } from '@/api/Books'
import { categoryApi } from '@/api/Category'



// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  bookName: '',
  author: '',
  categoryId: '',
  status: '',
  serialStatus: '',
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0,
})

// 选择的书籍
const selectedBooks = ref([])

// 书籍列表数据
const books = ref([])

// 分类数据
const categories = ref([])

// 书籍表单
const bookForm = reactive({
  id: '',
  title: '',
  author: '',
  cover: '',
  categoryId: '',
  wordCount: 0,
  status: '1',
  serialStatus: 1,
  description: '',
})

// 书籍表单规则
const bookRules = reactive({
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
})

// 书籍表单引用
const bookFormRef = ref()

// 书籍对话框
const bookDialogVisible = ref(false)
const isEditBook = ref(false)

// 分类对话框
const categoryDialogVisible = ref(false)

// 分类表单
const categoryForm = reactive({
  id: '',
  name: '',
  parentId: 0,
  sort: 0,
})

// 分类表单规则
const categoryRules = reactive({
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
})

// 分类表单引用
const categoryFormRef = ref()
const isEditCategory = ref(false)

// 封面文件列表
const coverFileList = ref([])

// 计算属性：筛选后的书籍
const filteredBooks = computed(() => {
  return books.value.filter(book => {
    // 根据搜索条件过滤
    const matchesBookName = searchForm.bookName ? book.title.toLowerCase().includes(searchForm.bookName.toLowerCase()) : true
    const matchesAuthor = searchForm.author ? book.author.toLowerCase().includes(searchForm.author.toLowerCase()) : true
    const matchesCategory = searchForm.categoryId != null && searchForm.categoryId !== '' ? book.categoryId === Number(searchForm.categoryId) : true
    const matchesStatus = searchForm.status != null && searchForm.status !== '' ? book.status === Number(searchForm.status) : true
    // 连载状态暂时注释，因为现在使用status字段表示
    // const matchesSerialStatus = searchForm.serialStatus != null && searchForm.serialStatus !== '' ? book.serialStatus === Number(searchForm.serialStatus) : true

    return matchesBookName && matchesAuthor && matchesCategory && matchesStatus
  })
})

// 计算总数
const total = computed(() => filteredBooks.value.length)

// 计算属性：分页后的书籍
const paginatedBooks = computed(() => {
  const startIndex = (pagination.currentPage - 1) * pagination.pageSize
  const endIndex = startIndex + pagination.pageSize
  return filteredBooks.value.slice(startIndex, endIndex)
})

// 搜索书籍
const searchBooks = () => {
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
    bookName: '',
    author: '',
    categoryId: '',
    status: '',
    serialStatus: '',
  })
  pagination.currentPage = 1
}

// 选择书籍
const handleSelectionChange = (selection) => {
  selectedBooks.value = selection
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

// 显示添加书籍对话框
const showAddBookDialog = () => {
  isEditBook.value = false
  resetBookForm()
  bookDialogVisible.value = true
}

// 显示编辑书籍对话框
const showEditBookDialog = (row) => {
  isEditBook.value = true
  Object.assign(bookForm, row)
  // 清空封面文件列表
  coverFileList.value = []
  bookDialogVisible.value = true
}

// 重置书籍表单
const resetBookForm = () => {
  Object.assign(bookForm, {
    id: '',
    title: '',
    author: '',
    cover: '',
    categoryId: '',
    wordCount: 0,
    status: 1,
    serialStatus: 1,
    description: '',
  })
  coverFileList.value = []
  if (bookFormRef.value) {
    bookFormRef.value.resetFields()
  }
}

// 提交书籍表单
const submitBookForm = async () => {
  if (!bookFormRef.value) return
  bookFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        if (isEditBook.value) {
          // 编辑书籍
          await booksApi.updateBooks(bookForm)
        } else {
          // 添加书籍
          await booksApi.addBooks(bookForm)
        }
        bookDialogVisible.value = false
        fetchBooks() // 刷新书籍列表
      } catch (error) {
        console.error('提交书籍失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 检查图片大小
const beforeCoverUpload = (file) => {
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessageBox.alert('图片大小不能超过 2MB', '警告', {
      confirmButtonText: '确定',
      type: 'warning',
    })
    return false
  }
  return true
}

// 处理封面变化
const handleCoverChange = (file, fileList) => {
  coverFileList.value = fileList
  // 实现图片预览
  if (file.raw) {
    const reader = new FileReader()
    reader.onload = (e) => {
      bookForm.cover = e.target.result
    }
    reader.readAsDataURL(file.raw)
  }
}

// 处理状态变化
const handleStatusChange = async (row) => {
  try {
    const statusText = row.status === 1 ? '上架' : '下架'
    await ElMessageBox.confirm(`确定要将该书籍${statusText}吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    loading.value = true
    await booksApi.updateBooks({
      id: row.id,
      status: row.status
    })
    fetchBooks() // 刷新书籍列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新书籍状态失败:', error)
    }
  } finally {
    loading.value = false
  }
}

// 删除书籍
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该书籍吗？此操作不可恢复。', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true,
    })
    loading.value = true
    await booksApi.deleteBooks(id)
    fetchBooks() // 刷新书籍列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除书籍失败:', error)
    }
  } finally {
    loading.value = false
  }
}

// 批量更新状态
const handleBatchStatusChange = async (status) => {
  if (selectedBooks.value.length === 0) {
    return
  }
  try {
    const statusText = status === 1 ? '上架' : '下架'
    await ElMessageBox.confirm(`确定要将选中的 ${selectedBooks.value.length} 本${statusText}吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    loading.value = true
    const bookIds = selectedBooks.value.map((book) => book.id)
    await booksApi.batchUpdateBooksStatus(bookIds, status)
    fetchBooks() // 刷新书籍列表
    selectedBooks.value = [] // 清空选择
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量更新状态失败:', error)
    }
  } finally {
    loading.value = false
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedBooks.value.length === 0) {
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedBooks.value.length} 本吗？此操作不可恢复。`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true,
    })
    loading.value = true
    const bookIds = selectedBooks.value.map((book) => book.id)
    await booksApi.batchDeleteBooks(bookIds)
    fetchBooks() // 刷新书籍列表
    selectedBooks.value = [] // 清空选择
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除书籍失败:', error)
    }
  } finally {
    loading.value = false
  }
}

// 批量导出
const handleBatchExport = () => {
  // 实际项目中，这里会调用API导出书籍
  console.log('批量导出书籍')
}

// 显示分类管理对话框
const showCategoryDialog = () => {
  categoryDialogVisible.value = true
  resetCategoryForm()
}

// 显示编辑分类对话框
const showEditCategoryDialog = (data) => {
  isEditCategory.value = true
  Object.assign(categoryForm, data)
}

// 重置分类表单
const resetCategoryForm = () => {
  Object.assign(categoryForm, {
    id: '',
    name: '',
    parentId: 0,
    sort: 0,
  })
  isEditCategory.value = false
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
}

// 提交分类表单
const submitCategoryForm = async () => {
  if (!categoryFormRef.value) return
  categoryFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        await categoryApi.addCategory(categoryForm)
        resetCategoryForm()
        fetchCategories() // 刷新分类列表
      } catch (error) {
        console.error('提交分类失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 删除分类
const handleDeleteCategory = async (id) => {
  try {
    loading.value = true
    await categoryApi.deleteCategory(id)
    fetchCategories() // 刷新分类列表
  } catch (error) {
    console.error('删除分类失败:', error)
  } finally {
    loading.value = false
  }
}

// 格式化字数
const formatWordCount = (wordCount) => {
  if (wordCount < 10000) {
    return `${wordCount}字`
  } else {
    return `${(wordCount / 10000).toFixed(2)}万字`
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

// 获取书籍列表
const fetchBooks = async () => {
  try {
    loading.value = true
    const response = await booksApi.getAllBooks()
    books.value = response.data || []
  } catch (error) {
    console.error('获取书籍列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await categoryApi.getCategoryList()
    categories.value = response.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 初始化数据
onMounted(() => {
  fetchBooks()
  fetchCategories()
})
</script>

<style scoped>
.admin-book-management {
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
  gap: 10px;
}

.action-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.books-card {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.book-title {
  font-weight: 500;
}

.book-form {
  padding: 20px 0;
}

.cover-upload {
  cursor: pointer;
}

.category-management {
  display: flex;
  gap: 20px;
  height: 500px;
}

.category-list {
  flex: 1;
  border-right: 1px solid #e6e6e6;
  padding-right: 20px;
  overflow-y: auto;
}

.category-list h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
}

.category-tree {
  max-height: 400px;
  overflow-y: auto;
}

.category-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
}

.category-actions {
  display: flex;
  gap: 5px;
}

.category-form-container {
  flex: 1;
  padding-left: 20px;
}

.category-form-container h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
}

.category-form {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
