package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.UserPreferences;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.UserPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/userPreferences")
@RequiredArgsConstructor
public class UserPreferencesController {
    private final UserPreferencesService userPreferencesService;

    /**
     * 获取用户偏好设置
     * @param userId 用户id
     * @return 用户偏好设置
     */
    @GetMapping("/fetch")
    public Result<UserPreferences> fetchUserPreferences(@RequestParam Long userId) {
        ServiceResult<UserPreferences> serviceResult = userPreferencesService.getUserPreferences(userId);
        return serviceResult.success() ?
                Result.success("获取用户偏好设置成功", serviceResult.data()) :
                Result.failed(500, "用户偏好设置不存在");
    }

    /**
     * 添加用户偏好设置
     * @param userPreferences 用户偏好设置
     * @return 是否成功
     */
    @PutMapping("/addOrUpdate")
    public Result<Boolean> addUserPreferences(@RequestBody UserPreferences userPreferences) {
        ServiceResult<Boolean> serviceResult = userPreferencesService.addUserPreferences(userPreferences);
        return serviceResult.success() ?
                Result.success("添加用户偏好设置成功", serviceResult.data()) :
                Result.failed(500, "添加用户偏好设置失败");
    }
}
