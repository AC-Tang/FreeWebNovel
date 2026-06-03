import axios from 'axios'

// DeepSeek API配置
const deepSeekConfig = {
  apiKey: 'sk-a621d6a014b44d14bcb06fc8643b9572',
  baseUrl: 'https://api.deepseek.com',
  chatEndpoint: '/chat/completions',
  timeout: 60000,
  maxTokens: 2000,
  temperature: 0.7,
  model: 'deepseek-chat'
}

// AI聊天相关API
export const aichatApi = {
  // 发送聊天消息
  async sendMessage(messages, systemPrompt = null) {
    try {
      const requestMessages = []
      
      // 添加系统提示词
      if (systemPrompt) {
        requestMessages.push({
          role: 'system',
          content: systemPrompt
        })
      }
      
      // 添加用户和AI消息
      requestMessages.push(...messages)
      
      const response = await axios.post(
        `${deepSeekConfig.baseUrl}${deepSeekConfig.chatEndpoint}`,
        {
          model: deepSeekConfig.model,
          messages: requestMessages,
          temperature: deepSeekConfig.temperature,
          max_tokens: deepSeekConfig.maxTokens,
          stream: false
        },
        {
          headers: {
            'Authorization': `Bearer ${deepSeekConfig.apiKey}`,
            'Content-Type': 'application/json'
          },
          timeout: deepSeekConfig.timeout
        }
      )
      
      return response.data
    } catch (error) {
      console.error('发送AI消息失败:', error)
      throw error
    }
  },
  
  // 从AI回复中提取书籍名称
  extractBookNames(content) {
    // 简单的书籍名称提取逻辑
    // 假设书籍名称通常出现在书名号《》中，或者前后有特定标识
    const bookNames = []
    
    // 提取书名号中的内容
    const bookTitleRegex = /《([^》]+)》/g
    let match
    while ((match = bookTitleRegex.exec(content)) !== null) {
      bookNames.push(match[1].trim())
    }
    
    // 如果没有书名号，尝试提取可能的书籍名称
    if (bookNames.length === 0) {
      // 简单的规则：提取以"《"开头或"》"结尾的内容，或者可能的书籍名称格式
      const lines = content.split(/[\n。！？；;]/)
      lines.forEach(line => {
        const trimmedLine = line.trim()
        // 匹配类似 "1. 书籍名称" 或 "- 书籍名称" 的格式
        const listItemRegex = /^(?:\d+\.|-|\*|•)\s*(.+)$/g
        let listMatch
        while ((listMatch = listItemRegex.exec(trimmedLine)) !== null) {
          const bookName = listMatch[1].trim()
          // 过滤掉太短的内容，避免误判
          if (bookName.length > 2 && !bookName.includes('：') && !bookName.includes(':')) {
            bookNames.push(bookName)
          }
        }
      })
    }
    
    // 去重
    return [...new Set(bookNames)]
  },
  
  // 检查是否是找书描述
  isBookRequest(content) {
    // 关键词列表，用于判断是否是找书请求
    const bookKeywords = [
      '推荐', '找', '搜索', '书籍', '小说', '书', '读物', '作品',
      '科幻', '玄幻', '武侠', '言情', '历史', '悬疑', '推理',
      '恐怖', '惊悚', '喜剧', '悲剧', '经典', '畅销', '热门',
      '好看', '不错', '精彩', '值得', '阅读', '看', '想读',
      '类似', '像', '比如', '例如', '关于', '有关', '主题',
      '作者', '作家', '写的', '作品', '著作', '文集', '选集'
    ]
    
    const contentLower = content.toLowerCase()
    return bookKeywords.some(keyword => contentLower.includes(keyword.toLowerCase()))
  }
}
