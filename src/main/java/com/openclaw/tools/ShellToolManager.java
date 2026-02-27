package com.openclaw.tools;

import com.openclaw.model.entity.ToolInfo;
import com.openclaw.utils.ShellUtil;
import com.openclaw.utils.SSHUtils;
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

        // 注册SSH连接工具
        Map<String, String> sshConnectParams = new ConcurrentHashMap<>();
        sshConnectParams.put("host", "远程服务器地址（如：192.168.3.109）");
        sshConnectParams.put("port", "SSH端口，可选，默认22");
        sshConnectParams.put("username", "用户名");
        sshConnectParams.put("password", "密码");
        registerTool("ssh_connect", "连接到远程服务器", sshConnectParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String host = (String) parameters.get("host");
                String portStr = (String) parameters.get("port");
                String username = (String) parameters.get("username");
                String password = (String) parameters.get("password");

                int port = 22;
                if (portStr != null && !portStr.isEmpty()) {
                    port = Integer.parseInt(portStr);
                }

                SSHUtils sshUtils = SSHUtils.getInstance();
                return sshUtils.connect(host, port, username, password);
            } catch (Exception e) {
                return "连接失败: " + e.getMessage();
            }
        });

        // 注册SSH执行命令工具
        Map<String, String> sshExecuteParams = new ConcurrentHashMap<>();
        sshExecuteParams.put("host", "远程服务器地址");
        sshExecuteParams.put("port", "SSH端口，可选，默认22");
        sshExecuteParams.put("username", "用户名");
        sshExecuteParams.put("command", "要执行的命令（如：cd ~/DataCenter/ && jps）");
        registerTool("ssh_execute", "在远程服务器上执行命令", sshExecuteParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String host = (String) parameters.get("host");
                String portStr = (String) parameters.get("port");
                String username = (String) parameters.get("username");
                String command = (String) parameters.get("command");

                int port = 22;
                if (portStr != null && !portStr.isEmpty()) {
                    port = Integer.parseInt(portStr);
                }

                SSHUtils sshUtils = SSHUtils.getInstance();
                return sshUtils.execute(host, port, username, command);
            } catch (Exception e) {
                return "执行命令失败: " + e.getMessage();
            }
        });

        // 注册SSH断开连接工具
        Map<String, String> sshDisconnectParams = new ConcurrentHashMap<>();
        sshDisconnectParams.put("host", "远程服务器地址");
        sshDisconnectParams.put("port", "SSH端口，可选，默认22");
        sshDisconnectParams.put("username", "用户名");
        registerTool("ssh_disconnect", "断开SSH连接", sshDisconnectParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String host = (String) parameters.get("host");
                String portStr = (String) parameters.get("port");
                String username = (String) parameters.get("username");

                int port = 22;
                if (portStr != null && !portStr.isEmpty()) {
                    port = Integer.parseInt(portStr);
                }

                SSHUtils sshUtils = SSHUtils.getInstance();
                return sshUtils.disconnect(host, port, username);
            } catch (Exception e) {
                return "断开连接失败: " + e.getMessage();
            }
        });

        // 注册SSH列出连接工具
        Map<String, String> sshListParams = new ConcurrentHashMap<>();
        registerTool("ssh_list", "列出所有活动的SSH连接", sshListParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                SSHUtils sshUtils = SSHUtils.getInstance();
                return sshUtils.listConnections();
            } catch (Exception e) {
                return "获取连接列表失败: " + e.getMessage();
            }
        });

        // 注册压缩工具
        Map<String, String> compressParams = new ConcurrentHashMap<>();
        compressParams.put("source_path", "要压缩的文件或目录路径");
        compressParams.put("zip_path", "生成的zip文件路径");
        registerTool("compress_file", "压缩文件或目录为zip格式", compressParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String sourcePath = (String) parameters.get("source_path");
                String zipPath = (String) parameters.get("zip_path");
                if (sourcePath == null || sourcePath.isEmpty()) {
                    return "源路径不能为空。";
                }
                if (zipPath == null || zipPath.isEmpty()) {
                    return "压缩文件路径不能为空。";
                }
                return com.openclaw.utils.CompressionUtils.compress(sourcePath, zipPath);
            } catch (Exception e) {
                return "压缩失败: " + e.getMessage();
            }
        });

        // 注册解压工具
        Map<String, String> decompressParams = new ConcurrentHashMap<>();
        decompressParams.put("zip_path", "zip压缩文件路径");
        decompressParams.put("dest_path", "解压目标目录");
        decompressParams.put("overwrite", "是否覆盖已存在的文件，可选，默认false");
        registerTool("decompress_file", "解压zip压缩文件", decompressParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String zipPath = (String) parameters.get("zip_path");
                String destPath = (String) parameters.get("dest_path");
                boolean overwrite = false;
                if (parameters.containsKey("overwrite")) {
                    Object overwriteObj = parameters.get("overwrite");
                    overwrite = Boolean.parseBoolean(overwriteObj.toString());
                }
                if (zipPath == null || zipPath.isEmpty()) {
                    return "压缩文件路径不能为空。";
                }
                if (destPath == null || destPath.isEmpty()) {
                    return "目标目录不能为空。";
                }
                return com.openclaw.utils.CompressionUtils.decompress(zipPath, destPath, overwrite);
            } catch (Exception e) {
                return "解压失败: " + e.getMessage();
            }
        });

//        System.out.println("ShellToolManager初始化完成，注册了 " + toolRegistry.size() + " 个工具");
    }
}