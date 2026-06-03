package com.example.freefiction.service;

import org.springframework.stereotype.Service;

/**
 * 小说资源获取服务，用于下载小说资源
 */
@Service
public interface NovelFetchService {

    /**
     * 获取单个小说资源
     * @param novelId 小说ID
     * @return 是否获取成功
     */
    boolean fetchNovel(Long novelId,boolean isRefresh);

    /**
     * 批量获取小说资源
     * @param startId 起始ID
     * @param endId 结束ID
     * @return 获取成功的小说数量
     */
    int fetchNovels(Long startId, Long endId);

    /**
     * 更新小说的最新章节
     * @param novelId 小说ID
     * @return 更新的章节数量
     */
    int updateNovelChapters(Long novelId);

    /**
     * 批量更新小说的最新章节
     * @param startId 起始ID
     * @param endId 结束ID
     * @return 更新的小说数量
     */
    int updateNovelsChapters(Long startId, Long endId);

    /**
     * 强制重新获取小说信息并全部复写更新
     * @param novelId 小说ID
     * @return 是否更新成功
     */
    boolean refreshNovel(Long novelId);

    /**
     * 批量强制重新获取小说信息并全部复写更新
     * @param startId 起始ID
     * @param endId 结束ID
     * @return 更新的小说数量
     */
    int refreshNovels(Long startId, Long endId);

    /**
     * 批量强制重新获取小说资源，并重新分章
     * @param novelId 小说ID
     * @return 更新的小说数量
     */
    boolean refreshChapter(Long novelId);
}
