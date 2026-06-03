<template>
  <div class="ranking-page">
    <div class="container">
      <div class="ranking-layout">
        <!-- 左侧导航栏 -->
        <div class="ranking-sidebar">
          <div class="sidebar-header">
            <img src="/hot.png" alt="热门" class="hot-icon">
            <h2 class="sidebar-title">人气榜单</h2>
          </div>
          <div class="sidebar-nav">
            <div
              v-for="nav in rankingNav"
              :key="nav.name"
              class="nav-item"
              :class="{ active: activeNav === nav.name }"
              @click="switchNav(nav.name)"
            >
              <span class="nav-text">{{ nav.label }}</span>
            </div>
          </div>
        </div>

        <!-- 右侧排行榜内容 -->
        <div class="ranking-content">

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-container">
            <el-loading text="正在加载排行榜数据..." />
          </div>

          <!-- 主页面显示六个排行榜 -->
          <div v-else-if="route.path === '/ranking'" class="ranking-grids">
            <!-- 第一行 -->
            <div class="ranking-row">
              <div class="ranking-card" v-for="(card, index) in currentRankingCards.slice(0, 3)" :key="index">
                <div class="card-header" :style="card.icon ? `background-image: url(${card.icon})` : ''">
                  <div class="card-title">
                    <span v-if="!card.icon">{{ card.title }}</span>
                  </div>
                </div>
                <div class="card-content">
                  <div class="list-items">
                    <div
                      v-for="(book, bookIndex) in card.books"
                      :key="book.id"
                      class="list-item"
                      :class="{ active: bookIndex === hoveredIndices[card.type] }"
                      @mouseenter="hoveredIndices[card.type] = bookIndex"
                      @click="goToBook(book.id)"
                    >
                      <div class="item-rank" :class="getRankClass(bookIndex + 1)">
                        <img v-if="bookIndex < 3" :src="getRankIcon(bookIndex + 1)" :alt="`第${bookIndex + 1}名`" class="rank-icon">
                        <span v-else>{{ bookIndex + 1 }}</span>
                      </div>
                      <div class="item-info">
                        <div class="item-title">{{ book.title }}</div>
                        <div class="item-details">
                          <span class="item-author" @click.stop="goToAuthorSearch(book.author)">{{ book.author }}</span>
                          <span class="item-stat-detail" v-if="card.type === 'popular'" :data-type="'popular'">{{ book.clickCount || 0 }} 人气</span>
                        <span class="item-stat-detail" v-else-if="card.type === 'favorite'" :data-type="'favorite'">{{ book.favoriteCount || 0 }} 收藏</span>
                        <span class="item-stat-detail" v-else-if="card.type === 'click'" :data-type="'click'">{{ book.clickCount || 0 }} 点击</span>
                        <span class="item-stat-detail" v-else-if="card.type === 'recommend'" :data-type="'recommend'">{{ book.recommendCount || 0 }} 推荐</span>
                          <span class="item-category" v-else-if="card.type === 'new'" @click.stop="goToCategory(book.category)">{{ book.category }}</span>
                          <span class="item-category" v-else-if="card.type === 'completed'" @click.stop="goToCategory(book.category)">{{ book.category }}</span>
                        </div>
                      </div>
                      <div class="item-stat-value" v-if="card.type !== 'new' && card.type !== 'completed' && bookIndex !== hoveredIndices[card.type]">
                        <span v-if="card.type === 'popular'">{{ book.clickCount || 0 }}</span>
                        <span v-else-if="card.type === 'favorite'">{{ book.favoriteCount || 0 }}</span>
                        <span v-else-if="card.type === 'click'">{{ book.clickCount || 0 }}</span>
                        <span v-else-if="card.type === 'recommend'">{{ book.recommendCount || 0 }}</span>
                      </div>
                      <img :src="getImageUrl(book.cover)" :alt="book.title" class="item-cover" @click.stop="goToBook(book.id)">
                    </div>
                  </div>
                  <!-- 更多按钮移到右下角 -->
                  <div class="card-more-bottom" @click="goToFullRanking(card.type)">
                    更多 <el-icon><ArrowRight /></el-icon>
                  </div>
                </div>
              </div>
            </div>

            <!-- 第二行 -->
            <div class="ranking-row">
              <div class="ranking-card" v-for="(card, index) in currentRankingCards.slice(3, 6)" :key="index + 3">
                <div class="card-header" :style="card.icon ? `background-image: url(${card.icon})` : ''">
                  <div class="card-title">
                    <span v-if="!card.icon">{{ card.title }}</span>
                  </div>
                </div>
                <div class="card-content">
                  <div class="list-items">
                    <div
                      v-for="(book, bookIndex) in card.books"
                      :key="book.id"
                      class="list-item"
                      :class="{ active: bookIndex === hoveredIndices[card.type] }"
                      @mouseenter="hoveredIndices[card.type] = bookIndex"
                      @click="goToBook(book.id)"
                    >
                      <div class="item-rank" :class="getRankClass(bookIndex + 1)">
                        <img v-if="bookIndex < 3" :src="getRankIcon(bookIndex + 1)" :alt="`第${bookIndex + 1}名`" class="rank-icon">
                        <span v-else>{{ bookIndex + 1 }}</span>
                      </div>
                      <div class="item-info">
                        <div class="item-title">{{ book.title }}</div>
                        <div class="item-details">
                          <span class="item-author" @click.stop="goToAuthorSearch(book.author)">{{ book.author }}</span>
                          <span class="item-stat-detail" v-if="card.type === 'popular'" :data-type="'popular'">{{ book.clickCount || 0 }} 人气</span>
                        <span class="item-stat-detail" v-else-if="card.type === 'favorite'" :data-type="'favorite'">{{ book.favoriteCount || 0 }} 收藏</span>
                        <span class="item-stat-detail" v-else-if="card.type === 'click'" :data-type="'click'">{{ book.clickCount || 0 }} 点击</span>
                        <span class="item-stat-detail" v-else-if="card.type === 'recommend'" :data-type="'recommend'">{{ book.recommendCount || 0 }} 推荐</span>
                          <span class="item-category" v-else-if="card.type === 'new'" @click.stop="goToCategory(book.category)">{{ book.category }}</span>
                          <span class="item-category" v-else-if="card.type === 'completed'" @click.stop="goToCategory(book.category)">{{ book.category }}</span>
                        </div>
                      </div>
                      <div class="item-stat-value" v-if="card.type !== 'new' && card.type !== 'completed' && bookIndex !== hoveredIndices[card.type]">
                        <span v-if="card.type === 'popular'">{{ book.clickCount || 0 }}</span>
                        <span v-else-if="card.type === 'favorite'">{{ book.favoriteCount || 0 }}</span>
                        <span v-else-if="card.type === 'click'">{{ book.clickCount || 0 }}</span>
                        <span v-else-if="card.type === 'recommend'">{{ book.recommendCount || 0 }}</span>
                      </div>
                      <img :src="book.cover" :alt="book.title" class="item-cover" @click.stop="goToBook(book.id)">
                    </div>
                  </div>
                  <!-- 更多按钮移到右下角 -->
                  <div class="card-more-bottom" @click="goToFullRanking(card.type)">
                    更多 <el-icon><ArrowRight /></el-icon>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 子页面显示单个详细榜单 -->
          <div v-else-if="!loading" class="single-ranking">
            <div class="ranking-card full-width" v-for="(card, index) in currentRankingCards" :key="index">
              <div class="card-header" :style="card.icon ? `background-image: url(${card.icon})` : ''">
                <div class="card-title">
                  <span v-if="!card.icon">{{ card.title }}</span>
                </div>
              </div>
              <div class="card-content detailed">
                <div
                  v-for="(book, bookIndex) in card.books"
                  :key="book.id"
                  class="book-item detailed"
                >
                  <div class="book-rank" :class="getRankClass(bookIndex + 1)">
                    <img v-if="bookIndex < 3" :src="getRankIcon(bookIndex + 1)" :alt="`第${bookIndex + 1}名`" class="rank-icon">
                    <span v-else>{{ bookIndex + 1 }}</span>
                  </div>
                  <div class="book-cover" @click="goToBook(book.id)">
                    <img :src="getImageUrl(book.cover)" :alt="book.title" class="cover-image">
                  </div>
                  <div class="book-info">
                    <h4 class="book-title" @click="goToBook(book.id)">{{ book.title }}</h4>
                    <p class="book-meta">
                      <span class="book-author" @click="goToAuthorSearch(book.author)">{{ book.author }}</span>
                      <span class="book-category" @click="goToCategory(book.category)">·{{ book.category }}· </span>
                      <span class="book-status" @click="goToLibraryWithStatus(book.status)">{{ book.status === 'completed' || book.status === 2 ? '已完结' : '连载中' }}</span>
                    </p>
                    <p class="book-description" @click="goToBook(book.id)">{{ book.description }}</p>
                    <p class="book-latest">
                      <span class="latest-label">最新章节：</span>
                      <span class="latest-chapter" @click="goToReadPage(book.id, book.latestChapter || '第' + (bookIndex + 1) + '章')">{{ book.latestChapter || '第' + (bookIndex + 1) + '章' }}</span>
                      <span class="update-time">{{ book.updateTime || '2023-12-01' }}</span>
                    </p>
                  </div>
                  <div class="book-actions">
                    <div class="book-stats">
                      <span v-if="card.type === 'popular'" class="stat-item"><span class="stat-number">{{ book.clickCount || 0 }}</span> <span class="stat-label">人气</span></span>
                      <span v-if="card.type === 'favorite'" class="stat-item"><span class="stat-number">{{ book.favoriteCount || 0 }}</span> <span class="stat-label">收藏</span></span>
                      <span v-if="card.type === 'click'" class="stat-item"><span class="stat-number">{{ book.clickCount || 0 }}</span> <span class="stat-label">点击</span></span>
                      <span v-if="card.type === 'recommend'" class="stat-item"><span class="stat-number">{{ book.recommendCount || 0 }}</span> <span class="stat-label">推荐</span></span>
                    </div>
                    <div class="action-buttons">
                      <button class="detail-button" @click="goToBook(book.id)">书籍详情</button>
                      <span
                        class="add-to-shelf-text"
                        :class="{ 'in-shelf': userBookshelf.has(book.id) }"
                        @click="!userBookshelf.has(book.id) && addToShelf(book.id)"
                      >
                        {{ userBookshelf.has(book.id) ? '已在书架' : '加入书架' }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              <!-- 分页 -->
              <div class="pagination">
                <el-pagination
                  @current-change="handlePageChange"
                  :current-page="currentPage"
                  :page-size="pageSize"
                  :total="totalBooks"
                  layout="prev, pager, next"
                >
                </el-pagination>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="RankingPage">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useHeaderStore } from '@/stores/header'
import { rankingApi } from '@/api/Rankings.js'
import { getImageUrl } from '@/utils/imageUtils.js'
import { bookshelvesApi } from '@/api/Bookshelves.js'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const headerStore = useHeaderStore()
const activeNav = ref('') // 初始不选中任何导航项
const authStore = useAuthStore()

// 左侧导航栏配置
const rankingNav = ref([
  { name: 'new', label: '新书榜' },
  { name: 'popular', label: '人气榜' },
  { name: 'click', label: '点击榜' },
  { name: 'completed', label: '完结榜' },
  { name: 'recommend', label: '推荐榜' },
  { name: 'favorite', label: '收藏榜' }
])

// 排行榜数据
const newRanking = ref([])
const popularRanking = ref([])
const clickRanking = ref([])
const recommendRanking = ref([])
const completedRanking = ref([])
const favoriteRanking = ref([])

// 加载状态
const loading = ref(false)

// 分页相关变量
const currentPage = ref(1)
const pageSize = ref(20)
const totalBooks = ref(100) // 假设总共有100本书

// 鼠标悬停状态 - 默认每个榜单显示第一个列表项的悬浮效果
const hoveredIndices = ref({
  'new': 0,
  'popular': 0,
  'click': 0,
  'completed': 0,
  'recommend': 0,
  'favorite': 0
})

// 用户书架状态管理
const userBookshelf = ref(new Set()) // 用户书架中的书籍ID集合

// 获取用户书架数据
const fetchUserBookshelf = async () => {
  // 只有登录用户才获取书架数据
  if (!authStore.user?.id) {
    return
  }

  try {
    const response = await bookshelvesApi.getUserBookshelf(authStore.user.id)
    if (response && response.code === 200 && response.data) {
      // 将书籍ID添加到集合中
      userBookshelf.value.clear()
      response.data.forEach(book => {
        if (book.bookId) {
          userBookshelf.value.add(book.bookId.toString())
        }
      })
    }
  } catch (error) {
    console.error('获取用户书架数据失败:', error)
  }
}

// 获取排行榜数据
const fetchRankingData = async () => {
  loading.value = true
  try {
    const response = await rankingApi.getAllRankingList()

    if (response && response.code === 200 && response.data) {
      const rankingData = response.data

      // 检查响应数据格式
      if (typeof rankingData !== 'object') {
        throw new Error('响应数据格式错误: data不是对象')
      }

      // 映射后端数据到前端数据结构
      newRanking.value = rankingData.newRank ? transformBackendBooks(rankingData.newRank) : []
      popularRanking.value = rankingData.popularRank ? transformBackendBooks(rankingData.popularRank) : []
      clickRanking.value = rankingData.countRank ? transformBackendBooks(rankingData.countRank) : []
      completedRanking.value = rankingData.completedRank ? transformBackendBooks(rankingData.completedRank) : []
      recommendRanking.value = rankingData.recommendRank ? transformBackendBooks(rankingData.recommendRank) : []
      favoriteRanking.value = rankingData.likeRank ? transformBackendBooks(rankingData.likeRank) : []

      // 更新总书籍数
      const allBooks = [...newRanking.value, ...popularRanking.value, ...clickRanking.value,
                       ...completedRanking.value, ...recommendRanking.value, ...favoriteRanking.value]
      totalBooks.value = allBooks.length
    } else {
      const errorMsg = response?.message || '获取排行榜数据失败'
      console.warn('API返回错误:', errorMsg)
      ElMessage.warning(`${errorMsg}`)
      // 清空数据
      newRanking.value = []
      popularRanking.value = []
      clickRanking.value = []
      completedRanking.value = []
      recommendRanking.value = []
      favoriteRanking.value = []
      totalBooks.value = 0
    }
  } catch (error) {
    console.error('获取排行榜数据失败:', error)
    ElMessage.error('获取排行榜数据失败')
    // 清空数据
    newRanking.value = []
    popularRanking.value = []
    clickRanking.value = []
    completedRanking.value = []
    recommendRanking.value = []
    favoriteRanking.value = []
    totalBooks.value = 0
  } finally {
    loading.value = false
  }
}



// 转换后端Books数据到前端格式
const transformBackendBooks = (backendBooks) => {
  try {
    if (!backendBooks || !Array.isArray(backendBooks)) {
      console.warn('后端数据格式不正确:', backendBooks)
      return []
    }

    return backendBooks.map(book => {
      try {
        return {
          id: book.bookId?.toString() || '',
          title: book.title || '',
          author: book.author || '',
          category: book.categoryName || '',
          status: book.status === 2 ? 'completed' : 'serializing',
          wordCount: book.wordCount || 0,
          description: book.description || '',
          cover: getImageUrl(book.cover || ''),
          clickCount: book.viewCount || 0,
          recommendCount: book.recommendCount || 0,
          favoriteCount: book.likeCount || 0,
          ratingAvg: book.ratingAvg || 0,
          ratingCount: book.ratingCount || 0,
          lastChapterTitle: book.lastChapterTitle || '',
          updateTime: book.lastChapterTime || book.publishDate || '',
          publishDate: book.publishDate || ''
        }
      } catch (bookError) {
        console.error('转换单本书籍数据失败:', bookError, book)
        return {
          id: '',
          title: '数据异常',
          author: '未知',
          category: '未知',
          status: 'serializing',
          wordCount: 0,
          description: '书籍数据加载失败',
          cover: '',
          clickCount: 0,
          recommendCount: 0,
          favoriteCount: 0,
          ratingAvg: 0,
          ratingCount: 0,
          lastChapterTitle: '',
          updateTime: '',
          publishDate: ''
        }
      }
    })
  } catch (error) {
    console.error('转换后端书籍数据失败:', error)
    return []
  }
}

// 计算当前选中的导航对应的排行榜卡片
const currentRankingCards = computed(() => {
  let cards = []

  // 判断是否是从主导航栏进入的页面（没有路由参数）
  const isMainPage = route.path === '/ranking'

  if (isMainPage) {
    // 主页面始终显示六个排行榜，不受activeNav影响
    cards = [
      { title: '新书榜', type: 'new', icon: '/新书榜.png', books: newRanking.value.slice(0, 10) },
      { title: '人气榜', type: 'popular', icon: '/人气榜.png', books: popularRanking.value.slice(0, 10) },
      { title: '点击榜', type: 'click', icon: '/点击榜.png', books: clickRanking.value.slice(0, 10) },
      { title: '完结榜', type: 'completed', icon: '/完结榜.png', books: completedRanking.value.slice(0, 10) },
      { title: '推荐榜', type: 'recommend', icon: '/推荐榜.png', books: recommendRanking.value.slice(0, 10) },
      { title: '收藏榜', type: 'favorite', icon: '/收藏榜.png', books: favoriteRanking.value.slice(0, 10) }
    ]
  } else {
    // 子页面只显示一个详细榜单，根据分页显示数据
    const startIndex = (currentPage.value - 1) * pageSize.value
    const endIndex = startIndex + pageSize.value

    switch (activeNav.value) {
      case 'new':
        cards = [
          { title: '新书榜', type: 'new', icon: '/新书榜.png', books: newRanking.value.slice(startIndex, endIndex) }
        ]
        break
      case 'popular':
        cards = [
          { title: '人气榜', type: 'popular', icon: '/人气榜.png', books: popularRanking.value.slice(startIndex, endIndex) }
        ]
        break
      case 'click':
        cards = [
          { title: '点击榜', type: 'click', icon: '/点击榜.png', books: clickRanking.value.slice(startIndex, endIndex) }
        ]
        break
      case 'completed':
        cards = [
          { title: '完结榜', type: 'completed', icon: '/完结榜.png', books: completedRanking.value.slice(startIndex, endIndex) }
        ]
        break
      case 'recommend':
        cards = [
          { title: '推荐榜', type: 'recommend', icon: '/推荐榜.png', books: recommendRanking.value.slice(startIndex, endIndex) }
        ]
        break
      case 'favorite':
        cards = [
          { title: '收藏榜', type: 'favorite', icon: '/收藏榜.png', books: favoriteRanking.value.slice(startIndex, endIndex) }
        ]
        break
    }
  }

  return cards
})

// 切换导航
const switchNav = (navName) => {
  activeNav.value = navName
  // 重置分页为第一页
  currentPage.value = 1
  // 更新路由参数
  router.push(`/ranking/${navName}`)
}

// 获取排名样式类
const getRankClass = (rank) => {
  if (rank === 1) return 'rank-first'
  if (rank === 2) return 'rank-second'
  if (rank === 3) return 'rank-third'
  return ''
}

// 获取排名图标
const getRankIcon = (rank) => {
  if (rank === 1) return '/1.png'
  if (rank === 2) return '/2.png'
  if (rank === 3) return '/3.png'
  return ''
}

// 跳转到书籍详情页
const goToBook = (bookId) => {
  router.push(`/book/${bookId}`)
}

// 跳转到作者搜索页
const goToAuthorSearch = (author) => {
  router.push(`/search?q=${encodeURIComponent(author)}&type=author`)
}

// 跳转到分类页面并勾选对应分类
const goToCategory = (category) => {
  // 根据分类名称获取对应的分类ID
  const categoryMap = {
    '玄幻': 1,
    '仙侠': 2,
    '都市': 3,
    '历史': 4,
    '科幻': 5,
    '言情': 6,
    '网游': 7,
    '悬疑': 8
  }

  const categoryId = categoryMap[category] || 1
  router.push(`/category/${categoryId}`)
}

// 跳转到书库页面并勾选对应筛选条件
const goToLibraryWithStatus = (status) => {
  const statusText = status === 'completed' ? '已完结' : '连载中'
  router.push(`/library?status=${encodeURIComponent(statusText)}`)
}

// 跳转到阅读详细页
const goToReadPage = (bookId, chapter) => {
  // 在新标签页打开阅读页面
  window.open(`/read/${bookId}/${encodeURIComponent(chapter)}`, '_blank')
}

// 跳转到完整排行榜页面
const goToFullRanking = (type) => {
  router.push(`/ranking/${type}`)
}

// 分页处理方法
const handlePageChange = (page) => {
  currentPage.value = page
  // 这里可以添加获取分页数据的逻辑
  fetchRankingDataForPage(page)
}

// 获取分页数据
const fetchRankingDataForPage = (page) => {
  // 根据当前页码获取对应的数据
  // 这里只是示例，实际应该调用API获取数据
  console.log(`获取第${page}页数据`)
}

// 加入书架方法
const addToShelf = async (bookId) => {
  // 检查用户是否登录
  if (!authStore.user?.id) {
    ElMessageBox.confirm(
      '您还未登录，是否前往登录页面？',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      // 跳转到登录页面
      router.push('/login')
    }).catch(() => {
      // 用户取消，不做任何操作
    })
    return
  }

  try {
    // 检查书籍是否已经在书架中
    const existResponse = await bookshelvesApi.existInBookshelf(bookId, authStore.user.id)

    if (existResponse && existResponse.code === 200) {
      if (existResponse.data) {
        // 书籍已在书架中
        ElMessage({
          message: '该书籍已在书架中',
          type: 'info'
        })
        return
      }
    }

    // 添加书籍到书架
    const addResponse = await bookshelvesApi.addBookToShelves(bookId, authStore.user.id)

    if (addResponse && addResponse.code === 200) {
      // 添加到本地书架集合
      userBookshelf.value.add(bookId)
      ElMessage({
        message: '已成功加入书架',
        type: 'success'
      })
    } else {
      ElMessage({
        message: addResponse?.message || '加入书架失败',
        type: 'error'
      })
    }
  } catch (error) {
    console.error('加入书架失败:', error)
    ElMessage({
      message: '加入书架失败，请稍后重试',
      type: 'error'
    })
  }
}



// 组件挂载时获取数据和设置导航栏文字颜色
onMounted(() => {
  fetchRankingData()
  fetchUserBookshelf() // 获取用户书架数据
  // 设置导航栏文字颜色为黑色
  headerStore.updateTextColor(false)

  // 从路由参数获取初始导航状态
  const pathSegments = route.path.split('/')

  // 如果路径是 /ranking/:type，则设置对应的导航项为选中状态
  if (pathSegments.length > 2 && pathSegments[1] === 'ranking') {
    const rankingType = pathSegments[2]
    // 根据路径参数设置activeNav
    switch (rankingType) {
      case 'new':
        activeNav.value = 'new'
        break
      case 'popular':
        activeNav.value = 'popular'
        break
      case 'click':
        activeNav.value = 'click'
        break
      case 'recommend':
        activeNav.value = 'recommend'
        break
      case 'completed':
        activeNav.value = 'completed'
        break
      case 'favorite':
        activeNav.value = 'favorite'
        break
      default:
        // 默认选中新书榜
        activeNav.value = 'new'
    }
  }
})

// 监听路由变化，重置导航栏选中状态
watch(() => route.path, (newPath) => {
  // 如果返回到主页(/ranking)，则重置导航栏选中状态
  if (newPath === '/ranking') {
    activeNav.value = ''
  } else {
    // 如果是详细页面，则根据路径设置对应的导航项为选中状态
    const pathSegments = newPath.split('/')
    if (pathSegments.length > 2 && pathSegments[1] === 'ranking') {
      const rankingType = pathSegments[2]
      // 根据路径参数设置activeNav
      switch (rankingType) {
        case 'new':
          activeNav.value = 'new'
          break
        case 'popular':
          activeNav.value = 'popular'
          break
        case 'click':
          activeNav.value = 'click'
          break
        case 'recommend':
          activeNav.value = 'recommend'
          break
        case 'completed':
          activeNav.value = 'completed'
          break
        case 'favorite':
          activeNav.value = 'favorite'
          break
        default:
          // 默认选中新书榜
          activeNav.value = 'new'
      }
    }
  }
})
</script>

<style scoped>
.ranking-page {
  padding: 80px 0 20px 0; /* 顶部增加80px的padding，避免被固定导航栏遮挡 */
  min-height: 100vh; /* 修改为100vh，不再减去导航栏高度 */
  background-color: #f5f7fa;
  position: relative; /* 添加相对定位 */
  z-index: 1; /* 确保内容在导航栏下方 */
}

.ranking-layout {
  display: flex;
  gap: 20px;
}

/* 左侧导航栏样式 */
.ranking-sidebar {
  width: 180px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  height: fit-content;
}

.sidebar-header {
  display: flex;
  align-items: center;
  padding: 20px 15px; /* 增加上下内边距从12px到20px */
  background-color: #fff; /* 修改背景色为白色 */
  margin-bottom: 10px; /* 添加底部外边距，与下方列表项隔开 */
}

.hot-icon {
  width: 20px;
  height: 20px;
  margin-right: 8px;
}

.sidebar-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0;
  color: #ff0000; /* 修改文字颜色为红色 */
}

.sidebar-nav {
  padding: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 15px 15px; /* 增加上下内边距从10px到15px */
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.nav-item:hover {
  background-color: #f5f7fa;
}

.nav-item.active {
  background-color: #f0f9ff;
  border-left-color: #409eff;
  color: #409eff;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
}

/* 右侧内容区域样式 */
.ranking-content {
  flex: 1;
}

.ranking-grids {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ranking-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.ranking-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  /* 移除所有过渡效果 */
}

/* 完全移除悬停效果 */
.ranking-card:hover {
  /* 不应用任何变换和阴影效果 */
  transform: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  height: 60px;
  background-size: cover;
  background-position: center 30%; /* 将图片位置向下移动30% */
  background-repeat: no-repeat;
}

.card-title {
  display: flex;
  align-items: center;
  font-size: 20px; /* 增大字体大小 */
  font-weight: bold;
  margin: 0;
}

.card-title-icon {
  height: 28px; /* 增大图标尺寸 */
  width: 28px; /* 添加宽度 */
  margin-right: 10px; /* 增加右边距 */
}

.card-more {
  display: flex;
  align-items: center;
  font-size: 13px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.card-more:hover {
  opacity: 0.8;
}

.card-content {
  padding: 12px 15px;
  min-height: 580px; /* 增加最小高度，确保容器高度固定 */
}

.book-item {
  display: flex;
  align-items: center;
  padding: 12px 0; /* 增加上下内边距从8px到12px */
  cursor: pointer;
  transition: background-color 0.3s;
  /* 移除底部边框 */
}

.book-item:last-child {
  border-bottom: none;
}

.book-rank {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 36px;
  height: 36px;
  margin-right: 12px;
  font-size: 18px;
  font-weight: bold;
  color: #666;
  background-color: #f5f5f5;
  border-radius: 50%;
}

.rank-icon {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.rank-first,
.rank-second,
.rank-third {
  background-color: transparent;
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 4px 0;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-meta {
  display: flex;
  font-size: 12px;
  color: #666;
  margin: 0;
}

.book-author {
  margin-right: 8px;
}

.book-category {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .ranking-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  .ranking-layout {
    flex-direction: column;
  }

  .ranking-sidebar {
    width: 100%;
  }

  .ranking-row {
    grid-template-columns: 1fr;
  }
}

/* 单个详细榜单样式 */
.single-ranking {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.full-width {
  width: 100%;
}

.book-item.detailed {
  display: flex;
  align-items: center;
  padding: 20px;
  /* 移除底部边框 */
}

.book-cover {
  width: 120px;
  height: 160px;
  margin-right: 20px;
  flex-shrink: 0;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.book-info {
  flex: 1;
}

.book-item.detailed .book-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
  white-space: normal;
  overflow: visible;
  text-overflow: unset;
}

.book-item.detailed .book-author,
.book-item.detailed .book-category,
.book-item.detailed .book-status,
.book-item.detailed .book-wordcount {
  font-size: 14px;
  margin-bottom: 5px;
  color: #666;
}

.book-item.detailed .book-description {
  font-size: 14px;
  color: #666;
  margin: 10px 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-stats {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.click-count,
.recommend-count {
  font-size: 14px;
  color: #666;
}

.card-content.detailed {
  padding: 0;
}

/* 书籍操作区域样式 */
.book-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin-left: 20px;
  min-width: 180px;
}

.book-stats {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.stat-item {
  font-size: 14px;
  color: #666;
}

.stat-number {
  color: #ff0000;
  font-size: 16px;
  font-weight: bold;
  font-stretch: condensed;
  letter-spacing: -0.5px;
}

.stat-label {
  color: #999;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
}

.detail-button {
  background-color: #ff4757;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
  box-shadow: 0 2px 4px rgba(255, 71, 87, 0.2);
  width: 120px;
  text-align: center;
}

.detail-button:hover {
  background-color: #ff3838;
}

.add-to-shelf-text {
  color: #999;
  font-size: 12px;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.3s;
}

.add-to-shelf-text:hover {
  color: #409eff;
}

/* 最新章节样式 */
.book-latest {
  font-size: 14px;
  color: #666;
  margin-top: 10px;
}

.latest-label {
  font-weight: bold;
}

.latest-chapter {
  color: #409eff;
  margin-right: 10px;
}

.update-time {
  color: #999;
  font-size: 12px;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 更多按钮样式 - 右下角灰色小字 */
.card-more-bottom {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-top: 10px;
  padding-top: 8px;
  font-size: 12px;
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
}

.card-more-bottom:hover {
  color: #409eff;
}

.card-more-bottom .el-icon {
  margin-left: 4px;
  font-size: 12px;
}

/* 详细页面书籍列表项的悬浮高亮效果 */
.book-item.detailed .book-title:hover {
  color: #ff0000 !important;
}

/* TOP10强推榜样式 */
.list-items {
  display: flex;
  flex-direction: column;
  padding: 10px;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  height: auto; /* 移除max-height限制，完整显示所有项目 */
  overflow-y: visible; /* 移除滚动条 */
  min-height: 500px; /* 增加最小高度，确保容器高度固定 */
}

.list-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  height: 40px;
  transition: all 0.3s ease;
  cursor: pointer;
  border-radius: 4px;
  position: relative;
  padding-right: 80px; /* 为右侧的封面或统计数据预留空间 */
  margin-bottom: 5px; /* 增加底部间距，为悬停时的高度增加预留更多空间 */
}

.list-item:hover,
.list-item.active {
  background-color: rgba(64, 158, 255, 0.1);
  height: auto;
  transform: translateX(5px);
  margin-bottom: 5px; /* 保持相同的底部间距 */
  position: relative;
  z-index: 10; /* 确保悬停的列表项在其他元素之上 */
}

.item-rank {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #f0f0f0;
  font-size: 12px;
  font-weight: bold;
  color: #666;
  margin-right: 10px;
  flex-shrink: 0;
}

.item-rank.rank-1 {
  background-image: url('/1.png');
  background-size: contain;
  background-repeat: no-repeat;
  background-color: transparent;
}

.item-rank.rank-2 {
  background-image: url('/2.png');
  background-size: contain;
  background-repeat: no-repeat;
  background-color: transparent;
}

.item-rank.rank-3 {
  background-image: url('/3.png');
  background-size: contain;
  background-repeat: no-repeat;
  background-color: transparent;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.list-item:hover .item-info,
.list-item.active .item-info {
  align-items: flex-start;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  cursor: pointer;
  transition: color 0.3s ease;
}

.item-title:hover {
  color: #ff0000;
}

.list-item:hover .item-title,
.list-item.active .item-title {
  white-space: normal;
  overflow: visible;
  text-overflow: unset;
}

.item-details {
  display: none;
  font-size: 12px;
  color: #666;
  margin-top: 4px;
  width: 100%;
}

.list-item:hover .item-details,
.list-item.active .item-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-detail-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-author {
  color: #666;
  cursor: pointer;
  transition: color 0.3s ease;
}

.item-author:hover {
  color: #ff0000;
}

.item-category {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  transition: color 0.3s ease;
}

.item-category:hover {
  color: #ff0000;
}


.item-cover {
  display: none;
  width: 60px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  transition: transform 0.3s ease;
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 20; /* 确保封面在统计数据之上 */
}

.list-item:hover .item-cover,
.list-item.active .item-cover {
  display: block;
  transform: translateY(-50%);
}

.item-cover:hover {
  transform: translateY(-50%) scale(1.05); /* 悬停时微微放大 */
}

.item-stat {
  display: flex;
  align-items: center;
  justify-content: center; /* 水平居中 */
  gap: 5px;
  font-size: 12px;
  color: #999;
  position: absolute;
  right: 10px;
  top: 50%; /* 垂直居中 */
  transform: translateY(-50%); /* 垂直居中 */
  z-index: 5; /* 确保统计数据在封面之下 */
}

.item-stat-value {
  color: #999;
  font-size: 11px;
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 5; /* 确保统计数据在封面之下 */
}

.list-item:hover .item-stat-value,
.list-item.active .item-stat-value {
  opacity: 0; /* 悬浮时隐藏数值 */
  visibility: hidden; /* 悬浮时隐藏数值 */
}

.item-stat-detail {
  font-size: 12px;
  margin-top: 2px;
}

/* 不同榜单数据使用不同颜色 */
.item-stat-detail[data-type="popular"] {
  color: #ff6b6b; /* 人气榜 - 红色 */
}

.item-stat-detail[data-type="favorite"] {
  color: #4ecdc4; /* 收藏榜 - 青色 */
}

.item-stat-detail[data-type="click"] {
  color: #45b7d1; /* 点击榜 - 蓝色 */
}

.item-stat-detail[data-type="recommend"] {
  color: #f9ca24; /* 推荐榜 - 黄色 */
}

.book-item.detailed .book-author:hover {
  color: #ff0000 !important;
}

.book-item.detailed .book-category:hover {
  color: #ff0000 !important;
}

.book-item.detailed .book-status:hover {
  color: #ff0000 !important;
}

.book-item.detailed .book-description:hover {
  color: #ff0000 !important;
}

.book-item.detailed .latest-chapter:hover {
  color: #ff0000 !important;
}

/* 添加鼠标指针样式 */
.book-item.detailed .book-title,
.book-item.detailed .book-author,
.book-item.detailed .book-category,
.book-item.detailed .book-status,
.book-item.detailed .book-description,
.book-item.detailed .latest-chapter,
.book-item.detailed .book-cover {
  cursor: pointer;
}

/* 加载状态样式 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  width: 100%;
}

/* 已在书架状态样式 */
.add-to-shelf-text.in-shelf {
  color: #999;
  cursor: not-allowed;
  opacity: 0.6;
}

.add-to-shelf-text.in-shelf:hover {
  color: #999;
  background-color: transparent;
}
</style>
