package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.CheckIns;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.CheckInsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkIns")
@RequiredArgsConstructor
public class CheckInsController {
    private final CheckInsService checkInsService;

    /**
     * 添加打卡
     * @param checkIns 打卡对象
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<Boolean> addCheckIn(@RequestBody CheckIns checkIns) {
        ServiceResult<Boolean> serviceResult = checkInsService.addCheckIns(checkIns);
        return serviceResult.success() ?
                Result.success("成功打卡",serviceResult.data()) :
                Result.failed(404,"打卡失败");
    }

    /**
     * 获取用户打卡信息
     * @param userId 用户ID
     * @param bookId 小说ID
     * @param chapterId 章节ID
     * @return 打卡对象
     */
    @GetMapping("/fetch")
    public Result<CheckIns> getCheckIn( @RequestParam Long userId,@RequestParam Long bookId, @RequestParam Long chapterId) {
        ServiceResult<CheckIns> serviceResult = checkInsService.getCheckInsByInfo(userId, bookId, chapterId);
        return serviceResult.success() ?
                Result.success("成功获取打卡",serviceResult.data()) :
                Result.success("暂无打卡记录", null);
    }

    /**
     * 判断用户是否已打卡
     * @param userId 用户ID
     * @param bookId 小说ID
     * @param chapterId 章节ID
     * @return 是否已打卡
     */
    @GetMapping("/check")
    public Result<Boolean> exists(@RequestParam Long userId,@RequestParam Long bookId, @RequestParam Long chapterId) {
        ServiceResult<Boolean> serviceResult = checkInsService.exists(userId, bookId, chapterId);
        return serviceResult.success() ?
                Result.success("已打卡",true) :
                Result.success("未打卡",false);
    }

    /**
     * 获取某个用户的所有打卡记录
     * @param userId 用户ID
     * @return 打卡列表
     */
    @GetMapping("/fetch/list/user")
    public Result<List<CheckIns>> getCheckInsListByUserId(@RequestParam Long userId) {
        ServiceResult<List<CheckIns>> serviceResult = checkInsService.getCheckInsListByUserId(userId);
        return serviceResult.success() ?
                Result.success("成功获取打卡列表",serviceResult.data()) :
                Result.failed(404,"获取打卡列表失败");
    }

    /**
     * 获取整本书籍打卡列表
     * @param bookId 小说ID
     * @return 打卡列表
     */
    @GetMapping("/fetch/list/book")
    public Result<List<CheckIns>> getCheckInsListByBookId(@RequestParam Long bookId) {
        ServiceResult<List<CheckIns>> serviceResult = checkInsService.getCheckInsListByBookId(bookId);
        return serviceResult.success() ?
                Result.success("成功获取打卡列表",serviceResult.data()) :
                Result.failed(404,"获取打卡列表失败");
    }

    /**
     * 获取整章打卡列表
     * @param chapterId 章节ID
     * @return 打卡列表
     */
    @GetMapping("/fetch/list/chapter")
    public Result<List<CheckIns>> getCheckInsListByChapterId(@RequestParam Long chapterId) {
        ServiceResult<List<CheckIns>> serviceResult = checkInsService.getCheckInsListByChapterId(chapterId);
        return serviceResult.success() ?
                Result.success("成功获取打卡列表",serviceResult.data()) :
                Result.failed(404,"获取打卡列表失败");
    }
}
