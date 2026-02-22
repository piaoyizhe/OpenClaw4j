package com.openclaw;

import com.openclaw.console.ConsoleInterface;
import com.openclaw.model.manager.MemoryManager;
import com.openclaw.utils.ApplicationContext;

public class Main {
    public static void main(String[] args) {
        try {
            // 初始化应用程序上下文
            System.out.println("正在初始化OpenClaw...");
            
            ApplicationContext context = ApplicationContext.getInstance();

            // 初始化记忆管理器
            MemoryManager memoryManager = context.getComponent(MemoryManager.class);

            System.out.println("记忆管理器初始化完成");
            
            // 验证记忆存储目录
            if (!memoryManager.isMemoryDirectoryWritable()) {
                System.err.println("警告: 记忆存储目录不可写，对话记录可能无法保存");
            }

            // 启动控制台交互
            System.out.println();
            ConsoleInterface consoleInterface = new ConsoleInterface();
            consoleInterface.start();
            
        } catch (Exception e) {
            System.err.println("程序启动失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
