package com.example.freefiction.controller.sys;

import com.example.freefiction.domain.ChapterInfo;
import com.example.freefiction.entity.Chapters;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.ChaptersService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chapters")
public class ChaptersController {
    private final ChaptersService chaptersService;

    /**
     * 获取小说的章节列表
     * @param novelId 小说ID
     * @return 章节列表
     */
    @GetMapping("/fetch/{novelId}")
    public Result<List<ChapterInfo>> getChapters(@PathVariable Long novelId) {
        ServiceResult<List<ChapterInfo>> serviceResult = chaptersService.getChaptersList(novelId);
        return serviceResult.success() ?
                Result.success("成功获取小说章节列表",serviceResult.data()) :
                Result.failed(404,"小说不存在或章节列表为空");
    }

    /**
     * 获取单个章节内容
     * @param novelId 小说ID
     * @param chapterOrder 章节顺序
     * @return 章节内容
     */
    @GetMapping("/fetch/{novelId}/{chapterOrder}")
    public Result<Chapters> getChapter(@PathVariable Long novelId, @PathVariable Integer chapterOrder) {
         ServiceResult<Chapters> serviceResult = chaptersService.getChapter(novelId, chapterOrder);
         return serviceResult.success() ?
                 Result.success("成功获取章节内容",serviceResult.data()) :
                 Result.failed(404,"章节不存在");
    }
}
