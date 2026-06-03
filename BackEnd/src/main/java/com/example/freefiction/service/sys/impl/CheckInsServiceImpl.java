package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.Chapters;
import com.example.freefiction.entity.CheckIns;
import com.example.freefiction.entity.Users;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.ChaptersService;
import com.example.freefiction.service.sys.CheckInsService;
import com.example.freefiction.mapper.CheckInsMapper;
import com.example.freefiction.service.sys.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【check_ins(打卡表)】的数据库操作Service实现
* @createDate 2025-11-24 10:49:06
*/
@Service
@RequiredArgsConstructor
public class CheckInsServiceImpl extends ServiceImpl<CheckInsMapper, CheckIns>
    implements CheckInsService{
    private final ChaptersService chaptersService;
    private final UsersService usersService;
    /**
     * 添加打卡
     * @param checkIns 打卡对象
     * @return 添加结果
     */
    @Override
    public ServiceResult<Boolean> addCheckIns(CheckIns checkIns){
        checkIns.setChapterId((long) getRealChapterId(checkIns.getBookId(), checkIns.getChapterId()));
        System.out.println("添加打卡："+checkIns);
        checkIns.setRanks(this.count(new QueryWrapper<CheckIns>().eq("book_id", checkIns.getBookId()).eq("chapter_id",checkIns.getChapterId() ))+1);
        return save(checkIns)?ServiceResult.ok(true):ServiceResult.fail("添加打卡失败");
    }

    /**
     * 获取打卡列表
     * @param userId 用户ID
     * @return 打卡列表
     */
    @Override
    public ServiceResult<List<CheckIns>> getCheckInsListByUserId(Long userId){
        List<CheckIns> checkInsList = list(new QueryWrapper<CheckIns>().eq("user_id", userId));
        return checkInsList!=null?ServiceResult.ok(checkInsList):ServiceResult.fail("获取打卡列表失败");
    }

    /**
     * 获取打卡列表
     * @param bookId 小说ID
     * @return 打卡列表
     */
    @Override
    public ServiceResult<List<CheckIns>> getCheckInsListByBookId(Long bookId){
        List<CheckIns> checkInsList = list(new QueryWrapper<CheckIns>().eq("book_id", bookId));
        return checkInsList!=null?ServiceResult.ok(checkInsList):ServiceResult.fail("获取打卡列表失败");
    }

    /**
     * 获取打卡列表
     * @param chapterId 章节ID
     * @return 打卡列表
     */
    @Override
    public ServiceResult<List<CheckIns>> getCheckInsListByChapterId(Long chapterId){
        List<CheckIns> checkInsList = list(new QueryWrapper<CheckIns>().eq("chapter_id", chapterId));
        return checkInsList!=null?ServiceResult.ok(checkInsList):ServiceResult.fail("获取打卡列表失败");
    }

    /**
     * 获取用户打卡信息
     * @param userId 用户ID
     * @param bookId 小说ID
     * @param chapterId 章节ID
     * @return 打卡信息
     */
    @Override
    public ServiceResult<CheckIns> getCheckInsByInfo(Long userId, Long bookId, Long chapterId){
        CheckIns checkIns = getOne(new QueryWrapper<CheckIns>().eq("user_id", userId).eq("book_id", bookId).eq("chapter_id", getRealChapterId(bookId, chapterId)));
        checkIns.setAvatar(usersService.getOne(new QueryWrapper<Users>().eq("id", checkIns.getUserId())).getAvatar());
        return ServiceResult.ok(checkIns);
    }

    /**
     * 判断用户是否已打卡
     * @param userId 用户ID
     * @param bookId 小说ID
     * @param chapterId 章节ID
     * @return 是否已打卡
     */
    @Override
    public ServiceResult<Boolean> exists(Long userId, Long bookId, Long chapterId){
        boolean exists = getOne(new QueryWrapper<CheckIns>().eq("user_id", userId).eq("book_id", bookId).eq("chapter_id", getRealChapterId(bookId, chapterId)))!=null;
        System.out.println("是否存在打卡："+ exists);
        return exists?ServiceResult.ok(true):ServiceResult.fail("获取打卡信息失败");
    }

    int getRealChapterId(Long bookId, Long chapterId){
        return Math.toIntExact(chaptersService.getOne(new QueryWrapper<Chapters>().eq("book_id", bookId).eq("sort_order", chapterId)).getId());
    }
}




