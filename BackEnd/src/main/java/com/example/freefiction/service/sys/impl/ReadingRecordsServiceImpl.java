package com.example.freefiction.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.freefiction.entity.ReadingRecords;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.BooksService;
import com.example.freefiction.service.sys.ChaptersService;
import com.example.freefiction.service.sys.ReadingRecordsService;
import com.example.freefiction.mapper.ReadingRecordsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Tjianwei
* @description 针对表【reading_records(阅读记录表)】的数据库操作Service实现
* @createDate 2025-11-24 10:48:47
*/
@Service
@RequiredArgsConstructor
public class ReadingRecordsServiceImpl extends ServiceImpl<ReadingRecordsMapper, ReadingRecords>
    implements ReadingRecordsService{
    private final BooksService booksService;
    private final ChaptersService chaptersService;
    /**
     * 添加阅读记录
     * @param readingRecord 阅读记录
     * @return 添加结果
     */
    @Override
    public ServiceResult<Boolean> addOrUpdateReadingRecord(ReadingRecords readingRecord){
        ReadingRecords db=this.getOne(new QueryWrapper<ReadingRecords>().eq("user_id", readingRecord.getUserId()).eq("book_id", readingRecord.getBookId()));
        boolean result= (db != null);
        if(result)
            result=this.update(new UpdateWrapper<ReadingRecords>().eq("user_id", readingRecord.getUserId()).eq("book_id", readingRecord.getBookId()).set("chapter_id", readingRecord.getChapterId()));
        else
            result=this.save(readingRecord);
        return result ? ServiceResult.ok(true) : ServiceResult.fail("添加阅读记录失败");
    }

    /**
     * 获取用户阅读记录
     * @param userId 用户ID
     * @return 阅读记录列表
     */
    @Override
    public ServiceResult<List<ReadingRecords>> getUserReadingRecords(Long userId){
        List<ReadingRecords> readingRecords = this.list(new QueryWrapper<ReadingRecords>().eq("user_id", userId));
        readingRecords.forEach(readingRecord -> {
            readingRecord.setBookName(booksService.getBooksById(readingRecord.getBookId()).data().getTitle());
            readingRecord.setChapterName(chaptersService.getChapter(readingRecord.getBookId(), Math.toIntExact(readingRecord.getChapterId())).data().getTitle());
            readingRecord.setAuthorName(booksService.getBooksById(readingRecord.getBookId()).data().getAuthor());
            readingRecord.setBookCover(booksService.getBooksById(readingRecord.getBookId()).data().getCover());
            readingRecord.setLastUpdateTime(booksService.getBooksById(readingRecord.getBookId()).data().getLastChapterTime());
            readingRecord.setStatusName(booksService.getBooksById(readingRecord.getBookId()).data().getStatusName());
            readingRecord.setCategoryName(booksService.getBooksById(readingRecord.getBookId()).data().getCategoryName());
        });
        return ServiceResult.ok(readingRecords);
    }

    /**
     * 删除阅读记录
     * @param readingRecordId 阅读记录ID
     * @return 删除结果
     */
    @Override
    public ServiceResult<Boolean> deleteReadingRecord(Long readingRecordId){
        boolean result = this.removeById(readingRecordId);
        return result ? ServiceResult.ok(true) : ServiceResult.fail("删除阅读记录失败");
    }

    /**
     * 删除用户阅读记录
     * @param userId 用户ID
     * @return 删除结果
     */
    @Override
    public ServiceResult<Boolean> deleteReadingRecordByUserId(Long userId){
        boolean result = this.remove(new QueryWrapper<ReadingRecords>().eq("user_id", userId));
        return result ? ServiceResult.ok(true) : ServiceResult.fail("删除用户阅读记录失败");
    }
}




