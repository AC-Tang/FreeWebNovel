import request from '@/utils/request'

// 书籍相关API
export const booksApi = {
  // 获取所有书籍
  getAllBooks() {
    return request({
      url: '/books/fetch/all',
      method: 'get',
    })
  },
  // 根据状态获取书籍
  getBooksByStatus(status) {
    return request({
      url: `/books/fetch/status`,
      method: 'get',
      params: {
        status: status,
      },
    })
  },

  // 根据ID获取书籍
  getBookById(bookId) {
    return request({
      url: `/books/fetch/${bookId}`,
      method: 'get',
    })
  },

  // 根据分类获取书籍
  getBooksByCategory(category) {
    return request({
      url: `/books/fetch/Category`,
      method: 'get',
      params: {
        categoryId: category,
      },
    })
  },

  // 添加书籍点击量
  addViewCount(bookId) {
    return request({
      url: `/books/add/viewCount`,
      method: 'post',
      params: {
        bookId: bookId,
      },
    })
  },
  // 获取书籍数量
  getBookCount() {
    return request({
      url: `/books/count`,
      method: 'get',
    })
  },
  //更新书籍
  updateBooks(data) {
    return request({
      url: '/books/update',
      method: 'put',
      data,
    })
  },
  //删除书籍
  deleteBooks(novelId) {
    return request({
      url: `/books/delete`,
      method: 'delete',
      params: {
        novelId: novelId,
      },
    })
  },
  // 批量更新书籍状态
  batchUpdateBooksStatus(novelIds, status) {
    return request({
      url: '/books/batchUpdateStatus',
      method: 'put',
      data: {
        bookIds: novelIds,
        status: status,
      },
    })
  },
  // 批量删除书籍
  batchDeleteBooks(novelIds) {
    return request({
      url: '/books/batchDelete',
      method: 'delete',
      data: {
        bookIds: novelIds,
      },
    })
  },
  //添加书籍
  addBooks(data) {
    return request({
      url: '/books/add',
      method: 'post',
      data,
    })
  },
}
