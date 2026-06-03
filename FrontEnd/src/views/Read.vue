<template>
  <div class="reader-page" :class="readingSettings.backgroundColor">
    <div class="reader-container">
      <!-- 阅读内容区 -->
      <div class="reader-content">
        <div class="chapter-content" :style="contentStyle">
          <h2 class="chapter-title">
            {{ chapterInfo.title }}
            <!-- 打卡弹幕按钮 -->
            <el-button
              v-if="readingSettings.checkInDanmaku"
              type="text"
              class="check-in-button"
              @click="handleCheckInClick"
            >
              <el-icon><ChatSquare /></el-icon>
              {{ isCheckedIn ? `弹幕 (${danmakuCount})` : '打卡' }}
            </el-button>
          </h2>
          <div class="content-text">
            <!-- 遍历段落数组渲染内容 -->
            <div
              v-for="(paragraph, index) in paragraphs"
              :key="paragraph.id"
              class="paragraph-item"
            >
              <p
                class="paragraph-text"
                @click.stop="readingSettings.paragraphComments && showParagraphCommentSection(index)"
              >
                {{ paragraph.text }}
                <!-- 段评数显示，仅在开启段评功能时显示 -->
                <span
                  v-if="readingSettings.paragraphComments && paragraph.commentCount > 0"
                  class="paragraph-comment-count"
                >
                  <el-icon><ChatDotRound /></el-icon>
                  <span>{{ paragraph.commentCount }}</span>
                </span>
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- 悬浮导航栏 -->
      <div class="floating-nav">
        <div class="nav-item" @click="showChapterSidebar = true">
          <el-icon><Menu /></el-icon>
          <span>目录</span>
        </div>
        <div class="nav-item" @click="showSettings = true">
          <el-icon><Setting /></el-icon>
          <span>设置</span>
        </div>
        <div class="nav-item" @click="goToBookDetail">
          <el-icon><Document /></el-icon>
          <span>书详情</span>
        </div>
        <div class="nav-item" @click="handleShowComments">
          <el-icon><ChatDotRound /></el-icon>
          <span>评论</span>
        </div>
        <div class="nav-item" @click="toggleBookshelf">
          <el-icon><Collection /></el-icon>
          <span>{{ isInBookshelf ? '移除' : '加入书架' }}</span>
        </div>
        <div class="nav-item scroll-top" v-show="showScrollTop" @click="scrollToTop">
          <el-icon><Top /></el-icon>
          <span>置顶</span>
        </div>
      </div>

      <!-- 评论弹窗 -->
      <div class="comments-modal" :class="{ 'show': showComments }">
        <div class="modal-overlay" @click="showComments = false"></div>
        <div class="modal-content">
          <div class="modal-header">
            <h3>评论</h3>
            <el-button text @click="showComments = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <div class="modal-body">
            <CommentSection target-type="chapter" :book-id="Number(bookId)" :chapter-id="Number(chapterId)" />
          </div>
        </div>
      </div>

    <!-- 打卡弹幕界面 -->
    <div v-if="showCheckInDanmaku" class="danmaku-overlay">
      <!-- 背景遮罩 -->
      <div class="danmaku-backdrop" @click="closeCheckInDanmaku"></div>

      <!-- 顶部打卡成功信息 -->
      <div v-if="checkInInfo" class="check-in-success">
        <div class="check-in-badge">
          <el-avatar :size="50" :src="checkInInfo?.avatar ? `data:image/jpeg;base64,${checkInInfo.avatar}` : 'https://picsum.photos/seed/user/50/50.jpg'" />
        </div>
        <div class="check-in-message">
          <h4>本章打卡成功 NO.{{ checkInInfo?.ranks || 0 }}</h4>
        </div>
      </div>

      <!-- 循环滚动弹幕层 -->
      <div class="danmaku-layer" v-if="danmakuList.length">
        <div
          v-for="(danmaku, index) in danmakuList"
          :key="danmaku.id || index"
          class="danmaku-item"
          :style="getLoopDanmakuStyle(index)"
        >
          <el-avatar
            :size="20"
            :src="danmaku.avatar ? `data:image/jpeg;base64,${danmaku.avatar}` : 'https://picsum.photos/seed/user/20/20.jpg'"
          />
          <span class="danmaku-username">
            {{ danmaku.username || danmaku.user?.username || '匿名用户' }}
          </span>
          <span class="danmaku-content">
            {{ danmaku.content }}
          </span>
        </div>
      </div>

      <!-- 底部弹幕输入 -->
      <div class="danmaku-input-container">
        <el-input
          v-model="danmakuInput"
          placeholder="一起愉快的发弹幕(๑•̀ㅂ•́)و✧"
          maxlength="100"
          show-word-limit
          @keyup.enter="sendDanmaku"
          class="danmaku-input"
        >
          <template #append>
            <el-button type="primary" @click="sendDanmaku" :loading="danmakuLoading" :disabled="!danmakuInput.trim()">发送</el-button>
          </template>
        </el-input>
        <el-button text @click="closeCheckInDanmaku" class="close-danmaku-btn">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 段评弹窗 -->
    <div class="comments-modal" :class="{ 'show': showParagraphComments }">
        <div class="modal-overlay" @click="closeParagraphComments"></div>
        <div class="modal-content">
          <div class="modal-header">
            <h3>段落评论</h3>
            <div class="modal-actions">
              <el-button text @click="refreshParagraphComments">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
              <el-button text @click="closeParagraphComments">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="modal-body">
            <!-- 当前段落内容预览 -->
            <div v-if="selectedParagraphIndex >= 0 && paragraphs[selectedParagraphIndex]" class="paragraph-preview">
              <h4>当前段落</h4>
              <p>{{ paragraphs[selectedParagraphIndex].text }}</p>
            </div>

            <!-- 发表段评 - 固定位置 -->
            <div v-if="isAuthenticated" class="comment-form">
              <el-input
                v-model="newParagraphComment"
                type="textarea"
                :rows="3"
                placeholder="发表你的段评..."
                maxlength="200"
                show-word-limit
              />
              <div class="comment-actions">
                <el-button type="primary" @click="submitParagraphComment" :loading="submittingParagraphComment">发表段评</el-button>
              </div>
            </div>
            <div v-else class="login-prompt">
              <p>登录后才能发表段评</p>
              <el-button type="primary" @click="goToLogin">去登录</el-button>
            </div>

            <!-- 段落评论区 - 可滚动 -->
            <div class="paragraph-comments">
              <h4>全部评论 {{ selectedParagraphIndex >= 0 && paragraphs[selectedParagraphIndex] ? paragraphs[selectedParagraphIndex].comments.length : 0 }}</h4>
              <div class="comment-list-wrapper">
                <div v-if="selectedParagraphIndex >= 0 && paragraphs[selectedParagraphIndex].comments.length > 0" class="comment-list">
                  <div
                    v-for="comment in paragraphs[selectedParagraphIndex].comments"
                    :key="comment.id || comment._id"
                    class="comment-item"
                  >
                    <!-- 段评头部信息 -->
                    <div class="comment-header">
                      <div class="comment-user">
                        <el-avatar :size="30" :src="comment.avatar?`data:image/png;base64,${comment.avatar}`:'https://picsum.photos/seed/default_user/40/40.jpg'" />
                        <div class="user-info">
                          <div class="username">{{ comment.username || '匿名用户' }}</div>
                          <div class="comment-time">{{ comment.createdAt ? formatTime(new Date(comment.createdAt)) : '刚刚' }}</div>
                        </div>
                      </div>
                      <!-- 操作菜单 -->
                      <div class="comment-actions-menu">
                        <el-dropdown>
                          <el-button type="text" circle>
                            <el-icon><More /></el-icon>
                          </el-button>
                          <template #dropdown>
                            <el-dropdown-menu>
                              <el-dropdown-item @click.stop="handleReportClick(comment.id)">举报</el-dropdown-item>
                              <el-dropdown-item
                                v-if="authStore.isAuthenticated && comment.userId === authStore.userId"
                                @click.stop="deleteComment(comment.id)"
                                style="color: #f56c6c;"
                              >
                                删除
                              </el-dropdown-item>
                            </el-dropdown-menu>
                          </template>
                        </el-dropdown>
                      </div>
                    </div>
                    <!-- 段评内容 -->
                    <div class="comment-content-wrapper">
                      <div class="comment-content" @click.stop="goToCommentDetail(comment.id)">
                        {{ comment.content }}
                      </div>
                    </div>
                    <!-- 段评底部操作 -->
                    <div class="comment-footer">
                      <div class="comment-actions-left">
                        <!-- 全部回复显示 -->
                        <div v-if="comment.replyCount > 0" class="all-replies" @click.stop="goToCommentDetail(comment.id)">
                          全部{{ comment.replyCount }}条回复>
                        </div>
                      </div>
                      <div class="comment-actions-right">
                        <div
                          class="action-item"
                          :class="{ 'active': comment.isLiked }"
                          @click.stop="toggleLike(comment)"
                        >
                          <el-icon><Star /></el-icon>
                          <span>{{ comment.likeCount || 0 }}</span>
                        </div>
                        <!-- 回复按钮 -->
                        <div class="action-item reply-action"
                             @click.stop="goToCommentDetail(comment.id)">
                          <el-icon><ChatDotRound /></el-icon>
                          <span>回复</span>
                        </div>
                      </div>
                    </div>
                    <!-- 回复列表 -->
                    <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
                      <div v-for="reply in comment.replies" :key="reply.id || reply._id" class="reply-item">
                        <div class="reply-header">
                          <div class="reply-user">
                            <el-avatar :size="24" :src="reply.avatar || 'https://picsum.photos/seed/default_user/40/40.jpg'" />
                            <div class="user-info">
                              <div class="username">{{ reply.username || '匿名用户' }}</div>
                              <div class="reply-time">{{ reply.createTime ? formatTime(new Date(reply.createTime)) : '刚刚' }}</div>
                            </div>
                          </div>
                          <!-- 回复操作菜单 -->
                          <div class="reply-actions-menu">
                            <el-dropdown>
                              <el-button type="text" circle size="small">
                                <el-icon><More /></el-icon>
                              </el-button>
                              <template #dropdown>
                                <el-dropdown-menu>
                                  <el-dropdown-item @click.stop="handleReportClick(reply.id)">举报</el-dropdown-item>
                                  <el-dropdown-item
                                    v-if="authStore.isAuthenticated && reply.userId === authStore.userId"
                                    @click.stop="deleteComment(reply.id)"
                                    style="color: #f56c6c;"
                                  >
                                    删除
                                  </el-dropdown-item>
                                </el-dropdown-menu>
                              </template>
                            </el-dropdown>
                          </div>
                        </div>
                        <div class="reply-content">
                          <p>{{ reply.content }}</p>
                        </div>
                        <div class="reply-footer">
                          <div class="reply-actions">
                            <div
                              class="action-item"
                              :class="{ 'active': reply.isLiked }"
                              @click.stop="toggleLike(reply)"
                            >
                              <el-icon><Star /></el-icon>
                              <span>{{ reply.likeCount || 0 }}</span>
                            </div>
                            <div class="action-item reply-action"
                                 @click.stop="replyToComment(reply, comment)">
                              <el-icon><ChatDotRound /></el-icon>
                              <span>回复</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else class="no-comments">
                  暂无评论，快来抢沙发吧！
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部工具栏悬浮触发区域 -->
      <div class="footer-hover-trigger"></div>

      <!-- 底部固定工具栏 -->
      <div class="reader-footer">
        <div class="chapter-navigation">
          <el-button
            :disabled="!hasPrevChapter"
            @click="prevChapter"
            class="nav-button"
          >
            上一章
          </el-button>
          <el-button class="chapter-select" @click="toggleChapterSidebar">
            目录
          </el-button>
          <el-button
            :disabled="!hasNextChapter"
            @click="nextChapter"
            class="nav-button"
          >
            下一章
          </el-button>
        </div>
      </div>

      <!-- 阅读设置面板 -->
      <div class="reader-settings" :class="{ 'show': showSettings }">
        <div class="settings-panel">
          <div class="settings-header">
            <h3>阅读设置</h3>
            <el-button text @click="showSettings = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>

          <div class="settings-content">
            <!-- 阅读背景 -->
            <div class="setting-item">
              <div class="setting-label">阅读背景</div>
              <div class="setting-control">
                <div class="color-options">
                  <div
                    v-for="color in backgroundColors"
                    :key="color.value"
                    class="color-option"
                    :class="{ 'active': readingSettings.backgroundColor === color.value }"
                    :style="{ backgroundColor: color.bgColor }"
                    @click="selectBackgroundColor(color.value)"
                  >
                    <span class="color-name">{{ color.name }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 字体设置 -->
            <div class="setting-item">
              <div class="setting-label">字体类型</div>
              <div class="setting-control">
                <div class="font-options">
                  <div
                    v-for="font in fontOptions"
                    :key="font.value"
                    class="font-option"
                    :class="{ 'active': readingSettings.fontFamily === font.value }"
                    @click="selectFontFamily(font.value)"
                  >
                    {{ font.name }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 字体大小 -->
            <div class="setting-item">
              <div class="setting-label">字体大小</div>
              <div class="setting-control">
                <div class="adjust-control">
                  <el-button
                    :icon="Minus"
                    @click="decreaseFontSize"
                    :disabled="readingSettings.fontSize <= 16"
                    size="small"
                  />
                  <span class="adjust-value">{{ readingSettings.fontSize }}</span>
                  <el-button
                    :icon="Plus"
                    @click="increaseFontSize"
                    :disabled="readingSettings.fontSize >= 30"
                    size="small"
                  />
                </div>
              </div>
            </div>

            <!-- 页面宽度 -->
            <div class="setting-item">
              <div class="setting-label">页面宽度</div>
              <div class="setting-control">
                <div class="width-options">
                  <div
                    v-for="width in pageWidthOptions"
                    :key="width"
                    class="width-option"
                    :class="{ 'active': readingSettings.pageWidth === width }"
                    @click="selectPageWidth(width)"
                  >
                    {{ width }}
                  </div>
                </div>
              </div>
            </div>



            <!-- 段评开关 -->
            <div class="setting-item">
              <div class="setting-label">段评功能</div>
              <div class="setting-control">
                <el-switch
                  v-model="readingSettings.paragraphComments"
                  @change="saveReadingSettings"
                />
              </div>
            </div>

            <!-- 自动书签 -->
            <div class="setting-item">
              <div class="setting-label">自动书签</div>
              <div class="setting-control">
                <el-switch
                  v-model="readingSettings.autoBookmark"
                  @change="saveReadingSettings"
                />
              </div>
            </div>

            <!-- 打卡弹幕 -->
            <div class="setting-item">
              <div class="setting-label">打卡弹幕</div>
              <div class="setting-control">
                <el-switch
                  v-model="readingSettings.checkInDanmaku"
                  @change="saveReadingSettings"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 目录侧边栏遮罩层 -->
      <div
        class="sidebar-overlay"
        :class="{ 'show': showChapterSidebar }"
        @click="showChapterSidebar = false"
      ></div>

      <!-- 目录侧边栏 -->
      <div class="chapter-sidebar" :class="{ 'show': showChapterSidebar }">
        <div class="sidebar-header">
          <h3>目录</h3>
          <div class="sidebar-header-actions">
            <el-button text @click="toggleChapterSort" class="sort-button">
              <el-icon><Sort /></el-icon>
              {{ chapterSortOrder === 'asc' ? '升序' : '倒序' }}
            </el-button>
            <el-button text @click="showChapterSidebar = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
        <div class="chapter-list" ref="chapterListRef" @scroll="checkCurrentChapterVisible">
          <div
            v-for="chapter in sortedChapters"
            :key="chapter.id"
            class="chapter-item"
            :class="{ 'active': chapter.sortOrder == route.params.chapterId }"
            @click="selectChapter(chapter)"
            :ref="el => setChapterRef(el, chapter.sortOrder)"
          >
            {{ chapter.title }}
          </div>
        </div>
        <!-- 定位按钮 -->
        <div
          class="chapter-locate-button"
          :class="{ 'show': showChapterLocate }"
          @click="locateCurrentChapter"
        >
          <el-icon><Position /></el-icon>
          定位到当前章节
        </div>
      </div>
    </div>
  </div>

  <!-- 举报对话框 -->
  <el-dialog v-model="showReportDialog" title="举报评论" width="500px" @close="handleReportDialogClose" :z-index="10001">
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
</template>

<script setup name="ReadPage">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Collection, Setting, Close, Menu, Document, ChatDotRound, Top, Sort, Plus, Minus, Position, Refresh } from '@element-plus/icons-vue'
import { useFavoriteStore } from '../stores/favorite'
import { useReadingHistoryStore } from '../stores/readingHistory'
import { chaptersApi } from '../api/Chapters'
import { commentsApi } from '../api/Comment'
import { bookshelvesApi } from '../api/Bookshelves'
import { readingRecordsApi } from '../api/ReadingRecords'
import CommentSection from '../components/CommentSection.vue'
import { likesApi } from '../api/Like'
import { reportsApi } from '../api/Reports'
import { checkInsApi } from '../api/CheckIns'
import { danmakuApi } from '../api/Danmaku'

const route = useRoute()
const router = useRouter()

// 获取store实例
const authStore = useAuthStore()
const favoriteStore = useFavoriteStore()
const readingHistoryStore = useReadingHistoryStore()

// 获取书籍ID和章节ID
const bookId = computed(() => route.params.bookId)
const chapterId = computed(() => route.params.chapterId)

// 书籍信息
const bookInfo = ref({
  id: '',
  title: '',
  author: '',
  category: ''
})

// 章节信息
const chapterInfo = ref({
  id: '',
  title: '',
  content: '',
  wordCount: 0,
  dbId: '',
})

// 段落数据，包含内容和评论数
const paragraphs = ref([])
// 当前选中的段落索引
const selectedParagraphIndex = ref(-1)
// 段评弹窗显示状态
const showParagraphComments = ref(false)

// 举报对话框相关状态
const showReportDialog = ref(false)
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

// 章节列表
const chapters = ref([])
const currentChapterIndex = ref(0)

// 是否有上一章/下一章
const hasPrevChapter = computed(() => currentChapterIndex.value > 0)
const hasNextChapter = computed(() => currentChapterIndex.value < chapters.value.length - 1)

// UI状态
const showFooter = ref(true)
const showSettings = ref(false)
const showChapterSidebar = ref(false)
const showComments = ref(false)
const showScrollTop = ref(false)
const showChapterLocate = ref(false)

// 段评相关
const newParagraphComment = ref('')
const submittingParagraphComment = ref(false)

// 当前用户
const isAuthenticated = computed(() => authStore.isAuthenticated)

// 章节排序状态
const chapterSortOrder = ref('asc') // 'asc' 升序, 'desc' 倒序

// 章节列表引用
const chapterListRef = ref(null)
const chapterRefs = ref({})

// 是否已在书架
const isInBookshelf = ref(false)

// 打卡弹幕相关状态
const showCheckInDanmaku = ref(false)
const checkInInfo = ref(null)
const danmakuList = ref([])
const danmakuInput = ref('')
const checkInLoading = ref(false)
const danmakuLoading = ref(false)
const isCheckedIn = ref(false)
const danmakuCount = ref(0)

// 阅读设置
const readingSettings = ref({
  fontSize: 18,
  lineHeight: 1.8,
  backgroundColor: 'white',
  fontFamily: 'default',
  autoBookmark: true,
  pageWidth: 1000,
  paragraphComments: true,
  checkInDanmaku: true
})

// 背景颜色选项
const backgroundColors = [
  { name: '纯白', value: 'white', bgColor: '#ffffff' },
  { name: '护眼', value: 'sepia', bgColor: '#f7f3e9' },
  { name: '浅灰', value: 'light-gray', bgColor: '#f5f5f5' },
  { name: '米黄', value: 'beige', bgColor: '#f5f5dc' },
  { name: '浅绿', value: 'light-green', bgColor: '#f0f8f0' },
  { name: '纯黑', value: 'dark', bgColor: '#1a1a1a' }
]

// 字体选项
const fontOptions = [
  { name: '黑体', value: 'SimHei' },
  { name: '雅黑', value: 'Microsoft YaHei' },
  { name: '宋体', value: 'SimSun' },
  { name: '楷体', value: 'KaiTi' }
]



// 页面宽度选项
const pageWidthOptions = [640, 800, 900, 1000, 1280]

// 行间距选项


// 内容样式
const contentStyle = computed(() => {
  const fontFamilyMap = {
    'default': 'system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    'SimHei': 'SimHei, "黑体", sans-serif',
    'Microsoft YaHei': '"Microsoft YaHei", "微软雅黑", sans-serif',
    'SimSun': 'SimSun, "宋体", serif',
    'KaiTi': 'KaiTi, "楷体", serif'
  }

  const fontFamily = fontFamilyMap[readingSettings.value.fontFamily] || fontFamilyMap.default

  return {
    fontSize: `${readingSettings.value.fontSize}px`,
    lineHeight: readingSettings.value.lineHeight,
    fontFamily,
    maxWidth: `${readingSettings.value.pageWidth}px`,
    margin: '0 auto'
  }
})

// 获取书籍信息
const fetchBookInfo = async () => {
  // 模拟API请求获取书籍信息
  const mockBookInfo = {
    id: bookId.value || '1',
    title: '玄幻小说',
    author: '知名作者',
    category: '玄幻'
  }
  bookInfo.value = mockBookInfo

  // 检查是否已在书架（仅当用户登录时）
  if (authStore.isLoggedIn) {
    try {
      const response = await bookshelvesApi.existInBookshelf(bookId.value, authStore.userId)
      if (response.code === 200) {
        isInBookshelf.value = response.data
      } else {
        console.error('检查书架状态失败:', response.message)
        isInBookshelf.value = false
      }
    } catch (error) {
      console.error('检查书架状态出错:', error)
      isInBookshelf.value = false
    }
  }
}

// 获取章节列表
const fetchChapters = async () => {
  try {
    const response = await chaptersApi.getChaptersByNovelId(bookId.value)
    if (response.code === 200) {
      chapters.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取章节列表失败')
      // 如果API调用失败，使用模拟数据作为备用
      const mockChapters = []
      const chapterCount = 150

      for (let i = 1; i <= chapterCount; i++) {
        mockChapters.push({
          id: `chapter_${i}`,
          sortOrder: i,
          title: `第${i}章 ${i === 1 ? '初入玄幻' : i === chapterCount ? '大结局' : '冒险继续'}`
        })
      }

      chapters.value = mockChapters
    }
  } catch (error) {
    console.error('获取章节列表出错:', error)
    ElMessage.error('获取章节列表失败')

    // 如果API调用失败，使用模拟数据作为备用
    const mockChapters = []
    const chapterCount = 150

    for (let i = 1; i <= chapterCount; i++) {
      mockChapters.push({
        id: `chapter_${i}`,
        sortOrder: i,
        title: `第${i}章 ${i === 1 ? '初入玄幻' : i === chapterCount ? '大结局' : '冒险继续'}`
      })
    }

    chapters.value = mockChapters
  }
}

// 获取章节内容
const fetchChapterContent = async (chapterId) => {
  try {
    // 从路由参数获取章节顺序
    const chapterOrder = route.params.chapterId || 1

    // 从章节列表中找到当前章节的索引（使用sortOrder而不是id）
    const chapterIndex = chapters.value.findIndex(ch => ch.sortOrder == chapterOrder)
    if (chapterIndex !== -1) {
      currentChapterIndex.value = chapterIndex
    }

    // 调用API获取章节详情
    const response = await chaptersApi.getChapterDetail(bookId.value, chapterOrder)

    if (response && response.code === 200 && response.data) {
      // 保存原始内容
      const content = response.data.content || ''

      // 处理内容，将其分割为段落数组
      const paragraphArray = content.split(/\r?\n/).filter(p => p.trim())

      // 为每个段落初始化数据结构
      paragraphs.value = paragraphArray.map((text, index) => ({
        id: `paragraph_${index}`,
        text: text,
        commentCount: 0, // 初始化为0，后续从API获取
        comments: [] // 段落评论列表
      }))

      try {
        // 调用API一次获取所有段落评论，传入段落总数
        const commentResponse = await commentsApi.getCommentsByParagraphId(
          bookId.value,
          chapterOrder,
          paragraphs.value.length
        )

        if (commentResponse && commentResponse.code === 200) {
          // 处理返回的双重列表数据
          const paragraphsComments = commentResponse.data || []

          // 遍历每个段落的评论数据
          paragraphsComments.forEach((paragraphData, index) => {
            if (index < paragraphs.value.length) {
              // 计算该段落的评论总数：每个段评的replyCount加起来再加上本身数量
              const totalComments = paragraphData.reduce((sum, comment) => {
                return sum + 1 + (comment.replyCount || 0)
              }, 0)

              // 更新段落评论数和评论列表
              paragraphs.value[index].commentCount = totalComments
              paragraphs.value[index].comments = paragraphData
            }
          })
        }
      } catch (error) {
        console.error('获取段落评论失败:', error)
        // 失败时保持默认值0
      }

      chapterInfo.value = {
        id: chapterId,
        title: response.data.title || '章节标题',
        content: content, // 保存原始内容
        wordCount: response.data.wordCount || 0,
        dbId: response.data.id,
      }
    } else {
      console.error('API返回异常:', response)
      ElMessage.error(response?.message || '获取章节内容失败')
    }
  } catch (error) {
    console.error('获取章节内容出错:', error)
    ElMessage.error('获取章节内容失败')
  }

  // 记录阅读历史（仅当用户已登录时）
  if (authStore.isLoggedIn) {
    try {
      const readingRecordData = {
        userId: authStore.userId,
        bookId: bookId.value,
        chapterId: chapterId,
      }
      await readingRecordsApi.addReadingRecord(readingRecordData)
    } catch (error) {
      console.error('保存阅读记录失败:', error)
      // 不显示错误消息，避免影响用户阅读体验
    }
  }

  // 自动保存阅读进度
  if (readingSettings.value.autoBookmark) {
    saveReadingProgress()
  }
}

// 上一章
const prevChapter = () => {
  if (hasPrevChapter.value) {
    const prevChapterSortOrder = chapters.value[currentChapterIndex.value - 1].sortOrder
    router.push(`/read/${bookId.value}/${prevChapterSortOrder}`)
  }
}

// 下一章
const nextChapter = () => {
  if (hasNextChapter.value) {
    const nextChapterSortOrder = chapters.value[currentChapterIndex.value + 1].sortOrder
    router.push(`/read/${bookId.value}/${nextChapterSortOrder}`)
  }
}

// 排序后的章节列表
const sortedChapters = computed(() => {
  const sorted = [...chapters.value].sort((a, b) => {
    if (chapterSortOrder.value === 'asc') {
      return a.sortOrder - b.sortOrder
    } else {
      return b.sortOrder - a.sortOrder
    }
  })
  return sorted
})

// 切换章节排序
const toggleChapterSort = () => {
  chapterSortOrder.value = chapterSortOrder.value === 'asc' ? 'desc' : 'asc'
}

// 切换目录侧边栏显示
const toggleChapterSidebar = () => {
  showChapterSidebar.value = !showChapterSidebar.value

  // 如果打开侧边栏，延迟一下再定位到当前章节
  if (showChapterSidebar.value) {
    setTimeout(() => {
      locateCurrentChapter()
    }, 100)
  }
}

// 设置章节引用
const setChapterRef = (el, sortOrder) => {
  if (el) {
    chapterRefs.value[sortOrder] = el
  }
}

// 选择章节
const selectChapter = (chapter) => {
  // 使用章节的sortOrder进行导航，而不是id
  const chapterSortOrder = chapter.sortOrder || 1
  router.push(`/read/${bookId.value}/${chapterSortOrder}`)
  showChapterSidebar.value = false
}

// 定位到当前章节
const locateCurrentChapter = () => {
  const currentSortOrder = route.params.chapterId
  const currentChapterEl = chapterRefs.value[currentSortOrder]

  if (currentChapterEl && chapterListRef.value) {
    // 滚动到当前章节元素
    currentChapterEl.scrollIntoView({
      behavior: 'smooth',
      block: 'center'
    })
    showChapterLocate.value = false
  }
}

// 检查当前章节是否在可视区域内
const checkCurrentChapterVisible = () => {
  const currentSortOrder = route.params.chapterId
  const currentChapterEl = chapterRefs.value[currentSortOrder]

  if (currentChapterEl && chapterListRef.value) {
    const listRect = chapterListRef.value.getBoundingClientRect()
    const chapterRect = currentChapterEl.getBoundingClientRect()

    // 检查当前章节是否在可视区域内
    const isVisible = chapterRect.top >= listRect.top && chapterRect.bottom <= listRect.bottom
    showChapterLocate.value = !isVisible
  }
}

// 切换书架状态
const toggleBookshelf = async () => {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    if (isInBookshelf.value) {
      // 从书架移除 - 添加确认弹窗
      ElMessageBox.confirm(
        '确定要将本书从书架中移除吗？',
        '移除确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        const response = await bookshelvesApi.removeBookFromShelves(bookId.value, authStore.userId)
        if (response.code === 200) {
          ElMessage.success('已从书架移除')
          isInBookshelf.value = false
        } else {
          ElMessage.error(response.message || '移除失败')
        }
      }).catch(() => {
        // 用户取消操作
        ElMessage.info('已取消移除')
      })
    } else {
      // 添加到书架
      const response = await bookshelvesApi.addBookToShelves(bookId.value, authStore.userId)
      if (response.code === 200) {
        ElMessage.success('已加入书架')
        isInBookshelf.value = true
      } else {
        ElMessage.error(response.message || '添加失败')
      }
    }
  } catch (error) {
    console.error('书架操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 检查用户是否已打卡
const checkUserCheckInStatus = async () => {
  if (!authStore.isAuthenticated) {
    isCheckedIn.value = false
    danmakuCount.value = 0
    return
  }

  try {
    const response = await checkInsApi.checkUserCheckIn(
      authStore.userId,
      Number(bookId.value),
      Number(chapterId.value)
    )
    console.log('用户打卡状态:', response.data)
    isCheckedIn.value = response.data || false

    // 获取弹幕数量
    const danmakuResponse = await danmakuApi.getVideoDanmakus(
      Number(bookId.value),
      Number(chapterId.value)
    )
    danmakuCount.value = danmakuResponse.data?.length || 0
  } catch (error) {
    console.error('检查打卡状态或获取弹幕数量失败:', error)
    isCheckedIn.value = false
    danmakuCount.value = 0
  }
}

// 处理打卡按钮点击
const handleCheckInClick = async () => {
  // 检查用户是否登录
  if (!authStore.isAuthenticated) {
    try {
      await ElMessageBox.confirm(
        '此功能需要登录才能使用，是否前往登录？',
        '提示',
        {
          confirmButtonText: '前往登录',
          cancelButtonText: '取消',
          type: 'info'
        }
      )
      router.push('/login')
    } catch {
      // 用户取消登录
      return
    }
  }

  // 显示打卡弹幕窗口
  showCheckInDanmaku.value = true

  if (!isCheckedIn.value) {
    try {
      // 执行打卡
      checkInLoading.value = true
      await checkInsApi.checkIn({
        userId: authStore.userId,
        bookId: Number(bookId.value),
        chapterId: Number(chapterId.value)
      })

      // 更新打卡状态
      isCheckedIn.value = true
    } catch (error) {
      console.error('打卡失败:', error)
      // 获取弹幕界面元素
      const danmakuOverlay = document.querySelector('.danmaku-overlay')
      let originalZIndex = ''

      // 如果存在弹幕界面，临时降低其z-index
      if (danmakuOverlay) {
        originalZIndex = danmakuOverlay.style.zIndex
        danmakuOverlay.style.zIndex = '999'
      }

      try {
        ElMessage.error({
          message: '打卡失败，请稍后再试',
          offset: 100
        })
      } finally {
        // 恢复弹幕界面的原始z-index
        if (danmakuOverlay) {
          danmakuOverlay.style.zIndex = originalZIndex || '10000'
        }
        checkInLoading.value = false
      }
    }
  }

  try {
    // 获取用户打卡信息
    const checkInResponse = await checkInsApi.getCheckIn(
      authStore.userId,
      Number(bookId.value),
      Number(chapterId.value)
    )
    checkInInfo.value = checkInResponse.data

    // 获取弹幕信息
    const danmakuResponse = await danmakuApi.getVideoDanmakus(
      Number(bookId.value),
      Number(chapterId.value)
    )
    danmakuList.value = danmakuResponse.data || []
    danmakuCount.value = danmakuList.value.length
  } catch (error) {
    console.error('获取数据失败:', error)
    // 获取弹幕界面元素
    const danmakuOverlay = document.querySelector('.danmaku-overlay')
    let originalZIndex = ''

    // 如果存在弹幕界面，临时降低其z-index
    if (danmakuOverlay) {
      originalZIndex = danmakuOverlay.style.zIndex
      danmakuOverlay.style.zIndex = '999'
    }

    try {
      ElMessage.error({
        message: '获取数据失败，请稍后再试',
        offset: 100
      })
    } finally {
      // 恢复弹幕界面的原始z-index
      if (danmakuOverlay) {
        danmakuOverlay.style.zIndex = originalZIndex || '10000'
      }
    }
  }
}









// 发送弹幕
const sendDanmaku = async () => {
  if (!danmakuInput.value.trim()) {
    return
  }

  try {
    danmakuLoading.value = true
    const response = await danmakuApi.addVideoDanmaku({
      userId: authStore.userId,
      bookId: Number(bookId.value),
      chapterId: Number(chapterId.value),
      content: danmakuInput.value.trim()
    })

    if (response.code === 200) {
      // 添加新弹幕到列表
      const newDanmaku = {
        id: response.data.id || Date.now(), // 确保有唯一ID
        userId: authStore.userId,
        username: authStore.user.username,
        avatar: checkInInfo.value?.avatar,
        content: danmakuInput.value.trim(),
        createdAt: new Date()
      }

      // 添加到列表前端，立即显示
      danmakuList.value.unshift(newDanmaku)
      danmakuInput.value = ''

      // 更新弹幕数量
      danmakuCount.value = danmakuList.value.length

      // 获取弹幕界面元素
      const danmakuOverlay = document.querySelector('.danmaku-overlay')
      let originalZIndex = ''

      // 如果存在弹幕界面，临时降低其z-index
      if (danmakuOverlay) {
        originalZIndex = danmakuOverlay.style.zIndex
        danmakuOverlay.style.zIndex = '999'
      }

      try {
        ElMessage.success({
          message: '弹幕发送成功',
          offset: 100
        })
      } finally {
        // 恢复弹幕界面的原始z-index
        if (danmakuOverlay) {
          danmakuOverlay.style.zIndex = originalZIndex || '10000'
        }
      }

      // 弹幕过多时清理（保持最多50条）
      if (danmakuList.value.length > 50) {
        danmakuList.value = danmakuList.value.slice(0, 50)
      }
    }
  } catch (error) {
    console.error('发送弹幕失败:', error)

    // 获取弹幕界面元素
    const danmakuOverlay = document.querySelector('.danmaku-overlay')
    let originalZIndex = ''

    // 如果存在弹幕界面，临时降低其z-index
    if (danmakuOverlay) {
      originalZIndex = danmakuOverlay.style.zIndex
      danmakuOverlay.style.zIndex = '999'
    }

    try {
      ElMessage.error({
        message: '弹幕发送失败，请稍后再试',
        offset: 100
      })
    } finally {
      // 恢复弹幕界面的原始z-index
      if (danmakuOverlay) {
        danmakuOverlay.style.zIndex = originalZIndex || '10000'
      }
      danmakuLoading.value = false
    }
  }
}

// 关闭打卡弹幕窗口
const closeCheckInDanmaku = () => {
  showCheckInDanmaku.value = false
  checkInInfo.value = null
  danmakuList.value = []
  danmakuInput.value = ''
}

// 选择背景颜色
const selectBackgroundColor = (color) => {
  readingSettings.value.backgroundColor = color
  saveReadingSettings()
}

// 选择字体类型
const selectFontFamily = (font) => {
  readingSettings.value.fontFamily = font
  saveReadingSettings()
}

// 选择页面宽度
const selectPageWidth = (width) => {
  console.log('切换页面宽度:', width)
  readingSettings.value.pageWidth = width
  saveReadingSettings()
  console.log('contentStyle:', contentStyle.value)
}

// 减小字体大小
const decreaseFontSize = () => {
  if (readingSettings.value.fontSize > 16) {
    readingSettings.value.fontSize -= 2
    saveReadingSettings()
  }
}

// 增大字体大小
const increaseFontSize = () => {
  if (readingSettings.value.fontSize < 30) {
    readingSettings.value.fontSize += 2
    saveReadingSettings()
  }
}

// 保存阅读设置
const saveReadingSettings = () => {
  // 实际项目中应调用API保存设置
  localStorage.setItem('readingSettings', JSON.stringify(readingSettings.value))
}

// 加载阅读设置
const loadReadingSettings = () => {
  const savedSettings = localStorage.getItem('readingSettings')
  if (savedSettings) {
    readingSettings.value = { ...readingSettings.value, ...JSON.parse(savedSettings) }
  }
}

// 组件卸载时清理弹幕
onUnmounted(() => {
  // 清理弹幕数据
  danmakuList.value = []
  showCheckInDanmaku.value = false
})

// 保存阅读进度
const saveReadingProgress = () => {
  // 添加到阅读历史
  readingHistoryStore.addToHistory(
    {
      id: bookInfo.value.id,
      title: bookInfo.value.title,
      author: bookInfo.value.author,
      cover: `https://picsum.photos/seed/book${bookInfo.value.id}/200/280.jpg`
    },
    {
      id: chapterInfo.value.id,
      title: chapterInfo.value.title
    },
    Math.round((currentChapterIndex.value / chapters.value.length) * 100)
  )

  // 保存本地进度（兼容旧版本）
  const progress = {
    bookId: bookId.value,
    chapterId: chapterInfo.value.id,
    timestamp: new Date()
  }
  localStorage.setItem(`readingProgress_${bookId.value}`, JSON.stringify(progress))
}

// 键盘事件处理
const handleKeydown = (event) => {
  switch (event.key) {
    case 'ArrowLeft':
      prevChapter()
      break
    case 'ArrowRight':
      nextChapter()
      break
    case 'Escape':
      showFooter.value = true
      showSettings.value = false
      break
  }
}

// 监听路由变化
watch(() => route.params.chapterId, (newChapterId) => {
  if (newChapterId) {
    fetchChapterContent(newChapterId)
    // 如果目录侧边栏打开，检查是否需要显示定位按钮
    if (showChapterSidebar.value) {
      setTimeout(() => {
        checkCurrentChapterVisible()
      }, 100)
    }
  }
})

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

// 滚动监听
const handleScroll = () => {
  // 当页面滚动超过200px时显示置顶按钮
  showScrollTop.value = window.scrollY > 200
}

// 跳转到书籍详情页
const goToBookDetail = () => {
  // 在新标签页打开书籍详情页
  window.open(`/book/${bookId.value}`, '_blank')
}

// 显示评论弹窗，先检查登录状态
const handleShowComments = () => {
  if (!authStore.isLoggedIn) {
    // 未登录，提示登录
    ElMessageBox.confirm(
      '登录后才能使用评论功能，是否前往登录页面？',
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
      // 用户取消，不做操作
    })
  } else {
    // 已登录，显示评论弹窗
    showComments.value = true
  }
}

// 定时器引用
let refreshTimer = null

// 组件挂载时获取数据
onMounted(async () => {
  loadReadingSettings()
  await fetchBookInfo()
  await fetchChapters()

  // 等待章节列表加载完成后，再获取章节内容
  // 如果有章节ID，获取章节内容；否则默认第一章
  const targetChapterId = chapterId.value || chapters.value[0]?.id
  if (targetChapterId) {
    await fetchChapterContent(targetChapterId)
  }

  // 检查用户打卡状态
  await checkUserCheckInStatus()

  // 如果目录侧边栏打开，检查是否需要显示定位按钮
  if (showChapterSidebar.value) {
    setTimeout(() => {
      checkCurrentChapterVisible()
    }, 100)
  }

  // 添加键盘事件监听
  window.addEventListener('keydown', handleKeydown)

  // 添加滚动事件监听
  window.addEventListener('scroll', handleScroll)

  // 初始化store
  favoriteStore.initFavorites()
  readingHistoryStore.initHistory()

  // 设置定期刷新定时器（5分钟）
  refreshTimer = setInterval(() => {
    refreshParagraphComments()
  }, 5 * 60 * 1000)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('scroll', handleScroll)

  // 清除定期刷新定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

// 监听路由变化，切换章节时刷新评论
watch(() => route.params.chapterId, (newChapterId, oldChapterId) => {
  if (newChapterId && newChapterId !== oldChapterId) {
    // 章节变化时，会重新调用fetchChapterContent，不需要在这里额外刷新
    // 但如果用户在同一章节停留时间较长，可能需要刷新
    setTimeout(() => {
      refreshParagraphComments()
    }, 1000)
  }
})

// 显示段落评论
const showParagraphCommentSection = (index) => {
  selectedParagraphIndex.value = index
  showParagraphComments.value = true
}

// 刷新段落评论数据
const refreshParagraphComments = async () => {
  try {
    // 调用API获取所有段落评论
    const commentResponse = await commentsApi.getCommentsByParagraphId(
      bookId.value,
      chapterId.value,
      paragraphs.value.length
    )

    if (commentResponse && commentResponse.code === 200) {
      // 处理返回的双重列表数据
      const paragraphsComments = commentResponse.data || []

      // 遍历每个段落的评论数据
      paragraphsComments.forEach((paragraphData, index) => {
        if (index < paragraphs.value.length) {
          // 计算该段落的评论总数：每个段评的replyCount加起来再加上本身数量
          const totalComments = paragraphData.reduce((sum, comment) => {
            return sum + 1 + (comment.replyCount || 0)
          }, 0)

          // 更新段落评论数和评论列表
          paragraphs.value[index].commentCount = totalComments
          paragraphs.value[index].comments = paragraphData
        }
      })

      ElMessage.success('评论已刷新')
    }
  } catch (error) {
    console.error('刷新段落评论失败:', error)
    ElMessage.error('刷新评论失败，请稍后重试')
  }
}

// 关闭段落评论
const closeParagraphComments = () => {
  showParagraphComments.value = false
  selectedParagraphIndex.value = -1
  newParagraphComment.value = ''
}

// 提交段评
const submitParagraphComment = async () => {
  if (!newParagraphComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submittingParagraphComment.value = true

  try {
    // 准备评论数据
    const commentData = {
      userId: authStore.userId,
      bookId: Number(bookId.value),
      chapterId: Number(chapterId.value),
      paragraphIndex: selectedParagraphIndex.value,
      commentType: 2, // 段评类型为2
      content: newParagraphComment.value
      // 一层发表的段评不需要parentId和topParentId
    }

    // 调用统一的发布评论API
    const response = await commentsApi.publishComment(commentData)

    if (response && response.code === 200) {
      ElMessage.success('评论发表成功')

      // 方案1：直接添加评论到本地列表，避免重新请求API（推荐，性能更好）
      // 创建新评论对象
      const newComment = {
        id: response.data?.id || `comment_${Date.now()}`,
        userId: authStore.userId,
        content: newParagraphComment.value,
        replyCount: 0,
        createdAt: new Date().toISOString(),
        // 其他必要字段
      }

      // 直接更新本地数据
      if (selectedParagraphIndex.value >= 0 && paragraphs.value[selectedParagraphIndex.value]) {
        // 添加新评论到评论列表
        paragraphs.value[selectedParagraphIndex.value].comments.push(newComment)
        // 更新评论总数
        paragraphs.value[selectedParagraphIndex.value].commentCount++
      }

      // 清空输入框
      newParagraphComment.value = ''
    } else {
      ElMessage.error(response?.message || '发表评论失败')
    }
  } catch (error) {
    console.error('发表段评失败:', error)
    ElMessage.error('发表评论失败，请稍后重试')
  } finally {
    submittingParagraphComment.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
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

// 跳转到评论详情页
const goToCommentDetail = (commentId) => {
  // 章评类型为1
  router.push(`/comment/${commentId}?type=2`)
}

// 点赞/取消点赞
const toggleLike = async (target) => {
  if (!isAuthenticated.value) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    if (target.isLiked) {
      // 取消点赞
      await likesApi.cancelLike(authStore.userId, target.id)
      target.likeCount--
      target.isLiked = false
      ElMessage.success('已取消点赞')
    } else {
      // 点赞
      const likeData = {
        userId: authStore.userId,
        targetId: target.id,
        targetType: 1 // 1表示评论或回复
      }
      await likesApi.like(likeData)
      target.likeCount++
      target.isLiked = true
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败，请稍后再试')
  }
}

// 回复评论
const replyToComment = (comment) => {
  // 这里可以实现直接回复功能，或者跳转到评论详情页
  goToCommentDetail(comment.id)
}

// 删除评论
const deleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 1. 先从本地删除评论，提升用户体验
    let commentDeleted = false
    let paragraphIndexToUpdate = -1

    // 遍历所有段落的评论列表
    for (let i = 0; i < paragraphs.value.length; i++) {
      const paragraph = paragraphs.value[i]
      const commentIndex = paragraph.comments.findIndex(comment => comment.id === commentId)

      if (commentIndex > -1) {
        // 计算删除的评论的回复数，用于更新评论总数
        const deletedComment = paragraph.comments[commentIndex]
        const deletedCommentCount = 1 + (deletedComment.replyCount || 0)

        // 从评论列表中删除
        paragraph.comments.splice(commentIndex, 1)
        // 更新段落评论总数
        paragraph.commentCount = Math.max(0, paragraph.commentCount - deletedCommentCount)
        commentDeleted = true
        paragraphIndexToUpdate = i
        break
      }
    }

    // 2. 调用API删除评论
    await commentsApi.deleteComment(commentId)
    ElMessage.success('删除成功')

    // 3. 可选：如果是当前查看的段落，直接更新本地数据；否则可以选择刷新
    if (commentDeleted) {
      // 如果删除的是当前打开的段评弹窗中的评论，确保弹窗中的评论列表也更新
      if (selectedParagraphIndex.value === paragraphIndexToUpdate) {
        // 由于我们已经直接修改了paragraphs.value，弹窗会自动更新
        // 这里可以添加额外的逻辑确保弹窗显示正确
      }

      // 一段时间后刷新评论列表，确保数据一致性
      setTimeout(() => {
        refreshParagraphComments()
      }, 1000)
    } else {
      // 本地没找到评论，直接刷新
      refreshParagraphComments()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败，请稍后再试')
      // 恢复本地数据
      refreshParagraphComments()
    }
  }
}

// 打开举报对话框
const handleReportClick = (commentId) => {
  if (!authStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    return
  }

  // 设置举报表单数据
  reportForm.value = {
    reporterId: authStore.userId,
    targetType: 2, // 举报类型：评论
    targetId: commentId,
    reasonType: 1,
    reasonDetail: '',
    isAnonymous: 0
  }

  // 打开举报对话框
  showReportDialog.value = true
}

// 关闭举报对话框
const handleReportDialogClose = () => {
  // 重置表单
  if (reportFormRef.value) {
    reportFormRef.value.resetFields()
  }
  // 清空表单数据
  reportForm.value = {
    reporterId: '',
    targetType: 2,
    targetId: '',
    reasonType: 1,
    reasonDetail: '',
    isAnonymous: 0
  }
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
      reportData.isAnonymous = 1
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
    if (error.name !== 'ValidationError') {
      console.error('举报评论失败:', error)
      ElMessage.error('举报失败，请稍后再试')
    }
  }
}

// 弹幕轨道数量（决定垂直行数）
const DANMAKU_LANES = 6

// 每条弹幕的动画时长（秒）
const DANMAKU_DURATION = 12

// 生成循环弹幕样式
const getLoopDanmakuStyle = (index) => {
  const lane = index % DANMAKU_LANES

  return {
    top: `${lane * 48 + 20}px`, // 每条轨道高度 48px
    animationDuration: `${DANMAKU_DURATION}s`,
    animationDelay: `${index * 1.2}s`
  }
}
</script>

<style scoped>
.reader-page {
  min-height: 100vh;
  transition: background-color 0.3s;
}

.reader-page.white {
  background-color: #fafafa;
  color: #333333;
}

.reader-page.sepia {
  background-color: #fcf9f2;
  color: #5c4b37;
}

.reader-page.dark {
  background-color: #1a1a1a;
  color: #cccccc;
}

.reader-page.light-gray {
  background-color: #fafafa;
  color: #333333;
}

.reader-page.beige {
  background-color: #fcf9f0;
  color: #5c4b37;
}

.reader-page.light-green {
  background-color: #f5faf5;
  color: #2d4a2d;
}

.reader-container {
  position: relative;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 阅读内容区 */
.reader-content {
  flex: 1;
  padding: 20px 20px 100px;
  width: 100%;
  margin: 0 auto;
  cursor: pointer;
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-top: 16px;
  margin-bottom: 16px;
}

.chapter-content {
  text-align: justify;
}

.chapter-content .chapter-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: 600;
}

.content-text {
  line-height: 1.8;
}

/* 段落样式 */
.paragraph-item {
  position: relative;
  margin-bottom: 20px;
  padding: 5px;
  transition: background-color 0.2s ease;
}

.paragraph-item:hover {
  background-color: rgba(64, 158, 255, 0.05);
}

.paragraph-text {
  margin-bottom: 0;
  text-indent: 2em;
  line-height: 1.8;
  cursor: pointer;
  transition: color 0.2s ease;
}

.paragraph-text:hover {
  color: #409eff;
}

/* 段评数显示 */
.paragraph-comment-count {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
  font-size: 12px;
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  vertical-align: middle;
}

.paragraph-comment-count:hover {
  background-color: rgba(64, 158, 255, 0.2);
  transform: scale(1.05);
}

.paragraph-comment-count .el-icon {
  font-size: 12px;
}

/* 段评弹窗相关样式 */
.paragraph-preview {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.paragraph-preview h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.paragraph-preview p {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

/* 段评模态框内容区域布局 */
.paragraph-comment-modal .el-dialog__body {
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 评论表单固定位置 */
.comment-form {
  padding: 15px;
  background-color: #fafafa;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 10;
}

.login-prompt {
  text-align: center;
  padding: 15px;
  color: #999;
  background-color: #fafafa;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 10;
}

.login-prompt p {
  margin: 0 0 10px 0;
}

/* 段落评论区 */
.paragraph-comments {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.paragraph-comments h4 {
  margin: 0;
  padding: 15px;
  font-size: 16px;
  color: #333;
  background-color: #fff;
  border-bottom: 1px solid #eee;
}

/* 可滚动的评论列表 */
.comment-list-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 0 15px;
}

/* 适配不同主题的滚动条 */
.reader-page.dark .comment-list-wrapper::-webkit-scrollbar-track {
  background: #333;
}

.reader-page.dark .comment-list-wrapper::-webkit-scrollbar-thumb {
  background: #666;
}

.reader-page.dark .comment-list-wrapper::-webkit-scrollbar-thumb:hover {
  background: #888;
}

.comment-list-wrapper::-webkit-scrollbar {
  width: 8px;
}

.comment-list-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.comment-list-wrapper::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.comment-list-wrapper::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* 评论列表 */
.comment-list {
  margin-bottom: 0;
}

/* 评论项 */
.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.comment-item:hover {
  background-color: rgba(64, 158, 255, 0.02);
}

.comment-item:last-child {
  border-bottom: none;
}

/* 评论头部 */
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

/* 评论用户信息 */
.comment-user {
  display: flex;
  gap: 10px;
  flex: 1;
  cursor: pointer;
}

.comment-user .el-avatar {
  margin-top: 2px;
  transition: transform 0.2s;
}

.comment-user:hover .el-avatar {
  transform: scale(1.05);
}

.user-info {
  flex: 1;
}

.username {
  font-weight: 500;
  color: #333;
  font-size: 14px;
  transition: color 0.2s;
}

.username:hover {
  color: #409eff;
}

.comment-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

/* 评论操作菜单 */
.comment-actions-menu {
  display: flex;
  align-items: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.comment-item:hover .comment-actions-menu {
  opacity: 1;
}

.comment-actions-menu .el-button {
  padding: 0;
  min-width: auto;
  color: #999;
}

.comment-actions-menu .el-button:hover {
  color: #666;
}

/* 评论内容容器 */
.comment-content-wrapper {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

/* 评论内容 */
.comment-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  word-break: break-word;
  cursor: pointer;
  transition: color 0.2s;
  flex: 1;
  min-width: 0;
  margin-bottom: 0;
}

.comment-content:hover {
  color: #409eff;
}

/* 全部回复显示 */
.all-replies {
  font-size: 12px;
  color: #409eff;
  cursor: pointer;
  transition: color 0.2s;
  display: inline-flex;
  align-items: center;
  margin-bottom: 0;
  white-space: nowrap;
}

.all-replies:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* 评论底部 */
.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  flex-wrap: wrap;
  gap: 10px;
}

/* 评论左侧操作区 */
.comment-actions-left {
  display: flex;
  align-items: center;
}

/* 评论右侧操作区 */
.comment-actions-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 评论操作 */
.comment-actions {
  display: flex;
  gap: 20px;
  align-items: center;
}

/* 操作项 */
.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
  cursor: pointer;
  transition: all 0.2s;
  padding: 4px 8px;
  border-radius: 4px;
}

.action-item:hover {
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.05);
}

.action-item.active {
  color: #f56c6c;
}

.action-item.active:hover {
  color: #f78989;
  background-color: rgba(245, 108, 108, 0.05);
}

/* 回复列表 */
.reply-list {
  margin-top: 10px;
  margin-left: 40px;
  padding: 0;
  background-color: transparent;
}

/* 回复项 */
.reply-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.reply-item:hover {
  background-color: rgba(64, 158, 255, 0.02);
}

.reply-item:last-child {
  border-bottom: none;
}

/* 回复头部 */
.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 5px;
}

/* 回复用户信息 */
.reply-user {
  display: flex;
  gap: 8px;
  flex: 1;
  cursor: pointer;
}

.reply-user .el-avatar {
  margin-top: 2px;
  transition: transform 0.2s;
}

.reply-user:hover .el-avatar {
  transform: scale(1.05);
}

.reply-time {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

/* 回复操作菜单 */
.reply-actions-menu {
  display: flex;
  align-items: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.reply-item:hover .reply-actions-menu {
  opacity: 1;
}

.reply-actions-menu .el-button {
  padding: 0;
  min-width: auto;
  color: #999;
  font-size: 12px;
}

.reply-actions-menu .el-button:hover {
  color: #666;
}

/* 回复内容 */
.reply-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
  margin-left: 0;
  word-break: break-word;
}

/* 回复底部 */
.reply-footer {
  display: flex;
  justify-content: flex-start;
  margin-top: 8px;
  margin-left: 0;
}

/* 回复操作 */
.reply-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.reply-actions .action-item {
  font-size: 12px;
  padding: 3px 6px;
}

/* 回复按钮 */
.reply-action {
  font-size: 13px;
}

/* 暂无评论 */
.no-comments {
  text-align: center;
  padding: 20px;
  color: #999;
  background-color: #f9f9f9;
  border-radius: 8px;
}

/* 打卡按钮样式 */
.check-in-button {
  margin-left: 10px;
  font-size: 14px;
  color: #409eff;
}

/* 全局样式：提高Element Plus Message组件的z-index，确保显示在弹幕界面上方 */
:deep(.el-message-container) {
  z-index: 10010 !important;
}

/* 弹幕覆盖层 */
.danmaku-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 9000;
  pointer-events: auto;
}

/* 背景遮罩 */
.danmaku-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  z-index: 9000;
  cursor: pointer;
}

/* 确保其他弹幕元素显示在遮罩之上 */
.check-in-success,
.danmaku-container,
.danmaku-layer,
.danmaku-input-container {
  position: relative;
  z-index: 9001;
}

/* 顶部打卡成功信息 */
.check-in-success {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 25px;
  background-color: rgba(0, 0, 0, 0.8);
  border-radius: 25px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.5);
  color: white;
  pointer-events: auto;
  z-index: 10001;
  animation: fadeIn 0.5s ease;
}

.check-in-badge .el-avatar {
  border: 2px solid #409eff;
}

.check-in-message h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}



/* 循环滚动弹幕层 */
.danmaku-layer {
  position: fixed;
  top: 80px;
  left: 0;
  width: 100%;
  height: 300px;
  pointer-events: none;
  z-index: 9001;
  overflow: hidden;
}

/* 单条弹幕 */
.danmaku-item {
  position: absolute;
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  padding: 6px 12px;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 14px;

  animation-name: danmaku-loop;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
}

/* 用户名 */
.danmaku-username {
  margin: 0 6px;
  font-weight: 600;
  font-size: 12px;
  color: #ccc;
}

/* 弹幕内容 */
.danmaku-content {
  font-size: 14px;
  color: white;
}

/* 核心动画：右 → 左 → 再从右出现 */
@keyframes danmaku-loop {
  0% {
    transform: translateX(100vw);
  }
  100% {
    transform: translateX(-120%);
  }
}

.danmaku-item .el-avatar {
  width: 20px;
  height: 20px;
}

/* 底部弹幕输入 */
.danmaku-input-container {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 10px;
  width: 80%;
  max-width: 800px;
  pointer-events: auto;
  z-index: 10001;
  animation: fadeIn 0.5s ease 0.4s both;
}

.danmaku-input {
  flex: 1;
  background-color: rgba(0, 0, 0, 0.8);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
  border-radius: 25px;
  padding: 10px 20px;
  pointer-events: auto;
}

.danmaku-input .el-input__inner {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
  color: white;
  border-radius: 25px;
  height: 50px;
  line-height: 50px;
}

.danmaku-input .el-input__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.danmaku-input .el-input__count {
  color: rgba(255, 255, 255, 0.5);
}

.danmaku-input-container .el-button {
  pointer-events: auto;
  border-radius: 25px;
}

.close-danmaku-btn {
  width: 40px;
  height: 40px;
  padding: 0;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  pointer-events: auto;
}



/* 底部工具栏 */
.reader-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(8px);
  box-shadow: 0 -1px 6px rgba(0, 0, 0, 0.08);
  z-index: 100;
  transform: translateY(100%);
  transition: transform 0.2s ease;
}

/* 悬浮触发区域 */
.footer-hover-trigger {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 30px;
  z-index: 99;
  cursor: pointer;
}

/* 当鼠标悬浮在触发区域或工具栏上时显示 */
.footer-hover-trigger:hover + .reader-footer,
.reader-footer:hover {
  transform: translateY(0);
}

.reader-page.dark .reader-footer {
  background-color: rgba(26, 26, 26, 0.95);
}

.reader-page.sepia .reader-footer {
  background-color: rgba(247, 243, 233, 0.95);
}

.reader-page.light-gray .reader-footer {
  background-color: rgba(245, 245, 245, 0.95);
}

.reader-page.beige .reader-footer {
  background-color: rgba(245, 245, 220, 0.95);
}

.reader-page.light-green .reader-footer {
  background-color: rgba(240, 248, 240, 0.95);
}

/* 暗色模式下的阅读内容区样式 */
.reader-page.dark .reader-content {
  background-color: #2a2a2a;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

/* 护眼模式下的阅读内容区样式 */
.reader-page.sepia .reader-content {
  background-color: #f7f3e9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 浅灰模式下的阅读内容区样式 */
.reader-page.light-gray .reader-content {
  background-color: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 米色模式下的阅读内容区样式 */
.reader-page.beige .reader-content {
  background-color: #f5f5dc;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 浅绿模式下的阅读内容区样式 */
.reader-page.light-green .reader-content {
  background-color: #f0f8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chapter-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  max-width: 800px;
  margin: 0 auto;
}

.nav-button {
  width: 100%;
}

.chapter-dropdown {
  flex: 1;
  margin: 0 20px;
}

.chapter-select {
  width: 100px;
}

.chapter-menu {
  max-height: 300px;
  overflow-y: auto;
}

.chapter-menu .el-dropdown-menu__item.active {
  color: #409eff;
  font-weight: 500;
}

/* 阅读设置面板 */
.reader-settings {
  position: fixed;
  top: 0;
  right: -400px;
  width: 400px;
  height: 100vh;
  background-color: #fff;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
  z-index: 200;
  transition: right 0.3s;
}

.reader-settings.show {
  right: 0;
}

.reader-page.dark .reader-settings {
  background-color: #2a2a2a;
  color: #ccc;
}

.reader-page.sepia .reader-settings {
  background-color: #f7f3e9;
  color: #5c4b37;
}

.reader-page.light-gray .reader-settings {
  background-color: #f5f5f5;
  color: #333;
}

.reader-page.beige .reader-settings {
  background-color: #f5f5dc;
  color: #5c4b37;
}

.reader-page.light-green .reader-settings {
  background-color: #f0f8f0;
  color: #2d4a2d;
}

.settings-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.reader-page.dark .settings-header {
  border-bottom-color: #444;
}

.reader-page.sepia .settings-header {
  border-bottom-color: #e8e0d0;
}

.reader-page.light-gray .settings-header {
  border-bottom-color: #e0e0e0;
}

.reader-page.beige .settings-header {
  border-bottom-color: #e8e0d0;
}

.reader-page.light-green .settings-header {
  border-bottom-color: #e0e8e0;
}

.settings-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.setting-item {
  margin-bottom: 25px;
}

.setting-label {
  margin-bottom: 10px;
  font-weight: 500;
}

.setting-control {
  width: 100%;
}

.color-options {
  display: flex;
  gap: 8px;
}

.color-option {
  flex: 1;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.color-option:hover {
  transform: scale(1.05);
}

.color-option.active {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.color-name {
  font-size: 11px;
  font-weight: 500;
}

/* 目录侧边栏遮罩层 */
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 199;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s, visibility 0.3s;
}

.sidebar-overlay.show {
  opacity: 1;
  visibility: visible;
}

/* 目录侧边栏头部操作区域 */
.sidebar-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-button {
  font-size: 14px;
}

.sort-button .el-icon {
  margin-right: 4px;
}

/* 目录侧边栏 */
.chapter-sidebar {
  position: fixed;
  top: 0;
  left: -300px;
  width: 300px;
  height: 100vh;
  background-color: #fff;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  z-index: 200;
  transition: left 0.3s;
}

.chapter-sidebar.show {
  left: 0;
}

.reader-page.dark .chapter-sidebar {
  background-color: #2a2a2a;
  color: #ccc;
}

.reader-page.sepia .chapter-sidebar {
  background-color: #f7f3e9;
  color: #5c4b37;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.reader-page.dark .sidebar-header {
  border-bottom-color: #444;
}

.reader-page.sepia .sidebar-header {
  border-bottom-color: #e8e0d0;
}

.chapter-list {
  height: calc(100vh - 70px);
  overflow-y: auto;
  position: relative;
}

.chapter-item {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.reader-page.dark .chapter-item {
  border-bottom-color: #444;
}

.reader-page.sepia .chapter-item {
  border-bottom-color: #e8e0d0;
}

.reader-page.light-gray .chapter-item {
  border-bottom-color: #e0e0e0;
}

.reader-page.beige .chapter-item {
  border-bottom-color: #e8e0d0;
}

.reader-page.light-green .chapter-item {
  border-bottom-color: #e0e8e0;
}

.chapter-item:hover {
  background-color: #f5f7fa;
}

.reader-page.dark .chapter-item:hover {
  background-color: #333;
}

.reader-page.sepia .chapter-item:hover {
  background-color: #ede6d6;
}

.reader-page.light-gray .chapter-item:hover {
  background-color: #f0f0f0;
}

.reader-page.beige .chapter-item:hover {
  background-color: #f0e8d8;
}

.reader-page.light-green .chapter-item:hover {
  background-color: #e8f5e8;
}

.chapter-item.active {
    background-color: #ecf5ff;
    color: #409eff;
    font-weight: 500;
  }

  .chapter-locate-button {
    position: absolute;
    bottom: 20px;
    right: 20px;
    background-color: #409eff;
    color: white;
    border: none;
    border-radius: 20px;
    padding: 8px 16px;
    font-size: 12px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;
    opacity: 0;
    visibility: hidden;
    transform: translateY(10px);
    z-index: 10;
  }

  .chapter-locate-button.show {
    opacity: 1;
    visibility: visible;
    transform: translateY(0);
  }

  .chapter-locate-button:hover {
    background-color: #66b1ff;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  }

  .reader-page.dark .chapter-locate-button {
    background-color: #409eff;
  }

  .reader-page.dark .chapter-locate-button:hover {
    background-color: #66b1ff;
  }

.reader-page.dark .chapter-item.active {
  background-color: #1e3a5f;
}

.reader-page.sepia .chapter-item.active {
  background-color: #d4e1f5;
  color: #409eff;
}

.reader-page.light-gray .chapter-item.active {
  background-color: #e6f2ff;
  color: #409eff;
}

.reader-page.beige .chapter-item.active {
  background-color: #d4e1f5;
  color: #409eff;
}

.reader-page.light-green .chapter-item.active {
  background-color: #e6ffe6;
  color: #409eff;
}

/* 悬浮导航栏 */
.floating-nav {
  position: fixed;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border-radius: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  z-index: 90;
  display: flex;
  flex-direction: column;
  padding: 8px 0;
  transition: all 0.2s ease;
}

.reader-page.dark .floating-nav {
  background-color: rgba(42, 42, 42, 0.9);
}

.reader-page.sepia .floating-nav {
  background-color: rgba(247, 243, 233, 0.9);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 50%;
  margin: 4px 0;
}

.nav-item:hover {
  background-color: rgba(64, 158, 255, 0.1);
  transform: scale(1.1);
}

.reader-page.dark .nav-item:hover {
  background-color: rgba(64, 158, 255, 0.2);
}

.reader-page.sepia .nav-item:hover {
  background-color: rgba(64, 158, 255, 0.15);
}

.reader-page.light-gray .nav-item:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

.reader-page.beige .nav-item:hover {
  background-color: rgba(64, 158, 255, 0.12);
}

.reader-page.light-green .nav-item:hover {
  background-color: rgba(64, 158, 255, 0.12);
}

.nav-item .el-icon {
  font-size: 18px;
  margin-bottom: 2px;
  color: #606266;
}

.reader-page.dark .nav-item .el-icon {
  color: #cccccc;
}

.reader-page.sepia .nav-item .el-icon {
  color: #5c4b37;
}

.nav-item span {
  font-size: 11px;
  color: #606266;
  text-align: center;
  line-height: 1;
}

.reader-page.dark .nav-item span {
  color: #cccccc;
}

.reader-page.sepia .nav-item span {
  color: #5c4b37;
}

.reader-page.light-gray .nav-item span {
  color: #666666;
}

.reader-page.beige .nav-item span {
  color: #5c4b37;
}

.reader-page.light-green .nav-item span {
  color: #2d4a2d;
}

.nav-item.scroll-top {
  background-color: #409eff;
}

.nav-item.scroll-top .el-icon,
.nav-item.scroll-top span {
  color: #ffffff;
}

/* 评论弹窗 */
.comments-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 300;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
}

.comments-modal.show {
  opacity: 1;
  visibility: visible;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-content {
  position: relative;
  width: 80%;
  max-width: 800px;
  height: 80vh;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  transform: scale(0.9);
  transition: transform 0.3s ease;
}

.comments-modal.show .modal-content {
  transform: scale(1);
}

.reader-page.dark .modal-content {
  background-color: #2a2a2a;
  color: #ccc;
}

.reader-page.sepia .modal-content {
  background-color: #f7f3e9;
  color: #5c4b37;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.reader-page.dark .modal-header {
  border-bottom-color: #444;
}

.reader-page.sepia .modal-header {
  border-bottom-color: #e8e0d0;
}

.reader-page.light-gray .modal-header {
  border-bottom-color: #e0e0e0;
}

.reader-page.beige .modal-header {
  border-bottom-color: #e8e0d0;
}

.reader-page.light-green .modal-header {
  border-bottom-color: #e0e8e0;
}

.modal-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 字体选项按钮样式 */
.font-options {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.font-option {
  padding: 5px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
  background-color: #fff;
}

.font-option:hover {
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-1px);
}

.font-option.active {
  background-color: #409eff;
  color: #fff;
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.reader-page.dark .font-option {
  background-color: #2a2a2a;
  border-color: #444;
  color: #ccc;
}

.reader-page.dark .font-option:hover {
  border-color: #409eff;
  color: #409eff;
}

.reader-page.dark .font-option.active {
  background-color: #409eff;
  color: #fff;
}

.reader-page.sepia .font-option {
  background-color: #f7f3e9;
  border-color: #d4c5a9;
  color: #5c4b37;
}

.reader-page.sepia .font-option:hover {
  border-color: #409eff;
  color: #409eff;
}

.reader-page.sepia .font-option.active {
  background-color: #409eff;
  color: #fff;
}

/* 字体大小选项按钮样式 */
.font-size-options {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.font-size-option {
  padding: 5px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
  background-color: #fff;
  min-width: 28px;
  text-align: center;
}

.font-size-option:hover {
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-1px);
}

.font-size-option.active {
  background-color: #409eff;
  color: #fff;
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.reader-page.dark .font-size-option {
  background-color: #2a2a2a;
  border-color: #444;
  color: #ccc;
}

.reader-page.dark .font-size-option:hover {
  border-color: #409eff;
  color: #409eff;
}

.reader-page.dark .font-size-option.active {
  background-color: #409eff;
  color: #fff;
}

.reader-page.sepia .font-size-option {
  background-color: #f7f3e9;
  border-color: #d4c5a9;
  color: #5c4b37;
}

.reader-page.sepia .font-size-option:hover {
  border-color: #409eff;
  color: #409eff;
}

.reader-page.sepia .font-size-option.active {
  background-color: #409eff;
  color: #fff;
}

/* 页面宽度选项按钮样式 */
.width-options {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.width-option {
  padding: 5px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
  background-color: #fff;
  min-width: 52px;
  text-align: center;
}

.width-option:hover {
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-1px);
}

.width-option.active {
  background-color: #409eff;
  color: #fff;
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.reader-page.dark .width-option {
  background-color: #2a2a2a;
  border-color: #444;
  color: #ccc;
}

.reader-page.dark .width-option:hover {
  border-color: #409eff;
  color: #409eff;
}

.reader-page.dark .width-option.active {
  background-color: #409eff;
  color: #fff;
}

.reader-page.sepia .width-option {
  background-color: #f7f3e9;
  border-color: #d4c5a9;
  color: #5c4b37;
}

.reader-page.sepia .width-option:hover {
  border-color: #409eff;
  color: #409eff;
}

.reader-page.sepia .width-option.active {
  background-color: #409eff;
  color: #fff;
}



/* 加减按钮控制样式 */
.adjust-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.adjust-value {
  min-width: 36px;
  text-align: center;
  font-weight: 500;
  color: #333;
  font-size: 13px;
}

/* 暗色模式下的加减按钮样式 */
.reader-page.dark .adjust-value {
  color: #e0e0e0;
}

.reader-page.sepia .adjust-value {
  color: #5c4b37;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .reader-content {
    max-width: 800px;
  }
}

@media (max-width: 992px) {
  .reader-content {
    max-width: 700px;
  }
}

@media (max-width: 768px) {
  .reader-footer {
    height: 60px;
    padding: 0 15px;
  }

  .reader-settings {
    width: 100%;
    right: -100%;
  }

  .chapter-sidebar {
    width: 80%;
    left: -80%;
  }

  .chapter-navigation {
    padding: 15px;
  }

  .chapter-dropdown {
    margin: 0 10px;
  }

  .reader-content {
    padding: 20px 15px 80px;
    font-size: 16px;
  }

  .reader-content h2 {
    font-size: 20px;
  }

  .floating-nav {
    right: 10px;
  }

  .nav-item {
    width: 50px;
    height: 50px;
  }

  .nav-item .el-icon {
    font-size: 18px;
  }

  .nav-item span {
    font-size: 10px;
  }

  .modal-content {
    width: 90%;
    height: 90vh;
  }
}

@media (max-width: 576px) {
  .reader-footer {
    height: 60px;
    padding: 0 12px;
  }

  .reader-footer .el-button {
    padding: 8px 12px;
    font-size: 13px;
  }

  .chapter-dropdown {
    width: 150px;
  }

  .reader-content {
    padding: 20px 12px 70px;
    font-size: 15px;
  }

  .reader-content h2 {
    font-size: 18px;
  }

  .reader-content p {
    line-height: 1.8;
    margin-bottom: 15px;
  }

  .settings-panel {
    padding: 15px;
  }

  .settings-header {
    padding: 15px;
  }

  .settings-content {
    padding: 15px;
  }

  .setting-item {
    margin-bottom: 20px;
  }

  .sidebar-header {
    padding: 15px;
  }

  .chapter-item {
    padding: 12px 15px;
  }
}
</style>
