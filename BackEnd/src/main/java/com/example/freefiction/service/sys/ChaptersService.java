package com.example.freefiction.service.sys;

import com.example.freefiction.domain.ChapterInfo;
import com.example.freefiction.entity.Chapters;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【chapters(章节表)】的数据库操作Service
* @createDate 2025-11-24 10:49:08
*/
@Service
public interface ChaptersService extends IService<Chapters> {
    /**
     * 获取小说的章节列表
     * @param novelId 小说ID
     * @return 章节列表
     */
    ServiceResult<List<ChapterInfo>> getChaptersList(Long novelId);

    /**
     * 获取单个章节内容
     * @param novelId 小说ID
     * @param chapterOrder 章节顺序
     * @return 章节内容
     */
    ServiceResult<Chapters> getChapter(Long novelId, Integer chapterOrder);
}
