package com.openclaw.utils;

import com.openclaw.model.entity.ToolParameters;
import com.openclaw.model.entity.ToolResult;
import okhttp3.*;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP工具类
 * 用于发送HTTP请求
 */
public class HttpUtil {
    private static HttpUtil instance;
    private OkHttpClient client;

    /**
     * 私有构造方法
     */
    private HttpUtil() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 获取单例实例
     * @return HttpUtil实例
     */
    public static HttpUtil getInstance() {
        if (instance == null) {
            synchronized (HttpUtil.class) {
                if (instance == null) {
                    instance = new HttpUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 执行HTTP请求
     * @param parameters 参数
     * @return 执行结果
     */
    public ToolResult execute(ToolParameters parameters) {
        String url = parameters.getString("url");
        if (url == null || url.isEmpty()) {
            return new ToolResult(false, "URL不能为空", null);
        }

        String method = parameters.getString("method");
        if (method == null || method.isEmpty()) {
            method = "GET"; // 默认GET方法
        }

        String timeoutStr = parameters.getString("timeout");
        long timeout = 30000; // 默认30秒
        if (timeoutStr != null && !timeoutStr.isEmpty()) {
            try {
                timeout = Long.parseLong(timeoutStr);
            } catch (NumberFormatException e) {
                // 忽略错误，使用默认值
            }
        }

        try {
            System.out.println("发送HTTP请求: " + method + " " + url);
            
            // 构建请求
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url);

            // 添加请求头
            String headersStr = parameters.getString("headers");
            if (headersStr != null && !headersStr.isEmpty()) {
                try {
                    JSONObject headersJson = JSONObject.parseObject(headersStr);
                    for (String key : headersJson.keySet()) {
                        requestBuilder.addHeader(key, headersJson.getString(key));
                    }
                } catch (Exception e) {
                    // 忽略错误，继续执行
                }
            }

            // 添加请求体（如果需要）
            String bodyStr = parameters.getString("body");
            if (bodyStr != null && !bodyStr.isEmpty() && !"GET".equals(method)) {
                RequestBody requestBody = RequestBody.create(
                        bodyStr, MediaType.parse("application/json; charset=utf-8"));
                requestBuilder.method(method, requestBody);
            } else {
                requestBuilder.method(method, null);
            }

            // 构建最终请求
            Request request = requestBuilder.build();

            // 发送请求，处理超时
            OkHttpClient timeoutClient = client.newBuilder()
                    .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                    .readTimeout(timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                    .build();

            try (Response response = timeoutClient.newCall(request).execute()) {
                int statusCode = response.code();
                String responseBody = response.body() != null ? response.body().string() : "";

                StringBuilder result = new StringBuilder();
                result.append("状态码: " + statusCode + "\n");
                result.append("响应: " + responseBody);

                if (statusCode >= 200 && statusCode < 300) {
                    return new ToolResult(true, result.toString(), null);
                } else {
                    return new ToolResult(false, "请求失败，" + result.toString(), null);
                }
            }
        } catch (Exception e) {
            return new ToolResult(false, "发送请求时发生错误: " + e.getMessage(), null);
        }
    }

    /**
     * 发送GET请求
     * @param url URL
     * @param headers 头部
     * @param timeout 超时时间（毫秒）
     * @return 响应结果
     */
    public String sendGetRequest(String url, Map<String, String> headers, long timeout) throws Exception {
        ToolParameters params = new ToolParameters();
        params.setParameter("url", url);
        params.setParameter("method", "GET");
        if (headers != null && !headers.isEmpty()) {
            params.setParameter("headers", com.alibaba.fastjson.JSONObject.toJSONString(headers));
        }
        params.setParameter("timeout", String.valueOf(timeout));
        ToolResult result = execute(params);
        return result.getMessage();
    }

    /**
     * 发送POST请求
     * @param url URL
     * @param body 正文
     * @param headers 头部
     * @param timeout 超时时间（毫秒）
     * @return 响应结果
     */
    public String sendPostRequest(String url, String body, Map<String, String> headers, long timeout) throws Exception {
        ToolParameters params = new ToolParameters();
        params.setParameter("url", url);
        params.setParameter("method", "POST");
        params.setParameter("body", body);
        if (headers != null && !headers.isEmpty()) {
            params.setParameter("headers", com.alibaba.fastjson.JSONObject.toJSONString(headers));
        }
        params.setParameter("timeout", String.valueOf(timeout));
        ToolResult result = execute(params);
        return result.getMessage();
    }

    /**
     * 检查工具是否可用
     * @return 是否可用
     */
    public boolean isAvailable() {
        return true; // HTTP工具在大多数系统上都可用
    }
}