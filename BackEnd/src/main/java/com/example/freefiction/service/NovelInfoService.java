package com.example.freefiction.service;

import com.example.freefiction.entity.Books;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 小说信息服务，用于爬取小说信息
 */
@Service
public interface NovelInfoService {

    /**
     * 爬取小说信息，并写入数据库
     * @param novelId 小说ID
     * @return 是否成功
     */
    Books crawlNovelInfo(Long novelId);

    /**
     * 更新小说信息
     * @param novelId 小说ID
     * @return 小说信息实体
     */
    Books updateNovelInfo(Long novelId);
    
    /**
     * 根据小说名称搜索小说
     * @param keyword 搜索关键词
     * @return 搜索结果列表
     */
    List<Map<String, Object>> searchNovelsByName(String keyword);
}
