package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Books;
import com.example.freefiction.entity.Chapters;
import com.example.freefiction.handler.CacheTimeStrategy;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.mapper.ChaptersMapper;
import com.example.freefiction.service.RedisService;
import com.example.freefiction.service.sys.*;
import com.example.freefiction.mapper.BooksMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Tjianwei
* @description 针对表【books(书籍表)】的数据库操作Service实现
* @createDate 2025-11-24 10:49:16
*/
@Service
@RequiredArgsConstructor
public class BooksServiceImpl extends ServiceImpl<BooksMapper, Books>
        implements BooksService {
    private final ChaptersService chaptersService;
    private final ChaptersMapper chaptersMapper;
    private final BooksMapper booksMapper;
    private final CategoriesService categoriesService;
    private final RedisService redisService;

    private final static String KeyIP = "IP:";

    /**
     * 更新小说统计信息（章节数和总字数）
     *
     * @param novelId 小说ID
     * @return 更新后的小说实体
     */
    @Override
    public Books updateNovelStats(Long novelId) {
        // 获取小说实体
        Books booksEntity = this.getById(novelId);
        if (booksEntity == null) {
            return null;
        }

        // 获取章节数
        int chapterCount = getNovelChapterCount(novelId);

        // 获取总字数
        int totalWords = getNovelTotalWords(novelId);
        // 只改，其它不动
        booksMapper.update(
                null,                                      // 实体：null 表示不携带任何字段
                new LambdaUpdateWrapper<Books>()
                        .eq(Books::getBookId, novelId)
                        .set(Books::getChapterCount, chapterCount)
                        .set(Books::getWordCount, totalWords)            // 要改的值
        );

        return booksEntity;
    }

    /**
     * 获取小说的章节数
     *
     * @param novelId 小说ID
     * @return 章节数
     */
    @Override
    public int getNovelChapterCount(Long novelId) {
        return (int) chaptersService.count(new QueryWrapper<Chapters>().eq("book_id", novelId));
    }

    /**
     * 获取小说的总字数
     *
     * @param novelId 小说ID
     * @return 总字数
     */
    @Override
    public int getNovelTotalWords(Long novelId) {
        // 查询该小说的所有章节
        // 1. 查一条记录，列别名 total
        Map<String, Object> row = chaptersMapper.selectMaps(
                new QueryWrapper<Chapters>()
                        .select("sum(word_count) as total")
                        .eq("book_id", novelId)
        ).get(0);

// 2. 转成 int
        return ((Number) row.get("total")).intValue();   // null 自己兜底
    }

    @Override
    public ServiceResult<Books> getBooksById(Long novelId) {
        String key = "Books" + novelId;
        Books cacheBooks = redisService.get(key, Books.class);
        if (cacheBooks == null) {
            Books books = this.getOne(new QueryWrapper<Books>().eq("book_id", novelId));
            if (books == null) {
                return ServiceResult.fail("小说不存在");
            }
            books.setCategoryName(setCategory(books.getCategoryId()));
            books.setStatusName(setStatusName(books.getStatus()));
            redisService.set(key, books, CacheTimeStrategy.NOVEL_INFO);
            return ServiceResult.ok(books);
        }
        return ServiceResult.ok(cacheBooks);
    }

    @Override
    public ServiceResult<List<Books>> getAll() {
        String key = "Books:All";
        List<Books> cacheBooks = (List<Books>) redisService.get(key, List.class);
        if (cacheBooks == null) {
            List<Books> books = this.list();
            if (books == null) {
                return ServiceResult.fail("没有小说");
            }
            for (Books book : books) {
                book.setCategoryName(setCategory(book.getCategoryId()));
                book.setStatusName(setStatusName(book.getStatus()));
            }
            redisService.set(key, books, CacheTimeStrategy.NOVEL_INFO);
            return ServiceResult.ok(books);
        }
        return ServiceResult.ok(cacheBooks);
    }

    @Override
    public ServiceResult<List<Books>> getBooksByStatus(int status) {
        String key = "Books:Status:" + status;
        List<Books> cacheBooks = (List<Books>) redisService.get(key, List.class);
        if (cacheBooks == null) {
            List<Books> books = this.list(new QueryWrapper<Books>().eq("status", status));
            if (books == null) {
                return ServiceResult.fail("没有小说");
            }
            for (Books book : books) {
                book.setCategoryName(setCategory(book.getCategoryId()));
                book.setStatusName(setStatusName(book.getStatus()));
            }
            redisService.set(key, books, CacheTimeStrategy.NOVEL_INFO);
            return ServiceResult.ok(books);
        }
        return ServiceResult.ok(cacheBooks);
    }

    /**
     * 修改小说信息
     * @param books 小说信息
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> updateBooks(Books books){
        return this.update(books, new QueryWrapper<Books>().eq("book_id", books.getBookId()))?ServiceResult.ok(true):ServiceResult.fail("修改失败");
    }

    /**
     * 删除小说
     * @param novelId 小说ID
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> deleteBooks(Long novelId){
        return this.remove(new QueryWrapper<Books>().eq("book_id", novelId))?ServiceResult.ok(true):ServiceResult.fail("删除失败");
    }

    /**
     * 批量修改小说状态
     * @param novelIds 小说ID列表
     * @param status 状态
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> batchUpdateStatus(List<Long> novelIds, int status){
        return this.update(new UpdateWrapper<Books>().in("book_id", novelIds).set("status", status))?ServiceResult.ok(true):ServiceResult.fail("批量修改失败");
    }

    /**
     * 批量删除小说
     * @param novelIds 小说ID列表
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> batchDelete(List<Long> novelIds){
        return this.remove(new QueryWrapper<Books>().in("book_id", novelIds))?ServiceResult.ok(true):ServiceResult.fail("批量删除失败");
    }

    /**
     * 添加小说
     * @param books 小说信息
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> addBooks(Books books){
        return this.save(books)?ServiceResult.ok(true):ServiceResult.fail("添加失败");
    }

    @Override
    public ServiceResult<List<Books>> getBooksByCategory(Integer categoryId) {
        String key = "Books:Category:" + categoryId;
        List<Books> cacheBooks = (List<Books>) redisService.get(key, List.class);
        if (cacheBooks == null) {
            List<Books> books = this.list(new QueryWrapper<Books>().eq("category_id", categoryId));
            if (books == null) {
                return ServiceResult.fail("没有小说");
            }
            for (Books book : books) {
                book.setCategoryName(setCategory(book.getCategoryId()));
                book.setStatusName(setStatusName(book.getStatus()));
            }
            redisService.set(key, books, CacheTimeStrategy.NOVEL_INFO);
            return ServiceResult.ok(books);
        }
        return ServiceResult.ok(cacheBooks);
    }

    @Override
    public ServiceResult<Boolean> addViewCount(Long novelId) {
        boolean result = this.update(null, new UpdateWrapper<Books>().eq("book_id", novelId).setSql("view_count = view_count + 1"));
        if (!result) {
            return ServiceResult.fail("更新点击量失败");
        }
        return ServiceResult.ok(true);
    }

    private String setCategory(Integer categoryId) {
        return categoriesService.getById(categoryId).getName();
    }

    private String setStatusName(Integer status) {
        return switch (status) {
            case 0 -> "下架";
            case 1 -> "连载中";
            case 2 -> "已完结";
            default -> "未知";
        };
    }

    /**
     * 获取新书榜
     *
     * @return 小说列表
     */
    @Override
    public ServiceResult<List<Books>> getNewRankBooks() {
        List<Books> cacheBooks = (List<Books>) redisService.get("NewRank", Books.class);
        if (cacheBooks == null) {
            List<Books> books = this.getAll().data();
            if (books == null) {
                return ServiceResult.fail("没有小说");
            }
            for (Books book : books) {
                book.setCategoryName(setCategory(book.getCategoryId()));
                book.setStatusName(setStatusName(book.getStatus()));
            }
            // 新书榜三级排序规则：
            // 1. 按 publish_date 降序（最新发布的排前）
            // 2. 按 last_chapter_time 降序（最近有更新的排前）
            // 3. 按 book_id 降序（后入库的排前）
            books.sort((b1, b2) -> {
                // 第一级：publish_date 降序
                int compare = b2.getPublishDate().compareTo(b1.getPublishDate());
                if (compare != 0) {
                    return compare;
                }

                // 第二级：last_chapter_time 降序（处理null值）
                if (b1.getLastChapterTime() == null && b2.getLastChapterTime() == null) {
                    compare = 0;
                } else if (b1.getLastChapterTime() == null) {
                    return 1; // null 值排后面
                } else if (b2.getLastChapterTime() == null) {
                    return -1; // 非null 排前面
                } else {
                    compare = b2.getLastChapterTime().compareTo(b1.getLastChapterTime());
                }
                if (compare != 0) {
                    return compare;
                }

                // 第三级：book_id 降序
                return b2.getBookId().compareTo(b1.getBookId());
            });
            redisService.set("NewRank", books, CacheTimeStrategy.NOVEL_INFO);
        }
        return ServiceResult.ok(cacheBooks);
    }

    /**
     * 获取点击榜
     *
     * @return 小说列表
     */
    @Override
    public ServiceResult<List<Books>> getCountRankBooks() {
        List<Books> cacheBooks = (List<Books>) redisService.get("HotRank", Books.class);
        if (cacheBooks == null) {
            List<Books> books = this.getAll().data();
            if (books == null || books.isEmpty()) {
                return ServiceResult.fail("没有小说");
            }

            for (Books book : books) {
                book.setCategoryName(setCategory(book.getCategoryId()));
                book.setStatusName(setStatusName(book.getStatus()));
            }

            // 点击榜三级排序规则：
            // 1. 按 view_count 降序（点击量最高的排前）
            // 2. 按 recommend_count 降序（推荐数多的排前）
            // 3. 按 last_chapter_time 降序（最近有更新的排前）
            books.sort((b1, b2) -> {
                // 第一级：view_count 降序
                int compare = Integer.compare(b2.getViewCount(), b1.getViewCount());
                if (compare != 0) return compare;

                // 第二级：recommend_count 降序
                compare = Integer.compare(b2.getRecommendCount(), b1.getRecommendCount());
                if (compare != 0) return compare;

                // 第三级：last_chapter_time 降序（处理null值）
                if (b1.getLastChapterTime() == null && b2.getLastChapterTime() == null) return 0;
                if (b1.getLastChapterTime() == null) return 1;
                if (b2.getLastChapterTime() == null) return -1;
                return b2.getLastChapterTime().compareTo(b1.getLastChapterTime());
            });

            int limit = Math.min(books.size(), 50);
            List<Books> rankedBooks = new ArrayList<>(books.subList(0, limit));

            redisService.set("HotRank", rankedBooks, CacheTimeStrategy.NOVEL_INFO);
            return ServiceResult.ok(rankedBooks);
        }
        return ServiceResult.ok(cacheBooks);
    }

    /**
     * 获取人气榜
     *
     * @return 小说列表
     */
// 先添加一个计算热度值的私有方法
    private double calculateHotValue(Books book) {
        // 热度值公式：(view_count * 0.5) + (recommend_count * 0.3) + (rating_avg * log(1 + rating_count) * 0.2)
        double viewScore = book.getViewCount() * 0.5;
        double recommendScore = book.getRecommendCount() * 0.3;
        double ratingScore = 0;

        if (book.getRatingCount() > 0) {
            ratingScore = book.getRatingAvg() * Math.log(1 + book.getRatingCount()) * 0.2;
        }

        return viewScore + recommendScore + ratingScore;
    }

    @Override
    public ServiceResult<List<Books>> getPopularRankBooks() {
        List<Books> cacheBooks = (List<Books>) redisService.get("PopularRank", Books.class);
        if (cacheBooks == null) {
            List<Books> books = this.getAll().data();
            if (books == null || books.isEmpty()) {
                return ServiceResult.fail("没有小说");
            }

            for (Books book : books) {
                book.setCategoryName(setCategory(book.getCategoryId()));
                book.setStatusName(setStatusName(book.getStatus()));
            }

            // 为每本书计算热度值
            Map<Long, Double> hotValueMap = new HashMap<>();
            for (Books book : books) {
                hotValueMap.put(book.getBookId(), calculateHotValue(book));
            }

            // 人气榜三级排序规则：
            // 1. 按热度值降序（综合热度最高的排前）
            // 2. 按 view_count 降序（点击量高的排前）
            // 3. 按 last_chapter_time 降序（最近有更新的排前）
            books.sort((b1, b2) -> {
                // 第一级：热度值降序
                double hot1 = hotValueMap.get(b1.getBookId());
                double hot2 = hotValueMap.get(b2.getBookId());
                int compare = Double.compare(hot2, hot1);
                if (compare != 0) return compare;

                // 第二级：view_count 降序
                compare = Integer.compare(b2.getViewCount(), b1.getViewCount());
                if (compare != 0) return compare;

                // 第三级：last_chapter_time 降序（处理null值）
                if (b1.getLastChapterTime() == null && b2.getLastChapterTime() == null) return 0;
                if (b1.getLastChapterTime() == null) return 1;
                if (b2.getLastChapterTime() == null) return -1;
                return b2.getLastChapterTime().compareTo(b1.getLastChapterTime());
            });

            int limit = Math.min(books.size(), 50);
            List<Books> rankedBooks = new ArrayList<>(books.subList(0, limit));

            redisService.set("PopularRank", rankedBooks, CacheTimeStrategy.NOVEL_INFO);
            return ServiceResult.ok(rankedBooks);
        }
        return ServiceResult.ok(cacheBooks);
    }

    /**
     * 获取收藏榜
     *
     * @return 小说列表
     */
    @Override
    public ServiceResult<List<Books>> getLikeRankBooks() {
        List<Books> cacheBooks = (List<Books>) redisService.get("FavoriteRank", Books.class);
        if (cacheBooks == null) {
            List<Books> books = this.getAll().data();
            if (books == null || books.isEmpty()) {
                return ServiceResult.fail("没有小说");
            }

            for (Books book : books) {
                book.setCategoryName(setCategory(book.getCategoryId()));
                book.setStatusName(setStatusName(book.getStatus()));
            }

            // 收藏榜三级排序规则（假设已添加 favorite_count 字段）：
            // 1. 按 favorite_count 降序（收藏数最高的排前）
            // 2. 按 rating_avg 降序（评分更高的排前）
            // 3. 按 last_chapter_time 降序（最近有更新的排前）
            books.sort((b1, b2) -> {
                // 第一级：favorite_count 降序
                int compare = Integer.compare(Math.toIntExact(b2.getLikeCount()), Math.toIntExact(b1.getLikeCount()));
                if (compare != 0) return compare;

                // 第二级：rating_avg 降序
                compare = Double.compare(b2.getRatingAvg(), b1.getRatingAvg());
                if (compare != 0) return compare;

                // 第三级：last_chapter_time 降序（处理null值）
                if (b1.getLastChapterTime() == null && b2.getLastChapterTime() == null) return 0;
                if (b1.getLastChapterTime() == null) return 1;
                if (b2.getLastChapterTime() == null) return -1;
                return b2.getLastChapterTime().compareTo(b1.getLastChapterTime());
            });

            int limit = Math.min(books.size(), 50);
            List<Books> rankedBooks = new ArrayList<>(books.subList(0, limit));

            redisService.set("FavoriteRank", rankedBooks, CacheTimeStrategy.NOVEL_INFO);
            return ServiceResult.ok(rankedBooks);
        }
        return ServiceResult.ok(cacheBooks);
    }

    /**
     * 获取推荐榜
     *
     * @return 小说列表
     */
    @Override
    public ServiceResult<List<Books>> getRecommendRankBooks() {
        List<Books> cacheBooks = (List<Books>) redisService.get("RecommendRank", Books.class);
        if (cacheBooks == null) {
            List<Books> books = this.getAll().data();
            if (books == null || books.isEmpty()) {
                return ServiceResult.fail("没有小说");
            }

            for (Books book : books) {
                book.setCategoryName(setCategory(book.getCategoryId()));
                book.setStatusName(setStatusName(book.getStatus()));
            }

            // 推荐榜三级排序规则：
            // 1. 按 recommend_count 降序（推荐数最高的排前）
            // 2. 按 rating_avg 降序（评分更高的排前）
            // 3. 按 view_count 降序（点击量更高的排前）
            books.sort((b1, b2) -> {
                // 第一级：recommend_count 降序
                int compare = Integer.compare(b2.getRecommendCount(), b1.getRecommendCount());
                if (compare != 0) return compare;

                // 第二级：rating_avg 降序
                compare = Double.compare(b2.getRatingAvg(), b1.getRatingAvg());
                if (compare != 0) return compare;

                // 第三级：view_count 降序
                return Integer.compare(b2.getViewCount(), b1.getViewCount());
            });

            int limit = Math.min(books.size(), 50);
            List<Books> rankedBooks = new ArrayList<>(books.subList(0, limit));

            redisService.set("RecommendRank", rankedBooks, CacheTimeStrategy.NOVEL_INFO);
            return ServiceResult.ok(rankedBooks);
        }
        return ServiceResult.ok(cacheBooks);
    }

    /**
     * 获取完结榜
     *
     * @return 小说列表
     */
    @Override
    public ServiceResult<List<Books>> getCompletedRankBooks() {
        List<Books> cacheBooks = (List<Books>) redisService.get("CompletedRank", Books.class);
        if (cacheBooks == null) {
            List<Books> books = this.getAll().data();
            if (books == null || books.isEmpty()) {
                return ServiceResult.fail("没有小说");
            }

            // 首先筛选出已完结的书籍（status = 2）
            List<Books> completedBooks = books.stream()
                    .filter(book -> book.getStatus() == 2)
                    .collect(Collectors.toList());

            if (completedBooks.isEmpty()) {
                return ServiceResult.fail("没有完结小说");
            }

            for (Books book : completedBooks) {
                book.setCategoryName(setCategory(book.getCategoryId()));
                book.setStatusName(setStatusName(book.getStatus()));
            }

            // 完结榜三级排序规则：
            // 1. 按 word_count 降序（字数最多的排前）
            // 2. 按 rating_avg 降序（评分更高的排前）
            // 3. 按 rating_count 降序（评分人数更多的排前）
            completedBooks.sort((b1, b2) -> {
                // 第一级：word_count 降序
                int compare = Integer.compare(b2.getWordCount(), b1.getWordCount());
                if (compare != 0) return compare;

                // 第二级：rating_avg 降序
                compare = Double.compare(b2.getRatingAvg(), b1.getRatingAvg());
                if (compare != 0) return compare;

                // 第三级：rating_count 降序
                return Integer.compare(b2.getRatingCount(), b1.getRatingCount());
            });

            int limit = Math.min(completedBooks.size(), 50);
            List<Books> rankedBooks = completedBooks.subList(0, limit);

            redisService.set("CompletedRank", rankedBooks, CacheTimeStrategy.NOVEL_INFO);
            return ServiceResult.ok(rankedBooks);
        }
        return ServiceResult.ok(cacheBooks);
    }
}








