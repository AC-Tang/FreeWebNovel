package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.UserPreferences;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.UserPreferencesService;
import com.example.freefiction.mapper.UserPreferencesMapper;
import org.springframework.stereotype.Service;

/**
* @author Tjianwei
* @description 针对表【user_preferences(用户偏好表)】的数据库操作Service实现
* @createDate 2025-11-24 10:48:32
*/
@Service
public class UserPreferencesServiceImpl extends ServiceImpl<UserPreferencesMapper, UserPreferences>
    implements UserPreferencesService{
    /**
     * 添加用户偏好
     * @param userPreferences 用户偏好
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> addUserPreferences(UserPreferences userPreferences){
        boolean result= this.saveOrUpdate(userPreferences);
        return result ? ServiceResult.ok(true) : ServiceResult.fail("添加失败");
    }

    /**
     * 获取用户偏好
     * @param userId 用户id
     * @return 用户偏好
     */
    @Override
    public ServiceResult<UserPreferences> getUserPreferences(Long userId){
        UserPreferences userPreferences = this.getOne(new QueryWrapper<UserPreferences>().eq("user_id", userId));
        return userPreferences != null ? ServiceResult.ok(userPreferences) : ServiceResult.fail("获取失败");
    }
}




