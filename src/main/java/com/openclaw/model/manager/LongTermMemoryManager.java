package com.openclaw.model.manager;

import com.openclaw.config.ConfigManager;
import com.openclaw.model.service.FileIndexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 长期记忆管理器
 * 负责管理长期精选记忆文件（MEMORY.md / USER.md / SOUL.md）
 */
public class LongTermMemoryManager {
    private static LongTermMemoryManager instance;
    private ConfigManager configManager;
    private String memoryDirectory;
    private String projectRootDirectory;
    private String globalMemoryDirectory;

    // 长期记忆文件名称
    private static final String MEMORY_FILE = "MEMORY.md";
    private static final String USER_FILE = "USER.md";
    private static final String SOUL_FILE = "SOUL.md";
    private static final String IDENTITY_FILE = "IDENTITY.md";

    /**
     * 私有构造方法
     */
    private LongTermMemoryManager() {
        configManager = ConfigManager.getInstance();
        memoryDirectory = configManager.getMemoryDirectory();
        projectRootDirectory = System.getProperty("user.dir");
        globalMemoryDirectory = System.getProperty("user.home") + File.separator + ".openclaw";
        initializeDirectories();
        initializeLongTermMemoryFiles();
    }

    /**
     * 获取单例实例
     * @return LongTermMemoryManager实例
     */
    public static LongTermMemoryManager getInstance() {
        if (instance == null) {
            synchronized (LongTermMemoryManager.class) {
                if (instance == null) {
                    instance = new LongTermMemoryManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化目录结构
     */
    private void initializeDirectories() {
        // 确保全局记忆目录存在
        File globalDir = new File(globalMemoryDirectory);
        if (!globalDir.exists()) {
            if (!globalDir.mkdirs()) {
                System.err.println("创建全局记忆目录失败: " + globalMemoryDirectory);
            }
        }

        // 确保项目根目录存在（应该已经存在）
        File projectDir = new File(projectRootDirectory);
        if (!projectDir.exists()) {
            System.err.println("项目根目录不存在: " + projectRootDirectory);
        }
    }

    /**
     * 初始化长期记忆文件
     */
    private void initializeLongTermMemoryFiles() {
        // 初始化全局级长期记忆文件（始终创建空文件）
        createDefaultFile(getGlobalMemoryFilePath(MEMORY_FILE), "");
        createDefaultFile(getGlobalMemoryFilePath(USER_FILE), "");
        createDefaultFile(getGlobalMemoryFilePath(SOUL_FILE), "");
        createDefaultFile(getGlobalMemoryFilePath(IDENTITY_FILE), "");
    }
    
    /**
     * 检查是否是首次运行
     * @return 是否是首次运行
     */
    public boolean isFirstRun() {
        // 检查是否存在核心记忆文件，或者文件内容是否为空
        return !exists(MEMORY_FILE, true) || !exists(SOUL_FILE, true) || 
               readLongTermMemory(MEMORY_FILE, true).isEmpty() || 
               readLongTermMemory(SOUL_FILE, true).isEmpty();
    }
    
    /**
     * 初始化首次运行的记忆文件
     */
    public void initializeFirstRunFiles() {
        // 初始化项目级长期记忆文件
        createDefaultFile(getProjectMemoryFilePath(MEMORY_FILE), getDefaultMemoryContent());
        createDefaultFile(getProjectMemoryFilePath(SOUL_FILE), getDefaultSoulContent());
        createDefaultFile(getProjectMemoryFilePath(USER_FILE), getDefaultUserContent());
        createDefaultFile(getProjectMemoryFilePath(IDENTITY_FILE), getDefaultIdentityContent());
    }
    
    /**
     * 更新数字员工信息
     * @param name 姓名
     * @param jobId 工号
     * @param description 功能简介
     */
    public void updateDigitalEmployeeInfo(String name, String jobId, String description) {
        // 读取当前IDENTITY.md内容
        String currentContent = readLongTermMemory(IDENTITY_FILE, true);
        
        // 如果文件不存在或为空，使用默认内容
        if (currentContent.isEmpty()) {
            currentContent = getDefaultIdentityContent();
        }
        
        // 替换姓名
        if (name != null && !name.isEmpty()) {
            currentContent = currentContent.replace("(待设置)", name);
        }
        
        // 替换工号
        if (jobId != null && !jobId.isEmpty()) {
            currentContent = currentContent.replace("(待设置)", jobId);
        }
        
        // 添加功能简介
        if (description != null && !description.isEmpty()) {
            // 检查是否已有功能简介部分
            if (!currentContent.contains("## 功能简介")) {
                currentContent += "\n## 功能简介\n" + description + "\n";
            } else {
                // 替换现有的功能简介
                String[] parts = currentContent.split("## 功能简介");
                currentContent = parts[0] + "## 功能简介\n" + description + "\n" + parts[1].split("##")[1];
            }
        }
        
        // 写回文件
        writeLongTermMemory(IDENTITY_FILE, currentContent, true);
    }

    /**
     * 创建默认文件
     * @param filePath 文件路径
     * @param defaultContent 默认内容
     */
    private void createDefaultFile(String filePath, String defaultContent) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    if (!defaultContent.isEmpty()) {
                        Files.write(Paths.get(filePath), defaultContent.getBytes());
                    }
                    System.out.println("创建长期记忆文件: " + filePath);
                }
            } catch (IOException e) {
                System.err.println("创建长期记忆文件失败: " + e.getMessage());
            }
        }
    }

    /**
     * 获取默认MEMORY.md内容
     * @return 默认内容
     */
    private String getDefaultMemoryContent() {
        return "# OpenClaw 长期精选记忆\n\n" +
                "## 重要决策\n\n" +
                "## 偏好\n\n" +
                "## 关键事实\n\n" +
                "## 项目信息\n\n";
    }

    /**
     * 获取默认USER.md内容
     * @return 默认内容
     */
    private String getDefaultUserContent() {
        return "# OpenClaw 用户画像\n\n" +
                "## 基本信息\n\n" +
                "## 偏好语言\n\n" +
                "## 工作习惯\n\n" +
                "## 专业背景\n\n" +
                "## 兴趣爱好\n\n";
    }

    /**
     * 获取默认SOUL.md内容
     * @return 默认内容
     */
    private String getDefaultSoulContent() {
        return "# OpenClaw AI 人格与行为规则\n\n" +
                "## 性格特征\n\n" +
                "## 语气风格\n\n" +
                "## 做事风格\n\n" +
                "## 行为规则\n\n";
    }
    
    /**
     * 获取默认IDENTITY.md内容
     * @return 默认内容
     */
    private String getDefaultIdentityContent() {
        return "# IDENTITY.md - Who Am I\n\n" +
                "- **Name:**  (待设置)\n" +
                "- **Creature:** 数字员工 (Digital Employee)\n" +
                "- **Company:** 智教科技有限公司\n" +
                "- **Job ID:**  (待设置)\n" +
                "- **Vibe:** 专业、高效、可靠\n" +
                "- **Emoji:** ❄️\n" +
                "- **Avatar:** (待设置)\n\n" +
                "## 工作内容\n\n" +
                "- 查找资料\n" +
                "- 软件开发\n" +
                "- 协助解决技术问题\n\n" +
                "## 工作环境\n\n" +
                "- 已配备电脑\n" +
                "- 已分配工位\n\n" +
                "---\n\n" +
                "_智教科技有限公司数字员工，随时待命。_\n";
    }

    /**
     * 获取项目级长期记忆文件路径
     * @param fileName 文件名
     * @return 文件路径
     */
    public String getProjectMemoryFilePath(String fileName) {
        return memoryDirectory + File.separator + fileName;
    }

    /**
     * 获取全局级长期记忆文件路径
     * @param fileName 文件名
     * @return 文件路径
     */
    public String getGlobalMemoryFilePath(String fileName) {
        return globalMemoryDirectory + File.separator + fileName;
    }

    /**
     * 读取长期记忆文件内容
     * @param fileName 文件名
     * @param useProjectLevel 是否使用项目级文件
     * @return 文件内容
     */
    public String readLongTermMemory(String fileName, boolean useProjectLevel) {
        try {
            // 优先使用项目级文件
            String filePath = useProjectLevel ? getProjectMemoryFilePath(fileName) : getGlobalMemoryFilePath(fileName);
            File file = new File(filePath);

            if (file.exists()) {
                return new String(Files.readAllBytes(Paths.get(filePath)));
            }

            // 如果项目级文件不存在，尝试使用全局级文件
            if (useProjectLevel) {
                filePath = getGlobalMemoryFilePath(fileName);
                file = new File(filePath);
                if (file.exists()) {
                    return new String(Files.readAllBytes(Paths.get(filePath)));
                }
            }
        } catch (IOException e) {
            System.err.println("读取长期记忆文件失败: " + e.getMessage());
        }
        return "";
    }

    /**
     * 写入长期记忆文件
     * @param fileName 文件名
     * @param content 内容
     * @param useProjectLevel 是否使用项目级文件
     * @return 是否写入成功
     */
    public boolean writeLongTermMemory(String fileName, String content, boolean useProjectLevel) {
        try {
            String filePath = useProjectLevel ? getProjectMemoryFilePath(fileName) : getGlobalMemoryFilePath(fileName);
            Files.write(Paths.get(filePath), content.getBytes());
//            System.out.println("写入长期记忆文件成功: " + filePath);
            
            // 重新索引文件
            FileIndexer.getInstance().indexFile(filePath);
            return true;
        } catch (IOException e) {
            System.err.println("写入长期记忆文件失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 追加内容到长期记忆文件
     * @param fileName 文件名
     * @param content 要追加的内容
     * @param useProjectLevel 是否使用项目级文件
     * @return 是否追加成功
     */
    public boolean appendToLongTermMemory(String fileName, String content, boolean useProjectLevel) {
        try {
            String filePath = useProjectLevel ? getProjectMemoryFilePath(fileName) : getGlobalMemoryFilePath(fileName);
            String existingContent = readLongTermMemory(fileName, useProjectLevel);
            String newContent = existingContent + "\n" + content;
            return writeLongTermMemory(fileName, newContent, useProjectLevel);
        } catch (Exception e) {
            System.err.println("追加内容到长期记忆文件失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 记住重要信息到MEMORY.md
     * @param content 要记住的内容
     * @param useProjectLevel 是否使用项目级文件
     * @return 是否记住成功
     */
    public boolean remember(String content, boolean useProjectLevel) {
        return appendToLongTermMemory(MEMORY_FILE, content, useProjectLevel);
    }

    /**
     * 更新用户画像到USER.md
     * @param content 用户画像内容
     * @param useProjectLevel 是否使用项目级文件
     * @return 是否更新成功
     */
    public boolean updateUserProfile(String content, boolean useProjectLevel) {
        return appendToLongTermMemory(USER_FILE, content, useProjectLevel);
    }

    /**
     * 更新AI人格到SOUL.md
     * @param content AI人格内容
     * @param useProjectLevel 是否使用项目级文件
     * @return 是否更新成功
     */
    public boolean updateSoul(String content, boolean useProjectLevel) {
        return appendToLongTermMemory(SOUL_FILE, content, useProjectLevel);
    }

    /**
     * 获取所有长期记忆文件路径
     * @return 文件路径列表
     */
    public List<String> getAllLongTermMemoryFiles() {
        List<String> files = new ArrayList<>();

        // 添加项目级文件
        files.add(getProjectMemoryFilePath(MEMORY_FILE));
        files.add(getProjectMemoryFilePath(USER_FILE));
        files.add(getProjectMemoryFilePath(SOUL_FILE));
        files.add(getProjectMemoryFilePath(IDENTITY_FILE));

        // 添加全局级文件
        files.add(getGlobalMemoryFilePath(MEMORY_FILE));
        files.add(getGlobalMemoryFilePath(USER_FILE));
        files.add(getGlobalMemoryFilePath(SOUL_FILE));
        files.add(getGlobalMemoryFilePath(IDENTITY_FILE));

        return files;
    }

    /**
     * 索引所有长期记忆文件
     */
    public void indexAllLongTermMemoryFiles() {
        FileIndexer fileIndexer = FileIndexer.getInstance();
        for (String file : getAllLongTermMemoryFiles()) {
            fileIndexer.indexFile(file);
        }
    }

    /**
     * 获取MEMORY.md文件路径
     * @param useProjectLevel 是否使用项目级文件
     * @return 文件路径
     */
    public String getMemoryFilePath(boolean useProjectLevel) {
        return useProjectLevel ? getProjectMemoryFilePath(MEMORY_FILE) : getGlobalMemoryFilePath(MEMORY_FILE);
    }

    /**
     * 获取USER.md文件路径
     * @param useProjectLevel 是否使用项目级文件
     * @return 文件路径
     */
    public String getUserFilePath(boolean useProjectLevel) {
        return useProjectLevel ? getProjectMemoryFilePath(USER_FILE) : getGlobalMemoryFilePath(USER_FILE);
    }

    /**
     * 获取SOUL.md文件路径
     * @param useProjectLevel 是否使用项目级文件
     * @return 文件路径
     */
    public String getSoulFilePath(boolean useProjectLevel) {
        return useProjectLevel ? getProjectMemoryFilePath(SOUL_FILE) : getGlobalMemoryFilePath(SOUL_FILE);
    }
    
    /**
     * 获取IDENTITY.md文件路径
     * @param useProjectLevel 是否使用项目级文件
     * @return 文件路径
     */
    public String getIdentityFilePath(boolean useProjectLevel) {
        return useProjectLevel ? getProjectMemoryFilePath(IDENTITY_FILE) : getGlobalMemoryFilePath(IDENTITY_FILE);
    }

    /**
     * 检查长期记忆文件是否存在
     * @param fileName 文件名
     * @param useProjectLevel 是否使用项目级文件
     * @return 是否存在
     */
    public boolean exists(String fileName, boolean useProjectLevel) {
        String filePath = useProjectLevel ? getProjectMemoryFilePath(fileName) : getGlobalMemoryFilePath(fileName);
        return new File(filePath).exists();
    }
}
