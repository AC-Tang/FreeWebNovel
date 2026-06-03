package com.example.freefiction.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.freefiction.entity.Books;
import com.example.freefiction.service.*;
import com.example.freefiction.service.sys.BooksService;
import com.example.freefiction.service.sys.ChaptersService;
import com.example.freefiction.utils.download.IDMDownloader;
import com.example.freefiction.utils.novels.HttpUtil;
import com.example.freefiction.utils.novels.FileUtil;
import com.example.freefiction.entity.Chapters;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * 小说资源获取服务实现
 */
@Service
@RequiredArgsConstructor
public class NovelFetchServiceImpl implements NovelFetchService{

    @Value("${fiction.upload.path}")
    private String uploadPath;

    @Value("${fiction.upload.novel.path}")
    private String novelUploadPath;

    @Value("${fiction.download.url}")
    private String downloadUrlTemplate;

    @Value("${fiction.info.url}")
    private String infoUrlTemplate;

    @Value("${fiction.source.url}")
    private String sourceUrl;

    @Value("${fiction.newInfo.path}")
    private String newInfoPath;


    private final NovelInfoService novelInfoService;
    private final ChapterParseService chapterParseService;
    private final BooksService booksService;
    private final ChaptersService chaptersService;
    private final IDMDownloader idmDownloader;

    @Override
    public boolean fetchNovel(Long novelId,boolean isRefresh) {
        //非强制刷新，存在就不重写
        if(booksService.getOne(new QueryWrapper<Books>().eq("book_id", novelId))!=null&&!isRefresh){
            System.out.println("小说已下载");
            return false;
        }
        try {
            // 1. 爬取小说信息
            System.out.println("开始爬取小说信息，ID: " + novelId);
            if(novelInfoService.crawlNovelInfo(novelId)==null){
                System.out.println("爬取小说信息失败");
                return false;
            }
            // 2. 下载小说文件
            System.out.println("开始下载小说资源，ID: " + novelId);
            if(!downloadNovel(novelId)){
                System.out.println("下载小说资源失败");
                return false;
            }
            // 3. 解析章节
            System.out.println("开始解析章节，ID: " + novelId);
            int chapterCount = chapterParseService.parseChapters(novelId);
            System.out.println("章节解析完成，ID: " + novelId + "，共" + chapterCount + "章");

            // 4. 更新小说统计信息（章节数和总字数）
            System.out.println("更新小说统计信息，ID: " + novelId);
            booksService.updateNovelStats(novelId);
            return true;
        } catch (Exception e) {
            System.err.println("获取小说时发生未知错误，ID: " + novelId + "，错误信息: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int fetchNovels(Long startId, Long endId) {
        int successCount = 0;
        int totalCount = (int) (endId - startId + 1);
        long startTime = System.currentTimeMillis();

        System.out.println("=========================================");
        System.out.println("开始批量获取小说 - 安全单线程模式");
        System.out.println("ID范围: " + startId + " ~ " + endId);
        System.out.println("总数: " + totalCount + " 本");
        System.out.println("=========================================");

        for (long i = startId; i <= endId; i++) {
            int currentIndex = (int) (i - startId + 1);

            System.out.println("\n--- [" + currentIndex + "/" + totalCount + "] ---");
            System.out.println("正在获取小说 ID: " + i);

            try {
                boolean success = fetchNovel(i, false);

                if (success) {
                    successCount++;
                    System.out.println("✅ 获取成功: ID " + i);

                    // 成功后的随机延迟：1-3秒
                    randomDelay(1000, 3000);
                } else {
                    System.err.println("❌ 获取失败: ID " + i);

                    // 失败后的延迟稍长：3-5秒
                    randomDelay(3000, 5000);
                }

            } catch (Exception e) {
                System.err.println("⚠️  获取异常 ID " + i + ": " + e.getMessage());

                // 异常情况下，延迟更长时间：5-8秒
                randomDelay(5000, 8000);
            }

            // 每处理10本书输出一次进度
            if (currentIndex % 10 == 0) {
                printProgress(currentIndex, totalCount, successCount, startTime);
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;

        System.out.println("\n=========================================");
        System.out.println("批量获取完成！");
        System.out.println("总耗时: " + duration + " 秒");
        System.out.println("成功率: " + successCount + "/" + totalCount +
                " (" + String.format("%.1f", successCount * 100.0 / totalCount) + "%)");
        System.out.println("=========================================");

        return successCount;
    }

    @Override
    public int updateNovelChapters(Long novelId) {
        try {
            // 1. 获取小说详情页面，解析最新章节列表
            String detailUrl = infoUrlTemplate.replace("{id}", novelId.toString());
            Document doc = HttpUtil.getHtmlDocument(detailUrl, Charset.forName("GBK"));

            // 获取最新章节列表（只获取最新章节部分，不包括正文章节）
            Elements chapterLinks = doc.select("div.layout-col1 h2.layout-tit:contains(最新章节) + div.section-box ul.section-list li a");
            Element lastChapterElement= chapterLinks.first();
            if (chapterLinks.isEmpty()) {
                System.out.println("未找到最新章节列表，ID: " + novelId);
                return 0;
            }

            // 2. 获取本地最新章节
            String localLatestChapterTitle = booksService.getOne(new QueryWrapper<Books>().eq("book_id", novelId)).getLastChapterTitle();

            // 3. 确定需要下载的新章节
            List<Element> newChapters = new ArrayList<>();
            boolean foundLocalLatest = false;

            // 如果本地没有章节，则下载所有章节
            if (localLatestChapterTitle == null) {
                newChapters.addAll(chapterLinks);
                System.out.println("本地没有章节，将下载所有最新章节，ID: " + novelId);
            } else {
                // 查找本地最新章节在列表中的位置
                for (Element chapterLink : chapterLinks) {
                    String chapterTitle = chapterLink.text().trim();
                    if (chapterTitle.equals(localLatestChapterTitle)) {
                        foundLocalLatest = true;
                        break; // 找到了本地最新章节，停止添加
                    }
                    newChapters.add(chapterLink); // 添加到新章节列表
                }

                if (!foundLocalLatest) {
                    System.out.println("未在最新章节列表中找到本地最新章节，将下载所有章节，ID: " + novelId);
                    newChapters.clear();
                    newChapters.addAll(chapterLinks);
                }
            }

            if (newChapters.isEmpty()) {
                System.out.println("没有新章节需要更新，ID: " + novelId);
                return 0;
            }

            // 4. 获取本地最大章节文件序号
            int maxChapterFileId = getMaxChapterFileId(novelId);

            // 5. 下载新章节（倒序，从最新章节开始）
            int updatedCount = 0;
            for (int i = newChapters.size() - 1; i >= 0; i--) {
                org.jsoup.nodes.Element chapterLink = newChapters.get(i);
                String chapterTitle = chapterLink.text().trim();
                String chapterHref = chapterLink.attr("href");

                // 构建章节URL
                String chapterUrl = "https://www.qishuxia.com/book/" + novelId + "/" + chapterHref;

                // 下载章节内容
                String chapterContent = downloadChapterContent(chapterUrl);
                if (chapterContent != null && !chapterContent.isEmpty()) {
                    // 保存章节到文件
                    maxChapterFileId++;
                    String chapterFileName = maxChapterFileId + ".txt";
                    String chapterFilePath = saveChapterToFile(novelId, chapterFileName, chapterTitle, chapterContent);

                    // 保存章节到数据库
                    saveChapterToDatabase(novelId, maxChapterFileId, chapterTitle, chapterContent, chapterFilePath);

                    System.out.println("已更新章节: " + chapterTitle + " -> " + chapterFilePath);
                    updatedCount++;
                }

                // 章节之间添加延迟：1-2秒
                if (i > 0) {
                    randomDelay(1000, 2000);
                }
            }
            // 更新小说统计信息&最新章节信息
            booksService.updateNovelStats(novelId);
            booksService.update(new UpdateWrapper<Books>().set("last_chapter_title", lastChapterElement.text()).eq("book_id", novelId));
            return updatedCount;
        } catch (Exception e) {
            System.err.println("更新小说章节失败，ID: " + novelId + "，错误信息: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int updateNovelsChapters(Long startId, Long endId) {
        int successCount = 0;
        int totalCount = (int) (endId - startId + 1);
        long startTime = System.currentTimeMillis();

        System.out.println("=========================================");
        System.out.println("开始批量更新小说章节 - 安全单线程模式");
        System.out.println("ID范围: " + startId + " ~ " + endId);
        System.out.println("总数: " + totalCount + " 本");
        System.out.println("=========================================");

        for (long i = startId; i <= endId; i++) {
            int currentIndex = (int) (i - startId + 1);

            System.out.println("\n--- [" + currentIndex + "/" + totalCount + "] ---");
            System.out.println("正在更新小说章节 ID: " + i);

            try {
                int updatedChapters = updateNovelChapters(i);

                if (updatedChapters > 0) {
                    successCount++;
                    System.out.println("✅ 更新成功: ID " + i + "，更新章节数: " + updatedChapters);

                    // 成功后的随机延迟：2-4秒
                    randomDelay(2000, 4000);
                } else {
                    System.out.println("ℹ️  无需更新: ID " + i + "，没有新章节");

                    // 无需更新时的延迟：1-2秒
                    randomDelay(1000, 2000);
                }

            } catch (Exception e) {
                System.err.println("⚠️  更新异常 ID " + i + ": " + e.getMessage());

                // 异常情况下，延迟更长时间：5-8秒
                randomDelay(5000, 8000);
            }

            // 每处理10本书输出一次进度
            if (currentIndex % 10 == 0) {
                printProgress(currentIndex, totalCount, successCount, startTime);
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;

        System.out.println("\n=========================================");
        System.out.println("批量更新完成！");
        System.out.println("总耗时: " + duration + " 秒");
        System.out.println("成功更新: " + successCount + "/" + totalCount + " 本");
        System.out.println("=========================================");

        return successCount;
    }

    @Override
    public boolean refreshNovel(Long novelId) {
        try {
            // 1. 直接调用fetchNovel方法，该方法会重新下载小说、爬取信息并解析章节
            // fetchNovel方法已经实现了完整的获取逻辑
            return fetchNovel(novelId,true);
        } catch (Exception e) {
            System.err.println("刷新小说失败，ID: " + novelId + "，错误信息: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int refreshNovels(Long startId, Long endId) {
        int successCount = 0;
        int totalCount = (int) (endId - startId + 1);
        long startTime = System.currentTimeMillis();

        System.out.println("=========================================");
        System.out.println("开始批量刷新小说 - 安全单线程模式");
        System.out.println("ID范围: " + startId + " ~ " + endId);
        System.out.println("总数: " + totalCount + " 本");
        System.out.println("=========================================");

        for (long i = startId; i <= endId; i++) {
            int currentIndex = (int) (i - startId + 1);

            System.out.println("\n--- [" + currentIndex + "/" + totalCount + "] ---");
            System.out.println("正在刷新小说 ID: " + i);

            try {
                boolean refreshed = refreshNovel(i);

                if (refreshed) {
                    successCount++;
                    System.out.println("✅ 刷新成功: ID " + i);

                    // 成功后的随机延迟：2-4秒
                    randomDelay(2000, 4000);
                } else {
                    System.err.println("❌ 刷新失败: ID " + i);

                    // 失败后的延迟稍长：4-6秒
                    randomDelay(4000, 6000);
                }

            } catch (Exception e) {
                System.err.println("⚠️  刷新异常 ID " + i + ": " + e.getMessage());

                // 异常情况下，延迟更长时间：6-10秒
                randomDelay(6000, 10000);
            }

            // 每处理10本书输出一次进度
            if (currentIndex % 10 == 0) {
                printProgress(currentIndex, totalCount, successCount, startTime);
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;

        System.out.println("\n=========================================");
        System.out.println("批量刷新完成！");
        System.out.println("总耗时: " + duration + " 秒");
        System.out.println("成功率: " + successCount + "/" + totalCount +
                " (" + String.format("%.1f", successCount * 100.0 / totalCount) + "%)");
        System.out.println("=========================================");

        return successCount;
    }

    @Override
    public boolean refreshChapter(Long novelId){
        try {
            // 1. 下载小说文件
            System.out.println("开始下载小说资源，ID: " + novelId);
            if(!downloadNovel(novelId)){
                System.out.println("下载小说资源失败");
                return false;
            }
            // 2. 下载Html 文件
            System.out.println("开始下载Html文件，ID: " + novelId);
            //IDM全自动化获取
            String htmlFilePath = newInfoPath +"\\" +novelId + ".html";
            // 1. 将文件路径字符串转换为 Path 对象
            Path htmlFile = Paths.get(htmlFilePath);
            // 2. 检查文件是否存在,存在则先删除，再下载
            boolean exists = Files.exists(htmlFile);
            if (!exists) {
                try {
                    String infoUrl = infoUrlTemplate.replace("{id}", novelId.toString());
                    boolean isSuccess = idmDownloader.downloadAndWait(infoUrl, newInfoPath, novelId + ".html",5, 80000);
                    if (!isSuccess) {
                        System.out.println("下载网页信息资源失败");
                        return false;
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            // 3. 解析章节
            System.out.println("开始解析章节，ID: " + novelId);
            int chapterCount = chapterParseService.parseChapters(novelId);
            System.out.println("章节解析完成，ID: " + novelId + "，共" + chapterCount + "章");

            // 4. 更新小说统计信息（章节数和总字数）
            System.out.println("更新小说统计信息，ID: " + novelId);
            booksService.updateNovelStats(novelId);
            return true;
        } catch (Exception e) {
            System.err.println("获取小说时发生未知错误，ID: " + novelId + "，错误信息: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 智能随机延迟，避免被识别为爬虫
     */
    private void randomDelay(int minMs, int maxMs) {
        try {
            // 添加随机性，避免固定间隔
            int delay = minMs + (int)(Math.random() * (maxMs - minMs));

            // 额外的随机暂停，增加不可预测性（10%的概率）
            if (Math.random() < 0.1) {
                delay += 3000; // 额外增加3秒
                System.out.println("    [随机长暂停: " + delay + "ms]");
            }

            System.out.println("    [等待: " + delay + "ms]");
            Thread.sleep(delay);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("延迟被中断");
        }
    }

    /**
     * 打印进度信息
     */
    private void printProgress(int current, int total, int success, long startTime) {
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        double progress = current * 100.0 / total;

        // 预计剩余时间
        long estimatedTotalTime = (long) (elapsedTime * 100.0 / progress);
        long remainingTime = estimatedTotalTime - elapsedTime;

        System.out.println("\n📊 进度报告:");
        System.out.println("   进度: " + current + "/" + total +
                " (" + String.format("%.1f", progress) + "%)");
        System.out.println("   成功: " + success + " 本");
        System.out.println("   已用时间: " + elapsedTime + " 秒");

        if (remainingTime > 0) {
            System.out.println("   预计剩余: " + remainingTime + " 秒");
            System.out.println("   预计总耗时: " + estimatedTotalTime + " 秒");
        }
    }

    /**
     * 下载小说
     * @param novelId 小说ID
     * @return 是否下载成功
     */
    private boolean downloadNovel(Long novelId) {
        //IDM全自动化下载
        try {
            String downloadUrl = downloadUrlTemplate.replace("{id}", novelId.toString());
            String bookFileName = booksService.getBooksById(novelId).data().getTitle() + ".txt";
            // 2. 创建小说目录
            String novelDir = uploadPath + novelId + "/";
            Path novelDirPath = Paths.get(novelDir);
            if(Files.exists(novelDirPath)){
                Files.walk(novelDirPath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(file -> {
                            // 检查文件是否需要保留
                            if (!isFileToBeRetained(file,bookFileName)) {
                                file.delete(); // 删除不需要保留的文件
                            }
                        });
            }
//            else {
                FileUtil.createDirectory(novelDir);
                boolean downloaded = idmDownloader.downloadAndWait(downloadUrl, novelUploadPath + "\\" + novelId, bookFileName, 3, 10000);
                if (!downloaded) {
                    System.out.println("下载小说失败，ID: " + novelId);
                    return false;
                }
//            }
        } catch (IOException | InterruptedException e) {
            System.err.println("下载小说失败，ID: " + novelId + "，错误信息: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        //IDM全自动化下载
        //半自动化爬取小说
        String novelDir = uploadPath + novelId + "/";
        String novelFilePath = novelDir + booksService.getBooksById(novelId).data().getTitle() + ".txt";
        // 先将字节数组转换为字符串(网站使用GBK编码)
        byte[] novelContentBytes;
        try {
            novelContentBytes = Files.readAllBytes(Paths.get(novelFilePath));
        } catch (IOException e) {
            System.err.println("读取小说文件失败，路径: " + novelFilePath + "，错误信息: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        //半自动化爬取小说

        String novelContent = new String(novelContentBytes, Charset.forName("GBK"));
        // 再以UTF-8编码写入文件
        FileUtil.writeString(novelFilePath, novelContent, StandardCharsets.UTF_8);
        return true;
    }

    private boolean isFileToBeRetained(File file, String bookFileName) {
        return file.getName().equals(bookFileName);
    }

    /**
     * 获取本地最大章节文件ID
     * @param novelId 小说ID
     * @return 最大文件ID，如果没有则返回0
     */
    private int getMaxChapterFileId(Long novelId) {
        try {
            // 获取小说目录
            java.nio.file.Path novelDir = java.nio.file.Paths.get(uploadPath, String.valueOf(novelId));
            if (!java.nio.file.Files.exists(novelDir)) {
                return 0;
            }

            // 查找所有.txt文件，并获取最大ID
            int maxId = 0;
            java.io.File dir = novelDir.toFile();
            java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));

            if (files != null) {
                for (java.io.File file : files) {
                    String fileName = file.getName();
                    // 移除.txt扩展名
                    String idStr = fileName.substring(0, fileName.lastIndexOf('.'));
                    try {
                        int id = Integer.parseInt(idStr);
                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (NumberFormatException e) {
                        // 忽略非数字文件名
                    }
                }
            }

            return maxId;
        } catch (Exception e) {
            System.err.println("获取最大章节文件ID失败: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 下载章节内容
     * @param chapterUrl 章节URL
     * @return 章节内容
     */
    private String downloadChapterContent(String chapterUrl) {
        try {
            System.out.println("正在下载章节: " + chapterUrl);
            org.jsoup.nodes.Document doc = HttpUtil.getHtmlDocument(chapterUrl, java.nio.charset.Charset.forName("GBK"));

            // 获取章节标题
            org.jsoup.nodes.Element titleElement = doc.selectFirst("div.reader-main h1");
            String title = "";
            if (titleElement != null) {
                title = titleElement.text().trim();
            }

            // 获取章节内容
            org.jsoup.nodes.Element contentElement = doc.selectFirst("div.reader-main div#content");
            if (contentElement == null) {
                contentElement = doc.selectFirst("div.reader-main div.content");
            }
            if (contentElement == null) {
                contentElement = doc.selectFirst("div.reader-main");
            }

            if (contentElement != null) {
                // 移除广告等不需要的元素
                contentElement.select("script, style, .ad, .advertisement, h1").remove();

                // 获取HTML内容，然后转换HTML标签为文本格式
                String htmlContent = contentElement.html();

                // 将HTML标签转换为文本格式
                // 1. 将<br>和<br/>替换为换行符
                String content = htmlContent.replaceAll("(?i)<br\\s*/?>", "\\n");

                // 2. 将<p>和</p>替换为段落分隔
                content = content.replaceAll("(?i)</p>", "\\n\\n");
                content = content.replaceAll("(?i)<p>", "");

                // 3. 将<div>和</div>替换为换行
                content = content.replaceAll("(?i)</div>", "\\n");
                content = content.replaceAll("(?i)<div[^>]*>", "");

                // 4. 移除其他HTML标签
                content = content.replaceAll("<[^>]+>", "");

                // 5. 处理HTML实体
                content = content.replace("&nbsp;", " ");
                content = content.replace("&lt;", "<");
                content = content.replace("&gt;", ">");
                content = content.replace("&amp;", "&");
                content = content.replace("&quot;", "\"");
                content = content.replace("&#39;", "'");

                // 6. 清理多余的空白行
                content = content.replaceAll("\\n\\s*\\n\\s*\\n", "\\n\\n");

                // 7. 去除首尾空白
                content = content.trim();

                // 返回标题和内容
                if (!title.isEmpty()) {
                    return title + "\\n\\n" + content;
                } else {
                    return content;
                }
            } else {
                System.err.println("无法找到章节内容，URL: " + chapterUrl);
                return null;
            }
        } catch (Exception e) {
            System.err.println("下载章节内容失败: " + e.getMessage() + ", URL: " + chapterUrl);
            return null;
        }
    }

    /**
     * 保存章节到文件
     * @param novelId 小说ID
     * @param fileName 文件名
     * @param title 章节标题
     * @param content 章节内容
     * @return 文件路径
     */
    private String saveChapterToFile(Long novelId, String fileName, String title, String content) {
        try {
            // 创建小说目录
            java.nio.file.Path novelDir = java.nio.file.Paths.get(uploadPath, String.valueOf(novelId));
            if (!java.nio.file.Files.exists(novelDir)) {
                java.nio.file.Files.createDirectories(novelDir);
            }

            // 创建章节文件
            java.nio.file.Path chapterFile = novelDir.resolve(fileName);

            // 写入文件，使用UTF-8编码
            FileUtil.writeString(chapterFile.toString(), content, java.nio.charset.StandardCharsets.UTF_8);

            return chapterFile.toString();
        } catch (Exception e) {
            throw new RuntimeException("保存章节文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存章节到数据库
     * @param novelId 小说ID
     * @param chapterOrder 章节序号
     * @param title 章节标题
     * @param content 章节内容
     * @param filePath 文件路径
     */
    private void saveChapterToDatabase(Long novelId, int chapterOrder, String title, String content, String filePath) {
        try {
            // 检查章节是否已存在
            Chapters existingChapter = chaptersService.getOne(
                    new QueryWrapper<Chapters>()
                            .eq("book_id", novelId)
                            .eq("sort_order", chapterOrder)
            );

            Chapters chapter;
            if (existingChapter != null) {
                // 章节已存在，更新内容
                chapter = existingChapter;
            } else {
                // 章节不存在，创建新章节
                chapter = new Chapters();
                chapter.setBookId(novelId);
                chapter.setSortOrder(chapterOrder);
                chapter.setViewCount(0);
                chapter.setCreatedAt(new Date());
            }

            // 更新章节信息
            chapter.setTitle(title);
            chapter.setContent(content);
            chapter.setWordCount(content.length());
            chapter.setUpdatedAt(new Date());

            // 保存到数据库
            chaptersService.saveOrUpdate(chapter);
        } catch (Exception e) {
            throw new RuntimeException("保存章节到数据库失败: " + e.getMessage(), e);
        }
    }
}