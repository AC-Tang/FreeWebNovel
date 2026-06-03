package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Likes;
import com.example.freefiction.entity.Notifications;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.CommentsService;
import com.example.freefiction.service.sys.LikesService;
import com.example.freefiction.mapper.LikesMapper;
import com.example.freefiction.service.sys.NotificationsService;
import com.example.freefiction.service.sys.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author Tjianwei
* @description 针对表【likes(点赞表)】的数据库操作Service实现
* @createDate 2025-11-24 10:48:57
*/
@Service
@RequiredArgsConstructor
public class LikesServiceImpl extends ServiceImpl<LikesMapper, Likes>
    implements LikesService{
    private final UsersService usersService;
    private final CommentsService commentsService;
    private final NotificationsService notificationsService;
    /**
     * 添加点赞
     * @param likes 点赞
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> addLike(Likes likes){
        if(!Objects.equals(likes.getUserId(), commentsService.getById(likes.getTargetId()).getUserId())){
            Notifications notifications = new Notifications();
            notifications.setUserId(commentsService.getById(likes.getTargetId()).getUserId());
            notifications.setType(2);
            notifications.setTitle("点赞通知");
            notifications.setContent("用户"+usersService.getById(likes.getUserId()).getUsername()+"赞了你的评论");
            notifications.setRelatedId(likes.getId());
            notifications.setRelatedType("likes");
            notificationsService.save( notifications);
        }
        return save(likes)?ServiceResult.ok(true):ServiceResult.fail("添加失败");
    }

    /**
     * 删除点赞
     * @param userId 用户id
     *               评论id
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> removeLike(Long userId,Long targetId){
        return remove(new QueryWrapper<Likes>().eq("user_id",userId).eq("target_id",targetId))?ServiceResult.ok(true):ServiceResult.fail("删除失败");
    }

    /**
     * 判断用户是否点赞
     * @param userId 用户id
     *               评论id
     *                类型
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> existLike(Long userId,Long type,Long id){
        return getOne(new QueryWrapper<Likes>().eq("user_id",userId).eq("target_id",id).eq("target_type",type))==null?
                ServiceResult.ok(false):ServiceResult.ok(true);
    }

    /**
     * 获取用户点赞数量
     * @param userId 用户id
     * @return 数量
     */
    @Override
    public ServiceResult<Long> getLikeCount(Long userId){
        return ServiceResult.ok(count(new QueryWrapper<Likes>().eq("user_id",userId)));
    }

    /**
     * 获取对应评论类型点赞数量
     * @param type 类型
     * @param id id
     * @return 数量
     */
    @Override
    public ServiceResult<Long> getLikeCountByTypeAndID(Long type,Long id){
        return ServiceResult.ok(count(new QueryWrapper<Likes>().eq("target_type",type).eq("target_id",id)));
    }
}




