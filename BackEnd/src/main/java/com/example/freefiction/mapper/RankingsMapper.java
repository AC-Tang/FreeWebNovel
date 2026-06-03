package com.example.freefiction.mapper;

import com.example.freefiction.entity.Rankings;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Tjianwei
* @description 针对表【rankings(排行榜表)】的数据库操作Mapper
* @createDate 2025-11-24 10:48:52
* @Entity com.example.freefiction.entity.Rankings
*/
@Mapper
public interface RankingsMapper extends BaseMapper<Rankings> {

}




