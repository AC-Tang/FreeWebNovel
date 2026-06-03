<template>
  <div class="comment-section">
    <div class="comment-header">
      <h3 class="comment-title">本章章评 ({{ totalComments }})</h3>
      <div class="comment-sort">
        <el-radio-group v-model="sortType" @change="sortComments">
          <el-radio-button label="newest">最新</el-radio-button>
          <el-radio-button label="hottest">最热</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 固定的发表评论表单容器 -->
    <div class="comment-form-container">
      <!-- 发表评论 -->
      <div class="comment-form" v-if="isAuthenticated">
        <div class="comment-user-info">
          <el-avatar :size="40" :src="currentUser.avatar ? `data:image/jpeg;base64,${currentUser.avatar}` : 'https://picsum.photos/seed/user/40/40.jpg'" />
          <span class="username">{{ currentUser.username }}</span>
        </div>
        <div class="comment-input">
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            placeholder="发表你的评论..."
            maxlength="500"
            show-word-limit
          />
          <div class="comment-actions">
            <el-button type="primary" @click="submitComment" :loading="submitting">发表评论</el-button>
          </div>
        </div>
      </div>

      <!-- 未登录提示 -->
      <div class="login-prompt" v-else>
        <p>登录后才能发表评论</p>
        <el-button type="primary" @click="$router.push('/login')">立即登录</el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <div v-if="loading" class="comment-loading">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="comments.length === 0" class="empty-comments">
        <el-empty description="暂无评论" />
      </div>

      <div v-else>
        <div v-for="comment in comments" :key="comment.id" class="comment-item" @click="goToCommentDetail(comment.id)">
          <div class="comment-user">
            <el-avatar :size="40" :src="comment.user.avatar " />
            <div class="user-info">
              <div class="username">{{ comment.user.username }}</div>
              <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
            </div>
            <!-- 评论操作菜单 -->
            <div class="comment-actions-menu">
              <el-dropdown>
                <el-button type="text" circle>
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click.stop="reportReview(comment.id)">举报</el-dropdown-item>
                    <el-dropdown-item
                      v-if="authStore.isAuthenticated && comment.user.id === authStore.userId"
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

          <div class="comment-content-wrapper">
            <div class="comment-content">
              <p>{{ comment.content }}</p>
              <!-- 全部回复显示 -->
              <div v-if="comment.replyCount > 0" class="all-replies" @click.stop="goToCommentDetail(comment.id)">
                全部{{ comment.replyCount }}条回复>
              </div>
            </div>

            <div class="comment-footer">
              <div class="comment-actions">
                <div
                  class="action-item"
                  :class="{ 'active': comment.isLiked }"
                  @click.stop="toggleLike(comment)"
                >
                  <el-icon><Star /></el-icon>
                  <span>{{ comment.likeCount }}</span>
                </div>
              </div>
            </div>
          </div>


        </div>


      </div>
    </div>
  </div>

  <!-- 举报对话框 -->
  <el-dialog v-model="showReportDialog" title="举报评论" width="500px" @close="handleReportDialogClose" :z-index="10000">
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

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, More } from '@element-plus/icons-vue'
import { commentsApi } from '../api/Comment'
import { likesApi } from '../api/Like'
import { reportsApi } from '../api/Reports'

const props = defineProps({
  targetType: {
    type: String,
    required: true,
    validator: (value) => ['book', 'chapter'].includes(value)
  },
  bookId: {
    type: Number,
    required: true
  },
  chapterId: {
    type: Number,
    required: true
  }
})

const authStore = useAuthStore()
const router = useRouter()

// 评论数据
const comments = ref([])
const totalComments = ref(0)
const loading = ref(false)
const sortType = ref('newest')

// 发表评论
const newComment = ref('')
const submitting = ref(false)

// 当前用户
const isAuthenticated = computed(() => authStore.isAuthenticated)
const currentUser = computed(() => authStore.user || { username: '游客', avatar: '' })

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

// 获取评论列表
const fetchComments = async () => {
  comments.value = []
  loading.value = true

  try {
    // 调用真实API获取评论
    let response
    if (props.targetType === 'chapter') {
      // 获取章节评论
      response = await commentsApi.getCommentsByChapterId(props.bookId, props.chapterId)
    } else {
      // 获取书籍评论（暂时保留，根据实际需求调整）
      return
    }

    if (response && response.code === 200) {
      // 处理API返回的评论数据
      const apiComments = response.data || []

      // 转换评论数据格式，适配组件
      let formattedComments = apiComments.map(comment => ({
        id: comment.id,
        user: {
          id: comment.userId || comment.user_id,
          username: comment.username || '匿名用户',
          avatar: comment.avatar ? `data:image/jpeg;base64,${comment.avatar}` : 'https://picsum.photos/seed/default_user/40/40.jpg'
        },
        content: comment.content || '',
        likeCount: comment.likeCount || 0,
        isLiked: false, // 初始化为未点赞，后续可根据API返回调整
        replyCount: comment.replyCount || 0,
        createTime: new Date(comment.createdAt || comment.createTime || new Date())
      }))

      // 如果用户已登录，检查每个评论的点赞状态
      if (authStore.isAuthenticated) {
        for (let i = 0; i < formattedComments.length; i++) {
          try {
            const likeResponse = await likesApi.existLike(formattedComments[i].id, authStore.userId, 1)
            if (likeResponse && likeResponse.code === 200) {
              formattedComments[i].isLiked = likeResponse.data
            }
          } catch (error) {
            console.error(`检查评论${formattedComments[i].id}的点赞状态失败:`, error)
            // 继续处理其他评论，不中断整个流程
          }
        }
      }

      comments.value = formattedComments
      totalComments.value = formattedComments.length
    } else {
      throw new Error(response?.message || '获取评论失败')
    }
  } catch (error) {
    console.error('获取评论失败:', error)
    ElMessage.error('获取评论失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 排序评论
const sortComments = () => {
  if (sortType.value === 'newest') {
    comments.value.sort((a, b) => b.createTime - a.createTime)
  } else {
    comments.value.sort((a, b) => b.likeCount - a.likeCount)
  }
}

// 提交评论
const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true

  try {
    // 调用真实API发表评论
    const commentData = {
      userId: authStore.userId,
      bookId: props.bookId,
      chapterId: props.chapterId,
      commentType: 1, // 1表示章评
      parentId: null, // 发布章评，parentId为null
      content: newComment.value
    }
    const response = await commentsApi.publishComment(commentData)

    if (response && response.code === 200) {
      // 刷新评论列表
      await fetchComments()
      newComment.value = ''
      ElMessage.success('评论发表成功')
    } else {
      throw new Error(response?.message || '发表评论失败')
    }
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败，请稍后再试')
  } finally {
    submitting.value = false
  }
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



// 删除评论
const deleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？删除后不可恢复。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 调用API删除评论
    const response = await commentsApi.deleteComment(commentId)

    if (response && response.code === 200) {
      // 删除成功提示
      ElMessage.success('删除成功')
      // 刷新评论列表
      await fetchComments(true)
    } else {
      // 删除失败提示
      ElMessage.error(response?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败，请稍后再试')
    }
  }
}

// 打开举报对话框
const reportReview = (commentId) => {
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

// 跳转到评论详情页
const goToCommentDetail = (commentId) => {
  // 章评类型为1
  router.push(`/comment/${commentId}?type=1`)
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

// 组件挂载时获取评论
onMounted(() => {
  authStore.initAuth()
  fetchComments()
})
</script>

<style scoped>
.comment-section {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 固定发表评论表单的容器 */
.comment-form-container {
  position: sticky;
  top: 20px;
  z-index: 100;
  background-color: white;
  padding: 20px 0;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.comment-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.comment-form {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.comment-user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.username {
  font-size: 14px;
  color: #666;
}

.comment-input {
  flex: 1;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 30px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 30px;
}

.login-prompt p {
  margin: 0;
  color: #666;
}

.comment-list {
  max-height: calc(100vh - 300px); /* 设置最大高度，减去头部、表单等高度 */
  overflow-y: auto; /* 添加垂直滚动条 */
  padding-right: 10px; /* 为滚动条预留空间 */
  scroll-behavior: smooth; /* 平滑滚动效果 */
}

/* 自定义滚动条样式 */
.comment-list::-webkit-scrollbar {
  width: 6px; /* 滚动条宽度 */
}

.comment-list::-webkit-scrollbar-track {
  background: #f1f1f1; /* 滚动条轨道背景 */
  border-radius: 3px;
}

.comment-list::-webkit-scrollbar-thumb {
  background: #c1c1c1; /* 滚动条滑块颜色 */
  border-radius: 3px;
}

.comment-list::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1; /* 滚动条滑块悬停颜色 */
}

.comment-loading {
  padding: 20px;
}

.empty-comments {
  padding: 40px 0;
}

.comment-item {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.comment-user {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.user-info {
  flex: 1;
}

.comment-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.comment-content-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.comment-content {
  line-height: 1.6;
  color: #333;
  flex: 1;
}

.all-replies {
  margin-top: 10px;
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
}

.all-replies:hover {
  text-decoration: underline;
}

.comment-footer {
  display: flex;
  justify-content: flex-end;
  margin-left: 20px;
  align-items: flex-start;
}

.comment-actions {
  display: flex;
  gap: 20px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 0.3s;
}

.action-item:hover {
  color: #409eff;
}

.action-item.active {
  color: #409eff;
}

.reply-list {
  margin-top: 15px;
  padding-left: 52px;
  background-color: #f9f9f9;
  border-radius: 8px;
  padding-top: 15px;
  padding-right: 15px;
  padding-bottom: 15px;
}

.reply-item {
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.reply-user {
  display: flex;
  gap: 10px;
  flex: 1;
}

.comment-actions-menu,
.reply-actions-menu {
  display: flex;
  align-items: center;
}

.reply-content {
  margin-bottom: 10px;
  line-height: 1.5;
  color: #333;
  font-size: 14px;
}

.reply-footer {
  display: flex;
  justify-content: flex-end;
}

.reply-actions {
  display: flex;
  gap: 15px;
}

.load-more-replies {
  text-align: center;
  margin-top: 10px;
}

.reply-form {
  margin-top: 15px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  display: flex;
  gap: 10px;
}

.reply-user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.reply-input {
  flex: 1;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .comment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .comment-form {
    flex-direction: column;
    gap: 10px;
  }

  .comment-user-info {
    flex-direction: row;
    align-items: center;
  }

  .reply-list {
    padding-left: 20px;
  }

  .reply-form {
    flex-direction: column;
    gap: 10px;
  }

  .reply-user-info {
    flex-direction: row;
    align-items: center;
  }
}
</style>
