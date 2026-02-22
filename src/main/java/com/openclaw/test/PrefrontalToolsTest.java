package com.openclaw.test;

import com.openclaw.brain.Prefrontal;

/**
 * Prefrontal工具测试类
 */
public class PrefrontalToolsTest {
    public static void main(String[] args) {
        try {
            System.out.println("测试Prefrontal加载工具信息...");
            Prefrontal prefrontal = Prefrontal.getInstance();
            
            // 测试生成包含Shell命令的任务列表
            String query1 = "执行Shell命令查看当前目录文件列表";
            System.out.println("\n测试1: " + query1);
            prefrontal.processQuery(query1);
            
            // 测试生成包含HTTP请求的任务列表
            String query2 = "发送HTTP请求获取GitHub用户octocat的信息";
            System.out.println("\n测试2: " + query2);
            prefrontal.processQuery(query2);
            
            // 测试生成包含多个工具的任务列表
            String query3 = "先执行Shell命令查看当前目录，然后查询关于Java的记忆";
            System.out.println("\n测试3: " + query3);
            prefrontal.processQuery(query3);
            
            System.out.println("\n测试完成!");
            
        } catch (Exception e) {
            System.err.println("测试时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}