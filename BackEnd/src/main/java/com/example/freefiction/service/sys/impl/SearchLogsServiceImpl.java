package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.SearchLogs;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.SearchLogsService;
import com.example.freefiction.mapper.SearchLogsMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
* @author Tjianwei
* @description 针对表【search_logs(搜索记录表)】的数据库操作Service实现
* @createDate 2025-11-24 10:48:40
*/
@Service
public class SearchLogsServiceImpl extends ServiceImpl<SearchLogsMapper, SearchLogs>
    implements SearchLogsService{
    /**
     * 添加搜索记录
     * @param searchLogs 搜索记录
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> addSearchLogs(SearchLogs searchLogs){
        searchLogs.setIsDelete(1);
        searchLogs.setCreatedAt(new Date());
        return save(searchLogs)?ServiceResult.ok(true):ServiceResult.fail("添加失败");
    }

    /**
     * 获取所有搜索记录
     * @return 搜索记录列表
     */
    @Override
    public ServiceResult<List<SearchLogs>> getAllSearchLogs(){
        return list()!=null?ServiceResult.ok(list()):ServiceResult.fail("获取失败");
    }

    /**
     * 获取用户所有搜索记录
     * @param userId 用户ID
     * @return 搜索记录列表
     */
    @Override
    public ServiceResult<List<SearchLogs>> getSearchLogsByUserId(Long userId){
        List<SearchLogs> list = list(new QueryWrapper<SearchLogs>().eq("user_id", userId));
        return  list!=null?ServiceResult.ok(list):ServiceResult.fail("获取失败");
    }

    /**
     * 删除用户所有搜索记录
     * @param userId 用户ID
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> deleteSearchLogsByUserId(Long userId){
        return update(new UpdateWrapper<SearchLogs>().eq("user_id", userId).set("is_delete", 0))?ServiceResult.ok(true):ServiceResult.fail("删除失败");
    }

    /**
     * 删除搜索记录
     * @param searchLogsId 搜索记录ID
     * @return 是否成功
     */
    @Override
    public ServiceResult<Boolean> deleteSearchLogs(List<Long> searchLogsId){
        return update(new UpdateWrapper<SearchLogs>().in("id", searchLogsId).set("is_delete", 0))?ServiceResult.ok(true):ServiceResult.fail("删除失败");
    }
}




