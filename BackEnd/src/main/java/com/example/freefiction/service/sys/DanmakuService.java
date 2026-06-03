package com.example.freefiction.service.sys;

import com.example.freefiction.entity.Danmaku;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【danmaku(弹幕表)】的数据库操作Service
* @createDate 2025-11-24 10:49:01
*/
@Service
public interface DanmakuService extends IService<Danmaku> {
    /**
     * 添加弹幕
     * @param danmaku 弹幕对象
     * @return 添加结果
     */
    ServiceResult<Boolean> addDanmaku(Danmaku danmaku);

    /**
     * 获取弹幕列表
     * @param bookId 小说ID
     * @param chapterId 章节ID
     * @return 弹幕列表
     */
    ServiceResult<List<Danmaku>> getDanmakuList(Long bookId,Long chapterId);

    /**
     * 删除弹幕
     * @param danmakuId 弹幕ID
     * @return 删除结果
     */
    ServiceResult<Boolean> deleteDanmaku(Long danmakuId);

    /**
     * 获取所有弹幕
     * @return 所有弹幕列表
     */
    ServiceResult<List<Danmaku>> getAllDanmaku();
}
