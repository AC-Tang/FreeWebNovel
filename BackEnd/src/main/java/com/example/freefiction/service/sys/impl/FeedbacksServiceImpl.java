package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Feedbacks;
import com.example.freefiction.entity.Notifications;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.FeedbacksService;
import com.example.freefiction.mapper.FeedbacksMapper;
import com.example.freefiction.service.sys.NotificationsService;
import com.example.freefiction.service.sys.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
* @author Tjianwei
* @description 针对表【feedbacks(反馈表)】的数据库操作Service实现
* @createDate 2025-11-24 10:48:59
*/
@Service
@RequiredArgsConstructor
public class FeedbacksServiceImpl extends ServiceImpl<FeedbacksMapper, Feedbacks>
    implements FeedbacksService{
    private final UsersService usersService;
    private final NotificationsService notificationsService;

    /**
     * 创建反馈
     * @param feedbacks 反馈对象
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> addFeedback(Feedbacks feedbacks){
        return save(feedbacks)?ServiceResult.ok(true):ServiceResult.fail("创建反馈失败");
    }

    /**
     * 根据状态获取所有反馈
     * @param status 状态
     * @return 反馈列表
     */
    @Override
    public ServiceResult<List<Feedbacks>> getFeedbacksByStatus(Integer  status){
        List<Feedbacks> list = list(new QueryWrapper<Feedbacks>().eq("status", status));
        return list==null?ServiceResult.fail("获取反馈失败"):ServiceResult.ok(list);
    }

    /**
     * 删除反馈
     * @param feedbackId 反馈id
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> deleteFeedback(Long feedbackId){
        return removeById(feedbackId)?ServiceResult.ok(true):ServiceResult.fail("删除反馈失败");
    }

    /**
     * 获取所有反馈
     * @return 反馈列表
     */
    @Override
    public ServiceResult<List<Feedbacks>> getAll(){
        List<Feedbacks> list = this.list();
        list.forEach(feedback -> {
            if(feedback.getUserId() != null)
                feedback.setUsername(usersService.getById(feedback.getUserId()).getUsername());
            if(feedback.getAdminId()!= null)
                feedback.setAdminName(usersService.getById(feedback.getAdminId()).getUsername());
        });
        return ServiceResult.ok(list);
    }

    /**
     * 更新反馈状态
     * @param feedback 反馈
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> updateFeedback(Feedbacks feedback){
        if(feedback.getStatus() == 3) {
            Notifications notifications = new Notifications();
            notifications.setUserId(feedback.getUserId());
            notifications.setType(5);
            notifications.setTitle("反馈处理结果");
            notifications.setContent(feedback.getAdminReply());
            notifications.setRelatedId(feedback.getId());
            notifications.setRelatedType("feedback");
            notificationsService.saveOrUpdate(notifications);
        }
        return update(new UpdateWrapper<Feedbacks>().eq("id", feedback.getId()).set("status", feedback.getStatus()).set("admin_reply", feedback.getAdminReply()).set("admin_id", feedback.getAdminId()).set("processed_at",new Date()))
                ?ServiceResult.ok(true):ServiceResult.fail("更新反馈失败");
    }
}




