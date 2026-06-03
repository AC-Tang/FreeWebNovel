package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.SearchLogs;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.SearchLogsService;
import com.example.freefiction.utils.ip.IpAddressUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/searchLogs")
@RequiredArgsConstructor
public class SearchLogsController {
    private final SearchLogsService searchLogsService;

    /**
     * 获取所有搜索记录
     * @return 搜索记录列表
     */
    @GetMapping("/fetch/all")
    public Result<List<SearchLogs>> fetchAll(){
        ServiceResult<List<SearchLogs>> result = searchLogsService.getAllSearchLogs();
        return result.success()?
                Result.success("获取所有搜索记录成功", result.data()):
                Result.failed(500,"获取搜索记录失败");
    }

    /**
     * 获取用户所有搜索记录
     * @param userId 用户ID
     * @return 搜索记录列表
     */
    @GetMapping("/fetch/user")
    public Result<List<SearchLogs>> fetchUser(@RequestParam Long userId){
        ServiceResult<List<SearchLogs>> result = searchLogsService.getSearchLogsByUserId(userId);
        return result.success()?
                Result.success("获取用户搜索记录成功", result.data()):
                Result.failed(500,"获取用户搜索记录失败");
    }

    /**
     * 删除用户所有搜索记录
     * @param userId 用户ID
     * @return 是否成功
     */
    @DeleteMapping("/delete/user")
    public Result<Boolean> deleteUser(@RequestParam Long userId){
        ServiceResult<Boolean> result = searchLogsService.deleteSearchLogsByUserId(userId);
        return result.success()?
                Result.success("删除用户搜索记录成功", result.data()):
                Result.failed(500,"删除用户搜索记录失败");
    }

    /**
     * 删除搜索记录
     * @param searchLogsId 搜索记录ID
     * @return 是否成功
     */
    @DeleteMapping("/delete")
    public Result<Boolean> delete(@RequestParam List<Long> searchLogsId){
        ServiceResult<Boolean> result = searchLogsService.deleteSearchLogs(searchLogsId);
        return result.success()?
                Result.success("删除搜索记录成功", result.data()):
                Result.failed(500,"删除搜索记录失败");
    }

    /**
     * 添加搜索记录
     * @param searchLogs 搜索记录
     * @return 是否成功
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody SearchLogs searchLogs){
        ServiceResult<Boolean> result = searchLogsService.addSearchLogs(searchLogs);
        return result.success()?
                Result.success("添加搜索记录成功", result.data()):
                Result.failed(500,"添加搜索记录失败");
    }
}
