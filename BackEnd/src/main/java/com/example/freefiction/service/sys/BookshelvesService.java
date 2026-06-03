package com.example.freefiction.service.sys;

import com.example.freefiction.entity.Bookshelves;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【bookshelves(书架表)】的数据库操作Service
* @createDate 2025-11-24 10:49:13
*/
@Service
public interface BookshelvesService extends IService<Bookshelves> {
    /**
     * 判断用户是否已添加该书籍到书架
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 是否已添加
     */
    ServiceResult<Boolean> existInBookshelves(Long userId, Long bookId);

    /**
     * 添加书籍到书架
     * @param bookshelves 书架信息
     * @return 是否添加成功
     */
    ServiceResult<Boolean> addBookToBookshelves(Bookshelves bookshelves);

    /**
     * 移除书籍从书架
     * @param bookshelves 书架信息
     * @return 是否移除成功
     */
    ServiceResult<Boolean> removeBookFromBookshelves(Bookshelves bookshelves);

    /**
     * 根据用户ID获取所有书架书籍
     * @param userId 用户ID
     * @return 书架书籍列表
     */
    ServiceResult<List<Bookshelves>> getBookshelvesByUserId(Long userId);

    /**
     * 创建用户书架
     * @param userId 用户ID
     * @return 是否创建成功
     */
    ServiceResult<Boolean> createBookshelves(Long userId, String name);

    /**
     * 获取用户书架名称
     * @param userId 用户ID
     * @return 书架名称列表
     */
    ServiceResult<List<Bookshelves>> getBookshelvesName(Long userId);

    /**
     * 根据书架获取对应书架书籍
     * @param userId 用户ID
     * @param id 书架id
     * @return 书架书籍列表
     */
    ServiceResult<List<Bookshelves>> getBookshelvesByName(Long userId, Long id);

    /**
     * 更新书架书籍信息(名字和简介）
     * @param bookshelves 书架信息
     * @return 是否更新成功
     */
    ServiceResult<Boolean> updateBookshelves(Bookshelves bookshelves);

    /**
     * 删除书架
     * @param bookshelvesId 书架ID
     * @return 是否删除成功
     */
    ServiceResult<Boolean> deleteBookshelves(Long bookshelvesId);

    /**
     * 将书籍移动到书架
     * @param booksId 书籍ID列表
     * @param bookshelvesId 目标书架ID
     * @return 是否移除成功
     */
    ServiceResult<Boolean> moveBookToBookshelves(Long booksId, Long bookshelvesId, Long userId);
}
