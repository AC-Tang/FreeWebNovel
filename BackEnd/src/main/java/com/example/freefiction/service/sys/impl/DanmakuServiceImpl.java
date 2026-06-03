package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Chapters;
import com.example.freefiction.entity.Danmaku;
import com.example.freefiction.entity.Users;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.ChaptersService;
import com.example.freefiction.service.sys.DanmakuService;
import com.example.freefiction.mapper.DanmakuMapper;
import com.example.freefiction.service.sys.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【danmaku(弹幕表)】的数据库操作Service实现
* @createDate 2025-11-24 10:49:01
*/
@Service
@RequiredArgsConstructor
public class DanmakuServiceImpl extends ServiceImpl<DanmakuMapper, Danmaku>
    implements DanmakuService{
    private final ChaptersService chaptersService;
    private final UsersService usersService;
    /**
     * 添加弹幕
     * @param danmaku 弹幕对象
     * @return 添加结果
     */
    @Override
    public ServiceResult<Boolean> addDanmaku(Danmaku danmaku){
        danmaku.setChapterId(getRealChapterId(danmaku.getBookId(), danmaku.getChapterId()));
        return save(danmaku)?ServiceResult.ok(true):ServiceResult.fail("添加弹幕失败");
    }

    /**
     * 获取弹幕列表
     * @param bookId 小说ID
     * @param chapterId 章节ID
     * @return 弹幕列表
     */
    @Override
    public ServiceResult<List<Danmaku>> getDanmakuList(Long bookId, Long chapterId){
        List<Danmaku> danmakuList = list(new QueryWrapper<Danmaku>().eq("book_id", bookId).eq("chapter_id", getRealChapterId(bookId, chapterId)));
        danmakuList.forEach(danmaku -> {
            danmaku.setUsername(usersService.getOne(new QueryWrapper<Users>().eq("id", danmaku.getUserId())).getUsername());
            danmaku.setAvatar(usersService.getOne(new QueryWrapper<Users>().eq("id", danmaku.getUserId())).getAvatar());
        });
        return ServiceResult.ok(danmakuList);
    }

    /**
     * 删除弹幕
     * @param danmakuId 弹幕ID
     * @return 删除结果
     */
    @Override
    public ServiceResult<Boolean> deleteDanmaku(Long danmakuId){
        return removeById(danmakuId)?ServiceResult.ok(true):ServiceResult.fail("删除弹幕失败");
    }

    /**
     * 获取所有弹幕
     * @return 所有弹幕列表
     */
    @Override
    public ServiceResult<List<Danmaku>> getAllDanmaku(){
        List<Danmaku> danmakuList = list();
        return list()!=null?ServiceResult.ok(danmakuList):ServiceResult.fail("获取所有弹幕失败");
    }

    long getRealChapterId(Long bookId, Long chapterId){
        return Math.toIntExact(chaptersService.getOne(new QueryWrapper<Chapters>().eq("book_id", bookId).eq("sort_order", chapterId)).getId());
    }
}




