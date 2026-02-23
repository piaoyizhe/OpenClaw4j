package com.openclaw.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.openclaw.model.entity.ToolInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 工具管理器抽象类
 * 实现ToolManager接口的通用方法
 */
public abstract class AbstractToolManager implements ToolManager {
    private final String name;
    private final String description;
    protected Map<String, ToolInfo> toolRegistry;

    /**
     * 构造方法
     * @param name 工具管理器名称
     * @param description 工具管理器描述
     */
    public AbstractToolManager(String name, String description) {
        this.name = name;
        this.description = description;
        this.toolRegistry = new ConcurrentHashMap<>();
    }

    /**
     * 获取工具管理器名称
     * @return 工具管理器名称
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 获取工具管理器描述
     * @return 工具管理器描述
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * 注册工具
     * @param name 工具名称
     * @param description 工具描述
     * @param parameters 工具参数
     * @param caller 工具调用器
     */
    @Override
    public void registerTool(String name, String description, Map<String, String> parameters, ToolInfo.ToolCaller caller) {
        ToolInfo toolInfo = new ToolInfo(name, description, parameters, caller);
        toolRegistry.put(name, toolInfo);
    }

    /**
     * 获取工具信息
     * @param toolName 工具名称
     * @return 工具信息
     */
    @Override
    public ToolInfo getToolInfo(String toolName) {
        return toolRegistry.get(toolName);
    }

    /**
     * 获取所有工具信息
     * @return 所有工具信息
     */
    @Override
    public Map<String, ToolInfo> getAllTools() {
        return toolRegistry;
    }

    /**
     * 获取工具信息的JSONArray格式
     * @return JSONArray格式的工具信息
     */
    @Override
    public JSONArray getToolsInfoAsJsonArray() {
        JSONArray toolsArray = new JSONArray();
        
        for (ToolInfo toolInfo : toolRegistry.values()) {
            JSONObject toolObj = new JSONObject();
            toolObj.put("type", "function");
            
            JSONObject functionObj = new JSONObject();
            functionObj.put("name", toolInfo.getName());
            functionObj.put("description", toolInfo.getDescription());
            
            // 构建parameters对象
            JSONObject paramsObj = new JSONObject();
            JSONObject propertiesObj = new JSONObject();
            JSONArray requiredArray = new JSONArray();
            
            for (Map.Entry<String, String> param : toolInfo.getParameters().entrySet()) {
                String paramName = param.getKey();
                String paramDesc = param.getValue();
                
                JSONObject paramObj = new JSONObject();
                paramObj.put("type", "string");
                paramObj.put("description", paramDesc);
                propertiesObj.put(paramName, paramObj);
                
                // 检查参数是否为必填（如果描述中不包含"可选"，则视为必填）
                if (paramDesc == null || !paramDesc.contains("可选")) {
                    requiredArray.add(paramName);
                }
            }
            
            paramsObj.put("type", "object");
            paramsObj.put("properties", propertiesObj);
            if (requiredArray.size() > 0) {
                paramsObj.put("required", requiredArray);
            }
            
            functionObj.put("parameters", paramsObj);
            toolObj.put("function", functionObj);
            
            toolsArray.add(toolObj);
        }
        
        return toolsArray;
    }

    /**
     * 调用工具
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 工具执行结果
     */
    @Override
    public String callTool(String toolName, Map<String, Object> parameters) throws Exception {
        ToolInfo toolInfo = getToolInfo(toolName);
        if (toolInfo == null) {
            throw new Exception("工具不存在: " + toolName);
        }
        return toolInfo.getCaller().call(parameters);
    }

    /**
     * 初始化工具管理器
     */
    @Override
    public void initialize() {
        // 默认实现，子类可以重写
        registerDefaultTools();
    }

    /**
     * 注册默认工具
     */
    protected abstract void registerDefaultTools();

    /**
     * 检查工具管理器是否可用
     * @return 是否可用
     */
    @Override
    public boolean isAvailable() {
        // 默认实现，子类可以重写
        return true;
    }
}
