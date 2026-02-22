package com.openclaw.utils;

import com.openclaw.config.ConfigManager;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * 工作工具类
 * 实现日报和周报的创建、读取功能
 */
public class WorkUtils {
    private static WorkUtils instance;
    private ConfigManager configManager;
    private String memoryDirectory;

    /**
     * 私有构造方法
     */
    private WorkUtils() {
        configManager = ConfigManager.getInstance();
        memoryDirectory = configManager.getMemoryDirectory();
        ensureDirectoryExists();
    }

    /**
     * 获取单例实例
     * @return WorkUtils实例
     */
    public static WorkUtils getInstance() {
        if (instance == null) {
            synchronized (WorkUtils.class) {
                if (instance == null) {
                    instance = new WorkUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 确保目录存在
     */
    private void ensureDirectoryExists() {
        Path path = Paths.get(memoryDirectory);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("创建记忆目录: " + memoryDirectory);
            } catch (IOException e) {
                System.err.println("创建目录失败: " + e.getMessage());
            }
        }
    }

    /**
     * 写日报
     * @param content 日报内容
     * @param date 日期（格式：yyyy-MM-dd），null表示当天
     * @return 写入结果
     */
    public String writeDailyReport(String content, String date) {
        try {
            // 确定日期
            String reportDate = date;
            if (reportDate == null || reportDate.isEmpty()) {
                reportDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            // 生成文件路径
            Path filePath = Paths.get(memoryDirectory, reportDate + ".md");

            // 写入内容
            StringBuilder reportContent = new StringBuilder();
            reportContent.append("# 日报 - " + reportDate + "\n\n");
            reportContent.append("## 工作内容\n" + content + "\n\n");
            reportContent.append("## 完成情况\n- [ ] 任务1\n- [ ] 任务2\n- [ ] 任务3\n\n");
            reportContent.append("## 明日计划\n- [ ] 任务1\n- [ ] 任务2\n- [ ] 任务3\n\n");
            reportContent.append("## 遇到的问题\n- 无\n\n");
            reportContent.append("## 备注\n- 无\n");

            Files.write(filePath, reportContent.toString().getBytes("UTF-8"));

            return "日报写入成功\n文件路径: " + filePath.toString();
        } catch (Exception e) {
            return "日报写入失败: " + e.getMessage();
        }
    }

    /**
     * 读日报
     * @param date 日期（格式：yyyy-MM-dd），null表示当天
     * @return 日报内容
     */
    public String readDailyReport(String date) {
        try {
            // 确定日期
            String reportDate = date;
            if (reportDate == null || reportDate.isEmpty()) {
                reportDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            // 生成文件路径
            Path filePath = Paths.get(memoryDirectory, reportDate + ".md");

            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                return "日报文件不存在: " + reportDate;
            }

            // 读取内容
            String content = new String(Files.readAllBytes(filePath), "UTF-8");
            return "日报内容:\n" + content;
        } catch (Exception e) {
            return "日报读取失败: " + e.getMessage();
        }
    }

    /**
     * 写周报
     * @param content 周报内容
     * @param year 年份，null表示今年
     * @param week 周数，null表示本周
     * @return 写入结果
     */
    public String writeWeeklyReport(String content, String year, String week) {
        try {
            // 确定年份和周数
            LocalDate now = LocalDate.now();
            int reportYear = year != null && !year.isEmpty() ? Integer.parseInt(year) : now.getYear();
            int reportWeek;

            if (week != null && !week.isEmpty()) {
                reportWeek = Integer.parseInt(week);
            } else {
                // 获取本周周数
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                reportWeek = now.get(weekFields.weekOfWeekBasedYear());
            }

            // 生成文件路径
            String fileName = reportYear + "-W" + String.format("%02d", reportWeek) + ".md";
            Path filePath = Paths.get(memoryDirectory, fileName);

            // 写入内容
            StringBuilder reportContent = new StringBuilder();
            reportContent.append("# 周报 - " + reportYear + "年第" + reportWeek + "周\n\n");
            reportContent.append("## 本周工作内容\n" + content + "\n\n");
            reportContent.append("## 完成情况\n- [ ] 任务1\n- [ ] 任务2\n- [ ] 任务3\n\n");
            reportContent.append("## 下周计划\n- [ ] 任务1\n- [ ] 任务2\n- [ ] 任务3\n\n");
            reportContent.append("## 遇到的问题\n- 无\n\n");
            reportContent.append("## 备注\n- 无\n");

            Files.write(filePath, reportContent.toString().getBytes("UTF-8"));

            return "周报写入成功\n文件路径: " + filePath.toString();
        } catch (Exception e) {
            return "周报写入失败: " + e.getMessage();
        }
    }

    /**
     * 读周报
     * @param year 年份，null表示今年
     * @param week 周数，null表示本周
     * @return 周报内容
     */
    public String readWeeklyReport(String year, String week) {
        try {
            // 确定年份和周数
            LocalDate now = LocalDate.now();
            int reportYear = year != null && !year.isEmpty() ? Integer.parseInt(year) : now.getYear();
            int reportWeek;

            if (week != null && !week.isEmpty()) {
                reportWeek = Integer.parseInt(week);
            } else {
                // 获取本周周数
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                reportWeek = now.get(weekFields.weekOfWeekBasedYear());
            }

            // 生成文件路径
            String fileName = reportYear + "-W" + String.format("%02d", reportWeek) + ".md";
            Path filePath = Paths.get(memoryDirectory, fileName);

            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                return "周报文件不存在: " + fileName;
            }

            // 读取内容
            String content = new String(Files.readAllBytes(filePath), "UTF-8");
            return "周报内容:\n" + content;
        } catch (Exception e) {
            return "周报读取失败: " + e.getMessage();
        }
    }

    /**
     * 列出所有日报
     * @return 日报列表
     */
    public String listDailyReports() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("## 日报列表\n");

            Path dirPath = Paths.get(memoryDirectory);
            if (!Files.exists(dirPath)) {
                return "记忆目录不存在";
            }

            int count = 0;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.md")) {
                for (Path entry : stream) {
                    String fileName = entry.getFileName().toString();
                    // 匹配日期格式的文件
                    if (fileName.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}[.]md")) {
                        result.append("- " + fileName + "\n");
                        count++;
                    }
                }
            }

            if (count == 0) {
                result.append("- 无日报文件\n");
            } else {
                result.append("\n共 " + count + " 个日报文件\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "列出日报失败: " + e.getMessage();
        }
    }

    /**
     * 列出所有周报
     * @return 周报列表
     */
    public String listWeeklyReports() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("## 周报列表\n");

            Path dirPath = Paths.get(memoryDirectory);
            if (!Files.exists(dirPath)) {
                return "记忆目录不存在";
            }

            int count = 0;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.md")) {
                for (Path entry : stream) {
                    String fileName = entry.getFileName().toString();
                    // 匹配周报格式的文件
                    if (fileName.matches("[0-9]{4}-W[0-9]{2,}[.]md")) {
                        result.append("- " + fileName + "\n");
                        count++;
                    }
                }
            }

            if (count == 0) {
                result.append("- 无周报文件\n");
            } else {
                result.append("\n共 " + count + " 个周报文件\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "列出周报失败: " + e.getMessage();
        }
    }
}