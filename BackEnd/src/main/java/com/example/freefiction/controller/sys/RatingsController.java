package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.Ratings;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.RatingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingsController {
    private final RatingsService ratingsService;

    /**
     * 获取用户对书籍的评分
     * @param userId 用户ID
     * @param bookId 书籍ID
     * @return 评分对象
     */
    @GetMapping("/fetch")
    public Result<Ratings> getRatingByUserIdAndBookId(@RequestParam Long userId,@RequestParam Long bookId){
        ServiceResult<Ratings> result = ratingsService.getRatingByUserIdAndBookId(userId, bookId);
        return result.success() ?
                Result.success("获取用户评分成功",result.data()) :
                Result.failed(404,"未找到该用户对该书籍的评分");
    }

    /**
     * 添加或修改评分
     * @param ratings 评分对象
     * @return 是否成功
     */
    @PutMapping("/addOrUpdate")
    public Result<Boolean> addOrUpdateRating(@RequestBody Ratings ratings){
        ServiceResult<Boolean> result = ratingsService.addOrUpdateRating(ratings);
        return result.success() ?
                Result.success("添加或修改评分成功",result.data()) :
                Result.failed(500,"添加或修改评分失败");
    }
}
