package com.openclaw.tools;

import com.openclaw.model.entity.ToolInfo;
import com.openclaw.utils.ShellUtil;
import com.openclaw.model.entity.ToolParameters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shell工具管理器
 * 负责管理Shell相关的工具，提供大模型调用接口
 */
public class ShellToolManager extends AbstractToolManager {

    /**
     * 构造方法
     */
    public ShellToolManager() {
        super("shell_tool_manager", "Shell工具管理器");
    }

    /**
     * 注册默认工具
     */
    @Override
    protected void registerDefaultTools() {
        // 注册Shell命令执行工具
        Map<String, String> shellParams = new ConcurrentHashMap<>();
        shellParams.put("command", "要执行的Shell命令");
        shellParams.put("timeout", "超时时间（毫秒），可选，默认30000");
        registerTool("execute_shell", "执行Shell命令", shellParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                ShellUtil shellUtil = ShellUtil.getInstance();
                ToolParameters toolParams = new ToolParameters((Map<String, Object>) parameters);
                return shellUtil.execute(toolParams).getMessage();
            } catch (Exception e) {
                return "执行Shell命令失败: " + e.getMessage();
            }
        });

//        System.out.println("ShellToolManager初始化完成，注册了 " + toolRegistry.size() + " 个工具");
    }
}