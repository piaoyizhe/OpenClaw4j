package com.openclaw.model;

import com.openclaw.config.ConfigManager;
import com.openclaw.utils.LLMClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 记忆压缩管理器
 * 负责压缩记忆内容，保留原始内容用于追溯
 */
public class MemoryCompressionManager {
    private static MemoryCompressionManager instance;
    private ConfigManager configManager;
    private LLMClient llmClient;
    private String memoryDirectory;
    private String archiveDirectory;
    private String indexFilePath;
    
    // 默认压缩阈值（字符数）
    private static final int DEFAULT_THRESHOLD = 8000;
    
    /**
     * 压缩记录
     */
    public static class CompressionRecord {
        public String fileName;
        public String originalFilePath;
        public Date compressionDate;
        public int originalLength;
        public int compressedLength;
        public String compressionRatio;
        
        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("fileName", fileName);
            json.put("originalFilePath", originalFilePath);
            json.put("compressionDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(compressionDate));
            json.put("originalLength", originalLength);
            json.put("compressedLength", compressedLength);
            json.put("compressionRatio", compressionRatio);
            return json;
        }
        
        public static CompressionRecord fromJSON(JSONObject json) {
            CompressionRecord record = new CompressionRecord();
            record.fileName = json.getString("fileName");
            record.originalFilePath = json.getString("originalFilePath");
            try {
                record.compressionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(json.getString("compressionDate"));
            } catch (Exception e) {
                record.compressionDate = new Date();
            }
            record.originalLength = json.getInt("originalLength");
            record.compressedLength = json.getInt("compressedLength");
            record.compressionRatio = json.getString("compressionRatio");
            return record;
        }
    }
    
    /**
     * 私有构造方法
     */
    private MemoryCompressionManager() {
        configManager = ConfigManager.getInstance();
        llmClient = LLMClient.getInstance();
        memoryDirectory = configManager.getMemoryDirectory();
        archiveDirectory = memoryDirectory + File.separator + "archive";
        indexFilePath = memoryDirectory + File.separator + "memory_index.json";
        
        initializeDirectories();
    }
    
    /**
     * 获取单例实例
     * @return MemoryCompressionManager实例
     */
    public static MemoryCompressionManager getInstance() {
        if (instance == null) {
            synchronized (MemoryCompressionManager.class) {
                if (instance == null) {
                    instance = new MemoryCompressionManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化目录
     */
    private void initializeDirectories() {
        File archiveDir = new File(archiveDirectory);
        if (!archiveDir.exists()) {
            if (!archiveDir.mkdirs()) {
                System.err.println("创建归档目录失败: " + archiveDirectory);
            }
        }
        
        // 确保索引文件存在
        File indexFile = new File(indexFilePath);
        if (!indexFile.exists()) {
            try {
                JSONObject emptyIndex = new JSONObject();
                emptyIndex.put("compressions", new JSONArray());
                Files.write(Paths.get(indexFilePath), emptyIndex.toString(2).getBytes());
            } catch (IOException e) {
                System.err.println("创建索引文件失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 检查是否需要压缩
     * @param fileName 文件名
     * @param threshold 压缩阈值
     * @return 是否需要压缩
     */
    public boolean needsCompression(String fileName, int threshold) {
        try {
            Path filePath = Paths.get(memoryDirectory, fileName);
            if (!Files.exists(filePath)) {
                return false;
            }
            
            String content = new String(Files.readAllBytes(filePath));
            return content.length() > threshold;
        } catch (IOException e) {
            System.err.println("检查压缩需求失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 压缩记忆文件
     * @param fileName 文件名
     * @return 压缩记录
     */
    public CompressionRecord compressMemory(String fileName) {
        return compressMemory(fileName, DEFAULT_THRESHOLD);
    }
    
    /**
     * 压缩记忆文件
     * @param fileName 文件名
     * @param threshold 压缩阈值
     * @return 压缩记录
     */
    public CompressionRecord compressMemory(String fileName, int threshold) {
        try {
            Path filePath = Paths.get(memoryDirectory, fileName);
            if (!Files.exists(filePath)) {
                System.err.println("文件不存在: " + fileName);
                return null;
            }
            
            // 读取原始内容
            String originalContent = new String(Files.readAllBytes(filePath));
            int originalLength = originalContent.length();
            
            // 检查是否需要压缩
            if (originalLength <= threshold) {
                System.out.println("内容长度 " + originalLength + " 未超过阈值 " + threshold + "，无需压缩");
                return null;
            }
            
            System.out.println("正在压缩记忆文件: " + fileName + " (长度: " + originalLength + ")");
            
            // 备份原始内容到归档目录
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String archiveFileName = fileName.replace(".md", "_" + timestamp + "_original.md");
            Path archivePath = Paths.get(archiveDirectory, archiveFileName);
            Files.copy(filePath, archivePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 使用LLM压缩内容
            String compressedContent = compressWithLLM(originalContent, fileName);
            int compressedLength = compressedContent.length();
            
            // 计算压缩率
            double ratio = (1.0 - (double)compressedLength / originalLength) * 100;
            String compressionRatio = String.format("%.1f%%", ratio);
            
            // 写入压缩后的内容
            Files.write(filePath, compressedContent.getBytes());
            
            // 创建压缩记录
            CompressionRecord record = new CompressionRecord();
            record.fileName = fileName;
            record.originalFilePath = archivePath.toString();
            record.compressionDate = new Date();
            record.originalLength = originalLength;
            record.compressedLength = compressedLength;
            record.compressionRatio = compressionRatio;
            
            // 更新索引
            addCompressionToIndex(record);
            
            System.out.println("压缩完成: " + fileName);
            System.out.println("原始长度: " + originalLength + ", 压缩后: " + compressedLength + ", 压缩率: " + compressionRatio);
            System.out.println("原始内容已保存到: " + archivePath);
            
            return record;
            
        } catch (Exception e) {
            System.err.println("压缩记忆文件失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 使用LLM压缩内容
     * @param content 原始内容
     * @param fileName 文件名
     * @return 压缩后的内容
     */
    private String compressWithLLM(String content, String fileName) throws Exception {
        String prompt = "请对以下内容进行智能压缩，保留所有关键信息，去除冗余内容，保持原有结构和分类。" +
                       "压缩后的内容应该简洁但不丢失重要信息。\n\n" +
                       "文件名: " + fileName + "\n\n" +
                       "原始内容:\n" + content;
        
        return llmClient.generateResponse(prompt);
    }
    
    /**
     * 恢复原始记忆
     * @param fileName 文件名
     * @return 是否成功
     */
    public boolean restoreOriginalMemory(String fileName) {
        try {
            // 查找最新的归档版本
            CompressionRecord latestRecord = findLatestCompression(fileName);
            if (latestRecord == null) {
                System.err.println("未找到 " + fileName + " 的归档版本");
                return false;
            }
            
            Path archivePath = Paths.get(latestRecord.originalFilePath);
            if (!Files.exists(archivePath)) {
                System.err.println("归档文件不存在: " + latestRecord.originalFilePath);
                return false;
            }
            
            // 备份当前压缩版本
            Path currentPath = Paths.get(memoryDirectory, fileName);
            if (Files.exists(currentPath)) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                Path backupPath = Paths.get(archiveDirectory, fileName.replace(".md", "_" + timestamp + "_backup.md"));
                Files.copy(currentPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // 恢复原始版本
            Files.copy(archivePath, currentPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("已恢复原始记忆: " + fileName);
            return true;
            
        } catch (IOException e) {
            System.err.println("恢复原始记忆失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 查找最新的压缩记录
     * @param fileName 文件名
     * @return 最新的压缩记录
     */
    public CompressionRecord findLatestCompression(String fileName) {
        List<CompressionRecord> records = getCompressionHistory(fileName);
        if (records.isEmpty()) {
            return null;
        }
        
        CompressionRecord latest = records.get(0);
        for (CompressionRecord record : records) {
            if (record.compressionDate.after(latest.compressionDate)) {
                latest = record;
            }
        }
        return latest;
    }
    
    /**
     * 获取压缩历史
     * @param fileName 文件名（可选，为空则返回所有）
     * @return 压缩记录列表
     */
    public List<CompressionRecord> getCompressionHistory(String fileName) {
        List<CompressionRecord> records = new ArrayList<>();
        try {
            String indexContent = new String(Files.readAllBytes(Paths.get(indexFilePath)));
            JSONObject index = new JSONObject(indexContent);
            JSONArray compressions = index.getJSONArray("compressions");
            
            for (int i = 0; i < compressions.length(); i++) {
                JSONObject json = compressions.getJSONObject(i);
                CompressionRecord record = CompressionRecord.fromJSON(json);
                if (fileName == null || fileName.isEmpty() || record.fileName.equals(fileName)) {
                    records.add(record);
                }
            }
        } catch (IOException e) {
            System.err.println("读取压缩历史失败: " + e.getMessage());
        }
        return records;
    }
    
    /**
     * 在归档中搜索
     * @param keyword 关键词
     * @return 搜索结果列表
     */
    public List<String> searchInArchive(String keyword) {
        List<String> results = new ArrayList<>();
        try {
            File archiveDir = new File(archiveDirectory);
            File[] files = archiveDir.listFiles((dir, name) -> name.endsWith("_original.md"));
            
            if (files == null) {
                return results;
            }
            
            for (File file : files) {
                String content = new String(Files.readAllBytes(file.toPath()));
                if (content.contains(keyword)) {
                    results.add(file.getName() + ": 找到匹配内容");
                }
            }
        } catch (IOException e) {
            System.err.println("搜索归档失败: " + e.getMessage());
        }
        return results;
    }
    
    /**
     * 添加压缩记录到索引
     * @param record 压缩记录
     */
    private void addCompressionToIndex(CompressionRecord record) {
        try {
            String indexContent = new String(Files.readAllBytes(Paths.get(indexFilePath)));
            JSONObject index = new JSONObject(indexContent);
            JSONArray compressions = index.getJSONArray("compressions");
            compressions.put(record.toJSON());
            Files.write(Paths.get(indexFilePath), index.toString(2).getBytes());
        } catch (IOException e) {
            System.err.println("更新索引失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查并压缩所有需要压缩的记忆文件
     */
    public void checkAndCompressAll() {
        String[] memoryFiles = {"MEMORY.md", "SOUL.md", "USER.md"};
        for (String fileName : memoryFiles) {
            if (needsCompression(fileName, DEFAULT_THRESHOLD)) {
                compressMemory(fileName, DEFAULT_THRESHOLD);
            }
        }
    }
}
