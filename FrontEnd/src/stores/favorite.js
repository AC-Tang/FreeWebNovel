import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useFavoriteStore = defineStore('favorite', () => {
  // 收藏的书籍列表
  const favorites = ref([])
  
  // 计算属性：收藏的书籍数量
  const favoriteCount = computed(() => favorites.value.length)
  
  // 计算属性：检查某本书是否已收藏
  const isFavorite = (bookId) => {
    return favorites.value.some(book => book.id === bookId)
  }
  
  // 获取收藏的书籍ID列表
  const getFavoriteIds = () => {
    return favorites.value.map(book => book.id)
  }
  
  // 添加书籍到收藏
  const addToFavorites = (book) => {
    if (!isFavorite(book.id)) {
      favorites.value.unshift(book)
      saveFavorites()
      return true
    }
    return false
  }
  
  // 从收藏中移除书籍
  const removeFromFavorites = (bookId) => {
    const index = favorites.value.findIndex(book => book.id === bookId)
    if (index !== -1) {
      favorites.value.splice(index, 1)
      saveFavorites()
      return true
    }
    return false
  }
  
  // 切换收藏状态
  const toggleFavorite = (book) => {
    if (isFavorite(book.id)) {
      return removeFromFavorites(book.id)
    } else {
      return addToFavorites(book)
    }
  }
  
  // 清空收藏列表
  const clearFavorites = () => {
    favorites.value = []
    saveFavorites()
  }
  
  // 从本地存储加载收藏列表
  const loadFavorites = () => {
    try {
      const savedFavorites = localStorage.getItem('favorites')
      if (savedFavorites) {
        favorites.value = JSON.parse(savedFavorites)
      }
    } catch (error) {
      console.error('加载收藏列表失败:', error)
      favorites.value = []
    }
  }
  
  // 保存收藏列表到本地存储
  const saveFavorites = () => {
    try {
      localStorage.setItem('favorites', JSON.stringify(favorites.value))
    } catch (error) {
      console.error('保存收藏列表失败:', error)
    }
  }
  
  // 获取收藏的书籍，按收藏时间倒序
  const getFavoritesByTime = () => {
    return [...favorites.value]
  }
  
  // 获取收藏的书籍，按更新时间倒序
  const getFavoritesByUpdateTime = () => {
    return [...favorites.value].sort((a, b) => {
      return new Date(b.updateTime) - new Date(a.updateTime)
    })
  }
  
  // 获取收藏的书籍，按点击量倒序
  const getFavoritesByClickCount = () => {
    return [...favorites.value].sort((a, b) => {
      return b.clickCount - a.clickCount
    })
  }
  
  // 初始化收藏列表
  const initFavorites = () => {
    loadFavorites()
  }
  
  return {
    favorites,
    favoriteCount,
    isFavorite,
    getFavoriteIds,
    addToFavorites,
    removeFromFavorites,
    toggleFavorite,
    clearFavorites,
    loadFavorites,
    saveFavorites,
    getFavoritesByTime,
    getFavoritesByUpdateTime,
    getFavoritesByClickCount,
    initFavorites
  }
})