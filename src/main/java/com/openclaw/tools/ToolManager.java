package com.openclaw.tools;

import java.util.Map;

/**
 * 工具管理器接口
 * 定义工具管理器的通用方法
 */
public interface ToolManager {
    /**
     * 获取工具管理器名称
     * @return 工具管理器名称
     */
    String getName();

    /**
     * 获取工具管理器描述
     * @return 工具管理器描述
     */
    String getDescription();

    /**
     * 注册工具
     * @param name 工具名称
     * @param description 工具描述
     * @param parameters 工具参数
     * @param caller 工具调用器
     */
    void registerTool(String name, String description, Map<String, String> parameters, ToolInfo.ToolCaller caller);

    /**
     * 获取工具信息
     * @param toolName 工具名称
     * @return 工具信息
     */
    ToolInfo getToolInfo(String toolName);

    /**
     * 获取所有工具信息
     * @return 所有工具信息
     */
    Map<String, ToolInfo> getAllTools();

    /**
     * 获取工具信息的JSONArray格式
     * @return JSONArray格式的工具信息
     */
    org.json.JSONArray getToolsInfoAsJsonArray();

    /**
     * 调用工具
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 工具执行结果
     */
    String callTool(String toolName, Map<String, Object> parameters) throws Exception;

    /**
     * 初始化工具管理器
     */
    void initialize();

    /**
     * 检查工具管理器是否可用
     * @return 是否可用
     */
    boolean isAvailable();
}
