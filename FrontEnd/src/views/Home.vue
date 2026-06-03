<template>
  <PageTransition>
    <div class="home">
      <div v-if="loading" class="loading-container">
        <LoadingSpinner text="正在加载首页内容..." />
      </div>
      <div v-else>
        <!-- 轮播图 -->
        <section class="banner-section">
          <el-carousel :interval="4000" height="500px" indicator-position="outside" @change="handleCarouselChange" v-model="carouselIndex">
            <el-carousel-item v-for="item in bannerList" :key="item.id">
              <div class="banner-item" @click="goToBook(item.id)">
                <img :src="item.image" :alt="item.title" class="banner-image" @load="handleImageLoad($event, item)">
              </div>
            </el-carousel-item>
          </el-carousel>
        </section>

    <!-- 小说类型分类 -->
    <section class="categories-section">
      <h2 class="section-title">小说分类</h2>
      <div class="container">
        <div class="categories-grid">
          <div
            v-for="category in categories"
            :key="category.id"
            class="category-item"
            @click="goToCategory(category.id)"
          >
            <div class="category-icon">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <h3>{{ category.name }}</h3>
            <p>{{ category.count }}本</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 推荐小说 -->
    <section class="recommend-section">
      <div class="container">
        <h2 class="section-title-left">编辑推荐</h2>
        <div class="recommend-layout">
          <!-- 左侧轮播图 -->
          <div class="recommend-carousel">
            <div class="carousel-container clickable-image" @click="goToBook(currentRecommendBook.id)">
              <img :src="currentRecommendBook.cover" :alt="currentRecommendBook.title" class="carousel-image">
            </div>
          </div>

          <!-- 右侧书籍信息 -->
          <div class="recommend-info">
            <h3 class="book-title-large clickable-text" @click="goToBook(currentRecommendBook.id)">{{ currentRecommendBook.title }}</h3>
            <p class="book-author">作者：<span class="clickable-text" @click="goToSearch(currentRecommendBook.author)">{{ currentRecommendBook.author }}</span></p>
            <div class="book-description clickable-text" @click="goToBook(currentRecommendBook.id)"><span>{{ currentRecommendBook.description }}</span></div>
            <div class="book-meta">
              <span class="book-category clickable-text" @click="goToCategoryByName(currentRecommendBook.category)">{{ currentRecommendBook.category }}</span>
              <span class="book-word-count">{{ currentRecommendBook.wordCount }}万字</span>
              <span class="book-status clickable-text" @click="goToLibraryByStatus(currentRecommendBook.status)">{{ currentRecommendBook.status === 'completed' ? '已完结' : '连载中' }}</span>
            </div>

            <!-- 五张封面图 -->
            <div class="book-covers">
              <div
                v-for="(book, index) in featuredBooks"
                :key="book.id"
                class="cover-item"
                :class="{ active: currentBookIndex === index }"
                @mouseenter="switchRecommendBook(index)"
                @click="goToBook(book.id)"
              >
                <img :src="book.cover" :alt="book.title" class="cover-image">
              </div>
            </div>
          </div>
        </div>

        <!-- 下方十二个书籍展示框 -->
        <div class="recommend-books-grid">
          <div
            v-for="book in recommendBooks"
            :key="book.id"
            class="recommend-book-item"
          >
            <div class="book-cover" @click="goToBook(book.id)">
              <img :src="book.cover" :alt="book.title" class="cover-img">
            </div>
            <div class="book-info">
              <h4 class="book-name" @click="goToBook(book.id)">{{ book.title }}</h4>
              <p class="book-description" @click="goToBook(book.id)"><span>{{ book.description }}</span></p>
              <p class="book-author-name">
                <span class="clickable-text" @click="goToSearch(book.author)">{{ book.author }}</span>·
                <span class="clickable-text" @click="goToCategoryByName(book.category)">{{ book.category }}</span>
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 小说导航区域 -->
    <section class="novel-navigation-section">
      <div class="container">
        <!-- 导航栏：最近更新、最近完本、最近入库 -->
        <div class="nav-tabs">
          <el-tabs v-model="activeNavTab" @tab-change="handleNavTabChange">
            <el-tab-pane label="最近更新" name="recent-updates"></el-tab-pane>
            <el-tab-pane label="最近完本" name="recent-completed"></el-tab-pane>
            <el-tab-pane label="最近入库" name="recent-added"></el-tab-pane>
          </el-tabs>
        </div>

        <!-- 标签选项：全部、玄幻等分类标签 -->
        <div class="category-tags">
          <el-tag
            v-for="category in allCategories"
            :key="category.id"
            :type="selectedCategory === category.id ? '' : 'info'"
            :effect="selectedCategory === category.id ? 'dark' : 'plain'"
            @click="selectCategory(category.id)"
            class="category-tag"
          >
            {{ category.name }}
          </el-tag>
        </div>

        <!-- 表格列名：类别、书名、章节名称、字数、作者、更新时间 -->
        <div class="table-header">
          <div class="header-cell category-col">类别</div>
          <div class="header-cell title-col">书名</div>
          <div class="header-cell chapter-col">章节名称</div>
          <div class="header-cell word-count-col">字数</div>
          <div class="header-cell author-col">作者</div>
          <div class="header-cell update-time-col">更新时间</div>
        </div>

        <!-- 表格内容 -->
        <div class="table-content">
          <div
            v-for="book in filteredBooks"
            :key="book.id"
            class="table-row"
          >
            <div class="table-cell category-col clickable-text" @click="goToCategoryByName(book.category)">{{ book.category }}</div>
            <div class="table-cell title-col clickable-text" @click="goToBook(book.id)">{{ book.title }}</div>
            <div class="table-cell chapter-col clickable-text" @click="goToChapter(book.id, book.latestChapter)">{{ book.latestChapter }}</div>
            <div class="table-cell word-count-col">{{ book.wordCount }}万字</div>
            <div class="table-cell author-col clickable-text" @click="goToSearch(book.author)">{{ book.author }}</div>
            <div class="table-cell update-time-col">{{ formatTime(book.updateTime) }}</div>
          </div>
        </div>
      </div>
    </section>
      </div>
    </div>
  </PageTransition>
</template>

<script setup name="HomePage">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useHeaderStore } from '../stores/header'
import { booksApi } from '../api/Books'
import { getImageUrl } from '../utils/imageUtils'
import { Document } from '@element-plus/icons-vue'
import PageTransition from '../components/PageTransition.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'

const router = useRouter()
const headerStore = useHeaderStore()
const activeNavTab = ref('recent-updates')
const selectedCategory = ref('all')

// 加载状态
const loading = ref(true)

// 轮播图索引
const carouselIndex = ref(0)

// 轮播图数据
const bannerList = ref([])

// 小说分类
const categories = ref([])

// 所有分类（包含"全部"选项）
const allCategories = computed(() => {
  return [
    { id: 'all', name: '全部' },
    ...categories.value
  ]
})

// 推荐小说
const recommendBooks = ref([])

// 右侧五个封面图对应的书籍（与下方十二本书分开）
const featuredBooks = ref([])

// 分类小说
const categoryBooks = ref([])

// 最新更新
const latestUpdates = ref([])

// 最近完本
const recentCompleted = ref([])

// 最新入库
const newestBooks = ref([])

// 计算属性：根据当前选中的导航标签和分类筛选书籍
const filteredBooks = computed(() => {
  let books = []

  // 根据导航标签选择数据源
  switch (activeNavTab.value) {
    case 'recent-updates':
      books = latestUpdates.value || []
      break
    case 'recent-completed':
      books = recentCompleted.value || []
      break
    case 'recent-added':
      books = newestBooks.value || []
      break
    default:
      books = latestUpdates.value || []
  }

  // 根据分类筛选
  if (selectedCategory.value !== 'all') {
    const categoryName = categories.value?.find(c => c.id === selectedCategory.value)?.name
    books = books.filter(book => book.category === categoryName)
  }

  return books
})

// 初始化分类小说数据
const initCategoryBooks = async () => {
  try {
    // 从API获取所有书籍数据
    const response = await booksApi.getAllBooks()

    // 检查响应状态码
    if (response.code !== 200) {
      throw new Error(response.message || '获取书籍数据失败')
    }

    const allBooks = response.data || []

    // 提取所有分类
    const categoryMap = new Map()
    allBooks.forEach(book => {
      if (book.categoryName && !categoryMap.has(book.categoryName)) {
        categoryMap.set(book.categoryName, {
          id: book.categoryId || book.categoryName,
          name: book.categoryName,
          icon: 'Document', // 默认图标
          count: 0
        })
      }
    })

    // 统计每个分类的书籍数量
    allBooks.forEach(book => {
      if (book.categoryName && categoryMap.has(book.categoryName)) {
        categoryMap.get(book.categoryName).count++
      }
    })

    // 更新分类数据
    categories.value = Array.from(categoryMap.values())

    // 过滤出所有recommended=1的书籍
    const recommendedBooks = allBooks.filter(book => book.recommended === 1)

    // 初始化分类小说数据
    categoryBooks.value = allBooks.slice(0, 12).map(book => ({
      id: book.bookId,
      title: book.title,
      author: book.author,
      description: book.description,
      cover: getImageUrl(book.cover),
      category: book.categoryName,
      wordCount: Math.round(book.wordCount / 10000), // 转换为万字
      viewCount: book.viewCount,
      recommendCount: book.recommendCount,
      ratingAvg: book.ratingAvg,
      ratingCount: book.ratingCount,
      status: book.status === 1 ? 'serializing' : 'completed',
      latestChapter: book.lastChapterTitle,
      updateTime: book.lastChapterTime,
      recommended: book.recommended,
      carousel: getImageUrl(book.carousel+book.title+".jpg")
    }))

    // 初始化轮播图数据：从recommended=1的书籍中随机挑选5个，使用carousel字段
    const shuffledRecommendedBooks = [...recommendedBooks].sort(() => Math.random() - 0.5)
    bannerList.value = shuffledRecommendedBooks.slice(0, 5).map(book => ({
      id: book.bookId,
      title: book.title,
      image: getImageUrl(book.carousel+'/'+book.title+".jpg"),
      needsWhiteText: true // 默认需要白色文字
    }))

    // 初始化精选书籍数据：从recommended=1的书籍中选出ratingAvg排名前五的
    const topRatedBooks = [...recommendedBooks]
      .sort((a, b) => b.ratingAvg - a.ratingAvg)
      .slice(0, 5)
    featuredBooks.value = topRatedBooks.map(book => ({
      id: book.bookId,
      title: book.title,
      author: book.author,
      description: book.description,
      cover: getImageUrl(book.cover),
      category: book.categoryName,
      wordCount: Math.round(book.wordCount / 10000), // 转换为万字
      status: book.status === 1 ? 'serializing' : 'completed',
      latestChapter: book.lastChapterTitle,
      updateTime: book.lastChapterTime
    }))

    // 初始化推荐小说数据：从recommended=1的书籍中排除上面五个精选推荐后，随机选12个
    const topRatedBookIds = new Set(topRatedBooks.map(book => book.bookId))
    const remainingRecommendedBooks = recommendedBooks.filter(book => !topRatedBookIds.has(book.bookId))
    const shuffledRemainingBooks = [...remainingRecommendedBooks].sort(() => Math.random() - 0.5)
    recommendBooks.value = shuffledRemainingBooks.slice(0, 12).map(book => ({
      id: book.bookId,
      title: book.title,
      author: book.author,
      description: book.description,
      cover: getImageUrl(book.cover),
      category: book.categoryName,
      wordCount: Math.round(book.wordCount / 10000), // 转换为万字
      status: book.status === 1 ? 'serializing' : 'completed',
      latestChapter: book.lastChapterTitle,
      updateTime: book.lastChapterTime
    }))

    // 初始化最新更新数据：展示15条
    latestUpdates.value = allBooks
      .filter(book => book.status === 1) // 状态为1表示连载中
      .slice(0, 15)
      .map(book => ({
        id: book.bookId,
        category: book.categoryName,
        title: book.title,
        latestChapter: book.lastChapterTitle || '最新章节',
        author: book.author,
        wordCount: Math.round(book.wordCount / 10000), // 转换为万字
        updateTime: book.lastChapterTime
      }))

    // 初始化最近完本数据：展示15条
    recentCompleted.value = allBooks
      .filter(book => book.status === 0) // 状态为0表示已完结
      .slice(0, 15)
      .map(book => ({
        id: book.bookId,
        category: book.categoryName,
        title: book.title,
        latestChapter: book.lastChapterTitle || '已完结',
        author: book.author,
        wordCount: Math.round(book.wordCount / 10000), // 转换为万字
        updateTime: book.lastChapterTime
      }))

    // 初始化最新入库数据：展示15条
    newestBooks.value = allBooks
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      .slice(0, 15)
      .map(book => ({
        id: book.bookId,
        category: book.categoryName,
        title: book.title,
        latestChapter: book.lastChapterTitle || '第一章',
        author: book.author,
        wordCount: Math.round(book.wordCount / 10000), // 转换为万字
        updateTime: book.createdAt
      }))

    loading.value = false
  } catch (error) {
    console.error('获取书籍数据失败:', error)
    loading.value = false

    // 如果API调用失败，使用默认数据
    categories.value = [
      { id: '1', name: '玄幻', icon: 'MagicStick', count: 12580 },
      { id: '2', name: '仙侠', icon: 'Star', count: 9876 },
      { id: '3', name: '都市', icon: 'OfficeBuilding', count: 8765 },
      { id: '4', name: '历史', icon: 'Clock', count: 6543 },
      { id: '5', name: '科幻', icon: 'Rocket', count: 4321 },
      { id: '6', name: '言情', icon: 'Heart', count: 7654 },
      { id: '7', name: '网游', icon: 'Trophy', count: 3210 },
      { id: '8', name: '悬疑', icon: 'Warning', count: 2109 }
    ]

    bannerList.value = [
      {
        id: '1',
        title: '斗破苍穹',
        image: 'https://picsum.photos/seed/book1/1920/450.jpg',
        needsWhiteText: true
      },
      {
        id: '2',
        title: '大主宰',
        image: 'https://picsum.photos/seed/book2/1920/450.jpg',
        needsWhiteText: false
      },
      {
        id: '3',
        title: '圣墟',
        image: 'https://picsum.photos/seed/book3/1920/450.jpg',
        needsWhiteText: true
      }
    ]
  }
}

// 处理导航标签切换
const handleNavTabChange = () => {
  // 导航标签切换时，可以重置分类选择
  selectedCategory.value = 'all'
}

// 处理分类选择
const selectCategory = (categoryId) => {
  selectedCategory.value = categoryId
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 跳转到书籍详情页
const goToBook = (bookId) => {
  router.push(`/book/${bookId}`)
}

// 跳转到分类页
const goToCategory = (categoryId) => {
  router.push(`/category/${categoryId}`)
}

// 跳转到搜索页面
const goToSearch = (keyword) => {
  router.push(`/search?q=${encodeURIComponent(keyword)}`)
}

// 根据分类名称跳转到分类页面
const goToCategoryByName = (categoryName) => {
  if (!categories.value || !Array.isArray(categories.value)) {
    console.warn('Categories data is not available')
    return
  }

  const category = categories.value.find(c => c.name === categoryName)
  if (category && category.id) {
    router.push(`/category/${category.id}`)
  } else {
    console.warn(`Category not found: ${categoryName}`)
  }
}

// 根据状态跳转到书库页面
const goToLibraryByStatus = (status) => {
  // 根据新的数据结构，状态可能是数字(0/1)或字符串
  let statusText = 'serializing' // 默认为连载中

  if (status === 0 || status === 'completed') {
    statusText = 'completed'
  } else if (status === 1 || status === 'serializing') {
    statusText = 'serializing'
  }

  router.push(`/library?status=${statusText}`)
}

// 处理轮播图切换
const handleCarouselChange = (index) => {
  nextTick(() => {
    const item = bannerList.value[index]
    if (item && item.needsWhiteText !== undefined) {
      headerStore.updateTextColor(item.needsWhiteText)
    }
  })
}

// 处理图片加载
const handleImageLoad = (event, item) => {
  // 如果是当前显示的轮播图，立即更新导航栏文字颜色
  if (bannerList.value[carouselIndex.value] === item) {
    headerStore.updateTextColor(item.needsWhiteText)
  }
}

// 组件挂载时初始化数据
onMounted(async () => {
  await initCategoryBooks()

  // 设置初始轮播图的文字颜色
  nextTick(() => {
    if (bannerList.value.length > 0) {
      headerStore.updateTextColor(bannerList.value[0].needsWhiteText)
    }
  })
})

// 当前显示的推荐书籍索引
const currentBookIndex = ref(0)

// 计算当前显示的推荐书籍
const currentRecommendBook = computed(() => {
  // 确保featuredBooks有数据，否则返回默认对象
  if (!featuredBooks.value || featuredBooks.value.length === 0) {
    return {
      id: 'default',
      title: '加载中...',
      author: '未知作者',
      description: '正在加载书籍信息...',
      cover: 'https://picsum.photos/seed/default/200/280.jpg',
      category: '未知',
      wordCount: 0,
      status: 'serializing'
    }
  }

  return featuredBooks.value[currentBookIndex.value] || featuredBooks.value[0]
})

// 跳转到章节详情页
const goToChapter = (bookId, chapterName) => {
  // 提取章节号，例如从"第两千一百三十四章 恐怖"中提取"2134"
  const chapterNumberMatch = chapterName.match(/第.*?章/)
  if (chapterNumberMatch) {
    const chapterTitle = chapterNumberMatch[0]
    // 在新标签页打开阅读页面
    window.open(`/read/${bookId}/${encodeURIComponent(chapterTitle)}`, '_blank')
  } else {
    // 如果没有匹配到章节格式，直接跳转到阅读页
    window.open(`/read/${bookId}`, '_blank')
  }
}

// 切换推荐书籍
const switchRecommendBook = (index) => {
  currentBookIndex.value = index
}
</script>

<style scoped>
/* 标题样式 */
.book-title-large {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.book-title {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

/* 推荐区域样式 */
.recommend-section {
  padding: 40px 0;
  background-color: #f8f9fa;
}

.recommend-layout {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.recommend-carousel {
  flex: 0 0 300px;
}

.carousel-container {
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.carousel-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  transition: transform 0.3s;
}

.carousel-container:hover .carousel-image {
  transform: scale(1.03);
}

.recommend-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.book-author {
  font-size: 16px;
  color: #666;
  margin: 10px 0;
}

.book-description {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 15px 0;
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  text-align: left;
}

.book-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.book-category, .book-word-count, .book-status {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  background-color: #f0f0f0;
  color: #666;
}

.book-covers {
  display: flex;
  gap: 10px;
}

.cover-item {
  width: 60px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  opacity: 0.7;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.cover-item.active {
  opacity: 1;
  border-color: #409eff;
}

.cover-item:hover {
  opacity: 1;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.recommend-books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.recommend-book-item {
  display: flex;
  cursor: pointer;
  transition: transform 0.2s;
  height: 160px;
}

.recommend-book-item:hover {
  transform: translateY(-5px);
}

.book-cover {
  flex: 0 0 120px;
  height: 100%;
  border-radius: 8px 0 0 8px;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.recommend-book-item:hover .cover-img {
  transform: scale(1.05);
}

.book-info {
  flex: 1;
  padding: 10px 15px;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 0 8px 8px 0;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
}

.book-name {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  transition: color 0.3s;
}

.book-name:hover {
  color: #ff4757;
}

.book-description {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
  margin: 0 0 8px;
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  text-align: left;
  position: relative;
  height: 2.8em;
  cursor: pointer;
  transition: color 0.3s;
}

.book-description:hover {
  color: #ff4757;
}

.book-description::before {
  content: "";
  display: inline-block;
  height: 100%;
  vertical-align: middle;
  width: 0;
}

.book-description span {
  display: inline-block;
  vertical-align: middle;
  width: 100%;
}

.book-author-name {
  font-size: 12px;
  color: #666;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 分类区域样式 */
.categories-section {
  padding: 40px 0;
}

.section-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  color: #333;
}

.categories-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
}

.categories-grid .category-item {
  flex: 0 0 150px; /* 固定宽度 */
  max-width: 150px; /* 确保不超过指定宽度 */
}

.category-item {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
}

.category-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.category-icon {
  margin-bottom: 10px;
  color: #409eff;
}

.category-item h3 {
  margin: 10px 0 5px;
  font-size: 16px;
}

.category-item p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

/* 小说导航区域样式 */
.novel-navigation-section {
  padding: 40px 0;
  background-color: #fff;
}

.section-title-left {
  margin-bottom: 20px;
  font-size: 20px;
  color: #333;
}

.nav-tabs {
  margin-bottom: 20px;
}

.nav-tabs :deep(.el-tabs__nav) {
  justify-content: center;
  width: 100%;
}

.nav-tabs :deep(.el-tabs__nav-scroll) {
  display: flex;
  justify-content: center;
}

.nav-tabs :deep(.el-tabs__item) {
  font-size: 18px;
  text-align: center;
  padding: 0 30px;
}

.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
  justify-content: center;
}

.category-tag {
  cursor: pointer;
}

.table-header {
  display: flex;
  background-color: #f5f7fa;
  font-weight: bold;
  border-bottom: 2px solid #e4e7ed;
}

.header-cell {
  padding: 12px 10px;
  text-align: left;
}

.category-col {
  flex: 0 0 10%;
}

.title-col {
  flex: 0 0 35%;
}

.chapter-col {
  flex: 0 0 20%;
}

.word-count-col {
  flex: 0 0 10%;
}

.author-col {
  flex: 0 0 10%;
}

.update-time-col {
  flex: 0 0 15%;
}

.table-content {
  max-height: 500px;
  overflow-y: auto;
}

.table-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.table-row:hover {
  background-color: #f5f7fa;
}

.table-cell {
  padding: 12px 10px;
  text-align: left;
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 可点击文字样式 */
.clickable-text {
  cursor: pointer;
  transition: color 0.2s;
}

.clickable-text:hover {
  color: #f56c6c;
}

/* 可点击图片样式 */
.clickable-image {
  cursor: pointer;
}

/* 轮播图区域样式 */
.banner-section {
  margin-bottom: 30px;
}

.banner-item {
  height: 100%;
  cursor: pointer;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .recommend-layout {
    flex-direction: column;
  }

  .recommend-carousel {
    flex: 0 0 auto;
    width: 100%;
    max-width: 300px;
    margin: 0 auto;
  }

  .carousel-image {
    height: 300px;
  }

  .recommend-books-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 15px;
  }

  .book-cover {
    height: 180px;
  }

  .categories-grid {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  }

  .table-header, .table-row {
    flex-direction: column;
  }

  .header-cell, .table-cell {
    padding: 8px 0;
    border-bottom: 1px solid #ebeef5;
  }

  .table-header .header-cell:last-child,
  .table-row .table-cell:last-child {
    border-bottom: none;
  }
}

@media (max-width: 480px) {
  .recommend-books-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .categories-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
