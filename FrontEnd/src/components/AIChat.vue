<template>
  <!-- AI聊天窗口 -->
  <div class="ai-chat-overlay" v-if="visible" @click="handleCloseOverlay">
    <div class="ai-chat-window" @click.stop>
      <!-- 窗口头部 -->
      <div class="chat-header">
        <h3 class="chat-title">智能AI</h3>
        <div class="header-actions">
          <el-button
            type="text"
            size="small"
            @click="clearChatHistory"
            class="clear-history-btn"
          >
            清除历史
          </el-button>
          <button class="close-btn" @click="handleClose">
            <el-icon><Close /></el-icon>
          </button>
        </div>
      </div>

      <!-- 聊天内容区域 -->
      <div class="chat-content" ref="chatContentRef">
        <!-- 欢迎消息 -->
        <div class="message ai-message" v-if="messages.length === 0">
          <div class="message-avatar">
            <img src="../../dist/智能AI.png" alt="AI" class="avatar-img">
          </div>
          <div class="message-content">
            <div class="message-text">你好！我是智能AI，有什么可以帮助你的吗？</div>
            <div class="message-text" style="font-size: 12px; color: #909399; margin-top: 8px;">请输入书籍相关的描述，我会为你推荐合适的书籍</div>
          </div>
        </div>

        <!-- 聊天消息列表 -->
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['message', message.role === 'user' ? 'user-message' : 'ai-message']"
        >
          <div class="message-avatar">
            <img
              :src="message.role === 'user' ? userAvatar : '../../dist/智能AI.png'"
              :alt="message.role === 'user' ? '用户' : 'AI'"
              class="avatar-img"
            >
          </div>
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>

            <!-- 书籍推荐列表 -->
            <div v-if="message.role === 'ai' && message.bookRecommendations && message.bookRecommendations.length > 0" class="book-recommendations">
              <div class="recommendation-title">📚 推荐书籍：</div>
              <div class="book-list">
                <el-tag
                  v-for="book in message.bookRecommendations"
                  :key="book"
                  class="book-tag"
                  @click="handleBookSearch(book)"
                >
                  {{ book }} <el-icon><Search /></el-icon>
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载中状态 -->
        <div class="message ai-message" v-if="loading">
          <div class="message-avatar">
            <img src="../../dist/智能AI.png" alt="AI" class="avatar-img">
          </div>
          <div class="message-content">
            <div class="loading-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <div class="input-container">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            placeholder="请输入你的书籍需求...例如：推荐一些科幻小说"
            @keyup.enter="handleSendMessage"
            resize="none"
            maxlength="500"
            show-word-limit
            class="chat-input"
          ></el-input>
          <el-button
            type="primary"
            @click="handleSendMessage"
            :disabled="!inputMessage.trim() || loading"
            :loading="loading"
            class="send-button"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Close, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { aichatApi } from '@/api/AIChat.js'

// 定义组件属性
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

// 定义事件
const emit = defineEmits(['close'])

// 路由
const router = useRouter()

// 聊天内容引用
const chatContentRef = ref(null)

// 用户头像
const userAvatar = 'https://picsum.photos/seed/user/100/100.jpg'

// 聊天消息列表
const messages = ref([])

// 输入消息
const inputMessage = ref('')

// 加载状态
const loading = ref(false)

// 从localStorage加载对话历史
const loadChatHistory = () => {
  try {
    const savedHistory = localStorage.getItem('aiChatHistory')
    if (savedHistory) {
      const parsedHistory = JSON.parse(savedHistory)
      // 将时间戳字符串转换回Date对象
      messages.value = parsedHistory.map(msg => ({
        ...msg,
        timestamp: new Date(msg.timestamp)
      }))
      // 滚动到底部
      setTimeout(() => {
        if (chatContentRef.value) {
          chatContentRef.value.scrollTop = chatContentRef.value.scrollHeight
        }
      }, 100)
    }
  } catch (error) {
    console.error('加载对话历史失败:', error)
    messages.value = []
  }
}

// 保存对话历史到localStorage
const saveChatHistory = () => {
  try {
    localStorage.setItem('aiChatHistory', JSON.stringify(messages.value))
  } catch (error) {
    console.error('保存对话历史失败:', error)
  }
}

// 清除对话历史
const clearChatHistory = () => {
  messages.value = []
  localStorage.removeItem('aiChatHistory')
}

// 初始化时加载对话历史
onMounted(() => {
  loadChatHistory()
})

// 监听消息变化，自动保存对话历史
watch(messages, () => {
  saveChatHistory()
  // 滚动到底部
  setTimeout(() => {
    if (chatContentRef.value) {
      chatContentRef.value.scrollTop = chatContentRef.value.scrollHeight
    }
  }, 100)
}, { deep: true })

// 监听visible变化，自动滚动到底部
watch(() => props.visible, (newVal) => {
  if (newVal) {
    setTimeout(() => {
      if (chatContentRef.value) {
        chatContentRef.value.scrollTop = chatContentRef.value.scrollHeight
      }
    }, 100)
  }
})

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 处理书籍搜索
const handleBookSearch = (bookName) => {
  // 跳转到搜索页面，传递搜索参数，并标记为AI推荐
  router.push({
    path: '/search',
    query: {
      q: bookName,
      fromAI: 'true' // 标记为AI推荐，用于特判显示书籍请求提示
    }
  })
  // 关闭聊天窗口
  emit('close')
}

// 发送消息
const handleSendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || loading.value) return

  // 本地检查是否是找书描述
  if (!aichatApi.isBookRequest(content)) {
    ElMessage.warning('请输入与书籍相关的描述，以便我为你推荐合适的书籍')
    return
  }

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: content,
    timestamp: new Date()
  })

  // 清空输入框
  inputMessage.value = ''

  // 显示加载状态
  loading.value = true

  try {
    // 调用封装的AI聊天API
    const systemPrompt = '你是一个专业的书籍推荐助手，请根据用户的需求推荐合适的书籍。请在回复中使用书名号《》包裹书籍名称，以便于提取。'

    // 准备消息格式：只传递最近的用户消息，或者可以传递完整的对话历史
    const requestMessages = [
      { role: 'user', content: content }
    ]

    const response = await aichatApi.sendMessage(requestMessages, systemPrompt)

    // 处理AI回复
    if (response && response.choices && response.choices.length > 0) {
      const aiContent = response.choices[0].message.content

      // 从AI回复中提取书籍名称
      const bookRecommendations = aichatApi.extractBookNames(aiContent)

      // 添加AI回复
      messages.value.push({
        role: 'ai',
        content: aiContent,
        timestamp: new Date(),
        bookRecommendations: bookRecommendations
      })
    } else {
      throw new Error('AI回复格式错误')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败，请稍后重试')
  } finally {
    // 隐藏加载状态
    loading.value = false
  }
}

// 关闭聊天窗口
const handleClose = () => {
  emit('close')
}

// 点击遮罩层关闭
const handleCloseOverlay = () => {
  emit('close')
}
</script>

<style scoped>
/* AI聊天遮罩层 */
.ai-chat-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

/* AI聊天窗口 */
.ai-chat-window {
  width: 500px;
  max-width: 90vw;
  height: 600px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 聊天头部 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

/* 头部操作按钮容器 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 清除历史按钮样式 */
.clear-history-btn {
  font-size: 14px;
  color: #606266;
  transition: all 0.3s;
}

.clear-history-btn:hover {
  color: #f56c6c;
  background-color: rgba(245, 108, 108, 0.1);
}

.chat-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #909399;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.3s;
}

.close-btn:hover {
  background-color: #ecf5ff;
  color: #409eff;
}

/* 聊天内容区域 */
.chat-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #fafafa;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 消息样式 */
.message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-text {
  padding: 10px 14px;
  border-radius: 8px;
  line-height: 1.5;
  word-wrap: break-word;
}

.message-time {
  font-size: 12px;
  color: #909399;
  align-self: flex-end;
}

/* AI消息样式 */
.ai-message .message-text {
  background-color: #f0f9eb;
  color: #389e0d;
  border: 1px solid #c2e7b0;
}

/* 用户消息样式 */
.user-message {
  flex-direction: row-reverse;
  align-items: flex-start;
}

.user-message .message-content {
  align-items: flex-end;
}

.user-message .message-text {
  background-color: #ecf5ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

/* 输入区域 */
.chat-input-area {
  padding: 16px 20px;
  background-color: white;
  border-top: 1px solid #e4e7ed;
}

/* 输入容器样式 */
.input-container {
  display: flex;
  gap: 10px;
  align-items: stretch;
}

/* 聊天输入框样式 */
.chat-input {
  flex: 1;
}

/* 发送按钮样式 */
.send-button {
  width: 80px;
  font-weight: 500;
  height: auto;
}

/* 加载状态 */
.loading-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.loading-dots span {
  width: 6px;
  height: 6px;
  background-color: #909399;
  border-radius: 50%;
  animation: loading-dot 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes loading-dot {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 滚动条样式 */
.chat-content::-webkit-scrollbar {
  width: 6px;
}

.chat-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 书籍推荐列表样式 */
.book-recommendations {
  margin-top: 12px;
  padding: 10px;
  background-color: rgba(56, 158, 13, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(56, 158, 13, 0.2);
}

.recommendation-title {
  font-size: 14px;
  font-weight: 600;
  color: #389e0d;
  margin-bottom: 8px;
}

.book-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.book-tag {
  cursor: pointer;
  background-color: #ecf5ff;
  color: #409eff;
  border-color: #91d5ff;
  transition: all 0.3s;
}

.book-tag:hover {
  background-color: #d6ecff;
  border-color: #66b1ff;
  transform: translateY(-1px);
}

.book-tag .el-icon {
  margin-left: 4px;
  font-size: 12px;
}
</style>
