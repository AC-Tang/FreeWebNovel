package com.example.freefiction.service.sys;

import com.example.freefiction.entity.ReadingRecords;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.freefiction.handler.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【reading_records(阅读记录表)】的数据库操作Service
* @createDate 2025-11-24 10:48:47
*/
@Service
public interface ReadingRecordsService extends IService<ReadingRecords> {
    /**
     * 添加阅读记录
     * @param readingRecord 阅读记录
     * @return 添加结果
     */
    ServiceResult<Boolean> addOrUpdateReadingRecord(ReadingRecords readingRecord);

    /**
     * 获取用户阅读记录
     * @param userId 用户ID
     * @return 阅读记录列表
     */
    ServiceResult<List<ReadingRecords>> getUserReadingRecords(Long userId);

    /**
     * 删除阅读记录
     * @param readingRecordId 阅读记录ID
     * @return 删除结果
     */
    ServiceResult<Boolean> deleteReadingRecord(Long readingRecordId);

    /**
     * 删除用户阅读记录
     * @param userId 用户ID
     * @return 删除结果
     */
    ServiceResult<Boolean> deleteReadingRecordByUserId(Long userId);
}
