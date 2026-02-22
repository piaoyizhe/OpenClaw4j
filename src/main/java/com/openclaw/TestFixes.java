package com.openclaw;

import com.openclaw.brain.Prefrontal;
import com.openclaw.model.manager.InitializationManager;
import com.openclaw.model.manager.MemoryManager;

public class TestFixes {
    public static void main(String[] args) {
        try {
            // 初始化系统
            System.out.println("正在初始化系统...");
            InitializationManager initializationManager = InitializationManager.getInstance();
            initializationManager.initialize();
            MemoryManager memoryManager = MemoryManager.getInstance();
            System.out.println("系统初始化完成");

            // 测试1: 天气查询任务
            System.out.println("\n测试1: 天气查询任务");
            Prefrontal prefrontal = Prefrontal.getInstance();
            String weatherResult = prefrontal.processQuery("北京天气");
            System.out.println("天气查询结果: " + weatherResult);

            // 测试2: 变量替换
            System.out.println("\n测试2: 变量替换");
            // 先执行一个任务，然后在后续任务中引用其结果
            String task1Result = prefrontal.processQuery("获取本地IP");
            System.out.println("任务1结果: " + task1Result);

            // 测试3: 任务重复检查
            System.out.println("\n测试3: 任务重复检查");
            // 连续执行相同的天气查询，应该只创建一个任务
            String weatherResult2 = prefrontal.processQuery("北京天气");
            System.out.println("第二次天气查询结果: " + weatherResult2);

            // 测试4: 搜索功能（验证SQL修复）
            System.out.println("\n测试4: 搜索功能");
            String searchResult = prefrontal.processQuery("搜索Java相关的记忆");
            System.out.println("搜索结果: " + searchResult);

            System.out.println("\n所有测试完成！");

        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
