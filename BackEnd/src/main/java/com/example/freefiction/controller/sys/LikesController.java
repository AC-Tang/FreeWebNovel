package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.Likes;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.LikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/like")
@RequiredArgsConstructor
public class LikesController {
    private final LikesService likesService;

    /**
     * 添加点赞
     * @param likes 点赞对象
     * @return 点赞结果
     */
    @PostMapping("/add")
    public Result<Boolean> addLike(@RequestBody Likes likes) {
        ServiceResult<Boolean> serviceResult = likesService.addLike(likes);
        return serviceResult.success() ?
                Result.success("添加成功",serviceResult.data()):
                Result.failed(500,"添加失败");
    }

    /**
     * 删除点赞
     * @param userId 用户id
     *               评论id
     * @return 删除结果
     */
    @DeleteMapping("/remove")
    public Result<Boolean> removeLike(@RequestParam Long userId, @RequestParam Long targetId) {
        ServiceResult<Boolean> serviceResult = likesService.removeLike(userId, targetId);
        return serviceResult.success() ?
                Result.success("删除成功",serviceResult.data()):
                Result.failed(500,"删除失败");
    }

    /**
     * 查询点赞是否存在
     * @param userId 用户id
     * @param typeId 类型id
     * @param id 用户id
     * @return 是否存在
     */
    @GetMapping("/exist")
    public Result<Boolean> exist(@RequestParam Long userId, @RequestParam Long typeId, @RequestParam Long id) {
        ServiceResult<Boolean> serviceResult = likesService.existLike(userId, typeId, id);
        return serviceResult.success() ?
                Result.success("查询成功",serviceResult.data()):
                Result.failed(500,"查询失败");
    }

    /**
     * 获取用户点赞数量
     * @param userId 用户id
     * @return 数量
     */
    @GetMapping("/count/uer")
    public Result<Long> getLike(@RequestParam Long userId) {
        ServiceResult<Long> serviceResult = likesService.getLikeCount(userId);
        return serviceResult.success() ?
                Result.success("查询成功",serviceResult.data()):
                Result.failed(500,"查询失败");
    }

    /**
     * 获取书籍点赞数量
     * @param typeId 类型id
     * @param id 书籍id
     * @return 数量
     */
    @GetMapping("/count")
    public Result<Long> getLikeCount(@RequestParam Long typeId, @RequestParam Long id) {
        ServiceResult<Long> serviceResult = likesService.getLikeCountByTypeAndID(typeId, id);
        return serviceResult.success() ?
                Result.success("查询成功",serviceResult.data()):
                Result.failed(500,"查询失败");
    }
}
