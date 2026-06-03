package com.example.freefiction.utils.novels;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * HTTP工具类，用于HTTP请求，下载小说和爬取页面
 */
public class HttpUtil {
    
    // 默认User-Agent
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    // 默认连接超时时间（毫秒）
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    
    // 默认读取超时时间（毫秒）
    private static final int DEFAULT_READ_TIMEOUT = 5000;

    /**
     * 发送GET请求，获取字符串响应
     * @param url 请求URL
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String get(String url) throws IOException {
        return get(url, StandardCharsets.UTF_8);
    }

    /**
     * 发送GET请求，获取字符串响应
     * @param url 请求URL
     * @param charset 字符集
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String get(String url, Charset charset) throws IOException {
        return get(url, charset, 3);
    }

    /**
     * 发送GET请求，获取字符串响应，带有重试机制
     * @param url 请求URL
     * @param charset 字符集
     * @param retryCount 重试次数
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String get(String url, Charset charset, int retryCount) throws IOException {
        int attempt = 0;
        while (attempt < retryCount) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
                connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
                connection.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
                // 检查响应码
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (InputStream in = connection.getInputStream();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset))) {
                        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
                    }
                } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    String location = connection.getHeaderField("Location");
                    if (location != null) {
                        url = location; // 更新URL并重新尝试
                        continue;
                    }
                }
                throw new IOException("HTTP请求失败，响应码: " + responseCode + ", URL: " + url);
//                // 检查响应码
//                int responseCode = connection.getResponseCode();
//                if (responseCode != HttpURLConnection.HTTP_OK) {
//                    throw new IOException("HTTP请求失败，响应码: " + responseCode + ", URL: " + url);
//                }
//
//                try (InputStream in = connection.getInputStream();
//                     BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset))) {
//                    return reader.lines().collect(Collectors.joining(System.lineSeparator()));
//                }
            } catch (IOException e) {
                attempt++;
                if (attempt >= retryCount) {
                    throw new IOException("HTTP请求失败，已重试" + retryCount + "次: " + e.getMessage(), e);
                }
                // 等待一段时间后重试，使用指数退避策略
                try {
                    long waitTime = (long) (1000 * Math.pow(2, attempt - 1));
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("HTTP请求被中断: " + ie.getMessage(), ie);
                }
            }
        }
        return null;
    }

    /**
     * 发送GET请求，获取HTML文档
     * @param url 请求URL
     * @return Jsoup Document对象
     * @throws IOException IO异常
     */
    public static Document getHtmlDocument(String url) throws IOException {
        return getHtmlDocument(url, StandardCharsets.UTF_8);
    }

    /**
     * 发送GET请求，获取HTML文档
     * @param url 请求URL
     * @param charset 字符集
     * @return Jsoup Document对象
     * @throws IOException IO异常
     */
    public static Document getHtmlDocument(String url, Charset charset) throws IOException {
        return getHtmlDocument(url, charset, 3);
    }
    
    /**
     * 发送GET请求，获取HTML文档，带有重试机制
     * @param url 请求URL
     * @param charset 字符集
     * @param retryCount 重试次数
     * @return Jsoup Document对象
     * @throws IOException IO异常
     */
    public static Document getHtmlDocument(String url, Charset charset, int retryCount) throws IOException {
        String html = get(url, charset, retryCount);
        return Jsoup.parse(html);
    }
    
    /**
     * 使用Jsoup发送GET请求，获取HTML文档
     * @param url 请求URL
     * @return Jsoup Document对象
     * @throws IOException IO异常
     */
    public static Document getHtmlDocumentWithJsoup(String url) throws IOException {
        return getHtmlDocumentWithJsoup(url, StandardCharsets.UTF_8);
    }
    
    /**
     * 使用Jsoup发送GET请求，获取HTML文档
     * @param url 请求URL
     * @param charset 字符集
     * @return Jsoup Document对象
     * @throws IOException IO异常
     */
    public static Document getHtmlDocumentWithJsoup(String url, Charset charset) throws IOException {
        return getHtmlDocumentWithJsoup(url, charset, 3);
    }
    
    /**
     * 使用Jsoup发送GET请求，获取HTML文档，带有重试机制
     * @param url 请求URL
     * @param charset 字符集
     * @param retryCount 重试次数
     * @return Jsoup Document对象
     * @throws IOException IO异常
     */
    public static Document getHtmlDocumentWithJsoup(String url, Charset charset, int retryCount) throws IOException {
        int attempt = 0;
        while (attempt < retryCount) {
            try {
                Connection connection = Jsoup.connect(url)
                        .userAgent(DEFAULT_USER_AGENT)
                        .timeout(DEFAULT_CONNECT_TIMEOUT)
                        .ignoreContentType(true);
                
                Connection.Response response = connection.execute();
                return response.parse();
            } catch (IOException e) {
                attempt++;
                if (attempt >= retryCount) {
                    throw new IOException("Jsoup请求失败，已重试" + retryCount + "次: " + e.getMessage(), e);
                }
                // 等待一段时间后重试，使用指数退避策略
                try {
                    long waitTime = (long) (1000 * Math.pow(2, attempt - 1));
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Jsoup请求被中断: " + ie.getMessage(), ie);
                }
            }
        }
        return null;
    }

    /**
     * 下载文件，返回文件内容
     * @param url 文件URL
     * @return 文件内容字节数组
     * @throws IOException IO异常
     */
    public static byte[] downloadFile(String url) throws IOException {
        return downloadFile(url, 3);
    }

    /**
     * 下载文件，返回文件内容，带有重试机制
     * @param url 文件URL
     * @param retryCount 重试次数
     * @return 文件内容字节数组
     * @throws IOException IO异常
     */
    public static byte[] downloadFile(String url, int retryCount) throws IOException {
        int attempt = 0;
        while (attempt < retryCount) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
                connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
                connection.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
                connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br, zstd");
                connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
                connection.setRequestProperty("Connection", "keep-alive");
                connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
                connection.setRequestProperty("Sec-Fetch-Dest", "document");
                connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
                connection.setRequestProperty("Sec-Fetch-Site", "same-origin");
                connection.setRequestProperty("Sec-Fetch-User", "?1");
                connection.setRequestProperty("Cache-Control", "max-age=0");
//                connection.setRequestProperty("Accept", "*/*");
                
                // 检查响应码
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("文件下载失败，响应码: " + responseCode + ", URL: " + url);
                }
                
                try (InputStream in = connection.getInputStream()) {
                    return in.readAllBytes();
                }
            } catch (IOException e) {
                attempt++;
                if (attempt >= retryCount) {
                    throw new IOException("文件下载失败，已重试" + retryCount + "次: " + e.getMessage(), e);
                }
                // 等待一段时间后重试，使用指数退避策略
                try {
                    long waitTime = (long) (1000 * Math.pow(2, attempt - 1));
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("文件下载被中断: " + ie.getMessage(), ie);
                }
            }
        }
        return null;
    }
    
    /**
     * 使用Jsoup下载文件，返回文件内容
     * @param url 文件URL
     * @param retryCount 重试次数
     * @return 文件内容字节数组
     * @throws IOException IO异常
     */
    public static byte[] downloadFileWithJsoup(String url, int retryCount) throws IOException {
        int attempt = 0;
        while (attempt < retryCount) {
            try {
                Connection connection = Jsoup.connect(url)
                        .userAgent(DEFAULT_USER_AGENT)
                        .timeout(DEFAULT_CONNECT_TIMEOUT)
                        .ignoreContentType(true);
                
                Connection.Response response = connection.execute();
                return response.bodyAsBytes();
            } catch (IOException e) {
                attempt++;
                if (attempt >= retryCount) {
                    throw new IOException("Jsoup文件下载失败，已重试" + retryCount + "次: " + e.getMessage(), e);
                }
                // 等待一段时间后重试，使用指数退避策略
                try {
                    long waitTime = (long) (1000 * Math.pow(2, attempt - 1));
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Jsoup文件下载被中断: " + ie.getMessage(), ie);
                }
            }
        }
        return null;
    }
    
    /**
     * 使用Jsoup下载文件，返回文件内容
     * @param url 文件URL
     * @return 文件内容字节数组
     * @throws IOException IO异常
     */
    public static byte[] downloadFileWithJsoup(String url) throws IOException {
        return downloadFileWithJsoup(url, 3);
    }

    /**
     * 下载文本文件，返回文件内容字符串
     * @param url 文件URL
     * @param charset 字符集
     * @return 文件内容字符串
     * @throws IOException IO异常
     */
    public static String downloadTextFile(String url, Charset charset) throws IOException {
        return downloadTextFile(url, charset, 3);
    }

    /**
     * 下载文本文件，返回文件内容字符串，带有重试机制
     * @param url 文件URL
     * @param charset 字符集
     * @param retryCount 重试次数
     * @return 文件内容字符串
     * @throws IOException IO异常
     */
    public static String downloadTextFile(String url, Charset charset, int retryCount) throws IOException {
        byte[] content = downloadFile(url, retryCount);
        return new String(content, charset);
    }

    /**
     * 下载文本文件，返回文件内容字符串，使用默认字符集UTF-8
     * @param url 文件URL
     * @return 文件内容字符串
     * @throws IOException IO异常
     */
    public static String downloadTextFile(String url) throws IOException {
        return downloadTextFile(url, StandardCharsets.UTF_8);
    }
    
    /**
     * 下载文件到指定路径
     * @param url 文件URL
     * @param filePath 保存路径
     * @throws IOException IO异常
     */
    public static void downloadFileToPath(String url, String filePath) throws IOException {
        downloadFileToPath(url, filePath, 3);
    }
    
    /**
     * 下载文件到指定路径，带有重试机制
     * @param url 文件URL
     * @param filePath 保存路径
     * @param retryCount 重试次数
     * @throws IOException IO异常
     */
    public static void downloadFileToPath(String url, String filePath, int retryCount) throws IOException {
        byte[] content = downloadFile(url, retryCount);
        File file = new File(filePath);
        
        // 创建父目录
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content);
        }
    }
    
    /**
     * 发送POST请求，获取字符串响应
     * @param url 请求URL
     * @param postData POST数据
     * @param charset 字符集
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String post(String url, String postData, Charset charset) throws IOException {
        return post(url, postData, charset, 3);
    }
    
    /**
     * 发送POST请求，获取字符串响应，带有重试机制
     * @param url 请求URL
     * @param postData POST数据
     * @param charset 字符集
     * @param retryCount 重试次数
     * @return 响应字符串
     * @throws IOException IO异常
     */
    public static String post(String url, String postData, Charset charset, int retryCount) throws IOException {
        int attempt = 0;
        while (attempt < retryCount) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
                connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
                connection.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                
                // 写入POST数据
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(postData.getBytes(charset));
                    out.flush();
                }
                
                // 检查响应码
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP请求失败，响应码: " + responseCode + ", URL: " + url);
                }
                
                try (InputStream in = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset))) {
                    return reader.lines().collect(Collectors.joining(System.lineSeparator()));
                }
            } catch (IOException e) {
                attempt++;
                if (attempt >= retryCount) {
                    throw new IOException("HTTP请求失败，已重试" + retryCount + "次: " + e.getMessage(), e);
                }
                // 等待一段时间后重试，使用指数退避策略
                try {
                    long waitTime = (long) (1000 * Math.pow(2, attempt - 1));
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("HTTP请求被中断: " + ie.getMessage(), ie);
                }
            }
        }
        return null;
    }
    
    /**
     * 发送POST请求，获取HTML文档
     * @param url 请求URL
     * @param postData POST数据
     * @param charset 字符集
     * @return Jsoup Document对象
     * @throws IOException IO异常
     */
    public static Document postHtmlDocument(String url, String postData, Charset charset) throws IOException {
        return postHtmlDocument(url, postData, charset, 3);
    }
    
    /**
     * 发送POST请求，获取HTML文档，带有重试机制
     * @param url 请求URL
     * @param postData POST数据
     * @param charset 字符集
     * @param retryCount 重试次数
     * @return Jsoup Document对象
     * @throws IOException IO异常
     */
    public static Document postHtmlDocument(String url, String postData, Charset charset, int retryCount) throws IOException {
        String html = post(url, postData, charset, retryCount);
        return Jsoup.parse(html);
    }
}
