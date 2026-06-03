package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Bookshelves;
import com.example.freefiction.entity.ReadingRecords;
import com.example.freefiction.entity.Users;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.BookshelvesService;
import com.example.freefiction.service.sys.ReadingRecordsService;
import com.example.freefiction.service.sys.UsersService;
import com.example.freefiction.mapper.UsersMapper;
import com.example.freefiction.utils.constant.TokenBlacklist;
import com.example.freefiction.utils.jjwt.JJwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
* @author Tjianwei
* @description 针对表【users(用户表)】的数据库操作Service实现
* @createDate 2025-11-24 10:48:23
*/
@Service
@RequiredArgsConstructor
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{
    private final JJwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;
    private final BookshelvesService bookshelvesService;
    private final ReadingRecordsService readingRecordsService;

    @Override
    public ServiceResult<Users> info(String jwt){
        if (jwt == null || !jwt.startsWith("Bearer ")) {
            return ServiceResult.fail("无效的token");
        }
        String token = jwt.substring(7);
        if (!jwtUtil.verify(token)) {
            return ServiceResult.fail("token已过期");
        }
        Integer userId = jwtUtil.getUserFromToken(token);
        return ServiceResult.ok(this.getById(userId));
    }

    @Override
    public ServiceResult<Boolean> logout(String jwt){
        if (jwt == null || !jwt.startsWith("Bearer ")) {
            return ServiceResult.fail("无效的token");
        }
        String token = jwt.substring(7);

        tokenBlacklist.blacklistToken(token);
        System.out.println("退出登录成功");
        return ServiceResult.ok(true);
    }

    @Override
    public ServiceResult<Users> updateInfo(Users user){
        Users dbUser = this.getOne(new QueryWrapper<Users>().eq("id", user.getId()));
        if(dbUser== null){
            return ServiceResult.fail("用户不存在");
        }
        user.setId(dbUser.getId());
        boolean result = this.updateById(user);
        return result ? ServiceResult.ok(user) : ServiceResult.fail("修改用户信息失败");
    }

    @Override
    public ServiceResult<Users> updateEmail(Users user){
        Users dbUser = this.getOne(new QueryWrapper<Users>().eq("id", user.getId()));
        if (dbUser== null){
            return ServiceResult.fail("用户不存在");
        }
        boolean result = this.update(new UpdateWrapper<Users>().eq("id", dbUser.getId()).set("email", user.getEmail()));
        return result ? ServiceResult.ok(user) : ServiceResult.fail("修改邮箱失败");
    }


    @Override
    public ServiceResult<Users> updateAvatar(MultipartFile file, Long userId) throws IOException {
        Users dbUser = this.getById(userId);
        if(dbUser== null){
            return ServiceResult.fail("用户不存在");
        }
        boolean result = this.update(new UpdateWrapper<Users>().eq("id",userId).set("avatar", file.getBytes()));
        dbUser.setAvatar(file.getBytes());
        return result ? ServiceResult.ok(dbUser) : ServiceResult.fail("修改头像失败");
    }

    /**
     * 获取所有用户
     * @return 所有用户列表
     */
    @Override
    public ServiceResult<List<Users>> getAll(){
        List<Users> usersList = list().stream().map(users -> new Users(
                    users.getId(),
                    users.getUsername(),
                    users.getEmail(),
                    users.getRoleName(),
                    users.getStatus(),
                    users.getLastLoginTime(),
                    users.getCreatedAt(),
                    users.getAvatar()
        )).toList();
        usersList.forEach(users -> {
            users.setBookshelfCount(bookshelvesService.count(new QueryWrapper<Bookshelves>().eq("user_id", users.getId())));
            users.setReadingCount(readingRecordsService.count(new QueryWrapper<ReadingRecords>().eq("user_id", users.getId())));
        });
        return ServiceResult.ok(usersList);
    }

    /**
     * 删除用户
     * @param userId 用户ID
     * @return 删除结果
     */
    @Override
    public ServiceResult<Boolean> deleteUser(Long userId){
        return remove(new QueryWrapper<Users>().eq("id", userId))? ServiceResult.ok(true) : ServiceResult.fail("删除用户失败");
    }

    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param role 角色
     * @return 更新结果
     */
    @Override
    public ServiceResult<Boolean> updateRole(Long userId, String role){
        return update(new UpdateWrapper<Users>().eq("id", userId).set("role_name", role))? ServiceResult.ok(true) : ServiceResult.fail("更新用户角色失败");
    }

    /**
     * 更新用户状态
     * @param userId 用户ID
     * @return 更新结果
     */
    @Override
    public ServiceResult<Boolean> updateStatus(Long userId, Integer status){
        return update(new UpdateWrapper<Users>().eq("id", userId).set("status", status))? ServiceResult.ok(true) : ServiceResult.fail("更新用户状态失败");
    }
}




