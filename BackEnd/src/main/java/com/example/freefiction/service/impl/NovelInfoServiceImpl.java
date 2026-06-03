package com.example.freefiction.service.impl;

import com.example.freefiction.entity.Books;
import com.example.freefiction.entity.Categories;
import com.example.freefiction.service.NovelInfoService;
import com.example.freefiction.service.sys.BooksService;
import com.example.freefiction.service.sys.CategoriesService;
import com.example.freefiction.utils.download.IDMDownloader;
import com.example.freefiction.utils.novels.HttpUtil;
import com.example.freefiction.utils.novels.FileUtil;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 小说信息服务实现
 */
@Service
@RequiredArgsConstructor
public class NovelInfoServiceImpl implements NovelInfoService {

    @Value("${fiction.info.url}")
    private String infoUrlTemplate;

    @Value("${fiction.source.url}")
    private String sourceUrl;
    
    @Value("${fiction.upload.path}")
    private String uploadPath;

    @Value("${fiction.infoHtml.path}")
    private String infoHtmlPath;

    @Value("${fiction.newInfo.path}")
    private String newInfoPath;

    @Value("${fiction.searchInfo.path}")
    private String searchInfoPath;

    private final BooksService booksService;
    private final CategoriesService categoriesService;
    private final IDMDownloader idmDownloader;


    @Override
    public Books crawlNovelInfo(Long novelId) {
        //IDM全自动化获取
        String htmlFilePath = newInfoPath +"\\" +novelId + ".html";
        // 1. 将文件路径字符串转换为 Path 对象
        Path htmlFile = Paths.get(htmlFilePath);
        // 2. 检查文件是否存在,存在则先删除，再下载
        boolean exists = Files.exists(htmlFile);
        if (exists) {
            try {
                boolean deleted = Files.deleteIfExists(htmlFile);
                if (deleted) {
                    System.out.println("HTML文件已删除: " + htmlFilePath);
                } else {
                    System.out.println("HTML文件原本就不存在: " + htmlFilePath);
                }
            } catch (IOException e) {
                System.err.println("删除文件时发生错误: " + e.getMessage());
            }
        }

        try {
            String infoUrl = infoUrlTemplate.replace("{id}", novelId.toString());
            boolean isSuccess = idmDownloader.downloadAndWait(infoUrl, newInfoPath, novelId + ".html",5, 80000);
            if (!isSuccess) {
                System.out.println("下载网页信息资源失败");
                return null;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        //IDM全自动化获取
        //半自动化爬取小说
        Document doc;
        try {
            doc = Jsoup.parse(new File(htmlFilePath), "GBK");
        } catch (IOException e) {
            System.err.println("读取HTML文件失败，路径: " + htmlFilePath + "，错误信息: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        //半自动化爬取小说

        // 解析小说信息
        Books booksEntity = new Books();
        booksEntity.setBookId(novelId);
        booksEntity.setTitle(getTitle(doc));
        booksEntity.setAuthor(getAuthor(doc));
        booksEntity.setCover(downloadCover(novelId, doc));
        booksEntity.setDescription(getDescription(doc));
        booksEntity.setCategoryId(getCategory(doc));
        booksEntity.setStatus(getStatus(doc));
        booksEntity.setLastChapterTitle(getLastChapterTitle(doc));
        booksEntity.setLastChapterTime(getLastChapterTime(doc));
        booksEntity.setPublishDate(getPublishDate(doc));
//        booksEntity.setCreatedAt(new Date());
        booksEntity.setUpdatedAt(new Date());

        // 使用UTF-8编码输出，避免乱码问题
        System.out.println("装在实体完成："+booksEntity);
        // 保存到数据库
        boolean isSuccess = booksService.saveOrUpdate(booksEntity);
        if (!isSuccess) {
            System.out.println("保存小说信息失败");
            return null;
        }
        return booksEntity;
    }

    @Override
    public Books updateNovelInfo(Long novelId) {
        Books booksEntity = crawlNovelInfo(novelId);
        // 调用BooksService的updateNovelStats方法计算章节数和总字数
        booksService.updateNovelStats(novelId);
        return booksEntity;
    }

    /**
     * 从HTML文档中提取书名
     * @param doc HTML文档
     * @return 书名
     */
    private String getTitle(Document doc) {
        // 直接使用精确选择器获取书名
        Element titleElement = doc.selectFirst("div.info div.top h1");
        return titleElement != null ? titleElement.text().trim() : "未知书名";
    }

    /**
     * 从HTML文档中提取作者
     * @param doc HTML文档
     * @return 作者
     */
    private String getAuthor(Document doc) {
        // 直接使用精确选择器获取作者
        Element authorElement = doc.selectFirst("div.info div.top div.fix p:contains(作者)");
        
        if (authorElement != null) {
            String authorText = authorElement.text();
            // 提取作者名，去除"作者："前缀
            if (authorText.contains("作者：")) {
                authorText = authorText.split("作者：")[1].trim();
            } else if (authorText.contains("作者:")) {
                authorText = authorText.split("作者:")[1].trim();
            } else if (authorText.contains("作者")) {
                authorText = authorText.replace("作者", "").trim();
            } else {
                authorText = authorText.trim();
            }
            return authorText;
        }
        return "未知作者";
    }

    /**
     * 从HTML文档中提取封面图片URL并下载
     * @param novelId 小说ID
     * @param doc HTML文档
     * @return 封面图片保存路径
     */
    private String downloadCover(Long novelId, Document doc) {
        try {
            // 直接使用精确选择器获取封面图片
            Element coverElement = doc.selectFirst("div.imgbox img");
            
            if (coverElement != null) {
                String coverUrl = coverElement.attr("src");
                // 如果是相对路径，转换为绝对路径
                if (coverUrl.startsWith("/")) {
                    coverUrl = sourceUrl + coverUrl;
                } else if (!coverUrl.startsWith("http")) {
                    coverUrl = sourceUrl + "/" + coverUrl;
                }
                
                // 下载封面图片
                String imageDir = uploadPath + "/Images/" + novelId;
                FileUtil.createDirectory(imageDir);
                
                // 获取图片名称，使用小说ID作为文件名
                String imageName = "cover.jpg";
                String imagePath = imageDir + "/" + imageName;
                
                // 下载图片
                HttpUtil.downloadFileToPath(coverUrl, imagePath);
                
                // 返回相对路径，用于数据库存储
                return "/Images/" + novelId + "/" + imageName;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 从HTML文档中提取小说简介
     * @param doc HTML文档
     * @return 小说简介
     */
    private String getDescription(Document doc) {
        // 直接使用精确选择器获取小说简介
        Element descElement = doc.selectFirst("div.info div.desc.xs-hidden");
        
        if (descElement != null) {
            return descElement.text().trim();
        }
        return "";
    }
    
    /**
     * 从HTML文档中提取分类ID
     * @param doc HTML文档
     * @return 分类ID
     */
    private Integer getCategory(Document doc) {
        // 直接使用精确选择器获取分类
        Element categoryElement = doc.selectFirst("div.info div.top div.fix p.xs-show:contains(类别)");
        
        if (categoryElement != null) {
            String categoryText = categoryElement.text().trim();
            // 提取分类名称，去掉"类别："前缀
            String categoryName = categoryText.replace("类别：", "").trim();
            categoryName = categoryName.replace("类别:", "").trim();
            
            // 查询或创建分类
            return findOrCreateCategory(categoryName);
        }
        
        // 默认分类ID
        return 1;
    }
    
    /**
     * 查询或创建分类
     * @param categoryName 分类名称
     * @return 分类ID
     */
    private Integer findOrCreateCategory(String categoryName) {
        // 使用CategoriesService的createOrGetCategory方法
        Categories category = categoriesService.createOrGetCategory(categoryName);
        return category.getId();
    }
    
    /**
     * 从HTML文档中提取小说状态
     * @param doc HTML文档
     * @return 状态: 0-下架, 1-连载中, 2-已完结
     */
    private Integer getStatus(Document doc) {
        // 直接使用精确选择器获取状态
        Element statusElement = doc.selectFirst("div.info div.top div.fix p.xs-show:contains(状态)");
        
        if (statusElement != null) {
            String statusText = statusElement.text().trim();
            System.out.println("找到状态文本: " + statusText);
            
            // 提取状态名称，去掉"状态："前缀
            String statusName = statusText.replace("状态：", "").trim();
            statusName = statusName.replace("状态:", "").trim();
            
            // 根据状态名称设置状态值
            if (statusName.contains("连载") || statusName.contains("更新")) {
                return 1; // 连载中
            } else if (statusName.contains("完结") || statusName.contains("完本")) {
                return 2; // 已完结
            } else {
                return 0; // 下架
            }
        }
        
        // 默认状态为连载中
        return 1;
    }
    
    /**
     * 从HTML文档中提取最新章节标题
     * @param doc HTML文档
     * @return 最新章节标题
     */
    private String getLastChapterTitle(Document doc) {
        // 直接使用精确选择器获取最新章节标题
        Element lastChapterElement = doc.select("div.layout-col1 h2.layout-tit:contains(最新章节) + div.section-box ul.section-list li a").first();

        if (lastChapterElement != null) {
            return lastChapterElement.text();
        }else{
            System.out.println("没有找到最新章节标题");
            return "未知";
        }
    }
    
    /**
     * 从HTML文档中提取最后章节更新时间
     * @param doc HTML文档
     * @return 最后章节更新时间
     */
    private Date getLastChapterTime(Document doc) {
        // 直接使用精确选择器获取最后章节更新时间
        Element lastUpdateElement = doc.selectFirst("div.info div.top div.fix p:contains(最后更新)");
        
        if (lastUpdateElement != null) {
            String lastUpdateText = lastUpdateElement.text().trim();
            // 提取更新时间，去掉"最后更新："前缀
            if (lastUpdateText.contains("最后更新：")) {
                lastUpdateText = lastUpdateText.split("最后更新：")[1].trim();
            } else if (lastUpdateText.contains("最后更新:")) {
                lastUpdateText = lastUpdateText.split("最后更新:")[1].trim();
            } else if (lastUpdateText.contains("最后更新")) {
                lastUpdateText = lastUpdateText.replace("最后更新", "").trim();
            }
            
            // 解析时间格式，假设格式为"2023-10-01 12:00:00"
            try {
                return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastUpdateText);
            } catch (java.text.ParseException e) {
                // 尝试其他格式，如"2023-10-01"
                try {
                    return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(lastUpdateText);
                } catch (java.text.ParseException ex) {
                    // 如果解析失败，返回当前时间
                    return new Date();
                }
            }
        }
        
        return new Date();
    }
    
    /**
     * 从HTML文档中提取小说发布时间
     * @param doc HTML文档
     * @return 小说发布时间
     */
    private Date getPublishDate(Document doc) {
        // 注意：根据novel_1.html页面，该网站没有直接显示发布时间
        // 我们可以通过获取第一章的发布时间或者使用当前时间作为默认值
        return new Date();
    }
    
    /**
     * 从HTML文档中获取最新章节列表
     * @param doc HTML文档
     * @return 最新章节列表，每个元素包含章节标题和链接
     */
    private List<Map<String, String>> getLatestChapters(Document doc) {
        List<Map<String, String>> latestChapters = new ArrayList<>();
        
        // 直接使用精确选择器获取最新章节列表
        Elements chapterElements = doc.select("div.section-box ul.section-list.fix li a");
        
        for (Element chapterElement : chapterElements) {
            Map<String, String> chapter = new HashMap<>();
            chapter.put("title", chapterElement.text().trim());
            chapter.put("url", chapterElement.attr("href"));
            latestChapters.add(chapter);
        }
        
        return latestChapters;
    }
    
    /**
     * 从HTML文档中获取开始阅读URL
     * @param doc HTML文档
     * @return 开始阅读URL
     */
    private String getStartReadUrl(Document doc) {
        // 直接使用精确选择器获取开始阅读URL
        Element startReadElement = doc.selectFirst("div.info div.top div.fix p.opt a.btn-read");
        
        if (startReadElement != null) {
            String url = startReadElement.attr("href");
            // 如果是相对路径，转换为绝对路径
            if (url.startsWith("/")) {
                url = sourceUrl + url;
            } else if (!url.startsWith("http")) {
                url = sourceUrl + "/" + url;
            }
            return url;
        }
        
        return "";
    }
    
    /**
     * 从HTML文档中获取TXT下载URL
     * @param doc HTML文档
     * @return TXT下载URL
     */
    private String getTxtDownloadUrl(Document doc) {
        // 直接使用精确选择器获取TXT下载URL
        Element txtDownloadElement = doc.selectFirst("div.info div.top div.fix p.opt a.btn-dl");
        
        if (txtDownloadElement != null) {
            String url = txtDownloadElement.attr("href");
            // 如果是相对路径，转换为绝对路径
            if (url.startsWith("/")) {
                url = sourceUrl + url;
            } else if (!url.startsWith("http")) {
                url = sourceUrl + "/" + url;
            }
            return url;
        }
        
        return "";
    }
    
    @Override
    public List<Map<String, Object>> searchNovelsByName(String keyword) {
        List<Map<String, Object>> searchResults = new ArrayList<>();
        try {
            // 搜索URL
            String searchUrl = "https://www.qishuxia.com/modules/article/search.php";
            // 使用GBK编码处理关键词
            String encodedKeyword = java.net.URLEncoder.encode(keyword, "GBK");
            String postData = "?searchkey=" + encodedKeyword;
            //IDM全自动化获取
            String searchHtmlPath = searchInfoPath+"\\"+ keyword + ".html";
            // 1. 将文件路径字符串转换为 Path 对象
            Path htmlFile = Paths.get(searchHtmlPath);
            // 2. 检查文件是否存在,存在则先删除，再下载
            boolean exists = Files.exists(htmlFile);
            if (!exists) {
                try {
                    String infoUrl = searchUrl + postData;
                    boolean isSuccess = idmDownloader.downloadAndWait(infoUrl, searchInfoPath, keyword + ".html",3, 10000);
                    if (!isSuccess) {
                        System.out.println("下载网页信息资源失败");
                        return null;
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            //IDM全自动化获取
            //半自动化爬取小说
            Document doc;
            try {
                doc = Jsoup.parse(new File(searchHtmlPath), "GBK");
            } catch (IOException e) {
                System.err.println("读取HTML文件失败，路径: " + searchHtmlPath + "，错误信息: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
            
            // 查找搜索结果列表
            Elements resultItems = doc.select("ul.txt-list.txt-list-row5 li");
            
            // 跳过表头行
            boolean isFirstRow = true;
            for (Element item : resultItems) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                
                // 提取各个字段
                Map<String, Object> novelInfo = new HashMap<>();
                
                // 提取类型
                Element typeElement = item.selectFirst(".s1");
                if (typeElement != null) {
                    novelInfo.put("type", typeElement.text().trim());
                }
                
                // 提取小说页面链接和名称
                Element novelElement = item.selectFirst(".s2 a");
                if (novelElement != null) {
                    novelInfo.put("name", novelElement.text().trim());
                    novelInfo.put("url", novelElement.attr("href"));
                    
                    // 从URL中提取小说ID
                    String novelUrl = novelElement.attr("href");
                    Pattern pattern = Pattern.compile("/book/(\\d+)/");
                    Matcher matcher = pattern.matcher(novelUrl);
                    if (matcher.find()) {
                        String novelId = matcher.group(1);
                        novelInfo.put("id", novelId);
                    }
                }
                
                // 提取最新章节
                Element latestChapterElement = item.selectFirst(".s3 a");
                if (latestChapterElement != null) {
                    novelInfo.put("latest_chapter", latestChapterElement.text().trim());
                    novelInfo.put("latest_chapter_url", latestChapterElement.attr("href"));
                }
                
                // 提取作者
                Element authorElement = item.selectFirst(".s4");
                if (authorElement != null) {
                    novelInfo.put("author", authorElement.text().trim());
                }
                
                // 提取最新更新时间
                Element updateTimeElement = item.selectFirst(".s5");
                if (updateTimeElement != null) {
                    novelInfo.put("update_time", updateTimeElement.text().trim());
                }
                
                searchResults.add(novelInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return searchResults;
    }
}
