<template>
  <div class="bookshelf-page">
    <div class="bookshelf-container">
      <!-- 侧边导航栏 -->
      <aside class="bookshelf-sidebar">
        <div class="sidebar-header">
          <h2 class="sidebar-title">我的书架</h2>
          <el-button type="primary" size="small" @click="createDialogVisible = true">
            <el-icon><Plus /></el-icon> 创建书架
          </el-button>
        </div>
        <div class="sidebar-content">
          <div
            v-for="bookshelf in bookshelves"
            :key="bookshelf.id"
            class="sidebar-item"
            :class="{ 'active': currentBookshelf && currentBookshelf.id === bookshelf.id }"
            @click="selectBookshelf(bookshelf)"
          >
            <span class="bookshelf-name">{{ bookshelf.name }}</span>
            <div class="sidebar-item-actions">
              <el-button size="mini" circle @click.stop="openEditDialog(bookshelf)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button size="mini" circle type="danger" @click.stop="deleteBookshelf(bookshelf.id, bookshelf.name)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </aside>

      <!-- 主内容区域 -->
      <main class="bookshelf-main">
        <!-- 顶部操作栏 -->
        <div class="bookshelf-header">
          <div class="bookshelf-title-container">
            <h1 class="page-title">{{ currentBookshelf ? currentBookshelf.name : '我的书架' }}</h1>
            <span class="bookshelf-intro" v-if="currentBookshelf && currentBookshelf.description">: {{ currentBookshelf.description }}</span>
          </div>
          <div class="header-actions">
            <!-- 编辑按钮 -->
            <el-button
              size="small"
              @click="toggleEditMode"
              :type="isEditMode ? 'primary' : 'default'"
            >
              <el-icon><Edit /></el-icon> {{ isEditMode ? '取消' : '编辑' }}
            </el-button>
            <!-- 批量删除按钮 -->
            <el-button
              size="small"
              type="danger"
              @click="batchDelete"
              :disabled="selectedBooks.length === 0 || !isEditMode"
            >
              <el-icon><Delete /></el-icon> 批量删除
            </el-button>
            <!-- 批量移动按钮 -->
            <el-button
              size="small"
              @click="batchMoveDialogVisible = true"
              :disabled="selectedBooks.length === 0 || !isEditMode"
            >
              <el-icon><SwitchButton /></el-icon> 批量移动
            </el-button>
          </div>
        </div>

        <!-- 筛选区域 -->
        <div class="filter-section">
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索书籍"
              clearable
              prefix-icon="Search"
            />
          </div>
          <div class="filter-actions">
            <!-- 排序选项 -->
            <div class="sort-options">
              <span>排序：</span>
              <el-radio-group v-model="sortBy" size="small">
                <el-radio-button label="lastReadTime">最近阅读</el-radio-button>
                <el-radio-button label="lastUpdateTime">最近更新</el-radio-button>
              </el-radio-group>
            </div>
            <!-- 状态筛选 -->
            <div class="status-filter">
              <span>状态：</span>
              <el-radio-group v-model="statusFilter" size="small">
                <el-radio-button label="">全部</el-radio-button>
                <el-radio-button label="连载中">连载中</el-radio-button>
                <el-radio-button label="已完结">已完结</el-radio-button>
              </el-radio-group>
            </div>
            <!-- 视图切换 -->
            <div class="view-options">
              <span>视图：</span>
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button label="grid">网格</el-radio-button>
                <el-radio-button label="list">列表</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </div>

        <!-- 书籍内容区域 -->
        <div class="books-content" v-if="currentBookshelf">

        <!-- 网格视图 -->
        <div v-if="viewMode === 'grid' && sortedBooks.length > 0" class="grid-mode" :class="{ 'isEditMode': isEditMode }">
          <div class="grid-view">
            <div
              v-for="book in sortedBooks"
              :key="book.bookId"
              class="grid-item"
              @click="!isEditMode && goToBookDetail(book.bookId)"
              @click.stop="isEditMode && !$event.target.closest('.el-checkbox') && toggleBookSelection(book.bookId)"
            >
            <!-- 编辑模式复选框 -->
              <div class="edit-checkbox" v-if="isEditMode">
                <el-checkbox v-model="selectedBooks" :value="book.bookId" @change="onSelectChange" />
              </div>

              <div class="book-cover" :class="{ 'darkened': isEditMode }">
                <img :src="getImageUrl(book.bookCover)" :alt="book.bookName" class="cover-img">
                <div class="book-status" :class="book.statusName">
                  {{ book.statusName }}
                </div>
              </div>

              <div class="book-info">
                <h4 class="book-name">{{ book.bookName }}</h4>
                <p class="book-author-name">
                  <span class="clickable-text">{{ book.authorName }}</span>·
                  <span class="clickable-text">{{ book.categoryName }}</span>
                </p>
                <div class="last-chapter" v-if="book.lastChapterName">
                  最后读至：{{ book.lastChapterName }}
                </div>
                <div class="reading-meta" v-if="book.readProgress">
                  <span class="reading-progress">进度：{{ Math.round(book.readProgress) }}%</span>
                </div>
                <div class="last-read-time">
                  最后阅读：{{ formatDate(book.lastReadTime || book.lastUpdateTime) }}
                </div>
                <div class="book-actions">
                  <el-button type="primary" size="small" @click.stop="goToRead(book.bookId, book.lastReadChapter || 1)">
                    <el-icon><Reading /></el-icon> 立即阅读
                  </el-button>
                  <el-button size="small" class="hover-button" @click.stop="openMoveDialog(book)">
                    <el-icon><SwitchButton /></el-icon> 移动
                  </el-button>
                  <el-button size="small" type="danger" class="hover-button" @click.stop="handleRemoveFavorite(book.bookId)">
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 网格视图分页 -->
          <div v-if="total > pageSize" class="pagination-container">
            <el-pagination
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              v-model:current-page="currentPage"
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <!-- 列表视图 -->
        <div v-else-if="viewMode === 'list' && sortedBooks.length > 0" class="list-mode" :class="{ 'isEditMode': isEditMode }">
          <div class="list-view">
            <!-- 表头 -->
            <div class="list-header">
              <div class="header-checkbox" v-if="isEditMode">
                <el-checkbox v-model="selectAll" @change="handleSelectAll" />
              </div>
              <div class="header-category">分类</div>
              <div class="header-title">书名</div>
              <div class="header-chapter">最后阅读章节</div>
              <div class="header-author">作者</div>
              <div class="header-status">状态</div>
              <div class="header-update-time">最后更新时间</div>
              <div class="header-actions">操作</div>
            </div>

            <!-- 列表内容 -->
            <div
              v-for="book in sortedBooks"
              :key="book.bookId"
              class="list-item"
              @click.stop="isEditMode && !$event.target.closest('.el-checkbox') && toggleBookSelection(book.bookId)"
            >
              <!-- 编辑模式复选框 -->
              <div class="item-checkbox" v-if="isEditMode">
                <el-checkbox v-model="selectedBooks" :value="book.bookId" @change="onSelectChange" />
              </div>

              <!-- 分类 -->
              <div class="item-category">{{ book.categoryName }}</div>

              <!-- 书名 -->
              <div class="item-title" @click="!isEditMode && goToBookDetail(book.bookId)">
                {{ book.bookName }}
              </div>

              <!-- 最后阅读章节 -->
              <div class="item-chapter">{{ book.lastChapterName || '未开始阅读' }}</div>

              <!-- 作者 -->
              <div class="item-author">{{ book.authorName }}</div>

              <!-- 状态 -->
              <div class="item-status" :class="book.statusName">
                {{ book.statusName }}
              </div>

              <!-- 最后更新时间 -->
              <div class="item-update-time">
                {{ formatYearMonth(book.lastUpdateTime) }}
              </div>

              <!-- 操作按钮 -->
              <div class="item-actions">
                <el-button type="primary" size="small" @click.stop="goToRead(book.bookId, book.lastReadChapter || 1)">
                  <el-icon><Reading /></el-icon> 立即阅读
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="sortedBooks.length === 0" class="empty-bookshelf">
          <el-empty description="当前书架没有书籍">
            <el-button type="primary" @click="goToLibrary">去书库添加书籍</el-button>
          </el-empty>
        </div>
      </div>
    </main>

    <!-- 创建书架对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建书架"
      width="400px"
    >
      <div class="dialog-form">
        <el-form-item label="书架名称" required>
          <el-input v-model="newBookshelfName" placeholder="请输入书架名称" />
        </el-form-item>
        <el-form-item label="书架描述">
          <el-input v-model="newBookshelfDesc" type="textarea" placeholder="请输入书架描述" :rows="3" />
        </el-form-item>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createBookshelf">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑书架对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑书架"
      width="400px"
    >
      <div class="dialog-form">
        <el-form-item label="书架名称" required>
          <el-input v-model="editBookshelfData.name" placeholder="请输入书架名称" />
        </el-form-item>
        <el-form-item label="书架描述">
          <el-input v-model="editBookshelfData.description" type="textarea" placeholder="请输入书架描述" :rows="3" />
        </el-form-item>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="editBookshelf">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 移动书籍对话框 -->
    <el-dialog
      v-model="moveDialogVisible"
      title="移动书籍"
      width="400px"
    >
      <div class="dialog-form">
        <el-form-item label="目标书架" required>
          <el-select v-model="targetBookshelfId" placeholder="请选择目标书架">
            <el-option
              v-for="bookshelf in bookshelves"
              :key="bookshelf.id"
              :label="bookshelf.name"
              :value="bookshelf.id"
              :disabled="currentBookshelf && currentBookshelf.id === bookshelf.id"
            />
          </el-select>
        </el-form-item>
        <el-divider />
        <div class="add-bookshelf-section">
          <h4>没有合适的书架？</h4>
          <el-button type="primary" size="small" @click="createDialogVisible = true">
            <el-icon><Plus /></el-icon> 创建新书架
          </el-button>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="moveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="moveBooks">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量移动书籍对话框 -->
    <el-dialog
      v-model="batchMoveDialogVisible"
      title="批量移动书籍"
      width="400px"
    >
      <div class="dialog-form">
        <el-form-item label="目标书架" required>
          <el-select v-model="targetBookshelfId" placeholder="请选择目标书架">
            <el-option
              v-for="bookshelf in bookshelves"
              :key="bookshelf.id"
              :label="bookshelf.name"
              :value="bookshelf.id"
              :disabled="currentBookshelf && currentBookshelf.id === bookshelf.id"
            />
          </el-select>
        </el-form-item>
        <el-divider />
        <div class="add-bookshelf-section">
          <h4>没有合适的书架？</h4>
          <el-button type="primary" size="small" @click="createDialogVisible = true">
            <el-icon><Plus /></el-icon> 创建新书架
          </el-button>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchMoveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="batchMove">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElDialog, ElInput } from 'element-plus'
import { Reading, Plus, Edit, Delete, SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { bookshelvesApi } from '../api/Bookshelves'
import { getImageUrl } from '@/utils/imageUtils'

const router = useRouter()
const authStore = useAuthStore()

// 视图模式
const viewMode = ref('grid')
const sortBy = ref('lastReadTime')
const statusFilter = ref('') // 状态筛选：'' 全部, '连载中' 连载中, '已完结' 已完结
const searchKeyword = ref('') // 搜索关键词

// 书架相关数据
const bookshelves = ref([]) // 书架列表
const currentBookshelf = ref(null) // 当前选中的书架
const books = ref([]) // 当前书架的书籍列表

// 编辑模式
const isEditMode = ref(false) // 是否处于编辑模式
const selectedBooks = ref([]) // 选中的书籍
const selectAll = ref(false) // 是否全选

// 对话框状态
const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const moveDialogVisible = ref(false)
const batchMoveDialogVisible = ref(false)

// 表单数据
const newBookshelfName = ref('')
const newBookshelfDesc = ref('')
const editBookshelfData = ref({})
const targetBookshelfId = ref(null) // 目标书架ID

// 加载状态
const loading = ref(false)
const booksLoading = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 计算属性：过滤后的书籍总数
const filteredBooksCount = computed(() => {
  let filteredBooks = [...books.value]
  // 搜索关键词筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filteredBooks = filteredBooks.filter(book =>
      book.bookName.toLowerCase().includes(keyword) ||
      book.authorName.toLowerCase().includes(keyword) ||
      book.categoryName.toLowerCase().includes(keyword)
    )
  }

  // 状态筛选
  if (statusFilter.value) {
    filteredBooks = filteredBooks.filter(book => book.statusName === statusFilter.value)
  }

  return filteredBooks.length
})

// 计算属性：当前书架的书籍（带分页）
const sortedBooks = computed(() => {
  let filteredBooks = [...books.value]
  // 搜索关键词筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filteredBooks = filteredBooks.filter(book =>
      book.bookName.toLowerCase().includes(keyword) ||
      book.authorName.toLowerCase().includes(keyword) ||
      book.categoryName.toLowerCase().includes(keyword)
    )
  }

  // 状态筛选
  if (statusFilter.value) {
    filteredBooks = filteredBooks.filter(book => book.statusName === statusFilter.value)
  }

  // 排序逻辑
  filteredBooks.sort((a, b) => {
    switch (sortBy.value) {
      case 'lastReadTime':
        return new Date(b.lastReadTime || 0) - new Date(a.lastReadTime || 0)
      case 'lastUpdateTime':
        return new Date(b.lastUpdateTime || 0) - new Date(a.lastUpdateTime || 0)
      default:
        return 0
    }
  })

  // 分页逻辑
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredBooks.slice(start, end)
})

// 监听过滤后的书籍总数变化，更新总页数
watch(filteredBooksCount, (newCount) => {
  total.value = newCount
})

// 切换编辑模式
const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value
  if (!isEditMode.value) {
    // 退出编辑模式，清空选择
    selectedBooks.value = []
    selectAll.value = false
  }
}

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page
}

// 全选/取消全选
const handleSelectAll = (value) => {
  selectAll.value = value
  if (value) {
    // 全选
    selectedBooks.value = books.value.map(book => book.bookId)
  } else {
    // 取消全选
    selectedBooks.value = []
  }
}

// 单个选择变化
const onSelectChange = () => {
  // 检查是否全选
  selectAll.value = selectedBooks.value.length === books.value.length
}

// 批量删除
const batchDelete = async () => {
  if (selectedBooks.value.length === 0) {
    ElMessage.warning('请先选择要删除的书籍')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的${selectedBooks.value.length}本书籍吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 调用删除API - 这里需要根据实际API调整
    for (const bookId of selectedBooks.value) {
      await bookshelvesApi.removeBookFromShelves(bookId, authStore.user.id)
    }

    // 重新获取书籍
    if (currentBookshelf.value) {
      getBooksInBookshelf(currentBookshelf.value.id)
    }

    ElMessage.success(`已删除选中的${selectedBooks.value.length}本书籍`)
        // 清空选择
    selectedBooks.value = []
    selectAll.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 批量移动
const batchMove = async () => {
  if (selectedBooks.value.length === 0 || !targetBookshelfId.value) {
    ElMessage.warning('请先选择要移动的书籍和目标书架')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要将选中的${selectedBooks.value.length}本书籍移动到目标书架吗？`,
      '移动确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    for (const bookId of selectedBooks.value) {
      await bookshelvesApi.moveBookToBookshelf(
        bookId,
        authStore.user.id,
        targetBookshelfId.value
    )}
    // 调用移动API


    // 清空选择和目标书架
    selectedBooks.value = []
    selectAll.value = false
    targetBookshelfId.value = null
    batchMoveDialogVisible.value = false

    // 重新获取书籍
    if (currentBookshelf.value) {
      getBooksInBookshelf(currentBookshelf.value.id)
    }

    ElMessage.success(`已移动选中的${selectedBooks.value.length}本书籍`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量移动失败:', error)
      ElMessage.error('批量移动失败')
    }
  }
}

// 切换单个书籍选择状态
const toggleBookSelection = (bookId) => {
  const index = selectedBooks.value.indexOf(bookId)
  if (index > -1) {
    selectedBooks.value.splice(index, 1)
  } else {
    selectedBooks.value.push(bookId)
  }
  // 更新全选状态
  onSelectChange()
}

// 跳转到书籍详情页
const goToBookDetail = (bookId) => {
  router.push(`/book/${bookId}`)
}

// 跳转到阅读页面
const goToRead = (bookId, chapterId) => {
  // 在新标签页打开阅读页面
  window.open(`/read/${bookId}/${chapterId}`, '_blank')
}

// 跳转到书库
const goToLibrary = () => {
  router.push('/library')
}

// 处理移除收藏
const handleRemoveFavorite = async (bookId) => {
  try {
    await ElMessageBox.confirm(
      '确定要将这本书从书架中移除吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const response = await bookshelvesApi.removeBookFromShelves(bookId, authStore.user.id)
    if (response.code === 200) {
      ElMessage.success('已从书架移除')
      // 重新获取当前书架的书籍
      if (currentBookshelf.value) {
        getBooksInBookshelf(currentBookshelf.value.id)
      }
    } else {
      ElMessage.error(response.message || '移除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除书籍失败:', error)
      ElMessage.error('移除失败')
    }
  }
}


// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date

  if (diff < 24 * 60 * 60 * 1000) {
    return '今天'
  } else if (diff < 7 * 24 * 60 * 60 * 1000) {
    return Math.floor(diff / (24 * 60 * 60 * 1000)) + '天前'
  } else {
    return dateStr
  }
}

// 格式化年月日（用于最后更新时间）
const formatYearMonth = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 获取书架列表
const getBookshelves = async () => {
  if (!authStore.user?.id) return

  loading.value = true
  try {
    const response = await bookshelvesApi.getUserBookshelves(authStore.user.id)
    if (response.code === 200) {
      bookshelves.value = response.data || []
      // 默认选择第一个书架
      if (bookshelves.value.length > 0 && !currentBookshelf.value) {
        currentBookshelf.value = bookshelves.value[0]
        getBooksInBookshelf(bookshelves.value[0].id)
      }
    } else {
      ElMessage.error(response.message || '获取书架列表失败')
    }
  } catch (error) {
    console.error('获取书架列表失败:', error)
    ElMessage.error('获取书架列表失败')
  } finally {
    loading.value = false
  }
}

// 获取书架中的书籍
const getBooksInBookshelf = async (bookshelfId) => {
  if (!authStore.user?.id || !bookshelfId) return

  booksLoading.value = true
  try {
    const response = await bookshelvesApi.getBooksInBookshelf(authStore.user.id, bookshelfId)
    if (response.code === 200) {
      books.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取书籍列表失败')
    }
  } catch (error) {
    console.error('获取书籍列表失败:', error)
    ElMessage.error('获取书籍列表失败')
  } finally {
    booksLoading.value = false
  }
}

// 创建书架
const createBookshelf = async () => {
  if (!authStore.user?.id || !newBookshelfName.value.trim()) {
    ElMessage.warning('请输入书架名称')
    return
  }

  try {
    const response = await bookshelvesApi.createBookshelf(authStore.user.id, newBookshelfName.value.trim())
    if (response.code === 200) {
      ElMessage.success('创建书架成功')
      createDialogVisible.value = false
      newBookshelfName.value = ''
      newBookshelfDesc.value = ''
      // 重新获取书架列表
      getBookshelves()
    } else {
      ElMessage.error(response.message || '创建书架失败')
    }
  } catch (error) {
    console.error('创建书架失败:', error)
    ElMessage.error('创建书架失败')
  }
}

// 编辑书架
const editBookshelf = async () => {
  if (!authStore.user?.id || !editBookshelfData.value.id) return

  try {
    const response = await bookshelvesApi.updateBookshelfName(editBookshelfData.value)
    if (response.code === 200) {
      ElMessage.success('更新书架成功')
      editDialogVisible.value = false
      // 重新获取书架列表
      getBookshelves()
    } else {
      ElMessage.error(response.message || '更新书架失败')
    }
  } catch (error) {
    console.error('更新书架失败:', error)
    ElMessage.error('更新书架失败')
  }
}

// 删除书架
const deleteBookshelf = async (bookshelfId, bookshelfName) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除书架"${bookshelfName}"吗？该书架下的所有书籍也会被删除。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await bookshelvesApi.deleteBookshelf(bookshelfId)
    if (response.code === 200) {
      ElMessage.success('删除书架成功')
      // 重新获取书架列表
      getBookshelves()
    } else {
      ElMessage.error(response.message || '删除书架失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除书架失败:', error)
      ElMessage.error('删除书架失败')
    }
  }
}

// 移动书籍
const moveBooks = async () => {
  if (!selectedBooks.value.length || !targetBookshelfId.value) {
    ElMessage.warning('请选择要移动的书籍和目标书架')
    return
  }

  try {
    const response = await bookshelvesApi.moveBookToBookshelf(
      selectedBooks.value,
      authStore.user.id,
      targetBookshelfId.value
    )
    if (response.code === 200) {
      ElMessage.success('移动书籍成功')
      moveDialogVisible.value = false
      selectedBooks.value = []
      targetBookshelfId.value = null
      // 重新获取当前书架的书籍
      getBooksInBookshelf(currentBookshelf.value.id)
    } else {
      ElMessage.error(response.message || '移动书籍失败')
    }
  } catch (error) {
    console.error('移动书籍失败:', error)
    ElMessage.error('移动书籍失败')
  }
}

// 选择当前书架
const selectBookshelf = (bookshelf) => {
  currentBookshelf.value = bookshelf
  getBooksInBookshelf(bookshelf.id)
}

// 打开编辑对话框
const openEditDialog = (bookshelf) => {
  editBookshelfData.value = { ...bookshelf }
  editDialogVisible.value = true
}

// 打开移动对话框
const openMoveDialog = (book) => {
  selectedBooks.value = [book.bookId]
  moveDialogVisible.value = true
}

// 组件挂载时初始化
onMounted(() => {
  getBookshelves()
})
</script>

<style scoped>
.bookshelf-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 80px 20px 0;
}

.bookshelf-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.bookshelf-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.bookshelf-title-container {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.bookshelf-intro {
  font-size: 13px;
  color: #666;
  margin: 0 0 0 8px;
  line-height: 1.4;
}

.view-options {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 10px;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

.book-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.book-cover {
  position: relative;
  height: 240px;
  overflow: hidden;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s;
}

.book-cover img:hover {
  transform: scale(1.05);
}

.book-status {
  position: absolute;
  top: 5px;
  right: 5px;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
  color: #fff;
}

.book-status.completed {
  background-color: #67c23a;
}

.book-status.ongoing {
  background-color: #e6a23c;
}

.book-info {
  padding: 15px;
}

.book-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 5px;
  color: #333;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-title:hover {
  color: #409eff;
}

.book-author {
  font-size: 14px;
  color: #666;
  margin: 0 0 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.last-chapter {
  font-size: 12px;
  color: #666;
  margin: 5px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.last-read-time {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.book-progress {
  margin-top: 10px;
}

.progress-text {
  font-size: 12px;
  color: #666;
  margin-left: 5px;
}

.books-list {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.book-item {
  display: flex;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.book-item:last-child {
  border-bottom: none;
}

.book-item .book-cover {
  width: 120px;
  height: 160px;
  margin-right: 20px;
  flex-shrink: 0;
}

.book-item .book-info {
  flex: 1;
}

.book-item .book-title {
  font-size: 18px;
  margin-bottom: 10px;
}

.book-item .book-author,
.book-item .book-category {
  font-size: 14px;
  color: #666;
  margin: 5px 0;
}

.book-item .book-desc {
  font-size: 14px;
  color: #666;
  margin: 10px 0;
  line-height: 1.6;
  display: -webkit-box;
  display: -moz-box;
  display: box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  -moz-box-orient: vertical;
  box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  /* 兼容性后备方案 */
  max-height: 3.2em; /* 2行 × 1.6行高 */
  position: relative;
}

.book-item .book-stats {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #999;
  margin: 10px 0;
}

.book-item .book-stats span {
  display: flex;
  align-items: center;
  gap: 3px;
}

.book-item .book-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
  margin-left: 20px;
}

.empty-bookshelf {
  background-color: #fff;
  border-radius: 8px;
  padding: 60px 0;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.initial-state {
  background-color: #fff;
  border-radius: 8px;
  padding: 60px 0;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 书架导航样式 */
.bookshelf-nav {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  background-color: #fff;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
}

.bookshelf-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 15px;
  background-color: #f5f7fa;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.bookshelf-nav-item:hover {
  background-color: #e4e7ed;
}

.bookshelf-nav-item.active {
  background-color: #409eff;
  color: #fff;
}

.bookshelf-nav-actions {
  display: flex;
  gap: 5px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.bookshelf-nav-item:hover .bookshelf-nav-actions {
  opacity: 1;
}

/* 对话框样式 */
.dialog-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 网格视图样式 */
.grid-view {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  margin-bottom: 20px;
}

.grid-item {
  display: flex;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.2s;
  min-height: 200px;
  overflow: hidden;
}

.grid-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.grid-item .book-cover {
  flex: 0 0 150px;
  height: 100%;
  overflow: hidden;
}

.grid-item .book-cover .cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.grid-item:hover .book-cover .cover-img {
  transform: scale(1.05);
}

.grid-item .book-info {
  flex: 1;
  padding: 15px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.grid-item .book-name {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 10px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #333;
}

.grid-item .book-author-name {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #666;
}

.grid-item .book-description {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.grid-item .reading-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #999;
}

.grid-item .last-read-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 15px;
}

.grid-item .book-actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
  flex-wrap: nowrap;
  justify-content: flex-start;
  align-items: center;
  position: relative;
  min-height: 32px;
}

.grid-item .book-actions .el-button:not(.hover-button) {
  flex: 0 0 auto;
  padding: 6px 10px;
  font-size: 12px;
  min-width: 80px;
  width: 80px;
  transition: all 0.3s ease;
}

.grid-item .book-actions .hover-button {
  opacity: 0;
  width: 0;
  padding: 6px 0;
  margin: 0;
  overflow: hidden;
  flex: none;
  transition: all 0.3s ease;
  pointer-events: none;
}

.grid-item:hover .book-actions .hover-button {
  opacity: 1 !important;
  width: 80px !important;
  padding: 6px 10px !important;
  margin: 0;
  flex: 0 0 auto;
  pointer-events: auto;
}

.grid-item .book-actions .el-button[type="primary"]:not(.hover-button) {
  opacity: 1;
  transition: all 0.3s ease;
  flex: 0 0 auto;
  width: 80px;
}

/* 列表视图样式 */
.list-mode {
  margin-top: 20px;
}

.list-view {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.book-item {
  display: flex;
  padding: 15px;
  border-bottom: 1px solid #f5f5f5;
}

.book-item:last-child {
  border-bottom: none;
}

.book-item .book-cover {
  flex: 0 0 100px;
  height: 140px;
  margin-right: 20px;
}

.book-item .book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.book-item .book-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.book-item .book-actions {
  display: flex;
  gap: 8px;
  margin-top: 15px;
}

/* 整体布局 */
.bookshelf-container {
  display: flex;
  min-height: calc(100vh - 80px);
}

/* 侧边导航栏样式 */
.bookshelf-sidebar {
  width: 250px;
  background-color: #fff;
  border-right: 1px solid #eaeaea;
  display: flex;
  flex-direction: column;
  padding: 20px 0;
}

.sidebar-header {
  padding: 0 20px 20px;
  border-bottom: 1px solid #eaeaea;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
}

.sidebar-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.sidebar-item:hover {
  background-color: #f5f7fa;
}

.sidebar-item.active {
  background-color: #ecf5ff;
  color: #409eff;
  border-right: 3px solid #409eff;
}

.sidebar-item-actions {
  display: flex;
  gap: 5px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.sidebar-item:hover .sidebar-item-actions {
  opacity: 1;
}

/* 主内容区域样式 */
.bookshelf-main {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

/* 顶部操作栏样式 */
.bookshelf-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}

.bookshelf-title-container {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.bookshelf-intro {
  font-size: 13px;
  color: #666;
  margin: 0 0 0 8px;
  line-height: 1.4;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 筛选区域样式 */
.filter-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.search-box {
  margin-bottom: 20px;
}

.filter-actions {
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.sort-options, .status-filter {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 编辑模式样式 */
.edit-checkbox {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 10;
  transform: scale(1.2); /* 放大勾选框，使其更突出 */
}

.grid-item {
  position: relative;
}

/* 编辑模式下封面变暗 */
.book-cover.darkened {
  position: relative;
  filter: brightness(0.6); /* 降低亮度，使封面变暗 */
  transition: filter 0.3s ease;
}

.book-cover.darkened::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.4); /* 添加半透明遮罩层 */
  pointer-events: none;
}

/* 编辑模式下隐藏操作按钮 */
.grid-mode .book-actions {
  opacity: 1;
  transition: opacity 0.3s ease;
}

/* 编辑模式下隐藏所有操作按钮 */
.isEditMode .book-actions {
  opacity: 0 !important;
  pointer-events: none;
}

/* 编辑模式下隐藏悬停按钮 */
.isEditMode .hover-button {
  display: none !important;
}

/* 列表视图样式 */
.list-view {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.list-header {
  display: grid;
  grid-template-columns: 80px 160px 160px 120px 80px 120px 100px 120px;
  padding: 12px 15px;
  background-color: #fafafa;
  border-bottom: 1px solid #eaeaea;
  font-weight: bold;
  font-size: 14px;
}

.list-item {
  display: grid;
  grid-template-columns: 80px 160px 160px 120px 80px 120px 100px 120px;
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
  transition: all 0.3s ease;
}

/* 编辑模式下的网格布局 - 调整为9列，适配复选框 */
.isEditMode .list-header,
.isEditMode .list-item {
  grid-template-columns: 20px 80px 160px 160px 120px 80px 120px 100px 120px;
}

.list-item:hover {
  background-color: #f5f7fa;
}

.list-item:last-child {
  border-bottom: none;
}

.item-checkbox {
  align-items: center;
  justify-content: center;
}

.item-title {
  font-weight: 500;
  cursor: pointer;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-title:hover {
  color: #409eff;
}

.item-category {
  white-space: nowrap;
  overflow: hidden;
}

.item-author,
.item-update-time {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-chapter {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-status {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  color: #606266;
  background-color: #f0f2f5;
}

.item-status.ongoing {
  background-color: #e6a23c;
  color: #fff;
}

.item-status.completed {
  background-color: #67c23a;
  color: #fff;
}

.item-status.paused {
  background-color: #909399;
  color: #fff;
}

.item-status.hiatus {
  background-color: #f56c6c;
  color: #fff;
}

.item-progress {
  width: 100%;
}

.item-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
}

/* 对话框样式 */
.add-bookshelf-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.add-bookshelf-section h4 {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

/* 点击文本样式 */
.clickable-text {
  color: #409eff;
  cursor: pointer;
}

.clickable-text:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .bookshelf-sidebar {
    width: 200px;
  }

  .list-header,
  .list-item {
    grid-template-columns: 80px 160px 220px 100px 70px 110px 90px 110px;
    padding: 10px 12px;
  }
}

@media (max-width: 992px) {
  .bookshelf-container {
    flex-direction: column;
  }

  .bookshelf-sidebar {
    width: 100%;
    flex-direction: row;
    border-right: none;
    border-bottom: 1px solid #eaeaea;
    padding: 10px 0;
  }

  .sidebar-header {
    border-bottom: none;
    padding: 0 15px;
  }

  .sidebar-content {
    flex: 1;
    overflow-y: visible;
    overflow-x: auto;
    padding: 0 10px;
  }

  .sidebar-item {
    white-space: nowrap;
    padding: 8px 15px;
    border-right: 1px solid #eaeaea;
  }

  .sidebar-item:last-child {
    border-right: none;
  }

  .sidebar-item.active {
    border-right: 3px solid #409eff;
    border-left: none;
  }

  .list-header,
  .list-item {
    grid-template-columns: 35px 200px 110px 80px 60px 100px 80px 100px;
    font-size: 13px;
    padding: 8px 10px;
  }
}

@media (max-width: 768px) {
  .bookshelf-main {
    padding: 10px;
  }

  .bookshelf-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .filter-section {
    padding: 15px;
  }

  .filter-actions {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .view-options {
    flex-direction: row;
    gap: 10px;
    align-items: center;
  }

  .sort-options, .status-filter {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .grid-view {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .grid-item {
    flex-direction: column;
    min-height: auto;
  }

  .grid-item .book-cover {
    flex: none;
    height: 200px;
    width: 100%;
  }

  .list-header,
  .list-item {
    grid-template-columns: 30px 1fr;
    gap: 10px;
    align-items: flex-start;
  }

  .list-header > div:not(:first-child),
  .list-item > div:not(:first-child),
  .list-header > div:not(:nth-child(2)),
  .list-item > div:not(:nth-child(2)) {
    display: none;
  }

  .list-item > div:nth-child(2) {
    grid-column: 2;
  }
}

@media (max-width: 576px) {
  .sidebar-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .sidebar-item-actions {
    opacity: 1;
  }

  .grid-view {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .container {
    max-width: 960px;
  }
}

@media (max-width: 992px) {
  .container {
    max-width: 720px;
  }

  .books-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }

  .book-item .book-cover {
    width: 110px;
    height: 150px;
    margin-right: 15px;
  }

  .book-item .book-title {
    font-size: 17px;
  }
}

@media (max-width: 768px) {
  .container {
    max-width: 540px;
    padding: 0 15px;
  }

  .bookshelf-page {
    padding: 15px 0;
  }

  .bookshelf-container {
    padding: 0;
  }

  .bookshelf-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .page-title {
    font-size: 22px;
    margin-bottom: 15px;
  }

  .bookshelf-description {
    font-size: 13px;
    margin: 3px 0 0 0;
  }

  .view-options {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .books-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
  }

  .book-cover {
    height: 220px;
  }

  .book-info {
    padding: 12px;
  }

  .book-title {
    font-size: 14px;
    margin-bottom: 5px;
  }

  .book-author {
    font-size: 13px;
    margin-bottom: 4px;
  }

  .book-update-time {
    font-size: 11px;
  }

  .book-item {
    flex-direction: column;
    padding: 15px 0;
  }

  .book-item .book-cover {
    width: 100px;
    height: 130px;
    margin: 0 auto 15px;
  }

  .book-item .book-info {
    text-align: center;
  }

  .book-item .book-title {
    font-size: 16px;
    margin-bottom: 8px;
  }

  .book-item .book-author,
  .book-item .book-category {
    font-size: 13px;
    margin: 3px 0;
  }

  .book-item .book-desc {
    font-size: 13px;
    margin: 8px 0;
  }

  .book-item .book-stats {
    font-size: 12px;
    justify-content: center;
  }

  .book-item .book-actions {
    flex-direction: row;
    margin-left: 0;
    margin-top: 15px;
  }
}

@media (max-width: 576px) {
  .container {
    max-width: 100%;
    padding: 0 12px;
  }

  .bookshelf-page {
    padding: 12px 0;
  }

  .page-title {
    font-size: 20px;
    margin-bottom: 12px;
  }

  .bookshelf-description {
    font-size: 12px;
    margin: 2px 0 0 0;
  }

  .bookshelf-header {
    gap: 12px;
  }

  .view-options {
    gap: 8px;
    flex-direction: row;
    align-items: center;
  }

  .books-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .book-cover {
    height: 200px;
  }

  .book-info {
    padding: 10px;
  }

  .book-title {
    font-size: 13px;
    margin-bottom: 4px;
  }

  .book-author {
    font-size: 12px;
    margin-bottom: 3px;
  }

  .book-update-time {
    font-size: 10px;
  }

  .progress-text {
    font-size: 11px;
  }

  .book-item {
    padding: 12px 0;
  }

  .book-item .book-cover {
    width: 90px;
    height: 120px;
    margin-bottom: 12px;
  }

  .book-item .book-title {
    font-size: 15px;
    margin-bottom: 6px;
  }

  .book-item .book-author,
  .book-item .book-category {
    font-size: 12px;
    margin: 2px 0;
  }

  .book-item .book-desc {
    font-size: 12px;
    margin: 6px 0;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    max-height: 3.6em; /* 3行 × 1.2行高 (12px × 1.2) */
  }

  .book-item .book-stats {
    font-size: 11px;
    gap: 10px;
  }

  .empty-bookshelf {
    padding: 40px 0;
  }
}
</style>
