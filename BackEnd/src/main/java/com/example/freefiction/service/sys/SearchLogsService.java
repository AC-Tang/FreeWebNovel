package com.example.freefiction.service.sys;

import com.example.freefiction.entity.SearchLogs;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【search_logs(搜索记录表)】的数据库操作Service
* @createDate 2025-11-24 10:48:40
*/
@Service
public interface SearchLogsService extends IService<SearchLogs> {
    /**
    * 添加搜索记录
    * @param searchLogs 搜索记录
    * @return 是否成功
    */
    ServiceResult<Boolean> addSearchLogs(SearchLogs searchLogs);

    /**
    * 获取所有搜索记录
    * @return 搜索记录列表
    */
    ServiceResult<List<SearchLogs>> getAllSearchLogs();

    /**
    * 获取用户所有搜索记录
    * @param userId 用户ID
    * @return 搜索记录列表
    */
    ServiceResult<List<SearchLogs>> getSearchLogsByUserId(Long userId);

    /**
     * 删除用户所有搜索记录
     * @param userId 用户ID
    * @return 是否成功
    */
    ServiceResult<Boolean> deleteSearchLogsByUserId(Long userId);

    /**
     * 删除搜索记录
     * @param searchLogsId 搜索记录ID
    * @return 是否成功
    */
    ServiceResult<Boolean> deleteSearchLogs(List<Long> searchLogsId);

}
