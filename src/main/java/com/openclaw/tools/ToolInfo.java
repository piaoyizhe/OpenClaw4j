package com.openclaw.tools;

import java.util.Map;

/**
 * 工具信息类
 * 用于存储工具的基本信息和调用接口
 */
public class ToolInfo {
    private String name;
    private String description;
    private Map<String, String> parameters;
    private ToolCaller caller;

    public ToolInfo(String name, String description, Map<String, String> parameters, ToolCaller caller) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.caller = caller;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public ToolCaller getCaller() {
        return caller;
    }

    /**
     * 工具调用接口
     */
    public interface ToolCaller {
        String call(Map<String, Object> parameters) throws Exception;
    }
}
