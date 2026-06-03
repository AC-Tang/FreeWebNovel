package com.example.freefiction.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.freefiction.entity.Books;
import com.example.freefiction.handler.CacheTimeStrategy;
import com.example.freefiction.handler.Result;
import com.example.freefiction.service.RedisService;
import com.example.freefiction.service.NovelFetchService;
import com.example.freefiction.service.NovelInfoService;
import com.example.freefiction.service.sys.BooksService;
import com.example.freefiction.utils.novels.HttpUtil;
import com.example.freefiction.utils.result.ResultUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 小说控制器，提供REST API接口
 */
@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
public class NovelController {
    private final NovelFetchService novelFetchService;
    private final NovelInfoService novelInfoService;
    private final RedisService redisService;
    private final RestTemplate restTemplate;
    private final BooksService booksService;

    /**
     * 批量获取小说
     *
     * @param startId 起始ID
     * @param endId   结束ID
     * @return 操作结果
     */
    @PostMapping("/crawl/batch")
    public Result<Integer> batchFetchNovels(@RequestParam Long startId, @RequestParam Long endId) throws IOException {
        int result = novelFetchService.fetchNovels(startId, endId);
        if(result!=0) {
            for (long i = startId; i <= endId; i++) {
                redisService.delete("Books:" + i);
                redisService.delete("crawl:info:" + i);
            }
            redisService.delete("Books:All");
            redisService.delete("Books:Status:0");
            redisService.delete("Books:Status:1");
            redisService.delete("Books:Status:2");
        }
        return ResultUtil.of(
                () -> result,
                "成功获取"+result+"本小说",
                500,
                "批量获取小说失败"
        );
    }

    /**
     * 获取单个小说
     *
     * @param novelId 小说ID
     * @return 操作结果
     */
    @PostMapping("/crawl/{novelId}")
    public Result<Boolean> fetchNovel(@PathVariable Long novelId) throws IOException {
        System.out.println("开始获取小说"+novelId);
        boolean result = novelFetchService.fetchNovel(novelId,false);
        if( result){
            redisService.delete("Books:"+novelId);
            redisService.delete("crawl:info:"+novelId);
            redisService.delete("Books:All");
            redisService.delete("Books:Status:0");
            redisService.delete("Books:Status:1");
            redisService.delete("Books:Status:2");
        }
        return ResultUtil.of(
                () -> result,
                "成功获取小说",
                500,
                "获取小说失败"
        );
    }

//    @RequestMapping("/crawl/manual")
//    public Result<List<Long>> fetchNovelsManual() {
//        // 定义总文件夹路径
//        String directoryPath = "Html/newNovels";
//
//        // 使用Paths获取路径对象
//        Path path = Paths.get(directoryPath);
//
//        // 检查路径是否存在
//        if (!Files.exists(path)) {
//            System.out.println("指定的目录不存在: " + directoryPath);
//            return Result.failed("指定的目录不存在");
//        }
//
//        // 获取子文件夹名称列表
//        List<String> folderNames = getSequenceNumbers(path);
//        List<Long> novelIds = new ArrayList<>();
//        String baseUrl="http://localhost:8080/api/novels/crawl/";
//        for (String folderName : folderNames) {
//            try {
//                System.out.println("开始获取小说 " + folderName);
//                Long novelId = Long.parseLong(folderName);
//                String url = baseUrl + novelId;
//                ResponseEntity<Result> response = restTemplate.postForEntity(url, null, Result.class);
//                if (response.getStatusCode().is2xxSuccessful()) {
//                    novelIds.add(novelId);
//                    System.out.println("成功获取小说 " + folderName);
//                } else {
//                    System.out.println("获取小说失败 " + folderName);
//                }
//            } catch (Exception e) {
//                System.out.println("获取小说失败 " + folderName + "，原因: " + e.getMessage());
//            }
//        }
//        return ResultUtil.of(
//                () -> novelIds,
//                "成功获取"+novelIds.size()+"本小说",
//                500,
//                "批量获取小说失败"
//        );
//    }
//    // 获取序号列表的方法
//    public static List<String> getSequenceNumbers(Path path) {
//        List<String> sequenceNumbers = new ArrayList<>();
//        try (Stream<Path> paths = Files.list(path)) {
//            sequenceNumbers = paths
//                    .filter(Files::isRegularFile) // 过滤出文件
//                    .map(Path::getFileName) // 获取文件名
//                    .map(Object::toString) // 转换为字符串
//                    .filter(fileName -> fileName.endsWith(".html")) // 过滤出HTML文件
//                    .map(fileName -> fileName.replace(".html", "")) // 去掉文件扩展名
//                    .collect(Collectors.toList()); // 收集到列表
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return sequenceNumbers;
//    }
//    /**
//     * 更新小说的最新章节
//     *
//     * @param novelId 小说ID
//     * @return 操作结果
//     */
//    @PutMapping("/update/{novelId}/chapters")
//    public Result<Integer> updateNovelChapters(@PathVariable Long novelId) throws IOException {
//        redisService.delete("Books:"+novelId);
//        redisService.delete("crawl:info:"+novelId);
//        redisService.delete("Books:All");
//        redisService.delete("Books:Status:0");
//        redisService.delete("Books:Status:1");
//        redisService.delete("Books:Status:2");
//        int result = novelFetchService.updateNovelChapters(novelId);
//        return ResultUtil.of(
//                () -> result,
//                "成功更新小说章节",
//                500,
//                "更新小说章节失败"
//        );
//    }
//
//    /**
//     * 批量更新小说的最新章节
//     *
//     * @param startId 起始ID
//     * @param endId   结束ID
//     * @return 操作结果
//     */
//    @PutMapping("/update/batch")
//    public Result<Integer> updateNovelsChapters(@RequestParam Long startId, @RequestParam Long endId) throws IOException {
//        int result = novelFetchService.updateNovelsChapters(startId, endId);
//        for (long i = startId; i <= endId; i++){
//            redisService.delete("Books:"+i);
//            redisService.delete("crawl:info:"+i);
//        }
//        redisService.delete("Books:All");
//        redisService.delete("Books:Status:0");
//        redisService.delete("Books:Status:1");
//        redisService.delete("Books:Status:2");
//        return ResultUtil.of(
//                () -> result,
//                "成功更新"+result+"小说章节",
//                500,
//                "更新小说章节失败"
//        );
//    }

    /**
     * 刷新小说信息和章节
     *
     * @param novelId 小说ID
     * @return 操作结果
     */
    @PutMapping("/refresh/{novelId}")
    public Result<Boolean> refreshNovel(@PathVariable Long novelId) throws IOException {
        redisService.delete("Books:"+novelId);
        redisService.delete("crawl:info:"+novelId);
        redisService.delete("Books:All");
        redisService.delete("Books:Status:0");
        redisService.delete("Books:Status:1");
        redisService.delete("Books:Status:2");
        boolean result = novelFetchService.refreshNovel(novelId);
        return ResultUtil.of(
                () -> result,
                "成功更新小说资源",
                500,
                "更新小说资源失败"
        );
    }

    /**
     * 批量刷新小说信息
     *
     * @param startId 起始ID
     * @param endId   结束ID
     * @return 操作结果
     */
    @PutMapping("/refresh/batch")
    public Result<Integer> refreshNovels(@RequestParam Long startId, @RequestParam Long endId) throws IOException {
        for (long i = startId; i <= endId; i++){
            redisService.delete("Books:"+i);
            redisService.delete("crawl:info:"+i);
        }
        redisService.delete("Books:All");
        redisService.delete("Books:Status:0");
        redisService.delete("Books:Status:1");
        redisService.delete("Books:Status:2");
        int result = novelFetchService.refreshNovels(startId, endId);
        return ResultUtil.of(
                () -> result,
                "成功更新"+result+"本小说",
                500,
                "更新小说失败"
        );
    }

    /**
     * 更新小说的books表信息
     *
     * @param novelId 小说ID
     * @return 操作结果
     */
    @PutMapping("/update/info/{novelId}")
    public Result<Books> updateNovelInfo(@PathVariable Long novelId) throws IOException {
        redisService.delete("Books:"+novelId);
        redisService.delete("crawl:info:"+novelId);
        redisService.delete("Books:All");
        redisService.delete("Books:Status:0");
        redisService.delete("Books:Status:1");
        redisService.delete("Books:Status:2");
        Books result = novelInfoService.updateNovelInfo(novelId);
        return ResultUtil.of(
                () -> result,
                "成功更新小说信息",
                500,
                "更新小说信息失败"
        );
    }
    
    /**
     * 根据小说名称搜索小说
     * @param keyword 搜索关键词
     * @return 搜索结果列表
     */
    @GetMapping("/crawl/search")
    public Result<List<Map<String, Object>>> searchNovels(@RequestParam String keyword) throws IOException {
        String key="crawl:search:"+keyword;
        List<Map<String, Object>> cacheBooks = (List<Map<String, Object>>)redisService.get(key, List.class);
        if(cacheBooks == null) {
            cacheBooks = novelInfoService.searchNovelsByName(keyword);
            if(cacheBooks.isEmpty()){
                return Result.notFound("未找到相关小说");
            }
            redisService.set(key, cacheBooks, CacheTimeStrategy.SEARCH_RESULT);
        }
        return Result.success("成功搜索小说", cacheBooks);
    }

    @GetMapping("/crawl/info/{novelId}")
    public Result<Books> getNovelInfo(@PathVariable Long novelId){
        String key="crawl:info:"+novelId;
        Books cacheBook = (Books)redisService.get(key);
        if(cacheBook == null) {
            cacheBook = novelInfoService.crawlNovelInfo(novelId);
            if(cacheBook == null){
                return Result.notFound("未找到该小说");
            }
            redisService.set("Books:"+novelId, cacheBook, CacheTimeStrategy.NOVEL_INFO);
        }
        return Result.success("成功获取小说信息", cacheBook);
    }

    @PostMapping("/refresh/chapter/{novelId}")
    public Result<String> refreshChapter(@PathVariable Long novelId){
        redisService.delete("Books:"+novelId);
        redisService.delete("crawl:info:"+novelId);
        redisService.delete("Books:All");
        redisService.delete("Books:Status:0");
        redisService.delete("Books:Status:1");
        redisService.delete("Books:Status:2");
        boolean result = novelFetchService.refreshChapter(novelId);
        return result?Result.success("成功更新小说章节"):Result.failed("更新小说章节失败");
    }

    @RequestMapping("/refresh/chapter/auto")
    public Result<List<Long>> refreshAuto() {
        List<Long> novelIds = booksService.list(new QueryWrapper<Books>().gt("book_id",1000)).stream().map(Books::getBookId).toList();
        List<Long> successIds = new ArrayList<>();
        String baseUrl="http://localhost:8080/api/novels/refresh/chapter/";
        for (long id : novelIds) {
            try {
                System.out.println("正在重写小说章节 " + id);
                String url = baseUrl + id;
                ResponseEntity<Result> response = restTemplate.postForEntity(url, null, Result.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    successIds.add(id);
                    System.out.println("成功重构小说章节 " + id);
                } else {
                    System.out.println("重构小说章节失败 " + id);
                }
            } catch (Exception e) {
                System.out.println("重构小说章节失败 " + id + "，原因: " + e.getMessage());
            }
        }
        return ResultUtil.of(
                () -> successIds,
                "成功获取"+successIds.size()+"本小说",
                500,
                "批量获取小说失败"
        );
    }
}

