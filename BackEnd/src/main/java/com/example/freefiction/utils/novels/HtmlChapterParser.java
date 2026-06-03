package com.example.freefiction.utils.novels;

import com.example.freefiction.entity.Chapters;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * HTML章节解析器，用于从HTML文件中提取章节标题并在TXT文件中拆分章节
 */
public class HtmlChapterParser {
    private static final String newInfoPath="D:\\\\JavaProjectSpace\\\\FreeFiction\\\\Html\\\\novels";
    /**
     * 从HTML文件中提取章节标题列表
     * @param novelId 小说ID
     * @return 章节标题列表
     */
    public static List<String> extractChapterTitles(Long novelId) throws IOException {
        String htmlFilePath = newInfoPath +"\\" +novelId + ".html";
        System.out.println("HTML文件路径："+htmlFilePath);
        // 1. 将文件路径字符串转换为 Path 对象
        List<String> chapterTitles = new ArrayList<>();

        Document doc;
        try {
            doc = Jsoup.parse(new File(htmlFilePath), "GBK");
        } catch (IOException e) {
            System.err.println("读取HTML文件失败，路径: " + htmlFilePath + "，错误信息: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
        // 查找章节列表
        Elements chapterLinks = doc.select("#section-list li a");
        
        // 提取章节标题
        for (Element link : chapterLinks) {
            String title = link.text().trim();
            if (!title.isEmpty()) {
                chapterTitles.add(title);
            }
        }
        
        return chapterTitles;
    }
    
    /**
     * 根据章节标题列表在TXT文件中拆分章节
     * @param novelContent 小说完整内容
     * @param chapterTitles 章节标题列表
     * @return 章节信息列表，每个元素包含章节标题和内容
     */
    public static List<Chapters> parseChaptersByHtmlTitles(String novelContent, List<String> chapterTitles) {
        List<Chapters> chapters = new ArrayList<>();

        // 处理每个章节
        for (int i = 0; i < chapterTitles.size(); i++) {
            String currentTitle = chapterTitles.get(i);
            int startPos = novelContent.indexOf(currentTitle);

            if (startPos == -1) {
                System.out.println("未找到章节标题: " + currentTitle);
                continue; // 如果找不到当前章节标题，跳过
            }

            // 确定章节结束位置
            int endPos;
            if (i + 1 < chapterTitles.size()) {
                // 下一个章节标题的位置
                String nextTitle = chapterTitles.get(i + 1);
                endPos = novelContent.indexOf(nextTitle, startPos + currentTitle.length());
                if (endPos == -1) {
                    endPos = novelContent.length(); // 如果找不到下一个章节标题，取到文件末尾
                }
            } else {
                // 最后一章，取到文件末尾
                endPos = novelContent.length();
            }

            // 提取章节内容（包括当前章节标题）
            String chapterContent = novelContent.substring(startPos, endPos).trim();
            System.out.println("章节标题：" + chapterContent);
            chapters.add(new Chapters(currentTitle, chapterContent));
        }
        return chapters;
    }
    
    /**
     * 根据小说ID获取HTML文件路径
     * @param htmlBaseDir HTML文件基础目录
     * @param novelId 小说ID
     * @return HTML文件路径
     */
    public static String getHtmlFilePath(String htmlBaseDir, Long novelId) {
        return htmlBaseDir + novelId + ".html";
    }
}