package com.openclaw.console;

import com.openclaw.utils.LLMClient;
import com.openclaw.brain.Prefrontal;
import com.openclaw.config.ConfigManager;
import com.openclaw.model.entity.Conversation;
import com.openclaw.model.manager.MemoryManager;
import com.openclaw.model.service.MemorySearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * 控制台交互接口
 * 提供用户与系统交互的命令行界面，支持对话、搜索、日志查询等功能
 */
public class ConsoleInterface {
    private ConfigManager configManager;
    private LLMClient llmClient;
    private MemoryManager memoryManager;
    private Conversation currentConversation;

    /**
     * 构造方法
     */
    public ConsoleInterface() {
        this.configManager = ConfigManager.getInstance();
        this.llmClient = LLMClient.getInstance();
        this.memoryManager = MemoryManager.getInstance();
        this.currentConversation = memoryManager.createConversation();
    }

    /**
     * 启动控制台交互
     */
    public void start() {
        System.out.println(configManager.getWelcomeMessage());
        System.out.println("输入 'help' 查看可用命令");
        System.out.println();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String input;
            while (true) {
                System.out.print(configManager.getPrompt());
                input = reader.readLine();

                if (input == null || "exit".equalsIgnoreCase(input.trim())) {
                    System.out.println("感谢使用OpenClaw！再见！");
                    memoryManager.close();
                    break;
                }

                if (input.trim().isEmpty()) {
                    continue;
                }

                processInput(input);
                System.out.println();
            }
        } catch (IOException e) {
            System.err.println("控制台输入输出错误: " + e.getMessage());
        }
    }

    /**
     * 处理用户输入
     * @param input 用户输入
     */
    private void processInput(String input) {
        // 检查是否是命令
        if (input.startsWith("/") || input.startsWith("help")) {
            processCommand(input);
        } else {
            processConversation(input);
        }
    }

    /**
     * 处理对话输入
     * @param input 用户输入
     */
    private void processConversation(String input) {
        try {
            // 添加用户消息到对话
            memoryManager.addMessage(currentConversation, "user", input);

            // 获取模型回答
            System.out.println("正在思考...");
            
            // 使用Prefrontal处理查询（Prefrontal会自动判断是否需要搜索记忆）
            Prefrontal prefrontal = Prefrontal.getInstance();
            String response = prefrontal.processQuery(input);

            // 显示模型回答
            System.out.println("\n助手:");
            System.out.println(response);

            // 添加助手消息到对话
            memoryManager.addMessage(currentConversation, "assistant", response);

            // 保存对话记录
            memoryManager.saveConversation(currentConversation);

        } catch (Exception e) {
            System.err.println(configManager.getErrorMessage());
            System.err.println("错误详情: " + e.getMessage());
        }
    }

    /**
     * 处理命令输入
     * @param input 命令输入
     */
    private void processCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "help":
                showHelp();
                break;
            case "/search":
            case "search":
                if (parts.length > 1) {
                    searchMemory(parts[1]);
                } else {
                    System.out.println("用法: search <关键词>");
                }
                break;
            case "/log":
            case "log":
                if (parts.length > 1) {
                    viewLog(parts[1]);
                } else {
                    viewTodayLog();
                }
                break;
            case "/memory":
            case "memory":
                if (parts.length > 1) {
                    processMemoryCommand(parts[1]);
                } else {
                    System.out.println("用法: memory <subcommand>\n可用子命令: index, optimize, clean, archive");
                }
                break;
            case "/clear":
            case "clear":
                clearConsole();
                break;
            case "/new":
            case "new":
                startNewConversation();
                break;
            default:
                System.out.println("未知命令: " + command);
                System.out.println("输入 'help' 查看可用命令");
                break;
        }
    }

    /**
     * 显示帮助信息
     */
    private void showHelp() {
        System.out.println("===== OpenClaw 命令帮助 =====");
        System.out.println("基本对话:");
        System.out.println("  直接输入问题，获取模型回答");
        System.out.println();
        System.out.println("搜索命令:");
        System.out.println("  search <关键词>      - 搜索记忆中的内容");
        System.out.println();
        System.out.println("日志命令:");
        System.out.println("  log                  - 查看今日日志");
        System.out.println("  log <日期>           - 查看指定日期的日志 (格式: YYYY-MM-DD)");
        System.out.println();
        System.out.println("记忆管理命令:");
        System.out.println("  memory index         - 索引所有记忆文件");
        System.out.println("  memory optimize      - 优化数据库");
        System.out.println("  memory clean <天数>   - 清理指定天数前的过期数据");
        System.out.println("  memory archive <天数> - 归档指定天数前的日志");
        System.out.println();
        System.out.println("其他命令:");
        System.out.println("  clear                - 清空控制台");
        System.out.println("  new                  - 开始新的对话");
        System.out.println("  exit                 - 退出程序");
        System.out.println("  help                 - 显示此帮助信息");
        System.out.println("==========================");
    }

    /**
     * 搜索记忆
     * @param query 搜索关键词
     */
    private void searchMemory(String query) {
        System.out.println("正在搜索: " + query);
        List<MemorySearch.SearchResult> results = memoryManager.searchMemory(query, 10);

        if (results.isEmpty()) {
            System.out.println("没有找到匹配的结果");
            return;
        }

        System.out.println("搜索结果 (共 " + results.size() + " 条):");
        System.out.println("========================================");
        
        for (int i = 0; i < results.size(); i++) {
            MemorySearch.SearchResult result = results.get(i);
            System.out.println((i + 1) + ". 分数: " + String.format("%.2f", result.getScore()));
            System.out.println("   文件: " + result.getFilePath());
            if (result.getStartLine() != null) {
                System.out.println("   行号: " + result.getStartLine() + "-" + result.getEndLine());
            }
            if (result.getContent() != null && !result.getContent().isEmpty()) {
                String content = result.getContent();
                if (content.length() > 100) {
                    content = content.substring(0, 100) + "...";
                }
                System.out.println("   内容: " + content.replaceAll("\\n", " "));
            }
            System.out.println("----------------------------------------");
        }
    }

    /**
     * 查看今日日志
     */
    private void viewTodayLog() {
        String todayLogPath = memoryManager.getDailyLogManager().getTodayLog();
        System.out.println("今日日志路径: " + todayLogPath);
        // 这里可以实现读取并显示日志内容的逻辑
    }

    /**
     * 查看指定日期的日志
     * @param date 日期字符串 (格式: YYYY-MM-DD)
     */
    private void viewLog(String date) {
        String logPath = memoryManager.getDailyLogManager().getMemoryDirectory() + "/" + date + ".md";
        System.out.println("日志路径: " + logPath);
        // 这里可以实现读取并显示日志内容的逻辑
    }

    /**
     * 处理记忆管理命令
     * @param subcommand 子命令
     */
    private void processMemoryCommand(String subcommand) {
        String[] parts = subcommand.split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "index":
                System.out.println("正在索引所有记忆文件...");
                memoryManager.indexAllFiles();
                System.out.println("索引完成");
                break;
            case "optimize":
                System.out.println("正在优化数据库...");
                memoryManager.optimizeDatabase();
                System.out.println("优化完成");
                break;
            case "clean":
                if (parts.length > 1) {
                    try {
                        int days = Integer.parseInt(parts[1]);
                        System.out.println("正在清理 " + days + " 天前的过期数据...");
                        memoryManager.cleanExpiredData(days);
                        System.out.println("清理完成");
                    } catch (NumberFormatException e) {
                        System.out.println("无效的天数: " + parts[1]);
                    }
                } else {
                    System.out.println("用法: memory clean <天数>");
                }
                break;
            case "archive":
                if (parts.length > 1) {
                    try {
                        int days = Integer.parseInt(parts[1]);
                        System.out.println("正在归档 " + days + " 天前的日志...");
                        memoryManager.archiveLogs(days);
                        System.out.println("归档完成");
                    } catch (NumberFormatException e) {
                        System.out.println("无效的天数: " + parts[1]);
                    }
                } else {
                    System.out.println("用法: memory archive <天数>");
                }
                break;
            default:
                System.out.println("未知的记忆管理命令: " + cmd);
                System.out.println("可用子命令: index, optimize, clean, archive");
                break;
        }
    }

    /**
     * 清空控制台
     */
    private void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            // 清空失败，使用换行替代
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    /**
     * 开始新的对话
     */
    private void startNewConversation() {
        currentConversation = memoryManager.createConversation();
        System.out.println("已开始新的对话");
    }

    /**
     * 获取记忆目录
     * @return 记忆目录路径
     */
    private String getMemoryDirectory() {
        return memoryManager.getDailyLogManager().getMemoryDirectory();
    }
}
