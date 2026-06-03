package com.example.freefiction.mapper;

import com.example.freefiction.entity.Categories;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
* @author Tjianwei
* @description 针对表【categories(分类表)】的数据库操作Mapper
* @createDate 2025-11-24 10:49:10
* @Entity com.example.freefiction.entity.Categories
*/
@Mapper
public interface CategoriesMapper extends BaseMapper<Categories> {

}




