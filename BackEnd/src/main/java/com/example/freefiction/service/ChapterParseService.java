package com.example.freefiction.service;

import org.springframework.stereotype.Service;

/**
 * 章节解析服务，用于解析小说章节并存储
 */
@Service
public interface ChapterParseService {

    /**
     * 解析小说章节
     * @param novelId 小说ID
     * @return 解析出的章节数量
     */
    int parseChapters(Long novelId);

    /**
     * 解析并存储章节
     * @param novelId 小说ID
     * @param novelContent 小说内容
     * @return 存储的章节数量
     */
    int parseAndStoreChapters(Long novelId, String novelContent);
}
