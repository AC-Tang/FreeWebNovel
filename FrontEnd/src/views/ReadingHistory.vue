<template>
  <PageTransition>
    <div class="reading-history-page">
    <div class="page-header">
      <h1>阅读历史</h1>
      <div class="header-actions">
        <div class="view-toggle">
          <el-button-group>
            <el-button
              :type="viewMode === 'grid' ? 'primary' : 'default'"
              @click="viewMode = 'grid'"
            >
              <el-icon><Grid /></el-icon>
              图标
            </el-button>
            <el-button
              :type="viewMode === 'list' ? 'primary' : 'default'"
              @click="viewMode = 'list'"
            >
              <el-icon><List /></el-icon>
              列表
            </el-button>
          </el-button-group>
        </div>
        <el-button type="danger" plain @click="handleClearHistory">
          清空历史
        </el-button>
      </div>
    </div>

    <div class="history-content">
      <!-- 图标模式 -->
      <div v-if="viewMode === 'grid' && displayHistory.length > 0" class="grid-mode">
        <div class="grid-view">
          <div
            v-for="item in displayHistory"
            :key="item.bookId"
            class="grid-item"
            @click="goToBook(item.bookId)"
          >
            <div class="book-cover">
              <img :src="item.bookCover" :alt="item.bookTitle" class="cover-img">
            </div>
            <div class="book-info">
              <h4 class="book-name">{{ item.bookTitle }}</h4>
              <p class="book-author-name">
                <span class="clickable-text">{{ item.bookAuthor }}</span>·
                <span class="clickable-text">{{ item.categoryName }}</span>
              </p>
              <p class="book-description"><span>上次阅读：{{ item.chapterTitle }}</span></p>
              <div class="reading-meta">
                <span class="reading-progress">进度：{{ Math.round(item.progress * 100) }}%</span>
              </div>
              <div class="last-read-time">
                最后阅读：{{ formatTime(item.lastReadTime) }}
              </div>
              <div class="book-actions">
                <el-button type="primary" size="small" @click.stop="continueReading(item)">
                  继续阅读
                </el-button>
                <el-button
                  v-if="!bookshelfStatus[item.bookId]"
                  size="small"
                  class="hover-button"
                  @click.stop="addToBookshelf(item)"
                >
                  加入书架
                </el-button>
                <el-button size="small" class="hover-button" @click.stop="removeFromHistory(item)">
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 图标模式分页 -->
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

      <!-- 列表模式 -->
      <div v-else-if="viewMode === 'list' && displayHistory.length > 0" class="list-mode">
        <div class="list-view">
          <el-table :data="displayHistory" stripe style="width: 100%">
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column prop="bookTitle" label="书名" min-width="150">
              <template #default="scope">
                <span class="clickable-text">{{ scope.row.bookTitle }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="bookAuthor" label="作者" width="120" />
            <el-table-column prop="chapterTitle" label="上次阅读章节" min-width="150">
              <template #default="scope">
                <span>{{ scope.row.chapterTitle }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="statusName" label="连载状态" width="100" />
            <el-table-column prop="lastUpdateTime" label="最后更新时间" width="150">
              <template #default="scope">
                <span>{{ formatTime(scope.row.lastUpdateTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click.stop="continueReading(scope.row)">
                  继续阅读
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-history">
        <el-empty description="暂无阅读历史">
          <el-button type="primary" @click="goToHome">去首页找书</el-button>
        </el-empty>
      </div>
    </div>
    </div>
  </PageTransition>
</template>

<script setup name="ReadingHistoryPage">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Grid, List } from '@element-plus/icons-vue'
import { readingRecordsApi } from '../api/ReadingRecords'
import { bookshelvesApi } from '../api/Bookshelves'
import { getImageUrl } from '../utils/imageUtils'
import { useAuthStore } from '../stores/auth'
import PageTransition from '../components/PageTransition.vue'

const router = useRouter()
const authStore = useAuthStore()

// 显示模式：grid（图标）或 list（列表）
const viewMode = ref('grid')

// 阅读历史数据
const readingHistory = ref([])
const loading = ref(false)

// 书架状态记录（记录哪些书籍已在书架中）
const bookshelfStatus = ref({})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 监听阅读历史数据变化，更新总数
watch(() => readingHistory.value.length, (newLength) => {
  total.value = newLength
})

// 计算当前页显示的数据
const paginatedHistory = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return readingHistory.value.slice(start, end)
})

// 显示的历史记录
const displayHistory = computed(() => {
  return paginatedHistory.value
})

// 继续阅读
const continueReading = (item) => {
  goToChapter(item.bookId, item.chapterId)
}

// 跳转到书籍详情页
const goToBook = (bookId) => {
  router.push(`/book/${bookId}`)
}

// 跳转到章节阅读页
const goToChapter = (bookId, chapterId) => {
  // 在新标签页打开阅读页面
  window.open(`/read/${bookId}/${chapterId}`, '_blank')
}

// 跳转到首页
const goToHome = () => {
  router.push('/')
}



// 清空所有历史记录
const handleClearHistory = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有阅读历史吗？此操作不可恢复！',
      '清空历史',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const userId = authStore.user?.id
    if (!userId) {
      ElMessage.error('请先登录')
      return
    }

    // 调用实际的删除全部API
    await readingRecordsApi.deleteAllReadingRecords(userId)

    // 清空本地数据
    readingHistory.value = []
    bookshelfStatus.value = {}

    ElMessage.success('历史记录已清空')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空历史记录失败')
      console.error('清空历史记录失败:', error)
    }
  }
}

// 检查书籍是否在书架中
const checkBookshelfStatus = async (bookId) => {
  if (!bookId || !authStore.user?.id) return false

  try {
    const response = await bookshelvesApi.existInBookshelf(bookId, authStore.user.id)
    return response.data === true
  } catch (error) {
    console.error('检查书架状态失败:', error)
    return false
  }
}

// 添加到书架
const addToBookshelf = async (item) => {
  try {
    const userId = authStore.user?.id
    if (!userId) {
      ElMessage.error('请先登录')
      return
    }

    // 调用实际的添加到书架API
    await bookshelvesApi.addBookToShelves(item.bookId, userId)

    // 更新本地状态
    bookshelfStatus.value[item.bookId] = true
    ElMessage.success(`《${item.bookTitle}》已添加到书架`)
  } catch (error) {
    ElMessage.error('添加到书架失败')
    console.error('添加到书架失败:', error)
  }
}

// 从阅读历史中删除
const removeFromHistory = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要从阅读历史中删除《${item.bookTitle}》吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 调用实际的删除API - 需要获取记录ID
    // 假设item中有记录ID，如果没有则需要从API获取
    if (item.id) {
      await readingRecordsApi.deleteReadingRecord(item.id)
    } else {
      // 如果没有recordId，可能需要调用其他API或先获取记录ID
      console.warn('缺少记录ID，无法调用删除API')
    }

    // 从本地数据中移除
    const index = readingHistory.value.findIndex(record => record.bookId === item.bookId)
    if (index > -1) {
      readingHistory.value.splice(index, 1)
    }

    ElMessage.success('已从阅读历史中删除')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除阅读历史失败:', error)
    }
  }
}

// 格式化时间
const formatTime = (timeString) => {
  const time = new Date(timeString)
  const now = new Date()
  const diff = now - time
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
    return time.toLocaleDateString()
  }
}

// 从后端获取阅读记录
const fetchReadingHistory = async () => {
  loading.value = true
  try {
    // 这里假设userId可以从某种方式获取，比如store或localStorage
    const userId = authStore.user.id
    const response = await readingRecordsApi.getUserReadingRecords(userId)
    // console.log('API response:', response)
    // 处理API返回的数据，映射到组件需要的格式
    readingHistory.value = response.data.map(item => ({
      id:item.id,
      bookId: item.bookId,
      bookTitle: item.bookName,
      chapterId: item.chapterId,
      chapterTitle: item.chapterName,
      bookAuthor: item.authorName,
      bookCover: getImageUrl(item.bookCover),
      categoryName: item.categoryName,
      lastUpdateTime: item.lastUpdateTime,
      statusName: item.statusName,
      progress: item.readPercentage / 100,
      lastReadTime: item.readTime
    }))
    // console.log('Fetched reading history:', readingHistory.value)

    // 检查每本书是否在书架中
    for (const item of readingHistory.value) {
      const isInBookshelf = await checkBookshelfStatus(item.bookId)
      bookshelfStatus.value[item.bookId] = isInBookshelf
    }

  } catch (error) {
    ElMessage.error('获取阅读记录失败')
    console.error('Error fetching reading history:', error)
  } finally {
    loading.value = false
  }
}

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page
}

// 组件挂载时初始化
onMounted(() => {
  fetchReadingHistory()
})
</script>

<style scoped>
.reading-history-page {
  padding: 80px 20px 20px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 120px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.view-toggle {
  margin-right: 10px;
}

.clickable-text {
  cursor: pointer;
  color: #409eff;
}

.clickable-text:hover {
  text-decoration: underline;
}

.filter-tabs {
  margin-bottom: 20px;
}

.tab-description {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
}

.history-list {
  margin-bottom: 20px;
}

.book-history-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.book-history-item {
  display: flex;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.book-history-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.book-cover {
  width: 100px;
  height: 140px;
  flex-shrink: 0;
  margin-right: 20px;
  border-radius: 4px;
  overflow: hidden;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.book-title {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: #333;
}

.book-author {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.reading-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.last-chapter, .reading-progress, .last-read-time {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.book-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
}

.chapter-history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chapter-history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.chapter-history-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.chapter-info {
  flex: 1;
}

.chapter-title {
  margin: 5px 0;
  font-weight: 500;
  color: #333;
}

.reading-meta {
  display: flex;
  gap: 15px;
  margin-top: 5px;
}

.reading-progress, .read-time {
  font-size: 13px;
  color: #666;
}

.chapter-actions {
  display: flex;
  gap: 10px;
}

.grid-view {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 20px;
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
  flex-wrap: wrap;
}

.grid-item .book-actions .el-button {
  flex: 1;
  padding: 6px 10px;
  font-size: 12px;
  min-width: 80px;
}

/* 按钮悬停交互效果 */
.grid-item .book-actions {
  display: flex;
  flex-direction: row;
  gap: 8px;
  margin-top: auto;
}

.grid-item .book-actions .hover-button {
  opacity: 0;
  transition: opacity 0.3s ease;
  width: 0;
  padding: 6px 0;
  margin: 0;
  overflow: hidden;
  flex: none;
}

.grid-item:hover .book-actions .hover-button {
  opacity: 1;
  width: auto;
  padding: 6px 10px;
  margin: 0;
  flex: 1;
}

.grid-item .book-actions .el-button[type="primary"] {
  opacity: 1;
  transition: all 0.3s ease;
  flex: 1;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  margin-bottom: 20px;
}

/* 列表展示样式 */
.list-view {
  margin-bottom: 20px;
}

.list-view .el-table {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

/* 空状态 */
.empty-history {
  padding: 40px 0;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .reading-history-page {
    max-width: 960px;
  }

  .grid-view {
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  }

  .grid-item {
    height: 180px;
  }

  .grid-item .book-cover {
    flex: 0 0 130px;
  }
}

@media (max-width: 1200px) {
  .grid-view {
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  }
}

@media (max-width: 992px) {
  .grid-view {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 15px;
  }

  .grid-item {
    min-height: 190px;
  }

  .grid-item .book-cover {
    flex: 0 0 130px;
  }
}

@media (max-width: 768px) {
  .grid-view {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .grid-item {
    flex-direction: column;
    min-height: 180px;
  }

  .grid-item .book-cover {
    width: 100%;
    height: 180px;
    flex: none;
  }

  .grid-item .book-info {
    padding: 12px;
  }

  .grid-item .book-name {
    font-size: 16px;
    margin-bottom: 8px;
  }

  .grid-item .book-actions .el-button {
    padding: 5px 8px;
    font-size: 11px;
    min-width: 70px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .view-toggle {
    margin-right: 0;
    margin-bottom: 10px;
  }
}

@media (max-width: 992px) {
  .reading-history-page {
    max-width: 720px;
  }

  .book-cover {
    width: 90px;
    height: 130px;
    margin-right: 15px;
  }

  .book-title {
    font-size: 17px;
  }

  .book-author {
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .reading-history-page {
    max-width: 540px;
    padding: 15px;
  }

  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .page-header h1 {
    font-size: 22px;
  }

  .filter-tabs {
    margin-bottom: 15px;
  }

  .tab-description {
    font-size: 13px;
    margin-bottom: 12px;
  }

  .book-history-item {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 15px;
  }

  .book-cover {
    width: 100px;
    height: 140px;
    margin-right: 0;
    margin-bottom: 15px;
  }

  .book-info {
    width: 100%;
  }

  .book-title {
    font-size: 16px;
    margin-bottom: 4px;
  }

  .book-author {
    font-size: 13px;
    margin-bottom: 8px;
  }

  .reading-info {
    gap: 3px;
  }

  .last-chapter, .reading-progress, .last-read-time {
    font-size: 13px;
  }

  .book-actions {
    flex-direction: row;
    width: 100%;
    margin-top: 10px;
  }

  .chapter-history-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    padding: 12px 15px;
  }

  .chapter-title {
    font-size: 15px;
  }

  .reading-meta {
    flex-direction: column;
    gap: 5px;
  }

  .reading-progress, .read-time {
    font-size: 12px;
  }

  .chapter-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .empty-history {
    padding: 30px 0;
  }
}

@media (max-width: 576px) {
  .reading-history-page {
    max-width: 100%;
    padding: 12px;
  }

  .page-header h1 {
    font-size: 20px;
  }

  .filter-tabs {
    margin-bottom: 12px;
  }

  .tab-description {
    font-size: 12px;
    margin-bottom: 10px;
  }

  .book-history-item {
    padding: 12px;
  }

  .book-cover {
    width: 90px;
    height: 120px;
    margin-bottom: 12px;
  }

  .book-title {
    font-size: 15px;
  }

  .book-author {
    font-size: 12px;
  }

  .last-chapter, .reading-progress, .last-read-time {
    font-size: 12px;
  }

  .chapter-history-item {
    padding: 10px 12px;
  }

  .chapter-title {
    font-size: 14px;
  }

  .reading-progress, .read-time {
    font-size: 11px;
  }

  .empty-history {
    padding: 25px 0;
  }
}
</style>
