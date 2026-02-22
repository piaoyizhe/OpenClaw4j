package com.openclaw.model.service;

import com.openclaw.config.ConfigManager;
import com.openclaw.model.manager.SQLiteManager;
import com.openclaw.model.manager.DailyLogManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 记忆搜索类
 * 实现混合搜索功能，结合向量搜索和文本搜索
 */
public class MemorySearch {
    private static MemorySearch instance;
    private ConfigManager configManager;
    private SQLiteManager sqliteManager;
    private DailyLogManager dailyLogManager;

    // 搜索参数
    private double vectorWeight = 0.7; // 向量搜索权重
    private double textWeight = 0.3;   // 文本搜索权重
    private int defaultMaxResults = 10; // 默认最大结果数
    private double defaultMinScore = 0.1; // 默认最小评分

    /**
     * 私有构造方法
     */
    private MemorySearch() {
        configManager = ConfigManager.getInstance();
        sqliteManager = SQLiteManager.getInstance();
        dailyLogManager = DailyLogManager.getInstance();
        loadConfig();
    }

    /**
     * 获取单例实例
     * @return MemorySearch实例
     */
    public static MemorySearch getInstance() {
        if (instance == null) {
            synchronized (MemorySearch.class) {
                if (instance == null) {
                    instance = new MemorySearch();
                }
            }
        }
        return instance;
    }

    /**
     * 加载配置
     */
    private void loadConfig() {
        // 这里可以从配置文件加载搜索参数
        // 暂时使用默认值
    }

    /**
     * 混合搜索
     * @param query 查询文本
     * @param maxResults 最大结果数
     * @param minScore 最小评分
     * @return 搜索结果列表
     */
    public List<SearchResult> hybridSearch(String query, int maxResults, double minScore) {
        // 执行文本搜索（使用SQLite的FTS5）
        List<Map<String, Object>> textResults = sqliteManager.advancedFuzzySearch(query, maxResults * 2);

        // 执行日志文件搜索
        List<String> logResults = dailyLogManager.searchLogs(query, null, null);

        // 构建搜索结果
        List<SearchResult> results = new ArrayList<>();

        // 处理文本搜索结果
        for (Map<String, Object> textResult : textResults) {
            SearchResult result = new SearchResult();
            result.setId((Integer) textResult.get("id"));
            result.setFilePath((String) textResult.get("file_path"));
            result.setStartLine((Integer) textResult.get("start_line"));
            result.setEndLine((Integer) textResult.get("end_line"));
            result.setContent((String) textResult.get("content"));
            result.setScore(calculateTextScore(textResult, query));
            result.setType(ResultType.TEXT_CHUNK);
            results.add(result);
        }

        // 处理日志文件搜索结果
        for (String logFilePath : logResults) {
            SearchResult result = new SearchResult();
            result.setFilePath(logFilePath);
            result.setScore(calculateLogScore(logFilePath, query));
            result.setType(ResultType.LOG_FILE);
            results.add(result);
        }

        // 按评分排序并过滤
        results = results.stream()
                .filter(result -> result.getScore() >= minScore)
                .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .limit(maxResults)
                .collect(Collectors.toList());

        return results;
    }

    /**
     * 计算文本搜索评分
     * @param textResult 文本搜索结果
     * @param query 查询文本
     * @return 评分
     */
    private double calculateTextScore(Map<String, Object> textResult, String query) {
        // 这里可以实现更复杂的评分算法
        // 暂时返回默认评分
        return 0.8; // 简化处理
    }

    /**
     * 计算日志文件评分
     * @param logFilePath 日志文件路径
     * @param query 查询文本
     * @return 评分
     */
    private double calculateLogScore(String logFilePath, String query) {
        // 这里可以实现更复杂的评分算法
        // 暂时返回默认评分
        return 0.6; // 简化处理
    }

    /**
     * 搜索记忆
     * @param query 查询文本
     * @return 搜索结果
     */
    public List<SearchResult> searchMemory(String query) {
        return hybridSearch(query, defaultMaxResults, defaultMinScore);
    }

    /**
     * 搜索记忆
     * @param query 查询文本
     * @param maxResults 最大结果数
     * @return 搜索结果
     */
    public List<SearchResult> searchMemory(String query, int maxResults) {
        return hybridSearch(query, maxResults, defaultMinScore);
    }

    /**
     * 获取记忆内容
     * @param filePath 文件路径
     * @param startLine 起始行
     * @param endLine 结束行
     * @return 记忆内容
     */
    public String getMemoryContent(String filePath, int startLine, int endLine) {
        // 这里可以实现从文件中读取指定范围的内容
        // 暂时返回空字符串
        return "";
    }

    /**
     * 搜索结果类
     */
    public static class SearchResult {
        private Integer id;
        private String filePath;
        private Integer startLine;
        private Integer endLine;
        private String content;
        private double score;
        private ResultType type;

        // Getters and Setters
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public Integer getStartLine() {
            return startLine;
        }

        public void setStartLine(Integer startLine) {
            this.startLine = startLine;
        }

        public Integer getEndLine() {
            return endLine;
        }

        public void setEndLine(Integer endLine) {
            this.endLine = endLine;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public ResultType getType() {
            return type;
        }

        public void setType(ResultType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "SearchResult{" +
                    "id=" + id +
                    ", filePath='" + filePath + '\'' +
                    ", startLine=" + startLine +
                    ", endLine=" + endLine +
                    ", score=" + score +
                    ", type=" + type +
                    '}';
        }
    }

    /**
     * 结果类型枚举
     */
    public enum ResultType {
        TEXT_CHUNK, // 文本块
        LOG_FILE,   // 日志文件
        MEMORY_FILE // 记忆文件
    }

    /**
     * 设置向量搜索权重
     * @param vectorWeight 向量搜索权重
     */
    public void setVectorWeight(double vectorWeight) {
        this.vectorWeight = vectorWeight;
        this.textWeight = 1.0 - vectorWeight;
    }

    /**
     * 设置文本搜索权重
     * @param textWeight 文本搜索权重
     */
    public void setTextWeight(double textWeight) {
        this.textWeight = textWeight;
        this.vectorWeight = 1.0 - textWeight;
    }

    /**
     * 设置默认最大结果数
     * @param defaultMaxResults 默认最大结果数
     */
    public void setDefaultMaxResults(int defaultMaxResults) {
        this.defaultMaxResults = defaultMaxResults;
    }

    /**
     * 设置默认最小评分
     * @param defaultMinScore 默认最小评分
     */
    public void setDefaultMinScore(double defaultMinScore) {
        this.defaultMinScore = defaultMinScore;
    }
}
