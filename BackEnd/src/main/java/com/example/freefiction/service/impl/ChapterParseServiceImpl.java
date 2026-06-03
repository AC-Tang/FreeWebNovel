package com.example.freefiction.service.impl;

import com.example.freefiction.entity.Chapters;
import com.example.freefiction.service.ChapterParseService;
import com.example.freefiction.service.sys.BooksService;
import com.example.freefiction.service.sys.ChaptersService;
import com.example.freefiction.utils.novels.FileUtil;
import com.example.freefiction.utils.novels.HtmlChapterParser;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 章节解析服务实现
 */
@Service
@RequiredArgsConstructor
public class ChapterParseServiceImpl implements ChapterParseService {

    private final BooksService booksService;
    @Value("${fiction.upload.path}")
    private String uploadPath;

    private final ChaptersService chaptersService;

    /**
     * 解析并保存小说章节
     * @param novelId 小说ID
     * @return 章节数量
     */
    @Override
    public int parseChapters(Long novelId) {
        // 1. 读取完整小说文件
        String novelDir = uploadPath + novelId + "/";
        String novelFilePath = novelDir + booksService.getBooksById(novelId).data().getTitle() + ".txt";
        
        // 使用UTF-8编码读取文件，解决中文乱码问题
        String novelContent = FileUtil.readString(novelFilePath, StandardCharsets.UTF_8);

        // 2. 解析并存储章节 - 使用HTML章节标题解析
        return parseAndStoreChaptersByHtml(novelId, novelContent);
    }

    /**
     * 使用HTML章节标题解析并存储章节
     * @param novelId 小说ID
     * @param novelContent 小说内容
     * @return 章节数量
     */
    public int parseAndStoreChaptersByHtml(Long novelId, String novelContent) {
        try {
            String novelDir = uploadPath + novelId + "/";
            // 1. 从HTML文件中提取章节标题列表
            List<String> chapterTitles = HtmlChapterParser.extractChapterTitles(novelId);
            //删除多余的章节（全更新时使用，平时注释）
            boolean result= chaptersService.remove(new QueryWrapper<Chapters>().eq("book_id", novelId).gt("sort_order",chapterTitles.size()));
            // 2. 如果没有找到任何章节，将整个内容作为一章
            if (chapterTitles.isEmpty()) {
                return saveSingleChapter(novelId, novelContent);
            }
            
            // 3. 根据HTML章节标题解析章节
            List<Chapters> chapters = HtmlChapterParser.parseChaptersByHtmlTitles(novelContent, chapterTitles);
            
            // 4. 保存章节
            int chapterCount = 0;
            for (int i = 0; i < chapters.size(); i++) {
                Chapters chapterInfo = chapters.get(i);
                String title = chapterInfo.getTitle();
                String content = chapterInfo.getContent();
                
                // 确保标题不为空且长度大于1
                if (title.trim().length() <= 1) {
                    title = "第" + (i + 1) + "章";
                }
                
                // 保存章节
                if (!content.isEmpty()) {
                    chapterCount++;
                    saveChapter(novelId, chapterCount, title, content, novelDir);
                }
            }
            
            return chapterCount;
        } catch (IOException e) {
            e.printStackTrace();
            // 如果HTML解析失败，回退到使用正则表达式解析
            return parseAndStoreChapters(novelId, novelContent);
        }
    }

    /**
     * 使用正则表达式解析并保存章节
     * @param novelId 小说ID
     * @param novelContent 小说内容
     * @return 章节数量
     */
    @Override
    public int parseAndStoreChapters(Long novelId, String novelContent) {
        // 如果HTML解析失败，回退到将整个内容作为单章保存
        return saveSingleChapter(novelId, novelContent);
    }
    
    /**
     * 将整个内容作为单章保存
     * @param novelId 小说ID
     * @param novelContent 小说内容
     * @return 章节数量
     */
    private int saveSingleChapter(Long novelId, String novelContent) {
        String novelDir = uploadPath + novelId + "/";
        String title = "全文";
        
        // 尝试从内容开头提取标题
        if (novelContent.length() > 50) {
            String startContent = novelContent.substring(0, 50);
            // 简单的标题提取，假设前20个字符包含标题
            title = startContent.trim();
        }
        
        saveChapter(novelId, 1, title, novelContent, novelDir);
        return 1;
    }
    
    /**
     * 保存单个章节
     * @param novelId 小说ID
     * @param chapterOrder 章节序号
     * @param title 章节标题
     * @param content 章节内容
     * @param novelDir 小说目录
     */
    private void saveChapter(Long novelId, int chapterOrder, String title, String content, String novelDir) {
        // 章节内容包含标题和正文
        String chapterContent = title + "\n\n" + content;
        
        // 生成章节文件名
        String chapterFileName = chapterOrder + ".txt";
        String chapterFilePath = novelDir + chapterFileName;
        
        // 保存章节文件，使用UTF-8编码
        FileUtil.writeString(chapterFilePath, chapterContent, StandardCharsets.UTF_8);
        
        // 保存章节信息到数据库
        saveChapterToDatabase(novelId, chapterOrder, title, chapterContent, chapterFilePath);
    }
    
    /**
     * 保存章节信息到数据库
     * @param novelId 小说ID
     * @param chapterOrder 章节序号
     * @param title 章节标题
     * @param content 章节内容
     * @param filePath 章节文件路径
     */
    private void saveChapterToDatabase(Long novelId, int chapterOrder, String title, String content, String filePath) {
        // 先查询是否已存在该章节
        Chapters existingChapter = chaptersService.getOne(
                new QueryWrapper<Chapters>()
                        .eq("book_id", novelId)
                        .eq("sort_order", chapterOrder)
        );
        
        Chapters chapter;
        if (existingChapter != null) {
            // 如果已存在，更新现有章节
            chapter = existingChapter;
        } else {
            // 如果不存在，创建新章节
            chapter = new Chapters();
            chapter.setBookId(novelId);
            chapter.setSortOrder(chapterOrder);
            chapter.setViewCount(0);
            chapter.setCreatedAt(new Date());
        }
        
        // 确保标题不超过数据库限制(200字符)
        String safeTitle = title;
        if (title != null && title.length() > 200) {
            safeTitle = title.substring(0, 197) + "...";
            System.out.println("章节标题过长，已截取: " + title + " -> " + safeTitle);
        }
        
        // 更新章节信息
        chapter.setTitle(safeTitle);
        chapter.setContent(content);
        chapter.setWordCount(content.length());
        chapter.setUpdatedAt(new Date());
        
        chaptersService.saveOrUpdate(chapter);
    }
}
