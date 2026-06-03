package com.example.freefiction.utils.novels;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件工具类，用于文件操作，创建目录、读写文件
 */
public class FileUtil {

    /**
     * 创建目录，如果目录不存在
     * @param dirPath 目录路径
     */
    public static void createDirectory(String dirPath) {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory: " + dirPath, e);
            }
        }
    }

    /**
     * 写入字节数组到文件
     * @param filePath 文件路径
     * @param content 字节数组内容
     */
    public static void writeBytes(String filePath, byte[] content) {
        writeBytes(filePath, content, false);
    }
    
    /**
     * 写入字节数组到文件
     * @param filePath 文件路径
     * @param content 字节数组内容
     * @param append 是否追加到文件末尾
     */
    public static void writeBytes(String filePath, byte[] content, boolean append) {
        // 确保父目录存在
        createDirectory(new File(filePath).getParent());
        
        try (FileOutputStream fos = new FileOutputStream(filePath, append)) {
            fos.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + filePath, e);
        }
    }

    /**
     * 写入字符串到文件，使用UTF-8编码
     * @param filePath 文件路径
     * @param content 字符串内容
     */
    public static void writeString(String filePath, String content) {
        writeString(filePath, content, StandardCharsets.UTF_8);
    }
    
    /**
     * 写入字符串到文件，使用指定编码
     * @param filePath 文件路径
     * @param content 字符串内容
     * @param charset 字符集
     */
    public static void writeString(String filePath, String content, Charset charset) {
        writeString(filePath, content, charset, false);
    }
    
    /**
     * 写入字符串到文件，使用指定编码
     * @param filePath 文件路径
     * @param content 字符串内容
     * @param charset 字符集
     * @param append 是否追加到文件末尾
     */
    public static void writeString(String filePath, String content, Charset charset, boolean append) {
        // 确保父目录存在
        createDirectory(new File(filePath).getParent());
        
        try (FileOutputStream fos = new FileOutputStream(filePath, append);
             OutputStreamWriter writer = new OutputStreamWriter(fos, charset)) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + filePath, e);
        }
    }

    /**
     * 读取文件内容为字符串，使用UTF-8编码
     * @param filePath 文件路径
     * @return 文件内容字符串
     */
    public static String readString(String filePath) {
        return readString(filePath, StandardCharsets.UTF_8);
    }
    
    /**
     * 读取文件内容为字符串，使用指定编码
     * @param filePath 文件路径
     * @param charset 字符集
     * @return 文件内容字符串
     */
    public static String readString(String filePath, Charset charset) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
        return sb.toString();
    }
    
    /**
     * 读取文件内容为字节数组
     * @param filePath 文件路径
     * @return 文件内容字节数组
     */
    public static byte[] readBytes(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }
    
    /**
     * 读取文件内容为行列表，使用UTF-8编码
     * @param filePath 文件路径
     * @return 文件内容行列表
     */
    public static List<String> readLines(String filePath) {
        return readLines(filePath, StandardCharsets.UTF_8);
    }
    
    /**
     * 读取文件内容为行列表，使用指定编码
     * @param filePath 文件路径
     * @param charset 字符集
     * @return 文件内容行列表
     */
    public static List<String> readLines(String filePath, Charset charset) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
        return lines;
    }

    /**
     * 检查文件是否存在
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * 检查目录是否存在
     * @param dirPath 目录路径
     * @return 是否存在
     */
    public static boolean isDirectory(String dirPath) {
        return Files.isDirectory(Paths.get(dirPath));
    }
    
    /**
     * 检查是否是文件
     * @param filePath 文件路径
     * @return 是否是文件
     */
    public static boolean isFile(String filePath) {
        return Files.isRegularFile(Paths.get(filePath));
    }

    /**
     * 获取文件大小
     * @param filePath 文件路径
     * @return 文件大小（字节）
     */
    public static long getFileSize(String filePath) {
        Path path = Paths.get(filePath);
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get file size: " + filePath, e);
        }
    }
    
    /**
     * 获取目录下的所有文件
     * @param dirPath 目录路径
     * @return 文件列表
     */
    public static List<File> listFiles(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<>();
        }
        
        File[] files = dir.listFiles();
        List<File> result = new ArrayList<>();
        if (files != null) {
            result.addAll(Arrays.asList(files));
        }
        return result;
    }
    
    /**
     * 获取目录下的所有文件，使用过滤器
     * @param dirPath 目录路径
     * @param filter 文件过滤器
     * @return 文件列表
     */
    public static List<File> listFiles(String dirPath, FilenameFilter filter) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return new ArrayList<>();
        }
        
        File[] files = dir.listFiles(filter);
        List<File> result = new ArrayList<>();
        if (files != null) {
            result.addAll(Arrays.asList(files));
        }
        return result;
    }
    
    /**
     * 删除文件
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }
    
    /**
     * 删除目录及其内容
     * @param dirPath 目录路径
     * @return 是否删除成功
     */
    public static boolean deleteDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file.getAbsolutePath());
                } else {
                    file.delete();
                }
            }
        }
        
        return dir.delete();
    }
    
    /**
     * 获取安全的文件名，替换掉不允许的字符
     * @param fileName 原始文件名
     * @return 安全的文件名
     */
    public static String getSafeFileName(String fileName) {
        // 替换掉Windows和Linux系统中不允许的文件名字符
        return fileName.replaceAll("[\\/:*?\"<>|]", "_");
    }
    
    /**
     * 获取文件的扩展名
     * @param filePath 文件路径
     * @return 文件扩展名，不包含点号
     */
    public static String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * 获取文件名，不包含路径和扩展名
     * @param filePath 文件路径
     * @return 文件名，不包含路径和扩展名
     */
    public static String getFileNameWithoutExtension(String filePath) {
        int lastSeparatorIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        int lastDotIndex = filePath.lastIndexOf('.');
        
        String fileName = filePath.substring(lastSeparatorIndex + 1);
        if (lastDotIndex > lastSeparatorIndex) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
    
    /**
     * 获取文件名，包含扩展名但不包含路径
     * @param filePath 文件路径
     * @return 文件名，包含扩展名但不包含路径
     */
    public static String getFileName(String filePath) {
        int lastSeparatorIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return filePath.substring(lastSeparatorIndex + 1);
    }
}
