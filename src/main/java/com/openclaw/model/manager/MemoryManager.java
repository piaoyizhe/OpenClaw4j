package com.openclaw.model.manager;

import com.openclaw.config.ConfigManager;
import com.openclaw.brain.Hippocampus;
import com.openclaw.model.entity.Conversation;
import com.openclaw.model.entity.Message;
import com.openclaw.model.service.ContentExtractor;
import com.openclaw.model.service.ExperienceManager;
import com.openclaw.model.service.FileIndexer;
import com.openclaw.model.service.MemorySearch;
import com.openclaw.utils.ApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 记忆管理器
 * 负责管理所有记忆相关的功能，包括对话存储、每日日志生成、搜索等
 */
public class MemoryManager {
    private static MemoryManager instance;
    private ConfigManager configManager;
    private String memoryDirectory;
    private List<Conversation> conversations;

    // 集成的管理器
    private DailyLogManager dailyLogManager;
    private SQLiteManager sqliteManager;
    private MemorySearch memorySearch;
    private FileIndexer fileIndexer;
    private LongTermMemoryManager longTermMemoryManager;
    private ExperienceManager experienceManager;
    private ContentExtractor contentExtractor;
    private Hippocampus hippocampus;

    /**
     * 获取单例实例
     */
    public static synchronized MemoryManager getInstance() {
        if (instance == null) {
            instance = new MemoryManager();
        }
        return instance;
    }

    /**
     * 构造方法
     */
    public MemoryManager() {
        ApplicationContext context = ApplicationContext.getInstance();
        configManager = context.getComponent(ConfigManager.class);
        memoryDirectory = configManager.getMemoryDirectory();
        conversations = new ArrayList<>();
        initializeMemoryDirectory();
        initializeBasicManagers();
        loadBasicData();
        loadConversations(); // 加载历史对话数据
        lazyLoadFullManagers();
    }

    /**
     * 延迟初始化依赖组件
     */
    private void lazyInitializeComponents() {
        ApplicationContext context = ApplicationContext.getInstance();
        if (longTermMemoryManager == null) {
            longTermMemoryManager = context.getComponent(LongTermMemoryManager.class);
        }
        if (experienceManager == null) {
            experienceManager = context.getComponent(ExperienceManager.class);
        }
        if (contentExtractor == null) {
            contentExtractor = context.getComponent(ContentExtractor.class);
        }
        if (hippocampus == null) {
            hippocampus = context.getComponent(Hippocampus.class);
        }
    }

    /**
     * 初始化记忆存储目录
     */
    private void initializeMemoryDirectory() {
        File directory = new File(memoryDirectory);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                System.err.println("创建记忆存储目录失败: " + memoryDirectory);
            }
        }

        // 确保必要的子目录存在
        File[] subDirs = {
                new File(memoryDirectory, "archive"),
                new File(memoryDirectory, "sessions")
        };

        for (File subDir : subDirs) {
            if (!subDir.exists()) {
                if (!subDir.mkdirs()) {
                    System.err.println("创建子目录失败: " + subDir.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 初始化基础管理器
     */
    private void initializeBasicManagers() {
        // 初始化每日日志管理器
        dailyLogManager = DailyLogManager.getInstance();

        // 初始化长期记忆管理器（仅加载SOUL和USER基础数据）
        LongTermMemoryManager.getInstance();

        System.out.println("基础管理器初始化完成");
    }

    /**
     * 加载基础数据
     */
    private void loadBasicData() {
        try {
            // 仅加载SOUL和USER基础数据
            System.out.println("正在加载基础数据...");

            // 延迟初始化依赖组件
            lazyInitializeComponents();

            // 读取SOUL.md内容（AI人格数据）
            String soulContent = longTermMemoryManager.readLongTermMemory("SOUL.md", true);
            if (!soulContent.isEmpty()) {
                System.out.println("SOUL基础数据加载完成");
            }

            // 读取USER.md内容（用户画像数据）
            String userContent = longTermMemoryManager.readLongTermMemory("USER.md", true);
            if (!userContent.isEmpty()) {
                System.out.println("USER基础数据加载完成");
            }

            System.out.println("基础数据加载完成，完整记忆数据将按需加载");
        } catch (Exception e) {
            System.err.println("加载基础数据失败: " + e.getMessage());
        }
    }

    /**
     * 初始化完整管理器
     */
    private void initializeManagers() {
        // 初始化SQLite管理器
        sqliteManager = SQLiteManager.getInstance();

        // 初始化记忆搜索
        memorySearch = MemorySearch.getInstance();

        // 初始化文件索引器
        fileIndexer = FileIndexer.getInstance();
        fileIndexer.startWatching();

        System.out.println("所有记忆管理器初始化完成");
    }

    /**
     * 懒加载完整记忆管理器
     */
    private void lazyLoadFullManagers() {
        if (sqliteManager == null) {
            synchronized (this) {
                if (sqliteManager == null) {
//                    System.out.println("正在按需加载完整记忆管理器...");
                    initializeManagers();
//                    System.out.println("完整记忆管理器加载完成");
                }
            }
        }
    }

    /**
     * 从存储目录加载历史对话
     */
    private void loadConversations() {
        try {
            // 加载最近的对话记录文件
            File memoryDir = new File(memoryDirectory);
            File[] conversationFiles = memoryDir.listFiles((dir, name) -> name.startsWith("conversation_") && name.endsWith(".md"));

            if (conversationFiles != null && conversationFiles.length > 0) {
                // 按修改时间排序，获取最新的文件
                java.util.Arrays.sort(conversationFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                // 加载最新的对话文件
                File latestFile = conversationFiles[0];
                System.out.println("正在加载对话记录: " + latestFile.getName());
                String content = new String(Files.readAllBytes(latestFile.toPath()));
                parseConversationFile(content);
            }
        } catch (Exception e) {
            System.err.println("加载历史对话失败: " + e.getMessage());
        }
    }

    /**
     * 解析对话文件内容
     *
     * @param content 文件内容
     */
    private void parseConversationFile(String content) {
        try {
            // 简单解析对话文件，提取用户和助手的消息
            String[] lines = content.split("\n");
            Conversation currentConversation = null;
            String currentRole = null;
            StringBuilder currentContent = new StringBuilder();

            for (String line : lines) {
                line = line.trim();

                if (line.startsWith("## 对话时间:")) {
                    // 新的对话开始
                    saveCurrentMessage(currentConversation, currentRole, currentContent);
                    currentConversation = createConversation();
                    currentRole = null;
                    currentContent.setLength(0);
                } else if (line.startsWith("### 用户")) {
                    // 新的用户消息开始
                    saveCurrentMessage(currentConversation, currentRole, currentContent);
                    currentRole = "user";
                    currentContent.setLength(0);
                } else if (line.startsWith("### 助手")) {
                    // 新的助手消息开始
                    saveCurrentMessage(currentConversation, currentRole, currentContent);
                    currentRole = "assistant";
                    currentContent.setLength(0);
                } else if (line.equals("---")) {
                    // 对话结束
                    saveCurrentMessage(currentConversation, currentRole, currentContent);
                    currentRole = null;
                    currentContent.setLength(0);
                } else if (!line.isEmpty() && currentRole != null) {
                    // 消息内容
                    currentContent.append(line).append("\n");
                }
            }

            // 保存最后一条消息
            saveCurrentMessage(currentConversation, currentRole, currentContent);
        } catch (Exception e) {
            System.err.println("解析对话文件失败: " + e.getMessage());
        }
    }

    /**
     * 保存当前消息
     *
     * @param conversation 对话
     * @param role         角色
     * @param content      内容
     */
    private void saveCurrentMessage(Conversation conversation, String role, StringBuilder content) {
        if (conversation != null && role != null && content.length() > 0) {
            addMessage(conversation, role, content.toString());
        }
    }

    /**
     * 获取最近的历史对话消息
     *
     * @param limit 消息数量限制
     * @return 历史对话消息列表
     */
    public List<String> getRecentMessages(int limit) {
        List<String> recentMessages = new ArrayList<>();

        // 从最近的对话开始，获取消息
        for (int i = conversations.size() - 1; i >= 0 && recentMessages.size() < limit; i--) {
            Conversation conversation = conversations.get(i);
            List<Message> messages = conversation.getMessages();

            // 从对话的最后一条消息开始，反向遍历
            for (int j = messages.size() - 1; j >= 0 && recentMessages.size() < limit; j--) {
                Message message = messages.get(j);
                String role = message.getRole();
                String content = message.getContent();

                // 转换为聊天历史的格式
                String formattedMessage = formatMessageForChatHistory(role, content);
                if (formattedMessage != null) {
                    recentMessages.add(formattedMessage);
                }
            }
        }

        // 反转列表，使最早的消息在前
        java.util.Collections.reverse(recentMessages);
        return recentMessages;
    }

    /**
     * 格式化消息为聊天历史格式
     *
     * @param role    角色
     * @param content 内容
     * @return 格式化后的消息
     */
    private String formatMessageForChatHistory(String role, String content) {
        if ("user".equals(role)) {
            return "用户: " + content.trim();
        } else if ("assistant".equals(role)) {
            return "系统: " + content.trim();
        }
        return null;
    }

    /**
     * 创建新对话
     *
     * @return 新创建的对话对象
     */
    public Conversation createConversation() {
        Conversation conversation = new Conversation();
        conversations.add(conversation);
        return conversation;
    }

    /**
     * 向对话添加消息
     *
     * @param conversation 对话对象
     * @param role         角色（user/assistant）
     * @param content      消息内容
     */
    public void addMessage(Conversation conversation, String role, String content) {
        Message message = new Message(role, content);
        conversation.addMessage(message);
    }

    /**
     * 保存对话
     *
     * @param conversation 对话对象
     */
    public void saveConversation(Conversation conversation) {
        try {
            // 按日聚合保存对话记录
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String fileName = "conversation_" + dateStr + ".md";
            Path filePath = Paths.get(memoryDirectory, fileName);

            StringBuilder content = new StringBuilder();

            // 如果文件不存在，创建新文件并添加头部
            if (!Files.exists(filePath)) {
                content.append("# OpenClaw 对话记录 - " + dateStr + "\n\n");
            } else {
                // 如果文件存在，读取现有内容
                content.append(new String(Files.readAllBytes(filePath)));
            }

            // 添加新对话内容
            content.append("## 对话时间: " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "\n\n");

            for (Message message : conversation.getMessages()) {
                if ("user".equals(message.getRole())) {
                    content.append("### 用户\n");
                } else if ("assistant".equals(message.getRole())) {
                    content.append("### 助手\n");
                }
                content.append(message.getContent() + "\n\n");
            }

            content.append("---\n\n");

            Files.write(filePath, content.toString().getBytes());
//            System.out.println("对话记录已保存到: " + fileName);

            // 确保fileIndexer已初始化
            if (fileIndexer == null) {
                lazyLoadFullManagers();
            }

            // 索引对话文件
            fileIndexer.indexFile(filePath.toString());

            // 分析对话内容并提取事实
            Hippocampus.MemoryAnalysisResult result = analyzeAndWriteToLongTermMemory(conversation);
            // 判断内容是否需要更新
            updateMemoryContent(result, conversation.getCreatedAt());
            // 分析对话内容，记录经验值
//            experienceManager.analyzeConversation(conversation);

        } catch (Exception e) {
            System.err.println("保存对话记录失败: " + e.getMessage());
            // 这里可以添加更多的错误处理逻辑，比如重试机制
        }
    }

    /**
     * 搜索记忆
     *
     * @param query      查询文本
     * @param maxResults 最大结果数
     * @return 搜索结果列表
     */
    public List<MemorySearch.SearchResult> searchMemory(String query, int maxResults) {
        // 懒加载完整记忆管理器
        lazyLoadFullManagers();
        return memorySearch.hybridSearch(query, maxResults, 0.1);
    }

    /**
     * 搜索记忆（使用默认参数）
     *
     * @param query 查询文本
     * @return 搜索结果列表
     */
    public List<MemorySearch.SearchResult> searchMemory(String query) {
        // 懒加载完整记忆管理器
        lazyLoadFullManagers();
        return memorySearch.searchMemory(query);
    }

    /**
     * 分析对话内容并提取需要保存的内容
     *
     * @param conversation 对话对象
     * @return
     */
    private Hippocampus.MemoryAnalysisResult analyzeAndWriteToLongTermMemory(Conversation conversation) {
        try {
            // 延迟初始化依赖组件
            lazyInitializeComponents();

            // 收集所有需要分析的对话内容
            List<String> allContents = new ArrayList<>();

            for (Message message : conversation.getMessages()) {
                if (message.getContent() != null && message.getContent().length() > 5) {
                    allContents.add(message.getRole() + ": " + message.getContent());
                }
            }

            if (allContents.isEmpty()) {
                System.out.println("没有需要分析的对话内容\n");
                return null;
            }

            // 合并所有对话内容
            String combinedContent = String.join("\n\n", allContents);
//            System.out.println(">>> 待分析内容长度: " + combinedContent.length() + " 字符");

            // 是否有需要记录的内容 包括提取的事实，以及保存的位置）
            Hippocampus.MemoryAnalysisResult result = hippocampus.analyzeMemory(combinedContent);
            return result;
        } catch (Exception e) {
            System.err.println("分析对话内容并写入长期记忆失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 选择性更新记忆
     */
    private void updateMemoryContent(Hippocampus.MemoryAnalysisResult result, Long createAt) throws Exception {
        // 延迟初始化依赖组件
        lazyInitializeComponents();

        LongTermMemoryManager longTermMemoryManager = LongTermMemoryManager.getInstance();
        // 读取目标文件的内容，将想要保存的内容 + 原始文件的内容，来判断 是否需要更新文件 以及是否有冲突，需要更新哪几行
        if (result != null && result.shouldUpdate && result.extractedContent != null && !result.extractedContent.isEmpty()) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(createAt);
            String entryContent = "\n- " + timestamp + ": " + result.extractedContent;

            // 读取目标文件的现有内容
            String existingContent = longTermMemoryManager.readLongTermMemory(result.targetFile, true);
//            System.out.println(">>> 目标文件现有内容长度: " + (existingContent != null ? existingContent.length() : 0) + " 字符");

            // 分析记忆更新需求
            Hippocampus.MemoryUpdateAnalysis updateAnalysis = hippocampus.analyzeMemoryUpdate(result.targetFile, entryContent, existingContent != null ? existingContent : "");

            // 根据分析结果决定是否更新
            if (updateAnalysis.needsUpdate) {
                boolean success = false;
                if ("按行更新".equals(updateAnalysis.updateScope)) {
                    // 按行更新或需要确认的情况
                    if (updateAnalysis.lineUpdates != null && updateAnalysis.lineUpdates.size() > 0) {
                        // 执行按行更新
                        String updatedContent = performLineBasedUpdate(existingContent, updateAnalysis.lineUpdates);
                        if (updatedContent != null) {
                            success = longTermMemoryManager.writeLongTermMemory(result.targetFile, updatedContent, true);
                        }
                    }
                } else if("整个文件更新".equals(updateAnalysis.updateScope)&&null!=updateAnalysis.fileContent && !"".equals(updateAnalysis.fileContent.trim())) {
                    // 使用整文件更新
                    success = longTermMemoryManager.writeLongTermMemory(result.targetFile, updateAnalysis.fileContent, true);

                }

                if (success) {
//                    System.out.println("✓ 已更新" + result.targetFile + ": " + result.extractedContent);
                } else {
                    System.out.println("✗ 更新" + result.targetFile + "失败");
                }
            } else {
//                System.out.println("○ 无需更新记忆");
            }
        }
    }

    /**
     * 归档日志文件
     *
     * @param daysToKeep 保留天数
     */
    public void archiveLogs(int daysToKeep) {
        dailyLogManager.archiveLogs(daysToKeep);
    }

    /**
     * 清理过期数据
     *
     * @param days 保留天数
     */
    public void cleanExpiredData(int days) {
        sqliteManager.cleanExpiredData(days);
    }

    /**
     * 优化数据库
     */
    public void optimizeDatabase() {
        sqliteManager.optimizeDatabase();
    }

    /**
     * 索引所有记忆文件
     */
    public void indexAllFiles() {
        fileIndexer.indexAllMemoryFiles();
    }

    /**
     * 验证记忆存储目录是否可写
     *
     * @return 是否可写
     */
    public boolean isMemoryDirectoryWritable() {
        File directory = new File(memoryDirectory);
        return directory.exists() && directory.isDirectory() && directory.canWrite();
    }

    /**
     * 生成文件名
     *
     * @return 生成的文件名
     */
    private String generateFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return "conversation_" + sdf.format(new Date()) + ".md";
    }

    /**
     * 获取所有对话
     *
     * @return 对话列表
     */
    public List<Conversation> getConversations() {
        return conversations;
    }

    /**
     * 获取最新的对话
     *
     * @return 最新的对话对象
     */
    public Conversation getLatestConversation() {
        if (conversations.isEmpty()) {
            return createConversation();
        }
        return conversations.get(conversations.size() - 1);
    }

    /**
     * 获取每日日志管理器
     *
     * @return DailyLogManager实例
     */
    public DailyLogManager getDailyLogManager() {
        return dailyLogManager;
    }

    /**
     * 获取SQLite管理器
     *
     * @return SQLiteManager实例
     */
    public SQLiteManager getSqliteManager() {
        return sqliteManager;
    }

    /**
     * 获取记忆搜索
     *
     * @return MemorySearch实例
     */
    public MemorySearch getMemorySearch() {
        return memorySearch;
    }

    /**
     * 获取文件索引器
     *
     * @return FileIndexer实例
     */
    public FileIndexer getFileIndexer() {
        return fileIndexer;
    }

    /**
     * 获取经验值管理器
     *
     * @return ExperienceManager实例
     */
    public ExperienceManager getExperienceManager() {
        return experienceManager;
    }

    /**
     * 获取记忆存储目录
     *
     * @return 记忆存储目录路径
     */
    public String getMemoryDirectory() {
        return memoryDirectory;
    }

    /**
     * 关闭所有资源
     */
    public void close() {
        if (fileIndexer != null) {
            fileIndexer.close();
        }
        if (sqliteManager != null) {
            sqliteManager.closeConnection();
        }
        System.out.println("所有记忆管理器资源已关闭");
    }

    /**
     * 执行精确的部分更新
     *
     * @param existingContent 现有内容
     * @param updateDetails   更新详情
     * @return 更新后的内容
     */
    private String performPreciseUpdate(String existingContent, String updateDetails) {
        // 如果现有内容为空，直接返回更新详情
        if (existingContent == null || existingContent.isEmpty()) {
            return updateDetails;
        }

        // 简单的更新策略：查找关键字段并替换
        // 这里可以根据实际需求扩展更复杂的更新逻辑

        // 1. 尝试解析updateDetails中的具体更新指令
        // 假设updateDetails格式为："修改[关键字段]为[新值]"
        if (updateDetails.contains("修改") && updateDetails.contains("为")) {
            // 提取关键字段和新值
            String[] parts = updateDetails.split("为");
            if (parts.length == 2) {
                String fieldPart = parts[0].replace("修改", "").trim();
                String newValue = parts[1].trim();

                // 移除可能的方括号
                fieldPart = fieldPart.replace("[", "").replace("]", "");

                // 在现有内容中查找并替换该字段
                if (existingContent.contains(fieldPart)) {
                    // 简单的替换逻辑：查找包含字段的行并替换
                    StringBuilder updated = new StringBuilder();
                    String[] lines = existingContent.split("\\n");
                    for (String line : lines) {
                        if (line.contains(fieldPart)) {
                            // 替换整行
                            updated.append("- " + fieldPart + ": " + newValue + "\n");
                        } else {
                            updated.append(line + "\n");
                        }
                    }
                    return updated.toString().trim();
                }
            }
        }

        // 2. 尝试解析更复杂的更新格式
        // 假设格式为JSON或结构化描述

        // 3. 如果无法解析具体更新指令，返回null，让调用方使用备选方案
        return null;
    }

    /**
     * 执行按行更新
     *
     * @param existingContent 现有内容
     * @param lineUpdates     行更新信息数组
     * @return 更新后的内容
     */
    private String performLineBasedUpdate(String existingContent, com.alibaba.fastjson.JSONArray lineUpdates) {
        // 如果现有内容为空，直接返回空字符串
        if (existingContent == null || existingContent.isEmpty()) {
            return "";
        }

        // 将现有内容按行分割
        String[] lines = existingContent.split("\\n");
        java.util.List<String> lineList = new java.util.ArrayList<>(java.util.Arrays.asList(lines));

        // 处理每行更新
        for (int i = 0; i < lineUpdates.size(); i++) {
            try {
                com.alibaba.fastjson.JSONObject lineUpdate = lineUpdates.getJSONObject(i);
                int lineNumber = lineUpdate.getIntValue("lineNumber");
                String content = lineUpdate.getString("content") != null ? lineUpdate.getString("content") : "";
                String operation = lineUpdate.getString("operation") != null ? lineUpdate.getString("operation") : "update";

                if (lineNumber > 0) {
                    if ("update".equals(operation)) {
                        // 更新现有行
                        if (lineNumber <= lineList.size()) {
                            lineList.set(lineNumber - 1, content);
//                            System.out.println(">>> 更新行 " + lineNumber + ": " + content);
                        } else {
                            // 如果行号超出范围，添加新行
                            lineList.add(content);
                            System.out.println(">>> 行号超出范围，添加新行: " + content);
                        }
                    } else if ("add".equals(operation)) {
                        // 添加新行
                        if (lineNumber <= lineList.size()) {
                            lineList.add(lineNumber - 1, content);
                        } else {
                            lineList.add(content);
                        }
                        System.out.println(">>> 添加新行 " + lineNumber + ": " + content);
                    }
                }
            } catch (Exception e) {
                System.err.println(">>> 处理行更新时发生异常: " + e.getMessage());
            }
        }

        // 将更新后的行重新组合为字符串
        StringBuilder updatedContent = new StringBuilder();
        for (int i = 0; i < lineList.size(); i++) {
            updatedContent.append(lineList.get(i));
            if (i < lineList.size() - 1) {
                updatedContent.append("\n");
            }
        }

        return updatedContent.toString();
    }
}
