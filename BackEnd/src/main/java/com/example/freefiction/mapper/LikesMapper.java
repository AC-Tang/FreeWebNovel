package com.example.freefiction.mapper;

import com.example.freefiction.entity.Likes;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Tjianwei
* @description 针对表【likes(点赞表)】的数据库操作Mapper
* @createDate 2025-11-24 10:48:57
* @Entity com.example.freefiction.entity.Likes
*/
@Mapper
public interface LikesMapper extends BaseMapper<Likes> {

}




