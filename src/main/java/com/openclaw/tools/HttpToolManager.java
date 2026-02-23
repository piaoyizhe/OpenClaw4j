package com.openclaw.tools;

import com.openclaw.model.entity.ToolInfo;
import com.openclaw.utils.HttpUtil;
import com.openclaw.model.entity.ToolParameters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP工具管理器
 * 负责管理HTTP相关的工具，提供大模型调用接口
 */
public class HttpToolManager extends AbstractToolManager {

    /**
     * 构造方法
     */
    public HttpToolManager() {
        super("http_tool_manager", "HTTP工具管理器");
    }

    /**
     * 注册默认工具
     */
    @Override
    protected void registerDefaultTools() {
        // 注册HTTP请求工具
        Map<String, String> httpParams = new ConcurrentHashMap<>();
        httpParams.put("url", "请求URL");
        httpParams.put("method", "请求方法（GET/POST/PUT/DELETE等），可选，默认GET");
        httpParams.put("body", "请求体（JSON格式），可选");
        httpParams.put("headers", "请求头（JSON格式），可选");
        httpParams.put("timeout", "超时时间（毫秒），可选，默认30000");
        registerTool("send_http_request", "发送HTTP请求", httpParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                HttpUtil httpUtil = HttpUtil.getInstance();
                ToolParameters toolParams = new ToolParameters((Map<String, Object>) parameters);
                return httpUtil.execute(toolParams).getMessage();
            } catch (Exception e) {
                return "发送HTTP请求失败: " + e.getMessage();
            }
        });

//        System.out.println("HttpToolManager初始化完成，注册了 " + toolRegistry.size() + " 个工具");
    }
}