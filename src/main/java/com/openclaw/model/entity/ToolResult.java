package com.openclaw.model.entity;

import org.json.JSONObject;

/**
 * 工具执行结果类
 */
public class ToolResult {
    private boolean success;
    private String message;
    private Object data;
    private long executionTime;
    
    public ToolResult(boolean success, String message) {
        this(success, message, null);
    }
    
    public ToolResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.executionTime = 0;
    }
    
    public static ToolResult success(String message) {
        return new ToolResult(true, message);
    }
    
    public static ToolResult success(String message, Object data) {
        return new ToolResult(true, message, data);
    }
    
    public static ToolResult failure(String message) {
        return new ToolResult(false, message);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
    
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("success", success);
        json.put("message", message);
        json.put("data", data);
        json.put("executionTime", executionTime);
        return json;
    }
    
    @Override
    public String toString() {
        return toJSON().toString(2);
    }
}
