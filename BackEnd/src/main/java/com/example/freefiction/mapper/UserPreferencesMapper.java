package com.example.freefiction.mapper;

import com.example.freefiction.entity.UserPreferences;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Tjianwei
* @description 针对表【user_preferences(用户偏好表)】的数据库操作Mapper
* @createDate 2025-11-24 10:48:32
* @Entity com.example.freefiction.entity.UserPreferences
*/
@Mapper
public interface UserPreferencesMapper extends BaseMapper<UserPreferences> {

}




