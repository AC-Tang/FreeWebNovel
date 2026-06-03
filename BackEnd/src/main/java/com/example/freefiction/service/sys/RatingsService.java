package com.example.freefiction.service.sys;

import com.example.freefiction.entity.Ratings;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

/**
* @author Tjianwei
* @description 针对表【ratings(评分表)】的数据库操作Service
* @createDate 2025-11-24 10:48:49
*/
@Service
public interface RatingsService extends IService<Ratings> {
    /**
     * 获取用户对书籍的评分
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 评分对象
     */
    ServiceResult<Ratings> getRatingByUserIdAndBookId(Long userId, Long bookId);

    /**
     * 添加或更新评分
     * @param ratings 评分对象
     * @return 是否成功
     */
    ServiceResult<Boolean> addOrUpdateRating(Ratings ratings);
}
