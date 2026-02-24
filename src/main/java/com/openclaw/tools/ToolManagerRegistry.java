package com.openclaw.tools;

import com.alibaba.fastjson.JSONObject;
import com.openclaw.model.entity.ToolInfo;
import com.alibaba.fastjson.JSONArray;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具管理器注册表
 * 负责管理所有工具管理器，实现工具的动态路由
 */
public class ToolManagerRegistry {
    private static ToolManagerRegistry instance;
    private Map<String, ToolManager> toolManagers;
    private Map<String, String> toolToManagerMap; // 工具名称到工具管理器名称的映射
    private JSONArray cachedToolsArray; // 缓存的工具信息JSONArray
    private Map<String, JSONObject> cachedToolsMap; // 缓存的工具信息Map
    private boolean toolsUpdated; // 工具是否已更新的标志

    /**
     * 私有构造方法
     */
    private ToolManagerRegistry() {
        toolManagers = new ConcurrentHashMap<>();
        toolToManagerMap = new ConcurrentHashMap<>();
        cachedToolsArray = null;
        cachedToolsMap = null;
        toolsUpdated = true; // 初始状态为需要更新
        initialize();
    }

    /**
     * 获取单例实例
     * @return ToolManagerRegistry实例
     */
    public static ToolManagerRegistry getInstance() {
        if (instance == null) {
            synchronized (ToolManagerRegistry.class) {
                if (instance == null) {
                    instance = new ToolManagerRegistry();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化工具管理器注册表
     */
    private void initialize() {
        // 注册默认的工具管理器
//        registerToolManager(new FileToolManager());
        registerToolManager(new SystemToolManager());
        registerToolManager(new MemoryToolManager());
        registerToolManager(new NetworkToolManager());
        registerToolManager(new WeatherToolManager());
        registerToolManager(new HttpToolManager());
        registerToolManager(new ShellToolManager());
    }

    /**
     * 注册工具管理器
     * @param toolManager 工具管理器
     */
    public void registerToolManager(ToolManager toolManager) {
        if (toolManager != null) {
            toolManager.initialize();
            toolManagers.put(toolManager.getName(), toolManager);
            // 工具管理器更新，需要重新计算缓存
            toolsUpdated = true;
//            System.out.println("工具管理器注册成功: " + toolManager.getName());
        }
    }

    /**
     * 注销工具管理器
     * @param name 工具管理器名称
     * @return 是否注销成功
     */
    public boolean unregisterToolManager(String name) {
        if (toolManagers.containsKey(name)) {
            toolManagers.remove(name);
            // 同时从映射中移除该工具管理器管理的所有工具
            toolToManagerMap.entrySet().removeIf(entry -> entry.getValue().equals(name));
            // 工具管理器更新，需要重新计算缓存
            toolsUpdated = true;
            System.out.println("工具管理器注销成功: " + name);
            return true;
        }
        System.out.println("工具管理器不存在: " + name);
        return false;
    }

    /**
     * 获取工具管理器
     * @param name 工具管理器名称
     * @return 工具管理器
     */
    public ToolManager getToolManager(String name) {
        return toolManagers.get(name);
    }

    /**
     * 获取所有工具管理器
     * @return 工具管理器映射
     */
    public Map<String, ToolManager> getAllToolManagers() {
        return toolManagers;
    }

    /**
     * 注册工具到指定的工具管理器
     * @param toolName 工具名称
     * @param managerName 工具管理器名称
     */
    public void registerTool(String toolName, String managerName) {
        toolToManagerMap.put(toolName, managerName);
    }

    /**
     * 获取工具所属的工具管理器
     * @param toolName 工具名称
     * @return 工具管理器名称
     */
    public String getToolManagerName(String toolName) {
        return toolToManagerMap.get(toolName);
    }

    /**
     * 动态路由调用工具
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 工具执行结果
     */
    public String callTool(String toolName, Map<String, Object> parameters) {
        try {
            // 查找工具所属的工具管理器
            String managerName = getToolManagerName(toolName);
            ToolManager toolManager = null;

            // 如果没有映射，尝试在所有工具管理器中查找
            if (managerName == null) {
                for (ToolManager tm : toolManagers.values()) {
                    if (tm.getToolInfo(toolName) != null) {
                        toolManager = tm;
                        // 缓存映射关系
                        registerTool(toolName, tm.getName());
                        break;
                    }
                }
            } else {
                toolManager = getToolManager(managerName);
            }

            // 如果找到工具管理器，调用工具
            if (toolManager != null) {
                return toolManager.callTool(toolName, parameters);
            } else {
                return "工具不存在: " + toolName;
            }
        } catch (Exception e) {
            System.err.println("调用工具时发生错误: " + e.getMessage());
            return "调用工具时发生错误: " + e.getMessage();
        }
    }

    /**
     * 获取所有工具的信息
     * @return 工具信息映射
     */
    public Map<String, ToolInfo> getAllTools() {
        Map<String, ToolInfo> allTools = new ConcurrentHashMap<>();
        for (ToolManager toolManager : toolManagers.values()) {
            allTools.putAll(toolManager.getAllTools());
        }
        return allTools;
    }

    /**
     * 获取所有工具信息的JSONArray格式
     * @return JSONArray格式的工具信息
     */
    public JSONArray getToolsInfoAsJsonArray() {
        // 如果缓存存在且未更新，直接返回缓存
        if (cachedToolsArray != null && !toolsUpdated) {
            return cachedToolsArray;
        }

        // 重新计算缓存
        JSONArray toolsArray = new JSONArray();
        for (ToolManager toolManager : toolManagers.values()) {
            JSONArray managerTools = toolManager.getToolsInfoAsJsonArray();
            for (int i = 0; i < managerTools.size(); i++) {
                toolsArray.add(managerTools.get(i));
            }
        }

        // 更新缓存
        cachedToolsArray = toolsArray;
        toolsUpdated = false;
        return toolsArray;
    }

    /**
     * 获取所有工具信息的Map格式
     * @return Map格式的工具信息
     */
    public Map<String, JSONObject> getToolsInfoAsMap() {
        // 如果缓存存在且未更新，直接返回缓存
        if (cachedToolsMap != null && !toolsUpdated) {
            return cachedToolsMap;
        }

        // 重新计算缓存
        Map<String,JSONObject> toolsMap = new HashMap<>();
        for (ToolManager toolManager : toolManagers.values()) {
            JSONArray managerTools = toolManager.getToolsInfoAsJsonArray();
            for (int i = 0; i < managerTools.size(); i++) {
                toolsMap.put(managerTools.getJSONObject(i).getJSONObject("function").getString("name"),managerTools.getJSONObject(i));
            }
        }

        // 更新缓存
        cachedToolsMap = toolsMap;
        toolsUpdated = false;
        return toolsMap;
    }
}
