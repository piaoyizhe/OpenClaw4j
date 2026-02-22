package com.openclaw.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统工具管理器
 * 负责管理系统相关的工具
 */
public class SystemToolManager extends AbstractToolManager {

    /**
     * 构造方法
     */
    public SystemToolManager() {
        super("system_tool_manager", "系统工具管理器");
    }

    /**
     * 注册默认工具
     */
    @Override
    protected void registerDefaultTools() {
        // 注册本地IP获取工具
        Map<String, String> localIpParams = new ConcurrentHashMap<>();
        registerTool("get_local_ip", "获取本地网络信息，包括主机名和IP地址", localIpParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                return getLocalIp();
            } catch (Exception e) {
                return "获取本地IP失败: " + e.getMessage();
            }
        });

        // 注册系统信息工具
        Map<String, String> systemInfoParams = new ConcurrentHashMap<>();
        registerTool("get_system_info", "获取系统信息，包括操作系统、Java版本等", systemInfoParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                return getSystemInfo();
            } catch (Exception e) {
                return "获取系统信息失败: " + e.getMessage();
            }
        });

        // 注册邮件发送工具
        Map<String, String> emailParams = new ConcurrentHashMap<>();
        emailParams.put("to", "收件人邮箱地址");
        emailParams.put("subject", "邮件主题");
        emailParams.put("body", "邮件正文");
        registerTool("send_email", "发送邮件到指定邮箱", emailParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String to = (String) parameters.get("to");
                String subject = (String) parameters.get("subject");
                String body = (String) parameters.get("body");
                return sendEmail(to, subject, body);
            } catch (Exception e) {
                return "发送邮件失败: " + e.getMessage();
            }
        });

        // 注册回复用户工具
        Map<String, String> replyParams = new ConcurrentHashMap<>();
        replyParams.put("content", "回复内容");
        registerTool("reply_user", "回复用户消息", replyParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String content = (String) parameters.get("content");
                if (content == null || content.isEmpty()) {
                    return "回复内容不能为空。";
                }
                return content;
            } catch (Exception e) {
                return "回复用户失败: " + e.getMessage();
            }
        });

        System.out.println("SystemToolManager初始化完成，注册了 " + toolRegistry.size() + " 个工具");
    }

    /**
     * 获取本地IP
     * @return 本地网络信息
     */
    private String getLocalIp() throws Exception {
        StringBuilder result = new StringBuilder();
        result.append("本地网络信息:\n");
        
        // 获取本地主机名
        java.net.InetAddress localhost = java.net.InetAddress.getLocalHost();
        result.append("主机名: " + localhost.getHostName() + "\n");
        
        // 获取本地IP地址
        result.append("IP地址: " + localhost.getHostAddress() + "\n");
        
        return result.toString();
    }

    /**
     * 获取系统信息
     * @return 系统信息
     */
    private String getSystemInfo() throws Exception {
        StringBuilder result = new StringBuilder();
        result.append("系统信息:\n");
        
        // 操作系统信息
        result.append("操作系统: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "\n");
        result.append("操作系统架构: " + System.getProperty("os.arch") + "\n");
        
        // Java信息
        result.append("Java版本: " + System.getProperty("java.version") + "\n");
        result.append("Java虚拟机: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + "\n");
        
        // 运行时信息
        result.append("最大内存: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB\n");
        result.append("可用内存: " + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + " MB\n");
        
        return result.toString();
    }

    /**
     * 发送邮件
     * @param to 收件人
     * @param subject 主题
     * @param body 正文
     * @return 发送结果
     */
    private String sendEmail(String to, String subject, String body) throws Exception {
        // 这里实现邮件发送功能
        // 由于是示例，仅返回模拟结果
        return "邮件发送成功\n收件人: " + to + "\n主题: " + subject + "\n正文: " + body;
    }
}
