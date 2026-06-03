<template>
  <div class="category-page">
    <!-- 顶部图片 -->
    <section class="category-banner">
      <img :src="bannerImage" alt="Category Banner" class="banner-image">
    </section>

    <div class="container">
      <!-- 分类列表 -->
      <div class="category-wrapper">
        <div class="category-grid">
          <div
                v-for="category in visibleCategories"
                :key="category.id"
                class="category-card"
                :class="{ active: selectedCategory && selectedCategory.id === category.id }"
                @click="goToCategory(category.id)"
              >
                <div class="category-icon">
                  <el-icon :size="24">
                    <Document />
                  </el-icon>
                </div>
                <h3 class="category-name">{{ category.name }}</h3>
              </div>

          <!-- 更多按钮 -->
          <div
            v-if="hasMoreCategories"
            class="category-card more-button"
            @mouseenter="showMoreMenu = true"
            @mouseleave="showMoreMenu = false"
          >
            <div class="category-icon">
              <el-icon :size="24">
                <MoreFilled />
              </el-icon>
            </div>
            <h3 class="category-name">更多</h3>

            <!-- 下拉菜单 -->
            <div v-show="showMoreMenu" class="more-dropdown">
              <div
                v-for="category in hiddenCategories"
                :key="category.id"
                class="dropdown-item"
                :class="{ active: selectedCategory && selectedCategory.id === category.id }"
                @click="goToCategory(category.id)"
              >
                <div class="dropdown-icon">
                  <el-icon :size="20">
                    <Document />
                  </el-icon>
                </div>
                <span class="dropdown-name">{{ category.name }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分类列表分隔线 -->
      <div class="category-divider"></div>

      <!-- 第一个section：编辑强推 -->
      <section class="content-section editor-recommend">
        <div class="section-left">
          <h3 class="section-title">编辑强推</h3>
          <div class="featured-book">
            <div class="featured-cover" @click="goToBookDetail(featuredBooks[currentFeaturedIndex]?.id)" v-if="featuredBooks.length > 0">
                  <img :src="getImageUrl(featuredBooks[currentFeaturedIndex]?.cover)" :alt="featuredBooks[currentFeaturedIndex]?.title">
                </div>
            <div class="featured-info" v-if="featuredBooks.length > 0">
              <h3 class="featured-title" @click="goToBookDetail(featuredBooks[currentFeaturedIndex]?.id)">{{ featuredBooks[currentFeaturedIndex]?.title }}</h3>
              <div class="featured-meta">
                <span class="featured-author-label">作者：</span>
                <span class="featured-author" @click="goToAuthorSearch(featuredBooks[currentFeaturedIndex]?.author)">{{ featuredBooks[currentFeaturedIndex]?.author }}</span>
                <span class="featured-category-label">类型：</span>
                <span class="featured-category" @click="goToCategoryFilter(featuredBooks[currentFeaturedIndex]?.categoryName)">{{ featuredBooks[currentFeaturedIndex]?.categoryName }}</span>
              </div>
              <div class="featured-desc-wrapper">
                <p class="featured-desc" @click="goToBookDetail(featuredBooks[currentFeaturedIndex]?.id)">{{ featuredBooks[currentFeaturedIndex]?.description }}</p>
              </div>
              <div class="featured-latest" @click="goToChapterRead(featuredBooks[currentFeaturedIndex]?.id, featuredBooks[currentFeaturedIndex]?.chapterCount)">最新章节：{{ featuredBooks[currentFeaturedIndex]?.lastChapterTitle }}</div>
              <div class="featured-update">更新时间：{{ featuredBooks[currentFeaturedIndex]?.lastChapterTime }}</div>
            </div>
            <div class="featured-info" v-else>
              <h3 class="featured-title">加载中...</h3>
              <div class="featured-meta">
                <span class="featured-author-label">作者：</span>
                <span class="featured-author">-</span>
                <span class="featured-category-label">类型：</span>
                <span class="featured-category">-</span>
              </div>
              <div class="featured-desc-wrapper">
                <p class="featured-desc">正在加载书籍数据...</p>
              </div>
              <div class="featured-latest">最新章节：-</div>
              <div class="featured-update">更新时间：-</div>
            </div>
            <div class="book-carousel" v-if="featuredBooks.length > 0">
              <div
                v-for="(book, index) in featuredBooks"
                :key="book.id"
                class="carousel-item"
                :class="{ active: index === currentFeaturedIndex }"
                @mouseenter="currentFeaturedIndex = index"
                @click="currentFeaturedIndex = index"
              >
                <img :src="getImageUrl(book.cover)" :alt="book.title">
              </div>
            </div>
          </div>
        </div>
        <div class="section-right">
          <div class="top-list" @mouseleave="handleRecommendListLeave">
            <h3 class="list-title recommend-title">TOP10 强推榜</h3>
            <div class="list-items">
              <div
                v-for="(book, index) in topRecommendBooks"
                :key="book.id"
                class="list-item"
                :class="{ active: index === hoveredRecommendIndex }"
                @mouseenter="hoveredRecommendIndex = index"
              >
                <div class="item-rank">{{ index + 1 }}</div>
                <div class="item-info">
                  <div class="item-title" @click="goToBookDetail(book.id)">{{ book.title }}</div>
                  <div class="item-details" v-show="index === hoveredRecommendIndex">
                    <span class="item-author" @click="goToAuthorSearch(book.author)">{{ book.author }}</span>
                    <span class="item-category" @click="goToCategoryFilter(book.categoryName)">{{ book.categoryName }}</span>
                    <img :src="getImageUrl(book.cover)" :alt="book.title" class="item-cover" @click="goToBookDetail(book.id)">
                  </div>
                </div>
                <!-- <div class="item-recommend-value" v-show="index !== hoveredRecommendIndex">{{ formatRecommendCount(book.recommends || 0) }}</div> -->
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 第二个section：新书精选 -->
      <section class="content-section new-books">
        <div class="section-left">
          <h3 class="section-title">新书精选</h3>
          <div class="book-grid">
            <div
              v-for="book in newBooks"
              :key="book.id"
              class="grid-item"
            >
              <div class="grid-cover" @click="goToBookDetail(book.id)">
                <img :src="getImageUrl(book.cover)" :alt="book.title">
              </div>
              <div class="grid-info">
                <h4 class="grid-title" @click="goToBookDetail(book.id)">{{ book.title }}</h4>
                <p class="grid-desc" @click="goToBookDetail(book.id)">{{ book.description }}</p>
                <div class="grid-meta">
                  <span class="grid-author" @click="goToAuthorSearch(book.author)">{{ book.author }}</span> · <span class="grid-category" @click="goToCategoryFilter(book.categoryName)">{{ book.categoryName }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="section-right">
          <div class="top-list" @mouseleave="handleNewListLeave">
            <h3 class="list-title new-title" @click="goToRanking('new')">TOP10 新书榜</h3>
            <div class="list-items">
              <div
                v-for="(book, index) in topNewBooks"
                :key="book.id"
                class="list-item"
                :class="{ active: index === hoveredNewIndex }"
                @mouseenter="hoveredNewIndex = index"
              >
                <div class="item-rank">{{ index + 1 }}</div>
                <div class="item-info">
                  <div class="item-title" @click="goToBookDetail(book.id)">{{ book.title }}</div>
                  <div class="item-details" v-show="index === hoveredNewIndex">
                    <span class="item-author" @click="goToAuthorSearch(book.author)">{{ book.author }}</span>
                    <span class="item-popularity"><span>{{ formatPopularityCount(book.viewCount) }}</span> 人气</span>
                    <img :src="getImageUrl(book.cover)" :alt="book.title" class="item-cover" @click="goToBookDetail(book.id)">
                  </div>
                </div>
                <div class="item-popularity-value" v-show="index !== hoveredNewIndex">{{ formatPopularityCount(book.viewCount) }}</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 第三个section：人气作品 -->
      <section class="content-section popular-books">
        <div class="section-left">
          <h3 class="section-title">人气作品</h3>
          <div class="popular-layout">
            <div class="popular-grid">
              <!-- 左侧轮播区域 -->
              <div class="featured-carousel-vertical">
                <div class="carousel-container-vertical">
                  <div class="carousel-items">
                    <div
                      v-for="(book, index) in popularFeaturedBooks"
                      :key="book.id"
                      class="carousel-item-vertical"
                      :class="{
                        active: index === currentPopularIndex,
                        prev: popularFeaturedBooks.length > 0 && index === (currentPopularIndex - 1 + popularFeaturedBooks.length) % popularFeaturedBooks.length,
                        next: popularFeaturedBooks.length > 0 && index === (currentPopularIndex + 1) % popularFeaturedBooks.length
                      }"
                      @click="handlePopularCarouselClick(index)"
                    >
                      <img :src="getImageUrl(book.cover)" :alt="book.title" class="carousel-cover-vertical">
                    </div>
                  </div>
                </div>
                <div class="carousel-info-vertical" v-if="currentPopularBook">
                  <h4 class="carousel-title-vertical" @click.stop="goToBookDetail(currentPopularBook.id)">{{ currentPopularBook.title }}</h4>
                  <p class="carousel-author-vertical" @click.stop="goToAuthorSearch(currentPopularBook.author)">{{ currentPopularBook.author }}</p>
                  <p class="carousel-desc-vertical" @click.stop="goToBookDetail(currentPopularBook.id)">{{ currentPopularBook.description }}</p>
                  <button class="detail-btn-vertical" @click.stop="goToBookDetail(currentPopularBook.id)">书籍详情</button>
                </div>
                <div class="carousel-info-vertical" v-else>
                  <h4 class="carousel-title-vertical">加载中...</h4>
                  <p class="carousel-author-vertical">-</p>
                  <p class="carousel-desc-vertical">正在加载书籍数据...</p>
                  <button class="detail-btn-vertical" disabled>书籍详情</button>
                </div>
              </div>
              <!-- 右侧六宫格 -->
              <div
                v-for="book in popularGridBooks"
                :key="book.id"
                class="grid-item"
              >
                <div class="grid-cover" @click="goToBookDetail(book.id)">
                  <img :src="getImageUrl(book.cover)" :alt="book.title">
                </div>
                <div class="grid-info">
                  <h4 class="grid-title" @click="goToBookDetail(book.id)">{{ book.title }}</h4>
                  <p class="grid-desc" @click="goToBookDetail(book.id)">{{ book.description }}</p>
                  <div class="grid-meta">
                    <span class="grid-author" @click="goToAuthorSearch(book.author)">{{ book.author }}</span> · <span class="grid-category" @click="goToCategoryFilter(book.category)">{{ book.category }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="section-right">
          <div class="top-list" @mouseleave="handleClickListLeave">
            <h3 class="list-title click-title" @click="goToRanking('click')">TOP 点击榜</h3>
            <div class="list-items">
              <div
                v-for="(book, index) in topClickBooks"
                :key="book.id"
                class="list-item"
                :class="{ active: index === hoveredClickIndex }"
                @mouseenter="hoveredClickIndex = index"
              >
                <div class="item-rank">{{ index + 1 }}</div>
                <div class="item-info">
                  <div class="item-title" @click="goToBookDetail(book.id)">{{ book.title }}</div>
                  <div class="item-details" v-show="index === hoveredClickIndex">
                    <span class="item-author" @click="goToAuthorSearch(book.author)">{{ book.author }}</span>
                    <span class="item-click"><span>{{ formatClickCount(book.viewCount) }}</span> 点击</span>
                    <img :src="getImageUrl(book.cover)" :alt="book.title" class="item-cover" @click="goToBookDetail(book.id)">
                  </div>
                </div>
                <div class="item-click-value" v-show="index !== hoveredClickIndex">{{ formatClickCount(book.viewCount) }}</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 第四个section：经典完本 -->
      <section class="content-section completed-books">
        <div class="section-left">
          <h3 class="section-title">经典完本</h3>
          <div class="popular-layout">
            <div class="popular-grid">
              <!-- 左侧轮播区域 -->
              <div class="featured-carousel-vertical">
                <div class="carousel-container-vertical">
                  <div class="carousel-items">
                    <div
                      v-for="(book, index) in completedFeaturedBooks"
                      :key="book.id"
                      class="carousel-item-vertical"
                      :class="{
                        active: index === currentCompletedIndex,
                        prev: completedFeaturedBooks.length > 0 && index === (currentCompletedIndex - 1 + completedFeaturedBooks.length) % completedFeaturedBooks.length,
                        next: completedFeaturedBooks.length > 0 && index === (currentCompletedIndex + 1) % completedFeaturedBooks.length
                      }"
                      @click="handleCompletedCarouselClick(index)"
                    >
                      <img :src="getImageUrl(book.cover)" :alt="book.title" class="carousel-cover-vertical">
                    </div>
                  </div>
                </div>
                <div class="carousel-info-vertical" v-if="currentCompletedBook">
                  <h4 class="carousel-title-vertical" @click.stop="goToBookDetail(currentCompletedBook.id)">{{ currentCompletedBook.title }}</h4>
                  <p class="carousel-author-vertical" @click.stop="goToAuthorSearch(currentCompletedBook.author)">{{ currentCompletedBook.author }}</p>
                  <p class="carousel-desc-vertical" @click.stop="goToBookDetail(currentCompletedBook.id)">{{ currentCompletedBook.description }}</p>
                  <button class="detail-btn-vertical" @click.stop="goToBookDetail(currentCompletedBook.id)">书籍详情</button>
                </div>
                <div class="carousel-info-vertical" v-else>
                  <h4 class="carousel-title-vertical">加载中...</h4>
                  <p class="carousel-author-vertical">-</p>
                  <p class="carousel-desc-vertical">正在加载书籍数据...</p>
                  <button class="detail-btn-vertical" disabled>书籍详情</button>
                </div>
              </div>
              <!-- 右侧六宫格 -->
              <div
                v-for="book in completedGridBooks"
                :key="book.id"
                class="grid-item"
              >
                <div class="grid-cover" @click="goToBookDetail(book.id)">
                  <img :src="getImageUrl(book.cover)" :alt="book.title">
                </div>
                <div class="grid-info">
                  <h4 class="grid-title" @click="goToBookDetail(book.id)">{{ book.title }}</h4>
                  <p class="grid-desc" @click="goToBookDetail(book.id)">{{ book.description }}</p>
                  <div class="grid-meta">
                    <span class="grid-author" @click="goToAuthorSearch(book.author)">{{ book.author }}</span> · <span class="grid-category" @click="goToCategoryFilter(book.category)">{{ book.category }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="section-right">
          <div class="top-list" @mouseleave="handleRecommend2ListLeave">
            <h3 class="list-title recommend2-title" @click="goToRanking('recommend')">TOP10 推荐榜</h3>
            <div class="list-items">
              <div
                v-for="(book, index) in topRecommendBooks2"
                :key="book.id"
                class="list-item"
                :class="{ active: index === hoveredRecommend2Index }"
                @mouseenter="hoveredRecommend2Index = index"
              >
                <div class="item-rank">{{ index + 1 }}</div>
                <div class="item-info">
                  <div class="item-title" @click="goToBookDetail(book.id)">{{ book.title }}</div>
                  <div class="item-details" v-show="index === hoveredRecommend2Index">
                    <span class="item-author" @click="goToAuthorSearch(book.author)">{{ book.author }}</span>
                    <span class="item-recommend"><span>{{ formatRecommendCount(book.recommendCount) }}</span> 推荐</span>
                    <img :src="getImageUrl(book.cover)" :alt="book.title" class="item-cover" @click="goToBookDetail(book.id)">
                  </div>
                </div>
                <div class="item-recommend-value" v-show="index !== hoveredRecommend2Index">{{ formatRecommendCount(book.recommendCount) }}</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 第五个section：连载推荐 -->
      <section class="content-section serializing-books">
        <div class="section-left">
          <h3 class="section-title">连载推荐</h3>
          <div class="book-grid">
            <div
              v-for="book in serializingBooks"
              :key="book.id"
              class="grid-item"
            >
              <div class="grid-cover" @click="goToBookDetail(book.id)">
                <img :src="getImageUrl(book.cover)" :alt="book.title">
              </div>
              <div class="grid-info">
                <h4 class="grid-title" @click="goToBookDetail(book.id)">{{ book.title }}</h4>
                <p class="grid-desc" @click="goToBookDetail(book.id)">{{ book.description }}</p>
                <div class="grid-meta">
                  <span class="grid-author" @click="goToAuthorSearch(book.author)">{{ book.author }}</span> · <span class="grid-category" @click="goToCategoryFilter(book.category)">{{ book.category }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="section-right">
          <div class="top-list" @mouseleave="handleFavoriteListLeave">
            <h3 class="list-title favorite-title" @click="goToRanking('favorite')">TOP10 收藏榜</h3>
            <div class="list-items">
              <div
                v-for="(book, index) in topFavoriteBooks"
                :key="book.id"
                class="list-item"
                :class="{ active: index === hoveredFavoriteIndex }"
                @mouseenter="hoveredFavoriteIndex = index"
              >
                <div class="item-rank">{{ index + 1 }}</div>
                <div class="item-info">
                  <div class="item-title" @click="goToBookDetail(book.id)">{{ book.title }}</div>
                  <div class="item-details" v-show="index === hoveredFavoriteIndex">
                    <span class="item-author" @click="goToAuthorSearch(book.author)">{{ book.author }}</span>
                    <span class="item-favorite"><span>{{ formatFavoriteCount(book.likeCount) }}</span> 收藏</span>
                    <img :src="getImageUrl(book.cover)" :alt="book.title" class="item-cover" @click="goToBookDetail(book.id)">
                  </div>
                </div>
                <div class="item-favorite-value" v-show="index !== hoveredFavoriteIndex">{{ formatFavoriteCount(book.likeCount) }}</div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MoreFilled, Document } from '@element-plus/icons-vue'
import { categoryApi } from '@/api/Category'
import { booksApi } from '@/api/Books'
import { rankingApi } from '@/api/Rankings'
import { getImageUrl } from '@/utils/imageUtils'

// 导入外部数据文件
// 移除模拟数据导入，使用空数组初始化

const router = useRouter()
const route = useRoute()

// 直接使用后端数据，移除多余的转换函数

// 创建填充书籍
const createPlaceholderBook = (categoryId, categoryName) => {
  const placeholder = {
    id: `placeholder-${categoryId}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    bookId: `placeholder-${categoryId}-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    title: '更多精彩小说即将上线',
    author: '系统推荐',
    cover: 'https://picsum.photos/seed/user/40/40.jpg',
    description: '该分类下暂无更多书籍，敬请期待更多优质作品',
    categoryName: categoryName,
    categoryId: categoryId,
    status: 0,
    wordCount: 0,
    chapterCount: 0,
    viewCount: 0,
    recommendCount: 0,
    likeCount: 0,
    ratingAvg: 0,
    lastChapterTitle: '敬请期待',
    lastChapterTime: new Date().toISOString(),
    recommended: 0,
    publishDate: new Date().toISOString(),
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
  return placeholder
}

// 顶部横幅图片
const bannerImage = ref('https://picsum.photos/seed/category-banner/1920/300.jpg')

// 分类数据
const categories = ref([])

// 当前选中的分类
const selectedCategory = ref(null)

// 分类导航相关状态
const showMoreMenu = ref(false)
const maxVisibleCategories = ref(8) // 最多显示8个分类

// 响应式计算可见分类数量
const updateVisibleCategories = () => {
  const screenWidth = window.innerWidth
  if (screenWidth >= 1200) {
    maxVisibleCategories.value = 8
  } else if (screenWidth >= 992) {
    maxVisibleCategories.value = 6
  } else if (screenWidth >= 768) {
    maxVisibleCategories.value = 4
  } else {
    maxVisibleCategories.value = 3
  }
}

// 组件挂载时检查路由参数
const originalOnMounted = () => {
  const categoryId = route.params.categoryId
  if (categoryId) {
    // 使用setTimeout确保在watch之后执行，避免被覆盖
    setTimeout(() => {
      updateSelectedCategory(categoryId)
    }, 0)
  }

  // 启动轮播
  startFeaturedCarousel()
  startPopularCarousel()
  startCompletedCarousel()
}

// 组件卸载时清除轮播
const originalOnUnmounted = () => {
  stopAllCarousels()
}

// 获取分类数据
const fetchCategories = async () => {
  try {
    const response = await categoryApi.getCategoryList()
    if (response.code === 200) {
      // 筛选出status=1（可用）的分类，并按sortOrder排序
      categories.value = (response.data || [])
        .filter(category => category.status === 1)
        .sort((a, b) => a.sortOrder - b.sortOrder)
    }
  } catch{
    // 如果API调用失败，使用默认分类数据
    categories.value = [
      { id: '1', name: '玄幻', icon: 'MagicStick', count: 12580, status: 1, sortOrder: 1 },
      { id: '2', name: '仙侠', icon: 'Star', count: 9876, status: 1, sortOrder: 2 },
      { id: '3', name: '都市', icon: 'OfficeBuilding', count: 8765, status: 1, sortOrder: 3 },
      { id: '4', name: '历史', icon: 'Clock', count: 6543, status: 1, sortOrder: 4 },
      { id: '5', name: '科幻', icon: 'Star', count: 4321, status: 1, sortOrder: 5 },
      { id: '6', name: '言情', icon: 'Star', count: 7654, status: 1, sortOrder: 6 },
      { id: '7', name: '网游', icon: 'Trophy', count: 3210, status: 1, sortOrder: 7 },
      { id: '8', name: '悬疑', icon: 'Warning', count: 2109, status: 1, sortOrder: 8 },
      { id: '9', name: '军事', icon: 'Warning', count: 1987, status: 1, sortOrder: 9 },
      { id: '10', name: '武侠', icon: 'Star', count: 1765, status: 1, sortOrder: 10 },
      { id: '11', name: '奇幻', icon: 'MagicStick', count: 1543, status: 1, sortOrder: 11 },
      { id: '12', name: '竞技', icon: 'Trophy', count: 1321, status: 1, sortOrder: 12 }
    ]
  }
}

// 根据分类更新顶部横幅图片
const updateBannerImage = (category) => {
  if (category && category.icon) {
    // 使用分类的icon字段作为封面路径，通过getImageUrl处理
    bannerImage.value = getImageUrl(category.icon)
  } else {
    // 默认图片
    bannerImage.value = 'https://picsum.photos/seed/category-banner/1920/300.jpg'
  }
}

// 组件挂载时初始化认证状态
onMounted(() => {
  updateVisibleCategories()
  window.addEventListener('resize', updateVisibleCategories)
  fetchCategories()
  originalOnMounted()
})

// 组件卸载时移除监听器
onUnmounted(() => {
  window.removeEventListener('resize', updateVisibleCategories)
  originalOnUnmounted()
})

// 计算属性：可见的分类
const visibleCategories = computed(() => {
  return categories.value.slice(0, maxVisibleCategories.value)
})

// 计算属性：隐藏的分类
const hiddenCategories = computed(() => {
  return categories.value.slice(maxVisibleCategories.value)
})

// 计算属性：是否有更多分类
const hasMoreCategories = computed(() => {
  return categories.value.length > maxVisibleCategories.value
})

// 分类下的小说列表
const categoryBooks = ref([])

// 编辑强推相关数据
const currentFeaturedIndex = ref(0)
const featuredBooks = ref([])

const hoveredRecommendIndex = ref(0)
const topRecommendBooks = ref([])

// 新书精选相关数据
const hoveredNewIndex = ref(0)
const newBooks = ref([])

const topNewBooks = ref([])

// 人气作品相关数据
const hoveredClickIndex = ref(0)
const currentPopularIndex = ref(0)
const popularFeaturedBooks = ref([])

const currentPopularBook = computed(() =>
  popularFeaturedBooks.value.length > 0 ? popularFeaturedBooks.value[currentPopularIndex.value] : null
)

const popularGridBooks = ref([])

const topClickBooks = ref([])

// 经典完本相关数据
const hoveredRecommend2Index = ref(0)
const currentCompletedIndex = ref(0)
const completedFeaturedBooks = ref([])

const currentCompletedBook = computed(() =>
  completedFeaturedBooks.value.length > 0 ? completedFeaturedBooks.value[currentCompletedIndex.value] : null
)

const completedGridBooks = ref([])

const topRecommendBooks2 = ref([])

// 连载推荐相关数据
const hoveredFavoriteIndex = ref(0)
const serializingBooks = ref([])

const topFavoriteBooks = ref([])

// 获取编辑强推和强推榜数据
const fetchEditorRecommendedBooks = async (categoryId, categoryName) => {
  try {
    const response = await booksApi.getBooksByCategory(categoryId)

    if (response.code==200&&response.data) {
      // 直接使用后端数据，移除转换步骤
      const allBooks = response.data

      // 筛选recommended=1且按lastChapterTime从新到旧排序
      let recommendedBooks = allBooks
        .filter(book => book.recommended === 1)
        .sort((a, b) => new Date(b.lastChapterTime) - new Date(a.lastChapterTime))

      // 如果推荐书籍不足，添加填充书籍
      if (recommendedBooks.length === 0) {
        recommendedBooks.push(createPlaceholderBook(categoryId, categoryName))
      }

      // 更新编辑强推轮播数据（取前3本）
      featuredBooks.value = recommendedBooks.slice(0, 3)

      // 更新强推榜数据（取前10本）
      topRecommendBooks.value = recommendedBooks.slice(0, 10)

      // 更新人气作品数据（如果没有足够数据，使用填充书籍）
      if (recommendedBooks.length < 9) {
        while (recommendedBooks.length < 9) {
          recommendedBooks.push(createPlaceholderBook(categoryId, categoryName))
        }
      }
      popularFeaturedBooks.value = recommendedBooks.slice(0, 3)
      popularGridBooks.value = recommendedBooks.slice(3, 9) // 从第4本开始取6本，避免重复
    }
  } catch{
    // 出错时使用填充书籍
    const placeholderBook = createPlaceholderBook(categoryId, categoryName)
    featuredBooks.value = [placeholderBook]
    topRecommendBooks.value = [placeholderBook]
    popularFeaturedBooks.value = [placeholderBook]
    popularGridBooks.value = [placeholderBook]
  }
}

// 获取排行榜数据
const fetchRankingBooks = async (categoryId, categoryName) => {
  try {
    const response = await rankingApi.getAllRankingList()

    if (response.data && response.code==200) {
      const rankingData = response.data

      // 先按类型拆分排行榜数据
      const completedRankings = rankingData.completedRank || []
      const serializingRankings = rankingData.serializingRank || []
      const newRankings = rankingData.newRank || []
      const clickRankings = rankingData.countRank || []
      const favoriteRankings = rankingData.likeRank || []

      // 经典完本 - 筛选当前分类的数据
      let completedBooks = completedRankings
        .filter(book => book.categoryId === categoryId)
        .filter(book => book && book.id)

      if (completedBooks.length === 0) {
        completedBooks.push(createPlaceholderBook(categoryId, categoryName))
      }
      completedFeaturedBooks.value = completedBooks.slice(0, 3)
      completedGridBooks.value = completedBooks.slice(3, 9) // 从第4本开始取6本，避免重复

      // 连载推荐 - 筛选当前分类的数据
      let serializingBooksList = serializingRankings
        .filter(book => book.categoryId === categoryId)
        .filter(book => book && book.id)

      if (serializingBooksList.length === 0) {
        serializingBooksList.push(createPlaceholderBook(categoryId, categoryName))
      }
      serializingBooks.value = serializingBooksList.slice(0, 6)

      // 新书榜 - 筛选当前分类的数据
      let newBooksList = newRankings
        .filter(book => book.categoryId === categoryId)
        .filter(book => book && book.id)

      if (newBooksList.length === 0) {
        newBooksList.push(createPlaceholderBook(categoryId, categoryName))
      }
      newBooks.value = newBooksList.slice(0, 10)

      // 点击榜 - 筛选当前分类的数据
      let clickBooksList = clickRankings
        .filter(book => book.categoryId === categoryId)
        .filter(book => book && book.id)

      if (clickBooksList.length === 0) {
        clickBooksList.push(createPlaceholderBook(categoryId, categoryName))
      }
      topClickBooks.value = clickBooksList.slice(0, 10)

      // 收藏榜 - 筛选当前分类的数据
      let favoriteBooksList = favoriteRankings
        .filter(book => book.categoryId === categoryId)
        .filter(book => book && book.id)

      if (favoriteBooksList.length === 0) {
        favoriteBooksList.push(createPlaceholderBook(categoryId, categoryName))
      }
      topFavoriteBooks.value = favoriteBooksList.slice(0, 10)
    }
  } catch{
    const placeholderBook = createPlaceholderBook(categoryId, categoryName)

    // 出错时使用填充书籍
    completedFeaturedBooks.value = [placeholderBook]
    completedGridBooks.value = [placeholderBook]
    serializingBooks.value = [placeholderBook]
    newBooks.value = [placeholderBook]
    topClickBooks.value = [placeholderBook]
    topFavoriteBooks.value = [placeholderBook]
  }
}

// 模拟获取分类小说数据
const fetchCategoryBooks = (categoryId, page = 1) => {
  // 模拟API请求
  const mockBooks = Array.from({ length: 12 }, (_, i) => ({
    id: `${categoryId}-${page}-${i}`,
    title: `${getCategoryName(categoryId)}小说示例${page}-${i + 1}`,
    author: `作者${(page - 1) * 12 + i + 1}`,
    description: '这是一本非常精彩的小说，讲述了主人公在奇幻世界中的冒险故事。情节跌宕起伏，人物形象鲜明，值得一读。',
    cover: `https://picsum.photos/seed/book-${categoryId}-${page}-${i}/200/280.jpg`,
    category: getCategoryName(categoryId),
    words: Math.floor(Math.random() * 500000) + 100000,
    rating: (Math.random() * 2 + 3).toFixed(1),
    status: Math.random() > 0.5 ? 'completed' : 'ongoing',
    statusText: Math.random() > 0.5 ? '已完结' : '连载中'
  }))

  categoryBooks.value = mockBooks
}

// 根据分类ID获取分类名称
const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : ''
}

// 跳转到分类详情
const goToCategory = (categoryId) => {
  router.push(`/category/${categoryId}`)
}

// 更新选中分类和横幅图片
const updateSelectedCategory = async (categoryId) => {
  if (categoryId) {
    // 确保categories已经加载
    await nextTick()
    selectedCategory.value = categories.value.find(c => c.id === categoryId)
    updateBannerImage(selectedCategory.value)

    const categoryName = getCategoryName(categoryId)

    // 并行获取所有数据
    await Promise.all([
      fetchEditorRecommendedBooks(categoryId, categoryName),
      fetchRankingBooks(categoryId, categoryName),
      fetchCategoryBooks(categoryId, 1)
    ])


  } else {
    selectedCategory.value = null
    updateBannerImage(null)
    categoryBooks.value = []
  }
}

// 自动轮播功能
let featuredCarouselInterval = null
let popularCarouselInterval = null
let completedCarouselInterval = null

const startFeaturedCarousel = () => {
  if (featuredBooks.value.length === 0) return
  featuredCarouselInterval = setInterval(() => {
    currentFeaturedIndex.value = (currentFeaturedIndex.value + 1) % featuredBooks.value.length
  }, 3000)
}

const startPopularCarousel = () => {
  if (popularFeaturedBooks.value.length === 0) return
  popularCarouselInterval = setInterval(() => {
    currentPopularIndex.value = (currentPopularIndex.value + 1) % popularFeaturedBooks.value.length
  }, 4000)
}

const startCompletedCarousel = () => {
  if (completedFeaturedBooks.value.length === 0) return
  completedCarouselInterval = setInterval(() => {
    currentCompletedIndex.value = (currentCompletedIndex.value + 1) % completedFeaturedBooks.value.length
  }, 5000)
}

// 处理人气作品轮播点击
const handlePopularCarouselClick = (index) => {
  // 如果点击的是当前显示的图片，则跳转到详情页
  if (index === currentPopularIndex.value && popularFeaturedBooks.value.length > 0) {
    goToBookDetail(popularFeaturedBooks.value[index].id)
  } else {
    // 否则切换到点击的图片
    currentPopularIndex.value = index
  }
}

// 处理经典完本轮播点击
const handleCompletedCarouselClick = (index) => {
  // 如果点击的是当前显示的图片，则跳转到详情页
  if (index === currentCompletedIndex.value && completedFeaturedBooks.value.length > 0) {
    goToBookDetail(completedFeaturedBooks.value[index].id)
  } else {
    // 否则切换到点击的图片
    currentCompletedIndex.value = index
  }
}

const stopAllCarousels = () => {
  if (featuredCarouselInterval) clearInterval(featuredCarouselInterval)
  if (popularCarouselInterval) clearInterval(popularCarouselInterval)
  if (completedCarouselInterval) clearInterval(completedCarouselInterval)
}

// 监听路由变化
watch(() => route.params.categoryId, (newCategoryId) => {
  updateSelectedCategory(newCategoryId)
}, { immediate: true })

// 格式化人气值，将"万"转换为纯数字
const formatPopularityCount = (value) => {
  if (typeof value === 'string' && value.includes('万')) {
    const num = parseFloat(value.replace('万', ''))
    return Math.floor(num * 10000).toString()
  }
  return value
}

// 格式化点击数，将"万"转换为纯数字
const formatClickCount = (value) => {
  if (typeof value === 'string' && value.includes('万')) {
    const num = parseFloat(value.replace('万', ''))
    return Math.floor(num * 10000).toString()
  }
  return value
}

// 格式化推荐数，将"万"转换为纯数字
const formatRecommendCount = (value) => {
  if (typeof value === 'string' && value.includes('万')) {
    const num = parseFloat(value.replace('万', ''))
    return Math.floor(num * 10000).toString()
  }
  return value
}

// 格式化收藏数，将"万"转换为纯数字
const formatFavoriteCount = (value) => {
  if (typeof value === 'string' && value.includes('万')) {
    const num = parseFloat(value.replace('万', ''))
    return Math.floor(num * 10000).toString()
  }
  return value
}
// 跳转到书籍详情页
const goToBookDetail = (bookId) => {
  router.push(`/book/${bookId}`)
}

// 跳转到作者搜索页
const goToAuthorSearch = (author) => {
  router.push(`/search?author=${encodeURIComponent(author)}`)
}

// 跳转到类型筛选页
const goToCategoryFilter = (category) => {
  router.push(`/library?category=${encodeURIComponent(category)}`)
}

// 跳转到章节阅读页
const goToChapterRead = (bookId, chapterId) => {
  // 在新标签页打开阅读页面
  window.open(`/read/${bookId}/${chapterId}`, '_blank')
}

// 跳转到排行榜页面
const goToRanking = (type) => {
  router.push(`/ranking/${type}`)
}

// 处理排行榜鼠标移出事件，保留最后一个悬浮列表的悬浮效果
const handleRecommendListLeave = () => {
  // 不做任何操作，保持当前的悬浮状态
}

const handleNewListLeave = () => {
  // 不做任何操作，保持当前的悬浮状态
}

const handleClickListLeave = () => {
  // 不做任何操作，保持当前的悬浮状态
}

const handleRecommend2ListLeave = () => {
  // 不做任何操作，保持当前的悬浮状态
}

const handleFavoriteListLeave = () => {
  // 不做任何操作，保持当前的悬浮状态
}
</script>

<style scoped>
.category-page {
  padding: 0;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部横幅样式 */
.category-banner {
  width: 100%;
  height: 300px;
  overflow: hidden;
  margin-bottom: 30px;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.category-wrapper {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  position: relative;
  z-index: 10;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 0;
  margin-bottom: 0;
}

.category-divider {
  height: 1px;
  background-color: #ebeef5;
  margin: 20px 0;
}

.category-card {
  background-color: #fff;
  border-radius: 0;
  padding: 10px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: none;
  border-right: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.category-card.active {
  background-color: #ecf5ff;
  color: #409eff;
}

.category-card.active .category-icon {
  color: #409eff;
}

.category-card:nth-child(8n) {
  border-right: none;
}

.category-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

.category-icon {
  color: #409eff;
  margin-bottom: 5px;
  font-size: 20px;
}

.category-name {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}

.category-card.active .category-name {
  color: #409eff;
}

/* 更多按钮样式 */
.more-button {
  position: relative;
}

.more-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 1px solid #f5f7fa;
}

.dropdown-item:last-child {
  border-bottom: none;
}

.dropdown-item:hover {
  background: #f5f7fa;
}

.dropdown-item.active {
  background: #ecf5ff;
  color: #409eff;
}

.dropdown-icon {
  margin-right: 10px;
  color: #409eff;
}

.dropdown-name {
  font-size: 14px;
  font-weight: 500;
}

.category-detail {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.detail-header h2 {
  font-size: 22px;
  color: #303133;
  margin: 0;
}

.book-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.book-item {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid #ebeef5;
}

.book-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px 0 rgba(0, 0, 0, 0.15);
}

.book-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.book-item:hover .book-cover img {
  transform: scale(1.05);
}

.book-status {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 2px 6px;
  border-radius: 4px;
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
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-author {
  font-size: 14px;
  color: #606266;
  margin: 0 0 8px 0;
}

.book-desc {
  font-size: 13px;
  color: #909399;
  margin: 0 0 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.book-category {
  color: #409eff;
}

.book-rating {
  display: flex;
  align-items: center;
  color: #e6a23c;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .category-banner {
    height: 200px;
    margin-bottom: 20px;
  }

  .category-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 0;
  }

  .category-card:nth-child(2n) {
    border-right: none;
  }

  .book-list {
    grid-template-columns: repeat(auto-fill, minmax(100%, 1fr));
  }

  .detail-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .sort-options {
    margin-top: 10px;
  }
}

/* 新增section样式 */
.content-section {
  margin-bottom: 40px;
  display: flex;
  gap: 0; /* 移除左右两部分之间的间隙 */
  /* 移除统一的背景色、圆角和阴影，让各部分独立处理 */
}

.section-left {
  flex: 1;
  padding: 20px; /* 为左侧内容添加内边距 */
}

.section-right {
  width: 300px;
  /* 移除padding和分隔线，让标题与背景融合 */
}

.section-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

/* 编辑强推区域样式 */
.editor-recommend .featured-book {
  display: flex;
  gap: 20px;
  /* 移除背景色、圆角、padding和阴影，因为现在整个section已经有了 */
  height: 440px; /* 减去padding后的实际可用高度 */
}

.featured-cover {
  width: 280px;
  height: 440px; /* 减去padding(20px*2)后的实际可用高度 */
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.featured-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.featured-cover:hover img {
  transform: scale(1.05);
}

.featured-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
  overflow: hidden;
  justify-content: center;
}

.featured-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.featured-meta {
  display: flex;
  gap: 5px;
  font-size: 14px;
  color: #666;
}

.featured-author-label,
.featured-category-label {
  color: #999;
  font-weight: normal;
  cursor: default;
}

.featured-author,
.featured-category {
  margin-right: 15px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.featured-author:hover,
.featured-category:hover {
  color: #409eff;
}

.featured-desc-wrapper {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.featured-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 6;
  line-clamp: 6;
  -webkit-box-orient: vertical;
  text-align: left;
  max-width: 100%;
}

.featured-latest,
.featured-update {
  font-size: 14px;
  color: #409eff;
}

.book-carousel {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.carousel-item {
  width: 60px;
  height: 84px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-item:hover {
  transform: scale(1.1);
  border-color: #409eff;
}

.carousel-item.active {
  border-color: #409eff;
}

/* TOP榜单样式 */
.top-list {
  /* 移除背景色、圆角、padding和阴影，让标题与背景融合 */
  height: 440px; /* 减去padding后的实际可用高度 */
  display: flex;
  flex-direction: column;
}

.list-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
  text-align: center;
  /* 标题与背景融合，不需要特殊样式 */
}

.list-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1; /* 使用flex-grow填充剩余空间 */
  overflow: hidden; /* 隐藏超出部分 */
  /* 添加轻微的背景色和圆角，与页面背景有轻微区分 */
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  padding: 10px;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: rgba(255, 255, 255, 0.8); /* 与父容器背景略有区分 */
  border-radius: 6px;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  height: 40px; /* 固定一行高度 */
  overflow: hidden; /* 隐藏超出部分 */
}

.list-item:hover,
.list-item.active {
  background: #ecf5ff;
  transform: translateX(5px);
  height: auto; /* 悬浮时自动高度 */
  min-height: 70px; /* 最小高度 */
}

.item-rank {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 50%;
  font-size: 12px;
  font-weight: bold;
  margin-right: 10px;
  color: #666;
  flex-shrink: 0;
  position: relative;
}

/* 第一名 - 金牌 */
.list-item:nth-child(1) .item-rank {
  width: 30px;
  height: 30px;
  background-image: url('/1.png');
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
  color: transparent;
  font-size: 0;
}

/* 第二名 - 银牌 */
.list-item:nth-child(2) .item-rank {
  width: 30px;
  height: 30px;
  background-image: url('/2.png');
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
  color: transparent;
  font-size: 0;
}

/* 第三名 - 铜牌 */
.list-item:nth-child(3) .item-rank {
  width: 30px;
  height: 30px;
  background-image: url('/3.png');
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
  color: transparent;
  font-size: 0;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  justify-content: center; /* 垂直居中，使不悬浮时只显示一行 */
}

.list-item:hover .item-info,
.list-item.active .item-info {
  justify-content: flex-start; /* 悬浮时恢复顶部对齐 */
}

.item-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 0; /* 移除下边距，使不悬浮时更紧凑 */
  cursor: pointer;
  transition: color 0.3s ease;
  white-space: nowrap; /* 不悬浮时单行显示 */
  overflow: hidden; /* 隐藏超出部分 */
  text-overflow: ellipsis; /* 超出部分显示省略号 */
}

.list-item:hover .item-title,
.list-item.active .item-title {
  margin-bottom: 4px; /* 悬浮时恢复下边距 */
  white-space: normal; /* 悬浮时允许换行 */
  overflow: visible; /* 悬浮时显示全部内容 */
  text-overflow: unset; /* 悬浮时取消省略号 */
}

.item-title:hover {
  color: #409eff;
}

.item-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #666;
  opacity: 0; /* 不悬浮时隐藏详细信息 */
  visibility: hidden; /* 不悬浮时隐藏详细信息 */
  max-height: 0; /* 不悬浮时高度为0 */
  overflow: hidden; /* 隐藏超出部分 */
  transition: opacity 0.3s ease, visibility 0.3s ease, max-height 0.3s ease;
}

.list-item:hover .item-details,
.list-item.active .item-details {
  opacity: 1; /* 悬浮时显示详细信息 */
  visibility: visible; /* 悬浮时显示详细信息 */
  max-height: 100px; /* 悬浮时设置足够的高度 */
}

.item-author {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  transition: color 0.3s ease;
}

.item-author:hover {
  color: #409eff;
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
  color: #409eff;
}

.item-popularity {
  font-size: 12px;
  color: #666;
}

.item-popularity span {
  color: #ff6b6b;
  font-weight: bold;
}

.item-click {
  font-size: 12px;
  color: #666;
}

.item-click span {
  color: #4caf50;
  font-weight: bold;
}

.item-recommend {
  font-size: 12px;
  color: #666;
}

.item-recommend span {
  color: #2196f3;
  font-weight: bold;
}

.item-favorite {
  font-size: 12px;
  color: #666;
}

.item-favorite span {
  color: #ff9800;
  font-weight: bold;
}

.item-cover {
  width: 40px;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
  position: absolute;
  right: 10px;
  top: 0;
  bottom: 0;
  max-height: 60px;
  flex-shrink: 0;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  opacity: 0; /* 不悬浮时隐藏封面 */
  visibility: hidden; /* 不悬浮时隐藏封面 */
}

.list-item:hover .item-cover,
.list-item.active .item-cover {
  opacity: 1; /* 悬浮时显示封面 */
  visibility: visible; /* 悬浮时显示封面 */
}

.item-cover:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.item-popularity-value {
  font-size: 12px;
  color: #666;
  font-weight: normal;
  margin-left: 10px;
  flex-shrink: 0;
  opacity: 1; /* 不悬浮时显示数值 */
  visibility: visible; /* 不悬浮时显示数值 */
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.list-item:hover .item-popularity-value,
.list-item.active .item-popularity-value {
  opacity: 0; /* 悬浮时隐藏数值 */
  visibility: hidden; /* 悬浮时隐藏数值 */
}

.item-click-value {
  font-size: 12px;
  color: #666;
  font-weight: normal;
  margin-left: 10px;
  flex-shrink: 0;
  opacity: 1; /* 不悬浮时显示数值 */
  visibility: visible; /* 不悬浮时显示数值 */
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.list-item:hover .item-click-value,
.list-item.active .item-click-value {
  opacity: 0; /* 悬浮时隐藏数值 */
  visibility: hidden; /* 悬浮时隐藏数值 */
}

.item-recommend-value {
  font-size: 12px;
  color: #666;
  font-weight: normal;
  margin-left: 10px;
  flex-shrink: 0;
  opacity: 1; /* 不悬浮时显示数值 */
  visibility: visible; /* 不悬浮时显示数值 */
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.list-item:hover .item-recommend-value,
.list-item.active .item-recommend-value {
  opacity: 0; /* 悬浮时隐藏数值 */
  visibility: hidden; /* 悬浮时隐藏数值 */
}

.item-favorite-value {
  font-size: 12px;
  color: #666;
  font-weight: normal;
  margin-left: 10px;
  flex-shrink: 0;
  opacity: 1; /* 不悬浮时显示数值 */
  visibility: visible; /* 不悬浮时显示数值 */
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.list-item:hover .item-favorite-value,
.list-item.active .item-favorite-value {
  opacity: 0; /* 悬浮时隐藏数值 */
  visibility: hidden; /* 悬浮时隐藏数值 */
}

/* 书籍网格样式 */
.book-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(3, 1fr);
  gap: 15px;
  /* 移除背景色和圆角，与背景融为一体 */
}

.grid-item {
  /* 移除背景色和圆角，与背景融为一体 */
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 15px; /* 保留内边距 */
}

.grid-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.grid-cover {
  width: 80px;
  height: 110px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
}

.grid-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.grid-item:hover .grid-cover img {
  transform: scale(1.05);
}

.grid-info {
  flex: 1;
  text-align: left;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 110px;
}

.grid-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.grid-desc {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
  margin: 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.grid-meta {
  font-size: 12px;
  color: #333;
  margin-top: auto;
}

.grid-author,
.grid-category {
  display: inline;
}

/* 人气作品和经典完本的特殊布局 */
.popular-layout {
  display: flex;
  flex-direction: column;
  gap: 0; /* 移除gap，让内部元素连接起来 */
}

.popular-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0; /* 移除gap，让内部元素连接起来 */
  /* 添加更轻微的背景色和圆角，与页面背景更好地融合 */
  background: rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  padding: 15px;
}

/* 轮播区域样式 */
.featured-carousel-vertical {
  grid-row: span 3;
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 15px;
}

.carousel-container-vertical {
  height: 280px; /* 固定高度 */
  position: relative;
  margin-bottom: 20px;
  overflow: hidden;
}

.carousel-items {
  position: relative;
  width: 100%;
  height: 100%;
}

.carousel-item-vertical {
  position: absolute;
  width: 120px;
  height: 160px;
  transition: all 0.5s ease;
  cursor: pointer;
  opacity: 0;
  z-index: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  top: 50%;
  margin-top: -80px;
  left: 50%;
  margin-left: -60px;
  transform: scale(0.8);
}

.carousel-item-vertical.active {
  opacity: 1;
  transform: scale(1);
  z-index: 3;
}

.carousel-item-vertical.prev {
  opacity: 0.8;
  transform: scale(0.9) translateX(-100%);
  z-index: 2;
}

.carousel-item-vertical.next {
  opacity: 0.8;
  transform: scale(0.9) translateX(100%);
  z-index: 2;
}

.carousel-cover-vertical {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}

.carousel-cover-vertical img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.carousel-item-vertical:hover .carousel-cover-vertical img {
  transform: scale(1.05);
}

/* 轮播图下方信息区域 */
.carousel-info-vertical {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 15px 0;
}

.carousel-title-vertical {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
  cursor: pointer;
  transition: color 0.3s ease;
}

.carousel-title-vertical:hover {
  color: #ff4757;
}

.carousel-author-vertical {
  font-size: 14px;
  color: #666;
  margin: 0 0 10px 0;
  cursor: pointer;
  transition: color 0.3s ease;
}

.carousel-author-vertical:hover {
  color: #ff4757;
}

.carousel-desc-vertical {
  font-size: 13px;
  color: #777;
  margin: 0 0 15px 0;
  line-height: 1.4;
  cursor: pointer;
  transition: color 0.3s ease;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.carousel-desc-vertical:hover {
  color: #ff4757;
}

.detail-btn-vertical {
  padding: 8px 20px;
  background: #ff4757;
  color: #fff;
  border: none;
  border-radius: 20px; /* 椭圆形按钮 */
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s ease;
}

.detail-btn-vertical:hover {
  background: #e63946;
}

/* 为人气作品和经典完本中的grid-item添加样式 */
.popular-grid .grid-item {
  background: rgba(255, 255, 255, 0.8); /* 与父容器背景略有区分 */
  border-radius: 6px;
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 15px; /* 保留内边距 */
  margin: 7.5px; /* 添加外边距，替代原来的gap */
}

.popular-grid .grid-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}
@media (max-width: 1200px) {
  .book-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .content-section {
    flex-direction: column;
  }

  .section-right {
    width: 100%;
  }

  .featured-book {
    flex-direction: column;
  }

  .featured-carousel {
    flex-direction: column;
  }

  .carousel-container {
    width: 100%;
    height: 200px;
  }

  .book-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .book-grid {
    grid-template-columns: 1fr;
  }
}
/* 书籍标题悬停效果 */
.featured-title:hover,
.grid-title:hover,
.carousel-title:hover {
  color: #ff4757;
  cursor: pointer;
}

/* 书籍简介悬停效果 */
.featured-desc:hover,
.grid-desc:hover {
  color: #ff4757;
  cursor: pointer;
}

/* 作者悬停效果 */
.featured-author:hover,
.grid-author:hover,
.carousel-author:hover {
  color: #ff4757;
  cursor: pointer;
}

/* 类型悬停效果 */
.featured-category:hover,
.grid-category:hover {
  color: #ff4757;
  cursor: pointer;
}

/* 最新章节悬停效果 */
.featured-latest:hover {
  color: #ff4757;
  cursor: pointer;
}

/* 轮播封面悬停效果 */
.carousel-cover:hover {
  cursor: pointer;
}

.carousel-cover:hover img {
  transform: scale(1.05);
  transition: transform 0.3s ease;
}

/* 不同榜单标题的颜色样式 */
.recommend-title {
  color: #e6a23c; /* 橙色 - 强推榜 */
  cursor: default;
}

.new-title {
  color: #409eff; /* 蓝色 - 新书榜 */
  cursor: pointer;
  transition: color 0.3s ease;
}

.new-title:hover {
  color: #66b1ff;
}

.click-title {
  color: #f56c6c; /* 红色 - 点击榜 */
  cursor: pointer;
  transition: color 0.3s ease;
}

.click-title:hover {
  color: #f78989;
}

.recommend2-title {
  color: #67c23a; /* 绿色 - 推荐榜 */
  cursor: pointer;
  transition: color 0.3s ease;
}

.recommend2-title:hover {
  color: #85ce61;
}

.favorite-title {
  color: #909399; /* 灰色 - 收藏榜 */
  cursor: pointer;
  transition: color 0.3s ease;
}

.favorite-title:hover {
  color: #a6a9ad;
}
</style>
