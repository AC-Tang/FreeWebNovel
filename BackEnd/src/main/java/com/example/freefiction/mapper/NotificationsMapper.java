package com.example.freefiction.mapper;

import com.example.freefiction.entity.Notifications;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Tjianwei
* @description 针对表【notifications(消息通知表)】的数据库操作Mapper
* @createDate 2025-11-24 10:48:55
* @Entity com.example.freefiction.entity.Notifications
*/
@Mapper
public interface NotificationsMapper extends BaseMapper<Notifications> {

}




