package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.domain.ChapterInfo;
import com.example.freefiction.entity.Chapters;
import com.example.freefiction.handler.CacheTimeStrategy;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.RedisService;
import com.example.freefiction.service.sys.ChaptersService;
import com.example.freefiction.mapper.ChaptersMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author Tjianwei
* @description 针对表【chapters(章节表)】的数据库操作Service实现
* @createDate 2025-11-24 10:49:08
*/
@Service
@RequiredArgsConstructor
public class ChaptersServiceImpl extends ServiceImpl<ChaptersMapper, Chapters> implements ChaptersService{
    private final RedisService redisService;

    /**
     * 获取章节列表
     * @param novelId 小说ID
     * @return 章节列表
     */
    @Override
    public ServiceResult<List<ChapterInfo>> getChaptersList(Long novelId) {
        System.out.println("获取章节列表 - 直接查询数据库");

        // 直接查询数据库，不使用缓存
        List<Chapters> chapters = this.list(new QueryWrapper<Chapters>()
                .eq("book_id", novelId)
                .orderByAsc("sort_order")
        );

        if (chapters == null || chapters.isEmpty()) {
            return ServiceResult.fail("获取章节列表失败");
        }

        List<ChapterInfo> chapterInfos = chapters.stream()
                .map(chapter -> {
                    // 清理标题
                    String cleanTitle = chapter.getTitle();
                    if (cleanTitle != null) {
                        cleanTitle = cleanTitle.replaceAll("[\\u0000-\\u001F\\u007F-\\u009F]", "");
                    }
                    return new ChapterInfo(
                            chapter.getBookId(),
                            cleanTitle,
                            chapter.getSortOrder(),
                            chapter.getUpdatedAt(),
                            chapter.getWordCount()
                    );
                })
                .collect(Collectors.toList());

        // 可选：以更安全的方式存储到Redis
        try {
            String key = "Chapters:List:" + novelId;
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(chapterInfos);
            // 使用RedisTemplate直接存储字符串
            redisService.set(key, json,CacheTimeStrategy.NOVEL_INFO);
        } catch (Exception e) {
            // 忽略缓存错误
        }

        return ServiceResult.ok(chapterInfos);
    }

    /**
     * 获取章节
     * @param novelId 小说ID
     * @param chapterOrder 章节顺序
     * @return 章节对象
     */
    @Override
    public ServiceResult<Chapters> getChapter(Long novelId, Integer chapterOrder){
        Chapters chapters = this.getOne(new QueryWrapper<Chapters>()
                .eq("book_id", novelId)
                .eq("sort_order", chapterOrder)
        );
        if(chapters == null)
            return ServiceResult.fail("获取章节失败");
        return ServiceResult.ok(chapters);
    }
}




