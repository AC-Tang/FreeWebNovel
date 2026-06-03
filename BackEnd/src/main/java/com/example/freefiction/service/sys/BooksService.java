package com.example.freefiction.service.sys;

import com.example.freefiction.entity.Books;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【books(书籍表)】的数据库操作Service
* @createDate 2025-11-24 10:49:16
*/
@Service
public interface BooksService extends IService<Books> {
    /**
     * 更新小说统计信息（章节数和总字数）
     * @param novelId 小说ID
     * @return 更新后的小说实体
     */
    Books updateNovelStats(Long novelId);

    /**
     * 获取小说的章节数
     * @param novelId 小说ID
     * @return 章节数
     */
    int getNovelChapterCount(Long novelId);

    /**
     * 获取小说的总字数
     * @param novelId 小说ID
     * @return 总字数
     */
    int getNovelTotalWords(Long novelId);
    /**
     * 获取单个小说
     * @param novelId 小说ID
     * @return 小说信息
     */
    ServiceResult<Books> getBooksById(Long novelId);
    /**
     * 获取所有小说列表
     * @return 小说列表
     */
    ServiceResult<List<Books>> getAll();

    /**
     * 获取指定状态的小说列表
     * @param status 状态
     * @return 小说列表
     */
    ServiceResult<List<Books>> getBooksByStatus(int status);

    /**
     * 添加点击量
     *
     * @param novelId 小说ID
     * @return 是否成功
     */
    ServiceResult<Boolean> addViewCount(Long novelId);

    /**
     * 获取指定分类下的小说列表
     * @param categoryId 分类ID
     * @return 小说列表
     */
    ServiceResult<List<Books>> getBooksByCategory(Integer categoryId);

    /**
     * 获取新书榜
     * @return 小说列表
     */
    ServiceResult<List<Books>> getNewRankBooks();

    /**
     * 获取点击榜
     * @return 小说列表
     */
    ServiceResult<List<Books>> getCountRankBooks();

    /**
     * 获取人气榜
     * @return 小说列表
     */
    ServiceResult<List<Books>> getPopularRankBooks();

    /**
     * 获取收藏榜
     * @return 小说列表
     */
    ServiceResult<List<Books>> getLikeRankBooks();

    /**
     * 获取推荐榜
     * @return 小说列表
     */
    ServiceResult<List<Books>> getRecommendRankBooks();

    /**
     * 获取完结榜
     * @return 小说列表
     */
    ServiceResult<List<Books>> getCompletedRankBooks();

    /**
     * 修改小说信息
     * @param books 小说信息
     * @return 是否成功
     */
    ServiceResult<Boolean> updateBooks(Books books);

    /**
     * 删除小说
     * @param novelId 小说ID
     * @return 是否成功
     */
    ServiceResult<Boolean> deleteBooks(Long novelId);

    /**
     * 批量修改小说状态
     * @param novelIds 小说ID列表
     * @param status 状态
     * @return 是否成功
     */
    ServiceResult<Boolean> batchUpdateStatus(List<Long> novelIds, int status);

    /**
     * 批量删除小说
     * @param novelIds 小说ID列表
     * @return 是否成功
     */
    ServiceResult<Boolean> batchDelete(List<Long> novelIds);

    /**
     * 添加小说
     * @param books 小说信息
     * @return 是否成功
     */
    ServiceResult<Boolean> addBooks(Books books);
}
