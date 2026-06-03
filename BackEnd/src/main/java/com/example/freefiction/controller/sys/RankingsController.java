package com.example.freefiction.controller.sys;

import com.example.freefiction.entity.Books;
import com.example.freefiction.handler.Result;
import com.example.freefiction.handler.ServiceResult;
import com.example.freefiction.service.sys.BooksService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingsController {
    private final BooksService booksService;
    private final ExecutorService rankExecutor = Executors.newFixedThreadPool(6);

    /**
     * 获取所有排行榜
     * @return 所有排行榜
     */
    @GetMapping("/all")
    public Result<Map<String, List<Books>>> getAllRankBooks() {
        Map<String, Future<ServiceResult<List<Books>>>> futures = new HashMap<>();
        futures.put("newRank", rankExecutor.submit(booksService::getNewRankBooks));
        futures.put("popularRank", rankExecutor.submit(booksService::getPopularRankBooks));
        futures.put("countRank", rankExecutor.submit(booksService::getCountRankBooks));
        futures.put("completedRank", rankExecutor.submit(booksService::getCompletedRankBooks));
        futures.put("recommendRank", rankExecutor.submit(booksService::getRecommendRankBooks));
        futures.put("likeRank", rankExecutor.submit(booksService::getLikeRankBooks));

        Map<String, List<Books>> result = new HashMap<>();

        for (Map.Entry<String, Future<ServiceResult<List<Books>>>> entry : futures.entrySet()) {
            try {
                ServiceResult<List<Books>> serviceResult = entry.getValue().get();
                if (serviceResult.success() && serviceResult.data() != null) {
                    result.put(entry.getKey(), serviceResult.data());
                } else {
                    result.put(entry.getKey(), Collections.emptyList());
                }
            } catch (Exception e) {
                result.put(entry.getKey(), Collections.emptyList());
            }
        }

        // 即使部分失败，也返回成功的数据
        return Result.success("获取排行榜成功（部分数据可能缺失）", result);
    }
}
