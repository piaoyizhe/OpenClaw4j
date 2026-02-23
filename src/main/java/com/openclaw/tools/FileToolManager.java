package com.openclaw.tools;

import com.openclaw.model.entity.ToolInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件工具管理器
 * 负责管理文件操作相关的工具
 */
public class FileToolManager extends AbstractToolManager {

    /**
     * 构造方法
     */
    public FileToolManager() {
        super("file_tool_manager", "文件操作工具管理器");
    }

    /**
     * 注册默认工具
     */
    @Override
    protected void registerDefaultTools() {
        // 注册文件读取工具
        Map<String, String> readFileParams = new ConcurrentHashMap<>();
        readFileParams.put("file_path", "文件路径");
        registerTool("read_file", "读取指定文件的内容", readFileParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String filePath = (String) parameters.get("file_path");
                if (filePath == null || filePath.isEmpty()) {
                    return "文件路径不能为空。";
                }
                java.io.File file = new java.io.File(filePath);
                if (!file.exists()) {
                    return "文件不存在: " + filePath;
                }
                return new String(java.nio.file.Files.readAllBytes(file.toPath()));
            } catch (Exception e) {
                return "读取文件失败: " + e.getMessage();
            }
        });

        // 注册文件更新工具
        Map<String, String> updateFileParams = new ConcurrentHashMap<>();
        updateFileParams.put("file_path", "文件路径");
        updateFileParams.put("content", "文件内容");
        registerTool("update_file", "更新指定文件的内容", updateFileParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String filePath = (String) parameters.get("file_path");
                String content = (String) parameters.get("content");
                if (filePath == null || filePath.isEmpty()) {
                    return "文件路径不能为空。";
                }
                if (content == null) {
                    return "文件内容不能为空。";
                }
                java.io.File file = new java.io.File(filePath);
                // 确保目录存在
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                java.nio.file.Files.write(file.toPath(), content.getBytes());
                return "文件更新成功: " + filePath;
            } catch (Exception e) {
                return "更新文件失败: " + e.getMessage();
            }
        });

        // 注册文件创建工具
        Map<String, String> createFileParams = new ConcurrentHashMap<>();
        createFileParams.put("file_path", "文件路径");
        createFileParams.put("content", "文件内容");
        registerTool("create_file", "创建新文件并写入内容", createFileParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String filePath = (String) parameters.get("file_path");
                String content = (String) parameters.get("content");
                if (filePath == null || filePath.isEmpty()) {
                    return "文件路径不能为空。";
                }
                if (content == null) {
                    return "文件内容不能为空。";
                }
                java.io.File file = new java.io.File(filePath);
                // 确保目录存在
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                java.nio.file.Files.write(file.toPath(), content.getBytes());
                return "文件创建成功: " + filePath;
            } catch (Exception e) {
                return "创建文件失败: " + e.getMessage();
            }
        });

        // 注册文件删除工具
        Map<String, String> deleteFileParams = new ConcurrentHashMap<>();
        deleteFileParams.put("file_path", "文件路径");
        registerTool("delete_file", "删除指定文件", deleteFileParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String filePath = (String) parameters.get("file_path");
                if (filePath == null || filePath.isEmpty()) {
                    return "文件路径不能为空。";
                }
                java.io.File file = new java.io.File(filePath);
                if (!file.exists()) {
                    return "文件不存在: " + filePath;
                }
                if (file.delete()) {
                    return "文件删除成功: " + filePath;
                } else {
                    return "文件删除失败: " + filePath;
                }
            } catch (Exception e) {
                return "删除文件失败: " + e.getMessage();
            }
        });

        // 注册文件列表工具
        Map<String, String> listFilesParams = new ConcurrentHashMap<>();
        listFilesParams.put("directory", "目录路径");
        listFilesParams.put("recursive", "是否递归列出子目录，可选，默认为false");
        registerTool("list_files", "列出指定目录下的文件和子目录", listFilesParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String directory = (String) parameters.get("directory");
                if (directory == null || directory.isEmpty()) {
                    return "目录路径不能为空。";
                }
                boolean recursive = false;
                if (parameters.containsKey("recursive")) {
                    Object recursiveObj = parameters.get("recursive");
                    recursive = Boolean.parseBoolean(recursiveObj.toString());
                }
                java.io.File dir = new java.io.File(directory);
                if (!dir.exists() || !dir.isDirectory()) {
                    return "目录不存在: " + directory;
                }
                StringBuilder result = new StringBuilder();
                listFiles(dir, result, 0, recursive);
                return result.toString();
            } catch (Exception e) {
                return "列出文件失败: " + e.getMessage();
            }
        });

        // 注册目录创建工具
        Map<String, String> createDirParams = new ConcurrentHashMap<>();
        createDirParams.put("directory_path", "目录路径");
        registerTool("create_directory", "创建指定的目录", createDirParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String directoryPath = (String) parameters.get("directory_path");
                if (directoryPath == null || directoryPath.isEmpty()) {
                    return "目录路径不能为空。";
                }
                java.io.File directory = new java.io.File(directoryPath);
                if (directory.exists()) {
                    return "目录已存在: " + directoryPath;
                }
                if (directory.mkdirs()) {
                    return "目录创建成功: " + directoryPath;
                } else {
                    return "目录创建失败: " + directoryPath;
                }
            } catch (Exception e) {
                return "创建目录失败: " + e.getMessage();
            }
        });

        // 注册文件信息工具
        Map<String, String> fileInfoParams = new ConcurrentHashMap<>();
        fileInfoParams.put("file_path", "文件路径");
        registerTool("get_file_info", "获取指定文件的信息", fileInfoParams, (ToolInfo.ToolCaller) parameters -> {
            try {
                String filePath = (String) parameters.get("file_path");
                if (filePath == null || filePath.isEmpty()) {
                    return "文件路径不能为空。";
                }
                java.io.File file = new java.io.File(filePath);
                if (!file.exists()) {
                    return "文件不存在: " + filePath;
                }
                StringBuilder result = new StringBuilder();
                result.append("文件信息:\n");
                result.append("路径: " + file.getAbsolutePath() + "\n");
                result.append("大小: " + file.length() + " 字节\n");
                result.append("是否为文件: " + file.isFile() + "\n");
                result.append("是否为目录: " + file.isDirectory() + "\n");
                result.append("创建时间: " + new java.util.Date(file.lastModified()) + "\n");
                result.append("可读: " + file.canRead() + "\n");
                result.append("可写: " + file.canWrite() + "\n");
                result.append("可执行: " + file.canExecute() + "\n");
                return result.toString();
            } catch (Exception e) {
                return "获取文件信息失败: " + e.getMessage();
            }
        });

//        System.out.println("FileToolManager初始化完成，注册了 " + toolRegistry.size() + " 个工具");
    }

    /**
     * 递归列出目录下的文件
     * @param directory 目录
     * @param result 结果字符串
     * @param depth 深度
     * @param recursive 是否递归
     */
    private void listFiles(java.io.File directory, StringBuilder result, int depth, boolean recursive) {
        java.io.File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (java.io.File file : files) {
            // 添加缩进
            for (int i = 0; i < depth; i++) {
                result.append("  ");
            }
            // 添加文件或目录名称
            if (file.isDirectory()) {
                result.append("📁 " + file.getName() + "\n");
                // 如果递归，继续列出子目录
                if (recursive) {
                    listFiles(file, result, depth + 1, true);
                }
            } else {
                result.append("📄 " + file.getName() + " (" + file.length() + " 字节)\n");
            }
        }
    }
}
