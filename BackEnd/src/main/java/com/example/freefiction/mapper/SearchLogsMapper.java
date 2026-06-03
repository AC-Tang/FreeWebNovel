package com.example.freefiction.mapper;

import com.example.freefiction.entity.SearchLogs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Tjianwei
* @description 针对表【search_logs(搜索记录表)】的数据库操作Mapper
* @createDate 2025-11-24 10:48:40
* @Entity com.example.freefiction.entity.SearchLogs
*/
@Mapper
public interface SearchLogsMapper extends BaseMapper<SearchLogs> {

}




