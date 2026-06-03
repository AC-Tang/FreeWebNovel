package com.example.freefiction.service.sys;

import com.example.freefiction.entity.Comments;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【comments(评论表)】的数据库操作Service
* @createDate 2025-11-24 10:49:04
*/
@Service
public interface CommentsService extends IService<Comments> {
    /**
     * 获取小说的书评列表
     * @param novelId 小说ID
     * @return 评论列表
     */
     ServiceResult<List<Comments>> getBookCommentsList(Long novelId);

     /**
     * 获取小说的章节书评列表
     * @param novelId 小说ID
     * @param chapterId 章节ID
     * @return 评论列表
     */
     ServiceResult<List<Comments>> getChapterCommentsList(Long novelId, Long chapterId);

     /**
     * 获取小说的章节小节书评列表
     * @param novelId 小说ID
     * @param chapterId 章节ID
      *                  段落数
     * @return 评论列表
     */
     ServiceResult<List<List<Comments>>> getSectionCommentsList(Long novelId, Long chapterId,Integer paragraphs);

     /**
     * 发表评论
     * @param comments 评论实体
     * @return 评论实体
     */
     ServiceResult<Comments> publishComment(Comments comments);

     /**
     * 获取所有评论
     * @param userId 用户ID
     * @return 评论实体
     */
     ServiceResult<List<Comments>> getAllByUserId(Long userId);

     /**
     * 获取用户所有评论
     * @param userId 用户ID
     * @param  type 评论类型
     * @return 评论实体
     */
     ServiceResult<List<Comments>> getByUserIdAndType(Long userId, Integer type);

     /**
     * 获取小说最热评论
     * @param bookId 小说ID
     * @return 评论实体
     */
     ServiceResult<Comments> getHottestCommentByBookId(Long bookId);

     /**
     * 获取整个评论及其附属评论
     * @param id 评论ID
     * @return 评论实体
     */
     ServiceResult<List<Comments>> getCommentsById(Long id);

     /**
     * 删除评论
     * @param id 评论ID
     * @return 删除结果
     */
     ServiceResult<Boolean> deleteComment(Long id);
}
