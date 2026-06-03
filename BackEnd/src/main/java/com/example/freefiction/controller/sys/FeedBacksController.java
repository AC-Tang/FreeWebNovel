package com.example.freefiction.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.freefiction.entity.Feedbacks;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.FeedbacksService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedBacksController {
    private final FeedbacksService feedbacksService;

    /**
     * 添加反馈
     * @param feedbacks 反馈对象
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<Boolean> addFeedback(@RequestBody Feedbacks feedbacks) {
        ServiceResult<Boolean> result = feedbacksService.addFeedback(feedbacks);
        return result.success()?
                Result.success("添加反馈成功",result.data()):
                Result.failed(500,"添加反馈失败");
    }

    /**
     * 更新反馈
     * @param feedback 反馈对象
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result<Boolean> updateFeedback(@RequestBody Feedbacks feedback) {
        ServiceResult<Boolean> result = feedbacksService.updateFeedback( feedback);
        return result.success()?
                Result.success("更新反馈成功",result.data()):
                Result.failed(500,"更新反馈失败");
    }

    /**
     * 删除反馈
     * @param feedbackId 反馈ID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<Boolean> deleteFeedback(@RequestParam Long feedbackId) {
        ServiceResult<Boolean> result = feedbacksService.deleteFeedback(feedbackId);
        return result.success()?
                Result.success("删除反馈成功",result.data()):
                Result.failed(500,"删除反馈失败");
    }

    /**
     * 获取反馈数量
     * @param status 状态
     * @return 数量
     */
    @GetMapping("/count")
    public Result<Long> countFeedback(@RequestParam Integer status) {
        return Result.success("获取反馈数量成功",feedbacksService.count(new QueryWrapper<Feedbacks>().eq("status",status)));
    }

    /**
     * 获取所有反馈
     * @return 所有反馈列表
     */
    @GetMapping("/all")
    public Result<List<Feedbacks>> getAll() {
        ServiceResult<List<Feedbacks>> result = feedbacksService.getAll();
        return result.success()?
                Result.success("获取所有反馈成功",result.data()):
                Result.failed(500,"获取所有反馈失败");
    }
}
