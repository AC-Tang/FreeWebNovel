package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Notifications;
import com.example.freefiction.entity.Users;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.NotificationsService;
import com.example.freefiction.mapper.NotificationsMapper;
import com.example.freefiction.service.sys.UsersService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* @author Tjianwei
* @description 针对表【notifications(消息通知表)】的数据库操作Service实现
* @createDate 2025-11-24 10:48:55
*/
@Service
public class NotificationsServiceImpl extends ServiceImpl<NotificationsMapper, Notifications>
    implements NotificationsService{
    private final UsersService usersService;

    public NotificationsServiceImpl(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public ServiceResult<List<Notifications>> getAllByUserId(Long userId){
        List<Notifications> notifications= this.list(new QueryWrapper<Notifications>().eq("user_id", userId));
        if(notifications == null)
            return ServiceResult.fail("没有消息");
        return ServiceResult.ok(notifications);
    }

    @Override
    public ServiceResult<List<Notifications>> getByUserIdAndType(Long userId, Integer type){
        if(type == null)
            return ServiceResult.fail("请选择消息类型");
        List<Integer> typeList = new ArrayList<>();
        if (type == 1) {
            typeList.addAll(Arrays.asList(3, 5));
        } else if (type == 2) {
            typeList.addAll(Arrays.asList(1, 2));
        } else if (type == 3) {
            typeList.add(4);
        }
        List<Notifications> notifications= this.list(new QueryWrapper<Notifications>()
                .eq("user_id", userId)
                .in("type", typeList));
        if(notifications == null)
            return ServiceResult.fail("没有消息");
        return ServiceResult.ok(notifications);
    }

    @Override
    public ServiceResult<Integer> unreadCount(Long userId){
        Integer count = Math.toIntExact(this.count(new QueryWrapper<Notifications>()
                .eq("user_id", userId)
                .eq("is_read", 0)));
        return ServiceResult.ok(count);
    }

    /**
     * 设置消息已读
     * @param notificationId 消息id
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> isRead(Long notificationId){
        boolean result = this.update(new UpdateWrapper<Notifications>()
                .eq("id", notificationId)
                .set("is_read", 1));
        if (!result)
            return ServiceResult.fail("更新失败");
        return ServiceResult.ok(true);
    }

    /**
     * 删除消息
     * @param notificationId 消息id
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> deleteNotification(Long notificationId){
        boolean result = this.removeById(notificationId);
        return result ? ServiceResult.ok(true) : ServiceResult.fail("删除失败");
    }

    /**
     * 批量删除消息
     * @param notificationIds 消息id列表
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> deleteBatch(List<Long> notificationIds){
        boolean result = this.removeByIds(notificationIds);
        return result ? ServiceResult.ok(true) : ServiceResult.fail("删除失败");
    }

    /**
     * 批量删除消息
     * @param userId 用户id
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> deleteByUserIdAndType(Long userId, Integer type){
        boolean result = this.remove(new QueryWrapper<Notifications>().eq("user_id", userId).eq("type", type));
        return result ? ServiceResult.ok(true) : ServiceResult.fail("删除失败");
    }

    /**
     * 批量设置消息已读
     * @param notificationIds 消息id列表
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> setReadBatch(List<Long> notificationIds){
        boolean result = this.update(new UpdateWrapper<Notifications>()
                .in("id", notificationIds)
                .set("is_read", 1));
        return result ? ServiceResult.ok(true) : ServiceResult.fail("更新失败");
    }

    /**
     * 批量设置消息已读
     * @param userId 用户id
     * @param type 消息类型
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> setReadsByUserIdAndType(Long userId, Integer type){
        boolean result = this.update(new UpdateWrapper<Notifications>()
                .eq("user_id", userId)
                .eq("type", type)
                .set("is_read", 1));
        return result ? ServiceResult.ok(true) : ServiceResult.fail("更新失败");
    }

    /**
     * 发送消息
     * @param notifications 消息对象
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> publicNotification(Notifications notifications){
        List<Long> userIds = switch (notifications.getUserIds()) {
            case "ALL" -> usersService.list(new QueryWrapper<Users>().select("id")).stream().map(Users::getId).toList();
            case "ROLE_USER" ->
                    usersService.list(new QueryWrapper<Users>().select("id").eq("role_name", "ROLE_USER")).stream().map(Users::getId).toList();
            case "ROLE_ADMIN" ->
                    usersService.list(new QueryWrapper<Users>().select("id").eq("role_name", "ROLE_ADMIN")).stream().map(Users::getId).toList();
            default -> Arrays.stream(notifications.getUserIds().split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList();
        };
        for (Long userId : userIds){
            notifications.setUserId(userId);
            save( notifications);
        }
        return ServiceResult.ok(true);
    }
}




