package com.example.freefiction.service.sys;

import com.example.freefiction.entity.CheckIns;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【check_ins(打卡表)】的数据库操作Service
* @createDate 2025-11-24 10:49:06
*/
@Service
public interface CheckInsService extends IService<CheckIns> {
    /**
     * 添加打卡
     * @param checkIns 打卡对象
     * @return 添加结果
     */
    ServiceResult<Boolean> addCheckIns(CheckIns checkIns);

    /**
     * 获取打卡列表
     * @param userId 用户ID
     * @return 打卡列表
     */
    ServiceResult<List<CheckIns>> getCheckInsListByUserId(Long userId);

    /**
     * 获取打卡列表
     * @param bookId 小说ID
     * @return 打卡列表
     */
    ServiceResult<List<CheckIns>> getCheckInsListByBookId(Long bookId);

    /**
     * 获取打卡列表
     * @param chapterId 章节ID
     * @return 打卡列表
     */
    ServiceResult<List<CheckIns>> getCheckInsListByChapterId(Long chapterId);

    /**
     * 获取用户打卡信息
     * @param userId 用户ID
     * @param bookId 小说ID
     * @param chapterId 章节ID
     * @return 打卡信息
     */
    ServiceResult<CheckIns> getCheckInsByInfo(Long userId, Long bookId, Long chapterId);

    /**
     * 判断用户是否已打卡
     * @param userId 用户ID
     * @param bookId 小说ID
     * @param chapterId 章节ID
     * @return 是否已打卡
     */
    ServiceResult<Boolean> exists(Long userId, Long bookId, Long chapterId);
}
