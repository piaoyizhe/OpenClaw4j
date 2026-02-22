package com.openclaw.brain;

import com.openclaw.utils.LLMClient;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * AI记忆管理器
 * 负责记忆分析、内容提炼、记忆目标判断等
 */
public class Hippocampus {
    private static Hippocampus instance;
    private LLMClient llmClient;
    private LLMClient.ModelParams defaultModelParams; // 默认模型参数实例

    /**
     * 记忆分析结果
     */
    public static class MemoryAnalysisResult {
        public String targetFile;      // 目标文件: SOUL.md / USER.md / MEMORY.md
        public String extractedContent; // 提炼后的内容
        public boolean shouldUpdate;    // 是否需要更新
        public String reason;           // 原因
        public boolean hasConflict;     // 是否存在冲突
        public String conflictDetails;  // 冲突详情

        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("targetFile", targetFile);
            json.put("extractedContent", extractedContent);
            json.put("shouldUpdate", shouldUpdate);
            json.put("reason", reason);
            json.put("hasConflict", hasConflict);
            json.put("conflictDetails", conflictDetails);
            return json;
        }
    }

    /**
     * 冲突解决结果
     */
    public static class ConflictResolutionResult {
        public boolean hasConflict;          // 是否存在冲突
        public String conflictType;          // 冲突类型
        public String resolutionStrategy;    // 解决方案
        public String updatedContent;        // 处理后的内容
        public String reason;                // 处理原因
        
        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("hasConflict", hasConflict);
            json.put("conflictType", conflictType);
            json.put("resolutionStrategy", resolutionStrategy);
            json.put("updatedContent", updatedContent);
            json.put("reason", reason);
            return json;
        }
    }
    
    /**
     * 行更新信息
     */
    public static class LineUpdate {
        public int lineNumber;        // 行号
        public String content;         // 更新内容
        public String operation;       // 操作类型：update或add
        
        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("lineNumber", lineNumber);
            json.put("content", content);
            json.put("operation", operation);
            return json;
        }
    }
    
    /**
     * 记忆更新分析结果
     */
    public static class MemoryUpdateAnalysis {
        public boolean hasConflict;          // 是否存在冲突
        public String conflictType;          // 冲突类型
        public boolean needsUpdate;          // 是否需要更新
        public String updateScope;           // 更新范围
        public JSONArray lineUpdates;        // 行更新信息数组
        public String fileContent;           // 整个文件更新的内容
        public String uncertainParts;        // 不确定的部分
        public String reason;                // 判断原因
        public String confirmationRequired;  // 需要确认的具体内容
        
        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("hasConflict", hasConflict);
            json.put("conflictType", conflictType);
            json.put("needsUpdate", needsUpdate);
            json.put("updateScope", updateScope);
            json.put("lineUpdates", lineUpdates);
            json.put("fileContent", fileContent);
            json.put("uncertainParts", uncertainParts);
            json.put("reason", reason);
            json.put("confirmationRequired", confirmationRequired);
            return json;
        }
    }

    /**
     * 私有构造方法
     */
    private Hippocampus() {
        llmClient = LLMClient.getInstance();
        // 初始化默认模型参数
        defaultModelParams = new LLMClient.ModelParams();
        defaultModelParams.temperature = 0.7;
        defaultModelParams.maxTokens = 4096;
        defaultModelParams.thinking = false;
    }

    /**
     * 获取单例实例
     * @return Hippocampus实例
     */
    public static Hippocampus getInstance() {
        if (instance == null) {
            synchronized (Hippocampus.class) {
                if (instance == null) {
                    instance = new Hippocampus();
                }
            }
        }
        return instance;
    }

    // ==================== 记忆分析模块 ====================

    /**
     * 一站式记忆分析（带参数）
     * @param content 待分析内容
     * @return 分析结果
     */
    public MemoryAnalysisResult analyzeMemory(String content) throws Exception {
        LLMClient.ModelParams params =  defaultModelParams;
        params.responseFormat="json_object";
        String prompt = "你是一个大脑的海马体。请分析以下对话内容，哪些是作为一个专业数字员工需要提取和保存的长期记忆的内容。\n\n" +
                       "## 待分析内容\n" + content + "\n\n" +
                       "## 判断规则\n" +
                       "1. 目标文件判断：\n" +
                       "   - SOUL.md: AI人格特征、性格、语气、做事风格、行为规则、\n" +
                       "   - USER.md: 用户画像信息、姓名、职业、技能、偏好等\n" +
                       "   - MEMORY.md: 重要事实、决策、项目信息等\n\n" +
                       "   - IDENTITY.md - Who Am I，你的个人信息\n\n" +
                "2. 不保存短期记忆，例如问候 和 语气词\n\n" +
                       "3. 更新判断：判断内容中是否有值得提炼的记忆或者有价值的内容\n\n" +
                       "4. 如果有的话 提炼要求：从内容中提取关键信息，用简洁的markdown格式\n\n" +
                       "## 输出格式（JSON）\n" +
                       "请直接返回JSON，不要其他内容：\n" +
                       "{\n" +
                       "  \"targetFile\": \"SOUL.md\" 或 \"USER.md\" 或 \"MEMORY.md\",\n" +
                       "  \"extractedContent\": \"提炼后的关键信息（markdown格式）\",\n" +
                       "  \"shouldUpdate\": true 或 false,\n" +
                       "  \"reason\": \"判断原因\",\n"+
                       "}";

        // 直接使用传入的参数
        String response = llmClient.generateResponse(prompt, null, true, null, params);

        // 解析JSON响应
        try {
            JSONObject json = new JSONObject(response);
            MemoryAnalysisResult result = new MemoryAnalysisResult();
            result.targetFile = json.optString("targetFile", "MEMORY.md");
            result.extractedContent = json.optString("extractedContent", "");
            result.shouldUpdate = json.optBoolean("shouldUpdate", false);
            result.reason = json.optString("reason", "");
            result.hasConflict = json.optBoolean("hasConflict", false);
            result.conflictDetails = json.optString("conflictDetails", "");

            // 确保targetFile格式正确
            if (!result.targetFile.contains(".md")) {
                result.targetFile = "MEMORY.md";
            }
            return result;
        } catch (Exception e) {
            System.err.println("解析JSON失败，使用默认结果: " + e.getMessage());
            MemoryAnalysisResult result = new MemoryAnalysisResult();
            result.targetFile = "MEMORY.md";
            result.extractedContent = content;
            result.shouldUpdate = true;
            result.reason = "JSON解析失败，默认处理";
            result.hasConflict = false;
            result.conflictDetails = "";
            return result;
        }
    }

    /**
     * 判断是否需要查找历史记录
     * @param userQuery 用户查询
     * @return 是否需要查找
     */
    public boolean shouldSearchHistory(String userQuery) throws Exception {
        return shouldSearchHistory(userQuery, defaultModelParams);
    }

    /**
     * 判断是否需要查找历史记录（带参数）
     * @param userQuery 用户查询
     * @param params 模型参数
     * @return 是否需要查找
     */
    public boolean shouldSearchHistory(String userQuery, LLMClient.ModelParams params) throws Exception {
//        System.out.println("\n========== [Hippocampus] 判断是否需要查找历史记录 ==========");
//        System.out.println(">>> 用户查询: " + userQuery);

        String prompt = "你是一个决策助手。请判断以下用户查询是否需要查找历史记录。\n\n" +
                       "用户查询: " + userQuery + "\n\n" +
                       "判断标准：\n" +
                       "- 需要：涉及之前的对话内容、历史事件、已讨论的项目等\n" +
                       "- 不需要：一般性问题、自我介绍、新话题等\n\n" +
                       "请只返回true或false，不要其他内容。";

        // 直接使用传入的参数
        String response = llmClient.generateResponse(prompt, null, false, null, params);
        boolean shouldSearch = response.toLowerCase().contains("true");
//        System.out.println("<<< 判断结果: " + shouldSearch);
//        System.out.println("==========================================================\n");
        return shouldSearch;
    }

    // ==================== 辅助方法 ====================

    /**
     * 分析记忆更新需求
     * @param targetFile 目标文件
     * @param newContent 新内容
     * @param existingContent 现有内容
     * @return 分析结果
     */
    public MemoryUpdateAnalysis analyzeMemoryUpdate(String targetFile, String newContent, String existingContent) throws Exception {
        return analyzeMemoryUpdate(targetFile, newContent, existingContent, defaultModelParams);
    }
    
    /**
     * 分析记忆更新需求（带参数）
     * @param targetFile 目标文件
     * @param newContent 新内容
     * @param existingContent 现有内容
     * @param params 模型参数
     * @return 分析结果
     */
    public MemoryUpdateAnalysis analyzeMemoryUpdate(String targetFile, String newContent, String existingContent, LLMClient.ModelParams params) throws Exception {
        params.responseFormat="json_object";

        String prompt = "你是一个记忆管理专家。请分析以下内容，判断记忆更新需求。\n\n" + 
                        "## 目标文件\n" + targetFile + "\n\n" + 
                        "## 现有内容\n" + existingContent + "\n\n" + 
                        "## 新内容\n" + newContent + "\n\n" + 
                        "## 分析要求\n" + 
                        "1. 冲突检测：分析新内容与现有内容是否存在冲突\n" + 
                        "2. 冲突类型：如果存在冲突，说明冲突类型（事实冲突、时间冲突、观点冲突等）\n" + 
                        "3. 更新需求：判断是否需要更新现有内容\n" + 
                        "4. 更新方式选择：根据内容差异程度选择合适的更新方式\n" + 
                        "   - 整个文件更新：当新内容与现有内容差异较大，或结构完全不同时\n" + 
                        "   - 按行更新：当新内容与现有内容差异较小，只需更新部分行时\n" + 
                        "5. 行号分析：如果选择按行更新，分析现有内容的行号，确定需要更新或添加的具体行号\n" + 
                        "6. 具体操作：如果选择按行更新，为每一行需要更新或添加的内容，提供详细的行号和更新内容\n" + 
                        "7. 不确定性处理：如果对某些内容拿不准，请在uncertainParts中说明不确定的部分\n\n" + 
                        "## 输出格式（JSON）\n" + 
                        "请直接返回JSON，不要其他内容：\n" + 
                        "{\n" + 
                        "  \"hasConflict\": true 或 false,\n" + 
                        "  \"conflictType\": \"冲突类型（如果有）\",\n" + 
                        "  \"needsUpdate\": true 或 false,\n" + 
                        "  \"updateScope\": \"整个文件更新\" 或 \"按行更新\" 或 \"无需更新\" 或 \"需要确认\",\n" + 
                        "  \"lineUpdates\": [\n" + 
                        "    {\n" + 
                        "      \"lineNumber\": 行号,\n" + 
                        "      \"content\": \"更新内容\",\n" + 
                        "      \"operation\": \"update\" 或 \"add\"\n" + 
                        "    },\n" + 
                        "    ...\n" + 
                        "  ],\n" + 
                        "  \"fileContent\": \"整个文件更新的内容（如果updateScope为整个文件更新）\",\n" + 
                        "  \"uncertainParts\": \"不确定的部分（如果有）\",\n" + 
                        "  \"reason\": \"判断原因\",\n" + 
                        "  \"confirmationRequired\": \"需要确认的具体内容（如果有）\"\n" + 
                        "}";
        
        // 直接使用传入的参数
        String response = llmClient.generateResponse(prompt, null, true, null, params);

        // 解析JSON响应
        try {
            JSONObject json = new JSONObject(response);
            MemoryUpdateAnalysis result = new MemoryUpdateAnalysis();
            result.hasConflict = json.optBoolean("hasConflict", false);
            result.conflictType = json.optString("conflictType", "");
            result.needsUpdate = json.optBoolean("needsUpdate", false);
            result.updateScope = json.optString("updateScope", "无需更新");
            result.lineUpdates = json.optJSONArray("lineUpdates");
            result.fileContent = json.optString("fileContent", "");
            result.uncertainParts = json.optString("uncertainParts", "");
            result.reason = json.optString("reason", "");
            result.confirmationRequired = json.optString("confirmationRequired", "");

            if (result.lineUpdates != null && result.lineUpdates.length() > 0) {
                for (int i = 0; i < result.lineUpdates.length(); i++) {
                    JSONObject lineUpdate = result.lineUpdates.getJSONObject(i);
                    int lineNumber = lineUpdate.optInt("lineNumber", 0);
                    String content = lineUpdate.optString("content", "");
                    String operation = lineUpdate.optString("operation", "update");
//                    System.out.println("    - 行" + lineNumber + " (" + operation + "): " + content);
                }
            }
            
            // 处理整个文件更新的情况
            if ("整个文件更新".equals(result.updateScope)) {
                // 如果没有提供fileContent，使用新内容作为默认值
                if (result.fileContent.isEmpty()) {
                    result.fileContent = newContent;
                }
//                System.out.println(">>> 整个文件更新内容: " + (result.fileContent.length() > 100 ? result.fileContent.substring(0, 100) + "..." : result.fileContent));
            }
            
            if (!result.uncertainParts.isEmpty()) {
                System.out.println(">>> 不确定的部分: " + result.uncertainParts);
            }
            if (!result.confirmationRequired.isEmpty()) {
                System.out.println(">>> 需要确认的内容: " + result.confirmationRequired);
            }
//            System.out.println(">>> 原因: " + result.reason);
//            System.out.println("==================================================\n");
            
            return result;
        } catch (Exception e) {
            System.err.println("解析JSON失败，使用默认结果: " + e.getMessage());
            MemoryUpdateAnalysis result = new MemoryUpdateAnalysis();
            result.hasConflict = false;
            result.conflictType = "";
            result.needsUpdate = true;
            result.updateScope = "按行更新";
            result.lineUpdates = new JSONArray();
            result.fileContent = "";
            result.uncertainParts = "";
            result.confirmationRequired = "";
            result.reason = "JSON解析失败，默认处理";
            return result;
        }
    }
    
    /**
     * 处理记忆更新冲突（带参数）
     * @param targetFile 目标文件
     * @param newContent 新内容
     * @param existingContent 现有内容
     * @param params 模型参数
     * @return 处理结果
     */
    public ConflictResolutionResult resolveMemoryConflict(String targetFile, String newContent, String existingContent, LLMClient.ModelParams params) throws Exception {
        System.out.println("\n========== [Hippocampus] 处理记忆更新冲突 ==========");
        System.out.println(">>> 目标文件: " + targetFile);
        System.out.println(">>> 新内容长度: " + newContent.length() + " 字符");
        System.out.println(">>> 现有内容长度: " + existingContent.length() + " 字符");
        params.responseFormat="json_object";

        String prompt = "你是一个记忆管理专家。请分析以下内容，处理记忆更新冲突。\n\n" +
                       "## 目标文件\n" + targetFile + "\n\n" +
                       "## 现有内容\n" + existingContent + "\n\n" +
                       "## 新内容\n" + newContent + "\n\n" +
                       "## 处理要求\n" +
                       "1. 冲突检测：分析新内容与现有内容是否存在冲突\n" +
                       "2. 冲突类型：如果存在冲突，说明冲突类型（事实冲突、时间冲突、观点冲突等）\n" +
                       "3. 解决方案：\n" +
                       "   - 直接覆盖：新内容完全替代现有内容\n" +
                       "   - 合并更新：将新内容与现有内容合并，保留两者的有效信息\n" +
                       "   - 部分更新：只更新现有内容中与新内容冲突的部分\n" +
                       "   - 放弃更新：新内容不包含有价值的信息，无需更新\n" +
                       "4. 具体操作：如果选择部分更新，请明确指出需要修改的具体部分\n\n" +
                       "## 输出格式（JSON）\n" +
                       "请直接返回JSON，不要其他内容：\n" +
                       "{\n" +
                       "  \"hasConflict\": true 或 false,\n" +
                       "  \"conflictType\": \"冲突类型（如果有）\",\n" +
                       "  \"resolutionStrategy\": \"直接覆盖\" 或 \"合并更新\" 或 \"部分更新\" 或 \"放弃更新\",\n" +
                       "  \"updatedContent\": \"处理后的内容\",\n" +
                       "  \"reason\": \"处理原因\"\n" +
                       "}";

        // 直接使用传入的参数
        String response = llmClient.generateResponse(prompt, null, true, null, params);
//        System.out.println("<<< LLM响应: " + (response.length() > 100 ? response.substring(0, 100) + "..." : response));
        
        // 解析JSON响应
        try {
            JSONObject json = new JSONObject(response);
            ConflictResolutionResult result = new ConflictResolutionResult();
            result.hasConflict = json.optBoolean("hasConflict", false);
            result.conflictType = json.optString("conflictType", "");
            result.resolutionStrategy = json.optString("resolutionStrategy", "直接覆盖");
            result.updatedContent = json.optString("updatedContent", newContent);
            result.reason = json.optString("reason", "");
            
            System.out.println(">>> 存在冲突: " + result.hasConflict);
            if (result.hasConflict) {
                System.out.println(">>> 冲突类型: " + result.conflictType);
            }
            System.out.println(">>> 解决方案: " + result.resolutionStrategy);
            System.out.println(">>> 原因: " + result.reason);
            System.out.println("==================================================\n");
            
            return result;
        } catch (Exception e) {
            System.err.println("解析JSON失败，使用默认结果: " + e.getMessage());
            ConflictResolutionResult result = new ConflictResolutionResult();
            result.hasConflict = false;
            result.conflictType = "";
            result.resolutionStrategy = "直接覆盖";
            result.updatedContent = newContent;
            result.reason = "JSON解析失败，默认处理";
            return result;
        }
    }
}