package com.openclaw.tools;

import com.openclaw.model.entity.ToolInfo;
import com.openclaw.model.entity.ToolParameters;
import com.openclaw.utils.ShellUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网络工具管理器
 * 负责管理网络特定的工具，如网络连接测试、网络状态检查等
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
        // 注册网络连接测试工具
        Map<String, String> pingParams = new ConcurrentHashMap<>();
        pingParams.put("host", "要测试的主机地址");
        pingParams.put("count", "测试次数，可选，默认4");
        pingParams.put("timeout", "超时时间（毫秒），可选，默认30000");
        registerTool("test_network_connection", "测试网络连接", pingParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                ShellUtil shellUtil = ShellUtil.getInstance();
                Map<String, Object> paramsMap = (Map<String, Object>) parameters;
                String host = (String) paramsMap.get("host");
                String count = paramsMap.getOrDefault("count", "4").toString();
                
                String command = "ping -c " + count + " " + host;
                Map<String, Object> shellParams = new ConcurrentHashMap<>();
                shellParams.put("command", command);
                if (paramsMap.containsKey("timeout")) {
                    shellParams.put("timeout", paramsMap.get("timeout"));
                }
                
                ToolParameters toolParams = new ToolParameters(shellParams);
                return shellUtil.execute(toolParams).getMessage();
            } catch (Exception e) {
                return "测试网络连接失败: " + e.getMessage();
            }
        });

        // 注册网络接口信息工具
        Map<String, String> ifconfigParams = new ConcurrentHashMap<>();
        ifconfigParams.put("interface", "网络接口名称，可选，默认显示所有接口");
        ifconfigParams.put("timeout", "超时时间（毫秒），可选，默认30000");
        registerTool("get_network_interfaces", "获取网络接口信息", ifconfigParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                ShellUtil shellUtil = ShellUtil.getInstance();
                Map<String, Object> paramsMap = (Map<String, Object>) parameters;
                String iface = paramsMap.getOrDefault("interface", "").toString();
                
                String command = iface.isEmpty() ? "ifconfig" : "ifconfig " + iface;
                Map<String, Object> shellParams = new ConcurrentHashMap<>();
                shellParams.put("command", command);
                if (paramsMap.containsKey("timeout")) {
                    shellParams.put("timeout", paramsMap.get("timeout"));
                }
                
                ToolParameters toolParams = new ToolParameters(shellParams);
                return shellUtil.execute(toolParams).getMessage();
            } catch (Exception e) {
                return "获取网络接口信息失败: " + e.getMessage();
            }
        });

//        System.out.println("NetworkToolManager初始化完成，注册了 " + toolRegistry.size() + " 个工具");
    }
}
