import request from '@/utils/request'

/**
 * 书架相关API
 */
export const bookshelvesApi = {
  // 获取用户书架上的所有书籍(列表)
  getUserBookshelf(userId) {
    return request({
      url: `/bookshelves/all`,
      method: 'get',
      params: {
        userId: userId,
      },
    })
  },

  // 添加书籍到书架
  addBookToShelves(bookId, userId) {
    return request({
      url: `/bookshelves/add`,
      method: 'post',
      data: {
        bookId: bookId,
        userId: userId,
      },
    })
  },

  // 从书架中移除书籍
  removeBookFromShelves(bookId, userId) {
    return request({
      url: `/bookshelves/remove`,
      method: 'delete',
      data: {
        bookId: bookId,
        userId: userId,
      },
    })
  },

  // 创建书架
  createBookshelf(userId, name) {
    return request({
      url: `/bookshelves/create`,
      method: 'post',
      params: {
        userId: userId,
        name: name,
      },
    })
  },

  // 删除书架
  deleteBookshelf(bookshelfId) {
    return request({
      url: `/bookshelves/delete`,
      method: 'delete',
      params: {
        id: bookshelfId,
      },
    })
  },

  // 更新书架名称
  updateBookshelfName(data) {
    return request({
      url: `/bookshelves/update`,
      method: 'put',
      data: data,
    })
  },

  // 检查书籍是否在用户书架中
  existInBookshelf(bookId, userId) {
    return request({
      url: `/bookshelves/exist`,
      method: 'get',
      params: {
        novelId: bookId,
        userId: userId,
      },
    })
  },

  // 获取用户所有书架(名称)
  getUserBookshelves(userId) {
    return request({
      url: `/bookshelves/fetch`,
      method: 'get',
      params: {
        userId: userId,
      },
    })
  },
  // 获取对应书架中的书籍（列表）
  getBooksInBookshelf(userId, id) {
    return request({
      url: `/bookshelves/fetch/${id}`,
      method: 'get',
      params: {
        userId: userId,
      },
    })
  },
  // 移动(批量/单个)书籍到其他书架
  moveBookToBookshelf(bookId, userId, targetBookshelfId) {
    return request({
      url: `/bookshelves/move`,
      method: 'post',
      params: {
        booksId: bookId,
        userId: userId,
        id: targetBookshelfId,
      },
    })
  },
}
