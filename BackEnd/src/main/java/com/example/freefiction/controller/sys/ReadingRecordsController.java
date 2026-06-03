package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.ReadingRecords;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.ReadingRecordsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readingRecords")
@RequiredArgsConstructor
public class ReadingRecordsController {
    private final ReadingRecordsService readingRecordsService;

    /**
     * 添加阅读记录
     * @param readingRecords 阅读记录对象
     * @return 是否成功
     */
    @PostMapping("/add")
    public Result<Boolean> addReadingRecord(@RequestBody ReadingRecords readingRecords) {
        ServiceResult<Boolean> serviceResult = readingRecordsService.addOrUpdateReadingRecord(readingRecords);
        return serviceResult.success() ?
                Result.success("添加成功",serviceResult.data()) :
                Result.failed(400,"添加失败");
    }

    /**
     * 删除阅读记录
     * @param id 阅读记录ID
     * @return 是否成功
     */
    @DeleteMapping("/delete")
    public Result<Boolean> deleteReadingRecord(@RequestParam Long  id) {
        ServiceResult<Boolean> serviceResult = readingRecordsService.deleteReadingRecord(id);
        return serviceResult.success() ?
                Result.success("删除成功",serviceResult.data()) :
                Result.failed(400,"删除失败");
    }

    /**
     * 删除用户所有阅读记录
     * @param userId 用户ID
     * @return 是否成功
     */
    @DeleteMapping("/delete/all")
    public Result<Boolean> deleteUserReadingRecord(@RequestParam Long userId) {
        ServiceResult<Boolean> serviceResult = readingRecordsService.deleteReadingRecordByUserId(userId);
        return serviceResult.success() ?
                Result.success("删除成功",serviceResult.data()) :
                Result.failed(400,"删除失败");
    }

    /**
     * 获取用户阅读记录
     * @param userId 用户ID
     * @return 阅读记录列表
     */
    @GetMapping("/fetch")
    public Result<List<ReadingRecords>> getReadingRecord(@RequestParam Long  userId) {
        ServiceResult<List<ReadingRecords>> serviceResult = readingRecordsService.getUserReadingRecords(userId);
        return serviceResult.success() ?
                Result.success("获取成功",serviceResult.data()) :
                Result.failed(400,"获取失败");
    }
}
