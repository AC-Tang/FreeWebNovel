package com.example.freefiction.mapper;

import com.example.freefiction.entity.Ratings;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Tjianwei
* @description 针对表【ratings(评分表)】的数据库操作Mapper
* @createDate 2025-11-24 10:48:49
* @Entity com.example.freefiction.entity.Ratings
*/
@Mapper
public interface RatingsMapper extends BaseMapper<Ratings> {

}




