package com.example.freefiction.mapper;

import com.example.freefiction.entity.Books;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
* @author Tjianwei
* @description 针对表【books(书籍表)】的数据库操作Mapper
* @createDate 2025-11-24 10:49:16
* @Entity com.example.freefiction.entity.Books
*/
@Mapper
public interface BooksMapper extends BaseMapper<Books> {

}




