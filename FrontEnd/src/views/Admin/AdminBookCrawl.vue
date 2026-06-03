<template>
  <div class="admin-book-crawl">
    <el-card shadow="hover" class="page-header">
      <template #header>
        <div class="card-header">
          <h1>自动化获取小说资源</h1>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item><el-icon><House /></el-icon>首页</el-breadcrumb-item>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>小说管理</el-breadcrumb-item>
              <el-breadcrumb-item>获取小说资源</el-breadcrumb-item>
            </el-breadcrumb>
          </el-breadcrumb>
        </div>
      </template>
    </el-card>

    <!-- 标签页 -->
    <el-card shadow="hover" class="main-card">
      <el-tabs v-model="activeTab" class="crawl-tabs">
        <!-- 爬取导入 -->
        <el-tab-pane label="爬取导入" name="import">
          <div class="crawl-import">
            <el-radio-group v-model="importType" size="large" class="import-type-tabs">
              <el-radio-button label="number">编号爬取</el-radio-button>
              <el-radio-button label="search">搜索爬取</el-radio-button>
            </el-radio-group>

            <!-- 编号爬取 -->
            <div v-if="importType === 'number'" class="number-crawl">
              <el-card shadow="hover" class="crawl-card">
                <template #header>
                  <div class="card-header-content">
                    <div class="card-title">书籍编号爬取</div>
                  </div>
                </template>
                <el-form :model="numberCrawlForm" label-width="120px" class="crawl-form">
                  <el-form-item label="爬取模式">
                    <el-radio-group v-model="numberCrawlForm.mode">
                      <el-radio label="single">单个编号</el-radio>
                      <el-radio label="range">编号区间</el-radio>
                    </el-radio-group>
                  </el-form-item>

                  <el-form-item label="书籍编号" v-if="numberCrawlForm.mode === 'single'">
                    <el-input v-model="numberCrawlForm.number" placeholder="请输入书籍编号" clearable />
                    <div v-if="numberError" class="error-message">请输入正确的整数</div>
                  </el-form-item>

                  <template v-else>
                    <el-form-item label="起始编号">
                      <el-input v-model="numberCrawlForm.startNumber" placeholder="请输入起始编号" clearable />
                      <div v-if="startNumberError" class="error-message">请输入正确的整数</div>
                    </el-form-item>
                    <el-form-item label="结束编号">
                      <el-input v-model="numberCrawlForm.endNumber" placeholder="请输入结束编号" clearable />
                      <div v-if="endNumberError" class="error-message">请输入正确的整数</div>
                    </el-form-item>
                  </template>

                  <el-form-item>
                    <el-button type="primary" @click="startNumberCrawl" :loading="numberCrawling">
                      <el-icon><Download /></el-icon> 开始爬取
                    </el-button>
                    <el-button @click="resetNumberForm">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-card>
            </div>

            <!-- 搜索爬取 -->
            <div v-else class="search-crawl">
              <el-card shadow="hover" class="crawl-card">
                <template #header>
                  <div class="card-header-content">
                    <div class="card-title">搜索爬取</div>
                  </div>
                </template>
                <el-form :model="searchCrawlForm" label-width="120px" class="crawl-form">
                  <el-form-item label="搜索关键词">
                    <el-input v-model="searchCrawlForm.keyword" placeholder="请输入小说名或作者名" clearable />
                  </el-form-item>

                  <el-form-item>
                    <el-button type="primary" @click="searchBooks" :loading="searching">
                      <el-icon><Search /></el-icon> 搜索
                    </el-button>
                    <el-button @click="resetSearchForm">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-card>

              <!-- 搜索结果 -->
              <el-card shadow="hover" class="result-card" v-if="searchResults.length > 0">
                <template #header>
                  <div class="card-header-content">
                    <div class="card-title">搜索结果</div>
                    <div class="card-actions">
                      <el-button type="primary" size="small" @click="batchImportFromSearch" :disabled="selectedSearchBooks.length === 0">
                        <el-icon><Upload /></el-icon> 批量导入
                      </el-button>
                    </div>
                  </div>
                </template>

                <!-- 结果二次筛选 -->
                <div class="result-filter" v-if="searchResults.length > 0">
                  <el-select
                    v-model="resultFilter.type"
                    placeholder="筛选类型"
                    size="small"
                    @change="handleResultFilter"
                    style="width: 120px; margin-right: 10px"
                  >
                    <el-option label="默认" value="default" />
                    <el-option label="作者" value="author" />
                    <el-option label="书名" value="name" />
                    <el-option label="最新章节" value="latest_chapter" />
                  </el-select>
                  <el-input
                    v-model="resultFilter.keyword"
                    placeholder="请输入筛选关键词"
                    clearable
                    style="width: 400px; margin-right: 10px"
                    @input="handleResultFilter"
                  >
                  </el-input>
                </div>

                <el-table
                  v-loading="searching"
                  :data="fullyFilteredResults"
                  @selection-change="handleSearchSelectionChange"
                  @sort-change="handleSortChange"
                  @filter-change="handleFilterChange"
                >
                  <el-table-column type="selection" width="55" />
                  <el-table-column
                    prop="id"
                    label="ID"
                    width="80"
                    sortable="custom"
                  />
                  <el-table-column
                    prop="name"
                    label="书名"
                    min-width="200"
                  />
                  <el-table-column
                    prop="author"
                    label="作者"
                    width="120"
                  />
                  <el-table-column
                    label="分类"
                    width="120"
                    column-key="type"
                    :filters="categoryFilters"
                    filter-placement="bottom-end"
                  >
                    <template #default="scope">
                      {{ scope.row.type || scope.row.category || '未知' }}
                    </template>
                  </el-table-column>
                  <el-table-column
                    prop="latest_chapter"
                    label="最新章节"
                    min-width="180"
                  />
                  <el-table-column
                    prop="update_time"
                    label="更新时间"
                    width="120"
                    sortable="custom"
                  />
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button type="primary" size="small" @click="importSingleBook(scope.row.id)">
                        <el-icon><Download /></el-icon> 导入
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <!-- 爬取更新 -->
        <el-tab-pane label="爬取更新" name="update">
          <div class="crawl-update">
            <el-card shadow="hover" class="crawl-card">
              <template #header>
                <div class="card-header-content">
                  <div class="card-title">爬取更新</div>
                  <div class="card-actions">
                    <el-button type="primary" @click="batchUpdateBooks" :disabled="selectedUpdateBooks.length === 0">
                      <el-icon><Download /></el-icon> 批量更新
                    </el-button>
                  </div>
                </div>
              </template>
              <div class="search-box">
                <el-input v-model="updateSearchForm.keyword" placeholder="搜索小说名或作者名" clearable prefix-icon="Search" @keyup.enter="getSerialBooks">
                  <template #append>
                    <el-button @click="getSerialBooks"><el-icon><Search /></el-icon> 搜索</el-button>
                  </template>
                </el-input>
              </div>

              <el-table v-loading="updateLoading" :data="paginatedSerialBooks" @selection-change="handleUpdateSelectionChange" @sort-change="handleUpdateSortChange" @filter-change="handleUpdateFilterChange">
                <el-table-column type="selection" width="55" />
                <el-table-column
                  prop="bookId"
                  label="ID"
                  width="80"
                  sortable="custom"
                />
                <el-table-column
                  prop="title"
                  label="书名"
                  min-width="200"
                />
                <el-table-column
                  prop="author"
                  label="作者"
                  width="120"
                />
                <el-table-column
                  label="分类"
                  width="120"
                  column-key="category"
                  :filters="updateCategoryFilters"
                  filter-placement="bottom-end"
                >
                  <template #default="scope">
                    {{ scope.row.categoryName || '未知' }}
                  </template>
                </el-table-column>
                <el-table-column
                  prop="lastChapterTitle"
                  label="最新章节"
                  min-width="180"
                />
                <el-table-column
                  prop="lastChapterTime"
                  label="上次更新"
                  width="180"
                  sortable="custom"
                >
                  <template #default="scope">
                    {{ formatDate(scope.row.lastChapterTime) }}
                  </template>
                </el-table-column>
              </el-table>

              <!-- 分页 -->
              <div class="pagination">
                <el-pagination
                  v-model:current-page="updatePagination.currentPage"
                  v-model:page-size="updatePagination.pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="filteredSerialBooks.length"
                  @size-change="handleUpdateSizeChange"
                  @current-change="handleUpdateCurrentChange"
                ></el-pagination>
              </div>
            </el-card>
          </div>
        </el-tab-pane>

        <!-- 纠正数据 -->
        <el-tab-pane label="纠正数据" name="correct">
          <div class="correct-data">
            <el-card shadow="hover" class="crawl-card">
              <template #header>
                <div class="card-header-content">
                  <div class="card-title">纠正数据</div>
                  <div class="card-actions">
                    <el-button type="primary" @click="batchCorrectBooks" :disabled="selectedCorrectBooks.length === 0">
                      <el-icon><EditPen /></el-icon> 批量纠正
                    </el-button>
                  </div>
                </div>
              </template>
              <div class="search-box">
                <el-input v-model="correctSearchForm.keyword" placeholder="搜索小说名或作者名" clearable prefix-icon="Search" @keyup.enter="getAllBooks">
                  <template #append>
                    <el-button @click="getAllBooks"><el-icon><Search /></el-icon> 搜索</el-button>
                  </template>
                </el-input>
              </div>

              <el-table v-loading="correctLoading" :data="paginatedAllBooks" @selection-change="handleCorrectSelectionChange" @sort-change="handleCorrectSortChange" @filter-change="handleCorrectFilterChange">
                <el-table-column type="selection" width="55" />
                <el-table-column
                  prop="bookId"
                  label="ID"
                  width="80"
                  sortable="custom"
                />
                <el-table-column
                  prop="title"
                  label="书名"
                  min-width="200"
                />
                <el-table-column
                  prop="author"
                  label="作者"
                  width="120"
                />
                <el-table-column
                  label="分类"
                  width="120"
                  column-key="category"
                  :filters="correctCategoryFilters"
                  filter-placement="bottom-end"
                >
                  <template #default="scope">
                    {{ scope.row.categoryName || '未知' }}
                  </template>
                </el-table-column>
                <el-table-column
                  prop="status"
                  label="状态"
                  width="100"
                  column-key="status"
                  :filters="statusFilters"
                  filter-placement="bottom-end"
                >
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 'serial' ? 'warning' : 'success'">
                      {{ scope.row.statusName || (scope.row.status === 'serial' ? '连载' : '完结') }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 分页 -->
              <div class="pagination">
                <el-pagination
                  v-model:current-page="correctPagination.currentPage"
                  v-model:page-size="correctPagination.pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="filteredAllBooks.length"
                  @size-change="handleCorrectSizeChange"
                  @current-change="handleCorrectCurrentChange"
                ></el-pagination>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { House, Download, Search, Upload, EditPen } from '@element-plus/icons-vue'
import { crawlApi } from '@/api/Crawl'
import { booksApi } from '@/api/Books'
import { ElMessage } from 'element-plus'

// 活动标签页
const activeTab = ref('import')

// 导入类型
const importType = ref('number')

// 编号爬取表单
const numberCrawlForm = reactive({
  mode: 'single',
  number: '',
  startNumber: '',
  endNumber: ''
})

// 编号验证错误信息
const numberError = ref(false)
const startNumberError = ref(false)
const endNumberError = ref(false)

// 搜索爬取表单
const searchCrawlForm = reactive({
  keyword: ''
})

// 更新搜索表单
const updateSearchForm = reactive({
  keyword: ''
})

// 纠正搜索表单
const correctSearchForm = reactive({
  keyword: ''
})

// 加载状态
const numberCrawling = ref(false)
const searching = ref(false)
const updateLoading = ref(false)
const correctLoading = ref(false)

// 搜索结果
const searchResults = ref([])

// 结果二次筛选
const resultFilter = reactive({
  keyword: '',
  type: 'default'
})

// 分类筛选
const categoryFilters = ref([])
const currentCategoryFilter = ref([])

// 更新标签页分类筛选
const updateCategoryFilters = ref([])
const currentUpdateCategoryFilter = ref([])

// 纠正标签页分类筛选
const correctCategoryFilters = ref([])
const currentCorrectCategoryFilter = ref([])

// 状态筛选
const statusFilters = ref([
  { text: '连载', value: 'serial' },
  { text: '完结', value: 'completed' }
])
const currentStatusFilter = ref([])

// 排序和筛选状态
const sortState = reactive({
  prop: '',
  order: ''
})

// 更新标签页排序状态
const updateSortState = reactive({
  prop: '',
  order: ''
})

// 纠正标签页排序状态
const correctSortState = reactive({
  prop: '',
  order: ''
})

// 连载书籍
const serialBooks = ref([])
const selectedUpdateBooks = ref([])

// 所有书籍
const allBooks = ref([])
const selectedCorrectBooks = ref([])

// 分页配置
const updatePagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const correctPagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 搜索结果选中
const selectedSearchBooks = ref([])

// 初始化
onMounted(() => {
  // 获取连载书籍
  getSerialBooks()
  // 获取所有书籍
  getAllBooks()
})

// 验证输入是否为整数且在Long范围内
const validateInteger = (value) => {
  if (!value) return true
  const num = Number(value)
  return Number.isInteger(num) && num >= -9007199254740991 && num <= 9007199254740991
}

// 开始编号爬取
const startNumberCrawl = async () => {
  try {
    // 重置错误状态
    numberError.value = false
    startNumberError.value = false
    endNumberError.value = false

    // 验证输入
    let isValid = true

    if (numberCrawlForm.mode === 'single') {
      if (!validateInteger(numberCrawlForm.number)) {
        numberError.value = true
        isValid = false
      }
    } else {
      if (!validateInteger(numberCrawlForm.startNumber)) {
        startNumberError.value = true
        isValid = false
      }
      if (!validateInteger(numberCrawlForm.endNumber)) {
        endNumberError.value = true
        isValid = false
      }
      if (isValid) {
        const start = Number(numberCrawlForm.startNumber)
        const end = Number(numberCrawlForm.endNumber)
        if (start > end) {
          endNumberError.value = true
          isValid = false
          ElMessage.error('结束编号不能小于起始编号')
        }
      }
    }

    if (!isValid) {
      ElMessage.error('请输入正确的整数')
      return
    }

    numberCrawling.value = true

    let response
    let successMessage = ''

    if (numberCrawlForm.mode === 'single') {
      // 单个编号爬取
      const novelId = Number(numberCrawlForm.number)
      response = await crawlApi.fetchNovel(novelId)
      successMessage = `成功爬取编号为 ${novelId} 的小说`
    } else {
      // 编号区间爬取
      const startId = Number(numberCrawlForm.startNumber)
      const endId = Number(numberCrawlForm.endNumber)
      response = await crawlApi.batchFetchNovels(startId, endId)
      successMessage = `成功爬取编号 ${startId} 到 ${endId} 区间的小说`
    }

    console.log('爬取结果:', response.data)
    ElMessage.success(successMessage)
  } catch (error) {
    console.error('爬取失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '爬取失败，请稍后重试'
    ElMessage.error(errorMessage)
  } finally {
    numberCrawling.value = false
  }
}

// 重置编号爬取表单
const resetNumberForm = () => {
  Object.assign(numberCrawlForm, {
    mode: 'single',
    number: '',
    startNumber: '',
    endNumber: ''
  })
  // 重置错误状态
  numberError.value = false
  startNumberError.value = false
  endNumberError.value = false
}

// 搜索书籍
const searchBooks = async () => {
  try {
    if (!searchCrawlForm.keyword) return

    searching.value = true
    const response = await crawlApi.searchNovels(searchCrawlForm.keyword)
    // 直接使用返回的数组作为搜索结果
    searchResults.value = response.data || []

    // 提取分类过滤选项
    extractCategoryFilters()
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    searching.value = false
  }
}

// 提取分类过滤选项
const extractCategoryFilters = () => {
  // 尝试从不同的可能字段名中提取分类
  const categories = [...new Set(searchResults.value.map(item => item.type || item.category || '未知'))]
  categoryFilters.value = categories.map(cat => ({ text: cat, value: cat }))
}

// 处理结果筛选
const handleResultFilter = () => {
  // 筛选逻辑在computed属性中处理
}

// 处理排序
const handleSortChange = ({ prop, order }) => {
  sortState.prop = prop
  sortState.order = order
}

// 处理分类筛选
const handleFilterChange = (filters) => {
  if (filters.type) {
    currentCategoryFilter.value = filters.type
  }
}

// 重置搜索表单
const resetSearchForm = () => {
  searchCrawlForm.keyword = ''
  searchResults.value = []
  selectedSearchBooks.value = []
  resultFilter.keyword = ''
  resultFilter.type = 'default'
  sortState.prop = ''
  sortState.order = ''
  currentCategoryFilter.value = []
}

// 单个书籍导入
const importSingleBook = async (bookId) => {
  try {
    const response = await crawlApi.fetchNovel(bookId)
    const bookName = response.data?.name || response.data?.title || `编号为${bookId}的小说`
    console.log('导入结果:', response.data)
    ElMessage.success(`成功导入《${bookName}》`)
  } catch (error) {
    console.error('导入失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '导入失败，请稍后重试'
    ElMessage.error(errorMessage)
  }
}

// 批量导入
const batchImportFromSearch = async () => {
  try {
    if (selectedSearchBooks.value.length === 0) {
      ElMessage.warning('请先选择要导入的书籍')
      return
    }

    let successCount = 0
    let failCount = 0
    const failedBooks = []

    // 循环调用单个导入接口（后端没有批量导入接口）
    for (const book of selectedSearchBooks.value) {
      try {
        await crawlApi.fetchNovel(book.id)
        successCount++
      } catch (error) {
        failCount++
        failedBooks.push(book.name || book.title || `编号为${book.id}的小说`)
        console.error(`导入 ${book.name || book.title} 失败:`, error)
      }
    }

    if (failCount === 0) {
      ElMessage.success(`成功导入全部 ${successCount} 本书籍`)
    } else if (successCount === 0) {
      ElMessage.error(`导入失败，共 ${failCount} 本书籍导入失败`)
    } else {
      ElMessage.warning(`部分导入成功，成功 ${successCount} 本，失败 ${failCount} 本`)
      console.log('导入失败的书籍:', failedBooks)
    }
  } catch (error) {
    console.error('批量导入失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '批量导入失败，请稍后重试'
    ElMessage.error(errorMessage)
  }
}

// 获取连载书籍
const getSerialBooks = async () => {
  try {
    updateLoading.value = true
    // 使用booksApi.getBooksByStatus(1)获取连载中的小说
    const response = await booksApi.getBooksByStatus(1)
    serialBooks.value = response.data || []
    updatePagination.total = serialBooks.value.length

    // 提取分类过滤选项
    extractUpdateCategoryFilters()
  } catch (error) {
    console.error('获取连载书籍失败:', error)
    ElMessage.error('获取连载书籍失败，请稍后重试')
  } finally {
    updateLoading.value = false
  }
}

// 提取更新标签页分类过滤选项
const extractUpdateCategoryFilters = () => {
  // 尝试从不同的可能字段名中提取分类
  const categories = [...new Set(serialBooks.value.map(item => item.categoryName || '未知'))]
  updateCategoryFilters.value = categories.map(cat => ({ text: cat, value: cat }))
}

// 处理更新标签页排序
const handleUpdateSortChange = ({ prop, order }) => {
  updateSortState.prop = prop
  updateSortState.order = order
}

// 处理更新标签页分类筛选
const handleUpdateFilterChange = (filters) => {
  if (filters.category) {
    currentUpdateCategoryFilter.value = filters.category
  }
}

// 获取所有书籍
const getAllBooks = async () => {
  try {
    correctLoading.value = true
    // 使用booksApi.getAllBooks()获取所有小说
    const response = await booksApi.getAllBooks()
    allBooks.value = response.data || []
    correctPagination.total = allBooks.value.length

    // 提取分类过滤选项
    extractCorrectCategoryFilters()
  } catch (error) {
    console.error('获取所有书籍失败:', error)
    ElMessage.error('获取所有书籍失败，请稍后重试')
  } finally {
    correctLoading.value = false
  }
}

// 提取纠正标签页分类过滤选项
const extractCorrectCategoryFilters = () => {
  // 尝试从不同的可能字段名中提取分类
  const categories = [...new Set(allBooks.value.map(item => item.categoryName || '未知'))]
  correctCategoryFilters.value = categories.map(cat => ({ text: cat, value: cat }))
}

// 处理纠正标签页排序
const handleCorrectSortChange = ({ prop, order }) => {
  correctSortState.prop = prop
  correctSortState.order = order
}

// 处理纠正标签页筛选
const handleCorrectFilterChange = (filters) => {
  if (filters.category) {
    currentCorrectCategoryFilter.value = filters.category
  }
  if (filters.status) {
    currentStatusFilter.value = filters.status
  }
}

// 批量更新书籍
const batchUpdateBooks = async () => {
  try {
    if (selectedUpdateBooks.value.length === 0) {
      ElMessage.warning('请先选择要更新的书籍')
      return
    }

    let successCount = 0
    let failCount = 0
    const failedBooks = []

    // 循环调用更新接口（后端没有批量更新接口）
    for (const book of selectedUpdateBooks.value) {
      try {
        await crawlApi.updateNovelChapters(book.id)
        successCount++
      } catch (error) {
        failCount++
        failedBooks.push(book.title || `编号为${book.id}的小说`)
        console.error(`更新 ${book.title} 失败:`, error)
      }
    }

    if (failCount === 0) {
      ElMessage.success(`成功更新全部 ${successCount} 本书籍的章节`)
    } else if (successCount === 0) {
      ElMessage.error(`更新失败，共 ${failCount} 本书籍更新失败`)
    } else {
      ElMessage.warning(`部分更新成功，成功 ${successCount} 本，失败 ${failCount} 本`)
      console.log('更新失败的书籍:', failedBooks)
    }
  } catch (error) {
    console.error('批量更新失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '批量更新失败，请稍后重试'
    ElMessage.error(errorMessage)
  }
}

// 批量纠正书籍
const batchCorrectBooks = async () => {
  try {
    if (selectedCorrectBooks.value.length === 0) {
      ElMessage.warning('请先选择要纠正的书籍')
      return
    }

    let successCount = 0
    let failCount = 0
    const failedBooks = []

    // 循环调用纠正接口（后端没有批量纠正接口）
    for (const book of selectedCorrectBooks.value) {
      try {
        await crawlApi.refreshNovel(book.id)
        successCount++
      } catch (error) {
        failCount++
        failedBooks.push(book.title || `编号为${book.id}的小说`)
        console.error(`纠正 ${book.title} 失败:`, error)
      }
    }

    if (failCount === 0) {
      ElMessage.success(`成功纠正全部 ${successCount} 本书籍的章节`)
    } else if (successCount === 0) {
      ElMessage.error(`纠正失败，共 ${failCount} 本书籍纠正失败`)
    } else {
      ElMessage.warning(`部分纠正成功，成功 ${successCount} 本，失败 ${failCount} 本`)
      console.log('纠正失败的书籍:', failedBooks)
    }
  } catch (error) {
    console.error('批量纠正失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '批量纠正失败，请稍后重试'
    ElMessage.error(errorMessage)
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
    second: '2-digit'
  })
}

// 更新标签页分页处理
const handleUpdateSizeChange = (size) => {
  updatePagination.pageSize = size
  updatePagination.currentPage = 1
}

const handleUpdateCurrentChange = (current) => {
  updatePagination.currentPage = current
}

// 纠正标签页分页处理
const handleCorrectSizeChange = (size) => {
  correctPagination.pageSize = size
  correctPagination.currentPage = 1
}

const handleCorrectCurrentChange = (current) => {
  correctPagination.currentPage = current
}

// 选择处理 - 基于分页后的数据
const handleSearchSelectionChange = (selection) => {
  selectedSearchBooks.value = selection
}

// 更新标签页选择处理
const handleUpdateSelectionChange = (selection) => {
  selectedUpdateBooks.value = selection
}

// 纠正标签页选择处理
const handleCorrectSelectionChange = (selection) => {
  selectedCorrectBooks.value = selection
}

// 筛选后的搜索结果（用于关键词筛选和排序，不包括分类筛选）
const filteredSearchResults = computed(() => {
  let results = [...searchResults.value]

  // 关键词筛选
  if (resultFilter.keyword) {
    const keyword = resultFilter.keyword.toLowerCase()
    results = results.filter(item => {
      switch (resultFilter.type) {
        case 'author':
          return item.author.toLowerCase().includes(keyword)
        case 'name':
          return item.name.toLowerCase().includes(keyword)
        case 'latest_chapter':
          return item.latest_chapter.toLowerCase().includes(keyword)
        default:
          return item.author.toLowerCase().includes(keyword) ||
                 item.name.toLowerCase().includes(keyword) ||
                 item.latest_chapter.toLowerCase().includes(keyword)
      }
    })
  }

  // 排序
  if (sortState.prop && sortState.order) {
    results.sort((a, b) => {
      const aVal = a[sortState.prop]
      const bVal = b[sortState.prop]

      if (typeof aVal === 'string') {
        return sortState.order === 'ascending'
          ? aVal.localeCompare(bVal)
          : bVal.localeCompare(aVal)
      } else {
        return sortState.order === 'ascending'
          ? aVal - bVal
          : bVal - aVal
      }
    })
  }

  return results
})

// 完全筛选后的搜索结果（包括分类筛选）
const fullyFilteredResults = computed(() => {
  let results = [...filteredSearchResults.value]

  // 分类筛选
  if (currentCategoryFilter.value.length > 0) {
    results = results.filter(item => {
      const itemCategory = item.type || item.category || '未知'
      return currentCategoryFilter.value.includes(itemCategory)
    })
  }

  return results
})

// 更新标签页筛选和排序后的连载书籍
const filteredSerialBooks = computed(() => {
  let results = [...serialBooks.value]

  // 分类筛选
  if (currentUpdateCategoryFilter.value.length > 0) {
    results = results.filter(item => {
      const itemCategory = item.categoryName || '未知'
      return currentUpdateCategoryFilter.value.includes(itemCategory)
    })
  }

  // 排序
  if (updateSortState.prop && updateSortState.order) {
    results.sort((a, b) => {
      const aVal = a[updateSortState.prop]
      const bVal = b[updateSortState.prop]

      if (typeof aVal === 'string') {
        return updateSortState.order === 'ascending'
          ? aVal.localeCompare(bVal)
          : bVal.localeCompare(aVal)
      } else {
        return updateSortState.order === 'ascending'
          ? aVal - bVal
          : bVal - aVal
      }
    })
  }

  return results
})

// 更新标签页当前页显示的连载书籍
const paginatedSerialBooks = computed(() => {
  const start = (updatePagination.currentPage - 1) * updatePagination.pageSize
  const end = start + updatePagination.pageSize
  return filteredSerialBooks.value.slice(start, end)
})

// 纠正标签页筛选和排序后的所有书籍
const filteredAllBooks = computed(() => {
  let results = [...allBooks.value]

  // 分类筛选
  if (currentCorrectCategoryFilter.value.length > 0) {
    results = results.filter(item => {
      const itemCategory = item.categoryName || '未知'
      return currentCorrectCategoryFilter.value.includes(itemCategory)
    })
  }

  // 状态筛选
  if (currentStatusFilter.value.length > 0) {
    results = results.filter(item => {
      return currentStatusFilter.value.includes(item.status)
    })
  }

  // 排序
  if (correctSortState.prop && correctSortState.order) {
    results.sort((a, b) => {
      const aVal = a[correctSortState.prop]
      const bVal = b[correctSortState.prop]

      if (typeof aVal === 'string') {
        return correctSortState.order === 'ascending'
          ? aVal.localeCompare(bVal)
          : bVal.localeCompare(aVal)
      } else {
        return correctSortState.order === 'ascending'
          ? aVal - bVal
          : bVal - aVal
      }
    })
  }

  return results
})

// 纠正标签页当前页显示的所有书籍
const paginatedAllBooks = computed(() => {
  const start = (correctPagination.currentPage - 1) * correctPagination.pageSize
  const end = start + correctPagination.pageSize
  return filteredAllBooks.value.slice(start, end)
})
</script>

<style scoped>
.admin-book-crawl {
  background-color: #f5f7fa;
  min-height: 100vh;
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

/* 面包屑导航样式 */
.card-header .el-breadcrumb {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 400px;
}

.card-header .el-breadcrumb .el-breadcrumb__item {
  display: inline-block;
  white-space: nowrap;
}

.main-card {
  margin-bottom: 20px;
}

.crawl-tabs {
  padding: 20px 0;
}

.crawl-tabs .el-tabs__header {
  margin-bottom: 20px;
}

/* 导入类型标签 */
.import-type-tabs {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f0f2f5;
  border-radius: 4px;
}

/* 爬取卡片 */
.crawl-card {
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.el-card__header {
  padding: 12px 20px;
}

.card-actions {
  display: flex;
  gap: 10px;
}

/* 爬取表单 */
.crawl-form {
  padding: 20px 0;
}

/* 搜索框 */
.search-box {
  margin-bottom: 20px;
}

/* 结果卡片 */
.result-card {
  margin-top: 20px;
}

/* 结果筛选 */
.result-filter {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 错误信息 */
.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
}

/* 筛选图标 */
.filter-icon {
  margin-left: 5px;
  cursor: pointer;
  color: #909399;
  font-size: 14px;
}

.filter-icon:hover {
  color: #409eff;
}
</style>
