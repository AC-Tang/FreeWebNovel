package com.example.freefiction.service.sys;

import com.example.freefiction.entity.Notifications;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【notifications(消息通知表)】的数据库操作Service
* @createDate 2025-11-24 10:48:55
*/
@Service
public interface NotificationsService extends IService<Notifications> {
    /**
     * 获取所有消息
     * @param userId 用户id
     * @return 消息列表
     */
    ServiceResult<List<Notifications>> getAllByUserId(Long userId);

    /**
     * 获取指定用户指定类型的消息
     * @param userId 用户id
     * @param type 消息类型
     * @return 消息列表
     */
    ServiceResult<List<Notifications>> getByUserIdAndType(Long userId, Integer type);

    /**
     * 获取未读消息数量
     * @param userId 用户id
     * @return 未读消息数量
     */
    ServiceResult<Integer> unreadCount(Long userId);

    /**
     * 设置消息已读
     * @param notificationId 消息id
     * @return 是否已读
     */
    ServiceResult<Boolean> isRead(Long notificationId);


    /**
     * 删除消息
     * @param notificationId 消息id
     * @return 是否成功
     */
    ServiceResult<Boolean> deleteNotification(Long notificationId);

    /**
     * 批量删除消息
     * @param notificationIds 消息id列表
     * @return 是否成功
     */
    ServiceResult<Boolean> deleteBatch(List<Long> notificationIds);

    /**
     * 批量删除消息
     * @param userId 用户id
     * @return 是否成功
     */
    ServiceResult<Boolean> deleteByUserIdAndType(Long userId , Integer type);

    /**
     * 批量设置消息已读
     * @param notificationIds 消息id列表
     * @return 是否成功
     */
    ServiceResult<Boolean> setReadBatch(List<Long> notificationIds);

    /**
     * 批量设置消息已读
     * @param userId 用户id
     * @param type 消息类型
     * @return 是否成功
     */
    ServiceResult<Boolean> setReadsByUserIdAndType(Long userId, Integer type);

    /**
     * 发送消息
     * @param notifications 消息对象
     * @return 是否成功
     */
    ServiceResult<Boolean> publicNotification(Notifications notifications);
}
