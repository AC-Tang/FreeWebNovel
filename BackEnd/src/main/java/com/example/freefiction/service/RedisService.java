package com.example.freefiction.service;

import com.alibaba.fastjson2.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 基础操作 ====================

    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存并指定过期时间（秒）
     */
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 带过期时间的设置缓存
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取缓存并转换为指定类型
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (T) value : null;
    }

    public <T> T get(String key, TypeReference<T> typeReference) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        // 直接返回，让调用方处理类型转换
        // 或者使用 GenericJackson2JsonRedisSerializer 的反序列化
        return (T) value;
    }
    /**
     * 删除缓存
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 判断key是否存在
     */
    public boolean exists(String key) {
        Boolean hasKey = redisTemplate.hasKey(key);
        return hasKey != null && hasKey;
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeout) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, TimeUnit.SECONDS));
    }

    /**
     * 自增
     */
    public void increment(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    // ==================== 小说相关操作 ====================

    /**
     * 缓存小说基本信息
     */
    public void cacheNovelInfo(String novelId, Object novelInfo, long timeout) {
        String key = "novel:info:" + novelId;
        set(key, novelInfo, timeout);
    }

    /**
     * 获取小说基本信息
     */
    public Object getNovelInfo(String novelId) {
        String key = "novel:info:" + novelId;
        return get(key);
    }

    /**
     * 缓存小说章节内容
     */
    public void cacheChapterContent(String novelId, String chapterId, Object content, long timeout) {
        String key = "novel:chapter:" + novelId + ":" + chapterId;
        set(key, content, timeout);
    }

    /**
     * 获取小说章节内容
     */
    public Object getChapterContent(String novelId, String chapterId) {
        String key = "novel:chapter:" + novelId + ":" + chapterId;
        return get(key);
    }

    /**
     * 缓存小说目录
     */
    public void cacheNovelCatalog(String novelId, List<Object> catalog, long timeout) {
        String key = "novel:catalog:" + novelId;
        // 先删除旧的
        delete(key);
        // 批量添加
        if (catalog != null && !catalog.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(key, catalog.toArray());
        }
        if (timeout > 0) {
            expire(key, timeout);
        }
    }

    /**
     * 获取小说目录
     */
    public List<Object> getNovelCatalog(String novelId) {
        String key = "novel:catalog:" + novelId;
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return null;
        }
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存热门小说列表
     */
    public void cacheHotNovels(List<Object> novels, long timeout) {
        String key = "novel:hot";
        // 先删除旧的
        delete(key);
        // 批量添加
        if (novels != null && !novels.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(key, novels.toArray());
        }
        if (timeout > 0) {
            expire(key, timeout);
        }
    }

    /**
     * 获取热门小说列表
     */
    public List<Object> getHotNovels() {
        String key = "novel:hot";
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return null;
        }
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    // ==================== 阅读记录相关 ====================

    /**
     * 记录用户阅读历史
     */
    public void recordReadingHistory(String userId, String novelId, String chapterId) {
        String key = "user:reading:" + userId;
        // 使用哈希存储，novelId作为field，chapterId作为value
        redisTemplate.opsForHash().put(key, novelId, chapterId);
        // 设置30天过期
        expire(key, 30 * 24 * 3600);
    }

    /**
     * 获取用户阅读历史
     */
    public Map<Object, Object> getReadingHistory(String userId) {
        String key = "user:reading:" + userId;
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取用户对某本小说的阅读进度
     */
    public Object getReadingProgress(String userId, String novelId) {
        String key = "user:reading:" + userId;
        return redisTemplate.opsForHash().get(key, novelId);
    }

    // ==================== 搜索相关 ====================

    /**
     * 缓存搜索结果
     */
    public void cacheSearchResult(String keyword, List<Object> results, long timeout) {
        String key = "search:" + keyword;
        // 使用set存储，自动去重
        delete(key);
        if (results != null && !results.isEmpty()) {
            redisTemplate.opsForSet().add(key, results.toArray());
        }
        if (timeout > 0) {
            expire(key, timeout);
        }
    }

    /**
     * 获取缓存的搜索结果
     */
    public Set<Object> getSearchResult(String keyword) {
        String key = "search:" + keyword;
        return redisTemplate.opsForSet().members(key);
    }

    // ==================== 计数相关 ====================

    /**
     * 增加小说阅读量
     */
    public Long incrementNovelViews(String novelId) {
        String key = "novel:views:" + novelId;
        Long views = redisTemplate.opsForValue().increment(key);
        // 每天重置，可以设置24小时过期
        expire(key, 24 * 3600);
        return views;
    }

    /**
     * 获取小说阅读量
     */
    public Long getNovelViews(String novelId) {
        String key = "novel:views:" + novelId;
        Object views = get(key);
        return views != null ? ((Number) views).longValue() : 0L;
    }

    // ==================== 批量操作 ====================

    /**
     * 批量删除小说相关缓存
     */
    public void deleteNovelCache(String novelId) {
        // 删除小说信息
        delete("novel:info:" + novelId);
        // 删除小说目录
        delete("novel:catalog:" + novelId);
        // 删除所有章节缓存（需要模糊匹配）
        Set<String> keys = redisTemplate.keys("novel:chapter:" + novelId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 清除所有缓存（慎用）
     */
    public void clearAll() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}