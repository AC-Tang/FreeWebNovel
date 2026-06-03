package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.Recommendations;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.RecommendationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/recommendation")
public class RecommendationsController {
    private final RecommendationsService recommendationsService;

    /**
     * 添加推荐
     * @param recommendations 推荐对象
     * @return 是否成功
     */
    @PostMapping("/add")
    public Result<String> addRecommendations(Recommendations  recommendations){
        ServiceResult<Boolean> serviceResult = recommendationsService.addRecommendations(recommendations);
        return serviceResult.success() ?
                Result.success("添加推荐成功") :
                Result.failed("添加推荐失败");
    }

    /**
     * 删除推荐
     * @param novelId 书籍ID
     * @return 是否成功
     */
    @PostMapping("/delete")
    public Result<String> deleteRecommendations(Long novelId){
        ServiceResult<Boolean> serviceResult = recommendationsService.deleteRecommendations(novelId);
        return serviceResult.success() ?
                Result.success("删除推荐成功") :
                Result.failed("删除推荐失败");
    }
}
