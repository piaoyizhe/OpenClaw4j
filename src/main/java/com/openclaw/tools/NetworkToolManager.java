package com.openclaw.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网络工具管理器
 * 负责管理网络相关的工具
 */
public class NetworkToolManager extends AbstractToolManager {

    /**
     * 构造方法
     */
    public NetworkToolManager() {
        super("network_tool_manager", "网络工具管理器");
    }

    /**
     * 注册默认工具
     */
    @Override
    protected void registerDefaultTools() {
        // 注册HTTP请求工具
        Map<String, String> httpParams = new ConcurrentHashMap<>();
        httpParams.put("url", "请求URL");
        httpParams.put("method", "请求方法（GET/POST/PUT/DELETE）");
        httpParams.put("headers", "请求头，JSON格式，可选");
        httpParams.put("body", "请求体，可选");
        httpParams.put("timeout", "请求超时时间（毫秒），可选");
        registerTool("send_http_request", "发送HTTP请求", httpParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                HttpTool httpTool = HttpTool.getInstance();
                ToolParameters params = new ToolParameters(parameters);
                ToolResult result = httpTool.execute(params);
                return result.getMessage();
            } catch (Exception e) {
                return "发送HTTP请求失败: " + e.getMessage();
            }
        });

        // 注册Shell命令执行工具
        Map<String, String> shellParams = new ConcurrentHashMap<>();
        shellParams.put("command", "要执行的Shell命令");
        shellParams.put("timeout", "执行超时时间（毫秒），可选");
        registerTool("execute_shell", "执行Shell命令", shellParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                ShellTool shellTool = ShellTool.getInstance();
                ToolParameters params = new ToolParameters(parameters);
                ToolResult result = shellTool.execute(params);
                return result.getMessage();
            } catch (Exception e) {
                return "执行Shell命令失败: " + e.getMessage();
            }
        });

        System.out.println("NetworkToolManager初始化完成，注册了 " + toolRegistry.size() + " 个工具");
    }
}
