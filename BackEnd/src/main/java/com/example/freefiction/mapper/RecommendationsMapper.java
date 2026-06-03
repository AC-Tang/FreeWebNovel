package com.example.freefiction.mapper;

import com.example.freefiction.entity.Recommendations;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Tjianwei
* @description 针对表【recommendations(推荐表)】的数据库操作Mapper
* @createDate 2025-11-24 10:48:44
* @Entity com.example.freefiction.entity.Recommendations
*/
@Mapper
public interface RecommendationsMapper extends BaseMapper<Recommendations> {

}




