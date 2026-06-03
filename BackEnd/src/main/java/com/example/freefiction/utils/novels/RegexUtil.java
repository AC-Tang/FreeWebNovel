package com.example.freefiction.utils.novels;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具类，用于章节解析，识别不同格式的章节标题
 */
public class RegexUtil {

    // 章节标题正则表达式
    // 只支持两种格式：第三章 某某某、第3章 某某某
    // 注意："第xx章"和章节名之间必须有一个空格
    // 匹配：第 + 中文数字/阿拉伯数字 + 章 + 空格 + 标题内容
    private static final String CHAPTER_PATTERN = "第[零一二三四五六七八九十百千万0-9]+章\\s+[^\\n]*";

    private static final Pattern CHAPTER_REGEX = Pattern.compile(CHAPTER_PATTERN);

    // 章节号提取正则表达式
    private static final Pattern CHAPTER_NUMBER_PATTERN = Pattern.compile("第([0-9]+)章");

    // 中文数字到阿拉伯数字的映射
    private static final String[] CHINESE_NUMBERS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] CHINESE_UNITS = {"", "十", "百", "千", "万", "十万", "百万", "千万", "亿"};

    /**
     * 检查字符串是否匹配章节标题格式
     * @param line 待检查的字符串
     * @return 是否匹配章节标题
     */
    public static boolean isChapterTitle(String line) {
        Matcher matcher = CHAPTER_REGEX.matcher(line.trim());
        return matcher.matches();
    }

    /**
     * 获取章节标题匹配器
     * @param content 小说内容
     * @return 章节标题匹配器
     */
    public static Matcher getChapterMatcher(String content) {
        return CHAPTER_REGEX.matcher(content);
    }

    /**
     * 查找所有章节标题
     * @param content 小说内容
     * @return 章节标题列表
     */
    public static List<String> findAllChapterTitles(String content) {
        List<String> chapterTitles = new ArrayList<>();
        Matcher matcher = CHAPTER_REGEX.matcher(content);
        while (matcher.find()) {
            chapterTitles.add(matcher.group());
        }
        return chapterTitles;
    }

    /**
     * 查找所有章节标题的起始位置
     * @param content 小说内容
     * @return 章节标题起始位置列表
     */
    public static List<Integer> findAllChapterPositions(String content) {
        List<Integer> positions = new ArrayList<>();
        Matcher matcher = CHAPTER_REGEX.matcher(content);
        while (matcher.find()) {
            positions.add(matcher.start());
        }
        return positions;
    }

    /**
     * 从章节标题中提取章节号
     * @param title 章节标题
     * @return 章节号，如果无法提取则返回null
     */
    public static Integer extractChapterNumber(String title) {
        // 先尝试匹配阿拉伯数字格式
        Matcher matcher = CHAPTER_NUMBER_PATTERN.matcher(title);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        // 尝试匹配中文数字格式
        return extractChineseChapterNumber(title);
    }

    /**
     * 从章节标题中提取中文数字章节号
     * @param title 章节标题
     * @return 章节号，如果无法提取则返回null
     */
    private static Integer extractChineseChapterNumber(String title) {
        // 提取中文数字格式的章节号
        Pattern chinesePattern = Pattern.compile("第([零一二三四五六七八九十百千万]+)章");
        Matcher matcher = chinesePattern.matcher(title);
        if (matcher.find()) {
            String chineseNumber = matcher.group(1);
            return convertChineseNumber(chineseNumber);
        }
        return null;
    }

    /**
     * 将中文数字转换为阿拉伯数字
     * @param chineseNumber 中文数字
     * @return 阿拉伯数字
     */
    private static Integer convertChineseNumber(String chineseNumber) {
        if (chineseNumber == null || chineseNumber.isEmpty()) {
            return null;
        }

        // 简单转换，支持常见中文数字
        try {
            // 如果直接是阿拉伯数字的字符串，直接转换
            return Integer.parseInt(chineseNumber);
        } catch (NumberFormatException e) {
            // 处理中文数字
        }

        // 处理简单中文数字
        for (int i = 0; i < CHINESE_NUMBERS.length; i++) {
            if (chineseNumber.equals(CHINESE_NUMBERS[i])) {
                return i;
            }
        }

        // 处理"十"及组合
        if (chineseNumber.equals("十")) {
            return 10;
        } else if (chineseNumber.startsWith("十")) {
            // 例如：十三 -> 13
            String unitPart = chineseNumber.substring(1);
            for (int i = 0; i < CHINESE_NUMBERS.length; i++) {
                if (unitPart.equals(CHINESE_NUMBERS[i])) {
                    return 10 + i;
                }
            }
        } else if (chineseNumber.endsWith("十")) {
            // 例如：二十 -> 20
            String digitPart = chineseNumber.substring(0, chineseNumber.length() - 1);
            for (int i = 0; i < CHINESE_NUMBERS.length; i++) {
                if (digitPart.equals(CHINESE_NUMBERS[i])) {
                    return i * 10;
                }
            }
        }

        // 对于更复杂的中文数字，可以在这里扩展
        // 暂时返回null表示无法转换
        return null;
    }

    /**
     * 从章节标题中提取章节标题文本（去掉"第xx章"前缀和空格）
     * @param title 章节标题
     * @return 章节标题文本
     */
    public static String extractChapterTitle(String title) {
        // 去除"第xx章"前缀和后面的空格，返回纯标题文本
        String trimmedTitle = title.trim();

        // 使用正则表达式移除"第xx章"以及后面的空格
        Pattern pattern = Pattern.compile("第[零一二三四五六七八九十百千万0-9]+章\\s+");
        Matcher matcher = pattern.matcher(trimmedTitle);

        if (matcher.find()) {
            // 移除匹配到的部分（包括空格）
            trimmedTitle = trimmedTitle.substring(matcher.end()).trim();
        }

        return trimmedTitle;
    }

    /**
     * 检查内容是否包含章节标题
     * @param content 小说内容
     * @return 是否包含章节标题
     */
    public static boolean containsChapters(String content) {
        Matcher matcher = CHAPTER_REGEX.matcher(content);
        return matcher.find();
    }

    /**
     * 提取章节内容
     * @param content 小说内容
     * @param startPos 章节起始位置
     * @param endPos 章节结束位置
     * @return 章节内容
     */
    public static String extractChapterContent(String content, int startPos, int endPos) {
        if (startPos < 0 || endPos > content.length() || startPos >= endPos) {
            return "";
        }
        return content.substring(startPos, endPos).trim();
    }

    /**
     * 从完整内容中提取章节标题和内容
     * @param content 完整小说内容
     * @return 章节信息列表，每个元素包含章节标题和内容
     */
    public static List<ChapterInfo> extractChapters(String content) {
        List<ChapterInfo> chapters = new ArrayList<>();
        Matcher matcher = CHAPTER_REGEX.matcher(content);

        int lastEnd = 0;
        String lastTitle = null;

        while (matcher.find()) {
            if (lastTitle != null) {
                // 提取上一章的内容
                String chapterContent = content.substring(lastEnd, matcher.start()).trim();
                chapters.add(new ChapterInfo(lastTitle, chapterContent));
            }

            lastTitle = matcher.group();
            lastEnd = matcher.start();
        }

        // 处理最后一章
        if (lastTitle != null) {
            String chapterContent = content.substring(lastEnd).trim();
            chapters.add(new ChapterInfo(lastTitle, chapterContent));
        }

        return chapters;
    }

    /**
     * 章节信息类，包含章节标题和内容
     */
    @Getter
    public static class ChapterInfo {
        private String title;
        private String content;

        public ChapterInfo(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }
}