package com.example.freefiction.mapper;

import com.example.freefiction.entity.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
/**
* @author Tjianwei
* @description 针对表【users(用户表)】的数据库操作Mapper
* @createDate 2025-11-24 10:48:23
* @Entity com.example.freefiction.entity.Users
*/
@Mapper
public interface UsersMapper extends BaseMapper<Users> {

}




