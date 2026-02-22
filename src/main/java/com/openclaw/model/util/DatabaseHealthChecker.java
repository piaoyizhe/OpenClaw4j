package com.openclaw.model.util;

import com.openclaw.model.manager.SQLiteManager;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库健康检查器
 * 负责检查SQLite数据库的连接状态、表结构和数据完整性
 */
public class DatabaseHealthChecker {
    private SQLiteManager sqliteManager;
    private Connection connection;

    /**
     * 构造方法
     */
    public DatabaseHealthChecker() {
        sqliteManager = SQLiteManager.getInstance();
        connection = sqliteManager.getConnection();
    }

    /**
     * 执行全面数据库健康检查
     * @return 健康检查报告
     */
    public Map<String, Object> performHealthCheck() {
        Map<String, Object> report = new HashMap<>();
        
        try {
            // 检查数据库连接状态
            checkConnection(report);
            
            // 检查表结构
            checkTableStructure(report);
            
            // 检查数据完整性
            checkDataIntegrity(report);
            
            // 检查查询性能
            checkQueryPerformance(report);
            
            // 生成健康状态摘要
            generateHealthSummary(report);
            
        } catch (Exception e) {
            report.put("overall_status", "ERROR");
            report.put("error_message", e.getMessage());
            System.err.println("数据库健康检查失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return report;
    }

    /**
     * 检查数据库连接状态
     * @param report 检查报告
     */
    private void checkConnection(Map<String, Object> report) {
        Map<String, Object> connectionStatus = new HashMap<>();
        
        try {
            if (connection != null && !connection.isClosed()) {
                connectionStatus.put("status", "OK");
                connectionStatus.put("message", "数据库连接正常");
                connectionStatus.put("url", connection.getMetaData().getURL());
                connectionStatus.put("driver", connection.getMetaData().getDriverName());
                connectionStatus.put("version", connection.getMetaData().getDriverVersion());
            } else {
                connectionStatus.put("status", "ERROR");
                connectionStatus.put("message", "数据库连接失败或已关闭");
            }
        } catch (SQLException e) {
            connectionStatus.put("status", "ERROR");
            connectionStatus.put("message", "检查数据库连接失败: " + e.getMessage());
        }
        
        report.put("connection", connectionStatus);
    }

    /**
     * 检查表结构
     * @param report 检查报告
     */
    private void checkTableStructure(Map<String, Object> report) {
        Map<String, Object> tableStatus = new HashMap<>();
        
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 检查chunks表
            checkTableExists(metaData, tableStatus, "chunks");
            
            // 检查metadata表
            checkTableExists(metaData, tableStatus, "metadata");
            
            // 检查fts5_index表
            checkTableExists(metaData, tableStatus, "fts5_index");
            
        } catch (SQLException e) {
            tableStatus.put("overall_status", "ERROR");
            tableStatus.put("error_message", "检查表结构失败: " + e.getMessage());
        }
        
        report.put("table_structure", tableStatus);
    }

    /**
     * 检查指定表是否存在
     * @param metaData 数据库元数据
     * @param tableStatus 表状态报告
     * @param tableName 表名
     */
    private void checkTableExists(DatabaseMetaData metaData, Map<String, Object> tableStatus, String tableName) throws SQLException {
        ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE", "VIEW"});
        if (rs.next()) {
            Map<String, Object> tableInfo = new HashMap<>();
            tableInfo.put("exists", true);
            tableInfo.put("table_type", rs.getString("TABLE_TYPE"));
            
            // 检查表的列
            checkTableColumns(metaData, tableInfo, tableName);
            
            tableStatus.put(tableName, tableInfo);
        } else {
            Map<String, Object> tableInfo = new HashMap<>();
            tableInfo.put("exists", false);
            tableInfo.put("error", "表不存在");
            tableStatus.put(tableName, tableInfo);
        }
        rs.close();
    }

    /**
     * 检查表的列结构
     * @param metaData 数据库元数据
     * @param tableInfo 表信息
     * @param tableName 表名
     */
    private void checkTableColumns(DatabaseMetaData metaData, Map<String, Object> tableInfo, String tableName) throws SQLException {
        ResultSet rs = metaData.getColumns(null, null, tableName, null);
        int columnCount = 0;
        while (rs.next()) {
            columnCount++;
        }
        rs.close();
        tableInfo.put("column_count", columnCount);
    }

    /**
     * 检查数据完整性
     * @param report 检查报告
     */
    private void checkDataIntegrity(Map<String, Object> report) {
        Map<String, Object> dataStatus = new HashMap<>();
        
        try {
            // 检查chunks表数据
            checkTableData(dataStatus, "chunks");
            
            // 检查metadata表数据
            checkTableData(dataStatus, "metadata");
            
            // 检查fts5_index表数据
            checkTableData(dataStatus, "fts5_index");
            
        } catch (SQLException e) {
            dataStatus.put("overall_status", "ERROR");
            dataStatus.put("error_message", "检查数据完整性失败: " + e.getMessage());
        }
        
        report.put("data_integrity", dataStatus);
    }

    /**
     * 检查表数据
     * @param dataStatus 数据状态报告
     * @param tableName 表名
     */
    private void checkTableData(Map<String, Object> dataStatus, String tableName) throws SQLException {
        Map<String, Object> tableData = new HashMap<>();
        
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
            
            if (rs.next()) {
                int rowCount = rs.getInt(1);
                tableData.put("row_count", rowCount);
                tableData.put("status", rowCount > 0 ? "OK" : "WARNING");
                tableData.put("message", rowCount > 0 ? "表中有数据" : "表中无数据");
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            tableData.put("status", "ERROR");
            tableData.put("error_message", "检查表数据失败: " + e.getMessage());
        }
        
        dataStatus.put(tableName, tableData);
    }

    /**
     * 检查查询性能
     * @param report 检查报告
     */
    private void checkQueryPerformance(Map<String, Object> report) {
        Map<String, Object> performanceStatus = new HashMap<>();
        
        try {
            // 测试简单查询性能
            long startTime = System.currentTimeMillis();
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM chunks");
            if (rs.next()) {
                // 记录查询结果
            }
            rs.close();
            stmt.close();
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            performanceStatus.put("simple_query_time_ms", executionTime);
            performanceStatus.put("performance_status", executionTime < 1000 ? "GOOD" : "WARNING");
            
        } catch (SQLException e) {
            performanceStatus.put("status", "ERROR");
            performanceStatus.put("error_message", "检查查询性能失败: " + e.getMessage());
        }
        
        report.put("query_performance", performanceStatus);
    }

    /**
     * 生成健康状态摘要
     * @param report 检查报告
     */
    private void generateHealthSummary(Map<String, Object> report) {
        // 分析各部分状态
        boolean hasErrors = false;
        boolean hasWarnings = false;
        
        // 检查连接状态
        Map<String, Object> connectionStatus = (Map<String, Object>) report.get("connection");
        if (connectionStatus != null && "ERROR".equals(connectionStatus.get("status"))) {
            hasErrors = true;
        }
        
        // 检查表结构
        Map<String, Object> tableStatus = (Map<String, Object>) report.get("table_structure");
        if (tableStatus != null) {
            for (Map.Entry<String, Object> entry : tableStatus.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> tableInfo = (Map<String, Object>) entry.getValue();
                    if (tableInfo.containsKey("exists") && !((Boolean) tableInfo.get("exists"))) {
                        hasErrors = true;
                    }
                }
            }
        }
        
        // 检查数据完整性
        Map<String, Object> dataStatus = (Map<String, Object>) report.get("data_integrity");
        if (dataStatus != null) {
            for (Map.Entry<String, Object> entry : dataStatus.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> tableData = (Map<String, Object>) entry.getValue();
                    if ("ERROR".equals(tableData.get("status"))) {
                        hasErrors = true;
                    } else if ("WARNING".equals(tableData.get("status"))) {
                        hasWarnings = true;
                    }
                }
            }
        }
        
        // 确定整体状态
        if (hasErrors) {
            report.put("overall_status", "ERROR");
            report.put("health_summary", "数据库存在严重问题，需要立即修复");
        } else if (hasWarnings) {
            report.put("overall_status", "WARNING");
            report.put("health_summary", "数据库存在一些警告，但可以正常运行");
        } else {
            report.put("overall_status", "HEALTHY");
            report.put("health_summary", "数据库状态良好，运行正常");
        }
    }

    /**
     * 打印健康检查报告
     * @param report 检查报告
     */
    public void printHealthReport(Map<String, Object> report) {
        System.out.println("\n===== SQLite数据库健康状态报告 =====");
        System.out.println("整体状态: " + report.get("overall_status"));
        System.out.println("健康摘要: " + report.get("health_summary"));
        
        if (report.containsKey("error_message")) {
            System.out.println("错误信息: " + report.get("error_message"));
        }
        
        // 打印连接状态
        System.out.println("\n--- 连接状态 ---");
        Map<String, Object> connectionStatus = (Map<String, Object>) report.get("connection");
        if (connectionStatus != null) {
            System.out.println("状态: " + connectionStatus.get("status"));
            System.out.println("消息: " + connectionStatus.get("message"));
            if (connectionStatus.containsKey("url")) {
                System.out.println("连接URL: " + connectionStatus.get("url"));
            }
            if (connectionStatus.containsKey("driver")) {
                System.out.println("驱动: " + connectionStatus.get("driver"));
            }
        }
        
        // 打印表结构状态
        System.out.println("\n--- 表结构状态 ---");
        Map<String, Object> tableStatus = (Map<String, Object>) report.get("table_structure");
        if (tableStatus != null) {
            for (Map.Entry<String, Object> entry : tableStatus.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> tableInfo = (Map<String, Object>) entry.getValue();
                    System.out.println(entry.getKey() + ": " + tableInfo.get("exists"));
                    if (tableInfo.containsKey("column_count")) {
                        System.out.println("  列数: " + tableInfo.get("column_count"));
                    }
                    if (tableInfo.containsKey("error")) {
                        System.out.println("  错误: " + tableInfo.get("error"));
                    }
                }
            }
        }
        
        // 打印数据完整性状态
        System.out.println("\n--- 数据完整性状态 ---");
        Map<String, Object> dataStatus = (Map<String, Object>) report.get("data_integrity");
        if (dataStatus != null) {
            for (Map.Entry<String, Object> entry : dataStatus.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> tableData = (Map<String, Object>) entry.getValue();
                    System.out.println(entry.getKey() + ": " + tableData.get("row_count") + " 行");
                    System.out.println("  状态: " + tableData.get("status"));
                    if (tableData.containsKey("message")) {
                        System.out.println("  消息: " + tableData.get("message"));
                    }
                }
            }
        }
        
        // 打印查询性能状态
        System.out.println("\n--- 查询性能状态 ---");
        Map<String, Object> performanceStatus = (Map<String, Object>) report.get("query_performance");
        if (performanceStatus != null) {
            System.out.println("简单查询时间: " + performanceStatus.get("simple_query_time_ms") + " ms");
            System.out.println("性能状态: " + performanceStatus.get("performance_status"));
        }
        
        System.out.println("\n===== 健康检查报告结束 =====");
    }
}