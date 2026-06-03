package com.example.freefiction.service.sys;

import com.example.freefiction.entity.Feedbacks;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【feedbacks(反馈表)】的数据库操作Service
* @createDate 2025-11-24 10:48:59
*/
@Service
public interface FeedbacksService extends IService<Feedbacks> {
    /**
     * 创建反馈
     * @param feedbacks 反馈对象
     * @return 是否成功
     */
    ServiceResult<Boolean> addFeedback(Feedbacks feedbacks);

    /**
     * 根据状态获取所有反馈
     * @param status 状态
     * @return 反馈列表
     */
    ServiceResult<List<Feedbacks>> getFeedbacksByStatus(Integer  status);

    /**
     * 更新反馈状态
     * @param feedback 反馈
     * @return 是否成功
     */
    ServiceResult<Boolean> updateFeedback(Feedbacks feedback);

    /**
     * 删除反馈
     * @param feedbackId 反馈id
     * @return 是否成功
     */
    ServiceResult<Boolean> deleteFeedback(Long feedbackId);

    /**
     * 获取所有反馈
     * @return 反馈列表
     */
    ServiceResult<List<Feedbacks>> getAll();
}
