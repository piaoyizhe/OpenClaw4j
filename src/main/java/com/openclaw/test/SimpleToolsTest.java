package com.openclaw.test;

import com.openclaw.tools.ToolManagerRegistry;

/**
 * 简单工具测试类
 */
public class SimpleToolsTest {
    public static void main(String[] args) {
        try {
            System.out.println("测试ToolManagerRegistry加载工具配置...");
            ToolManagerRegistry toolRegistry = ToolManagerRegistry.getInstance();
            
            // 打印工具数量
            System.out.println("加载的工具数量: " + toolRegistry.getAllTools().size());
            
            // 打印工具信息
            System.out.println("工具信息 (JSON格式):");
            System.out.println(toolRegistry.getToolsInfoAsJsonArray().toString());
            
            System.out.println("测试完成!");
            
        } catch (Exception e) {
            System.err.println("测试时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}