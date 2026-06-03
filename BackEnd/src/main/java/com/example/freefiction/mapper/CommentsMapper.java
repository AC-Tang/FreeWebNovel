package com.example.freefiction.mapper;

import com.example.freefiction.entity.Comments;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Tjianwei
* @description 针对表【comments(评论表)】的数据库操作Mapper
* @createDate 2025-11-24 10:49:04
* @Entity com.example.freefiction.entity.Comments
*/
@Mapper
public interface CommentsMapper extends BaseMapper<Comments> {

}




