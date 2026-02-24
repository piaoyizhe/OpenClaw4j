package com.openclaw.brain;

import com.openclaw.utils.LLMClient;
import com.openclaw.utils.DateUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流程管理器
 * 负责工作流程的管理、意图识别、工作手册加载和执行
 */
public class WorkflowManager {
    private static WorkflowManager instance;
    private LLMClient llmClient;
    private Map<String, WorkflowManual> workflowManuals; // 工作手册映射
    private Map<String, String> intentToWorkflowMap; // 意图到工作流程的映射

    /**
     * 工作手册类
     */
    public static class WorkflowManual {
        private String name; // 工作手册名称（给大模型判断是否调用）
        private String description; // 工作手册描述（给大模型判断是否调用）
        private String content; // 工作手册完整内容（给大模型执行的时候使用的）
        private String tools; // 工作手册中的工具说明部分

        public WorkflowManual(String name, String content) {
            this.name = name;
            this.content = content;
            this.tools = parseTools(content);
            this.description = parseDescription(content);
        }

        /**
         * 解析工作手册内容，提取描述
         * 根据Skills格式解析
         */
        private String parseDescription(String content) {
            try {
                // 简单的解析逻辑，提取描述部分
                // Skills格式通常在开头有描述部分
                String[] lines = content.split("\\n");
                StringBuilder descriptionBuilder = new StringBuilder();
                boolean inDescription = false;

                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("## ")) {
                        String section = line.substring(3).trim();
                        if (section.equals("Description") || section.equals("描述")) {
                            inDescription = true;
                        } else if (inDescription) {
                            break; // 结束描述部分
                        }
                    } else if (inDescription && !line.isEmpty()) {
                        descriptionBuilder.append(line).append(" ");
                    }
                }

                // 如果没有找到描述部分，返回默认所有内容
                String description = descriptionBuilder.toString().trim();
                return description.isEmpty() ? content : description;
            } catch (Exception e) {
                System.err.println("解析工作手册描述失败: " + e.getMessage());
                return "工作流程手册";
            }
        }
        /**
         * 解析工作手册内容，提取描述
         * 根据Skills格式解析
         */
        private String parseTools(String content) {
            try {
                // 简单的解析逻辑，提取描述部分
                // Skills格式通常在开头有描述部分
                String[] lines = content.split("\\n");
                StringBuilder descriptionBuilder = new StringBuilder();
                boolean inTools = false;

                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("## ")) {
                        String section = line.substring(3).trim();
                        if (section.equals("Tools") || section.equals("工具")|| section.equals("可用工具")) {
                            inTools = true;
                        } else if (inTools) {
                            break; // 结束描述部分
                        }
                    } else if (inTools && !line.isEmpty()) {
                        descriptionBuilder.append(line).append(" ");
                    }
                }

                // 如果没有找到描述部分，返回默认所有内容
                String tools = descriptionBuilder.toString().trim();
                return tools.isEmpty() ? "" : tools;
            } catch (Exception e) {
                System.err.println("解析工作手册描述失败: " + e.getMessage());
                return "工作流程手册";
            }
        }

        // Getter方法
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getTools() { return tools; }
        public String getContent() { return content; }
    }

    /**
     * 意图识别结果
     */
    public static class IntentRecognitionResult {
        private String intent; // 识别的意图
        private String workflowName; // 推荐的工作流程
        private List<String> toolList; // 推荐的工具列表
        private double confidence; // 置信度
        private Map<String, Object> parameters; // 提取的参数

        public IntentRecognitionResult(String intent, String workflowName, List<String> toolList, double confidence, Map<String, Object> parameters) {
            this.intent = intent;
            this.workflowName = workflowName;
            this.toolList = toolList != null ? toolList : new ArrayList<>();
            this.confidence = confidence;
            this.parameters = parameters;
        }

        // Getter方法
        public String getIntent() { return intent; }
        public String getWorkflowName() { return workflowName; }
        public List<String> getToolList() { return toolList; }
        public double getConfidence() { return confidence; }
        public Map<String, Object> getParameters() { return parameters; }
    }

    /**
     * 私有构造方法
     */
    private WorkflowManager() {
        this.llmClient = LLMClient.getInstance();
        this.workflowManuals = new ConcurrentHashMap<>();
        this.intentToWorkflowMap = new ConcurrentHashMap<>();
        initialize();
    }

    /**
     * 获取单例实例
     * @return WorkflowManager实例
     */
    public static synchronized WorkflowManager getInstance() {
        if (instance == null) {
            instance = new WorkflowManager();
        }
        return instance;
    }

    /**
     * 初始化工作流程管理器
     */
    private void initialize() {
        // 加载工作手册
        loadWorkflowManuals();
    }

    /**
     * 加载工作手册
     */
    private void loadWorkflowManuals() {
        try {
            Path workflowsDir = Paths.get("src/main/resources/workflows");
            if (Files.exists(workflowsDir)) {
                Files.walk(workflowsDir)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".md"))
                        .forEach(this::loadWorkflowManual);
            }
        } catch (Exception e) {
            System.err.println("加载工作手册失败: " + e.getMessage());
        }
    }

    /**
     * 加载单个工作手册
     * @param path 工作手册文件路径
     */
    private void loadWorkflowManual(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            String content = new String(bytes, StandardCharsets.UTF_8);
            String fileName = path.getFileName().toString();
            String manualName = fileName.substring(0, fileName.lastIndexOf('.'));
            WorkflowManual manual = new WorkflowManual(manualName, content);
            workflowManuals.put(manualName, manual);
//            System.out.println("工作手册加载成功: " + manualName);
        } catch (Exception e) {
            System.err.println("加载工作手册失败 " + path + ": " + e.getMessage());
        }
    }

    /**
     * 识别用户意图
     * @param userQuery 用户查询
     * @return 意图识别结果
     */
    public IntentRecognitionResult recognizeIntent(String userQuery) {
        try {
            // 构建意图识别提示
             StringBuilder prompt = new StringBuilder();prompt.append("你是一个意图识别专家，请分析以下用户查询，识别其意图、选择适合的工作流程和需要的工具列表。\n\n");
            prompt.append("## 用户查询\n");
            prompt.append(userQuery).append("\n\n");
            prompt.append("## 可用工作流程\n");
            for (WorkflowManual manual : workflowManuals.values()) {
                prompt.append("- 名称: " + manual.getName() + "\n");
                prompt.append("  内容: " + manual.getContent() + "\n");
            }
            prompt.append("\n");
            prompt.append("## 工作流程使用说明\n");
            prompt.append("请根据用户请求选择最适合的工作流程，并在返回结果中包含完整的工作流程名称。\n");
            prompt.append("工作流程的详细执行步骤和所需工具请参考工作流程完整内容。\n");
            prompt.append("\n");
            prompt.append("## 输出格式\n");
            prompt.append("请返回JSON格式，包含以下字段：\n");
            prompt.append("{\n");
            prompt.append("  \"intent\": \"识别的意图\",\n");
            prompt.append("  \"workflowName\": \"推荐使用的工作流程名称，如果没有合适的你就返回：general_manual\",\n");
            prompt.append("  \"toolList\": [...],\n");
            prompt.append("  \"confidence\": 0.0-1.0,\n");
            prompt.append("  \"parameters\": {\n");
            prompt.append("    \"key\": \"value\" // 提取的参数\n");
            prompt.append("  }\n");
            prompt.append("}\n");
            
            // 使用LLM进行意图识别
            LLMClient.ModelParams params = new LLMClient.ModelParams();
            params.temperature = 0.1; // 低温度，提高确定性
            params.responseFormat = "json_object";
            
            String response = llmClient.generateResponse(prompt.toString(), "意图识别", true);
            JSONObject jsonResponse = JSONObject.parseObject(response);
            
            // 解析识别结果
            String intent = jsonResponse.getString("intent") != null ? jsonResponse.getString("intent") : "未知";
            String workflowName = jsonResponse.getString("workflowName") != null ? jsonResponse.getString("workflowName") : "general_manual";
            JSONArray toolListJson = jsonResponse.getJSONArray("toolList");
            List<String> toolList = new ArrayList<>();
            if (toolListJson != null) {
                for (int i = 0; i < toolListJson.size(); i++) {
                    toolList.add(toolListJson.getString(i));
                }
            }
            double confidence = jsonResponse.getDoubleValue("confidence");
            JSONObject parametersJson = jsonResponse.getJSONObject("parameters");
            Map<String, Object> parameters = parametersJson != null ? parametersJson : new HashMap<>();
            
            return new IntentRecognitionResult(intent, workflowName, toolList, confidence, parameters);
        } catch (Exception e) {
            System.err.println("意图识别失败: " + e.getMessage());
            // 返回默认结果
            return new IntentRecognitionResult("未知", "general_manual", new ArrayList<>(), 0.5, new HashMap<>());
        }
    }

    /**
     * 将意图映射到工作流程
     * @param intent 意图
     * @return 工作流程名称
     */
    private String mapIntentToWorkflow(String intent) {
        for (Map.Entry<String, String> entry : intentToWorkflowMap.entrySet()) {
            if (intent.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        // 默认返回通用工作流程
        return "general_manual";
    }

    /**
     * 获取工作手册
     * @param name 工作手册名称
     * @return 工作手册
     */
    public WorkflowManual getWorkflowManual(String name) {
        return workflowManuals.get(name);
    }

    /**
     * 获取所有工作手册
     * @return 工作手册映射
     */
    public Map<String, WorkflowManual> getAllWorkflowManuals() {
        return workflowManuals;
    }


    /**
     * 获取推荐的工具
     * @param workflowManual 工作手册
     * @return 推荐的工具列表
     */
    public List<String> getRecommendedTools(WorkflowManual workflowManual) {
        // 根据用户要求，不再解析工具列表，直接返回空列表
        // 工具选择由大模型根据完整工作手册内容决定
        return new ArrayList<>();
    }

    /**
     * 获取所有工作流程的名称和描述
     * @return 工作流程信息列表
     */
    public List<Map<String, String>> getWorkflowInfos() {
        List<Map<String, String>> workflowInfos = new ArrayList<>();
        for (WorkflowManual manual : workflowManuals.values()) {
            Map<String, String> info = new HashMap<>();
            info.put("name", manual.getName());
            info.put("description", manual.getDescription());
            workflowInfos.add(info);
        }
        return workflowInfos;
    }

    /**
     * 根据工作流程名称获取完整的工作流程内容
     * @param workflowName 工作流程名称
     * @return 工作流程内容
     */
    public String getWorkflowContent(String workflowName) {
        WorkflowManual manual = getWorkflowManual(workflowName);
        return manual != null ? manual.getContent() : "";
    }

    /**
     * 重新加载工作手册
     */
    public void reloadWorkflowManuals() {
        workflowManuals.clear();
        loadWorkflowManuals();
    }

    /**
     * 检查工作流程管理器是否就绪
     * @return 是否就绪
     */
    public boolean isReady() {
        return !workflowManuals.isEmpty();
    }
}
