package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.*;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.*;
import com.example.freefiction.mapper.CommentsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
* @author Tjianwei
* @description 针对表【comments(评论表)】的数据库操作Service实现
* @createDate 2025-11-24 10:49:04
*/
@Service
@RequiredArgsConstructor
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments>
    implements CommentsService{
    private final BooksService booksService;
    private final RatingsService ratingsService;
    private final UsersService usersService;
    private final ChaptersService chaptersService;
    private final NotificationsService notificationsService;

    /**
     * 获取小说的书评列表
     * @param novelId 小说ID
     * @return 评论列表
     */
    @Override
    public ServiceResult<List<Comments>> getBookCommentsList(Long novelId){
        List<Comments> comments = this.list(new QueryWrapper<Comments>()
                .eq("book_id", novelId)
                .eq("comment_type", 3)
                .eq("status", 1)
                .isNull("parent_id")
                .orderByDesc("created_at")
        );
        if(comments == null) {
            return ServiceResult.fail("没有评论");
        }
        comments.forEach(comment -> {
            comment.setUsername(usersService.getOne(new QueryWrapper<Users>().eq("id", comment.getUserId())).getUsername());
            comment.setAvatar(usersService.getOne(new QueryWrapper<Users>().eq("id", comment.getUserId())).getAvatar());
            comment.setRating(ratingsService.getOne(new QueryWrapper<Ratings>().eq("user_id", comment.getUserId())).getScore());
        });
        return ServiceResult.ok(comments);
    }

    /**
     * 获取小说的章节书评列表
     * @param novelId 小说ID
     * @param chapterId 章节ID
     * @return 评论列表
     */
    @Override
    public ServiceResult<List<Comments>> getChapterCommentsList(Long novelId, Long chapterId){
        List<Comments> comments = this.list(new QueryWrapper<Comments>()
                .eq("book_id", novelId)
                .eq("chapter_id", getRealChapterId(novelId, chapterId))
                .eq("comment_type", 1)
                .eq("status", 1)
                .isNull("parent_id")
                .orderByDesc("created_at")
        );
        if(comments == null) {
            return ServiceResult.fail("没有评论");
        }
        comments.forEach(comment -> {
            comment.setUsername(usersService.getOne(new QueryWrapper<Users>().eq("id", comment.getUserId())).getUsername());
            comment.setAvatar(usersService.getOne(new QueryWrapper<Users>().eq("id", comment.getUserId())).getAvatar());
        });
        return ServiceResult.ok(comments);
    }

    /**
     * 获取小说的章节小节书评列表
     * @param novelId 小说ID
     * @param chapterId 章节ID
     * @return 评论列表
     */
    @Override
    public ServiceResult<List<List<Comments>>> getSectionCommentsList(Long novelId, Long chapterId,Integer paragraphs){
        List<List<Comments>> map = new ArrayList<>();
        for(int i = 0; i < paragraphs; i++){
            List<Comments> comments = this.list(new QueryWrapper<Comments>()
                    .eq("book_id", novelId)
                    .eq("chapter_id", getRealChapterId(novelId, chapterId))
                    .eq("comment_type", 2)
                    .eq("status", 1)
                    .eq("paragraph_index", i)
                    .isNull("parent_id")
                    .orderByDesc("created_at")
            );
            if(comments != null) {
                comments.forEach(comment -> {
                    comment.setUsername(usersService.getOne(new QueryWrapper<Users>().eq("id", comment.getUserId())).getUsername());
                    comment.setAvatar(usersService.getOne(new QueryWrapper<Users>().eq("id", comment.getUserId())).getAvatar());
                });
            }
            map.add( comments);
        }
        return ServiceResult.ok(map);
    }

    /**
     * 发表评论
     * @param comments 评论
     * @return 评论
     */
    @Override
    public ServiceResult<Comments> publishComment(Comments comments){
        if(comments.getChapterId()!= null){
            comments.setChapterId(chaptersService.getOne(new QueryWrapper<Chapters>().eq("book_id", comments.getBookId()).eq("sort_order", comments.getChapterId())).getId());
        }
        boolean result = this.saveOrUpdate(comments);
        if(!result){
            return ServiceResult.fail("发布失败");
        }
        if(comments.getTopParentId()!=null){
            result=this.update(new UpdateWrapper<Comments>().setSql("reply_count=reply_count+1").eq("id", comments.getTopParentId()));
            if(!result){
                return ServiceResult.fail("发布失败");
            }
            if(!Objects.equals(comments.getUserId(), this.getById(comments.getParentId()).getUserId())){
                Notifications notifications = new Notifications();
                notifications.setUserId(this.getById(comments.getParentId()).getUserId());
                notifications.setType(2);
                notifications.setTitle("点赞通知");
                notifications.setContent("用户"+usersService.getById(comments.getUserId()).getUsername()+"回复了你的评论");
                notifications.setRelatedId(comments.getId());
                notifications.setRelatedType("comment");
                notificationsService.save( notifications);
            }
        }

        return ServiceResult.ok(comments);
    }

    /**
     * 获取用户所有评论
     * @param userId 用户ID
     * @return 评论列表
     */
    @Override
    public ServiceResult<List<Comments>> getAllByUserId(Long userId){
        List<Comments> comments= this.list(new QueryWrapper<Comments>().eq("user_id", userId).orderByAsc("id"));
        for (Comments comment : comments){
            comment.setBookName(booksService.getById(comment.getBookId()).getTitle());
            if(comment.getCommentType()!=3&&comment.getTopParentId()!=null)
                comment.setChapterId(this.getById(comment.getTopParentId()).getChapterId());
            if(comment.getChapterId() != null) {
                comment.setChapterName(chaptersService.getById(comment.getChapterId()).getTitle());
                comment.setRealChapterId(Long.valueOf(chaptersService.getById(comment.getChapterId()).getSortOrder()));
            }
        }
        return ServiceResult.ok(comments);
    }

    /**
     * 获取用户指定类型的所有评论
     * @param userId 用户ID
     * @param type 类型
     * @return 评论列表
     */
    @Override
    public ServiceResult<List<Comments>> getByUserIdAndType(Long userId, Integer type){
        List<Comments> comments= this.list(new QueryWrapper<Comments>().eq("user_id", userId).eq("comment_type", type));
        if(comments == null)
            return ServiceResult.fail("没有评论");
        return ServiceResult.ok(comments);
    }

    /**
     * 获取小说最热评论
     * @param bookId 小说ID
     * @return 评论实体
     */
    @Override
    public ServiceResult<Comments> getHottestCommentByBookId(Long bookId){
        Comments comments = this.getOne(new QueryWrapper<Comments>()
                .eq("book_id", bookId)
                .eq("comment_type", 3)
                .isNull("parent_id")
                .orderByDesc("like_count")
                .orderByDesc("reply_count")
                .last("LIMIT 1")
        );
        if(comments!=null) {
            comments.setCount((int) this.count(new QueryWrapper<Comments>().eq("book_id", bookId).eq("comment_type", 3)));
            comments.setRealCount((int) this.count(new QueryWrapper<Comments>().eq("book_id", bookId).eq("comment_type", 3).isNull("parent_id")));
            comments.setAvatar(usersService.getOne(new QueryWrapper<Users>().eq("id", comments.getUserId())).getAvatar());
            comments.setRating(ratingsService.getOne(new QueryWrapper<Ratings>().eq("user_id", comments.getUserId())).getScore());
            comments.setUsername(usersService.getOne(new QueryWrapper<Users>().eq("id", comments.getUserId())).getUsername());
        }
        return ServiceResult.ok(comments);
    }

    /**
     * 获取整个评论及其附属评论
     * @param id 评论ID
     * @return 评论实体
     */
    @Override
    public ServiceResult<List<Comments>> getCommentsById(Long id){
        List<Comments> comments = new ArrayList<>();
        comments.add(this.getOne(new QueryWrapper<Comments>().eq("id", id)));
        comments.get(0).setRating(ratingsService.getOne(new QueryWrapper<Ratings>().eq("user_id", comments.get(0).getUserId())).getScore());
        comments.addAll(this.list(new QueryWrapper<Comments>().eq("top_parent_id", id)));
        comments.forEach(comment -> {
            comment.setUsername(usersService.getOne(new QueryWrapper<Users>().eq("id", comment.getUserId())).getUsername());
            comment.setAvatar(usersService.getOne(new QueryWrapper<Users>().eq("id", comment.getUserId())).getAvatar());
        });
        return ServiceResult.ok(comments);
    }

    /**
     * 删除评论
     * @param id 评论ID
     * @return 删除结果
     */
    @Override
    public ServiceResult<Boolean> deleteComment(Long id){
        return this.removeById(id)?ServiceResult.ok(true):ServiceResult.fail("删除失败");
    }

    int getRealChapterId(Long bookId, Long chapterId){
        return Math.toIntExact(chaptersService.getOne(new QueryWrapper<Chapters>().eq("book_id", bookId).eq("sort_order", chapterId)).getId());
    }
}




