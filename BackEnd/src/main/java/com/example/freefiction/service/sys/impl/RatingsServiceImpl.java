package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Books;
import com.example.freefiction.entity.Ratings;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.BooksService;
import com.example.freefiction.service.sys.RatingsService;
import com.example.freefiction.mapper.RatingsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author Tjianwei
* @description 针对表【ratings(评分表)】的数据库操作Service实现
* @createDate 2025-11-24 10:48:49
*/
@Service
@RequiredArgsConstructor
public class RatingsServiceImpl extends ServiceImpl<RatingsMapper, Ratings>
    implements RatingsService{
    private final BooksService booksService;
    /**
     * 获取用户对书籍的评分
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 评分对象
     */
    @Override
    public ServiceResult<Ratings> getRatingByUserIdAndBookId(Long userId, Long bookId){
        Ratings rating = this.getOne(new QueryWrapper<Ratings>().eq("user_id", userId).eq("book_id", bookId));
        return rating == null ? ServiceResult.fail("未找到该用户对该书籍的评分") : ServiceResult.ok(rating);
    }

    /**
     * 添加或修改评分
     * @param ratings 评分对象
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> addOrUpdateRating(Ratings ratings){
        boolean result = this.saveOrUpdate(ratings);
        if(!result)
            return ServiceResult.fail("修改评分失败");
        boolean update = updateBookRating(ratings.getBookId());
        return update ? ServiceResult.ok(true) : ServiceResult.fail("修改书籍评分失败");
    }


    public Boolean updateBookRating(Long bookId){
//        QueryWrapper<Ratings> queryWrapper = new QueryWrapper<>();
//        queryWrapper.select("ROUND(AVG(score), 1) AS rating_avg");
//        double ratingAvg = this.baseMapper.selectOne(queryWrapper).getScore();
//        return booksService.update(new UpdateWrapper<Books>()
//                .eq("id", bookId)
//                .set("rating_avg", ratingAvg)
//                .set("rating_count", this.baseMapper.selectCount(new QueryWrapper<Ratings>().eq("book_id", bookId)))
//        );
        QueryWrapper<Ratings> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("ROUND(AVG(score), 1) AS rating_avg");
        Ratings ratings = this.baseMapper.selectOne(queryWrapper);

        double ratingAvg = 0.0;
        if (ratings != null) {
            ratingAvg = ratings.getScore();
        }

        int ratingCount = Math.toIntExact(this.baseMapper.selectCount(new QueryWrapper<Ratings>().eq("book_id", bookId)));

        return booksService.update(new UpdateWrapper<Books>()
                .eq("book_id", bookId)
                .set("rating_avg", ratingAvg)
                .set("rating_count", ratingCount)
        );
    }
}




