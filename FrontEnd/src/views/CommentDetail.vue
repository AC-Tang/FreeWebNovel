<template>
  <PageTransition>
    <LoadingSpinner v-if="loading" />
    <div v-else class="comment-detail-page">
      <div class="comment-detail-content">
        <!-- 返回按钮 -->
        <div class="back-button">
          <el-button @click="goBack" type="primary">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
        </div>

        <!-- 评论详情标题 -->
        <div class="comment-detail-header">
          <h1 class="comment-detail-title">评论详情</h1>
        </div>

        <!-- 评论主体 -->
        <div v-if="comment" class="main-comment">
          <!-- 评论项 -->
          <div class="comment-item">
            <!-- 评论右上角三个点 -->
            <div class="review-actions-menu">
              <el-dropdown>
                <el-button type="text" circle>
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="reportComment">举报</el-dropdown-item>
                    <el-dropdown-item
                      v-if="authStore.isAuthenticated && comment.userId === authStore.user.id"
                      @click.stop="deleteComment(comment.id)"
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
                <el-avatar :src="comment.avatar" />
                <div class="reviewer-details">
                  <span class="reviewer-name">{{ comment.username }}</span>
                  <span class="review-time">{{ formatTime(comment.createTime) }}</span>
                </div>
              </div>
            </div>
            <!-- 评分星星 - 只有书评显示评分 -->
            <div class="main-comment-rating" v-if="commentType === 3 && comment.rating">
              <el-rate :model-value="comment.rating" disabled size="medium" />
            </div>
            <div class="review-content">{{ comment.content }}</div>

            <!-- 评论底部操作区 -->
            <div class="review-footer">
              <!-- 点赞 -->
              <div class="review-actions">
                <!-- 点赞 - 右下角 -->
                <div class="action-item" @click="likeComment">
                  <el-icon>
                    <Star v-if="!comment.isLiked" />
                    <StarFilled v-else />
                  </el-icon>
                  <span>{{ comment.likeCount }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 发表评论输入框 -->
        <div class="comment-input-section">
          <h3 class="comment-input-title">发表评论</h3>
          <div v-if="authStore.isAuthenticated">
            <el-input
              v-model="commentForm.content"
              type="textarea"
              :rows="4"
              placeholder="写下你的评论..."
              maxlength="500"
              show-word-limit
              @keyup.enter.ctrl="submitComment"
            />
            <div class="comment-input-actions">
            <el-button type="primary" @click="submitComment" :loading="submittingComment">发表评论</el-button>
          </div>
          </div>
          <div class="login-prompt" v-else>
            <p>请先登录后再发表评论</p>
            <el-button type="primary" @click="goToLogin">去登录</el-button>
          </div>
        </div>

        <!-- 楼中楼评论列表 -->
        <div class="replies-section" v-if="comment && comment.replyCount > 0">
          <h3 class="replies-title">全部{{ comment.replyCount }}条回复</h3>
          <div class="replies-list" v-loading="repliesLoading">
            <div v-for="reply in replies" :key="reply.id" class="reply-item">
              <!-- 回复右上角三个点 -->
              <div class="reply-actions-menu">
                <el-dropdown>
                  <el-button type="text" circle>
                    <el-icon><More /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click.stop="reportReview(reply.id)">举报</el-dropdown-item>
                      <el-dropdown-item
                        v-if="authStore.isAuthenticated && reply.userId === authStore.user.id"
                        @click.stop="deleteComment(reply.id)"
                        style="color: #f56c6c;"
                      >
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>

              <div class="reply-main">
                <!-- 回复者信息（头像+名称） -->
                <div class="reply-avatar">
                  <el-avatar :src="reply.avatar" size="small" />
                </div>
                <div class="reply-info">
                  <!-- 回复者名称 -->
                  <div class="reply-name">{{ reply.username }}</div>

                  <!-- 回复内容 -->
                  <div class="reply-content">
                    {{ reply.content }}
                  </div>

                  <!-- 回复时间、回复按钮、点赞 -->
                  <div class="reply-footer">
                    <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                    <div class="reply-actions">
                      <div class="action-item reply-to-action" @click="replyTo(reply)">
                        <span>回复</span>
                      </div>
                      <div class="action-item like-action" @click="likeReply(reply)">
                        <el-icon>
                          <Star v-if="!reply.isLiked" />
                          <StarFilled v-else />
                        </el-icon>
                        <span>{{ reply.likeCount }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- 全部x条回复按钮 -->
                  <div v-if="reply.replyCount > 0" class="all-replies-button">
                    <el-button type="text" class="all-replies-link" @click="showThirdLevelReplies(reply.id)">
                      全部{{ reply.replyCount }}条回复>
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 回复输入框 -->
          <div class="reply-input-section" v-if="authStore.isAuthenticated">
            <div v-if="replyingTo" class="replying-to-info">
              <span>回复 @{{ replyingTo.username }}：</span>
              <el-button type="text" @click="cancelReply">取消</el-button>
            </div>
            <el-input
              v-model="commentForm.content"
              type="textarea"
              :rows="2"
              placeholder="写下你的回复..."
              maxlength="200"
              show-word-limit
              @keyup.enter.ctrl="submitComment"
            />
            <div class="reply-input-actions">
              <el-button type="primary" @click="submitComment" :loading="submittingComment">回复</el-button>
            </div>
          </div>
          <div class="login-prompt" v-else>
            <p>请先登录后再回复</p>
            <el-button type="primary" @click="goToLogin">去登录</el-button>
          </div>
        </div>

        <!-- 无评论提示 -->
        <div v-if="comment && comment.replyCount === 0" class="no-replies">
          <p>暂无回复</p>
        </div>
      </div>

      <!-- 自定义三级回复弹窗 -->
      <div v-if="showThirdLevelReply" class="custom-third-level-dialog">
        <!-- 遮罩层 -->
        <div class="dialog-overlay" @click="closeThirdLevelReply"></div>

        <!-- 弹窗内容 -->
        <div class="dialog-content">
          <!-- 弹窗头部 -->
          <div class="dialog-header">
            <h3 class="dialog-title">回复详情</h3>
            <button class="close-button" @click="closeThirdLevelReply">
              <el-icon><CircleClose /></el-icon>
            </button>
          </div>

          <!-- 弹窗主体 -->
          <div class="dialog-body">
            <!-- 回复列表 -->
            <div class="third-level-replies-list" v-loading="thirdLevelLoading">
              <!-- 主评论 -->
              <div v-if="thirdLevelReplies.length > 0" class="third-level-main-comment reply-item">
                <div class="reply-actions-menu">
                  <div class="custom-dropdown">
                    <button class="action-button" @click="toggleCustomMenu(thirdLevelReplies[0].id)">
                      <el-icon><More /></el-icon>
                    </button>
                    <div v-if="customMenuVisible[thirdLevelReplies[0].id]" class="custom-dropdown-menu">
                      <div class="menu-item" @click="reportReview(thirdLevelReplies[0].id)">举报</div>
                      <div
                        v-if="authStore.isAuthenticated && thirdLevelReplies[0].userId === authStore.userId"
                        class="menu-item delete-item"
                        @click="deleteComment(thirdLevelReplies[0].id)"
                      >
                        删除
                      </div>
                    </div>
                  </div>
                </div>

                <div class="reply-main">
                  <div class="reply-avatar">
                    <el-avatar :src="thirdLevelReplies[0].avatar" size="small" />
                  </div>
                  <div class="reply-info">
                    <div class="reply-name">{{ thirdLevelReplies[0].username }}</div>
                    <div class="reply-content">
                      {{ thirdLevelReplies[0].content }}
                    </div>
                    <div class="reply-footer">
                      <span class="reply-time">{{ formatTime(thirdLevelReplies[0].createdAt) }}</span>
                      <div class="reply-actions">
                        <div class="action-item reply-to-action" @click="replyTo(thirdLevelReplies[0])">
                          <span>回复</span>
                        </div>
                        <div class="action-item like-action" @click="likeReply(thirdLevelReplies[0])">
                          <el-icon>
                            <Star v-if="!thirdLevelReplies[0].isLiked" />
                            <StarFilled v-else />
                          </el-icon>
                          <span>{{ thirdLevelReplies[0].likeCount }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 全部评论标题 -->
              <div v-if="thirdLevelReplies.length > 1" class="all-comments-title">
                全部评论 {{ thirdLevelReplies.length - 1 }}
              </div>

              <!-- 其他回复 -->
              <div
                v-for="reply in thirdLevelReplies.slice(1)"
                :key="reply.id"
                class="third-level-other-reply reply-item"
              >
                <div class="reply-actions-menu">
                  <div class="custom-dropdown">
                    <button class="action-button" @click="toggleCustomMenu(reply.id)">
                      <el-icon><More /></el-icon>
                    </button>
                    <div v-if="customMenuVisible[reply.id]" class="custom-dropdown-menu">
                      <div class="menu-item" @click="reportReview(reply.id)">举报</div>
                      <div
                        v-if="authStore.isAuthenticated && reply.userId === authStore.userId"
                        class="menu-item delete-item"
                        @click="deleteComment(reply.id)"
                      >
                        删除
                      </div>
                    </div>
                  </div>
                </div>

                <div class="reply-main">
                  <div class="reply-avatar">
                    <el-avatar :src="reply.avatar" size="small" />
                  </div>
                  <div class="reply-info">
                    <div class="reply-name">{{ reply.username }}</div>
                    <!-- 回复内容，根据parentId显示回复对象 -->
                    <div class="reply-content">
                      <!-- 只有当parentId存在且不等于三级评论主评论id时，才显示回复对象 -->
                      <span v-if="reply.parentId && reply.parentId !== thirdLevelReplies[0].id" class="reply-to">
                        回复 @{{ findReplyUsername(reply.parentId) }}：
                      </span>
                      {{ reply.content }}
                    </div>
                    <div class="reply-footer">
                      <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                      <div class="reply-actions">
                        <div class="action-item reply-to-action" @click="replyTo(reply)">
                          <span>回复</span>
                        </div>
                        <div class="action-item like-action" @click="likeReply(reply)">
                          <el-icon>
                            <Star v-if="!reply.isLiked" />
                            <StarFilled v-else />
                          </el-icon>
                          <span>{{ reply.likeCount }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 无回复提示 -->
            <div v-if="!thirdLevelLoading && thirdLevelReplies.length === 0" class="no-replies">
              <p>暂无回复</p>
            </div>
          </div>

          <!-- 弹窗底部（回复输入框） -->
          <div class="dialog-footer">
            <div class="reply-input-section" v-if="authStore.isAuthenticated">
              <div v-if="replyingTo" class="replying-to-info">
                <span>回复 @{{ replyingTo.username }}：</span>
                <el-button type="text" @click="cancelReply">取消</el-button>
              </div>
              <el-input
                v-model="commentForm.content"
                type="textarea"
                :rows="2"
                placeholder="写下你的回复..."
                maxlength="200"
                show-word-limit
                @keyup.enter.ctrl="submitComment"
              />
              <div class="reply-input-actions">
                        <el-button type="primary" @click="submitComment" :loading="submittingComment">回复</el-button>
                      </div>
            </div>
            <div class="login-prompt" v-else>
              <p>请先登录后再回复</p>
              <el-button type="primary" @click="goToLogin">去登录</el-button>
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
    </div>
  </PageTransition>
</template>

<script setup name="CommentDetailPage">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, StarFilled, ArrowLeft, More, CircleClose } from '@element-plus/icons-vue'
import PageTransition from '../components/PageTransition.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import { commentsApi } from '../api/Comment'
import { likesApi } from '../api/Like'
import { reportsApi } from '../api/Reports'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 加载状态
const loading = ref(true)
const repliesLoading = ref(false)

// 评论ID
const commentId = ref(route.params.id)
// 评论类型，1-章评，2-段评，3-书评
// 从路由参数中获取，支持query参数和params参数
const commentType = ref(Number(route.query.type || route.params.type) || 3)

// 评论数据
const comment = ref(null)
const replies = ref([])

// 评论表单（统一管理发表评论和回复）
const commentForm = ref({
  content: ''
})

// 回复对象（null表示直接回复主评论）
const replyingTo = ref(null)

// 提交评论加载状态
const submittingComment = ref(false)

// 三级回复相关状态
const showThirdLevelReply = ref(false) // 是否显示三级回复弹窗
const currentThirdLevelCommentId = ref(null) // 当前查看三级回复的评论ID
const thirdLevelReplies = ref([]) // 三级回复列表
const thirdLevelLoading = ref(false) // 三级回复加载状态
// 自定义操作菜单状态
const customMenuVisible = ref({}) // 使用对象存储每个评论的操作菜单可见性，key为评论id

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

// 页面挂载时获取评论详情
onMounted(async () => {
  await fetchCommentDetail()
})

// 获取评论详情和回复
const fetchCommentDetail = async (targetCommentId = commentId.value, isThirdLevel = false) => {
  try {
    if (isThirdLevel) {
      thirdLevelLoading.value = true
    } else {
      // 非三级回复时，同时设置页面级加载状态和回复列表加载状态
      loading.value = true
      repliesLoading.value = true
    }

    const response = await commentsApi.getCommentDetail(targetCommentId)
    if (response && response.code === 200 && Array.isArray(response.data)) {
      if (isThirdLevel) {
        // 处理三级回复列表
        thirdLevelReplies.value = response.data.map(reply => ({
          ...reply,
          avatar: reply.avatar ? `data:image/jpeg;base64,${reply.avatar}` : 'https://picsum.photos/seed/default_user/40/40.jpg',
          isLiked: false
        }))

        // 优化：并行检查点赞状态，减少加载时间
        if (authStore.isAuthenticated) {
          await Promise.all(
            thirdLevelReplies.value.map(reply => checkLikeStatus(reply, 2))
          )
        }
      } else {
        // 分离主评论和回复列表
        const [mainComment, ...replyList] = response.data

        // 处理主评论
        comment.value = {
          id: mainComment.id || '',
          bookId: mainComment.bookId || '',
          userId: mainComment.userId || '',
          username: mainComment.username || '匿名用户',
          avatar: mainComment.avatar ? `data:image/jpeg;base64,${mainComment.avatar}` : 'https://picsum.photos/seed/default_user/40/40.jpg',
          content: mainComment.content || '暂无内容',
          rating: mainComment.rating || 0,
          likeCount: mainComment.likeCount || 0,
          replyCount: replyList.length,
          createTime: mainComment.createdAt || new Date(),
          isLiked: false
        }

        // 处理回复列表
        replies.value = replyList.map(reply => ({
          ...reply,
          avatar: reply.avatar ? `data:image/jpeg;base64,${reply.avatar}` : 'https://picsum.photos/seed/default_user/40/40.jpg',
          isLiked: false
        }))

        // 优化：并行检查点赞状态，减少加载时间
        if (authStore.isAuthenticated) {
          // 并行检查主评论和所有回复的点赞状态
          await Promise.all([
            checkLikeStatus(comment.value, 1),
            ...replies.value.map(reply => checkLikeStatus(reply, 2))
          ])
        }
      }
    }
  } catch (error) {
    console.error('获取评论详情失败:', error)
    ElMessage.error('获取评论详情失败')
  } finally {
    if (isThirdLevel) {
      thirdLevelLoading.value = false
    } else {
      // 重置页面级加载状态和回复列表加载状态
      loading.value = false
      repliesLoading.value = false
    }
  }
}

// 检查点赞状态（同时适用于评论和回复）
const checkLikeStatus = async (target, targetType) => {
  try {
    const response = await likesApi.existLike(target.id, authStore.user.id, targetType)
    if (response && response.code === 200) {
      target.isLiked = response.data
    }
  } catch (error) {
    console.error('检查点赞状态失败:', error)
  }
}

// 点赞评论
const likeComment = async () => {
  if (!authStore.isAuthenticated) {
    await showLoginPrompt()
    return
  }

  try {
    if (comment.value.isLiked) {
      // 取消点赞
      await likesApi.cancelLike(authStore.user.id, comment.value.id)
      comment.value.likeCount--
      comment.value.isLiked = false
      ElMessage.success('取消点赞成功')
    } else {
      // 点赞
      await likesApi.like({
        userId: authStore.user.id,
        targetType: 1,
        targetId: comment.value.id
      })
      comment.value.likeCount++
      comment.value.isLiked = true
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('点赞操作失败，请稍后再试')
  }
}

// 点赞回复
const likeReply = async (reply) => {
  if (!authStore.isAuthenticated) {
    await showLoginPrompt()
    return
  }

  try {
    if (reply.isLiked) {
      // 取消点赞
      await likesApi.cancelLike(authStore.user.id, reply.id)
      reply.likeCount--
      reply.isLiked = false
      ElMessage.success('取消点赞成功')
    } else {
      // 点赞
      await likesApi.like({
        userId: authStore.user.id,
        targetType: 2,
        targetId: reply.id
      })
      reply.likeCount++
      reply.isLiked = true
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('点赞操作失败，请稍后再试')
  }
}

// 回复评论
const replyTo = (reply) => {
  if (!authStore.isAuthenticated) {
    showLoginPrompt()
    return
  }
  replyingTo.value = reply
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
  commentForm.value.content = ''
}

// 提交评论或回复
const submitComment = async () => {
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    // 显示提交加载状态
    submittingComment.value = true

    // 确定parentId
    let parentIdValue = commentId.value; // 默认值为主评论id

    if (replyingTo.value) {
      // 如果有明确的回复对象，使用该回复对象的id
      parentIdValue = replyingTo.value.id;
    } else if (showThirdLevelReply.value && thirdLevelReplies.value.length > 0) {
      // 如果在三级回复弹窗中且没有明确回复对象，使用三级回复主评论的id
      parentIdValue = thirdLevelReplies.value[0].id;
    }

    // 发表评论API需要的参数
    const commentData = {
      userId: authStore.user.id,
      bookId: comment.value.bookId || '',
      commentType: commentType.value, // 1:章评，2：段评，3：书评
      parentId: parentIdValue,
      content: commentForm.value.content
    }

    // 处理topParentId：所有回复都需要添加
    if (showThirdLevelReply.value && thirdLevelReplies.value.length > 0) {
      // 三级回复：topParentId为三级回复列表的第一个元素的id
      commentData.topParentId = thirdLevelReplies.value[0].id
    } else {
      // 二级回复：根据情况设置topParentId
      if (replyingTo.value) {
        // 如果是回复某个具体评论（楼中楼回复），topParentId为被回复的评论id
        commentData.topParentId = replyingTo.value.id
      } else {
        // 如果是直接回复主评论，topParentId为主评论的id
        commentData.topParentId = commentId.value
      }
    }

    const response = await commentsApi.publishComment(commentData)
    if (response && response.code === 200) {
      ElMessage.success(replyingTo.value ? '回复成功' : '评论发表成功')
      // 重置表单和回复对象
      commentForm.value.content = ''
      replyingTo.value = null

      // 优化：根据当前状态决定需要重新获取哪些数据
      if (showThirdLevelReply.value && currentThirdLevelCommentId.value) {
        // 如果在三级回复弹窗中，只重新获取三级回复列表
        await fetchCommentDetail(currentThirdLevelCommentId.value, true)
      } else {
        // 否则只重新获取主评论的回复列表
        await fetchCommentDetail()
      }
    }
  } catch (error) {
    console.error('提交评论失败:', error)
    ElMessage.error(replyingTo.value ? '回复失败，请稍后再试' : '发表评论失败，请稍后再试')
  } finally {
    // 无论成功失败，都重置提交加载状态
    submittingComment.value = false
  }
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

// 举报主评论
const reportComment = () => {
  // 设置举报目标ID
  reportForm.value.targetId = commentId.value
  // 设置举报者ID（如果用户已登录）
  if (authStore.isAuthenticated) {
    reportForm.value.reporterId = authStore.user.id
  }
  // 打开举报弹窗
  showReportDialog.value = true
}

// 举报回复
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
    if (error === 'cancel') {
      // 用户取消验证
      return
    }
    console.error('提交举报失败:', error)
    ElMessage.error('举报提交失败，请稍后重试')
  }
}

// 删除评论
const deleteComment = async (commentIdToDelete) => {
  try {
    // 关闭所有自定义菜单，避免影响对话框显示
    customMenuVisible.value = {}

    // 获取三级回复弹窗元素
    const thirdLevelDialog = document.querySelector('.custom-third-level-dialog')
    let originalZIndex = ''

    // 如果存在三级回复弹窗，临时降低其z-index
    if (thirdLevelDialog) {
      originalZIndex = thirdLevelDialog.style.zIndex
      thirdLevelDialog.style.zIndex = '999'
    }

    // 使用Element Plus的MessageBox组件
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？删除后不可恢复。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 调用删除评论API
    const response = await commentsApi.deleteComment(commentIdToDelete)

    if (response && response.code === 200) {
      // 删除成功提示
      ElMessage.success('评论删除成功')

      // 更新评论数据
      if (showThirdLevelReply.value) {
        // 如果显示三级回复弹窗，重新获取三级回复列表
        if (currentThirdLevelCommentId.value) {
          await fetchCommentDetail(currentThirdLevelCommentId.value, true)
        }
      } else {
        // 否则重新获取评论详情
        await fetchCommentDetail()
      }
    } else {
      // 删除失败提示
      ElMessage.error(response?.message || '评论删除失败')
    }

    // 恢复三级回复弹窗的原始z-index
    if (thirdLevelDialog) {
      thirdLevelDialog.style.zIndex = originalZIndex || '9999'
    }
  } catch (error) {
    // 恢复三级回复弹窗的原始z-index
    const thirdLevelDialog = document.querySelector('.custom-third-level-dialog')
    if (thirdLevelDialog) {
      thirdLevelDialog.style.zIndex = '9999'
    }

    // 如果是用户取消，不显示错误提示
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('评论删除失败，请稍后重试')
    }
  }
}

// 显示登录提示
const showLoginPrompt = async () => {
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
    router.push('/login')
  } catch {
    // 用户取消
  }
}

// 去登录
const goToLogin = () => {
  router.push('/login')
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 显示三级回复
const showThirdLevelReplies = async (replyId) => {
  currentThirdLevelCommentId.value = replyId
  await fetchCommentDetail(replyId, true)
  showThirdLevelReply.value = true
  // 禁止页面滚动
  document.body.style.overflow = 'hidden'
}

// 关闭三级回复
const closeThirdLevelReply = () => {
  showThirdLevelReply.value = false
  currentThirdLevelCommentId.value = null
  thirdLevelReplies.value = []
  // 关闭所有自定义菜单
  customMenuVisible.value = []
  // 恢复页面滚动
  document.body.style.overflow = ''
}

// 切换自定义操作菜单显示
const toggleCustomMenu = (commentId) => {
  // 切换当前菜单的显示状态
  customMenuVisible.value[commentId] = !customMenuVisible.value[commentId]
  // 如果当前菜单不存在，初始化为true
  if (customMenuVisible.value[commentId] === undefined) {
    customMenuVisible.value[commentId] = true
  }
}

// 根据replyId查找用户名
const findReplyUsername = (replyId) => {
  // 先在三级回复列表中查找
  const reply = thirdLevelReplies.value.find(item => item.id === replyId)
  if (reply) {
    return reply.username
  }

  // 如果找不到，返回默认值
  return '未知用户'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) {
    return '未知时间'
  }

  const now = new Date()
  const date = new Date(time)

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
</script>

<style scoped>
.comment-detail-page {
  padding: 20px 0;
  min-height: calc(100vh - 120px);
  margin-top: 60px;
}

.comment-detail-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.back-button {
  margin-bottom: 20px;
}

.comment-detail-header {
  margin-bottom: 30px;
}

.comment-detail-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

/* 主评论样式 */
.main-comment {
  margin-bottom: 40px;
}

/* 评论项样式 */
.comment-item {
  position: relative;
  padding: 20px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.review-actions-menu {
  position: absolute;
  top: 15px;
  right: 15px;
  z-index: 10;
}

.review-header {
  margin-bottom: 15px;
}

.reviewer-info {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.reviewer-details {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1;
}

.reviewer-name {
  font-weight: 500;
  color: #333;
  font-size: 16px;
}

.review-time {
  font-size: 12px;
  color: #999;
}

.review-rating {
  display: flex;
  align-items: center;
  margin-top: 2px;
}

.main-comment-rating {
  display: flex;
  align-items: center;
  margin: 10px 0;
  color: #e6a23c;
}

.review-content {
  line-height: 1.6;
  margin-bottom: 15px;
  color: #333;
}

.review-footer {
  margin-top: 15px;
  padding: 0 0 0 50px; /* 为了与头像对齐 */
}

.review-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 10px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  transition: color 0.3s;
}

.action-item:hover {
  color: #409eff;
}

/* 回复部分样式 */
.replies-section {
  margin-top: 30px;
  background-color: #fafafa;
  padding: 20px;
  border-radius: 8px;
}

.replies-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.replies-list {
  margin-bottom: 20px;
}

.reply-item {
  position: relative;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 15px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 回复右上角三个点 */
.reply-actions-menu {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
}

/* 回复主容器，包含头像和内容 */
.reply-main {
  display: flex;
  gap: 10px;
}

/* 头像列 */
.reply-avatar {
  display: flex;
  flex-shrink: 0;
}

/* 回复信息区域 */
.reply-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

/* 回复者名称 */
.reply-name {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

/* 回复内容 */
.reply-content {
  line-height: 1.5;
  color: #333;
  font-size: 14px;
  margin-bottom: 5px;
}

/* 回复底部信息：时间、回复按钮、点赞 */
.reply-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
}

/* 回复时间 */
.reply-time {
  font-size: 12px;
  color: #999;
}

/* 回复操作按钮组 */
.reply-actions {
  display: flex;
  gap: 20px;
  align-items: center;
}

/* 回复按钮 */
.reply-to-action {
  color: #409eff;
  font-size: 14px;
}

/* 点赞按钮 */
.like-action {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
  font-size: 14px;
}

/* 回复提示 */
.reply-to {
  color: #409eff;
  font-size: 14px;
}

/* 确保回复输入框与内容对齐 */
.reply-input-section {
  margin-left: 40px; /* 与头像对齐 */
  margin-top: 15px;
}

/* 确保楼中楼评论的回复输入框也与内容对齐 */
.replies-section .reply-input-section {
  margin-left: 0;
}

/* 回复输入区样式 */
.reply-input-section {
  margin-top: 20px;
}

.reply-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

/* 发表评论输入框样式 */
.comment-input-section {
  margin: 30px 0;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.comment-input-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.comment-input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

/* 回复输入框样式 */
.reply-input-section {
  margin-top: 20px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
}

.replying-to-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.replying-to-info span {
  color: #409eff;
}

.reply-input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

/* 登录提示样式 */
.login-prompt {
  text-align: center;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
  margin-top: 10px;
}

.login-prompt p {
  margin-bottom: 15px;
  color: #666;
}

/* 无评论提示样式 */
.no-comment {
  text-align: center;
  padding: 60px 0;
  color: #999;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 无回复提示样式 */
.no-replies {
  text-align: center;
  padding: 30px 0;
  color: #999;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .comment-detail-content {
    padding: 0 15px;
  }

  .comment-item {
    padding: 15px;
  }

  .reply-item {
    padding: 12px;
  }
}

/* 自定义三级回复弹窗样式 */
.custom-third-level-dialog {
  /* 固定定位，覆盖整个屏幕 */
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 9999;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

/* 确保Element Plus MessageBox始终显示在最上层 */
:deep(.el-message-box__wrapper) {
  z-index: 10000 !important;
}

:deep(.el-overlay) {
  z-index: 10000 !important;
}

/* 遮罩层 */
.dialog-overlay {
  /* 全屏遮罩 */
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1;
}

/* 弹窗内容 */
.dialog-content {
  /* 固定在底部，宽高固定 */
  position: relative;
  z-index: 2;
  width: 90%;
  height: 60vh;
  background-color: #fff;
  border-radius: 12px 12px 0 0;
  box-shadow: 0 -2px 12px 0 rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 弹窗头部 */
.dialog-header {
  /* 固定高度，灰色背景 */
  height: 60px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-sizing: border-box;
  flex-shrink: 0;
}

/* 弹窗标题 */
.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* 关闭按钮 */
.close-button {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-button:hover {
  color: #666;
}

/* 弹窗主体 */
.dialog-body {
  /* 自动填充剩余空间，显示滚动条 */
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  box-sizing: border-box;
  background-color: #fff;
}

/* 弹窗底部 */
.dialog-footer {
  /* 固定高度，灰色背景 */
  min-height: 120px;
  background-color: #fafafa;
  border-top: 1px solid #e4e7ed;
  padding: 15px 20px;
  box-sizing: border-box;
  flex-shrink: 0;
}

/* 确保滚动条始终显示 */
.dialog-body {
  scrollbar-width: thin;
  scrollbar-color: #888 #f1f1f1;
}

/* WebKit滚动条样式（Chrome, Safari, Edge） */
.dialog-body::-webkit-scrollbar {
  width: 8px;
}

.dialog-body::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.dialog-body::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.dialog-body::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* Firefox滚动条样式 */
@-moz-document url-prefix() {
  .dialog-body {
    scrollbar-width: thin;
    scrollbar-color: #888 #f1f1f1;
  }
}

.third-level-replies-list {
  margin-bottom: 20px;
}

/* 三级回复主评论样式 */
.third-level-main-comment {
  background-color: #fafafa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 全部评论标题样式 */
.all-comments-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 20px 0;
  padding: 10px 0;
  border-bottom: 1px solid #e4e7ed;
}

/* 三级回复其他评论样式 */
.third-level-other-reply {
  margin-bottom: 15px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 自定义操作菜单样式 */
.custom-dropdown {
  position: relative;
  display: inline-block;
  z-index: 100;
}

.action-button {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  color: #999;
  padding: 4px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.action-button:hover {
  background-color: #f0f0f0;
  color: #666;
}

.custom-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  min-width: 100px;
  z-index: 101;
  overflow: hidden;
}

.menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: background-color 0.3s;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

.delete-item {
  color: #f56c6c;
}

.delete-item:hover {
  background-color: #fef0f0;
}

/* 回复对象样式 */
.reply-to {
  color: #409eff;
  font-size: 14px;
  margin-right: 5px;
}

.all-replies-button {
  margin-top: 10px;
  text-align: left;
}

.all-replies-link {
  color: #409eff;
  font-size: 14px;
  padding: 0;
  height: auto;
  line-height: normal;
}
</style>
