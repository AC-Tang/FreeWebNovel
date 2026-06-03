<template>
  <div class="search-page">
    <div class="search-container">
      <div class="search-header">
        <div class="search-box">
          <el-input
            v-model="searchQuery"
            placeholder="搜索书名、作者、关键词"
            size="large"
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>

        <div class="search-filters">
          <el-radio-group v-model="searchType" size="large">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="title">书名</el-radio-button>
            <el-radio-button label="author">作者</el-radio-button>
          </el-radio-group>
        </div>
      </div>



      <div class="search-results" v-if="hasSearched">
        <!-- 骨架屏加载效果 -->
        <div class="skeleton-loading" v-if="loading && searchResults.length === 0">
          <div v-for="i in 6" :key="i" class="skeleton-item">
            <div class="skeleton-cover"></div>
            <div class="skeleton-info">
              <div class="skeleton-title"></div>
              <div class="skeleton-meta"></div>
              <div class="skeleton-desc"></div>
              <div class="skeleton-actions"></div>
            </div>
          </div>
        </div>

        <!-- 排序导航栏 -->
        <div class="sort-nav">
          <div class="sort-nav-item" :class="{ active: sortBy === 'popularity' }" @click="sortBy = 'popularity'">
            人气排序
          </div>
          <div class="sort-nav-item" :class="{ active: sortBy === 'updateTime' }" @click="sortBy = 'updateTime'">
            更新时间
          </div>
          <div class="sort-nav-item" :class="{ active: sortBy === 'wordCount' }" @click="sortBy = 'wordCount'">
            总字数
          </div>
          <div class="sort-nav-item" :class="{ active: sortBy === 'recommendCount' }" @click="sortBy = 'recommendCount'">
            总推荐
          </div>
          <div class="sort-nav-item" :class="{ active: sortBy === 'favoriteCount' }" @click="sortBy = 'favoriteCount'">
            总收藏
          </div>
          <div class="sort-nav-item" :class="{ active: sortBy === 'clickCount' }" @click="sortBy = 'clickCount'">
            总用户点击
          </div>
        </div>

        <!-- 搜索结果统计 -->
        <div class="results-count">
          共搜索到<span class="count-number">{{ searchResults.length }}</span>部与"<span class="search-keyword">{{ searchQuery }}</span>"相关结果
        </div>

        <div class="results-list" v-loading="loading">
          <div
            v-for="book in sortedResults"
            :key="book.id"
            class="book-item"
          >
            <div class="book-cover" @click="goToBookDetail(book.bookId)">
              <img :src="book.cover" :alt="book.title" />
            </div>
            <div class="book-info">
              <h3 class="book-title" @click="goToBookDetail(book.bookId)" v-html="shouldHighlight('title') ? highlightKeyword(book.title, searchQuery) : book.title"></h3>
              <div class="book-meta" @click="goToBookDetail(book.bookId)">
                <span class="book-author" v-html="shouldHighlight('author') ? highlightKeyword(book.author, searchQuery) : book.author"></span>
                <span class="separator">|</span>
                <span class="book-category" v-html="shouldHighlight('category') ? highlightKeyword(book.category, searchQuery) : book.category"></span>
                <span class="separator">|</span>
                <span class="book-status">{{ book.status === 'completed' ? '完本' : '连载' }}</span>
                <span class="separator">|</span>
                <span class="book-word-count">{{ formatNumber(book.wordCount) }}字</span>
              </div>
              <p class="book-desc" v-html="shouldHighlight('description') ? highlightKeyword(book.description, searchQuery) : book.description"></p>
              <div class="book-chapter-info">
                <span class="latest-chapter">最新章节：{{ book.latestChapter }}</span>
                <span class="update-time">更新时间：{{ formatDate(book.updateTime) }}</span>
              </div>
            </div>

            <div class="book-actions">
              <el-button type="danger" round @click.stop="goToReadPage(book.bookId)">立即阅读</el-button>
              <el-button
                v-if="!booksInShelf.has(book.bookId)"
                text
                class="add-to-shelf-btn"
                @click.stop="addToShelf(book.bookId)"
              >
                加入书架
              </el-button>
              <el-button
                v-else
                text
                class="in-shelf-btn"
                disabled
              >
                已在书架
              </el-button>
              <div class="sort-stat" v-if="sortBy === 'recommendCount' || sortBy === 'favoriteCount' || sortBy === 'clickCount'">
                {{ sortBy === 'recommendCount' ? formatNumber(book.recommendCount) + ' 总推荐' :
                   sortBy === 'favoriteCount' ? formatNumber(book.favoriteCount) + ' 总收藏' :
                   formatNumber(book.clickCount) + ' 总用户点击' }}
              </div>
            </div>
          </div>
        </div>

        <div class="pagination" v-if="searchResults.length > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="searchResults.length"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>

        <div class="no-results" v-if="searchResults.length === 0 && !loading">
          <el-empty description="没有找到相关书籍">
            <template #description>
              <div class="empty-content">
                <p class="empty-title">没有找到相关书籍</p>
                <p class="empty-tips">试试更换搜索词或调整搜索类型</p>

        <!-- 基本提示 -->
                <div v-if="emptySearchCount < 3 && !route.query.fromAI" class="search-suggestions">
                  <p class="suggestion-title">搜索建议：</p>
                  <ul class="suggestion-list">
                    <li>检查关键词拼写是否正确</li>
                    <li>尝试使用更简单的关键词</li>
                    <li>切换搜索类型（书名、作者、全部）</li>
                  </ul>
                </div>

                <!-- 多次搜索无果后的愿望单提示 -->
                <div v-if="emptySearchCount >= 3 || route.query.fromAI" class="book-request-prompt">
                  <p class="request-title">😔 多次搜索都没有找到您想要的书籍</p>
                  <p class="request-desc">您可以向我们申请添加这本书籍</p>
                  <el-button
                    v-if="authStore.isAuthenticated"
                    type="primary"
                    size="large"
                    @click="openBookRequestForm"
                    class="request-button">
                    填写书籍申请
                  </el-button>
                  <div v-else class="login-prompt">
                    <p class="login-text">请先登录后再申请添加书籍</p>
                    <el-button
                      type="primary"
                      size="large"
                      @click="$router.push('/login')"
                      class="login-button">
                      立即登录
                    </el-button>
                  </div>
                </div>
              </div>
            </template>
          </el-empty>
        </div>
      </div>
    </div>
  </div>

  <!-- 书籍申请表单对话框 -->
  <el-dialog
    v-model="showBookRequestForm"
    title="申请添加书籍"
    width="500px"
    :close-on-click-modal="false"
    @close="closeBookRequestForm">
    <el-form :model="bookRequestForm" label-width="80px">
      <el-form-item label="书名" required>
        <el-input
          v-model="bookRequestForm.bookTitle"
          placeholder="请输入书名"
          maxlength="100"
          show-word-limit />
      </el-form-item>

      <el-form-item label="作者" required>
        <el-input
          v-model="bookRequestForm.author"
          placeholder="请输入作者"
          maxlength="50"
          show-word-limit />
      </el-form-item>

      <el-form-item label="出版社">
        <el-input
          v-model="bookRequestForm.publisher"
          placeholder="请输入出版社（可选）"
          maxlength="100"
          show-word-limit />
      </el-form-item>

      <el-form-item label="ISBN">
        <el-input
          v-model="bookRequestForm.isbn"
          placeholder="请输入ISBN号（可选）"
          maxlength="20" />
      </el-form-item>

      <el-form-item label="书籍描述">
        <el-input
          v-model="bookRequestForm.description"
          type="textarea"
          :rows="4"
          placeholder="请输入书籍描述（可选）"
          maxlength="500"
          show-word-limit />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeBookRequestForm">取消</el-button>
        <el-button type="primary" @click="submitBookRequest">提交申请</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup name="SearchPage">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { booksApi } from '@/api/Books.js'
import { bookshelvesApi } from '@/api/Bookshelves.js'
import { bookRequestApi } from '@/api/BookRequest.js'
import { searchLogsApi } from '@/api/SearchLogs.js'
import { getImageUrl } from '@/utils/imageUtils'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 搜索相关状态
const searchQuery = ref('')
const searchType = ref('all')
const searchResults = ref([])

const hasSearched = ref(false)
const loading = ref(false)

// 分页和排序
const currentPage = ref(1)
const pageSize = ref(10)
const sortBy = ref('popularity')

// 所有书籍数据（从后端获取）
const allBooks = ref([])

// 书籍在书架中的状态
const booksInShelf = ref(new Set())

// 搜索统计相关
const emptySearchCount = ref(0) // 空结果搜索次数
const showBookRequestForm = ref(false) // 是否显示书籍申请表单
const bookRequestForm = ref({ // 书籍申请表单数据
  bookTitle: '',
  author: '',
  publisher: '',
  isbn: '',
  description: ''
})

// 高亮关键词
const highlightKeyword = (text, keyword) => {
  if (!keyword || !text) return text

  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

// 根据搜索类型判断是否需要高亮
const shouldHighlight = (field) => {
  if (searchType.value === 'all') return true
  if (searchType.value === 'title' && field === 'title') return true
  if (searchType.value === 'author' && field === 'author') return true
  return false
}

// 计算属性：排序后的结果
const sortedResults = computed(() => {
  const results = [...searchResults.value]

  switch (sortBy.value) {
    case 'popularity':
      // 人气排序：综合点击量、收藏量和推荐量
      return results.sort((a, b) => {
        const scoreA = a.clickCount + a.favoriteCount * 10 + (a.recommendCount || 0) * 5
        const scoreB = b.clickCount + b.favoriteCount * 10 + (b.recommendCount || 0) * 5
        return scoreB - scoreA
      })
    case 'updateTime':
      return results.sort((a, b) => new Date(b.lastChapterTime || b.updateTime) - new Date(a.lastChapterTime || a.updateTime))
    case 'wordCount':
      return results.sort((a, b) => (b.wordCount || 0) - (a.wordCount || 0))
    case 'recommendCount':
      return results.sort((a, b) => (b.recommendCount || 0) - (a.recommendCount || 0))
    case 'favoriteCount':
      return results.sort((a, b) => b.favoriteCount - a.favoriteCount)
    case 'clickCount':
      return results.sort((a, b) => b.clickCount - a.clickCount)
    default:
      return results
  }
})

// 获取所有书籍数据
const fetchAllBooks = async () => {
  try {
    const response = await booksApi.getAllBooks()

    if (response.code === 200 && response.data && Array.isArray(response.data)) {
      // 映射字段格式，参照Library.vue的处理方式
      allBooks.value = response.data.map(book => ({
        bookId: book.bookId || book.id || 0,
        title: book.title || '',
        author: book.author || '',
        category: book.categoryName || book.category || '',
        description: book.description || '',
        cover: getImageUrl(book.cover),
        status: book.statusName === '已完结' ? 'completed' : (book.statusName === '连载中' ? 'ongoing' : 'ongoing'),
        clickCount: book.clickCount || book.viewCount || 0,
        favoriteCount: book.likeCount || book.favoriteCount || 0,
        recommendCount: book.recommendCount || 0,
        wordCount: book.wordCount || 0,
        updateTime: book.lastChapterTime || book.updateTime || new Date(),
        tags: book.tags || [],
        latestChapter: book.lastChapterTitle || book.latestChapter || '',
        // 兼容性字段
        id: book.bookId || book.id || 0,
        categoryName: book.categoryName || book.category || '',
        statusName: book.statusName || '',
        lastChapterTime: book.lastChapterTime ? new Date(book.lastChapterTime) : new Date()
      }))
    } else if (response.code === 200 && response.data && response.data.books && Array.isArray(response.data.books)) {
      // 如果返回的是对象包含books数组
      allBooks.value = response.data.books.map(book => ({
        bookId: book.bookId || book.id || 0,
        title: book.title || '',
        author: book.author || '',
        category: book.categoryName || book.category || '',
        description: book.description || '',
        cover: getImageUrl(book.cover),
        status: book.statusName === '已完结' ? 'completed' : (book.statusName === '连载中' ? 'ongoing' : 'ongoing'),
        clickCount: book.clickCount || book.viewCount || 0,
        favoriteCount: book.likeCount || book.favoriteCount || 0,
        recommendCount: book.recommendCount || 0,
        wordCount: book.wordCount || 0,
        updateTime: book.lastChapterTime || book.updateTime || new Date(),
        tags: book.tags || [],
        latestChapter: book.lastChapterTitle || book.latestChapter || '',
        // 兼容性字段
        id: book.bookId || book.id || 0,
        categoryName: book.categoryName || book.category || '',
        statusName: book.statusName || '',
        lastChapterTime: book.lastChapterTime ? new Date(book.lastChapterTime) : new Date()
      }))
    } else {
      allBooks.value = []
      ElMessage.warning('获取书籍数据格式异常')
    }
  } catch (error) {
    console.error('获取书籍数据失败:', error)
    ElMessage.error('获取书籍数据失败，请稍后重试')
    allBooks.value = []
  }
}

// 处理搜索
const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  hasSearched.value = true
  currentPage.value = 1

  try {
    // 如果还没有获取书籍数据，先获取
    if (allBooks.value.length === 0) {
      await fetchAllBooks()
    }

    // 搜索逻辑
    const query = searchQuery.value.toLowerCase()
    let results = []

    if (searchType.value === 'all' || searchType.value === 'title') {
      results = results.concat(
        allBooks.value.filter(book =>
          book.title.toLowerCase().includes(query)
        )
      )
    }

    if (searchType.value === 'all' || searchType.value === 'author') {
      results = results.concat(
        allBooks.value.filter(book =>
          book.author.toLowerCase().includes(query)
        )
      )
    }

    // 全部搜索时也搜索描述和标签
    if (searchType.value === 'all') {
      results = results.concat(
        allBooks.value.filter(book =>
          book.description.toLowerCase().includes(query) ||
          (book.tags && book.tags.some(tag => tag.toLowerCase().includes(query)))
        )
      )
    }

    // 去重
    const uniqueResults = []
    const ids = new Set()

    for (const book of results) {
      if (!ids.has(book.bookId)) {
        ids.add(book.bookId)
        uniqueResults.push(book)
      }
    }

    searchResults.value = uniqueResults

    // 统计空结果搜索次数
    if (uniqueResults.length === 0) {
      emptySearchCount.value++
      localStorage.setItem('emptySearchCount', emptySearchCount.value.toString())
    } else {
      // 如果有结果，重置空搜索计数
      emptySearchCount.value = 0
      localStorage.setItem('emptySearchCount', '0')
    }

    // 如果用户已登录，检查搜索结果中的书籍是否已在书架中
    if (authStore.isAuthenticated && uniqueResults.length > 0) {
      const bookIds = uniqueResults.map(book => book.bookId)
      checkBooksInShelf(bookIds)
    }



    // 发送搜索日志
    const searchLogData = {
      keyword: searchQuery.value.trim(),
      searchType: mapSearchType(searchType.value),
      resultCount: uniqueResults.length
    }

    // 只有当用户已登录且有有效userId时才添加userId字段
    if (authStore.isAuthenticated && authStore.user?.id) {
      searchLogData.userId = authStore.user.id
    }

    try {
      await searchLogsApi.addSearchLog(searchLogData)
    } catch (error) {
      console.error('添加搜索日志失败:', error)
      // 搜索日志发送失败不影响搜索结果展示
    }

    // 更新URL参数
    const queryParams = {
      q: searchQuery.value.trim()
    }

    // 如果搜索类型不是默认的'all'，添加到URL参数
    if (searchType.value !== 'all') {
      queryParams.type = searchType.value
    }

    // 使用replace避免添加历史记录
    router.replace({
      path: '/search',
      query: queryParams
    })

  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
    searchResults.value = []
  } finally {
    loading.value = false
  }
}



// 分页处理
const handlePageChange = (page) => {
  currentPage.value = page
  // 滚动到顶部
  window.scrollTo(0, 0)
}

// 搜索类型映射函数，将字符串转换为数字
const mapSearchType = (type) => {
  switch (type) {
    case 'title':
      return 1 // 书籍
    case 'author':
      return 2 // 作者
    case 'all':
    default:
      return 3 // 综合
  }
}

// 格式化数字
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
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
    // 获取年、月、日、时、分、秒
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')

    // 格式化为YYYY-MM-DD HH:mm:ss格式
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  }
}

// 跳转到阅读页面
const goToReadPage = (bookId) => {
  // 在新标签页打开阅读页面
  window.open(`/read/${bookId}/1`, '_blank')
}

// 检查书籍是否在书架中
const checkBookInShelf = async (bookId) => {
  if (!authStore.isAuthenticated) {
    return false
  }

  try {
    const userId = authStore.user?.id
    if (!userId) return false

    const response = await bookshelvesApi.existInBookshelf(bookId, userId)
    return response.data
  } catch (error) {
    console.error('检查书籍是否在书架中失败:', error)
    return false
  }
}

// 批量检查书籍是否在书架中
const checkBooksInShelf = async (bookIds) => {
  if (!authStore.isAuthenticated || bookIds.length === 0) {
    return
  }

  try {
    const userId = authStore.user?.id
    if (!userId) return

    // 使用 Promise.all 并行检查，提高效率
    const checkPromises = bookIds.map(async (bookId) => {
      try {
        const exists = await checkBookInShelf(bookId)
        return { bookId, exists }
      } catch (error) {
        console.error(`检查书籍 ${bookId} 状态失败:`, error)
        return { bookId, exists: false }
      }
    })

    const results = await Promise.all(checkPromises)

    // 更新状态 - 创建新的Set以触发响应式更新
    const newSet = new Set(booksInShelf.value)
    results.forEach(({ bookId, exists }) => {
      if (exists) {
        newSet.add(bookId)
      }
    })
    booksInShelf.value = newSet
  } catch (error) {
    console.error('批量检查书籍状态失败:', error)
  }
}

// 跳转到书籍详情页
const goToBookDetail = (bookId) => {
  // 使用Vue Router跳转到书籍详情页
  router.push(`/book/${bookId}`)
}

// 添加到书架
const addToShelf = async (bookId) => {
  // 检查用户是否登录
  if (!authStore.isAuthenticated) {
    // 显示确认对话框
    try {
      await ElMessageBox.confirm(
        '您还未登录，是否前往登录页面？',
        '提示',
        {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )
      // 用户点击确认，跳转到登录页面
      router.push('/login')
    } catch {
      // 用户点击取消，不做任何操作
    }
    return
  }

  try {
    // 获取当前用户ID
    const userId = authStore.user?.id
    if (!userId) {
      ElMessage.error('获取用户信息失败')
      return
    }

    // 调用API添加书籍到书架
    const response = await bookshelvesApi.addBookToShelves(bookId, userId)

    if (response.code === 200) {
      ElMessage.success('已加入书架')
      // 更新状态 - 创建新的Set以触发响应式更新
      const newSet = new Set(booksInShelf.value)
      newSet.add(bookId)
      booksInShelf.value = newSet
      console.log(`书籍 ${bookId} 已成功添加到书架`)
    } else {
      ElMessage.warning(response.message || '添加失败，请稍后重试')
    }
  } catch (error) {
    console.error('添加书籍到书架失败:', error)

    // 根据错误类型显示不同的提示
    ElMessage.error(error.response?.data?.message || '添加失败，请稍后重试')
  }
}

// 打开书籍申请表单
const openBookRequestForm = () => {
  // 预填充当前搜索关键词到书名
  bookRequestForm.value.bookTitle = searchQuery.value
  showBookRequestForm.value = true
}

// 关闭书籍申请表单
const closeBookRequestForm = () => {
  showBookRequestForm.value = false
  // 重置表单
  bookRequestForm.value = {
    bookTitle: '',
    author: '',
    publisher: '',
    isbn: '',
    description: ''
  }
}

// 提交书籍申请
const submitBookRequest = async () => {
  // 表单验证
  if (!bookRequestForm.value.bookTitle.trim()) {
    ElMessage.warning('请输入书名')
    return
  }
  if (!bookRequestForm.value.author.trim()) {
    ElMessage.warning('请输入作者')
    return
  }

  try {

    // 提交申请
    const bookRequestData = {
        userId: authStore.user.id,
        requestSource: authStore.user.roleName,
        bookTitle: bookRequestForm.value.bookTitle,
        author: bookRequestForm.value.author,
        publisher: bookRequestForm.value.publisher,
        isbn: bookRequestForm.value.isbn,
        description: bookRequestForm.value.description
    }
    const response = await bookRequestApi.addBookRequest(bookRequestData)

    if (response.code === 200) {
      ElMessage.success(response.message || '书籍申请已提交，我们会尽快处理！')
      closeBookRequestForm()
      // 重置空搜索计数
      emptySearchCount.value = 0
      localStorage.setItem('emptySearchCount', '0')
    } else {
      ElMessage.warning(response.message || '提交失败，请稍后重试')
    }
  } catch (error) {
    console.error('提交书籍申请失败:', error)
    ElMessage.error('提交失败，请稍后重试')
  }
}

// 组件挂载时加载搜索历史和书籍数据
onMounted(async () => {
  // 进入搜索页面时滚动到顶部
  window.scrollTo(0, 0);



  // 加载空搜索计数
  const savedCount = localStorage.getItem('emptySearchCount')
  if (savedCount) {
    emptySearchCount.value = parseInt(savedCount) || 0
  }

  // 检查URL中是否有搜索参数
  const query = route.query.q
  const type = route.query.type

  if (query) {
    searchQuery.value = query

    // 设置搜索类型
    if (type === 'author') {
      searchType.value = 'author'
    } else if (type === 'title') {
      searchType.value = 'title'
    } else {
      searchType.value = 'all'
    }

    // 如果有搜索参数，直接进行搜索，fetchAllBooks会在搜索时调用
    handleSearch()
  } else {
    // 只有在没有搜索参数时才预加载数据（用于浏览全部书籍）
    // 使用延迟加载，避免阻塞页面渲染
    setTimeout(async () => {
      try {
        await fetchAllBooks()

        // 如果用户已登录，检查所有书籍是否在书架中
        if (authStore.isAuthenticated && allBooks.value.length > 0) {
          const bookIds = allBooks.value.map(book => book.bookId)
          checkBooksInShelf(bookIds)
        }
      } catch (error) {
        console.error('预加载书籍数据失败:', error)
      }
    }, 100) // 延迟100ms执行，让页面先渲染出来
  }
})

// 监听路由查询参数变化
watch(() => route.query.q, (newQuery) => {
  if (newQuery && newQuery !== searchQuery.value) {
    searchQuery.value = newQuery
    handleSearch()
  }
})
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 80px 0 20px; /* 添加顶部内边距，确保内容从导航栏下方开始 */
}

.search-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.search-header {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.search-box {
  margin-bottom: 20px;
}

.search-input {
  font-size: 16px;
}

.search-filters {
  display: flex;
  justify-content: center;
}

.search-history {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.history-tag {
  cursor: pointer;
}

.search-results {
  padding: 0 20px;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 骨架屏样式 */
.skeleton-loading {
  padding: 20px;
}

.skeleton-item {
  display: flex;
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.skeleton-cover {
  width: 120px;
  height: 160px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
  margin-right: 20px;
  flex-shrink: 0;
}

.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.skeleton-title {
  height: 24px;
  width: 60%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-meta {
  height: 16px;
  width: 40%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-desc {
  height: 16px;
  width: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 8px;
  flex: 1;
}

.skeleton-actions {
  height: 32px;
  width: 30%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 4px;
  margin-top: 12px;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 排序导航栏 */
.sort-nav {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 15px;
}

.sort-nav-item {
  padding: 10px 16px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  transition: all 0.3s;
}

.sort-nav-item:hover {
  color: #409eff;
}

.sort-nav-item.active {
  color: #409eff;
  border-bottom-color: #409eff;
  font-weight: 500;
}

/* 搜索结果统计 */
.results-count {
  font-size: 12px;
  color: #606266;
  margin-bottom: 15px;
}

.count-number {
  color: #f56c6c;
  font-weight: bold;
}

.search-keyword {
  color: #f56c6c;
  font-weight: 500;
}

:deep(.highlight) {
  color: #f56c6c;
  font-weight: bold;
  background-color: rgba(245, 108, 108, 0.1);
  padding: 0 2px;
  border-radius: 2px;
}

.results-list {
  margin-bottom: 20px;
}

.book-item {
  display: flex;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.book-item:hover {
  background-color: #f9f9f9;
}

.book-item:last-child {
  border-bottom: none;
}

.book-cover {
  position: relative;
  width: 120px;
  height: 160px;
  margin-right: 20px;
  flex-shrink: 0;
  cursor: pointer;
  transition: transform 0.3s;
}

.book-cover:hover {
  transform: scale(1.05);
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.book-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
}

.book-title:hover {
  color: #409eff;
}

.book-meta {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  cursor: pointer;
}

.separator {
  margin: 0 8px;
  color: #ddd;
}

.book-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px;
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

.book-chapter-info {
  display: flex;
  justify-content: flex-start;
  font-size: 13px;
  color: #999;
  margin-top: auto;
  gap: 20px;
}

.latest-chapter {
  color: #409eff;
  font-weight: 500;
  cursor: pointer;
}

.update-time {
  color: #999;
}

.book-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-left: 20px;
  width: 120px;
}

.book-actions .el-button {
  transition: none;
}

.book-actions .el-button:hover {
  transform: none;
  box-shadow: none;
}

.add-to-shelf-btn {
  margin-top: 10px;
  font-size: 13px;
  color: #909399;
  text-align: center;
  width: 100%;
  margin-left: 0;
  margin-right: 0;
}

.in-shelf-btn {
  margin-top: 10px;
  font-size: 13px;
  color: #67c23a;
  text-align: center;
  width: 100%;
  margin-left: 0;
  margin-right: 0;
}

.sort-stat {
  margin-top: 15px;
  font-size: 13px;
  color: #f56c6c;
  font-weight: 500;
  text-align: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.no-results {
  padding: 40px 0;
  text-align: center;
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

  .book-cover {
    width: 110px;
    height: 150px;
    margin-right: 15px;
  }

  .book-title {
    font-size: 17px;
  }

  .book-author, .book-category {
    font-size: 13px;
  }

  .book-desc {
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .container {
    max-width: 540px;
    padding: 0 15px;
  }

  .search-page {
    padding: 15px 0;
  }

  .search-container {
    padding: 0;
  }

  .search-header {
    padding: 15px;
  }

  .search-box {
    margin-bottom: 15px;
  }

  .search-input {
    font-size: 15px;
  }

  .search-filters {
    overflow-x: auto;
    justify-content: flex-start;
    padding-bottom: 5px;
  }

  .search-history {
    padding: 15px;
    margin-bottom: 15px;
  }

  .history-header {
    margin-bottom: 12px;
  }

  .search-results {
    padding: 15px;
  }

  .results-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
    padding-bottom: 12px;
  }

  .sort-options {
    width: 100%;
    justify-content: space-between;
  }

  .book-item {
    flex-direction: column;
    padding: 15px 0;
  }

  .book-cover {
    width: 100px;
    height: 130px;
    margin: 0 auto 15px;
  }

  .book-info {
    text-align: center;
  }

  .book-title {
    font-size: 16px;
    margin-bottom: 8px;
  }

  .book-author, .book-category {
    font-size: 13px;
    margin: 3px 0;
  }

  .book-desc {
    font-size: 13px;
    margin: 8px 0;
  }

  .book-stats {
    justify-content: center;
    font-size: 12px;
  }
}

@media (max-width: 576px) {
  .container {
    max-width: 100%;
    padding: 0 12px;
  }

  .search-page {
    padding: 12px 0;
  }

  .search-header {
    padding: 12px;
  }

  .search-box {
    margin-bottom: 12px;
  }

  .search-input {
    font-size: 14px;
  }

  .search-history {
    padding: 12px;
    margin-bottom: 12px;
  }

  .history-header {
    margin-bottom: 10px;
  }

  .history-tags {
    gap: 8px;
  }

  .search-results {
    padding: 12px;
  }

  .results-header {
    padding-bottom: 10px;
  }

  .book-item {
    padding: 12px 0;
  }

  .book-cover {
    width: 90px;
    height: 120px;
    margin-bottom: 12px;
  }

  .book-title {
    font-size: 15px;
    margin-bottom: 6px;
  }

  .book-author, .book-category {
    font-size: 12px;
    margin: 2px 0;
  }

  .book-desc {
    font-size: 12px;
    margin: 6px 0;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    max-height: 3.6em; /* 3行 × 1.2行高 (12px × 1.2) */
  }

  .book-stats {
    font-size: 11px;
    gap: 10px;
  }

  .pagination {
    margin-top: 15px;
  }

  .no-results {
  padding: 30px 0;
}

/* 空结果提示样式 */
.empty-content {
  text-align: center;
}

.empty-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 8px;
  font-weight: 500;
}

.empty-tips {
  font-size: 14px;
  color: #909399;
  margin-bottom: 20px;
}

.search-suggestions {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  margin: 20px auto;
  max-width: 400px;
  text-align: left;
}

.suggestion-title {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  margin-bottom: 10px;
}

.suggestion-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.suggestion-list li {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
  padding-left: 16px;
  position: relative;
}

.suggestion-list li:before {
  content: "•";
  position: absolute;
  left: 0;
  color: #c0c4cc;
}

.suggestion-list li:last-child {
  margin-bottom: 0;
}

.book-request-prompt {
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  border-radius: 12px;
  padding: 25px;
  margin: 20px auto;
  max-width: 400px;
  color: white;
  text-align: center;
}

.request-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  line-height: 1.4;
}

.request-desc {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 20px;
  line-height: 1.5;
}

.request-button {
  font-size: 14px;
  padding: 12px 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.request-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 登录提示区域 */
.login-prompt {
  margin-top: 15px;
  padding: 15px;
  background: linear-gradient(135deg, #fff7e6 0%, #fff0d9 100%);
  border-radius: 6px;
  border: 1px solid #ffd591;
}

.login-text {
  font-size: 14px;
  color: #d46b08;
  margin-bottom: 10px;
  font-weight: 500;
}

.login-button {
  background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
  border: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(250, 140, 22, 0.4);
}
}
</style>
