package com.example.freefiction.utils.download;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * IDM 下载工具类
 * 用于通过命令行调用 Internet Download Manager (IDM) 执行文件下载任务
 */
@Component
@Slf4j
public class IDMDownloader {

    /**
     * IDM 可执行文件路径 (默认路径，可通过配置文件修改)
     * 例如：C:\\Program Files (x86)\\Internet Download Manager\\IDMan.exe
     */
    @Value("${idm.path}")
    private String idmPath;

    /**
     * 默认重试次数
     */
    @Value("${idm.retry.times:3}")
    private int defaultRetryTimes;

    /**
     * 异步任务执行器
     */
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();

    /**
     * 下载并等待文件完成
     * @param url 下载地址
     * @param saveDir 保存目录
     * @param fileName 文件名
     * @param maxWaitSeconds 最大等待秒数
     * @param checkIntervalMillis 检查间隔毫秒数
     * @return true 表示文件已成功下载并稳定
     */
    public boolean downloadAndWait(String url, String saveDir, String fileName,
                                   int maxWaitSeconds, long checkIntervalMillis)
            throws IOException, InterruptedException {

        // 1. 发起下载（使用 /q 参数自动开始）
        boolean taskAdded = downloadSilently(url, saveDir, fileName); // 确保这个方法内部调用的是含 /q 的命令
        if (!taskAdded) {
            System.out.println("警告：任务可能未成功添加到IDM队列。");
            // 即使返回false，也可能已在后台开始，我们继续检查文件
        }

        // 2. 构建完整的文件路径
        String fullPath = getExpectedFilePath(saveDir, fileName);
        File targetFile = new File(fullPath);

        // 3. 轮询检查文件状态
        long startTime = System.currentTimeMillis();
        long deadline = startTime + (maxWaitSeconds * 1000L);
        long lastFileSize = -1;
        int stableCount = 0; // 文件大小稳定的次数
        final int REQUIRED_STABLE_CHECKS = 3; // 要求连续3次检查大小不变才视为稳定

        System.out.println("开始等待下载完成，文件: " + fullPath);

        while (System.currentTimeMillis() < deadline) {
            if (targetFile.exists()) {
                long currentSize = targetFile.length();

                // 如果文件大小连续几次检查都未变化，则认为下载已完成
                if (currentSize > 0 && currentSize == lastFileSize) {
                    stableCount++;
                    System.out.printf("文件大小稳定 (%d/%d): %d bytes%n",
                            stableCount, REQUIRED_STABLE_CHECKS, currentSize);
                } else {
                    stableCount = 0; // 大小变化，重置稳定计数器
                    System.out.println("文件大小变化中: " + currentSize + " bytes");
                }

                if (stableCount >= REQUIRED_STABLE_CHECKS) {
                    System.out.println("下载完成确认，文件已稳定。");
                    return true;
                }

                lastFileSize = currentSize;
            } else {
                System.out.println("等待文件生成...");
            }

            // 等待一段时间再检查
            Thread.sleep(checkIntervalMillis);
        }

        // 超时，检查最终状态
        if (targetFile.exists() && targetFile.length() > 0) {
            System.out.println("等待超时，但文件已存在（可能未完全下载）。文件大小: " + targetFile.length() + " bytes");
            // 根据需求决定是否返回true，这里返回true意味着“有文件”，但不保证完整
            return true;
        } else {
            System.out.println("下载超时或失败，文件未生成。");
            return false;
        }
    }

    /**
     * 执行同步下载 (阻塞直到IDM进程结束)
     *
     * @param url      文件URL
     * @param saveDir  保存目录
     * @param fileName 文件名
     * @return 是否成功添加到IDM队列
     * @throws IOException          执行命令或IO错误
     * @throws InterruptedException 进程被中断
     */
    public boolean download(String url, String saveDir, String fileName) throws IOException, InterruptedException {
        System.out.println("开始下载: " + url);
        System.out.println("保存目录: " + saveDir);
        System.out.println("文件名: " + fileName);
        return executeIDMCommand(buildCommand(url, saveDir, fileName, true));
    }

    /**
     * 执行静默同步下载 (不弹窗)
     *
     * @param url      文件URL
     * @param saveDir  保存目录
     * @param fileName 文件名
     * @return 是否成功
     * @throws IOException          执行命令或IO错误
     * @throws InterruptedException 进程被中断
     */
    public boolean downloadSilently(String url, String saveDir, String fileName) throws IOException, InterruptedException {
        return executeIDMCommand(buildCommand(url, saveDir, fileName, true));
    }

    /**
     * 异步下载 (非阻塞)
     *
     * @param url      文件URL
     * @param saveDir  保存目录
     * @param fileName 文件名
     * @return Future对象，可获取下载任务结果
     */
    public Future<Boolean> downloadAsync(String url, String saveDir, String fileName) {
        return downloadAsync(url, saveDir, fileName, true);
    }

    /**
     * 异步下载 (可指定静默模式)
     *
     * @param url      文件URL
     * @param saveDir  保存目录
     * @param fileName 文件名
     * @param silent   是否静默模式
     * @return Future对象
     */
    public Future<Boolean> downloadAsync(String url, String saveDir, String fileName, boolean silent) {
        return asyncExecutor.submit(() -> {
            try {
                return executeIDMCommand(buildCommand(url, saveDir, fileName, silent));
            } catch (Exception e) {
                log.error("异步下载任务失败: URL={}, 错误={}", url, e.getMessage(), e);
                return false;
            }
        });
    }

    /**
     * 带重试的下载
     *
     * @param url        文件URL
     * @param saveDir    保存目录
     * @param fileName   文件名
     * @param maxRetries 最大重试次数
     * @return 是否成功
     */
    public boolean downloadWithRetry(String url, String saveDir, String fileName, int maxRetries) {
        int attempts = 0;
        while (attempts <= maxRetries) {
            try {
                log.info("尝试下载 (第{}次): {}", attempts + 1, url);
                boolean success = downloadSilently(url, saveDir, fileName);

                if (success) {
                    log.info("下载任务添加成功: {}", fileName);
                    return true;
                } else if (attempts == maxRetries) {
                    log.warn("下载失败，已达最大重试次数: {}", url);
                    return false;
                }
            } catch (Exception e) {
                log.warn("下载异常 (第{}次): {}", attempts + 1, e.getMessage());
                if (attempts == maxRetries) {
                    log.error("下载失败，已达最大重试次数: {}", url, e);
                    return false;
                }
            }

            attempts++;
            // 指数退避策略
            waitBeforeRetry(attempts);
        }
        return false;
    }

    /**
     * 带默认重试次数的下载
     *
     * @param url      文件URL
     * @param saveDir  保存目录
     * @param fileName 文件名
     * @return 是否成功
     */
    public boolean downloadWithRetry(String url, String saveDir, String fileName) {
        return downloadWithRetry(url, saveDir, fileName, defaultRetryTimes);
    }

    /**
     * 验证IDM是否可访问
     *
     * @return 是否可用
     */
    public boolean validateIDM() {
        File idmFile = new File(idmPath);
        if (!idmFile.exists()) {
            log.error("IDM可执行文件不存在: {}", idmPath);
            return false;
        }

        if (!idmFile.canExecute()) {
            log.error("IDM可执行文件没有执行权限: {}", idmPath);
            return false;
        }

        // 尝试执行一个简单命令（获取版本信息）
        try {
            ProcessBuilder pb = new ProcessBuilder(idmPath, "/?", "/h");
            Process process = pb.start();
            int exitCode = process.waitFor();
            log.info("IDM验证成功，退出码: {}", exitCode);
            return exitCode == 0 || exitCode == 1; // IDM通常返回0或1
        } catch (Exception e) {
            log.error("IDM验证失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 构建IDM命令行
     *
     * @param url      下载URL
     * @param saveDir  保存目录
     * @param fileName 文件名
     * @param silent   是否静默模式
     * @return 完整的命令行参数数组
     */
    private String[] buildCommand(String url, String saveDir, String fileName, boolean silent) {
        // 构建参数列表
        List<String> commandList = new ArrayList<>();
        commandList.add(wrapPath(idmPath));
        commandList.add("/d"); commandList.add(wrapPath(url));
        commandList.add("/p"); commandList.add(wrapPath(saveDir));
        commandList.add("/f"); commandList.add(wrapPath(fileName));
        commandList.add("/q"); // 使用/q参数实现自动下载并退出

        // 注意：/q 参数通常自身就带有“安静”属性，可酌情保留/n
        if (silent) {
            commandList.add("/n");
        }
        return commandList.toArray(new String[0]);
    }

    /**
     * 执行IDM命令
     *
     * @param command 命令行参数数组
     * @return 执行是否成功
     * @throws IOException          执行失败
     * @throws InterruptedException 进程中断
     */
    private boolean executeIDMCommand(String[] command) throws IOException, InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("执行IDM命令: {}", String.join(" ", command));
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        // 重定向错误流到标准输出
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // 读取输出（避免缓冲区阻塞）
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                if (log.isDebugEnabled()) {
                    log.debug("IDM输出: {}", line);
                }
            }
        }

        // 等待进程完成，设置超时60秒
        boolean finished = process.waitFor(60, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            log.warn("IDM命令执行超时");
            return false;
        }

        int exitCode = process.exitValue();

        // IDM退出码: 通常0表示成功，1或其他表示失败
        boolean success = (exitCode == 0);

        if (!success) {
            log.warn("IDM命令执行失败，退出码: {}, 输出: {}", exitCode, output.toString().trim());
        } else {
            log.info("IDM命令执行成功，退出码: {}", exitCode);
        }

        return success;
    }

    /**
     * 路径包装（处理空格和特殊字符）
     *
     * @param path 原始路径
     * @return 包装后的路径
     */
    private String wrapPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "\"\"";
        }
        // 如果路径包含空格且未用引号包裹，则包裹它
        if (path.contains(" ") && !(path.startsWith("\"") && path.endsWith("\""))) {
            return "\"" + path + "\"";
        }
        return path;
    }

    /**
     * 重试等待（指数退避）
     *
     * @param attempt 当前尝试次数
     */
    private void waitBeforeRetry(int attempt) {
        try {
            // 指数退避：1秒, 2秒, 4秒, 8秒...
            long waitTime = (long) Math.pow(2, Math.min(attempt, 6)) * 1000;
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获取预期的文件保存路径
     *
     * @param saveDir  目录
     * @param fileName 文件名
     * @return 完整路径
     */
    public String getExpectedFilePath(String saveDir, String fileName) {
        return saveDir + File.separator + fileName;
    }

    /**
     * 检查文件是否已存在（简单验证下载是否可能已完成）
     *
     * @param saveDir  目录
     * @param fileName 文件名
     * @return 文件是否存在
     */
    public boolean isFileExists(String saveDir, String fileName) {
        File file = new File(getExpectedFilePath(saveDir, fileName));
        return file.exists() && file.length() > 0;
    }
}