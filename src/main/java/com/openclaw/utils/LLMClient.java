package com.openclaw.utils;

import com.openclaw.config.ConfigManager;
import com.openclaw.utils.OpenClawException;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * LLM客户端
 * 专注于与大模型交互，支持多种模型，不参杂业务逻辑
 */
public class LLMClient {
    private static LLMClient instance;

    Logger logger = Logger.getLogger(LLMClient.class.getName());
    private ConfigManager configManager;
    private OkHttpClient client;

    /**
     * 模型参数配置
     */
    public static class ModelParams {
        public double temperature = 0.7;    // 温度参数
        public int maxTokens = 1024;         // 最大令牌数
        public boolean thinking = false; // 思考模式
        public boolean stream = false; // 流式输出
        public String responseFormat = "text"; // text表示普通文本输出，json_object表示JSON格式输出
        public String model = null;          // 指定模型


        public ModelParams() {
        }

        public ModelParams(double temperature, int maxTokens) {
            this.temperature = temperature;
            this.maxTokens = maxTokens;
        }
    }

    /**
     * 请求参数配置
     */
    public static class RequestParams {
        public String prompt;                // 提示词
        public String currentUserName;       // 当前用户名
        public boolean requireJson = false;  // 是否需要返回JSON格式
        public List<String> historyRecords;  // 历史记录
        public ModelParams modelParams;      // 模型参数
        public JSONArray tools;              // 工具信息
        public List<JSONObject> messages;    // 消息列表（使用官方Message格式）

        public RequestParams() {
        }

        public RequestParams(String prompt) {
            this.prompt = prompt;
        }

        public RequestParams(String prompt, ModelParams modelParams) {
            this.prompt = prompt;
            this.modelParams = modelParams;
        }

        public RequestParams(List<JSONObject> messages, ModelParams modelParams, JSONArray tools) {
            this.messages = messages;
            this.modelParams = modelParams;
            this.tools = tools;
        }
    }

    /**
     * 获取单例实例
     */
    public static synchronized LLMClient getInstance() {
        if (instance == null) {
            instance = new LLMClient();
        }
        return instance;
    }

    /**
     * 构造方法
     */
    private LLMClient() {
        configManager = ConfigManager.getInstance();
        initializeHttpClient();
    }
    


    /**
     * 初始化HTTP客户端
     */
    private void initializeHttpClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 生成响应
     *
     * @param prompt 提示词
     * @return 生成的响应
     */
    public String generateResponse(String prompt) throws OpenClawException {
        RequestParams params = new RequestParams(prompt);
        return generateResponse(params);
    }

    /**
     * 生成响应
     *
     * @param prompt          提示词
     * @param currentUserName 当前用户名
     * @return 生成的响应
     */
    public String generateResponse(String prompt, String currentUserName) throws OpenClawException {
        RequestParams params = new RequestParams(prompt);
        params.currentUserName = currentUserName;
        return generateResponse(params);
    }

    /**
     * 生成响应
     *
     * @param prompt          提示词
     * @param currentUserName 当前用户名
     * @param requireJson     是否需要返回JSON格式
     * @return 生成的响应
     */
    public String generateResponse(String prompt, String currentUserName, boolean requireJson) throws OpenClawException {
        RequestParams params = new RequestParams(prompt);
        params.currentUserName = currentUserName;
        params.requireJson = requireJson;
        return generateResponse(params);
    }

    /**
     * 生成响应（包含历史记录）
     *
     * @param prompt         提示词
     * @param historyRecords 历史记录
     * @return 生成的响应
     */
    public String generateResponse(String prompt, List<String> historyRecords) throws OpenClawException {
        RequestParams params = new RequestParams(prompt);
        params.historyRecords = historyRecords;
        return generateResponse(params);
    }

    /**
     * 生成响应（使用官方Message格式）
     *
     * @param messages 消息列表（使用官方Message格式）
     * @param params   模型参数
     * @param tools    工具信息
     * @return 生成的响应
     */
    public String generateResponse(List<JSONObject> messages, ModelParams params, JSONArray tools) throws OpenClawException {
        RequestParams requestParams = new RequestParams(messages, params, tools);
        return generateResponse(requestParams);
    }
    
    /**
     * 生成完整响应（使用官方Message格式），返回完整的message对象
     *
     * @param messages 消息列表（使用官方Message格式）
     * @param params   模型参数
     * @param tools    工具信息
     * @return 完整的message对象，包含tool_calls和content
     */
    public JSONObject generateCompleteResponse(List<JSONObject> messages, ModelParams params, JSONArray tools) throws OpenClawException {
        try {
            // 确保params不为null
            final ModelParams finalParams = params != null ? params : new ModelParams();
            
            String apiKey = configManager.getLLMApiKey();
            String apiUrl = configManager.getLLMApiUrl();
            String model = finalParams.model != null ? finalParams.model : configManager.getLLMModel();

            // 验证配置
            if (apiKey == null || apiKey.isEmpty() || "your_api_key_here".equals(apiKey)) {
                throw new Exception("API密钥未配置，请在application.yml文件中设置有效的API密钥");
            }

            if (apiUrl == null || apiUrl.isEmpty()) {
                throw new Exception("API URL未配置，请在application.yml文件中设置有效的API URL");
            }

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("stream", finalParams.stream);
            
            // 简化response_format构建
            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", finalParams.responseFormat);
            requestBody.put("response_format", responseFormat);

            // 简化thinking参数构建
            JSONObject thinkingConfig = new JSONObject();
            thinkingConfig.put("type", finalParams.thinking ? "enabled" : "disabled");
            thinkingConfig.put("clear_thinking", true);
            requestBody.put("thinking", thinkingConfig);

            JSONArray messagesArray = new JSONArray();
            
            // 消息列表已经包含系统消息（由调用方提供）

            // 添加用户提供的消息
            for (JSONObject message : messages) {
                messagesArray.put(message);
            }

            requestBody.put("messages", messagesArray);
            requestBody.put("temperature", finalParams.temperature);
            requestBody.put("max_tokens", finalParams.maxTokens);
            
            // 添加工具信息
            if (tools != null && tools.length() > 0) {
                requestBody.put("tools", tools);
            }

            // 构建请求
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(
                            requestBody.toString(),
                            MediaType.parse("application/json")
                    ))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 发送请求并获取响应
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    // 获取详细的错误信息
                    String errorBody = response.body().string();
                    throw new Exception("API请求失败: " + response.code() + " " + response.message() + " - " + errorBody);
                }

                // 解析响应
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);

                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject messageResponse = choice.getJSONObject("message");
                    return messageResponse;
                } else {
                    throw new Exception("API响应中没有可用的回复");
                }
            } catch (IOException e) {
                throw new Exception("网络请求失败，请检查您的网络连接", e);
            }
        } catch (Exception e) {
            throw new OpenClawException(OpenClawException.ErrorCode.API_ERROR, "生成响应失败", e);
        }
    }

    /**
     * 生成响应（带参数和工具调用）
     *
     * @param prompt          提示词
     * @param currentUserName 当前用户名
     * @param requireJson     是否需要返回JSON格式
     * @param historyRecords  历史记录
     * @param params          模型参数
     * @param tools           工具信息
     * @return 生成的响应
     */
    public String generateResponse(String prompt, String currentUserName, boolean requireJson, List<String> historyRecords, ModelParams params, JSONArray tools) throws OpenClawException {
        RequestParams requestParams = new RequestParams(prompt, params);
        requestParams.currentUserName = currentUserName;
        requestParams.requireJson = requireJson;
        requestParams.historyRecords = historyRecords;
        requestParams.tools = tools;
        return generateResponse(requestParams);
    }

    /**
     * 生成响应（带参数）
     *
     * @param prompt          提示词
     * @param currentUserName 当前用户名
     * @param requireJson     是否需要返回JSON格式
     * @param historyRecords  历史记录
     * @param params          模型参数
     * @return 生成的响应
     */
    public String generateResponse(String prompt, String currentUserName, boolean requireJson, List<String> historyRecords, ModelParams params) throws OpenClawException {
        return generateResponse(prompt, currentUserName, requireJson, historyRecords, params, null);
    }

    /**
     * 生成响应（使用RequestParams）
     *
     * @param requestParams 请求参数
     * @return 生成的响应
     */
    public String generateResponse(RequestParams requestParams) throws OpenClawException {
        try {
            // 确保requestParams不为null
            if (requestParams == null) {
                throw new Exception("请求参数不能为空");
            }
            
            // 确保modelParams不为null
            final ModelParams finalParams = requestParams.modelParams != null ? requestParams.modelParams : new ModelParams();
            
            String apiKey = configManager.getLLMApiKey();
            String apiUrl = configManager.getLLMApiUrl();
            String model = finalParams.model != null ? finalParams.model : configManager.getLLMModel();

            // 验证配置
            if (apiKey == null || apiKey.isEmpty() || "your_api_key_here".equals(apiKey)) {
                throw new Exception("API密钥未配置，请在application.yml文件中设置有效的API密钥");
            }

            if (apiUrl == null || apiUrl.isEmpty()) {
                throw new Exception("API URL未配置，请在application.yml文件中设置有效的API URL");
            }

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("stream", finalParams.stream);
            
            // 构建response_format
            JSONObject responseFormat = new JSONObject();
            // 如果requireJson为true，设置responseFormat为json_object
            String responseFormatType = requestParams.requireJson ? "json_object" : finalParams.responseFormat;
            responseFormat.put("type", responseFormatType);
            requestBody.put("response_format", responseFormat);

            // 构建thinking参数
            JSONObject thinkingConfig = new JSONObject();
            thinkingConfig.put("type", finalParams.thinking ? "enabled" : "disabled");
            thinkingConfig.put("clear_thinking", true);
            requestBody.put("thinking", thinkingConfig);

            JSONArray messagesArray = new JSONArray();
            
            // 检查是否使用官方Message格式
            if (requestParams.messages != null && !requestParams.messages.isEmpty()) {
                // 使用官方Message格式
                for (JSONObject message : requestParams.messages) {
                    messagesArray.put(message);
                }
            } else if (requestParams.prompt != null) {
                // 使用传统格式，构建用户消息
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", requestParams.prompt);
                messagesArray.put(userMsg);
            } else {
                throw new Exception("提示词或消息列表不能为空");
            }

            requestBody.put("messages", messagesArray);
            requestBody.put("temperature", finalParams.temperature);
            requestBody.put("max_tokens", finalParams.maxTokens);
            
            // 添加工具信息
            if (requestParams.tools != null && requestParams.tools.length() > 0) {
                requestBody.put("tools", requestParams.tools);
            }

            // 构建请求
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(
                            requestBody.toString(),
                            MediaType.parse("application/json")
                    ))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 发送请求并获取响应
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    // 获取详细的错误信息
                    String errorBody = response.body().string();
                    throw new Exception("API请求失败: " + response.code() + " " + response.message() + " - " + errorBody);
                }

                // 解析响应
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);

                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject messageResponse = choice.getJSONObject("message");
                    
                    // 检查是否有tool_calls
                    if (messageResponse.has("tool_calls")) {
                        JSONArray toolCalls = messageResponse.getJSONArray("tool_calls");
                        // 返回原始的tool_calls数据作为JSON字符串
                        return toolCalls.toString();
                    } else if (messageResponse.has("content")) {
                        // 普通回复
                        String content = messageResponse.getString("content");
                        return content;
                    } else {
                        throw new Exception("API响应中没有可用的回复内容");
                    }
                } else {
                    throw new Exception("API响应中没有可用的回复");
                }
            } catch (IOException e) {
                throw new Exception("网络请求失败，请检查您的网络连接", e);
            }
        } catch (Exception e) {
            throw new OpenClawException(OpenClawException.ErrorCode.API_ERROR, "生成响应失败", e);
        }
    }

    /**
     * 生成响应（使用JSONObject）
     *
     * @param paramsJsonObject JSON格式的请求参数
     * @return 生成的响应
     */
    public String generateResponse(JSONObject paramsJsonObject) throws OpenClawException {
        try {
            // 确保paramsJsonObject不为null
            if (paramsJsonObject == null) {
                throw new Exception("请求参数不能为空");
            }
            
            // 创建RequestParams对象
            RequestParams requestParams = new RequestParams();
            
            // 提取基本参数
            if (paramsJsonObject.has("prompt")) {
                requestParams.prompt = paramsJsonObject.getString("prompt");
            }
            if (paramsJsonObject.has("currentUserName")) {
                requestParams.currentUserName = paramsJsonObject.getString("currentUserName");
            }
            if (paramsJsonObject.has("requireJson")) {
                requestParams.requireJson = paramsJsonObject.getBoolean("requireJson");
            }
            
            // 提取历史记录
            if (paramsJsonObject.has("historyRecords")) {
                JSONArray historyArray = paramsJsonObject.getJSONArray("historyRecords");
                List<String> historyRecords = new ArrayList<>();
                for (int i = 0; i < historyArray.length(); i++) {
                    historyRecords.add(historyArray.getString(i));
                }
                requestParams.historyRecords = historyRecords;
            }
            
            // 提取模型参数
            if (paramsJsonObject.has("modelParams")) {
                JSONObject modelParamsJson = paramsJsonObject.getJSONObject("modelParams");
                ModelParams modelParams = new ModelParams();
                if (modelParamsJson.has("temperature")) {
                    modelParams.temperature = modelParamsJson.getDouble("temperature");
                }
                if (modelParamsJson.has("maxTokens")) {
                    modelParams.maxTokens = modelParamsJson.getInt("maxTokens");
                }
                if (modelParamsJson.has("thinking")) {
                    modelParams.thinking = modelParamsJson.getBoolean("thinking");
                }
                if (modelParamsJson.has("stream")) {
                    modelParams.stream = modelParamsJson.getBoolean("stream");
                }
                if (modelParamsJson.has("responseFormat")) {
                    modelParams.responseFormat = modelParamsJson.getString("responseFormat");
                }
                if (modelParamsJson.has("model")) {
                    modelParams.model = modelParamsJson.getString("model");
                }
                requestParams.modelParams = modelParams;
            }
            
            // 提取工具信息
            if (paramsJsonObject.has("tools")) {
                requestParams.tools = paramsJsonObject.getJSONArray("tools");
            }
            
            // 提取消息列表
            if (paramsJsonObject.has("messages")) {
                JSONArray messagesArray = paramsJsonObject.getJSONArray("messages");
                List<JSONObject> messages = new ArrayList<>();
                for (int i = 0; i < messagesArray.length(); i++) {
                    messages.add(messagesArray.getJSONObject(i));
                }
                requestParams.messages = messages;
            }
            
            // 调用RequestParams版本的方法
            return generateResponse(requestParams);
        } catch (Exception e) {
            throw new OpenClawException(OpenClawException.ErrorCode.API_ERROR, "生成响应失败", e);
        }
    }

    /**
     * 测试方法，用于验证API连接
     *
     * @return 是否连接成功
     */
    public boolean testConnection() {
        try {
            String apiKey = configManager.getLLMApiKey();
            String apiUrl = configManager.getLLMApiUrl();
            String model = configManager.getLLMModel();

            if (apiKey == null || apiKey.isEmpty() || "your_api_key_here".equals(apiKey)) {
                System.out.println("API密钥未配置");
                return false;
            }

            if (apiUrl == null || apiUrl.isEmpty()) {
                System.out.println("API URL未配置");
                return false;
            }

            // 发送简单的测试请求
            String testPrompt = "测试连接";
            String response = generateResponse(testPrompt);
            System.out.println("API连接测试成功: " + response.substring(0, Math.min(50, response.length())) + "...");
            return true;
        } catch (Exception e) {
            System.err.println("API连接测试失败: " + e.getMessage());
            return false;
        }
    }


    /**
     * 获取支持的模型列表
     *
     * @return 支持的模型列表
     */
    public String[] getSupportedModels() {
        return new String[]{
                "glm-4",            // GLM 4
                "glm-4.5",          // GLM 4.5
                "glm-4.7",          // GLM 4.7
                "glm-5",            // GLM 5
                "qwen-3.5",         // Qwen 3.5
                "kimi-2.5"           // Kimi 2.5
        };
    }

    /**
     * 获取默认模型参数
     *
     * @param model 模型名称
     * @return 默认模型参数
     */
    public ModelParams getDefaultParams(String model) {
        ModelParams params = new ModelParams();

        // 根据模型设置默认参数
        if (model != null) {
            switch (model.toLowerCase()) {
                case "glm-4":
                case "glm-4.5":
                case "glm-4.7":
                    params.temperature = 0.7;
                    params.maxTokens = 2048;
                    break;
                case "glm-5":
                    params.temperature = 0.6;
                    params.maxTokens = 4096;
                    break;
                case "qwen-3.5":
                    params.temperature = 0.8;
                    params.maxTokens = 3072;
                    break;
                case "kimi-2.5":
                    params.temperature = 0.75;
                    params.maxTokens = 2048;
                    break;
                default:
                    break;
            }
        }

        return params;
    }
    
    /**
     * 计算文本的tokens长度
     * @param text 要计算的文本
     * @return tokens长度
     */
    public int calculateTokens(String text) throws OpenClawException {
        return calculateTokens(text, "glm-4.6");
    }
    
    /**
     * 计算文本的tokens长度
     * @param text 要计算的文本
     * @param model 模型名称
     * @return tokens长度
     */
    public int calculateTokens(String text, String model) throws OpenClawException {
        try {
            // 确保params不为null
            String apiKey = configManager.getLLMApiKey();
            if (apiKey == null || apiKey.isEmpty() || "your_api_key_here".equals(apiKey)) {
                throw new Exception("API密钥未配置，请在application.yml文件中设置有效的API密钥");
            }
            
            // 构建tokenizer API请求
            String tokenizerUrl = "https://open.bigmodel.cn/api/paas/v4/tokenizer";
            
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            
            // 构建messages数组
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            
            JSONArray content = new JSONArray();
            JSONObject textContent = new JSONObject();
            textContent.put("type", "text");
            textContent.put("text", text);
            content.put(textContent);
            
            message.put("content", content);
            messages.put(message);
            requestBody.put("messages", messages);
            
            // 构建请求
            Request request = new Request.Builder()
                    .url(tokenizerUrl)
                    .post(RequestBody.create(
                            requestBody.toString(),
                            MediaType.parse("application/json")
                    ))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            // 发送请求并获取响应
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new Exception("Tokenizer API请求失败: " + response.code() + " " + response.message());
                }
                
                // 解析响应
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                
                // 获取usage字段中的total_tokens
                JSONObject usage = jsonResponse.getJSONObject("usage");
                int totalTokens = usage.getInt("total_tokens");
                
                return totalTokens;
            } catch (IOException e) {
                throw new Exception("网络请求失败，请检查您的网络连接", e);
            }
        } catch (Exception e) {
            throw new OpenClawException(OpenClawException.ErrorCode.API_ERROR, "计算tokens长度失败", e);
        }
    }
    
    /**
     * 计算消息列表的tokens长度
     * @param messages 消息列表
     * @return tokens长度
     */
    public int calculateTokens(List<String> messages) throws OpenClawException {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }
        StringBuilder combinedText = new StringBuilder();
        for (String message : messages) {
            combinedText.append(message).append("\n");
        }
        return calculateTokens(combinedText.toString());
    }
}
