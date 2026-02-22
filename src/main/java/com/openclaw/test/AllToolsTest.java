package com.openclaw.test;

import com.openclaw.brain.Prefrontal;

/**
 * 所有工具测试类
 */
public class AllToolsTest {
    public static void main(String[] args) {
        try {
            System.out.println("测试所有工具，包括查找记忆和组织语言工具...");
            Prefrontal prefrontal = Prefrontal.getInstance();
            
            // 测试生成包含查找记忆的任务列表
            String query1 = "查询关于Java的记忆";
            System.out.println("\n测试1: " + query1);
            prefrontal.processQuery(query1);
            
            // 测试生成包含组织语言的任务列表
            String query2 = "组织一段关于人工智能的回答";
            System.out.println("\n测试2: " + query2);
            prefrontal.processQuery(query2);
            
            // 测试生成包含多个工具的任务列表
            String query3 = "先查询关于Python的记忆，然后组织一段回答";
            System.out.println("\n测试3: " + query3);
            prefrontal.processQuery(query3);
            
            // 测试生成包含Shell命令和查找记忆的任务列表
            String query4 = "先执行Shell命令查看当前目录，然后查询关于Java的记忆";
            System.out.println("\n测试4: " + query4);
            prefrontal.processQuery(query4);
            
            System.out.println("\n测试完成!");
            
        } catch (Exception e) {
            System.err.println("测试时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}