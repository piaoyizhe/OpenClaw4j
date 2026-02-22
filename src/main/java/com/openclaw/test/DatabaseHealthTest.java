package com.openclaw.test;

import com.openclaw.model.util.DatabaseHealthChecker;

import java.util.Map;

/**
 * 数据库健康检查测试
 */
public class DatabaseHealthTest {
    public static void main(String[] args) {
        System.out.println("开始执行SQLite数据库健康检查...");
        
        // 创建数据库健康检查器
        DatabaseHealthChecker healthChecker = new DatabaseHealthChecker();
        
        // 执行健康检查
        Map<String, Object> report = healthChecker.performHealthCheck();
        
        // 打印健康检查报告
        healthChecker.printHealthReport(report);
        
        System.out.println("数据库健康检查完成！");
    }
}