package com.example.freefiction.service.sys;

import com.example.freefiction.entity.Likes;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

/**
* @author Tjianwei
* @description 针对表【likes(点赞表)】的数据库操作Service
* @createDate 2025-11-24 10:48:57
*/
@Service
public interface LikesService extends IService<Likes> {
    /**
     * 添加点赞
     * @param likes 点赞
     * @return 是否成功
     */
    ServiceResult<Boolean> addLike(Likes likes);

    /**
     * 删除点赞
     * @param userId 用户id
     *               评论id
     * @return 是否成功
     */
    ServiceResult<Boolean> removeLike(Long userId,Long targetId );

    /**
     * 判断用户是否点赞
     * @param userId 用户id
     *               评论id
     *                类型
     * @return 是否成功
     */
    ServiceResult<Boolean> existLike(Long userId,Long type,Long id);

    /**
     * 获取用户点赞数量
     * @param userId 用户id
     * @return 数量
     */
    ServiceResult<Long> getLikeCount(Long userId);

    /**
     * 获取对应评论类型点赞数量
     * @param type 类型
     * @param id id
     * @return 数量
     */
    ServiceResult<Long> getLikeCountByTypeAndID(Long type,Long id);
}
