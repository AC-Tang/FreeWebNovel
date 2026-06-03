package com.example.freefiction.mapper;

import com.example.freefiction.entity.Chapters;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
* @author Tjianwei
* @description 针对表【chapters(章节表)】的数据库操作Mapper
* @createDate 2025-11-24 10:49:08
* @Entity com.example.freefiction.entity.Chapters
*/
@Mapper
public interface ChaptersMapper extends BaseMapper<Chapters> {

}




