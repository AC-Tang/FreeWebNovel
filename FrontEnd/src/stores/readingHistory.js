import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useReadingHistoryStore = defineStore('readingHistory', () => {
  // 阅读历史列表
  const historyList = ref([])
  
  // 最大历史记录数量
  const MAX_HISTORY_COUNT = 100
  
  // 从本地存储加载历史记录
  const loadHistory = () => {
    try {
      const savedHistory = localStorage.getItem('readingHistory')
      if (savedHistory) {
        historyList.value = JSON.parse(savedHistory)
      }
    } catch (error) {
      console.error('加载阅读历史失败:', error)
      historyList.value = []
    }
  }
  
  // 保存历史记录到本地存储
  const saveHistory = () => {
    try {
      localStorage.setItem('readingHistory', JSON.stringify(historyList.value))
    } catch (error) {
      console.error('保存阅读历史失败:', error)
    }
  }
  
  // 添加阅读记录
  const addToHistory = (bookInfo, chapterInfo, progress) => {
    // 检查是否已存在该书籍的记录
    const existingIndex = historyList.value.findIndex(item => item.bookId === bookInfo.id)
    
    const historyItem = {
      bookId: bookInfo.id,
      bookTitle: bookInfo.title,
      bookAuthor: bookInfo.author,
      bookCover: bookInfo.cover,
      chapterId: chapterInfo.id,
      chapterTitle: chapterInfo.title,
      progress: progress || 0,
      readTime: new Date().toISOString(), // 阅读时间
      lastReadTime: new Date().toISOString() // 最后阅读时间
    }
    
    if (existingIndex !== -1) {
      // 更新现有记录
      historyList.value[existingIndex] = {
        ...historyList.value[existingIndex],
        chapterId: chapterInfo.id,
        chapterTitle: chapterInfo.title,
        progress: progress || 0,
        lastReadTime: new Date().toISOString()
      }
      
      // 将更新的记录移到最前面
      const updatedItem = historyList.value.splice(existingIndex, 1)[0]
      historyList.value.unshift(updatedItem)
    } else {
      // 添加新记录
      historyList.value.unshift(historyItem)
      
      // 限制历史记录数量
      if (historyList.value.length > MAX_HISTORY_COUNT) {
        historyList.value = historyList.value.slice(0, MAX_HISTORY_COUNT)
      }
    }
    
    saveHistory()
  }
  
  // 获取书籍的最新阅读记录
  const getBookHistory = (bookId) => {
    return historyList.value.find(item => item.bookId === bookId)
  }
  
  // 删除阅读记录
  const removeFromHistory = (bookId) => {
    const index = historyList.value.findIndex(item => item.bookId === bookId)
    if (index !== -1) {
      historyList.value.splice(index, 1)
      saveHistory()
      return true
    }
    return false
  }
  
  // 清空所有历史记录
  const clearAllHistory = () => {
    historyList.value = []
    saveHistory()
  }
  
  // 按书籍分组历史记录（获取每本书的最新记录）
  const getGroupedByBook = () => {
    const bookMap = new Map()
    
    historyList.value.forEach(item => {
      if (!bookMap.has(item.bookId)) {
        bookMap.set(item.bookId, item)
      }
    })
    
    return Array.from(bookMap.values())
  }
  
  // 获取最近阅读的书籍
  const getRecentBooks = (limit = 10) => {
    return getGroupedByBook().slice(0, limit)
  }
  
  // 获取排序后的历史记录（按最后阅读时间）
  const sortedHistory = computed(() => {
    return [...historyList.value].sort((a, b) => 
      new Date(b.lastReadTime) - new Date(a.lastReadTime)
    )
  })
  
  // 初始化
  const initHistory = () => {
    loadHistory()
  }
  
  return {
    historyList,
    sortedHistory,
    addToHistory,
    getBookHistory,
    removeFromHistory,
    clearAllHistory,
    getGroupedByBook,
    getRecentBooks,
    initHistory
  }
})