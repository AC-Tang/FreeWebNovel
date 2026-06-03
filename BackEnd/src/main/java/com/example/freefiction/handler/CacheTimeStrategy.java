package com.example.freefiction.handler;

public class CacheTimeStrategy {
    // 静态内容（很少变化）
    public static final long STATIC_CONTENT = 24 * 3600L;  // 24小时

    // 小说基本信息
    public static final long NOVEL_INFO = 2 * 3600L;       // 2小时

    // 章节内容（一旦发布很少修改）
    public static final long CHAPTER_CONTENT = 7 * 24 * 3600L;  // 7天

    // 小说目录（可能新增章节）
    public static final long NOVEL_CATALOG = 3600L;        // 1小时

    // 热门推荐（频繁更新）
    public static final long HOT_RECOMMEND = 300L;         // 5分钟

    // 搜索结果（用户搜索频繁）
    public static final long SEARCH_RESULT = 600L;         // 10分钟

    // 用户个人数据
    public static final long USER_DATA = 30 * 24 * 3600L;  // 30天

    // 统计计数（每天重置）
    public static final long STATISTICS = 24 * 3600L;      // 24小时

    public static final long VIEW_COUNT = 300L;
}
