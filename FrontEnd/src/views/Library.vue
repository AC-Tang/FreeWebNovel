<template>
  <PageTransition>
    <div class="library-page">
      <div class="container">
        <div class="library-header">
        </div>

        <div class="library-content">
          <!-- 筛选区域 -->
          <div class="filter-section">
            <el-card>
              <div class="filter-row">
                <div class="filter-item">
                  <div class="filter-options">
                    <span class="filter-label">作品分类：</span>
                    <el-radio-group v-model="filters.category" @change="handleFilterChange">
                      <el-radio label="all">全部</el-radio>
                      <el-radio
                        v-for="category in categories"
                        :key="category.value"
                        :label="category.value"
                      >
                        {{ category.label }}
                      </el-radio>
                    </el-radio-group>
                  </div>
                </div>

                <div class="filter-item">
                  <div class="filter-options">
                    <span class="filter-label">作品字数：</span>
                    <el-radio-group v-model="filters.wordCount" @change="handleFilterChange">
                      <el-radio label="all">全部</el-radio>
                      <el-radio label="0-30">30万字以下</el-radio>
                      <el-radio label="30-50">30-50万字</el-radio>
                      <el-radio label="50-100">50-100万字</el-radio>
                      <el-radio label="100-200">100-200万字</el-radio>
                      <el-radio label="200+">200万字以上</el-radio>
                    </el-radio-group>
                  </div>
                </div>

                <div class="filter-item">
                  <div class="filter-options">
                    <span class="filter-label">更新时间：</span>
                    <el-radio-group v-model="filters.updateTime" @change="handleFilterChange">
                      <el-radio label="all">全部</el-radio>
                      <el-radio label="three-days">三日内</el-radio>
                      <el-radio label="week">一周内</el-radio>
                      <el-radio label="month">一月内</el-radio>
                      <el-radio label="three-months">三月内</el-radio>
                      <el-radio label="half-year">半年内</el-radio>
                    </el-radio-group>
                  </div>
                </div>

                <div class="filter-item">
                  <div class="filter-options">
                    <span class="filter-label">是否完结：</span>
                    <el-radio-group v-model="filters.status" @change="handleFilterChange">
                      <el-radio label="all">全部</el-radio>
                      <el-radio label="serializing">连载中</el-radio>
                      <el-radio label="completed">已完结</el-radio>
                    </el-radio-group>
                  </div>
                </div>
              </div>
            </el-card>

            <div class="filter-actions">
              <el-button @click="resetFilters">重置</el-button>
            </div>
          </div>

        <!-- 书籍列表 -->
        <div class="books-section">
          <div class="section-header">
            <div class="sort-nav">
              <el-radio-group v-model="sortBy" @change="handleSortChange" class="sort-radio-group">
                <el-radio-button label="default">人气排序</el-radio-button>
                <el-radio-button label="updateTime">最新更新</el-radio-button>
                <el-radio-button label="wordCount">字数最多</el-radio-button>
                <el-radio-button label="clickCount">总点击</el-radio-button>
                <el-radio-button label="recommendCount">总推荐</el-radio-button>
                <el-radio-button label="favoriteCount">总收藏</el-radio-button>
              </el-radio-group>
            </div>
            <div class="view-toggle">
              <el-radio-group v-model="viewMode" @change="handleViewModeChange">
                <el-radio-button label="grid">
                  <el-icon><Grid /></el-icon>
                  图标模式
                </el-radio-button>
                <el-radio-button label="list">
                  <el-icon><List /></el-icon>
                  列表模式
                </el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <!-- 网格模式 -->
          <div v-if="viewMode === 'grid'" class="books-grid">
            <div v-if="loading">
              <SkeletonLoader :count="12" :show-avatar="false" :show-title="true" :text-lines="3" />
            </div>
            <div
              v-else
              v-for="book in paginatedBooks"
              :key="book.bookId"
              class="book-card"
            >
              <div class="book-cover clickable" @click="goToBook(book.bookId)">
                <img :src="book.cover" :alt="book.title">
              </div>
              <div class="book-info">
                <h3 class="book-title clickable" @click="goToBook(book.bookId)">{{ book.title }}</h3>
                <p class="book-meta">
                  <span class="book-author clickable" @click="searchByAuthor(book.author)">{{ book.author }}</span>·
                  <span class="book-category clickable" @click="filterByCategory(book.categoryName)">{{ book.categoryName }}</span>·
                  <span class="book-status-text clickable" @click="filterByStatus(book.statusName)">{{ book.statusName }}</span>·
                  <span class="book-word-count">{{ formatWordCount(book.wordCount) }}</span>
                </p>
                <p class="book-description clickable" @click="goToBook(book.bookId)">{{ book.description }}</p>
                <p class="book-latest">
                  <span class="chapter-name clickable" @click="goToReadPage(book.bookId, book.lastChapterTitle)">{{ book.lastChapterTitle }}</span>
                  <span class="update-time">
                    {{ getSortDisplayText(book) }}
                  </span>
                </p>
              </div>
            </div>
          </div>

          <!-- 列表模式 -->
          <div v-else class="books-list">
            <div v-if="loading">
              <SkeletonLoader :count="12" :show-avatar="false" :show-title="true" :text-lines="2" />
            </div>
            <el-table v-else :data="paginatedBooks" style="width: 100%" @row-click="goToBook" row-class-name="book-row">
              <el-table-column type="index" width="50" />
              <el-table-column prop="categoryName" label="分类" width="80">
                <template #default="scope">
                  <span class="clickable" @click.stop="filterByCategory(scope.row.categoryName)">{{ scope.row.categoryName }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="书名" min-width="200">
                <template #default="scope">
                  <span class="book-title-link clickable" @click.stop="goToBook(scope.row.bookId)">{{ scope.row.title }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="lastChapterTitle" label="最新章节" min-width="200">
                <template #default="scope">
                  <span class="clickable" @click.stop="goToReadPage(scope.row.bookId, scope.row.lastChapterTitle)">{{ scope.row.lastChapterTitle }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="author" label="作者" width="120">
                <template #default="scope">
                  <span class="clickable" @click.stop="searchByAuthor(scope.row.author)">{{ scope.row.author }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="wordCount" label="字数" width="100">
                <template #default="scope">
                  {{ formatWordCount(scope.row.wordCount) }}
                </template>
              </el-table-column>
              <el-table-column prop="statusName" label="状态" width="80">
                <template #default="scope">
                  <span class="clickable" @click.stop="filterByStatus(scope.row.statusName)">
                    <el-tag :type="scope.row.statusName === '已完结' ? 'success' : 'warning'">
                      {{ scope.row.statusName }}
                    </el-tag>
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="lastChapterTime" label="更新时间" width="120">
                <template #default="scope">
                  {{ formatTime(scope.row.lastChapterTime) }}
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[12, 24, 36, 48]"
              :total="filteredBooks.length"
              :disabled="loading"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
        </div>
      </div>
    </div>
  </PageTransition>
</template>

<script setup name="LibraryPage">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Grid, List } from '@element-plus/icons-vue'
import PageTransition from '../components/PageTransition.vue'
import SkeletonLoader from '../components/SkeletonLoader.vue'
import { booksApi } from '@/api/Books.js'
import { categoryApi } from '@/api/Category.js'
import { ElMessage } from 'element-plus'
import { getImageUrl } from '@/utils/imageUtils'

const router = useRouter()
const route = useRoute()

// 视图模式
const viewMode = ref('grid')

// 筛选条件
const filters = reactive({
  category: 'all',
  wordCount: 'all',
  updateTime: 'all',
  status: 'all'
})

// 排序方式
const sortBy = ref('default')

// 分页
const currentPage = ref(1)
const pageSize = ref(12)

// 书籍数据
const books = ref([])

// 分类数据
const categories = ref([])

// 加载状态
const loading = ref(true)

// 获取分类数据
const fetchCategories = async () => {
  try {
    const response = await categoryApi.getCategoryList()
    if (response && response.data) {
      if (Array.isArray(response.data)) {
        categories.value = response.data.map(category => ({
          value: category.id || category.code || category.value,
          label: category.name || category.label
        }))
      } else {
        categories.value = []
        console.warn('分类数据格式异常')
      }
    }
  } catch (error) {
    console.error('获取分类数据失败:', error)
    // 如果获取失败，使用默认分类
    categories.value = [
      { value: 'xuanhuan', label: '玄幻' },
      { value: 'xianxia', label: '仙侠' },
      { value: 'dushi', label: '都市' },
      { value: 'lishi', label: '历史' },
      { value: 'kehuan', label: '科幻' },
      { value: 'yanqing', label: '言情' },
      { value: 'wangyou', label: '网游' },
      { value: 'xuanyi', label: '悬疑' }
    ]
  }
}

// 获取书籍数据
const fetchBooks = async () => {
  loading.value = true

  try {
    const response = await booksApi.getAllBooks()

    // 处理API响应数据格式
    if (response && response.data) {
      // 如果返回的是数组直接赋值
      if (Array.isArray(response.data)) {
        books.value = response.data.map(book => ({
          ...book,
          // 确保字段格式正确
          bookId: book.bookId || book.id || 0,
          title: book.title || '',
          author: book.author || '',
          categoryId: book.categoryId || 0,
          cover: getImageUrl(book.cover),
          description: book.description || '',
          wordCount: book.wordCount || 0,
          chapterCount: book.chapterCount || 0,
          viewCount: book.viewCount || 0,
          clickCount: book.clickCount || 0,
          recommendCount: book.recommendCount || 0,
          ratingAvg: book.ratingAvg || 0,
          ratingCount: book.ratingCount || 0,
          lastChapterTitle: book.lastChapterTitle || '',
          lastChapterTime: book.lastChapterTime ? new Date(book.lastChapterTime) : new Date(),
          categoryName: book.categoryName || '',
          statusName: book.statusName || '',
          likeCount: book.likeCount || 0,
          // 转换时间格式（兼容性处理）
          updateTime: book.updateTime || book.lastChapterTime || book.updatedAt || new Date()
        }))
      } else if (response.data.books && Array.isArray(response.data.books)) {
        // 如果返回的是对象包含books数组
        books.value = response.data.books.map(book => ({
          ...book,
          bookId: book.bookId || book.id || 0,
          title: book.title || '',
          subtitle: book.subtitle || null,
          author: book.author || '',
          categoryId: book.categoryId || 0,
          cover: getImageUrl(book.cover),
          description: book.description || '',
          status: book.status || 0,
          wordCount: book.wordCount || 0,
          chapterCount: book.chapterCount || 0,
          viewCount: book.viewCount || 0,
          clickCount: book.clickCount || 0,
          recommendCount: book.recommendCount || 0,
          ratingAvg: book.ratingAvg || 0,
          ratingCount: book.ratingCount || 0,
          lastChapterTitle: book.lastChapterTitle || '',
          lastChapterTime: book.lastChapterTime ? new Date(book.lastChapterTime) : new Date(),
          publishDate: book.publishDate ? new Date(book.publishDate) : new Date(),
          createdAt: book.createdAt ? new Date(book.createdAt) : new Date(),
          updatedAt: book.updatedAt ? new Date(book.updatedAt) : new Date(),
          recommended: book.recommended || 0,
          carousel: book.carousel || '',
          categoryName: book.categoryName || '',
          statusName: book.statusName || '',
          likeCount: book.likeCount || 0,
          updateTime: book.updateTime || book.lastChapterTime || book.updatedAt || new Date()
        }))
      } else {
        // 其他情况，使用空数组
        books.value = []
        ElMessage.warning('获取书籍数据格式异常')
      }
    } else {
      books.value = []
      ElMessage.warning('未获取到书籍数据')
    }
  } catch (error) {
    console.error('获取书籍数据失败:', error)
    ElMessage.error('获取书籍数据失败，请稍后重试')
    books.value = []
  } finally {
    loading.value = false
  }
}

// 根据筛选条件过滤书籍
const filteredBooks = computed(() => {
  let result = [...books.value]

  // 分类筛选
  if (filters.category !== 'all') {
    const categoryMap = categories.value.reduce((map, category) => {
      map[category.value] = category.label
      return map
    }, {})
    const categoryLabel = categoryMap[filters.category]
    if (categoryLabel) {
      result = result.filter(book => book.categoryName === categoryLabel)
    }
  }

  // 字数筛选（按万字单位筛选）
  if (filters.wordCount !== 'all') {
    if (filters.wordCount === '0-30') {
      result = result.filter(book => book.wordCount < 300000) // 30万字
    } else if (filters.wordCount === '30-50') {
      result = result.filter(book => book.wordCount >= 300000 && book.wordCount < 500000) // 30-50万字
    } else if (filters.wordCount === '50-100') {
      result = result.filter(book => book.wordCount >= 500000 && book.wordCount < 1000000) // 50-100万字
    } else if (filters.wordCount === '100-200') {
      result = result.filter(book => book.wordCount >= 1000000 && book.wordCount < 2000000) // 100-200万字
    } else if (filters.wordCount === '200+') {
      result = result.filter(book => book.wordCount >= 2000000) // 200万字以上
    }
  }

  // 更新时间筛选（根据最后章节更新时间）
  if (filters.updateTime !== 'all') {
    const now = new Date()
    let daysThreshold = 0

    if (filters.updateTime === 'three-days') {
      daysThreshold = 3
    } else if (filters.updateTime === 'week') {
      daysThreshold = 7
    } else if (filters.updateTime === 'month') {
      daysThreshold = 30
    } else if (filters.updateTime === 'three-months') {
      daysThreshold = 90
    } else if (filters.updateTime === 'half-year') {
      daysThreshold = 180
    }

    if (daysThreshold > 0) {
      const thresholdDate = new Date(now - daysThreshold * 24 * 60 * 60 * 1000)
      result = result.filter(book => book.lastChapterTime >= thresholdDate)
    }
  }

  // 状态筛选（根据statusName字段）
  if (filters.status !== 'all') {
    if (filters.status === 'completed') {
      result = result.filter(book => book.statusName === '已完结')
    } else if (filters.status === 'serializing') {
      result = result.filter(book => book.statusName === '连载中')
    }
  }

  // 排序
      if (sortBy.value !== 'default') {
        result.sort((a, b) => {
          if (sortBy.value === 'updateTime') {
            return b.lastChapterTime - a.lastChapterTime
          } else if (sortBy.value === 'clickCount') {
            return b.clickCount - a.clickCount
          } else if (sortBy.value === 'recommendCount') {
            return b.recommendCount - a.recommendCount
          } else if (sortBy.value === 'favoriteCount') {
            return (b.likeCount || 0) - (a.likeCount || 0)
          } else if (sortBy.value === 'wordCount') {
            return b.wordCount - a.wordCount
          }
          return 0
        })
      }

  return result
})

// 分页后的书籍
const paginatedBooks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredBooks.value.slice(start, end)
})

// 处理视图模式切换
const handleViewModeChange = () => {
  // 切换视图模式时重置页码
  currentPage.value = 1
}

// 处理筛选条件变化
const handleFilterChange = () => {
  loading.value = true

  // 构建查询参数
  const query = {}

  // 只添加非默认值的参数
  if (filters.category !== 'all') {
    query.category = filters.category
  }

  if (filters.wordCount !== 'all') {
    query.wordCount = filters.wordCount
  }

  if (filters.updateTime !== 'all') {
    query.updateTime = filters.updateTime
  }

  if (filters.status !== 'all') {
    // 将内部状态值转换为中文显示值
    if (filters.status === 'completed') {
      query.status = '已完结'
    } else if (filters.status === 'serializing') {
      query.status = '连载中'
    } else {
      query.status = filters.status
    }
  }

  if (sortBy.value !== 'default') {
    query.sortBy = sortBy.value
  }

  // 筛选条件变化时重置页码
  currentPage.value = 1
  loading.value = false
}

// 处理排序变化
const handleSortChange = () => {
  loading.value = true

  // 构建查询参数
  const query = {}

  // 只添加非默认值的参数
  if (filters.category !== 'all') {
    query.category = filters.category
  }

  if (filters.wordCount !== 'all') {
    query.wordCount = filters.wordCount
  }

  if (filters.updateTime !== 'all') {
    query.updateTime = filters.updateTime
  }

  if (filters.status !== 'all') {
    // 将内部状态值转换为中文显示值
    if (filters.status === 'completed') {
      query.status = '已完结'
    } else if (filters.status === 'serializing') {
      query.status = '连载中'
    } else {
      query.status = filters.status
    }
  }

  if (sortBy.value !== 'default') {
    query.sortBy = sortBy.value
  }

  // 更新URL参数
  router.push({
    path: '/library',
    query
  })

  // 排序变化时重置页码
  currentPage.value = 1
  loading.value = false
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

// 处理当前页变化
const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 重置筛选条件
const resetFilters = () => {
  filters.category = 'all'
  filters.wordCount = 'all'
  filters.updateTime = 'all'
  filters.status = 'all'
  sortBy.value = 'default'
  currentPage.value = 1

  // 清除URL参数
  router.push('/library')
}

// 根据排序选项获取显示文本
const getSortDisplayText = (book) => {
  if (sortBy.value === 'clickCount') {
    return `总点击：${book.clickCount}`
  } else if (sortBy.value === 'recommendCount') {
    return `总推荐：${book.recommendCount}`
  } else if (sortBy.value === 'favoriteCount') {
    return `总收藏：${book.likeCount || 0}`
  } else {
    return formatTime(book.lastChapterTime)
  }
}

// 格式化字数（转换为万字单位）
const formatWordCount = (wordCount) => {
  if (!wordCount || wordCount <= 0) return '0字'
  
  // 如果小于1万字，显示具体字数
  if (wordCount < 10000) {
    return `${wordCount}字`
  }
  
  // 转换为万字，保留一位小数
  const wanCount = (wordCount / 10000).toFixed(1)
  return `${wanCount}万字`
}

// 格式化时间
const formatTime = (time) => {
  // 确保time是Date对象
  const timeDate = time instanceof Date ? time : new Date(time)

  // 检查日期是否有效
  if (isNaN(timeDate.getTime())) {
    return '未知时间'
  }

  const now = new Date()
  const diff = now - timeDate
  const days = Math.floor(diff / (24 * 60 * 60 * 1000))

  if (days === 0) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    if (hours === 0) {
      const minutes = Math.floor(diff / (60 * 1000))
      return minutes === 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return timeDate.toLocaleDateString()
  }
}

// 跳转到书籍详情页
const goToBook = (book) => {
  if (typeof book === 'object') {
    router.push(`/book/${book.bookId || book.id}`)
  } else {
    router.push(`/book/${book}`)
  }
}

// 跳转到阅读页面
const goToReadPage = (bookId, chapterName) => {
  // 从章节名称中提取章节号
  const chapterMatch = chapterName.match(/第(\d+)章/)
  const chapterId = chapterMatch ? chapterMatch[1] : '1'

  // 在新标签页打开阅读页面
  window.open(`/read/${bookId}/${chapterId}`, '_blank')
}

// 根据作者搜索
const searchByAuthor = (author) => {
  router.push({
    path: '/search',
    query: {
      q: author,
      type: 'author'
    }
  })
}

// 根据分类筛选
const filterByCategory = (category) => {
  // 将中文分类转换为英文值
  const categoryMap = {
    '玄幻': 'xuanhuan',
    '仙侠': 'xianxia',
    '都市': 'dushi',
    '历史': 'lishi',
    '科幻': 'kehuan',
    '言情': 'yanqing',
    '网游': 'wangyou',
    '悬疑': 'xuanyi'
  }

  const categoryValue = categoryMap[category] || category

  // 更新筛选条件
  filters.category = categoryValue

  // 更新URL参数
  updateQueryParams()
}

// 根据状态筛选
const filterByStatus = (statusName) => {
  // 将中文状态名转换为内部状态值
  if (statusName === '已完结') {
    filters.status = 'completed'
  } else if (statusName === '连载中') {
    filters.status = 'serializing'
  } else {
    filters.status = 'all'
  }

  // 更新URL参数
  updateQueryParams()
}

// 更新URL参数
const updateQueryParams = () => {
  const query = {}

  if (filters.status !== 'all') {
    query.status = filters.status === 'completed' ? '已完结' : '连载中'
  }

  if (filters.category !== 'all') {
    query.category = filters.category
  }

  if (filters.wordCount !== 'all') {
    query.wordCount = filters.wordCount
  }

  if (filters.updateTime !== 'all') {
    query.updateTime = filters.updateTime
  }

  if (sortBy.value !== 'default') {
    query.sortBy = sortBy.value
  }

  router.push({
    path: '/library',
    query
  })
}

// 从URL参数初始化筛选条件
const initFiltersFromQuery = () => {
  // 重置所有筛选条件为默认值
  filters.category = 'all'
  filters.wordCount = 'all'
  filters.updateTime = 'all'
  filters.status = 'all'
  sortBy.value = 'default'

  // 根据URL参数设置筛选条件
  if (route.query.status) {
    // 处理中文状态参数
    if (route.query.status === '已完结') {
      filters.status = 'completed'
    } else if (route.query.status === '连载中') {
      filters.status = 'serializing'
    } else {
      filters.status = route.query.status
    }
  }

  if (route.query.category) {
    filters.category = route.query.category
  }

  if (route.query.wordCount) {
    filters.wordCount = route.query.wordCount
  }

  if (route.query.updateTime) {
    filters.updateTime = route.query.updateTime
  }

  if (route.query.sortBy) {
    sortBy.value = route.query.sortBy
  }
}

// 监听路由变化
watch(() => route.query, () => {
  initFiltersFromQuery()
  fetchBooks()
}, { immediate: true })

// 组件挂载时初始化数据
onMounted(async () => {
  await fetchCategories()
  initFiltersFromQuery()
  await fetchBooks()
})
</script>

<style scoped>
.library-page {
  padding: 80px 0 20px;
  min-height: calc(100vh - 120px);
  background-color: #f5f5f5;
}

.page-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.library-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 10px;
}

.library-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-section .el-card {
  box-shadow: none;
}

.filter-row {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.filter-item h3 {
  font-size: 16px;
  margin-bottom: 10px;
  color: #333;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.filter-label {
  font-weight: bold;
  color: #333;
  margin-right: 5px;
  white-space: nowrap;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.sort-nav {
  flex: 1;
}

.sort-radio-group {
  display: flex;
  width: 100%;
  justify-content: flex-start;
}

.view-toggle {
  margin-left: 20px;
  flex-shrink: 0;
}

.view-toggle :deep(.el-radio-button__inner) {
  padding: 8px 12px;
  font-size: 14px;
  background-color: transparent;
  border-color: #e4e7ed;
  color: #606266;
}

.view-toggle :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: transparent;
  border-color: #409eff;
  color: #409eff;
  box-shadow: none;
}

.view-toggle :deep(.el-radio-button__original-radio:not(:checked):not(:disabled) + .el-radio-button__inner:hover) {
  color: #409eff;
  background-color: transparent;
}

.sort-radio-group :deep(.el-radio-button__inner) {
  padding: 8px 15px;
  font-size: 14px;
  background-color: transparent;
  border: none;
  color: #606266;
  border-radius: 0;
  position: relative;
}

.sort-radio-group :deep(.el-radio-button__inner::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background-color: #409eff;
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.sort-radio-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: transparent;
  border: none;
  color: #409eff;
  box-shadow: none;
}

.sort-radio-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner::after) {
  width: 80%;
}

.sort-radio-group :deep(.el-radio-button__original-radio:not(:checked):not(:disabled) + .el-radio-button__inner:hover) {
  color: #409eff;
  background-color: transparent;
}

.sort-radio-group :deep(.el-radio-button) {
  margin-right: 15px;
}

.sort-radio-group :deep(.el-radio-button:last-child) {
  margin-right: 0;
}

/* 网格模式样式 */
.books-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0;
  margin-bottom: 20px;
  background-color: #f5f5f5;
}

.book-card {
  background-color: #f5f5f5;
  border-radius: 0;
  overflow: hidden;
  box-shadow: none;
  transition: all 0.3s;
  display: flex;
  height: 160px;
  border: none;
  padding: 10px;
  box-sizing: border-box;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

/* 可点击元素样式 */
.clickable {
  cursor: pointer;
  transition: color 0.2s;
}

.clickable:hover {
  color: #ff4d4f !important;
}

.book-cover {
  position: relative;
  width: 120px;
  height: 160px;
  overflow: hidden;
  flex-shrink: 0;
  transition: none;
  border-radius: 4px;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.book-cover:hover img {
  transform: scale(1.05);
}

.book-info {
  padding: 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
  background-color: #f5f5f5;
  border-radius: 4px;
  height: 140px;
}

.book-title {
  font-size: 16px;
  margin-bottom: 8px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-weight: bold;
}

.book-meta {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.book-description {
  font-size: 12px;
  color: #999;
  line-height: 1.4;
  margin-bottom: 8px;
  overflow: hidden;
  display: -webkit-box;
  display: -moz-box;
  display: box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  -moz-box-orient: vertical;
  box-orient: vertical;
  text-overflow: ellipsis;
  flex: 1;
}

.book-latest {
  font-size: 12px;
  color: #666;
  display: flex;
  justify-content: space-between;
  overflow: hidden;
}

.chapter-name {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 70%;
}

.update-time {
  flex-shrink: 0;
  color: #999;
}

/* 列表模式样式 */
.books-list {
  margin-bottom: 20px;
}

.book-title-link {
  font-weight: 600;
  font-size: 15px;
  color: #333;
}

:deep(.book-row) {
  cursor: pointer;
  transition: background-color 0.2s;
  background-color: #5d3030 !important;
}

:deep(.book-row:hover) {
  background-color: #ffffff !important;
}

/* 表格单元格样式 */
:deep(.el-table td) {
  padding: 12px 0;
  border-bottom: 1px solid #e8e8e8;
  background-color: #f5f5f5 !important;
}

:deep(.el-table__body tr:hover td) {
  background-color: #ffffff !important;
}

/* 表格表头样式 */
:deep(.el-table th) {
  background-color: #efebeb !important;
  color: #333;
  font-weight: 600;
  border-bottom: 2px solid #d0d0d0 !important;
}

/* 表格整体样式 */
:deep(.el-table) {
  background-color: #f5f5f5 !important;
}

:deep(.el-table__body-wrapper) {
  background-color: #f5f5f5 !important;
}

:deep(.el-table__body) {
  background-color: #f5f5f5 !important;
}

/* 最新章节样式 */
:deep(.el-table .cell .clickable) {
  font-weight: 500;
  font-size: 14px;
}

/* 标签样式 */
:deep(.el-tag) {
  cursor: pointer;
  transition: all 0.2s;
}

:deep(.el-tag:hover) {
  transform: scale(1.05);
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .container {
    max-width: 960px;
  }

  .books-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  .container {
    max-width: 720px;
  }

  .books-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .container {
    max-width: 540px;
    padding: 0 15px;
  }

  .library-page {
    padding: 70px 0 15px;
  }

  .page-title {
    font-size: 22px;
    margin-bottom: 15px;
  }

  .library-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .sort-nav {
    width: 100%;
  }

  .sort-radio-group {
    overflow-x: auto;
    white-space: nowrap;
    padding-bottom: 5px;
  }

  .view-toggle {
    margin-left: 0;
  }

  .books-grid {
    grid-template-columns: 1fr;
  }

  .book-card {
    height: 140px;
  }

  .book-cover {
    width: 100px;
    height: 140px;
  }

  .filter-row {
    grid-template-columns: 1fr;
  }

  .filter-options {
    flex-direction: column;
    gap: 8px;
  }
}

@media (max-width: 576px) {
  .container {
    max-width: 100%;
    padding: 0 12px;
  }

  .library-page {
    padding: 70px 0 12px;
  }

  .page-title {
    font-size: 20px;
    margin-bottom: 12px;
  }

  .library-header {
    gap: 12px;
  }

  .section-header {
    gap: 8px;
  }

  .sort-options {
    width: 100%;
    justify-content: space-between;
  }

  .book-card {
    height: 120px;
  }

  .book-cover {
    width: 80px;
    height: 120px;
  }

  .book-info {
    padding: 10px;
  }

  .book-title {
    font-size: 14px;
    margin-bottom: 5px;
  }

  .book-meta, .book-latest {
    font-size: 11px;
  }

  .book-description {
    font-size: 11px;
  }

  .book-status {
    top: 5px;
    right: 5px;
    font-size: 10px;
    padding: 1px 5px;
  }

  .pagination {
    margin-top: 15px;
  }
}
</style>
