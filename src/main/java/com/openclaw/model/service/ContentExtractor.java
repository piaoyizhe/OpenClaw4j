package com.openclaw.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 内容提炼器
 * 负责从聊天内容中提取关键信息，生成摘要，识别重要事实
 */
public class ContentExtractor {
    private static ContentExtractor instance;
    
    /**
     * 私有构造方法
     */
    private ContentExtractor() {
    }
    
    /**
     * 获取单例实例
     * @return ContentExtractor实例
     */
    public static ContentExtractor getInstance() {
        if (instance == null) {
            synchronized (ContentExtractor.class) {
                if (instance == null) {
                    instance = new ContentExtractor();
                }
            }
        }
        return instance;
    }
    
    /**
     * 提炼内容
     * @param content 原始内容
     * @param contentType 内容类型 (soul/user/memory)
     * @return 提炼后的内容
     */
    public String extractContent(String content, String contentType) {
        // 1. 清理内容
        String cleanedContent = cleanContent(content);
        
        // 2. 提取关键信息
        List<String> keyPoints = extractKeyPoints(cleanedContent, contentType);
        
        // 3. 生成摘要
        String summary = generateSummary(keyPoints, contentType);
        
        // 4. 优化摘要
        return optimizeSummary(summary);
    }
    
    /**
     * 清理内容
     * @param content 原始内容
     * @return 清理后的内容
     */
    private String cleanContent(String content) {
        // 移除多余的空白字符
        content = content.replaceAll("\\s+", " ").trim();
        
        // 移除重复的标点符号
        content = content.replaceAll("([.!?])\\1+", "$1");
        
        // 移除无意义的前缀和后缀
        content = content.replaceAll("^\\s*[-,;:]+\\s*", "");
        content = content.replaceAll("\\s*[-,;:]+\\s*$", "");
        
        return content;
    }
    
    /**
     * 提取关键信息
     * @param content 清理后的内容
     * @param contentType 内容类型
     * @return 关键信息列表
     */
    private List<String> extractKeyPoints(String content, String contentType) {
        List<String> keyPoints = new ArrayList<>();
        
        // 根据内容类型提取不同的关键信息
        switch (contentType) {
            case "soul":
                keyPoints.addAll(extractSoulKeyPoints(content));
                break;
            case "user":
                keyPoints.addAll(extractUserKeyPoints(content));
                break;
            case "memory":
                keyPoints.addAll(extractMemoryKeyPoints(content));
                break;
            default:
                keyPoints.addAll(extractGeneralKeyPoints(content));
                break;
        }
        
        // 如果没有提取到关键信息，使用通用提取
        if (keyPoints.isEmpty()) {
            keyPoints.addAll(extractGeneralKeyPoints(content));
        }
        
        // 去重
        return removeDuplicates(keyPoints);
    }
    
    /**
     * 提取SOUL相关关键信息
     * @param content 内容
     * @return 关键信息列表
     */
    private List<String> extractSoulKeyPoints(String content) {
        List<String> keyPoints = new ArrayList<>();
        
        // 提取AI人格特征
        String[] soulKeywords = {
            "性格", "特征", "语气", "风格", "做事", "行为", "规则",
            "我是", "我的", "我会", "我将", "我应该", "我必须",
            "服务", "帮助", "协助", "支持", "提供", "确保"
        };
        
        for (String keyword : soulKeywords) {
            if (content.contains(keyword)) {
                // 提取包含关键词的句子
                String[] sentences = content.split("[.!?]");
                for (String sentence : sentences) {
                    if (sentence.contains(keyword) && sentence.length() > 10) {
                        keyPoints.add(sentence.trim());
                    }
                }
            }
        }
        
        return keyPoints;
    }
    
    /**
     * 提取USER相关关键信息
     * @param content 内容
     * @return 关键信息列表
     */
    private List<String> extractUserKeyPoints(String content) {
        List<String> keyPoints = new ArrayList<>();
        
        // 提取用户信息
        String[] userKeywords = {
            "同事", "姓名", "名字", "我是", "我叫", "我的名字", "我的姓名",
            "职业", "工作", "职位", "岗位", "开发", "程序员", "工程师",
            "语言", "技能", "擅长", "会", "熟悉", "了解", "掌握"
        };
        
        for (String keyword : userKeywords) {
            if (content.contains(keyword)) {
                // 提取包含关键词的句子
                String[] sentences = content.split("[.!?]");
                for (String sentence : sentences) {
                    if (sentence.contains(keyword) && sentence.length() > 10) {
                        keyPoints.add(sentence.trim());
                    }
                }
            }
        }
        
        // 提取姓名
        Pattern namePattern = Pattern.compile("(我是|我叫|我的名字是|我的姓名是)\\s*([\\u4e00-\\u9fa5]{2,4})");
        Matcher nameMatcher = namePattern.matcher(content);
        if (nameMatcher.find()) {
            keyPoints.add("姓名: " + nameMatcher.group(2));
        }
        
        // 提取职业
        Pattern jobPattern = Pattern.compile("(职业是|工作是|职位是|岗位是|是)\\s*([\\u4e00-\\u9fa5\\w]+(程序员|工程师|开发|设计师|产品|运营|销售))");
        Matcher jobMatcher = jobPattern.matcher(content);
        if (jobMatcher.find()) {
            keyPoints.add("职业: " + jobMatcher.group(2));
        }
        
        return keyPoints;
    }
    
    /**
     * 提取MEMORY相关关键信息
     * @param content 内容
     * @return 关键信息列表
     */
    private List<String> extractMemoryKeyPoints(String content) {
        List<String> keyPoints = new ArrayList<>();
        
        // 提取记忆信息
        String[] memoryKeywords = {
            "工号", "身份", "决策", "决定", "确定",
            "喜欢", "偏好", "希望", "项目", "任务", "工作"
        };
        
        for (String keyword : memoryKeywords) {
            if (content.contains(keyword)) {
                // 提取包含关键词的句子
                String[] sentences = content.split("[.!?]");
                for (String sentence : sentences) {
                    if (sentence.contains(keyword) && sentence.length() > 10) {
                        keyPoints.add(sentence.trim());
                    }
                }
            }
        }
        
        return keyPoints;
    }
    
    /**
     * 提取通用关键信息
     * @param content 内容
     * @return 关键信息列表
     */
    private List<String> extractGeneralKeyPoints(String content) {
        List<String> keyPoints = new ArrayList<>();
        
        // 提取包含数字的信息
        Pattern numberPattern = Pattern.compile(".*\\d+.*");
        Matcher numberMatcher = numberPattern.matcher(content);
        if (numberMatcher.matches()) {
            keyPoints.add(content);
        }
        
        // 提取包含时间的信息
        Pattern timePattern = Pattern.compile(".*[0-9]{4}.*");
        Matcher timeMatcher = timePattern.matcher(content);
        if (timeMatcher.matches()) {
            keyPoints.add(content);
        }
        
        // 提取长度大于20的句子
        if (content.length() > 20) {
            keyPoints.add(content);
        }
        
        return keyPoints;
    }
    
    /**
     * 生成摘要
     * @param keyPoints 关键信息列表
     * @param contentType 内容类型
     * @return 摘要
     */
    private String generateSummary(List<String> keyPoints, String contentType) {
        if (keyPoints.isEmpty()) {
            return "";
        }
        
        StringBuilder summary = new StringBuilder();
        
        // 根据内容类型生成不同格式的摘要
        switch (contentType) {
            case "soul":
                summary.append("AI人格特征：");
                break;
            case "user":
                summary.append("用户信息：");
                break;
            case "memory":
                summary.append("重要信息：");
                break;
            default:
                summary.append("关键信息：");
                break;
        }
        
        // 合并关键信息
        int count = 0;
        for (String keyPoint : keyPoints) {
            if (count > 0) {
                summary.append("；");
            }
            summary.append(keyPoint);
            count++;
            
            // 限制摘要长度
            if (summary.length() > 150) {
                break;
            }
        }
        
        return summary.toString();
    }
    
    /**
     * 优化摘要
     * @param summary 原始摘要
     * @return 优化后的摘要
     */
    private String optimizeSummary(String summary) {
        // 移除重复的短语
        summary = removeDuplicatePhrases(summary);
        
        // 确保摘要长度合理
        if (summary.length() > 200) {
            summary = summary.substring(0, 200) + "...";
        }
        
        // 确保摘要以句号结尾
        if (!summary.endsWith(".") && !summary.endsWith("...")) {
            summary += "。";
        }
        
        return summary;
    }
    
    /**
     * 移除重复的短语
     * @param text 文本
     * @return 去重后的文本
     */
    private String removeDuplicatePhrases(String text) {
        // 简单的去重逻辑
        String[] phrases = text.split("；");
        List<String> uniquePhrases = new ArrayList<>();
        
        for (String phrase : phrases) {
            boolean isDuplicate = false;
            for (String uniquePhrase : uniquePhrases) {
                if (uniquePhrase.contains(phrase) || phrase.contains(uniquePhrase)) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                uniquePhrases.add(phrase);
            }
        }
        
        return String.join("；", uniquePhrases);
    }
    
    /**
     * 移除重复项
     * @param list 原始列表
     * @return 去重后的列表
     */
    private List<String> removeDuplicates(List<String> list) {
        List<String> uniqueList = new ArrayList<>();
        for (String item : list) {
            if (!uniqueList.contains(item)) {
                uniqueList.add(item);
            }
        }
        return uniqueList;
    }
    
    /**
     * 分析内容重要性
     * @param content 内容
     * @return 重要性评分 (0-10)
     */
    public int analyzeImportance(String content) {
        int score = 0;
        
        // 长度评分
        if (content.length() > 50) {
            score += 2;
        } else if (content.length() > 20) {
            score += 1;
        }
        
        // 包含数字评分
        if (content.matches(".*\\d+.*")) {
            score += 2;
        }
        
        // 包含关键词评分
        String[] importantKeywords = {
            "决定", "确定", "必须", "需要", "应该",
            "姓名", "职业", "身份", "工号", "项目"
        };
        
        for (String keyword : importantKeywords) {
            if (content.contains(keyword)) {
                score += 1;
                break;
            }
        }
        
        // 限制最高评分
        return Math.min(score, 10);
    }
}