package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.Comments;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.CommentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentsController {
    private final CommentsService commentsService;
    /**
     * 获取书评详情
     * @param id 书评ID
     * @return 书评信息
     */
    @GetMapping("/fetch/detail")
    public Result<List<Comments>> fetchComment(@RequestParam Long id) {
        ServiceResult<List<Comments>> serviceResult = commentsService.getCommentsById(id);
        return serviceResult.success() ?
                Result.success("成功获取书评信息",serviceResult.data()) :
                Result.failed(404,"书评不存在");
    }

    /**
     * 获取小说书评列表
     * @param novelId 小说ID
     * @return 书评列表
     */
    @GetMapping("/fetch/novel/{novelId}")
    public Result<List<Comments>> fetchComments(@PathVariable Long novelId) {
        ServiceResult<List<Comments>> serviceResult = commentsService.getBookCommentsList(novelId);
        return serviceResult.success() ?
                Result.success("成功获取小说书评列表",serviceResult.data()) :
                Result.failed(404,"小说不存在或书评列表为空");
    }

    /**
     * 获取章节书评列表
     * @param novelId 小说ID
     * @param chapterId 章节ID
     * @return 书评列表
     */
    @GetMapping("/fetch/chapter")
    public Result<List<Comments>> fetchChapterComments(@RequestParam Long novelId, @RequestParam Long chapterId) {
        ServiceResult<List<Comments>> serviceResult = commentsService.getChapterCommentsList(novelId, chapterId);
        return serviceResult.success() ?
                Result.success("成功获取章节书评列表",serviceResult.data()) :
                Result.failed(404,"章节书评列表为空");
    }

    /**
     * 获取章节段评列表
     * @param novelId 小说ID
     * @param chapterId 章节ID
     * @return 书评列表
     */
    @GetMapping("/fetch/paragraph")
    public Result<List<List<Comments>>> fetchSectionComments(@RequestParam Long novelId, @RequestParam Long chapterId, @RequestParam Integer paragraphs) {
        ServiceResult<List<List<Comments>>> serviceResult = commentsService.getSectionCommentsList(novelId, chapterId, paragraphs);
        return serviceResult.success() ?
                Result.success("成功获取章节书评列表",serviceResult.data()) :
                Result.failed(404,"章节书评列表为空");
    }

    /**
     * 获取小说最热书评
     * @param novelId 小说ID
     * @return 书评信息
     */
    @GetMapping("/fetch/hot/novel/{novelId}")
    public Result<Comments> fetchHotComments(@PathVariable Long novelId) {
        ServiceResult<Comments> serviceResult = commentsService.getHottestCommentByBookId(novelId);
        return serviceResult.success() ?
                Result.success("成功获取小说最热书评",serviceResult.data()) :
                Result.failed(404,"小说不存在或书评为空");
    }

    /**
     * 获取用户所有书评
     * @param userId 用户ID
     * @return 书评列表
     */
    @GetMapping("/fetch/user/{userId}")
    public Result<List<Comments>> fetchCommentsById(@PathVariable Long userId) {
        ServiceResult<List<Comments>> serviceResult = commentsService.getAllByUserId(userId);
        return serviceResult.success() ?
                Result.success("成功获取书评列表",serviceResult.data()) :
                Result.failed(404,"书评列表为空");
    }

    /**
     * 获取用户指定类型的书评
     * @param userId 用户ID
     * @param type 书评类型
     * @return 书评列表
     */
    @GetMapping("/fetch")
    public Result<List<Comments>> fetchCommentsByIdAndType(@RequestParam Long userId, @RequestParam Integer type) {
        ServiceResult<List<Comments>> serviceResult = commentsService.getByUserIdAndType(userId, type);
        return serviceResult.success() ?
                Result.success("成功获取书评列表",serviceResult.data()) :
                Result.failed(404,"书评列表为空");
    }

    /**
     * 发布书评
     * @param comments 书评信息
     * @return 书评信息
     */
    @PostMapping("/publish")
    public Result<Comments> publishComment(@RequestBody Comments comments) {
        ServiceResult<Comments> serviceResult = commentsService.publishComment(comments);
        return serviceResult.success() ?
                Result.success("成功发布书评",serviceResult.data()) :
                Result.failed(404,"发布书评失败");
    }

    /**
     * 删除书评
     * @param id 书评ID
     * @return 是否删除成功
     */
    @DeleteMapping("/delete")
    public Result<Boolean> deleteComment(@RequestParam Long id) {
        ServiceResult<Boolean> serviceResult = commentsService.deleteComment(id);
        return serviceResult.success() ?
                Result.success("成功删除书评",serviceResult.data()) :
                Result.failed(404,"删除书评失败");
    }

    /**
     * 获取书评数量
     * @return 书评数量
     */
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success("成功获取书评数量",commentsService.count());
    }
}
