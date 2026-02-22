package com.openclaw.model.entity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具参数类
 */
public class ToolParameters {
    private Map<String, Object> parameters;
    
    public ToolParameters() {
        this.parameters = new HashMap<>();
    }
    
    public ToolParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    
    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }
    
    public Object getParameter(String key) {
        return parameters.get(key);
    }
    
    public String getString(String key) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : null;
    }
    
    public int getInt(String key, int defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        try {
            return Integer.parseInt(value != null ? value.toString() : String.valueOf(defaultValue));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value != null ? value.toString() : String.valueOf(defaultValue));
    }
    
    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }
    
    public Map<String, Object> getAllParameters() {
        return new HashMap<>(parameters);
    }
    
    public JSONObject toJSON() {
        return new JSONObject(parameters);
    }
    
    public static ToolParameters fromJSON(JSONObject json) {
        ToolParameters params = new ToolParameters();
        for (String key : json.keySet()) {
            params.setParameter(key, json.get(key));
        }
        return params;
    }
}
