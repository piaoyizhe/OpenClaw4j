package com.openclaw.model.manager;

import com.openclaw.config.ConfigManager;
import com.openclaw.model.manager.LongTermMemoryManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 初始化管理器
 * 负责在第一次启动时执行初始化操作
 */
public class InitializationManager {
    private static InitializationManager instance;
    private ConfigManager configManager;
    private LongTermMemoryManager longTermMemoryManager;
    private boolean initialized;

    /**
     * 私有构造方法
     */
    private InitializationManager() {
        this.configManager = ConfigManager.getInstance();
        this.longTermMemoryManager = LongTermMemoryManager.getInstance();
        this.initialized = false;
    }

    /**
     * 获取单例实例
     * @return InitializationManager实例
     */
    public static InitializationManager getInstance() {
        if (instance == null) {
            synchronized (InitializationManager.class) {
                if (instance == null) {
                    instance = new InitializationManager();
                }
            }
        }
        return instance;
    }

    /**
     * 执行初始化操作
     */
    public void initialize() {
        if (initialized) {
            System.out.println("系统已经初始化过，跳过初始化操作");
            return;
        }

        System.out.println("===== OpenClaw 系统初始化 =====");

        // 1. 自动创建必要的文件
        System.out.println("1. 自动创建必要的文件...");

        // 2. 引导用户输入一些内容（仅当USER.md文件不存在或不包含用户信息时）
        if (!isUserInfoExists()) {
            System.out.println("2. 引导用户输入初始化信息...");
            try {
               引导用户输入();
            } catch (IOException e) {
                System.err.println("用户输入失败: " + e.getMessage());
            }
        } else {
            System.out.println("2. 用户信息已存在，跳过引导输入...");
        }

        // 3. 优化默认内容
        System.out.println("3. 优化默认内容...");
        优化默认内容();

        initialized = true;
        System.out.println("===== 初始化完成 =====");
    }

    /**
     * 检查用户信息是否已存在
     * @return 如果USER.md文件存在且包含用户信息，则返回true
     */
    private boolean isUserInfoExists() {
        try {
            // 检查USER.md文件是否存在
          return longTermMemoryManager.exists("USER.md", true);
        } catch (Exception e) {
            System.err.println("检查用户信息失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 引导用户输入初始化信息
     */
    private void 引导用户输入() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // 示例：引导用户输入用户名
        System.out.print("请输入您的姓名: ");
        String userName = reader.readLine();
        if (!userName.isEmpty()) {
            // 更新USER.md中的用户信息
            String userContent = longTermMemoryManager.readLongTermMemory("USER.md", true);
            if (userContent.contains("## 基本信息")) {
                userContent = userContent.replace("## 基本信息\n\n", "## 基本信息\n\n- **姓名:** " + userName + "\n");
                longTermMemoryManager.writeLongTermMemory("USER.md", userContent, true);
                System.out.println("用户姓名已更新到USER.md");
            }
        }

        // 示例：引导用户输入职业
        System.out.print("请输入您的职业: ");
        String userJob = reader.readLine();
        if (!userJob.isEmpty()) {
            // 更新USER.md中的职业信息
            String userContent = longTermMemoryManager.readLongTermMemory("USER.md", true);
            if (userContent.contains("## 专业背景")) {
                userContent = userContent.replace("## 专业背景\n\n", "## 专业背景\n\n- **职业:** " + userJob + "\n");
                longTermMemoryManager.writeLongTermMemory("USER.md", userContent, true);
                System.out.println("用户职业已更新到USER.md");
            }
        }
    }

    /**
     * 优化默认内容
     */
    private void 优化默认内容() {
        // 这里可以添加优化默认内容的逻辑
        // 例如，根据用户输入的信息更新默认内容
    }

    /**
     * 检查是否已经初始化
     * @return 是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }
}
