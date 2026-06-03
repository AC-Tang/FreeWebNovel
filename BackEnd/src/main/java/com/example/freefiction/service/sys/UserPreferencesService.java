package com.example.freefiction.service.sys;

import com.example.freefiction.entity.UserPreferences;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

/**
* @author Tjianwei
* @description 针对表【user_preferences(用户偏好表)】的数据库操作Service
* @createDate 2025-11-24 10:48:32
*/
@Service
public interface UserPreferencesService extends IService<UserPreferences> {
    /**
     * 添加用户偏好
     * @param userPreferences 用户偏好
     * @return 是否成功
     */
    ServiceResult<Boolean> addUserPreferences(UserPreferences userPreferences);

    /**
     * 获取用户偏好
     * @param userId 用户id
     * @return 用户偏好
     */
    ServiceResult<UserPreferences> getUserPreferences(Long userId);
}
