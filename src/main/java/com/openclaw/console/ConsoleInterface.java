package com.openclaw.console;

import com.openclaw.model.manager.LongTermMemoryManager;
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

        // 检查是否是首次运行
        LongTermMemoryManager longTermMemoryManager = LongTermMemoryManager.getInstance();
        if (longTermMemoryManager.isFirstRun()) {
            System.out.println("========================================");
            System.out.println("首次运行引导");
            System.out.println("========================================");
            System.out.println("欢迎使用OpenClaw！这是您第一次运行应用程序。");
            System.out.println("请完成以下配置，以便系统更好地为您服务。");
            System.out.println();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                // 1. 数字员工基本信息
                System.out.println("1. 数字员工基本信息");
                System.out.println("====================");
                
                System.out.print("请输入数字员工姓名: ");
                String name = reader.readLine().trim();
                
                System.out.print("请输入数字员工工号: ");
                String jobId = reader.readLine().trim();
                
                System.out.print("请输入数字员工功能简介: ");
                String description = reader.readLine().trim();
                System.out.println();
                
                // 2. 大模型配置
                System.out.println("2. 大模型配置");
                System.out.println("====================");
                
                System.out.print("请输入大模型API密钥 (按回车使用默认值): ");
                String apiKey = reader.readLine().trim();
                
                System.out.print("请输入大模型API地址 (按回车使用默认值): ");
                String apiUrl = reader.readLine().trim();
                
                System.out.print("请输入大模型名称 (按回车使用默认值): ");
                String model = reader.readLine().trim();
                System.out.println();
                
                // 3. 邮箱配置
                System.out.println("3. 邮箱配置");
                System.out.println("====================");
                
                System.out.print("是否启用邮件功能？(y/n，默认n): ");
                String enableMailInput = reader.readLine().trim();
                boolean enableMail = "y".equalsIgnoreCase(enableMailInput);
                
                String mailHost = "";  // 邮件服务器主机
                int mailPort = 465;    // 邮件服务器端口
                String mailUsername = "";  // 用户名
                String mailPassword = "";  // 密码
                String mailFrom = "";  // 发件人地址
                
                if (enableMail) {
                    System.out.print("请输入邮件服务器主机 (默认 smtp.qq.com): ");
                    mailHost = reader.readLine().trim();
                    if (mailHost.isEmpty()) {
                        mailHost = "smtp.qq.com";
                    }
                    
                    System.out.print("请输入邮件服务器端口 (默认 465): ");
                    String portInput = reader.readLine().trim();
                    if (!portInput.isEmpty()) {
                        try {
                            mailPort = Integer.parseInt(portInput);
                        } catch (NumberFormatException e) {
                            System.out.println("无效的端口号，使用默认值 465");
                        }
                    }
                    
                    System.out.print("请输入邮箱用户名: ");
                    mailUsername = reader.readLine().trim();
                    
                    System.out.print("请输入邮箱密码/授权码: ");
                    mailPassword = reader.readLine().trim();
                    
                    System.out.print("请输入发件人邮箱地址: ");
                    mailFrom = reader.readLine().trim();
                }
                System.out.println();
                
                // 4. 初始化记忆文件
                System.out.println("4. 初始化系统");
                System.out.println("====================");
                System.out.println("正在初始化系统文件...");
                
                // 初始化首次运行的记忆文件
                longTermMemoryManager.initializeFirstRunFiles();
                
                // 更新数字员工信息
                longTermMemoryManager.updateDigitalEmployeeInfo(name, jobId, description);
                
                // 更新大模型配置
                if (!apiKey.isEmpty() || !apiUrl.isEmpty() || !model.isEmpty()) {
                    configManager.updateLLMConfig(apiKey, apiUrl, model);
                    configManager.saveConfig();
                }
                
                // 更新邮箱配置
                if (enableMail && !mailUsername.isEmpty() && !mailPassword.isEmpty() && !mailFrom.isEmpty()) {
                    configManager.updateMailConfig(mailHost, mailPort, mailUsername, mailPassword, mailFrom);
                    configManager.saveConfig();
                }
                
                System.out.println("系统初始化完成！");
                System.out.println("========================================");
                System.out.println();
                
            } catch (Exception e) {
                System.err.println("初始化系统失败: " + e.getMessage());
            }
        }

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
