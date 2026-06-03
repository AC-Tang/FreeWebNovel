package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.Notifications;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.NotificationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {
    private final NotificationsService notificationsService;

    /**
     * 获取用户所有通知
     * @param userId 用户ID
     * @return 通知列表
     */
    @GetMapping("/fetch/{userId}")
    public Result<List<Notifications>> fetchNotifications(@PathVariable Long userId) {
        ServiceResult<List<Notifications>> serviceResult = notificationsService.getAllByUserId(userId);
        return serviceResult.success() ?
                Result.success("成功获取通知列表",serviceResult.data()) :
                Result.failed(404,"通知列表为空");
    }

    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @GetMapping("/fetch/unread/count/{userId}")
    public Result<Integer> fetchUnreadCount(@PathVariable Long userId) {
        ServiceResult<Integer> serviceResult = notificationsService.unreadCount(userId);
        return serviceResult.success() ?
                Result.success("成功获取未读通知数量",serviceResult.data()) :
                Result.failed(404,"未读通知数量为空");
    }

    /**
     * 获取用户指定类型的通知
     * @param userId 用户ID
     * @param type 通知类型
     * @return 通知列表
     */
    @GetMapping("/fetch")
    public Result<List<Notifications>> fetchNotificationsByIdAndType(@RequestParam Long userId, @RequestParam Integer type) {
        ServiceResult<List<Notifications>> serviceResult = notificationsService.getByUserIdAndType(userId, type);
        return serviceResult.success() ?
                Result.success("成功获取通知列表",serviceResult.data()) :
                Result.failed(404,"通知列表为空");
    }

    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     * @return 是否成功
     */
    @PutMapping("/markAsRead")
    public Result<Boolean> markAsRead(@RequestParam Long notificationId) {
        ServiceResult<Boolean> serviceResult = notificationsService.isRead(notificationId);
        return serviceResult.success() ?
                Result.success("成功标记通知为已读",serviceResult.data()) :
                Result.failed(404,"标记通知为已读失败");
    }

    /**
     * 批量标记通知为已读
     * @param notificationIds 批量通知ID
     * @return 是否成功
     */
    @PutMapping("/batchMarkAsRead")
    public Result<Boolean> markAsReads(@RequestParam List<Long> notificationIds) {
        ServiceResult<Boolean> serviceResult = notificationsService.setReadBatch(notificationIds);
        return serviceResult.success() ?
                Result.success("成功标记通知为已读",serviceResult.data()) :
                Result.failed(404,"标记通知为已读失败");
    }

    /**
     * 批量标记用户指定类型的通知为已读
     * @param userId 用户ID
     * @param type 通知类型
     * @return 是否成功
     */
    @PutMapping("/markAsReads")
    public Result<Boolean> markAsReads(@RequestParam Long userId, @RequestParam Integer type) {
        ServiceResult<Boolean> serviceResult = notificationsService.setReadsByUserIdAndType(userId, type);
        return serviceResult.success() ?
                Result.success("成功标记通知为已读",serviceResult.data()) :
                Result.failed(404,"标记通知为已读失败");
    }
    /**
     * 删除通知
     * @param notificationId 通知ID
     * @return 是否成功
     */
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam Long notificationId) {
        ServiceResult<Boolean> serviceResult = notificationsService.deleteNotification(notificationId);
        return serviceResult.success() ?
                Result.success("成功删除通知",serviceResult.data()) :
                Result.failed(404,"删除通知失败");
    }

    /**
     * 批量删除通知
     * @param notificationIds 通知ID列表
     * @return 是否成功
     */
    @DeleteMapping("/batchDelete")
    public Result<Boolean> deleteAll(@RequestParam List<Long> notificationIds) {
        ServiceResult<Boolean> serviceResult = notificationsService.deleteBatch(notificationIds);
        return serviceResult.success() ?
                Result.success("成功删除通知",serviceResult.data()) :
                Result.failed(404,"删除通知失败");
    }

    /**
     * 删除用户所有通知
     * @param userId 用户ID
     * @return 是否成功
     */
    @DeleteMapping("/deleteAll")
    public Result<Boolean> deleteByUserId(@RequestParam Long userId,@RequestParam Integer type) {
        ServiceResult<Boolean> serviceResult = notificationsService.deleteByUserIdAndType(userId, type);
        return serviceResult.success() ?
                Result.success("成功删除通知",serviceResult.data()) :
                Result.failed(404,"删除通知失败");
    }

    /**
     * 添加通知
     * @param notifications 通知
     * @return 是否成功
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Notifications notifications) {
        ServiceResult<Boolean> serviceResult = notificationsService.publicNotification(notifications);
        return serviceResult.success() ?
                Result.success("成功添加通知",serviceResult.data()) :
                Result.failed(404,"添加通知失败");
    }

}
