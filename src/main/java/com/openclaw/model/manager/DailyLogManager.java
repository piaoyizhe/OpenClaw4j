package com.openclaw.model.manager;

import com.openclaw.config.ConfigManager;
import com.openclaw.brain.Hippocampus;
import com.openclaw.utils.LLMClient;
import com.openclaw.model.entity.Conversation;
import com.openclaw.model.entity.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 每日日志管理器
 * 负责每日日志的生成、管理和归档
 */
public class DailyLogManager {
    private static DailyLogManager instance;
    private ConfigManager configManager;
    private Hippocampus hippocampus;
    private LLMClient llmClient;
    private String memoryDirectory;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat archiveDateFormat;
    private SimpleDateFormat weekFormat;
    private SimpleDateFormat monthFormat;
    private ScheduledExecutorService scheduler;

    /**
     * 私有构造方法
     */
    private DailyLogManager() {
        configManager = ConfigManager.getInstance();
        hippocampus = Hippocampus.getInstance();
        llmClient = LLMClient.getInstance();
        memoryDirectory = configManager.getMemoryDirectory();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        archiveDateFormat = new SimpleDateFormat("yyyy-MM");
        weekFormat = new SimpleDateFormat("yyyy-'W'ww"); // 周格式：2026-W08
        monthFormat = new SimpleDateFormat("yyyy-MM");   // 月格式：2026-02
        initializeDirectories();
        initializeScheduler();
    }

    /**
     * 获取单例实例
     * @return DailyLogManager实例
     */
    public static DailyLogManager getInstance() {
        if (instance == null) {
            synchronized (DailyLogManager.class) {
                if (instance == null) {
                    instance = new DailyLogManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化目录结构
     */
    private void initializeDirectories() {
        // 确保记忆目录存在
        File memoryDir = new File(memoryDirectory);
        if (!memoryDir.exists()) {
            if (!memoryDir.mkdirs()) {
                System.err.println("创建记忆存储目录失败: " + memoryDirectory);
            }
        }

        // 确保归档目录存在
        File archiveDir = new File(memoryDirectory, "archive");
        if (!archiveDir.exists()) {
            if (!archiveDir.mkdirs()) {
                System.err.println("创建归档目录失败: " + archiveDir.getAbsolutePath());
            }
        }
        
        // 确保报告目录存在
        File reportsDir = new File(memoryDirectory, "reports");
        if (!reportsDir.exists()) {
            if (!reportsDir.mkdirs()) {
                System.err.println("创建报告目录失败: " + reportsDir.getAbsolutePath());
            }
        }
    }

    /**
     * 初始化定时任务
     */
    private void initializeScheduler() {
        scheduler = Executors.newScheduledThreadPool(3); // 3个线程，分别处理日/周/月报告
        
        // 初始化每日日志任务
        if (configManager.isDailyLogEnabled()) {
            String dailyCron = configManager.getDailyLogCronExpression();
            scheduleTask(this::generateDailyReport, dailyCron, "每日日志");
        }
        
        // 初始化周报任务
        if (configManager.isWeeklyReportEnabled()) {
            String weeklyCron = configManager.getWeeklyReportCronExpression();
            scheduleTask(this::generateWeeklyReport, weeklyCron, "周报");
        }
        
        // 初始化月报任务
        if (configManager.isMonthlyReportEnabled()) {
            String monthlyCron = configManager.getMonthlyReportCronExpression();
            scheduleTask(this::generateMonthlyReport, monthlyCron, "月报");
        }
        
        System.out.println("报告定时任务已初始化");
    }

    /**
     * 调度任务
     * @param task 任务
     * @param cronExpression cron表达式
     * @param taskName 任务名称
     */
    private void scheduleTask(Runnable task, String cronExpression, String taskName) {
        try {
            long initialDelay = calculateInitialDelay(cronExpression);
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    System.err.println(taskName + "执行失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }, initialDelay, 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
            
            System.out.println(taskName + "定时任务已初始化，cron表达式: " + cronExpression);
        } catch (Exception e) {
            System.err.println("初始化" + taskName + "定时任务失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 计算到下次执行的延迟时间
     * @param cronExpression cron表达式
     * @return 延迟时间（毫秒）
     */
    private long calculateInitialDelay(String cronExpression) throws Exception {
        // 简化实现：假设cron表达式格式为 "秒 分 时 日 月 周"
        String[] parts = cronExpression.trim().split("\\s+");
        if (parts.length < 6) {
            throw new Exception("无效的cron表达式格式");
        }
        
        int hour = Integer.parseInt(parts[2]);
        int minute = Integer.parseInt(parts[1]);
        
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        
        // 计算到今天指定时间的延迟
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        
        long initialDelay = calendar.getTimeInMillis() - System.currentTimeMillis();
        
        // 如果今天的时间已过，推迟到明天
        if (initialDelay < 0) {
            initialDelay += 24 * 60 * 60 * 1000;
        }
        
        return initialDelay;
    }

    /**
     * 生成每日报告（由定时任务调用）
     */
    public void generateDailyReport() {
        System.out.println("\n========== [DailyLogManager] 生成每日报告 ==========");
        System.out.println(">>> 开始时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        
        try {
            // 获取今天的日期
            String today = dateFormat.format(new Date());
            
            // 获取今天的对话记录文件
            String conversationFile = "conversation_" + today + ".md";
            Path conversationPath = Paths.get(memoryDirectory, conversationFile);
            
            if (!Files.exists(conversationPath)) {
                System.out.println("<<< 今日无对话记录，跳过报告生成");
                System.out.println("==================================================\n");
                return;
            }
            
            // 读取完整的聊天内容
            String conversationContent = new String(Files.readAllBytes(conversationPath));
            System.out.println(">>> 读取对话记录成功，内容长度: " + conversationContent.length() + " 字符");
            
            // 使用大模型生成日报
            String dailyReport = generateReportWithLLM(conversationContent, today, "daily");
            
            // 保存日报
            Path reportPath = Paths.get(memoryDirectory, today + ".md");
            Files.write(reportPath, dailyReport.getBytes());
            
            System.out.println("<<< 每日报告生成完成: " + today + ".md");
            System.out.println("==================================================\n");
            
        } catch (Exception e) {
            System.err.println("生成每日报告失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 生成周报（由定时任务调用）
     */
    public void generateWeeklyReport() {
        System.out.println("\n========== [DailyLogManager] 生成周报 ==========");
        System.out.println(">>> 开始时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        
        try {
            // 获取本周的日期范围
            Calendar calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            Date weekStart = calendar.getTime();
            calendar.add(Calendar.DAY_OF_WEEK, 6);
            Date weekEnd = calendar.getTime();
            
            String weekRange = dateFormat.format(weekStart) + " 至 " + dateFormat.format(weekEnd);
            String weekId = weekFormat.format(new Date()); // 2026-W08
            
            // 收集本周的对话记录
            StringBuilder weeklyContent = new StringBuilder();
            boolean hasContent = false;
            
            calendar.setTime(weekStart);
            for (int i = 0; i < 7; i++) {
                String dateStr = dateFormat.format(calendar.getTime());
                String conversationFile = "conversation_" + dateStr + ".md";
                Path conversationPath = Paths.get(memoryDirectory, conversationFile);
                
                if (Files.exists(conversationPath)) {
                    weeklyContent.append("## " + dateStr + "\n\n");
                    weeklyContent.append(new String(Files.readAllBytes(conversationPath)));
                    weeklyContent.append("\n\n---\n\n");
                    hasContent = true;
                }
                
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            
            if (!hasContent) {
                System.out.println("<<< 本周无对话记录，跳过周报生成");
                System.out.println("==================================================\n");
                return;
            }
            
            System.out.println(">>> 收集本周对话记录成功，内容长度: " + weeklyContent.length() + " 字符");
            
            // 使用大模型生成周报
            String weeklyReport = generateReportWithLLM(weeklyContent.toString(), weekRange, "weekly");
            
            // 保存周报
            Path reportsDir = Paths.get(memoryDirectory, "reports");
            Path reportPath = reportsDir.resolve("weekly_report_" + weekId + ".md");
            Files.write(reportPath, weeklyReport.getBytes());
            
            System.out.println("<<< 周报生成完成: " + reportPath.getFileName());
            System.out.println("==================================================\n");
            
        } catch (Exception e) {
            System.err.println("生成周报失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 生成月报（由定时任务调用）
     */
    public void generateMonthlyReport() {
        System.out.println("\n========== [DailyLogManager] 生成月报 ==========");
        System.out.println(">>> 开始时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        
        try {
            // 获取上个月的日期
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            String lastMonth = monthFormat.format(calendar.getTime());
            
            // 收集上月的对话记录
            StringBuilder monthlyContent = new StringBuilder();
            boolean hasContent = false;
            
            // 获取上个月的天数
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            
            for (int i = 0; i < daysInMonth; i++) {
                String dateStr = dateFormat.format(calendar.getTime());
                String conversationFile = "conversation_" + dateStr + ".md";
                Path conversationPath = Paths.get(memoryDirectory, conversationFile);
                
                if (Files.exists(conversationPath)) {
                    monthlyContent.append("## " + dateStr + "\n\n");
                    monthlyContent.append(new String(Files.readAllBytes(conversationPath)));
                    monthlyContent.append("\n\n---\n\n");
                    hasContent = true;
                }
                
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            
            if (!hasContent) {
                System.out.println("<<< 上月无对话记录，跳過月报生成");
                System.out.println("==================================================\n");
                return;
            }
            
            System.out.println(">>> 收集上月对话记录成功，内容长度: " + monthlyContent.length() + " 字符");
            
            // 使用大模型生成月报
            String monthlyReport = generateReportWithLLM(monthlyContent.toString(), lastMonth, "monthly");
            
            // 保存月报
            Path reportsDir = Paths.get(memoryDirectory, "reports");
            Path reportPath = reportsDir.resolve("monthly_report_" + lastMonth + ".md");
            Files.write(reportPath, monthlyReport.getBytes());
            
            System.out.println("<<< 月报生成完成: " + reportPath.getFileName());
            System.out.println("==================================================\n");
            
        } catch (Exception e) {
            System.err.println("生成月报失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 使用大模型生成报告
     * @param content 内容
     * @param period 周期
     * @param reportType 报告类型
     * @return 生成的报告
     */
    private String generateReportWithLLM(String content, String period, String reportType) throws Exception {
        System.out.println(">>> 使用大模型生成" + getReportTypeName(reportType) + "报告");
        
        String prompt = buildReportPrompt(content, period, reportType);
        
        // 设置模型参数
        LLMClient.ModelParams params = new LLMClient.ModelParams();
        params.temperature = 0.3; // 降低随机性
        params.maxTokens = 3072;  // 增加令牌数，确保报告完整
        
        // 调用大模型
        String report = llmClient.generateResponse(prompt, null, false, null, params);
        System.out.println(">>> 大模型生成报告完成，长度: " + report.length() + " 字符");
        
        return report;
    }

    /**
     * 构建报告提示词
     * @param content 内容
     * @param period 周期
     * @param reportType 报告类型
     * @return 提示词
     */
    private String buildReportPrompt(String content, String period, String reportType) {
        String reportName = getReportTypeName(reportType);
        String sections = getReportSections(reportType);
        
        return "你是一个专业的" + reportName + "生成助手。请根据以下完整的聊天记录，生成一份详细的" + reportName + "。\n\n" +
               "## 报告周期\n" + period + "\n\n" +
               "## 完整聊天记录\n" + content + "\n\n" +
               "## 报告要求\n" +
               "1. 格式要求：\n" +
               "   - 使用Markdown格式\n" +
               "   - 包含以下章节：" + sections + "\n" +
               "\n" +
               "2. 内容要求：\n" +
               "   - 提取聊天中关于工作的重要信息\n" +
               "   - 识别关键决策和行动项\n" +
               "   - 总结工作进展和成果\n" +
               "   - 语言简洁明了，条理清晰\n" +
               "   - 只关注工作相关内容，忽略闲聊\n" +
               "\n" +
               "3. 输出格式：\n" +
               "   请直接返回生成的" + reportName + "内容，不要其他解释。\n";
    }

    /**
     * 获取报告类型名称
     * @param reportType 报告类型
     * @return 报告类型名称
     */
    private String getReportTypeName(String reportType) {
        switch (reportType) {
            case "daily": return "每日工作";
            case "weekly": return "周工作";
            case "monthly": return "月工作";
            default: return "工作";
        }
    }

    /**
     * 获取报告章节
     * @param reportType 报告类型
     * @return 报告章节
     */
    private String getReportSections(String reportType) {
        switch (reportType) {
            case "daily":
                return "工作内容、关键决策、遇到的问题、解决方案、明日计划、总结";
            case "weekly":
                return "本周工作内容、关键决策、遇到的问题、解决方案、下周计划、本周总结";
            case "monthly":
                return "本月工作内容、关键项目进展、关键决策、遇到的问题、解决方案、下月计划、本月总结";
            default:
                return "工作内容、关键决策、遇到的问题、解决方案、未来计划、总结";
        }
    }

    /**
     * 生成每日日志（实时生成，保持原有功能）
     * @param conversation 对话对象
     * @return 生成的日志文件路径
     */
    public String generateDailyLog(Conversation conversation) {
        // 保持原有功能，实时生成简单日志
        String today = dateFormat.format(new Date());
        Path logFilePath = Paths.get(memoryDirectory, today + ".md");

        try {
            // 提取关键信息
            Map<String, Object> keyInfo = extractKeyInfo(conversation);

            // 构建日志内容
            StringBuilder logContent = buildLogContent(keyInfo, conversation);

            // 写入日志文件
            writeLogFile(logFilePath, logContent.toString());

            System.out.println("每日日志已生成: " + logFilePath.getFileName());
            return logFilePath.toString();
        } catch (IOException e) {
            System.err.println("生成每日日志失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从对话中提取关键信息
     * @param conversation 对话对象
     * @return 提取的关键信息
     */
    private Map<String, Object> extractKeyInfo(Conversation conversation) {
        Map<String, Object> keyInfo = new HashMap<>();

        // 提取时间戳
        keyInfo.put("timestamp", new Date());

        // 提取对话ID
        keyInfo.put("conversationId", conversation.getId());

        // 提取参与用户
        Set<String> users = new HashSet<>();
        for (Message message : conversation.getMessages()) {
            if ("user".equals(message.getRole())) {
                users.add("user"); // 简化处理，实际可能需要从消息中提取用户名
            }
        }
        keyInfo.put("users", users);

        // 提取核心对话内容摘要
        StringBuilder contentSummary = new StringBuilder();
        List<String> keyDecisions = new ArrayList<>();
        List<String> actionItems = new ArrayList<>();

        for (Message message : conversation.getMessages()) {
            String content = message.getContent();
            contentSummary.append(content).append(" ");

            // 提取关键决策点
            extractKeyDecisions(content, keyDecisions);

            // 提取行动项
            extractActionItems(content, actionItems);
        }

        // 生成摘要（限制长度）
        String summary = contentSummary.toString();
        if (summary.length() > 500) {
            summary = summary.substring(0, 500) + "...";
        }
        keyInfo.put("contentSummary", summary);
        keyInfo.put("keyDecisions", keyDecisions);
        keyInfo.put("actionItems", actionItems);

        return keyInfo;
    }

    /**
     * 提取关键决策点
     * @param content 消息内容
     * @param keyDecisions 关键决策点列表
     */
    private void extractKeyDecisions(String content, List<String> keyDecisions) {
        // 简单的规则匹配，实际可能需要更复杂的NLP处理
        Pattern decisionPattern = Pattern.compile("(决定|确定|同意|批准|否决|选择|计划)\\s+(.+?)([。，；！？]|$)");
        Matcher matcher = decisionPattern.matcher(content);
        while (matcher.find()) {
            keyDecisions.add(matcher.group(0));
        }
    }

    /**
     * 提取行动项
     * @param content 消息内容
     * @param actionItems 行动项列表
     */
    private void extractActionItems(String content, List<String> actionItems) {
        // 简单的规则匹配，实际可能需要更复杂的NLP处理
        Pattern actionPattern = Pattern.compile("(需要|应该|必须|建议|计划|安排)\\s+(.+?)([。，；！？]|$)");
        Matcher matcher = actionPattern.matcher(content);
        while (matcher.find()) {
            actionItems.add(matcher.group(0));
        }
    }

    /**
     * 构建日志内容
     * @param keyInfo 关键信息
     * @param conversation 对话对象
     * @return 日志内容
     */
    private StringBuilder buildLogContent(Map<String, Object> keyInfo, Conversation conversation) {
        StringBuilder content = new StringBuilder();

        // 日志头部
        content.append("# OpenClaw 每日日志\n\n");
        content.append("## 基本信息\n");
        content.append("- **日期:** " + dateFormat.format(keyInfo.get("timestamp")) + "\n");
        content.append("- **时间:** " + new SimpleDateFormat("HH:mm:ss").format(keyInfo.get("timestamp")) + "\n");
        content.append("- **对话ID:** " + keyInfo.get("conversationId") + "\n");
        content.append("- **参与用户:** " + String.join(", ", (Set<String>) keyInfo.get("users")) + "\n\n");

        // 核心对话内容摘要
        content.append("## 核心对话内容摘要\n");
        content.append((String) keyInfo.get("contentSummary") + "\n\n");

        // 关键决策点
        List<String> keyDecisions = (List<String>) keyInfo.get("keyDecisions");
        if (!keyDecisions.isEmpty()) {
            content.append("## 关键决策点\n");
            for (String decision : keyDecisions) {
                content.append("- " + decision + "\n");
            }
            content.append("\n");
        }

        // 后续行动项
        List<String> actionItems = (List<String>) keyInfo.get("actionItems");
        if (!actionItems.isEmpty()) {
            content.append("## 后续行动项\n");
            for (String action : actionItems) {
                content.append("- " + action + "\n");
            }
            content.append("\n");
        }

        // 完整对话记录（可选）
        content.append("## 完整对话记录\n");
        for (Message message : conversation.getMessages()) {
            if ("user".equals(message.getRole())) {
                content.append("### 用户\n");
            } else if ("assistant".equals(message.getRole())) {
                content.append("### 助手\n");
            }
            content.append(message.getContent() + "\n\n");
        }

        return content;
    }

    /**
     * 写入日志文件
     * @param logFilePath 日志文件路径
     * @param content 日志内容
     * @throws IOException IO异常
     */
    private void writeLogFile(Path logFilePath, String content) throws IOException {
        // 如果文件已存在，追加内容
        if (Files.exists(logFilePath)) {
            String existingContent = new String(Files.readAllBytes(logFilePath));
            // 保留文件头部，追加新内容
            int contentStartIndex = existingContent.indexOf("## 完整对话记录");
            if (contentStartIndex != -1) {
                existingContent = existingContent.substring(0, contentStartIndex);
                content = existingContent + content.substring(content.indexOf("## 完整对话记录"));
            }
        }

        Files.write(logFilePath, content.getBytes());
    }

    /**
     * 归档日志文件
     * @param daysToKeep 保留天数
     */
    public void archiveLogs(int daysToKeep) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysToKeep);
        Date cutoffDate = calendar.getTime();

        File memoryDir = new File(memoryDirectory);
        File[] files = memoryDir.listFiles((dir, name) -> name.endsWith(".md") && !name.equals("README.md"));

        if (files != null) {
            for (File file : files) {
                try {
                    String fileName = file.getName();
                    String dateStr = fileName.substring(0, fileName.length() - 3);
                    Date fileDate = dateFormat.parse(dateStr);

                    if (fileDate.before(cutoffDate)) {
                        // 归档文件
                        archiveFile(file, fileDate);
                    }
                } catch (Exception e) {
                    System.err.println("处理日志文件失败: " + file.getName() + ", 错误: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 归档单个文件
     * @param file 要归档的文件
     * @param fileDate 文件日期
     */
    private void archiveFile(File file, Date fileDate) {
        String monthStr = archiveDateFormat.format(fileDate);
        File archiveDir = new File(memoryDirectory, "archive/" + monthStr);

        // 确保归档目录存在
        if (!archiveDir.exists()) {
            if (!archiveDir.mkdirs()) {
                System.err.println("创建归档目录失败: " + archiveDir.getAbsolutePath());
                return;
            }
        }

        // 移动文件到归档目录
        File destFile = new File(archiveDir, file.getName());
        if (file.renameTo(destFile)) {
            System.out.println("日志已归档: " + file.getName() + " -> " + destFile.getAbsolutePath());
        } else {
            System.err.println("归档日志失败: " + file.getName());
        }
    }

    /**
     * 搜索日志
     * @param keyword 关键词
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 搜索结果
     */
    public List<String> searchLogs(String keyword, Date startDate, Date endDate) {
        List<String> results = new ArrayList<>();

        // 搜索当前目录的日志
        searchLogsInDirectory(new File(memoryDirectory), keyword, startDate, endDate, results);

        // 搜索归档目录的日志
        searchLogsInDirectory(new File(memoryDirectory, "archive"), keyword, startDate, endDate, results);

        return results;
    }

    /**
     * 在指定目录中搜索日志
     * @param directory 搜索目录
     * @param keyword 关键词
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param results 搜索结果
     */
    private void searchLogsInDirectory(File directory, String keyword, Date startDate, Date endDate, List<String> results) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归搜索子目录
                searchLogsInDirectory(file, keyword, startDate, endDate, results);
            } else if (file.getName().endsWith(".md")) {
                // 检查文件日期
                try {
                    String fileName = file.getName();
                    String dateStr = fileName.substring(0, fileName.length() - 3);
                    Date fileDate = dateFormat.parse(dateStr);

                    // 检查日期范围
                    if ((startDate == null || !fileDate.before(startDate)) &&
                            (endDate == null || !fileDate.after(endDate))) {
                        // 读取文件内容并搜索关键词
                        String content = new String(Files.readAllBytes(file.toPath()));
                        if (content.contains(keyword)) {
                            results.add(file.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    // 忽略无法解析的文件
                }
            }
        }
    }

    /**
     * 获取今日日志
     * @return 今日日志文件路径
     */
    public String getTodayLog() {
        String today = dateFormat.format(new Date());
        Path logFilePath = Paths.get(memoryDirectory, today + ".md");
        return logFilePath.toString();
    }

    /**
     * 获取昨日日志
     * @return 昨日日志文件路径
     */
    public String getYesterdayLog() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = dateFormat.format(calendar.getTime());
        Path logFilePath = Paths.get(memoryDirectory, yesterday + ".md");
        return logFilePath.toString();
    }

    /**
     * 关闭定时任务
     */
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
            System.out.println("报告定时任务已关闭");
        }
    }

    /**
     * 获取记忆目录
     * @return 记忆目录路径
     */
    public String getMemoryDirectory() {
        return memoryDirectory;
    }

    /**
     * 获取报告目录
     * @return 报告目录路径
     */
    public String getReportsDirectory() {
        return Paths.get(memoryDirectory, "reports").toString();
    }
} 
