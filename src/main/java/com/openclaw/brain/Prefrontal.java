package com.openclaw.brain;

import com.openclaw.model.manager.MemoryManager;
import com.openclaw.model.manager.LongTermMemoryManager;
import com.openclaw.model.service.MemorySearch;
import com.openclaw.tools.ToolManagerRegistry;
import com.openclaw.utils.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.sqlite.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 前额叶皮层
 * 作为任务规划核心组件，负责决策逻辑，判断用户查询应执行的操作
 */
public class Prefrontal {
    private static Prefrontal instance;
    private static final int TOKENS_THRESHOLD = 184000; // 184k tokens阈值
    private static final int COMPRESS_ROUNDS = 20; // 每次压缩的对话轮数

    private MemoryManager memoryManager;
    private LongTermMemoryManager longTermMemoryManager;
    private LLMClient llmClient;
    private TaskExecutor taskExecutor;
    private WorkflowManager workflowManager;
    private AtomicInteger taskIdGenerator;
    private List<JSONObject> chatHistory; // 聊天历史记录（使用官方Message格式）

    /**
     * 获取单例实例
     */
    public static synchronized Prefrontal getInstance() {
        if (instance == null) {
            instance = new Prefrontal();
        }
        return instance;
    }

    /**
     * 任务类
     */
    public static class Task {
        private String taskId;
        private String taskType;
        private String description;
        private Map<String, Object> parameters;
        private TaskStatus status;
        private String result;
        private int retryCount;

        public Task(String taskId, String taskType, String description, Map<String, Object> parameters) {
            this.taskId = taskId;
            this.taskType = taskType;
            this.description = description;
            this.parameters = parameters;
            this.status = TaskStatus.PENDING;
            this.result = "";
            this.retryCount = 0;
        }

        public String getTaskId() {
            return taskId;
        }

        public String getTaskType() {
            return taskType;
        }

        public String getDescription() {
            return description;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public TaskStatus getStatus() {
            return status;
        }

        public void setStatus(TaskStatus status) {
            this.status = status;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public void incrementRetryCount() {
            this.retryCount++;
        }

        @Override
        public String toString() {
            return "Task{" +
                    "taskId='" + taskId + '\'' +
                    ", taskType='" + taskType + '\'' +
                    ", parameters='" + parameters + '\'' +
                    ", description='" + description + '\'' +
                    ", result='" + result + '\'' +
                    ", status=" + status +
                    "}";
        }
    }

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING, RUNNING, COMPLETED, FAILED
    }

    /**
     * 构造方法
     */
    private Prefrontal() {
        ApplicationContext context = ApplicationContext.getInstance();
        this.memoryManager = context.getComponent(MemoryManager.class);
        this.longTermMemoryManager = context.getComponent(LongTermMemoryManager.class);
        this.llmClient = context.getComponent(LLMClient.class);
        this.taskExecutor = new TaskExecutor(memoryManager);
        this.workflowManager = WorkflowManager.getInstance();
        this.taskIdGenerator = new AtomicInteger(1);
        this.chatHistory = new ArrayList<>(); // 初始化聊天历史记录（使用官方Message格式）

        // 加载最近的40条历史对话消息
        loadRecentMessages();
    }

    /**
     * 加载最近的历史对话消息
     */
    private void loadRecentMessages() {
        try {
            // 从MemoryManager获取最近的40条消息
            List<String> recentMessages = memoryManager.getRecentMessages(40);
            if (!recentMessages.isEmpty()) {
                // 将字符串格式的消息转换为JSONObject格式
                for (String messageStr : recentMessages) {
                    JSONObject message = new JSONObject();
                    if (messageStr.startsWith("用户: ")) {
                        message.put("role", "user");
                        message.put("content", messageStr.substring(4));
                    } else if (messageStr.startsWith("系统: ")) {
                        message.put("role", "assistant");
                        message.put("content", messageStr.substring(4));
                    } else {
                        message.put("role", "system");
                        message.put("content", messageStr);
                    }
                    chatHistory.add(message);
                }
                System.out.println("已加载 " + recentMessages.size() + " 条历史对话消息");
                // 检查加载后的聊天历史长度
                checkAndCompressChatHistory();
            } else {
                System.out.println("未找到历史对话消息");
            }
        } catch (Exception e) {
            System.err.println("加载历史对话消息失败: " + e.getMessage());
        }
    }

    /**
     * 处理用户查询
     *
     * @param query 用户查询内容
     * @return 处理结果
     */
    public String processQuery(String query) {
        try {
            // 输入验证
            if (query == null || query.trim().isEmpty()) {
                String errorResponse = "请提供有效的查询内容。";
                addToChatHistory("系统", errorResponse);
                return errorResponse;
            }

            // 检查并压缩聊天历史
            checkAndCompressChatHistory();

            // 添加用户查询到聊天历史记录
            addToChatHistory("用户", query);

            // 意图识别和工作流程管理
            WorkflowManager.IntentRecognitionResult intentResult = workflowManager.recognizeIntent(query);
//            System.out.println("意图识别结果: 意图=" + intentResult.getIntent() + ", 工作流程=" + intentResult.getWorkflowName());

            // 使用大模型的工具调用功能
            String response = generateResponseWithToolCalls(query, intentResult);

            // 添加系统响应到聊天历史记录
            addToChatHistory("助手", response);

            // 再次检查并压缩聊天历史
            checkAndCompressChatHistory();

            return response;
        } catch (Exception e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            String errorResponse = "抱歉，处理您的请求时发生错误。" + errorHandler.handleException(e);
            addToChatHistory("系统", errorResponse);
            return errorResponse;
        }
    }

    /**
     * 使用大模型的工具调用功能生成响应
     *
     * @param query 用户查询内容
     * @param intentResult 意图识别结果
     * @return 处理结果
     */
    private String generateResponseWithToolCalls(String query, WorkflowManager.IntentRecognitionResult intentResult) throws Exception {
        // 获取ToolManagerRegistry实例
        ToolManagerRegistry toolRegistry = ToolManagerRegistry.getInstance();
        JSONArray tools = null;
        // 构建消息列表，包含系统提示、最近的聊天历史和当前查询
        List<JSONObject> messages = new ArrayList<>();

        // 加载SOUL.md、USER.md和IDENTITY.md文件的内容作为系统提示
        String soulContent = longTermMemoryManager.readLongTermMemory("SOUL.md", true);
        String userContent = longTermMemoryManager.readLongTermMemory("USER.md", true);
        String identityContent = longTermMemoryManager.readLongTermMemory("IDENTITY.md", true);

        // 添加系统消息（包含记忆文件内容）
        StringBuilder systemPrompt = new StringBuilder();
        if (!soulContent.isEmpty() || !userContent.isEmpty() || !identityContent.isEmpty()) {
            if (!identityContent.isEmpty()) {
                systemPrompt.append("# AI身份信息\n").append(identityContent).append("\n\n");
            }
            if (!soulContent.isEmpty()) {
                systemPrompt.append("# AI人格信息\n").append(soulContent).append("\n\n");
            }
            if (!userContent.isEmpty()) {
                systemPrompt.append("# 用户信息\n").append(userContent).append("\n\n");
            }
        }
        systemPrompt.append("# 当前时间\n").append(DateUtils.getString()).append("\n\n");

        // 添加意图识别结果和工作流程信息
        if (intentResult != null) {
            systemPrompt.append("# 意图识别结果\n");
            systemPrompt.append("意图: " + intentResult.getIntent() + "\n");
            systemPrompt.append("工作流程: " + intentResult.getWorkflowName() + "\n");
            systemPrompt.append("所需工具: " + String.join(", ", intentResult.getToolList()) + "\n");
            systemPrompt.append("\n");
        }

        // 如果有推荐的工作流程，添加完整的工作流程内容
        if (intentResult != null && intentResult.getWorkflowName() != null && !intentResult.getWorkflowName().equals("general_manual")) {
            String workflowContent = workflowManager.getWorkflowContent(intentResult.getWorkflowName());
            if (!workflowContent.isEmpty()) {
                systemPrompt.append("# 工作流程详细内容\n").append(workflowContent).append("\n\n");
            }
        }

        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt.toString());
        messages.add(systemMsg);

        // 添加最近的聊天历史（最多10条）
        if (!chatHistory.isEmpty()) {
            int startIndex = Math.max(0, chatHistory.size() - 10);
            messages.addAll(chatHistory.subList(startIndex, chatHistory.size()));
        }

        // 添加当前用户查询
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", query);
        messages.add(userMessage);

        // 调用大模型生成响应（使用官方Message格式）
        LLMClient.ModelParams params = new LLMClient.ModelParams();
        params.temperature = 0.7;

        // 根据意图识别结果过滤工具
        if (intentResult != null && !intentResult.getToolList().isEmpty()) {
            // 过滤工具，只保留工作流程中指定的工具
            tools = filterToolsByNames(toolRegistry.getToolsInfoAsMap(), intentResult.getToolList());
        } else {
            // 如果没有指定工具，使用所有工具
            tools = toolRegistry.getToolsInfoAsJsonArray();
        }

        // 调用大模型生成最终响应
        JSONObject messageResponse = llmClient.generateCompleteResponse(messages, params, tools);

        // 循环处理工具调用
        while (true) {
            // 检查是否有tool_calls
            if (messageResponse.containsKey("tool_calls") && messageResponse.get("tool_calls") != null) {
                JSONArray toolCalls = messageResponse.getJSONArray("tool_calls");

                if (toolCalls != null && toolCalls.size() > 0) {
                    // 添加助手消息（包含tool_calls）
                    JSONObject toolCallMessage = new JSONObject();
                    toolCallMessage.put("role", "assistant");
                    toolCallMessage.put("tool_calls", toolCalls);
                    messages.add(toolCallMessage);

                    // 执行所有工具调用
                    for (int i = 0; i < toolCalls.size(); i++) {
                        JSONObject toolCall = toolCalls.getJSONObject(i);

                        if (toolCall.containsKey("function")) {
                            JSONObject function = toolCall.getJSONObject("function");
                            String functionName = function.getString("name");
                            String arguments = function.getString("arguments");

                            // 解析arguments
                            JSONObject argsJson = JSONObject.parseObject(arguments);

                            // 创建任务
                            Task task = new Task(
                                    "task_" + taskIdGenerator.getAndIncrement(),
                                    functionName,
                                    "执行工具调用: " + functionName,
                                    argsJson
                            );

                            // 执行任务
                            String taskResult = taskExecutor.executeTask(task);

                            // 添加工具执行结果消息
                            JSONObject toolResultMessage = new JSONObject();
                            toolResultMessage.put("role", "tool");
                            toolResultMessage.put("tool_call_id", toolCall.getString("id"));
                            toolResultMessage.put("name", functionName);
                            toolResultMessage.put("content", taskResult);
                            messages.add(toolResultMessage);
                        }
                    }

                    // 再次调用大模型，获取总结
                    messageResponse = llmClient.generateCompleteResponse(messages, params, tools);
                    continue;
                }
            } else {
                // 检查是否有content直接返回
                if (messageResponse.containsKey("content") && messageResponse.get("content") != null) {
                    return messageResponse.getString("content");
                }
            }

            // 如果没有content和tool_calls，返回默认响应
            return "处理完成，但没有返回内容。";
        }
    }



    /**
     * 根据工具名称过滤工具列表
     * @param allTools 所有工具
     * @param requiredToolNames 需要的工具名称列表
     * @return 过滤后的工具列表
     */
    private JSONArray filterToolsByNames(Map<String,JSONObject> allTools, List<String> requiredToolNames) {
        JSONArray filteredTools = new JSONArray();
        try {
            for (String toolName:requiredToolNames) {
                filteredTools.add(allTools.get(toolName));
            }
        } catch (Exception e) {
            System.err.println("过滤工具失败: " + e.getMessage());
        }
        return filteredTools;
    }

    /**
     * 添加消息到聊天历史记录
     *
     * @param role    角色（用户/助手/系统）
     * @param content 内容
     */
    private void addToChatHistory(String role, String content) {
        // 使用官方Message格式保存聊天记录
        JSONObject message = new JSONObject();
        message.put("role", role.equals("用户") ? "user" : role.equals("助手") ? "assistant" : "system");
        message.put("content", content);
        chatHistory.add(message);
        checkAndCompressChatHistory();
    }

    /**
     * 检查并压缩聊天历史
     */
    private void checkAndCompressChatHistory() {
        int currentTokens = calculateTokensLength();

        // 当tokens长度接近阈值时，压缩最早的对话
        if (currentTokens > TOKENS_THRESHOLD * 0.8) { // 当达到阈值的80%时开始压缩
            System.out.println("聊天历史tokens长度接近阈值，开始压缩...");
            compressChatHistory();
            System.out.println("聊天历史压缩完成，当前长度: " + calculateTokensLength() + " tokens");
        }
    }

    /**
     * 计算聊天历史的tokens长度
     *
     * @return tokens长度
     */
    private int calculateTokensLength() {
        try {
            // 使用LLMClient的calculateTokens方法计算tokens长度
            StringBuilder combinedText = new StringBuilder();
            for (JSONObject message : chatHistory) {
                combinedText.append(message.getString("role")).append(": ").append(message.getString("content")).append("\n");
            }
            return llmClient.calculateTokens(combinedText.toString());
        } catch (Exception e) {
            System.err.println("计算tokens长度失败: " + e.getMessage());
            // 失败时使用估算方法
            int totalChars = 0;
            for (JSONObject message : chatHistory) {
                totalChars += message.getString("content").length();
            }
            // 估算tokens长度（平均每个字符约4个tokens）
            return totalChars * 4;
        }
    }

    /**
     * 压缩聊天历史，保留核心信息
     */
    private void compressChatHistory() {
        int compressSize = COMPRESS_ROUNDS * 2;
        if (chatHistory.size() <= compressSize) { // 确保有足够的对话可以压缩
            return;
        }

        // 获取最早的20轮对话（40条消息，每轮包含用户和系统消息）
        List<String> oldestMessages = new ArrayList<>();
        for (int i = 0; i < compressSize; i++) {
            JSONObject message = chatHistory.get(i);
            oldestMessages.add(message.getString("role") + ": " + message.getString("content"));
        }

        // 压缩这些对话，生成摘要
        String compressedSummary = generateChatSummary(oldestMessages);

        // 移除最早的对话，添加摘要
        chatHistory.subList(0, compressSize).clear();

        // 添加摘要消息
        JSONObject summaryMessage = new JSONObject();
        summaryMessage.put("role", "system");
        summaryMessage.put("content", "[压缩摘要]: " + compressedSummary);
        chatHistory.add(0, summaryMessage);
    }

    /**
     * 生成聊天摘要
     *
     * @param messages 要摘要的消息列表
     * @return 摘要内容
     */
    private String generateChatSummary(List<String> messages) {
        try {
            // 直接使用LLMClient生成摘要
            String messagesText = String.join("\n", messages);
            String prompt = "请对以下对话进行简要摘要，保留核心信息和关键话题：\n" + messagesText;
            return llmClient.generateResponse(prompt, "生成对话摘要");
        } catch (Exception e) {
            System.err.println("生成聊天摘要失败: " + e.getMessage());
            return "[对话摘要生成失败]";
        }
    }


}

