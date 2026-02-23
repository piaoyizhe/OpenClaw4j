package com.openclaw.utils;

import com.openclaw.model.entity.ToolParameters;
import com.openclaw.model.entity.ToolResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.*;

/**
 * Shell工具类
 * 用于执行Shell命令
 */
public class ShellUtil {
    private static ShellUtil instance;
    private ExecutorService executorService;

    /**
     * 私有构造方法
     */
    private ShellUtil() {
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * 获取单例实例
     * @return ShellUtil实例
     */
    public static ShellUtil getInstance() {
        if (instance == null) {
            synchronized (ShellUtil.class) {
                if (instance == null) {
                    instance = new ShellUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 执行Shell命令
     * @param parameters 参数
     * @return 执行结果
     */
    public ToolResult execute(ToolParameters parameters) {
        String command = parameters.getString("command");
        if (command == null || command.isEmpty()) {
            return new ToolResult(false, "命令不能为空", null);
        }

        String timeoutStr = parameters.getString("timeout");
        long timeout = 30000; // 默认30秒
        if (timeoutStr != null && !timeoutStr.isEmpty()) {
            try {
                timeout = Long.parseLong(timeoutStr);
            } catch (NumberFormatException e) {
                // 忽略错误，使用默认值
            }
        }

        try {
            System.out.println("执行Shell命令: " + command);
            
            // 构建进程
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // 使用Future来处理超时
            Future<String> future = executorService.submit(() -> {
                try (InputStream inputStream = process.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        return output.toString();
                    } else {
                        return "命令执行失败，退出码: " + exitCode + "\n输出: " + output.toString();
                    }
                } catch (Exception e) {
                    return "执行命令时发生错误: " + e.getMessage();
                }
            });
            
            // 获取结果，处理超时
            String result = future.get(timeout, TimeUnit.MILLISECONDS);
            process.destroy(); // 确保进程被销毁
            
            return new ToolResult(true, result, null);
        } catch (TimeoutException e) {
            return new ToolResult(false, "命令执行超时", null);
        } catch (Exception e) {
            return new ToolResult(false, "执行命令时发生错误: " + e.getMessage(), null);
        }
    }

    /**
     * 执行Shell命令（直接调用版本）
     * @param command 命令
     * @param timeout 超时时间（毫秒）
     * @return 执行结果
     */
    public String executeCommand(String command, long timeout) throws Exception {
        ToolParameters params = new ToolParameters();
        params.setParameter("command", command);
        params.setParameter("timeout", String.valueOf(timeout));
        ToolResult result = execute(params);
        return result.getMessage();
    }

    /**
     * 检查工具是否可用
     * @return 是否可用
     */
    public boolean isAvailable() {
        return true; // Shell工具在大多数系统上都可用
    }

    /**
     * 关闭资源
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}