package com.openclaw.model.service;

import com.openclaw.config.ConfigManager;
import com.openclaw.model.manager.LongTermMemoryManager;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * 经验值管理器
 * 负责跟踪特定事件的发生次数，并在达到阈值时将其保存到长期记忆中
 */
public class ExperienceManager {
    private static ExperienceManager instance;
    private Map<String, Integer> eventCounter; // 事件计数器
    private Map<String, Long> eventLastOccurred; // 事件最后发生时间
    private final int DEFAULT_THRESHOLD = 3; // 默认阈值
    private String experienceFile; // 经验值存储文件

    /**
     * 私有构造方法
     */
    private ExperienceManager() {
        eventCounter = new HashMap<>();
        eventLastOccurred = new HashMap<>();
        // 延迟初始化经验值文件路径，避免循环依赖
        experienceFile = null;
        loadExperienceData();
    }
    
    /**
     * 初始化经验值文件路径
     */
    private void initializeExperienceFile() {
        if (experienceFile == null) {
            // 从配置管理器直接获取记忆目录，避免循环依赖
            ConfigManager configManager = ConfigManager.getInstance();
            String memoryDirectory = configManager.getMemoryDirectory();
            experienceFile = memoryDirectory + File.separator + "experience.json";
        }
    }

    /**
     * 获取单例实例
     * @return ExperienceManager实例
     */
    public static ExperienceManager getInstance() {
        if (instance == null) {
            synchronized (ExperienceManager.class) {
                if (instance == null) {
                    instance = new ExperienceManager();
                }
            }
        }
        return instance;
    }

    /**
     * 记录事件
     * @param eventType 事件类型
     * @param eventDescription 事件描述
     * @return 是否达到阈值并保存到长期记忆
     */
    public boolean recordEvent(String eventType, String eventDescription) {
        // 增加事件计数
        int count = eventCounter.getOrDefault(eventType, 0) + 1;
        eventCounter.put(eventType, count);
        eventLastOccurred.put(eventType, System.currentTimeMillis());

        // 保存经验值数据
        saveExperienceData();

        // 检查是否达到阈值
        if (count >= DEFAULT_THRESHOLD) {
            // 达到阈值，保存到长期记忆
            saveToLongTermMemory(eventType, eventDescription, count);
            return true;
        }

        return false;
    }

    /**
     * 保存到长期记忆
     * @param eventType 事件类型
     * @param eventDescription 事件描述
     * @param occurrenceCount 发生次数
     */
    private void saveToLongTermMemory(String eventType, String eventDescription, int occurrenceCount) {
        try {
            LongTermMemoryManager longTermMemoryManager = LongTermMemoryManager.getInstance();
            
            // 构建记忆内容
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String memoryContent = String.format("[%s] 事件类型: %s, 描述: %s, 发生次数: %d次", 
                timestamp, eventType, eventDescription, occurrenceCount);

            // 读取当前MEMORY.md内容
            String currentContent = longTermMemoryManager.readLongTermMemory("MEMORY.md", true);

            // 检查内容是否已存在
            if (!currentContent.contains(eventType + ": " + eventDescription)) {
                // 构建经验值记忆条目
                String experienceEntry = "\n- " + memoryContent;

                // 查找或创建经验值部分
                String updatedContent;
                if (currentContent.contains("## 经验值记录\n")) {
                    // 如果已存在经验值部分，添加到该部分
                    updatedContent = currentContent.replace("## 经验值记录\n", "## 经验值记录\n" + experienceEntry);
                } else {
                    // 如果不存在经验值部分，添加新部分
                    updatedContent = currentContent + "\n## 经验值记录\n" + experienceEntry;
                }

                // 写入到MEMORY.md
                boolean success = longTermMemoryManager.writeLongTermMemory("MEMORY.md", updatedContent, true);
                if (success) {
                    System.out.println("经验值事件已保存到长期记忆: " + eventType);
                    
                    // 重置该事件的计数（可选）
                    // eventCounter.put(eventType, 0);
                }
            }

        } catch (Exception e) {
            System.err.println("保存经验值到长期记忆失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取事件计数
     * @param eventType 事件类型
     * @return 事件发生次数
     */
    public int getEventCount(String eventType) {
        return eventCounter.getOrDefault(eventType, 0);
    }

    /**
     * 获取所有事件计数
     * @return 事件计数映射
     */
    public Map<String, Integer> getAllEventCounts() {
        return new HashMap<>(eventCounter);
    }

    /**
     * 重置事件计数
     * @param eventType 事件类型
     */
    public void resetEventCount(String eventType) {
        eventCounter.put(eventType, 0);
        eventLastOccurred.remove(eventType);
        saveExperienceData();
    }

    /**
     * 重置所有事件计数
     */
    public void resetAllEventCounts() {
        eventCounter.clear();
        eventLastOccurred.clear();
        saveExperienceData();
    }

    /**
     * 加载经验值数据
     */
    private void loadExperienceData() {
        initializeExperienceFile();
        File file = new File(experienceFile);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            String eventType = parts[0].trim();
                            int count = Integer.parseInt(parts[1].trim());
                            eventCounter.put(eventType, count);
                            eventLastOccurred.put(eventType, System.currentTimeMillis());
                        }
                    }
                }
                System.out.println("经验值数据加载完成，共加载 " + eventCounter.size() + " 个事件");
            } catch (IOException e) {
                System.err.println("加载经验值数据失败: " + e.getMessage());
                // 初始化空数据
                eventCounter = new HashMap<>();
                eventLastOccurred = new HashMap<>();
            }
        }
    }

    /**
     * 保存经验值数据
     */
    private void saveExperienceData() {
        initializeExperienceFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(experienceFile))) {
            for (Map.Entry<String, Integer> entry : eventCounter.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("保存经验值数据失败: " + e.getMessage());
        }
    }

    /**
     * 提取事件描述
     * @param content 消息内容
     * @param pattern 匹配的模式
     * @return 事件描述
     */
    private String extractEventDescription(String content, String pattern) {
        // 简单实现：返回包含模式的前50个字符
        int index = content.indexOf(pattern);
        if (index != -1) {
            int endIndex = Math.min(index + pattern.length() + 30, content.length());
            return content.substring(Math.max(0, index - 10), endIndex).trim();
        }
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }

    /**
     * 生成经验值报告
     * @return 经验值报告
     */
    public String generateExperienceReport() {
        StringBuilder report = new StringBuilder();
        report.append("# 经验值报告\n\n");
        report.append("生成时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n\n");
        
        if (eventCounter.isEmpty()) {
            report.append("暂无经验值记录\n");
        } else {
            report.append("## 事件统计\n\n");
            report.append("| 事件类型 | 发生次数 | 最后发生时间 | 距阈值还需 |\n");
            report.append("|---------|---------|------------|----------|\n");
            
            for (Map.Entry<String, Integer> entry : eventCounter.entrySet()) {
                String eventType = entry.getKey();
                int count = entry.getValue();
                long lastOccurred = eventLastOccurred.getOrDefault(eventType, System.currentTimeMillis());
                String lastOccurredStr = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(lastOccurred));
                int remaining = Math.max(0, DEFAULT_THRESHOLD - count);
                
                report.append(String.format("| %s | %d | %s | %d |\n", 
                    eventType, count, lastOccurredStr, remaining));
            }
        }
        
        return report.toString();
    }

    /**
     * 保存经验值报告到文件
     * @return 报告文件路径
     */
    public String saveExperienceReport() {
        String report = generateExperienceReport();
        // 从配置管理器直接获取记忆目录，避免循环依赖
        initializeExperienceFile();
        String memoryDirectory = experienceFile.substring(0, experienceFile.lastIndexOf(File.separator));
        String reportFile = memoryDirectory + File.separator + 
            "experience_report_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".md";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
            writer.write(report);
            System.out.println("经验值报告已保存到: " + reportFile);
            return reportFile;
        } catch (IOException e) {
            System.err.println("保存经验值报告失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取默认阈值
     * @return 默认阈值
     */
    public int getDefaultThreshold() {
        return DEFAULT_THRESHOLD;
    }
}