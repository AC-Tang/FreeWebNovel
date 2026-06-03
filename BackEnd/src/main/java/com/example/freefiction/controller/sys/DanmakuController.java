package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.Danmaku;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.DanmakuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/danmakus")
@RequiredArgsConstructor
public class DanmakuController {
    private final DanmakuService danmakuService;

    /**
     * 添加弹幕
     * @param danmaku 弹幕对象
     * @return 添加结果
     */
    @PostMapping("/add")
    public Result<Boolean> addDanmaku(@RequestBody Danmaku danmaku) {
        ServiceResult<Boolean> serviceResult = danmakuService.addDanmaku(danmaku);
        return serviceResult.success() ?
                Result.success("成功添加弹幕",serviceResult.data()) :
                Result.failed(404,"添加弹幕失败");
    }

    /**
     * 获取某本书的某个章节的弹幕列表
     * @param bookId 小说ID
     * @param chapterId 章节ID
     * @return 弹幕列表
     */
    @GetMapping("/fetch/list")
    public Result<List<Danmaku>> getDanmakuList(@RequestParam Long bookId, @RequestParam Long chapterId) {
        ServiceResult<List<Danmaku>> serviceResult = danmakuService.getDanmakuList(bookId, chapterId);
        return serviceResult.success() ?
                Result.success("成功获取弹幕列表",serviceResult.data()) :
                Result.failed(404,"获取弹幕列表失败");
    }

    /**
     * 删除弹幕
     * @param id 弹幕ID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public Result<Boolean> deleteDanmaku(@RequestParam Long id) {
        ServiceResult<Boolean> serviceResult = danmakuService.deleteDanmaku(id);
        return serviceResult.success() ?
                Result.success("成功删除弹幕",serviceResult.data()) :
                Result.failed(404,"删除弹幕失败");
    }

    /**
     * 获取所有弹幕
     * @return 所有弹幕列表
     */
    @GetMapping("/fetch/all")
    public Result<List<Danmaku>> getAllDanmaku() {
        ServiceResult<List<Danmaku>> serviceResult = danmakuService.getAllDanmaku();
        return serviceResult.success() ?
                Result.success("成功获取所有弹幕",serviceResult.data()) :
                Result.failed(404,"获取所有弹幕失败");
    }
}
