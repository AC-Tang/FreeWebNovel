package com.example.freefiction.mapper;

import com.example.freefiction.entity.Feedbacks;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Tjianwei
* @description 针对表【feedbacks(反馈表)】的数据库操作Mapper
* @createDate 2025-11-24 10:48:59
* @Entity com.example.freefiction.entity.Feedbacks
*/
@Mapper
public interface FeedbacksMapper extends BaseMapper<Feedbacks> {

}




