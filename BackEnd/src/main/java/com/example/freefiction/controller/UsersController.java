package com.example.freefiction.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.freefiction.domain.AuthResponse;
import com.example.freefiction.handler.Result;
import com.example.freefiction.domain.UsersLogin;
import com.example.freefiction.entity.Users;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.AuthService;
import com.example.freefiction.service.sys.UsersService;
import com.example.freefiction.utils.jjwt.JJwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UsersController {
    private final UsersService usersService;
    private final JJwtUtil jwtUtil;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 登录
     * @param userLogin 用户登录信息
     * @return 登录结果
     */
    @PostMapping( "/login")
    public Result<Users> login(@RequestBody UsersLogin userLogin) {
        ServiceResult<Users> serviceResult = authService.login(userLogin);
        if (serviceResult.success()) {
            Result<Users>  result = Result.success(serviceResult.data());
            result.getData().setAvatar(null);
            result.setToken(new AuthResponse(jwtUtil.generateToken(serviceResult.data()), jwtUtil.generateRefreshToken(serviceResult.data())));
            return result;
        }
        return Result.failed(serviceResult.message());
    }

    /**
     * 注册
     * @param userLogin 用户登录信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Users> register(@RequestBody UsersLogin userLogin) {
        ServiceResult<Users> serviceResult = authService.register(userLogin);
        return serviceResult.success() ? Result.success("注册成功",serviceResult.data()):
                Result.failed(400,serviceResult.message());
    }

    /**
     * 修改密码
     * @param user 用户信息
     * @return 修改结果
     */
    @PutMapping("/update/password")
    public Result<Users> updatePassword(@RequestBody Users user) {
        Users dbUser = usersService.getOne(new QueryWrapper<Users>().eq("email", user.getEmail()));
        if(dbUser== null){
            return Result.failed("用户不存在");
        }
        if(passwordEncoder.matches(user.getNewPassword(), dbUser.getPasswordHash())){
            return Result.failed("新密码不能与旧密码一致");
        }
        boolean result = usersService.update(new UpdateWrapper<Users>().eq("email", user.getEmail()).set("password_hash", passwordEncoder.encode(user.getNewPassword())));
        dbUser.setPasswordHash(passwordEncoder.encode(user.getNewPassword()));
        return result ? Result.success(dbUser) : Result.failed("修改密码失败");
    }

    /**
     * 修改用户信息
     * @param users 用户信息
     * @return 修改结果
     */
    @PutMapping("/update/info")
    public Result<Users> updateInfo(@RequestBody Users users) {
        ServiceResult<Users> serviceResult = usersService.updateInfo(users);
        return serviceResult.success() ? Result.success("修改用户信息成功",serviceResult.data()):
                Result.failed(400,serviceResult.message());
    }

    /**
     * 修改邮箱
     * @param users 用户信息
     * @return 修改结果
     */
    @PutMapping("/update/email")
    public Result<Users> updateEmail(@RequestBody Users users) {
        ServiceResult<Users> serviceResult = usersService.updateEmail(users);
        return serviceResult.success() ? Result.success("修改邮箱成功",serviceResult.data()):
                Result.failed(400,serviceResult.message());
    }

    /**
     * 修改头像
     * @param file 文件
     * @param userId 用户id
     * @return 修改结果
     */
    @PutMapping("/update/avatar")
    public Result<Users> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) throws IOException {
        ServiceResult<Users> serviceResult = usersService.updateAvatar(file, userId);
        return serviceResult.success() ? Result.success("修改头像成功",serviceResult.data()):
                Result.failed(400,serviceResult.message());
    }

    /**
     * 修改用户角色
     * @param userId 用户id
     * @param role 角色
     * @return 修改结果
     */
    @PutMapping("/update/role")
    public Result<Boolean> updateRole(@RequestParam("userId") Long userId, @RequestParam("role") String role) {
        ServiceResult<Boolean> serviceResult = usersService.updateRole(userId, role);
        return serviceResult.success() ? Result.success("修改用户角色成功",serviceResult.data()):
                Result.failed(400,serviceResult.message());
    }

    /**
     * 重置密码
     * @param userId 用户id
     * @return 重置结果
     */
    @PutMapping("/resetPassword")
    public Result<Boolean> resetPassword(@RequestParam Long userId) {
        boolean result = usersService.update(new UpdateWrapper<Users>().eq("id", userId).set("password_hash", passwordEncoder.encode("111111")));
        return result ? Result.success("重置密码成功",result) : Result.failed(400,"重置密码失败");
    }

    /**
     * 修改用户状态
     * @param userId 用户id
     * @param status 状态
     * @return 修改结果
     */
    @PutMapping("/update/status")
    public Result<Boolean> updateStatus(@RequestParam Long userId, @RequestParam Integer status) {
        ServiceResult<Boolean> serviceResult = usersService.updateStatus(userId, status);
        return serviceResult.success() ? Result.success("修改用户状态成功",serviceResult.data()):
                Result.failed(400,serviceResult.message());
    }

    /**
     * 注销
     * @param jwt jwt
     * @return 注销结果
     */
    @PostMapping("/logout")
    public Result<Boolean> logout(@RequestHeader("Authorization") String jwt) {
        ServiceResult<Boolean> serviceResult = usersService.logout(jwt);
        return serviceResult.success() ? Result.success("注销成功",serviceResult.data()):
                Result.failed(401,serviceResult.message());
    }

    /**
     * 获取用户信息
     * @param jwt jwt
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<Users> info(@RequestHeader("Authorization") String jwt) {
        ServiceResult<Users> serviceResult = usersService.info(jwt);
        return serviceResult.success() ? Result.success("获取用户信息成功",serviceResult.data()):
                Result.failed(401,serviceResult.message());
    }

    /**
     * 获取所有用户
     * @return 所有用户
     */
    @GetMapping("/all")
    public Result<List<Users>> getAll() {
        ServiceResult<List<Users>> serviceResult = usersService.getAll();
        return serviceResult.success() ? Result.success("获取所有用户成功",serviceResult.data()):
                Result.failed(401,serviceResult.message());
    }

    /**
     * 获取用户数量
     * @return 用户数量
     */
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success("获取用户数量成功",usersService.count());
    }

    /**
     * 删除用户
     * @param userId 用户id
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<Boolean> deleteUser(@RequestParam Long userId) {
        ServiceResult<Boolean> serviceResult = usersService.deleteUser(userId);
        return serviceResult.success() ? Result.success("删除用户成功",true):
                Result.failed(401,"删除用户失败");
    }
}
