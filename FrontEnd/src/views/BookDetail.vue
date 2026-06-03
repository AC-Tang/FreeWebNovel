<template>
  <PageTransition>
    <LoadingSpinner v-if="loading" />
    <div v-else class="book-detail-page">
      <div class="book-detail-content">
        <!-- 当显示全部书评时，只显示书评界面 -->
        <div v-if="showAllReviews" class="all-reviews-only">
          <!-- 全部书评模式 -->
          <div class="all-reviews-section">
            <div class="review-header-section">
              <div class="review-title">
                <span>全部书评 ·  {{ totalReviews }}</span>
                <el-button type="primary" @click="goToAllReviews">返回</el-button>
              </div>
            </div>

            <!-- 评分统计 -->
            <div class="review-stats-main">
              <!-- 第一行：评分 + 星星 -->
              <div class="rating-row-first">
                <!-- 评分：x.x分 -->
                <div class="rating-score">
                  <span class="score-value">{{ hottestReview?.rating || 0 }}</span>
                  <span class="score-unit">分</span>
                </div>
                <!-- 五颗星星（快速评分） -->
                <div class="rating-stars">
                  <el-rate
                    v-model="reviewForm.rating"
                    @click="handleRatingClick"
                    size="large"
                  />
                </div>
              </div>

              <!-- 第二行：点评人数 + 提示文字 -->
              <div class="rating-row-second">
                <!-- 点评人数 -->
                <div class="rating-people">
                  <span>{{ totalReviews }}人点评</span>
                </div>
                <!-- 提示文字 -->
                <div class="rating-tip">
                  <span>轻按星星点评此书</span>
                </div>
              </div>
            </div>

            <!-- 书评列表 -->
            <div class="reviews-list" v-loading="reviewLoading">
              <div
                v-for="review in allReviews"
                :key="review.id"
                class="review-item"
                @click="goToCommentDetail(review.id)"
              >
                <!-- 评论右上角三个点 -->
                <div class="review-actions-menu">
                  <el-dropdown>
                    <el-button type="text" circle>
                      <el-icon><More /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item @click.stop="reportReview(review.id)">举报</el-dropdown-item>
                        <el-dropdown-item
                          v-if="authStore.isAuthenticated && review.userId === authStore.user.id"
                          @click.stop="deleteReview(review.id)"
                          style="color: #f56c6c;"
                        >
                          删除
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>

                <!-- 评论内容 -->
                <div class="review-header">
                  <div class="reviewer-info">
                    <el-avatar :src="review.avatar" />
                    <span class="reviewer-name">{{ review.username }}</span>
                    <span class="review-time">{{ formatTime(review.createdAt) }}</span>
                  </div>
                </div>
                <div class="review-content">{{ review.content }}</div>

                <!-- 评论底部操作区 -->
                <div class="review-footer">
                  <!-- 评分和点赞 -->
                  <div class="review-actions">
                    <!-- 评分星星 - 左下角 -->
                    <div class="review-rating">
                      <el-rate :model-value="review.rating" disabled size="small" />
                    </div>
                    <!-- 点赞 - 右下角 -->
                    <div class="action-item" @click.stop="likeReview(review.id)">
                      <el-icon>
                        <Star v-if="!review.isLiked" />
                        <StarFilled v-else />
                      </el-icon>
                      <span>{{ review.likeCount }}</span>
                    </div>
                  </div>
                  <!-- 评论数 - 单独一行 -->
                  <div class="review-reply-count" v-if="review.replyCount > 0">
                    <span>全部{{ review.replyCount }}条评论 ></span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div class="review-pagination">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total="totalReviews"
                layout="total, sizes, prev, pager, next, jumper"
                :page-sizes="[10, 20, 50, 100]"
                @size-change="handleReviewSizeChange"
                @current-change="handleReviewPageChange"
              />
            </div>
          </div>
        </div>

        <!-- 非全部书评时显示正常内容 -->
        <div v-else>
          <!-- 小说信息部分 -->
          <div class="book-header">
            <div class="book-cover">
              <img :src="bookInfo.cover" :alt="bookInfo.title" />
              <div class="book-status" :class="bookInfo.status">
                {{ bookInfo.status === 'serializing' ? '连载中' : '已完结' }}
              </div>
            </div>
            <div class="book-info">
              <h1 class="book-title">{{ bookInfo.title }}</h1>
              <div class="book-meta">
                <div class="meta-item">
                  <span class="label">作者：</span>
                  <span class="value">{{ bookInfo.author }}</span>
                </div>
                <div class="meta-item">
                  <span class="label">分类：</span>
                  <span class="value">{{ bookInfo.category }}</span>
                </div>
                <div class="meta-item">
                  <span class="label">更新：</span>
                  <span class="value">{{ formatTime(bookInfo.updateTime) }}</span>
                </div>
              </div>
              <div class="book-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ formatWordCount(bookInfo.wordCount) }}</div>
                  <div class="stat-label">总字数</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ formatNumber(bookInfo.clickCount) }}</div>
                  <div class="stat-label">点击量</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ formatNumber(bookInfo.favoriteCount) }}</div>
                  <div class="stat-label">收藏量</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ formatNumber(bookInfo.recommendCount) }}</div>
                  <div class="stat-label">推荐量</div>
                </div>
              </div>
              <div class="book-actions">
                <el-button type="primary" size="large" @click="startReading">
                  开始阅读
                </el-button>
                <el-button
                  type="success"
                  size="large"
                  :loading="checkingBookshelfStatus"
                  @click="addToBookshelf"
                >
                  {{ isInBookshelf ? '移出书架' : '加入书架' }}
                </el-button>
              </div>
            </div>
          </div>

          <!-- 导航栏切换 -->
          <div class="tab-navigation">
            <div
              class="tab-item"
              :class="{ active: activeTab === 'info' }"
              @click="handleTabChange('info')"
            >
              作品信息
            </div>
            <div
              class="tab-item"
              :class="{ active: activeTab === 'chapters' }"
              @click="handleTabChange('chapters')"
            >
              目录
            </div>
            <div
              class="tab-item"
              :class="{ active: activeTab === 'reviews' }"
              @click="handleTabChange('reviews')"
            >
              书评
            </div>
          </div>

          <div v-show="activeTab === 'info'" class="book-info-section">
              <!-- 简介 -->
              <div class="book-intro">
                <h2 class="section-title">作品简介</h2>
                <div class="intro-content">
                  <p v-for="(para, index) in bookInfo.introduction" :key="index">
                    {{ para }}
                  </p>
                </div>
              </div>

              <!-- 相关推荐 -->
              <div class="book-recommendations">
                <h2 class="section-title">相关推荐</h2>
                <div class="recommendation-list">
                  <div
                    v-for="book in recommendations"
                    :key="book.id"
                    class="recommendation-item"
                    @click="goToBook(book.id)"
                  >
                    <div class="book-cover">
                      <img :src="book.cover" :alt="book.title" />
                    </div>
                    <div class="book-info">
                      <div class="book-title">{{ book.title }}</div>
                      <div class="book-author">{{ book.author }}</div>
                      <div class="book-category">{{ book.category }}</div>
                      <div class="book-status" :class="book.status">
                        {{ book.status === 'serializing' ? '连载中' : '已完结' }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 目录部分 -->
            <div v-show="activeTab === 'chapters'" class="book-chapters">
              <div class="chapter-controls">
                <div class="chapter-stats">
                  共{{ chapters.length }}章·本卷共{{ calculateTotalWords(chapters) }}字
                </div>
                <div class="chapter-actions">
                  <el-input
                    v-model="chapterKeyword"
                    placeholder="搜索章节"
                    class="chapter-filter"
                    clearable
                    @input="filterChapters"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                  <div class="sort-buttons">
                    <div
                      class="sort-button"
                      :class="{ active: chapterSort === 'asc' }"
                      @click="setChapterSort('asc')"
                    >
                      正序
                    </div>
                    <div
                      class="sort-button"
                      :class="{ active: chapterSort === 'desc' }"
                      @click="setChapterSort('desc')"
                    >
                      倒序
                    </div>
                  </div>
                </div>
              </div>
              <div class="chapter-list">
                <div
                  v-for="chapter in filteredChapters"
                  :key="chapter.id"
                  class="chapter-item"
                  @click="goToChapter(chapter)"
                >
                  <span class="chapter-title">{{ chapter.title }}</span>
                </div>
              </div>
            </div>

            <!-- 书评部分 -->
            <div v-show="activeTab === 'reviews'" class="book-reviews">
              <!-- 最热书评模式 -->
              <div class="hot-review-section">
                <div class="review-header-section">
                  <div class="review-title">
                    <span>书评 ·  {{ realTotalReviews }}</span>
                    <el-button type="primary" @click="goToAllReviews">全部书评</el-button>
                  </div>
                </div>

                <div class="review-rating-section">
                  <span class="rating-label">轻点评分：</span>
                  <el-rate
                    v-model="reviewForm.rating"
                    @click="handleRatingClick"
                    size="large"
                  />
                </div>

                <div class="hottest-review-section" v-if="hottestReview">
                  <h3 class="hottest-review-title">最热书评</h3>
                  <div
                    class="review-item"
                    @click="goToCommentDetail(hottestReview.id)"
                  >
                    <!-- 评论右上角三个点 -->
                    <div class="review-actions-menu">
                      <el-dropdown>
                        <el-button type="text" circle>
                          <el-icon><More /></el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item @click.stop="reportReview(hottestReview.id)">举报</el-dropdown-item>
                            <el-dropdown-item
                              v-if="authStore.isAuthenticated && hottestReview.userId === authStore.user.id"
                              @click.stop="deleteReview(hottestReview.id)"
                              style="color: #f56c6c;"
                            >
                              删除
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>

                    <div class="review-header">
                      <div class="reviewer-info">
                        <el-avatar :src="hottestReview.avatar" />
                        <span class="reviewer-name">{{ hottestReview.username }}</span>
                        <span class="review-time">{{ formatTime(hottestReview.createTime) }}</span>
                      </div>
                    </div>
                    <div class="review-content">{{ hottestReview.content }}</div>

                    <!-- 评论底部操作区 -->
                    <div class="review-footer">
                      <!-- 评分和点赞 -->
                      <div class="review-actions">
                        <!-- 评分星星 - 左下角 -->
                        <div class="review-rating">
                          <el-rate :model-value="hottestReview.rating" disabled size="small" />
                        </div>
                        <!-- 点赞 - 右下角 -->
                        <div class="action-item" @click.stop="likeReview(hottestReview.id)">
                          <el-icon>
                            <Star v-if="!isLiked" />
                            <StarFilled v-else />
                          </el-icon>
                          <span>{{ hottestReview.likeCount }}</span>
                        </div>
                      </div>
                      <!-- 评论数 - 单独一行 -->
                      <div class="review-reply-count" v-if="hottestReview.replyCount > 0">
                        <span>全部{{ hottestReview.replyCount }}条评论 ></span>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="no-review" v-else>
                  <p>暂无书评，快来写第一条书评吧！</p>
                </div>

                <div class="view-all-reviews">
                  <el-button type="primary" @click="goToAllReviews">查看全部书评</el-button>
                </div>
              </div>
            </div>
          </div>

        <!-- 书评对话框 -->
        <el-dialog v-model="showReviewDialog" title="发表书评" width="500px" @close="handleReviewDialogClose">
          <el-form :model="reviewForm" label-width="80px">
            <el-form-item label="评分">
              <el-rate v-model="reviewForm.rating" />
            </el-form-item>
            <el-form-item label="内容">
              <el-input
                v-model="reviewForm.content"
                type="textarea"
                :rows="6"
                placeholder="请输入您的书评内容..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="showReviewDialog = false">取消</el-button>
              <el-button type="primary" @click="submitReview">发表</el-button>
            </span>
          </template>
        </el-dialog>

        <!-- 举报对话框 -->
        <el-dialog v-model="showReportDialog" title="举报评论" width="500px" @close="handleReportDialogClose">
          <el-form :model="reportForm" label-width="80px" :rules="reportRules" ref="reportFormRef">
            <el-form-item label="举报类型" prop="reasonType">
              <el-radio-group v-model="reportForm.reasonType">
                <el-radio :label="1">内容违规</el-radio>
                <el-radio :label="2">垃圾信息</el-radio>
                <el-radio :label="3">版权侵犯</el-radio>
                <el-radio :label="4">恶意攻击</el-radio>
                <el-radio :label="5">其他</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="举报描述" prop="reasonDetail">
              <el-input
                v-model="reportForm.reasonDetail"
                type="textarea"
                :rows="4"
                placeholder="请简要描述您的举报理由..."
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="reportForm.isAnonymous">匿名举报</el-checkbox>
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="showReportDialog = false">取消</el-button>
              <el-button type="primary" @click="submitReport">提交举报</el-button>
            </span>
          </template>
        </el-dialog>
      </div>
    </div>
  </PageTransition>
</template>

<script setup name="BookDetailPage">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useFavoriteStore } from '../stores/favorite'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, Search, More } from '@element-plus/icons-vue'
import PageTransition from '../components/PageTransition.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import { booksApi } from '../api/Books'
import { chaptersApi } from '../api/Chapters'
import { commentsApi } from '../api/Comment'
import { ratingsApi } from '../api/Rating'
import { likesApi } from '../api/Like'
import { reportsApi } from '../api/Reports'
import { getImageUrl } from '../utils/imageUtils'

// 导入书架API
import { bookshelvesApi } from '../api/Bookshelves'

// 防抖函数
const debounce = (func, wait) => {
  let timeout
  return function(...args) {
    const context = this
    clearTimeout(timeout)
    timeout = setTimeout(() => func.apply(context, args), wait)
  }
}
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const favoriteStore = useFavoriteStore()

// 加载状态
const loading = ref(true)

// 获取书籍ID
const bookId = computed(() => route.params.id)

// 书籍信息
const bookInfo = ref({
  id: '',
  title: '',
  author: '',
  category: '',
  cover: '',
  status: 'serializing', // serializing or completed
  wordCount: 0,
  updateTime: new Date(),
  clickCount: 0,
  favoriteCount: 0,
  recommendCount: 0,
  introduction: []
})

// 书架状态
const isInBookshelf = ref(false)
const checkingBookshelfStatus = ref(false)

// 章节相关
const chapters = ref([])
const filteredChapters = ref([])
const chapterKeyword = ref('')
const chapterSort = ref('asc')
const totalChapters = ref(0)

// 书评相关
const hottestReview = ref(null) // 最热评论
const allReviews = ref([]) // 全部书评列表
const totalReviews = ref(0) // 评论总数
const realTotalReviews = ref(0) // 实际评论总数(有评分的评论)
const isLiked = ref(false) // 用户是否已点赞
const showReviewDialog = ref(false)
const showAllReviews = ref(false) // 是否显示全部书评
const reviewForm = ref({
  rating: 0, // 默认0星，根据要求初始设置为0
  content: ''
})

// 分页相关
const currentPage = ref(1) // 当前页码
const pageSize = ref(20) // 每页条数
const reviewLoading = ref(false) // 书评加载状态

// 相关推荐
const recommendations = ref([])

// 导航栏切换
const activeTab = ref('info') // 默认选中"作品信息"

// 举报相关
const showReportDialog = ref(false) // 举报弹窗显示状态
const reportFormRef = ref() // 举报表单引用
const reportForm = ref({
  reporterId: '', // 举报者ID
  targetType: 2, // 举报类型：评论
  targetId: '', // 举报对象ID
  reasonType: 1, // 举报原因类型
  reasonDetail: '', // 举报描述
  isAnonymous: 0 // 是否匿名举报，0-不匿名，1-匿名
})

// 举报表单验证规则
const reportRules = {
  reasonType: [
    { required: true, message: '请选择举报类型', trigger: 'change' }
  ],
  reasonDetail: [
    { required: true, message: '请输入举报描述', trigger: 'blur' },
    { min: 10, max: 200, message: '举报描述长度在 10 到 200 个字符', trigger: 'blur' }
  ]
}

// 添加tab切换的点击事件处理
const handleTabChange = (tab) => {
  activeTab.value = tab
  if (tab === 'reviews') {
    fetchHottestReview()
  }
}

// 增加浏览量
const addViewCount = async () => {
  try {
    const response = await booksApi.addViewCount(bookId.value)
    if (response && response.code === 200) {
      console.log('浏览量增加成功')
    } else {
      console.warn('增加浏览量返回异常:', response)
    }
  } catch (error) {
    console.error('增加浏览量失败:', error)
  }
}

// 使用防抖包装增加浏览量函数
const debouncedAddViewCount = debounce(addViewCount, 1000)

// 获取书籍详情
const fetchBookDetail = async () => {
  try {
    const response = await booksApi.getBookById(bookId.value)
    if (response && response.code === 200 && response.data) {
      // 确保所有必要字段都有默认值，防止undefined错误
      bookInfo.value = {
        id: response.data.id || '',
        title: response.data.title || '未知标题',
        author: response.data.author || '未知作者',
        category: response.data.categoryName || response.data.category || '未分类',
        cover: getImageUrl(response.data.cover),
        status: response.data.statusName === '连载中' ? 'serializing' : 'completed',
        wordCount: response.data.wordCount || 0,
        updateTime: response.data.lastChapterTime || response.data.updateTime || new Date(),
        clickCount: response.data.viewCount || response.data.clickCount || 0,
        favoriteCount: response.data.likeCount || response.data.favoriteCount || 0,
        recommendCount: response.data.recommendCount || 0,
        introduction: response.data.description ? [response.data.description] : ['暂无简介']
      }
    } else {
      console.warn('获取书籍详情返回异常:', response)
      ElMessage.warning(response?.message || '获取书籍详情失败')
    }
  } catch (error) {
    console.error('获取书籍详情失败:', error)
    ElMessage.error('获取书籍详情失败')
  }
}

// 获取章节列表
const fetchChapters = async () => {
  try {
    const response = await chaptersApi.getChaptersByNovelId(bookId.value)
    if (response && response.code === 200 && response.data) {
      // 确保每个章节都有必要的字段
      chapters.value = Array.isArray(response.data) ? response.data.map(chapter => ({
        id: chapter.id || `chapter_${Date.now()}_${Math.random()}`,
        title: chapter.title || '未知章节',
        wordCount: chapter.wordCount || 0,
        bookId: chapter.bookId || bookId.value,
        sortOrder: chapter.sortOrder || 1,
        updateAt: chapter.updateAt || new Date()
      })) : []

      totalChapters.value = chapters.value.length
      // 初始化章节列表
      sortChapters()
    } else {
      console.warn('获取章节列表返回异常:', response)
      ElMessage.warning(response?.message || '获取章节列表失败')
    }
  } catch (error) {
    console.error('获取章节列表失败:', error)
    ElMessage.error('获取章节列表失败')
  }
}



// 获取最热评论
const fetchHottestReview = async () => {
  try {
    const response = await commentsApi.getHottestCommentsByBookId(bookId.value)
    if (response && response.code === 200) {
        hottestReview.value ={
          id: response.data.id || `review_${Date.now()}_${Math.random()}`,
          userId: response.data.userId || response.data.user_id || '',
          username: response.data.username || '匿名用户',
          // 将二进制avatar转换为base64格式
          avatar: response.data.avatar ? `data:image/jpeg;base64,${response.data.avatar}` : 'https://picsum.photos/seed/default_user/40/40.jpg',
          rating: response.data.rating || 5,
          content: response.data.content || '暂无内容',
          likeCount: response.data.likeCount || 0,
          replyCount: response.data.replyCount || 0,
          createTime: response.data.createdAt || new Date(),
          isLiked: false // 初始化未点赞
        }

        // 检查用户是否已点赞该评论
        if (authStore.isAuthenticated) {
          await checkLikeStatus()
        } else {
          isLiked.value = false
          hottestReview.value.isLiked = false
        }
      } else {
        hottestReview.value = null
        isLiked.value = false
      }

      totalReviews.value = response.data.count || 0
      realTotalReviews.value = response.data.realCount || 0
  } catch (error) {
    console.error('获取最热评论失败:', error)
    // 不显示错误提示，避免影响用户体验
    isLiked.value = false
  }
}

// 获取全部书评
const fetchAllReviews = async () => {
  reviewLoading.value = true
  try {
    const response = await commentsApi.getCommentsByBookId(bookId.value)

    if (response && response.code === 200) {
      // 处理书评数据，为每条评论添加isLiked状态
        allReviews.value = response.data.map(review => ({
          ...review,
          isLiked: false, // 初始化为未点赞
          userId: review.userId || review.user_id || '', // 确保userId存在
          // 将二进制avatar转换为base64格式
          avatar: review.avatar ? `data:image/jpeg;base64,${review.avatar}` : 'https://picsum.photos/seed/default_user/40/40.jpg'
        }))

      // 如果用户已登录，检查每条评论的点赞状态
      if (authStore.isAuthenticated) {
        for (const review of allReviews.value) {
          try {
            const likeResponse = await likesApi.existLike(review.id, authStore.user.id, 1)
            review.isLiked = likeResponse && likeResponse.code === 200 ? likeResponse.data : false
          } catch (error) {
            console.error('检查评论点赞状态失败:', error)
            review.isLiked = false
          }
        }
      }
    }
  } catch (error) {
    console.error('获取全部书评失败:', error)
    allReviews.value = []
  } finally {
    reviewLoading.value = false
  }
}

// 检查用户是否已点赞该评论
const checkLikeStatus = async () => {
  try {
    if (hottestReview.value) {
      const response = await likesApi.existLike(
        hottestReview.value.id, // 评论id
        authStore.user.id, // 用户id
        1 // targetType，评论点赞为1
      )

      if (response && response.code === 200) {
        isLiked.value = response.data // 根据API返回结果设置是否已点赞
      }
    }
  } catch (error) {
    console.error('检查点赞状态失败:', error)
    isLiked.value = false
  }
}

// 获取相关推荐
const fetchRecommendations = () => {
  // 模拟API请求获取相关推荐
  const mockRecommendations = []
  const categories = ['玄幻', '仙侠', '都市', '历史', '科幻']

  for (let i = 1; i <= 6; i++) {
    const category = categories[Math.floor(Math.random() * categories.length)]

    mockRecommendations.push({
      id: `recommend_book_${i}`,
      title: `${category}推荐小说${i}`,
      author: `推荐作者${i}`,
      category,
      cover: `https://picsum.photos/seed/recommend_${i}/200/280.jpg`,
      status: Math.random() > 0.5 ? 'serializing' : 'completed'
    })
  }

  recommendations.value = mockRecommendations
}

// 开始阅读
const startReading = () => {
  // 在新标签页打开阅读页面，默认从第一章开始
  window.open(`/read/${bookId.value}/1`, '_blank')
}

// 检查书籍是否在书架中
const checkBookInBookshelf = async () => {
  if (!authStore.isAuthenticated) {
    // 非登录用户，直接显示"加入书架"
    isInBookshelf.value = false
    return
  }

  try {
    checkingBookshelfStatus.value = true
    const response = await bookshelvesApi.existInBookshelf(bookId.value, authStore.user.id)
    if (response && response.code === 200) {
      isInBookshelf.value = response.data === true
    } else {
      console.warn('检查书籍是否在书架中返回异常:', response)
      isInBookshelf.value = false
    }
  } catch (error) {
    console.error('检查书籍是否在书架中失败:', error)
    isInBookshelf.value = false
  } finally {
    checkingBookshelfStatus.value = false
  }
}

// 加入书架
const addToBookshelf = async () => {
  if (!authStore.isAuthenticated) {
    // 非登录用户，弹出确认框
    ElMessageBox.confirm('登录后才能加入书架，是否前往登录页面？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => {
      router.push('/login')
    }).catch(() => {
      // 用户取消，不做任何操作
    })
    return
  }

  if (isInBookshelf.value) {
    // 已在书架，弹出确认框询问是否移除
    ElMessageBox.confirm('确定要将此书从书架中移除吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        const response = await bookshelvesApi.removeBookFromShelves(bookId.value, authStore.user.id)
        if (response && response.code === 200 && response.data === true) {
          isInBookshelf.value = false
          ElMessage.success('已从书架移除')
        } else {
          ElMessage.error(response?.message || '移除失败')
        }
      } catch (error) {
        console.error('从书架移除失败:', error)
        ElMessage.error('移除失败，请稍后再试')
      }
    }).catch(() => {
      // 用户取消，不做任何操作
    })
  } else {
    // 未在书架，直接添加
    try {
      const response = await bookshelvesApi.addBookToShelves(bookId.value, authStore.user.id)
      if (response && response.code === 200 && response.data === true) {
        isInBookshelf.value = true
        ElMessage.success('已加入书架')
      } else {
        ElMessage.error(response?.message || '加入失败')
      }
    } catch (error) {
      console.error('加入书架失败:', error)
      ElMessage.error('加入失败，请稍后再试')
    }
  }
}

// 计算总字数
const calculateTotalWords = (chapters) => {
  if (!chapters || !chapters.length) return 0
  return chapters.reduce((total, chapter) => total + (chapter.wordCount || 0), 0)
}

// 设置章节排序
const setChapterSort = (sort) => {
  chapterSort.value = sort
  sortChapters()
}

// 搜索章节
const filterChapters = () => {
  if (!chapterKeyword.value) {
    sortChapters()
    return
  }

  const keyword = chapterKeyword.value.toLowerCase()
  filteredChapters.value = chapters.value.filter(chapter =>
    chapter.title.toLowerCase().includes(keyword)
  )
}

// 排序章节
const sortChapters = () => {
  if (chapterSort.value === 'asc') {
    filteredChapters.value = [...chapters.value]
  } else {
    filteredChapters.value = [...chapters.value].reverse()
  }

  // 如果有搜索关键词，再进行过滤
  if (chapterKeyword.value) {
    filterChapters()
  }
}

// 跳转到章节
const goToChapter = (chapter) => {
  // 使用章节的sortOrder和bookId作为参数
  const novelId = chapter.bookId || bookInfo.value.id
  const sortOrder = chapter.sortOrder || 1

  // 在新标签页打开阅读页面
  window.open(`/read/${novelId}/${sortOrder}`, '_blank')
}

// 点赞书评
const likeReview = async (reviewId) => {
  // 检查用户是否已登录
  if (!authStore.isAuthenticated) {
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
      return
    } catch {
      // 用户点击取消，不做任何操作
      return
    }
  }

  if (hottestReview.value && hottestReview.value.id === reviewId) {
    try {
      if (isLiked.value) {
        // 已点赞，取消点赞
        const response = await likesApi.cancelLike(authStore.user.id,reviewId)

        if (response && response.code === 200) {
          hottestReview.value.likeCount--
          isLiked.value = false
          ElMessage.success('取消点赞成功')
        }
      } else {
        // 未点赞，进行点赞
        const response = await likesApi.like({
          userId: authStore.user.id,
          targetType: 1, // 评论点赞为1
          targetId: reviewId
        })

        if (response && response.code === 200) {
          hottestReview.value.likeCount++
          isLiked.value = true
          ElMessage.success('点赞成功')
        }
      }
    } catch (error) {
      console.error('点赞操作失败:', error)
      ElMessage.error('点赞操作失败，请稍后再试')
    }
  }
}

// 处理评分点击事件
const handleRatingClick = async () => {
  // 检查用户是否已登录
  if (!authStore.isAuthenticated) {
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
      return
    } catch {
      // 用户点击取消，不做任何操作
      return
    }
  }

  // 用户已登录，显示书评对话框
  showReviewDialog.value = true
}

// 提交书评
const submitReview = async () => {
  // 检查用户是否已登录
  if (!authStore.isAuthenticated) {
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
      return
    } catch {
      // 用户点击取消，不做任何操作
      return
    }
  }

  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请输入书评内容')
    return
  }

  try {
    // 1. 提交评分
    const ratingData = {
      userId: authStore.user.id,
      bookId: bookId.value,
      score: reviewForm.value.rating
    }

    await ratingsApi.addOrUpdateBookRating(ratingData)

    // 2. 提交书评
    const commentData = {
      userId: authStore.user.id,
      bookId: bookId.value,
      commentType: 3, // 书评的comment_type为3
      content: reviewForm.value.content
    }

    const response = await commentsApi.publishComment(commentData)

    if (response && response.code === 200) {
      // 重置表单
      reviewForm.value = {
        rating: 5,
        content: ''
      }
      showReviewDialog.value = false

      // 重新获取最热评论
      fetchHottestReview()

      ElMessage.success('书评发表成功')
    }
  } catch (error) {
    console.error('发表书评失败:', error)
    ElMessage.error('发表书评失败，请稍后再试')
  }
}

// 处理书评对话框关闭
const handleReviewDialogClose = () => {
  reviewForm.value = {
    rating: 5,
    content: ''
  }
  showReviewDialog.value = false
}



// 跳转到书籍详情页
const goToBook = (bookId) => {
  router.push(`/book/${bookId}`)
}

// 显示/隐藏全部书评
const goToAllReviews = () => {
  showAllReviews.value = !showAllReviews.value
  if (showAllReviews.value) {
    // 显示全部书评时，获取数据
    currentPage.value = 1
    fetchAllReviews()
  }
}

// 跳转到评论详情页
const goToCommentDetail = (commentId) => {
  // 书评类型为3
  router.push(`/comment/${commentId}?type=3`)
}

// 分页大小变化
const handleReviewSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchAllReviews()
}

// 当前页码变化
const handleReviewPageChange = (page) => {
  currentPage.value = page
  fetchAllReviews()
}

// 处理举报对话框关闭
const handleReportDialogClose = () => {
  // 重置举报表单
  reportForm.value = {
    reporterId: '',
    targetType: 2,
    targetId: '',
    reasonType: 1,
    reasonDetail: '',
    isAnonymous: 0 // 重置为不匿名
  }
  showReportDialog.value = false
}

// 举报评论
const reportReview = (reviewId) => {
  // 设置举报目标ID
  reportForm.value.targetId = reviewId
  // 设置举报者ID（如果用户已登录）
  if (authStore.isAuthenticated) {
    reportForm.value.reporterId = authStore.user.id
  }
  // 打开举报弹窗
  showReportDialog.value = true
}

// 提交举报
const submitReport = async () => {
  // 表单验证
  if (!reportFormRef.value) return

  try {
    await reportFormRef.value.validate()

    // 准备举报数据
    const reportData = {
      ...reportForm.value
    }

    // 如果是匿名举报，移除reporterId
    if (reportData.isAnonymous) {
      delete reportData.reporterId
    }

    // 调用举报API
    const response = await reportsApi.createReport(reportData)

    if (response && response.code === 200) {
      // 举报成功
      ElMessage.success('举报提交成功，感谢您的反馈')
      // 关闭举报弹窗
      showReportDialog.value = false
      // 重置表单
      handleReportDialogClose()
    } else {
      // 举报失败
      ElMessage.error(response?.message || '举报提交失败')
    }
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消验证
      return
    }
    console.error('提交举报失败:', error)
    ElMessage.error('举报提交失败，请稍后重试')
  }
}

// 删除评论
const deleteReview = async (reviewId) => {
  try {
    // 删除前的确认提示
    await ElMessageBox.confirm('确定要删除这条评论吗？删除后不可恢复。', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 调用删除评论API
    const response = await commentsApi.deleteComment(reviewId)

    if (response && response.code === 200) {
      // 删除成功提示
      ElMessage.success('评论删除成功')

      // 更新书评数据
      if (showAllReviews.value) {
        // 如果显示全部书评，重新获取书评列表
        fetchAllReviews()
      } else {
        // 否则重新获取最热评论
        fetchHottestReview()
      }
    } else {
      // 删除失败提示
      ElMessage.error(response?.message || '评论删除失败')
    }
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消删除
      return
    }
    console.error('删除评论失败:', error)
    ElMessage.error('评论删除失败，请稍后重试')
  }
}

// 格式化字数
const formatWordCount = (count) => {
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}万字`
  }
  return `${count}字`
}

// 格式化数字
const formatNumber = (num) => {
  // 检查num是否存在且为有效数字
  if (num === undefined || num === null || isNaN(num)) {
    return '0'
  }

  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}万`
  }
  return num.toString()
}

// 格式化时间
const formatTime = (time) => {
  // 检查time是否存在
  if (!time) {
    return '未知时间'
  }

  const now = new Date()
  const date = new Date(time)

  // 检查date是否为有效日期
  if (isNaN(date.getTime())) {
    return '未知时间'
  }

  const diff = now - date
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
    return date.toLocaleDateString()
  }
}

// 组件挂载时获取数据
onMounted(async () => {
  authStore.initAuth()
  favoriteStore.initFavorites()

  try {
    // 并行获取数据
    await Promise.all([
      fetchBookDetail(),
      fetchChapters()
    ])

    // 初始化最热评论
    if (activeTab.value === 'reviews') {
      await fetchHottestReview()
    }

    fetchRecommendations()
    loading.value = false

    // 调用增加浏览量函数（使用防抖）
    debouncedAddViewCount()

    // 检查书籍是否在书架中
    await checkBookInBookshelf()
  } catch (error) {
    console.error('初始化数据失败:', error)
    ElMessage.error('加载数据失败，请刷新页面重试')
    loading.value = false
  }
})</script>

<style scoped>
.book-detail-page {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
  margin-top: 60px; /* 添加顶部边距，确保内容从导航栏下方开始 */
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.book-detail-content {
  max-width: 1200px;
  margin: 0 auto;
}

.book-header {
  display: flex;
  gap: 30px;
  margin-bottom: 40px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.book-cover {
  position: relative;
  width: 200px;
  height: 280px;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-status {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 8px;
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 12px;
  border-radius: 4px;
}

.book-status.completed {
  background-color: #67c23a;
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 28px;
  margin-bottom: 20px;
  color: #333;
}

.book-meta {
  margin-bottom: 20px;
}

.meta-item {
  display: inline-block;
  margin-right: 20px;
  margin-bottom: 10px;
}

.label {
  color: #666;
}

.value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.book-stats {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.book-actions {
  display: flex;
  gap: 15px;
}

/* 导航栏样式 */
.tab-navigation {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 0;
  background-color: #fff;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.tab-item {
  padding: 15px 30px;
  cursor: pointer;
  font-size: 16px;
  color: #606266;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
  font-weight: 500;
}

.tab-item:hover {
  color: #409eff;
}

.tab-item.active {
  color: #409eff;
  border-bottom-color: #409eff;
  background-color: #f5f7fa;
}

/* 作品信息部分样式 */
.book-info-section {
  margin-bottom: 40px;
  padding: 30px;
  background-color: #fff;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-top: 0;
}

/* 章节和书评部分样式 */
.book-chapters, .book-reviews {
  margin-bottom: 40px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-top: 0;
}

.book-intro, .book-chapters, .book-reviews, .book-recommendations {
  margin-bottom: 40px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 10px;
}

.intro-content p {
  line-height: 1.8;
  margin-bottom: 15px;
  color: #333;
}

/* 章节控制区域样式 */
.chapter-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chapter-stats {
  font-size: 14px;
  color: #666;
}

.chapter-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.chapter-filter {
  width: 300px;
}

.sort-buttons {
  display: flex;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.sort-button {
  padding: 8px 15px;
  cursor: pointer;
  background-color: #fff;
  color: #606266;
  border-right: 1px solid #dcdfe6;
  transition: all 0.3s;
  font-size: 14px;
}

.sort-button:last-child {
  border-right: none;
}

.sort-button:hover {
  background-color: #f5f7fa;
}

.sort-button.active {
  background-color: #409eff;
  color: #fff;
}

/* 章节列表样式 - 两列布局 */
.chapter-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  max-height: none;
  overflow-y: visible;
  border: none;
  border-radius: 4px;
}

.chapter-item {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 12px 15px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.chapter-item:hover {
  background-color: #f5f7fa;
  border-color: #e0e0e0;
}

.chapter-title {
  color: #333;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
  line-clamp: 2; /* 标准属性，用于兼容性 */
}

.chapter-word-count {
  color: #999;
  font-size: 12px;
  flex-shrink: 0;
}

.chapter-pagination, .review-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.review-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.review-list {
  margin-bottom: 20px;
}

.review-item {
  padding: 20px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  margin-bottom: 15px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.reviewer-name {
  font-weight: 500;
  color: #333;
}

.review-time {
  font-size: 14px;
  color: #999;
}

.review-content {
  line-height: 1.6;
  margin-bottom: 15px;
  color: #333;
}

.review-actions {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  margin-top: 15px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
  cursor: pointer;
  font-size: 14px;
}

.action-item:hover {
  color: #409eff;
}

/* 新的书评部分样式 */
.review-header-section {
  margin-bottom: 20px;
}

.review-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.review-rating-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.rating-label {
  font-size: 16px;
  color: #333;
  margin-right: 10px;
}

.hottest-review-section {
  margin-bottom: 30px;
}

.hottest-review-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

.no-review {
  text-align: center;
  padding: 30px 0;
  color: #999;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 30px;
}

.view-all-reviews {
  text-align: center;
  margin-top: 20px;
}

/* 全部书评单独显示样式 */
.all-reviews-only {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 评分统计主要样式 */
.review-stats-main {
  margin: 30px 0;
  padding: 30px;
  background-color: #f5f7fa;
  border-radius: 12px;
}

/* 第一行：评分 + 星星 */
.rating-row-first {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

/* 评分样式 */
.rating-score {
  display: flex;
  align-items: baseline;
  font-weight: 600;
}

.score-value {
  font-size: 48px;
  color: #333;
  margin-right: 5px;
}

.score-unit {
  font-size: 24px;
  color: #666;
}

/* 星星样式 */
.rating-stars {
  display: flex;
}

/* 第二行：点评人数 + 提示文字 */
.rating-row-second {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

/* 点评人数 */
.rating-people {
  font-size: 18px;
  color: #666;
}

/* 提示文字 */
.rating-tip {
  font-size: 14px;
  color: #999;
}

/* 快速评分 */
.quick-rating-main {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 评论项样式调整 */
.review-item {
  position: relative;
  padding: 20px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 15px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.review-actions-menu {
  position: absolute;
  top: 15px;
  right: 15px;
  z-index: 10;
}

.review-footer {
  margin-top: 15px;
}

.review-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.review-rating {
  display: flex;
  align-items: center;
}

/* 确保点赞图标在评论数为0时仍在右下角 */
.review-actions {
  justify-content: space-between;
}

.review-reply-count {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}

.recommendation-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.recommendation-item {
  display: flex;
  flex-direction: column;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.recommendation-item:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.recommendation-item .book-cover {
  width: 100%;
  height: 140px;
  margin-bottom: 10px;
}

.recommendation-item .book-info {
  text-align: center;
}

.recommendation-item .book-title {
  font-size: 16px;
  margin-bottom: 5px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommendation-item .book-author,
.recommendation-item .book-category,
.recommendation-item .book-status {
  font-size: 12px;
  color: #666;
  margin-bottom: 3px;
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

  .book-detail-layout {
    flex-direction: column;
  }

  .book-main {
    width: 100%;
  }

  .book-sidebar {
    width: 100%;
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .container {
    max-width: 540px;
    padding: 0 15px;
  }

  .book-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .book-cover {
    width: 150px;
    height: 200px;
    margin-right: 0;
    margin-bottom: 15px;
  }

  .book-info {
    text-align: center;
  }

  .book-title {
    font-size: 20px;
  }

  .book-author {
    font-size: 14px;
  }

  .book-stats {
    justify-content: center;
  }

  .book-actions {
    justify-content: center;
  }

  .chapter-controls {
    flex-direction: column;
    gap: 15px;
  }

  .chapter-filter {
    width: 100%;
  }

  .review-controls {
    flex-direction: column;
    gap: 15px;
  }

  .recommendation-list {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}

@media (max-width: 576px) {
  .container {
    max-width: 100%;
    padding: 0 12px;
  }

  .book-detail-section {
    padding: 15px 0;
  }

  .book-cover {
    width: 120px;
    height: 160px;
  }

  .book-title {
    font-size: 18px;
  }

  .book-author {
    font-size: 13px;
  }

  .book-stats {
    flex-wrap: wrap;
    gap: 10px;
  }

  .book-actions {
    flex-direction: column;
    gap: 10px;
  }

  .book-actions .el-button {
    width: 100%;
  }

  .section-title {
    font-size: 18px;
    margin-bottom: 15px;
  }

  .intro-content {
    font-size: 14px;
  }

  .review-item {
    padding: 15px;
  }

  .review-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .recommendation-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .recommendation-item {
    padding: 10px;
  }

  .recommendation-item .book-cover {
    height: 120px;
  }

  .recommendation-item .book-title {
    font-size: 14px;
  }

  .recommendation-item .book-author,
  .recommendation-item .book-category,
  .recommendation-item .book-status {
    font-size: 11px;
  }
}
</style>

