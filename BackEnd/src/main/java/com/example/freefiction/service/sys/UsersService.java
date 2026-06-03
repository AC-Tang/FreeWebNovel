package com.example.freefiction.service.sys;

import com.example.freefiction.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
* @author Tjianwei
* @description 针对表【users(用户表)】的数据库操作Service
* @createDate 2025-11-24 10:48:23
*/
@Service
public interface UsersService extends IService<Users> {
    ServiceResult<Boolean> logout(String jwt);

    ServiceResult<Users> info(String jwt);

    ServiceResult<Users> updateEmail(Users user);

    ServiceResult<Users> updateInfo(Users user);

    ServiceResult<Users> updateAvatar(MultipartFile file, Long userId) throws IOException;

    /**
     * 获取所有用户
     * @return 所有用户
     */
    ServiceResult<List<Users>> getAll();

    /**
     * 删除用户
     * @param userId 用户id
     * @return 删除结果
     */
    ServiceResult<Boolean> deleteUser(Long userId);


    /**
     * 更新用户角色
     * @param userId 用户id
     * @param role 角色
     * @return 更新结果
     */
    ServiceResult<Boolean> updateRole(Long userId, String role);

    /**
     * 更新用户状态
     * @param userId 用户id
     * @param status 状态
     * @return 更新结果
     */
    ServiceResult<Boolean> updateStatus(Long userId, Integer status);
}
