package com.example.freefiction.mapper;

import com.example.freefiction.entity.BookRequests;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Tjianwei
* @description 针对表【book_requests(书籍请求表)】的数据库操作Mapper
* @createDate 2025-11-24 10:49:18
* @Entity com.example.freefiction.entity.BookRequests
*/
@Mapper
public interface BookRequestsMapper extends BaseMapper<BookRequests> {

}




